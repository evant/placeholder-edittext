package me.tatarka.placeholderedittext.app;

import android.content.Context;
import android.text.Editable;
import android.text.GetChars;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.View;

import me.tatarka.placeholderedittext.PlaceholderEditText;

public class SsnEditText extends PlaceholderEditText {
    private boolean inTextChange;

    public SsnEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setPlaceholder("___-__-____");
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        setTransformationMethod(new PartialPasswordTransformationMethod());
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
        if (forward && (s.length() == 3 || s.length() == 6)) {
            s.append('-');
        } else if (!forward && (s.length() == 3 || s.length() == 6)) {
            s.delete(s.length() - 1, s.length());
        }
    }

    static class PartialPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PartialPasswordCharSequence(source, super.getTransformation(source, view));
        }

        static class PartialPasswordCharSequence implements CharSequence, GetChars {
            private final CharSequence plainSource;
            private final CharSequence passwordSource;

            PartialPasswordCharSequence(CharSequence plainSource, CharSequence passwordSource) {
                this.plainSource = plainSource;
                this.passwordSource = passwordSource;
            }

            @Override
            public int length() {
                return passwordSource.length();
            }

            @Override
            public char charAt(int index) {
                if (index == 3 || index == 6) {
                    return plainSource.charAt(index);
                } else {
                    return passwordSource.charAt(index);
                }
            }

            @Override
            public CharSequence subSequence(int start, int end) {
                return new PartialPasswordCharSequence(plainSource.subSequence(start, end), passwordSource.subSequence(start, end));
            }

            @Override
            public void getChars(int start, int end, char[] dest, int destoff) {
                GetChars passwordChars = (GetChars) passwordSource;
                passwordChars.getChars(start, end, dest, destoff);
                if (start <= 3 && end > 3) {
                    dest[3] = plainSource.charAt(3);
                }
                if (start <= 6 && end > 6) {
                    dest[6] = plainSource.charAt(6);
                }
            }
        }
    }
}
