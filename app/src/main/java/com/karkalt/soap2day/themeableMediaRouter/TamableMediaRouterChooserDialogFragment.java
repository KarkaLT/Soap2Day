package com.karkalt.soap2day.themeableMediaRouter;

import android.content.Context;
import android.os.Bundle;

import androidx.mediarouter.app.MediaRouteChooserDialog;
import androidx.mediarouter.app.MediaRouteChooserDialogFragment;

import com.karkalt.soap2day.R;

public class TamableMediaRouterChooserDialogFragment extends MediaRouteChooserDialogFragment {

    @Override
    public MediaRouteChooserDialog onCreateChooserDialog(Context context, Bundle savedInstanceState) {
        return new MediaRouteChooserDialog(context, R.style.CastChooserDialogTheme);
    }
}
