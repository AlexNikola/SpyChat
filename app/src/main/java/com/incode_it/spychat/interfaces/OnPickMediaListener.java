package com.incode_it.spychat.interfaces;


import android.content.Intent;

public interface OnPickMediaListener {

    //void onResult(int requestCode, int resultCode, Intent data);

    void onPickMedia(Intent intent, int requestCode);
}
