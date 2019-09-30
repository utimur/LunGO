package android.example.mas;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

     RecyclerView recyclerView;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;
    public static ContactsHolderAdapter chatHolderAdapter;
   ContactsHolderAdapter searchChatHolderAdapter;
    ArrayList<User> chatList;
    ArrayList<User> searchList;
    EditText searchEditText;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater
            , @Nullable ViewGroup container
            , @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup)inflater.inflate(R.layout.chat_fragment, container, false);
        recyclerView = viewGroup.findViewById(R.id.chat_frag_rv);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        chatList  = new ArrayList<User>();
        chatHolderAdapter = new ContactsHolderAdapter(chatList, 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(viewGroup.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatHolderAdapter);
        searchEditText = getActivity().findViewById(R.id.ET_search_contact);
        searchList = new ArrayList<>();
        searchChatHolderAdapter = new ContactsHolderAdapter(searchList, 1);

        searchLisner();
        new DataBase(getContext()).chatDBchange(chatList, chatHolderAdapter);
        new myThread().start();

        return viewGroup;
    }


    public static boolean dateCompare(String s1, String s2)
    {

        // МЕсяц
        if(s1 != null && s2 != null && (!s1.equals("")) && (!s2.equals(""))) {
            String m1 = s1.substring(3, 5);
            String m2 = s2.substring(3, 5);

            // День
            String d1 = s1.substring(0, 2);
            String d2 = s2.substring(0, 2);

            // Час
            String h1 = s1.substring(6, 8);
            String h2 = s2.substring(6, 8);

            // Минута
            String min1 = s1.substring(9, 11);
            String min2 = s2.substring(9, 11);

            // Секунда
            String sec1 = s1.substring(12, 14);
            String sec2 = s2.substring(12, 14);

            if (Integer.parseInt(m1) > Integer.parseInt(m2))
            {
                return true;
            }
            else {
                if (Integer.parseInt(m1) < Integer.parseInt(m2))
                {
                    return false;
                }
                if (Integer.parseInt(m1) == Integer.parseInt(m2)) {
                    if (Integer.parseInt(d1) > Integer.parseInt(d2)) return true;
                    else {
                        if (Integer.parseInt(d1) < Integer.parseInt(d2)) return false;
                        if (Integer.parseInt(d1) == Integer.parseInt(d2)) {
                            if (Integer.parseInt(h1) > Integer.parseInt(h2)) return true;
                            else {
                                if (Integer.parseInt(h1) < Integer.parseInt(h2)) return false;
                                if (Integer.parseInt(h1) == Integer.parseInt(h2)) {
                                    if (Integer.parseInt(min1) > Integer.parseInt(min2))
                                        return true;
                                    else {
                                        if (Integer.parseInt(min1) < Integer.parseInt(min2)) return false;
                                        if(Integer.parseInt(min1) == Integer.parseInt(min2)) {
                                            if (Integer.parseInt(sec1) > Integer.parseInt(sec2))
                                                return true;
                                            else {
                                                if (Integer.parseInt(sec1) < Integer.parseInt(sec2))
                                                    return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void quickSort(ArrayList<User> arr, int left, int right) {
        if (arr.size() < 2) {
            return;
        }
        if (left >= right) {
            return;
        }
        String pivot = arr.get((left + right) / 2).getLastMessageDate(); // 6 5 2 3 4 1 8
        int i = left;
        int j = right;
        User temp;
        while (i <= j) {
            while (ChatFragment.dateCompare(arr.get(i).getLastMessageDate(), pivot) == false) {
                i++;
            }
            while (ChatFragment.dateCompare(arr.get(j).getLastMessageDate(), pivot) == true) {
                j--;
            }
            if (i<=j) {
                if (i < j) {
                    temp = arr.get(i);
                    arr.set(i,arr.get(j));
                    arr.set(j,temp);
                }
                i++;
                j--;
            }

        }
        if (i < right) {
            quickSort(arr, i, right);
        }
        if (left < j) {
            quickSort(arr,left,j);
        }
    }

    public static void sortArray(ArrayList<User> arr)
    {
        User temp;

        for(int i = 0; i< arr.size(); i++)
        {
            for(int j = i+1; j < arr.size(); j++ )
            {
                if(dateCompare(arr.get(i).getLastMessageDate(),arr.get(j).getLastMessageDate()) == false)
                {
                    temp = arr.get(i);
                    arr.set(i,arr.get(j));
                    arr.set(j,temp);
                }
            }
        }
    }
    class myThread extends Thread
    {
        @Override
        public void run() {
            while(chatList.size() <2 )
            {

            }
            while (chatList.get(0).getLastMessageDate() == null && chatList.get(1).getLastMessageDate() == null)
            {
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sortArray(chatList);
        }
    }

    // Поиск
    private void searchLisner() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(MainActivity.tabLayout.getSelectedTabPosition() == MainActivity.CHAT_TAB_POSITION)
                {
                    if(s.equals(""))
                    {
                        recyclerView.setAdapter(chatHolderAdapter);
                        chatHolderAdapter.notifyDataSetChanged();
                    }
                    else {
                        recyclerView.setAdapter(searchChatHolderAdapter);
                        searchList.clear();
                        for(int i = 0; i<chatList.size(); i++)
                        {
                            try {
                                if (s.toString().toLowerCase().equals(chatList.get(i).getUsername().substring(0, s.toString().length()).toLowerCase())) {
                                    searchList.add(chatList.get(i));
                                }
                            }catch (StringIndexOutOfBoundsException e)
                            {

                            }
                        }
                        searchChatHolderAdapter.notifyDataSetChanged();
                    }
                }
                //if(contactLabel.isEnabled() == false)
                //{
                //    if(s.equals(""))
                //    {
                //        contactsRecyclerView.setAdapter(contactsHolderAdapter);
                //        contactsHolderAdapter.notifyDataSetChanged();
                //    }
                //    else
                //    {
                //        contactsRecyclerView.setAdapter(searchChatHolderAdapter);
                //        searchList.clear();
                //        for(int i = 0; i<contactList.size(); i++)
                //        {
                //            try {
                //                if (s.toString().toLowerCase().equals(contactList.get(i).getUsername().substring(0, s.toString().length()).toLowerCase())) {
                //                    searchList.add(contactList.get(i));
                //                }
                //            }catch (StringIndexOutOfBoundsException e)
                //            {
//
                //            }
                //        }
                //        searchChatHolderAdapter.notifyDataSetChanged();
                //    }
                // }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
