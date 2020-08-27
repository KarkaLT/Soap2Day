package com.karkalt.soap2day.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.mediarouter.app.MediaRouteButton;

import com.github.vkay94.dtpv.DoubleTapPlayerView;
import com.github.vkay94.dtpv.youtube.YouTubeOverlay;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.karkalt.soap2day.R;
import com.karkalt.soap2day.models.Series;
import com.karkalt.soap2day.themeableMediaRouter.TamableMediaRouteDialogFactory;
import com.karkalt.soap2day.utils.TinyDB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collections;

import static com.karkalt.soap2day.utils.Utils.buildMediaInfo;
import static com.karkalt.soap2day.utils.Utils.loadCastVideo;
import static com.karkalt.soap2day.utils.Utils.setupCast;

public class PlayerActivity extends AppCompatActivity {

    String url;
    String episodeUrl;
    String videoUrl = null;
    boolean tv;
    String name;
    String showName;
    String episodeName;
    int episodeNumber;
    int seasonNumber;
    String imageUrl;
    String previousUrl;
    String nextUrl;
    String previousName;
    String nextName;
    ImageButton prev;
    ImageButton next;
    boolean prevAvailable = false;
    boolean nextAvailable = false;

    DoubleTapPlayerView playerView;
    TextView nameTextView;
    SimpleExoPlayer player;
    WebView webView;
    YouTubeOverlay youTubeOverlay;
    MediaSessionCompat mediaSession;
    MediaSessionConnector mediaSessionConnector;
    boolean playWhenReady = true;
    int currentWindow = 0;
    long playbackPosition = 0;

    boolean activityFinished = false;

    MediaRouteButton mediaRouteButton;
    CastSession mCastSession;
    CastContext mCastContext;

    boolean hasProgressFocus;
    boolean hasPlayFocus;
    boolean hasPauseFocus;
    boolean hasPrevFocus;
    boolean hasNextFocus;

    TinyDB tinyDB;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tinyDB = new TinyDB(this);

        url = getIntent().getStringExtra("url");
        tv = getIntent().getBooleanExtra("tv", false);
        name = getIntent().getStringExtra("name");
        showName = getIntent().getStringExtra("show_name");
        episodeUrl = getIntent().getStringExtra("episode_url");
        episodeName = getIntent().getStringExtra("episode_name");
        episodeNumber = getIntent().getIntExtra("episode_number", 0);
        seasonNumber = getIntent().getIntExtra("season_number", 0);
        imageUrl = getIntent().getStringExtra("url_image");
        setContentView(R.layout.activity_player);

        mCastContext = setupCast(this, new SessionManagerListener<CastSession>() {
            @Override
            public void onSessionStarting(CastSession castSession) {
            }

            @Override
            public void onSessionStarted(CastSession castSession, String s) {
                mCastSession = castSession;
                playbackPosition = player != null ? player.getCurrentPosition() : 0;
                if (tv) {
                    if (videoUrl != null) {
                        loadCastVideo(buildMediaInfo(name, videoUrl, imageUrl, MediaMetadata.MEDIA_TYPE_TV_SHOW), true, playbackPosition);
                    }
                } else {
                    loadCastVideo(buildMediaInfo(name, url, imageUrl, MediaMetadata.MEDIA_TYPE_MOVIE), true, playbackPosition);
                }
                releasePlayer();
            }

            @Override
            public void onSessionStartFailed(CastSession castSession, int i) {
            }

            @Override
            public void onSessionEnding(CastSession castSession) {
            }

            @Override
            public void onSessionEnded(CastSession castSession, int i) {
            }

            @Override
            public void onSessionResuming(CastSession castSession, String s) {
            }

            @Override
            public void onSessionResumed(CastSession castSession, boolean b) {
            }

            @Override
            public void onSessionResumeFailed(CastSession castSession, int i) {
            }

            @Override
            public void onSessionSuspended(CastSession castSession, int i) {
            }
        });

        if (mCastContext != null) {
            mediaRouteButton = findViewById(R.id.media_route_button);
            mediaRouteButton.setRemoteIndicatorDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_cast, getTheme()));
            CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mediaRouteButton);
            mediaRouteButton.setDialogFactory(new TamableMediaRouteDialogFactory());
            if (mCastContext.getCastState() != CastState.NO_DEVICES_AVAILABLE) {
                mediaRouteButton.setVisibility(View.VISIBLE);
            }
            mCastContext.addCastStateListener(state -> {
                if (state == CastState.NO_DEVICES_AVAILABLE) {
                    mediaRouteButton.setVisibility(View.GONE);
                } else {
                    mediaRouteButton.setVisibility(View.VISIBLE);
                }
            });
        }

        nameTextView = findViewById(R.id.name);
        nameTextView.setText(name);

        if (tv) {
            nameTextView.setText(episodeName);
            webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.loadUrl(episodeUrl);
            webView.addJavascriptInterface(this, "HtmlViewer");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    webView.loadUrl("javascript:function waitForElementToDisplay(e){null==document.querySelector(e)?setTimeout(function(){waitForElementToDisplay(e)},100):window.HtmlViewer.showHTML(\"<html>\"+document.getElementsByTagName(\"html\")[0].innerHTML+\"</html>\", video_src)};waitForElementToDisplay(\"video\")");
                }
            });

            prev = findViewById(R.id.prev);
            prev.setEnabled(false);
            prev.setOnClickListener(v -> playPrevious());
            prev.setOnFocusChangeListener((v, hasFocus) -> hasPrevFocus = hasFocus);

            next = findViewById(R.id.next);
            next.setEnabled(false);
            next.setOnClickListener(v -> playNext());
            next.setOnFocusChangeListener((v, hasFocus) -> hasNextFocus = hasFocus);

        } else {
            ((ViewGroup)findViewById(R.id.contentControls).getParent()).removeView(findViewById(R.id.contentControls));
        }

        playerView = findViewById(R.id.playerView);

        youTubeOverlay = findViewById(R.id.youtube_overlay);
        youTubeOverlay.performListener(new YouTubeOverlay.PerformListener() {
            @Override
            public void onAnimationStart() {
                youTubeOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd() {
                youTubeOverlay.setVisibility(View.GONE);
            }
        });

        hideSystemUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }

        findViewById(R.id.exo_progress).setOnFocusChangeListener((v, hasFocus) -> hasProgressFocus = hasFocus);
        findViewById(R.id.exo_play).setOnFocusChangeListener((v, hasFocus) -> hasPlayFocus = hasFocus);
        findViewById(R.id.exo_pause).setOnFocusChangeListener((v, hasFocus) -> hasPauseFocus = hasFocus);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player != null) {
            player.release();
            player = null;
        }
        activityFinished = true;
        finish();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (player != null) {
                player.release();
                player = null;
            }
            activityFinished = true;
            finish();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (hasPlayFocus && playerView.isControllerVisible()) {
                    if (player != null) {
                        player.setPlayWhenReady(true);
                    }
                } else if (hasPauseFocus) {
                    if (player != null) {
                        player.setPlayWhenReady(false);
                    }
                } else if (hasNextFocus) {
                    playNext();
                } else if (hasPrevFocus) {
                    playPrevious();
                }
                if (!playerView.isControllerVisible()) {
                    playerView.showController();
                }
            }
        }

        if (hasProgressFocus) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                playerView.showController();
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (player != null) {
                        player.seekTo(Math.max(player.getCurrentPosition() - 10 * 1000, 0));
                    }
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (player != null) {
                        player.seekTo(Math.min(player.getCurrentPosition() + 10 * 1000, player.getDuration()));
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        hideSystemUI();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private MediaSource buildMediaSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, "exoplayer-codelab");
        return new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    private AudioManager.OnAudioFocusChangeListener focusChangeListener =
            focusChange -> {
                switch (focusChange) {
                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK):
                        if (player != null) {
                            player.setVolume(0.2f);
                        }
                        break;
                    case (AudioManager.AUDIOFOCUS_LOSS_TRANSIENT):
                    case (AudioManager.AUDIOFOCUS_LOSS):
                        if (player != null) {
                            player.setPlayWhenReady(false);
                            player.getPlaybackState();
                        }
                        break;
                    case (AudioManager.AUDIOFOCUS_GAIN):
                        if (player != null) {
                            player.setVolume(1f);
                            player.setPlayWhenReady(true);
                            player.getPlaybackState();
                        }
                        break;
                    default: break;
                }
            };

    private void initializePlayer(String url) {
        if (!activityFinished) {
            runOnUiThread(() -> {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                assert am != null;
                int result = am.requestAudioFocus(focusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    Log.d("PLAYER", "initializePlayer: starting player with url: " + url);
                    TrackSelector trackSelector = new DefaultTrackSelector(this);
                    DefaultLoadControl loadControl = new DefaultLoadControl.Builder().setBufferDurationsMs(32 * 1024, 64 * 1024, 1024, 1024).createDefaultLoadControl();

                    if (player == null) {
                        player = new SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).setLoadControl(loadControl).build();
                        playerView.setPlayer(player);
                    }
                    player.setPlayWhenReady(playWhenReady);
                    player.seekTo(currentWindow, playbackPosition);

                    Uri uri = Uri.parse(url);
                    MediaSource mediaSource = buildMediaSource(uri);
                    player.prepare(mediaSource, false, false);

                    player.addListener(new Player.EventListener() {
                        @Override
                        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                            if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !playWhenReady) {
                                playerView.setKeepScreenOn(false);
                            } else {
                                playerView.setKeepScreenOn(true);
                            }
                            if (playbackState == Player.STATE_ENDED) {
                                if (tv) {
                                    if (new File(url).exists()) {
                                        boolean deleted = new File(url).delete();
                                        if (deleted) {
                                            Log.d("DELETE", "onPlayerStateChanged: episode deleted");
                                        }
                                    }
                                    playNext();
                                    Series series = tinyDB.getObject(showName, Series.class);
                                    if (series != null) {
                                        series.setEpisodeWatched(seasonNumber, episodeNumber, true);
                                        tinyDB.putObject(showName, series);
                                    }
                                }
                            }
                            if (tv) {
                                if (player.getPlayWhenReady() && playbackState == Player.STATE_READY) {
                                    prev.setNextFocusRightId(R.id.exo_pause);
                                    next.setNextFocusLeftId(R.id.exo_pause);
                                    findViewById(R.id.exo_progress).setNextFocusUpId(R.id.exo_pause);
                                } else {
                                    prev.setNextFocusRightId(R.id.exo_play);
                                    next.setNextFocusLeftId(R.id.exo_play);
                                    findViewById(R.id.exo_progress).setNextFocusUpId(R.id.exo_play);
                                }
                            }
                        }
                    });
                    mediaSession = new MediaSessionCompat(this, getPackageName());
                    mediaSession.setMediaButtonReceiver(null);
                    mediaSessionConnector = new MediaSessionConnector(mediaSession);
                    youTubeOverlay.player(player);
                    mediaSessionConnector.setPlayer(player);
                    mediaSession.setActive(true);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart() {
        super.onStart();
        activityFinished = false;
        hideSystemUI();
        if (Util.SDK_INT >= 24) {
            if (tv && videoUrl != null) {
                initializePlayer(videoUrl);
            } else {
                initializePlayer(url);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onResume() {
        super.onResume();
        activityFinished = false;
        hideSystemUI();
        if ((Util.SDK_INT < 24 || player == null)) {
            if (tv && videoUrl != null) {
                initializePlayer(videoUrl);
            } else {
                initializePlayer(url);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activityFinished = true;
        if (Util.SDK_INT < 24) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activityFinished = true;
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityFinished = true;
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            Log.d("PLAYER", "releasePlayer: Player released");
            player.setPlayWhenReady(false);
            playWhenReady = false;
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            mediaSessionConnector.setPlayer(null);
            mediaSession.setActive(false);
        }
    }

    private void playPrevious() {
        if (prevAvailable) {
            url = "";
            next.setEnabled(false);
            prev.setEnabled(false);
            if (player != null) {
                player.stop(true);
            }
            releasePlayer();
            currentWindow = 0;
            playbackPosition = 0;
            nameTextView.setText(previousName);
            webView.loadUrl(previousUrl);
        }
    }

    private void playNext() {
        if (nextAvailable) {
            url = "";
            next.setEnabled(false);
            prev.setEnabled(false);
            if (player != null) {
                player.stop(true);
            }
            releasePlayer();
            currentWindow = 0;
            playbackPosition = 0;
            nameTextView.setText(nextName);
            webView.loadUrl(nextUrl);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @JavascriptInterface
    public void showHTML(String html, String video_url) {
        videoUrl = video_url;
        runOnUiThread(() -> {
            if (mCastContext != null) {
                if (mCastContext.getCastState() == CastState.CONNECTED) {
                    loadCastVideo(buildMediaInfo(name, videoUrl, imageUrl, MediaMetadata.MEDIA_TYPE_TV_SHOW), true, 0L);
                } else if (!new File(url).exists()) {
                    initializePlayer(videoUrl);
                }
            } else if (!new File(url).exists()) {
                initializePlayer(videoUrl);
            }
        });

        if (tv) {
            Document doc = Jsoup.parse(html);

            Elements seasons = doc.getElementsByClass("alert-info-ex");
            Collections.reverse(seasons);

            Elements episodes = seasons.first().getElementsByTag("a");
            Collections.reverse(episodes);

            for (int i = 1; i < seasons.size(); i++) {
                Elements elements = seasons.get(i).getElementsByTag("a");
                Collections.reverse(elements);
                episodes.addAll(elements);
            }

            for (int i = 0; i < episodes.size(); i++) {
                if (episodes.get(i).attr("href").equals("#")) {
                    try {
                        previousName = episodes.get(i - 1).text().replaceAll("^\\d+.", "");
                        previousUrl = "https://soap2day.to" + episodes.get(i - 1).attr("href");
                        runOnUiThread(() -> {
                            prev.setEnabled(true);
                            prev.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
                        });
                        prevAvailable = true;
                    } catch (IndexOutOfBoundsException e) {
                        runOnUiThread(() -> {
                            prev.setEnabled(false);
                            prev.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccentSecondary)));
                        });
                        prevAvailable = false;
                    }

                    try {
                        nextName = episodes.get(i + 1).text().replaceAll("^\\d+.", "");
                        nextUrl = "https://soap2day.to" + episodes.get(i + 1).attr("href");
                        runOnUiThread(() -> {
                            next.setEnabled(true);
                            next.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
                        });
                        nextAvailable = true;
                    } catch (IndexOutOfBoundsException e) {
                        runOnUiThread(() -> {
                            next.setEnabled(false);
                            next.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.colorAccentSecondary)));
                        });
                        nextAvailable = false;
                    }
                    break;
                }
            }
        }
    }
}