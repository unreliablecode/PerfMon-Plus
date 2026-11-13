package xzr.perfmon;

import android.app.ActivityManager;
import android.content.Context;

public class RefreshingDateThread extends Thread {
    static int cpunum;
    static int[] cpufreq;
    static int[] cpuload;
    static int[] cpuonline;
    static int cpuAvgLoad;
    static int cpuMaxFreq;
    static int adrenoload;
    static int adrenofreq;
    static int mincpubw;
    static int cpubw;
    static int m4m;
    static int maxtemp;
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
        cpufreq = new int[cpunum];
        cpuload = new int[cpunum];
        cpuonline = new int[cpunum];
        while (!FloatingWindow.doExit) {
            int onlineCount = 0;
            int totalLoad = 0;
            int maxFreq = 0;

            for (int i = 0; i < cpunum; i++) {
                cpuonline[i] = JniTools.getCpuOnlineStatus(i);
                if (cpuonline[i] == 1) {
                    onlineCount++;
                    if (FloatingWindow.showCpufreqNow) {
                        cpufreq[i] = JniTools.getCpuFreq(i);
                        if (cpufreq[i] > maxFreq) {
                            maxFreq = cpufreq[i];
                        }
                    }
                    totalLoad += cpuload[i];
                }
            }
            cpuAvgLoad = onlineCount > 0 ? (totalLoad / onlineCount) : 0;
            cpuMaxFreq = maxFreq;

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
            if (FloatingWindow.showGpubwNow && Support.support_gpubw)
                gpubw = JniTools.getGpuBw();
            if (FloatingWindow.showLlcbwNow && Support.support_llcbw)
                llcbw = JniTools.getLlccBw();
            if (FloatingWindow.showFpsNow && Support.support_fps) {
                String rawFps = JniTools.getFps();
                fps = rawFps != null ? rawFps.trim() : "";
            }
            if (reverseCurrentNow)
                current = -current;

            if (FloatingWindow.uiRefresher != null) {
                FloatingWindow.uiRefresher.sendEmptyMessage(0);
            }

            for (int i = 0; i < cpunum; i++) {
                if (cpuonline[i] == 1 && FloatingWindow.showCpuloadNow && Support.support_cpuload) {
                    final int ii = i;
                    new Thread() {
                        public void run() {
                            cpuload[ii] = JniTools.getCpuLoad(ii);
                        }
                    }.start();
                }
            }
            try {
                Thread.sleep(delay);
            } catch (Exception ignored) {
            }
        }
    }
}
