package com.incode_it.spychat.amazon;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Looper;
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
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.data_base.MyDbHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class UploadService extends IntentService {

    private final static String TAG = "amaz_upload";



    static ArrayList<TransferObserver> arrayList;
    //private TransferObserver transferObserver;
    final CountDownLatch latch = new CountDownLatch(1);

    public UploadService() {
        super("UploadService");
    }

    @Override
    public void onCreate() {
        arrayList = new ArrayList<>();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        upload(intent);
    }

    protected void upload(Intent intent) {

        Log.d(TAG, "onHandleIntent "+this.hashCode());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String path = intent.getStringExtra(C.EXTRA_MEDIA_FILE_PATH);
        final String mediaType = intent.getStringExtra(C.EXTRA_MEDIA_TYPE);
        final String opponentPhone = intent.getStringExtra(C.EXTRA_OPPONENT_PHONE_NUMBER);
        final int messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);

        File file = new File(path);

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "us-east-1:3bc44367-78a8-47e8-b689-1f05f72f74e5", // Identity Pool ID
                Regions.US_EAST_1 // Region
        );

        AmazonS3 s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.US_EAST_1));
        TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());

        TransferObserver transferObserver = transferUtility.upload(
                "spy-chat",     /* The bucket to upload to */
                mediaType + "/" + opponentPhone + "/" + file.getName(),       /* The key for the uploaded object */
                file       /* The file where the data to upload exists */
        );

        arrayList.add(transferObserver);

        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d(TAG, "my_state: " + state);
                if (state.toString().equals("COMPLETED"))
                {
                    boolean isMain = Looper.myLooper() == Looper.getMainLooper();
                    new SendMessageTask(messageId, opponentPhone, mediaType).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    Log.d(TAG, "COMPLETED" + isMain);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float curr = bytesCurrent;
                float total = bytesTotal;
                float percentage = (curr/total * 100f);
                Log.d(TAG, "my_percentage: " + percentage);
                /*Log.d("amaz_upload", "bytesCurrent: " + bytesCurrent);
                Log.d("amaz_upload", "bytesTotal: " + bytesTotal);*/
            }

            @Override
            public void onError(int id, Exception ex) {
                MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_ERROR, messageId);
                sendBroadcast(messageId, "FAILED");
                Log.e(TAG,"my_error: " + ex.getLocalizedMessage());
            }
        });

    }

    public class SendMessageTask extends AsyncTask<Integer, Void, String>
    {
        private Message message;
        private int messageId;
        private String opponentPhone;
        private String type;

        public SendMessageTask(int messageId, String opponentPhone, String type) {
            this.messageId = messageId;
            this.opponentPhone = opponentPhone;
            this.type = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {

            message = MyDbHelper.readMessage(new MyDbHelper(getApplicationContext()).getReadableDatabase(), messageId);
            String path = message.getMessage();
            File file = new File(path);
            String remoteMediaPath = type + "/" + opponentPhone + "/" + file.getName();

            String result = null;
            try
            {
                result = trySendMessage(remoteMediaPath, opponentPhone);
            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null)
            {
                message.state = Message.STATE_ERROR;
                MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_ERROR, messageId);
                sendBroadcast(messageId, "FAILED");
            }
            else
            {
                if (result.equals("success"))
                {
                    message.state = Message.STATE_SUCCESS;
                    MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_SUCCESS, messageId);
                    sendBroadcast(messageId, "COMPLETED");
                }
                else if (result.equals("error"))
                {
                    message.state = Message.STATE_ERROR;
                    MyDbHelper.updateMessageState(new MyDbHelper(getApplicationContext()).getWritableDatabase(), Message.STATE_ERROR, messageId);
                    sendBroadcast(messageId, "FAILED");
                }
            }
        }
    }

    private String trySendMessage(String remoteMediaPath, String opponentPhone) throws IOException, JSONException
    {
        StringBuilder sbParams = new StringBuilder();
        sbParams.append("message=").append(URLEncoder.encode(remoteMediaPath, "UTF-8")).append("&").append("destination=").append(URLEncoder.encode(opponentPhone, "UTF-8"));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
        URL url = new URL(C.BASE_URL + "api/v1/message/sendMessage/");
        String header = "Bearer "+accessToken;

        String response = MyConnection.post(url, sbParams.toString(), header);

        String result = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(getApplicationContext()))
                result = trySendMessage(remoteMediaPath, opponentPhone);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            result = jsonResponse.getString("result");
        }

        return result;
    }

    private void sendBroadcast(int messageId, String state)
    {
        Intent intent = new Intent(QuickstartPreferences.UPLOAD_MEDIA);
        intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
        intent.putExtra(C.EXTRA_MEDIA_STATE, state);
        LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.e(TAG,"onDestroy");
        super.onDestroy();
    }
}
