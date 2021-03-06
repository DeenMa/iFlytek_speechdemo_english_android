/**
 * 
 */
package com.iflytek.ise.result;

import com.iflytek.ise.result.util.ResultFormatUtil;

/**
 * <p>Title: ReadWordResult</p>
 * <p>Description: </p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 12, 2015 17:03:50
 */
public class ReadWordResult extends Result {
	
	public ReadWordResult() {
		category = "read_word";
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		
		if ("cn".equals(language)) {
			buffer.append("[Overall result]\n")
				.append("Evaluation content: " + content + "\n")
				.append("Reading time length: " + time_len + "\n")
				.append("Total score: " + total_score + "\n\n")
				.append("[Reading details]")
				.append(ResultFormatUtil.formatDetails_CN(sentences));
		} else {
			if (is_rejected) {
				buffer.append("Randomly reading detected，")
				.append("except_info:" + except_info + "\n\n");	// except_info For code description, please refer to the documents for Speech Evaluation and Result Description
			}
			
			buffer.append("[Overall result]\n")
				.append("Evaluation content: " + content + "\n")
				.append("Total score: " + total_score + "\n\n")
				.append("[Reading details]")
				.append(ResultFormatUtil.formatDetails_EN(sentences));
		}
		
		return buffer.toString();
	}
}
