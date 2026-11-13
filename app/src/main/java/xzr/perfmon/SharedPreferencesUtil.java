package xzr.perfmon;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
    static SharedPreferences sharedPreferences;

    static final String SKIP_FIRST_SCREEN = "skip_first_screen";
    static final boolean DEFAULT_SKIP_FIRST_SCREEN = false;

    static final String REFRESHING_DELAY = "refreshing_delay";
    static final int DEFAULT_DELAY = 1000;

    static final String WINDOW_WIDTH = "window_width";
    static final int DEFAULT_WIDTH = -1;

    static final String WINDOW_HEIGHT = "window_height";
    static final int DEFAULT_WINDOW_HEIGHT = -1;

    static final String REVERSE_CURRENT = "reverse_current";
    static final boolean REVERSE_CURRENT_DEFAULT = false;

    static final String SIZE_MULTIPLE = "size_multiple";
    static final float SIZE_MULTIPLE_DEFAULT = (float) 1.0;

    static final String SHOW_CPUFREQ = "show_cpufreq";
    static final boolean SHOW_CPUFREQ_DEFAULT = true;

    static final String SHOW_CPULOAD = "show_cpuload";
    static final boolean SHOW_CPULOAD_DEFAULT = true;

    static final String SHOW_CPU_ACTIVE = "show_cpu_active";
    static final boolean SHOW_CPU_ACTIVE_DEFAULT = true;

    static final String SHOW_GPUFREQ = "show_gpufreq";
    static final boolean SHOW_GPUFREQ_DEFAULT = true;

    static final String SHOW_GPULOAD = "show_gpuload";
    static final boolean SHOW_GPULOAD_DEFAULT = true;

    static final String SHOW_CPUBW = "show_cpubw";
    static final boolean SHOW_CPUBW_DEFAULT = true;

    static final String SHOW_MINCPUBW = "show_mincpubw";
    static final boolean SHOW_MINCPUBW_DEFAULT = false;

    static final String SHOW_M4M = "show_m4m";
    static final boolean SHOW_M4M_DEFAULT = false;

    static final String SHOW_THERMAL = "show_thermal";
    static final boolean SHOW_THERMAL_DEFAULT = true;

    static final String SHOW_MEM = "show_mem";
    static final boolean SHOW_MEM_DEFAULT = true;

    static final String SHOW_CURRENT = "show_current";
    static final boolean SHOW_CURRENT_DEFAULT = true;

    static final String SHOW_BATTTEMP = "show_batttemp";
    static final boolean SHOW_BATTTEMP_DEFAULT = true;

    static final String SHOW_GPUBW = "show_gpubw";
    static final boolean SHOW_GPUBW_DEFAULT = false;

    static final String SHOW_LLCBW = "show_llcbw";
    static final boolean SHOW_LLCBW_DEFAULT = false;

    static final String SHOW_FPS = "show_fps";
    static final boolean SHOW_FPS_DEFAULT = true;

    static final String SHOW_HARDWARE_NAMES = "show_hardware_names";
    static final boolean SHOW_HARDWARE_NAMES_DEFAULT = true;

    static final String SHOW_FRAMETIME = "show_frametime";
    static final boolean SHOW_FRAMETIME_DEFAULT = true;

    static final String SHOW_FPS_LOW_HIGH = "show_fps_low_high";
    static final boolean SHOW_FPS_LOW_HIGH_DEFAULT = true;

    static final String SHOW_FRAMETIME_GRAPH = "show_frametime_graph";
    static final boolean SHOW_FRAMETIME_GRAPH_DEFAULT = true;

    // RivaTuner (RTSS) specific settings
    static final String RTSS_THEME = "rtss_theme";
    static final int RTSS_THEME_ORANGE_CYAN = 0;
    static final int RTSS_THEME_CLASSIC_ORANGE = 1;
    static final int RTSS_THEME_GREEN = 2;
    static final int RTSS_THEME_ICE = 3;
    static final int RTSS_THEME_WHITE = 4;
    static final int DEFAULT_RTSS_THEME = RTSS_THEME_ORANGE_CYAN;

    static final String RTSS_FONT = "rtss_font";
    static final int RTSS_FONT_PIXEL = 0;
    static final int RTSS_FONT_MONOSPACE = 1;
    static final int DEFAULT_RTSS_FONT = RTSS_FONT_PIXEL;

    static final String RTSS_CPU_MODE = "rtss_cpu_mode";
    static final int RTSS_CPU_GROUPED = 0;
    static final int RTSS_CPU_PER_CORE = 1;
    static final int RTSS_CPU_BOTH = 2;
    static final int DEFAULT_RTSS_CPU_MODE = RTSS_CPU_GROUPED;

    static final String RTSS_SHOW_HEADER = "rtss_show_header";
    static final boolean DEFAULT_RTSS_SHOW_HEADER = true;

    static final String RTSS_BG_STYLE = "rtss_bg_style";
    static final int RTSS_BG_TRANSLUCENT = 0;
    static final int RTSS_BG_TRANSPARENT = 1;
    static final int RTSS_BG_SOLID = 2;
    static final int DEFAULT_RTSS_BG_STYLE = RTSS_BG_TRANSLUCENT;

    static final String RTSS_FPS_LABEL = "rtss_fps_label";
    static final String DEFAULT_RTSS_FPS_LABEL = "FPS";

    static void init(Context context) {
        sharedPreferences = context.getSharedPreferences("main", 0);
    }
}
