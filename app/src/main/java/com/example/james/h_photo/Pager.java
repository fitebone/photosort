package com.example.james.h_photo;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Pager extends AppCompatActivity implements PagerListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    Album album;
    ArrayList<Image> images;
    ArrayList<String> indexedImages;
    Context mContext;
    static Point dimensions;
    Toolbar toolbar;
    Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);
        //indexedImages = getIntent().getStringArrayListExtra("paths");
        toolbar = findViewById(R.id.toolbar_pager);
        setSupportActionBar(toolbar);
        album = AlbumList.AlbumHolder.getData();
        images = album.getImages();
        //TODO IDK DOES THIS LOADING TAKE TIME?
        loadIntoIndexes(images);
        if(indexedImages == null){
            indexedImages = new ArrayList<>();
        }
        mContext = this.getApplicationContext();
        dimensions = new Point();
        int position = getIntent().getIntExtra("position", 0);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(position);
        //checkPermissions();
    }

    @Override
    protected void onDestroy() {
        AlbumList.AlbumHolder.setData(album);
        super.onDestroy();
    }

    public void loadIntoIndexes(ArrayList<Image> images){
        indexedImages = new ArrayList<>();
        if(images.size() > 0){
            for(int i = 0; i < images.size(); i++){
                File image = images.get(i).getPath();
                indexedImages.add(i,image.getAbsolutePath());
            }
        }
    }

    public static class PlaceholderFragment extends Fragment {
        String filePath;

        public PlaceholderFragment() {
        }

        PlaceholderFragment newInstance(String path) {
            filePath = path;
            Bundle b = new Bundle();
            b.putString("path", path);
            this.setArguments(b);
            return this;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_pager, container, false);
            TouchImageView imageView = view.findViewById(R.id.pagerImage);
            String path = getArguments().getString("path");
            File file = new File(path);
            int width = 0, height = 0;
            boolean resize = false;
            //TODO Make a resize limiter or just build your own cache system

            if(resize){
                Glide.with(super.getActivity()).load(path).override(400, 200).into(imageView);
            }else{
                Glide.with(super.getActivity()).load(path).crossFade().into(imageView);
            }

            return view;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_pager, menu);
        MenuItem showTags = menu.findItem(R.id.showTags);
        MenuItem addTag = menu.findItem(R.id.addTag);
        showTags.setVisible(true);
        addTag.setVisible(true);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.showTags:
                ShowTagsDialogFragment showTagsFragment = new ShowTagsDialogFragment();
                showTagsFragment.show(getSupportFragmentManager(), null);
                return true;
            case R.id.addTag:
                AddTagDialogFragment addTagFragment = new AddTagDialogFragment();
                addTagFragment.show(getSupportFragmentManager(), null);
                return true;
            case R.id.renameImage:
                return true;
            case R.id.deleteImage:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public ArrayList<String> showTags() {
        SQLiteDatabase database = new SQLiteHelper(this).getReadableDatabase();
        Cursor cursor = null;
        int position = mViewPager.getCurrentItem();
        String imagePath = album.getImages().get(position).getPath().toString();
        String rawTags;
        ArrayList<String> splitTags = new ArrayList<>();
        try{
            cursor = database.rawQuery("SELECT Tags FROM ImageTagData WHERE ImagePath=?", new String[] {imagePath});
            if(cursor.moveToFirst()){
                rawTags = cursor.getString(cursor.getColumnIndex("Tags"));
                String[] temp = rawTags.split(" ");
                splitTags.addAll(Arrays.asList(temp));
            }else{
                splitTags.add("Image has no tags");
            }
        }finally {
            cursor.close();
        }
        return splitTags;
    }

    public boolean addTagToDB(String tagName){
        boolean tagCreated = false;
        long newRowID = 0;
        int numRowAffected = 0;
        SQLiteDatabase database = new SQLiteHelper(this).getWritableDatabase();
        Cursor cursor = null;

        ContentValues values = new ContentValues();
        int position = mViewPager.getCurrentItem();
        String imagePath = album.getImages().get(position).getPath().toString();
        try{
            cursor = database.rawQuery("SELECT Tags FROM ImageTagData WHERE ImagePath=?", new String[]{imagePath});
            if(cursor.moveToFirst()){
                String existingTags = cursor.getString(cursor.getColumnIndex("Tags"));
                String newTags = existingTags + " " + tagName;
                values.put(TagDB.Tags.COLUMN_PATH, imagePath);
                values.put(TagDB.Tags.COLUMN_TAGS, newTags);
                numRowAffected = database.update(TagDB.Tags.TABLE_NAME, values,"Tags=?", new String[]{existingTags});
                if(numRowAffected > 0){
                    Toast.makeText(this, "Image's tags have been updated", Toast.LENGTH_LONG).show();
                }
            }else{
                values.put(TagDB.Tags.COLUMN_PATH, imagePath);
                values.put(TagDB.Tags.COLUMN_TAGS, tagName);
                newRowID = database.insert(TagDB.Tags.TABLE_NAME, null, values);
                Toast.makeText(this, "This is image number " + newRowID + " with a tag", Toast.LENGTH_LONG).show();
            }
        }finally {
            cursor.close();
        }

        if(newRowID > 0 || numRowAffected > 0){
            tagCreated = true;
        }
        return tagCreated;
    }

    public static class ShowTagsDialogFragment extends DialogFragment {
        private PagerListener listener;

        public void onAttach(Context context) {
            super.onAttach(getContext());
            try {
                listener = (PagerListener) context;
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
            View showTagsView = inflater.inflate(R.layout.dialog_show_tags, null);
            builder.setMessage("Tags")
                    .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .setView(showTagsView);
            //TextView textView = showTagsView.findViewById(R.id.list_of_tags);
            ArrayList<String> tags = listener.showTags();
            final ListView listOfButtons = showTagsView.findViewById(R.id.pager_tag_list);
            listOfButtons.setAdapter(new PagerListAdapter(getContext(), R.layout.pager_textview, tags));
            listOfButtons.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Delete fragment maybe idk
                    Toast.makeText(getContext(), "Delete tag: " + listOfButtons.getAdapter().getItem(position) , Toast.LENGTH_LONG).show();
                }
            });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class AddTagDialogFragment extends DialogFragment {
        private PagerListener listener;

        public void onAttach(Context context) {
            super.onAttach(getContext());
            try {
                listener = (PagerListener) context;
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
            //TODO Make this layout more ambiguous ----------------\/
            final View addTagView = inflater.inflate(R.layout.dialog_rename_album, null);
            builder.setMessage("Add new tag to image")
                    .setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditText newName = addTagView.findViewById(R.id.editTextAlbumName);
                            String name = newName.getText().toString();
                            boolean renamed = listener.addTagToDB(name);
                            if(renamed){
                                Log.d("TAG","Tag added to image, SUCCESS");
                            }else{
                                Log.d("TAG", "Tag not added, FAILED");
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    })
                    .setView(addTagView);
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class above).
            PlaceholderFragment phf = new PlaceholderFragment();
            return phf.newInstance(indexedImages.get(position));
        }

        @Override
        public int getCount() {
            return indexedImages.size();
        }
    }

    //dont know if this affects performance, IT PROBABLY DOES IDIOT
    /*
    public static class ImageLoadTask extends AsyncTask<File, Void, Point>{
        private WeakReference<View> imageViewReference;
        private int bWidth;
        private int bHeight;
        ImageLoadTask(View v){
            this.imageViewReference = new WeakReference<>(v);
        }

        @Override
        protected Point doInBackground(File... files) {
            File image = files[0];
            Point resized = new Point();
            dimensions.x = imageViewReference.get().getWidth();
            dimensions.y = imageViewReference.get().getHeight();
            //BitmapFactory.Options options = decodeSampledBitmap(image, dimensions.x, dimensions.y);
            //Bitmap help = BitmapFactory.decodeFile(image.getAbsolutePath(), options);
            //bWidth = options.outWidth;
            //bHeight = options.outHeight;
            //if(bitmap != null) {
                /*int width = imageViewReference.get().getWidth();
                int height = imageViewReference.get().getHeight();
                bWidth = bitmap.getWidth();
                bHeight = bitmap.getHeight();
                if (bWidth > width || bHeight > height) {
                    while (bWidth > width) {
                        bWidth /= 2;
                    }
                    while (bHeight > height) {
                        bHeight /= 2;
                    }
                }
                bitmap.recycle();
            }
            getDimensions(resized);
            return resized;
        }

        protected void onPostExecute(Point point){
            updatePoint(point);
        }

        private void getDimensions(Point p){
            p.x = bWidth;
            p.y = bHeight;
        }
    }

    public static void updatePoint(Point p){
        dimensions.x = p.x;
        dimensions.y = p.y;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static BitmapFactory.Options decodeSampledBitmap(File file, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            // First decode with inJustDecodeBounds=true to check dimensions
            File shit = new File(file.getParent());
            File[] f = shit.listFiles();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            //options.inJustDecodeBounds = false;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);
        }catch (FileNotFoundException e) {}
        return options;
    }

    public void checkPermissions(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
        }
    }*/
}
