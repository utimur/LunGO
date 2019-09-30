package android.example.mas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangeNameActivity extends AppCompatActivity {

    EditText changeNameET;
    Button changeNameBT;
    private final static int CHANGE_NAME_REQUEST = 1;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_name);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        changeNameBT = findViewById(R.id.change_name_button);
        changeNameET = findViewById(R.id.change_name_edittext);


        changeNameButtonClick();
    }

    private void changeNameButtonClick()
    {
        changeNameBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changeNameET.getText().length() > 1 && changeNameET.getText().length() < 20)
                {
                    String str = changeNameET.getText().toString();
                    myRef.child(user.getUid()).child("username").setValue(str);
                    SharedPreferences mSettings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("username",str);
                    editor.apply();
                    MainActivity.myNicknameLabel.setText(str);
                    finish();
                }
            }
        });
    }
    protected void onStart() {
        super.onStart();

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
                    MainActivity.myUserDB.child("status").setValue("online");

                } else {
                    MainActivity.myUserDB.child("status").setValue("");
                }
            }
        }).start();

    }
    @Override
    protected void onPause() {
        super.onPause();

        if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
            MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
        } else {
            MainActivity.myUserDB.child("status").setValue("");
        }}
}
