package android.example.mas;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.File;
import java.util.Random;

public class MyFirebaseMessagingServiceNotification extends FirebaseMessagingService {

    private final String ADMIN_CHANNEL_ID ="admin_channel";
    private static final String TAG = "mFirebaseIIDService";
    private static final String SUBSCRIBE_TO = "userABC";

    static final String NOTIFICATION_ID = "Notification_ID";
    static final String NOTIFICATION_IS_GROUP_CHAT = "Notification_IsGroupChat";
    static final String NOTIFICATION_USERNAME = "Notification_Username";
    static final String NOTIFICATION_AVATAR_PATH = "Notification_AvatarPath";

   // SharedPreferences mSettings;

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.e("Refreshed token:",token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String msgText = remoteMessage.getData().get("message");
        String userID;
        String currentMessage;
        User notificationUser = new User();

        if (msgText.length() < 46)
        {
            userID = msgText.substring(0, 28);
            currentMessage = msgText.substring(28);
            notificationUser.setIsGroupChat("0");
        }
        else if ((!msgText.substring(36,45).equals("groupchat"))) {
             userID = msgText.substring(0, 28);
             currentMessage = msgText.substring(28);
            notificationUser.setIsGroupChat("0");
        }
        else
        {
             userID = msgText.substring(0, 45);
             Log.d("GroupChatsLog", userID);
             currentMessage = msgText.substring(45);
            notificationUser.setIsGroupChat("1");
        }
        String userName = remoteMessage.getData().get("title");
        //    msg.setMessage(msgText.substring(33));
        //    msg.setTime(msgText.substring(28,33));
        //    msg.setId(msgText.substring(0,28));
        // Log.d("Zheka", msgText.substring(38));



        Intent intentChat = new Intent(this,ChatActivity.class);
        intentChat.putExtra("id", userID);
        Log.d("Zheka","id for Intent = " + userID);



        notificationUser.setId(userID);
        if (notificationUser.getIsGroupChat().equals("0"))
        notificationUser.setUsername(userName);
        else
        {
            String[] searchNameOfGroupChat = userName.split("\\(");
            String groupUsername = searchNameOfGroupChat[searchNameOfGroupChat.length-1];
            notificationUser.setUsername(groupUsername.substring(0,groupUsername.length()-1));
        //    notificationUser.setUsername(searchNameOfGroupChat[searchNameOfGroupChat.length-1].substring(0,searchNameOfGroupChat.length-2));
        }
        String pathForGroupChat = Environment.getExternalStorageDirectory() + "/Android/data/" + getPackageName() + "/" + userID + "/" + remoteMessage.getData().get("avatarPath");
        notificationUser.setAvatarPuth(pathForGroupChat);
        if (pathForGroupChat.equals(""))
            pathForGroupChat = "nothingAvatar";
        Log.d("Zheka","pathOfAvatarInNotification = " + pathForGroupChat);


      //  mSettings = getSharedPreferences(MainActivity.APP_PREFERENCES,Context.MODE_PRIVATE);
      //  SharedPreferences.Editor editor = mSettings.edit();
      //  editor.putString(NOTIFICATION_IS_GROUP_CHAT,notificationUser.getIsGroupChat());
      //  editor.putString(NOTIFICATION_ID,notificationUser.getId());
      //  editor.putString(NOTIFICATION_USERNAME,notificationUser.getUsername());
      //  editor.putString(NOTIFICATION_AVATAR_PATH,notificationUser.getAvatarPuth());
      //  editor.apply();


        intentChat.putExtra("user",(Parcelable) notificationUser);

        PendingIntent pendIntent = PendingIntent.getActivity(this,0,intentChat,PendingIntent.FLAG_UPDATE_CURRENT);
        //    Log.d("Zheka", msgForNotification.getId());

        // PendingIntent pendIntent = PendingIntent.getBroadcast(getApplicationContext(),0, intentChat,PendingIntent.FLAG_UPDATE_CURRENT);


        //intentChat.putExtra("user", (Parcelable) msgForNotification);
        Log.d("Zheka","notification");
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"notification_id")
                .setAutoCancel(true)
                .setContentTitle(userName)
                .setContentText(currentMessage)
                .setContentIntent(pendIntent)
                .setSound(uri)
                .setDefaults(-1)
                .setSmallIcon(R.mipmap.ic_launcher);
        Bitmap mBitmap = null;
        if (new File(pathForGroupChat).exists()) {
            try {
                mBitmap = MainActivity.decodeSampledBitmapFromResource(pathForGroupChat, 100, 100);
                mBitmap = getCircularBitmap(mBitmap);

                // Add a border around circular bitmap
                //mBitmap = addBorderToCircularBitmap(mBitmap, 15, R.color.lungo_start);
                builder.setLargeIcon(mBitmap);   // Проверить потом на ошибку
            } catch (NullPointerException e) {

            }
        }
        Notification notification = builder.build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        return output;
    }
    protected Bitmap getCircularBitmap(Bitmap srcBitmap) {
        // Calculate the circular bitmap width with border
        int squareBitmapWidth = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());

        // Initialize a new instance of Bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);


        RectF rectF = new RectF(rect);


        canvas.drawOval(rectF, paint);


        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // Calculate the left and top of copied bitmap
        float left = (squareBitmapWidth-srcBitmap.getWidth())/2;
        float top = (squareBitmapWidth-srcBitmap.getHeight())/2;


        canvas.drawBitmap(srcBitmap, left, top, paint);

        // Free the native object associated with this bitmap.
        srcBitmap.recycle();

        // Return the circular bitmap
        return dstBitmap;
    }

    // Custom method to add a border around circular bitmap
    protected Bitmap addBorderToCircularBitmap(Bitmap srcBitmap, int borderWidth, int borderColor){
        // Calculate the circular bitmap width with border
        int dstBitmapWidth = srcBitmap.getWidth()+borderWidth*2;

        // Initialize a new Bitmap to make it bordered circular bitmap
        Bitmap dstBitmap = Bitmap.createBitmap(dstBitmapWidth,dstBitmapWidth, Bitmap.Config.ARGB_8888);

        // Initialize a new Canvas instance
        Canvas canvas = new Canvas(dstBitmap);
        // Draw source bitmap to canvas
        canvas.drawBitmap(srcBitmap, borderWidth, borderWidth, null);

        // Initialize a new Paint instance to draw border
        Paint paint = new Paint();
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        paint.setAntiAlias(true);


        canvas.drawCircle(
                canvas.getWidth() / 2, // cx
                canvas.getWidth() / 2, // cy
                canvas.getWidth()/2 - borderWidth / 2, // Radius
                paint // Paint
        );

        // Free the native object associated with this bitmap.
        srcBitmap.recycle();

        // Return the bordered circular bitmap
        return dstBitmap;
    }
}
