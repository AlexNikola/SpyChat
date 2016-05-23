package com.incode_it.spychat.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.MyTimerTask;
import com.incode_it.spychat.R;
import com.incode_it.spychat.alarm.AlarmReceiverIndividual;
import com.incode_it.spychat.amazon.DownloadService;
import com.incode_it.spychat.data_base.MyDbHelper;
import com.incode_it.spychat.interfaces.OnMessageDialogListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;

public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<MyChatRecyclerViewAdapter.MessageViewHolder>
{
    private ArrayList<Message> messages;
    private MyContacts.Contact contact;
    private Bitmap contactBitmap;
    private FragmentChat.OnFragmentChatInteractionListener listener;
    private OnChatAdapterListener chatAdapterListener;
    private String myPhoneNumber;
    private Bitmap noPhotoBitmap;
    private Bitmap emptyImageMessageBitmap;
    private Context context;
    private LruCache<String, Bitmap> mMemoryCache;
    private static final String DOWNLOAD_TAG = "amaz_download";

    public MyChatRecyclerViewAdapter(Context context,
                                     ArrayList<Message> messages,
                                     MyContacts.Contact contact,
                                     Bitmap contactBitmap,
                                     FragmentChat.OnFragmentChatInteractionListener listener,
                                     OnChatAdapterListener chatAdapterListener) {
        this.context = context;
        this.messages = messages;
        this.contact = contact;
        this.contactBitmap = contactBitmap;
        this.listener = listener;
        this.chatAdapterListener = chatAdapterListener;

        noPhotoBitmap = C.getNoPhotoBitmap(context);
        emptyImageMessageBitmap = C.getEmptyImageMessageBitmap(context);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, null);


        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType)
        {
            case Message.MY_MESSAGE_TEXT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_message_text_item, parent, false);
                return new MessageTextViewHolder(view);
            case Message.NOT_MY_MESSAGE_TEXT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.not_my_message_text_item, parent, false);
                return new MessageTextViewHolder(view);
            case Message.MY_MESSAGE_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_message_img_item, parent, false);
                return new MessageImageViewHolder(view);
            case Message.NOT_MY_MESSAGE_IMAGE:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.not_my_message_img_item, parent, false);
                return new MessageImageViewHolder(view);
            case Message.MY_MESSAGE_VIDEO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.my_message_video_item, parent, false);
                return new MessageVideoViewHolder(view);
            case Message.NOT_MY_MESSAGE_VIDEO:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.not_my_message_video_item, parent, false);
                return new MessageVideoViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(MessageViewHolder messageHolder, int position) {
        messageHolder.bindViewHolder(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static Bitmap getVideoFrame(Context context, String uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(uri);
            return retriever.getFrameAtTime(0);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }

    public class MessageVideoViewHolder extends MessageViewHolder
    {
        public ImageView videoMessage;
        public ImageView download;
        public ImageView play;

        //public VideoView videoView;

        public MessageVideoViewHolder(View itemView) {
            super(itemView);
            videoMessage = (ImageView) itemView.findViewById(R.id.video_message);
            download = (ImageView) itemView.findViewById(R.id.download);
            download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = messages.get(getAdapterPosition());
                    String filePath = message.getMessage();
                    //MyDbHelper.updateMediaPath(new MyDbHelper(context.getApplicationContext()).getWritableDatabase(), "", message.getMessageId());
                    Log.d("vfrm", "amazonLoadBitmap");
                    Intent serviceIntent = new Intent(context, DownloadService.class);
                    serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, filePath);
                    serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
                    serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_VIDEO);
                    context.getApplicationContext().startService(serviceIntent);

                    progressBar.setVisibility(View.VISIBLE);
                    message.state = Message.STATE_DOWNLOADING;
                    MyDbHelper.updateMessageState(new MyDbHelper(context.getApplicationContext()).getWritableDatabase(), Message.STATE_DOWNLOADING, message.getMessageId());
                    download.setVisibility(View.INVISIBLE);
                    play.setVisibility(View.INVISIBLE);
                }
            });
            play = (ImageView) itemView.findViewById(R.id.play);
            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = messages.get(getAdapterPosition());
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getMessage()));
                    intent.setDataAndType(Uri.parse(message.getMessage()), "video/mp4");
                    context.startActivity(intent);
                }
            });
        }

        @Override
        public void bindViewHolder(Message message) {
            timeText.setText(message.getDate());
            timerTextView.setText("");
            startTimer();

            //videoMessage.setImageBitmap(C.getEmptyVideoMessageBitmap(context));

            if (!message.getSenderPhoneNumber().equals(myPhoneNumber))
            {
                profileImageView.setVisibility(View.VISIBLE);
                if (contactBitmap == null) profileImageView.setImageBitmap(noPhotoBitmap);
                else profileImageView.setImageBitmap(contactBitmap);

                switch (message.state)
                {
                    case Message.STATE_ADDED:
                        messageContainer.setBackgroundResource(R.drawable.bg_not_my_message);
                        progressBar.setVisibility(View.INVISIBLE);

                        download.setVisibility(View.VISIBLE);
                        play.setVisibility(View.INVISIBLE);
                        break;
                    case Message.STATE_SUCCESS:
                        messageContainer.setBackgroundResource(R.drawable.bg_not_my_message);
                        progressBar.setVisibility(View.INVISIBLE);

                        download.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.VISIBLE);
                        break;
                    case Message.STATE_ERROR:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_error);
                        progressBar.setVisibility(View.INVISIBLE);

                        download.setVisibility(View.VISIBLE);
                        play.setVisibility(View.INVISIBLE);
                        break;
                    case Message.STATE_DOWNLOADING:
                        messageContainer.setBackgroundResource(R.drawable.bg_not_my_message);
                        progressBar.setVisibility(View.VISIBLE);

                        download.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.INVISIBLE);
                        break;
                }
            }
            else
            {
                //profileImageView.setImageBitmap(noPhotoBitmap);
                profileImageView.setVisibility(View.GONE);

                switch (message.state)
                {
                    case Message.STATE_ADDED:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_added);
                        progressBar.setVisibility(View.VISIBLE);

                        download.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.INVISIBLE);
                        break;
                    case Message.STATE_SUCCESS:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_success);
                        progressBar.setVisibility(View.INVISIBLE);

                        download.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.VISIBLE);
                        break;
                    case Message.STATE_ERROR:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_error);
                        progressBar.setVisibility(View.INVISIBLE);

                        download.setVisibility(View.INVISIBLE);
                        play.setVisibility(View.INVISIBLE);
                        break;
                }
            }



            Uri uri = Uri.parse(message.getMessage());
            String yourRealPath = uri.getPath();

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
            if (cursor != null)
            {
                if(cursor.moveToFirst()){
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    yourRealPath = cursor.getString(columnIndex);
                } else {
                    //boooo, cursor doesn't have rows ...
                }
                cursor.close();
            }


            Log.d("vfrm", "Path "+message.getMessage());
            Log.d("vfrm", "yourRealPath "+yourRealPath);

            localLoadBitmap(yourRealPath, videoMessage, "frame");



            /*videoView.setVideoURI(videoUri);
            videoView.setKeepScreenOn(true);
            videoView.start();*/
        }
    }

    public class MessageImageViewHolder extends MessageViewHolder
    {
        public ImageView imageMessage;

        public MessageImageViewHolder(View itemView) {
            super(itemView);

            imageMessage = (ImageView) itemView.findViewById(R.id.image_message);
        }

        @Override
        public void bindViewHolder(Message message) {
            super.bindViewHolder(message);
            imageMessage.setImageBitmap(C.emptyImageBitmap);
            String filePath = message.getMessage();
            Log.e(DOWNLOAD_TAG,"onReceive download");
            Log.d("vfrm", "Path "+message.getMessage());
            if (filePath.startsWith(C.MEDIA_TYPE_IMAGE + "/" + myPhoneNumber + "/"))
            {
                MyDbHelper.updateMediaPath(new MyDbHelper(context.getApplicationContext()).getWritableDatabase(), "", message.getMessageId());
                Log.d("vfrm", "amazonLoadBitmap");
                Intent serviceIntent = new Intent(context, DownloadService.class);
                serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, filePath);
                serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
                serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_IMAGE);
                context.getApplicationContext().startService(serviceIntent);

            }
            else if (!filePath.equals(""))
            {
                Log.d("vfrm", "localLoadBitmap");
                localLoadBitmap(filePath, imageMessage, "");
            }

            /*if (message.imageProgress > 0)
            {
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress((int) message.imageProgress);
                progressBar.setMax((int) message.imageTotalProgress);
            }*/


        }
    }

    public class MessageTextViewHolder extends MessageViewHolder
    {
        public TextView textMessage;

        public MessageTextViewHolder(View itemView) {
            super(itemView);

            textMessage = (TextView) itemView.findViewById(R.id.text_message);
        }

        @Override
        public void bindViewHolder(Message message) {
            super.bindViewHolder(message);
            textMessage.setText(message.getMessage());

            if (!message.getSenderPhoneNumber().equals(myPhoneNumber)) {
                textMessage.setTextColor(Color.parseColor("#000000"));
            } else {
                switch (message.state)
                {
                    case Message.STATE_ADDED:
                        textMessage.setTextColor(Color.parseColor("#55000000"));
                        break;
                    case Message.STATE_SUCCESS:
                        textMessage.setTextColor(Color.parseColor("#000000"));
                        break;
                    case Message.STATE_ERROR:
                        textMessage.setTextColor(Color.parseColor("#000000"));
                        break;
                }
            }
        }
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder implements OnMessageDialogListener {
        public ImageView profileImageView;
        public View messageContainer;
        public View progressBar;
        public TextView timeText;
        public TextView timerTextView;
        public MyTimerTask timerTask;

        public MessageViewHolder(View itemView) {
            super(itemView);

            timerTextView = (TextView) itemView.findViewById(R.id.timer_message_tv);
            profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
            progressBar = itemView.findViewById(R.id.progressBar);
            timeText = (TextView) itemView.findViewById(R.id.time_tv);
            messageContainer = itemView.findViewById(R.id.message_container);
            messageContainer.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d("merr", "onLongClick state "+messages.get(getAdapterPosition()).state);
                    if (messages.get(getAdapterPosition()).state == Message.STATE_SUCCESS)
                    {
                        listener.onCreateSuccessMessageDialog(MessageViewHolder.this);
                    }
                    else if (messages.get(getAdapterPosition()).state == Message.STATE_ERROR)
                    {
                        listener.onCreateErrorMessageDialog(MessageViewHolder.this);
                    }
                    return true;
                }
            });
        }

        public void bindViewHolder(Message message)
        {
            timeText.setText(message.getDate());
            timerTextView.setText("");
            startTimer();

            if (!message.getSenderPhoneNumber().equals(myPhoneNumber))
            {
                profileImageView.setVisibility(View.VISIBLE);
                if (contactBitmap == null) profileImageView.setImageBitmap(noPhotoBitmap);
                else profileImageView.setImageBitmap(contactBitmap);

                switch (message.state)
                {
                    case Message.STATE_ADDED:
                        messageContainer.setBackgroundResource(R.drawable.bg_not_my_message);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case Message.STATE_SUCCESS:
                        messageContainer.setBackgroundResource(R.drawable.bg_not_my_message);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case Message.STATE_ERROR:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_error);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                }
            }
            else
            {
                //profileImageView.setImageBitmap(noPhotoBitmap);
                profileImageView.setVisibility(View.GONE);

                switch (message.state)
                {
                    case Message.STATE_ADDED:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_added);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case Message.STATE_SUCCESS:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_success);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                    case Message.STATE_ERROR:
                        messageContainer.setBackgroundResource(R.drawable.bg_my_message_error);
                        progressBar.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        }

        @Override
        public void onDeleteMessage() {
            MyDbHelper.removeMessage(new MyDbHelper(context).getWritableDatabase(), messages.get(getAdapterPosition()).getMessageId());
            AlarmReceiverIndividual alarmReceiverIndividual = new AlarmReceiverIndividual();
            alarmReceiverIndividual.cancelAlarm(context, messages.get(getAdapterPosition()).getMessageId());
            messages.remove(getAdapterPosition());
            if (timerTask != null && timerTask.isRunning)
            {
                timerTask.cancel();
            }

            notifyItemRemoved(getAdapterPosition());
        }

        @Override
        public void onReSendMessage() {
            chatAdapterListener.onReSendMessage(messages.get(getAdapterPosition()));
        }

        @Override
        public void onSetTime() {
            listener.onCreateTimeDialog(this);
        }

        @Override
        public void onApplyTime(long removalTime, long timer) {
            Message message = messages.get(getAdapterPosition());

            AlarmReceiverIndividual alarmReceiverIndividual = new AlarmReceiverIndividual();
            if (timer == 0)
            {
                message.setRemovalTime(0);
                MyDbHelper.updateMessageTimer(new MyDbHelper((Context) listener).getWritableDatabase(), message.getMessageId(), 0);
                alarmReceiverIndividual.cancelAlarm((Context)listener, message.getMessageId());
            }
            else
            {
                message.setRemovalTime(removalTime);
                MyDbHelper.updateMessageTimer(new MyDbHelper((Context) listener).getWritableDatabase(), message.getMessageId(), removalTime);
                alarmReceiverIndividual.setAlarm((Context)listener, removalTime, message.getMessageId());
            }

            startTimer();
        }

        public void startTimer()
        {
            long removalTime = messages.get(getAdapterPosition()).getRemovalTime();
            if (timerTask != null && timerTask.isRunning)
            {
                timerTask.cancel();
            }
            if (removalTime > 0)
            {
                timerTask = new MyTimerTask(removalTime, timerTextView, -1);
                timerTask.isRunning = true;
                Timer myTimer = new Timer();
                myTimer.schedule(timerTask, 0, 1000);
            }
            else
            {
                timerTextView.setText("");
            }
        }
    }







    @Override
    public int getItemViewType(int position) {
        return messages.get(position).messageType;
    }

    public interface OnChatAdapterListener
    {
        void onReSendMessage(Message message);
    }



//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void localLoadBitmap(String filePath, ImageView imageView, String work) {
        if (cancelPotentialWork(filePath, imageView))
        {
            final Bitmap bitmap = getBitmapFromMemCache(filePath);
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
            else
            {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(context.getResources(), emptyImageMessageBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                //task.execute(uri);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, filePath, work);
            }
        }
    }

    private static boolean cancelPotentialWork(String filePath, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapFilePath = bitmapWorkerTask.imageFilePath;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapFilePath == null || !bitmapFilePath.equals(filePath)) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private String imageFilePath;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... resIds) {
            //Log.d(TAG, "doInBackground");


            imageFilePath = resIds[0];
            String work = resIds[1];
            Bitmap bitmap = null;
            if (work.equals("frame"))
            {
                bitmap = ThumbnailUtils.createVideoThumbnail(imageFilePath,
                        MediaStore.Images.Thumbnails.MINI_KIND);
            }
            else
            {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imageFilePath, options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, 200, 200);
                bitmap= BitmapFactory.decodeFile(imageFilePath, options);
            }

            if (bitmap != null) addBitmapToMemoryCache(imageFilePath, bitmap);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable
    {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask)
        {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask()
        {
            return bitmapWorkerTaskReference.get();
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d("lifes", "height: "+ height +" width: " + width + " inSampleSize: " + inSampleSize);
        return inSampleSize;
    }

}

