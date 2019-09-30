package android.example.mas;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChangePassActivity extends AppCompatActivity {

    EditText changePassET;
    Button changePassBT;
    private final static int CHANGE_Pass_REQUEST = 1;

    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        changePassBT = findViewById(R.id.change_pass_button);
        changePassET = findViewById(R.id.change_pass_edittext);


        changePassButtonClick();
    }

    private void changePassButtonClick()
    {
        changePassBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(changePassET.getText().length() > 5 && changePassET.getText().length() < 20)
                {
                    String str = changePassET.getText().toString();
                    user.updatePassword(str).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful())
                            {
                                Log.d("TimLog", "Все шик");
                            }
                            else Log.d("TimLog", "Все bad");
                        }
                    });
                    finish();
                }
                else
                {
                    Toast.makeText(ChangePassActivity.this, "Слишком короткий пароль", Toast.LENGTH_SHORT).show();
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
