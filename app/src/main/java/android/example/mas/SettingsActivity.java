package android.example.mas;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

public class SettingsActivity extends AppCompatActivity {

    // Констранты
    private final static int CHANGE_NAME_REQUEST = 1;
    // Создание layouts
    ConstraintLayout changeName;
    ConstraintLayout changePass;
    ConstraintLayout changeStyle;
    ConstraintLayout privateSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initialization();

        // Слушатели
        changeNameClick();
        changePassClick();
        changeStyleClick();
        privateSettingsClick();
    }

    private void changeStyleClick() {
        changeStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this,ChangeStyleActivity.class);
                startActivity(intent);
            }
        });
    }

    private void changeNameClick()
    {
        changeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ChangeNameActivity.class);
                startActivity(intent);

            }
        });
    }

    private void changePassClick()
    {
        changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, ChangePassActivity.class);
                startActivity(intent);

            }
        });
    }

    private void privateSettingsClick() {
        privateSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, PrivateSettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    private void initialization()
    {
        changeName = findViewById(R.id.change_name_layout);
        changePass = findViewById(R.id.change_pass_layout);
        changeStyle = findViewById(R.id.change_style_layout);
        privateSettings = findViewById(R.id.private_settings);

    }

    //@Override
    protected void onStop() {
        super.onStop();
        Log.d("TimLog", "сработал стоп Settings");

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
