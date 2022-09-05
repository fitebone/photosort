package com.example.james.h_photo;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Album implements Parcelable {

    private File path;
    //boolean hidden;
    //int location;
    //int STATE; //remove
    //String name;
    List<Image> images;
    Context context; //remove

    Album(Context context, boolean hidden, int location, String name){
        //this.hidden = hidden;
        //this.location = location;
        //this.name = name;
        this.context = context;
        images = new ArrayList<>();
        //STATE = 0;
        setPath(hidden, location, name);
        //tagCheck();
        setImages();
    }

    //REMOVE
    protected Album(Parcel in) {
        path = new File(in.readString());
        //hidden = in.readByte() != 0;
        //location = in.readInt();
        //name = in.readString();
        images = new ArrayList<>();
        //STATE = 0;
        in.readTypedList(images, Image.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Override
        public Album createFromParcel(Parcel in) {
            return new Album(in);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };

    //TODO Switch with SQLite database
    private void tagCheck(){
        File tagFile = new File(path, "tags.txt");
        try {
            boolean created = tagFile.createNewFile();
            if(created){
                Log.d("TAG", "Tag file creation SUCCESS");
            }else{
                Log.d("TAG", "Tag file already exists");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void setPath(boolean hidden, int location, String name){
        //SD Card
        if(location == -1){
            File[] externalStorage = context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES);
            File directory = externalStorage[1];
            if(hidden){
                path = new File(directory, name);
            }else{
                path = new File(directory, name);
            }
        }
        //Emulated Storage
        else if(location == 0){
            File[] externalStorage = context.getExternalFilesDirs(Environment.DIRECTORY_PICTURES);
            File directory = externalStorage[0];
            if(hidden){
                path = new File(directory, "." + name);
            }else{
                path = new File(directory, name);
            }
        }
        //Internal Storage
        else{
            File directory = context.getFilesDir();
            if(hidden){
                path = new File(directory, "." + name);
            }else{
                path = new File(directory, name);
            }
        }
        if(!path.exists()){
            boolean created = this.path.mkdirs();
            if(created){
                Log.d("ALBUM_CREATION","SUCCESS: Album \"" + getName() + "\" created at: " + this.path.getAbsolutePath());
            }else{
                Log.d("ALBUM_CREATION","FAILURE: Album \"" + getName() + "\" failed to be created");
            }
        }else{
            Log.d("ALBUM_CREATION","SUCCESS: Album \"" + getName() + "\" already exists");
        }
    }

    void setImages(){
        images.clear();
        File[] albumImages = path.listFiles();
        for(File f: albumImages){
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path.getAbsolutePath(), options);
            if (options.outWidth != -1 && options.outHeight != -1) {
                Image image = new Image(f);
                images.add(image);
            }
            else {
                Log.d("IMAGE_ERROR", "Not an image file!");
            }
        }
    }

    //CHANGE NAME
    int getSize(){
        return images.size();
    }

    String getName(){
        return path.getName();
    }

    ArrayList<Image> getImages(){
        return (ArrayList<Image>) images;
    }

    File getPath(){
        return path;
    }

    //FIX
    boolean getHiddenState(){
        return true;
    }

    //FIX
    int getLocation(){
        return -1;
    }

    //FIX
    public void changeHidden(boolean hidden){
        File parent = this.path.getParentFile();
    }

    public boolean changeName(String name){
        File parent = path.getParentFile();
        File destination = new File(parent, name);
        path = destination;
        return path.renameTo(destination);
    }

    public boolean addImage(Image i){
        return images.add(i);
    }

    public boolean removeImage(Image i){
        return images.remove(i);
    }

    //TODO REMOVE PARCEL SHIT CUS I CAN JUST ENUM
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path.getAbsolutePath());
        //parcel.writeByte((byte) (hidden ? 1 : 0));
        //parcel.writeInt(location);
        //parcel.writeString(name);
        parcel.writeTypedList(images);
    }
}
