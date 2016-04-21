package com.incode_it.spychat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MyContactRecyclerViewAdapter extends RecyclerView.Adapter<MyContactRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "updateContacts";
    private List<MyContacts.Contact> mContacts;
    private final OnFragmentInteractionListener mListener;
    private Context context;
    public static Bitmap noPhotoBitmap;
    private LruCache<String, Bitmap> mMemoryCache;

    public MyContactRecyclerViewAdapter(OnFragmentInteractionListener listener) {
        context = (Context) listener;
        mContacts = MyContacts.getContactsList(context);
        updateContacts();
        mListener = listener;
        noPhotoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile);
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }


    private void updateContacts()
    {
        ArrayList<String> contactsNumbers = new ArrayList<>();
        for (MyContacts.Contact contact: mContacts)
        {
            contactsNumbers.add(contact.phoneNumber);
        }
        new UpdateContactsTask(contactsNumbers).execute();
    }

    private class UpdateContactsTask extends AsyncTask<Void, Void, JSONArray>
    {
        ArrayList<String> contactsNumbers;

        public UpdateContactsTask(ArrayList<String> contactsNumbers) {
            this.contactsNumbers = contactsNumbers;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONArray doInBackground(Void... params) {
            JSONArray jsonArray = null;
            try
            {
                jsonArray = tryUpdateContacts(contactsNumbers);

            }
            catch (IOException | JSONException e)
            {
                e.printStackTrace();
            }

            return jsonArray;
        }

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try
            {
                if (jsonArray == null)
                {
                    Toast.makeText(context, "Connection error", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    for (int i = 0; i < jsonArray.length(); i++)
                    {
                        JSONObject contact = (JSONObject) jsonArray.get(i);
                        String phoneNumber = contact.getString("phone");
                        phoneNumber = phoneNumber.replaceFirst(" ", "+");
                        boolean isRegistered = contact.getBoolean("isRegistered");
                        mContacts.get(i).isRegistrated = isRegistered;
                        //Log.d(TAG, "jsonArray: " + phoneNumber + " " + isRegistered);
                    }
                    notifyDataSetChanged();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    private JSONArray tryUpdateContacts(ArrayList<String> contactsNumbers) throws IOException, JSONException {
        StringBuilder sbParams = new StringBuilder();
        for (String number: contactsNumbers)
        {
            //Log.d(TAG, "number: " + number);
            sbParams.append("contacts=").append(number).append("&");
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String accessToken = sharedPreferences.getString(C.ACCESS_TOKEN, "");

        URL url = new URL(C.BASE_URL + "api/v1/usersJob/inSystem/");
        Log.i(TAG, "URL: " + url.toString() + sbParams.toString());
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setConnectTimeout(20000);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.addRequestProperty("Authorization", "Bearer "+accessToken);
        httpURLConnection.connect();

        OutputStreamWriter outputWriter = new OutputStreamWriter(httpURLConnection.getOutputStream());
        outputWriter.write(sbParams.toString());
        outputWriter.flush();
        outputWriter.close();

        int httpResponse = httpURLConnection.getResponseCode();
        Log.d(TAG, "HTTP RESP CODE "+httpResponse);
        InputStream inputStream;

        if (httpResponse == HttpURLConnection.HTTP_OK) inputStream = httpURLConnection.getInputStream();
        else inputStream = httpURLConnection.getErrorStream();

        String response = IOUtils.toString(inputStream);
        inputStream.close();
        Log.d(TAG, "resp: " + response);


        JSONArray jsonArray = null;
        if (response.equals("Access token is expired"))
        {
            if (MyConnection.sendRefreshToken(context, TAG))
                jsonArray = tryUpdateContacts(contactsNumbers);
        }
        else
        {
            JSONObject jsonResponse = new JSONObject(response);
            String res = jsonResponse.getString("result");
            if (res.equals("success"))
            jsonArray = jsonResponse.getJSONArray("contacts");

        }


        return jsonArray;
    }

    public void setContacts(List<MyContacts.Contact> mContacts)
    {
        this.mContacts = mContacts;
        notifyDataSetChanged();
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

        if (holder.mContact.isRegistrated)
        {
            holder.mNumberView.setTextColor(Color.GREEN);
        }
        else holder.mNumberView.setTextColor(Color.BLACK);

        String name = mContacts.get(position).name;
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
        else holder.mNameView.setText(name);


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
            //holder.mImage.setImageResource(R.drawable.profle);
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

            mNameView.setTypeface(ActivityMain.typeface, Typeface.BOLD);

            mNumberView.setTypeface(ActivityMain.typeface);

            mImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ActivityChat.class);
                    intent.putExtra("position", getAdapterPosition());
                    context.startActivity(intent);
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
                //Log.d(LOG_TAG, "getBitmapFromMemCache");
                imageView.setImageBitmap(bitmap);
            }
            else
            {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncDrawable asyncDrawable =
                        new AsyncDrawable(context.getResources(), noPhotoBitmap, task);
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
            //Log.d(LOG_TAG, "doInBackground");
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
