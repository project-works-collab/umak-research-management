package ph.edu.umak.crm.app.common.animation;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import ph.edu.umak.crm.app.common.util.UiTheme;

public final class UiMotion {
    private UiMotion() {}

    public static final DecelerateInterpolator DECELERATE = new DecelerateInterpolator(1.6f);
    public static final OvershootInterpolator OVERSHOOT = new OvershootInterpolator(2.2f);

    public static void animateEntrance(View view) {
        animateEntrance(view, 260, 24);
    }

    public static void animateEntrance(View view, long durationMs, float distanceDp) {
        if (view == null) return;
        view.animate().cancel();
        float offset = UiTheme.dp(view.getContext(), (int) distanceDp);
        view.setAlpha(0f);
        view.setTranslationX(0f);
        view.setTranslationY(offset);
        view.post(() -> {
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(durationMs)
                    .setInterpolator(DECELERATE)
                    .start();
        });
    }

    public static void slideHorizontal(View view, int direction) {
        slideHorizontal(view, direction, 250, 72);
    }

    public static void slideHorizontal(View view, int direction, long durationMs, float distanceDp) {
        if (view == null) return;
        if (direction == 0) {
            animateEntrance(view, durationMs, 24);
            return;
        }
        view.animate().cancel();
        float offset = UiTheme.dp(view.getContext(), (int) distanceDp) * (direction > 0 ? 1f : -1f);
        view.setAlpha(0f);
        view.setTranslationY(0f);
        view.setTranslationX(offset);
        view.post(() -> {
            view.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(durationMs)
                    .setInterpolator(DECELERATE)
                    .start();
        });
    }

    public static void animateDialog(View view) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setScaleX(0.92f);
        view.setScaleY(0.92f);
        view.post(() -> {
            view.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(220)
                    .setInterpolator(DECELERATE)
                    .start();
        });
    }

    public static void bounceIcon(View view) {
        if (view == null) return;
        view.animate()
                .scaleX(0.7f)
                .scaleY(0.7f)
                .setDuration(80)
                .setInterpolator(DECELERATE)
                .withEndAction(() -> view.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(160)
                        .setInterpolator(OVERSHOOT)
                        .start())
                .start();
    }
}
