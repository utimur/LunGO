package android.example.mas;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.constraint.Constraints.TAG;

public class MyFirebaseMessagingService extends Service {

    FirebaseAuth mAuth;
    FirebaseDatabase database;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TimLog", "Сервис сработал");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d("TimLog", "В ЦИКЛЕ");
                    if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
                        MainActivity.myUserDB.child("status").setValue("online");
                    } else {
                        MainActivity.myUserDB.child("status").setValue("");
                    }
                }
            }
        }).start();




    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Zheka", "StartCommand");
        return START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TimLog", "Сервис упал");
        if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
             MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
         }
         else {
             MainActivity.myUserDB.child("status").setValue("");
         }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d("Zheka", "TaskRemove");
        Log.d("TimLog", "Сервис тскремув");
        if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
            MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
        }
        else {
            MainActivity.myUserDB.child("status").setValue("");
        }
        super.onTaskRemoved(rootIntent);
    }

}