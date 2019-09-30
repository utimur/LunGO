package android.example.mas;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

public class DataBase  {
    public FirebaseAuth mAuth;
    public FirebaseUser user;
    public FirebaseDatabase database;
    public DatabaseReference myRef ;
    public DatabaseReference groupChatRef ;
    public StorageReference mStorageRef;
    public Context context;
    final String[] userNickname = new String[1];
    public String nameOfUserFile;


    public DataBase(Context ctx) {
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        groupChatRef = database.getReference("groupChats");
        mStorageRef = FirebaseStorage.getInstance().getReference();
        context = ctx;
    }

    public File avatarDownload(final String userID, String nameOfAvatarFile, final ContactsHolderAdapter contactsHolderAdapter)  throws IOException {

        if (nameOfAvatarFile == null || nameOfAvatarFile.equals(""))
            nameOfAvatarFile = "fileIsNotExist";

        File externalAppDir = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName());
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
                        contactsHolderAdapter.notifyDataSetChanged();

                    }
                });
            }
    //   }
        Log.d("Zheka", file.getAbsolutePath());
        return file;

    }



    public void chatDBchange(final ArrayList<User> chatList, final ContactsHolderAdapter chatHolderAdapter)
    {
        myRef.child(user.getUid()).child("chats").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final User chatUser = new User();
                final String contID = dataSnapshot.getKey();
                chatUser.setId(contID);
                if(contID.substring(contID.length()-9).equals("groupchat")) {
                        chatUser.setIsGroupChat("1");
                        groupChatRef.child(contID).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                try {
                                    nameOfUserFile = dataSnapshot.child("avatar").getValue(String.class);
                                    File file = avatarDownload(contID,nameOfUserFile,chatHolderAdapter);
                                    chatUser.setAvatarPuth(file.getAbsolutePath());
                                    chatHolderAdapter.notifyDataSetChanged();

                                    //String avatar = dataSnapshot.child("avatar").getValue(String.class);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String groupLastMessageDate = dataSnapshot.child("lastmessagedate").getValue(String.class);
                                chatUser.setLastMessageDate(groupLastMessageDate);

                                String groupNoRead = dataSnapshot.child("noread").getValue(String.class);
                                chatUser.setCountNoRedMessage(groupNoRead);

                                String groupLastMessage = dataSnapshot.child("lastmessage").getValue(String.class);
                                chatUser.setLastMessage(groupLastMessage);

                                String groupName = dataSnapshot.child("name").getValue(String.class);
                                chatUser.setUsername(groupName);
                                ChatFragment.sortArray(chatList);
                                chatHolderAdapter.notifyDataSetChanged();
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {                            }
                        });
                    chatList.add(chatUser);
                    chatHolderAdapter.notifyDataSetChanged();

                }
                else {
                    chatUser.setIsGroupChat("0");
                    if(dataSnapshot.child("message").getChildrenCount() > 0) {
                        myRef.child(contID).child("avatar").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                File file = null;
                                try {
                                    file = avatarDownload(contID,dataSnapshot.getValue(String.class),chatHolderAdapter);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                chatUser.setAvatarPuth(file.getAbsolutePath());
                                chatHolderAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        database.getReference().child("users").child(contID).child("username").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                chatUser.setUsername(dataSnapshot1.getValue(String.class));
                                chatHolderAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        database.getReference().child("users").child(contID).child("status").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                try {


                                    String status = dataSnapshot1.getValue(String.class);
                                        chatUser.setStatus(status);

                                    chatHolderAdapter.notifyDataSetChanged();
                                } catch (NullPointerException e) {

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        // Get lastMessage date
                        database.getReference().child("users").child(user.getUid()).child("chats").child(contID).child("lastmessagedate").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                chatUser.setLastMessageDate(dataSnapshot1.getValue(String.class));
                                ChatFragment.sortArray(chatList);
                                chatHolderAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });

                        // Get lastMessage
                        database.getReference().child("users").child(user.getUid()).child("chats").child(contID).child("lastmessage").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                chatUser.setLastMessage(dataSnapshot1.getValue(String.class));
                                chatHolderAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        // Get lastMessage
                        database.getReference().child("users").child(user.getUid()).child("chats").child(contID).child("noread").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot1) {
                                if (dataSnapshot1.getValue() != null) {
                                    chatUser.setCountNoRedMessage(dataSnapshot1.getValue(String.class));
                                    chatHolderAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                        chatList.add(chatUser);
                        chatHolderAdapter.notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                for (int i = 0; i < chatList.size(); i++) {
                    if (dataSnapshot.getKey().equals(chatList.get(i).getId())) {
                        chatList.remove(i);

                        chatHolderAdapter.notifyDataSetChanged();
                        
                    }
                }
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void contactsDbChange(final ArrayList<User> contactList, final ContactsHolderAdapter contactsHolderAdapter) {

        myRef.child(user.getUid()).child("contacts").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                {
                    final User userClass = new User();
                    final String userID= dataSnapshot.getKey();
                    userClass.setIsGroupChat("0");
                    myRef.child(userID).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            File file = null;
                            try {
                                file = avatarDownload(userID,dataSnapshot.getValue(String.class),contactsHolderAdapter);
                                userClass.setAvatarPuth(file.getAbsolutePath());
                                contactsHolderAdapter.notifyDataSetChanged();
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
                            userClass.setStatus(status);
                            contactsHolderAdapter.notifyDataSetChanged();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    userClass.setId(userID);
                    contactList.add(userClass);
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

    public void getContacts(final ArrayList<User> arrayList) {
        myRef.child(user.getUid()).child("contacts").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                {
                    final ContactsHolderAdapter contactsHolderAdapterMain = new ContactsHolderAdapter(new ArrayList<User>(),3);
                    final User userClass = new User();
                    final String userID= dataSnapshot.getKey();
                    userClass.setIsGroupChat("0");
                    myRef.child(userID).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            File file = null;
                            try {
                                file = avatarDownload(userID,dataSnapshot.getValue(String.class),contactsHolderAdapterMain);
                                userClass.setAvatarPuth(file.getAbsolutePath());
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
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    userClass.setId(userID);
                    arrayList.add(userClass);

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


    public interface MyCallbackForAvatar {
        void onCallbackAvatar(String nameOfAvatarFile);
    }

    void waitOfNameAvatar(String idOfUser,final MyCallbackForAvatar myCallbackForAvatar)
    {
        myRef.child(idOfUser).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myCallbackForAvatar.onCallbackAvatar(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    void returnAvatarName(String userId)
    {
        waitOfNameAvatar(userId, new MyCallbackForAvatar() {
            @Override
            public void onCallbackAvatar(String nameOfAvatarFile) {
                nameOfUserFile = nameOfAvatarFile;
            }
        });
    }
}