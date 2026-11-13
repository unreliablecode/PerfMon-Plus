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

        public MetricItem(String value, String unit) {
            this.value = value;
            this.unit = unit;
            this.isWarning = false;
        }

        public MetricItem(String value, String unit, boolean isWarning) {
            this.value = value;
            this.unit = unit;
            this.isWarning = isWarning;
        }
    }

    public static SpannableStringBuilder createRow(
            RtssTheme theme,
            String label,
            MetricItem... items) {
        SpannableStringBuilder sb = new SpannableStringBuilder();

        // Label column (padded to 6 characters for crisp alignment)
        int lStart = sb.length();
        sb.append(padRight(label, 6));
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

            int vStart = sb.length();
            sb.append(item.value);
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
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < n) {
            sb.append(' ');
        }
        return sb.toString();
    }
}
