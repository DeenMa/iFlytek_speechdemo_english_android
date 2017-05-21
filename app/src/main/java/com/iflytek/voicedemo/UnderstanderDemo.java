package com.iflytek.voicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUnderstanderListener;
import com.iflytek.cloud.TextUnderstander;
import com.iflytek.cloud.TextUnderstanderListener;
import com.iflytek.cloud.UnderstanderResult;
import com.iflytek.speech.setting.UnderstanderSettings;
import com.iflytek.sunflower.FlowerCollector;

public class UnderstanderDemo extends Activity implements OnClickListener {
	private static String TAG = UnderstanderDemo.class.getSimpleName();
	// Semantic understanding object (Speech to Semantic)
	private SpeechUnderstander mSpeechUnderstander;
	// Semantic understanding object (Text to Semantic)
	private TextUnderstander   mTextUnderstander;	
	private Toast mToast;	
	private EditText mUnderstanderText;
	
	private SharedPreferences mSharedPreferences;
	
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.understander);
		
		initLayout();
		/**
		 * In the appid applied by the developer, we have enabled Open Semantic (Semantic Understanding)
		 * Because of the scenarios for semantic understanding is various, the developers need to visit Open Semantic Platform: http://www.xfyun.cn/services/osp
		 * to configure the appropriate speech understanding, otherwise the text understanding will not work, the semantic understanding will return ASR result
		 */
		// Initializes the object
		mSpeechUnderstander = SpeechUnderstander.createUnderstander(UnderstanderDemo.this, mSpeechUdrInitListener);
		mTextUnderstander = TextUnderstander.createTextUnderstander(UnderstanderDemo.this, mTextUdrInitListener);
		
		mToast = Toast.makeText(UnderstanderDemo.this, "", Toast.LENGTH_SHORT);
	}
	
	/**
	 * Initializes Layout
	 */
	private void initLayout(){
		findViewById(R.id.text_understander).setOnClickListener(UnderstanderDemo.this);
		findViewById(R.id.start_understander).setOnClickListener(UnderstanderDemo.this);
		
		mUnderstanderText = (EditText)findViewById(R.id.understander_text);
		
		findViewById(R.id.understander_stop).setOnClickListener(UnderstanderDemo.this);
		findViewById(R.id.understander_cancel).setOnClickListener(UnderstanderDemo.this);
		findViewById(R.id.image_understander_set).setOnClickListener(UnderstanderDemo.this);
		
		mSharedPreferences = getSharedPreferences(UnderstanderSettings.PREFER_NAME, Activity.MODE_PRIVATE);
	}
	
    /**
     * Initializes the listener (Speech to Semantic)
     */
    private InitListener mSpeechUdrInitListener = new InitListener() {
    	
		@Override
		public void onInit(int code) {
			Log.d(TAG, "speechUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("Initialization failed, error code: "+code);
        	}			
		}
    };
    
    /**
     * Initializes the listener (Text to Semantic)
     */
    private InitListener mTextUdrInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "textUnderstanderListener init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("Initialization failed, error code: "+code);
        	}
		}
    };
	
    
	int ret = 0;// Returned value for function call
	@Override
	public void onClick(View view) {				
		switch (view.getId()) {
		// Go to the Parameter setting page
		case R.id.image_understander_set:
			Intent intent = new Intent(UnderstanderDemo.this, UnderstanderSettings.class);
			startActivity(intent);
			break;
		// Starts the text understanding
		case R.id.text_understander:
			mUnderstanderText.setText("");
			String text = "What is the weather like tomorrow in San Francisco?";
			showTip(text);
			
			if(mTextUnderstander.isUnderstanding()){
				mTextUnderstander.cancel();
				showTip("Cancel");
			}else {
				ret = mTextUnderstander.understandText(text, mTextUnderstanderListener);
				if(ret != 0)
				{
					showTip("Semantic Understanding failed, error code: "+ ret);
				}
			}
			break;
		// Starts the speech understanding
		case R.id.start_understander:
			mUnderstanderText.setText("");
			// Sets parameter
			setParam();
	
			if(mSpeechUnderstander.isUnderstanding()){// Checks the status before start
				mSpeechUnderstander.stopUnderstanding();
				showTip("Stop recording");
			}else {
				ret = mSpeechUnderstander.startUnderstanding(mSpeechUnderstanderListener);
				if(ret != 0){
					showTip("Semantic Understanding failed, error code: "	+ ret);
				}else {
					showTip(getString(R.string.text_begin));
				}
			}
			break;
		// Stops the speech understanding
		case R.id.understander_stop:
			mSpeechUnderstander.stopUnderstanding();
			showTip("Stop semantic understanding");
			break;
		// Cancels the speech understanding
		case R.id.understander_cancel:
			mSpeechUnderstander.cancel();
			showTip("Cancel semantic understanding");
			break;
		default:
			break;
		}
	}
	
	private TextUnderstanderListener mTextUnderstanderListener = new TextUnderstanderListener() {
		
		@Override
		public void onResult(final UnderstanderResult result) {
			if (null != result) {
				// show
				String text = result.getResultString();
				if (!TextUtils.isEmpty(text)) {
					mUnderstanderText.setText(text);
				}
			} else {
				Log.d(TAG, "understander result:null");
				showTip("Incorrect recognition result");
			}
		}
		
		@Override
		public void onError(SpeechError error) {
			// The text semantic can not use callback error code 14002, please confirm if you checked the publication for  semantic scenario and private semantic when downloading the sdk
			showTip("onError Code："	+ error.getErrorCode());
			
		}
	};
	
    /**
     * Semantic understanding callback
     */
    private SpeechUnderstanderListener mSpeechUnderstanderListener = new SpeechUnderstanderListener() {

		@Override
		public void onResult(final UnderstanderResult result) {
			if (null != result) {
				Log.d(TAG, result.getResultString());
				
				// show
				String text = result.getResultString();
				if (!TextUtils.isEmpty(text)) {
					mUnderstanderText.setText(text);
				}
			} else {
				showTip("Incorrect recognition result");
			}	
		}
    	
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        	showTip("Speaking, volumn " + volume);
        	Log.d(TAG, data.length+"");
        }
        
        @Override
        public void onEndOfSpeech() {
        	// This callback indicates: the end of the speech has been detected, the recognition process starts and no speech input is accepted
        	showTip("Stop speaking");
        }
        
        @Override
        public void onBeginOfSpeech() {
        	// This callback indicates: the internal recorder for sdk has been ready for user to start speech input
        	showTip("Start speaking");
        }

		@Override
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// The following codes are used to get the session id used for cloud, when the service throws error, the id will be provided to the technical support staff for them to query session log and locate the error cause
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
    };
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
        // Releases the connection when exiting
    	mSpeechUnderstander.cancel();
    	mSpeechUnderstander.destroy();
    	if(mTextUnderstander.isUnderstanding())
    		mTextUnderstander.cancel();
    	mTextUnderstander.destroy();    
    }
	
	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}
	
	/**
	 * Parameter setting
	 * @param
	 * @return 
	 */
	public void setParam(){
		String lang = mSharedPreferences.getString("understander_language_preference", "mandarin");
		if (lang.equals("en_us")) {
			// Sets the language
			mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "en_us");
		}else {
			// Sets the language
			mSpeechUnderstander.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// Sets the dialectal variety of the language
			mSpeechUnderstander.setParameter(SpeechConstant.ACCENT, lang);
		}
		// Sets the beginning of speech: mute timeout, namely, if the user does not speak within the given time length, it's considered to be timeout
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("understander_vadbos_preference", "4000"));
		
		// Sets the end of speech: The mute detection for end of speech, namely, after the user stops speaking for the given time, it’s considered to be end of speech
		mSpeechUnderstander.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("understander_vadeos_preference", "1000"));
		
		// Whether involves punctuation marks, default value: 1 (contains punctuation)
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("understander_punc_preference", "1"));
		
		// Sets the save path of the audio, The audio save format supports pcm, wav. If you set the path to sd card, Please pay attention to the WRITE_EXTERNAL_STORAGE right

		// Note:   For the AUDIO_FORMAT parameter, VoiceNote needs to update version to take effect
		mSpeechUnderstander.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mSpeechUnderstander.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/sud.wav");
	}	
	
	@Override
	protected void onResume() {
		// Statistical analysis of mobile data
		FlowerCollector.onResume(UnderstanderDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// Statistical analysis of mobile data
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(UnderstanderDemo.this);
		super.onPause();
	}
	
}
