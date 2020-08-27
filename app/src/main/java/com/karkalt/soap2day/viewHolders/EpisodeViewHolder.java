package com.karkalt.soap2day.viewHolders;

import android.view.View;
import android.widget.TextView;

import com.karkalt.soap2day.models.Episode;
import com.karkalt.soap2day.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class EpisodeViewHolder extends ChildViewHolder {
    private TextView mTextView;
    private TextView mTextViewWatched;

    public EpisodeViewHolder(View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.textView);
        mTextViewWatched = itemView.findViewById(R.id.watched);
    }

    public void bind(Episode episode) {
        String text = episode.episodeNumber + " | " + episode.episodeName;
        mTextView.setText(text);
        if (episode.episodeWatched) {
            mTextViewWatched.setVisibility(View.VISIBLE);
        }
    }
}
