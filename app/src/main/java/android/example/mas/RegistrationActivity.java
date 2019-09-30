package android.example.mas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {

    // Создание обьектов
    FirebaseDatabase database;
    private FirebaseAuth mAuth;
    EditText email;
    EditText username;
    EditText password;
    Button registration;
    Button login;
    String uID;
    DatabaseReference users;
    SharedPreferences mSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //Блок инициализации
        mAuth = FirebaseAuth.getInstance();
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        registration = findViewById(R.id.registration_button);
        login = findViewById(R.id.login_button);
        database  = FirebaseDatabase.getInstance();
        users = database.getReference("users");

        // присвоение слушателя
        registration.setOnClickListener(this);
        login.setOnClickListener(this);

    }

    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    // Метод создания нового пользователя
    private void createAccount(String email, final String password) {

        if(checkView()==false)
        {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            uID = user.getUid();
                            String mail = user.getEmail();
                            String usnameStr = username.getText().toString();

                            SharedPreferences mSettings = getSharedPreferences("mysettings", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = mSettings.edit();
                            editor.putString("username",usnameStr);
                            editor.putString("userAvatarPath","nothingAvatar");
                            editor.apply();

                            users.child(uID).child("mail").setValue(mail);         //1.03    поменял местами push мейла и username'a
                            users.child(uID).child("username").setValue(usnameStr);
                          //users.child(uID).child("contacts").push();
                           users.child(uID).child("status").setValue("offline");
                           users.child(uID).child("avatar").setValue("standart");
                           User myUser = new User(mail,usnameStr,uID,"offline");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    FirebaseMessaging.getInstance().subscribeToTopic("notification_"+ mAuth.getCurrentUser().getUid());
                                    Log.d("Zheka","Subscribe");
                                }
                            }).start();

                           Intent intent = new Intent(RegistrationActivity.this, SignInActivity.class);
                           intent.putExtra("myUser",(Parcelable) myUser);
                           startActivity(intent);

                        } else {
                            Toast.makeText(RegistrationActivity.this, "Некорректный EMAIL или данный пользователь уже зарегистрирован",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Метод проверки полей ввода
    boolean checkView()
    {
        if(email.getText().toString().equals("") || username.getText().toString().equals("") || username.getText().toString().equals(""))
        {
            Toast.makeText(RegistrationActivity.this, "Заполните поля ввода",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            return true;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.registration_button: // Обработка нажатия кнопки регистрации
                createAccount(email.getText().toString(), password.getText().toString());
                break;
            case R.id.login_button:
                logClick();
                break;
        }
    }

    private void logClick() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
    }
}
