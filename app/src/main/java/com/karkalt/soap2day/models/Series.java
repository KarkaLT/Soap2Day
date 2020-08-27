package com.karkalt.soap2day.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Series {

    @SerializedName("seasons")
    public ArrayList<Season> seasons;
    @SerializedName("story")
    public String story;
    @SerializedName("url")
    public String url;
    @SerializedName("name")
    public String name;
    @SerializedName("image_url")
    public String imageUrl;

    public void setEpisodeWatched(int seasonNumber, int episodeNumber, boolean watched) {
        ArrayList<Season> seasons = this.getSeasons();
        Season season = seasons.get(seasonNumber - 1);
        ArrayList<Episode> episodes = new ArrayList<>(season.getItems());
        Episode episode = episodes.get(episodeNumber - 1);
        episode.setEpisodeWatched(watched);
        episodes.set(episodeNumber - 1, episode);
        seasons.set(seasonNumber - 1, new Season(season.getTitle(), episodes));
        this.setSeasons(seasons);
    }

    public void setSeasons(ArrayList<Season> seasons) {
        this.seasons = seasons;
    }

    public ArrayList<Season> getSeasons() {
        return seasons;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getStory() {
        return story;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

