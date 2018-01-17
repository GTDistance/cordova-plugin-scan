package com.thomas.scan;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.thomas.scanlibrary.MipcaActivityCapture;



public class Scan extends CordovaPlugin {
    private final static int SCANNIN_GREQUEST_CODE = 1;
    private CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Activity activity = cordova.getActivity();
        this.callbackContext = callbackContext;


        if("recognize".equals(action)) {

            Intent intent = new Intent();
            intent.setClass(activity, MipcaActivityCapture.class);
            cordova.startActivityForResult((CordovaPlugin) this,intent, SCANNIN_GREQUEST_CODE);
        }
        return true;
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case SCANNIN_GREQUEST_CODE:
                if(resultCode == Activity.RESULT_OK){
                    Bundle bundle = data.getExtras();
                    callbackContext.success(bundle.getString("result"));
                }
                break;
        }
    }

}
