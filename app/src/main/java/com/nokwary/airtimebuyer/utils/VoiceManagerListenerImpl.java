package com.nokwary.airtimebuyer.utils;

import com.nokwary.airtimebuyer.MainActivity;

public class VoiceManagerListenerImpl implements MainActivity.VoiceManagerListener {
    private VoiceManager voiceManager;

    public VoiceManagerListenerImpl(VoiceManager vm){
        voiceManager = vm;
        voiceManager.setVoiceManagerListener(this);
    }

    //TODO 6: remove hardcoded codes
    @Override
    public void onResults(String results, int type) {
        switch (type){
            case 200:
                voiceManager.speak(results , type);
                break;

            case 300:
                voiceManager.speak(results , type);
                break;

            case 0:
                voiceManager.speak(results);
                break;

            case 1:
                //TODO 2: Remove hard coded teleco, number and amount.
                String teleco = "mtn";
                String number = "233542300000";
                Double amt = 12.00;
                //send request to mtn
                voiceManager.makeTransaction(teleco, number, amt);
                break;

        }
    }

    //TODO 7: remove hardcoded codes
    @Override
    public void onSpeechCompleted(int requestCode) {
        switch (requestCode){
            case 200:
                voiceManager.startSpeechListening(requestCode);
                break;
            case 300:
                voiceManager.startSpeechListening(requestCode);
                break;
            default:
                break;
        }
    }

    @Override
    public void onSpeechError(int requestCode) {
        voiceManager.startSpeechListening(requestCode);
    }
}
