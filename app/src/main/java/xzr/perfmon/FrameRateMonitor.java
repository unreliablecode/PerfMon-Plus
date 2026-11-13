package xzr.perfmon;

import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;

import java.util.Arrays;

public class FrameRateMonitor implements Choreographer.FrameCallback {
    public static final int HISTORY_CAPACITY = 80;

    private static FrameRateMonitor instance;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    private long lastFrameTimeNanos = 0;
    private final float[] history = new float[HISTORY_CAPACITY];
    private int historyCount = 0;
    private int historyIndex = 0;

    private float currentFps = 0.0f;
    private float currentFrametimeMs = 0.0f;
    private float lowFps = 0.0f;
    private float highFps = 0.0f;
    private float minFrametimeMs = 0.0f;
    private float maxFrametimeMs = 0.0f;

    // Window for FPS calculation from frame counts
    private int frameCounter = 0;
    private long lastFpsCalcTime = 0;

    public static synchronized FrameRateMonitor getInstance() {
        if (instance == null) {
            instance = new FrameRateMonitor();
        }
        return instance;
    }

    private FrameRateMonitor() {
        Arrays.fill(history, 16.6f);
    }

    public synchronized void start() {
        if (isRunning) return;
        isRunning = true;
        mainHandler.post(() -> Choreographer.getInstance().postFrameCallback(this));
    }

    public synchronized void stop() {
        isRunning = false;
        mainHandler.post(() -> Choreographer.getInstance().removeFrameCallback(this));
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning) return;

        if (lastFrameTimeNanos > 0) {
            long deltaNs = frameTimeNanos - lastFrameTimeNanos;
            float dtMs = deltaNs / 1_000_000.0f;

            if (dtMs > 1.0f && dtMs < 500.0f) {
                recordSample(dtMs);
            }
        }
        lastFrameTimeNanos = frameTimeNanos;

        frameCounter++;
        long now = System.currentTimeMillis();
        if (now - lastFpsCalcTime >= 1000) {
            float calculatedFps = (frameCounter * 1000.0f) / (now - lastFpsCalcTime);
            if (currentFps <= 0.0f || Math.abs(currentFps - calculatedFps) > 1.0f) {
                currentFps = Math.round(calculatedFps * 10.0f) / 10.0f;
            }
            frameCounter = 0;
            lastFpsCalcTime = now;
        }

        if (isRunning) {
            Choreographer.getInstance().postFrameCallback(this);
        }
    }

    /**
     * Called when sysfs measured_fps provides an update
     */
    public synchronized void onSysfsFps(float fps) {
        if (fps > 1.0f && fps < 360.0f) {
            this.currentFps = fps;
            float dtMs = 1000.0f / fps;
            recordSample(dtMs);
        }
    }

    private synchronized void recordSample(float dtMs) {
        currentFrametimeMs = Math.round(dtMs * 10.0f) / 10.0f;

        history[historyIndex] = dtMs;
        historyIndex = (historyIndex + 1) % HISTORY_CAPACITY;
        if (historyCount < HISTORY_CAPACITY) {
            historyCount++;
        }

        recomputeStats();
    }

    private void recomputeStats() {
        if (historyCount == 0) return;

        float[] copy = new float[historyCount];
        for (int i = 0; i < historyCount; i++) {
            copy[i] = history[i];
        }

        float minFt = copy[0];
        float maxFt = copy[0];
        for (float v : copy) {
            if (v < minFt) minFt = v;
            if (v > maxFt) maxFt = v;
        }
        minFrametimeMs = Math.round(minFt * 10.0f) / 10.0f;
        maxFrametimeMs = Math.round(maxFt * 10.0f) / 10.0f;

        // Sort copy to calculate 1% Low and High
        Arrays.sort(copy);

        // Max FPS comes from min frametime
        if (minFt > 0) {
            highFps = Math.min(240.0f, Math.round((1000.0f / minFt) * 10.0f) / 10.0f);
        }

        // 1% Low FPS comes from 99th percentile frametime (spikes)
        int p99Index = (int) Math.ceil(historyCount * 0.99f) - 1;
        if (p99Index < 0) p99Index = 0;
        if (p99Index >= historyCount) p99Index = historyCount - 1;
        float p99Frametime = copy[p99Index];
        if (p99Frametime > 0) {
            lowFps = Math.max(1.0f, Math.round((1000.0f / p99Frametime) * 10.0f) / 10.0f);
        }
    }

    public synchronized float getCurrentFps() {
        return currentFps;
    }

    public synchronized float getCurrentFrametimeMs() {
        if (currentFrametimeMs > 0) {
            return currentFrametimeMs;
        }
        if (currentFps > 0) {
            return Math.round((1000.0f / currentFps) * 10.0f) / 10.0f;
        }
        return 16.6f;
    }

    public synchronized float getLowFps() {
        return lowFps > 0 ? lowFps : Math.max(0, currentFps - 5);
    }

    public synchronized float getHighFps() {
        return highFps > 0 ? highFps : currentFps;
    }

    public synchronized float getMinFrametimeMs() {
        return minFrametimeMs > 0 ? minFrametimeMs : 15.0f;
    }

    public synchronized float getMaxFrametimeMs() {
        return maxFrametimeMs > 0 ? maxFrametimeMs : 25.0f;
    }

    /**
     * Returns an ordered array of history samples from oldest to newest
     */
    public synchronized float[] getOrderedHistory() {
        int count = Math.max(1, historyCount);
        float[] ordered = new float[count];
        int start = (historyIndex - historyCount + HISTORY_CAPACITY) % HISTORY_CAPACITY;
        for (int i = 0; i < count; i++) {
            ordered[i] = history[(start + i) % HISTORY_CAPACITY];
        }
        return ordered;
    }
}
