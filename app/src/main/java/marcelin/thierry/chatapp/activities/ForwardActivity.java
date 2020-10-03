package marcelin.thierry.chatapp.activities;


import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.ForwardLinkAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class ForwardActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private static DatabaseReference fDB = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private Toolbar mContactsBar;
    private static RecyclerView mContactsList;

    private List<Users> mContactsFromFirebase = new ArrayList<>();
    private ForwardLinkAdapter mForwardLinkAdapter;

    private String linkReceived;

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();


    private TextView title;
    private ImageView backButton;
    private CircleImageView profileImage;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ContactOnCreate", "method called");
        loadLocale();
        setContentView(R.layout.activity_contacts);

        mContactsBar = findViewById(R.id.contacts_bar_layout);
        setSupportActionBar(mContactsBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.contacts);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        linkReceived = getIntent().getStringExtra("link_to_send");

        mForwardLinkAdapter = new ForwardLinkAdapter(mContactsFromFirebase);
        mContactsList = findViewById(R.id.contacts_list);
        mContactsList.setHasFixedSize(true);
        mContactsList.setLayoutManager(new LinearLayoutManager(this));
        mContactsList.setAdapter(mForwardLinkAdapter);

        Toast.makeText(ForwardActivity.this, R.string.click_to_send,
                Toast.LENGTH_SHORT).show();

        title = findViewById(R.id.title);
        profileImage = findViewById(R.id.profileImage);

        title.setTextSize(32);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.allura);
        title.setTypeface(typeface);
        title.setText(R.string.contacts);
        profileImage.setVisibility(View.GONE);

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getContacts();
    }

    // Get contacts from user's phone
    private void getContacts() {
        mContactsFromFirebase.clear();

        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        if (Users.getLocalContactList() == null || Users.getLocalContactList().isEmpty()) {
            //TODO: try to see if thread successfully loaded the users
            Log.i("ContactList", "user list is empty");
            return;
        }

        Set<Map.Entry<String,String>> myList = Users.getLocalContactList().entrySet();
        for (Map.Entry<String, String> val : myList) {

            String phoneNumber = val.getKey();
            String localContactName = val.getValue();


            fDB.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if (u == null) {
                        Log.i("FirebaseEmpty", "object is null");
                        return;
                    }
                    Log.i("FirebaseList", "data found. user uid: " + u.getUid());

                    u.setNameStoredInPhone(localContactName);
                    u.setPhoneNumber(phoneNumber);
                    u.setMessage(linkReceived);
                    //u.setType(typeReceived);
                //    mContactsFromFirebase.add(u);

                    if(!u.getPhoneNumber().equals(phone)){
                        mContactsFromFirebase.add(u);
                    }

                    Collections.sort(mContactsFromFirebase, (Users a1, Users a2) ->
                            a1.getNameStoredInPhone().compareTo(a2.getNameStoredInPhone()));
                    if(u.getPhoneNumber().equals(phone)){
                        mContactsFromFirebase.remove(u);
                    }

                    mForwardLinkAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userEntry = newText.toLowerCase();
        List<Users> newUsers = new ArrayList<>();

        for(Users u : mContactsFromFirebase){
            if(u.getNameStoredInPhone().toLowerCase().contains(userEntry)){
                newUsers.add(u);
            }
        }

        mForwardLinkAdapter.updateList(newUsers);
        return true;
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