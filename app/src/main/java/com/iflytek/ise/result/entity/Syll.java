/**
 * 
 */
package com.iflytek.ise.result.entity;

import java.util.ArrayList;

/**
 * <p>Title: Syll</p>
 * <p>Description: Syllable, corresponds to the Syll label in the xml</p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 12, 2015 15:49:51
 */
public class Syll {
	/**
	 * The position of the beginning frame,  each frame is equivalent to 10ms
	 */
	public int beg_pos;
	/**
	 * The position of ending frame
	 */
	public int end_pos;
	/**
	 * Syllable content
	 */
	public String content;
	/**
	 * Pinyin (cn), the number indicates tone, 5 indicates neutral tone, such as fen1
	 */
	public String symbol;
	/**
	 * Adds/Misses information: 0(Correct), 16(Miss), 32(Add), 64(Readback), 128(Replace)
	 */
	public int dp_message;
	/**
	 * Duration (Unit: Frame, each frame is equivalent to 10ms) (cn)
	 */
	public int time_len;
	/**
	 * Syllables contained in Syll
	 */
	public ArrayList<Phone> phones;
	
	/**
	 * Gets the standard phonetic of the syllable (en)
	 * 
	 * @return standard phonetic
	 */
	public String getStdSymbol() {
		String stdSymbol = "";
		String[] symbols = content.split(" ");
		
		for (int i = 0; i < symbols.length; i++) {
			stdSymbol += Phone.getStdSymbol(symbols[i]);
		}
		
		return stdSymbol;
	}
}
