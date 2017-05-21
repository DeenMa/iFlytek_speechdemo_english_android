package com.iflytek.voicedemo;

import com.iflytek.ise.result.Result;
import com.iflytek.ise.result.xml.XmlResultParser;
import com.iflytek.speech.setting.IseSettings;
import com.iflytek.sunflower.FlowerCollector;
import com.iflytek.cloud.EvaluatorListener;
import com.iflytek.cloud.EvaluatorResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvaluator;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Speech Evaluation demo
 */
public class IseDemo extends Activity implements OnClickListener {
	private static String TAG = IseDemo.class.getSimpleName();
	
	private final static String PREFER_NAME = "ise_settings";
	private final static int REQUEST_CODE_SETTINGS = 1;

	private EditText mEvaTextEditText;
	private EditText mResultEditText;
	private Button mIseStartButton;
	private Toast mToast;

	// Evaluation language
	private String language;
	// Evaluation question type
	private String category;
	// Result level
	private String result_level;
	
	private String mLastResult;
	private SpeechEvaluator mIse;
	
	
	// Evaluation listener interface
	private EvaluatorListener mEvaluatorListener = new EvaluatorListener() {
		
		@Override
		public void onResult(EvaluatorResult result, boolean isLast) {
			Log.d(TAG, "evaluator result :" + isLast);

			if (isLast) {
				StringBuilder builder = new StringBuilder();
				builder.append(result.getResultString());
				
				if(!TextUtils.isEmpty(builder)) {
					mResultEditText.setText(builder.toString());
				}
				mIseStartButton.setEnabled(true);
				mLastResult = builder.toString();
				
				showTip("Evaluation complete");
			}
		}

		@Override
		public void onError(SpeechError error) {
			mIseStartButton.setEnabled(true);
			if(error != null) {	
				showTip("error:"+ error.getErrorCode() + "," + error.getErrorDescription());
				mResultEditText.setText("");
				mResultEditText.setHint("Please click \"Start evaluation\" button");
			} else {
				Log.d(TAG, "evaluator over");
			}
		}

		@Override
		public void onBeginOfSpeech() {
			// This callback indicates: the internal recorder of SDK has been ready for user to start speech input
			Log.d(TAG, "evaluator begin");
		}

		@Override
		public void onEndOfSpeech() {
			// This callback indicates: the end of speech has been detected, the recognition process starts and no speech input is accepted any more
			Log.d(TAG, "evaluator stoped");
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("Current volumn: " + volume);
			Log.d(TAG, "Return audio data: "+data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// The following codes are used to get the session id used for cloud,
			// when the service throws error, the id will be provided to the technical support staff for them
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
		
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.isedemo);

		mIse = SpeechEvaluator.createEvaluator(IseDemo.this, null);
		initUI();
		setEvaText();
	}

	private void initUI() {
		findViewById(R.id.image_ise_set).setOnClickListener(IseDemo.this);
		mEvaTextEditText = (EditText) findViewById(R.id.ise_eva_text);
		mResultEditText = (EditText)findViewById(R.id.ise_result_text);
		mIseStartButton = (Button) findViewById(R.id.ise_start);
		mIseStartButton.setOnClickListener(IseDemo.this);
		findViewById(R.id.ise_parse).setOnClickListener(IseDemo.this);
		findViewById(R.id.ise_stop).setOnClickListener(IseDemo.this);
		findViewById(R.id.ise_cancel).setOnClickListener(IseDemo.this);
				
		mToast = Toast.makeText(IseDemo.this, "", Toast.LENGTH_LONG);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.image_ise_set:
				Intent intent = new Intent(IseDemo.this, IseSettings.class);
				startActivityForResult(intent, REQUEST_CODE_SETTINGS);
				break;
			case R.id.ise_start:
				if (mIse == null) {
					return;
				}
	
				String evaText = mEvaTextEditText.getText().toString();
				mLastResult = null;
				mResultEditText.setText("");
				mResultEditText.setHint("Please read the content above");
				mIseStartButton.setEnabled(false);
				
				setParams();
				mIse.startEvaluating(evaText, null, mEvaluatorListener);
				break;
			case R.id.ise_parse:
				// Parse the final result
				if (!TextUtils.isEmpty(mLastResult)) {
					XmlResultParser resultParser = new XmlResultParser();
					Result result = resultParser.parse(mLastResult);
					
					if (null != result) {
						mResultEditText.setText(result.toString());
					} else {
						showTip("The result is empty");
					}
				}
				break;
			case R.id.ise_stop:
				if (mIse.isEvaluating()) {
					mResultEditText.setHint("The evaluation had stopped, waiting for the result...");
					mIse.stopEvaluating();
				}
				break;
			case R.id.ise_cancel: {
				mIse.cancel();
				mIseStartButton.setEnabled(true);
				mResultEditText.setText("");
				mResultEditText.setHint("Please click \"Start evaluation\" button");
				mLastResult = null;
				break;
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (REQUEST_CODE_SETTINGS == requestCode) {
			setEvaText();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (null != mIse) {
			mIse.destroy();
			mIse = null;
		}
	}
	
	// Sets the evaluation question
	private void setEvaText() {
		SharedPreferences pref = getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
		language = pref.getString(SpeechConstant.LANGUAGE, "zh_cn");
		category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");
		
		String text = "";
		if ("en_us".equals(language)) {
			if ("read_word".equals(category)) {
				text = getString(R.string.text_en_word);
			} else if ("read_sentence".equals(category)) {
				text = getString(R.string.text_en_sentence);
			} 
		} else {
			// 中文评测
			if ("read_syllable".equals(category)) {
				text = getString(R.string.text_cn_syllable);
			} else if ("read_word".equals(category)) {
				text = getString(R.string.text_cn_word);
			} else if ("read_sentence".equals(category)) {
				text = getString(R.string.text_cn_sentence);
			} 
		}
		
		mEvaTextEditText.setText(text);
		mResultEditText.setText("");
		mLastResult = null;
		mResultEditText.setHint("Please click \"Start evaluation\" button");
	}

	private void showTip(String str) {
		if(!TextUtils.isEmpty(str)) {
			mToast.setText(str);
			mToast.show();
		}
	}
	private void setParams() {
		SharedPreferences pref = getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
		// Sets the evaluation language
		language = pref.getString(SpeechConstant.LANGUAGE, "zh_cn");
		// Sets the required evaluation type
		category = pref.getString(SpeechConstant.ISE_CATEGORY, "read_sentence");
		// Sets the result level (Only 'complete' is supported for Chinese) 设置结果等级（中文仅支持complete）
		result_level = pref.getString(SpeechConstant.RESULT_LEVEL, "complete");
		// Sets mute timeout for the beginning of speech: namely, if the user does not speak within the given time length, it's considered to be timeout
		String vad_bos = pref.getString(SpeechConstant.VAD_BOS, "5000");
		// Set mute timeout for the end of speech: namely, if the user stops speaking within the given time, it is considered no longer recording and will stop recording automatically
		String vad_eos = pref.getString(SpeechConstant.VAD_EOS, "1800");
		// Speech input timeout , namely, the longest time for user to continually speak
		String speech_timeout = pref.getString(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");
		
		mIse.setParameter(SpeechConstant.LANGUAGE, language);
		mIse.setParameter(SpeechConstant.ISE_CATEGORY, category);
		mIse.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
		mIse.setParameter(SpeechConstant.VAD_BOS, vad_bos);
		mIse.setParameter(SpeechConstant.VAD_EOS, vad_eos);
		mIse.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, speech_timeout);
		mIse.setParameter(SpeechConstant.RESULT_LEVEL, result_level);
		// Sets the save path of the audio. The audio save format supports pcm, wav. If you set the path to sd card, Please pay attention to the WRITE_EXTERNAL_STORAGE right
		// Note: For the AUDIO_FORMAT parameter, the VoiceNote needs a version update to take effect
		mIse.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIse.setParameter(SpeechConstant.ISE_AUDIO_PATH, Environment.getExternalStorageDirectory().getAbsolutePath() + "/msc/ise.wav");
	}
	
	@Override
	protected void onResume() {
		// Open statistical: Statistical analysis of mobile data
		FlowerCollector.onResume(IseDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// Open statistical: Statistical analysis of mobile data
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(IseDemo.this);
		super.onPause();
	}
}
