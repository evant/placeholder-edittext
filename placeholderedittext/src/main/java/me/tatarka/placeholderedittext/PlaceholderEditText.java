package me.tatarka.placeholderedittext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.AppCompatEditText;
import android.text.DynamicLayout;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;

public class PlaceholderEditText extends AppCompatEditText {

    @Nullable
    private CharSequence placeholder;
    @Nullable
    private DynamicLayout placeholderLayout;
    private ColorStateList placeholderTextColors;
    private int currentPlaceholderTextColor;
    private boolean placeholderLayoutInvalidated = true;

    public PlaceholderEditText(Context context) {
        this(context, null);
    }

    public PlaceholderEditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.placeholderEditTextStyle);
    }

    public PlaceholderEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.Widget_PlaceholderEditText);
    }

    @RequiresApi(21)
    public PlaceholderEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    @SuppressLint("CustomViewStyleable")
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlaceholderEditText, defStyleAttr, defStyleRes);
            CharSequence placeholder = a.getText(R.styleable.PlaceholderEditText_placeholder);
            if (placeholder != null) {
                setPlaceholder(placeholder);
            }
            int ap = a.getResourceId(R.styleable.PlaceholderEditText_android_textAppearance, -1);
            if (ap != -1) {
                TypedArray appearance = context.obtainStyledAttributes(ap, R.styleable.PlaceholderEditText_TextAppearance);
                ColorStateList placeholderTextColor = appearance.getColorStateList(R.styleable.PlaceholderEditText_TextAppearance_textColorPlaceholder);
                if (placeholderTextColor != null) {
                    setPlaceholderTextColor(placeholderTextColor);
                }
                appearance.recycle();
            }
            if (a.hasValue(R.styleable.PlaceholderEditText_textColorPlaceholder)) {
                ColorStateList placeholderTextColor = a.getColorStateList(R.styleable.PlaceholderEditText_textColorPlaceholder);
                if (placeholderTextColor != null) {
                    setPlaceholderTextColor(placeholderTextColor);
                }
            }
            a.recycle();
        }
    }

    public void setPlaceholder(@Nullable CharSequence placeholder) {
        this.placeholder = placeholder;
        createPlaceholderLayout();
        requestLayout();
    }

    private void createPlaceholderLayout() {
        if (placeholder == null) {
            placeholderLayout = null;
            return;
        }
        this.placeholderLayout = new DynamicLayout(new SpannableStringBuilder(placeholder), createPlaceholderPaint(), Integer.MAX_VALUE, getLayoutAlignment(), 0, 1, getIncludeFontPadding());
        placeholderLayoutInvalidated = true;
    }

    private TextPaint createPlaceholderPaint() {
        TextPaint paint = new TextPaint(getPaint());
        paint.setColor(currentPlaceholderTextColor);
        return paint;
    }

    @Override
    public void setIncludeFontPadding(boolean includepad) {
        super.setIncludeFontPadding(includepad);
        createPlaceholderLayout();
    }

    @Override
    public void setTextAlignment(int textAlignment) {
        super.setTextAlignment(textAlignment);
        createPlaceholderLayout();
    }

    public void setPlaceholderTextColor(@ColorInt int placeholderTextColor) {
        this.placeholderTextColors = ColorStateList.valueOf(placeholderTextColor);
        updateTextColors();
    }

    public void setPlaceholderTextColor(ColorStateList colors) {
        this.placeholderTextColors = colors;
        updateTextColors();
    }

    private void updateTextColors() {
        final int[] drawableState = getDrawableState();
        int color = placeholderTextColors.getColorForState(drawableState, 0);
        if (color != currentPlaceholderTextColor) {
            currentPlaceholderTextColor = color;
            if (placeholderLayout != null) {
                placeholderLayout.getPaint().setColor(currentPlaceholderTextColor);
            }
            invalidate();
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (placeholderTextColors != null && placeholderTextColors.isStateful()) {
            updateTextColors();
        }
    }

    public ColorStateList getPlaceholderTextColors() {
        return placeholderTextColors;
    }

    public int getCurrentPlaceholderTextColor() {
        return currentPlaceholderTextColor;
    }

    @Nullable
    public CharSequence getPlaceholder() {
        return placeholder;
    }

    @Override
    @CallSuper
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        placeholderLayoutInvalidated = true;
    }

    private void updatePlaceholderLayout() {
        if (placeholder != null && getLayout() != null) {
            Editable editable = (Editable) placeholderLayout.getText();
            int drawnPlaceholderLength = drawnPlaceholderLength();
            if (drawnPlaceholderLength <= 0) {
                editable.clear();
            } else {
                editable.replace(0, editable.length(), placeholder.subSequence(placeholder.length() - drawnPlaceholderLength, placeholder.length()));
            }
            placeholderLayoutInvalidated = false;
        }
    }

    private int drawnPlaceholderLength() {
        Layout layout = getLayout();
        int firstLineLength = layout.getText().length();
        if (layout.getLineCount() > 1) {
            firstLineLength = layout.getOffsetForHorizontal(1, 0) - 1;
        }
        return placeholder.length() - firstLineLength;
    }

    @Override
    @SuppressLint("RtlHardcoded")
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (placeholder != null && (getHint() == null || getText().length() > 0)) {
            if (placeholderLayoutInvalidated) {
                updatePlaceholderLayout();
            }

            int drawnPlaceholderLength = placeholderLayout.getText().length();
            if (drawnPlaceholderLength <= 0) {
                return;
            }
            Layout layout = getLayout();
            int voffsetText = 0;
            // translate in by our padding
            /* shortcircuit calling getVerticalOffset() */
            if ((getGravity() & Gravity.VERTICAL_GRAVITY_MASK) != Gravity.TOP) {
                int layoutRange = (layout.getLineBottom(0) - layout.getLineTop(0)) - layout.getLineDescent(0);
                int placeholderRange = placeholderLayout.getHeight() - placeholderLayout.getLineDescent(0);
                voffsetText = getVerticalOffset() + (layoutRange - placeholderRange);
            }

            canvas.save();
            final int layoutDirection = getLayoutDirection();
            final int absoluteGravity = Gravity.getAbsoluteGravity(getGravity(), layoutDirection);
            float textWidth = 0;
            if (layout.getLineVisibleEnd(0) > 0) {
                textWidth = layout.getLineWidth(0);
            }
            if ((absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT) {
                canvas.translate(getCompoundPaddingLeft() + textWidth, getExtendedPaddingTop() + voffsetText);
                placeholderLayout.draw(canvas);
            } else if ((absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.CENTER_HORIZONTAL) {
                canvas.translate(textWidth + (getWidth() - textWidth) / 2, getExtendedPaddingTop() + voffsetText);
                placeholderLayout.draw(canvas);
            } else {
                float placeholderWidth = placeholderWidth();
                canvas.translate(getWidth() - placeholderWidth - getCompoundPaddingLeft(), getExtendedPaddingTop() + voffsetText);
//                canvas.clipRect(0, 0, placeholderWidth - textWidth, getHeight());
                placeholderLayout.draw(canvas);
            }
            canvas.restore();
        }
    }

    private int getVerticalOffset() {
        int voffset = 0;
        final int gravity = getGravity() & Gravity.VERTICAL_GRAVITY_MASK;

        Layout l = getLayout();

        if (gravity != Gravity.TOP) {
            int boxht = getBoxHeight();
            int textht = l.getHeight();

            if (textht < boxht) {
                if (gravity == Gravity.BOTTOM) {
                    voffset = boxht - textht;
                } else { // (gravity == Gravity.CENTER_VERTICAL)
                    voffset = (boxht - textht) >> 1;
                }
            }
        }
        return voffset;
    }

    private int getBoxHeight() {
        int padding = getExtendedPaddingTop() + getExtendedPaddingBottom();
        return getMeasuredHeight() - padding;
    }

    private Layout.Alignment getLayoutAlignment() {
        return Layout.Alignment.ALIGN_NORMAL;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (placeholder == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        // Pretend the placeholder is the hint so that it's measured.
        // TODO is there a better way to measure?
        CharSequence oldHint = getHint();
        setHint(placeholder);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setHint(oldHint);
    }

    private int placeholderWidth() {
        if (placeholderLayout == null) {
            return 0;
        }
        return (int) placeholderLayout.getLineWidth(0);
    }
}
