package com.example.james.h_photo;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import androidx.annotation.NonNull;

public class PagerListAdapter extends ArrayAdapter<String> {

    private Context mContext;

    public PagerListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
    }

}
