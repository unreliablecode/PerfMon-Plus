package xzr.perfmon;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static xzr.perfmon.RefreshingDateThread.adrenofreq;
import static xzr.perfmon.RefreshingDateThread.adrenoload;
import static xzr.perfmon.RefreshingDateThread.cpuAvgLoad;
import static xzr.perfmon.RefreshingDateThread.cpuMaxFreq;
import static xzr.perfmon.RefreshingDateThread.cpufreq;
import static xzr.perfmon.RefreshingDateThread.cpuload;
import static xzr.perfmon.RefreshingDateThread.cpunum;
import static xzr.perfmon.RefreshingDateThread.cpuonline;
import static xzr.perfmon.RefreshingDateThread.cpubw;
import static xzr.perfmon.RefreshingDateThread.current;
import static xzr.perfmon.RefreshingDateThread.fps;
import static xzr.perfmon.RefreshingDateThread.gpubw;
import static xzr.perfmon.RefreshingDateThread.llcbw;
import static xzr.perfmon.RefreshingDateThread.m4m;
import static xzr.perfmon.RefreshingDateThread.maxtemp;
import static xzr.perfmon.RefreshingDateThread.batttemp;
import static xzr.perfmon.RefreshingDateThread.memTotalMB;
import static xzr.perfmon.RefreshingDateThread.memUsedMB;
import static xzr.perfmon.RefreshingDateThread.memusage;
import static xzr.perfmon.RefreshingDateThread.mincpubw;

public class FloatingWindow extends Service {
    static String TAG = "FloatingWindow";
    public static boolean doExit = true;
    static WindowManager.LayoutParams params;
    static WindowManager windowManager;
    static int statusBarHeight = -1;
    static TextView line[];
    static int linen;
    static Handler uiRefresher;
    static float sizeMultipleNow;

    static boolean showCpufreqNow;
    static boolean showCpuloadNow;
    static boolean showGpufreqNow;
    static boolean showGpuloadNow;
    static boolean showCpubwNow;
    static boolean showMincpubwNow;
    static boolean showM4MNow;
    static boolean showThermalNow;
    static boolean showMemNow;
    static boolean showCurrentNow;
    static boolean showBattTempNow;
    static boolean showGpubwNow;
    static boolean showLlcbwNow;
    static boolean showFpsNow;

    private LinearLayout main;
    private RtssTheme theme;
    private int cpuMode;
    private boolean showHeader;
    private String fpsPrefix;

    // Metric row views
    private TextView gpuLine;
    private TextView cpuGroupedLine;
    private TextView[] cpuCoreLines;
    private TextView ramLine;
    private TextView fpsLine;
    private TextView tempLine;
    private TextView currentLine;
    private TextView mincpubwLine;
    private TextView cpubwLine;
    private TextView gpubwLine;
    private TextView llcbwLine;
    private TextView m4mLine;

    private GestureDetector gestureDetector;

    @SuppressLint("ClickableViewAccessibility")
    void init() {
        sizeMultipleNow = SharedPreferencesUtil.sharedPreferences.getFloat(
                SharedPreferencesUtil.SIZE_MULTIPLE, SharedPreferencesUtil.SIZE_MULTIPLE_DEFAULT);

        showCpufreqNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_CPUFREQ, SharedPreferencesUtil.SHOW_CPUFREQ_DEFAULT);
        showCpuloadNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_CPULOAD, SharedPreferencesUtil.SHOW_CPULOAD_DEFAULT);
        showGpufreqNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_GPUFREQ, SharedPreferencesUtil.SHOW_GPUFREQ_DEFAULT);
        showGpuloadNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_GPULOAD, SharedPreferencesUtil.SHOW_GPULOAD_DEFAULT);
        showCpubwNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_CPUBW, SharedPreferencesUtil.SHOW_CPUBW_DEFAULT);
        showMincpubwNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_MINCPUBW, SharedPreferencesUtil.SHOW_MINCPUBW_DEFAULT);
        showM4MNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_M4M, SharedPreferencesUtil.SHOW_M4M_DEFAULT);
        showThermalNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_THERMAL, SharedPreferencesUtil.SHOW_THERMAL_DEFAULT);
        showMemNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_MEM, SharedPreferencesUtil.SHOW_MEM_DEFAULT);
        showCurrentNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_CURRENT, SharedPreferencesUtil.SHOW_CURRENT_DEFAULT);
        showBattTempNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_BATTTEMP, SharedPreferencesUtil.SHOW_BATTTEMP_DEFAULT);
        showGpubwNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_GPUBW, SharedPreferencesUtil.SHOW_GPUBW_DEFAULT);
        showLlcbwNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_LLCBW, SharedPreferencesUtil.SHOW_LLCBW_DEFAULT);
        showFpsNow = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.SHOW_FPS, SharedPreferencesUtil.SHOW_FPS_DEFAULT);

        theme = RtssTheme.getTheme(this);
        cpuMode = SharedPreferencesUtil.sharedPreferences.getInt(
                SharedPreferencesUtil.RTSS_CPU_MODE, SharedPreferencesUtil.DEFAULT_RTSS_CPU_MODE);
        showHeader = SharedPreferencesUtil.sharedPreferences.getBoolean(
                SharedPreferencesUtil.RTSS_SHOW_HEADER, SharedPreferencesUtil.DEFAULT_RTSS_SHOW_HEADER);
        fpsPrefix = SharedPreferencesUtil.sharedPreferences.getString(
                SharedPreferencesUtil.RTSS_FPS_LABEL, SharedPreferencesUtil.DEFAULT_RTSS_FPS_LABEL);

        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        params.y = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, getResources().getDisplayMetrics());

        int userWidth = SharedPreferencesUtil.sharedPreferences.getInt(
                SharedPreferencesUtil.WINDOW_WIDTH, SharedPreferencesUtil.DEFAULT_WIDTH);
        if (userWidth != SharedPreferencesUtil.DEFAULT_WIDTH && userWidth > 0) {
            params.width = userWidth;
        } else {
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        int userHeight = SharedPreferencesUtil.sharedPreferences.getInt(
                SharedPreferencesUtil.WINDOW_HEIGHT, SharedPreferencesUtil.DEFAULT_WINDOW_HEIGHT);
        if (userHeight != SharedPreferencesUtil.DEFAULT_WINDOW_HEIGHT && userHeight > 0) {
            params.height = userHeight;
        } else {
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        main = new LinearLayout(this);
        main.setOrientation(LinearLayout.VERTICAL);

        // RivaTuner rounded HUD background
        GradientDrawable bgDrawable = new GradientDrawable();
        bgDrawable.setColor(theme.bgColor);
        bgDrawable.setCornerRadius(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics()));
        if (theme.borderColor != Color.TRANSPARENT) {
            int strokePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
            bgDrawable.setStroke(Math.max(1, strokePx), theme.borderColor);
        }
        main.setBackground(bgDrawable);

        int padH = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
        int padV = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
        main.setPadding(padH, padV, padH, padV);

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                stopSelf();
                return true;
            }
        });

        main.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) (event.getRawX() - initialTouchX);
                        int dy = (int) (event.getRawY() - initialTouchY);
                        if (Math.abs(dx) > 6 || Math.abs(dy) > 6 || isDragging) {
                            isDragging = true;
                            params.x = initialX + dx;
                            params.y = initialY + dy;
                            try {
                                windowManager.updateViewLayout(main, params);
                            } catch (Exception ignored) {
                            }
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        return isDragging;
                }
                return false;
            }
        });

        // RivaTuner Sleek Header Bar
        if (showHeader) {
            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            headerParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
            header.setLayoutParams(headerParams);

            TextView title = new TextView(this);
            title.setText("RTSS / PERFMON");
            title.setTypeface(theme.typeface);
            title.setTextColor(theme.headerColor);
            title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9 * sizeMultipleNow);
            title.setShadowLayer(2.5f, 1.5f, 1.5f, Color.BLACK);
            title.setIncludeFontPadding(false);
            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            title.setLayoutParams(titleParams);
            header.addView(title);

            TextView closeBtn = new TextView(this);
            closeBtn.setText(" [✕] ");
            closeBtn.setTypeface(theme.typeface);
            closeBtn.setTextColor(Color.rgb(255, 82, 82));
            closeBtn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10 * sizeMultipleNow);
            closeBtn.setShadowLayer(2.5f, 1.5f, 1.5f, Color.BLACK);
            closeBtn.setIncludeFontPadding(false);
            header.addView(closeBtn);

            closeBtn.setOnClickListener(v -> stopSelf());
            closeBtn.setOnLongClickListener(v -> {
                boolean cur = SharedPreferencesUtil.sharedPreferences.getBoolean(
                        SharedPreferencesUtil.SKIP_FIRST_SCREEN, false);
                SharedPreferencesUtil.sharedPreferences.edit().putBoolean(
                        SharedPreferencesUtil.SKIP_FIRST_SCREEN, !cur).commit();
                Toast.makeText(FloatingWindow.this, cur ?
                        R.string.skip_first_screen_str_disabled : R.string.skip_first_screen_str, Toast.LENGTH_SHORT).show();
                return true;
            });

            main.addView(header);
        }

        windowManager.addView(main, params);

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
    }

    private TextView createRtssTextView() {
        TextView tv = new TextView(this);
        tv.setTypeface(theme.typeface);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12 * sizeMultipleNow);
        tv.setTextColor(theme.valueColor);
        tv.setShadowLayer(3.0f, 1.8f, 1.8f, Color.BLACK);
        tv.setIncludeFontPadding(false);
        tv.setSingleLine(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        lp.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        tv.setLayoutParams(lp);
        return tv;
    }

    void monitorInit() {
        // Build rows according to configuration
        List<TextView> allLines = new ArrayList<>();

        // 1. GPU line
        if (Support.support_adrenofreq && (showGpufreqNow || showGpuloadNow)) {
            gpuLine = createRtssTextView();
            main.addView(gpuLine);
            allLines.add(gpuLine);
        }

        // 2. CPU line(s)
        boolean showGrouped = (cpuMode == SharedPreferencesUtil.RTSS_CPU_GROUPED ||
                cpuMode == SharedPreferencesUtil.RTSS_CPU_BOTH);
        boolean showPerCore = (cpuMode == SharedPreferencesUtil.RTSS_CPU_PER_CORE ||
                cpuMode == SharedPreferencesUtil.RTSS_CPU_BOTH);

        if (showGrouped && (showCpufreqNow || showCpuloadNow || showThermalNow)) {
            cpuGroupedLine = createRtssTextView();
            main.addView(cpuGroupedLine);
            allLines.add(cpuGroupedLine);
        }

        if (showPerCore && (showCpufreqNow || showCpuloadNow) && Support.support_cpufreq) {
            int coreCount = Math.max(1, RefreshingDateThread.cpunum);
            cpuCoreLines = new TextView[coreCount];
            for (int i = 0; i < coreCount; i++) {
                cpuCoreLines[i] = createRtssTextView();
                main.addView(cpuCoreLines[i]);
                allLines.add(cpuCoreLines[i]);
            }
        }

        // 3. RAM line
        if (Support.support_mem && showMemNow) {
            ramLine = createRtssTextView();
            main.addView(ramLine);
            allLines.add(ramLine);
        }

        // 4. Standalone Temp line (when not already included on grouped CPU line)
        if (Support.support_temp && showThermalNow && !showGrouped) {
            tempLine = createRtssTextView();
            main.addView(tempLine);
            allLines.add(tempLine);
        }

        // 5. FPS line
        if (Support.support_fps && showFpsNow) {
            fpsLine = createRtssTextView();
            main.addView(fpsLine);
            allLines.add(fpsLine);
        }

        // 6. Current / Battery line
        if ((Support.support_current && showCurrentNow) || (Support.support_batttemp && showBattTempNow)) {
            currentLine = createRtssTextView();
            main.addView(currentLine);
            allLines.add(currentLine);
        }

        // 7. Devfreq / Bus lines
        if (Support.support_cpubw && showCpubwNow) {
            cpubwLine = createRtssTextView();
            main.addView(cpubwLine);
            allLines.add(cpubwLine);
        }
        if (Support.support_gpubw && showGpubwNow) {
            gpubwLine = createRtssTextView();
            main.addView(gpubwLine);
            allLines.add(gpubwLine);
        }
        if (Support.support_llcbw && showLlcbwNow) {
            llcbwLine = createRtssTextView();
            main.addView(llcbwLine);
            allLines.add(llcbwLine);
        }
        if (Support.support_mincpubw && showMincpubwNow) {
            mincpubwLine = createRtssTextView();
            main.addView(mincpubwLine);
            allLines.add(mincpubwLine);
        }
        if (Support.support_m4m && showM4MNow) {
            m4mLine = createRtssTextView();
            main.addView(m4mLine);
            allLines.add(m4mLine);
        }

        line = allLines.toArray(new TextView[0]);
        linen = line.length;

        try {
            windowManager.updateViewLayout(main, params);
        } catch (Exception ignored) {
        }

        uiRefresher = new Handler(message -> {
            // Update GPU
            if (gpuLine != null) {
                List<RtssFormatter.MetricItem> items = new ArrayList<>();
                if (showGpuloadNow) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(adrenoload), "%"));
                }
                if (showGpufreqNow) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(adrenofreq), "MHz"));
                }
                gpuLine.setText(RtssFormatter.createRow(theme, "GPU", items.toArray(new RtssFormatter.MetricItem[0])));
            }

            // Update Grouped CPU
            if (cpuGroupedLine != null) {
                List<RtssFormatter.MetricItem> items = new ArrayList<>();
                if (showThermalNow && Support.support_temp && maxtemp != Support.UNSUPPORTED && maxtemp > 0) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(maxtemp), "°C"));
                }
                if (showCpuloadNow && Support.support_cpuload) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(cpuAvgLoad), "%"));
                }
                if (showCpufreqNow && Support.support_cpufreq) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(cpuMaxFreq), "MHz"));
                }
                cpuGroupedLine.setText(RtssFormatter.createRow(theme, "CPU", items.toArray(new RtssFormatter.MetricItem[0])));
            }

            // Update Per-Core CPU
            if (cpuCoreLines != null) {
                for (int c = 0; c < cpuCoreLines.length && c < cpunum; c++) {
                    String label = "CPU" + (c + 1);
                    if (cpuonline != null && c < cpuonline.length && cpuonline[c] == 1) {
                        List<RtssFormatter.MetricItem> items = new ArrayList<>();
                        if (showCpuloadNow && Support.support_cpuload && cpuload != null && c < cpuload.length) {
                            items.add(new RtssFormatter.MetricItem(String.valueOf(cpuload[c]), "%"));
                        }
                        if (showCpufreqNow && Support.support_cpufreq && cpufreq != null && c < cpufreq.length) {
                            items.add(new RtssFormatter.MetricItem(String.valueOf(cpufreq[c]), "MHz"));
                        }
                        cpuCoreLines[c].setText(RtssFormatter.createRow(theme, label, items.toArray(new RtssFormatter.MetricItem[0])));
                    } else {
                        cpuCoreLines[c].setText(RtssFormatter.createRow(theme, label,
                                new RtssFormatter.MetricItem(getString(R.string.offline).toUpperCase(), "", true)));
                    }
                }
            }

            // Update RAM
            if (ramLine != null) {
                List<RtssFormatter.MetricItem> items = new ArrayList<>();
                if (memUsedMB > 0) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(memUsedMB), "MB"));
                }
                if (memusage >= 0) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(memusage), "%"));
                }
                ramLine.setText(RtssFormatter.createRow(theme, "RAM", items.toArray(new RtssFormatter.MetricItem[0])));
            }

            // Update Standalone Temp
            if (tempLine != null) {
                tempLine.setText(RtssFormatter.createRow(theme, "TEMP",
                        new RtssFormatter.MetricItem(String.valueOf(maxtemp), "°C")));
            }

            // Update FPS
            if (fpsLine != null) {
                String fpsVal = (fps != null && !fps.trim().isEmpty()) ? fps.trim() : "--";
                fpsLine.setText(RtssFormatter.createRow(theme, fpsPrefix,
                        new RtssFormatter.MetricItem(fpsVal, "FPS")));
            }

            // Update Current & Battery Temp
            if (currentLine != null) {
                List<RtssFormatter.MetricItem> items = new ArrayList<>();
                if (showBattTempNow && Support.support_batttemp && batttemp > 0) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(batttemp), "°C"));
                }
                if (showCurrentNow && Support.support_current) {
                    items.add(new RtssFormatter.MetricItem(String.valueOf(current), "mA"));
                }
                currentLine.setText(RtssFormatter.createRow(theme, "BAT", items.toArray(new RtssFormatter.MetricItem[0])));
            }

            // Update Bus lines
            if (cpubwLine != null) {
                cpubwLine.setText(RtssFormatter.createRow(theme, "CPUBW",
                        new RtssFormatter.MetricItem(String.valueOf(cpubw), "")));
            }
            if (gpubwLine != null) {
                gpubwLine.setText(RtssFormatter.createRow(theme, "GPUBW",
                        new RtssFormatter.MetricItem(String.valueOf(gpubw), "")));
            }
            if (llcbwLine != null) {
                llcbwLine.setText(RtssFormatter.createRow(theme, "LLCBW",
                        new RtssFormatter.MetricItem(String.valueOf(llcbw), "")));
            }
            if (mincpubwLine != null) {
                mincpubwLine.setText(RtssFormatter.createRow(theme, "MINBW",
                        new RtssFormatter.MetricItem(String.valueOf(mincpubw), "")));
            }
            if (m4mLine != null) {
                m4mLine.setText(RtssFormatter.createRow(theme, "M4M",
                        new RtssFormatter.MetricItem(String.valueOf(m4m), "MHz")));
            }

            return false;
        });

        new RefreshingDateThread(this).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        doExit = false;
        init();
        monitorInit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Calling destroy service");
        doExit = true;
        try {
            windowManager.removeView(main);
        } catch (Exception ignored) {
        }
        super.onDestroy();
    }
}
