package de.oerntec.wordclock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	Context SettingsActivity = this;
	RadioGroup radio;
	SeekBar tresholdBar, delayBar, brightnessBar, refreshRateBar;
	TextView delayView, tresholdView, brightnessView, refreshRateView;
	//EditText macText;
	int delay, treshold, initByte, refreshRate;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		//action bar navigation
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		prefs = this.getSharedPreferences("share", MODE_MULTI_PROCESS);
		delay=prefs.getInt("shakeDelay", 700);
		treshold=prefs.getInt("shakeTreshold", 1000);
		refreshRate=prefs.getInt("refreshRate", 100);
		initByte=prefs.getInt("initByte", 101);
		
		radio=(RadioGroup)findViewById(R.id.radioGroup1);
		
		switch(initByte){
		case 101:
			radio.check(R.id.noSleep);
			break;
		case 102:
			radio.check(R.id.longSleep);
			break;
		case 103:
			radio.check(R.id.shortSleep);
			break;
		}

		tresholdBar=(SeekBar) findViewById(R.id.shakeTresholdBar);
		delayBar=(SeekBar) findViewById(R.id.shakeDelayBar);
		refreshRateBar=(SeekBar) findViewById(R.id.refreshRateBar);

		delayView=(TextView) findViewById(R.id.shakeDelay);
		tresholdView=(TextView) findViewById(R.id.shakeTreshold);
		refreshRateView=(TextView) findViewById(R.id.refreshRate);

		tresholdBar.setProgress(treshold);
		delayBar.setProgress(delay);
		refreshRateBar.setProgress(refreshRate);

		delayView.setText(String.valueOf(delay));
		tresholdView.setText(String.valueOf(treshold));
		refreshRateView.setText(String.valueOf(refreshRate)+" ms");
		
		tresholdBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override       
			public void onStopTrackingTouch(SeekBar seekBar) {}       

			@Override       
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				treshold=progress;
				tresholdView.setText(String.valueOf(progress));
			} 
		});
		delayBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override       
			public void onStopTrackingTouch(SeekBar seekBar) {}       

			@Override       
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				delay=progress;
				delayView.setText(String.valueOf(progress)+" ms");
			} 
		});
		refreshRateBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			boolean reset=false;
			@Override       
			public void onStopTrackingTouch(SeekBar seekBar) {
				if(reset){
					seekBar.setProgress(90);
					Toast.makeText(SettingsActivity, "Minimum value 90ms", Toast.LENGTH_SHORT).show();
				}
			}       

			@Override       
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				if(progress<90){
					reset=true;
				}
				else{
					refreshRate=progress;
					refreshRateView.setText(String.valueOf(progress)+" ms");
					reset=false;
				}
			} 
		});
	}
	
	public void sleepRadio(View v){
		switch(v.getId()){
		case R.id.noSleep:initByte=101;break;
		case R.id.longSleep:initByte=102;break;
		case R.id.shortSleep:initByte=103;break;
		}
	}
	
	public void tutorialClicked(View v) {
		Intent intent = new Intent(this, TutorialActivity.class);
		startActivity(intent);
	}

	private void save(){
		//save to sprefs
		Editor sprefEdit=prefs.edit();
		sprefEdit.putInt("shakeDelay", delay);
		sprefEdit.putInt("shakeTreshold", treshold);
		sprefEdit.putInt("refreshRate", refreshRate);
		sprefEdit.putInt("initByte", initByte);
		sprefEdit.commit();
	}

	@Override
	protected void onPause() {
		super.onPause();
		save();
	}
}
