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

public class ContactsFragment extends Fragment {

    RecyclerView recyclerView;
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference myRef ;
    ContactsHolderAdapter contactsHolderAdapter;
    ContactsHolderAdapter searchContactHolderAdapter;
    ArrayList<User> contactList;
    ArrayList<User> searchList;
    EditText searchEditText;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.contact_fragment, container, false);
        recyclerView = viewGroup.findViewById(R.id.cont_fragment_rv);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
        contactList  = new ArrayList<User>();
        contactsHolderAdapter = new ContactsHolderAdapter(contactList, 1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(viewGroup.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(contactsHolderAdapter);
        searchEditText = getActivity().findViewById(R.id.ET_search_contact);
        searchList = new ArrayList<>();
        searchContactHolderAdapter = new ContactsHolderAdapter(searchList, 2);

         new DataBase(getContext()).contactsDbChange(contactList,contactsHolderAdapter);
        searchLisner();
        return viewGroup;
    }



    // Поиск
    private void searchLisner() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            if(MainActivity.tabLayout.getSelectedTabPosition() == MainActivity.CONTACTS_TAB_POSITION)
            {
                if(s.equals(""))
                {
                    recyclerView.setAdapter(contactsHolderAdapter);
                    contactsHolderAdapter.notifyDataSetChanged();
                }
                else
                {
                    recyclerView.setAdapter(searchContactHolderAdapter);
                    searchList.clear();
                    for(int i = 0; i<contactList.size(); i++)
                    {
                        try {
                            if (s.toString().toLowerCase().equals(contactList.get(i).getUsername().substring(0, s.toString().length()).toLowerCase())) {
                                searchList.add(contactList.get(i));
                            }
                        }catch (StringIndexOutOfBoundsException e)
                        {

                        }
                    }
                    searchContactHolderAdapter.notifyDataSetChanged();
                }
             }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
