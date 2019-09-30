package android.example.mas;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GroupChatInfo extends AppCompatActivity {

    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public FirebaseDatabase database;
    public DatabaseReference myRef ;
    public DatabaseReference groupChatRef ;
    public StorageReference mStorageRef;

    RecyclerView recyclerView;
    ContactsHolderAdapter contactsAdapter;
    ArrayList<User> userList;


    TextView groupNameTextView;
    ImageView avatarGroup;
    User chatUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_info);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        groupChatRef = database.getReference("groupChats");
        mStorageRef = FirebaseStorage.getInstance().getReference();

        Intent intent = getIntent();
        chatUser = intent.getParcelableExtra("groupuser");

        recyclerView = findViewById(R.id.group_info_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        contactsAdapter = new ContactsHolderAdapter(userList, 3);
        recyclerView.setAdapter(contactsAdapter);

        contactsDbChange();


        groupNameTextView = findViewById(R.id.group_name_info_tv);
        groupNameTextView.setText(chatUser.getUsername());

        avatarGroup = findViewById(R.id.group_avatar_info_iv);

        File file = new File(chatUser.getAvatarPuth());
        if(file.length() != 0 || file.exists())
        {
            avatarGroup.setImageURI(Uri.parse(file.getAbsolutePath()));

        }
        else {
            avatarGroup.setImageResource(R.drawable.default_avatar);
        }

    }

    public void contactsDbChange() {
        groupChatRef.child(chatUser.getId()).child("contacts").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                {
                    Log.d("TimLog", "Вызовался контакт");
                    final User userClass = new User();
                    final String userID= dataSnapshot.getKey();
                    userClass.setIsGroupChat("0");
                    myRef.child(userID).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            File file = null;
                            try {
                                file = avatarDownload(userID,dataSnapshot.getValue(String.class));
                                userClass.setAvatarPuth(file.getAbsolutePath());
                                contactsAdapter.notifyDataSetChanged();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference mostafa = ref.child("users").child(userID).child("username");      // Путь к хранилищу users /  ID пользователя / username /
                    mostafa.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {                   // Берёт значение, содержащееся в users /  ID пользователя / username /
                            String username = dataSnapshot.getValue(String.class);
                            userClass.setUsername(username);
                            contactsAdapter.notifyDataSetChanged();

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    DatabaseReference mostafa1 = ref.child("users").child(userID).child("status");          // Путь к хранилищу users /  ID пользователя / mail /
                    mostafa1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {                    // Берёт значение, содержащееся в users /  ID пользователя / username /
                            String status = dataSnapshot.getValue(String.class);
                            userClass.setStatus(status);
                            contactsAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    userClass.setId(userID);
                    if (!userClass.getId().equals(user.getUid())) {
                        userList.add(userClass);

                    }
                    contactsAdapter.notifyDataSetChanged();
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

    public File avatarDownload(final String userID,String nameOfAvatarFile) throws IOException {

        //   File file = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName());
        //  if (nameOfAvatarFile == null)
        //  {
        //      Thread thread = new Thread();
        //      thread.run();
        //      try {
        //          thread.sleep(100);
        //      } catch (InterruptedException e) {
        //          e.printStackTrace();
        //      }
        //  }
        if (nameOfAvatarFile == null || nameOfAvatarFile.equals(""))
            nameOfAvatarFile = "fileIsNotExist";

        File externalAppDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + GroupChatInfo.this.getPackageName());
        if (!externalAppDir.exists()) {
            externalAppDir.mkdir();
        }
        File idPapka = new File(externalAppDir, userID);
        if (!idPapka.exists()) {
            idPapka.mkdir();
        }
        Log.d("Zheka", "File = " + nameOfAvatarFile);
        //    if (nameOfAvatarFile != null && !nameOfAvatarFile.equals("")) {
        Log.d("Zheka", "ku-ku");
        File file = new File(idPapka.getAbsolutePath(), nameOfAvatarFile);
        Log.d("Zheka", file.getAbsolutePath());
        if (!file.exists() && !nameOfAvatarFile.equals("standart") && !nameOfAvatarFile.equals("fileIsNotExist")) {
            Log.d("Zheka", "ku downloAD");

            mStorageRef.child(userID).child("avatar").getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Log.d("Zheka", "ku downloAD success");

                }
            });
        }
        //   }
        Log.d("Zheka", file.getAbsolutePath());
        return file;

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
