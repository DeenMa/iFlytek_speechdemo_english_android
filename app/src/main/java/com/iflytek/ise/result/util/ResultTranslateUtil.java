/**
 * 
 */
package com.iflytek.ise.result.util;

import java.util.HashMap;

/**
 * <p>Title: ResultTranslateUtl</p>
 * <p>Description: </p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date 2015年1月13日 下午6:05:03
 */
public class ResultTranslateUtil {
	
	private static HashMap<Integer, String> dp_message_map = new HashMap<Integer, String>();
	private static HashMap<String, String> special_content_map = new HashMap<String, String>();
	
	static {
		dp_message_map.put(0, "Normal");
		dp_message_map.put(16, "Read missed");
		dp_message_map.put(32, "Read extra");
		dp_message_map.put(64, "Read back");
		dp_message_map.put(128, "Replaced");
		
		special_content_map.put("sil", "Mute");
		special_content_map.put("silv", "Mute");
		special_content_map.put("fil", "Noise");
	}
	
	public static String getDpMessageInfo(int dp_message) {
		return dp_message_map.get(dp_message);
	}
	
	public static String getContent(String content) {
		String val = special_content_map.get(content);
		return (null == val)? content: val;
	}
}
