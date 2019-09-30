package android.example.mas;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.Serializable;

import static android.example.mas.MainActivity.APP_PREFERENCES_DifferenceInTime;
import static android.example.mas.MainActivity.APP_PREFERENCES_TIME;
import static android.example.mas.MainActivity.checkDifference;
import static android.example.mas.MainActivity.getCurrentTimeFromServer;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    // Обьявление элементов
    Button signIn;
    Button registratoin;
    Button forgotPassword;
    EditText username;
    EditText password;
    private FirebaseAuth mAuth;
    static final int STORAGE_REQUEST_CODE = 123;
    boolean accessToIntenet = false;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        accessToIntenet = checkAccessToInternetConnection();
        if (currentUser == null) {
            setContentView(R.layout.activity_sign_in);

            // инициализация элементов
            signIn = (Button) findViewById(R.id.button_sign_in);
            registratoin = (Button) findViewById(R.id.button_reg);
            username = findViewById(R.id.sign_username);
            password = findViewById(R.id.sign_password);
            forgotPassword = findViewById(R.id.textView3); // Придётся потом изменить, не стал менять на другой ID, т.к весь дизайн у acitivity_sign_in идёт на перекосяк.


            // Присвоение обработчиков события
            signIn.setOnClickListener(this);
            registratoin.setOnClickListener(this);
            forgotPassword.setOnClickListener(this);

            hasPermissions();
        }
        else if (!accessToIntenet) {
            Log.d("Zheka", "access to Internet is closed");
            setContentView(R.layout.activity_sign_in);
            username = findViewById(R.id.sign_username);
            password = findViewById(R.id.sign_password);
            username.setHint(currentUser.getEmail());
            password.setHint("******");
            username.setEnabled(false);
            password.setEnabled(false);
            progressBar = findViewById(R.id.waitingCircle_Progress_Bar);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button_sign_in: // Кнопка входа
                signIn(username.getText().toString(),password.getText().toString());
                break;
            case R.id.button_reg: // Кнопка регистрации
                regClick(); // Вызов метода
                break;
            case R.id.textView3:
                forgotPassword();

        }
    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        User myUser = new User();
        if(currentUser!=null)
        {
            if (!accessToIntenet) {
                progressBar.setVisibility(View.VISIBLE);
            }
            stopThreadWithoutAccessToInternet stopThread = new stopThreadWithoutAccessToInternet();
            stopThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    // Функция обработки нажатия кнопки Registration
    private void regClick() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    //Подключение пользователя
    public void signIn(String email, final String password) {
        // Проверка на заполненность полей
        if(checkView() == false) return;

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            final FirebaseUser user = mAuth.getCurrentUser();

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    FirebaseMessaging.getInstance().subscribeToTopic("notification_"+ user.getUid());
                                }
                            }).start();

                            getTimeAndNameFromInternet();


                            Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                            User userAcc = new User(user.getEmail(),username.getText().toString(),user.getUid());
                            intent.putExtra("user", userAcc);

                            startActivity(intent);
                            finish();
                        } else {

                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            return;

                        }
                    }
                });
    }

    // Метод проверки полей ввода
    boolean checkView()
    {
        if(username.getText().toString().equals("") || password.getText().toString().equals(""))
        {
            Toast.makeText(SignInActivity.this, "Заполните поля ввода",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            return true;
        }
    }

    public void forgotPassword()
    {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }
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

    void getTimeAndNameFromInternet()
    {

      //  ProgressBar progressBar = findViewById(R.id.signActivity_progressBar);
      //  progressBar.setVisibility(View.VISIBLE);

        final SharedPreferences mSettings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSettings.edit();

        // Установка имени пользователя (Для Тёмы)
        if (mSettings.getString("username","").equals("")) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            database.getReference().child("users").child(mAuth.getCurrentUser().getUid()).child("username").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    editor.putString("username", dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        editor.putString("time","");
        editor.putString("differenceInTime","");
        editor.apply();

        Log.d("Zheka", "pointOne");;               // Можно убрать лишние if, и без них можно работать.

        // Закомментировал, т.к тестируется новое взятие времени.

   //     new Thread(new Runnable() {
   //         @Override
   //         public void run() {
//
//
   //             if (mSettings.contains(APP_PREFERENCES_TIME))
   //             {
   //                 Log.d("Zheka", "pointTwo");
   //                 if (mSettings.getString(APP_PREFERENCES_TIME,"").equals(""))
   //                 {
   //                     String currentTime = MainActivity.getCurrentTimeFromServer(SignInActivity.this);
   //                     editor.putString(APP_PREFERENCES_TIME,currentTime);
   //                     editor.putString(APP_PREFERENCES_DifferenceInTime,MainActivity.checkDifference(currentTime));
   //                     editor.apply();
   //                 }
   //             }
   //             else
   //             {
   //                 Log.d("Zheka", "pointTwo");
   //                 String currentTime = MainActivity.getCurrentTimeFromServer(SignInActivity.this);
   //                 editor.putString(APP_PREFERENCES_TIME,currentTime);
   //                 editor.putString(APP_PREFERENCES_DifferenceInTime,MainActivity.checkDifference(currentTime));
   //                 editor.apply();
   //             }
   //             Log.d("Zheka", "checkTime " + mSettings.getString(APP_PREFERENCES_TIME,""));
   //             Log.d("Zheka", "differenceInTime " + mSettings.getString(APP_PREFERENCES_DifferenceInTime,""));
   //         }
   //     }).start();


        ChatActivity.getCurrentTimeFromFirebaseServerFast(this);

    }

    boolean checkAccessToInternetConnection()
    {
        boolean connected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
            //we are connected to a network
            connected = true;
        }
        return connected;
    }

    private class  stopThreadWithoutAccessToInternet extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... strings) {

            FirebaseUser currentUser = mAuth.getCurrentUser();
            User myUser = new User();
            if (currentUser != null) {
                int abc = 0;
                while (!accessToIntenet) {

                    try {
                        accessToIntenet = checkAccessToInternetConnection();
                        Thread.sleep(1000); //Приостанавливает поток на 1 секунду
                    } catch (Exception e) {

                        Log.d("Zheka", "ban!!!");
                    }
                    abc++;
                    Log.d("Zheka", "hi" + abc);
                }
                //progressBar.setVisibility(View.INVISIBLE);


                //getTimeAndNameFromInternet();
                ChatActivity.getCurrentTimeFromFirebaseServerFast(SignInActivity.this);

                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                myUser.setMail(currentUser.getEmail());
                myUser.setId(currentUser.getUid());
                myUser.setStatus("online");

                //  FirebaseMessaging.getInstance().subscribeToTopic("notification_" + currentUser.getUid());


                intent.putExtra("user", (Parcelable) myUser);
                startActivity(intent);
                finish();
            }

            return null;
        }
    }

}
