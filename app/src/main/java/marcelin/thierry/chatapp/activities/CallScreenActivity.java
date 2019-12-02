package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Users;

public class CallScreenActivity extends AppCompatActivity {

    private static final String LOG_TAG = CallScreenActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID_RECORD_AUDIO = 22;

    private RtcEngine mRtcEngine;// Tutorial Step 1

    private MediaPlayer mRingTone;

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    private static final DatabaseReference mCallNotifications = FirebaseDatabase.getInstance().getReference()
            .child("ads_call_notifications");

    private String mUserPhone;
    private String mChannelId;
    private ImageView mImageProfile;

    private TextView mCaller, mTimer;

    private long mMillisecondTime, mStartTime, mTimeBuff, mUpdateTime = 0L ;
    private Handler mHandler;
    private int mSeconds, mMinutes, mMilliSeconds, mHours;

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() { // Tutorial Step 1

        @Override
        public void onUserOffline(final int uid, final int reason) { // Tutorial Step 4
            runOnUiThread(() -> onRemoteUserLeft(uid, reason));
        }

        @Override
        public void onUserMuteAudio(final int uid, final boolean muted) { // Tutorial Step 6
            runOnUiThread(() -> onRemoteUserVoiceMuted(uid, muted));
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> onRemoteUserJoinedChannel(uid, elapsed));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_call_screen);

        mUserPhone = getIntent().getStringExtra("user_phone");
        mChannelId = getIntent().getStringExtra("channel_id");

        mHandler = new Handler();
        mCaller = findViewById(R.id.caller);
        mTimer = findViewById(R.id.timer);

        mImageProfile = findViewById(R.id.imageProfile);
        mRingTone = MediaPlayer.create(this, R.raw.progress_tone);
        mRingTone.setLooping(true);
        try {

            if (mRingTone.isPlaying()) {

                mRingTone.stop();
                mRingTone.release();

            }
            mRingTone.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCaller.setText(mUserPhone);

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO)) {
            initAgoraEngineAndJoinChannel();
        }

        mUsersReference.child(mUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                if (u == null) {
                    return;
                }
                Picasso.get().load(u.getThumbnail()).placeholder(R.drawable.ic_avatar).into(mImageProfile);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();     // Tutorial Step 1
        joinChannel();               // Tutorial Step 2
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID_RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO);
                    finish();
                }
                break;
            }
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;

        // Remove the notification from DB

    }

    // Tutorial Step 7
    public void onLocalAudioMuteClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.muteLocalAudioStream(iv.isSelected());
    }

    // Tutorial Step 5
    public void onSwitchSpeakerphoneClicked(View view) {
        ImageView iv = (ImageView) view;
        if (iv.isSelected()) {
            iv.setSelected(false);
            iv.clearColorFilter();
        } else {
            iv.setSelected(true);
            iv.setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
        }

        mRtcEngine.setEnableSpeakerphone(view.isSelected());
    }

    // Tutorial Step 3
    public void onEncCallClicked(View view) {
        finish();
    }

    // Tutorial Step 1
    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    // Tutorial Step 2
    private void joinChannel() {
        mRtcEngine.joinChannel(null, mChannelId, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }

    // Tutorial Step 3
    private void leaveChannel() {
        mRtcEngine.leaveChannel();

        // Newly added
        RtcEngine.destroy();
        mRtcEngine = null;

        try {
            if (mRingTone.isPlaying()) {

                mRingTone.stop();
                mRingTone.release();

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mCallNotifications.child(mUserPhone).child(mChannelId).removeValue();
    }

    // Tutorial Step 4
    private void onRemoteUserLeft(int uid, int reason) {
        showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));

        leaveChannel();
    }


    // Tutorial Step 6
    private void onRemoteUserVoiceMuted(int uid, boolean muted) {
        showLongToast(String.format(Locale.US, "user %d muted or unmuted %b", (uid & 0xFFFFFFFFL), muted));
    }

    private void onRemoteUserJoinedChannel(int uid, int elapsed) {
        // Stop playing the ringing tone that is in loop
        showLongToast(String.format(Locale.US, "user %d joined", (uid & 0xFFFFFFFFL), elapsed));
        try {
            if (mRingTone.isPlaying()) {

                mRingTone.stop();
                mRingTone.release();

            }
        }catch (Exception e){
            e.printStackTrace();
        }

        mTimer.setVisibility(View.VISIBLE);
        mStartTime = SystemClock.uptimeMillis();
        mHandler.postDelayed(runnable, 0);

    }

    public Runnable runnable = new Runnable() {

        public void run() {

            mMillisecondTime = SystemClock.uptimeMillis() - mStartTime;

            mUpdateTime = mTimeBuff + mMillisecondTime;

            mSeconds = (int) (mUpdateTime / 1000);

            mMinutes = mSeconds / 60;

            mHours = mMinutes / 60;

            mSeconds = mSeconds % 60;

            mMilliSeconds = (int) (mUpdateTime % 1000);

            if(mMinutes == 0){
                mTimer.setText(String.format("%02d", mSeconds) + ":"
                        + String.format("%03d", mMilliSeconds));
            }if(mMinutes > 60){
                mTimer.setText("" + mHours + ":"
                        +"" + mMinutes + ":"
                        + String.format("%02d", mSeconds) + ":"
                        + String.format("%03d", mMilliSeconds));
            }

            mHandler.postDelayed(this, 0);
        }

    };

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
