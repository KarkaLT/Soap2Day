package com.karkalt.soap2day.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.karkalt.soap2day.R;

public class InfoToast extends Toast {
    private TextView textView;

    public InfoToast(Context context) {
        super(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.info_toast, null);
        initialiseView(view);
        setView(view);
        setDuration(LENGTH_LONG);
        setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 20);
    }

    private void initialiseView(View view) {
        textView = (TextView) view.findViewById(R.id.textView);
    }

    public void setTitle(String s) {
        if (s != null && s.length() != 0) {
            textView.setText(s);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    @Override
    public void show() {
        super.show();
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    public static InfoToast makeText(Context context, String s) {
        InfoToast infoToast = new InfoToast(context);
        infoToast.setTitle(s);
        return infoToast;
    }
}

