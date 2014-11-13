package me.imczy.myslidmenu;

import java.util.LinkedList;

import me.imczy.myslidmenu.MyListView.OnRefreshListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	public static final String TAG = "MainActivity";

	MySlidLayout mySlidLayout;
	TextView textContent;
	Button menubtu;

	public static LinkedList<String> dataList;
	private MyListView myListView;
	private myAdapter myAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		mySlidLayout.setScrollEvent(myListView);
		
		menubtu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mySlidLayout.isLeftLayoutVisible()) {
					mySlidLayout.scrollToShowRightLayout();
				} else {
					mySlidLayout.scrollToShowLeftLayout();
				}
			}
		});

		myAdapter = new myAdapter(this);
		myListView.setAdapter(myAdapter);
		myListView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						dataList.addFirst("refresh msg");
						mHandler.sendEmptyMessage(0x1);
					}
				}).start();
			}
		});

	}

	void initView() {
		dataList = new LinkedList<>();
		for (int i = 0; i < 30; i++) {
			dataList.add("the " + i + " message");
		}
		Log.i(TAG, dataList.size() + "");
		mySlidLayout = (MySlidLayout) findViewById(R.id.slidingLayout);
		myListView = (MyListView) findViewById(R.id.myLsitView);
		menubtu = (Button) findViewById(R.id.menuButton);
	}

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0x1:
				myAdapter.notifyDataSetChanged();
				myListView.setSelection(0);
				myListView.onRefreshComplete();
				break;
			}
		};
	};
}
