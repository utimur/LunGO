package android.example.mas;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;


public class MyService extends FirebaseMessagingService {


 //   public MyService() {
 //     FirebaseUser currentuser = mAuth.getCurrentUser();
 //     userReference = (DatabaseReference) database.getReference().child(currentuser.getUid()).child("chats").addChildEventListener(new ChildEventListener() {
 //         @Override
 //         public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
 //             String msgText = dataSnapshot.getValue(String.class);
 //             Log.d("Zheka",msgText);
 //             String textOfMassage = msgText.substring(33);
 //             String friendUsername = userReference.child(msgText.substring(0,28)).child("username").getKey();
 //             Log.d("Zheka",friendUsername);
 //             sendNotification(textOfMassage,friendUsername);

 //         }

 //         @Override
 //         public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

 //         }

 //         @Override
 //         public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

 //         }

 //         @Override
 //         public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

 //         }

 //         @Override
 //         public void onCancelled(@NonNull DatabaseError databaseError) {

 //         }
 //     });

 //   }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("Zheka","poehali");
        sendNotification("test","test");

    }

    void sendNotification(String nameOfFriend, String messageText)
    {
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setContentTitle(nameOfFriend)
                .setContentText(messageText)
                .setSmallIcon(R.mipmap.ic_launcher);
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }

}
