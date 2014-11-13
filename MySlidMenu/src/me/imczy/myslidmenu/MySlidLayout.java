package me.imczy.myslidmenu;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MySlidLayout extends RelativeLayout implements OnTouchListener {
	public static final String TAG = "scrollToShowLeftLayout";

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

	private View mBindView;
	private MarginLayoutParams leftLayoutParams;
	private RelativeLayout.LayoutParams rightLayoutParams;
	private RelativeLayout.LayoutParams slidParams;

	private VelocityTracker mVelocityTracker;

	public MySlidLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = wm.getDefaultDisplay().getWidth();
		touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		rightLayout = getChildAt(1);
		Log.i(TAG, "onMeasure-----onMeasure ");
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		Log.i(TAG, "onLayout-----onLayout ");
		if (changed) {
			Log.i(TAG, "onLayout-----changed ");
			leftLayout = getChildAt(0);
			leftLayoutParams = (MarginLayoutParams) leftLayout.getLayoutParams();
			maxleftEdge = leftLayoutParams.width;

			rightLayout = getChildAt(1);
			rightLayoutParams = (RelativeLayout.LayoutParams) rightLayout.getLayoutParams();
			rightLayoutParams.width = 1080;
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
				Log.i(TAG, "ACTION_MOVE - isLeftLayoutVisible --rightLayoutParams.leftMargin" + rightLayoutParams.leftMargin);
				if (rightLayoutParams.leftMargin > maxleftEdge) {
					rightLayoutParams.leftMargin = maxleftEdge;
				}
				rightLayoutParams.width = screenWidth;
				rightLayout.setLayoutParams(rightLayoutParams);
			}

			if (isLeftLayoutVisible && -moveDistanceX >= touchSlop) {
				isSliding = true;
				rightLayoutParams.leftMargin = maxleftEdge + moveDistanceX;
				Log.i(TAG, "ACTION_MOVE - !!!isLeftLayoutVisible --rightLayoutParams.leftMargin" + rightLayoutParams.leftMargin);
				if (rightLayoutParams.leftMargin < minLeftEdge) {
					rightLayoutParams.leftMargin = minLeftEdge;
				}
				rightLayoutParams.width = screenWidth;
				rightLayout.setLayoutParams(rightLayoutParams);
			}
			break;

		case MotionEvent.ACTION_UP:
			xUp = (int) event.getRawX();
			int upDistanceX = xUp - xDown;
			if (isSliding) {
				// 手指抬起时，进行判断当前手势的意图，从而决定是滚动到左侧布局，还是滚动到右侧布局
				if (wantToShowLeftLayout()) {
					if (shouldScrollToShowLeftLayout()) {
						Log.i(TAG, "ACTION_UP - wantToShowLeftLayout -- shouldScrollToShowLeftLayout");
						scrollToShowLeftLayout();
					} else {
						Log.i(TAG, "ACTION_UP - wantToShowLeftLayout -- scrollToShowRightLayout");
						scrollToShowRightLayout();
					}
				} else if (wantToShowRightLayout()) {
					if (shouldScrollToShowRightLayout()) {
						Log.i(TAG, "ACTION_UP - wantToShowRightLayout -- shouldScrollToShowRightLayout");
						scrollToShowRightLayout();
					} else {
						scrollToShowLeftLayout();
						Log.i(TAG, "ACTION_UP - wantToShowRightLayout -- scrollToShowLeftLayout");
					}
				}
			} else {
				if (isLeftLayoutVisible) {
					Log.i(TAG, "ACTION_UP - !!!!isSliding scrollToShowLeftLayout");
					scrollToShowLeftLayout();
				} else {
					Log.i(TAG, "ACTION_UP - !!!!isSliding scrollToShowRightLayout");
					scrollToShowRightLayout();
				}
			}
			// else if (upDistanceX < touchSlop && isLeftLayoutVisible) {
			// scrollToShowRightLayout();
			// }
			recycleVelocityTracker();
			break;
		}
		if (v.isEnabled()) {
			if (isSliding) {
				unFocusBindView();
				return true;
			}
			if (isLeftLayoutVisible) {
				return true;
			}
			return false;
		}
		return true;
	}

	/**
	 * 使用可以获得焦点的控件在滑动的时候失去焦点。
	 */
	private void unFocusBindView() {
		if (mBindView != null) {
			mBindView.setPressed(false);
			mBindView.setFocusable(false);
			mBindView.setFocusableInTouchMode(false);
		}
	}

	private boolean shouldScrollToShowRightLayout() {
		return xDown - xUp > leftLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private boolean shouldScrollToShowLeftLayout() {
		return xUp - xDown > leftLayoutParams.width / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private boolean wantToShowLeftLayout() {
		return xUp - xDown > 0 && !isLeftLayoutVisible;
	}

	private boolean wantToShowRightLayout() {
		return xUp - xDown < 0 && isLeftLayoutVisible;
	}

	public void scrollToShowLeftLayout() {
		new ScrollTask().execute(30);
	}

	public void scrollToShowRightLayout() {
		new ScrollTask().execute(-30);
	}

	class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... speed) {
			int leftMargin = rightLayoutParams.leftMargin;
			Log.i(TAG, "doInBackground leftMargin = " + leftMargin);
			// 根据传入的速度来滚动界面，当滚动到达左边界或右边界时，跳出循环。
			while (true) {
				leftMargin = leftMargin + speed[0];
				if (leftMargin <= minLeftEdge) {
					leftMargin = minLeftEdge;
					break;
				}
				if (leftMargin >= maxleftEdge) {
					leftMargin = maxleftEdge;
					break;
				}
				Log.i(TAG, "content width ---------------------" + rightLayout.getWidth());
				publishProgress(leftMargin);
				// 为了要有滚动效果产生，每次循环使线程睡眠20毫秒，这样肉眼才能够看到滚动动画。
				sleep(15);
			}
			if (speed[0] > 0) {
				isLeftLayoutVisible = true;
			} else {
				isLeftLayoutVisible = false;
			}
			isSliding = false;
			Log.i(TAG, "doInBackground over leftMargin = " + leftMargin);
			return leftMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			Log.i(TAG, "onProgressUpdate leftMargin = " + leftMargin[0]);
			rightLayoutParams.leftMargin = leftMargin[0];
			rightLayoutParams.width = screenWidth;
			rightLayout.setLayoutParams(rightLayoutParams);
			unFocusBindView();
		}

		@Override
		protected void onPostExecute(Integer leftMargin) {
			Log.i(TAG, "onPostExecute leftMargin = " + leftMargin);
			rightLayoutParams.leftMargin = leftMargin;
			rightLayout.setLayoutParams(rightLayoutParams);
		}
	}

	public void setScrollEvent(View bindView) {
		mBindView = bindView;
		mBindView.setOnTouchListener(this);
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

	public boolean isLeftLayoutVisible() {
		return isLeftLayoutVisible;
	}

}
