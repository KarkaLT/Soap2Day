package com.karkalt.soap2day.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Episode implements Parcelable {

    @SerializedName("episode_name")
    public String episodeName;
    @SerializedName("episode_url")
    public String episodeUrl;
    @SerializedName("episode_video_url")
    public String episodeVideoUrl;
    @SerializedName("episode_number")
    public int episodeNumber;
    @SerializedName("season_number")
    public int seasonNumber;
    @SerializedName("episode_size")
    public Integer episodeSize;
    @SerializedName("episode_watched")
    public boolean episodeWatched;

    public Episode(String episodeName, String episodeUrl, int episodeNumber, int seasonNumber, boolean episodeWatched) {
        this.episodeName = episodeName;
        this.episodeUrl = episodeUrl;
        this.seasonNumber = seasonNumber;
        this.episodeNumber = episodeNumber;
        this.episodeWatched = episodeWatched;
    }

    protected Episode(Parcel in) {
        episodeName = in.readString();
        episodeUrl = in.readString();
        seasonNumber = in.readInt();
        episodeNumber = in.readInt();
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel in) {
            return new Episode(in);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(episodeName);
        dest.writeString(episodeUrl);
        dest.writeInt(seasonNumber);
        dest.writeInt(episodeNumber);
        dest.writeByte((byte) (episodeWatched ? 1 : 0));
    }

    public void setEpisodeName(String episodeName) {
        this.episodeName = episodeName;
    }

    public String getEpisodeName() {
        return episodeName;
    }

    public void setEpisodeUrl(String episodeUrl) {
        this.episodeUrl = episodeUrl;
    }

    public String getEpisodeUrl() {
        return episodeUrl;
    }

    public void setEpisodeVideoUrl(String episodeVideoUrl) {
        this.episodeVideoUrl = episodeVideoUrl;
    }

    public String getEpisodeVideoUrl() {
        return episodeVideoUrl;
    }

    public void setEpisodeNumber(int episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setEpisodeSize(Integer episodeSize) {
        this.episodeSize = episodeSize;
    }

    public Integer getEpisodeSize() {
        return episodeSize;
    }

    public void setEpisodeWatched(boolean episodeWatched) {
        this.episodeWatched = episodeWatched;
    }

    public boolean isEpisodeWatched() {
        return episodeWatched;
    }
}


