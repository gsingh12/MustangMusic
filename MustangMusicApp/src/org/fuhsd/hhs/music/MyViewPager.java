package org.fuhsd.hhs.music;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * This implementation of the ViewPager disables the flinging of the pages. Thus horizontal swipes in in views inside the page work correctly.
 */
public class MyViewPager extends ViewPager {	

	public MyViewPager(Context context) {
		super(context);
	}
	
	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent arg0) {
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent arg0) {
		return false;
	}
}
