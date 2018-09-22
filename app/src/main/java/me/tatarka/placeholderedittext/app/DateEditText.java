package me.tatarka.placeholderedittext.app;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;

import me.tatarka.placeholderedittext.PlaceholderEditText;

public class DateEditText extends PlaceholderEditText implements InputFilter {

    private boolean inTextChange;

    public DateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPlaceholder("MM/DD/YYYY");
        setFilters(new InputFilter[]{this, new LengthFilter(10)});
        setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (inTextChange) return;
        inTextChange = true;
        updateText((Editable) text, lengthAfter > lengthBefore);
        inTextChange = false;
    }

    private void updateText(Editable s, boolean forward) {
        if (forward && (s.length() == 2 || s.length() == 5)) {
            s.append('/');
        } else if (!forward && (s.length() == 2 || s.length() == 5)) {
            s.delete(s.length() - 1, s.length());
        }
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        SpannableStringBuilder b = new SpannableStringBuilder(dest);
        b.replace(dstart, dend, source.subSequence(start, end));
        if (b.length() > 0) {
            char c = b.charAt(0);
            if (c != '0' && c != '1') {
                return "";
            }
        }
        if (b.length() > 1) {
            try {
                int month = Integer.parseInt(TextUtils.substring(b, 0, 2));
                if (month > 12) {
                    return "";
                }
            } catch (NumberFormatException e) {
                return "";
            }
        }
        if (b.length() > 2) {
            if (b.charAt(2) != '/') {
                return "";
            }
        }
        if (b.length() > 3) {
            char c = b.charAt(3);
            if (c != '0' && c != '1' && c != '2' && c != '3') {
                return "";
            }
        }
        if (b.length() > 4) {
            try {
                int day = Integer.parseInt(TextUtils.substring(b, 3, 5));
                if (day > 31) {
                    return "";
                }
            } catch (NumberFormatException e) {
                return "";
            }
        }
        if (b.length() > 5) {
            if (b.charAt(5) != '/') {
                return "";
            }
        }
        return null;
    }
}
