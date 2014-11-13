package me.imczy.myslidmenu;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class MySlidLayout extends LinearLayout implements OnTouchListener {

	public static final int SNAP_VELOCITY = 200;

	private int screenWidth;

	private int maxleftEdge = 0;
	private int minLeftEdge = 0;

	private int touchSlop;

	private int xDown;
	private int xUp;
	private int xMove;

	private int yDown;
	private int yUp;
	private int yMove;

	private boolean isLeftLayoutVisible;

	private boolean isSliding;

	private View leftLayout;
	private View rightLayout;

	private View mBidnView;
	private MarginLayoutParams leftLayoutParams;
	private MarginLayoutParams rightLayoutParams;

	private VelocityTracker mVelocityTracker;

	public MySlidLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (changed) {
			leftLayout = getChildAt(0);
			leftLayoutParams = (MarginLayoutParams) leftLayout.getLayoutParams();
			maxleftEdge = leftLayoutParams.width;

			rightLayout = getChildAt(1);
			rightLayoutParams = (MarginLayoutParams) rightLayout.getLayoutParams();
			rightLayoutParams.width = screenWidth;
			rightLayout.setLayoutParams(rightLayoutParams);

		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
		if (leftLayout.getVisibility() != View.VISIBLE) {
			leftLayout.setVisibility(View.VISIBLE);
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = (int) event.getRawX();
			yDown = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = (int) event.getRawX();
			yMove = (int) event.getRawY();
			int moveDistanceX = xMove - xDown;
			int moveDistanceY = yMove - yDown;
			if (!isLeftLayoutVisible && moveDistanceX >= touchSlop && (isSliding || Math.abs(moveDistanceY) <= touchSlop)) {
				isSliding = true;
				rightLayoutParams.leftMargin = moveDistanceX;
				if (rightLayoutParams.leftMargin > maxleftEdge) {
					rightLayoutParams.leftMargin = maxleftEdge;
				}
				rightLayout.setLayoutParams(rightLayoutParams);
			}

			if (isLeftLayoutVisible && -moveDistanceX >= touchSlop) {
				isSliding = true;
				rightLayoutParams.leftMargin = maxleftEdge + moveDistanceX;
				if (rightLayoutParams.leftMargin < minLeftEdge) {
					rightLayoutParams.leftMargin = minLeftEdge;
				}
				rightLayout.setLayoutParams(rightLayoutParams);
			}
			break;

		case MotionEvent.ACTION_UP:
			xUp = (int) event.getRawX();
			int upDistanceX = xUp - xDown;
			if (isSliding) {
				// 手指抬起时，进行判断当前手势的意图，从而决定是滚动到左侧布局，还是滚动到右侧布局
				if (wantToShowLeftLayout()) {
					if (shouldScrollToLeftLayout()) {
						scrollToLeftLayout();
					} else {
						scrollToRightLayout();
					}
				} else if (wantToShowRightLayout()) {
					if (shouldScrollToRightLayout()) {
						scrollToRightLayout();
					} else {
						scrollToLeftLayout();
					}
				}
			}
			// else if (upDistanceX < touchSlop && isLeftLayoutVisible) {
			// scrollToRightLayout();
			// }
			recycleVelocityTracker();
			break;
		}
		// if (v.isEnabled()) {
		// if (isSliding) {
		// // unFocusBindView();
		// return true;
		// }
		// if (isLeftLayoutVisible) {
		// return true;
		// }
		// return false;
		// }
		return true;
	}

	private boolean shouldScrollToRightLayout() {
		return xDown - xUp > leftLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private boolean shouldScrollToLeftLayout() {
		return xUp - xDown > leftLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private boolean wantToShowLeftLayout() {
		return xUp - xDown > 0 && !isLeftLayoutVisible;
	}

	private boolean wantToShowRightLayout() {
		return xUp - xDown < 0 && isLeftLayoutVisible;
	}

	public void scrollToLeftLayout() {
		new ScrollTask().execute(-30);
	}

	public void scrollToRightLayout() {
		new ScrollTask().execute(30);
	}

	class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... speed) {
			int leftMargin = rightLayoutParams.leftMargin;
			// 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
			while (true) {
				leftMargin = leftMargin + speed[0];
				if (leftMargin < minLeftEdge) {
					leftMargin = minLeftEdge;
					break;
				}
				if (minLeftEdge > maxleftEdge) {
					minLeftEdge = maxleftEdge;
					break;
				}
				publishProgress(minLeftEdge);
				// 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
				sleep(15);
			}
			if (speed[0] > 0) {
				isLeftLayoutVisible = false;
			} else {
				isLeftLayoutVisible = true;
			}
			isSliding = false;
			return minLeftEdge;
		}

		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			rightLayoutParams.leftMargin = leftMargin[0];
			rightLayout.setLayoutParams(rightLayoutParams);
			// unFocusBindView();
		}

		@Override
		protected void onPostExecute(Integer leftMargin) {
			rightLayoutParams.leftMargin = leftMargin;
			rightLayout.setLayoutParams(rightLayoutParams);
		}
	}

	public void setScrollEvent(View bindView) {
		mBidnView = bindView;
		mBidnView.setOnTouchListener(this);
	}

	private void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void recycleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
