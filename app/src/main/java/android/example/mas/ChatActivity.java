package android.example.mas;

import android.Manifest;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.example.mas.MainActivity.APP_PREFERENCES;
import static android.example.mas.MainActivity.APP_PREFERENCES_DifferenceInTime;
import static android.example.mas.MainActivity.APP_PREFERENCES_TIME;
import static android.example.mas.MainActivity.convertToFileURL;
import static android.example.mas.MainActivity.decodeSampledBitmapFromResource;
import static android.example.mas.MainActivity.getRealPathFromURI;

public class ChatActivity extends AppCompatActivity {

    static final int GALLERY_REQUEST = 1;
    static final int AUDIO_REQUEST_CODE = 123;
    String myName;
    String countNoReadMessage;
    // Создание обьектов
    DataBase myDb;
    File friendAvFile;
    //MessageAdapter messageAdapter;
    RecyclerMassageAdapter messageAdapter;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRefUser1;
    DatabaseReference myRef;
    DatabaseReference groupChatsRef;
    DatabaseReference myRefUser2;
    EditText value_of_massage;
    Button sandMass;
    ArrayList<Message> massage = new ArrayList<>();
    String friendID;
    RecyclerView print_massages;
    LinearLayout llChat;
    static TextView friendNickname;
    TextView friendStatus;
    ImageView friendAvatar;
    Button addContact;
    static User friendUser;
    DatabaseReference myUserDB;
    // Создание объектов для записи голосовых сообщений
    Button recordbutton;
    MediaRecorder recorder;   // Библиотека аудио-сообщений
    String fileName;
    String fileOfName;
    String FileName123;
    // Объект для прослушаивания голосовых сообщений
    private MediaPlayer   player = null;
    // Кнопка для загрузки изображений в чат
    Button downloadImageInChat;

    //Search
    EditText searchChat;
    RecyclerMassageAdapter searchAdapter;
    ArrayList<Message> searchList;
    ArrayList<String> groupChatContactList;
    ArrayList<User> userContactList;

    private static final String LOG_TAG = "Record log";
    private StorageReference mStorageRef;   // Хранилище данных
    // FCM

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAT0HP0zA:APA91bElz2H28KldQuR0PPXW6IKd_E9bcosqJo4JHpypwwZxpSJxuP-aG5H9tjoY6yl3wcr0bIOTb3zZQDt3IGCHX1oFsnVmhtx5w9-0VqvQStiORZ-uQaSSt_nAgzepvHGcgBTJ1N_I";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Если активити единственное в стеке - вызвать этот метод(в нём будет взято время с сервера, чтобы при открытии уведомлений не сломать бд)
        checkToLastActivity();

        mSettings = getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);

        // Инициализация обьектов
        countNoReadMessage = "0";
        myDb = new DataBase(ChatActivity.this);
        myRef = database.getReference("users");
        llChat = findViewById(R.id.llChats);
        Intent intChat = getIntent();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        myUserDB = database.getReference("users").child(user.getUid());
        myUserDB.child("status").setValue("online");

        groupChatsRef = database.getReference("groupChats");

        userContactList = new ArrayList<>();
        value_of_massage = findViewById(R.id.ET_MASSAGE_LL);
        sandMass = findViewById(R.id.button_LL_sandmassage);
        sandMass.setEnabled(false);
        print_massages = findViewById(R.id.LV_PRINT_AllMassages);
        friendNickname = findViewById(R.id.friend_nickname_label);
        friendStatus = findViewById(R.id.friend_status_label);
        friendAvatar =findViewById(R.id.friend_avatar_chat);
        addContact = findViewById(R.id.button_add_contacts);
        Intent intUser = getIntent();
        friendUser = intUser.getParcelableExtra("user");

        if (friendUser != null) {
            friendID = friendUser.getId();
        }
        else friendID = intChat.getStringExtra("id");

        messageAdapter = new RecyclerMassageAdapter(massage,ChatActivity.this,friendID);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        print_massages.setLayoutManager(linearLayoutManager);
        print_massages.setAdapter(messageAdapter);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        recordbutton = findViewById(R.id.voice_recognition_button);
        fileName= Environment.getExternalStorageDirectory().getAbsolutePath();
        fileOfName = UUID.randomUUID().toString() + ".3gp";
        fileName+="/" + fileOfName;

        friendStatus.setText(friendUser.getStatus());
        downloadImageInChat = findViewById(R.id.buttonDownloadImage);
        groupChatContactList = new ArrayList<>();

        myName = mSettings.getString("username","");

        //search
        searchList=new ArrayList<>();
        searchChat = findViewById(R.id.chatSearch);
        searchAdapter = new RecyclerMassageAdapter(searchList,this,friendID);
        searchChat.addTextChangedListener(Search);

        // Нажатие на кнопку "скрепка" для загрузки изображения в чат.
        downloadImageInChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onScrepkaClick();
            }
        });

        // если групповой чат
        if (friendUser.getIsGroupChat().equals("1")) {
            groupChatLoad();
            groupChatAvatarClick();
            sandMassClickGroupChat();
            avatarDownload();
            lungoGroupChatButtonClick();
            friendGroupNicknameSetText();
            getGroupChatContactList();
            getUserContactList();
            checkChangeOfAvatar();
        }
        // Если одиночный
        if(friendUser.getIsGroupChat().equals("0")) {
            myRefUser1 = database.getReference("users").child(user.getUid()).child("chats").child(friendID).child("message");
            myRefUser2 = database.getReference("users").child(friendID).child("chats").child(user.getUid()).child("message");
            avatarDownload();
            getCountNoReadMessage();
            friendAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    avatarClick();
                }
            });
            chatLoad();
            sandMassClickChat();
            friendSoloNicknameSetText();
            lungoChatButtonClick();
            checkChangeOfAvatar();
        }
        recordButtonOnClick();
        value_of_massage.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                { if(s.toString().trim().length()==0){
                    recordbutton.setBackgroundResource(R.drawable.mic);
                } else {
                    recordbutton.setBackgroundResource(R.drawable.button_go);
                }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    // Срабатывание кнопки записи голосового сообщения
    private void recordButtonOnClick() {
        recordbutton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(hasPermissions()==true) {
                    try {
                        // String FileName =UUID.randomUUID().toString() + ".3gp";
                        if ((motionEvent.getAction() == MotionEvent.ACTION_DOWN) && (value_of_massage.getText().length() == 0)) {
                            FileName123 = UUID.randomUUID().toString() + ".3gp";
                            //  Toast.makeText(getApplicationContext(), FileName123, Toast.LENGTH_SHORT).show();
                            startRecording(FileName123);
                            recordbutton.setText("Стоп");
                        } else if ((motionEvent.getAction() == MotionEvent.ACTION_UP) && (value_of_massage.getText().length() == 0)) {
                            stopRecording(FileName123);
                            recordbutton.setText("");
                        }

                        if ((motionEvent.getAction() == MotionEvent.ACTION_UP) && (value_of_massage.getText().length() != 0)) {
                            sandMass.callOnClick();
                        }
                    } catch (RuntimeException e) {
                        Toast.makeText(ChatActivity.this,"Не удалось загрузить голосовое сообщение, попробуйте снова",Toast.LENGTH_SHORT);

                    } finally {
                        recordbutton.setText("");
                    }
                }
                return false;
            }
        });
    }

    // Устанавливает название конференции-чата.
    private void friendGroupNicknameSetText() {
        friendNickname.setText(friendUser.getUsername());
    }

    // Устанавливает имя собеседника
    private void friendSoloNicknameSetText() {
        friendNickname.setText(friendUser.getUsername());
    }

    // Lungo кнопка клик
    private void lungoGroupChatButtonClick() {
        // Добавить в контакты
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGroupChatPopupMenu(v);
            }
        });
    }

    // Lungo кнопка клик
    private void lungoChatButtonClick() {
        // Добавить в контакты
        addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChatPopupMenu(v);

            }
        });
    }

    // Функция отправления сообщения в чате.
    private void sandMassClickGroupChat() {
        sandMass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (value_of_massage.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Слишком короткое сообщение", Toast.LENGTH_SHORT).show();
                    return;
                } else if (value_of_massage.getText().length() < 500) // Отправка сообщения
                {
                    String currentTimeFromServer = getServerTimeRightNow(ChatActivity.this);
                    // Убрать комментарии, если дата будет браться правильно.
                    String date = mSettings.getString("currentDate","");
                    if (massage.size() > 1) {
                        if (DateTime.dateCompare(date, massage.get(massage.size() - 1).getDate()) == true) {
                            String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                            groupChatsRef.child(friendUser.getId()).child("message").push().setValue(msg);                        }
                    }
                    if (massage.size() == 0) {
                        String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                        groupChatsRef.child(friendUser.getId()).child("message").push().setValue(msg);

                    }
                    groupChatsRef.child(friendUser.getId()).child("message").push().setValue(user.getUid()+currentTimeFromServer +"read1" + "flag0" +date + value_of_massage.getText().toString());
                    putLastMessageAndHisTimeForChat(user.getUid(),friendID,currentTimeFromServer,value_of_massage.getText().toString());
                    final String valueForNotification = value_of_massage.getText().toString();
                    groupChatsRef.child(friendUser.getId()).child("contacts").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                            if (!dataSnapshot.getKey().equals(user.getUid())) {
                                fcmNotificationPart1(myName + "(" + friendUser.getUsername() + ")", valueForNotification, dataSnapshot.getKey(), friendUser.getId());
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else
                    Toast.makeText(getApplicationContext(), "Слишком длинное сообщение", Toast.LENGTH_SHORT).show();
                value_of_massage.setText("");
            }
        });
    }

    // Загрузка в чат последнеего сообщения, а также время этого сообщения (lastMessageDate / lastMessage)
    private void putLastMessageAndHisTimeForChat(String uid, String friendID, String currentTimeFromServer, String valueOfMessage) {
        String lastMessDate = DateTime.getDateTime();
        if (!currentTimeFromServer.equals(""))
            currentTimeFromServer = lastMessDate.substring(0,6) + currentTimeFromServer + lastMessDate.substring(11);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");;
        groupChatsRef.child(friendUser.getId()).child("lastmessagedate").setValue(currentTimeFromServer);
        groupChatsRef.child(friendUser.getId()).child("lastmessage").setValue(valueOfMessage);
    }

    // Добавление сообщения в чат из базы данных (при получении сообщения)
    private void groupChatLoad() {
        groupChatsRef.child(friendUser.getId()).child("message").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {
                final Message msg = new Message();
                String msgText = dataSnapshot.getValue(String.class);
                msg.setMessage(msgText.substring(48));
                msg.setDate(msgText.substring(43,48));
                msg.setTime(msgText.substring(28,33));
                msg.setId(msgText.substring(0,28));
                msg.setRead("1");
                msg.setFlag(Integer.valueOf(msgText.substring(42,43)));
                myRef.child(msg.getId()).child("username").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                        msg.setMsgUsername(dataSnapshot1.getValue(String.class));
                        messageAdapter.notifyDataSetChanged();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                dataSnapshot.getRef().setValue(msg.getId()+msg.getTime()+"read1"+ "flag" + msg.getFlag() +msg.getDate()+ msg.getMessage());
                massage.add(msg);
                messageAdapter.notifyDataSetChanged();
                print_massages.smoothScrollToPosition(massage.size());
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message msg = new Message();
                String msgText = dataSnapshot.getValue(String.class);
                msg.setMessage(msgText.substring(48));
                msg.setDate(msgText.substring(43,48));
                msg.setTime(msgText.substring(28,33));
                msg.setId(msgText.substring(0,28));
                msg.setRead("1");
                msg.setFlag(Integer.valueOf(msgText.substring(42,43)));
                for (int i=0;i<massage.size();i++)
                {
                    String masSearch = massage.get(i).getId() + massage.get(i).getTime() + "read" + massage.get(i).getRead() + "flag" + massage.get(i).getFlag() + massage.get(i).getDate() + massage.get(i).getMessage();
                    if (masSearch.equals(msgText)) {
                        massage.remove(i);
                        messageAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                print_massages.smoothScrollToPosition(massage.size());
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void sandMassClickChat() {
        // Обработка нажатия кнопки отправить
        sandMass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (value_of_massage.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Слишком короткое сообщение", Toast.LENGTH_SHORT).show();
                    return;
                } else if (value_of_massage.getText().length() < 5000) // Отправка сообщения
                {

                    String currentTimeFromServer = getServerTimeRightNow(ChatActivity.this);
                    fcmNotificationPart1(myName,value_of_massage.getText().toString(),friendID,user.getUid());
                    // Добавление даты
                    String date = mSettings.getString("currentDate","");
                    if (massage.size() > 1) {
                        if (DateTime.dateCompare(date, massage.get(massage.size() - 1).getDate()) == true) {
                            String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                            myRefUser1.push().setValue(msg);
                            myRefUser2.push().setValue(msg);
                        }
                    }
                    if (massage.size() == 0) {
                        String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                        myRefUser1.push().setValue(msg);
                        myRefUser2.push().setValue(msg);
                    }

                    myRefUser1.push().setValue(user.getUid()+currentTimeFromServer +"read1" + "flag0"  +date +  value_of_massage.getText().toString() );
                    myRefUser2.push().setValue(user.getUid()+ currentTimeFromServer + "read0" + "flag0" +date +value_of_massage.getText().toString());

                    putLastMessageAndHisTime(user.getUid(),friendID,currentTimeFromServer,value_of_massage.getText().toString());

                    if(countNoReadMessage == null)
                    {
                        countNoReadMessage = "0";
                    }
                    int count = Integer.parseInt(countNoReadMessage);
                    count++;
                    countNoReadMessage = String.valueOf(count);

                    myRef.child(friendID).child("chats").child(user.getUid()).child("noread").setValue(countNoReadMessage);

                } else
                    Toast.makeText(getApplicationContext(), "Слишком длинное сообщение", Toast.LENGTH_SHORT).show();
                value_of_massage.setText("");
            }

        });
    }

    /**
     *     Print messages from database to display screen.
     */

    private void chatLoad() {
        // ВЫвод сообщений на экран
        myRefUser1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Message msg = new Message();
                String msgText = dataSnapshot.getValue(String.class);
                msg.setMessage(msgText.substring(48));
                msg.setDate(msgText.substring(43,48));
                msg.setTime(msgText.substring(28,33));
                msg.setId(msgText.substring(0,28));
                msg.setRead("1");
                msg.setFlag(Integer.valueOf(msgText.substring(42,43)));
                dataSnapshot.getRef().setValue(msg.getId()+msg.getTime()+"read1"+ "flag" + msg.getFlag()+msg.getDate() + msg.getMessage());

                massage.add(msg);
                messageAdapter.notifyDataSetChanged();
                print_massages.smoothScrollToPosition(massage.size());
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Message msg = new Message();
                String msgText = dataSnapshot.getValue(String.class);
                msg.setMessage(msgText.substring(48));
                msg.setDate(msgText.substring(43,48));
                msg.setTime(msgText.substring(28,33));
                msg.setId(msgText.substring(0,28));
                msg.setRead("1");
                msg.setFlag(Integer.valueOf(msgText.substring(42,43)));
                for (int i=0;i<massage.size();i++)
                {
                    String masSearch = massage.get(i).getId() + massage.get(i).getTime() + "read" + massage.get(i).getRead() + "flag" + massage.get(i).getFlag() + massage.get(i).getDate() + massage.get(i).getMessage();
                    if (masSearch.equals(msgText)) {
                        massage.remove(i);
                        messageAdapter.notifyDataSetChanged();
                        break;
                    }
                }
                print_massages.smoothScrollToPosition(massage.size());

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void startRecording(String FileName) {
        String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() +"/" +friendID + "/"  +  FileName;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
        recorder.start();
    }

    private void stopRecording(final String FileName) {
        recorder.stop();
        recorder.release();
        recorder = null;
        String fileName =Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() +"/" +friendID + "/"  +  FileName;
        final File myFile = new File(fileName);
        final Uri ura = Uri.fromFile(myFile);
        StorageReference MyStoreUser1;
        final StorageReference MyStoreUser2 = FirebaseStorage.getInstance().getReference().child(friendID).child("chats").child((user.getUid())).child(FileName);
        if (friendUser.getIsGroupChat().equals("0")) {
            MyStoreUser1 = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("chats").child(friendID).child(FileName);
        }
        else
        {
            MyStoreUser1 = FirebaseStorage.getInstance().getReference().child(friendID).child("chats").child(FileName);
        }
        MyStoreUser1.putFile(ura).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful())
                {
                    if (friendUser.getIsGroupChat().equals("0")) {
                        MyStoreUser2.putFile(ura).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    String currentTimeFromServer = getServerTimeRightNow(ChatActivity.this);

                                    putLastMessageAndHisTime(user.getUid(), friendID, currentTimeFromServer, "Голосовое сообщение");
                                    fcmNotificationPart1(myName,"Голосовое сообщение",friendID,user.getUid());

                                    String date = mSettings.getString("currentDate","");
                                    if (massage.size() > 1) {
                                        if (DateTime.dateCompare(date, massage.get(massage.size() - 1).getDate()) == true) {
                                            String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                            myRefUser1.push().setValue(msg);
                                            myRefUser2.push().setValue(msg);
                                        }
                                    }
                                    if (massage.size() == 0) {
                                        String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                        myRefUser1.push().setValue(msg);
                                        myRefUser2.push().setValue(msg);
                                    }

                                    myRefUser1.push().setValue(mAuth.getCurrentUser().getUid() + currentTimeFromServer + "read1" + "flag1" + date + FileName);              //myRefUser1.push().setValue(user.getUid()+now.hour + ":" + now.minute + value_of_massage.getText().toString() );
                                    myRefUser2.push().setValue(mAuth.getCurrentUser().getUid() + currentTimeFromServer + "read0" + "flag1" + date + FileName);

                                    if (countNoReadMessage == null) {
                                        countNoReadMessage = "0";
                                    }
                                    int count = Integer.parseInt(countNoReadMessage);
                                    count++;
                                    countNoReadMessage = String.valueOf(count);

                                    myRef.child(friendID).child("chats").child(user.getUid()).child("noread").setValue(countNoReadMessage);

                                }
                            }
                        });
                    }
                    else
                    {
                        String currentTimeFromServer = getServerTimeRightNow(ChatActivity.this);
                        String date = mSettings.getString("currentDate","");
                        if (massage.size() > 1) {
                            if (DateTime.dateCompare(date, massage.get(massage.size() - 1).getDate()) == true) {
                                String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                groupChatsRef.child(friendUser.getId()).child("message").push().setValue(msg);                        }
                        }
                        if (massage.size() == 0) {
                            String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                            groupChatsRef.child(friendUser.getId()).child("message").push().setValue(msg);

                        }
                        groupChatsRef.child(friendUser.getId()).child("message").push().setValue(mAuth.getCurrentUser().getUid() + currentTimeFromServer + "read1" + "flag1" +date+ FileName);
                        if (countNoReadMessage == null) {
                            countNoReadMessage = "0";
                        }
                        int count = Integer.parseInt(countNoReadMessage);
                        count++;
                        countNoReadMessage = String.valueOf(count);

                        groupChatsRef.child(friendUser.getId()).child("contacts").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                if (!dataSnapshot.getKey().equals(user.getUid())) {
                                    fcmNotificationPart1(myName + "(" + friendUser.getUsername() + ")", "Голосовое сообщение", dataSnapshot.getKey(), friendUser.getId());
                                }
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        putLastMessageAndHisTimeForChat(user.getUid(), friendID, currentTimeFromServer, "Голосовое сообщение");
                    }
                }
            }
        });

    }

    protected void startPlaying(String NameOfFile) {
        player = new MediaPlayer();
        try {
            final String playingFileStorage = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() +"/" +friendID + "/"  +  NameOfFile;
            File papka1 = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + this.getPackageName());
            if (!papka1.exists()) {
                papka1.mkdir();
            }
            File idPapka = new File(papka1 , friendID);
            if (!idPapka.exists()) {
                idPapka.mkdir();
            }
            final File playingFile = new File(idPapka,NameOfFile);
            if(playingFile.exists())
            {
                player.setDataSource(playingFileStorage);
                player.prepare();
                player.start();
            }
            else
            {
                if (friendUser.getIsGroupChat().equals("0")) {
                    mStorageRef.child(user.getUid()).child("chats").child(friendID).child(NameOfFile).getFile(playingFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            try {

                                player.setDataSource(playingFileStorage);
                                player.prepare();
                                player.start();
                            } catch (IOException e) {
                                Log.e(LOG_TAG, "prepare() failed");
                            }
                        }
                    });
                }
                else
                {
                    mStorageRef.child(friendID).child("chats").child(NameOfFile).getFile(playingFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            try {

                                player.setDataSource(playingFileStorage);
                                player.prepare();
                                player.start();
                            } catch (IOException e) {
                                Log.e(LOG_TAG, "prepare() failed");
                            }
                        }
                    });
                }
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void avatarDownload() {
        friendAvFile = new File(friendUser.getAvatarPuth());
        if(friendAvFile.length() != 0 || friendAvFile.exists())
        {
            friendAvatar.setImageURI(Uri.parse(friendAvFile.getAbsolutePath()));
        }
        else {
            friendAvatar.setImageResource(R.drawable.default_avatar);
        }
    }

    protected void onScrepkaClick()
    {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        Bitmap bitmap = null;
        switch(requestCode) {

            case GALLERY_REQUEST + 1:
                if(resultCode == RESULT_OK){
                    final File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + this.getPackageName() + "/" + friendUser.getId() + "/" +"avatar");
                    Uri selectedImage = imageReturnedIntent.getData();
                    bitmap = MainActivity.decodeSampledBitmapFromResource(MainActivity.getRealPathFromURI(this,selectedImage), 300,300);
                    friendAvatar.setImageBitmap(bitmap);
                    friendUser.setAvatarPuth(file.getAbsolutePath());
                    ChatFragment.chatHolderAdapter.notifyDataSetChanged();
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
                    mStorageRef.child(friendUser.getId()).child("avatar").putFile(Uri.parse(convertToFileURL(file.getAbsolutePath()))).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful())
                            {
                                groupChatsRef.child(friendUser.getId()).child("avatar").setValue(nameOfAvatar);
                                Toast.makeText(ChatActivity.this, "Аватарка успешно загружена!", Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this, "Во время загрузки аватара произошла ошибка, попробуйте снова.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                break;
            case GALLERY_REQUEST:
                if(resultCode == RESULT_OK){
                    final String FileOfImage = UUID.randomUUID().toString();
                    String fileName;
                    if (friendUser.getIsGroupChat().equals("0"))
                        fileName =Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() +"/" +friendID + "/"  +  FileOfImage;
                    else fileName =Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + this.getPackageName() +"/" + friendUser.getId() + "/"  +  FileOfImage;
                    final File file = new File(fileName);
                    Uri selectedImage = imageReturnedIntent.getData();
                    Display display = getWindowManager().getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;
                    bitmap =decodeSampledBitmapFromResource(getRealPathFromURI(this,selectedImage), width,height);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, fos);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    final Uri ura=Uri.fromFile(file);
                    if (friendUser.getIsGroupChat().equals("0")) {
                        StorageReference MyStoreUser1 = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("chats").child(friendID).child(FileOfImage);
                        final StorageReference MyStoreUser2 = FirebaseStorage.getInstance().getReference().child(friendID).child("chats").child((user.getUid())).child(FileOfImage);
                        MyStoreUser1.putFile(ura).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    MyStoreUser2.putFile(ura).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                String currentTimeFromServer = getServerTimeRightNow(ChatActivity.this);

                                                putLastMessageAndHisTime(user.getUid(), friendID, currentTimeFromServer, "Изображение");

                                                String date = mSettings.getString("currentDate","");
                                                if (massage.size() > 1) {
                                                    if (DateTime.dateCompare(date, massage.get(massage.size() - 1).getDate()) == true) {
                                                        String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                                        myRefUser1.push().setValue(msg);
                                                        myRefUser2.push().setValue(msg);
                                                    }
                                                }
                                                if (massage.size() == 0) {
                                                    String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                                    myRefUser1.push().setValue(msg);
                                                    myRefUser2.push().setValue(msg);
                                                }

                                                myRefUser1.push().setValue(mAuth.getCurrentUser().getUid() + currentTimeFromServer + "read1" + "flag2" + date + FileOfImage);              //myRefUser1.push().setValue(user.getUid()+now.hour + ":" + now.minute + value_of_massage.getText().toString() );
                                                myRefUser2.push().setValue(mAuth.getCurrentUser().getUid() + currentTimeFromServer + "read0" + "flag2"+date + FileOfImage);

                                                fcmNotificationPart1(myName,"Изображение",friendID,user.getUid());

                                                if (countNoReadMessage == null) {
                                                    countNoReadMessage = "0";
                                                }
                                                int count = Integer.parseInt(countNoReadMessage);
                                                count++;
                                                countNoReadMessage = String.valueOf(count);

                                                myRef.child(friendID).child("chats").child(user.getUid()).child("noread").setValue(countNoReadMessage);
                                            }
                                        }
                                    });
                                }
                            }
                        }); // Если групповой чат
                    }
                    if(friendUser.getIsGroupChat().equals("1")) {
                        StorageReference groupStorage = FirebaseStorage.getInstance().getReference().child(friendUser.getId()).child("chats").child(FileOfImage);
                        groupStorage.putFile(ura).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    String currentTimeFromServer = getServerTimeRightNow(ChatActivity.this);
                                    putLastMessageAndHisTimeForChat(user.getUid(), friendID, currentTimeFromServer, "Изображение");

                                    String date = mSettings.getString("currentDate","");
                                    if (massage.size() > 1) {
                                        if (DateTime.dateCompare(date, massage.get(massage.size() - 1).getDate()) == true) {
                                            String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                            groupChatsRef.child(friendUser.getId()).child("message").push().setValue(msg);                        }
                                    }
                                    if (massage.size() == 0) {
                                        String msg = user.getUid()+currentTimeFromServer +"read1" + "flag3"  +date + "";
                                        groupChatsRef.child(friendUser.getId()).child("message").push().setValue(msg);

                                    }
                                    groupChatsRef.child(friendUser.getId()).child("message").push().setValue(mAuth.getCurrentUser().getUid() + currentTimeFromServer + "read1" + "flag2"+date + FileOfImage);              //myRefUser1.push().setValue(user.getUid()+now.hour + ":" + now.minute + value_of_massage.getText().toString() );
                                    groupChatsRef.child(friendUser.getId()).child("contacts").addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                                            //   Log.d("GroupChatsLog",dataSnapshot.getKey());
                                            if (!dataSnapshot.getKey().equals(user.getUid())) {
                                                fcmNotificationPart1(myName + "(" + friendUser.getUsername() + ")", "Изображение", dataSnapshot.getKey(), friendUser.getId());
                                            }
                                        }
                                        @Override
                                        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                                        }

                                        @Override
                                        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        });
                    }
                }
        }
    }

    TextWatcher Search = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            if (charSequence.toString().equals(""))
            {
                print_massages.setAdapter(messageAdapter);
                messageAdapter.notifyDataSetChanged();
                print_massages.smoothScrollToPosition(massage.size());
            }
            else
            {
                searchList.clear();
                for (i=0; i < massage.size(); i++)
                {
                    String mainLine;
                    if (massage.get(i).getFlag() == 1)
                        mainLine = "Голосовое сообщение(нажмите, чтобы прослушать)";
                    else if (massage.get(i).getFlag() == 2)
                        mainLine = "";
                    else mainLine = massage.get(i).getMessage();
                    if ((mainLine.toLowerCase().contains(charSequence.toString().toLowerCase())))
                    {
                        searchList.add(massage.get(i));
                    }
                }
                print_massages.setAdapter(searchAdapter);
                searchAdapter.notifyDataSetChanged();
                print_massages.smoothScrollToPosition(searchList.size());
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };


    private boolean hasPermissions()
    {
        int audioPermissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (audioPermissionStatus == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO},
                    AUDIO_REQUEST_CODE);
        }
        return false;
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case AUDIO_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                }
                return;
        }
    }


    public void groupChatAvatarClick() {
        friendAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST+1);
            }
        });
    }

    public void avatarClick()
    {
        if(friendAvFile.length() != 0) {
            Intent intent = new Intent(this, ImageIncreasing.class);
            intent.putExtra("path", friendAvFile.getAbsolutePath());
            startActivity(intent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(massage.size() != 0) {
            myRef.child(user.getUid()).child("chats").child(friendUser.getId()).child("noread").setValue("0");
        }
    }

    public void getCountNoReadMessage() {
        myRef.child(friendID).child("chats").child(user.getUid()).child("noread").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                countNoReadMessage = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    static String getServerTimeRightNow(Context context)
    {
        Time now = new Time();
        now.setToNow();
        String timeMinute="";
        if (now.minute < 10)
        {
            timeMinute = "0"+now.minute;
        }
        else timeMinute="" + now.minute;
        String timeHour ="";
        if (now.hour < 10)
        {
            timeHour = "0" + now.hour;
        }
        else timeHour ="" + now.hour;
        SharedPreferences mSettings;
        String currentTimeFromServer;
        mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (mSettings.getString(APP_PREFERENCES_DifferenceInTime,"").substring(0,1).equals("+"))
            currentTimeFromServer = RecyclerMassageAdapter.returnTimeWithDifferences(context,timeHour + ":" + timeMinute,("-" + mSettings.getString(APP_PREFERENCES_DifferenceInTime,"").substring(1)));
        else if (mSettings.getString(APP_PREFERENCES_DifferenceInTime,"").substring(0,1).equals("-"))
            currentTimeFromServer = RecyclerMassageAdapter.returnTimeWithDifferences(context,timeHour + ":" + timeMinute,("+" + mSettings.getString(APP_PREFERENCES_DifferenceInTime,"").substring(1)));
        else currentTimeFromServer = timeHour + ":" + timeMinute;

        return currentTimeFromServer;
    }

    static void putLastMessageAndHisTime(String userId,String friendID,String currentTimeFromServer, String valueOfMessage)
    {
        String lastMessDate = DateTime.getDateTime();
        if (!currentTimeFromServer.equals(""))
            currentTimeFromServer = lastMessDate.substring(0,6) + currentTimeFromServer + lastMessDate.substring(11);

        if (!friendID.substring(friendID.length() - 9).equals("groupchat")) {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("users");

            myRef.child(userId).child("chats").child(friendID).child("lastmessagedate").setValue(currentTimeFromServer);
            myRef.child(friendID).child("chats").child(userId).child("lastmessagedate").setValue(currentTimeFromServer);

            myRef.child(userId).child("chats").child(friendID).child("lastmessage").setValue(valueOfMessage);
            myRef.child(friendID).child("chats").child(userId).child("lastmessage").setValue(valueOfMessage);
        }
        else
        {
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("groupChats");
            myRef.child(friendID).child("lastmessagedate").setValue(currentTimeFromServer);
            myRef.child(friendID).child("lastmessage").setValue(valueOfMessage);
        }
    }


    void fcmNotificationPart1(String title,String message,String idToNotification,String myId)
    {
        TOPIC = "/topics/notification_"+idToNotification; //topic must match with what the receiver subscribed to
        NOTIFICATION_TITLE = title;
        NOTIFICATION_MESSAGE = message;

        String nameOfAvatarFile = "";
        if (friendUser.getIsGroupChat().equals("0"))
        {
            nameOfAvatarFile = mSettings.getString("userAvatarPath","");
        }
        else
        {
            String lengthOfAvatarPath = Environment.getExternalStorageDirectory() + "/Android/data/" + this.getPackageName() + "/" + friendUser.getId() + "/";
            nameOfAvatarFile = friendUser.getAvatarPuth().substring(friendUser.getAvatarPuth().length()- (friendUser.getAvatarPuth().length() - lengthOfAvatarPath.length()));
        }
        JSONObject notification = new JSONObject();
        JSONObject notifcationBody = new JSONObject();
        try {
            notifcationBody.put("title", NOTIFICATION_TITLE);
            notifcationBody.put("message", myId + NOTIFICATION_MESSAGE);
            notifcationBody.put("avatarPath",nameOfAvatarFile);

            notification.put("to", TOPIC);
            notification.put("data", notifcationBody);
        } catch (JSONException e) {
            Log.e(TAG, "onCreate: " + e.getMessage() );
        }
        sendNotificationFCMpart2(notification);
    }

    private void sendNotificationFCMpart2(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatActivity.this, "Request error", Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void showGroupChatPopupMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.Popup);
        PopupMenu popupMenu = new PopupMenu(wrapper, v);
        popupMenu.inflate(R.menu.group_chat_menu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.add_contact_group_chat_item:
                                addContactGroupChat();
                                return true;
                            case R.id.change_group_item_name:
                                changeGroupName();
                                return true;
                            case R.id.show_group_info_item:
                                showGroupInfo();
                                return true;
                            case R.id.leave_group_item:
                                leaveGroup();
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

    private void showGroupInfo() {
        Intent intent = new Intent(this, GroupChatInfo.class);
        intent.putExtra("groupuser",(Parcelable) friendUser);
        startActivity(intent);
    }

    private void leaveGroup() {
        myRef.child(user.getUid()).child("chats").child(friendUser.getId()).removeValue();
        groupChatsRef.child(friendUser.getId()).child("contacts").child(user.getUid()).removeValue();
        finish();
    }

    private void addContactGroupChat() {
        Intent intent = new Intent(this, NewGroupContactActivity.class);
        intent.putExtra("groupchat", 1);
        intent.putParcelableArrayListExtra("contactlist",userContactList);
        intent.putStringArrayListExtra("contactgroupchatlist",groupChatContactList);
        intent.putExtra("groupid", friendUser.getId());
        startActivity(intent);
    }

    private void showChatPopupMenu(View v) {
        Context wrapper = new ContextThemeWrapper(this, R.style.Popup);
        PopupMenu popupMenu = new PopupMenu(wrapper, v);
        popupMenu.inflate(R.menu.chat_menu);

        popupMenu
                .setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.add_contact_item:
                                myRef.child(user.getUid()).child("contacts").child(friendID).setValue("");
                                return true;
                            case R.id.show_profile_item:

                                return true;
                            case R.id.clear_chat_item:
                                clearChat();
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

    private void changeGroupName() {
        Intent intent = new Intent(this, ChangeGroupNameActivity.class);
        intent.putExtra("userchat", (Parcelable) friendUser);
        startActivity(intent);
    }

    private void clearChat() {
        massage.clear();
        myRefUser1.removeValue();
        myRefUser1.push().setValue("9JqWaOeX9GgmIT3VYFBwbxPLC0K218:09read1flag0");
    }

    private void getUserContactList() {
        new DataBase(this).getContacts(userContactList);
    }
    private void getGroupChatContactList() {
        groupChatsRef.child(friendUser.getId()).child("contacts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String id = dataSnapshot.getKey();
                groupChatContactList.add(id);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (RecyclerMassageAdapter.TTS != null)
        {
            RecyclerMassageAdapter.TTS.stop();
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
                    myUserDB.child("status").setValue("online");
                } else {
                    myUserDB.child("status").setValue("");
                }
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(massage.size() != 0) {
            myRef.child(user.getUid()).child("chats").child(friendUser.getId()).child("noread").setValue("0");
        }
        if (mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
            myUserDB.child("status").setValue(DateTime.getDateTime());
        } else {
            myUserDB.child("status").setValue("");
        }

        if (RecyclerMassageAdapter.TTS != null)
        {
            RecyclerMassageAdapter.TTS.stop();
        }
    }

    @Override
    public void onBackPressed() {
        ActivityManager mngr = (ActivityManager) getSystemService( ACTIVITY_SERVICE );

        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);
        if(taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(this.getClass().getName()))
        {
            User myUser = new User();
            Intent intent = new Intent(this, MainActivity.class);
            myUser.setMail(user.getEmail());
            myUser.setId(user.getUid());
            myUser.setStatus("online");
            intent.putExtra("user", (Parcelable) myUser);
            startActivity(intent);
        }
        else {
            super.onBackPressed();
        }
    }


    static void getCurrentTimeFromFirebaseServerFast(Context context)
    {
        final SharedPreferences mSettings = context.getSharedPreferences(APP_PREFERENCES,Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = mSettings.edit();
        readData(new MyCallback() {
            @Override
            public void onCallback(long mills) {
                Calendar currentDateFromServer = Calendar.getInstance();
                long calendarMills = currentDateFromServer.getTimeInMillis();
                currentDateFromServer.setTimeInMillis(mills);
                TimeZone mTimeZone = currentDateFromServer.getTimeZone();
                // Московский часовой пояс
                int mskGMT = TimeZone.getTimeZone("Europe/Moscow").getOffset(mills);
                // Часовой пояс пользователя
                int mGMTOffset = mTimeZone.getOffset(mills);
                // Приведение часового пояса к стандартному
                int resultGMT = mskGMT - mGMTOffset;
                currentDateFromServer.setTimeInMillis(mills + resultGMT);
                // может быть придётся увеличить день месяца на 1.
                String currentDate;
                String month;
                String day;
                if (currentDateFromServer.get(Calendar.MONTH) <9) {
                    month = "0" + (currentDateFromServer.get(Calendar.MONTH) + 1);
                }
                else month = "" + (currentDateFromServer.get(Calendar.MONTH) + 1);
                if (currentDateFromServer.get(Calendar.DAY_OF_MONTH) <= 9)
                {
                    day = "0" + currentDateFromServer.get(Calendar.DAY_OF_MONTH);
                }
                else day = "" + currentDateFromServer.get(Calendar.DAY_OF_MONTH);
                currentDate = day +"." + month;
                editor.putString("currentDate",currentDate);
                String currentTime = DateTime.getTime_Hour_And_Minute(currentDateFromServer.get(Calendar.HOUR_OF_DAY),currentDateFromServer.get(Calendar.MINUTE));
                editor.putString(APP_PREFERENCES_TIME,currentTime);
                editor.putString(APP_PREFERENCES_DifferenceInTime,MainActivity.checkDifference(currentTime));
                editor.apply();
            }
        });
    }

    void checkToLastActivity()
    {
        ActivityManager mngr = (ActivityManager) getSystemService( ACTIVITY_SERVICE );

        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);
        if(taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(this.getClass().getName()))
        {
            getCurrentTimeFromFirebaseServerFast(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    static void readData(final MyCallback myCallback)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference databaseTime = database.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("currentTime");
        databaseTime.setValue(ServerValue.TIMESTAMP).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                databaseTime.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        myCallback.onCallback(dataSnapshot.getValue(Long.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }


    public interface MyCallback {
        void onCallback(long mills);
    }

    void checkChangeOfAvatar()
    {
        DatabaseReference databaseReference;
        if (friendUser.getIsGroupChat().equals("0"))
        {
            databaseReference = FirebaseDatabase.getInstance().getReference().child("users").child(friendUser.getId()).child("avatar");
        }
        else databaseReference = FirebaseDatabase.getInstance().getReference().child("groupChats").child(friendUser.getId()).child("avatar");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String lengthOfAvatarPath = Environment.getExternalStorageDirectory() + "/Android/data/" + ChatActivity.this.getPackageName() + "/" + friendUser.getId() + "/";
                String nameOfAvatarFile = friendUser.getAvatarPuth().substring(friendUser.getAvatarPuth().length()- (friendUser.getAvatarPuth().length() - lengthOfAvatarPath.length()));
                if (!dataSnapshot.getValue(String.class).equals(nameOfAvatarFile))
                {
                    String newAvatarNameFromDatabase = dataSnapshot.getValue(String.class);
                    if (newAvatarNameFromDatabase.equals(""))
                        newAvatarNameFromDatabase = "nothingFile";
                    friendUser.setAvatarPuth(lengthOfAvatarPath + newAvatarNameFromDatabase);
                    final File newAvatarFile = new File(friendUser.getAvatarPuth());
                    if (!newAvatarFile.exists()) {
                        mStorageRef.child(friendUser.getId()).child("avatar").getFile(newAvatarFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                                if (task.isSuccessful())
                                {
                                    friendAvatar.setImageURI(null);
                                    friendAvatar.setImageURI(Uri.fromFile(newAvatarFile));
                                }
                                else
                                {
                                    friendAvatar.setImageURI(null);
                                    friendAvatar.setBackgroundResource(R.drawable.default_avatar);
                                }
                            }
                        });
                    }
                    else
                    {
                        friendAvatar.setImageURI(null);
                        friendAvatar.setImageURI(Uri.parse(newAvatarFile.getAbsolutePath()));
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}