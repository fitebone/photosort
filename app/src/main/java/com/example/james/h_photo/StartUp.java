package com.example.james.h_photo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class StartUp extends AppCompatActivity {

    private Context mContext;
    ProgressBar p;
    ImageView imageView;
    private boolean isInFront;
    Album d_fault;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_startup);
        mContext = this.getApplicationContext();

        d_fault = new Album(mContext, false, 1, "Default");
        p = findViewById(R.id.progressBar);

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInFront) {
                    //Intent intent = new Intent(mContext, AlbumList.class);
                    //mContext.startActivity(intent);
                }
            }
        });

        fakeLoading(0);
    }

    private void fakeLoading(final int progress){
        p.setVisibility(View.VISIBLE);
        p.setProgress(progress);
        if(p.getProgress() != 100) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    fakeLoading(progress + 1);
                }
            });
            thread.start();
        }else{
            p.setVisibility(View.INVISIBLE);
            if(isInFront){
                Intent intent = new Intent(this, AlbumList.class);
                this.startActivity(intent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isInFront = true;
        //fakeLoading(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
    }
}
