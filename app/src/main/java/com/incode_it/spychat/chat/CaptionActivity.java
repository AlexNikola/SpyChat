package com.incode_it.spychat.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.incode_it.spychat.C;
import com.incode_it.spychat.R;
import com.incode_it.spychat.effects.TextEffectsFragment;
import com.incode_it.spychat.effects.TextStyle;
import com.incode_it.spychat.effects.VisualsFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CaptionActivity extends AppCompatActivity {

    public static final String ACTION = "ACTION";
    public static final int INVALID_ACTION = 0;
    public static final int ACTION_OPEN_PHOTO_CAMERA = 1;
    public static final int ACTION_OPEN_VIDEO_CAMERA = 2;
    public static final int ACTION_PICK_IMAGE = 3;
    public static final int ACTION_PICK_VIDEO = 4;

    public static int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);
    }





    public static class CaptionFragment extends Fragment {
        static final int REQUEST_IMAGE_CAPTURE = 11;
        static final int REQUEST_VIDEO_CAPTURE = 12;
        static final int REQUEST_PHOTO_PICK = 13;
        static final int REQUEST_VIDEO_PICK = 14;

        private Activity activity;
        private Unbinder unbinder;
        private SharedPreferences sharedPreferences;

        public CaptionFragment() {
            // Required empty public constructor
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.activity = getActivity();
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            action = activity.getIntent().getIntExtra(ACTION, INVALID_ACTION);

            switch (action) {
                case ACTION_OPEN_PHOTO_CAMERA: {
                    openPhotoCamera();
                    break;
                }
                case ACTION_OPEN_VIDEO_CAMERA: {
                    openVideoCamera();
                    break;
                }
                case ACTION_PICK_IMAGE: {
                    openImagePicker();
                    break;
                }
                case ACTION_PICK_VIDEO: {
                    openVideoPicker();
                    break;
                }
                default: {
                    activity.finish();
                    return;
                }
            }



        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                String photoPath = sharedPreferences.getString(C.SHARED_NEW_PHOTO_PATH, "error");
                Toast.makeText(activity, photoPath, Toast.LENGTH_SHORT).show();
            }
            else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
                String videoPath = sharedPreferences.getString(C.SHARED_NEW_VIDEO_PATH, "error");
                Toast.makeText(activity, videoPath, Toast.LENGTH_SHORT).show();
            }
            else if (requestCode == REQUEST_PHOTO_PICK && resultCode == Activity.RESULT_OK) {
                String path = data.getData().toString();
                String realPath = getRealPath(path);
                Toast.makeText(activity, realPath, Toast.LENGTH_SHORT).show();
            }
            else if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK) {
                String path = data.getData().toString();
                String realPath = getRealPath(path);
                Toast.makeText(activity, realPath, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_blank, container, false);
            unbinder = ButterKnife.bind(this, view);

            return view;
        }

        private void openPhotoCamera() {
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

        private void openVideoCamera() {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
                File videoFile = null;
                try {
                    videoFile = createVideoFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                if (videoFile != null) {
                    takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(videoFile));
                    startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
                }
            }
        }

        private void openImagePicker() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image");
            startActivityForResult(intent, REQUEST_PHOTO_PICK);

        }

        private void openVideoPicker() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video");
            startActivityForResult(intent, REQUEST_VIDEO_PICK);
        }

        private File createImageFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getContext().getExternalFilesDir(null);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            sharedPreferences.edit().putString(C.SHARED_NEW_PHOTO_PATH, image.getAbsolutePath()).apply();
            return image;
        }

        private File createVideoFile() throws IOException {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "MP4_" + timeStamp + "_";
            File storageDir = getContext().getExternalFilesDir(null);
            File video = File.createTempFile(imageFileName, ".mp4", storageDir);
            sharedPreferences.edit().putString(C.SHARED_NEW_VIDEO_PATH, video.getAbsolutePath()).apply();
            return video;
        }

        private String getRealPath(String path) {
            String yourRealPath = null;

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContext().getContentResolver().query(Uri.parse(path), filePathColumn, null, null, null);
            if (cursor != null) {
                if(cursor.moveToFirst()){
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    yourRealPath = cursor.getString(columnIndex);
                }
                cursor.close();
            }
            return yourRealPath;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }
    }
}
