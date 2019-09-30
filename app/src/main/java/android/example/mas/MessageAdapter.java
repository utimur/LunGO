package android.example.mas;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    ArrayList<Message> messageList;
    Context context;
    LayoutInflater inflater;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    public MessageAdapter(ArrayList<Message> messageList, Context context) {
        this.context = context;
        this.messageList = messageList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return messageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
        {
            view = inflater.inflate(R.layout.message_layout,null,false);
        }
         Message message = (Message)getItem(position);
        LinearLayout ll = (LinearLayout)view.findViewById(R.id.v);
        LinearLayout cl = (LinearLayout)view.findViewById(R.id.message_cl);
        if(user.getUid().equals(message.getId()))
        {
            ll.setGravity(Gravity.RIGHT);
            cl.setGravity(Gravity.RIGHT);
            ((TextView)view.findViewById(R.id.message_label)).setGravity(Gravity.RIGHT);
        }
        else
        {
            ll.setGravity(Gravity.LEFT);
            cl.setGravity(Gravity.LEFT);
            ((TextView)view.findViewById(R.id.message_label)).setGravity(Gravity.LEFT);
        }

        ((TextView)view.findViewById(R.id.message_label)).setText(message.getMessage());
        ((TextView)view.findViewById(R.id.time_label)).setText(message.getTime());
        return view;
    }
}
