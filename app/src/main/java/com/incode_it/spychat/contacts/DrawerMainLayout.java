package com.incode_it.spychat.contacts;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.incode_it.spychat.R;

public class DrawerMainLayout {

    /*private NavigationMenu navigationMenu;
    private CoordinatorLayout movingLayout;
    private ViewDragHelper mDragHelper;

    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;
    private int mDraggingState = 0;
    private int mDraggingBorder = 0;
    private int mHorizontalRange;

    private boolean mIsOpen;

    public DrawerMainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHorizontalRange = (int) context.getResources().getDimension(R.dimen.nav_menu_wight);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        navigationMenu = (NavigationMenu) findViewById(R.id.navigation_menu);
        movingLayout = (CoordinatorLayout) findViewById(R.id.content_container);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
    }






    private void onStopDraggingToClosed() {
    }

    private void onStartDragging() {

    }

    public class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public void onViewDragStateChanged(int state) {
            if (state == mDraggingState) {
                return;
            }
            if ((mDraggingState == ViewDragHelper.STATE_DRAGGING || mDraggingState == ViewDragHelper.STATE_SETTLING) &&
                    state == ViewDragHelper.STATE_IDLE) {

                if (mDraggingBorder == 0) {
                    onStopDraggingToClosed();
                } else if (mDraggingBorder == mHorizontalRange) {
                    mIsOpen = true;
                }
            }
            if (state == ViewDragHelper.STATE_DRAGGING) {
                onStartDragging();
            }
            mDraggingState = state;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            mDraggingBorder = left;
            changedView.setScaleX(1f - (left * 100f / mHorizontalRange) * 0.07f / 100f);
            changedView.setScaleY(1f - (left * 100f / mHorizontalRange) * 0.07f / 100f);
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mHorizontalRange;
        }

        @Override
        public boolean tryCaptureView(View view, int i) {
            return (view.getId() == R.id.content_container);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            final int rightBound = mHorizontalRange;
            return Math.min(Math.max(left, 0), rightBound);
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final float rangeToCheck = mHorizontalRange;
            if (mDraggingBorder == 0) {
                mIsOpen = false;
                return;
            }
            if (mDraggingBorder == rangeToCheck) {
                mIsOpen = true;
                return;
            }
            boolean settleToOpen = false;
            if (xvel < -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = false;
            } else if (xvel > -AUTO_OPEN_SPEED_LIMIT) {
                settleToOpen = true;
            } else if (mDraggingBorder > -rangeToCheck / 2) {
                settleToOpen = true;
            } else if (mDraggingBorder < -rangeToCheck / 2) {
                settleToOpen = false;
            }

            final int finalLeft = settleToOpen ? mHorizontalRange : 0;

            if(mDragHelper.settleCapturedViewAt(finalLeft, 0)) {
                ViewCompat.postInvalidateOnAnimation(DrawerMainLayout.this);
            }
        }
    }



    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isDragTarget(event) && mDragHelper.shouldInterceptTouchEvent(event)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDragTarget(event) || isMoving()) {
            mDragHelper.processTouchEvent(event);
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }


    private boolean isDragTarget(MotionEvent event) {
        int[] queenLocation = new int[2];
        movingLayout.getLocationOnScreen(queenLocation);
        int upperLimit = queenLocation[1] + movingLayout.getMeasuredHeight();
        int lowerLimit = queenLocation[1];
        int y = (int) event.getRawY();
        return (y > lowerLimit && y < upperLimit);
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public boolean isMoving() {
        return (mDraggingState == ViewDragHelper.STATE_DRAGGING ||
                mDraggingState == ViewDragHelper.STATE_SETTLING);
    }

    public boolean isOpen() {
        return mIsOpen;
    }*/
}
