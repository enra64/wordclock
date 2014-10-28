package de.oerntec.wordclock;

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
	int initByte=101;
	
	public clockUpdateTask(DrawingView dv, blueComm bc, SharedPreferences sp, int i){
		drawView=dv;
		bt=bc;
		sPref=sp;
		if(initByte>100&&initByte<104)
			initByte=i;
		else
			Log.w("cUT", "incorrect initbyte");
	}
	
	@Override
	public void run() {
		int [] oldColorInt=drawView.getWordColors();
		byte[] data=new byte[79];
		int[] colorInt=new int[oldColorInt.length];
    	//conversion table for words
    	colorInt[0]=oldColorInt[24];
    	colorInt[1]=oldColorInt[23];
    	colorInt[2]=oldColorInt[22];
    	colorInt[3]=oldColorInt[21];
    	colorInt[4]=oldColorInt[19];
    	
    	colorInt[5]=oldColorInt[20];
    	colorInt[6]=oldColorInt[18];
    	colorInt[7]=oldColorInt[17];
    	colorInt[8]=oldColorInt[16];//ten
    	colorInt[9]=oldColorInt[14];//eleven
    	
    	colorInt[10]=oldColorInt[15];//nine
    	colorInt[11]=oldColorInt[13];//eight
    	colorInt[12]=oldColorInt[12];//twelve
    	colorInt[13]=oldColorInt[9];//one
    	colorInt[14]=oldColorInt[10];//two
    	
    	colorInt[15]=oldColorInt[11];
    	colorInt[16]=oldColorInt[8];//three
    	colorInt[17]=oldColorInt[7];//past
    	colorInt[18]=oldColorInt[6];//to
    	colorInt[19]=oldColorInt[4];//ten
    	
    	colorInt[20]=oldColorInt[5];//minutes
    	colorInt[21]=oldColorInt[3];//five
    	colorInt[22]=oldColorInt[2];//quarter
    	colorInt[23]=oldColorInt[0];//half
    	colorInt[24]=oldColorInt[1];//twenty
    	//Log.i(TAG, "colorInt: "+colorInt);
    	int counter=1;
    	data[0]=(byte)initByte;
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
    	//data[79]=(byte) sPref.getInt("brightness", 100);
//    	Log.i(TAG, "hms,bness: "+data[76]+data[77]+data[78]+data[79]);
    	//if(drawView.changingColors)
		if(!bt.sendData(data))
			Log.i(TAG, "bt upload failed");
	}

}
 