package org.porkholt.PHRemote;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.content.SharedPreferences;

class PHPortFilter implements InputFilter {

	@Override
	public CharSequence filter(CharSequence source, int start, int end,
			Spanned dest, int dstart, int dend) {
		int keep = 5 - (dest.length() - (dend - dstart));
		if (keep <=0)
			return "";
		else
		{
			int en = start + keep;
			if (en>end) 
				en = end;
			CharSequence sub = source.subSequence(start, en);
			String fin = dest.subSequence(0, dstart).toString() + sub.toString() + dest.subSequence(dend, dest.length()).toString();
			int nr;
			try {
				nr = Integer.parseInt(fin);
			} catch (Exception e) {
				nr = -1;
			}
			if (nr<=0 || nr >0xffff)
				return "";
			if (keep >= end-start)
				return null;
			else
				return sub;
		}
	}	
}

public class PHRemotePrefs extends Activity {

	private EditText portField;
	private EditText hostField;
	private ToggleButton touchToggle;
	private ToggleButton accelToggle;
	private ToggleButton groupToggle;
	
	@Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.prefs);
		 portField = (EditText) findViewById(R.id.portField);
		 hostField = (EditText) findViewById(R.id.hostField);
		 touchToggle = (ToggleButton) findViewById(R.id.touchToggle);
		 accelToggle = (ToggleButton) findViewById(R.id.accelToggle);
		 groupToggle = (ToggleButton) findViewById(R.id.groupToggle);
		 portField.setFilters(new InputFilter[]{new PHPortFilter()});
	 }
	
	@Override
	public void onResume() {
		SharedPreferences prefs = getSharedPreferences("org.porkholt.PHRemote",0);
		portField.setText(prefs.getString("port", RemoteController.DEFAULT_PORT));
		hostField.setText(prefs.getString("host", RemoteController.DEFAULT_HOST));
		touchToggle.setChecked(prefs.getBoolean("useTouch", RemoteController.DEFAULT_USE_TOUCH));
		accelToggle.setChecked(prefs.getBoolean("useAccel", RemoteController.DEFAULT_USE_ACCEL));
		groupToggle.setChecked(prefs.getBoolean("groupPacks", RemoteController.DEFAULT_GROUP_PACKS));
		super.onResume();
	}
	
	public void onPause() {
		SharedPreferences.Editor prefs = getSharedPreferences("org.porkholt.PHRemote",0).edit();
		prefs.putString("host", hostField.getText().toString());
		prefs.putString("port", portField.getText().toString());
		prefs.putBoolean("useTouch", touchToggle.isChecked());
		prefs.putBoolean("useAccel", accelToggle.isChecked());
		prefs.putBoolean("groupPacks", groupToggle.isChecked());
		prefs.commit();
		super.onPause();
	}

}
