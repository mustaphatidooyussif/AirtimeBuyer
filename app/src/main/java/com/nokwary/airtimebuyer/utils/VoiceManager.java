package com.nokwary.airtimebuyer.utils;

import com.nokwary.airtimebuyer.MainActivity;

public interface VoiceManager {

    void speak(String text);

    void speak( String speech,  int requestCode);

    void startSpeechListening(int requestCode);

    void stopSpeechListening();

    void shutdown();

    void makeTransaction(String teleco, String number, Double amt);

    void setVoiceManagerListener(MainActivity.VoiceManagerListener listener);
}