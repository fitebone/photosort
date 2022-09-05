package com.example.james.h_photo;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ImageAdapter extends BaseAdapter {

    //private ImageCache imageCache;
    double size;

    private Context mContext;
    //ArrayList<Image> images;
    //ArrayList<File> pathsAtIndex;
    ArrayList<String> parcelableArray;
    Album album;
    String albumPath;

    public ImageAdapter(Context c, Album album, int screenWidth){
        mContext = c;
        this.album = album;
        this.albumPath = album.getPath().getAbsolutePath();

        double columns = 3.0;
        int width = screenWidth-100;
        size = width/columns;

        updateParcelableArray(this.album.getImages());
    }

    public int getCount(){ return album.getImages().size(); }

    public Object getItem(int position){ return album.getImages().get(position).getPath(); }

    public long getItemId(int position){ return 0; }

    public View getView(final int position, View convertView, ViewGroup parent){
        final ImageView imageView;

        if(convertView == null){
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new ViewGroup.LayoutParams((int) size,(int) size));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(10,10,10,10);
        }else{
            imageView = (ImageView) convertView;
        }

        imageView.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(mContext, Pager.class);
                //intent.putStringArrayListExtra("paths", parcelableArray);
                AlbumList.AlbumHolder.setData(album);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });

        Glide.with(mContext)
                .load(album.getImages().get(position).getPath())
                .override((int)size,(int)size)
                .crossFade()
                .centerCrop()
                .into(imageView);
        return imageView;
    }

    //Orders images from a ArrayList<Image> to an ArrayList<File> that will be sent to the
    // ViewPager ordered by int index
    public void updateParcelableArray(ArrayList<Image> images){
        parcelableArray = new ArrayList<>();
        if(images.size() > 0){
            for(int i = 0; i < images.size(); i++){
                File image = images.get(i).getPath();
                parcelableArray.add(i,image.getAbsolutePath());
            }
        }
    }

    public void refresh(boolean random){
        if(!random){
            album.setImages();
        }
        updateParcelableArray(album.getImages());
        notifyDataSetChanged();
    }

    public void randomize(){
        /*int index;
        Random random = new Random();
        Image temp;
        for (int i = images.size()-1; i >= 0; i--)
        {
            index = random.nextInt(i + 1);
            temp = images.get(index);
            images.add(index, images.get(i));
            images.add(i, temp);
        }*/
        //Random random = new Random();
        Collections.shuffle(album.getImages(), new Random());
    }

    /*
    private Map<Integer, File> indexImages(File imageDir){
        Map<Integer, File> map = new TreeMap<>();
        File[] images = imageDir.listFiles();
        for(int i = 0; i < images.length; i++){
            if(images[i] != null){
                map.put(i, images[i]);
            }
        }
        return map;
    }


    //TO DO
    public class ImageCache extends LruCache<String, Bitmap> {

        public ImageCache(int maxSize) {
            super(maxSize);
        }

        public Bitmap getBitmapFromCache(String key){
            return (Bitmap) get(key);
        }

        public void addBitmapToCache(String key, Bitmap bitmap){
            if(getBitmapFromCache(key) == null){
                this.put(key, bitmap);
            }
        }

        public void getNewBitmap(File file, ImageView imageView){
            ImageLoadTask task = new ImageLoadTask(imageView);
            task.execute(file);
        }

        protected int sizeOf(String key) {
            return this.get(key).getByteCount() / 1024;
        }

        public class ImageLoadTask extends AsyncTask<File, Void, Bitmap> {
            private ImageView view;
            private File file;

            public ImageLoadTask(ImageView view){
                this.view = view;
            }
            @Override
            protected Bitmap doInBackground(File... images) {
                file = images[0];
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                imageCache.addBitmapToCache(file.getName(), bitmap);
                return bitmap;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap){
                if(bitmap != null){
                    if(view != null){
                        //decodeSampledBitmap(file, size, size);
                        view.setImageBitmap(bitmap);
                    }
                }
            }

        }
    }

    public void setIsDisplayed(boolean isDisplayed, Bitmap bm) {
        synchronized (this) {
            if (isDisplayed) {
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            } else {
                mDisplayRefCount--;
            }
        }
        // Check to see if recycle() can be called.
        checkState(bm);
    }

    // Notify the drawable that the cache state has changed.
    // Keep a count to determine when the drawable is no longer being cached.
    public void setIsCached(boolean isCached, Bitmap bm) {
        synchronized (this) {
            if (isCached) {
                mCacheRefCount++;
            } else {
                mCacheRefCount--;
            }
        }
        // Check to see if recycle() can be called.
        checkState(bm);
    }

    private synchronized void checkState(Bitmap bm) {
        // If the drawable cache and display ref counts = 0, and this drawable
        // has been displayed, then recycle.
        if (mCacheRefCount <= 0 && mDisplayRefCount <= 0 && mHasBeenDisplayed
                && hasValidBitmap(bm)) {
            bm.recycle();
        }
    }

    private synchronized boolean hasValidBitmap(Bitmap bm) {
        Bitmap bitmap = bm;
        return bitmap != null && !bitmap.isRecycled();
    }*/
}
