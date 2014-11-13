package me.imczy.myslidmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyListView extends ListView implements OnScrollListener {
	public static final String TAG = "RELEASE";

	// ���ֵ�ǰ������ˢ�»��Ǽ���
	public static final int REFRESH = 0;
	public static final int LOAD = 1;
	public static final int RADIO = 3;

	// ����PULL��RELEASE�ľ���Ĵ�С
	private static final int SPACE = 40;
	private Context mContext;
	private LayoutInflater inflater;

	private View head;
	private ImageView arrow;
	private TextView lastUpdate;
	private TextView refreshTip;
	private int headInitPaddingTop;
	private int headHeight;

	int firstVisibleItem;
	int scrollState;

	int startY;
	boolean isRecorded;

	private int state;
	private static final int NONE = 0;
	private static final int PULL = 1;
	private static final int RELEASE = 2;
	private static final int REFRESHING = 3;

	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	private OnRefreshListener onRefreshListener;

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		inflater = LayoutInflater.from(context);
		init();
	}

	private void init() {

		// ���ü�ͷ��Ч
		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(100);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(100);
		reverseAnimation.setFillAfter(true);

		head = inflater.inflate(R.layout.list_head, null);
		arrow = (ImageView) head.findViewById(R.id.arrow);
		lastUpdate = (TextView) head.findViewById(R.id.lastUpdate);
		refreshTip = (TextView) head.findViewById(R.id.refreshTip);
		this.addHeaderView(head);
		measureView(head);
		headHeight = head.getMeasuredHeight();
		headInitPaddingTop = head.getPaddingTop();
		setHeadTopPadding(-headHeight);
		// setHeadTopPadding(80);

		this.setOnScrollListener(this);
	}

	private void setHeadTopPadding(int topPadding) {
		head.setPadding(head.getPaddingLeft(), topPadding, head.getPaddingRight(), head.getPaddingBottom());
		head.invalidate();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if (firstVisibleItem == 0) {
				isRecorded = true;// Ҫ��ʼ����ˢ����
				startY = (int) ev.getY();
				oldY = startY;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if (state == PULL) {
				state = NONE;
				refreshHeaderViewByState();
			} else if (state == RELEASE) {
				state = REFRESHING;
				refreshHeaderViewByState();
				onRefresh();
			}
			isRecorded = false;
			break;
		case MotionEvent.ACTION_MOVE:
			whenMove(ev);
			int curY = (int) ev.getY();
			if (state == PULL || state == RELEASE) {
				if (curY < oldY) {
					return true;
				}
			}
			oldY = curY;
			break;
		}

		return super.onTouchEvent(ev);
	}

	private int oldY;

	private void whenMove(MotionEvent ev) {
		if (!isRecorded) {
			return;
		}
		int tmpY = (int) ev.getY();
		int space = tmpY - startY;
		int topPadding = space - headHeight;
		switch (state) {
		case NONE:
			if (space > 0) {
				state = PULL;
				refreshHeaderViewByState();
			}
			break;
		case PULL:
			setHeadTopPadding(topPadding);
			Log.i(TAG, "PULL");
			if (scrollState == SCROLL_STATE_TOUCH_SCROLL && space > headHeight + SPACE) {
				Log.i(TAG, "PULL  to RELEASE");
				state = RELEASE;
				refreshHeaderViewByState();
			}
			break;
		case RELEASE:
			Log.i(TAG, "RELEASE ");
			Log.i(TAG, "space =" + space);
			Log.i(TAG, "space -headHeight =" + space);
			setHeadTopPadding(topPadding);
			Log.i(TAG, "topPadding =" + head.getPaddingTop());
			if (space > 0 && head.getPaddingTop() < SPACE) {
				Log.i(TAG, "RELEASE to PULL");
				state = PULL;
				refreshHeaderViewByState();
			} else if (space <= 0) {
				state = NONE;
				refreshHeaderViewByState();
			}
			break;
		}
	}

	// ���ݵ�ǰ״̬������header
	private void refreshHeaderViewByState() {
		switch (state) {
		case NONE:
			setHeadTopPadding(-headHeight);
			refreshTip.setText("����ˢ��");
			arrow.clearAnimation();
			arrow.setImageResource(R.drawable.pull_to_refresh_arrow);
			break;
		case PULL:
			arrow.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);
			refreshTip.setText("����ˢ��");
			arrow.clearAnimation();
			arrow.setAnimation(reverseAnimation);
			break;
		case RELEASE:
			arrow.setVisibility(View.VISIBLE);
			refreshTip.setVisibility(View.VISIBLE);
			lastUpdate.setVisibility(View.VISIBLE);
			refreshTip.setText("�ɿ�ˢ��");
			arrow.clearAnimation();
			arrow.setAnimation(animation);
			break;
		case REFRESHING:
			setHeadTopPadding(headInitPaddingTop);
			arrow.clearAnimation();
			lastUpdate.setVisibility(View.GONE);
			arrow.setVisibility(View.GONE);
			refreshTip.setText("����ˢ��...");
			refreshTip.setVisibility(View.VISIBLE);
			break;
		}
	}

	public void onRefresh() {
		if (onRefreshListener != null) {
			onRefreshListener.onRefresh();
		}
	}

	// ��������ˢ�½�����Ļص�
	public void onRefreshComplete() {
		String currentTime = Utils.getCurrentTime();
		onRefreshComplete(currentTime);
	}

	public void onRefreshComplete(String updateTime) {
		lastUpdate.setText("������ʱ��" + Utils.getCurrentTime());
		state = NONE;
		refreshHeaderViewByState();
	}

	// OnScrollListener
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		this.scrollState = scrollState;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;
	}

	public void measureView(View child) {
		// ������Inflate ��ʱ�� û�а���ViewGropu ���� child ��ȡ����Layout��null ����������Ϊ�ļ�����
		child.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.WRAP_CONTENT));
		int widtSpec = MeasureSpec.makeMeasureSpec(1080, MeasureSpec.EXACTLY);
		int heightSpec = MeasureSpec.makeMeasureSpec(1570, MeasureSpec.EXACTLY);
		this.measureChild(child, widtSpec, heightSpec);
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;
	}

	/*
	 * ��������ˢ�½ӿ�
	 */
	public interface OnRefreshListener {
		public void onRefresh();
	}

	/*
	 * ������ظ���ӿ�
	 */
	public interface OnLoadListener {
		public void onLoad();
	}
}
