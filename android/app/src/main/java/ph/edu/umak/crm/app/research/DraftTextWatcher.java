package ph.edu.umak.crm.app.research;

import android.text.Editable;
import android.text.TextWatcher;

public final class DraftTextWatcher implements TextWatcher {
    private final Runnable onChanged;

    public DraftTextWatcher(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    @Override
    public void beforeTextChanged(CharSequence text, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable text) {
        onChanged.run();
    }
}
