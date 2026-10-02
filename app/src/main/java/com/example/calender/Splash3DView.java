package com.example.calender;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.Nullable;

/** Lightweight native Canvas/Camera 3D splash with reduced-motion support. */
public final class Splash3DView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final String label;
    private final boolean reducedMotion;
    private long startedAt;
    private Runnable ready;

    public Splash3DView(Context context, @Nullable String label) {
        super(context);
        this.label = label == null ? "" : label;
        AccessibilityManager am = (AccessibilityManager)
                context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        boolean accessibilityEnabled = am != null && am.isEnabled();
        float scale = 1f;
        try {
            scale = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f);
        } catch (Settings.SettingNotFoundException ignored) {}
        reducedMotion = accessibilityEnabled || scale == 0f;
        paint.setTypeface(android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        setBackgroundColor(Color.rgb(12, 15, 22));
    }

    public void start(Runnable readyCallback) {
        ready = readyCallback;
        startedAt = SystemClock.uptimeMillis();
        if (reducedMotion) {
            postDelayed(this::finish, 80L);
        } else {
            postInvalidateOnAnimation();
        }
    }

    private void finish() {
        if (ready != null) {
            Runnable callback = ready;
            ready = null;
            callback.run();
        }
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float progress = Math.min(1f,
                (SystemClock.uptimeMillis() - startedAt) / 850f);
        drawCube(canvas, reducedMotion ? 1f : progress);
        if (!reducedMotion && progress < 1f) {
            postInvalidateOnAnimation();
        } else if (!reducedMotion) {
            postDelayed(this::finish, 60L);
        }
    }

    private void drawCube(Canvas canvas, float progress) {
        float cx = getWidth() * .5f;
        float cy = getHeight() * .43f;
        float d = Math.min(getWidth(), getHeight()) * .15f;
        float angle = reducedMotion ? 0f : progress * 42f - 21f;

        paint.setColor(Color.rgb(34, 42, 58));
        canvas.drawCircle(cx, cy, d * 1.8f, paint);

        android.graphics.Camera camera = new android.graphics.Camera();
        camera.rotateX(angle * .55f);
        camera.rotateY(angle);
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        camera.getMatrix(matrix);
        matrix.postTranslate(cx, cy);
        matrix.preTranslate(-cx, -cy);

        canvas.save();
        canvas.concat(matrix);
        paint.setColor(Color.rgb(67, 125, 235));
        canvas.drawRect(cx - d, cy - d, cx + d, cy + d, paint);

        paint.setColor(Color.rgb(44, 88, 171));
        path.reset();
        path.moveTo(cx-d, cy-d);
        path.lineTo(cx-d*.6f, cy-d*1.35f);
        path.lineTo(cx+d*1.35f, cy-d*1.35f);
        path.lineTo(cx+d, cy-d);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.rgb(34, 70, 137));
        path.reset();
        path.moveTo(cx+d, cy-d);
        path.lineTo(cx+d*1.35f, cy-d*1.35f);
        path.lineTo(cx+d*1.35f, cy+d*.65f);
        path.lineTo(cx+d, cy+d);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(18f, d*.34f));
        canvas.drawText(label, cx, cy+d*.12f, paint);
        canvas.restore();

        paint.setTextSize(Math.max(15f, d*.21f));
        canvas.drawText("Preparing...", cx, getHeight()*.69f, paint);
    }
}
