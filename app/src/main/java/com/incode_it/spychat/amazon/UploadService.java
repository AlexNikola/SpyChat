package com.incode_it.spychat.amazon;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
import com.incode_it.spychat.QuickstartPreferences;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class UploadService extends IntentService {

    private String path;
    private File file;
    private int messageId;

    //private TransferObserver transferObserver;
    final CountDownLatch latch = new CountDownLatch(1);

    public UploadService() {
        super("UploadService");
    }

    /*@Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }*/

    @Override
    protected void onHandleIntent(Intent intent) {

        upload(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                upload(intent);
            }
        }).start();*/

        return super.onStartCommand(intent, flags, startId);
    }

    protected void upload(Intent intent) {

        Log.d("amaz_upload", "onHandleIntent "+this.hashCode());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        path = intent.getStringExtra(C.EXTRA_MEDIA_FILE_PATH);
        messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);

        //if (path == null) stopSelf();

        file = new File(path);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:2fb30153-0f2b-4f60-bbd2-28d08efa98f2", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        TransferObserver transferObserver = transferUtility.upload(
                "spy-chat-bucket",     /* The bucket to upload to */
                "public/"+file.getName(),       /* The key for the uploaded object */
                file       /* The file where the data to upload exists */
        );

        //arrayList.add(transferObserver);

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("amaz_upload", "state: " + state);
                if (state.toString().equals("COMPLETED"))
                {
                    Intent intent = new Intent(QuickstartPreferences.RECEIVE_MEDIA);
                    intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
                    intent.putExtra(C.EXTRA_MEDIA_STATE, "s");
                    LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
                    //latch.countDown();
                    //stopSelf();
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float curr = bytesCurrent;
                float total = bytesTotal;
                float percentage = (curr/total * 100f);
                Log.d("amaz_upload", "percentage: " + percentage);
                /*Log.d("amaz_upload", "bytesCurrent: " + bytesCurrent);
                Log.d("amaz_upload", "bytesTotal: " + bytesTotal);*/
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("amaz_upload","error: " + ex.getLocalizedMessage());
            }
        });


        /*try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onDestroy() {
        Log.e("amaz_upload","onDestroy");
        super.onDestroy();
    }
}
