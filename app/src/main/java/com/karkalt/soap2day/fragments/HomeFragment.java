package com.karkalt.soap2day.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.karkalt.soap2day.R;
import com.karkalt.soap2day.adapters.MovieAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    View view;

    ScrollView scrollView;

    RecyclerView recyclerViewMoviesPopular;
    RecyclerView.Adapter adapterMoviesPopular;
    RecyclerView.LayoutManager managerMoviesPopular;
    RecyclerView recyclerViewMoviesLatest;
    RecyclerView.Adapter adapterMoviesLatest;
    RecyclerView.LayoutManager managerMoviesLatest;
    RecyclerView recyclerViewTvPopular;
    RecyclerView.Adapter adapterTvPopular;
    RecyclerView.LayoutManager managerTvPopular;
    RecyclerView recyclerViewTvLatest;
    RecyclerView.Adapter adapterTvLatest;
    RecyclerView.LayoutManager managerTvLatest;

    ArrayList<String> moviesPopularNames = new ArrayList<>();
    ArrayList<String> moviesPopularImages = new ArrayList<>();
    ArrayList<String> moviesPopularUrls = new ArrayList<>();
    ArrayList<String> moviesLatestNames = new ArrayList<>();
    ArrayList<String> moviesLatestImages = new ArrayList<>();
    ArrayList<String> moviesLatestUrls = new ArrayList<>();
    ArrayList<String> tvPopularNames = new ArrayList<>();
    ArrayList<String> tvPopularImages = new ArrayList<>();
    ArrayList<String> tvPopularUrls = new ArrayList<>();
    ArrayList<String> tvLatestNames = new ArrayList<>();
    ArrayList<String> tvLatestImages = new ArrayList<>();
    ArrayList<String> tvLatestUrls = new ArrayList<>();
    WebView webView;


    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }
        view = inflater.inflate(R.layout.fragment_home, container, false);

        scrollView = view.findViewById(R.id.scrollView);
        scrollView.setAlpha(0f);

        recyclerViewMoviesPopular = view.findViewById(R.id.recycler_movies_most_popular);
        recyclerViewMoviesPopular.setHasFixedSize(true);
        managerMoviesPopular = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMoviesPopular.setLayoutManager(managerMoviesPopular);
        adapterMoviesPopular = new MovieAdapter(getActivity(), moviesPopularNames, moviesPopularImages, moviesPopularUrls);
        recyclerViewMoviesPopular.setAdapter(adapterMoviesPopular);

        recyclerViewMoviesLatest = view.findViewById(R.id.recycler_movies_latest_updates);
        recyclerViewMoviesLatest.setHasFixedSize(true);
        managerMoviesLatest = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMoviesLatest.setLayoutManager(managerMoviesLatest);
        adapterMoviesLatest = new MovieAdapter(getActivity(), moviesLatestNames, moviesLatestImages, moviesLatestUrls);
        recyclerViewMoviesLatest.setAdapter(adapterMoviesLatest);

        recyclerViewTvPopular = view.findViewById(R.id.recycler_tv_most_popular);
        recyclerViewTvPopular.setHasFixedSize(true);
        managerTvPopular = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTvPopular.setLayoutManager(managerTvPopular);
        adapterTvPopular = new MovieAdapter(getActivity(), tvPopularNames, tvPopularImages, tvPopularUrls);
        recyclerViewTvPopular.setAdapter(adapterTvPopular);

        recyclerViewTvLatest = view.findViewById(R.id.recycler_tv_latest_updates);
        recyclerViewTvLatest.setHasFixedSize(true);
        managerTvLatest = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTvLatest.setLayoutManager(managerTvLatest);
        adapterTvLatest = new MovieAdapter(getActivity(), tvLatestNames, tvLatestImages, tvLatestUrls);
        recyclerViewTvLatest.setAdapter(adapterTvLatest);

        webView = new WebView(getContext());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.loadUrl("https://soap2day.to");
        webView.addJavascriptInterface(this, "HtmlViewer");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl("javascript:window.HtmlViewer.showHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        onCreate(outState);
    }

    @JavascriptInterface
    public void showHTML(String html) {

        Document doc = Jsoup.parse(html);
        Log.d("TITLE", "showHTML: " + doc.title());

        if (!doc.title().equals("SOAP2DAY")) return;


        Elements elements = doc.getElementsByClass("panel");

        Element movies = elements.get(1);

        Element popular = movies.getElementsByClass("alert").get(0).parent();
        Elements thumbnails = popular.getElementsByClass("thumbnail");
        for (Element element : thumbnails) {
            String name = element.getElementsByTag("h5").text().replaceAll("\\[\\d+×\\d+]", "").trim();
            String image = element.getElementsByTag("img").attr("src");
            String url = "https://soap2day.to" + element.getElementsByTag("a").attr("href");
            moviesPopularNames.add(name);
            moviesPopularImages.add(image);
            moviesPopularUrls.add(url);
        }
        requireActivity().runOnUiThread(() -> adapterMoviesPopular.notifyDataSetChanged());

        Element latest = movies.getElementsByClass("alert").get(1).parent();
        thumbnails = latest.getElementsByClass("thumbnail");
        for (Element element : thumbnails) {
            String name = element.getElementsByTag("h5").text().replaceAll("\\[\\d+×\\d+]", "").trim();
            String image = element.getElementsByTag("img").attr("src");
            String url = "https://soap2day.to" + element.getElementsByTag("a").attr("href");
            moviesLatestNames.add(name);
            moviesLatestImages.add(image);
            moviesLatestUrls.add(url);
        }
        requireActivity().runOnUiThread(() -> adapterMoviesLatest.notifyDataSetChanged());

        Element tvShows = elements.get(2);
        popular = tvShows.getElementsByClass("alert").get(0).parent();
        thumbnails = popular.getElementsByClass("thumbnail");
        for (Element element : thumbnails) {
            String name = element.getElementsByTag("h5").text().replaceAll("\\[\\d+×\\d+]", "").trim();
            String image = element.getElementsByTag("img").attr("src");
            String url = "https://soap2day.to" + element.getElementsByTag("a").attr("href");
            tvPopularNames.add(name);
            tvPopularImages.add(image);
            tvPopularUrls.add(url);
        }
        requireActivity().runOnUiThread(() -> adapterTvPopular.notifyDataSetChanged());

        latest = tvShows.getElementsByClass("alert").get(1).parent();
        thumbnails = latest.getElementsByClass("thumbnail");
        for (Element element : thumbnails) {
            String name = element.getElementsByTag("h5").text().replaceAll("\\[\\d+×\\d+]", "").trim();
            String image = element.getElementsByTag("img").attr("src");
            String url = "https://soap2day.to" + element.getElementsByTag("a").attr("href");
            tvLatestNames.add(name);
            tvLatestImages.add(image);
            tvLatestUrls.add(url);
        }
        requireActivity().runOnUiThread(() -> {
            adapterTvLatest.notifyDataSetChanged();
            scrollView.animate().alpha(1f).setDuration(1000);
            webView.setVisibility(View.GONE);
        });
        Log.d("FRAGMENT", "showHTML: fragment loaded");
    }
}
