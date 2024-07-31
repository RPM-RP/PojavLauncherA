package com.kdt.mcgui;

import android.content.*;
import android.content.res.TypedArray;
import android.util.*;
import android.graphics.*;
import android.widget.EditText;

import net.kdt.pojavlaunch.R;

public class MineEditText extends androidx.appcompat.widget.AppCompatEditText {
	public MineEditText(Context ctx) {
		super(ctx);
		init(null, 0);
	}
	public MineEditText(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init(attrs, 0);
	}
	public MineEditText(Context context, AttributeSet attributeSet, int defStyleAttr) {
		super(context, attributeSet, defStyleAttr);
		init(attributeSet, defStyleAttr);
	}
	public void init(AttributeSet attributeSet, int defStyle) {
		MineDrawable.apply(this, attributeSet, defStyle);
	}
}
