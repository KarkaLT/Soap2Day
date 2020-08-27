package com.karkalt.soap2day.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
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

public class SearchFragment extends Fragment {

    View view;

    ScrollView scrollView;

    TextView noResultsMovies;
    TextView noResultsTv;

    RecyclerView recyclerViewMoviesSearch;
    RecyclerView.Adapter adapterMoviesSearch;
    RecyclerView.LayoutManager managerMoviesSearch;
    RecyclerView recyclerViewTvSearch;
    RecyclerView.Adapter adapterTvSearch;
    RecyclerView.LayoutManager managerTvSearch;

    ArrayList<String> moviesSearchNames = new ArrayList<>();
    ArrayList<String> moviesSearchImages = new ArrayList<>();
    ArrayList<String> moviesSearchUrls = new ArrayList<>();
    ArrayList<String> tvSearchNames = new ArrayList<>();
    ArrayList<String> tvSearchImages = new ArrayList<>();
    ArrayList<String> tvSearchUrls = new ArrayList<>();

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view != null) {
            return view;
        }

        view = inflater.inflate(R.layout.fragment_search, container, false);

        scrollView = view.findViewById(R.id.scrollView);
        scrollView.setVisibility(View.INVISIBLE);


        noResultsMovies = view.findViewById(R.id.no_results_movies);
        noResultsTv = view.findViewById(R.id.no_results_tv);

        recyclerViewMoviesSearch = view.findViewById(R.id.recycler_movies_search);
        recyclerViewMoviesSearch.setHasFixedSize(true);
        managerMoviesSearch = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMoviesSearch.setLayoutManager(managerMoviesSearch);
        adapterMoviesSearch = new MovieAdapter(getActivity(), moviesSearchNames, moviesSearchImages, moviesSearchUrls);
        recyclerViewMoviesSearch.setAdapter(adapterMoviesSearch);

        recyclerViewTvSearch = view.findViewById(R.id.recycler_tv_search);
        recyclerViewTvSearch.setHasFixedSize(true);
        managerTvSearch = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTvSearch.setLayoutManager(managerTvSearch);
        adapterTvSearch = new MovieAdapter(getActivity(), tvSearchNames, tvSearchImages, tvSearchUrls);
        recyclerViewTvSearch.setAdapter(adapterTvSearch);

        WebView webView = new WebView(getActivity());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
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

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setIconified(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                webView.loadUrl("https://soap2day.to/search.html?keyword=" + newText);
                return false;
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

        moviesSearchNames.clear();
        moviesSearchImages.clear();
        moviesSearchUrls.clear();

        tvSearchNames.clear();
        tvSearchImages.clear();
        tvSearchUrls.clear();

        Elements elements = doc.getElementsByClass("panel");
        Element movies = elements.get(0);
        Elements thumbnails = movies.getElementsByClass("thumbnail");
        for (Element element : thumbnails) {
            String name = element.getElementsByTag("h5").text().replaceAll("\\[\\d+×\\d+]", "").trim();
            String image = element.getElementsByTag("img").attr("src");
            String url = "https://soap2day.to" + element.getElementsByTag("a").attr("href");
            moviesSearchNames.add(name);
            moviesSearchImages.add(image);
            moviesSearchUrls.add(url);
        }
        requireActivity().runOnUiThread(() -> {
            if (moviesSearchNames.size() > 0) {
                noResultsMovies.setVisibility(View.GONE);
                adapterMoviesSearch.notifyDataSetChanged();
                recyclerViewMoviesSearch.setVisibility(View.VISIBLE);
            } else {
                recyclerViewMoviesSearch.setVisibility(View.GONE);
                noResultsMovies.setVisibility(View.VISIBLE);
            }
        });

        Element tvShows = elements.get(1);
        thumbnails = tvShows.getElementsByClass("thumbnail");
        for (Element element : thumbnails) {
            String name = element.getElementsByTag("h5").text().replaceAll("\\[\\d+×\\d+]", "").trim();
            String image = element.getElementsByTag("img").attr("src");
            String url = "https://soap2day.to" + element.getElementsByTag("a").attr("href");
            tvSearchNames.add(name);
            tvSearchImages.add(image);
            tvSearchUrls.add(url);
        }

        requireActivity().runOnUiThread(() -> {
            if (tvSearchNames.size() > 0) {
                noResultsTv.setVisibility(View.GONE);
                adapterTvSearch.notifyDataSetChanged();
                recyclerViewTvSearch.setVisibility(View.VISIBLE);
            } else {
                recyclerViewTvSearch.setVisibility(View.GONE);
                noResultsTv.setVisibility(View.VISIBLE);
            }
            scrollView.setVisibility(View.VISIBLE);
        });
    }

}
