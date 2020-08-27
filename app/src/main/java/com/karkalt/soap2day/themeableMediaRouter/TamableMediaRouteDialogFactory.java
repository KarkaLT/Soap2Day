package com.karkalt.soap2day.themeableMediaRouter;

import androidx.annotation.NonNull;
import androidx.mediarouter.app.MediaRouteChooserDialogFragment;
import androidx.mediarouter.app.MediaRouteControllerDialogFragment;
import androidx.mediarouter.app.MediaRouteDialogFactory;

public class TamableMediaRouteDialogFactory extends MediaRouteDialogFactory {
    @NonNull
    @Override
    public MediaRouteChooserDialogFragment onCreateChooserDialogFragment() {
        return new TamableMediaRouterChooserDialogFragment();
    }

    @NonNull
    @Override
    public MediaRouteControllerDialogFragment onCreateControllerDialogFragment() {
        return new TamableMediaRouteControllerDialogFragment();
    }
}




