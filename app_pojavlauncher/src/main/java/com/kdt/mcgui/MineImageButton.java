package com.kdt.mcgui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageButton;

import net.kdt.pojavlaunch.R;

public class MineImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public MineImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    public MineImageButton(Context context) {
        super(context);
        init(null, 0);
    }

    public MineImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }
    public void init(AttributeSet attributeSet, int defStyle) {
        MineDrawable.apply(this, attributeSet, defStyle);
    }
}
