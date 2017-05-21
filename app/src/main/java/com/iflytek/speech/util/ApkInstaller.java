package com.iflytek.speech.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;

import com.iflytek.cloud.SpeechUtility;


/**
 * Pops a prompt box to download service component
 */
public class ApkInstaller {
	private Activity mActivity ;
	
	public ApkInstaller(Activity activity) {
		mActivity = activity;
	}

	public void install(){
		AlertDialog.Builder builder = new Builder(mActivity);
		builder.setMessage("VoiceNote app is not detected!\nProceed to download VoiceNote?");
		builder.setTitle("Download tips");
		builder.setPositiveButton("Confirm", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				String url = SpeechUtility.getUtility().getComponentUrl();
				String assetsApk="SpeechService.apk";
				processInstall(mActivity, url,assetsApk);
			}
		});
		builder.setNegativeButton("Cancel", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
		return;
	}
	/**
	 * If the service component did not open the download page to install the speech service component, please install after downloading
	 */
	private boolean processInstall(Context context ,String url,String assetsApk){
		// Direct download mode
		Uri uri = Uri.parse(url);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		context.startActivity(it);
		return true;		
	}
}
