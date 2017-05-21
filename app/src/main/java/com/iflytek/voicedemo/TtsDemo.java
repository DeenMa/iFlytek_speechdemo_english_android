package com.iflytek.voicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.speech.setting.TtsSettings;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.sunflower.FlowerCollector;

public class TtsDemo extends Activity implements OnClickListener {
	private static String TAG = TtsDemo.class.getSimpleName(); 	
	// Text-to-Speech (TTS) object
	private SpeechSynthesizer mTts;

	// Default speaker
	private String voicer = "xiaoyan";
	
	private String[] mCloudVoicersEntries;
	private String[] mCloudVoicersValue ;
	
	// Buffer progress
	private int mPercentForBuffering = 0;
	// Playback progress
	private int mPercentForPlaying = 0;
	
	// Cloud / Local radio button
	private RadioGroup mRadioGroup;
	// Engine type
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// VoiceNote install assistant class
	ApkInstaller mInstaller ;
	
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ttsdemo);
		
		initLayout();
		// Initializes the synthesis object
		mTts = SpeechSynthesizer.createSynthesizer(TtsDemo.this, mTtsInitListener);
		
		// Cloud speaker name list
		mCloudVoicersEntries = getResources().getStringArray(R.array.voicer_cloud_entries);
		mCloudVoicersValue = getResources().getStringArray(R.array.voicer_cloud_values);
				
		mSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, MODE_PRIVATE);
		mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);
		
		mInstaller = new  ApkInstaller(TtsDemo.this);
	}

	/**
	 * Initialize Layout
	 */
	private void initLayout() {
		findViewById(R.id.tts_play).setOnClickListener(TtsDemo.this);
		findViewById(R.id.tts_cancel).setOnClickListener(TtsDemo.this);
		findViewById(R.id.tts_pause).setOnClickListener(TtsDemo.this);
		findViewById(R.id.tts_resume).setOnClickListener(TtsDemo.this);
		findViewById(R.id.image_tts_set).setOnClickListener(TtsDemo.this);
		findViewById(R.id.tts_btn_person_select).setOnClickListener(TtsDemo.this);
		
		mRadioGroup=((RadioGroup) findViewById(R.id.tts_rediogroup));
		mRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tts_radioCloud:
					mEngineType = SpeechConstant.TYPE_CLOUD;
					break;
				case R.id.tts_radioLocal:
					mEngineType =  SpeechConstant.TYPE_LOCAL;
					/**
					 * Selects Local synthesis
					 * Determines whether the VoiceNote has been installed, skips to the page to prompt installation if not installed
					 */
					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
						mInstaller.install();
					}
					break;
				default:
					break;
				}

			}
		} );
	}	

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.image_tts_set:
			if(SpeechConstant.TYPE_CLOUD.equals(mEngineType)){
				Intent intent = new Intent(TtsDemo.this, TtsSettings.class);
				startActivity(intent);
			}else{
				// The local setting skips to the VoiceNote
				if (!SpeechUtility.getUtility().checkServiceInstalled()) {
					mInstaller.install();
				}else {
					SpeechUtility.getUtility().openEngineSettings(null);				
				}
			}
			break;
		// Starts the synthesis
		// When the onCompleted  callback is received, the synthesis ends and the synthesized audio is generated
        // synthesized audio format: Only the pcm format is supported
		case R.id.tts_play:
			// Mobile data analysis, collects the synthesis start event
			FlowerCollector.onEvent(TtsDemo.this, "tts_play");
			
			String text = ((EditText) findViewById(R.id.tts_text)).getText().toString();
			// Sets parameter
			setParam();
			int code = mTts.startSpeaking(text, mTtsListener);
//			/** 
//			 * Only save the audio but not call the play interface, to call this interface, please comment out the startSpeaking interface
//			 * text: The text for synthesis, uri: The full path to the audio to be saved, listener: callback interface
//			*/
//			String path = Environment.getExternalStorageDirectory()+"/tts.pcm";
//			int code = mTts.synthesizeToUri(text, path, mTtsListener);
			
			if (code != ErrorCode.SUCCESS) {
				if(code == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
					// Skips to the page to prompt installation if not installed
					mInstaller.install();
				}else {
					showTip("TTS failed, error code: " + code);
				}
			}
			break;
		// Cancels the synthesis
		case R.id.tts_cancel:
			mTts.stopSpeaking();
			break;
		// Pauses play
		case R.id.tts_pause:
			mTts.pauseSpeaking();
			break;
		// Continues play
		case R.id.tts_resume:
			mTts.resumeSpeaking();
			break;
		// Selects speaker
		case R.id.tts_btn_person_select:
			showPresonSelectDialog();
			break;
		}
	}
	private int selectedNum = 0;
	/**
	 * Speaker selection
	 */
	private void showPresonSelectDialog() {
		switch (mRadioGroup.getCheckedRadioButtonId()) {
		// Select online synthesis
		case R.id.tts_radioCloud:			
			new AlertDialog.Builder(this).setTitle("TTS speaker options")
				.setSingleChoiceItems(mCloudVoicersEntries, // How many items contained in the radio box, what are their names?
						selectedNum, // Default option
						new DialogInterface.OnClickListener() { // 点击单选框后的处理
					public void onClick(DialogInterface dialog,
							int which) { // The processing performed after clicking the radio box
						voicer = mCloudVoicersValue[which];
						if ("catherine".equals(voicer) || "henry".equals(voicer) || "vimary".equals(voicer)) {
							 ((EditText) findViewById(R.id.tts_text)).setText(R.string.text_tts_source_en);
						}else {
							((EditText) findViewById(R.id.tts_text)).setText(R.string.text_tts_source);
						}
						selectedNum = which;
						dialog.dismiss();
					}
				}).show();
			break;
			
		// Selects Local synthesis
		case R.id.tts_radioLocal:
			if (!SpeechUtility.getUtility().checkServiceInstalled()) {
				mInstaller.install();
			}else {
				SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);				
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Initializes the listening
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			Log.d(TAG, "InitListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("Initialization failed, error code: "+code);
        	} else {
				// After the initialization succeeds, the startSpeaking method can be called
        		// Note: Some developers call the startSpeaking for synthesis immediately after creating synthesis object in onCreate method,
        		// The correct way is to transfer the startSpeaking call in the onCreate here
			}		
		}
	};

	/**
	 * synthesis callback listen
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {
		
		@Override
		public void onSpeakBegin() {
			showTip("Start playing");
		}

		@Override
		public void onSpeakPaused() {
			showTip("Pause playing");
		}

		@Override
		public void onSpeakResumed() {
			showTip("Resume playing");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos,
				String info) {
			// Synthesis progress
			mPercentForBuffering = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// Playback progress
			mPercentForPlaying = percent;
			showTip(String.format(getString(R.string.tts_toast_format),
					mPercentForBuffering, mPercentForPlaying));
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				showTip("Playing complete");
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// The following codes are used to get the session id used for cloud end. When the service throws error, the id will be provided to the technical support staff for them to query session log and locate the error cause
			// If the local feature is used, the session id is null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * Parameter setting
	 * @param
	 * @return 
	 */
	private void setParam(){
		// Clears the parameter
		mTts.setParameter(SpeechConstant.PARAMS, null);
		// Sets the appropriate parameter according to the synthesis engine
		if(mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// Sets the online synthesis speaker
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
			// Sets the synthesis speech speed
			mTts.setParameter(SpeechConstant.SPEED, mSharedPreferences.getString("speed_preference", "50"));
			// Sets the synthesis tone
			mTts.setParameter(SpeechConstant.PITCH, mSharedPreferences.getString("pitch_preference", "50"));
			// Sets the synthesis volume
			mTts.setParameter(SpeechConstant.VOLUME, mSharedPreferences.getString("volume_preference", "50"));
		}else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// Sets the local synthesis speaker to null, the speaker is specified through VoiceNote user interface by default
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");
			/**
			 * TODO The local synthesis does not set the speech speed, tone and volume, the setting of the VoiceNote is used by default.
			 * If the developer needs to customize the parameter, please refer to the online synthesis parameter setting
			 */
		}
		// Sets the player audio stream type
		mTts.setParameter(SpeechConstant.STREAM_TYPE, mSharedPreferences.getString("stream_preference", "3"));
		// Sets whether the synthesis audio play will interrupt the music play, the default setting is true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
		
		// Sets the save path of the audio, The audio save format supports pcm, wav. If you set the path to sd card, Please pay attention to the WRITE_EXTERNAL_STORAGE right
		// Note:  For the AUDIO_FORMAT parameter, VoiceNote needs to update version to take effect
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTts.stopSpeaking();
		// Releases the connection when exiting
		mTts.destroy();
	}
	
	@Override
	protected void onResume() {
		// Statistical analysis of mobile data
		FlowerCollector.onResume(TtsDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}
	@Override
	protected void onPause() {
		// Statistical analysis of mobile data
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(TtsDemo.this);
		super.onPause();
	}

}
