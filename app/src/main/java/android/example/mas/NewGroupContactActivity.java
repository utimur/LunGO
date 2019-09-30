package android.example.mas;

import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.UUID;

public class NewGroupContactActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;
    DatabaseReference groupChatsRef;
    RecyclerView contactsRecyclerView;
    FloatingActionButton newGroupFab;
    EditText searchEditText;
    ContactsHolderAdapter contactsHolderAdapter;
    public StorageReference mStorageRef;

    ArrayList<User> contactList;
    ArrayList<String> groupChatContact;
    ArrayList<User> userContact;
    int isGroupChat;

    String groupId;
    private static final int GROUP_CHAT =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group_contact);

        isGroupChat = 0;
        Intent intent = getIntent();

        try {
            isGroupChat = intent.getIntExtra("groupchat", 0);
        } catch (RuntimeException e) {
            isGroupChat = 0;
        }
        groupChatContact = intent.getStringArrayListExtra("contactgroupchatlist");
        userContact = intent.getParcelableArrayListExtra("contactlist");

        initialization();

        contactList = new ArrayList<>();

        contactsHolderAdapter = new ContactsHolderAdapter(contactList, ContactsHolderAdapter.NEW_GROUP_FLAG);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        contactsRecyclerView.setAdapter(contactsHolderAdapter);

        if (isGroupChat == GROUP_CHAT) { // Если добавляешь участника

            filtredList();
            groupId = intent.getStringExtra("groupid");
            fabClickAddNewContactInGroup();
        } else { // Если создаешь групповой чат

            new DataBase(this).contactsDbChange(contactList,contactsHolderAdapter);
            fabClick();
        }


    }

    public void initialization() {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        groupChatsRef = database.getReference("groupChats");
        contactsRecyclerView = findViewById(R.id.new_group_rv);
        newGroupFab = findViewById(R.id.new_group_fab);
        searchEditText = findViewById(R.id.new_group_search_et);
        mStorageRef = FirebaseStorage.getInstance().getReference();




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

    public void fabClick() {

        newGroupFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String chatsId = UUID.randomUUID().toString() + "groupchat";

                myRef.child(user.getUid()).child("chats").child(chatsId).child("noread").setValue("0");
                groupChatsRef.child(chatsId).child("lastmessage").setValue("");
                groupChatsRef.child(chatsId).child("lastmessagedate").setValue(DateTime.getDateTime());
                groupChatsRef.child(chatsId).child("name").setValue("Новая группа");
                groupChatsRef.child(chatsId).child("avatar").setValue("standart");

                groupChatsRef.child(chatsId).child("contacts").child(user.getUid()).setValue("");

                for(int i = 0; i<contactList.size(); i++)
                {
                    if(contactList.get(i).getDelete() == 1)
                    {
                        myRef.child(contactList.get(i).getId()).child("chats").child(chatsId).child("noread").setValue("0");
                        groupChatsRef.child(chatsId).child("contacts").child(contactList.get(i).getId()).setValue("");
                    }
                }

                finish();
            }

        });
    }

    public void fabClickAddNewContactInGroup() {
        newGroupFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i<contactList.size(); i++)
                {
                    if(contactList.get(i).getDelete() == 1)
                    {
                        myRef.child(contactList.get(i).getId()).child("chats").child(groupId).child("noread").setValue("0");
                        groupChatsRef.child(groupId).child("contacts").child(contactList.get(i).getId()).setValue("");
                    }
                }

                finish();
            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
            }
    public void filtredList() {

    //  for(int i = 0; i< groupChatContact.size(); i++)
    //  {
    //      for(int k = 0; k < userContact.size(); k++)
    //      {
    //          if (userContact.get(k).getId().equals(groupChatContact.get(i))) {
    //              Log.d("TimLog", "Зашло в условие " + userContact.get(i).getId());
    //              Log.d("TimLog", "Зашло в условие2 " + groupChatContact.get(k));
    //              userContact.remove(k);
    //          }
    //      }
    //  }
    //  contactsHolderAdapter.notifyDataSetChanged();
        myRef.child(user.getUid()).child("contacts").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                {

                    final User userClass = new User();
                    final String userID= dataSnapshot.getKey();
                    userClass.setIsGroupChat("0");
                    File file = null;
                    try {
                        file = avatarDownload(userID);
                        userClass.setAvatarPuth(file.getAbsolutePath());
                        contactsHolderAdapter.notifyDataSetChanged();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference mostafa = ref.child("users").child(userID).child("username");      // Путь к хранилищу users /  ID пользователя / username /
                    mostafa.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {                   // Берёт значение, содержащееся в users /  ID пользователя / username /
                            String username = dataSnapshot.getValue(String.class);
                            userClass.setUsername(username);
                            contactsHolderAdapter.notifyDataSetChanged();

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
                            String status1;
                            if(!status.equals("online"))
                            {
                                status1 = "Был " + status;
                                userClass.setStatus(status1);
                            }else userClass.setStatus(status);
                            contactsHolderAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    userClass.setId(userID);
                    contactList.add(userClass);
                    for (int i = 0; i < groupChatContact.size(); i++) {
                        if(userClass.getId().equals(groupChatContact.get(i)))
                        {
                            Log.d("TimLog", "Зашло " + contactList.get(contactList.size() - 1).getId());
                            Log.d("TimLog", "Зашло " + groupChatContact.get(i));
                            contactList.remove(contactList.size()-1);
                        }
                    }

                    contactsHolderAdapter.notifyDataSetChanged();
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
    public File avatarDownload( String userID) throws IOException {
        File externalAppDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + getPackageName());
        if (!externalAppDir.exists()) {
            externalAppDir.mkdir();
        }
        File idPapka = new File(externalAppDir , userID);
        if (!idPapka.exists()) {
            idPapka.mkdir();
        }
        final File file =  new File(idPapka.getAbsolutePath(), "avatar");
        mStorageRef.child(userID).child("avatar").getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

            }
        });
        return file;
    }
}