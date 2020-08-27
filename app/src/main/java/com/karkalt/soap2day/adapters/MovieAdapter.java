package com.karkalt.soap2day.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.Shimmer;
import com.facebook.shimmer.ShimmerDrawable;
import com.karkalt.soap2day.R;
import com.karkalt.soap2day.activities.MovieActivity;

import java.util.ArrayList;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {
    private ArrayList<String> mNames;
    private ArrayList<String> mImages;
    private ArrayList<String> mUrls;
    private LayoutInflater mInflater;
    private Context mContext;

    public MovieAdapter(Context context, ArrayList<String> names, ArrayList<String> images, ArrayList<String> urls) {
        mInflater = LayoutInflater.from(context);
        mContext = context;
        mNames = names;
        mImages = images;
        mUrls = urls;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.textView.setText(mNames.get(position));
        Shimmer shimmer = new Shimmer.AlphaHighlightBuilder()
                .setDuration(2000)
                .setBaseAlpha(0.9f)
                .setHighlightAlpha(1f)
                .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
                .setAutoStart(true)
                .build();
        ShimmerDrawable shimmerDrawable = new ShimmerDrawable();
        shimmerDrawable.setShimmer(shimmer);
        Glide.with(mContext)
                .load(mImages.get(position))
                .placeholder(shimmerDrawable)
                .into(holder.imageView);
        holder.relativeLayout.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, MovieActivity.class);
            intent.putExtra("url", mUrls.get(position));
            intent.putExtra("url_image", mImages.get(position));
            intent.putExtra("name", mNames.get(position));
            mContext.startActivity(intent);
        });
        holder.relativeLayout.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                holder.relativeLayout.animate().scaleY(1.3f).scaleX(1.3f).setDuration(100).start();
            } else {
                holder.relativeLayout.animate().scaleY(1f).scaleX(1f).setDuration(100).start();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNames.size();
    }



    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public RelativeLayout relativeLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name);
            imageView = itemView.findViewById(R.id.image);
            relativeLayout = itemView.findViewById(R.id.layout);
        }
    }
}
