//
// Created by xzr on 2019/6/23.
//

#include "xzr_perfmon_JniTools.h"
#include "perfmon.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <math.h>

#define MAX_SUPPORTED_CPUS 64

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuFreq
        (JNIEnv *env, jclass jclass1, jint cpu) {
    char path[DEFAULT_PATH_SIZE];
    int freq;
    sprintf(path, "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq", cpu);
    if (read_file_int(path, &freq))
        return UNSUPPORTED;

    return freq / 1000;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getAdrenoFreq
        (JNIEnv *env, jclass jclass1) {
    int freq;
    if (read_file_int("/sys/kernel/gpu/gpu_clock", &freq))
        return UNSUPPORTED;

    return freq;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getAdrenoLoad
        (JNIEnv *env, jclass jclass1) {
    int freq;
    if (read_file_int("/sys/kernel/gpu/gpu_busy", &freq))
        return UNSUPPORTED;

    return freq;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getMinCpuBw
        (JNIEnv *env, jclass jclass1) {
    int freq;

    if (read_file_int("/sys/class/devfreq/soc:qcom,mincpubw/cur_freq", &freq))
        return UNSUPPORTED;

    return freq;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuBw
        (JNIEnv *env, jclass jclass1) {
    int freq;

    if (!read_file_int("/sys/class/devfreq/soc:qcom,cpubw/cur_freq", &freq))
        return freq;

    if (!read_file_int("/sys/class/devfreq/soc:qcom,cpu-cpu-llcc-bw/cur_freq", &freq))
        return freq;

    return UNSUPPORTED;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getM4m
        (JNIEnv *env, jclass jclass1) {
    int freq;

    if (read_file_int("/sys/class/devfreq/soc:qcom,m4m/cur_freq", &freq))
        return UNSUPPORTED;

    return freq / 1000;
}

static unsigned long long prev_overall_active = 0;
static unsigned long long prev_overall_total = 0;
static int overall_cpu_load = 0;

static unsigned long long prev_core_active[MAX_SUPPORTED_CPUS];
static unsigned long long prev_core_total[MAX_SUPPORTED_CPUS];
static int prev_core_init[MAX_SUPPORTED_CPUS];
static int core_loads[MAX_SUPPORTED_CPUS];
static int proc_stat_accessible = -1; // -1 = unknown, 1 = accessible, 0 = blocked

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_updateCpuStats
        (JNIEnv *env, jclass jclass1) {
    FILE *file = fopen("/proc/stat", "r");
    if (file == NULL) {
        proc_stat_accessible = 0;
        return UNSUPPORTED;
    }
    proc_stat_accessible = 1;

    char line[256];
    while (fgets(line, sizeof(line), file)) {
        if (strncmp(line, "cpu", 3) != 0) {
            break;
        }
        if (line[3] == ' ') {
            unsigned long long u = 0, n = 0, s = 0, id = 0, io = 0, ir = 0, so = 0, st = 0;
            if (sscanf(line + 4, "%llu %llu %llu %llu %llu %llu %llu %llu",
                       &u, &n, &s, &id, &io, &ir, &so, &st) >= 4) {
                unsigned long long active = u + n + s + ir + so + st;
                unsigned long long total = active + id + io;
                if (prev_overall_total > 0 && total > prev_overall_total) {
                    unsigned long long dActive = active - prev_overall_active;
                    unsigned long long dTotal = total - prev_overall_total;
                    if (dTotal > 0) {
                        int pct = (int) ((dActive * 100) / dTotal);
                        if (pct < 0) pct = 0;
                        if (pct > 100) pct = 100;
                        overall_cpu_load = pct;
                    }
                }
                prev_overall_active = active;
                prev_overall_total = total;
            }
        } else if (line[3] >= '0' && line[3] <= '9') {
            char *endptr;
            int core = (int) strtol(line + 3, &endptr, 10);
            if (core >= 0 && core < MAX_SUPPORTED_CPUS) {
                unsigned long long u = 0, n = 0, s = 0, id = 0, io = 0, ir = 0, so = 0, st = 0;
                if (sscanf(endptr, "%llu %llu %llu %llu %llu %llu %llu %llu",
                           &u, &n, &s, &id, &io, &ir, &so, &st) >= 4) {
                    unsigned long long active = u + n + s + ir + so + st;
                    unsigned long long total = active + id + io;
                    if (prev_core_init[core] && total > prev_core_total[core]) {
                        unsigned long long dActive = active - prev_core_active[core];
                        unsigned long long dTotal = total - prev_core_total[core];
                        if (dTotal > 0) {
                            int pct = (int) ((dActive * 100) / dTotal);
                            if (pct < 0) pct = 0;
                            if (pct > 100) pct = 100;
                            core_loads[core] = pct;
                        }
                    }
                    prev_core_active[core] = active;
                    prev_core_total[core] = total;
                    prev_core_init[core] = 1;
                }
            }
        }
    }
    fclose(file);
    return overall_cpu_load;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuLoad
        (JNIEnv *env, jclass jclass1, jint cpu) {
    if (proc_stat_accessible == 0) {
        return UNSUPPORTED;
    }
    if (cpu >= 0 && cpu < MAX_SUPPORTED_CPUS) {
        if (prev_core_init[cpu]) {
            return core_loads[cpu];
        }
    }
    return UNSUPPORTED;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuOnlineStatus
        (JNIEnv *env, jclass jclass1, jint cpu) {
    int status;
    char path[DEFAULT_PATH_SIZE];

    sprintf(path, "/sys/devices/system/cpu/cpu%d/online", cpu);
    if (!read_file_int(path, &status))
        return status;

    if (cpu == 0)
        return 1;

    sprintf(path, "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_cur_freq", cpu);
    int freq = 0;
    if (!read_file_int(path, &freq) && freq > 0)
        return 1;

    return 0;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getMaxTemp
        (JNIEnv *env, jclass jclass1) {
    int temp;
    if (get_max_temp(&temp) || temp == NULLTEMP)
        return UNSUPPORTED;

    return temp;
}

JNIEXPORT jboolean JNICALL Java_xzr_perfmon_JniTools_checkCpuLoad
        (JNIEnv *env, jclass jclass1) {
    return 1;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getMemUsage
        (JNIEnv *env, jclass jclass1) {
    int total = -1;
    int available = -1;

    if (get_mem_info("MemTotal", &total))
        return UNSUPPORTED;

    if (get_mem_info("MemAvailable", &available))
        return UNSUPPORTED;

    return (int) ((1 - (float) available / total) * 100);
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCurrent
        (JNIEnv *env, jclass jclass1) {
    int current;

    if (read_file_int("/sys/class/power_supply/battery/current_now", &current))
        return UNSUPPORTED;

    return -current / 1000;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuNum
        (JNIEnv *env, jclass jclass1) {
    long nprocs = sysconf(_SC_NPROCESSORS_CONF);
    if (nprocs > 0)
        return (jint) nprocs;

    int count = 0;
    char path[DEFAULT_PATH_SIZE];
    for (int i = 0; i < 64; i++) {
        sprintf(path, "/sys/devices/system/cpu/cpu%d", i);
        if (access(path, F_OK) == 0) {
            count++;
        } else if (i > 0) {
            break;
        }
    }
    return count;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getGpuBw
        (JNIEnv *env, jclass jclass1) {
    int freq;

    if (read_file_int("/sys/class/devfreq/soc:qcom,gpubw/cur_freq", &freq))
        return UNSUPPORTED;

    return freq;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getLlccBw
        (JNIEnv *env, jclass jclass1) {
    int freq;

    if (!read_file_int("/sys/class/devfreq/soc:qcom,cpu-llcc-ddr-bw/cur_freq", &freq))
        return freq;

    if (!read_file_int("/sys/class/devfreq/soc:qcom,llccbw/cur_freq", &freq))
        return freq;

    return UNSUPPORTED;
}

JNIEXPORT jstring JNICALL Java_xzr_perfmon_JniTools_getFps
        (JNIEnv *env, jclass jclass1) {
    char fps[16] = "";

    if (!read_file_str("/sys/devices/virtual/graphics/fb0/measured_fps", fps))
        return (*env)->NewStringUTF(env, fps);

    if (!read_process_str("cat /sys/class/drm/sde-crtc-0/measured_fps | awk '{print $2}'", fps))
        return (*env)->NewStringUTF(env, fps);

    return (*env)->NewStringUTF(env, "");
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getBatteryTemp
        (JNIEnv *env, jclass jclass1) {
    int temp;
    if (!read_file_int("/sys/class/power_supply/battery/temp", &temp)) {
        if (temp > 1000) {
            return temp / 1000;
        } else if (temp > 100) {
            return temp / 10;
        }
        return temp;
    }
    if (!read_file_int("/sys/class/power_supply/bms/temp", &temp)) {
        if (temp > 1000) {
            return temp / 1000;
        } else if (temp > 100) {
            return temp / 10;
        }
        return temp;
    }
    return UNSUPPORTED;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuActivePercent
        (JNIEnv *env, jclass jclass1) {
    if (proc_stat_accessible == 0) {
        return UNSUPPORTED;
    }
    return overall_cpu_load;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuMaxFreq
        (JNIEnv *env, jclass jclass1, jint cpu) {
    char path[DEFAULT_PATH_SIZE];
    int freq;

    sprintf(path, "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_max_freq", cpu);
    if (!read_file_int(path, &freq))
        return freq / 1000;

    sprintf(path, "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_max_freq", cpu);
    if (!read_file_int(path, &freq))
        return freq / 1000;

    return UNSUPPORTED;
}

JNIEXPORT jint JNICALL Java_xzr_perfmon_JniTools_getCpuMinFreq
        (JNIEnv *env, jclass jclass1, jint cpu) {
    char path[DEFAULT_PATH_SIZE];
    int freq;

    sprintf(path, "/sys/devices/system/cpu/cpu%d/cpufreq/cpuinfo_min_freq", cpu);
    if (!read_file_int(path, &freq))
        return freq / 1000;

    sprintf(path, "/sys/devices/system/cpu/cpu%d/cpufreq/scaling_min_freq", cpu);
    if (!read_file_int(path, &freq))
        return freq / 1000;

    return UNSUPPORTED;
}
