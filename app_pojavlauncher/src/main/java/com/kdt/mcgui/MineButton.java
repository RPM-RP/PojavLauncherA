package com.kdt.mcgui;

import android.content.*;
import android.content.res.TypedArray;
import android.graphics.*;
import android.util.*;

import androidx.core.content.res.ResourcesCompat;

import net.kdt.pojavlaunch.R;

public class MineButton extends androidx.appcompat.widget.AppCompatButton {
	
	public MineButton(Context ctx) {
		super(ctx);
		init(null, 0);
	}
	
	public MineButton(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		init(attrs, 0);
	}

	public MineButton(Context ctx, AttributeSet attrs, int defStyle) {
		super(ctx, attrs);
		init(attrs, defStyle);
	}

	public void init(AttributeSet attributeSet, int defStyle) {
		MineDrawable.apply(this, attributeSet, defStyle);
	}
}
