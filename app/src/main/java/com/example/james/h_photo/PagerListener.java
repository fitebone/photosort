package com.example.james.h_photo;

import java.util.ArrayList;

public interface PagerListener {
    ArrayList<String> showTags();
    boolean addTagToDB(String name);
}
