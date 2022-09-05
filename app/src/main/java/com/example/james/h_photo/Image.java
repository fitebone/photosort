package com.example.james.h_photo;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Image implements Parcelable {

    File album;
    String name;
    List<String> tags;

    public Image(File album, String name){
        this.album = album;
        this.name = name;
        tags = new ArrayList<>();
        tags.add(album.getName());
    }

    public Image(File path){
        this.album = path.getParentFile();
        this.name = path.getName();
        tags = new ArrayList<>();
        tags.add(album.getName());
    }

    protected Image(Parcel in) {
        album = new File(in.readString());
        name = in.readString();
        tags = in.createStringArrayList();
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    public File getPath(){
        return new File(album, name);
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setAlbum(File album){
        this.album = album;
    }

    public File getAlbum() {
        return album;
    }

    public void addTag(String tag){
        tags.add(tag);
    }

    public void removeTag(String tag){
        tags.remove(tag);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(album.getAbsolutePath());
        parcel.writeString(name);
        parcel.writeStringList(tags);
    }
}
