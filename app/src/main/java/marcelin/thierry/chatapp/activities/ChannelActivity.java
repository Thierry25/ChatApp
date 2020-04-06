package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;

public class ChannelActivity extends AppCompatActivity {

    private Toolbar mChannelToolbar;
    private CircleImageView mChannelImage;
    private EditText mChannelName;
    private EditText mChannelDescription;
    private FloatingActionButton mCreateChannel;

    private Map<String, Object> mUser = new HashMap<>();
    private Map<String, Object> mAdmin = new HashMap<>();
    private Map<String, Object> mRegisteredChannel = new HashMap<>();

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String mCurrentPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

    private String prefix = "https://";
    private String link_key = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_channel);

        mChannelToolbar = findViewById(R.id.channel_bar);
        setSupportActionBar(mChannelToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.channel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChannelImage = findViewById(R.id.channel_image);
        mChannelImage.setOnClickListener(view -> Toast.makeText(ChannelActivity.this,
                R.string.channel_first
                , Toast.LENGTH_SHORT).show());

        mChannelName = findViewById(R.id.channel_id);
        mChannelDescription = findViewById(R.id.channel_description);

        mCreateChannel = findViewById(R.id.create_channel);

        mCreateChannel.setOnClickListener(view -> {
            if(TextUtils.isEmpty(mChannelName.getText().toString().trim()) || TextUtils.isEmpty
                    (mChannelDescription.getText().toString().trim())){
                Toast.makeText(ChannelActivity.this, getString(R.string.channel_creation_information)
                                , Toast.LENGTH_SHORT).show();
            }
            else if(mChannelName.getText().toString().trim().contains(".") ||
                    mChannelName.getText().toString().trim().contains("$")
                    || mChannelName.getText().toString().trim().contains("#")
                    || mChannelName.getText().toString().trim().contains("[")
                    || mChannelName.getText().toString().trim().contains("]")
                    || mChannelName.getText().toString().trim().contains("/")) {

                Toast.makeText(ChannelActivity.this, getString(R.string.channel_restriction)
                        , Toast.LENGTH_SHORT).show();
            } else{
                String name = mChannelName.getText().toString().trim();
                mRootReference.child("ads_channel").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(name)){
                            Toast.makeText(ChannelActivity.this, getString(R.string.channel_creation_error)
                                    , Toast.LENGTH_SHORT).show();
                        }else{
                            createChannel();
                            Intent goToChannelSettings = new Intent(ChannelActivity.this,
                                    ChannelSettingsActivity.class);
                            goToChannelSettings.putExtra("Channel_id", mChannelName.getText()
                                    .toString().trim());
                            goToChannelSettings.putExtra("Link", link_key);
                            startActivity(goToChannelSettings);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        });

        mUser.put(mCurrentPhoneNumber, ServerValue.TIMESTAMP);
        mAdmin.put(mCurrentPhoneNumber, ServerValue.TIMESTAMP);

    }

    private void createChannel(){

        final String channel_reference = "ads_channel/";

        final String user_reference = "ads_users/";

        Map<String, Object> mapLink = new HashMap<>();

        String channel_id = mChannelName.getText().toString().trim();

        String chan_name = "C-" + channel_id;

        mRegisteredChannel.put("id", channel_id);
        mRegisteredChannel.put("type", "channel");
        mRegisteredChannel.put("visible", true);
        mRegisteredChannel.put("phone_number",
                "");
        mRegisteredChannel.put("timestamp",
                ServerValue.TIMESTAMP);

        DatabaseReference link = mRootReference.child(channel_reference).push();

        link_key = prefix + link.getKey();

        mapLink.put(channel_id, link_key);

        Map<String, Object> channelMap = new HashMap<>();
        channelMap.put("image", "Default");
        channelMap.put("thumbnail", "Default");
        channelMap.put("isPrivate", true);
        channelMap.put("subscribers", mUser);
        channelMap.put("admins", mAdmin);
        channelMap.put("timestamp", ServerValue.TIMESTAMP);
        channelMap.put("name", channel_id);
        channelMap.put("newName", "");
        channelMap.put("lastMessage", "Default");
        channelMap.put("link", link_key);
        channelMap.put("description", mChannelDescription.getText().toString().trim());
        channelMap.put("password", "");
        channelMap.put("email", "");
        channelMap.put("question", "");
        channelMap.put("answer", "");
        channelMap.put("locked", "no");

        Map<String, Object> channelContentMap = new HashMap<>();
        channelContentMap.put(channel_reference + channel_id, channelMap);

        mRootReference.updateChildren(channelContentMap, (databaseError, databaseReference) -> {
        });

            // TODO: Change that code to newest version in order to call out channel, group and chat in the same fragment
            DatabaseReference channelToAddUnderUsers = mRootReference.child(user_reference)
                    .child(mCurrentPhoneNumber).child("conversation").child(chan_name);

            channelToAddUnderUsers.updateChildren(mRegisteredChannel);

            mRootReference.child("channel_link").updateChildren(mapLink);
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
