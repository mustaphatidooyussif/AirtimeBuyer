package com.nokwary.airtimebuyer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

//

public class DialogMessages {

    public static AlertDialog confirmationWithOneButtonDialog(Activity activity, String msg,
                                                              DialogInterface.OnClickListener positiveOnClick){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", positiveOnClick);
        AlertDialog alert = builder.create();
        alert.show();
        return  alert;
    }
}
