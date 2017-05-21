package com.iflytek.speech.util;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import android.content.Context;

/**
 * Utility function expansion class
 */
public class FucUtil {
	/**
	 * Reads files under the asset directory
	 * @return content
	 */
	public static String readFile(Context mContext,String file,String code)
	{
		int len = 0;
		byte []buf = null;
		String result = "";
		try {
			InputStream in = mContext.getAssets().open(file);			
			len  = in.available();
			buf = new byte[len];
			in.read(buf, 0, len);
			
			result = new String(buf,code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * Divide the byte cache into arrays of fixed size
	 * @param buffer Cache
	 * @param length Cache size
	 * @param spsize Block size
	 * @return
	 */
	public ArrayList<byte[]> splitBuffer(byte[] buffer,int length,int spsize)
	{
		ArrayList<byte[]> array = new ArrayList<byte[]>();
		if(spsize <= 0 || length <= 0 || buffer == null || buffer.length < length)
			return array;
		int size = 0;
		while(size < length)
		{
			int left = length - size;
			if(spsize < left)
			{
				byte[] sdata = new byte[spsize];
				System.arraycopy(buffer,size,sdata,0,spsize);
				array.add(sdata);
				size += spsize;
			}else
			{
				byte[] sdata = new byte[left];
				System.arraycopy(buffer,size,sdata,0,left);
				array.add(sdata);
				size += left;
			}
		}
		return array;
	}
	/**
	 * Finds whether the VoiceNote contains offline Short Form ASR resource, skips to the resource download page if not contain
	 *1.PLUS_LOCAL_ALL: All local resources
      2.PLUS_LOCAL_ASR: Local recognition resource
      3.PLUS_LOCAL_TTS: Local synthesis resource
	 */
	public static String checkLocalResource(){
		String resource = SpeechUtility.getUtility().getParameter(SpeechConstant.PLUS_LOCAL_ASR);
		try {
			JSONObject result = new JSONObject(resource);
			int ret = result.getInt(SpeechUtility.TAG_RESOURCE_RET);
			switch (ret) {
			case ErrorCode.SUCCESS:
				JSONArray asrArray = result.getJSONObject("result").optJSONArray("asr");
				if (asrArray != null) {
					int i = 0;
					// Finds whether to contain offline Short Form ASR resource
					for (; i < asrArray.length(); i++) {
						if("iat".equals(asrArray.getJSONObject(i).get(SpeechConstant.DOMAIN))){
							// asrArray contains languages and dialects fields, support for dialect Short Form ASR (local) will be added in the future
							// Such as："accent": "mandarin","language": "zh_cn"
							break;
						}
					}
					if (i >= asrArray.length()) {
						
						SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);	
						return "No recognition resources, navigate to resource download page";
					}
				}else {
					SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);
					return "No recognition resources, navigate to resource download page";
				}
				break;
			case ErrorCode.ERROR_VERSION_LOWER:
				return "The version of VoiceNote is too low, please update before using the local feature";
			case ErrorCode.ERROR_INVALID_RESULT:
				SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);
				return "Error occurred when acquiring result, navigate to resource download page\n";
			case ErrorCode.ERROR_SYSTEM_PREINSTALL:
				// Voicenote is the vendor built-in version
			default:
				break;
			}
		} catch (Exception e) {
			SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_ASR);
			return "Error occurred when acquiring result, navigate to resource download page\n";
		}
		return "";
	}
	
	/**
	 * Read the audio files under asset directory
	 * 
	 * @return Binary file data
	 */
	public static byte[] readAudioFile(Context context, String filename) {
		try {
			InputStream ins = context.getAssets().open(filename);
			byte[] data = new byte[ins.available()];
			
			ins.read(data);
			ins.close();
			
			return data;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
