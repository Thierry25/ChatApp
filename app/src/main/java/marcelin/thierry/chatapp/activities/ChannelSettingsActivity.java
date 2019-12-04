package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import marcelin.thierry.chatapp.R;

public class ChannelSettingsActivity extends AppCompatActivity {

    private Toolbar mChannelSettingsBar;
    private RadioGroup mRadioPrivacy;
    private RadioButton mRadioButton;

    private RadioButton mRadioPrivate;
    private RadioButton mRadioPublic;

    private TextView mTextToShowToUsers;
    private TextView mActualLink;

    private FloatingActionButton mContinueToUsers;

    private String mChannelId;
    private String mChannelLink;

    private Map<String, Object> mPublicChannelMap = new HashMap<>();

    private static final DatabaseReference mPublicChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("public_channel");

    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_channel_settings);

        mChannelSettingsBar = findViewById(R.id.channel_settings_bar);
        setSupportActionBar(mChannelSettingsBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.chan_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mChannelId = getIntent().getStringExtra("Channel_id");
        mChannelLink = getIntent().getStringExtra("Link");

        mPublicChannelMap.put(mChannelId, mChannelId.toLowerCase());

        mRadioPrivacy = findViewById(R.id.radioPrivacy);

        mRadioPrivate = findViewById(R.id.radioPrivate);
        mRadioPublic = findViewById(R.id.radioPublic);

        mRadioPrivate.setOnClickListener(view ->
                mTextToShowToUsers.setText(R.string.private_channel_text));

        mRadioPublic.setOnClickListener(view ->
                mTextToShowToUsers.setText(R.string.public_channel_text));

        mTextToShowToUsers = findViewById(R.id.textToShowToUser);
        mActualLink = findViewById(R.id.actualLink);

        mActualLink.setText(mChannelLink);

        mContinueToUsers = findViewById(R.id.continueToUsers);
        mContinueToUsers.setOnClickListener(view -> {

            if (mRadioPublic.isChecked()) {
                mPublicChannelReference.updateChildren(mPublicChannelMap);
                mChannelReference.child(mChannelId).child("isPrivate").setValue(false);
            }

            Intent goToUserSelection = new Intent(ChannelSettingsActivity.this
                    , UserSelectionActivity.class);
            goToUserSelection.putExtra("Channel_id", mChannelId);
            startActivity(goToUserSelection);
            finish();
        });
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
