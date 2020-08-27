package com.karkalt.soap2day.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.karkalt.soap2day.R;
import com.karkalt.soap2day.adapters.MovieAdapter;
import com.karkalt.soap2day.managers.GridAutoFitLayoutManager;
import com.karkalt.soap2day.models.Movie;
import com.karkalt.soap2day.models.Series;
import com.karkalt.soap2day.utils.TinyDB;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class DownloadsFragment extends Fragment {

    RelativeLayout noDownloads;
    ScrollView scrollView;

    RecyclerView recyclerViewMovies;
    RecyclerView.Adapter adapterMovies;
    RecyclerView.LayoutManager managerMovies;
    RecyclerView recyclerViewTv;
    RecyclerView.Adapter adapterTv;
    RecyclerView.LayoutManager managerTv;

    ArrayList<String> moviesNames = new ArrayList<>();
    ArrayList<String> moviesImages = new ArrayList<>();
    ArrayList<String> moviesUrls = new ArrayList<>();
    ArrayList<String> tvNames = new ArrayList<>();
    ArrayList<String> tvImages = new ArrayList<>();
    ArrayList<String> tvUrls = new ArrayList<>();

    TinyDB tinyDB;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_downloads, container, false);
        tinyDB = new TinyDB(getContext());

        noDownloads = view.findViewById(R.id.no_downloads);
        scrollView = view.findViewById(R.id.scrollView);

        recyclerViewMovies = view.findViewById(R.id.recycler_movies);
        recyclerViewMovies.setHasFixedSize(true);
        managerMovies = new GridAutoFitLayoutManager(requireContext(), (int) requireActivity().getResources().getDimension(R.dimen.movie_holder));
        recyclerViewMovies.setLayoutManager(managerMovies);
        adapterMovies = new MovieAdapter(getActivity(), moviesNames, moviesImages, moviesUrls);
        recyclerViewMovies.setAdapter(adapterMovies);

        recyclerViewTv = view.findViewById(R.id.recycler_tv);
        recyclerViewTv.setHasFixedSize(true);
        managerTv = new GridAutoFitLayoutManager(requireContext(), (int) requireActivity().getResources().getDimension(R.dimen.movie_holder));
        recyclerViewTv.setLayoutManager(managerTv);
        adapterTv = new MovieAdapter(getActivity(), tvNames, tvImages, tvUrls);
        recyclerViewTv.setAdapter(adapterTv);

        moviesNames.clear();
        moviesImages.clear();
        moviesUrls.clear();
        tvNames.clear();
        tvImages.clear();
        tvUrls.clear();

        File dir = new File(requireContext().getFilesDir() + "/Soap2day/");
        if (hasDownloads(dir)) {
            dir = new File(requireContext().getFilesDir() + "/Soap2day/Movies/");
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (Objects.requireNonNull(file.listFiles()).length > 0) {
                            Movie movie = tinyDB.getObject(file.getName(), Movie.class);
                            tvNames.add(movie.getName());
                            tvImages.add(movie.getImageUrl());
                            tvUrls.add(movie.getUrl());
                        }
                    }
                }
            }

            dir = new File(requireContext().getFilesDir() + "/Soap2day/Tv shows/");
            files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        if (Objects.requireNonNull(file.listFiles()).length > 0) {
                            Series series = tinyDB.getObject(file.getName(), Series.class);
                            tvNames.add(series.getName());
                            tvImages.add(series.getImageUrl());
                            tvUrls.add(series.getUrl());
                        }
                    }
                }
            }

            adapterMovies.notifyDataSetChanged();
            adapterTv.notifyDataSetChanged();
        } else {
            scrollView.setVisibility(View.GONE);
            noDownloads.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public boolean hasDownloads(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            assert files != null;
            for (File file : files) {
                if (file.isDirectory()) {
                    if (hasDownloads(file)) {
                        return true;
                    }
                } else if (file.getPath().endsWith(".mp4")) {
                    return true;
                }
            }
        }
        return false;
    }
}
