package ph.edu.umak.crm.app.common.components;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import ph.edu.umak.crm.app.common.animation.UiMotion;
import ph.edu.umak.crm.app.common.util.UiTheme;

public class UiSwitch extends FrameLayout {
    public interface OnCheckedChangeListener {
        void onCheckedChanged(UiSwitch switchView, boolean isChecked);
    }

    private static final int COLOR_OFF = 0xffcbd5e1;
    private static final int COLOR_ON = 0xff082d50;
    private static final int COLOR_THUMB = 0xffffffff;

    private boolean isChecked;
    private final View trackView;
    private final View thumbView;
    private final GradientDrawable trackDrawable;
    private final int trackWidth;
    private final int trackHeight;
    private final int thumbSize;
    private final int thumbMargin;
    private final float maxTranslation;
    private ValueAnimator animator;
    private OnCheckedChangeListener listener;

    public UiSwitch(Context context) {
        this(context, false);
    }

    public UiSwitch(Context context, boolean initialChecked) {
        super(context);
        this.isChecked = initialChecked;

        trackWidth = UiTheme.dp(context, 38);
        trackHeight = UiTheme.dp(context, 22);
        thumbSize = UiTheme.dp(context, 18);
        thumbMargin = UiTheme.dp(context, 2);
        maxTranslation = trackWidth - thumbSize - (2 * thumbMargin);

        setLayoutParams(new LayoutParams(trackWidth, trackHeight));
        setClipChildren(false);
        setClipToPadding(false);

        trackDrawable = new GradientDrawable();
        trackDrawable.setCornerRadius(trackHeight / 2.0f);
        trackDrawable.setColor(initialChecked ? COLOR_ON : COLOR_OFF);

        trackView = new View(context);
        trackView.setBackground(trackDrawable);
        addView(trackView, new LayoutParams(trackWidth, trackHeight));

        thumbView = new View(context);
        GradientDrawable thumbDrawable = new GradientDrawable();
        thumbDrawable.setCornerRadius(thumbSize / 2.0f);
        thumbDrawable.setColor(COLOR_THUMB);
        thumbDrawable.setStroke(UiTheme.dp(context, 1), 0xffcbd5e1);
        thumbView.setBackground(thumbDrawable);
        thumbView.setElevation(0);

        LayoutParams thumbParams = new LayoutParams(thumbSize, thumbSize);
        thumbParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        thumbParams.leftMargin = thumbMargin;
        addView(thumbView, thumbParams);

        if (initialChecked) {
            thumbView.setTranslationX(maxTranslation);
        }

        setClickable(true);
        setFocusable(true);
        setOnClickListener(v -> toggle());
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void toggle() {
        setChecked(!isChecked, true);
    }

    public void setChecked(boolean checked) {
        setChecked(checked, false);
    }

    public void setChecked(boolean checked, boolean animate) {
        if (this.isChecked == checked) return;
        this.isChecked = checked;

        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        float startTranslation = thumbView.getTranslationX();
        float endTranslation = checked ? maxTranslation : 0f;
        int startColor = checked ? COLOR_OFF : COLOR_ON;
        int endColor = checked ? COLOR_ON : COLOR_OFF;

        if (!animate) {
            thumbView.setTranslationX(endTranslation);
            trackDrawable.setColor(endColor);
            if (listener != null) {
                listener.onCheckedChanged(this, isChecked);
            }
            return;
        }

        final ArgbEvaluator evaluator = new ArgbEvaluator();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(180);
        animator.setInterpolator(UiMotion.DECELERATE);
        animator.addUpdateListener(animation -> {
            float fraction = animation.getAnimatedFraction();
            thumbView.setTranslationX(startTranslation + fraction * (endTranslation - startTranslation));
            int color = (int) evaluator.evaluate(fraction, startColor, endColor);
            trackDrawable.setColor(color);
        });
        animator.start();

        if (listener != null) {
            listener.onCheckedChanged(this, isChecked);
        }
    }
}
