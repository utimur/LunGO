package android.example.mas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;

import static android.example.mas.MainActivity.APP_PREFERENCES;

public class ImageIncreasing extends AppCompatActivity {

    SharedPreferences mSettings;
    DatabaseReference myUserDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_increasing);
        //Initialisation
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        ImageView imageIncrease = findViewById(R.id.imageIncrease);
        TextView textDate = findViewById(R.id.timeInImageIncreasing);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        myUserDB = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());


        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        String date = intent.getStringExtra("date");
        //Log.d("Zheka", "path == " + path);
        File file = new File (path);
        Glide .with(this)
                .load(file)
                .into(imageIncrease);
        textDate.setText(date);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
                    myUserDB.child("status").setValue("online");

                } else {
                    myUserDB.child("status").setValue("");
                }
            }
        }).start();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
            myUserDB.child("status").setValue(DateTime.getDateTime());
        } else {
            myUserDB.child("status").setValue("");
        }
    }
}
