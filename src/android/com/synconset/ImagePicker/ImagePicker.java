/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.content.pm.PackageManager;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";

	public static final int PERMISSION_DENIED_ERROR = 20;
	 
	private CallbackContext callbackContext;
	private JSONObject params;

	private int max;
	private int desiredWidth;
	private int desiredHeight;
	private int quality;

	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {

			this.max = 20;
			this.desiredWidth = 0;
			this.desiredHeight = 0;
			this.quality = 100;

			if (this.params.has("maximumImagesCount")) {
				this.max = this.params.getInt("maximumImagesCount");
			}
			if (this.params.has("width")) {
				this.desiredWidth = this.params.getInt("width");
			}
			if (this.params.has("height")) {
				this.desiredWidth = this.params.getInt("height");
			}
			if (this.params.has("quality")) {
				this.quality = this.params.getInt("quality");
			}

			if (PermissionHelper.hasPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
				 this.getImages();
			} else {
				PermissionHelper.requestPermission(this, 0, Manifest.permission.READ_EXTERNAL_STORAGE);
			}
		}
		return true;
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			JSONArray res = new JSONArray(fileNames);
			this.callbackContext.success(res);
		} else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			JSONArray res = new JSONArray();
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
	}

	/**
	 * Choosing a picture launches another Activity, so we need to implement the
	 * save/restore APIs to handle the case where the CordovaActivity is killed by the OS
	 * before we get the launched Activity's result.
	 *
	 * @see http://cordova.apache.org/docs/en/dev/guide/platforms/android/plugin.html#launching-other-activities
	*/
	public void onRestoreStateForActivityResult(Bundle state, CallbackContext callbackContext) {
	    this.callbackContext = callbackContext;
	}

	public void getImages() {
		Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);

		intent.putExtra("MAX_IMAGES", this.max);
		intent.putExtra("WIDTH", this.desiredWidth);
		intent.putExtra("HEIGHT", this.desiredHeight);
		intent.putExtra("QUALITY", this.quality);

		if (this.cordova != null) {
			this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
		}
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions,
										int[] grantResults) throws JSONException
	{
		for(int r:grantResults)
		{
			if(r == PackageManager.PERMISSION_DENIED)
			{
				this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
				return;
			}
		}
		switch(requestCode)
		{
			case 0:
				this.getImages();
				break;
			default:
				break;
		}
	}
}