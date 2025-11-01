# 🌧️ 弹幕雨 Demo（Popup Rain Effect）

一个有趣的 Android 实验性应用，模拟“弹幕雨”效果，在屏幕上随机飘动彩色气泡消息，类似直播中的弹幕效果。

> 使用 `WindowManager` 和 `SYSTEM_ALERT_WINDOW` 权限实现悬浮窗，配合动画实现流畅视觉效果。

---

## 🎯 功能特点

- ✅ 使用 `TYPE_APPLICATION_OVERLAY` 显示悬浮窗（需手动授权）
- ✅ 随机生成彩色气泡，带有祝福语
- ✅ 气泡从屏幕上方飘落，带有缩放 + 淡入动画
- ✅ 适配亮色/暗色主题

---

## ⚠️ 权限说明

本项目使用了 Android 的 **特殊权限**：

```xml
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
