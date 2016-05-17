package com.amazonaws.mobile;


import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.UserFileManager;
import com.incode_it.spychat.C;
import com.incode_it.spychat.QuickstartPreferences;

import java.io.File;

public class UploadService extends IntentService {

    private String path;
    private File file;
    private int messageId;

    public UploadService() {
        super("UploadService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d("amaz_upload", "onHandleIntent");
        path = intent.getStringExtra(C.EXTRA_MEDIA_FILE_PATH);
        messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);
        file = new File(path);

        AWSMobileClient.defaultMobileClient()
                .createUserFileManager(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public",
                        new UserFileManager.BuilderResultHandler() {
                            @Override
                            public void onComplete(final UserFileManager userFileManager) {

                                uploadFile(userFileManager);
                            }
                        });

    }

    private void uploadFile(UserFileManager userFileManager)
    {
        Log.d("amaz_upload", "uploadFile");
        userFileManager.uploadContent(file, file.getName(), new ContentProgressListener() {
            @Override
            public void onSuccess(final ContentItem contentItem) {
                Log.d("amaz_upload", "onSuccess");
                Intent intent = new Intent(QuickstartPreferences.RECEIVE_MEDIA);
                intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
                intent.putExtra(C.EXTRA_MEDIA_STATE, "s");
                LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
            }

            @Override
            public void onProgressUpdate(final String fileName, final boolean isWaiting,
                                         final long bytesCurrent, final long bytesTotal) {
                Log.d("amaz_upload", "onProgressUpdate "+bytesTotal+" "+bytesCurrent);
                Intent intent = new Intent(QuickstartPreferences.RECEIVE_MEDIA);
                intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
                intent.putExtra(C.EXTRA_MEDIA_STATE, "p");
                intent.putExtra(C.EXTRA_MEDIA_PROGRESS_TOTAL, bytesTotal);
                intent.putExtra(C.EXTRA_MEDIA_PROGRESS_CURRENT, bytesCurrent);
                LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
            }

            @Override
            public void onError(final String fileName, final Exception ex) {
                Log.e("amaz_upload", "onError "+ex.getLocalizedMessage());
                Intent intent = new Intent(QuickstartPreferences.RECEIVE_MEDIA);
                intent.putExtra(C.EXTRA_MESSAGE_ID, messageId);
                intent.putExtra(C.EXTRA_MEDIA_STATE, "e");
                LocalBroadcastManager.getInstance(UploadService.this).sendBroadcast(intent);
            }
        });
    }
}
