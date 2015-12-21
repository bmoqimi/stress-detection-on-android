package com.datenkrake;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * An ImageView which is always of same width as height (and vice-versa).
 * 
 * @author svenja
 */
public class SquaredImageView extends ImageView {

    public SquaredImageView(Context context) {
        super(context);
    }

    public SquaredImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquaredImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
