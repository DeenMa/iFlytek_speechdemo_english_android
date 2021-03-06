package com.iflytek.voicedemo;

import com.iflytek.sunflower.FlowerCollector;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private Toast mToast;

	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);
		
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		SimpleAdapter listitemAdapter = new SimpleAdapter();
		((ListView) findViewById(R.id.listview_main)).setAdapter(listitemAdapter);
	}

	@Override
	public void onClick(View view) {
		int tag = Integer.parseInt(view.getTag().toString());
		Intent intent = null;
		switch (tag) {
		case 0:
			// Long Form ASR
			intent = new Intent(MainActivity.this, IatDemo.class);
			break;
		case 1:
			// Grammar recognition
			intent = new Intent(MainActivity.this, AsrDemo.class);
			break;
		case 2:
			// Semantic understanding
			intent = new Intent(MainActivity.this, UnderstanderDemo.class);
			break;
		case 3:
			// Text-to-Speech (TTS)
			intent = new Intent(MainActivity.this, TtsDemo.class);
			break;
		case 4:
			// Speech evaluation
			intent = new Intent(MainActivity.this, IseDemo.class);
			break;
		case 5:
			// Wakeup
			showTip("Please login: http://www.xfyun.cn/ to download and experience.");
			break;
		case 6:
			// Voiceprint
		default:
			showTip("Not supported yet");
			break;
		}
		
		if (intent != null) {
			startActivity(intent);
		}
	}

	// Menu list
	String items[] = { "Automatic Speech Recognition", "Grammar Recognition", "Semantic Understanding", "Text to Speech",
			"Speech evaluation", "Voice wakeup", "Voiceprint" };

	private class SimpleAdapter extends BaseAdapter {
		public View getView(int position, View convertView, ViewGroup parent) {
			if (null == convertView) {
				LayoutInflater factory = LayoutInflater.from(MainActivity.this);
				View mView = factory.inflate(R.layout.list_items, null);
				convertView = mView;
			}
			
			Button btn = (Button) convertView.findViewById(R.id.btn);
			btn.setOnClickListener(MainActivity.this);
			btn.setTag(position);
			btn.setText(items[position]);
			
			return convertView;
		}

		@Override
		public int getCount() {
			return items.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}
	}

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	@Override
	protected void onResume() {
		// Open statistical: Statistical analysis of mobile data
		FlowerCollector.onResume(MainActivity.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Open statistical: Statistical analysis of mobile data
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(MainActivity.this);
		super.onPause();
	}
}
