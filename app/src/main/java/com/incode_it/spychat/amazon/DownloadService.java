package com.incode_it.spychat.amazon;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

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
import java.util.ArrayList;

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
        download(intent);
    }

    protected void download(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, null);

        final String remotePath = intent.getStringExtra(C.EXTRA_MEDIA_FILE_PATH);
        final String mediaType = intent.getStringExtra(C.EXTRA_MEDIA_TYPE);
        final int messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);

        final File remoteFile = new File(remotePath);
        File localFile = null;
        if (remotePath.startsWith(C.MEDIA_TYPE_IMAGE + "/"))
        {
            try {
                localFile = createImageFile(remoteFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (remotePath.startsWith(C.MEDIA_TYPE_VIDEO + "/"))
        {
            try {
                localFile = createVideoFile(remoteFile.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                C.amazonIdentityPoolID,
                C.amazonRegion);

        final AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        TransferObserver transferObserver = transferUtility.download(
                C.amazonBucket,
                remotePath,
                localFile);

        arrayList.add(transferObserver);

        final File finalLocalPath = localFile;

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.toString().equals("COMPLETED"))
                {
                    MyDbHelper.updateMediaPath(new MyDbHelper(getApplicationContext()).getWritableDatabase(), finalLocalPath.getAbsolutePath(), messageId);
                    MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_SUCCESS, messageId);
                    deleteRemoteFile(s3, C.amazonBucket, remotePath);
                    sendBroadcast(messageId, "COMPLETED", mediaType, finalLocalPath.getAbsolutePath());
                }
                else if (state.toString().equals("FAILED"))
                {

                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                /*float curr = bytesCurrent;
                float total = bytesTotal;
                float percentage = (curr/total * 100f);*/
            }

            @Override
            public void onError(int id, Exception ex) {
                MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_ERROR, messageId);
                sendBroadcast(messageId, "FAILED", mediaType, finalLocalPath.getAbsolutePath());
            }
        });

    }

    private void deleteRemoteFile(final AmazonS3 s3, final String bucket, final String keyName)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                s3.deleteObject(bucket, keyName);
            }
        }).start();

    }

    private File createImageFile(String fileName) throws IOException {
        File storageDir = this.getExternalFilesDir(null);
        String fullDir = storageDir.getAbsolutePath() + "/" + fileName;
        return new File(fullDir);
    }

    private File createVideoFile(String fileName) throws IOException {
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
