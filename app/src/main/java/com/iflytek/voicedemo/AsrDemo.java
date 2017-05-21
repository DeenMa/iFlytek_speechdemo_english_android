package com.iflytek.voicedemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.cloud.util.ContactManager.ContactListener;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.speech.util.FucUtil;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.sunflower.FlowerCollector;

public class AsrDemo extends Activity implements OnClickListener{
	private static String TAG = AsrDemo.class.getSimpleName();
	// Speech recognition object
	private SpeechRecognizer mAsr;
	private Toast mToast;	
	// Cache
	private SharedPreferences mSharedPreferences;
	// Local grammar file
	private String mLocalGrammar = null;
	// Local dictionary
	private String mLocalLexicon = null;
	// Cloud grammar file
	private String mCloudGrammar = null;
		
	private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
	private static final String GRAMMAR_TYPE_ABNF = "abnf";
	private static final String GRAMMAR_TYPE_BNF = "bnf";

	private String mEngineType = null;
	// VoiceNote installation assistant class
	ApkInstaller mInstaller ;
	
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.isrdemo);
		initLayout();
		
		// Initializes the recognition object
		mAsr = SpeechRecognizer.createRecognizer(AsrDemo.this, mInitListener);		

		// Initializes the grammar, command word
		mLocalLexicon = "Alice\nBob\nCathy\n";
		mLocalGrammar = FucUtil.readFile(this,"call.bnf", "utf-8");
		mCloudGrammar = FucUtil.readFile(this,"grammar_sample.abnf","utf-8");
		
		// Used when getting the contact and updating the local dictionary
		ContactManager mgr = ContactManager.createManager(AsrDemo.this, mContactListener);	
		mgr.asyncQueryAllContactsName();
		mSharedPreferences = getSharedPreferences(getPackageName(),	MODE_PRIVATE);
		mToast = Toast.makeText(this,"",Toast.LENGTH_SHORT);	
		
		mInstaller = new ApkInstaller(AsrDemo.this);
	}
	
	/**
	 * Initialize Layout
	 */
	private void initLayout() {
		findViewById(R.id.isr_recognize).setOnClickListener(AsrDemo.this);
		findViewById(R.id.isr_grammar).setOnClickListener(AsrDemo.this);
		findViewById(R.id.isr_lexcion).setOnClickListener(AsrDemo.this);
		findViewById(R.id.isr_stop).setOnClickListener(AsrDemo.this);
		findViewById(R.id.isr_cancel).setOnClickListener(AsrDemo.this);

		// Selects the cloud or local
		RadioGroup group = (RadioGroup)this.findViewById(R.id.radioGroup);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.radioCloud) {
					((EditText)findViewById(R.id.isr_text)).setText(mCloudGrammar);
					findViewById(R.id.isr_lexcion).setEnabled(false);
					mEngineType = SpeechConstant.TYPE_CLOUD;
				} else {
					((EditText)findViewById(R.id.isr_text)).setText(mLocalGrammar);
					findViewById(R.id.isr_lexcion).setEnabled(true);
					mEngineType = SpeechConstant.TYPE_LOCAL;
					/**
					 * Select Local synthesis
					 * Determine whether the VoiceNote has been installed, skips to the page to prompt installation if not installed
					 */
					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
						mInstaller.install();
					}
				}
			}
		});
	}
	
	// Grammar, dictionary temporary variable
	String mContent;
	// Returned value for function call
    int ret = 0;
	
    @Override
	public void onClick(View view) {		
		if(null == mEngineType) {
			showTip("Please select the recognition engine type");
			return;
		}	
		switch(view.getId())
		{
			case R.id.isr_grammar:
				showTip("Upload pre-defined keyword / grammar file");
				// Local - Builds grammar file, generates the grammar id
				if (mEngineType.equals(SpeechConstant.TYPE_LOCAL)) {
					((EditText)findViewById(R.id.isr_text)).setText(mLocalGrammar);
					mContent = new String(mLocalGrammar);
					mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
					// Specifies the engine type
					mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
					ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, mLocalGrammarListener);
					if(ret != ErrorCode.SUCCESS){
						if(ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
							// Skips to the page to prompt installation if not installed
							mInstaller.install();
						}else {
							showTip("Grammar build failed, error code: " + ret);
						}
					}
				}
				// Online - Builds the grammar file, generates the grammar id
				else {	
					((EditText)findViewById(R.id.isr_text)).setText(mCloudGrammar);
					mContent = new String(mCloudGrammar);
					// Specifies the engine type
					mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
					mAsr.setParameter(SpeechConstant.TEXT_ENCODING,"utf-8");
				    ret = mAsr.buildGrammar(GRAMMAR_TYPE_ABNF, mContent, mCloudGrammarListener);
					if(ret != ErrorCode.SUCCESS)
						showTip("Grammar build failed, error code: " + ret);
				}
				
				break;
			// Local -Update dictionary. Note: Updating dictionary must occur after the build grammar callback onBuildFinish is received
			case R.id.isr_lexcion: 
				((EditText)findViewById(R.id.isr_text)).setText(mLocalLexicon);
				mContent = new String(mLocalLexicon);
				// Specify engine type
				mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
				mAsr.setParameter(SpeechConstant.GRAMMAR_LIST, "call");
				ret = mAsr.updateLexicon("<contact>", mContent, mLexiconListener);
				if(ret != ErrorCode.SUCCESS){
					if(ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
						// Skips to the page to prompt installation if not installed
						mInstaller.install();
					}else {
						showTip("Update dictionary failed, error code: " + ret);
					}
				}
				break;
			// Starts recognition
			case R.id.isr_recognize:
				((EditText)findViewById(R.id.isr_text)).setText(null);// Empty the display
				// Sets parameter
				if (!setParam()) {
					showTip("Please build grammar first");
					return;
				};
				
				ret = mAsr.startListening(mRecognizerListener);
				if (ret != ErrorCode.SUCCESS) {
					if(ret == ErrorCode.ERROR_COMPONENT_NOT_INSTALLED){
						// Skips to the page to prompt installation if not installed
						mInstaller.install();
					}else {
						showTip("Recognition failed, error code: " + ret);
					}
				}
				break;
			// Stop recognition
			case R.id.isr_stop:
				mAsr.stopListening();
				showTip("Stop recognition");
				break;
			// Cancel the recognition
			case R.id.isr_cancel:
				mAsr.cancel();
				showTip("Cancel recognition");
				break;
		}
	}
	
	/**
     * Initializes the listener
     */
    private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
        		showTip("Initialization failed, error code: "+code);
        	}
		}
    };
    	
	/**
     * Update dictionary listener
     */
	private LexiconListener mLexiconListener = new LexiconListener() {
		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if(error == null){
				showTip("Dictionary update succeed");
			}else{
				showTip("Dictionary update failed, error code: "+error.getErrorCode());
			}
		}
	};
	
	/**
     * Locally built grammar listener
     */
	private GrammarListener mLocalGrammarListener = new GrammarListener() {
		@Override
		public void onBuildFinish(String grammarId, SpeechError error) {
			if(error == null){
				showTip("Grammar build succeed: " + grammarId);
			}else{
				showTip("Grammar build failed, error code: " + error.getErrorCode());
			}			
		}
	};
	/**
     * Cloud-based built grammar listener
     */
	private GrammarListener mCloudGrammarListener = new GrammarListener() {
		@Override
		public void onBuildFinish(String grammarId, SpeechError error) {
			if(error == null){
				String grammarID = new String(grammarId);
				Editor editor = mSharedPreferences.edit();
				if(!TextUtils.isEmpty(grammarId))
					editor.putString(KEY_GRAMMAR_ABNF_ID, grammarID);
				editor.commit();
				showTip("Grammar build succeed: " + grammarId);
			}else{
				showTip("Grammar build failed, error code: " + error.getErrorCode());
			}			
		}
	};
	/**
	 * Gets the contact listener
	 */
	private ContactListener mContactListener = new ContactListener() {
		@Override
		public void onContactQueryFinish(String contactInfos, boolean changeFlag) {
			// Gets the contact
			mLocalLexicon = contactInfos;
		}		
	};
	/**
     * Recognize the listener
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
        	showTip("Speaking, volumn: " + volume);
        	Log.d(TAG, "Return audio data: "+data.length);
        }
        
        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
        	if (null != result) {
        		Log.d(TAG, "recognizer result：" + result.getResultString());
        		String text ;
        		if("cloud".equalsIgnoreCase(mEngineType)){
        			text = JsonParser.parseGrammarResult(result.getResultString());
        		}else {
        			text = JsonParser.parseLocalGrammarResult(result.getResultString());
        		}
        		
        		// Display
        		((EditText)findViewById(R.id.isr_text)).setText(text);                
        	} else {
        		Log.d(TAG, "recognizer result : null");
        	}	
        }
        
        @Override
        public void onEndOfSpeech() {
        	// This callback indicates: the end of speech has been detected, the recognition process starts and no speech input is accepted
        	showTip("Stop speaking");
        }
        
        @Override
        public void onBeginOfSpeech() {
        	// This callback indicates: the internal recorder for sdk has been ready for user to start speech input
        	showTip("Start speaking");
        }

		@Override
		public void onError(SpeechError error) {
			showTip("onError Code："	+ error.getErrorCode());
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// The following codes are used to get the session id used for cloud, when the transaction throws error, the id will be provided to the technical support staff for them to query session log and locate the error cause
			// If the local feature is used, the session id is null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}

    };
    
	

	private void showTip(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mToast.setText(str);
				mToast.show();
			}
		});
	}

	/**
	 * Parameter setting
	 * @param
	 * @return 
	 */
	public boolean setParam(){
		boolean result = false;
		// Sets the recognition engine
		mAsr.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// Sets the returned result to jsonformat
		mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");

		if("cloud".equalsIgnoreCase(mEngineType))
		{
			String grammarId = mSharedPreferences.getString(KEY_GRAMMAR_ABNF_ID, null);
			if(TextUtils.isEmpty(grammarId))
			{
				result =  false;
			}else {
				// Sets the grammar id to be used by cloud recognition
				mAsr.setParameter(SpeechConstant.CLOUD_GRAMMAR, grammarId);
				result =  true;
			}
		} else {
			// Sets the grammar id to be used by local recognition
			mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, "call");
			// Sets the threshold for local recognition
			mAsr.setParameter(SpeechConstant.ASR_THRESHOLD, "30");
			result = true;
		}

		// Sets the save path of the audio, The audio save format supports pcm, wav. If you set the path to sd card, Please pay attention to the WRITE_EXTERNAL_STORAGE right
		// Note:   For the AUDIO_FORMAT parameter, VoiceNote needs to update version to take effect
		mAsr.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mAsr.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/asr.wav");
		return result;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Releases the connection when exiting
		mAsr.cancel();
		mAsr.destroy();
	}
	
	@Override
	protected void onResume() {
		// Statistical analysis of mobile data
		FlowerCollector.onResume(AsrDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// Statistical analysis of mobile data
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(AsrDemo.this);
		super.onPause();
	}
	
}
