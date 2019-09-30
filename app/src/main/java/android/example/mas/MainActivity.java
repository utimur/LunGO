package android.example.mas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Parcelable;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity  {
//

    static final int GALLERY_REQUEST = 1;
    static final int STORAGE_REQUEST_CODE = 123;
    public static final int CHAT_TAB_POSITION = 0;
    public static final int CONTACTS_TAB_POSITION = 1;


    // Обьявление элементов
    User myUser;
    // БД
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;
    DatabaseReference contactsDB ;
    static DatabaseReference myUserDB;
    StorageReference mStorageRef;
    DatabaseReference groupChatsRef;
    DataBase myDataBase;
    // VIEW элементы
    static TextView statusLabel;
    static TextView myNicknameLabel;
    static FloatingActionButton fab;
    static LinearLayout listViewLayout;
    static LinearLayout highMainPanel;
    static ConstraintLayout clMainPanel;
    static Button settings;
    static ConstraintLayout main;
    ImageView avatarImageView;

    public static SharedPreferences mSettings;
    public static final String ONLINE_PREFERENCE = "online";
    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_TIME = "time";
    public static final String APP_PREFERENCES_DifferenceInTime = "differenceInTime";



    public static TabLayout tabLayout;
    ViewPager viewPager;
    PagerSliderAdapter pagerSliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialization();
        viewPagerInit();
        hasPermissions();

        //startService(new Intent(MainActivity.this,MyFirebaseMessagingService.class));

        // Установка имени пользователя
        setNameToImageView();



        // Запуск потока сортировки

        try {
            downloadAvatar();
        } catch (IOException e) {
            e.printStackTrace();
        }





        Intent intentUser = getIntent();
        settings = (Button)findViewById(R.id.settings_btn);
        main = (ConstraintLayout)findViewById(R.id.main_background);
        myUser = (User) intentUser.getParcelableExtra("user");
        //  statusLabel.setText(myUser.getStatus());
        statusLabel.setText("Online");



        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        avatarImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarClick();
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AllContactsActivity.class);
                startActivity(intent);
            }
        });



       // startService(new Intent(getBaseContext(), MyFirebaseMessagingService.class));

    }


    // Функция выхода из аккаунта
    public void logOut()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("Zheka","ErrorOfExit");
                }
                finally {
                    myUserDB.child("status").setValue(DateTime.getDateTime());
                    mAuth.signOut();
                    Intent intExit = new Intent(MainActivity.this, SignInActivity.class);
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("username","");
                    editor.apply();
                    editor.putString(ONLINE_PREFERENCE, "0");
                    startActivity(intExit);
                    finish();


                }
            }
        }).start();
    }




    // Кнопка выхода
    private static long back_pressed;
    @Override
    public void onBackPressed() {

        if (back_pressed + 2000 > System.currentTimeMillis())
        {
            appExit();
        }
        else
            Toast.makeText(getBaseContext(), "Press once again to exit!",
                    Toast.LENGTH_SHORT).show();
        back_pressed = System.currentTimeMillis();
    }
    public void appExit () { this.finish(); Intent intent = new Intent(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_HOME); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(intent); }

    public void initialization()
    {
        // Инициализация
        // Базы данных
        myDataBase = new DataBase(MainActivity.this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        groupChatsRef = database.getReference("groupChats");
        contactsDB = database.getReference("users").child(user.getUid()).child("chats");
        myUserDB = database.getReference("users").child(user.getUid());
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // View компоненты
        listViewLayout = findViewById(R.id.listViewLayout);
        statusLabel = findViewById(R.id.my_user_status_label);
        myNicknameLabel = findViewById(R.id.my_user_nickname_label);
        avatarImageView = findViewById(R.id.avatarImageView);
        fab = findViewById(R.id.fab);
        highMainPanel = findViewById(R.id.high_main_panel);
        clMainPanel = findViewById(R.id.cl_main_panel);
        tabLayout = findViewById(R.id.tab_layout);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        // Адаптеры
    }

    public void viewPagerInit() {
        // Инициализация ViewPager
        viewPager = findViewById(R.id.view_pager);
        pagerSliderAdapter = new PagerSliderAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerSliderAdapter);
        viewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.getSelectedTabPosition();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                if(tabLayout.getSelectedTabPosition() == 0)
                    main.setBackgroundResource(R.drawable.chats_reg_phone);
                else
                    main.setBackgroundResource(R.drawable.contacts_reg_phone);

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    @Override
    protected void onStop() {
            super.onStop();
        Log.d("TimLog", "сработал стоп MainActivity");

      // if (mSettings.getString(ONLINE_PREFERENCE, "").equals("0")) {
//
      //       MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
//
      // } else {
      //     myUserDB.child("status").setValue("");
      // }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TimLog", "сработал start MainActivity");
          new Thread(new Runnable() {
          @Override
          public void run() {

              Log.d("TimLog", "сработал start mainactivity внутри потока");
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
        Log.d("TimLog", "сработал pause MainActivity");
       if (mSettings.getString(ONLINE_PREFERENCE, "").equals("0")) {
           MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
       } else {
           myUserDB.child("status").setValue("");
       }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TimLog", "сработал дестрой");
      //  if (mSettings.getString(ONLINE_PREFERENCE, "").equals("0")) {
      //      myUserDB.child("status").setValue(DateTime.getDateTime());
      //  } else {
      //      MainActivity.myUserDB.child("status").setValue("");
      //  }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {
        // Читаем с inJustDecodeBounds=true для определения размеров
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        // Вычисляем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        // Читаем с использованием inSampleSize коэффициента
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Реальные размеры изображения
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public void avatarClick()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String convertToFileURL ( String filename )
    {
        // On JDK 1.2 and later, simplify this to:
        // "path = file.toURL().toString()".
        String path = new File ( filename ).getAbsolutePath ();
        if ( File.separatorChar != '/' )
        {
            path = path.replace ( File.separatorChar, '/' );
        }
        if ( !path.startsWith ( "/" ) )
        {
            path = "/" + path;
        }
        String retVal = "file:" + path;
        return retVal;
    }

    // Возвращение аватарки с галереии
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Bitmap bitmap = null;
        ImageView imageView = (ImageView) findViewById(R.id.avatarImageView);
        switch(requestCode) {
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    final File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + this.getPackageName(), "MyAvatar" + user.getUid());
                    Uri selectedImage = imageReturnedIntent.getData();
                    bitmap =decodeSampledBitmapFromResource(getRealPathFromURI(this,selectedImage), 300,300);
                    imageView.setImageBitmap(bitmap);
                    try {
                        FileOutputStream fos = null;
                        try {
                            fos = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        } finally {
                            if (fos != null) fos.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    final String nameOfAvatar = UUID.randomUUID().toString();
                    mStorageRef.child(user.getUid()).child("avatar").putFile(Uri.parse(convertToFileURL(file.getAbsolutePath()))).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            myRef.child(mAuth.getCurrentUser().getUid()).child("avatar").setValue(nameOfAvatar);
                        }
                    });
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("userAvatarPath",nameOfAvatar);
                    editor.apply();

                }
        }
    }

    public void downloadAvatar() throws IOException {

        ImageView imageView = (ImageView) findViewById(R.id.avatarImageView);
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + this.getPackageName());
        if (!file.exists())
        {
            file.mkdir();
        }
        final File file1 = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + this.getPackageName(), "MyAvatar" + user.getUid());
        if (file1.exists()) {
            avatarImageView.setImageURI(Uri.parse(file1.getAbsolutePath()));
        }
        else {
            mStorageRef.child(user.getUid()).child("avatar").getFile(file1).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    avatarImageView.setImageURI(Uri.parse(file1.getAbsolutePath()));
                    Log.d("Zheka", "downloadReady");
                }
            });
        }
        if(!file1.exists())
        {
            Log.d("Zheka","downloadFromPhone");
            avatarImageView.setImageResource(R.drawable.default_avatar);
        }
        // }
    }



    // Получить разрешения
    private void hasPermissions()
    {
        int storagePermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int audioPermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);


        if (storagePermissionStatus == PackageManager.PERMISSION_GRANTED && audioPermissionStatus == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_REQUEST_CODE);
        }
    }

    // Разрешения
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted

                } else {
                    //finish();
                }
                return;
        }
    }

    private void settingsClick()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showPopupMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.Popup);
        PopupMenu popupMenu = new PopupMenu(wrapper, v);
        popupMenu.inflate(R.menu.main_menu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.log_out_item:
                                logOut();
                                return true;
                            case R.id.settings_item:
                                settingsClick();
                                return true;
                            case R.id.new_group_item:
                                newGroupClick();
                                return true;
                            default:
                                return false;
                        }
                    }
                });

        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.show();
    }

    private void newGroupClick() {
        Intent intent = new Intent(this, NewGroupContactActivity.class);
        startActivity(intent);
    }


    static String getCurrentTimeFromServer(Context context)
    {
        final int[] check = {1};
        final String[] stringToReturn = new String[1];
        OkHttpClient client = new OkHttpClient();
        String urlWebSite = "http://www.unn.ru/time/";
        Request request = new Request.Builder().url(urlWebSite).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                check[0] = 2;
                Log.d("Zheka", "getTime is Fail");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful())
                {
                    String myDate = response.header("Date").toString();
                    Log.d("Zheka", "Curr = " + myDate);
                    String parseDate[] = myDate.split(" ");
                    String dayOfWeek = parseDate[0];
                    String dayNumber = parseDate[1];
                    String mounth = parseDate[2];
                    String year = parseDate[3];
                    String timeInGMT = parseDate[4];
                    Log.d("Zheka", "time = " + dayOfWeek + " " + dayNumber + " " + mounth + " " + year + " " + timeInGMT);
                    String fullTime[] = timeInGMT.split(":");  //  0 - час,  1 - минуты  2 - секунды
                    int hourInInt = Integer.parseInt(fullTime[0]);
                    Log.d("Zheka", "hour =" + hourInInt);
                    hourInInt +=3;
                    if (hourInInt == 24)
                        hourInInt = 0;
                    if (hourInInt > 24)
                        hourInInt -=24;
                    Log.d("Zheka", "hour =" + hourInInt);
                    String CurrentTime = hourInInt + ":" + fullTime[1];
                    Log.d("Zheka", "TimeInRussian " + CurrentTime);
                    stringToReturn[0] = CurrentTime;
                    check[0] = 2;

                }
            }
        });
        while (check[0] == 1)
        {

        }
        Thread thread = new Thread();
        thread.run();
        try {
            thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return stringToReturn[0];
    }

    static String checkDifference (String timeReal)
    {
        String timeInPhone = DateTime.getTime();
        Log.d("Zheka", timeReal + " " + timeInPhone);
        int realHour;
        int realMinute;
        if (timeReal.length() == 4)
        {
            realHour = Integer.parseInt(timeReal.substring(0,1));
            realMinute = Integer.parseInt(timeReal.substring(2));
        }
        else
        {
            realHour = Integer.parseInt(timeReal.substring(0,2));
            realMinute = Integer.parseInt(timeReal.substring(3));
        }
        int phoneHour = Integer.parseInt(timeInPhone.substring(0,2));
        int phoneMinute = Integer.parseInt(timeInPhone.substring(3,5));
        Log.d("Zheka","FuncCheckDiffernceTry " + realHour + ":" + realMinute + " " + phoneHour + ":" + phoneMinute );
        int differenceInHour = 0;
        int differenceInMinute = 0;
        //   if (realHour > phoneHour)
        //   {
        //       differenceInHour = (realHour - phoneHour) * (-1);
        //   }
        //   else differenceInHour = phoneHour - realHour;
        //   if (realMinute > phoneMinute) {
        //       differenceInMinute = realMinute - phoneMinute;
        //   }
        //   else differenceInMinute = phoneMinute - realMinute;
        int realTime = realHour * 60 + realMinute;
        int phoneTime = phoneHour * 60 + phoneMinute;


        Log.d("Zheka","FuncTime " + (realTime) + " " + phoneTime );

        //   if (realTime > phoneTime)
        //       Log.d("Zheka", "pidoras");
        //   else Log.d("Zheka", "pidooor");


        if (realTime == phoneTime)
        {
            return "0";
        }
        else if (realTime > phoneTime)
        {
            //Log.d("Zheka", "zdarovaaa");
            differenceInHour = (phoneTime - realTime) / 60;
            differenceInMinute = (phoneTime - realTime) - ((differenceInHour)*60);
        }
        else
        {
            differenceInHour = (phoneTime - realTime) / 60;
            differenceInMinute = (phoneTime - realTime) - differenceInHour*60;
        }


        if (differenceInHour == 0)
        {

            if (differenceInMinute > 0) return "+00:" + differenceInMinute;
            else return "-00:" + ((differenceInMinute)*(-1));
        }
        else if (differenceInHour > 0)
        {
            if (differenceInHour < 10)
                return "+0" + differenceInHour + ":" + Math.abs(differenceInMinute);
            else return "+" + differenceInHour + ":" + Math.abs(differenceInMinute);
        }
        else
        {
            if (Math.abs(differenceInHour) < 10)
                return "-0" + Math.abs(differenceInHour) + ":" + Math.abs(differenceInMinute);
            else return "-" + Math.abs(differenceInHour) + ":" + Math.abs(differenceInMinute);
        }


    }

    void setNameToImageView()
    {
        if (mSettings.getString("username","").equals("")) {
            database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("username").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myNicknameLabel.setText(dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        else
        {
            myNicknameLabel.setText(mSettings.getString("username",""));
        }
    }




}