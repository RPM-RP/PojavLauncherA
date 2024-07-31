package com.kdt.mcgui;

import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.R;

public class MineDrawable extends Drawable {
    private final Paint mPrimaryPaint = new Paint();
    private final Path mClipPath = new Path();
    private final Path mSecondaryPath = new Path();
    private final int mGradientColorPrimary;
    private final int mGradientColorSecondary;
    private final float mInsetLeft;
    private final float mInsetRight;
    private final float mInsetTop;
    private final float mInsetBottom;

    public MineDrawable(int gradientPrimary, int gradientSecondary, float insetLeft, float insetRight, float insetTop, float insetBottom) {
        this.mGradientColorPrimary = gradientPrimary;
        this.mGradientColorSecondary = gradientSecondary;
        this.mInsetLeft = insetLeft;
        this.mInsetRight = insetRight;
        this.mInsetTop = insetTop;
        this.mInsetBottom = insetBottom;
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        LinearGradient shader = new LinearGradient(left, top, left, bottom, mGradientColorPrimary, mGradientColorSecondary, Shader.TileMode.CLAMP);
        mPrimaryPaint.setShader(shader);
        mSecondaryPath.addRect(left + mInsetLeft, top, right - mInsetRight, bottom, Path.Direction.CW);
        mClipPath.reset();
        mClipPath.addRect(left, top + mInsetTop, right, bottom - mInsetBottom, Path.Direction.CW);
        mClipPath.op(mSecondaryPath, Path.Op.UNION);
        mSecondaryPath.reset();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.clipPath(mClipPath);
        canvas.drawRect(getBounds(), mPrimaryPaint);
    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }

    public static void apply(View view, AttributeSet attributeSet, int defStyle) {
        if(attributeSet == null) return;
        //noinspection resource
        TypedArray styleAttrs = view.getContext().obtainStyledAttributes(attributeSet, R.styleable.MineButton, defStyle, 0);
        view.setBackground(new MineDrawable(
                styleAttrs.getColor(R.styleable.MineButton_gradientColorTop, Color.WHITE),
                styleAttrs.getColor(R.styleable.MineButton_gradientColorBottom, Color.BLACK),
                styleAttrs.getDimension(R.styleable.MineButton_drawInsetLeft, 16),
                styleAttrs.getDimension(R.styleable.MineButton_drawInsetRight, 16),
                styleAttrs.getDimension(R.styleable.MineButton_drawInsetTop, 16),
                styleAttrs.getDimension(R.styleable.MineButton_drawInsetBottom, 16)
        ));
        styleAttrs.recycle();
    }
}
