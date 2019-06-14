package com.nokwary.airtimebuyer.utils;

import android.Manifest;
import android.media.AudioFormat;

public class AppDefine {

    //Log
    public static final String APP_TAG = "AudioRecorder";

    //permissions
    public static final String[] APP_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE
    };
    public static final int PERMISSIONS_REQUEST_CODE = 1240;

    //To Wave file conversion ...
    public static final int RECORDER_BPP = 16;
    public static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    public static final String AUDIO_RECORDER_FOLDER = "NokwarySpeech";
    public static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
    public static final String AUDIO_RECORDER_OUTPUT_FILE = "output_file.wav";


    //TTS
    public final static String EN_MALE_1 = "en-us-x-sfg#male_1-local";
    public final static int TTS_CODE_ONE = 100;
    public final static int TTS_CODE_TWO = 200;
    public final static int TTS_CODE_THREE = 300;
    public final static int TTS_CODE_FOUR = 400;
    public final static int TTS_CODE_FIVE = 500;


    //Audio Recording ...
    public static final int[] SAMPLE_RATE_CANDIDATES = new int[]{16000, 11025, 22050, 44100};

    public static final int CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    public static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    public static final int AMPLITUDE_THRESHOLD = 1500;
    public static final int SPEECH_TIMEOUT_MILLIS = 2000;
    public static final int MAX_SPEECH_LENGTH_MILLIS = 30 * 1000;

    //Network
    public static final String BASE_URL = "https://nokwary-speech.herokuapp.com/uploadfile";

}
