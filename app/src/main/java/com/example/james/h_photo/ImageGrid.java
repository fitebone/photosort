package com.example.james.h_photo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;

public class ImageGrid extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,ImageGridListener {

    private int REQUEST_IMPORT = 1;
    private int REQUEST_TRANSFER = 2;
    private String selectedImagePath;
    private int screenWidth;
    private GridView grid;
    private DrawerLayout drawerLayout;
    private ImageAdapter adapter;
    private Context mContext;
    private Album album;
    private Intent gridToList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mContext = this.getApplicationContext();
        drawerLayout = findViewById(R.id.drawer_layout);
        //album = getIntent().getParcelableExtra("album");
        album = AlbumList.AlbumHolder.getData();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        screenWidth = size.x;
        //TODO SET GRIDVIEW SPACING PROGRAMMATICALLY

        grid = findViewById(R.id.grid);
        adapter = new ImageAdapter(this, album, screenWidth);
        grid.setAdapter(adapter);
    }

    protected  void onResume(){
        super.onResume();
        //prefs = getSharedPreferences("pref_settings.xml", MODE_PRIVATE);
        //spl.onSharedPreferenceChanged(prefs, "hidden_or_not");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            gridToList = new Intent();
            //gridToList.putExtra("updatedAlbum", album);
            AlbumList.AlbumHolder.setData(album);
            setResult(RESULT_OK, gridToList);
            finish();
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_import) {
            checkPermissions();
            importFolder(false);
        } else if (id == R.id.nav_transfer) {
            checkPermissions();
            importFolder(true);
        } else if (id == R.id.nav_delete) {
            deleteFiles(); //KILLED THIS
            adapter.refresh(false);
        } else if(id == R.id.nav_randomize){
            adapter.randomize();
            adapter.refresh(true);
        } else if(id == R.id.nav_rename){
            RenameDialogFragment renameFragment = new RenameDialogFragment();
            renameFragment.show(getSupportFragmentManager(), null);
        } else if (id == R.id.nav_tags) {

        } else if (id == R.id.nav_settings) {
            //Intent openSettings = new Intent(this, Settings.class);
            //openSettings.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, Settings.SettingsFragment.class.getName());
            //openSettings.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
            //this.startActivity(openSettings);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void importFolder(boolean transfer){
        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://media/internal/images/media"));
        intent.setType("image/*");
        if(transfer){
            startActivityForResult(Intent.createChooser(intent, "Select Image in the Desired Album for Transfer"), REQUEST_TRANSFER);
        }else {
            startActivityForResult(Intent.createChooser(intent, "Select Image in the Desired Album for Transfer"), REQUEST_IMPORT);
        }
    }

    private void deleteFiles(){
        //YEET
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMPORT) {
                Uri selectedImageUri = data.getData();

                selectedImagePath = getPath(selectedImageUri);

                if (selectedImagePath != null) {
                    File childImage = new File(selectedImagePath);
                    File parent = new File(childImage.getParent());

                    FloatingLoad fl = new FloatingLoad(false);
                    File[] params = new File[2];
                    params[0] = parent;
                    params[1] = new File(album.getPath().getAbsolutePath());
                    fl.execute(params);
                }
            }
            if(requestCode == REQUEST_TRANSFER){
                Uri selectedImageUri = data.getData();

                // OI FILE Manager
                //fileManagerString = selectedImageUri.getPath();
                // MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);

                if (selectedImagePath != null) {
                    File childImage = new File(selectedImagePath);
                    File parent = new File(childImage.getParent());
                    FloatingLoad fl = new FloatingLoad(true);
                    File[] params = new File[2];
                    params[0] = parent;
                    params[1] = new File(album.getPath().getAbsolutePath());
                    fl.execute(params);
                }
            }
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        } else
            return null;
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
        }
        FileChannel source;
        FileChannel destination;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
    }

    @Override
    public boolean renameAlbum(String name) {
        return album.changeName(name);
    }

    public static class RenameDialogFragment extends DialogFragment {
        private ImageGridListener listener;

        public void onAttach(Context context) {
            super.onAttach(getContext());
            try {
                listener = (ImageGridListener) context;
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
            final View renameAlbumView = inflater.inflate(R.layout.dialog_rename_album, null);
            builder.setMessage("Enter new album name")
                    .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditText newName = renameAlbumView.findViewById(R.id.editTextAlbumName);
                            String name = newName.getText().toString();
                            boolean renamed = listener.renameAlbum(name);
                            if(renamed){
                                Log.d("RENAME_ALBUM","Album renaming SUCCESS");
                            }else{
                                Log.d("RENAME_ALBUM", "Album renaming FAILED");
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .setView(renameAlbumView);
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class FloatingLoad extends AsyncTask <File, Void, Void> {
        boolean delete;
        ProgressBar progressBar;
        int progress;
        View view;
        FrameLayout rootView;

        FloatingLoad(boolean delete){
            this.delete = delete;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            rootView = findViewById(R.id.grid_frame);
            view = getLayoutInflater().inflate(R.layout.progress_grid, null);
            rootView.addView(view);
            progressBar = view.findViewById(R.id.gridProgressBar);
            progress = 0;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            rootView.removeView(view);
            album.setImages();
            adapter.refresh(false);
        }

        @Override
        protected Void doInBackground(File... params) {
            File dir = params[0];
            File album = params[1];
            File[] transferContents = dir.listFiles();
            progressBar.setMax(transferContents.length);
            progressBar.setProgress(progress);
            for(File f: transferContents){
                File newImage = new File(album, f.getName());
                progress++;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                //TODO remove bitmap var?
                Bitmap bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(), options);
                if(options.outWidth != -1 && options.outHeight != -1){
                    try {
                        copyFile(f, newImage);
                        final File scan_and_delete = f;
                        if(delete){
                            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{f.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String s, Uri uri) {
                                    if (uri != null) {
                                        getApplicationContext().getContentResolver().delete(uri, null,
                                                null);
                                        scan_and_delete.delete();
                                    }
                                }
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                progressBar.setProgress(progress);
            }
            return null;
        }
    }
}
