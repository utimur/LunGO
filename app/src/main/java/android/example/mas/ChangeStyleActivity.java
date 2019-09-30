package android.example.mas;

import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class ChangeStyleActivity extends AppCompatActivity {
    ConstraintLayout blackStyle;
    ConstraintLayout orangeStyle;
    ConstraintLayout whiteStyle;
    LinearLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_style);
        initialisation();
        onBlackStyleClick();
        onOrangeStyleClick();
        onWhiteStyleClick();
    }

    private void onWhiteStyleClick() {
        whiteStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.setBackgroundColor(Color.WHITE);
            }
        });
    }

    private void onOrangeStyleClick() {
        orangeStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainLayout.setBackgroundColor(Color.YELLOW);
            }
        });
    }

    private void onBlackStyleClick() {
        blackStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               mainLayout.setBackgroundColor(Color.BLACK);   // Тёма, измени цвет, этот чёрный - слишком чёрный.
            }
        });
    }


    void initialisation()
    {
        blackStyle = findViewById(R.id.change_style_layout_Black);
        mainLayout = findViewById(R.id.change_Style_Main_Layout);
        orangeStyle = findViewById(R.id.change_style_layout_Orange);
        whiteStyle = findViewById(R.id.change_style_layout_White);
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
