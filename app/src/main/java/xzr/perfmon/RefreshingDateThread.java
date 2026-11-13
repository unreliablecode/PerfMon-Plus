package xzr.perfmon;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class RefreshingDateThread extends Thread {
    static int cpunum;
    static int[] cpufreq;
    static int[] cpuload;
    static int[] cpuonline;
    static int[] coreMinFreq;
    static int[] coreMaxFreq;
    static int cpuAvgLoad;
    static int cpuMaxFreq;
    static int cpuActivePercent;
    static int adrenoload;
    static int adrenofreq;
    static int mincpubw;
    static int cpubw;
    static int m4m;
    static int maxtemp;
    static int batttemp;
    static int memusage;
    static int memUsedMB;
    static int memTotalMB;
    static int current;
    static int gpubw;
    static int llcbw;
    static String fps = "";

    static int delay;
    static boolean reverseCurrentNow;
    private final Context context;

    public RefreshingDateThread(Context context) {
        this.context = context;
    }

    public RefreshingDateThread() {
        this.context = null;
    }

    public void run() {
        delay = SharedPreferencesUtil.sharedPreferences.getInt(SharedPreferencesUtil.REFRESHING_DELAY, SharedPreferencesUtil.DEFAULT_DELAY);
        reverseCurrentNow = SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.REVERSE_CURRENT, SharedPreferencesUtil.REVERSE_CURRENT_DEFAULT);

        if (cpunum <= 0) {
            cpunum = JniTools.getCpuNum();
        }
        if (cpunum <= 0) {
            cpunum = Runtime.getRuntime().availableProcessors();
        }
        if (cpunum <= 0) {
            cpunum = 8;
        }

        cpufreq = new int[cpunum];
        cpuload = new int[cpunum];
        cpuonline = new int[cpunum];
        coreMinFreq = new int[cpunum];
        coreMaxFreq = new int[cpunum];

        for (int i = 0; i < cpunum; i++) {
            int maxF = JniTools.getCpuMaxFreq(i);
            int minF = JniTools.getCpuMinFreq(i);
            coreMaxFreq[i] = (maxF > 0) ? maxF : 0;
            coreMinFreq[i] = (minF > 0) ? minF : 0;
        }

        while (!FloatingWindow.doExit) {
            // Update CPU stats from /proc/stat if accessible
            int nativeOverallActive = JniTools.updateCpuStats();

            int onlineCount = 0;
            int totalLoad = 0;
            int maxFreq = 0;

            for (int i = 0; i < cpunum; i++) {
                cpuonline[i] = JniTools.getCpuOnlineStatus(i);
                if (cpuonline[i] == 1) {
                    onlineCount++;
                    int freq = JniTools.getCpuFreq(i);
                    cpufreq[i] = freq;
                    if (freq > maxFreq) {
                        maxFreq = freq;
                    }

                    int load = -1;
                    if (nativeOverallActive != Support.UNSUPPORTED) {
                        load = JniTools.getCpuLoad(i);
                    }

                    // Fallback to dynamic frequency scaling utilization
                    if (load < 0) {
                        if (freq > coreMaxFreq[i]) {
                            coreMaxFreq[i] = freq;
                        }
                        if (coreMinFreq[i] <= 0 || (freq < coreMinFreq[i] && freq > 100)) {
                            coreMinFreq[i] = freq;
                        }
                        int range = coreMaxFreq[i] - coreMinFreq[i];
                        if (range > 0 && freq >= coreMinFreq[i]) {
                            load = ((freq - coreMinFreq[i]) * 100) / range;
                        } else if (freq >= coreMaxFreq[i] && coreMaxFreq[i] > 0) {
                            load = 100;
                        } else {
                            load = 0;
                        }
                    }

                    load = Math.max(0, Math.min(100, load));
                    cpuload[i] = load;
                    totalLoad += load;
                } else {
                    cpuload[i] = 0;
                    cpufreq[i] = 0;
                }
            }

            cpuAvgLoad = onlineCount > 0 ? (totalLoad / onlineCount) : 0;
            cpuMaxFreq = maxFreq;

            if (FloatingWindow.showCpuActiveNow) {
                int act = nativeOverallActive;
                if (act < 0) {
                    act = readCpuActivePercentJava();
                }
                cpuActivePercent = (act >= 0) ? act : cpuAvgLoad;
            }

            if (FloatingWindow.showGpufreqNow && Support.support_adrenofreq)
                adrenofreq = JniTools.getAdrenoFreq();
            if (FloatingWindow.showGpuloadNow && Support.support_adrenofreq)
                adrenoload = JniTools.getAdrenoLoad();
            if (FloatingWindow.showMincpubwNow && Support.support_mincpubw)
                mincpubw = JniTools.getMinCpuBw();
            if (FloatingWindow.showCpubwNow && Support.support_cpubw)
                cpubw = JniTools.getCpuBw();
            if (FloatingWindow.showM4MNow && Support.support_m4m)
                m4m = JniTools.getM4m();
            if (FloatingWindow.showThermalNow && Support.support_temp)
                maxtemp = JniTools.getMaxTemp();
            if (FloatingWindow.showMemNow && Support.support_mem) {
                memusage = JniTools.getMemUsage();
                if (context != null) {
                    try {
                        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                        if (am != null) {
                            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                            am.getMemoryInfo(mi);
                            memTotalMB = (int) (mi.totalMem / (1024 * 1024));
                            memUsedMB = (int) ((mi.totalMem - mi.availMem) / (1024 * 1024));
                            if ((memusage <= 0 || memusage > 100) && memTotalMB > 0) {
                                memusage = (int) (((long) memUsedMB * 100) / memTotalMB);
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
            if (FloatingWindow.showCurrentNow && Support.support_current)
                current = JniTools.getCurrent();
            if (FloatingWindow.showBattTempNow && Support.support_batttemp) {
                int bTemp = -1;
                if (context != null) {
                    try {
                        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                        if (batteryIntent != null) {
                            int rawTemp = batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
                            if (rawTemp > 0) {
                                bTemp = rawTemp / 10;
                            }
                        }
                    } catch (Exception ignored) {}
                }
                if (bTemp <= 0) {
                    int nativeTemp = JniTools.getBatteryTemp();
                    if (nativeTemp != Support.UNSUPPORTED && nativeTemp > 0) {
                        bTemp = nativeTemp;
                    }
                }
                batttemp = bTemp;
            }
            if (FloatingWindow.showGpubwNow && Support.support_gpubw)
                gpubw = JniTools.getGpuBw();
            if (FloatingWindow.showLlcbwNow && Support.support_llcbw)
                llcbw = JniTools.getLlccBw();
            if (FloatingWindow.showFpsNow) {
                String rawFps = JniTools.getFps();
                fps = rawFps != null ? rawFps.trim() : "";
                if (!fps.isEmpty()) {
                    try {
                        float parsed = Float.parseFloat(fps);
                        FrameRateMonitor.getInstance().onSysfsFps(parsed);
                    } catch (Throwable ignored) {}
                }
            }
            if (reverseCurrentNow)
                current = -current;

            if (FloatingWindow.uiRefresher != null) {
                FloatingWindow.uiRefresher.sendEmptyMessage(0);
            }

            try {
                Thread.sleep(delay);
            } catch (Exception ignored) {
            }
        }
    }

    private static long prevJavaActive = 0;
    private static long prevJavaTotal = 0;

    private static int readCpuActivePercentJava() {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader("/proc/stat"))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("cpu ")) {
                String[] toks = line.trim().split("\\s+");
                if (toks.length >= 8) {
                    long user = Long.parseLong(toks[1]);
                    long nice = Long.parseLong(toks[2]);
                    long system = Long.parseLong(toks[3]);
                    long idle = Long.parseLong(toks[4]);
                    long iowait = toks.length > 5 ? Long.parseLong(toks[5]) : 0;
                    long irq = toks.length > 6 ? Long.parseLong(toks[6]) : 0;
                    long softirq = toks.length > 7 ? Long.parseLong(toks[7]) : 0;
                    long steal = toks.length > 8 ? Long.parseLong(toks[8]) : 0;

                    long active = user + nice + system + irq + softirq + steal;
                    long total = active + idle + iowait;

                    if (prevJavaTotal > 0 && total > prevJavaTotal) {
                        long dActive = active - prevJavaActive;
                        long dTotal = total - prevJavaTotal;
                        prevJavaActive = active;
                        prevJavaTotal = total;
                        if (dTotal > 0) {
                            int pct = (int) ((dActive * 100) / dTotal);
                            return Math.max(0, Math.min(100, pct));
                        }
                    }
                    prevJavaActive = active;
                    prevJavaTotal = total;
                }
            }
        } catch (Exception ignored) {}
        return -1;
    }
}
