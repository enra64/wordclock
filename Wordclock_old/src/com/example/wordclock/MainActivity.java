package com.example.wordclock;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

@SuppressLint("ClickableViewAccessibility")
@SuppressWarnings("deprecation")
public class MainActivity<CurrentActivity> extends Activity implements SensorListener{
	private DrawingView drawView;
	SharedPreferences data, sharedSettings;
	//shaky
	SensorManager sensorMgr;
	int timeTreshold=100, shakeTreshold, shakeDelay;
	// ontouch handling
	long startTime = 0, lastUpdate=0;
	float last_x=0, last_y=0, last_z=0;
	//bt
	TimerTask updateTask;
	Timer myTimer;
	blueComm bt;
	int refreshDelay;
	// ambilwarna
	int color, alpha;
	View viewHue;
	final float[] currentColorHsv = new float[3];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//shaky 
		sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorMgr.registerListener(this, SensorManager.SENSOR_ACCELEROMETER, SensorManager.SENSOR_DELAY_GAME);
		//drawview init
		drawView = (DrawingView) findViewById(R.id.drawing);
		//load preferences
		sharedSettings = this.getSharedPreferences("share", MODE_MULTI_PROCESS);
		//load data
		data = this.getSharedPreferences("MainActivity", MODE_MULTI_PROCESS);
		//init bt
		String macAddress=sharedSettings.getString("macAddress", "20:14:04:16:14:70");
		bt=new blueComm(this, macAddress);
		//shaky data
		refreshDelay=sharedSettings.getInt("refreshRate", 100);
		shakeDelay=sharedSettings.getInt("shakeDelay", 700);
		shakeTreshold=sharedSettings.getInt("shakeTreshold", 1000);
		if(shakeDelay==0)
			shakeDelay=700;
		if(shakeTreshold==0)
			shakeTreshold=1000;
		//led data
		int[] tempColors=new int[26];
		for(int i=0;i<26;i++)
			tempColors[i]=data.getInt("color"+i,Color.RED);
		drawView.setColors(tempColors);
		// ambil
		viewHue = findViewById(R.id.ambilwarna_viewHue);
		color = 0xff0000ff | 0xff000000;

		viewHue.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_MOVE
						|| event.getAction() == MotionEvent.ACTION_DOWN
						|| event.getAction() == MotionEvent.ACTION_UP) {

					float y = event.getX();
					if (y < 0.f) y = 0.f;
					if (y > viewHue.getMeasuredWidth()) y = viewHue.getMeasuredWidth() - 0.001f; // to avoid looping from end to start.
					float hue = 360.f - 360.f / viewHue.getMeasuredWidth() * y;
					if (hue==360.f) hue = 0.f;
					setHue(hue);
					final int argb = Color.HSVToColor(currentColorHsv);
					drawView.setColor(argb);
					return true;
				}
				return false;
			}
		});
		if(sharedSettings
				.getBoolean("firstStart", true)){
			Intent intent = new Intent(this, wordclockTutorial.class);
			startActivity(intent);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDelay=sharedSettings.getInt("refreshRate", 100);
		shakeDelay=sharedSettings.getInt("shakeDelay", 700);
		shakeTreshold=sharedSettings.getInt("shakeTreshold", 1000);
		if(shakeDelay==0)
			shakeDelay=700;
		if(shakeTreshold==0)
			shakeTreshold=1000;
	}

	@Override
	protected void onPause() {
		super.onPause();
		Editor sprefEdit=data.edit();
		int[] tempColors=new int[25];
		tempColors=drawView.getWordColors();
		for(int i=0;i<25;i++)
			sprefEdit.putInt("color"+i, tempColors[i]);
		sprefEdit.commit();
		if (myTimer!=null){
			myTimer.cancel();
			myTimer = null;
		}
		bt.close();
		invalidateOptionsMenu();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		if(bt.btConnected)
			menu.getItem(0).setIcon(R.drawable.ic_action_bluetooth_connected);
		else
			menu.getItem(0).setIcon(R.drawable.ic_action_bluetooth_disconnected);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.menu_connect:
			connectBluetooth();
			invalidateOptionsMenu();
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void connectBluetooth(){
		int answer=bt.open();
		switch(answer){
		case 1:
		case 2:
			Log.e("bt open", "fucked up with code:" + answer);
			break;
		case 3:
			toast("Wordclock not paired (Code 0808)");Log.e("bt open", "code 3; wordclock not paired");
			break;
		case 4:
			toast("Wordclock paired, but not within reach.\nKill and restart the app if the Clock should be in reach.", true);
			break;
		case 5:
			toast("disconnected wordclock");
			if (myTimer!=null){
				myTimer.cancel();
				myTimer = null;
			}
		break;
		}
		if(answer==0){//install timer to update clock
			if(myTimer != null)
				myTimer.cancel();
			myTimer=new Timer();
			updateTask = new clockUpdateTask(drawView, bt, sharedSettings);
			myTimer.schedule(updateTask, 0, refreshDelay);
		}
	}

	private void toast(String message){
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	private void toast(String message, boolean lengthLong){
		int l=Toast.LENGTH_SHORT;
		if(lengthLong)
			l=Toast.LENGTH_LONG;
		Toast.makeText(this, message, l).show();
	}

	private void setHue(float hue) {
		currentColorHsv[0] = hue;currentColorHsv[1]=1;currentColorHsv[2]=1;
	}

	public void onSensorChanged(int sensor, float[] values) {
		if (sensor == SensorManager.SENSOR_ACCELEROMETER) {
			long curTime = System.currentTimeMillis();
			// only allow one update every 100ms.
			if ((curTime - lastUpdate) > timeTreshold) {
				timeTreshold=100;
				long diffTime = (curTime - lastUpdate);
				lastUpdate = curTime;

				float x = values[SensorManager.DATA_X];
				float y = values[SensorManager.DATA_Y];
				float z = values[SensorManager.DATA_Z];

				float speed = Math.abs(x+y+z-last_x-last_y-last_z) / diffTime * 10000;

				if (speed > shakeTreshold) {
					Log.i("shake", "speed: "+speed);
					drawView.invertZero();
					timeTreshold=shakeDelay;
				}
				last_x = x;
				last_y = y;
				last_z = z;
			}
		}
	}

	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
	}
}