/**
 * 
 */
package com.iflytek.ise.result;

import com.iflytek.ise.result.util.ResultFormatUtil;

/**
 * <p>Title: ReadSyllableResult</p>
 * <p>Description: Chinese single word evaluation result</p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 12, 2015 17:03:14
 */
public class ReadSyllableResult extends Result {
	
	public ReadSyllableResult() {
		language = "cn";
		category = "read_syllable";
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[Overall result]\n")
			.append("Evaluation content: " + content + "\n")
			.append("Reading time length: " + time_len + "\n")
			.append("Total score: " + total_score + "\n\n")
			.append("[Reading details]").append(ResultFormatUtil.formatDetails_CN(sentences));
		
		return buffer.toString();
	}
}
