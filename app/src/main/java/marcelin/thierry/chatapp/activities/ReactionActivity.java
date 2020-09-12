package marcelin.thierry.chatapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.ReactionAdapter;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.CheckInternet_;


public class ReactionActivity extends AppCompatActivity {
    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();

    private static final int PERMISSION_REQ_ID = 22;

    // permission WRITE_EXTERNAL_STORAGE is not mandatory for Agora RTC SDK, just incase if you wanna save logs to external sdcard
    private static final String[] REQUESTED_PERMISSIONS =
            {Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private RtcEngine mRtcEngine;
    private static final DatabaseReference mCallNotifications = FirebaseDatabase.getInstance().getReference()
            .child("ads_call_notifications");

    private static final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance().getReference()
            .child("m");

    private List<Messages> mReactionMessages = new ArrayList<>();

    /**
     * Views
     **/
    private MediaPlayer mMediaPlayer;
    private int mCurrentVideoPosition;
    private VideoView videoView;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String mChannelId, mReceiverPhone;
    private RelativeLayout mReactionLayout;
    private EmojiTextView mAdsReaction, mAdsReactionCallerText,
            mPhoneNumber, mAcceptReaction, mDeclineReaction;
    private CircleImageView mProfilePic;
    private LinearLayout mLiveLayout, mTextLayout, mMessageLinLayout;
    private FrameLayout mLocalVideo, mOtherVideo;
    private RecyclerView mMessagesList;
    private ImageButton mSendEmoji, mSendTextButton;
    private EmojiEditText mSendText;
    private RelativeLayout mRootView;
    private String mMyPhoneNumber;
    private ReactionAdapter mReactionAdapter;
    private ImageButton mDelete;

    /* Intent values */
    private boolean fromMe;
    private String mCallerPhone;

    private static final DatabaseReference mUsersReference =
            FirebaseDatabase.getInstance().getReference().child("ads_users");

    private final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> onRemoteUserLeft(uid, reason));
        }

        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) {
            runOnUiThread(() -> onRemoteUserVideoMuted(uid, muted));
        }

        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> onRemoteUserJoinedChannel(uid, elapsed));
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reaction);
        videoView = findViewById(R.id.videoView);


        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.react_bg);
        videoView.setVideoURI(uri);
        videoView.start();
        videoView.setOnPreparedListener(mp -> {
            int videoWidth = mp.getVideoWidth();
            int videoHeight = mp.getVideoHeight();

            //Get VideoView's current width and height
            int videoViewWidth = videoView.getWidth();
            int videoViewHeight = videoView.getHeight();

            float xScale = (float) videoViewWidth / videoWidth;
            float yScale = (float) videoViewHeight / videoHeight;

            //For Center Crop use the Math.max to calculate the scale
            //float scale = Math.max(xScale, yScale);
            //For Center Inside use the Math.min scale.
            //I prefer Center Inside so I am using Math.min
            float scale = Math.min(xScale, yScale);

            float scaledWidth = scale * videoWidth;
            float scaledHeight = scale * videoHeight;

            //Set the new size for the VideoView based on the dimensions of the video
            ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
            layoutParams.width = (int) scaledWidth;
            layoutParams.height = (int) scaledHeight;
            videoView.setLayoutParams(layoutParams);

            mMediaPlayer = mp;
            mMediaPlayer.setLooping(true);
            if (mCurrentVideoPosition != 0) {
                mMediaPlayer.seekTo(mCurrentVideoPosition);
                mMediaPlayer.start();
            }
        });

        mCallerPhone = getIntent().getStringExtra("user_my_phone");
//        mChannelId = getIntent().getStringExtra("channel_id");
        fromMe = Objects.requireNonNull(getIntent().getExtras()).getBoolean("from_me");

        mRootView = findViewById(R.id.rootView);
        mReactionLayout = findViewById(R.id.reactionLayout);
        mAdsReaction = findViewById(R.id.adsReaction);
        mAdsReactionCallerText = findViewById(R.id.adsReactionCallerText);
        mPhoneNumber = findViewById(R.id.phoneNumber);
        mAcceptReaction = findViewById(R.id.acceptReaction);
        mDeclineReaction = findViewById(R.id.declineReaction);
        mProfilePic = findViewById(R.id.profilePic);
        mLiveLayout = findViewById(R.id.liveLayout);
        mLocalVideo = findViewById(R.id.localVideo);
        mOtherVideo = findViewById(R.id.otherVideo);
        mTextLayout = findViewById(R.id.textLayout);
        mMessagesList = findViewById(R.id.messagesList);
        mMessageLinLayout = findViewById(R.id.messageLinLayout);
        mSendEmoji = findViewById(R.id.sendEmoji);
        mSendText = findViewById(R.id.sendText);
        mSendTextButton = findViewById(R.id.sendTextButton);
        mDelete = findViewById(R.id.delete);

        mMyPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);
        mMessagesList.setNestedScrollingEnabled(false);

        mReactionAdapter = new ReactionAdapter(mReactionMessages);
        mMessagesList.setAdapter(mReactionAdapter);

        if (fromMe) {
            mChannelId = getIntent().getStringExtra("channel_id_chat_activity");
            mReceiverPhone = getIntent().getStringExtra("userPhone");
        } else {
            mChannelId = getIntent().getStringExtra("channel_id");
            mReceiverPhone = getIntent().getStringExtra("user_phone");
        }

        if (fromMe) {
            mReactionLayout.setVisibility(View.GONE);
            mAdsReactionCallerText.setVisibility(View.VISIBLE);
            String nameCaller = Users.getLocalContactList().get(mReceiverPhone);
            if (nameCaller != null) {
                mAdsReactionCallerText.setText(String.format(getString(R.string.waiting_rq), nameCaller));
            } else {
                mAdsReactionCallerText.setText(String.format(getString(R.string.waiting_rq), mReceiverPhone));
            }
            // joinChannel();
        } else {
            mReactionLayout.setVisibility(View.VISIBLE);

            mAcceptReaction.setOnClickListener(v1 -> {

                mReactionLayout.setVisibility(View.GONE);
                mAdsReaction.setVisibility(View.GONE);
                videoView.setVisibility(View.GONE);
                mLiveLayout.setVisibility(View.VISIBLE);
                mTextLayout.setVisibility(View.VISIBLE);
                mMessageLinLayout.setVisibility(View.VISIBLE);

                joinChannel();

            });

            mDeclineReaction.setOnClickListener(v -> {
                finish();
                leaveChannel();
            });

            mDelete.setOnClickListener(v ->{
                finish();
                leaveChannel();
            });

            mUsersReference.child(mReceiverPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if (u == null) {
                        return;
                    }
                    Picasso.get().load(u.getThumbnail()).placeholder(R.drawable.ic_avatar).into(mProfilePic);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            String name = Users.getLocalContactList().get(mReceiverPhone);
            if (name != null) {
                mPhoneNumber.setText(String.format(getString(R.string.reqst_react), name));
            } else {
                mPhoneNumber.setText(String.format(getString(R.string.reqst_react), mReceiverPhone));
            }

        }

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[2], PERMISSION_REQ_ID)) {
            //  initAgoraEngineAndJoinChannel();
            if (fromMe) {
                initAgoraEngineAndJoinChannel();
            } else {
                initAgoraEngineAndJoinChannel1();
//                mAcceptReaction.setOnClickListener(v->{
//                    initAgoraEngineAndJoinChannel();
//                });
            }
        }

        mSendTextButton.setOnClickListener(v -> {
            sendMessage();
        });

        loadReactionMessages();
    }

    private void initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine();
        setupVideoProfile();
        setupLocalVideo();
        joinChannel();
    }

    private void initAgoraEngineAndJoinChannel1() {
        initializeAgoraEngine();
        setupVideoProfile();
        setupLocalVideo();
        //  joinChannel();
    }

    private void initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(getBaseContext(), getString(R.string.agora_app_id), mRtcEventHandler);
            mRtcEngine.adjustRecordingSignalVolume(0);

        } catch (Exception e) {
            Log.e(LOG_TAG, Log.getStackTraceString(e));

            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
    }

    private void setupVideoProfile() {
        mRtcEngine.enableVideo();

//      mRtcEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, false); // Earlier than 2.3.0
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(VideoEncoderConfiguration.VD_640x360, VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    private void setupLocalVideo() {
        mLocalVideo = findViewById(R.id.localVideo);

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        surfaceView.setZOrderMediaOverlay(true);

        mLocalVideo.addView(surfaceView);
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
    }

    public boolean checkSelfPermission(String permission, int requestCode) {
        Log.i(LOG_TAG, "checkSelfPermission " + permission + " " + requestCode);
        if (ContextCompat.checkSelfPermission(this,
                permission)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    REQUESTED_PERMISSIONS,
                    requestCode);
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mCurrentVideoPosition  = mMediaPlayer.getCurrentPosition();
//        videoView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //    videoView.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMediaPlayer.release();
        mMediaPlayer = null;

        try{
            leaveChannel();
        }catch (Exception e){
            e.printStackTrace();
        }
       try {
           mCallNotifications.child(mReceiverPhone).child(mChannelId).removeValue();
           mMessagesReference.child(mChannelId).removeValue();
       }catch (Exception e){
           e.printStackTrace();
       }
    }

    private void setupRemoteVideo(int uid) {

        if (mOtherVideo.getChildCount() >= 1) {
            return;
        }

        SurfaceView surfaceView = RtcEngine.CreateRendererView(getBaseContext());
        mOtherVideo.addView(surfaceView);
        mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));

        surfaceView.setTag(uid);
    }

    private void onRemoteUserLeft(int uid, int reason) {
        mOtherVideo.removeAllViews();

        String name = Users.getLocalContactList().get(mReceiverPhone);
        if (name != null) {
            Toast.makeText(this, String.format(getString(R.string.left), name), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, String.format(getString(R.string.left), mReceiverPhone), Toast.LENGTH_SHORT).show();
        }
     //   showLongToast(String.format(Locale.US, "user %d left %d", (uid & 0xFFFFFFFFL), reason));
        mSendText.setEnabled(false);
        finish();

//        View tipMsg = findViewById(R.id.quick_tips_when_use_agora_sdk); // optional UI
//        tipMsg.setVisibility(View.VISIBLE);

        try{
            leaveChannel();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public final void showLongToast(final String msg) {
        this.runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show());
    }

    private void leaveChannel() {

//        mRtcEngine.leaveChannel();

        // TODO: REMOVE COMMENT IF ERRROR Newly added
//        RtcEngine.destroy();
//        mRtcEngine = null;

//        try {
//            if (mRingTone.isPlaying()) {
//
//                mRingTone.stop();
//                mRingTone.release();
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        try{
            mCallNotifications.child(mCallerPhone).child(mChannelId).removeValue();
        }catch(Exception e){
            e.printStackTrace();
        }
        mRtcEngine.leaveChannel();
        RtcEngine.destroy();
        mRtcEngine = null;
    }

    private void onRemoteUserVideoMuted(int uid, boolean muted) {

        SurfaceView surfaceView = (SurfaceView) mOtherVideo.getChildAt(0);

        Object tag = surfaceView.getTag();
        if (tag != null && (Integer) tag == uid) {
            surfaceView.setVisibility(muted ? View.GONE : View.VISIBLE);
        }
    }

    private void onRemoteUserJoinedChannel(int uid, int elapsed) {
        // Stop playing the ringing tone that is in loop
        //showLongToast(String.format(Locale.US, "user %d joined", (uid & 0xFFFFFFFFL), elapsed));
        String name = Users.getLocalContactList().get(mReceiverPhone);
        if (name != null) {
            showLongToast(name);
        } else {
            showLongToast(mReceiverPhone);
        }
        mReactionLayout.setVisibility(View.GONE);
        mAdsReactionCallerText.setVisibility(View.GONE);
        videoView.setVisibility(View.GONE);
        mLiveLayout.setVisibility(View.VISIBLE);
        mTextLayout.setVisibility(View.VISIBLE);
        mAdsReaction.setVisibility(View.GONE);
        mMessageLinLayout.setVisibility(View.VISIBLE);

//        try {
//            if (mRingTone.isPlaying()) {
//
//                mRingTone.stop();
//                mRingTone.release();
//
//            }
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }

    private void joinChannel() {
        mRtcEngine.joinChannel(null, mChannelId, "Extra Optional Data", 0); // if you do not specify the uid, we will generate the uid for you
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode);

        switch (requestCode) {
            case PERMISSION_REQ_ID: {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED || grantResults[2] != PackageManager.PERMISSION_GRANTED) {
                    showLongToast("Need permissions " + Manifest.permission.RECORD_AUDIO + "/" + Manifest.permission.CAMERA + "/" + Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    finish();
                    break;
                }
                //   initAgoraEngineAndJoinChannel();
                if (fromMe) {
                    initAgoraEngineAndJoinChannel();
                } else {
                    initAgoraEngineAndJoinChannel1();
//                    mAcceptReaction.setOnClickListener(v->{
//                        initAgoraEngineAndJoinChannel();
//                    });
                }
                break;
            }
        }
    }

    /*
     if(fromMe){
            mChannelId = getIntent().getStringExtra("channel_id_chat_activity");
            mReceiverPhone = getIntent().getStringExtra("userPhone");
        }else {
            mChannelId = getIntent().getStringExtra("channel_id");
            mReceiverPhone = getIntent().getStringExtra("user_phone");
        }
     */

    private void sendMessage() {
        String message = Objects.requireNonNull(mSendText.getText()).toString().trim();
        new CheckInternet_(internet -> {
            if (internet) {
                if (!TextUtils.isEmpty(message)) {

                    DatabaseReference msg_push = mCallNotifications.push();

                    String push_id = msg_push.getKey();

                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("content", message);
                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                    messageMap.put("type", "text");
                    messageMap.put("parent", "Default");
                    messageMap.put("visible", true);
                    messageMap.put("from", mMyPhoneNumber);
                    messageMap.put("seen", false);

                    mMessagesReference.child(mChannelId).child(push_id).updateChildren(messageMap);
                }
                mSendText.setText("");
            } else {
                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void loadReactionMessages() {
            mMessagesReference.child(mChannelId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Messages m = dataSnapshot.getValue(Messages.class);
                    if (m == null) {
                        return;
                    }
                    mReactionMessages.add(m);
                    mReactionAdapter.notifyDataSetChanged();
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
        }
    }

