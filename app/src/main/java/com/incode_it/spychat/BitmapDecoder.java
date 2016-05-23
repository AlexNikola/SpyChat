package com.incode_it.spychat;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class BitmapDecoder
{
    /*public static void decodeBitmapFromUri(Uri uri, ImageView videoMessage, Context context, Bitmap noPhotoBitmap, LruCache<String, Bitmap> mMemoryCache)
    {
        if (cancelPotentialWork(uri, videoMessage))
        {
            final Bitmap bitmap = getBitmapFromMemCache(uri.toString());
            if (bitmap != null)
            {
                videoMessage.setImageBitmap(bitmap);
            }
            else
            {
                final BitmapWorkerTask task = new BitmapWorkerTask(videoMessage);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(context.getResources(), noPhotoBitmap, task);
                videoMessage.setImageDrawable(asyncDrawable);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uri);
            }
        }
    }*/

    /*private static boolean cancelPotentialWork(Uri uri, ImageView videoMessage) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(videoMessage);

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
    }*/

   /* private static BitmapWorkerTask getBitmapWorkerTask(ImageView videoMessage) {
        if (videoMessage != null) {
            final Drawable drawable = videoMessage.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }*/

    /*private static void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private static Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }*/

    /*public static class AsyncDrawable extends BitmapDrawable
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
    }*/

    /*private static class BitmapWorkerTask extends AsyncTask<Uri, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;
        private Uri imageUri;

        public BitmapWorkerTask(ImageView videoMessage) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<>(videoMessage);

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
                final ImageView videoMessage = imageViewReference.get();
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(videoMessage);
                if (this == bitmapWorkerTask && videoMessage != null) {
                    videoMessage.setImageBitmap(bitmap);
                }
            }
        }
    }*/
}
