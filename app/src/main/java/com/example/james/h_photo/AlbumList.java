package com.example.james.h_photo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import java.io.File;
import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class AlbumList extends AppCompatActivity implements AlbumListListener {

    private ArrayList<Album> albumList;
    private RecyclerView recyclerView;
    private AlbumAdapter albumAdapter;
    private Context mContext;
    private Menu menu;
    private Toolbar toolbar;
    private int clickedPosition;
    final int CREATE_ALBUM = 100;

    enum AlbumHolder {
        INSTANCE;

        private Album album;

        public static boolean hasData() {
            return INSTANCE.album != null;
        }

        public static void setData(final Album album) {
            INSTANCE.album = album;
        }

        public static Album getData() {
            final Album retList = INSTANCE.album;
            INSTANCE.album = null;
            return retList;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        
        setTitle("Albums");
        mContext = getApplicationContext();
        albumList = new ArrayList<>();
        recyclerView = findViewById(R.id.recycler_view);
        toolbar = findViewById(R.id.toolbar_albumlist);
        setSupportActionBar(toolbar);
        clickedPosition = 0;

        prepareAlbumListData();

        albumAdapter = new AlbumAdapter(albumList, mContext);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(albumAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(mContext, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                if(albumAdapter.getItemViewType(position) == albumAdapter.VIEW_TYPE_DEFAULT){
                    clickedPosition = position;
                    //TODO ENUMS INSTEAD OF PARCELABLE
                    Album album = albumAdapter.getAlbumList().get(position);
                    Intent intent = new Intent(mContext, ImageGrid.class);
                    //intent.putExtra("album", album);
                    AlbumHolder.setData(album);

                    startActivityForResult(intent, 123);
                }else{

                }
            }

            @Override
            public void onLongClick(View view, int position) {
                MenuItem delete = menu.findItem(R.id.action_delete);
                //MenuItem rename = menu.findItem(R.id.action_rename);
                if(albumAdapter.getItemViewType(0) == albumAdapter.VIEW_TYPE_DEFAULT){
                    albumAdapter.setViewType(albumAdapter.VIEW_TYPE_DELETE);

                    delete.setVisible(true);
                    //rename.setVisible(true);
                }else{
                    for(int i = 0; i < recyclerView.getChildCount(); i++){
                        AlbumAdapter.ViewHolderDelete v = (AlbumAdapter.ViewHolderDelete) recyclerView.findViewHolderForAdapterPosition(i);
                        v.check.setChecked(false);
                    }
                    albumAdapter.setViewType(albumAdapter.VIEW_TYPE_DEFAULT);
                    delete.setVisible(false);
                    //rename.setVisible(false);
                }
            }
        }));
    }

    @Override
    public void onBackPressed() {
        MenuItem delete = menu.findItem(R.id.action_delete);
        //MenuItem rename = menu.findItem(R.id.action_rename);
        if(albumAdapter.getItemViewType(0) == albumAdapter.VIEW_TYPE_DELETE){
            for(int i = 0; i < recyclerView.getChildCount(); i++){
                AlbumAdapter.ViewHolderDelete v = (AlbumAdapter.ViewHolderDelete) recyclerView.findViewHolderForAdapterPosition(i);
                v.check.setChecked(false);
            }
            albumAdapter.setViewType(albumAdapter.VIEW_TYPE_DEFAULT);

            delete.setVisible(false);
            //rename.setVisible(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
        }
    }

    private void prepareAlbumListData(){
        File[] externalStorage = getExternalFilesDirs(Environment.DIRECTORY_PICTURES);
        File EmulatedDir = externalStorage[0];
        File SDdir = externalStorage[1];
        File LocalDir = getFilesDir();
        Album album;
        checkPermissions();
        ArrayList<File> checkedFiles = new ArrayList<>();
        //TODO Try catch just in case permissions bug out
        //TODO TONS OF RED FILENOTFOUND AT START FIXXXXXX
        for(File f: SDdir.listFiles()){
            if(f.isDirectory() && !checkedFiles.contains(f)){
                String fileName = f.getName();
                if(fileName.startsWith(".")){
                    album = new Album(mContext,true, -1, fileName);
                }else{
                    album = new Album(mContext,false, -1, fileName);
                }
                checkedFiles.add(album.getPath());
                albumList.add(album);
            }
        }
        for(File f: EmulatedDir.listFiles()){
            if(f.isDirectory() && !checkedFiles.contains(f)){
                String fileName = f.getName();
                if(fileName.startsWith(".")){
                    album = new Album(mContext,true, 0, fileName);
                }else{
                    album = new Album(mContext,false, 0, fileName);
                }
                checkedFiles.add(album.getPath());
                albumList.add(album);
            }
        }
        for(File f: LocalDir.listFiles()){
            if(f.isDirectory() && !checkedFiles.contains(f)){
                String fileName = f.getName();
                if(fileName.startsWith(".")){
                    album = new Album(mContext,true, 1, fileName);
                }else{
                    album = new Album(mContext,false, 1, fileName);
                }
                checkedFiles.add(album.getPath());
                albumList.add(album);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_albumlist, menu);
        MenuItem delete = menu.findItem(R.id.action_delete);
        //MenuItem rename = menu.findItem(R.id.action_rename);
        MenuItem createAlbum = menu.findItem(R.id.action_create_album);
        delete.setVisible(false);
        //rename.setVisible(false);
        createAlbum.setVisible(true);
        delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                boolean flag = false;
                ArrayList<Integer> toDelete = new ArrayList<>();
                for(int i = 0; i < albumAdapter.getItemCount(); i++){
                    AlbumAdapter.ViewHolderDelete v = (AlbumAdapter.ViewHolderDelete) recyclerView.findViewHolderForAdapterPosition(i);
                    if(v.check.isChecked()){
                        File album = albumAdapter.getAlbumList().get(i).getPath();
                        File[] albumContents = album.listFiles();
                        for(File f: albumContents){
                            f.delete();
                        }
                        flag = album.delete();
                        toDelete.add(i);
                    }
                }
                for(int i = toDelete.size()-1; i >= 0; i--){
                    albumAdapter.removeFromAlbumList(toDelete.get(i));
                    //albumAdapter.notifyItemRemoved(i);
                }
                albumAdapter.trimAlbumList();
                if(flag){
                    for(int i = 0; i < albumAdapter.getItemCount(); i++){
                        AlbumAdapter.ViewHolderDelete v = (AlbumAdapter.ViewHolderDelete) recyclerView.findViewHolderForAdapterPosition(i);
                        v.check.setChecked(false);
                    }
                    albumAdapter.setViewType(albumAdapter.VIEW_TYPE_DEFAULT);
                    MenuItem delete = AlbumList.this.menu.findItem(R.id.action_delete);
                    //MenuItem rename = AlbumList.this.menu.findItem(R.id.action_rename);
                    delete.setVisible(false);
                    //rename.setVisible(false);
                    albumAdapter.notifyDataSetChanged();
                }
                return false;
            }
        });
        createAlbum.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CreateAlbumDialogFragment createAlbumFragment = new CreateAlbumDialogFragment();
                createAlbumFragment.show(getSupportFragmentManager(),null);
                return false;
            }
        });
        this.menu = menu;
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                //Album updatedAlbum = data.getParcelableExtra("updatedAlbum");
                Album updatedAlbum = AlbumHolder.getData();
                albumAdapter.setToAlbumList(clickedPosition, updatedAlbum);
                albumAdapter.notifyItemChanged(clickedPosition);
                albumAdapter.notifyDataSetChanged();
            }
        }
        /*
        if (requestCode == CREATE_ALBUM) {
            if (resultCode == RESULT_OK) {
                boolean hidden = data.getBooleanExtra("hidden", false);
                int location = data.getIntExtra("location", 1);
                String name = data.getStringExtra("name");

                Album newAlbum = new Album(mContext, hidden, location, name);
                albumList.add(newAlbum);
            }
        }*/
    }

    @Override
    public void onFinishAddAlbum(boolean hidden, int location, String name) {
        Album newAlbum = new Album(mContext, hidden, location, name);
        //albumList.add(newAlbum);
        albumAdapter.addToAlbumList(newAlbum);
        albumAdapter.notifyDataSetChanged();
    }

    public static class CreateAlbumDialogFragment extends DialogFragment{

        private AlbumListListener listener;

        public void onAttach(Context context) {
            super.onAttach(getContext());
            try {
                listener = (AlbumListListener) context;
            }
            catch (ClassCastException e) {
                Log.d("MyDialog", "Activity doesn't implement the interface");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View createAlbumView = inflater.inflate(R.layout.dialog_add_album, null);
            builder.setTitle("Add New Album")
                    .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Switch switchHidden = createAlbumView.findViewById(R.id.switchHidden);
                            RadioGroup radioLocationGroup = createAlbumView.findViewById(R.id.radioLocationGroup);
                            RadioButton locationChosen = createAlbumView.findViewById(radioLocationGroup.getCheckedRadioButtonId());
                            EditText albumName = createAlbumView.findViewById(R.id.editTextAlbumName);

                            Boolean hidden = switchHidden.isChecked();
                            String locationName = locationChosen.getText().toString();
                            int location;
                            if(locationName.equals("Emulated")){
                                location = 0;
                            }else if(locationName.equals("SD")){
                                location = -1;
                            }else{
                                location = 1;
                            }
                            String name = albumName.getText().toString();

                            listener.onFinishAddAlbum(hidden, location, name);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .setView(createAlbumView);
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
}
