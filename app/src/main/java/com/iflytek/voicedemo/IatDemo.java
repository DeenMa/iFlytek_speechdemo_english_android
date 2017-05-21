package com.iflytek.voicedemo;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.cloud.util.ContactManager.ContactListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.speech.util.FucUtil;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.sunflower.FlowerCollector;

public class IatDemo extends Activity implements OnClickListener {
	private static String TAG = IatDemo.class.getSimpleName();
	// Short Form ASR object
	private SpeechRecognizer mIat;
	// Short Form ASR UI
	private RecognizerDialog mIatDialog;
	// Stores the Short Form ASR result through HashMap
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

	private EditText mResultText;
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	// Engine type
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// VoiceNote install assistant class
	ApkInstaller mInstaller;
	

	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.iatdemo);

		initLayout();
		// The initialization recognition has no UI recognition object
		// With SpeechRecognizer object, the UI can be customized according to callback message
		mIat = SpeechRecognizer.createRecognizer(IatDemo.this, mInitListener);
		
		// Initializes the ASR Dialog, if you only use the ASR feature with UI , it's not necessary to create the SpeechRecognizer
		// Uses the UI Short Form ASR feature, according to the notice.txt under the sdk file directory, place the layout file and picture resource
		mIatDialog = new RecognizerDialog(IatDemo.this, mInitListener);

		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
				Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		mResultText = ((EditText) findViewById(R.id.iat_text));
		mInstaller = new ApkInstaller(IatDemo.this);
	}

	/**
	 * Initializes  the Layout
	 */
	private void initLayout() {
		findViewById(R.id.iat_recognize).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_recognize_stream).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_upload_contacts).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_upload_userwords).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_stop).setOnClickListener(IatDemo.this);
		findViewById(R.id.iat_cancel).setOnClickListener(IatDemo.this);
		findViewById(R.id.image_iat_set).setOnClickListener(IatDemo.this);
		// Selects the cloud end or the local end
		RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.iatRadioCloud:
					mEngineType = SpeechConstant.TYPE_CLOUD;
					findViewById(R.id.iat_upload_contacts).setEnabled(true);
					findViewById(R.id.iat_upload_userwords).setEnabled(true);
					break;
				case R.id.iatRadioLocal:
					mEngineType = SpeechConstant.TYPE_LOCAL;
					findViewById(R.id.iat_upload_contacts).setEnabled(false);
					findViewById(R.id.iat_upload_userwords).setEnabled(false);
					/**
					 * Selects the local Short Form ASR to determine whether the VoiceNote has been installed, skips to the page to prompt installation if not installed
					 */
					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
						mInstaller.install();
					} else {
						String result = FucUtil.checkLocalResource();
						if (!TextUtils.isEmpty(result)) {
							showTip(result);
						}
					}
					break;
				case R.id.iatRadioMix:
					mEngineType = SpeechConstant.TYPE_MIX;
					findViewById(R.id.iat_upload_contacts).setEnabled(false);
					findViewById(R.id.iat_upload_userwords).setEnabled(false);
					/**
					 * Selects the local Short Form ASR to determine whether the VoiceNote has been installed, skips to the page to prompt installation if not installed
					 */
					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
						mInstaller.install();
					} else {
						String result = FucUtil.checkLocalResource();
						if (!TextUtils.isEmpty(result)) {
							showTip(result);
						}
					}
					break;
				default:
					break;
				}
			}
		});
	}

	int ret = 0; // Returned value for function call

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// Go to the Parameter setting page
		case R.id.image_iat_set:
			Intent intents = new Intent(IatDemo.this, IatSettings.class);
			startActivity(intents);
			break;
		// Starts the Short Form ASR
		// How to determine the ending of a Short Form ASR: OnResult isLast=true or onError
		case R.id.iat_recognize:
			// Mobile data analysis, collects the Short Form ASR start event
			FlowerCollector.onEvent(IatDemo.this, "iat_recognize");
			
			mResultText.setText(null);// Clears the contents displayed
			mIatResults.clear();
			// Sets parameter
			setParam();
			boolean isShowDialog = mSharedPreferences.getBoolean(
					getString(R.string.pref_key_iat_show), true);
			if (isShowDialog) {
				// Displays the Short Form ASR dialog box
				mIatDialog.setListener(mRecognizerDialogListener);
				mIatDialog.show();
				showTip(getString(R.string.text_begin));
			} else {
				// Does not display the Short Form ASR dialog box
				ret = mIat.startListening(mRecognizerListener);
				if (ret != ErrorCode.SUCCESS) {
					showTip("Recognition failed, error code: " + ret);
				} else {
					showTip(getString(R.string.text_begin));
				}
			}
			break;
		// Audio stream recognition
		case R.id.iat_recognize_stream:
			mResultText.setText(null);// Clear the contents displayed
			mIatResults.clear();
			// Sets parameter
			setParam();
			// Sets the audio source to external file
			mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
			// You can also set the audio file path recognition directly as shown below（requires the full path to the setup file on the sdcard）：
			// mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
			// mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "sdcard/XXX/XXX.pcm");
			ret = mIat.startListening(mRecognizerListener);
			if (ret != ErrorCode.SUCCESS) {
				showTip("Recognition failed, error code: " + ret);
			} else {
				byte[] audioData = FucUtil.readAudioFile(IatDemo.this, "iattest.wav");
				
				if (null != audioData) {
					showTip(getString(R.string.text_begin_recognizer));
					// Writes the audio file data for one time (or several times), the data format must be mono wav or pcm with sampling rate of 8KHz or 16KHz(the local  only supports sampling rate of 16K, the cloud end support both), bit length of 16bit
					// Before writing the audio with sampling rate of 8KHz, the setParameter(SpeechConstant.SAMPLE_RATE, "8000") must be called to set the correct sampling rate
					//
					// Note: If the audio is too long and the mute duration exceeds VAD_EOS, that will cause the content behind the mute can not be recognized
					// Audio segmentation method: FucUtil.splitBuffer(byte[] buffer,int length,int spsize);
					mIat.writeAudio(audioData, 0, audioData.length);
					mIat.stopListening();
				} else {
					mIat.cancel();
					showTip("Failed to read the audio stream");
				}
			}
			break;
		// Stops the Short Form ASR
		case R.id.iat_stop:
			mIat.stopListening();
			showTip("Stop recognition");
			break;
		// Cancels the Short Form ASR
		case R.id.iat_cancel:
			mIat.cancel();
			showTip("Cancel recognition");
			break;
		// Uploads the contact
		case R.id.iat_upload_contacts:
			showTip(getString(R.string.text_upload_contacts));
			ContactManager mgr = ContactManager.createManager(IatDemo.this,
					mContactListener);
			mgr.asyncQueryAllContactsName();
			break;
		// Uploads the customized word hint list
		case R.id.iat_upload_userwords:
			showTip(getString(R.string.text_upload_userwords));
			String contents = FucUtil.readFile(IatDemo.this, "userwords","utf-8");
			mResultText.setText(contents);
			// Specify engine type
			mIat.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
			ret = mIat.updateLexicon("userword", contents, mLexiconListener);
			if (ret != ErrorCode.SUCCESS)
				showTip("Failed to upload the hot words, error code: " + ret);
			break;
		default:
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
				showTip("Initialization failed, error code: " + code);
			}
		}
	};

	/**
	 * Uploads the contact / word hint list listener
	 */
	private LexiconListener mLexiconListener = new LexiconListener() {

		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if (error != null) {
				showTip(error.toString());
			} else {
				showTip(getString(R.string.text_upload_success));
			}
		}
	};

	/**
	 * Short Form ASR listener
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// This callback indicates: The internal recorder for sdk has been ready for user to start speech input
			showTip("Start speaking");
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// Error code: 10118 (You don't have the speech) permission, maybe the recorder permission is prohibited，prompt user to enable the recording permission for the application.
			// If the local feature is used, VoiceNote needs to prompt user to enable the recording permission for VoiceNote
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onEndOfSpeech() {
			// This callback indicates: The end of the speech has been detected, the recognition process starts and no speech input is accepted any more
			showTip("Stop speaking");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());
			printResult(results);

			if (isLast) {
				// TODO The final result
			}
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("Speaking, volumn: " + volume);
			Log.d(TAG, "Return audio data: "+data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// The following codes are used to get the session id with the cloud end, when the service throws error, the id will be provided to the technical support staff for them to query session log and locate the error cause
			// If the local feature is used, the session id is null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};

	private void printResult(RecognizerResult results) {
		String text = JsonParser.parseIatResult(results.getResultString());

		String sn = null;
		// Reads the sn field in the json result
		try {
			JSONObject resultJson = new JSONObject(results.getResultString());
			sn = resultJson.optString("sn");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		mIatResults.put(sn, text);

		StringBuffer resultBuffer = new StringBuffer();
		for (String key : mIatResults.keySet()) {
			resultBuffer.append(mIatResults.get(key));
		}

		mResultText.setText(resultBuffer.toString());
		mResultText.setSelection(mResultText.length());
	}

	/**
	 * Short Form ASR UI listener
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
			printResult(results);
		}

		/**
		 * Short Form ASR callback error
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};

	/**
	 * Gets the contact listener
	 */
	private ContactListener mContactListener = new ContactListener() {

		@Override
		public void onContactQueryFinish(final String contactInfos, boolean changeFlag) {
			// Note: In practical application, except the first upload, you should determine whether the upload is required through changeFlag, otherwise it will lead to unnecessary network traffic
			// Each time the contact changes, this interface is called back, and the object can be destroyed through ContactManager.destroy() and release the callback
			// if(changeFlag) {
			// Specify engine type
			runOnUiThread(new Runnable() {
				public void run() {
					mResultText.setText(contactInfos);
				}
			});
			
			mIat.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
			mIat.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
			ret = mIat.updateLexicon("contact", contactInfos, mLexiconListener);
			if (ret != ErrorCode.SUCCESS) {
				showTip("Failed to upload contacts: " + ret);
			}
		}
	};

	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * Parameter setting
	 * 
	 * @param
	 * @return
	 */
	public void setParam() {
		// Empties the parameter
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// Sets the Short Form ASR engine
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// Sets the returned result format
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

		String lag = mSharedPreferences.getString("iat_language_preference",
				"mandarin");
		if (lag.equals("en_us")) {
			// Sets the language
			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		} else {
			// Sets the language
			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
			// Sets the dialectal variety of the language
			mIat.setParameter(SpeechConstant.ACCENT, lag);
		}

		// Sets the beginning of speech: mute timeout, namely, if the user does not speak within the given time length, it's considered to be timeout
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		
		// Sets the end of speech: The mute detection  for end of speech, namely, after the user stops speaking for the given time, it’s considered to be end of speech
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		
		// Sets the punctuation: Setting to "0” will return the result without punctuation, Setting to "1” will return the result with punctuation
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		
		// Sets the save path of the audio, The audio save format supports pcm, wav. If you set the path to sd card, Please pay attention to the WRITE_EXTERNAL_STORAGE right

		// Note: For the AUDIO_FORMAT parameter, VoiceNote needs a version update to take effect
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Releases the connection when exiting
		mIat.cancel();
		mIat.destroy();
	}

	@Override
	protected void onResume() {
		// Open statistical: Statistical analysis of mobile data
		FlowerCollector.onResume(IatDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// Open statistical: Statistical analysis of mobile data
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(IatDemo.this);
		super.onPause();
	}
}
