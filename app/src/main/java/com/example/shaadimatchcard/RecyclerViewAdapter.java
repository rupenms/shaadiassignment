package com.example.shaadimatchcard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Bitmap bitmap;
    private String pictureURL;
    private DownloadImageTask imageTask;
    private int rowID;
    private static final String MEMBER_ACCEPTED = "Member accepted";
    private static final String MEMBER_REJECTED = "Member rejected";
    private Context mContext;

    // data is passed into the constructor
    RecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mContext = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String data = mData.get(position);
        String[] dataArray = data.split(",");
        holder.fullName.setText(dataArray[0]);
        holder.gender.setText(dataArray[1]);
        holder.email.setText(dataArray[2]);
        holder.phone.setText(dataArray[3]);
        pictureURL = dataArray[4];
        imageTask = new DownloadImageTask();
        imageTask.execute(pictureURL);
        holder.displayPicture.setImageBitmap(bitmap);
        rowID = Integer.parseInt(dataArray[5]);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView fullName, gender, email, phone, message;
        Button accept, decline;
        ImageView displayPicture;

        ViewHolder(View itemView) {
            super(itemView);
            fullName = itemView.findViewById(R.id.fullname);
            gender = itemView.findViewById(R.id.gender);
            email = itemView.findViewById(R.id.email);
            phone = itemView.findViewById(R.id.phone);
            accept = itemView.findViewById(R.id.accept);
            decline = itemView.findViewById(R.id.decline);
            displayPicture = itemView.findViewById(R.id.displaypicture);
            message = itemView.findViewById(R.id.message);

            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accept.setVisibility(View.INVISIBLE);
                    decline.setVisibility(View.INVISIBLE);
                    message.setVisibility(View.VISIBLE);
                    message.setText(MEMBER_ACCEPTED);
                    updateRow(MEMBER_ACCEPTED);
                }
            });

            decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    accept.setVisibility(View.INVISIBLE);
                    decline.setVisibility(View.INVISIBLE);
                    message.setVisibility(View.VISIBLE);
                    message.setText(MEMBER_REJECTED);
                    updateRow(MEMBER_REJECTED);
                }
            });
            //itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bitmap = result;
        }
    }

    private void updateRow(String msg) {
        try {
            SQLiteDatabase profileDb = mContext.openOrCreateDatabase("Shaadi",android.content.Context.MODE_PRIVATE ,null);
            ContentValues cv = new ContentValues();
            cv.put("status",msg);
            profileDb.update("Shaadi", cv, "id = ?", new String[]{String.valueOf(rowID)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
