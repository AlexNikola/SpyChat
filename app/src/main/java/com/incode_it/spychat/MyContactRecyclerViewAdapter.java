package com.incode_it.spychat;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;

import com.incode_it.spychat.dummy.DummyContent.DummyItem;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


public class MyContactRecyclerViewAdapter extends RecyclerView.Adapter<MyContactRecyclerViewAdapter.ViewHolder> {

    private final List<MyContacts.Contact> mContacts;
    private final OnFragmentInteractionListener mListener;
    private Context context;
    private Bitmap noPhotoBitmap;
    private LruCache<String, Bitmap> mMemoryCache;

    public MyContactRecyclerViewAdapter(List<MyContacts.Contact> contacts, OnFragmentInteractionListener listener) {
        mContacts = contacts;
        mListener = listener;
        context = (Context) listener;
        noPhotoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_black_24dp);
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
        holder.mNameView.setText(mContacts.get(position).name);
        holder.mNumberView.setText(mContacts.get(position).phoneNumber);

        Uri uri = mContacts.get(position).photoURI;
        if (uri != null)
        {
            loadBitmap(uri, holder.mImage);
            //holder.mImage.setImageURI(mContacts.get(position).photoURI);
        }
        else
        {
            holder.mImage.setImageBitmap(noPhotoBitmap);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onFragmentInteraction();
                }
            }
        });
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

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNameView = (TextView) view.findViewById(R.id.name);
            mNumberView = (TextView) view.findViewById(R.id.number);
            mImage = (ImageView) view.findViewById(R.id.image);

            mNameView.setTypeface(MainActivity.typeface, Typeface.BOLD);

            mNumberView.setTypeface(MainActivity.typeface);
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
                        new AsyncDrawable(context.getResources(), null, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(uri);
            }
        }
    }

    public static boolean cancelPotentialWork(Uri uri, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Uri bitmapUri = bitmapWorkerTask.imageUri;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapUri == null || bitmapUri.equals(uri)) {
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
            imageUri = resIds[0];
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
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
}
