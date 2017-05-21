/**
 * 
 */
package com.iflytek.ise.result.entity;

import java.util.HashMap;

/**
 * <p>Title: Phone</p>
 * <p>Description: phoneme, corresponds to the Phone label in the xml result</p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 12, 2015 15:55:56
 */
public class Phone {
	/**
	 * iFlytek Phonetic -Standard Phonetic Mapping Table (en)
	 */
	public static HashMap<String, String> phone_map = new HashMap<String, String>();
	
	static {
		phone_map.put("aa", "ɑ:");
		phone_map.put("oo", "ɔ");
		phone_map.put("ae", "æ");
		phone_map.put("ah", "ʌ");
		phone_map.put("ao", "ɔ:");
		phone_map.put("aw", "aʊ");
		phone_map.put("ax", "ə");
		phone_map.put("ay", "aɪ");
		phone_map.put("eh", "e");
		phone_map.put("er", "ə:");
		phone_map.put("ey", "eɪ");
		phone_map.put("ih", "ɪ");
		phone_map.put("iy", "i:");
		phone_map.put("ow", "əʊ");
		phone_map.put("oy", "ɔɪ");
		phone_map.put("uh", "ʊ");
		phone_map.put("uw", "ʊ:");
		phone_map.put("ch", "tʃ");
		phone_map.put("dh", "ð");
		phone_map.put("hh", "h");
		phone_map.put("jh", "dʒ");
		phone_map.put("ng", "ŋ");
		phone_map.put("sh", "ʃ");
		phone_map.put("th", "θ");
		phone_map.put("zh", "ʒ");
		phone_map.put("y", "j");
		phone_map.put("d", "d");
		phone_map.put("k", "k");
		phone_map.put("l", "l");
		phone_map.put("m", "m");
		phone_map.put("n", "n");
		phone_map.put("b", "b");
		phone_map.put("f", "f");
		phone_map.put("g", "g");
		phone_map.put("p", "p");
		phone_map.put("r", "r");
		phone_map.put("s", "s");
		phone_map.put("t", "t");
		phone_map.put("v", "v");
		phone_map.put("w", "w");
		phone_map.put("z", "z");
		phone_map.put("ar", "eə");
		phone_map.put("ir", "iə");
		phone_map.put("ur", "ʊə");
		phone_map.put("tr", "tr");
		phone_map.put("dr", "dr");
		phone_map.put("ts", "ts");
		phone_map.put("dz", "dz");
	}
	
	/**
	 * The position of the beginning frame,  each frame is equivalent to 10ms
	 */
	public int beg_pos;
	/**
	 * The position of the ending frame
	 */
	public int end_pos;
	/**
	 * The phoneme content
	 */
	public String content;
	/**
	 * Adds/Misses information: 0(Correct), 16(Miss), 32(Add), 64(Readback), 128(Replace)
	 */
	public int dp_message;
	/**
	 * Duration (Unit: Frame, each frame is equivalent to 10ms) (cn)
	 */
	public int time_len;
	
	/**
	 * Gets the standard phonetic corresponding to content (en)
	 */
	public String getStdSymbol() {
		return getStdSymbol(content);
	}
	
	public static String getStdSymbol(String content) {
		String std = phone_map.get(content);
		return (null == std)? content: std;
	}
	
}
