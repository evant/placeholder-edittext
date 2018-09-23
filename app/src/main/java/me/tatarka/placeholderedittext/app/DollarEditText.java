package me.tatarka.placeholderedittext.app;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.util.AttributeSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import me.tatarka.placeholderedittext.PlaceholderEditText;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
import static me.tatarka.placeholderedittext.app.Spanalot.textSize;

public class DollarEditText extends PlaceholderEditText implements InputFilter {
    boolean inTextChange;

    public DollarEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFilters(new InputFilter[]{this});
        updateText(getText(), true);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (inTextChange) return;
        inTextChange = true;
        updateText((Editable) text, lengthAfter > lengthBefore);
        inTextChange = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawText("$", 0, getLayout().getLineBottom(0), getPaint());
    }

    private void updateText(Editable s, boolean forward) {
        int periodIndex = TextUtils.indexOf(s, '.');
        int end = periodIndex >= 0 ? periodIndex : s.length();
        if (!forward && s.length() == 1 && s.charAt(0) == '0') {
            s.delete(0, 1);
        }
        if (s.length() > 0) {
            if (periodIndex == 0) {
                s.insert(0, "0");
            }
            if (end > 0) {
                NumberFormat numberFormat = DecimalFormat.getNumberInstance();
                try {
                    s.replace(0, end, numberFormat.format(numberFormat.parse(TextUtils.substring(s, 0, end))));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            periodIndex = TextUtils.indexOf(s, '.');
            end = periodIndex >= 0 ? periodIndex : s.length();
            s.setSpan(largeText(), 0, end, SPAN_EXCLUSIVE_EXCLUSIVE);
            s.setSpan(smallText(), end, s.length(), SPAN_EXCLUSIVE_EXCLUSIVE);
            Spanalot spanalot = new Spanalot();
            StringBuilder zeros = new StringBuilder();
            for (int i = 0; i < end; i++) {
                zeros.append('0');
            }
            spanalot.append(zeros, largeText());
            if (periodIndex >= 0) {
                spanalot.append(".00", smallText());
            }
            setPlaceholder(spanalot);
        } else {
            setPlaceholder(new Spanalot("0", largeText()));
        }
    }

    private AbsoluteSizeSpan smallText() {
        return textSize(getResources().getDimensionPixelSize(R.dimen.text_size_small));
    }

    private AbsoluteSizeSpan largeText() {
        return textSize(getResources().getDimensionPixelSize(R.dimen.text_size_large));
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        // Only allow 2 digits after the decimal
        SpannableStringBuilder b = new SpannableStringBuilder(dest);
        b.replace(dstart, dend, source.subSequence(start, end));
        int periodIndex = TextUtils.indexOf(b, '.');
        if (periodIndex >= 0 && b.length() - periodIndex > 3) {
            return "";
        }
        if (periodIndex >= 0 && TextUtils.indexOf(b, '.', periodIndex + 1) >= 0) {
            return "";
        }
        return null;
    }
}
