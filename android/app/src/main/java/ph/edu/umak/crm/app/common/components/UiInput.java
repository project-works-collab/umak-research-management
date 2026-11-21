package ph.edu.umak.crm.app.common.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Locale;
import ph.edu.umak.crm.app.R;
import ph.edu.umak.crm.app.ScreenTypography;
import ph.edu.umak.crm.app.common.util.UiTheme;

public final class UiInput {
    private UiInput() {}

    public static CharSequence formatLabel(CharSequence text) {
        if (text == null) return null;
        String str = text.toString();
        int idx = str.indexOf('*');
        if (idx == -1) return text;
        android.text.SpannableString ss = new android.text.SpannableString(text);
        while (idx != -1) {
            ss.setSpan(new android.text.style.ForegroundColorSpan(UiTheme.ERROR_RED),
                    idx, idx + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            idx = str.indexOf('*', idx + 1);
        }
        return ss;
    }

    public static class FormField {
        private final Context context;
        private final LinearLayout container;
        private final LinearLayout box;
        private final EditText input;
        private final TextView errorView;
        private final TextView helperView;
        private final ImageView errorIcon;
        private final LinearLayout errorRow;

        public FormField(Context context, CharSequence label, String hint, boolean secret) {
            this.context = context;
            container = new LinearLayout(context);
            container.setOrientation(LinearLayout.VERTICAL);

            if (label != null && label.length() > 0) {
                TextView caption = new TextView(context);
                caption.setText(formatLabel(label));
                caption.setTextColor(UiTheme.MUTED);
                caption.setTypeface(UiTheme.boldFont(context));
                ScreenTypography.apply(caption, 13);
                caption.setPadding(0, UiTheme.dp(context, 10), 0, UiTheme.dp(context, 4));
                container.addView(caption);
            }

            box = new LinearLayout(context);
            box.setGravity(Gravity.CENTER_VERTICAL);
            box.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.BORDER));

            input = new EditText(context);
            input.setHint(hint);
            input.setHintTextColor(UiTheme.MUTED);
            input.setTextColor(UiTheme.INK);
            ScreenTypography.apply(input, 15);
            input.setSingleLine(true);
            input.setBackgroundColor(Color.TRANSPARENT);
            input.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                    UiTheme.dp(context, 10), UiTheme.dp(context, 10));
            input.setMinHeight(UiTheme.dp(context, 48));
            input.setSaveEnabled(false);
            if (secret) {
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            input.setTypeface(UiTheme.bodyFont(context));
            box.addView(input, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            if (secret) {
                Button eye = new Button(context, null, android.R.attr.borderlessButtonStyle);
                eye.setText("");
                eye.setContentDescription("Show password");
                eye.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shared_visibility, 0, 0, 0);
                eye.setPadding(UiTheme.dp(context, 12), 0, 0, 0);
                eye.setBackgroundColor(Color.TRANSPARENT);
                eye.setOnClickListener(view -> {
                    boolean reveal = input.getTransformationMethod() instanceof PasswordTransformationMethod;
                    input.setTransformationMethod(reveal ? null : PasswordTransformationMethod.getInstance());
                    input.setSelection(input.length());
                    eye.setContentDescription((reveal ? "Hide " : "Show ") + "password");
                });
                box.addView(eye, new LinearLayout.LayoutParams(UiTheme.dp(context, 48), UiTheme.dp(context, 48)));
            }

            container.addView(box);

            errorRow = new LinearLayout(context);
            errorRow.setOrientation(LinearLayout.HORIZONTAL);
            errorRow.setGravity(Gravity.CENTER_VERTICAL);
            errorRow.setPadding(0, UiTheme.dp(context, 4), 0, 0);
            errorRow.setVisibility(View.GONE);

            errorIcon = new ImageView(context);
            Drawable alertDrawable = context.getDrawable(R.drawable.ic_crm_alert);
            if (alertDrawable != null) {
                alertDrawable = alertDrawable.mutate();
                alertDrawable.setColorFilter(UiTheme.ERROR_RED, PorterDuff.Mode.SRC_IN);
                errorIcon.setImageDrawable(alertDrawable);
            }
            int alertSize = UiTheme.dp(context, 14);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(alertSize, alertSize);
            iconParams.rightMargin = UiTheme.dp(context, 6);
            errorRow.addView(errorIcon, iconParams);

            errorView = new TextView(context);
            errorView.setTextColor(UiTheme.ERROR_RED);
            errorView.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(errorView, 12);
            errorRow.addView(errorView, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            container.addView(errorRow);

            helperView = new TextView(context);
            helperView.setTextColor(UiTheme.MUTED);
            helperView.setTypeface(UiTheme.bodyFont(context));
            ScreenTypography.apply(helperView, 12);
            helperView.setPadding(0, UiTheme.dp(context, 4), 0, 0);
            helperView.setVisibility(View.GONE);
            container.addView(helperView);
        }

        public View getView() {
            return container;
        }

        public EditText getInput() {
            return input;
        }

        public void setError(String message) {
            if (message != null && !message.isEmpty()) {
                errorView.setText(message);
                errorRow.setVisibility(View.VISIBLE);
                helperView.setVisibility(View.GONE);
                box.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.ERROR_RED));
            } else {
                clearError();
            }
        }

        public void clearError() {
            errorRow.setVisibility(View.GONE);
            box.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.BORDER));
        }

        public void setHelper(String message) {
            if (message != null && !message.isEmpty()) {
                helperView.setText(message);
                helperView.setVisibility(View.VISIBLE);
            } else {
                helperView.setVisibility(View.GONE);
            }
        }

        public void setEnabled(boolean enabled) {
            input.setEnabled(enabled);
            box.setAlpha(enabled ? 1.0f : 0.6f);
        }
    }

    public static FormField field(Context context, LinearLayout parent, CharSequence label, String hint, boolean secret) {
        FormField ff = new FormField(context, label, hint, secret);
        if (parent != null) parent.addView(ff.getView());
        return ff;
    }

    public static EditText create(Context context, LinearLayout parent, CharSequence label, String hint, boolean secret) {
        return create(context, parent, label, hint, secret, UiTheme.boldFont(context), UiTheme.bodyFont(context));
    }

    public static EditText create(Context context, LinearLayout parent, CharSequence label, String hint, boolean secret,
            Typeface labelFont, Typeface inputFont) {
        TextView caption = new TextView(context);
        caption.setText(formatLabel(label));
        caption.setTextColor(UiTheme.MUTED);
        caption.setTypeface(labelFont != null ? labelFont : UiTheme.boldFont(context));
        ScreenTypography.apply(caption, 13);
        caption.setPadding(0, UiTheme.dp(context, 12), 0, UiTheme.dp(context, 4));
        parent.addView(caption);

        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(UiTheme.shape(context, UiTheme.PAPER, 8, UiTheme.BORDER));

        EditText input = new EditText(context);
        input.setHint(hint);
        input.setHintTextColor(UiTheme.MUTED);
        input.setTextColor(UiTheme.INK);
        ScreenTypography.apply(input, 16);
        input.setSingleLine(true);
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(UiTheme.dp(context, 12), UiTheme.dp(context, 10),
                UiTheme.dp(context, 10), UiTheme.dp(context, 10));
        input.setMinHeight(UiTheme.dp(context, 52));
        input.setSaveEnabled(false);
        if (secret) {
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        input.setTypeface(inputFont != null ? inputFont : UiTheme.bodyFont(context));
        row.addView(input, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        if (secret) {
            Button eye = new Button(context, null, android.R.attr.borderlessButtonStyle);
            eye.setText("");
            String labelStr = label != null ? label.toString().toLowerCase(Locale.ROOT) : "password";
            eye.setContentDescription("Show " + labelStr);
            eye.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_shared_visibility, 0, 0, 0);
            eye.setPadding(UiTheme.dp(context, 12), 0, 0, 0);
            eye.setBackgroundColor(Color.TRANSPARENT);
            eye.setOnClickListener(view -> {
                boolean reveal = input.getTransformationMethod() instanceof PasswordTransformationMethod;
                input.setTransformationMethod(reveal ? null : PasswordTransformationMethod.getInstance());
                input.setSelection(input.length());
                eye.setContentDescription((reveal ? "Hide " : "Show ") + labelStr);
            });
            row.addView(eye, new LinearLayout.LayoutParams(UiTheme.dp(context, 48), UiTheme.dp(context, 48)));
        }

        parent.addView(row);
        return input;
    }
}
