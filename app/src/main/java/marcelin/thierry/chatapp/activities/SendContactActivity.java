package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.SendContactAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class SendContactActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private SendContactAdapter mSendContactAdapter;
    private RecyclerView mUsersRecyclerList;
    private Toolbar mContactsBarLayout;

    private List<Users> mUsersList = new ArrayList<>();

    private FirebaseAuth mAuth;

    private String mChatPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_send_contact);

        mContactsBarLayout = findViewById(R.id.contacts_bar_layout);
        setSupportActionBar(mContactsBarLayout);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSendContactAdapter = new SendContactAdapter(mUsersList);

        mUsersRecyclerList = findViewById(R.id.contactList);
        mUsersRecyclerList.setHasFixedSize(true);
        mUsersRecyclerList.setLayoutManager(new LinearLayoutManager(this));
        mUsersRecyclerList.setAdapter(mSendContactAdapter);

        mAuth = FirebaseAuth.getInstance();

        mChatPhone = getIntent().getStringExtra("user_phone");

        getContacts();


    }

    private void getContacts() {
        mUsersList.clear();

        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        if (Users.getLocalContactList() == null || Users.getLocalContactList().isEmpty()) {
            //TODO: try to see if thread successfully loaded the users
            Log.i("ContactList", "user list is empty");
            return;
        }

        Set<Map.Entry<String, String>> myList = Users.getLocalContactList().entrySet();
        for (Map.Entry<String, String> val : myList) {

            String phoneNumber = val.getKey();
            String localContactName = val.getValue();

            Users u = new Users();
            u.setUid(phoneNumber);
            u.setPhone(mChatPhone);
            u.setNameStoredInPhone(localContactName);
            u.setPhoneNumber(phoneNumber);
            u.setMessage(phoneNumber);
            mUsersList.add(u);

            Collections.sort(mUsersList, (Users a1, Users a2) ->
                    a1.getNameStoredInPhone().compareTo(a2.getNameStoredInPhone()));
            if (u.getPhoneNumber().equals(phone)) {
                mUsersList.remove(u);
            }

            mSendContactAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    // Load Language saved in shared preferences
    public void loadLocale(){
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }

}

