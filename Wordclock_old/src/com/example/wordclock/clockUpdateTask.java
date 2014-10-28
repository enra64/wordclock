package com.example.wordclock;

import java.util.Calendar;
import java.util.TimerTask;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

class clockUpdateTask extends TimerTask {
	protected DrawingView drawView;
	protected SharedPreferences sPref;
	protected blueComm bt;
	protected Calendar cal;
	
	private final static String TAG="cUpdateTask";
	
	public clockUpdateTask(DrawingView dv, blueComm bc, SharedPreferences sp){
		drawView=dv;
		bt=bc;
		sPref=sp;
	}
	
	@Override
	public void run() {
		byte[] data=new byte[80];int[] colorInt=new int[25];
    	colorInt=drawView.getWordColors();
    	//Log.i(TAG, "colorInt: "+colorInt);
    	int counter=1;
    	data[0]=(byte) 170;
    	for(int i=0; i<colorInt.length;i++){
    		data[counter]=(byte) Color.red(colorInt[i]);counter++;
    		data[counter]=(byte) Color.green(colorInt[i]);counter++;
    		data[counter]=(byte) Color.blue(colorInt[i]);counter++;
    	}
//    	hms
    	cal=Calendar.getInstance();
    	data[76]=(byte) cal.get(Calendar.HOUR_OF_DAY);
    	data[77]=(byte) cal.get(Calendar.MINUTE);
    	data[78]=(byte) cal.get(Calendar.SECOND);
    	data[79]=(byte) sPref.getInt("brightness", 100);
//    	Log.i(TAG, "hms,bness: "+data[76]+data[77]+data[78]+data[79]);
    	if(!bt.sendData(data))
    		Log.i(TAG, "bt upload failed");
	}

}