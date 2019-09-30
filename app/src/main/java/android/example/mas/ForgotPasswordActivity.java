package android.example.mas;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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


public class ForgotPasswordActivity extends AppCompatActivity {

    Button forgotPasswordClick;
    Button sendCode;
    EditText EmailOfForgotPassword;
    EditText codeFromEmail;
    FirebaseDatabase emailBase;
    DatabaseReference email;
    private FirebaseAuth mAuth;
    String uID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mAuth = FirebaseAuth.getInstance();
        /*sendCode=findViewById(R.id.BT_sand_code_from_email);
        codeFromEmail=findViewById(R.id.ET_code_from_reset_email);*/
        EmailOfForgotPassword = findViewById(R.id.ET_email_of_forgot_password);
        forgotPasswordClick = findViewById(R.id.BT_reset_password);
        forgotPasswordClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((EmailOfForgotPassword.getText().toString().length() < 30) && (!EmailOfForgotPassword.getText().toString().equals("")))        // Реализация функции - "забыл пароль"
                {
                //    FirebaseUser user = mAuth.getCurrentUser();
                //    uID=user.getUid();
                //    email = emailBase.getReference("users");
                //    if (email.child(uID).child("mail").getKey().toString().equals(EmailOfForgotPassword.toString()))
                //    {
                        mAuth.sendPasswordResetEmail(EmailOfForgotPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {          // Высылаем на почту письмо с восстановлением пароля.
                                if (task.isSuccessful())
                                {
                                    Toast.makeText(getApplicationContext(),"Сообщение отправлено! Через 5 секунд вы будете отправлены на страницу авторизации.",Toast.LENGTH_SHORT).show();
                           //        try {
                           //
                           //            Thread.sleep(5000);
                           //        } catch (InterruptedException e) {
                           //            e.printStackTrace();
                           //        }
                                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                                    startActivity(intent);
                     //               forgotPasswordClick.setEnabled(false);                                                          // Оказалось лишним
                     //               EmailOfForgotPassword.setEnabled(false);
                     //               sendCode.setEnabled(true);
                     //               codeFromEmail.setEnabled(true);
                                }
                                else Toast.makeText(getApplicationContext(),"Данная почта не зарегистрирована в базе данных приложения LunGo.",Toast.LENGTH_SHORT).show();
                            }
                        });
                //    }

                }
            }
        });
 //    sendCode.setOnClickListener(new View.OnClickListener() {                       // Попытка реализации введения кода, пришедшего на почту, оказался не нужным.
 //        @Override
 //        public void onClick(View view) {
 //            if (!(codeFromEmail.getText().toString().equals("")))
 //            {
 //                mAuth.confirmPasswordReset(sendCode.getText().toString(),"newPassword").addOnCompleteListener(new OnCompleteListener<Void>() {
 //                    @Override
 //                    public void onComplete(@NonNull Task<Void> task) {
 //                    if (task.isSuccessful())
 //                    {
 //                        Toast.makeText(getApplicationContext(),"Пароль успешно сменён!",Toast.LENGTH_SHORT).show();
 //                        Intent intent = new Intent(ForgotPasswordActivity.this, SignInActivity.class);
 //                        startActivity(intent);
 //                    }
 //                    else Toast.makeText(getApplicationContext(),"Код введён неверно, попробуйте ещё раз.",Toast.LENGTH_SHORT).show();
 //                    }
 //                });
 //            }

 //          }
 //      });
    }




}
