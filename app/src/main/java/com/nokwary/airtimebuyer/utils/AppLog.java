package com.nokwary.airtimebuyer.utils;

import android.util.Log;
import static com.nokwary.airtimebuyer.utils.AppDefine.APP_TAG;

public class AppLog {

    public static int infoString(String message){
        return Log.i(APP_TAG, message);
    }

    public static int debugString(String message){
        return Log.d(APP_TAG, message);
    }

    public static int warnString(String message){
        return Log.w(APP_TAG, message);
    }

    public static int errorString(String message){
        return Log.e(APP_TAG, message);
    }
}
