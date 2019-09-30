package android.example.mas;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.LongDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.example.mas.MainActivity.APP_PREFERENCES;
import static android.example.mas.MainActivity.APP_PREFERENCES_DifferenceInTime;

//import static android.example.mas.MainActivity.avatarImageView;

public class RecyclerMassageAdapter extends RecyclerView.Adapter<RecyclerMassageAdapter.RecyclerMassageHolder> {
    ArrayList<Message> Message;
    Context context;
    LayoutInflater inflater;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    FirebaseDatabase database;


    DatabaseReference myRef;
    String friendID;
    static TextToSpeech TTS;
    private boolean ttsEnabled;

    private final int LAYOUT_TEXT = 0;
    private final int LAYOUT_IMAGE = 2;
    private final int LAYOUT_MESSAGE_DATE = 3;

    private boolean isClicked = false;

    public RecyclerMassageAdapter(ArrayList<Message> message, Context context, String friendID) {
        this.Message = message;
        this.context = context;
        database = FirebaseDatabase.getInstance();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mAuth = FirebaseAuth.getInstance();
        this.user = mAuth.getCurrentUser();
        this.friendID = friendID;
        myRef = database.getReference("users");
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerMassageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = null;
        if (i == LAYOUT_TEXT) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout, viewGroup, false);
        }
        if (i == LAYOUT_IMAGE) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.imagemessage_layout, viewGroup, false);
        }
        if (i == LAYOUT_MESSAGE_DATE) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.date_message_layout, viewGroup, false);
        }
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.message_layout, viewGroup, false);
        }
        return new RecyclerMassageHolder(view);
    }

    public long getItemId(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(@NonNull final RecyclerMassageHolder recyclerMassageHolder, int position) {

        StorageReference mStorage = FirebaseStorage.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();
        LinearLayout view = (LinearLayout) recyclerMassageHolder.itemView;
        if (view == null) {
            view = (LinearLayout) inflater.inflate(R.layout.message_layout, null, false);
        }
        final Message message = Message.get(position);
        if (message.getFlag() == 3) {
            Log.d("TimLog", "Зашло в флаг 3");
            String date = getTextDate(message.getDate());
            recyclerMassageHolder.dateMessage.setText(date);

        } else {
            LinearLayout ll = (LinearLayout) view.findViewById(R.id.v);
            LinearLayout cl = (LinearLayout) view.findViewById(R.id.message_cl);

            try {
                if (message.getMsgUsername() != null) {
                    recyclerMassageHolder.msgUsername.setText(message.getMsgUsername() + "");
                    recyclerMassageHolder.msgUsername.setTextSize(14);
                }
            } catch (NullPointerException e) {
            }

            if (user.getUid().equals(message.getId())) {
                view.setGravity(Gravity.RIGHT);
                ll.setGravity(Gravity.RIGHT);
                cl.setGravity(Gravity.RIGHT);
                //           view.setPaddingRelative(400,0,0,0);
                //ll.setBackgroundColor(Color.rgb(219,210,210));
                if (Message.get(position).getFlag() != 2)
                    ((TextView) view.findViewById(R.id.message_label)).setGravity(Gravity.RIGHT);
            } else {
                recyclerMassageHolder.ll.setGravity(Gravity.LEFT);
                recyclerMassageHolder.cl.setGravity(Gravity.LEFT);
                //ll.setBackgroundColor(Color.rgb(167, 189, 108));
                if (Message.get(position).getFlag() != 2)
                    ((TextView) view.findViewById(R.id.message_label)).setGravity(Gravity.LEFT);
            }


            if (message.getFlag() == 1)
                recyclerMassageHolder.msgLabel.setText("Голосовое сообщение(нажмите, чтобы прослушать)");

            else if (message.getFlag() == 2) {


                String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + context.getPackageName() + "/" + friendID;
                final File file = new File(fileName, message.getMessage());


                if (file.exists()) {
                    try {
                        Glide
                                .with(recyclerMassageHolder.imgInChat.getContext())
                                .load(file)
                                .into(recyclerMassageHolder.imgInChat);
                    } catch (RuntimeException e) {
                        //           Thread th = new Thread();
                        //           th.start();
                        try {
                            Glide
                                    .with(recyclerMassageHolder.imgInChat.getContext())
                                    .load(file)
                                    .into(recyclerMassageHolder.imgInChat);
                        } catch (RuntimeException eb) {

                        }

                    }
                } else {
                    Log.d("Zheka", "Else");
                    if (friendID.substring(friendID.length() - 9).equals("groupchat")) {
                        mStorage.child(friendID).child("chats").child(message.getMessage()).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                try {
                                    Glide
                                            .with(context)
                                            .load(file)
                                            .into(recyclerMassageHolder.imgInChat);
                                } catch (IllegalArgumentException e) {

                                }
                            }
                        });
                    } else {
                        mStorage.child(user.getUid()).child("chats").child(friendID).child(message.getMessage()).getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                try {

                                    Glide
                                            .with(context)
                                            .load(file)
                                            .into(recyclerMassageHolder.imgInChat);
                                } catch (IllegalArgumentException e) {

                                }
                            }
                        });
                    }

                }
            } else recyclerMassageHolder.msgLabel.setText(message.getMessage());

            SharedPreferences mSettings;
            mSettings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            String differenceInTime = mSettings.getString(APP_PREFERENCES_DifferenceInTime, "");
            String resultTimeWithDifferences = returnTimeWithDifferences(context, message.getTime(), differenceInTime);
            // Log.d("Zheka", "timeWithDifferences " + resultTimeWithDifferences);
            recyclerMassageHolder.msgTime.setText(resultTimeWithDifferences);
        }
    }

    private String getTextDate(String date) {
        String monthList[] = {"января","Февраля","марта","апреля","мая","июня",
                                "июля","августа","сентября","октября","ноября","декабря"};
        int day = Integer.parseInt(date.substring(0,2));
        int month = Integer.parseInt(date.substring(3,5));
        return day + " " + monthList[month - 1];
    }

    @Override
    public int getItemCount() {
        return Message.size();
    }
    @Override
    public int getItemViewType(int position) {
        if (Message.get(position).getFlag() == 2)
            return LAYOUT_IMAGE;
       if(Message.get(position).getFlag() == 0)
           return LAYOUT_TEXT;
       if(Message.get(position).getFlag() == 3)
           return LAYOUT_MESSAGE_DATE;
       return 0;
    }

    public class RecyclerMassageHolder extends RecyclerView.ViewHolder {
        LinearLayout ll;
        LinearLayout cl;
        TextView msgLabel;
        TextView msgTime;
        TextView msgUsername;
        TextView dateMessage;
        ImageView imgInChat;
        FirebaseAuth mAuth;
        FirebaseUser user;
        FirebaseDatabase database;
        ArrayList<Message> massage;

        public RecyclerMassageHolder(@NonNull View itemView, FirebaseDatabase database) {
            super(itemView);
            this.mAuth = FirebaseAuth.getInstance();
            this.user = mAuth.getCurrentUser();
            this.database = database;
            this.massage = Message;
        }

        public RecyclerMassageHolder(@NonNull final View itemView) {
            super(itemView);

            database = FirebaseDatabase.getInstance();
            mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();
            massage = Message;
            ll = (LinearLayout) itemView.findViewById(R.id.v);
            cl = (LinearLayout) itemView.findViewById(R.id.message_cl);
            msgLabel = itemView.findViewById(R.id.message_label);
            msgTime = itemView.findViewById(R.id.time_label);
            msgUsername = itemView.findViewById(R.id.mess_username_tv);
            imgInChat = itemView.findViewById(R.id.image_InChat);
            dateMessage = itemView.findViewById(R.id.date_message_tv);
            LayoutInflater inflater = LayoutInflater.from(itemView.getContext());



            try {

                TTS = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int initStatus) {
                        if (initStatus == TextToSpeech.SUCCESS) {
                            if (TTS.isLanguageAvailable(new Locale(Locale.getDefault().getLanguage()))
                                    == TextToSpeech.LANG_AVAILABLE) {
                                Locale locale = new Locale("ru");
                                TTS.setLanguage(locale);
                            } else {
                                TTS.setLanguage(Locale.US);
                            }
                            TTS.setPitch(1.3f);
                            TTS.setSpeechRate(1.0f);
                            ttsEnabled = true;
                        } else if (initStatus == TextToSpeech.ERROR) {
                            //  Toast.makeText(context, "Ошибка воспроизведения", Toast.LENGTH_LONG).show();
                            ttsEnabled = false;
                        }
                    }
                });
            } catch (IllegalArgumentException e) {

                Toast.makeText(context, "Ошибка воспроизведения", Toast.LENGTH_LONG).show();
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position >= 0) {
                        if (massage.get(position).getFlag() == 0) {
                            speak(massage.get(position).getMessage());
                        }
                        if ((massage.get(position).getFlag()) == 1) {
                            String nameOfFile = massage.get(position).getMessage();

                            Context mContext = view.getContext();
                            ((ChatActivity) mContext).startPlaying(nameOfFile);

                            //startPlaying(nameOfFile);
                        }
                        if (Message.get(position).getFlag() == 2) {

                            String date = Message.get(position).getTime();
                            String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/" + context.getPackageName() + "/" + friendID + "/" + Message.get(position).getMessage();
                            Intent intentImage = new Intent(context, ImageIncreasing.class);
                            intentImage.putExtra("path", fileName);
                            intentImage.putExtra("date", date);
                            context.startActivity(intentImage);
                        }
                    }
                }
            });
            //       }


            // Удаление сообщений чата
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // FirebaseUser user;
                  //  Log.d("Zheka", "flag of massage = " + massage.get(getAdapterPosition()).getFlag());
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference myRefUser1;
                    DatabaseReference myRefUser2;
                    if (!friendID.substring(friendID.length() - 9).equals("groupchat")) {
                        myRefUser1 = database.getReference("users").child(user.getUid()).child("chats").child(friendID).child("message");
                        myRefUser2 = database.getReference("users").child(friendID).child("chats").child(user.getUid()).child("message");
                    } else {
                        myRefUser1 = database.getReference("groupChats").child(friendID).child("message");
                        myRefUser2 = database.getReference("groupChats").child(friendID).child("message");
                    }

                    int i = getAdapterPosition();
                    if ((getAdapterPosition() >= 0) && massage.get(getAdapterPosition()).getFlag() != 3) {

                        // Toast.makeText(ChatActivity.this, "IndexOfMassage" + i, Toast.LENGTH_SHORT).show();
                        final String DeleteMassage = massage.get(i).getId() + massage.get(i).getTime() + "read" + massage.get(i).getRead() + "flag" + massage.get(i).getFlag() + massage.get(i).getDate() + massage.get(i).getMessage();
                        Log.d("GroupChatsAudio", "DeleteMSGFromGroupChat " + DeleteMassage);
                        final String DeleteMassageForUser2 = massage.get(i).getId() + massage.get(i).getTime() + "read" + "flag" + massage.get(i).getFlag() + massage.get(i).getDate() + massage.get(i).getMessage();
                        // Ищет выделенное сообщение у первого пользователя, и , при нахождении, удаляет.
                        myRefUser1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                int IteratorDelete = 0;
                                for (DataSnapshot massage123 : dataSnapshot.getChildren()) {
                                    if (IteratorDelete != getAdapterPosition())
                                        IteratorDelete++;
                                    else {
                                        //  Log.d("Zheka", "Iterator = " + getAdapterPosition());
                                        Log.d("Zheka", massage123.getValue(String.class) + "  " + DeleteMassage);
                                        if (massage123.getValue(String.class).equals(DeleteMassage)) {
                                            massage123.getRef().removeValue();
                                            break;
                                        }
                                    }
                                }
                            }

                            //
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
//
                            }
                        });
                        //myRefUser1.child(DeleteMassage).removeValue();
                        // Ищет выделенное сообщение у второго пользователя, и , при нахождении, удаляет.
                        if (!friendID.substring(friendID.length() - 9).equals("groupchat")) {
                            myRefUser2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    int IteratorDelete = 0;
                                    for (DataSnapshot massage123 : dataSnapshot.getChildren()) {
                                        if (IteratorDelete != getAdapterPosition())
                                            IteratorDelete++;
                                        else {
                                            //Log.d("Zheka", "Iterator = " + getAdapterPosition());
                                            //Log.d("Zheka", massage123.getValue(String.class) + "  " + DeleteMassage);
                                            String searchDeleteMassage = massage123.getValue(String.class);
                                            searchDeleteMassage = searchDeleteMassage.substring(0, 37) + searchDeleteMassage.substring(38);
                                            Log.d("Zheka", DeleteMassageForUser2 + "---" + searchDeleteMassage);
                                            if (searchDeleteMassage.equals(DeleteMassageForUser2)) {
                                                massage123.getRef().removeValue();
                                                break;
                                            }
                                        }
                                        //            if (massage123.getValue(String.class).equals(DeleteMassageForUser2)) {
                                        //                massage123.getRef().removeValue();
                                        //                //       String msgValue = massage123.getValue(String.class);
                                        //                //       Toast.makeText(ChatActivity.this, msgValue, Toast.LENGTH_SHORT).show();
                                        //            }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        if (getAdapterPosition() == (massage.size() - 1)) {
                            // Log.d("Zheka", "PositionForDelete = " + getAdapterPosition());
                            if (getAdapterPosition() != 0) {

                                if (massage.get((getAdapterPosition()) - 1).getFlag() == 0)
                                    ChatActivity.putLastMessageAndHisTime(user.getUid(), friendID, massage.get((getAdapterPosition()) - 1).getTime(), massage.get((getAdapterPosition()) - 1).getMessage());
                                else if (massage.get((getAdapterPosition()) - 1).getFlag() == 1)
                                    ChatActivity.putLastMessageAndHisTime(user.getUid(), friendID, massage.get((getAdapterPosition()) - 1).getTime(), "Голосовое сообщение");
                                else
                                    ChatActivity.putLastMessageAndHisTime(user.getUid(), friendID, massage.get((getAdapterPosition()) - 1).getTime(), "Изображение");
                            } else
                                ChatActivity.putLastMessageAndHisTime(user.getUid(), friendID, "", "");
                        }
                        // myRefUser2.child(DeleteMassageForUser2).removeValue();
                        // Удаление файла из хранилища
                        if (Message.get(i).getFlag() != 0) {
                            final String deleteFile = Message.get(i).getMessage();
                            File FileForDeleting = new File(Environment.getExternalStorageDirectory() + "/Android/data/" + context.getPackageName() + "/" + friendID + "/" + deleteFile);
                            if (FileForDeleting.exists()) {
                                Log.d("Zheka","File is exist");
                                FileForDeleting.delete();
                            }
                            else
                            {
                                Log.d("Zheka", "File is not exist, his path : " + FileForDeleting.getAbsolutePath());
                            }
                            if (!friendID.substring(friendID.length() - 9).equals("groupchat")) {
                                StorageReference DeleteReference = FirebaseStorage.getInstance().getReference().child(user.getUid()).child("chats").child(friendID).child(deleteFile);
                                DeleteReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        StorageReference DeleteReferenceSecond = FirebaseStorage.getInstance().getReference().child(friendID).child("chats").child(user.getUid()).child(deleteFile);
                                        DeleteReferenceSecond.delete();
                                    }
                                });
                            } else {
                                StorageReference DeleteReference = FirebaseStorage.getInstance().getReference().child(friendID).child("chats").child(deleteFile);
                                DeleteReference.delete();
                            }
                        }

                    }
                    return false;
                }
            });

    }
    }


    static String returnTimeWithDifferences(Context context,String databaseTime,String differenceInTime)
    {
        if (differenceInTime.equals("0"))
            return databaseTime;
        int dbHour = Integer.parseInt(databaseTime.substring(0,2));
        int dbMinute = Integer.parseInt(databaseTime.substring(3));
       // Log.d("Zheka", "differenceInTime " + differenceInTime);
        int differenceHour = Integer.parseInt(differenceInTime.substring(1,3));
        int differenceMinute = Integer.parseInt(differenceInTime.substring(4));
        int dbTime = dbHour * 60 + dbMinute;
        int differenceTime = differenceHour*60 + differenceMinute;
        int resultTime = 0;
        if (differenceInTime.substring(0,1).equals("+"))
        {
            resultTime = dbTime + differenceTime;
            if (resultTime > 24*60)
                resultTime-= 24*60;    // Если больше 24 часов
        }
        else if (differenceInTime.substring(0,1).equals("-"))
        {
            if (dbTime < differenceTime)
                dbTime+= 24*60;
            resultTime = dbTime - differenceTime;
        }
        int resultHour = resultTime / 60;
        int resultMinute = resultTime - (resultHour*60);

        if (resultHour < 10)
        {
            if (resultMinute < 10)
            {
                return "0" + resultHour + ":0" + resultMinute;
            }
            else return "0" + resultHour + ":" + resultMinute;
        }

        else
        {
            if (resultMinute < 10)
                return resultHour + ":0" + resultMinute;
            else return resultHour + ":" + resultMinute;
        }





    }

    public void speak(String text) {
        if (!ttsEnabled) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsGreater21(text);
        } else {
            ttsUnder20(text);
        }
    }

    @SuppressWarnings("deprecation") private void ttsUnder20(String text) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "MessageId");
        TTS.speak(text, TextToSpeech.QUEUE_FLUSH, map);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void ttsGreater21(String text) {
        String utteranceId = this.hashCode() + "";
        TTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }



}

class RecyclerMessageTouch implements RecyclerView.OnItemTouchListener
{
    ArrayList<Message> massage;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    String friendID;

    public RecyclerMessageTouch(ArrayList<Message> massage,   String friendID) {
        this.massage = massage;
        this.mAuth = FirebaseAuth.getInstance();
        this.user = mAuth.getCurrentUser();
        this.database = FirebaseDatabase.getInstance();
        this.friendID = friendID;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {

        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            DatabaseReference myRefUser1 = database.getReference("users").child(user.getUid()).child("chats").child(friendID).child("message");
            DatabaseReference myRefUser2 = database.getReference("users").child(friendID).child("chats").child(user.getUid()).child("message");

            View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
            int i = recyclerView.getChildAdapterPosition(childView);
            final int indexOfDeleteMassage = i;
            // Toast.makeText(ChatActivity.this, "IndexOfMassage" + i, Toast.LENGTH_SHORT).show();
            final String DeleteMassage = massage.get(i).getId() + massage.get(i).getTime() + massage.get(i).getMessage();
            myRefUser1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot massage123 : dataSnapshot.getChildren()) {

                        if (massage123.getValue(String.class).equals(DeleteMassage)) {

                            massage123.getRef().removeValue();

                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            myRefUser2.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot massage123 : dataSnapshot.getChildren()) {
                        if (massage123.getValue(String.class).equals(DeleteMassage)) {
                            massage123.getRef().removeValue();
                            //       String msgValue = massage123.getValue(String.class);
                            //       Toast.makeText(ChatActivity.this, msgValue, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });


        }
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {


    }
}