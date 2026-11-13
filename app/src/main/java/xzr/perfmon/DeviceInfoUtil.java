package xzr.perfmon;

import android.content.Context;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Build;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DeviceInfoUtil {
    private static final String TAG = "DeviceInfoUtil";
    private static String cachedCpuName = null;
    private static String cachedGpuName = null;

    private static final Map<String, String> SOC_MAP = new HashMap<>();

    static {
        // Qualcomm Snapdragon flagship & mid-range
        SOC_MAP.put("sm8750", "Snapdragon 8 Elite");
        SOC_MAP.put("sm8650", "Snapdragon 8 Gen 3");
        SOC_MAP.put("sm8550", "Snapdragon 8 Gen 2");
        SOC_MAP.put("sm8475", "Snapdragon 8+ Gen 1");
        SOC_MAP.put("sm8450", "Snapdragon 8 Gen 1");
        SOC_MAP.put("taro", "Snapdragon 8 Gen 1");
        SOC_MAP.put("sm8350", "Snapdragon 888");
        SOC_MAP.put("lahaina", "Snapdragon 888");
        SOC_MAP.put("sm8250-ac", "Snapdragon 870");
        SOC_MAP.put("sm8250", "Snapdragon 865");
        SOC_MAP.put("kona", "Snapdragon 865");
        SOC_MAP.put("sm8150", "Snapdragon 855");
        SOC_MAP.put("msmnile", "Snapdragon 855");
        SOC_MAP.put("sdm845", "Snapdragon 845");
        SOC_MAP.put("msm8998", "Snapdragon 835");
        SOC_MAP.put("sm7475", "Snapdragon 7+ Gen 2");
        SOC_MAP.put("sm7325", "Snapdragon 778G");
        SOC_MAP.put("yupik", "Snapdragon 778G");
        SOC_MAP.put("sm7250", "Snapdragon 765G");
        SOC_MAP.put("lito", "Snapdragon 765G");
        SOC_MAP.put("sm7150", "Snapdragon 730G");
        SOC_MAP.put("sm7125", "Snapdragon 720G");
        SOC_MAP.put("atoll", "Snapdragon 720G");
        SOC_MAP.put("sm6375", "Snapdragon 695");
        SOC_MAP.put("sm6225", "Snapdragon 680");
        SOC_MAP.put("sm6115", "Snapdragon 662");
        SOC_MAP.put("sm4350", "Snapdragon 480");

        // MediaTek Dimensity & Helio
        SOC_MAP.put("mt6989", "Dimensity 9300");
        SOC_MAP.put("mt6985", "Dimensity 9200");
        SOC_MAP.put("mt6983", "Dimensity 9000");
        SOC_MAP.put("mt6895", "Dimensity 8100");
        SOC_MAP.put("mt6893", "Dimensity 1200");
        SOC_MAP.put("mt6885", "Dimensity 1000L");
        SOC_MAP.put("mt6877", "Dimensity 900");
        SOC_MAP.put("mt6853", "Dimensity 720");
        SOC_MAP.put("mt6833", "Dimensity 700");
        SOC_MAP.put("mt6789", "Helio G99");
        SOC_MAP.put("mt6785", "Helio G90T");
        SOC_MAP.put("mt6769", "Helio G85");
        SOC_MAP.put("mt6765", "Helio P35");

        // Google Tensor
        SOC_MAP.put("zuma", "Google Tensor G3");
        SOC_MAP.put("cloudripper", "Google Tensor G2");
        SOC_MAP.put("whitechapel", "Google Tensor");
        SOC_MAP.put("gs101", "Google Tensor");
        SOC_MAP.put("gs201", "Google Tensor G2");

        // Samsung Exynos
        SOC_MAP.put("s5e9925", "Exynos 2200");
        SOC_MAP.put("s5e9840", "Exynos 2100");
        SOC_MAP.put("s5e9830", "Exynos 990");
        SOC_MAP.put("s5e9820", "Exynos 9820");
        SOC_MAP.put("s5e9810", "Exynos 9810");
        SOC_MAP.put("s5e8835", "Exynos 1380");
        SOC_MAP.put("s5e8825", "Exynos 1280");
    }

    public static synchronized String getCpuName(Context context) {
        if (cachedCpuName != null) {
            return cachedCpuName;
        }

        String rawModel = "";

        // 1. Android 12+ Build.SOC_MODEL
        if (Build.VERSION.SDK_INT >= 31) {
            try {
                rawModel = Build.SOC_MODEL;
            } catch (Throwable ignored) {}
        }

        // 2. Read SystemProperties: ro.soc.model, ro.board.platform, ro.hardware
        if (rawModel == null || rawModel.trim().isEmpty()) {
            rawModel = getSystemProperty("ro.soc.model");
        }
        if (rawModel == null || rawModel.trim().isEmpty()) {
            rawModel = getSystemProperty("ro.board.platform");
        }
        if (rawModel == null || rawModel.trim().isEmpty()) {
            rawModel = Build.HARDWARE;
        }

        // 3. Parse /proc/cpuinfo Hardware line
        String cpuinfoHardware = readCpuinfoHardware();
        if (cpuinfoHardware != null && !cpuinfoHardware.isEmpty()) {
            if (rawModel == null || rawModel.isEmpty() || rawModel.equalsIgnoreCase("qcom") || rawModel.equalsIgnoreCase("mt6765")) {
                rawModel = cpuinfoHardware;
            }
        }

        // 4. Map or clean the model name
        cachedCpuName = resolveFriendlySocName(rawModel);
        if (cachedCpuName == null || cachedCpuName.isEmpty()) {
            cachedCpuName = Build.HARDWARE != null ? Build.HARDWARE.toUpperCase() : "CPU";
        }

        return cachedCpuName;
    }

    public static synchronized String getGpuName(Context context) {
        if (cachedGpuName != null) {
            return cachedGpuName;
        }

        // 1. Try querying OpenGL ES GL_RENDERER via off-screen EGL Pbuffer
        try {
            EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] version = new int[2];
            EGL14.eglInitialize(display, version, 0, version, 1);
            int[] attribList = {
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] numConfigs = new int[1];
            EGL14.eglChooseConfig(display, attribList, 0, configs, 0, 1, numConfigs, 0);
            if (numConfigs[0] > 0) {
                int[] contextAttribs = {
                        EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                        EGL14.EGL_NONE
                };
                EGLContext eglContext = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttribs, 0);
                int[] surfaceAttribs = {
                        EGL14.EGL_WIDTH, 1,
                        EGL14.EGL_HEIGHT, 1,
                        EGL14.EGL_NONE
                };
                EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttribs, 0);
                EGL14.eglMakeCurrent(display, surface, surface, eglContext);

                String renderer = GLES20.glGetString(GLES20.GL_RENDERER);
                if (renderer != null && !renderer.trim().isEmpty()) {
                    cachedGpuName = renderer.replace("(TM)", "").replace("(R)", "").trim();
                }

                EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                EGL14.eglDestroySurface(display, surface);
                EGL14.eglDestroyContext(display, eglContext);
                EGL14.eglTerminate(display);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Failed to get GPU name from EGL: " + t.getMessage());
        }

        // 2. Sysfs fallback
        if (cachedGpuName == null || cachedGpuName.isEmpty()) {
            cachedGpuName = readSysfsGpuModel();
        }

        // 3. Fallback default
        if (cachedGpuName == null || cachedGpuName.isEmpty()) {
            cachedGpuName = "Adreno / Mali GPU";
        }

        return cachedGpuName;
    }

    private static String resolveFriendlySocName(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "CPU";
        }
        String clean = raw.trim();

        // Check if raw string contains Qualcomm prefix
        if (clean.toLowerCase().contains("qualcomm technologies, inc ")) {
            clean = clean.substring("qualcomm technologies, inc ".length()).trim();
        }

        String key = clean.toLowerCase().replace("-", "").replace("_", "");
        for (Map.Entry<String, String> entry : SOC_MAP.entrySet()) {
            String mapKey = entry.getKey().toLowerCase().replace("-", "").replace("_", "");
            if (key.equals(mapKey) || key.contains(mapKey)) {
                return entry.getValue();
            }
        }

        // Check Build.BOARD as fallback key
        if (Build.BOARD != null) {
            String boardKey = Build.BOARD.toLowerCase().replace("-", "").replace("_", "");
            if (SOC_MAP.containsKey(boardKey)) {
                return SOC_MAP.get(boardKey);
            }
        }

        // Format common chips nicely
        if (clean.toUpperCase().startsWith("SM") || clean.toUpperCase().startsWith("SDM") || clean.toUpperCase().startsWith("MSM")) {
            return "Snapdragon " + clean.toUpperCase();
        }
        if (clean.toUpperCase().startsWith("MT")) {
            return "MediaTek " + clean.toUpperCase();
        }

        return clean;
    }

    private static String readCpuinfoHardware() {
        try (BufferedReader br = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Hardware")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length > 1) {
                        return parts[1].trim();
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static String readSysfsGpuModel() {
        String[] paths = {
                "/sys/class/kgsl/kgsl-3d0/gpu_model",
                "/sys/kernel/gpu/gpu_model",
                "/sys/devices/platform/gpusysfs/gpu_model"
        };
        for (String path : paths) {
            File f = new File(path);
            if (f.exists() && f.canRead()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line = br.readLine();
                    if (line != null && !line.trim().isEmpty()) {
                        return line.trim();
                    }
                } catch (Throwable ignored) {}
            }
        }
        return null;
    }

    private static String getSystemProperty(String key) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method getMethod = clazz.getMethod("get", String.class);
            return (String) getMethod.invoke(null, key);
        } catch (Throwable ignored) {
            return null;
        }
    }
}
