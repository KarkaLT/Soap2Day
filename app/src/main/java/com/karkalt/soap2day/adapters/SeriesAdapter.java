package com.karkalt.soap2day.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.UiModeManager;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karkalt.soap2day.R;
import com.karkalt.soap2day.models.Episode;
import com.karkalt.soap2day.models.Season;
import com.karkalt.soap2day.models.Series;
import com.karkalt.soap2day.utils.DownloadFileFromUrl;
import com.karkalt.soap2day.utils.InfoToast;
import com.karkalt.soap2day.utils.TinyDB;
import com.karkalt.soap2day.viewHolders.EpisodeViewHolder;
import com.karkalt.soap2day.viewHolders.SeasonViewHolder;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static android.content.Context.UI_MODE_SERVICE;
import static com.karkalt.soap2day.utils.Utils.buildMediaInfo;
import static com.karkalt.soap2day.utils.Utils.getCastName;
import static com.karkalt.soap2day.utils.Utils.isConnected;
import static com.karkalt.soap2day.utils.Utils.loadCastVideo;
import static com.karkalt.soap2day.utils.Utils.setupCast;
import static com.karkalt.soap2day.utils.Utils.startPlayerActivity;

public class SeriesAdapter extends ExpandableRecyclerViewAdapter<SeasonViewHolder, EpisodeViewHolder> {
    Activity activity;
    TinyDB tinyDB;
    Series series;

    public SeriesAdapter(List<? extends ExpandableGroup> groups, Activity activity, String showName) {
        super(groups);
        this.activity = activity;
        this.tinyDB = new TinyDB(activity);
        this.series = tinyDB.getObject(showName, Series.class);
    }

    @Override
    public SeasonViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.recyclerview_group_season, parent, false);
        return new SeasonViewHolder(view);
    }

    @Override
    public EpisodeViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.recyclerview_child_episode, parent, false);
        return new EpisodeViewHolder(view);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public void onBindChildViewHolder(EpisodeViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final Episode episode = (Episode) group.getItems().get(childIndex);
        holder.bind(episode);
        String path = activity.getFilesDir() + "/Soap2day/Tv shows/" + series.getName() + "/S" + episode.seasonNumber + "E" + episode.episodeNumber + ".mp4";
        holder.itemView.setOnClickListener(v -> {
            CastContext castContext = setupCast(activity);
            if (castContext != null) {
                if (castContext.getCastState() == CastState.CONNECTED) {
                    class JavaScriptInterface {
                        @JavascriptInterface
                        public void getUrl(String video_src) {
                            try {
                                URL url = new URL(video_src);
                                URLConnection urlConnection = url.openConnection();
                                urlConnection.connect();
                                episode.episodeSize = urlConnection.getContentLength();
                                episode.episodeVideoUrl = video_src;
                                activity.runOnUiThread(() -> loadCastVideo(buildMediaInfo(episode.episodeName, episode.episodeVideoUrl, series.getImageUrl(), MediaMetadata.MEDIA_TYPE_TV_SHOW), true, 0L));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    WebView webView = new WebView(activity);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.getSettings().setDomStorageEnabled(true);
                    webView.loadUrl(episode.episodeUrl);
                    webView.addJavascriptInterface(new JavaScriptInterface(), "HtmlViewer");
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                            return false;
                        }

                        @Override
                        public void onPageFinished(WebView view, String url) {
                            webView.loadUrl("javascript:function waitForElementToDisplay(e){null==document.querySelector(e)?setTimeout(function(){waitForElementToDisplay(e)},100):window.HtmlViewer.getUrl(video_src)};waitForElementToDisplay(\"video\")");
                        }
                    });
                    InfoToast.makeText(activity, activity.getString(R.string.cast_playing, getCastName(castContext))).show();
                } else {
                    if (new File(path).exists()) {
                        startPlayerActivity(activity, path, true, series.getName(), episode.episodeUrl, episode.episodeName, episode.episodeNumber, episode.seasonNumber, series.getImageUrl());
                    } else if (isConnected(activity)) {
                        startPlayerActivity(activity, series.getUrl(), true, series.getName(), episode.episodeUrl, episode.episodeName, episode.episodeNumber, episode.seasonNumber, series.getImageUrl());
                    } else {
                        InfoToast.makeText(activity, activity.getString(R.string.no_connection)).show();
                    }
                }
            } else {
                if (new File(path).exists()) {
                    startPlayerActivity(activity, path, true, series.getName(), episode.episodeUrl, episode.episodeName, episode.episodeNumber, episode.seasonNumber, series.getImageUrl());
                } else if (isConnected(activity)) {
                    startPlayerActivity(activity, series.getUrl(), true, series.getName(), episode.episodeUrl, episode.episodeName, episode.episodeNumber, episode.seasonNumber, series.getImageUrl());
                } else {
                    InfoToast.makeText(activity, activity.getString(R.string.no_connection)).show();
                }
            }
        });

        @SuppressLint("InflateParams") View view = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(activity);
        dialog.setContentView(view);

        holder.itemView.setOnLongClickListener(v -> {

            // Download button

            UiModeManager uiModeManager = (UiModeManager) activity.getSystemService(UI_MODE_SERVICE);
            assert uiModeManager != null;
            if (uiModeManager.getCurrentModeType() != Configuration.UI_MODE_TYPE_TELEVISION) {
                LinearLayout download = view.findViewById(R.id.download);
                TextView downloadTextView = view.findViewById(R.id.download_text);
                TextView sizeTextView = view.findViewById(R.id.size);
                File file = new File(path);
                Log.d("LOL", "onBindChildViewHolder: " + file.exists());
                if (file.exists()) {
                    downloadTextView.setText(R.string.delete);
                    long fileSize = file.length() / 1024 / 1024;
                    sizeTextView.setText(activity.getString(R.string.free_up, fileSize));
                    download.setOnClickListener(ignored -> {
                        boolean deleted = file.delete();
                        if (deleted) {
                            InfoToast.makeText(activity, activity.getString(R.string.delete_successful)).show();
                        }
                        dialog.dismiss();
                    });
                } else {
                    download.setEnabled(false);
                    downloadTextView.setText(R.string.download);
                    if (episode.episodeSize == null || episode.episodeSize == 0) {
                        class JavaScriptInterface {
                            @JavascriptInterface
                            public void getUrl(String video_src) {
                                try {
                                    URL url = new URL(video_src);
                                    URLConnection urlConnection = url.openConnection();
                                    urlConnection.connect();
                                    episode.episodeSize = urlConnection.getContentLength();
                                    episode.episodeVideoUrl = video_src;
                                    long fileSize = episode.episodeSize / 1024 / 1024;
                                    activity.runOnUiThread(() -> {
                                        sizeTextView.setText(activity.getString(R.string.size, fileSize));
                                        download.setEnabled(true);
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        WebView webView = new WebView(activity);
                        webView.getSettings().setJavaScriptEnabled(true);
                        webView.getSettings().setDomStorageEnabled(true);
                        webView.loadUrl(episode.episodeUrl);
                        webView.addJavascriptInterface(new JavaScriptInterface(), "HtmlViewer");
                        webView.setWebViewClient(new WebViewClient() {
                            @Override
                            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                return false;
                            }

                            @Override
                            public void onPageFinished(WebView view, String url) {
                                webView.loadUrl("javascript:function waitForElementToDisplay(e){null==document.querySelector(e)?setTimeout(function(){waitForElementToDisplay(e)},100):window.HtmlViewer.getUrl(video_src)};waitForElementToDisplay(\"video\")");
                            }
                        });
                    } else {
                        long fileSize = episode.episodeSize / 1024 / 1024;
                        activity.runOnUiThread(() -> {
                            sizeTextView.setText(activity.getString(R.string.size, fileSize));
                            download.setEnabled(true);
                        });
                    }
                    download.setOnClickListener(ignored -> {
                        new DownloadFileFromUrl(activity, path, episode.episodeName, Integer.parseInt(episode.seasonNumber + "" + episode.episodeNumber)).execute(episode.episodeVideoUrl);
                        dialog.dismiss();
                    });
                }
            }

            // Watched button

            TextView watched = view.findViewById(R.id.watched);
            if (episode.episodeWatched) {
                watched.setText(R.string.mark_as_unwatched);
            } else {
                watched.setText(R.string.mark_as_watched);
            }

            watched.setOnClickListener(ignored -> {
                episode.episodeWatched = !episode.episodeWatched;
                notifyDataSetChanged();
                series.setEpisodeWatched(episode.seasonNumber, episode.episodeNumber, episode.episodeWatched);
                tinyDB.putObject(series.getName(), series);
                dialog.dismiss();
            });

            dialog.show();
            return false;
        });
    }

    @Override
    public void onBindGroupViewHolder(SeasonViewHolder holder, int flatPosition, ExpandableGroup group) {
        final Season season = (Season) group;
        holder.bind(season);
    }
}
