package android.example.mas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CheckedOutputStream;

public class ContactsHolderAdapter extends RecyclerView.Adapter<ContactsHolderAdapter.ContactsViewHolder> {

    ArrayList<User> arrayList;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef;
    DatabaseReference groupChatsRef;
    static int massClick;

    // Значения флага
    // 1. РАБОТА С ЧАТАМИ
    // 2. РАБОТА С КОНТАКТАМИ
    // 3. РАБОТА СО ВСЕМИ ПОЛЬЗОВАТЕЛЯМИ
    // 4. РАБОТА С ГРУППОВЫМИ КОНТАКТАМИ

    int flag;

    public static int CHAT_FLAG = 1;
    public static int CONTACT_FLAG = 2;
    public static int ALL_CONTACT_FLAG = 3;
    public static int NEW_GROUP_FLAG = 4;


    public ContactsHolderAdapter(@NonNull ArrayList<User> arrayList, int flag) {
        this.arrayList = arrayList;
        this.flag = flag;
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        groupChatsRef = database.getReference("groupChats");
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.contacts_layout, viewGroup, false);
        ContactsViewHolder contactsViewHolder = new ContactsViewHolder(view, arrayList);
        return contactsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder contactsViewHolder, int i) {
        try {
            contactsViewHolder.bind(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private TextView lastMessage;
        private TextView status;
        private TextView countNoRead;
        private ImageView status_label;
        private ImageView avatar;
        private File file;


        public ContactsViewHolder(@NonNull final View view, ArrayList<User> contactsList) {
            super(view);
            LayoutInflater inflater = LayoutInflater.from(view.getContext());
            username = (TextView) view.findViewById(R.id.username_label);
            countNoRead = (TextView) view.findViewById(R.id.no_read_label);
            status = (TextView) view.findViewById(R.id.on_off_status_label);
            lastMessage = (TextView) view.findViewById(R.id.info_status_label);
            avatar = (ImageView) view.findViewById(R.id.avatar_view);
            status_label = (ImageView) view.findViewById(R.id.status_label);

            if (flag == CHAT_FLAG || flag == CONTACT_FLAG || flag == ALL_CONTACT_FLAG) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemClick(v);
                    }
                });
            }

            // Нажатие на аватарку
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    avatarClick();
                }
            });
            // Обработка длительного нажатия в чатах
            if (flag == CHAT_FLAG) {
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        itemChatLongClick();
                        return false;
                    }
                });
            }
            if (flag == CONTACT_FLAG) {
                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        itemContactsLongClick();
                        return false;
                    }
                });
            }
            if (flag == NEW_GROUP_FLAG) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    newGroupClick();
                    }
                });
            }
        }

        // Замена данных в элемента ресайклера
        void bind(int position) throws IOException {

            lastMessage.setText(arrayList.get(position).getLastMessage());
            username.setText(arrayList.get(position).getUsername());


            status.setText(arrayList.get(position).getStatus());
            countNoRead.setText(arrayList.get(position).getCountNoRedMessage());

            if (arrayList.get(position).getCountNoRedMessage() == null || arrayList.get(position).getCountNoRedMessage().equals("0")) {
                countNoRead.setVisibility(View.INVISIBLE);
            } else {
                countNoRead.setVisibility(View.VISIBLE);
            }


            try {
                Log.d("Zheka","avatarPathSetImage = " + arrayList.get(position).getAvatarPuth() + " username: " + arrayList.get(position).getUsername());
               // avatar.invalidate();
                file = new File(arrayList.get(position).getAvatarPuth());
                if (!file.exists()) {

                    avatar.setImageURI(null);
                    avatar.setBackgroundResource(R.drawable.default_avatar);
                }
                else
                {
                    avatar.setImageURI(null);
                    avatar.setImageURI(Uri.parse(arrayList.get(position).getAvatarPuth()));
                }
            } catch (NullPointerException e)
            {
                avatar.setBackgroundResource(R.drawable.default_avatar);
            }
            // Фон выделенных контактов
            if (arrayList.get(position).getDelete() == 0) {
                itemView.setBackgroundResource(R.drawable.line);
            } else {
                itemView.setBackgroundResource(R.drawable.selected_v2);
            }
        }

        // Обработка нажатия на итем ресайклера
        void itemClick(View view) {
            // Работа с БД
            // Если не элементы не выделены
            if (massClick == 0) {
                int position = getAdapterPosition();
                if(arrayList.get(position).getIsGroupChat().equals("1"))
                {
                    Intent intentChat = new Intent(view.getContext(), ChatActivity.class);
                    intentChat.putExtra("user", (Parcelable) arrayList.get(position));
                    view.getContext().startActivity(intentChat);
                }
                else {
                    myRef.child(user.getUid()).child("chats").child(arrayList.get(position).getId()).child("message").push().push();
                    myRef.child(arrayList.get(position).getId()).child("chats").child(user.getUid()).child("message").push().push();
                    Intent intentChat = new Intent(view.getContext(), ChatActivity.class);
                    String friendID;
                    friendID = arrayList.get(position).getId();
                    intentChat.putExtra("id", friendID);
                    intentChat.putExtra("user", (Parcelable) arrayList.get(position));
                    // Переход в чат
                    view.getContext().startActivity(intentChat);
                }
            } else // Если хотябы один элемент выделен
            {
                if (arrayList.get(getAdapterPosition()).getDelete() == 0) {

                    arrayList.get(getAdapterPosition()).setDelete(1);
                    itemView.setBackgroundResource(R.drawable.selected_v2);
                    massClick++;
                    notifyDataSetChanged();
                    return;
                }
            }
            //Если элемент выделен
            if (arrayList.get(getAdapterPosition()).getDelete() == 1) {
                arrayList.get(getAdapterPosition()).setDelete(0);
                view.setBackgroundResource(R.drawable.line);
                massClick--;
                notifyDataSetChanged();
            }
            // Если элементы не выделены
            if (massClick == 0) {
                MainActivity.fab.setImageResource(R.mipmap.fab);
                MainActivity.fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.fab.getContext(), AllContactsActivity.class);
                        MainActivity.fab.getContext().startActivity(intent);
                    }
                });
            }
        }


        // Длительное нажатие на элемент в чатах
        void itemChatLongClick() {
            if (arrayList.get(getAdapterPosition()).getDelete() == 0) {
                arrayList.get(getAdapterPosition()).setDelete(1);
                itemView.setBackgroundResource(R.drawable.selected_v2);
                massClick++;
                notifyDataSetChanged();

            }
            if (massClick > 0) {
                MainActivity.fab.setImageResource(R.drawable.fab_del);
                MainActivity.fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteButtonClick();
                    }
                });
            }
        }

        // Длительное нажатие на элемент в контактах
        public void itemContactsLongClick() {
            if (arrayList.get(getAdapterPosition()).getDelete() == 0) {
                arrayList.get(getAdapterPosition()).setDelete(1);
                itemView.setBackgroundResource(R.drawable.selected_v2);
                massClick++;
                notifyDataSetChanged();

            }
            if (massClick > 0) {
                MainActivity.fab.setImageResource(R.drawable.fab_del);
                MainActivity.fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteButtonClick();
                    }
                });
            }
        }

        // Удаление контактов и чатов
        public void deleteButtonClick() {
            // удаление контактов
            if (MainActivity.tabLayout.getSelectedTabPosition() == 1) {
                for (int i = arrayList.size() - 1; i >= 0; i--) {
                    if (arrayList.get(i).getDelete() == 1) {
                        myRef.child(user.getUid()).child("contacts").child(arrayList.get(i).getId()).removeValue();
                        arrayList.remove(i);
                    }
                }
                notifyDataSetChanged();
            }
            // Удаление чатов
            if (MainActivity.tabLayout.getSelectedTabPosition() == 0) {

                    for (int i = arrayList.size() - 1; i >= 0; i--) {
                        if (arrayList.get(i).getDelete() == 1) {
                            try {
                                if (arrayList.get(i).getIsGroupChat().equals("1")) {
                                    myRef.child(user.getUid()).child("chats").child(arrayList.get(i).getId()).removeValue();
                                    groupChatsRef.child(arrayList.get(i).getId()).child("contacts").child(user.getUid()).removeValue();
                                   // arrayList.remove(i);
                                }
                                if (arrayList.get(i).getIsGroupChat().equals("0")) {
                                    myRef.child(user.getUid()).child("chats").child(arrayList.get(i).getId()).removeValue();
                                   // arrayList.remove(i);
                                }
                            } catch (IndexOutOfBoundsException e) {
                            }
                        }
                    }

                notifyDataSetChanged();
            }
            massClick = 0;
            MainActivity.fab.setImageResource(R.mipmap.fab);
            MainActivity.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.fab.getContext(), AllContactsActivity.class);
                    MainActivity.fab.getContext().startActivity(intent);
                }
            });
        }

        public void avatarClick() {
            if (file.length() != 0) {
                Intent intent = new Intent(itemView.getContext(), ImageIncreasing.class);
                intent.putExtra("path", file.getAbsolutePath());
                itemView.getContext().startActivity(intent);
            }
        }


        public void newGroupClick() {
            if (arrayList.get(getAdapterPosition()).getDelete() == 0) {
                arrayList.get(getAdapterPosition()).setDelete(1);
                itemView.setBackgroundResource(R.drawable.selected_v2);
                notifyItemChanged(getAdapterPosition());
                return;
            }
            //Если элемент выделен
            if (arrayList.get(getAdapterPosition()).getDelete() == 1) {
                arrayList.get(getAdapterPosition()).setDelete(0);
                itemView.setBackgroundResource(R.drawable.line);
                notifyItemChanged(getAdapterPosition());
            }
        }
    }

}
