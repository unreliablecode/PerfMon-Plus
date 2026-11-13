package xzr.perfmon;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

public class RtssFormatter {

    public static class MetricItem {
        public final String value;
        public final String unit;
        public final boolean isWarning;
        public final int minWidth;

        public MetricItem(String value, String unit) {
            this(value, unit, getDefaultWidth(unit), false);
        }

        public MetricItem(String value, String unit, boolean isWarning) {
            this(value, unit, getDefaultWidth(unit), isWarning);
        }

        public MetricItem(String value, String unit, int minWidth) {
            this(value, unit, minWidth, false);
        }

        public MetricItem(String value, String unit, int minWidth, boolean isWarning) {
            this.value = value;
            this.unit = unit;
            this.minWidth = minWidth;
            this.isWarning = isWarning;
        }

        private static int getDefaultWidth(String unit) {
            if (unit == null) return 0;
            if (unit.equals("%") || unit.equals("% ACT")) return 3;
            if (unit.equals("MHz")) return 4;
            if (unit.equals("°C")) return 3;
            if (unit.equals("MB") || unit.equals("mA")) return 5;
            if (unit.equals("FPS")) return 3;
            if (unit.equals("ms")) return 4;
            return 0;
        }
    }

    public static SpannableStringBuilder createRow(
            RtssTheme theme,
            String label,
            MetricItem... items) {
        return createRow(theme, label, 6, items);
    }

    public static SpannableStringBuilder createRow(
            RtssTheme theme,
            String label,
            int labelWidth,
            MetricItem... items) {
        SpannableStringBuilder sb = new SpannableStringBuilder();

        // 1. Label column (padded to labelWidth characters for crisp vertical lock-in)
        int lStart = sb.length();
        sb.append(padRight(label, labelWidth));
        sb.setSpan(new ForegroundColorSpan(theme.labelColor), lStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new StyleSpan(Typeface.BOLD), lStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        boolean first = true;
        for (MetricItem item : items) {
            if (item == null || item.value == null || item.value.isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append("  ");
            }
            first = false;

            // Format value right-aligned to minWidth so numbers never jitter horizontally
            String formattedValue = item.minWidth > 0 ? padLeft(item.value, item.minWidth) : item.value;

            int vStart = sb.length();
            sb.append(formattedValue);
            int vColor = item.isWarning ? theme.offlineColor : theme.valueColor;
            sb.setSpan(new ForegroundColorSpan(vColor), vStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            sb.setSpan(new StyleSpan(Typeface.BOLD), vStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            if (item.unit != null && !item.unit.isEmpty()) {
                int uStart = sb.length();
                sb.append(" ").append(item.unit);
                sb.setSpan(new ForegroundColorSpan(theme.unitColor), uStart, sb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }

        return sb;
    }

    public static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s;
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String padLeft(String s, int n) {
        if (s == null) s = "";
        if (s.length() >= n) return s;
        StringBuilder sb = new StringBuilder();
        while (sb.length() + s.length() < n) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }
}
