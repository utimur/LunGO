package android.example.mas;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;

public class PrivateSettingsActivity extends AppCompatActivity {

    CheckBox onlineCheckBox;
    public static final String APP_PREFERENCES = "mysettings";
    public static final String ONLINE_PREFERENCE = "online";
    public  SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_settings);

        onlineCheckBox = findViewById(R.id.online_checkbox);

         settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = settings.edit();

        if (settings.getString(ONLINE_PREFERENCE, "").equals("1")) {
            onlineCheckBox.setChecked(true);
        }
            onlineCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        editor.putString(ONLINE_PREFERENCE, "1");
                        editor.apply();
                        Log.d("TimLog", settings.getString(ONLINE_PREFERENCE, ""));
                    } else {
                        editor.putString(ONLINE_PREFERENCE, "0");
                        editor.apply();
                        Log.d("TimLog", settings.getString(ONLINE_PREFERENCE, ""));

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
