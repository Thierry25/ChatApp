package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.UserSelectionAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class UserSelectionActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private Toolbar mUserSelectionBar;
    private RecyclerView mUsersList;
    private FloatingActionButton mDoneButton;

    private String mChannelId;

    private List<Users> mContactsFromFirebase = new ArrayList<>();
    private List<Users> mSelectedUsers = new ArrayList<>();

    private Map<String, Object> mPhoneFromSelectedUsers = new HashMap<>();
    private Map<String, Object> mRegisteredChannel = new HashMap<>();

    private UserSelectionAdapter mUserSelectionAdapter;
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();
    private static DatabaseReference fDB = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_user_selection);

        mUserSelectionBar = findViewById(R.id.userSelectionBar);

        setSupportActionBar(mUserSelectionBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.add_subs);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChannelId = getIntent().getStringExtra("Channel_id");

        mUserSelectionAdapter = new UserSelectionAdapter(mContactsFromFirebase);

        mUsersList = findViewById(R.id.usersList);
        mUsersList.setHasFixedSize(true);
        mUsersList.setItemAnimator(new DefaultItemAnimator());
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
        mUsersList.setAdapter(mUserSelectionAdapter);

        mDoneButton = findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(view -> {

            for (Users user : mContactsFromFirebase) {
                if (user.isSelected()) {
                    mSelectedUsers.add(user);
                    mPhoneFromSelectedUsers.put(user.getPhoneNumber(), true);
                }
            }
            if(mSelectedUsers.size() < 1){
                Toast.makeText(UserSelectionActivity.this
                        , R.string.participant_warning, Toast.LENGTH_SHORT).show();
            }else{
                updateChannel();
                // Ask to lock channel with passkey

                new TTFancyGifDialog.Builder(this)
                        .isCancellable(false)
                        .setTitle(getString(R.string.channel_lock))
                        .setMessage(getString(R.string.lock_channel_message))
                        .setGifResource(R.drawable.gif10)
                        .OnPositiveClicked(() ->{

                            Intent goToLockChannel = new Intent(UserSelectionActivity.this,
                                    LockChannelActivity.class);
                            goToLockChannel.putExtra("Channel_id", mChannelId);
                            goToLockChannel.putExtra("chat_id", mChannelId);
                            startActivity(goToLockChannel);
                            finish();

                        })
                        .OnNegativeClicked(() ->{

                            Intent goToAdminChat = new Intent(UserSelectionActivity.this,
                                    ChannelAdminChatActivity.class);
                            goToAdminChat.putExtra("Channel_id", mChannelId);
                            goToAdminChat.putExtra("chat_id", mChannelId);
                            startActivity(goToAdminChat);
                            finish();

                        })
                        .build();
            }
        });
    }

    private void updateChannel() {

        final String user_reference = "ads_users/";

        String chan_name = "C-" + mChannelId;

        mRegisteredChannel.put("id", mChannelId);
        mRegisteredChannel.put("type", "channel");
        mRegisteredChannel.put("visible", true);
        mRegisteredChannel.put("phone_number",
                "");
        mRegisteredChannel.put("timestamp",
                ServerValue.TIMESTAMP);

        for (String key : mPhoneFromSelectedUsers.keySet()) {
            Map<String, Object> newUsers = new HashMap<>();
            newUsers.put(key, ServerValue.TIMESTAMP);
            DatabaseReference channelToAddUnderUsers = mRootReference.child(user_reference)
                    .child(key).child("conversation").child(chan_name);

            channelToAddUnderUsers.updateChildren(mRegisteredChannel);

            DatabaseReference newUsersToAddToChannel = mRootReference.child("ads_channel")
                    .child(mChannelId).child("subscribers");

            newUsersToAddToChannel.updateChildren(newUsers);
        }
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
