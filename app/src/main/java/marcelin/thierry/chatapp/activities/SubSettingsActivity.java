package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Channel;

public class SubSettingsActivity extends AppCompatActivity {

    private Toolbar mSubSettingsBar;

    private CircleImageView mPicId;
    private TextView mChanName;
    private TextView mTimeCreated;
    private TextView mAmountOfSubscribers;

    private String mChannelName;
    private TextView mUnsubscribe;
    private TextView mMedia;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String mCurrentPhoneNumber;

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");
    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_sub_settings);

        mChannelName = getIntent().getStringExtra("chat_id");

        mPicId = findViewById(R.id.picId);
        mChanName = findViewById(R.id.chan_name);
        mTimeCreated = findViewById(R.id.time_created);
        mAmountOfSubscribers = findViewById(R.id.amount_subscribers);

        mUnsubscribe = findViewById(R.id.unsubscribe);
        mMedia = findViewById(R.id.media);

        mSubSettingsBar = findViewById(R.id.sub_settings_bar);
        setSupportActionBar(mSubSettingsBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(mChannelName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCurrentPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        getImage();
        getInfo();

        mUnsubscribe.setOnClickListener(view -> unsubscribeToChannel());

        mMedia.setOnClickListener(view -> goToChannelMediaShared());

    }


    private void goToChannelMediaShared() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, (id, dialog) ->{

            Intent intent = new Intent(this, ChannelSharedActivity.class);
            intent.putExtra("chat_name", mChannelName);
            intent.putExtra("chat_id", mChannelName);
            startActivity(intent);
            finish();

        });

        builder.setNegativeButton(R.string.cancel, (id, dialog) ->{

            Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setIcon(R.drawable.ic_warning);
        alertDialog.setTitle(R.string.media_loading);
        alertDialog.setMessage(getString(R.string.r_sure) + " " + mChannelName
        + "?" + "\n"+ getString(R.string.time_takin));
        alertDialog.show();
    }

    private void unsubscribeToChannel() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.ok, (dialog, id) ->{

            mChannelReference.child(mChannelName).child("subscribers").child(mCurrentPhoneNumber)
                    .removeValue();

            mUsersReference.child(mCurrentPhoneNumber).child("conversation").child("C-"+mChannelName)
                    .removeValue();

        });

        builder.setNegativeButton(R.string.cancel, (dialog, id) ->
                Toast.makeText(this, getString(R.string.cancel), Toast.LENGTH_SHORT).show());

        AlertDialog dialog = builder.create();
        dialog.setIcon(R.drawable.ic_warning);
        dialog.setTitle(getString(R.string.unsub));
        dialog.setMessage(getString(R.string.unsubscribe_text) + mChannelName + "?");
        dialog.show();

    }

    private void getInfo(){
        mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Channel c = dataSnapshot.getValue(Channel.class);
                String name;
                if (c != null) {
                    int subscribers = c.getSubscribers().size();

                    if(c.getNewName().equals("")){
                        name = c.getName();
                    }else{
                        name = c.getNewName();
                    }

                    String date = getDate(c.getTimestamp());

                    mChanName.setText(name);

                    mTimeCreated.setText(mTimeCreated.getText() + " " + date);

                    mAmountOfSubscribers.setText(subscribers + " "  + mAmountOfSubscribers.getText());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getImage(){

        mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                if(!image.equals("Default")) {

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_avatar)
                            .into(mPicId, new Callback() {
                                @Override
                                public void onSuccess() {

                                    Toast.makeText(SubSettingsActivity.this,
                                            "Done", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(image)
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(mPicId, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {

                                                }
                                            });

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(time);
            return DateFormat.format("MMM dd, HH:mm", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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
