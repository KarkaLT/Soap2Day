package com.karkalt.soap2day.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;

import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadRequestData;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.karkalt.soap2day.activities.PlayerActivity;

import static android.content.Context.UI_MODE_SERVICE;

public class Utils {
    public static MediaInfo buildMediaInfo(String name, String videoUrl, String imageUrl, int mediaMetadata) {
        MediaMetadata movieMetadata = new MediaMetadata(mediaMetadata);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, name);
        movieMetadata.addImage(new WebImage(Uri.parse(imageUrl)));
        return new MediaInfo.Builder(videoUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata)
                .build();
    }

    public static void loadCastVideo(MediaInfo mediaInfo, boolean playWhenReady, long playbackPosition) {
        CastContext castContext = CastContext.getSharedInstance();
        assert castContext != null;
        SessionManager mSessionManager = castContext.getSessionManager();
        CastSession mCastSession = mSessionManager.getCurrentCastSession();
        if (mCastSession == null) return;
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) return;
        remoteMediaClient.load(new MediaLoadRequestData.Builder()
                .setMediaInfo(mediaInfo)
                .setAutoplay(playWhenReady)
                .setCurrentTime(playbackPosition)
                .build());
    }

    public static CastContext setupCast(Context context, SessionManagerListener<CastSession> listener) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;
        CastContext castContext = null;
        if (uiModeManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION) {
            castContext = CastContext.getSharedInstance(context);
            SessionManager mSessionManager = castContext.getSessionManager();
            mSessionManager.addSessionManagerListener(listener, CastSession.class);
        }
        return castContext;
    }

    public static CastContext setupCast(Context context) {
        UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UI_MODE_SERVICE);
        assert uiModeManager != null;
        CastContext castContext = null;
        if (uiModeManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION) {
            castContext = CastContext.getSharedInstance(context);
        }
        return castContext;
    }

    public static String getCastName(CastContext castContext) {
        return castContext.getSessionManager().getCurrentCastSession().getCastDevice().getFriendlyName();
    }

    public static void startPlayerActivity(Context context, String url, boolean tv, String showName,
                                           String episodeUrl, String episodeName, int episodeNumber,
                                           int seasonNumber, String image) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("tv", tv);
        intent.putExtra("show_name", showName);
        intent.putExtra("episode_url", episodeUrl);
        intent.putExtra("episode_name", episodeName);
        intent.putExtra("episode_number", episodeNumber);
        intent.putExtra("season_number", seasonNumber);
        intent.putExtra("url_image", image);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startPlayerActivity(Context context, String url, String name, String image) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("name", name);
        intent.putExtra("url_image", image);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}
