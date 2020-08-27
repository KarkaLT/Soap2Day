package com.karkalt.soap2day.models;

import com.google.gson.annotations.SerializedName;

public class Movie {
    @SerializedName("story")
    public String story;
    @SerializedName("url")
    public String url;
    @SerializedName("name")
    public String name;
    @SerializedName("image_url")
    public String imageUrl;

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
