package xzr.perfmon;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

public class RtssTheme {
    public int labelColor;
    public int valueColor;
    public int unitColor;
    public int headerColor;
    public int offlineColor;
    public int bgColor;
    public int borderColor;
    public Typeface typeface;
    public boolean isPixelFont;

    public static RtssTheme getTheme(Context context) {
        int themeIdx = SharedPreferencesUtil.sharedPreferences.getInt(
                SharedPreferencesUtil.RTSS_THEME, SharedPreferencesUtil.DEFAULT_RTSS_THEME);
        int fontIdx = SharedPreferencesUtil.sharedPreferences.getInt(
                SharedPreferencesUtil.RTSS_FONT, SharedPreferencesUtil.DEFAULT_RTSS_FONT);
        int bgStyle = SharedPreferencesUtil.sharedPreferences.getInt(
                SharedPreferencesUtil.RTSS_BG_STYLE, SharedPreferencesUtil.DEFAULT_RTSS_BG_STYLE);

        RtssTheme theme = new RtssTheme();
        switch (themeIdx) {
            case SharedPreferencesUtil.RTSS_THEME_CLASSIC_ORANGE:
                theme.labelColor = Color.rgb(255, 128, 0);   // #FF8000
                theme.valueColor = Color.rgb(255, 128, 0);   // #FF8000
                theme.unitColor = Color.rgb(255, 167, 38);   // #FFA726
                theme.headerColor = Color.rgb(255, 128, 0);
                theme.borderColor = Color.argb(60, 255, 128, 0);
                break;
            case SharedPreferencesUtil.RTSS_THEME_GREEN:
                theme.labelColor = Color.rgb(0, 230, 118);   // #00E676
                theme.valueColor = Color.rgb(0, 230, 118);
                theme.unitColor = Color.rgb(129, 199, 132);  // #81C784
                theme.headerColor = Color.rgb(0, 230, 118);
                theme.borderColor = Color.argb(60, 0, 230, 118);
                break;
            case SharedPreferencesUtil.RTSS_THEME_ICE:
                theme.labelColor = Color.rgb(0, 229, 255);   // #00E5FF
                theme.valueColor = Color.rgb(255, 255, 255); // #FFFFFF
                theme.unitColor = Color.rgb(128, 222, 234);  // #80DEEA
                theme.headerColor = Color.rgb(0, 229, 255);
                theme.borderColor = Color.argb(60, 0, 229, 255);
                break;
            case SharedPreferencesUtil.RTSS_THEME_WHITE:
                theme.labelColor = Color.rgb(255, 255, 255);
                theme.valueColor = Color.rgb(255, 255, 255);
                theme.unitColor = Color.rgb(189, 189, 189);
                theme.headerColor = Color.rgb(255, 255, 255);
                theme.borderColor = Color.argb(60, 255, 255, 255);
                break;
            case SharedPreferencesUtil.RTSS_THEME_ORANGE_CYAN:
            default:
                // Quintessential RivaTuner / MSI Afterburner Modern OSD
                theme.labelColor = Color.rgb(255, 128, 0);   // #FF8000 RTSS Safety Orange
                theme.valueColor = Color.rgb(0, 229, 255);   // #00E5FF RTSS Vivid Cyan
                theme.unitColor = Color.rgb(128, 222, 234);  // #80DEEA RTSS Dim Cyan
                theme.headerColor = Color.rgb(255, 140, 0);  // Dark orange
                theme.borderColor = Color.argb(55, 255, 128, 0);
                break;
        }

        theme.offlineColor = Color.rgb(120, 120, 120);

        switch (bgStyle) {
            case SharedPreferencesUtil.RTSS_BG_TRANSPARENT:
                theme.bgColor = Color.TRANSPARENT;
                theme.borderColor = Color.TRANSPARENT;
                break;
            case SharedPreferencesUtil.RTSS_BG_SOLID:
                theme.bgColor = Color.rgb(10, 12, 14);
                break;
            case SharedPreferencesUtil.RTSS_BG_TRANSLUCENT:
            default:
                theme.bgColor = Color.argb(170, 10, 14, 18); // ~67% opacity dark HUD box
                break;
        }

        theme.isPixelFont = (fontIdx == SharedPreferencesUtil.RTSS_FONT_PIXEL);
        if (theme.isPixelFont) {
            try {
                theme.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/rtss_font.ttf");
            } catch (Exception e) {
                theme.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
                theme.isPixelFont = false;
            }
        } else {
            theme.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
        }

        return theme;
    }
}
