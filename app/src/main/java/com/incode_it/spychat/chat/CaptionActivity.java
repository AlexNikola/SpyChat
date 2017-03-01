package com.incode_it.spychat.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.BaseActivity;
import com.incode_it.spychat.C;
import com.incode_it.spychat.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import pl.droidsonroids.gif.GifImageView;

public class CaptionActivity extends AppCompatActivity {

    public static final String ACTION = "ACTION";
    public static final int INVALID_ACTION = 0;
    public static final int ACTION_OPEN_PHOTO_CAMERA = 1;
    public static final int ACTION_OPEN_VIDEO_CAMERA = 2;
    public static final int ACTION_PICK_IMAGE = 3;
    public static final int ACTION_PICK_VIDEO = 4;

    public static final String EXTRA_PATH = "EXTRA_PATH";
    public static final String EXTRA_CAPTION = "EXTRA_CAPTION";

    public static int action;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caption);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add caption");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }





    public static class CaptionFragment extends Fragment {
        static final int REQUEST_IMAGE_CAPTURE = 11;
        static final int REQUEST_VIDEO_CAPTURE = 12;
        static final int REQUEST_PHOTO_PICK = 13;
        static final int REQUEST_VIDEO_PICK = 14;
        private static final String TAG = "CaptionFragment";

        private SharedPreferences sharedPreferences;

        private String path = "";
        private String caption = "";

        private GifImageView imageView;
        private TextView textView;
        private EditText editText;

        public CaptionFragment() {
            // Required empty public constructor
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            action = getActivity().getIntent().getIntExtra(ACTION, INVALID_ACTION);

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
                    getActivity().finish();
                    return;
                }
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == Activity.RESULT_CANCELED) {
                getActivity().finish();
                return;
            }
            if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
                path = sharedPreferences.getString(C.SHARED_NEW_PHOTO_PATH, "error");
                setupImage();
            }
            else if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == Activity.RESULT_OK) {
                path = sharedPreferences.getString(C.SHARED_NEW_VIDEO_PATH, "error");
                setupThumbnail();
            }
            else if (requestCode == REQUEST_PHOTO_PICK && resultCode == Activity.RESULT_OK) {
                String dataPath = data.getData().toString();
                path = getRealPath(dataPath);
                setupImage();
            }
            else if (requestCode == REQUEST_VIDEO_PICK && resultCode == Activity.RESULT_OK) {
                String dataPath = data.getData().toString();
                path = getRealPath(dataPath);
                setupThumbnail();
            }
        }

        private void setupThumbnail() {
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path,
                    MediaStore.Images.Thumbnails.MINI_KIND);
            imageView.setImageBitmap(bitmap);
        }

        private void setupImage() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "setupImage: " + path);
                    if (path.endsWith(".gif")) {
                        imageView.setImageURI(Uri.parse("file://" + path));
                    } else {
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);
                        int width = size.x/2;
                        int height = size.y/2;
                        Picasso.with(getContext()).load("file://" + path).resize(width, height).centerInside().onlyScaleDown().into(imageView);
                    }
                }
            }, 1);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_caption, container, false);
            imageView = (GifImageView) view.findViewById(R.id.imageView);
            textView = (TextView) view.findViewById(R.id.textView);
            editText = (EditText) view.findViewById(R.id.editText);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    textView.setText(s);
                }
            });

            if (!path.equals("")) {
                if (action == ACTION_OPEN_PHOTO_CAMERA || action == ACTION_PICK_IMAGE) {
                    setupImage();
                } else if (action == ACTION_OPEN_VIDEO_CAMERA || action == ACTION_PICK_VIDEO) {
                    setupThumbnail();
                }
            }
            return view;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_fragment_captions, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_done:
                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_CAPTION, textView.getText().toString());
                    intent.putExtra(EXTRA_PATH, path);
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                    return true;
                case android.R.id.home:
                    getActivity().finish();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
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
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_PHOTO_PICK);
        }

        private void openVideoPicker() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("video/*");
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
        }
    }
}
