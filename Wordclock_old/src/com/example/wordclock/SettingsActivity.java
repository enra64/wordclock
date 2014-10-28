package com.example.wordclock;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	Context SettingsActivity = this;
	SeekBar tresholdBar, delayBar, brightnessBar, refreshRateBar;
	TextView delayView, tresholdView, brightnessView, refreshRateView, tutorialView;
	EditText macText;
	CheckBox introCheckBox;
	int delay, treshold, brightness, refreshRate;
	SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		prefs = this.getSharedPreferences("share", MODE_MULTI_PROCESS);
		delay=prefs.getInt("shakeDelay", 700);
		treshold=prefs.getInt("shakeTreshold", 1000);
		brightness=prefs.getInt("brightness", 100);
		refreshRate=prefs.getInt("refreshRate", 100);
		
		String macAddress=prefs.getString("macAddress", "20:14:04:16:14:70");
		
		boolean firstStart=prefs.getBoolean("firstStart", false);
		
		tresholdBar=(SeekBar) findViewById(R.id.shakeTresholdBar);
		delayBar=(SeekBar) findViewById(R.id.shakeDelayBar);
		brightnessBar=(SeekBar) findViewById(R.id.brightnessBar);
		refreshRateBar=(SeekBar) findViewById(R.id.refreshRateBar);
		
		delayView=(TextView) findViewById(R.id.shakeDelay);
		tresholdView=(TextView) findViewById(R.id.shakeTreshold);
		brightnessView=(TextView) findViewById(R.id.brightness);
		refreshRateView=(TextView) findViewById(R.id.refreshRate);
		
		tresholdBar.setProgress(treshold);
		delayBar.setProgress(delay);
		brightnessBar.setProgress(brightness);
		refreshRateBar.setProgress(refreshRate);
		
		delayView.setText(String.valueOf(delay));
		tresholdView.setText(String.valueOf(treshold));
		brightnessView.setText(String.valueOf(brightness)+" %");
		refreshRateView.setText(String.valueOf(refreshRate)+" ms");
		
		macText=(EditText) findViewById(R.id.macEditText);
		macText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		macText.setText(macAddress);
		
		tutorialView=(TextView) findViewById(R.id.tutorialView);
		introCheckBox=(CheckBox) findViewById(R.id.introCheckBox);
		if(firstStart)
			introCheckBox.setEnabled(true);
		else
			introCheckBox.setEnabled(false);
		
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
				delayView.setText(String.valueOf(progress));
			} 
		});
		brightnessBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			@Override       
		    public void onStopTrackingTouch(SeekBar seekBar) {}       

		    @Override       
		    public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
				brightness=progress;
				brightnessView.setText(String.valueOf(progress)+" %");
			} 
		});
		refreshRateBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			boolean reset=false;
			@Override       
		    public void onStopTrackingTouch(SeekBar seekBar) {
				if(reset){
					seekBar.setProgress(90);
					Toast.makeText(SettingsActivity, "Minimum value is 90ms", Toast.LENGTH_SHORT).show();
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
	
	public void introCheckBoxClicked(View v){
		introCheckBox.setChecked(!introCheckBox.isChecked());
	}
	
	private void save(){
		//save to sprefs
		String macAddress=String.valueOf(macText.getText());
		Editor sprefEdit=prefs.edit();
		sprefEdit.putInt("shakeDelay", delay);
		sprefEdit.putInt("shakeTreshold", treshold);
		sprefEdit.putInt("brightness", brightness);
		sprefEdit.putInt("refreshRate", refreshRate);
		
		sprefEdit.putBoolean("firstStart", introCheckBox.isChecked());
		
		if(macAddress.length()==17){
			int count=0;
			for(int i=0;i<macAddress.length();i++){
				if(macAddress.charAt(i)==':')
					count++;
			}
			if(count==5)
				sprefEdit.putString("macAddress", macAddress);
		}
		sprefEdit.commit();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		save();
	}
}
