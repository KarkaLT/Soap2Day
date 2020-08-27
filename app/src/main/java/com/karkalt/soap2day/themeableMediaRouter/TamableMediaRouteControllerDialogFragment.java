package com.karkalt.soap2day.themeableMediaRouter;

import android.content.Context;
import android.os.Bundle;

import androidx.mediarouter.app.MediaRouteControllerDialog;
import androidx.mediarouter.app.MediaRouteControllerDialogFragment;

import com.karkalt.soap2day.R;

public class TamableMediaRouteControllerDialogFragment extends MediaRouteControllerDialogFragment {

    @Override
    public MediaRouteControllerDialog onCreateControllerDialog(Context context, Bundle savedInstanceState) {
        return  new MediaRouteControllerDialog(context, R.style.CastControllerDialogTheme);
    }
}
