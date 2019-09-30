package android.example.mas;

import android.content.Intent;
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

public class ChangeGroupNameActivity extends AppCompatActivity {

    EditText changeNameET;
    Button changeNameBT;
    private final static int CHANGE_NAME_REQUEST = 1;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;

    User chatUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_group_name);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("groupChats");
        Intent intent = getIntent();
        chatUser = intent.getParcelableExtra("userchat");


        changeNameBT = findViewById(R.id.change_group_name_button);
        changeNameET = findViewById(R.id.change_group_name_edittext);


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
                    myRef.child(chatUser.getId()).child("name").setValue(str);
                    ChatActivity.friendNickname.setText(str);
                    ChatActivity.friendUser.setUsername(str);
                    finish();
                }
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();

        if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
            MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
        } else {
            MainActivity.myUserDB.child("status").setValue("");
        }
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
}
