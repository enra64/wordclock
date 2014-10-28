package com.example.wordclock;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

public class wordclockTutorial extends Activity{
	
	private int imgArray[] = {R.drawable.ambilwarna_arrow_down, R.drawable.ic_action_bluetooth_connected};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//show this tutorial only once
		SharedPreferences data =this.getSharedPreferences("share", MODE_MULTI_PROCESS);
		data.edit().putBoolean("firstStart", false).commit();
		//init adapter
		setContentView(R.layout.activity_tutorial);
		tutorialViewPagerAdapter adapter = new tutorialViewPagerAdapter(this, imgArray);
		ViewPager myPager = (ViewPager) findViewById(R.id.tutorialViewPager);
		myPager.setAdapter(adapter);
		myPager.setCurrentItem(0);
	}

}
