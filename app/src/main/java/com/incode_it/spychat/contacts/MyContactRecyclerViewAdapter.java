package com.incode_it.spychat.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.incode_it.spychat.chat.ActivityChat;
import com.incode_it.spychat.C;
import com.incode_it.spychat.ContactsComparator;
import com.incode_it.spychat.MyConnection;
import com.incode_it.spychat.MyContacts;
import com.incode_it.spychat.R;
import com.incode_it.spychat.data_base.MyDbHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class MyContactRecyclerViewAdapter extends RecyclerView.Adapter<MyContactRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "myhttp";
    private ArrayList<MyContacts.Contact> mContacts;
    private Context context;
    private Bitmap noPhotoBitmap;
    private LruCache<String, Bitmap> mMemoryCache;
    private Typeface typeface;

    public MyContactRecyclerViewAdapter(Context context, ArrayList<MyContacts.Contact> mContacts) {
        this.context = context;
        this.mContacts = mContacts;
        typeface = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Light.ttf");

        noPhotoBitmap = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.profile);
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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mContact = mContacts.get(position);

        if (holder.mContact.isRegistered)
        {
            holder.verifyView.setVisibility(View.VISIBLE);
        }
        else holder.verifyView.setVisibility(View.INVISIBLE);

        /*String name = mContacts.get(position).name;
        final SpannableStringBuilder sb = new SpannableStringBuilder(name);
        int start, end;
        start = name.toLowerCase().indexOf(holder.mContact.subString.toLowerCase());
        end = start + holder.mContact.subString.length();
        if (start != -1)
        {
            final ForegroundColorSpan fcs = new ForegroundColorSpan(context.getResources().getColor(R.color.colorPrimary));
            sb.setSpan(fcs, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            //holder.mNameView.setTextColor(Color.rgb(158, 158, 158));
            holder.mNameView.setText(sb);
        }
        else*/

        holder.mNameView.setText(mContacts.get(position).name);
        holder.mNumberView.setText(mContacts.get(position).phoneNumber);

        Uri uri = mContacts.get(position).photoURI;
        if (uri != null)
        {
            loadBitmap(uri, holder.mImage);
        }
        else
        {
            holder.mImage.setImageBitmap(noPhotoBitmap);
        }
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNameView;
        public final TextView mNumberView;
        public final ImageView mImage;
        public MyContacts.Contact mContact;
        public View verifyView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mNumberView = (TextView) view.findViewById(R.id.number);
            mImage = (ImageView) view.findViewById(R.id.image);
            verifyView = view.findViewById(R.id.verify);
            mNameView.setTypeface(typeface, Typeface.BOLD);
            mNumberView.setTypeface(typeface);

            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("mylist", "position "+getAdapterPosition());
                    if (mContact.isRegistered)
                    {
                        Intent intent = new Intent(context, ActivityChat.class);
                        intent.putExtra(C.PHONE_NUMBER, mContact.phoneNumber);
                        ((Activity)context).startActivityForResult(intent, 0);
                    }
                    else
                    {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setData(Uri.parse("sms:"));
                        smsIntent.setType("vnd.android-dir/mms-sms");
                        smsIntent.putExtra("sms_body", "my sms");
                        smsIntent.putExtra("address", mContact.phoneNumber);
                       context.startActivity(smsIntent);
                    }
                }
            });
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNumberView.getText() + "'";
        }
    }

    public void loadBitmap(Uri uri, ImageView imageView) {
        if (cancelPotentialWork(uri, imageView))
        {
            final Bitmap bitmap = getBitmapFromMemCache(uri.toString());
            if (bitmap != null)
            {
                imageView.setImageBitmap(bitmap);
            }
            else
            {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(context.getResources(), noPhotoBitmap, task);
                imageView.setImageDrawable(asyncDrawable);
                //task.execute(uri);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
            }
        }
    }

    public static boolean cancelPotentialWork(Uri uri, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Uri bitmapUri = bitmapWorkerTask.imageUri;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapUri == null || !bitmapUri.equals(uri)) {
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

    class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private Uri imageUri;

        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Uri... resIds) {
            //Log.d(TAG, "doInBackground");

            imageUri = resIds[0];
            Bitmap bitmap = null;
            try {
                InputStream image_stream = context.getContentResolver().openInputStream(imageUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(image_stream, null, options);
                if (image_stream != null) image_stream.close();

                image_stream = context.getContentResolver().openInputStream(imageUri);
                options.inJustDecodeBounds = false;
                options.inSampleSize = calculateInSampleSize(options, 100, 100);
                bitmap= BitmapFactory.decodeStream(image_stream, null, options);
                if (image_stream != null) image_stream.close();
                if (bitmap != null) addBitmapToMemoryCache(imageUri.toString(), bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        return inSampleSize;
    }
}
