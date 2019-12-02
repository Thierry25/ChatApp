package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.MoodChannelAdapter;
import marcelin.thierry.chatapp.adapters.MoodGroupAdapter;
import marcelin.thierry.chatapp.adapters.UserSelectionAdapter;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Group;
import marcelin.thierry.chatapp.classes.Users;

public class UnwantedActivity extends AppCompatActivity {

    private TextView mContactsName, mGroupsName, mChannelsName,
            mNoContactsText, mNoGroupsText, mNoChannelsText, mTextNoBlockedUsers;

    private RecyclerView mContactList, mGroupList, mChannelList;

    private List<Users> mContactsFromFirebase = new ArrayList<>();
    private List<Group> mGroupSubscriptionList = new ArrayList<>();
    private List<Channel> mChannelSubscriptionList = new ArrayList<>();

    private List<String> mPhoneFromSelectedUsers = new ArrayList<>();

    private UserSelectionAdapter mUserSelectionAdapter;
    private MoodChannelAdapter mMoodChannelAdapter;
    private MoodGroupAdapter mMoodGroupAdapter;

    private FloatingActionButton mDoneButton;

    private Toolbar mMoodBar;

    private boolean isClicked = false;
    private boolean isClicked_ = false;
    private boolean isClicked__ = false;

    private ProgressDialog progressDialog;

    private static final DatabaseReference fDB = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mGroupReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group");

    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");

    private static final DatabaseReference mExceptReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_except");

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private ProgressBar mGroupProgress;
    private ProgressBar mChannelProgress;
    private static final String myPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
    private TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_mood);

        mMoodBar = findViewById(R.id.moodBar);
        setSupportActionBar(mMoodBar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string._un_conv_);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserSelectionAdapter = new UserSelectionAdapter(mContactsFromFirebase);
        mMoodChannelAdapter = new MoodChannelAdapter(mChannelSubscriptionList);
        mMoodGroupAdapter = new MoodGroupAdapter(mGroupSubscriptionList);
        progressDialog = new ProgressDialog(this);

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

        mTextNoBlockedUsers = findViewById(R.id.textNoBlockedUsers);

        title = findViewById(R.id.title);
        title.setText(R.string._un_conv_);
        title.setOnClickListener(v -> finish());

        //getSubscriptions();
        //getBlockedUsers();
        getListOfMutedUsers();

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
                isClicked_ = !isClicked_;
                mGroupsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_left_arrow, 0);
            }else{
                mGroupList.setVisibility(View.VISIBLE);
                isClicked_ = !isClicked_;
                mGroupsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
            }
        });

        mChannelsName.setOnClickListener(view -> {
            if(isClicked__){
                mChannelList.setVisibility(View.GONE);
                isClicked__ = !isClicked__;
                mChannelsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_left_arrow, 0);
            }else{
                mChannelList.setVisibility(View.VISIBLE);
                isClicked__ = !isClicked__;
                mChannelsName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
            }
        });

        mDoneButton.setOnClickListener(view -> {

            for(Users u : mContactsFromFirebase){
                if(u.isSelected()) {
                    mPhoneFromSelectedUsers.add("U-"+ u.getPhoneNumber());
                }else{
                    mPhoneFromSelectedUsers.remove("U-"+ u.getPhoneNumber());
                }
            }

            for(Group g : mGroupSubscriptionList){
                if(g.isSelected()){
                    //mPhoneFromSelectedUsers.add("?GrOuP@007_-"+g.getName());
                    mPhoneFromSelectedUsers.add("G-" + g.getName());
                }else{
                    //mPhoneFromSelectedUsers.remove("?GrOuP@007_-"+g.getName());
                    mPhoneFromSelectedUsers.remove("G-" + g.getName());
                }
            }

            for(Channel c : mChannelSubscriptionList){
                if(c.isSelected()){
                    //mPhoneFromSelectedUsers.add("?ChAnNeL@007_-"+c.getName());
                    mPhoneFromSelectedUsers.add("C-" + c.getName());
                }else{
                    //mPhoneFromSelectedUsers.remove("?ChAnNeL@007_-"+c.getName());
                    mPhoneFromSelectedUsers.remove("C-" + c.getName());

                }
            }


            if(mPhoneFromSelectedUsers.size() > 0){
                new TTFancyGifDialog.Builder(UnwantedActivity.this)
                        .setTitle(getString(R.string._disturb))
                        .setMessage(getString(R.string.dis_tr_))
                        .setGifResource(R.drawable.gif16)
                        .isCancellable(false)
                        .OnPositiveClicked(() ->{

                            progressDialog.setTitle(getString(R.string.r_data));
                            progressDialog.setMessage(getString(R.string.mood_waiting_));
                            progressDialog.show();
                            //TODO: [fm] adding new path for exception
                            for (String s : mPhoneFromSelectedUsers) {
                                fDB.child(myPhoneNumber).child("e").child(s).removeValue()
                                        .addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Toast.makeText(UnwantedActivity.this,
                                                        getString(R.string.upload_success), Toast.LENGTH_SHORT).show();
                                                finish();

                                            }
                                        });
                            }

                            finish();
                        })
                        .OnNegativeClicked(() ->{

                        })

                        .build();
            }else{
                Toast.makeText(UnwantedActivity.this, R.string.mood_err_, Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void getListOfMutedUsers() {
        fDB.child(myPhoneNumber).child("e").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Log.i("MUTED_CALL", dataSnapshot.getValue().toString());
                            Map<String, Long> map = (Map<String, Long>) dataSnapshot.getValue();
                            if(map == null) { return; }

                            for (String s : map.keySet()) {
                                Log.i("MUTED_CONVO_ID", s);
                                String kind = s.substring(0,1);
                                if (kind.equals("G") || kind.equals("C")) {
                                    Query q = kind.equals("C") ? mChannelReference.child(s.substring(2))
                                            : mGroupReference.child(s.substring(2));

                                    q.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.i("MUTED_CONVO_DET", dataSnapshot.getValue().toString());
                                            if (kind.equals("G")) {
                                                Group g = dataSnapshot.getValue(Group.class);
                                                if(g == null) {
                                                    return;
                                                }

                                                mGroupSubscriptionList.add(g);
                                                mMoodGroupAdapter.notifyDataSetChanged();
                                                mGroupProgress.setVisibility(View.GONE);
                                                //comment: [fm] code below may be redundant
                                                if(mGroupSubscriptionList.size() == 0){
                                                    mNoGroupsText.setVisibility(View.VISIBLE);
                                                    mGroupProgress.setVisibility(View.GONE);
                                                }
                                            } else {
                                                Channel c = dataSnapshot.getValue(Channel.class);
                                                if(c == null){
                                                    return;
                                                }

                                                mChannelSubscriptionList.add(c);
                                                mMoodChannelAdapter.notifyDataSetChanged();
                                                mChannelProgress.setVisibility(View.GONE);
                                                //comment: [fm] code below may be redundant
                                                if(mChannelSubscriptionList.size() == 0){
                                                    mNoGroupsText.setVisibility(View.VISIBLE);
                                                    mGroupProgress.setVisibility(View.GONE);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                } else {
//
//                                    mNoGroupsText.setVisibility(View.VISIBLE);
//                                    mGroupProgress.setVisibility(View.GONE);
//                                    mNoGroupsText.setVisibility(View.VISIBLE);
//                                    mGroupProgress.setVisibility(View.GONE);

                                    fDB.child(s.substring(2)).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Log.i("MUTED_USR_DET", dataSnapshot.getValue().toString());
                                            Users u = dataSnapshot.getValue(Users.class);
                                            if(u == null){
                                                return;
                                            }

                                            String nameStored = Users.getLocalContactList().get(u.getPhone());
                                            nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                                    dataSnapshot.getKey();

                                            u.setNameStoredInPhone(nameStored);
                                            u.setPhoneNumber(u.getPhone());

                                            mContactsFromFirebase.add(u);
                                            mUserSelectionAdapter.notifyDataSetChanged();

                                            if(mContactsFromFirebase.size() == 0) {
                                                mNoContactsText.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    private void getBlockedUsers() {
//        mContactsFromFirebase.clear();
//        mChannelSubscriptionList.clear();
//        mGroupSubscriptionList.clear();
        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        //TODO: [fm] very bad idea to add a listener on the root reference. This is downloading the entire DB.
        mRootReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ads_except")){

                    mExceptReference.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            if(map == null){
                                return;
                            }

                            for(String s : map.keySet()){
                                if(s.length() > 14 && s.substring(0, 14).equals("?ChAnNeL@007_-")){
                                    //Channel
                                    String x = s.substring(14);
                                    mChannelReference.child(x).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Channel c = dataSnapshot.getValue(Channel.class);
                                            if(c == null){
                                                return;
                                            }

                                            mChannelSubscriptionList.add(c);
                                            mMoodChannelAdapter.notifyDataSetChanged();
                                            mChannelProgress.setVisibility(View.GONE);

                                            if(mChannelSubscriptionList.size() == 0){
                                                mNoChannelsText.setVisibility(View.VISIBLE);
                                                mChannelProgress.setVisibility(View.GONE);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }else if(s.length() > 12 && s.substring(0, 12).equals("?GrOuP@007_-")){
                                    //Group
                                    String x = s.substring(12);
                                    mGroupReference.child(x).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Group g = dataSnapshot.getValue(Group.class);
                                            if(g == null){
                                                return;
                                            }

                                            mGroupSubscriptionList.add(g);
                                            mMoodGroupAdapter.notifyDataSetChanged();
                                            mGroupProgress.setVisibility(View.GONE);

                                            if(mGroupSubscriptionList.size() == 0){
                                                mNoGroupsText.setVisibility(View.VISIBLE);
                                                mGroupProgress.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }else{
                                    //Users
                                    fDB.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Users u = dataSnapshot.getValue(Users.class);
                                            if(u == null){
                                                return;
                                            }

                                            String nameStored = Users.getLocalContactList().get(u.getPhone());
                                            nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                                    dataSnapshot.getKey();

                                            u.setNameStoredInPhone(nameStored);
                                            u.setPhoneNumber(u.getPhone());

                                            mContactsFromFirebase.add(u);
                                            mUserSelectionAdapter.notifyDataSetChanged();


                                            if(mContactsFromFirebase.size() == 0) {
                                                mNoContactsText.setVisibility(View.VISIBLE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (progressDialog!=null && progressDialog.isShowing() ){
            progressDialog.cancel();
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.mood_menu, menu);
//
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        super.onOptionsItemSelected(item);
//
//        if(item.getItemId() == R.id.menu_unwanted){
//            Intent intent = new Intent(UnwantedActivity.this, UnwantedActivity.class);
//            startActivity(intent);
//        }
//
//        return true;
//    }

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
