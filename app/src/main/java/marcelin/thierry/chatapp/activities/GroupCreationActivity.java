package marcelin.thierry.chatapp.activities;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.UsersAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class GroupCreationActivity extends AppCompatActivity{

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private Toolbar mContactsBar;
    private RecyclerView mContactsList;

    private List<Users> mUsersInGroup = new ArrayList<>();

    private UsersAdapter mUserAdapter;

    private HashMap<String, Object> mPhoneFromSelectedUsers = new HashMap<>();
    private Map<String, Object> mAdmins = new HashMap<>();
    private Map<String, Object> mRegisteredGroup = new HashMap<>();

    private FloatingActionButton mFloatingButton;

    private EditText mGroupId;
    private CircleImageView mGroupPicture;

    private String mCurrentPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
    private String groupId = "";

    private TextView title;
    private ImageView backButton;
    private CircleImageView profileImage;




    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("ContactOnCreate", "method called");
        loadLocale();
        setContentView(R.layout.activity_contacts_group);

        mContactsBar = findViewById(R.id.contacts_bar_layout);
        setSupportActionBar(mContactsBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.group);
      //  getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profileImage);
        title = findViewById(R.id.title);
        title.setTextSize(32);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.allura);
        title.setTypeface(typeface);
        title.setText(R.string.group);
        profileImage.setVisibility(View.GONE);

        mGroupId = findViewById(R.id.group_id);
        mGroupPicture = findViewById(R.id.group_image);

        mUsersInGroup = (List<Users>)getIntent().getSerializableExtra("list_users");

        mUserAdapter = new UsersAdapter(mUsersInGroup);

        mContactsList = findViewById(R.id.contacts_list);
        mContactsList.setHasFixedSize(true);
        mContactsList.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false);
        mContactsList.setLayoutManager(horizontalLayoutManager);
        mContactsList.setAdapter(mUserAdapter);

        mAdmins.put(mCurrentPhoneNumber,ServerValue.TIMESTAMP);

        mFloatingButton = findViewById(R.id.createGroup);

        mFloatingButton.setOnClickListener(view -> {

            if(mGroupId.getText().toString().trim().contains(".") ||
                    mGroupId.getText().toString().trim().contains("$")
                    || mGroupId.getText().toString().trim().contains("#")
                    || mGroupId.getText().toString().trim().contains("[")
                    || mGroupId.getText().toString().trim().contains("]")
                    || mGroupId.getText().toString().trim().contains("/")) {

                Toast.makeText(GroupCreationActivity.this, getString(R.string.group_restriction)
                        , Toast.LENGTH_SHORT).show();
            } else if (TextUtils.isEmpty(mGroupId.getText().
                    toString())) {
                Toast.makeText(GroupCreationActivity.this, R.string.add_name
                        , Toast.LENGTH_SHORT).show();
            } else {

                // TODO: Where all the code will be written
                createGroup();
                Intent goToGroupChat = new Intent(GroupCreationActivity.this,
                        GroupChatActivity.class);
                goToGroupChat.putExtra("Group_id", mGroupId.getText().toString().trim());
                goToGroupChat.putExtra("Group_image", "Default");
                goToGroupChat.putExtra("chat_id", mGroupId.getText().toString().trim());
                startActivity(goToGroupChat);
                finish();
            }

        });

        mGroupPicture.setOnClickListener(view -> {
           Toast.makeText(GroupCreationActivity.this, R.string.group_first
                   , Toast.LENGTH_SHORT).show();
        });

        // Adding the users in the list that will be triggered by the adapter

        mPhoneFromSelectedUsers.put(mCurrentPhoneNumber,createRandomColor());
        for(Users u : mUsersInGroup){
            mPhoneFromSelectedUsers.put(u.getPhoneNumber(),createRandomColor());
        }
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });       

    }



// Method to call when creating the group

    private void createGroup() {

        final String group_reference = "ads_group/";

        final String user_reference = "ads_users/";

        groupId = mGroupId.getText().toString().trim();

        String group_name = "G-" + groupId;

        mRegisteredGroup.put("id", groupId);
        mRegisteredGroup.put("type", "group");
        mRegisteredGroup.put("visible", true);
        mRegisteredGroup.put("phone_number",
                "");
        mRegisteredGroup.put("timestamp",
                ServerValue.TIMESTAMP);

        DatabaseReference link = mRootReference.child(group_reference).push();
        String prefix = "https://";
        String link_key = prefix + link.getKey();

        Map<String, Object> linkMap = new HashMap<>();
        linkMap.put(groupId, link_key);

        Map<String, Object> groupMap = new HashMap<>();
        groupMap.put("image", "Default");
        groupMap.put("thumbnail", "Default");
        groupMap.put("users", mPhoneFromSelectedUsers);
        groupMap.put("admins", mAdmins);
        groupMap.put("timestamp", ServerValue.TIMESTAMP);
        groupMap.put("name", groupId);
        groupMap.put("newName", "");
        groupMap.put("lastMessage", "Default");
        groupMap.put("link", link_key);

        Map<String, Object> groupContentMap = new HashMap<>();
        groupContentMap.put(group_reference + groupId, groupMap);


        mRootReference.updateChildren(groupContentMap, (databaseError, databaseReference) -> {
        });

        for (String key : mPhoneFromSelectedUsers.keySet()) {
            DatabaseReference groupToAddUnderUsers = mRootReference.child(user_reference)
                    .child(key).child("conversation").child(group_name);

            groupToAddUnderUsers.updateChildren(mRegisteredGroup);
        }

        mRootReference.child("group_link").updateChildren(linkMap, ((databaseError, databaseReference) -> {

        }));
    }

    public String createRandomColor(){
        Random random = new Random();

        // create a big random number - maximum is ffffff (hex) = 16777215 (dez)
        int nextInt = random.nextInt(0xffffff + 1);

        // format it as hexadecimal string (with hashtag and leading zeros)

        // print it
        return String.format("#%06x", nextInt);
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