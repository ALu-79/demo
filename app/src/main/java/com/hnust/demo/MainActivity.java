package com.hnust.demo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_DRAW_OVERLAY = 97001;//请求码

    private Button btnStart;
    private WindowManager windowManager;
    private FrameLayout popupContainer;
    private String[] messages = {
            "今天天气怎么样", "今天你辛苦了", "期待我们下次见面", "在干嘛", "别熬夜",
            "愿所有梦想成真", "好好吃饭", "见到你就很开心", "你笑起来真好看", "告诉你,我在想你",
            "你的努力很有用", "一切都会变好", "慢慢来", "今天你也要加油", "你已经很棒了",
            "保持微笑", "每一天都是新的开始", "不要放弃", "勇敢面对", "感谢有你",
            "相信自己", "支持你", "快乐是自己创造的", "做最好的自己", "享受生活"
    };
    private Random random = new Random();
    private Handler handler = new Handler();
    private AtomicInteger count = new AtomicInteger(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = findViewById(R.id.btn_start);
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        btnStart.setOnClickListener(v -> {
            if (canDrawOverlays()) {
                startPopupRain();
            } else {
                requestOverlayPermission();
            }
        });
    }
    

    //悬浮窗权限检查
    private boolean canDrawOverlays() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }

    //请求悬浮窗权限
    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY) {
            if (canDrawOverlays()) {
                startPopupRain();
            } else {
                btnStart.setEnabled(true);
                btnStart.setText("请先授予悬浮窗权限");
            }
        }
    }

    //背景色
    private GradientDrawable getRandomGradientDrawable() {
        int[] colors = new int[]{
                0xFFFFD1DC, // 草莓奶昔粉
                0xFFFFE0B5, // 奶油黄
                0xFFB5EAD7, // 梦幻浅蓝（童年色）
                0xFFC7CEEA, // 浅薰衣草紫
                0xFFFFB3B3, // 樱花粉
                0xFFD7E5C5, // 抹茶绿
                0xFFFFD6E5, // 草莓牛奶粉
                0xFFE2F0D9, // 清新薄荷绿
        };

        int color1 = colors[random.nextInt(colors.length)];
        int color2 = Color.argb(
                255,
                Math.min(255, (Color.red(color1) + 245) / 2),
                Math.min(255, (Color.green(color1) + 245) / 2),
                Math.min(255, (Color.blue(color1) + 245) / 2)
        );
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColors(new int[]{color1, color2});
        drawable.setCornerRadius(dp2px(16));
        drawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        return drawable;
    }
    //创建动画：淡入+缩放
    private Animation createPopupAnimation() {
        ScaleAnimation scale = new ScaleAnimation(
                0.1f, 1.0f,
                0.1f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        AlphaAnimation alpha = new AlphaAnimation(0.0f, 0.95f);

        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.addAnimation(alpha);
        set.setDuration(300);
        set.setInterpolator(new OvershootInterpolator(1.8f));
        return set;
    }
    // 启动弹窗雨
    private void startPopupRain() {
        btnStart.setEnabled(false);
        btnStart.setText("弹窗中...");

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

        // 创建悬浮层容器
        popupContainer = new FrameLayout(this);
        popupContainer.setBackgroundColor(Color.TRANSPARENT);

        // 悬浮窗参数
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;

        try {
            windowManager.addView(popupContainer, params);
        } catch (Exception e) {
            e.printStackTrace();
            btnStart.setEnabled(true);
            btnStart.setText("启动失败");
            return;
        }

        // 开始添加弹窗
        count.set(0);
        handler.postDelayed(new AddTextTask(screenWidth, screenHeight), 100);
    }

    class AddTextTask implements Runnable {
        private final int screenWidth;
        private final int screenHeight;

        public AddTextTask(int screenWidth, int screenHeight) {
            this.screenWidth = screenWidth;
            this.screenHeight = screenHeight;
        }

        @Override
        public void run() {
            if (count.incrementAndGet() > 99) {
                // 结束后 5 秒退出
                handler.postDelayed(() -> {
                    if (popupContainer != null && popupContainer.getParent() != null) {
                        try {
                            windowManager.removeView(popupContainer);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    finishAndRemoveTask();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }, 5000);
                return;
            }

            // 创建 TextView
            TextView tv = new TextView(MainActivity.this);
            tv.setText(messages[random.nextInt(messages.length)]);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15); // 使用 SP 单位确保文本大小适应不同设备
            tv.setTextColor(Color.parseColor("#5D4037"));
            tv.setTypeface(null, Typeface.BOLD);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp2px(16), dp2px(10), dp2px(16), dp2px(10));
            tv.setMaxLines(2);
            tv.setEllipsize(TextUtils.TruncateAt.END);

            // 动态计算文本宽度
            TextPaint paint = tv.getPaint();
            float textWidth = paint.measureText(tv.getText().toString());
            int minWidth = (int) Math.max(textWidth + dp2px(32), dp2px(100)); // 加上左右内边距，并设定最小宽度

            // 设置随机渐变背景
            tv.setBackground(getRandomGradientDrawable());

            //设置布局参数
            FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            tvParams.width = minWidth; // 确TextView至少有minWidth宽度
            tvParams.setMargins(random.nextInt(screenWidth - minWidth), random.nextInt(screenHeight - dp2px(80)), 0, 0);
            tv.setLayoutParams(tvParams);

            // 添加到容器
            popupContainer.addView(tv);

            // 立即播放动画
            tv.startAnimation(createPopupAnimation());

            //设置延迟时间
            long delay = 80 + random.nextInt(101);
            handler.postDelayed(this, delay);
        }
    }

    //dp转px
    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics()
        );
    }
}