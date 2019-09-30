package android.example.mas;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AllContactsActivity extends AppCompatActivity {
    // Databases
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;
    StorageReference mStorageRef;
    DataBase myDataBase;
    // View
    RecyclerView allContactsRW;
    EditText searchContacts;
    LinearLayout allContll;
    // Adapters
    ContactsHolderAdapter allContactsHolderAdapter;
    ContactsHolderAdapter searchAdapter;
    ArrayList<User> allContactList;
    ArrayList<User> searchList;
    public void initialization()
    {
        myDataBase = new DataBase(AllContactsActivity.this);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        // View
        allContactsRW = findViewById(R.id.rw_all_contacts);
        searchContacts = findViewById(R.id.search_contacts_edittext);
        allContll = findViewById(R.id.all_cont_ll);
        // Adapters
        allContactList = new ArrayList<User>();
        searchList = new ArrayList<>();
        allContactsHolderAdapter = new ContactsHolderAdapter(allContactList, 3);
        searchAdapter = new ContactsHolderAdapter(searchList,3);
        allContactsRW.setAdapter(allContactsHolderAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        allContactsRW.setLayoutManager(layoutManager);

        MainActivity.myUserDB.child("status").setValue("online");

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_contacts);
        initialization();
        contactOutput();
        allContactsHolderAdapter.notifyDataSetChanged();
                searchContacts.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if(s.toString().equals(""))
                {
                    allContactsRW.setAdapter(allContactsHolderAdapter);
                    allContactsHolderAdapter.notifyDataSetChanged();
                }
                else
                {
                    allContactsRW.setAdapter(searchAdapter);
                    searchList.clear();
                    for(int i = 0; i<allContactList.size(); i++)
                    {
                        try {
                            if (s.toString().toLowerCase().equals(allContactList.get(i).getUsername().substring(0, s.toString().length()).toLowerCase())) {
                                searchList.add(allContactList.get(i));
                            }
                        }catch (StringIndexOutOfBoundsException e)
                        {

                        }
                    }
                    searchAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }



    public void contactOutput()
    {
        myRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.getKey().toString().equals(user.getUid()));
                else
                {
                    final User userClass = new User();
                    final String userID= dataSnapshot.getKey();
                    userClass.setIsGroupChat("0");
                    dataSnapshot.getRef().child("avatar").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            File file = null;
                            try {
                                file = new DataBase(AllContactsActivity.this).avatarDownload(userID,dataSnapshot.getValue(String.class),allContactsHolderAdapter);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            userClass.setAvatarPuth(file.getAbsolutePath());
                            allContactsHolderAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    //String nameOfFriendFile = dataSnapshot.child("avatar").getValue(String.class);
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference mostafa = ref.child("users").child(userID).child("username");      // Путь к хранилищу users /  ID пользователя / username /
                    mostafa.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {                   // Берёт значение, содержащееся в users /  ID пользователя / username /
                            String username = dataSnapshot.getValue(String.class);
                            userClass.setUsername(username);
                            allContactsHolderAdapter.notifyDataSetChanged();

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
                            try {
                              userClass.setStatus(status);
                            } catch (NullPointerException e) {

                            }
                            allContactsHolderAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    userClass.setId(userID);
                    allContactList.add(userClass);
                    allContactsHolderAdapter.notifyDataSetChanged();
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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

    @Override
    protected void onPause() {
        super.onPause();

        if (MainActivity.mSettings.getString(MainActivity.ONLINE_PREFERENCE, "").equals("0")) {
            MainActivity.myUserDB.child("status").setValue(DateTime.getDateTime());
        } else {
            MainActivity.myUserDB.child("status").setValue("");
        }
    }

    //@Override
    protected void onStop() {
        super.onStop();
        Log.d("TimLog", "сработал стоп AllContacts");


    }
}
