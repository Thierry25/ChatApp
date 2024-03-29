package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.UserSelectionAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class GroupActivity extends AppCompatActivity implements SearchView.OnQueryTextListener
        , Serializable{

    private Toolbar mUserSelectionBar;
    private RecyclerView mUsersList;
    private FloatingActionButton mDoneButton;

    private List<Users> mContactsFromFirebase = new ArrayList<>();
    private List<Users> mSelectedUsers = new ArrayList<>();

    private Map<String, Object> mPhoneFromSelectedUsers = new HashMap<>();

    private UserSelectionAdapter mUserSelectionAdapter;

    private static DatabaseReference fDB = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView title;
    private ImageView backButton;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_user_selection);

        mUserSelectionBar = findViewById(R.id.userSelectionBar);

        setSupportActionBar(mUserSelectionBar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.group);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = findViewById(R.id.title);
        backButton = findViewById(R.id.backButton);
        profileImage = findViewById(R.id.profileImage);

        mUserSelectionAdapter = new UserSelectionAdapter(mContactsFromFirebase);

        mUsersList = findViewById(R.id.usersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setItemAnimator(new DefaultItemAnimator());
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersList.setAdapter(mUserSelectionAdapter);

        title.setText(R.string.group);
        title.setTextSize(32);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.allura);
        title.setTypeface(typeface);
        title.setPadding(8,0,0,0);
        backButton.setOnClickListener(v -> finish());

        mDoneButton = findViewById(R.id.doneButton);
        profileImage.setVisibility(View.GONE);
        mDoneButton.setOnClickListener(view -> {

            for (Users user : mContactsFromFirebase) {
                if (user.isSelected()) {
                    mSelectedUsers.add(user);
                    mPhoneFromSelectedUsers.put(user.getPhoneNumber(), true);
                }
            }
            if(mSelectedUsers.size() < 1){
                Toast.makeText(GroupActivity.this
                        , R.string.participant_warning, Toast.LENGTH_SHORT).show();
            }else{
                Intent goToGroupCreation = new Intent(GroupActivity.this,
                        GroupCreationActivity.class);
                goToGroupCreation.putExtra("list_users", (Serializable) mSelectedUsers);
                startActivity(goToGroupCreation);
                finish();
            }
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

        Set<Map.Entry<String, String>> myList = Users.getLocalContactList().entrySet();
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
                    mContactsFromFirebase.add(u);

                    if(u.getPhoneNumber().equals(phone)){
                        mContactsFromFirebase.remove(u);
                    }

                    mUserSelectionAdapter.notifyDataSetChanged();
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

        mUserSelectionAdapter.updateList(newUsers);
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
