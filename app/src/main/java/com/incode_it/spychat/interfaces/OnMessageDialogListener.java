package com.incode_it.spychat.interfaces;

public interface OnMessageDialogListener {
    void onSetTime();

    void onDeleteMessage();

    void onReSendMessage();

    void onApplyTime(long removalTime, long timer);
}
