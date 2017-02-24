package com.incode_it.spychat.chat;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.Message;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.OrientationUtils;
import com.incode_it.spychat.QuickstartPreferences;
import com.incode_it.spychat.R;
import com.incode_it.spychat.alarm.AlarmReceiverGlobal;
import com.incode_it.spychat.amazon.UploadService;
import com.incode_it.spychat.data_base.MyDbHelper;
import com.incode_it.spychat.effects.VisualButton;
import com.incode_it.spychat.effects.EffectsSelectorActivity;
import com.incode_it.spychat.effects.VisualsFragment;
import com.incode_it.spychat.effects.VisualsView;
import com.incode_it.spychat.interfaces.OnMessageDialogListener;
import com.incode_it.spychat.interfaces.OnPickMediaListener;
import com.incode_it.spychat.effects.TextEffectsFragment;
import com.incode_it.spychat.effects.TextStyle;
import com.incode_it.spychat.utils.Cypher;
import com.incode_it.spychat.utils.FontHelper;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.emoji.Emoji;
import com.vanniktech.emoji.listeners.OnEmojiBackspaceClickListener;
import com.vanniktech.emoji.listeners.OnEmojiClickedListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardCloseListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class FragmentChat extends Fragment implements MyChatRecyclerViewAdapter.Callback,
        TimePickerDialog.OnTimeSetListener, OnPickMediaListener, RecordAudioDialog.Callback, FakeToolbar.Callback {


    private static final int SEND_MESSAGE_DELAY = 500;
    private static final String TAG = "chatm";
    private static final String DOWNLOAD_TAG = "amaz_download";
    public static final String TAG_FRAGMENT = "FragmentChat";
    public static final int REQUEST_TEXT_SIZE = 99;
    public static final int REQUEST_TEXT_FONT = 100;
    public static final int REQUEST_EFFECTS = 101;

    private static final String SAVE_STATE_TEXT_STYLE = "SAVE_STATE_TEXT_STYLE";

    private String opponentPhone;
    private RecyclerView recyclerView;
    private EmojiEditText editText;
    private MyChatRecyclerViewAdapter adapter;
    private Bitmap contactBitmap;
    private String myPhoneNumber;

    private ArrayList<Message> messageArrayList;

    private BroadcastReceiver mUploadMediaReceiver;
    private boolean isUploadMediaReceiverRegistered;
    private BroadcastReceiver mDownloadMediaReceiver;
    private boolean isDownloadMediaReceiverRegistered;
    private BroadcastReceiver mMessageReceiver;
    private boolean isMessageReceiverRegistered;
    private BroadcastReceiver mDeleteMessagesReceiver;
    private boolean isDeleteMessagesReceiverRegistered;

    private OnFragmentChatInteractionListener fragmentChatInteractionListener;
    private MyContacts.Contact contact;
    private SharedPreferences sharedPreferences;
    private FakeToolbar fakeToolbar;

    private ViewGroup rootView;
    private ImageView emojiButton;
    private EmojiPopup emojiPopup;

    private AudioService mService;

    private TextStyle textStyle;

    private VisualButton visualButton;
    private VisualsView visualsView;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder) service;
            mService = binder.getService();
            adapter.mService = mService;
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }
    };

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            opponentPhone = getArguments().getString(C.EXTRA_OPPONENT_PHONE_NUMBER);
        }

        if (savedInstanceState == null) {
            textStyle = new TextStyle(getContext());
        } else {

            textStyle = (TextStyle) savedInstanceState.getSerializable(SAVE_STATE_TEXT_STYLE);
            Log.e(TAG, "onCreate: " + textStyle);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVE_STATE_TEXT_STYLE, textStyle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        messageArrayList = new ArrayList<>();

        Log.d(TAG, "onCreateView: " + textStyle);

        rootView = (ViewGroup) view.findViewById(R.id.root_view);
        visualsView = (VisualsView) rootView.findViewById(R.id.effectsView);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        findContactByNumber();

        if (contact == null)
        {
            getActivity().finish();
        }
        else
        {
            initMyPhoneNumber();
            loadOpponentBitmap();

            initRecyclerView(view);
            editText = (EmojiEditText) view.findViewById(R.id.edit_text);
            /*editText.setTextColor(selectedColor);
            editText.setTextSize(selectedSize);
            FontHelper.setCustomFont(getActivity(), editText, selectedFont);
            if (animation != null) {
                animation.setTarget(editText);
                if (isAnimated) {
                    animation.start();
                }
            }*/
            initFakeToolbar(view);
            initSendMessageView(view);
            initAddEffectView(view);

            emojiButton = (ImageView) view.findViewById(R.id.emojiBtn);
            emojiButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    emojiPopup.toggle();
                }
            });
            setUpEmojiPopup();

            textStyle.setStyle(editText);
        }



        return view;
    }

    public void lanchEffect(int effect) {
        visualsView.start(effect);
    }

    private void resetPickerColor() {
        //selectedColor = ContextCompat.getColor(getActivity(), R.color.black);
        //editText.setTextColor(selectedColor);
    }


    @Override
    public void onResume() {
        cancelNotification();
        initMessageReceiver();
        initUploadMediaReceiver();
        initDownloadMediaReceiver();
        initDeleteMassagesReceiver();
        updateViewedStateInDB();
        loadMessages();
        fakeToolbar.startTimer();

        Intent intent = new Intent(getContext(), AudioService.class);
        getContext().startService(intent);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("qwerty", "onActivityResult");
        /*if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            String photoPath = sharedPreferences.getString(C.SHARED_NEW_PHOTO_PATH, "error");
            uploadImage(photoPath);
        } else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
            String videoPath = sharedPreferences.getString(C.SHARED_NEW_VIDEO_PATH, "error");
            uploadVideo(videoPath);
        } else if (requestCode == REQUEST_PHOTO_PICK && resultCode == Activity.RESULT_OK) {
            String path = data.getData().toString();
            Log.d("qwerty", data.getType()+" - "+path);
            String realPath = getRealPath(path);
            uploadImage(realPath);
        }
        else if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK)
        {
            String path = data.getData().toString();
            Log.d("qwerty", data.getType()+" - "+path);
            String realPath = getRealPath(path);
            uploadVideo(realPath);
        } else*/ if (requestCode == REQUEST_EFFECTS && resultCode == Activity.RESULT_OK) {
            TextStyle textStyle = (TextStyle) data.getSerializableExtra(TextEffectsFragment.EXTRA_TEXT_STYLE);
            visualButton.setEffect(data.getIntExtra(VisualsFragment.EXTRA_EFFECT_ID, 0));
            setTextStyle(textStyle);
        }
    }

    private void setTextStyle(TextStyle textStyle) {
        this.textStyle.refresh(getContext(), editText);
        this.textStyle = textStyle;
        editText.setTextColor(textStyle.getColor());
        editText.setTextSize(textStyle.getSize());
        FontHelper.setCustomFont(getActivity(), editText, textStyle.getFont());
        textStyle.animate(editText, textStyle.getAnimationType());
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mUploadMediaReceiver);
        isUploadMediaReceiverRegistered = false;

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDownloadMediaReceiver);
        isDownloadMediaReceiverRegistered = false;

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        isMessageReceiverRegistered = false;

        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mDeleteMessagesReceiver);
        isDeleteMessagesReceiverRegistered = false;

        getContext().unbindService(mConnection);
        getContext().stopService(new Intent(getContext(), AudioService.class));
        mService = null;
        adapter.mService = null;
        super.onPause();
    }



    private void uploadImage(String photoPath)
    {
        final Message message = new Message(photoPath, myPhoneNumber, contact.phoneNumber, Message.STATE_ADDED, Message.MY_MESSAGE_IMAGE, myPhoneNumber);
        message.isViewed = 1;
        message.setEffect(visualButton.getEffect());
        visualButton.setEffect(VisualsView.EFFECT_NONE);
        setTextStyle(textStyle);
        messageArrayList.add(message);
        adapter.notifyItemInserted(messageArrayList.size() - 1);
        recyclerView.scrollToPosition(messageArrayList.size() - 1);
        MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message, getContext());

        Intent serviceIntent = new Intent(getContext(), UploadService.class);
        serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, photoPath);
        serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
        serviceIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, opponentPhone);
        serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_IMAGE);
        getContext().getApplicationContext().startService(serviceIntent);
    }

    private void uploadVideo(String videoPath)
    {
        final Message message = new Message(videoPath, myPhoneNumber, contact.phoneNumber, Message.STATE_ADDED, Message.MY_MESSAGE_VIDEO, myPhoneNumber);
        message.isViewed = 1;
        message.setEffect(visualButton.getEffect());
        visualButton.setEffect(VisualsView.EFFECT_NONE);
        setTextStyle(textStyle);
        messageArrayList.add(message);
        adapter.notifyItemInserted(messageArrayList.size() - 1);
        recyclerView.scrollToPosition(messageArrayList.size() - 1);
        MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message, getContext());

        Intent serviceIntent = new Intent(getContext(), UploadService.class);
        serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, videoPath);
        serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
        serviceIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, opponentPhone);
        serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_VIDEO);
        getContext().getApplicationContext().startService(serviceIntent);
    }

    private void uploadAudio(String audioPath)
    {
        MediaPlayer mPlayer = new MediaPlayer();
        int duration = 0;
        try {
            mPlayer.setDataSource(audioPath);
            mPlayer.prepare();
            duration = mPlayer.getDuration();
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }

        final Message message = new Message(audioPath, myPhoneNumber, contact.phoneNumber, Message.STATE_ADDED, Message.MY_MESSAGE_AUDIO, myPhoneNumber);
        message.isViewed = 1;
        message.setEffect(visualButton.getEffect());
        visualButton.setEffect(VisualsView.EFFECT_NONE);
        setTextStyle(textStyle);
        message.audioDuration = duration;
        messageArrayList.add(message);
        adapter.notifyItemInserted(messageArrayList.size() - 1);
        recyclerView.scrollToPosition(messageArrayList.size() - 1);
        MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message, getContext());

        Intent serviceIntent = new Intent(getContext(), UploadService.class);
        serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, audioPath);
        serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
        serviceIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, opponentPhone);
        serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_AUDIO);
        getContext().getApplicationContext().startService(serviceIntent);
    }

    private void takeVideo() {
        Toast.makeText(getContext(), "Open video", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext(), CaptionActivity.class);
        startActivityForResult(intent, CaptionActivity.ACTION_OPEN_VIDEO_CAMERA);
    }

    private void takePhoto() {
        Intent intent = new Intent(getContext(), CaptionActivity.class);
        startActivityForResult(intent, CaptionActivity.ACTION_OPEN_PHOTO_CAMERA);
    }

    @Override
    public void onPickImage() {
        Intent intent = new Intent(getContext(), CaptionActivity.class);
        startActivityForResult(intent, CaptionActivity.ACTION_PICK_IMAGE);
    }

    @Override
    public void onPickVideo() {
        Intent intent = new Intent(getContext(), CaptionActivity.class);
        startActivityForResult(intent, CaptionActivity.ACTION_PICK_VIDEO);
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
        fakeToolbar = (FakeToolbar) view.findViewById(R.id.toolbar_fake_layout);
        fakeToolbar.setCallback(this);
        fakeToolbar.setTitle(contact.name);
        fakeToolbar.setOnAudioClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecordAudioDialog();
            }
        });
        fakeToolbar.setOnPhotoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });
        fakeToolbar.setOnVideoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideo();
            }
        });
        fakeToolbar.setOnBackClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getActivity().getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                getActivity().finish();
            }
        });
        fakeToolbar.setOnGalleryClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = PickMediaDialogFragment.newInstance();
                newFragment.show(getChildFragmentManager(), PickMediaDialogFragment.TAG);
            }
        });
        fakeToolbar.setOnTimerClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimerDialog();
            }
        });
    }

    private void showRecordAudioDialog()
    {
        RecordAudioDialog recordAudioDialog = RecordAudioDialog.newInstance();
        recordAudioDialog.show(getChildFragmentManager(), RecordAudioDialog.FRAGMENT_TAG);
    }

    @Override
    public void onRecordAudioCompleted(String audioPath) {
        uploadAudio(audioPath);
    }





    /*@Override
    public void onOpenTextEffectsSelector() {
        Intent intent = new Intent(getContext(), EffectsSelectorActivity.class);
        intent.putExtra(EffectsSelectorActivity.EXTRA_EFFECT_TYPE, EffectsSelectorActivity.TEXT_EFFECTS);
        startActivityForResult(intent, REQUEST_EFFECTS);
    }

    @Override
    public void onOpenVisualsSelector() {
        Intent intent = new Intent(getContext(), EffectsSelectorActivity.class);
        intent.putExtra(EffectsSelectorActivity.EXTRA_EFFECT_TYPE, EffectsSelectorActivity.VISUALS);
        startActivityForResult(intent, REQUEST_EFFECTS);
    }*/

    @Override
    public void onOpenEffectsSelector() {
        Intent intent = new Intent(getContext(), EffectsSelectorActivity.class);
        intent.putExtra(VisualsFragment.EXTRA_EFFECT_ID, visualButton.getEffect());
        intent.putExtra(TextEffectsFragment.EXTRA_TEXT_STYLE, textStyle);
        startActivityForResult(intent, REQUEST_EFFECTS);

    }

    @Override
    public void onStartCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + opponentPhone));
        startActivity(callIntent);
    }

    public static class PickMediaDialogFragment extends DialogFragment {

        public static final String TAG = "PickMediaDialogFragment";
        private OnPickMediaListener listener;

        public static PickMediaDialogFragment newInstance()
        {
            PickMediaDialogFragment newFragment = new PickMediaDialogFragment();
            return newFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            listener = (OnPickMediaListener) getParentFragment();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            OrientationUtils.unlockOrientation(getActivity());
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            OrientationUtils.lockOrientation(getActivity());
            int arr = R.array.pick_media_dialog;
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(arr, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    listener.onPickImage();
                                    break;

                                case 1:
                                    listener.onPickVideo();
                                    break;
                            }
                            dismiss();
                        }
                    });

            return builder.create();
        }
    }



    private void findContactByNumber()
    {
        if (contact == null)
        {
            for (MyContacts.Contact contact: MyContacts.getContacts(getContext()))
            {
                if (contact.phoneNumber.equals(opponentPhone))
                {
                    this.contact = contact;
                    updateViewedState();
                    break;
                }
            }
        }

    }

    private void updateViewedState()
    {
        contact.countUnread = 0;
    }

    private void updateViewedStateInDB()
    {
        MyDbHelper.updateAllMessagesViewState(new MyDbHelper(getContext()).getWritableDatabase(), contact, getContext());
    }

    private void initMyPhoneNumber()
    {
        if (myPhoneNumber == null)
        {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            myPhoneNumber = sharedPreferences.getString(C.SHARED_MY_PHONE_NUMBER, null);
        }

    }

    private void loadMessages()
    {
        ArrayList<Message> arr = MyDbHelper.readContactMessages(new MyDbHelper(getContext()).getReadableDatabase(), contact, getContext());
        messageArrayList.clear();
        messageArrayList.addAll(arr);
        adapter.notifyDataSetChanged();
    }

    private void initSendMessageView(View view)
    {
        view.findViewById(R.id.send_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //fakeToolbar.hidePalettePopup();
                fakeToolbar.hidePopup();
                String textMessage = editText.getText().toString();
                editText.setText("");
                if (textMessage.length() > 0)
                {
                    final Message message = new Message(Cypher.encrypt(textMessage), myPhoneNumber, contact.phoneNumber, Message.STATE_ADDED, Message.MY_MESSAGE_TEXT, myPhoneNumber);
                    message.isViewed = 1;
                    message.setColor(textStyle.getColor());
                    message.setTextSize(textStyle.getSize());
                    message.setAnimationType(textStyle.getAnimationType());
                    message.setFont(textStyle.getFont());
                    message.setEffect(visualButton.getEffect());
                    visualButton.setEffect(VisualsView.EFFECT_NONE);

                    setTextStyle(textStyle);
                    messageArrayList.add(message);
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                    MyDbHelper.insertMessage(new MyDbHelper(getContext()).getWritableDatabase(), message, getContext());
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            new SendMessageTask(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
                        }
                    }, SEND_MESSAGE_DELAY);


                }
            }
        });
    }

    private void initAddEffectView(View view) {
        visualButton = (VisualButton) view.findViewById(R.id.effect);
        visualButton.findViewById(R.id.effect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (visualButton.getEffect() != VisualsView.EFFECT_NONE) {
                    Intent intent = new Intent(getContext(), EffectsSelectorActivity.class);
                    intent.putExtra(EffectsSelectorActivity.EXTRA_EFFECT_TYPE, EffectsSelectorActivity.VISUALS);
                    intent.putExtra(VisualsFragment.EXTRA_EFFECT_ID, visualButton.getEffect());
                    intent.putExtra(TextEffectsFragment.EXTRA_TEXT_STYLE, textStyle);
                    startActivityForResult(intent, REQUEST_EFFECTS);
                }
            }
        });
    }

    /*private void initStyleFlags() {
        selectedSize = 16;
        isAnimated = false;
        if (animation != null && animation.isRunning()) {
            animation.cancel();
        }
        editText.setAlpha(1);
        resetPickerColor();
        selectedFont = null;
        editText.setTypeface(Typeface.DEFAULT);
    }*/

    @Override
    public void onReSendMessage(Message message) {
        if (message.messageType == Message.MY_MESSAGE_IMAGE)
        {
            Intent serviceIntent = new Intent(getContext(), UploadService.class);
            serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, message.getMessage());
            serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            serviceIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, opponentPhone);
            serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_IMAGE);
            getContext().getApplicationContext().startService(serviceIntent);
        }
        else if (message.messageType == Message.MY_MESSAGE_VIDEO)
        {
            Intent serviceIntent = new Intent(getContext(), UploadService.class);
            serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, message.getMessage());
            serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            serviceIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, opponentPhone);
            serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_VIDEO);
            getContext().getApplicationContext().startService(serviceIntent);
        }
        else if (message.messageType == Message.MY_MESSAGE_AUDIO)
        {
            Intent serviceIntent = new Intent(getContext(), UploadService.class);
            serviceIntent.putExtra(C.EXTRA_MEDIA_FILE_PATH, message.getMessage());
            serviceIntent.putExtra(C.EXTRA_MESSAGE_ID, message.getMessageId());
            serviceIntent.putExtra(C.EXTRA_OPPONENT_PHONE_NUMBER, opponentPhone);
            serviceIntent.putExtra(C.EXTRA_MEDIA_TYPE, C.MEDIA_TYPE_AUDIO);
            getContext().getApplicationContext().startService(serviceIntent);
        }
        else
        {
            new SendMessageTask(getContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        }
        MyDbHelper.updateMessageState(new MyDbHelper(getContext()).getWritableDatabase(), Message.STATE_ADDED, message.getMessageId(), getContext());
        message.state = Message.STATE_ADDED;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onReplayEffect(int effect) {
        visualsView.start(effect);
    }

    public class SendMessageTask extends AsyncTask<Message, Void, String>
    {
        private Message message;
        private Context context;

        public SendMessageTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Message... params) {
            String font = "default";
            message = params[0];
            String textMessage = message.getMessage();
            JSONObject object = new JSONObject();
            try {
                object.put("type", "typeText");
                object.put("textmessage", textMessage);
                object.put("color", message.getColor());
                object.put("size", (double) message.getTextSize());
                object.put("animation", (message.getAnimationType() != 0));
                object.put("animationType", message.getAnimationType());
                object.put("receiverPhoneNumber", message.getReceiverPhoneNumber());
                object.put("effect", message.getEffect());
                if (message.getFont() != null) {
                  font = message.getFont();
                }
                object.put("font", font);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            String result = null;
            try
            {
//                result = trySendMessage(textMessage, context);
                result = trySendMessage(object.toString(), context);
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
                MyDbHelper.updateMessageState(new MyDbHelper(context).getWritableDatabase(), Message.STATE_ERROR, message.getMessageId(), getContext());
                adapter.notifyDataSetChanged();
            }
            else
            {
                if (result.equals("success"))
                {
                    message.state = Message.STATE_SUCCESS;
                    MyDbHelper.updateMessageState(new MyDbHelper(context).getWritableDatabase(), Message.STATE_SUCCESS, message.getMessageId(), getContext());
                    adapter.notifyDataSetChanged();
                }
                else if (result.equals("error"))
                {
                    message.state = Message.STATE_ERROR;
                    MyDbHelper.updateMessageState(new MyDbHelper(context).getWritableDatabase(), Message.STATE_ERROR, message.getMessageId(), getContext());
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String trySendMessage(String message, Context context) throws IOException, JSONException
    {
        StringBuilder sbParams = new StringBuilder();
        sbParams.append("message=").append(URLEncoder.encode(message, "UTF-8")).append("&").append("destination=").append(URLEncoder.encode(contact.phoneNumber, "UTF-8"));
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = sharedPreferences.getString(C.SHARED_ACCESS_TOKEN, "");
        URL url = new URL(C.BASE_URL + "api/v1/message/sendMessage/");
        String header = "Bearer "+accessToken;

        String response = MyConnection.post(url, sbParams.toString(), header);
        Log.d(TAG, "trySendMessage: " + response);

        String result = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(context))
                result = trySendMessage(message, context);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            result = jsonResponse.getString("result");
        }

        return result;
    }

    private void initUploadMediaReceiver()
    {
        mUploadMediaReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                int idToUpdate = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);
                String status = intent.getStringExtra(C.EXTRA_MEDIA_STATE);

                Message message = null;
                for (int i = 0; i < messageArrayList.size(); i++)
                {
                    int messageId = messageArrayList.get(i).getMessageId();
                    if (messageId == idToUpdate)
                    {
                        message = messageArrayList.get(i);
                        if (status.equals("COMPLETED"))
                        {
                            message.state = Message.STATE_SUCCESS;
                            adapter.notifyDataSetChanged();
                        }
                        else if (status.equals("FAILED"))
                        {
                            message.state = Message.STATE_ERROR;
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            }
        };

        registerUploadMediaReceiver();
    }

    private void registerUploadMediaReceiver(){
        if(!isUploadMediaReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mUploadMediaReceiver,
                    new IntentFilter(QuickstartPreferences.UPLOAD_MEDIA));
            isUploadMediaReceiverRegistered = true;
        }
    }

    private void initDownloadMediaReceiver()
    {
        mDownloadMediaReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                int idToUpdate = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);
                String status = intent.getStringExtra(C.EXTRA_MEDIA_STATE);
                String localPath = intent.getStringExtra(C.EXTRA_MEDIA_LOCAL_PATH);

                Message message = null;
                for (int i = 0; i < messageArrayList.size(); i++)
                {
                    int messageId = messageArrayList.get(i).getMessageId();
                    if (messageId == idToUpdate)
                    {
                        message = messageArrayList.get(i);
                        if (status.equals("COMPLETED"))
                        {
                            message.state = Message.STATE_SUCCESS;
                            message.setMessage(localPath);
                            adapter.notifyDataSetChanged();
                        }
                        else if (status.equals("FAILED"))
                        {
                            message.state = Message.STATE_ERROR;
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            }
        };
        registerDownloadMediaReceiver();
    }

    private void registerDownloadMediaReceiver(){
        if(!isDownloadMediaReceiverRegistered) {
            LocalBroadcastManager.getInstance(getContext()).registerReceiver(mDownloadMediaReceiver,
                    new IntentFilter(QuickstartPreferences.DOWNLOAD_MEDIA));
            isDownloadMediaReceiverRegistered = true;
        }
    }

    private void initMessageReceiver()
    {
        mMessageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String phone = intent.getStringExtra(C.EXTRA_OPPONENT_PHONE_NUMBER);
                if (phone.equals(contact.phoneNumber))
                {
                    cancelNotification();
                    MyDbHelper.updateAllMessagesViewState(new MyDbHelper(context).getWritableDatabase(), contact, getContext());
                    int messageId = intent.getIntExtra(C.EXTRA_MESSAGE_ID, 0);

                    Message message = MyDbHelper.readMessage(new MyDbHelper(context).getReadableDatabase(), messageId, getContext());
                    messageArrayList.add(message);
                    adapter.notifyItemInserted(messageArrayList.size() - 1);
                    recyclerView.scrollToPosition(messageArrayList.size() - 1);
                    visualsView.start(message.getEffect());
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
                int idToDelete = intent.getIntExtra(C.ID_TO_DELETE, 0);
                if (idToDelete == 0)
                {
                    ArrayList<Message> arrayList = MyDbHelper.readContactMessages(new MyDbHelper(context).getReadableDatabase(), contact, getContext());
                    messageArrayList.clear();
                    messageArrayList.addAll(arrayList);
                    adapter.notifyDataSetChanged();
                    fakeToolbar.startTimer();
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

        void onCreateErrorMessageDialog(OnMessageDialogListener listener, int state);

        void onCreateTimeDialog(OnMessageDialogListener listener);
    }

    private void startTimerDialog()
    {
        OrientationUtils.lockOrientation(getActivity());
        TimePickerDialog tpd = TimePickerDialog.newInstance(
                FragmentChat.this, 0, 0, true
        );

        tpd.vibrate(true);
        tpd.setAccentColor(getResources().getColor(R.color.colorPrimary));
        tpd.setTitle("Global timer");
        tpd.enableSeconds(true);
        tpd.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                OrientationUtils.unlockOrientation(getActivity());
            }
        });
        tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
            }
        });
        tpd.show(getActivity().getFragmentManager(), "Timepickerdialog");
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {

        long timer = (hourOfDay * 60 * 60 * 1000) + (minute * 60 * 1000) + (second * 1000);
        long removalTime = System.currentTimeMillis() + timer;
        AlarmReceiverGlobal alarmReceiverGlobal = new AlarmReceiverGlobal();
        if (timer == 0)
        {
            long id = sharedPreferences.getLong(C.GLOBAL_TIMER, 0);
            sharedPreferences.edit().putLong(C.REMOVAL_GLOBAL_TIME, 0).apply();
            sharedPreferences.edit().putLong(C.GLOBAL_TIMER, 0).apply();

            alarmReceiverGlobal.cancelAlarm(getContext(), 0);
        }
        else
        {
            sharedPreferences.edit().putLong(C.REMOVAL_GLOBAL_TIME, removalTime).apply();
            sharedPreferences.edit().putLong(C.GLOBAL_TIMER, timer).apply();
            alarmReceiverGlobal.cancelAlarm(getContext(), 0);
            alarmReceiverGlobal.setAlarm(getContext(), removalTime, timer);
        }
        fakeToolbar.startTimer();
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView).setOnEmojiBackspaceClickListener(new OnEmojiBackspaceClickListener() {
            @Override
            public void onEmojiBackspaceClicked(final View v) {
                Log.d("MainActivity", "Clicked on Backspace");
            }
        }).setOnEmojiClickedListener(new OnEmojiClickedListener() {
            @Override
            public void onEmojiClicked(final Emoji emoji) {
                Log.d("MainActivity", "Clicked on emoji");
            }
        }).setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
            @Override
            public void onEmojiPopupShown() {
                emojiButton.setImageResource(R.drawable.keyboard_black);
            }
        }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
            @Override
            public void onKeyboardOpen(final int keyBoardHeight) {
                Log.d("MainActivity", "Opened soft keyboard");
            }
        }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
            @Override
            public void onEmojiPopupDismiss() {
                emojiButton.setImageResource(R.drawable.emoji_black);
            }
        }).setOnSoftKeyboardCloseListener(new OnSoftKeyboardCloseListener() {
            @Override
            public void onKeyboardClose() {
                emojiPopup.dismiss();
            }
        }).build(editText);
    }

    public boolean backPressed()
    {
        if (emojiPopup != null && emojiPopup.isShowing()) {
            emojiPopup.dismiss();
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("fgdfgxcvcv", "onDestroy: ");
    }
}
