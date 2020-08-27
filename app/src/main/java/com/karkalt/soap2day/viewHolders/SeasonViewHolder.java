package com.karkalt.soap2day.viewHolders;

import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.karkalt.soap2day.R;
import com.karkalt.soap2day.models.Season;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class SeasonViewHolder extends GroupViewHolder {
    private TextView mTextView;
    private ImageView mImageView;

    public SeasonViewHolder(View itemView) {
        super(itemView);
        mTextView = itemView.findViewById(R.id.textView);
        mImageView = itemView.findViewById(R.id.imageView);
    }

    public void bind(Season season) {
        mTextView.setText(season.getTitle());
        mImageView.setImageResource(R.drawable.ic_arrow_down);
    }

    @Override
    public void expand() {
        animateExpand();
    }

    @Override
    public void collapse() {
        animateCollapse();
    }

    private void animateExpand() {
        RotateAnimation rotate = new RotateAnimation(360, 180, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        mImageView.setAnimation(rotate);
    }

    private void animateCollapse() {
        RotateAnimation rotate = new RotateAnimation(180, 360, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(300);
        rotate.setFillAfter(true);
        mImageView.setAnimation(rotate);
    }
}
