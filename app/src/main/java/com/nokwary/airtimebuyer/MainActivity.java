package com.nokwary.airtimebuyer;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.nokwary.airtimebuyer.utils.AppLog;
import com.nokwary.airtimebuyer.utils.AppPermissions;
import com.nokwary.airtimebuyer.utils.CustomProgressDialog;
import com.nokwary.airtimebuyer.utils.DialogMessages;
import com.nokwary.airtimebuyer.utils.HttpMultipartUpload;
import com.nokwary.airtimebuyer.utils.VoiceManager;
import com.nokwary.airtimebuyer.utils.VoiceManagerListenerImpl;
import com.nokwary.airtimebuyer.utils.VoiceRecorder;
import com.nokwary.airtimebuyer.utils.Wave;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

import static com.nokwary.airtimebuyer.utils.AppDefine.APP_PERMISSIONS;
import static com.nokwary.airtimebuyer.utils.AppDefine.PERMISSIONS_REQUEST_CODE;
import static com.nokwary.airtimebuyer.utils.AppDefine.EN_MALE_1;
import static com.nokwary.airtimebuyer.utils.VoiceRecorder.*;
import static com.nokwary.airtimebuyer.utils.AppDefine.TTS_CODE_ONE;
import static com.nokwary.airtimebuyer.utils.AppDefine.BASE_URL;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener, Callback , VoiceManager {

    private TextToSpeech textToSpeech;
    private boolean isRecording = false;
    private Thread recordingThread = null;
    private VoiceRecorder recorder = null;
    private int lastListeningType = 0;
    private CustomProgressDialog dialog;
    private VoiceManagerListener voiceManagerListener;
    private static String fileName = null;
    private ProgressDialog p;
    private String  request_code = null;
    private VoiceManagerListenerImpl voiceMnImpl;
    private FileOutputStream os = null;
    private String filename = null;
    private MediaPlayer beeOneMediaPlayer = null;
    private MediaPlayer beeTwoMediaPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for app permissions
        // In case one or more permissions are not granted,
        // ActivityCompat.requestPermissions() will request permissions
        // and the control goes to onRequestPermissionsResult() callback method.
        if (checkAndRequestPermissions())
        {
            // All permissions are granted already. Proceed ahead
            initApp();
        }
    }

    @Override
    public void speak(String text) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    public void speak(String speech, int requestCode) {
        speakOut(requestCode, speech);
    }

    @Override
    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
        textToSpeech = null;
    }

    @Override
    public void makeTransaction(String teleco, String number, Double amt) {
        String msg = "An amount of " + String.valueOf(amt)+ " cedis airtime has been added to the \n " +
                teleco.toUpperCase() + " number: " + number +".\n Thank you!!";
        DialogMessages.confirmationWithOneButtonDialog(this, msg, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do things
                AppLog.infoString("Transaction completed...");
            }
        });
    }

    @Override
    public void setVoiceManagerListener(VoiceManagerListener listener) {
        voiceManagerListener = listener;
    }

    private void speakOut(int requestCode, String text) {
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "");
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, params, String.valueOf(requestCode));
    }

    public interface VoiceManagerListener {

        void onResults(String results, int type);

        void onSpeechCompleted(int requestCode);

        void onSpeechError(int requestCode);
    }

    public  boolean checkAndRequestPermissions()
    {
        // Check which permissions are granted
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String perm : APP_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
            {
                listPermissionsNeeded.add(perm);
            }
        }

        // Ask for non-granted permissions
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,
                    listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),
                    PERMISSIONS_REQUEST_CODE
            );
            return false;
        }

        // App has all permissions. Proceed ahead
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_CODE)
        {
            HashMap<String, Integer> permissionResults = new HashMap<>();
            int deniedCount = 0;

            // Gather permission grant results
            for (int i=0; i<grantResults.length; i++)
            {
                // Add only permissions which are denied
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                {
                    permissionResults.put(permissions[i], grantResults[i]);
                    deniedCount++;
                }
            }

            // Check if all permissions are granted
            if (deniedCount == 0)
            {
                // Proceed ahead with the app
                initApp();
            }
            // Atleast one or all permissions are denied
            else
            {
                for (Map.Entry<String, Integer> entry : permissionResults.entrySet())
                {
                    String permName = entry.getKey();
                    int permResult = entry.getValue();

                    // permission is denied (this is the first time, when "never ask again" is not checked)
                    // so ask again explaining the usage of permission
                    // shouldShowRequestPermissionRationale will return true
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permName))
                    {
                        // Show dialog of explanation
                        AppPermissions.showDialog("", "This app needs Location and Storage permissions to work wihout any issues and problems.",
                                "Yes, Grant permissions",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        AppPermissions.checkAndRequestPermissions();
                                    }
                                },
                                "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                    }
                    //permission is denied (and never ask again is  checked)
                    //shouldShowRequestPermissionRationale will return false
                    else
                    {
                        // Ask user to go to settings and manually allow permissions
                        AppPermissions.showDialog("", "You have denied some permissions to the app. Please allow all permissions at [Setting] > [Permissions] screen",
                                "Go to Settings",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        // Go to app settings
                                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", getPackageName(), null));
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                },
                                "No, Exit app", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                        finish();
                                    }
                                }, false);
                        break;
                    }
                }
            }
        }
    }

  public void initApp(){
      //TTS
      textToSpeech = new TextToSpeech(MainActivity.this, this, "com.google.android.tts");

      //voice recorder
      recorder = new VoiceRecorder(this);

      // Record to the external cache directory for visibility
      fileName = Wave.getFilename();
      voiceMnImpl = new VoiceManagerListenerImpl(this);

      //beep sounds
      beeOneMediaPlayer = MediaPlayer.create(this, R.raw.beep1);
      beeTwoMediaPlayer = MediaPlayer.create(this, R.raw.beep2);
  }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.getDefault());
            textToSpeech.setOnUtteranceProgressListener(utteranceProgressListener);  //progress listener
            TTSSettings(1,1, EN_MALE_1);  //tts config

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(MainActivity.this, "TTS language is not supported", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "TTS initialization failed", Toast.LENGTH_LONG).show();
        }
    }

    public  void TTSSettings(float pitch, float speechRate, String accent) {
        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speechRate);
        Set<String> a = new HashSet<>();
        a.add("male");

        //here you can give male if you want to select mail voice.
        Voice voiceobj = new Voice(accent, Locale.getDefault(), Voice.QUALITY_HIGH, Voice.LATENCY_NORMAL, true, a);
        textToSpeech.setVoice(voiceobj);
    }

    public void startSpeech(View v){

        startSpeechListening(TTS_CODE_ONE);
    }

    @Override
    public void startSpeechListening(int requestCode){

        lastListeningType = requestCode;
        recorder.start();
    }

    @Override
    public void stopSpeechListening(){
        recorder.stop();
    }

    UtteranceProgressListener utteranceProgressListener =new UtteranceProgressListener() {
        @Override
        public void onStart(String s) {
            final String keyword = s;
            Toast.makeText(MainActivity.this, keyword, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onDone(final String s) {

            AppLog.infoString("---------------------------onDone: " + s);
            final int requestCode = Integer.parseInt(s);

            Handler mainHandler = new Handler(Looper.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    if (voiceManagerListener != null) {
                        voiceManagerListener.onSpeechCompleted(requestCode);
                    }
                }
            };
            mainHandler.post(myRunnable);
        }

        @Override
        public void onError(String s) {
            final String keyword = s;
            Toast.makeText(MainActivity.this, keyword, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onVoiceStart() {
        // TODO 1: start beep sound
        beeOneMediaPlayer.start();

        isRecording = true;
        AppLog.infoString("on voice start");

        filename = Wave.getTempFilename();

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onVoice(final byte[] data, final int size) {
        AppLog.infoString("recording ...");

        if(AudioRecord.ERROR_INVALID_OPERATION != size){
            try {
                os.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onVoiceEnd() {
        //Wave.deleteTempFile();
        AppLog.infoString("on voice end");

        //TODO 2:end beep sound
        beeTwoMediaPlayer.start();

        //isRecording = false;
        Wave.copyWaveFile(Wave.getTempFilename(),Wave.getFilename(), recorder.getSampleRate(), recorder.getBufferSize());
        Wave.deleteTempFile();
        stopSpeechListening();

        //send the request
        recognizeSpeech();
    }

    public void recognizeSpeech(){

        new RecognizeSpeechTask().execute();

    }

    public class RecognizeSpeechTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(Void... params) {
            String request_code = Integer.toString(lastListeningType);
            return HttpMultipartUpload.uploadFile(fileName, BASE_URL, request_code);
        }


        protected void onPostExecute(String result) {

            if (result != null && !result.equals("") && result.contains("message")) {
                //call on complete listener
                AppLog.infoString(result);
                JSONArray jsonarray = null;
                try {
                    jsonarray = new JSONArray(String.format("[%s]", result));
                    JSONObject jsonobject = jsonarray.getJSONObject(0);
                    Toast.makeText(MainActivity.this, jsonobject.getString("message"), Toast.LENGTH_LONG).show();

                    //Call on complete listener
                    int responseCode = Integer.parseInt(jsonobject.getString("code"));
                    String msg =  jsonobject.getString("message");
                    voiceManagerListener.onResults(msg, responseCode);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                //start recording again
                //Call on speech error listener
                voiceManagerListener.onSpeechError(lastListeningType);
            }
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if( dialog != null){
            dialog.hideDialog();
        }
        dialog = null;

        shutdown();
    }
}
