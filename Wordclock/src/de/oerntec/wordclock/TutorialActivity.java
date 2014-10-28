package de.oerntec.wordclock;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class TutorialActivity extends Activity{
	private TextView textView, headerView;
	private Button continueButton;
	private ImageView iconView;
	private int page=0;
	private String headerArray[]={"Main screen", "Managing the connection", "Settings screen", "WordClock debug output"};
	private String textArray[]={
			"Tap on words to select them,\nthen choose a color on the bottom slider or a random color with the dice in the actionbar.\n" +
			"\nLong tap a word to copy its color to the selected ones.\n" +
			"\nShake the phone to either deselect or select all words.",
			"Tap the Bluetooth icon in the action bar to connect or disconnect the clock.\n\n" +
			"Upon closing the app or putting the phone in standby, the connection will be aborted to save battery.",
			"Shake Treshold:" +
					"\nThe higher this value is, the harder you will have to shake the phone to register a shake.\n" +
			"\nShake Delay:\n" +
					"If the phone often detects more than one shake, raise this value.\n" +
			"\nRefresh delay:\n" +
					"This is the amount of time the phone waits before sending your new colors to the clock. Raise if inexplicable shit happens." +
			"\n\nSleep times:" +
			"		\nSelect a timespan during which the clock will be off.",
			"Everything is white but\n" +
			"\n\"ONE:\"The RTC is present, but not started.\nConsult Arne." +
			"\n\"TWO:\"The RTC is not present.\nConsult Arne." +
			"\n\"THREE:\"The RTC is not writeable.\nConsult Arne." +
			"\n\"FOUR:\"Garbage data. Reconnect phone to WordClock, consult Arne if problem persists."
	};
	private int[] iconArray={R.drawable.ic_home, R.drawable.ic_action_bluetooth_connected, R.drawable.ic_action_settings, R.drawable.ic_warning};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//show this tutorial only once
		SharedPreferences data =this.getSharedPreferences("share", MODE_MULTI_PROCESS);
		data.edit().putBoolean("firstStart", false).commit();
		//init adapter
		setContentView(R.layout.activity_tutorial);
		headerView=(TextView) findViewById(R.id.headerView);
		textView=(TextView) findViewById(R.id.textView);
		continueButton=(Button) findViewById(R.id.continueButton);
		iconView=(ImageView) findViewById(R.id.iconView);
		headerView.setText(headerArray[0]);textView.setText(textArray[0]);iconView.setImageResource(iconArray[0]);
	}
	
	public void continueClicked(View v){
		page++;
		if(page==textArray.length)
			finish();
		else{
		if(page==textArray.length-1)
			continueButton.setText("Finish");
		Log.i("tutPage", String.valueOf(page));
		headerView.setText(headerArray[page]);
		textView.setText(textArray[page]);
		iconView.setImageResource(iconArray[page]);
		}
	}

}
