package com.incode_it.spychat.chat;


import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.amazonaws.mobile.UploadServiceA;
import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;
import com.incode_it.spychat.amazon.UploadService;
import com.incode_it.spychat.data_base.MyDbHelper;
import com.incode_it.spychat.interfaces.OnMessageDialogListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentChat extends Fragment implements MyChatRecyclerViewAdapter.OnChatAdapterListener {

    static final int REQUEST_IMAGE_CAPTURE = 11;
    static final int REQUEST_VIDEO_CAPTURE = 12;


    private static final int SEND_MESSAGE_DELAY = 500;
    private static final String TAG = "chatm";
    public static final String TAG_FRAGMENT = "FragmentChat";
    private String opponentPhone;
    private RecyclerView recyclerView;
    private EditText editText;
    private MyChatRecyclerViewAdapter adapter;
    private Bitmap contactBitmap;
    private String myPhoneNumber;
    private BroadcastReceiver mDeleteMessagesReceiver;

    private ArrayList<Message> messageArrayList;

    private BroadcastReceiver mMediaReceiver;
    private boolean isMediaReceiverRegistered;
    private BroadcastReceiver mMessageReceiver;
    private boolean isMessageReceiverRegistered;
    private boolean isDeleteMessagesReceiverRegistered;
    private OnFragmentChatInteractionListener fragmentChatInteractionListener;


    private MyContacts.Contact contact;




    private SharedPreferences sharedPreferences;

    public FragmentChat() {
        // Required empty public constructor
    }

    public static FragmentChat newInstance(String phone) {
        FragmentChat fragment = new FragmentChat();
        Bundle bundle = new Bundle();
        bundle.putString(C.EXTRA_OPPONENT_PHONE_NUMBER, phone);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        fragmentChatInteractionListener = (OnFragmentChatInteractionListener) context;
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMediaReceiver);
        isMediaReceiverRegistered = false;

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        isMessageReceiverRegistered = false;

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDeleteMessagesReceiver);
        isDeleteMessagesReceiverRegistered = false;
        Log.e(TAG, "onPause Fragment Chat");
        super.onPause();
    }

    private void openVideoCamera()
    {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            /*Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");*/
            //mImageView.setImageBitmap(imageBitmap);

            String photoPath = sharedPreferences.getString(C.SHARED_NEW_PHOTO_PATH, "error");

            final Message message = new Message(photoPath, myPhoneNumber, contact.phoneNumber, Message.STATE_ADDED, Message.MY_MESSAGE_IMAGE);
            messageArrayList.add(message);
            adapter.notifyItemInserted(messageArrayList.size() - 1);
            recyclerView.scrollToPosition(messageArrayList.size() - 1);
            MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message);

            Log.d("amaz_upload", "onActivityResult "+photoPath);
            Intent serviceIntent = new Intent(getContext(), UploadService.class);
            serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, photoPath);
            serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            getContext().getApplicationContext().startService(serviceIntent);

        }
        else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK)
        {
            Uri videoUri = data.getData();
            final Message message = new Message(videoUri.toString(), myPhoneNumber, contact.phoneNumber, Message.STATE_SUCCESS, Message.MY_MESSAGE_VIDEO);
            messageArrayList.add(message);
            adapter.notifyItemInserted(messageArrayList.size() - 1);
            recyclerView.scrollToPosition(messageArrayList.size() - 1);
            MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message);
        }
    }

    private void openPhotoCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, true);
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
               ex.printStackTrace();
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }



    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        /*File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);*/
        File storageDir = getContext().getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        sharedPreferences.edit().putString(C.SHARED_NEW_PHOTO_PATH, image.getAbsolutePath()).apply();
        /*Log.d("lifes", "mCurrentPhotoPath "+mCurrentPhotoPath);
        Log.d("lifes", "getExternalStorageDirectory "+Environment.getExternalStorageDirectory());
        Log.d("lifes", "getExternalStorageDirectory "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));*/
        return image;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            opponentPhone = getArguments().getString(C.EXTRA_OPPONENT_PHONE_NUMBER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        messageArrayList = new ArrayList<>();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        findContactByNumber();

        initMyPhoneNumber();
        loadOpponentBitmap();

        initRecyclerView(view);
        editText = (EditText) view.findViewById(R.id.edit_text);
        initFakeToolbar(view);
        initSendMessageView(view);

        return view;
    }

    @Override
    public void onResume() {
        cancelNotification();
        initMessageReceiver();
        initMediaReceiver();
        initDeleteMassagesReceiver();
        updateUnreadStateInDB();
        loadMessages();
        super.onResume();
    }

    private void cancelNotification()
    {
        NotificationManager notificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        long longId = Long.parseLong(opponentPhone.substring(1));
        int id = (int) longId;
        notificationManager.cancel(id);
    }

    private void initFakeToolbar(View view)
    {
        FakeToolbar fakeToolbar = (FakeToolbar) view.findViewById(R.id.toolbar_fake_layout);
        fakeToolbar.setTitle(contact.name);
        fakeToolbar.startTimer();
        fakeToolbar.setOnPhotoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoCamera();
            }
        });
        fakeToolbar.setOnVideoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openVideoCamera();
            }
        });
        fakeToolbar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

    }



    private void findContactByNumber()
    {
        if (contact == null)
        {
            //loadMyContacts();
            for (MyContacts.Contact contact: MyContacts.getContacts(getContext()))
            {
                if (contact.phoneNumber.equals(opponentPhone))
                {
                    this.contact = contact;
                    updateUnreadState();
                    break;
                }
            }
        }
    }

    private void updateUnreadState()
    {
        contact.countUnread = 0;
    }

    private void updateUnreadStateInDB()
    {
        MyDbHelper.updateAllMessagesState(new MyDbHelper(getContext()).getWritableDatabase(), contact);
    }

    /*private void loadMyContacts()
    {
        myContactsArrayList = MyContacts.getContactsList(getContext());
        *//*if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                getContext().checkSelfPermission(Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    C.READ_CONTACTS_CODE);
        }
        else
        {
            myContactsArrayList = MyContacts.getContactsList(getContext());
        }*//*
    }*/

    private void initMyPhoneNumber()
    {
        if (myPhoneNumber == null)
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, null);
        }
        /*if (myPhoneNumber == null)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    getContext().checkSelfPermission(Manifest.permission.READ_SMS)
                            != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.READ_SMS},
                        C.READ_SMS_CODE);
            }
            else
            {
                TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
                myPhoneNumber = tm.getLine1Number();
            }
        }*/

    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == C.READ_SMS_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                TelephonyManager tm = (TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
                myPhoneNumber = tm.getLine1Number();
                if (myPhoneNumber == null || myPhoneNumber.length() == 0)
                {
                    myPhoneNumber = "";
                    Toast.makeText(getContext(), "Phone number is unavailable", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(getContext(), "Sorry!!! Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else if (requestCode == C.READ_CONTACTS_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                myContactsArrayList = MyContacts.getContactsList(getContext());
            }
            else
            {
                Toast.makeText(getContext(), "Sorry!!! Permission Denied",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    private void loadMessages()
    {
        ArrayList<Message> arr = MyDbHelper.readContactMessages(new MyDbHelper(getContext()).getReadableDatabase(), contact);
        messageArrayList.clear();
        messageArrayList.addAll(arr);
        adapter.notifyDataSetChanged();
    }

    private void initSendMessageView(View view)
    {
        view.findViewById(R.id.send_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textMessage = editText.getText().toString();
                editText.setText("");
                if (textMessage.length() > 0)
                {
                    final Message message = new Message(textMessage, myPhoneNumber, contact.phoneNumber, Message.STATE_ADDED, Message.MY_MESSAGE_TEXT);
                    messageArrayList.add(message);
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                    MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message);

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
                        }
                    }, SEND_MESSAGE_DELAY);

                }
            }
        });
    }

    @Override
    public void onReSendMessage(Message message) {
        message.state = Message.STATE_ADDED;
        MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), message);
        adapter.notifyDataSetChanged();
        new SendMessageTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
    }

    public class SendMessageTask extends AsyncTask<Message, Void, String>
    {
        private Message message;

        public SendMessageTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Message... params) {
            message = params[0];
            String textMessage = message.getMessage();
            String result = null;
            try
            {
                result = trySendMessage(textMessage);
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
                //Toast.makeText(getContext(), "Connection error", Toast.LENGTH_SHORT).show();
                message.state = Message.STATE_ERROR;
                MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), message);
                adapter.notifyDataSetChanged();
            }
            else
            {
                if (result.equals("success"))
                {
                    message.state = Message.STATE_SUCCESS;
                    MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), message);
                    adapter.notifyDataSetChanged();
                }
                else if (result.equals("error"))
                {
                    message.state = Message.STATE_ERROR;
                    MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), message);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String trySendMessage(String message) throws IOException, JSONException
    {
        StringBuilder sbParams = new StringBuilder();
        sbParams.append("message=").append(URLEncoder.encode(message, "UTF-8")).append("&").append("destination=").append(URLEncoder.encode(contact.phoneNumber, "UTF-8"));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
        URL url = new URL(C.BASE_URL + "api/v1/message/sendMessage/");
        String header = "Bearer "+accessToken;

        String response = MyConnection.post(url, sbParams.toString(), header);

        String result = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(getContext()))
                result = trySendMessage(message);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            result = jsonResponse.getString("result");
        }

        return result;
    }

    private void initMediaReceiver()
    {
        mMediaReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Message message = null;
                int position = 0;
                int idToUpdate = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);
                for (int i = 0; i < messageArrayList.size(); i++)
                {
                    int messageId = messageArrayList.get(i).getMessageId();
                    if (messageId == idToUpdate)
                    {
                        message = messageArrayList.get(i);
                        position = i;
                    }
                }

                String status = intent.getStringExtra(C.EXTRA_MEDIA_STATE);
                if (status.equals("s"))
                {
                    message.state = Message.STATE_SUCCESS;
                    MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), message);
                    message.imageTotalProgress = 0;
                    message.imageProgress = 0;
                    adapter.notifyDataSetChanged();
                }
                else if (status.equals("p"))
                {
                    long progress = intent.getLongExtra(C.EXTRA_MEDIA_PROGRESS_CURRENT, 0);
                    long totalProgress = intent.getLongExtra(C.EXTRA_MEDIA_PROGRESS_TOTAL, 0);
                    message.imageProgress = progress;
                    message.imageTotalProgress = totalProgress;
                    adapter.notifyDataSetChanged();
                }
                else if (status.equals("e"))
                {
                    message.state = Message.STATE_ERROR;
                    MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), message);
                    adapter.notifyDataSetChanged();
                }


            }
        };

        // Registering BroadcastReceiver
        registerMediaReceiver();
    }

    private void registerMediaReceiver(){
        if(!isMediaReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMediaReceiver,
                    new IntentFilter(QuickstartPreferences.RECEIVE_MEDIA));
            isMediaReceiverRegistered = true;
        }
    }

    private void initMessageReceiver()
    {
        mMessageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d("amaz_upload", "onReceive "+intent.getAction());
                String phone = intent.getStringExtra(C.EXTRA_OPPONENT_PHONE_NUMBER);
                if (phone.equals(contact.phoneNumber))
                {
                    cancelNotification();
                    MyDbHelper.updateAllMessagesState(new MyDbHelper(context).getWritableDatabase(), contact);
                    int messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);

                    Message message = MyDbHelper.readMessage(new MyDbHelper(context).getReadableDatabase(), messageId);
                    messageArrayList.add(message);
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                }
            }
        };

        // Registering BroadcastReceiver
        registerMessageReceiver();
    }

    private void registerMessageReceiver(){
        if(!isMessageReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mMessageReceiver,
                    new IntentFilter(QuickstartPreferences.RECEIVE_MESSAGE));
            isMessageReceiverRegistered = true;
        }
    }

    private void initDeleteMassagesReceiver()
    {
        mDeleteMessagesReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.d("recr", "onReceive");
                int idToDelete = intent.getIntExtra(C.ID_TO_DELETE, 0);
                if (idToDelete == 0)
                {
                    ArrayList<Message> arrayList = MyDbHelper.readContactMessages(new MyDbHelper(context).getReadableDatabase(), contact);
                    messageArrayList.clear();
                    messageArrayList.addAll(arrayList);
                    adapter.notifyDataSetChanged();
                }
                else {
                    for (int i = 0; i < messageArrayList.size(); i++)
                    {
                        int messageId = messageArrayList.get(i).getMessageId();
                        if (messageId == idToDelete)
                        {
                            if (adapter != null)
                            {
                                messageArrayList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                }

            }
        };

        // Registering BroadcastReceiver
        registerDeleteMassagesReceiver();
    }

    private void registerDeleteMassagesReceiver(){
        if(!isDeleteMessagesReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDeleteMessagesReceiver,
                    new IntentFilter(QuickstartPreferences.DELETE_MESSAGES));
            isDeleteMessagesReceiverRegistered = true;
        }
    }

    private void loadOpponentBitmap()
    {
        if (contact.photoURI != null && contactBitmap == null)
        {
            InputStream image_stream = null;
            try {
                image_stream = getContext().getContentResolver().openInputStream(contact.photoURI);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                contactBitmap = BitmapFactory.decodeStream(image_stream, null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                if (image_stream != null) try {
                    image_stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initRecyclerView(View view)
    {
        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        adapter = new MyChatRecyclerViewAdapter(getContext(), messageArrayList, contact, contactBitmap, fragmentChatInteractionListener, this);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        layoutManager.setStackFromEnd(true);
    }

    public interface OnFragmentChatInteractionListener
    {
        void onCreateSuccessMessageDialog(OnMessageDialogListener listener);

        void onCreateErrorMessageDialog(OnMessageDialogListener listener);

        void onCreateTimeDialog(OnMessageDialogListener listener);
    }


}
