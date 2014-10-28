package de.oerntec.wordclock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class blueComm {
	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;
	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition, counter;
	volatile boolean stopWorker;
	Activity act;
	String macAddress;

	public static final int BT_FUCKED_UP = 666;
	public boolean btConnected = false;

	public blueComm(Context c, String mac) {
		act = (Activity) c;
		macAddress = mac;
	}

	private void beginListenForData() {
		final String TAG = "beginListenForData";
		final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
									// character

		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {
					try {
						int bytesAvailable = mmInputStream.available();
						if (bytesAvailable > 0) {
							Log.d(TAG,
									"bytes Available: "
											+ String.valueOf(bytesAvailable));
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								Log.d("received byte: ", String.valueOf(b));
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0,
											encodedBytes, 0,
											encodedBytes.length);
									final String data = new String(
											encodedBytes, "US-ASCII");
									readBufferPosition = 0;

									handler.post(new Runnable() {
										public void run() {
											Log.d("runnable receive", data);
										}
									});
								} else {
									readBuffer[readBufferPosition] = b;
								}
							}
						}
						// else
						// Log.d(TAG, "no bytes Available");
					} catch (IOException ex) {
						stopWorker = true;
					}
				}
			}
		});
		workerThread.start();
	}

	public int open() {
		if (btConnected) {
			close();
			return 5;
		}
		Log.i("btwc open", "bt open begin");
		int returnCode = 0;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null)
			returnCode = 1;

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			act.startActivityForResult(enableBluetooth, 0);
			returnCode = 6;
		} 
		else {
			Set<BluetoothDevice> bondedSet = mBluetoothAdapter
					.getBondedDevices();
			if (bondedSet.size() > 0) {
				for (BluetoothDevice device : bondedSet) {
					String devAddress = device.getAddress();
					if (devAddress.equals(macAddress)) {
						mmDevice = device;
						// stolen from blueterm, allows insecure connections (i
						// have no idea what that means)
						try {
							Method method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
							mmSocket = (BluetoothSocket) method.invoke(device, 1);
							mmSocket.connect();
							mmOutputStream = mmSocket.getOutputStream();
							mmInputStream = mmSocket.getInputStream();
							Log.i("bt wordclock", "connected!");
							beginListenForData();
							btConnected = true;
						} catch (Exception e) {
							Log.e("btwc", "wordclock paired, but not within reach");
							returnCode = 4;
						}
					}
				}
			} else
				returnCode = 3;
		}
		return returnCode;
	}

	public boolean sendData(byte[] msg) {
		String stringMsg = "";
		for (int i = 0; i < msg.length; i++)
			stringMsg = stringMsg + String.valueOf(msg[i] & 0xFF) + ";";
		if (mmOutputStream != null) {
			try {
				// Log.i("bt write input", stringMsg);
				mmOutputStream.write(msg);
			} catch (IOException e) {
				Log.w("btwc", "outputstream null");
				return false;
			}
		} else
			return false;
		return true;
	}

	public void close() {
		stopWorker = true;
		try {
			if (mmOutputStream != null)
				mmOutputStream.close();
			if (mmInputStream != null)
				mmInputStream.close();
			if (mmSocket != null)
				mmSocket.close();
			btConnected = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}