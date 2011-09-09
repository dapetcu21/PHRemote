package org.porkholt.PHRemote;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.MotionEvent;
import android.widget.TextView;
import android.graphics.Matrix;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.hardware.SensorEventListener;
import android.hardware.Sensor;
import android.content.Context;

public class PHRemoteActivity extends Activity implements OnTouchListener, OnGlobalLayoutListener, SensorEventListener {
    
	private SensorManager sensorManager;
	private Sensor sensor;
	private RemoteController remote;
	private TextView textView;
	private View mainView;
	private Matrix scaleMatrix;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        
		remote = new RemoteController();
		
		mainView = (View)findViewById(R.id.captureView);
		textView = (TextView)findViewById(R.id.textView);

		mainView.setOnTouchListener(this);
		
        scaleMatrix = new Matrix();
        scaleMatrix.reset();
        
        ViewTreeObserver vto = mainView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(this);
    }
    
    public void onGlobalLayout() {
        scaleMatrix = new Matrix();
        //scaleMatrix.setScale(1.0f, -1.0f);
        //scaleMatrix.postTranslate(0, mainView.getHeight());
        scaleMatrix.reset();
        remote.setWidth(mainView.getWidth());
        remote.setHeight(mainView.getHeight());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    private void invokePreferencesActivity() 
    {
    	Intent i = new Intent(this,PHRemotePrefs.class);
    	startActivity(i);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.prefsMenu:
            invokePreferencesActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void onResume()
    {
    	SharedPreferences prefs = getSharedPreferences("org.porkholt.PHRemote",0);
		remote.setPort(prefs.getString("port", RemoteController.DEFAULT_PORT));
		remote.setHost(prefs.getString("host", RemoteController.DEFAULT_HOST));
		remote.setUseTouchEvents(prefs.getBoolean("useTouch", RemoteController.DEFAULT_USE_TOUCH));
		remote.setUseAccelerometer(prefs.getBoolean("useAccel", RemoteController.DEFAULT_USE_ACCEL));
		remote.setGroupPackets(prefs.getBoolean("groupPacks", RemoteController.DEFAULT_GROUP_PACKS));
		try {
			remote.start();
			setErrorText(null);
		} catch (Exception e) {
			setErrorText(e.getMessage());
		}
		
		sensorManager.registerListener(this, sensor,
				SensorManager.SENSOR_DELAY_GAME);
		
    	super.onResume();
    }
    
    @Override
    public void onPause() 
    {
    	sensorManager.unregisterListener(this);
    	remote.stop();
    	
    	super.onPause();
    }
    
    private void setErrorText(String err) 
    {
    	textView.setText(err);
    }
    
    @Override
	public void onSensorChanged(SensorEvent event) {
    	if (!remote.isRunning()) return;
    	try {
    		remote.sendAccelerometerPacket(event.values[0], -event.values[1], event.values[2]);
    		setErrorText(null);
    	} catch (Exception e) {
    		setErrorText(e.getMessage());
    	}
	}
    
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	
	}
    
    @Override
    public boolean onTouch(View v, MotionEvent ev) 
    {
    	if (!remote.isRunning()) return false;
    	try {
	    	int state;
	        int pointer = ev.getActionIndex();
	        
	        switch(ev.getActionMasked())
	        {
	        case MotionEvent.ACTION_DOWN:
	        case MotionEvent.ACTION_POINTER_DOWN:
	            	state = RemoteController.STATE_DOWN;
	        	break;
	        case MotionEvent.ACTION_UP:
	        case MotionEvent.ACTION_POINTER_UP:
	        case MotionEvent.ACTION_OUTSIDE:
	            	state = RemoteController.STATE_UP;
	        	break;
	        case MotionEvent.ACTION_MOVE:
	        	state = RemoteController.STATE_MOVED;
	        	final int pc = ev.getPointerCount();
	        	for (int p=0; p<pc; p++)
	            {
	        		float pnt[] = new float[]{ev.getX(p), ev.getY(p)};
	                scaleMatrix.mapPoints(pnt);
	            	remote.sendTouchEventPacket(ev.getPointerId(p), state, (int)pnt[0], (int)pnt[1]);
	            } 
	        	return true;
	        case MotionEvent.ACTION_CANCEL:
	        	state = RemoteController.STATE_CANCELLED;
	        	break;
	        default:
	        	return false;
	        }
	        
	        float pnt[] = new float[]{ev.getX(pointer), ev.getY(pointer)};
	        scaleMatrix.mapPoints(pnt);
	        remote.sendTouchEventPacket(ev.getPointerId(pointer), state, (int)pnt[0], (int)pnt[1]);
	        setErrorText(null);
	        
	    	return true;
    	} catch (Exception e) {
    		setErrorText(e.getMessage());
    		return false;
    	}
    }
    
}