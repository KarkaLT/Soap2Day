package com.karkalt.soap2day.activities;

import android.annotation.SuppressLint;
import android.app.UiModeManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karkalt.soap2day.R;
import com.karkalt.soap2day.adapters.SeriesAdapter;
import com.karkalt.soap2day.models.Episode;
import com.karkalt.soap2day.models.Movie;
import com.karkalt.soap2day.models.Season;
import com.karkalt.soap2day.models.Series;
import com.karkalt.soap2day.themeableMediaRouter.TamableMediaRouteDialogFactory;
import com.karkalt.soap2day.utils.DownloadFileFromUrl;
import com.karkalt.soap2day.utils.InfoToast;
import com.karkalt.soap2day.utils.TinyDB;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;
import static com.karkalt.soap2day.utils.Utils.buildMediaInfo;
import static com.karkalt.soap2day.utils.Utils.isConnected;
import static com.karkalt.soap2day.utils.Utils.loadCastVideo;
import static com.karkalt.soap2day.utils.Utils.setupCast;
import static com.karkalt.soap2day.utils.Utils.startPlayerActivity;

public class MovieActivity extends AppCompatActivity {

    TextView storyTextView;
    WebView webView;
    ImageView imageView;
    RecyclerView recyclerView;
    LinearLayout infoLayout;
    ShimmerFrameLayout shimmerLayout;
    FloatingActionButton fabPlay;
    FloatingActionButton fabDownload;

    ArrayList<Season> seasonsList = new ArrayList<>();
    String videoUrl = null;

    String name;
    String imageUrl;
    String url;

    CastContext mCastContext;
    CastSession mCastSession;

    AppBarLayout appBarLayout;

    TinyDB tinyDB;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        tinyDB = new TinyDB(this);

        name = getIntent().getStringExtra("name");
        imageUrl = getIntent().getStringExtra("url_image");
        url = getIntent().getStringExtra("url");

        String path = this.getFilesDir() + "/Soap2day/Movies/" + name + "/" + name + ".mp4";

        mCastContext = setupCast(this, new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionStarting(CastSession castSession) {
            }

            @Override
            public void onSessionStarted(CastSession castSession, String s) {
                mCastSession = castSession;
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

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        appBarLayout = findViewById(R.id.app_bar);

        imageView = findViewById(R.id.image);
        storyTextView = findViewById(R.id.storyText);
        infoLayout = findViewById(R.id.infoLayout);
        infoLayout.setAlpha(0f);
        shimmerLayout = findViewById(R.id.shimmerLayout);

        Glide.with(this)
                .load(imageUrl)
                .into(imageView);
        toolBarLayout.setTitle(name);

        fabPlay = findViewById(R.id.fab_play);
        fabPlay.setScaleX(0f);
        fabPlay.setScaleY(0f);

        if (url.contains("tv")) {
            fabPlay.setVisibility(View.GONE);
        } else {
            fabPlay.setOnClickListener(view -> {
                if (mCastContext != null) {
                    if (mCastContext.getCastState() == CastState.CONNECTED) {
                        loadCastVideo(buildMediaInfo(name, videoUrl, imageUrl, MediaMetadata.MEDIA_TYPE_MOVIE), true, 0L);
                    } else {
                        if (new File(path).exists()) {
                            startPlayerActivity(this, path, name, imageUrl);
                        } else if (isConnected(this)) {
                            if (videoUrl != null) {
                                startPlayerActivity(this, videoUrl, name, imageUrl);
                            } else {
                                webView.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                            }
                        } else {
                            InfoToast.makeText(this, this.getString(R.string.no_connection)).show();
                        }
                    }
                } else {
                    if (new File(path).exists()) {
                        startPlayerActivity(this, path, name, imageUrl);
                    } else if (isConnected(this)) {
                        if (videoUrl != null) {
                            startPlayerActivity(this, videoUrl, name, imageUrl);
                        } else {
                            webView.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                        }
                    } else {
                        InfoToast.makeText(this, this.getString(R.string.no_connection)).show();
                    }
                }
            });
        }

        fabDownload = findViewById(R.id.fab_download);
        fabDownload.setScaleX(0f);
        fabDownload.setScaleY(0f);

        if (url.contains("tv")) {
            fabDownload.setVisibility(View.GONE);
        } else {
            fabDownload.setOnClickListener(view -> {
                if (videoUrl != null) {

                    new DownloadFileFromUrl(this, path, name, ThreadLocalRandom.current().nextInt(1, 10000001)).execute(videoUrl);
                } else {
                    webView.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                }
            });
        }

        recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        if (url.contains("tv") && tinyDB.checkIfKeyExists(name)) {
            update();
        } else {
            webView = new WebView(this);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.loadUrl(url);
            webView.addJavascriptInterface(this, "HtmlViewer");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    webView.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
                    if (url.contains("movie")) {
                        webView.loadUrl("javascript:function waitForElementToDisplay(e){null==document.querySelector(e)?setTimeout(function(){waitForElementToDisplay(e)},100):window.HtmlViewer.setUrl(video_src)};waitForElementToDisplay(\"video\")");
                    }
                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            appBarLayout.setExpanded(true, true);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if ((appBarLayout.getHeight() - appBarLayout.getBottom()) == 0) {
                appBarLayout.setExpanded(false, true);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onResume() {
        update();
        super.onResume();
    }

    @Override
    protected void onStart() {
        update();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    private void update() {
        if (url.contains("tv")) {
            seasonsList.clear();
            Series series = tinyDB.getObject(name, Series.class);
            if (series != null) {
                storyTextView.setText(series.getStory());
                recyclerView.setAdapter(new SeriesAdapter(series.getSeasons(), MovieActivity.this, name));
                infoLayout.setVisibility(View.VISIBLE);
                infoLayout.animate().alpha(1f).setDuration(1000);
                shimmerLayout.setVisibility(View.GONE);
            }
        } else {
            Movie movie = tinyDB.getObject(name, Movie.class);
            if (movie != null) {
                storyTextView.setText(movie.getStory());
                infoLayout.setVisibility(View.VISIBLE);
                infoLayout.animate().alpha(1f).setDuration(1000);
                shimmerLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_movie, menu);
        MenuItem menuItem = CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        MediaRouteButton mediaRouteButton = (MediaRouteButton) menuItem.getActionView();
        mediaRouteButton.setDialogFactory(new TamableMediaRouteDialogFactory());
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @JavascriptInterface
    public void showHTML(String html) {
        Document doc = Jsoup.parse(html);
        if (url.contains("tv")) {
            Series series = new Series();
            String story = doc.select("p#wrap").text().trim();
            series.setStory(story);
            series.setUrl(url);
            series.setName(name);
            series.setImageUrl(imageUrl);
            runOnUiThread(() -> {
                storyTextView.setText(story);
                infoLayout.setVisibility(View.VISIBLE);
                infoLayout.animate().alpha(1f).setDuration(1000);
                shimmerLayout.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    storyTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                }
            });

            Elements seasons = doc.getElementsByClass("alert-info-ex");
            Collections.reverse(seasons);
            for (Element season : seasons) {

                int season_number = Integer.parseInt(season.getElementsByTag("h4").text().replaceAll("[^0-9]+", ""));

                Elements episodes = season.getElementsByTag("a");
                Collections.reverse(episodes);

                ArrayList<Episode> episodesList = new ArrayList<>();
                for (Element episode : episodes) {
                    String episode_name = episode.text().replaceAll("^\\d+.", "");
                    String episode_url = "https://soap2day.to" + episode.attr("href");
                    int episode_number = Integer.parseInt(Arrays.asList(episode.text().replaceAll("[^0-9]+", " ").trim().split(" ")).get(0));

                    episodesList.add(new Episode(episode_name, episode_url, episode_number, season_number, false));
                }
                seasonsList.add(new Season("Season " + season_number, episodesList));
            }
            series.setSeasons(seasonsList);
            tinyDB.putObject(name, series);
            runOnUiThread(() -> recyclerView.setAdapter(new SeriesAdapter(seasonsList, MovieActivity.this, name)));
        } else {
            Movie movie = new Movie();
            String story = doc.select("p#wrap").text().trim();
            movie.setStory(story);
            movie.setUrl(url);
            movie.setName(name);
            movie.setImageUrl(imageUrl);
            tinyDB.putObject(name, movie);
            runOnUiThread(() -> {
                storyTextView.setText(story);
                infoLayout.setVisibility(View.VISIBLE);
                infoLayout.animate().alpha(1f).setDuration(1000);
                shimmerLayout.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    storyTextView.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
                }
            });
        }
    }

    @JavascriptInterface
    public void setUrl(String videoSrc) {
        videoUrl = videoSrc;
        runOnUiThread(() -> {
            fabPlay.animate().scaleX(1f).scaleY(1f).setDuration(300);
            fabPlay.requestFocus();
            fabDownload.animate().scaleX(1f).scaleY(1f).setDuration(300);
        });
    }

}