package xzr.perfmon;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

class Settings {
    private static AlertDialog dialog;

    static void createDialog(Context context) {
        dialog = new AlertDialog.Builder(context)
                .setView(settingsView(context))
                .setTitle(R.string.settings)
                .create();
        dialog.show();
    }

    private static View settingsView(final Context context) {
        ScrollView scrollView = new ScrollView(context);
        LinearLayout linearLayout = new LinearLayout(context);
        scrollView.addView(linearLayout);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        int pad = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
        linearLayout.setPadding(pad, pad, pad, pad);

        // ════════════════════════════════════════════════════════════
        // RIVATUNER (RTSS) OSD SETTINGS SECTION
        // ════════════════════════════════════════════════════════════
        {
            TextView rtssHeader = new TextView(context);
            rtssHeader.setText(R.string.rtss_settings_header);
            rtssHeader.setTextColor(context.getResources().getColor(R.color.rtss_orange));
            rtssHeader.setTypeface(null, Typeface.BOLD);
            rtssHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            linearLayout.addView(rtssHeader);

            View sep = new View(context);
            sep.setBackgroundColor(context.getResources().getColor(R.color.rtss_orange));
            LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 3);
            sepParams.topMargin = 6;
            sepParams.bottomMargin = 14;
            linearLayout.addView(sep, sepParams);
        }

        // 1. Color Palette Selector
        {
            final String[] themes = new String[]{
                    context.getString(R.string.rtss_theme_modern),
                    context.getString(R.string.rtss_theme_classic),
                    context.getString(R.string.rtss_theme_green),
                    context.getString(R.string.rtss_theme_ice),
                    context.getString(R.string.rtss_theme_white)
            };

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(row);

            TextView label = new TextView(context);
            label.setText(R.string.rtss_theme_title);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            row.addView(label);

            final Button btn = new Button(context);
            int curTheme = SharedPreferencesUtil.sharedPreferences.getInt(
                    SharedPreferencesUtil.RTSS_THEME, SharedPreferencesUtil.DEFAULT_RTSS_THEME);
            if (curTheme < 0 || curTheme >= themes.length) curTheme = 0;
            btn.setText(themes[curTheme]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int sel = SharedPreferencesUtil.sharedPreferences.getInt(
                            SharedPreferencesUtil.RTSS_THEME, SharedPreferencesUtil.DEFAULT_RTSS_THEME);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.rtss_theme_title)
                            .setSingleChoiceItems(themes, sel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    SharedPreferencesUtil.sharedPreferences.edit().putInt(
                                            SharedPreferencesUtil.RTSS_THEME, which).commit();
                                    btn.setText(themes[which]);
                                    d.dismiss();
                                }
                            })
                            .create().show();
                }
            });
            row.addView(btn);
        }

        // 2. Font Style Selector
        {
            final String[] fonts = new String[]{
                    context.getString(R.string.rtss_font_pixel),
                    context.getString(R.string.rtss_font_mono)
            };

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(row);

            TextView label = new TextView(context);
            label.setText(R.string.rtss_font_title);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            row.addView(label);

            final Button btn = new Button(context);
            int curFont = SharedPreferencesUtil.sharedPreferences.getInt(
                    SharedPreferencesUtil.RTSS_FONT, SharedPreferencesUtil.DEFAULT_RTSS_FONT);
            if (curFont < 0 || curFont >= fonts.length) curFont = 0;
            btn.setText(fonts[curFont]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int sel = SharedPreferencesUtil.sharedPreferences.getInt(
                            SharedPreferencesUtil.RTSS_FONT, SharedPreferencesUtil.DEFAULT_RTSS_FONT);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.rtss_font_title)
                            .setSingleChoiceItems(fonts, sel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    SharedPreferencesUtil.sharedPreferences.edit().putInt(
                                            SharedPreferencesUtil.RTSS_FONT, which).commit();
                                    btn.setText(fonts[which]);
                                    d.dismiss();
                                }
                            })
                            .create().show();
                }
            });
            row.addView(btn);
        }

        // 3. CPU Layout Selector
        {
            final String[] cpuModes = new String[]{
                    context.getString(R.string.rtss_cpu_grouped),
                    context.getString(R.string.rtss_cpu_per_core),
                    context.getString(R.string.rtss_cpu_both)
            };

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(row);

            TextView label = new TextView(context);
            label.setText(R.string.rtss_cpu_mode_title);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            row.addView(label);

            final Button btn = new Button(context);
            int curCpu = SharedPreferencesUtil.sharedPreferences.getInt(
                    SharedPreferencesUtil.RTSS_CPU_MODE, SharedPreferencesUtil.DEFAULT_RTSS_CPU_MODE);
            if (curCpu < 0 || curCpu >= cpuModes.length) curCpu = 0;
            btn.setText(cpuModes[curCpu]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int sel = SharedPreferencesUtil.sharedPreferences.getInt(
                            SharedPreferencesUtil.RTSS_CPU_MODE, SharedPreferencesUtil.DEFAULT_RTSS_CPU_MODE);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.rtss_cpu_mode_title)
                            .setSingleChoiceItems(cpuModes, sel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    SharedPreferencesUtil.sharedPreferences.edit().putInt(
                                            SharedPreferencesUtil.RTSS_CPU_MODE, which).commit();
                                    btn.setText(cpuModes[which]);
                                    d.dismiss();
                                }
                            })
                            .create().show();
                }
            });
            row.addView(btn);
        }

        // 4. Background Style Selector
        {
            final String[] bgStyles = new String[]{
                    context.getString(R.string.rtss_bg_translucent),
                    context.getString(R.string.rtss_bg_transparent),
                    context.getString(R.string.rtss_bg_solid)
            };

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(row);

            TextView label = new TextView(context);
            label.setText(R.string.rtss_bg_style_title);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            row.addView(label);

            final Button btn = new Button(context);
            int curBg = SharedPreferencesUtil.sharedPreferences.getInt(
                    SharedPreferencesUtil.RTSS_BG_STYLE, SharedPreferencesUtil.DEFAULT_RTSS_BG_STYLE);
            if (curBg < 0 || curBg >= bgStyles.length) curBg = 0;
            btn.setText(bgStyles[curBg]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int sel = SharedPreferencesUtil.sharedPreferences.getInt(
                            SharedPreferencesUtil.RTSS_BG_STYLE, SharedPreferencesUtil.DEFAULT_RTSS_BG_STYLE);
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.rtss_bg_style_title)
                            .setSingleChoiceItems(bgStyles, sel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    SharedPreferencesUtil.sharedPreferences.edit().putInt(
                                            SharedPreferencesUtil.RTSS_BG_STYLE, which).commit();
                                    btn.setText(bgStyles[which]);
                                    d.dismiss();
                                }
                            })
                            .create().show();
                }
            });
            row.addView(btn);
        }

        // 5. FPS Prefix Selector
        {
            final String[] prefixes = new String[]{"FPS", "D3D11", "VULKAN", "OGL"};

            LinearLayout row = new LinearLayout(context);
            row.setOrientation(LinearLayout.VERTICAL);
            linearLayout.addView(row);

            TextView label = new TextView(context);
            label.setText(R.string.rtss_fps_label_title);
            label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            row.addView(label);

            final Button btn = new Button(context);
            String curP = SharedPreferencesUtil.sharedPreferences.getString(
                    SharedPreferencesUtil.RTSS_FPS_LABEL, SharedPreferencesUtil.DEFAULT_RTSS_FPS_LABEL);
            btn.setText(curP);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cur = SharedPreferencesUtil.sharedPreferences.getString(
                            SharedPreferencesUtil.RTSS_FPS_LABEL, SharedPreferencesUtil.DEFAULT_RTSS_FPS_LABEL);
                    int sel = 0;
                    for (int i = 0; i < prefixes.length; i++) {
                        if (prefixes[i].equals(cur)) {
                            sel = i;
                            break;
                        }
                    }
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.rtss_fps_label_title)
                            .setSingleChoiceItems(prefixes, sel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface d, int which) {
                                    SharedPreferencesUtil.sharedPreferences.edit().putString(
                                            SharedPreferencesUtil.RTSS_FPS_LABEL, prefixes[which]).commit();
                                    btn.setText(prefixes[which]);
                                    d.dismiss();
                                }
                            })
                            .create().show();
                }
            });
            row.addView(btn);
        }

        // 6. Show Title Bar Switch
        {
            final Switch sw = new Switch(context);
            linearLayout.addView(sw);
            sw.setText(R.string.rtss_show_header);
            if (SharedPreferencesUtil.sharedPreferences.getBoolean(
                    SharedPreferencesUtil.RTSS_SHOW_HEADER, SharedPreferencesUtil.DEFAULT_RTSS_SHOW_HEADER))
                sw.setChecked(true);
            sw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferencesUtil.sharedPreferences.edit().putBoolean(
                            SharedPreferencesUtil.RTSS_SHOW_HEADER, sw.isChecked()).commit();
                }
            });
        }

        // ════════════════════════════════════════════════════════════
        // GENERAL SETTINGS SECTION
        // ════════════════════════════════════════════════════════════
        {
            TextView generalHeader = new TextView(context);
            generalHeader.setText("\nGeneral Settings");
            generalHeader.setTextColor(Color.DKGRAY);
            generalHeader.setTypeface(null, Typeface.BOLD);
            generalHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            linearLayout.addView(generalHeader);

            View sep = new View(context);
            sep.setBackgroundColor(Color.LTGRAY);
            LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2);
            sepParams.topMargin = 4;
            sepParams.bottomMargin = 10;
            linearLayout.addView(sep, sepParams);
        }

        {
            final Switch sw = new Switch(context);
            linearLayout.addView(sw);
            sw.setText(R.string.skip_first_screen_str);
            if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SKIP_FIRST_SCREEN, SharedPreferencesUtil.DEFAULT_SKIP_FIRST_SCREEN))
                sw.setChecked(true);
            sw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sw.isChecked()) {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.notice)
                                .setMessage(R.string.skip_first_screen_str2)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SKIP_FIRST_SCREEN, true).commit();
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        sw.setChecked(false);
                                    }
                                })
                                .create().show();
                    } else {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SKIP_FIRST_SCREEN, false).commit();
                    }
                }
            });
        }

        {
            LinearLayout line = new LinearLayout(context);
            linearLayout.addView(line);

            TextView textView = new TextView(context);
            line.addView(textView);
            textView.setText(R.string.refresh_interval);

            EditText editText = new EditText(context);
            line.addView(editText);
            editText.setHint(R.string.default_value);
            editText.setText(SharedPreferencesUtil.sharedPreferences.getInt(SharedPreferencesUtil.REFRESHING_DELAY, SharedPreferencesUtil.DEFAULT_DELAY) + "");
            editText.setWidth(500);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        SharedPreferencesUtil.sharedPreferences.edit().putInt(SharedPreferencesUtil.REFRESHING_DELAY, Integer.parseInt(editable.toString())).commit();
                    } catch (Exception e) {
                        SharedPreferencesUtil.sharedPreferences.edit().remove(SharedPreferencesUtil.REFRESHING_DELAY).commit();
                    }
                }
            });
        }

        {
            TextView textView = new TextView(context);
            linearLayout.addView(textView);
            textView.setText(R.string.interval_notice);
        }

        {
            final Switch sw = new Switch(context);
            linearLayout.addView(sw);
            sw.setText(R.string.reverse_current);
            if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.REVERSE_CURRENT, SharedPreferencesUtil.REVERSE_CURRENT_DEFAULT))
                sw.setChecked(true);
            sw.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sw.isChecked()) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.REVERSE_CURRENT, true).commit();
                    } else {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.REVERSE_CURRENT, false).commit();
                    }
                }
            });
        }

        {
            LinearLayout line = new LinearLayout(context);
            linearLayout.addView(line);

            TextView textView = new TextView(context);
            line.addView(textView);
            textView.setText(R.string.size_multiple);

            EditText editText = new EditText(context);
            line.addView(editText);
            editText.setHint(R.string.default_value);
            editText.setText(SharedPreferencesUtil.sharedPreferences.getFloat(SharedPreferencesUtil.SIZE_MULTIPLE, SharedPreferencesUtil.SIZE_MULTIPLE_DEFAULT) + "");
            editText.setWidth(500);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        SharedPreferencesUtil.sharedPreferences.edit().putFloat(SharedPreferencesUtil.SIZE_MULTIPLE, Float.parseFloat(editable.toString())).commit();
                    } catch (Exception e) {
                        SharedPreferencesUtil.sharedPreferences.edit().remove(SharedPreferencesUtil.SIZE_MULTIPLE).commit();
                    }
                }
            });
        }

        // ════════════════════════════════════════════════════════════
        // MONITOR TOGGLES SECTION
        // ════════════════════════════════════════════════════════════
        {
            TextView textView = new TextView(context);
            linearLayout.addView(textView);
            textView.setText("\n" + context.getString(R.string.moni_ctl));
            textView.setTypeface(null, Typeface.BOLD);
            textView.setTextColor(Color.DKGRAY);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);

            View sep = new View(context);
            sep.setBackgroundColor(Color.LTGRAY);
            LinearLayout.LayoutParams sepParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2);
            sepParams.topMargin = 4;
            sepParams.bottomMargin = 10;
            linearLayout.addView(sep, sepParams);

            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_cpufreq);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_CPUFREQ, SharedPreferencesUtil.SHOW_CPUFREQ_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_CPUFREQ, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_cpuload);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_CPULOAD, SharedPreferencesUtil.SHOW_CPULOAD_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_CPULOAD, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_gpufreq);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_GPUFREQ, SharedPreferencesUtil.SHOW_GPUFREQ_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_GPUFREQ, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_gpuload);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_GPULOAD, SharedPreferencesUtil.SHOW_GPULOAD_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_GPULOAD, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_cpubw);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_CPUBW, SharedPreferencesUtil.SHOW_CPUBW_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_CPUBW, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_mincpubw);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_MINCPUBW, SharedPreferencesUtil.SHOW_MINCPUBW_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_MINCPUBW, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_gpubw);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_GPUBW, SharedPreferencesUtil.SHOW_GPUBW_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_GPUBW, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_llcbw);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_LLCBW, SharedPreferencesUtil.SHOW_LLCBW_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_LLCBW, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_m4m);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_M4M, SharedPreferencesUtil.SHOW_M4M_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_M4M, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_thermal);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_THERMAL, SharedPreferencesUtil.SHOW_THERMAL_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_THERMAL, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_mem);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_MEM, SharedPreferencesUtil.SHOW_MEM_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_MEM, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_current);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_CURRENT, SharedPreferencesUtil.SHOW_CURRENT_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_CURRENT, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_batttemp);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_BATTTEMP, SharedPreferencesUtil.SHOW_BATTTEMP_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_BATTTEMP, sw.isChecked()).commit();
                    }
                });
            }
            {
                final Switch sw = new Switch(context);
                linearLayout.addView(sw);
                sw.setText(R.string.show_fps);
                if (SharedPreferencesUtil.sharedPreferences.getBoolean(SharedPreferencesUtil.SHOW_FPS, SharedPreferencesUtil.SHOW_FPS_DEFAULT))
                    sw.setChecked(true);
                sw.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SharedPreferencesUtil.sharedPreferences.edit().putBoolean(SharedPreferencesUtil.SHOW_FPS, sw.isChecked()).commit();
                    }
                });
            }
        }

        // Window size overrides (optional)
        {
            LinearLayout line = new LinearLayout(context);
            linearLayout.addView(line);

            TextView textView = new TextView(context);
            line.addView(textView);
            textView.setText(R.string.window_width);
            EditText editText = new EditText(context);
            line.addView(editText);
            editText.setHint(R.string.default_value);
            int width = SharedPreferencesUtil.sharedPreferences.getInt(SharedPreferencesUtil.WINDOW_WIDTH, SharedPreferencesUtil.DEFAULT_WIDTH);
            if (width != -1)
                editText.setText(width + "");
            editText.setWidth(500);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        SharedPreferencesUtil.sharedPreferences.edit().putInt(SharedPreferencesUtil.WINDOW_WIDTH, Integer.parseInt(editable.toString())).commit();
                    } catch (Exception e) {
                        SharedPreferencesUtil.sharedPreferences.edit().remove(SharedPreferencesUtil.WINDOW_WIDTH).commit();
                    }
                }
            });
        }

        {
            LinearLayout line = new LinearLayout(context);
            linearLayout.addView(line);

            TextView textView = new TextView(context);
            line.addView(textView);
            textView.setText(R.string.window_height);

            EditText editText = new EditText(context);
            line.addView(editText);
            editText.setHint(R.string.default_value);
            int height = SharedPreferencesUtil.sharedPreferences.getInt(SharedPreferencesUtil.WINDOW_HEIGHT, SharedPreferencesUtil.DEFAULT_WINDOW_HEIGHT);
            if (height != -1)
                editText.setText(height + "");
            editText.setWidth(500);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    try {
                        SharedPreferencesUtil.sharedPreferences.edit().putInt(SharedPreferencesUtil.WINDOW_HEIGHT, Integer.parseInt(editable.toString())).commit();
                    } catch (Exception e) {
                        SharedPreferencesUtil.sharedPreferences.edit().remove(SharedPreferencesUtil.WINDOW_HEIGHT).commit();
                    }
                }
            });
        }

        return scrollView;
    }
}
