package com.example.james.h_photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Album> albumList;
    private Context mContext;
    public int VIEW_TYPE;
    public final int VIEW_TYPE_DEFAULT = 0;
    public final int VIEW_TYPE_DELETE = 1;

    public AlbumAdapter(ArrayList<Album> albumList, Context context) {
        this.albumList = albumList;
        mContext = context;
        VIEW_TYPE = VIEW_TYPE_DEFAULT;
    }

    public class ViewHolderDefault extends RecyclerView.ViewHolder {
        public ImageView preview;
        public TextView albumTitle, albumImageCount;

        public ViewHolderDefault(View view) {
            super(view);
            preview = view.findViewById(R.id.albumPreview);
            albumTitle = view.findViewById(R.id.albumTitle);
            albumImageCount = view.findViewById(R.id.albumImageCount);
        }
    }

    public class ViewHolderDelete extends RecyclerView.ViewHolder{
        public ImageView preview; //MAKE CHECK CHECKBOX
        public AppCompatCheckBox check;
        public TextView albumTitle, albumImageCount;

        public ViewHolderDelete(View view) {
            super(view);
            check = view.findViewById(R.id.albumCheck);
            //check.setTag(R.drawable.ic_general_checkempty);
            preview = view.findViewById(R.id.albumPreview);
            albumTitle = view.findViewById(R.id.albumTitle);
            albumImageCount = view.findViewById(R.id.albumImageCount);
            //setOnClicks();
        }

        public void setOnClicks(){
            check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    check.toggle();
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch(viewType){
            case VIEW_TYPE_DEFAULT:
                View view0 = inflater.inflate(R.layout.album_list_row, parent, false);
                viewHolder = new ViewHolderDefault(view0);
                break;
            case VIEW_TYPE_DELETE:
                View view1 = inflater.inflate(R.layout.album_list_row_delete, parent, false);
                viewHolder = new ViewHolderDelete(view1);
                break;
            default:
                View v = inflater.inflate(R.layout.album_list_row, parent, false);
                viewHolder = new ViewHolderDefault(v);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Album album = albumList.get(position);

        switch(getItemViewType(position)){
            case 0:
                ViewHolderDefault vh1 = (ViewHolderDefault) holder;
                vh1.preview.setImageBitmap(getPreview(album));
                vh1.albumTitle.setText(album.getName());
                vh1.albumImageCount.setText(String.valueOf(album.getSize()));
                break;
            case 1:
                ViewHolderDelete vh2 = (ViewHolderDelete) holder;
                vh2.preview.setImageBitmap(getPreview(album));
                vh2.albumTitle.setText(album.getName());
                vh2.albumImageCount.setText(String.valueOf(album.getSize()));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE;
    }

    public void setViewType(int viewType){
        this.VIEW_TYPE = viewType;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    private Bitmap getPreview(Album album){
        Bitmap preview = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        final BitmapFactory.Options decodeBoundsOptions = new BitmapFactory.Options();
        decodeBoundsOptions.inJustDecodeBounds = true;

        if(album.getImages().size() > 0){
            BitmapFactory.decodeFile(album.getImages().get(0).getPath().getAbsolutePath(), decodeBoundsOptions);
            if(decodeBoundsOptions.outWidth != -1 && decodeBoundsOptions.outHeight != -1){
                final int originalWidth = decodeBoundsOptions.outWidth;
                final int originalHeight = decodeBoundsOptions.outHeight;
                // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
                options.inSampleSize= Math.max(1,Math.min(originalWidth / 100, originalHeight / 100));
                preview = BitmapFactory.decodeFile(album.getImages().get(0).getPath().getAbsolutePath(), options);
            }
        }else{
            int resID = mContext.getResources().getIdentifier("bg" , "drawable", mContext.getPackageName());
            BitmapFactory.decodeResource(mContext.getResources(), resID, decodeBoundsOptions);
            final int originalWidth = decodeBoundsOptions.outWidth;
            final int originalHeight = decodeBoundsOptions.outHeight;
            // inSampleSize prefers multiples of 2, but we prefer to prioritize memory savings
            options.inSampleSize= Math.max(1,Math.min(originalWidth / 100, originalHeight / 100));
            Bitmap lowResPreview = BitmapFactory.decodeResource(mContext.getResources(), resID, options);
            //reconfigure with phone screen not hard code
            preview = Bitmap.createBitmap(lowResPreview, 0, 111, 263, 263);
        }
        return preview;
    }

    List<Album> getAlbumList(){
        return albumList;
    }

    void removeFromAlbumList(int position){
        albumList.remove(position);
    }

    void addToAlbumList(Album album){
        albumList.add(album);
    }

    void setToAlbumList(int position, Album album){
        albumList.set(position, album);
    }

    void trimAlbumList(){
        albumList.trimToSize();
    }

}