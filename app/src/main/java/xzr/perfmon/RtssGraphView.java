package xzr.perfmon;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class RtssGraphView extends View {
    private RtssTheme theme;
    private float sizeMultiple = 1.0f;

    private float[] history = new float[0];
    private float currentMs = 16.6f;
    private float minMs = 15.0f;
    private float maxMs = 20.0f;
    private float targetMs = 16.66f; // 60 FPS standard baseline

    private final Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF bounds = new RectF();
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    public RtssGraphView(Context context) {
        super(context);
        init();
    }

    public RtssGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        bgPaint.setColor(Color.argb(200, 10, 14, 22));
        bgPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(Color.argb(90, 255, 255, 255));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1.2f);

        gridPaint.setColor(Color.argb(80, 180, 180, 180));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1.0f);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{4f, 4f}, 0));

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.LEFT);
    }

    public void setTheme(RtssTheme theme, float sizeMultiple) {
        this.theme = theme;
        this.sizeMultiple = Math.max(0.5f, sizeMultiple);

        if (theme != null) {
            linePaint.setColor(theme.valueColor);
            textPaint.setColor(theme.unitColor);
            textPaint.setTypeface(theme.typeface != null ? theme.typeface : Typeface.MONOSPACE);
        }

        float density = getResources().getDisplayMetrics().density;
        linePaint.setStrokeWidth(1.5f * density * this.sizeMultiple);
        textPaint.setTextSize(7.5f * density * this.sizeMultiple);
        textPaint.setShadowLayer(2.0f, 1.0f, 1.0f, Color.BLACK);

        requestLayout();
        invalidate();
    }

    public void updateData(float[] orderedHistory, float currentMs, float minMs, float maxMs, float targetMs) {
        if (orderedHistory != null) {
            this.history = orderedHistory;
        }
        this.currentMs = currentMs;
        this.minMs = minMs;
        this.maxMs = maxMs;
        if (targetMs > 0) {
            this.targetMs = targetMs;
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        float density = getResources().getDisplayMetrics().density;
        int defaultHeight = (int) (34 * density * sizeMultiple);
        int defaultWidth = (int) (160 * density * sizeMultiple);

        int w = resolveSize(defaultWidth, widthMeasureSpec);
        int h = resolveSize(defaultHeight, heightMeasureSpec);

        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;

        float density = getResources().getDisplayMetrics().density;
        float cornerRadius = 3.0f * density * sizeMultiple;

        bounds.set(1, 1, w - 1, h - 1);

        // 1. Draw Background & Border
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, bgPaint);
        canvas.drawRoundRect(bounds, cornerRadius, cornerRadius, borderPaint);

        // 2. Compute dynamic Y scale bounds
        // Base range: 0ms to max(33.3ms, maxMs * 1.15f)
        float minY = 0.0f;
        float maxY = Math.max(33.3f, Math.max(targetMs * 1.5f, maxMs * 1.15f));
        float rangeY = maxY - minY;
        if (rangeY <= 0) rangeY = 33.3f;

        float chartTop = 4 * density * sizeMultiple;
        float chartBottom = h - (4 * density * sizeMultiple);
        float chartHeight = chartBottom - chartTop;

        // 3. Draw Target Line (e.g. 16.6ms or 33.3ms)
        float targetY = chartBottom - ((targetMs - minY) / rangeY * chartHeight);
        if (targetY >= chartTop && targetY <= chartBottom) {
            canvas.drawLine(4, targetY, w - 4, targetY, gridPaint);
        }

        // 4. Draw Frametime Waveform
        if (history != null && history.length > 1) {
            linePath.reset();
            fillPath.reset();

            int count = history.length;
            float stepX = (float) (w - 8) / (count - 1);

            float startX = 4;
            float startY = chartBottom - ((history[0] - minY) / rangeY * chartHeight);
            startY = Math.max(chartTop, Math.min(chartBottom, startY));

            linePath.moveTo(startX, startY);
            fillPath.moveTo(startX, chartBottom);
            fillPath.lineTo(startX, startY);

            for (int i = 1; i < count; i++) {
                float x = 4 + (i * stepX);
                float val = history[i];
                float y = chartBottom - ((val - minY) / rangeY * chartHeight);
                y = Math.max(chartTop, Math.min(chartBottom, y));

                linePath.lineTo(x, y);
                fillPath.lineTo(x, y);
            }

            fillPath.lineTo(w - 4, chartBottom);
            fillPath.close();

            // Gradient fill under the curve
            int baseColor = (theme != null) ? theme.valueColor : Color.rgb(0, 229, 255);
            int topColor = Color.argb(90, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
            int bottomColor = Color.argb(10, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor));
            fillPaint.setShader(new LinearGradient(0, chartTop, 0, chartBottom, topColor, bottomColor, Shader.TileMode.CLAMP));

            canvas.drawPath(fillPath, fillPaint);
            canvas.drawPath(linePath, linePaint);
        }

        // 5. Draw Scale and Info Labels
        // Top Left: Max scale marker
        String maxLabel = String.format("%.0fms", maxY);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(maxLabel, 6, chartTop + (7 * density * sizeMultiple), textPaint);

        // Center / Top: Current frametime
        String curLabel = String.format("%.1f ms", currentMs);
        textPaint.setTextAlign(Paint.Align.CENTER);
        int curColor = (theme != null) ? theme.valueColor : Color.WHITE;
        textPaint.setColor(curColor);
        canvas.drawText(curLabel, w / 2f, chartTop + (7 * density * sizeMultiple), textPaint);

        // Bottom Right: Min / Max stats
        String statsLabel = String.format("MIN:%.1f MAX:%.1f", minMs, maxMs);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setColor((theme != null) ? theme.unitColor : Color.LTGRAY);
        canvas.drawText(statsLabel, w - 6, chartBottom - (2 * density * sizeMultiple), textPaint);
    }
}
