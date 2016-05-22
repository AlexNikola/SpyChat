package com.incode_it.spychat.amazon;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.data_base.MyDbHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DownloadService extends IntentService {

    public final static String TAG = "amaz_download";
    static ArrayList<TransferObserver> arrayList;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        arrayList = new ArrayList<>();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        download(intent);
    }

    protected void download(Intent intent) {

        Log.d(TAG, "onHandleIntent "+this.hashCode());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, "");

        final String remotePath = intent.getStringExtra(C.EXTRA_MEDIA_FILE_PATH);
        final String mediaType = intent.getStringExtra(C.EXTRA_MEDIA_TYPE);
        final int messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);

        File remoteFile = new File(remotePath);
        File localPath = null;
        if (remotePath.startsWith(C.MEDIA_TYPE_IMAGE + "/"))
        {
            try {
                localPath = createImageFile(remoteFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (remotePath.startsWith(C.MEDIA_TYPE_VIDEO + "/"))
        {

        }

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:2fb30153-0f2b-4f60-bbd2-28d08efa98f2", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        TransferObserver transferObserver = transferUtility.download(
                "spy-chat-bucket",     /* The bucket to upload to */
                mediaType + "/" + myPhoneNumber + "/" + remoteFile.getName(),       /* The key for the uploaded object */
                localPath      /* The file where the data to upload exists */
        );

        arrayList.add(transferObserver);

        final File finalLocalPath = localPath;
        Log.d(TAG, "remote_path: " + remotePath);
        Log.d(TAG, "download_from: " + mediaType + "/" + myPhoneNumber + "/" + remoteFile.getName());
        Log.d(TAG, "localPath: " + finalLocalPath);

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "download_state: " + state);
                if (state.toString().equals("COMPLETED"))
                {
                    MyDbHelper.updateMediaPath(new MyDbHelper(getApplicationContext()).getWritableDatabase(), finalLocalPath.getAbsolutePath(), messageId);
                    MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_SUCCESS, messageId);
                    sendBroadcast(messageId, "COMPLETED", mediaType, finalLocalPath.getAbsolutePath());
                }
                else if (state.toString().equals("FAILED"))
                {
                    MyDbHelper.updateMediaPath(new MyDbHelper(getApplicationContext()).getWritableDatabase(), remotePath, messageId);
                    MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_ERROR, messageId);
                    sendBroadcast(messageId, "FAILED", mediaType, finalLocalPath.getAbsolutePath());
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float curr = bytesCurrent;
                float total = bytesTotal;
                float percentage = (curr/total * 100f);
                Log.d(TAG, "download_percentage: " + percentage);
            }

            @Override
            public void onError(int id, Exception ex) {
                MyDbHelper.updateMediaPath(new MyDbHelper(getApplicationContext()).getWritableDatabase(), remotePath, messageId);
                MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_ERROR, messageId);
                sendBroadcast(messageId, "FAILED", mediaType, finalLocalPath.getAbsolutePath());
                Log.e(TAG,"download_error: " + ex.getLocalizedMessage());
            }
        });

    }

    private File createImageFile(String fileName) throws IOException {
        /*File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);*/
        File storageDir = this.getExternalFilesDir(null);
        String fullDir = storageDir.getAbsolutePath() + "/" + fileName;

        return new File(fullDir);
    }

    private void sendBroadcast(int messageId, String state, String mediaType, String localPath)
    {
        Intent intent = new Intent(QuickstartPreferences.DOWNLOAD_MEDIA);
        intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(C.EXTRA_MEDIA_TYPE, mediaType);
        intent.putExtra(C.EXTRA_MEDIA_STATE, state);
        intent.putExtra(C.EXTRA_MEDIA_LOCAL_PATH, localPath);
        LocalBroadcastManager.getInstance(DownloadService.this).sendBroadcast(intent);
    }

}
