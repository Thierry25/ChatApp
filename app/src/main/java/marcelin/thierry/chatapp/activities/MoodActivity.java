package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
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
import marcelin.thierry.chatapp.adapters.MoodChannelAdapter;
import marcelin.thierry.chatapp.adapters.MoodGroupAdapter;
import marcelin.thierry.chatapp.adapters.UserSelectionAdapter;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Group;
import marcelin.thierry.chatapp.classes.Users;

public class MoodActivity extends AppCompatActivity {

    private Toolbar mMoodBar;

    private TextView mContactsName, mGroupsName, mChannelsName, mNoContactsText, mNoGroupsText, mNoChannelsText;

    private RecyclerView mContactList, mGroupList, mChannelList;

    private List<Users> mContactsFromFirebase = new ArrayList<>();
    private List<Group> mGroupSubscriptionList = new ArrayList<>();
    private List<Channel> mChannelSubscriptionList = new ArrayList<>();

    private Map<String, Object> mPhoneFromSelectedUsers = new HashMap<>();

    private UserSelectionAdapter mUserSelectionAdapter;
    private MoodChannelAdapter mMoodChannelAdapter;
    private MoodGroupAdapter mMoodGroupAdapter;

    private FloatingActionButton mDoneButton;

    private TextView title;

    private boolean isClicked = false;
    private boolean isClicked_ = false;
    private boolean isClicked__ = false;

    private static final DatabaseReference fDB = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final DatabaseReference mGroupReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group");

    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ProgressBar mGroupProgress;
    private ProgressBar mChannelProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_mood);

        mMoodBar = findViewById(R.id.moodBar);
//        setSupportActionBar(mMoodBar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setSupportActionBar(mMoodBar);


        mUserSelectionAdapter = new UserSelectionAdapter(mContactsFromFirebase);
        mMoodChannelAdapter = new MoodChannelAdapter(mChannelSubscriptionList);
        mMoodGroupAdapter = new MoodGroupAdapter(mGroupSubscriptionList);

        mContactList = findViewById(R.id.contactList);
        mContactList.setHasFixedSize(true);
        mContactList.setItemAnimator(new DefaultItemAnimator());
        mContactList.setLayoutManager(new LinearLayoutManager(this));
        mContactList.setAdapter(mUserSelectionAdapter);

        mChannelList = findViewById(R.id.channelList);
        mChannelList.setHasFixedSize(true);
        mChannelList.setItemAnimator(new DefaultItemAnimator());
        mChannelList.setLayoutManager(new LinearLayoutManager(this));
        mChannelList.setAdapter(mMoodChannelAdapter);

        mGroupList = findViewById(R.id.groupList);
        mGroupList.setHasFixedSize(true);
        mGroupList.setItemAnimator(new DefaultItemAnimator());
        mGroupList.setLayoutManager(new LinearLayoutManager(this));
        mGroupList.setAdapter(mMoodGroupAdapter);

        mNoContactsText = findViewById(R.id.noContactsText);
        mNoGroupsText = findViewById(R.id.noGroupsText);
        mNoChannelsText = findViewById(R.id.noChannelsText);

        mDoneButton = findViewById(R.id.doneButton);
        mGroupProgress = findViewById(R.id.groupProgress);
        mChannelProgress = findViewById(R.id.channelProgress);

        mContactsName = findViewById(R.id.contactsName);
        mGroupsName = findViewById(R.id.groupsName);
        mChannelsName = findViewById(R.id.channelsName);

        title = findViewById(R.id.title);
        title.setText(R.string._disturb);
        title.setOnClickListener(v -> finish());

        getSubscriptions();

        mContactsName.setOnClickListener(view -> {
            if(isClicked){
                mContactList.setVisibility(View.GONE);
                isClicked = !isClicked;
                mContactsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_left_arrow, 0);
            }else{
                mContactList.setVisibility(View.VISIBLE);
                isClicked = !isClicked;
                mContactsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
            }
        });

        mGroupsName.setOnClickListener(view -> {
            if(isClicked_){
                mGroupList.setVisibility(View.GONE);
                mNoGroupsText.setVisibility(View.GONE);
                isClicked_ = !isClicked_;
                mGroupsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_left_arrow, 0);
            }else{
                mGroupList.setVisibility(View.VISIBLE);
                mNoGroupsText.setVisibility(View.VISIBLE);
                isClicked_ = !isClicked_;
                mGroupsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
            }
        });

        mChannelsName.setOnClickListener(view -> {
            if(isClicked__){
                mChannelList.setVisibility(View.GONE);
                mNoChannelsText.setVisibility(View.GONE);
                isClicked__ = !isClicked__;
                mChannelsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_left_arrow, 0);
            }else{
                mChannelList.setVisibility(View.VISIBLE);
                mNoChannelsText.setVisibility(View.VISIBLE);
                isClicked__ = !isClicked__;
                mChannelsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
            }
        });

        mDoneButton.setOnClickListener(view -> {

            for(Users u : mContactsFromFirebase){
                if(u.isSelected()) {
                    mPhoneFromSelectedUsers.put("U-" + u.getPhoneNumber(), ServerValue.TIMESTAMP);
                }else{
                    mPhoneFromSelectedUsers.remove("U-" + u.getPhoneNumber());
                }
            }

            for(Group g : mGroupSubscriptionList){
                if(g.isSelected()){
                    mPhoneFromSelectedUsers.put("G-"+g.getName(), ServerValue.TIMESTAMP);
                }else{
                    mPhoneFromSelectedUsers.remove("G-"+g.getName());
                }
            }

            for(Channel c : mChannelSubscriptionList){
                if(c.isSelected()){
                    mPhoneFromSelectedUsers.put("C-"+ c.getName(), ServerValue.TIMESTAMP);
                }else{
                    mPhoneFromSelectedUsers.remove("C-"+ c.getName());

                }
            }


            if(mPhoneFromSelectedUsers.size() > 0){
                new TTFancyGifDialog.Builder(MoodActivity.this)
                        .setTitle(getString(R.string._disturb))
                        .setMessage(getString(R.string.reach_))
                        .setGifResource(R.drawable.gif16)
                        .isCancellable(false)
                        .OnPositiveClicked(() ->{

                            ProgressDialog progressDialog = new ProgressDialog(MoodActivity.this);
                            progressDialog.setTitle(getString(R.string.list_upl_));
                            progressDialog.setMessage(getString(R.string.mood_waiting_));
                            progressDialog.show();

                            String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                            fDB.child(phone).child("e").updateChildren(mPhoneFromSelectedUsers)
                                    .addOnCompleteListener(task -> {
                                        if(task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(MoodActivity.this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                                            finish();

                                        }
                                    });

                        })
                        .OnNegativeClicked(() ->{
                            Toast.makeText(MoodActivity.this, mPhoneFromSelectedUsers.toString(), Toast.LENGTH_SHORT).show();
                        })

                        .build();
            }else{
                Toast.makeText(MoodActivity.this, R.string.mood_err_, Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mood_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.menu_unwanted){
            Intent intent = new Intent(MoodActivity.this, UnwantedActivity.class);
            startActivity(intent);
        }

        return true;
    }


    private void getSubscriptions(){
        mGroupSubscriptionList.clear();

        String myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        if (myPhone != null) {
            fDB.child(myPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("conversation")){
                        fDB.child(myPhone).child("conversation").orderByChild("type").equalTo("group").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    mNoGroupsText.setVisibility(View.VISIBLE);
                                    mGroupProgress.setVisibility(View.GONE);
                                    mGroupsName.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        fDB.child(myPhone).child("conversation").orderByChild("type").equalTo("channel").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(!dataSnapshot.exists()){
                                    mNoChannelsText.setVisibility(View.VISIBLE);
                                    mChannelProgress.setVisibility(View.GONE);
                                    mChannelsName.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        fDB.child(myPhone).child("conversation").addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                Conversation c = dataSnapshot.getValue(Conversation.class);
                                if(c == null){
                                    return;
                                }


                                if(c.getType().equals("group")){
                                    mGroupReference.child(c.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Group g = dataSnapshot.getValue(Group.class);
                                            if(g == null){
                                           //     mNoGroupsText.setVisibility(View.VISIBLE);
                                                return;
                                            }

                                            mGroupSubscriptionList.remove(g);
                                            // c.setMessageTimestamp(c.getTimestamp());
                                            mGroupSubscriptionList.add(g);

                                            mMoodGroupAdapter.notifyDataSetChanged();
                                            mGroupProgress.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }else if(c.getType().equals("channel")){
                                    mChannelReference.child(c.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Channel chan = dataSnapshot.getValue(Channel.class);
                                            if(chan == null){
                                             //   mNoChannelsText.setVisibility(View.VISIBLE);
                                                return;
                                            }

                                            mChannelSubscriptionList.remove(chan);

                                            if(!chan.getAdmins().containsKey(myPhone)){
                                                mChannelSubscriptionList.add(chan);
                                            }
                                            mMoodChannelAdapter.notifyDataSetChanged();
                                            mChannelProgress.setVisibility(View.GONE);

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

                    }else{
                        mNoGroupsText.setVisibility(View.VISIBLE);
                        mNoChannelsText.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
            mNoContactsText.setVisibility(View.VISIBLE);
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
