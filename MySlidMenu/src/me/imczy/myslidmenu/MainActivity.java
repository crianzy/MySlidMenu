package me.imczy.myslidmenu;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MainActivity extends ActionBarActivity implements OnTouchListener {
	/**
	 * 滚动显示和隐藏menu时，手指滑动需要达到的速度。
	 */
	public static final int SNAP_VELOCITY = 200;
	LinearLayout content;
	LinearLayout menu;

	LinearLayout.LayoutParams menuParams;
	LinearLayout.LayoutParams contentParams;

	int screenWidth;
	int menuPadding = 100;
	int leftEdge;

	float xDown;
	float xMove;
	float xUp;

	boolean isMenuVisable;
	int rightEdge = 0;

	VelocityTracker mVelocityTracker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		content.setOnTouchListener(this);
	}

	public void initView() {
		content = (LinearLayout) findViewById(R.id.content);
		menu = (LinearLayout) findViewById(R.id.menu);
		screenWidth = this.getWindowManager().getDefaultDisplay().getWidth();

		menuParams = (LayoutParams) menu.getLayoutParams();
		menuParams.width = screenWidth - menuPadding;
		leftEdge = -menuParams.width;
		menuParams.leftMargin = leftEdge;
		content.getLayoutParams().width = screenWidth;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		createVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDown = event.getRawX();
			break;
		case MotionEvent.ACTION_MOVE:
			xMove = event.getRawX();
			int distanceX = (int) (xMove - xDown);
			if (isMenuVisable) {
				menuParams.leftMargin = distanceX;
			} else {
				menuParams.leftMargin = leftEdge + distanceX;
			}

			if (menuParams.leftMargin < leftEdge) {
				menuParams.leftMargin = leftEdge;
			} else if (menuParams.leftMargin > rightEdge) {
				menuParams.leftMargin = rightEdge;
			}
			menu.setLayoutParams(menuParams);
			break;
		case MotionEvent.ACTION_UP:
			xUp = event.getRawX();
			if (wantToShowMeni()) {
				if (shouldScrollToMenu()) {
					scrollToMenu();
				} else {
					scrollToContent();
				}
			} else if (wantToShowContent()) {
				if (shouldScrollToContent()) {
					scrollToContent();
				} else {
					scrollToMenu();
				}
			}
			recucleVelocityTracker();
			break;
		default:
		}
		return true;
	}

	private boolean wantToShowMeni() {
		return xUp - xDown > 0 && !isMenuVisable;
	}

	private boolean wantToShowContent() {
		return xUp - xDown < 0 && isMenuVisable;
	}

	private boolean shouldScrollToMenu() {
		return xUp - xDown > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	public boolean shouldScrollToContent() {
		return xDown - xUp + menuPadding > screenWidth / 2 || getScrollVelocity() > SNAP_VELOCITY;
	}

	private void scrollToMenu() {
		new ScrollTask().execute(30);
	}

	private void scrollToContent() {
		new ScrollTask().execute(-30);
	}

	private int getScrollVelocity() {
		mVelocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) mVelocityTracker.getXVelocity();
		return Math.abs(velocity);
	}

	class ScrollTask extends AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... speed) {
			int leftMargin = menuParams.leftMargin;
			while (true) {
				leftMargin = leftMargin + speed[0];
				if (leftMargin > rightEdge) {
					leftMargin = rightEdge;
					break;
				}
				if (leftMargin < leftEdge) {
					leftMargin = leftEdge;
					break;
				}
				publishProgress(leftMargin);
				sleep(10);
				if (speed[0] > 0) {
					isMenuVisable = true;
				} else {
					isMenuVisable = false;
				}
			}
			return leftMargin;
		}

		@Override
		protected void onProgressUpdate(Integer... leftMargin) {
			menuParams.leftMargin = leftMargin[0];
			menu.setLayoutParams(menuParams);
		}

		@Override
		protected void onPostExecute(Integer result) {
			menuParams.leftMargin = result;
			menu.setLayoutParams(menuParams);
		}
	}

	public void createVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void recucleVelocityTracker() {
		mVelocityTracker.recycle();
		mVelocityTracker = null;
	}

	public void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
