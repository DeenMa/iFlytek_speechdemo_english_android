package com.iflytek.voicedemo;

import android.app.Application;
import com.iflytek.cloud.Setting;
import com.iflytek.cloud.SpeechUtility;

public class SpeechApp extends Application {

	@Override
	public void onCreate() {
		// Calls at the entrance of the application, to avoid the internal memory being to low and killing background process
		// entering the Activity through previous intent and causing the SpeechUtility object to be null
		// If you call initialization in Application, you should register the application in Mainifest
		// Note: In the non-main process call, this interface will return null object, if you need to use the speech feature in the non-main process, please add the following parameter
		// SpeechConstant.FORCE_LOGIN+"=true"
		// The parameters are delimited with comma “,”
		// Sets your applied application appid. Do not add space and empty escape character between '=’ and appid
		
		// Note: appid  must be consistent with the downloaded SDK, otherwise the 10407 error will occur
		
		SpeechUtility.createUtility(SpeechApp.this, "appid=" + getString(R.string.app_id));
			
		// The following statement is used to switch log on/off (default value is on), when set to false, the speech cloud SDK log print is off
		// Setting.setShowLog(false);
		super.onCreate();
	}
	
}
