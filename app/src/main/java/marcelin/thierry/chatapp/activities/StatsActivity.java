package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

//import app.frantic.kplcompressor.KplCompressor;
import app.frantic.kplcompressor.KplCompressor;
import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
//import droidninja.filepicker.utils.Orientation;
import jp.shts.android.storiesprogressview.StoriesProgressView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.TryAdapter;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import marcelin.thierry.chatapp.classes.Status;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.CheckInternet_;

public class StatsActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    private StoriesProgressView storiesProgressView;
    private ImageView image;
    private VideoView videoAdded;

    private int counter = 0;
    static final String TAG = "StatsActivity";

    private FirebaseAuth mAuth;
    private String mPhoneNumber;
    private Map<String, Object> mRead = new HashMap<>();
    private CircleImageView mIm;
    private TextView mName, mTimestamp, mNumberOfSeen;
    private String time;
    private RelativeLayout relLayout;
    private LinearLayout mLinLayoutForSeen;
    private View mRootView;
    private MediaRecorder mRecorder;
    private List<String> mImagesPath;
    private List<String> mVideosPath;
    private List<Uri> mImagesData;
    private List<Uri> mVideosData;
    private static String mFileName = null;
    private RelativeLayout l;

    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
    private static final String LOG_TAG = "AudioRecordTest";
    private static final String[] WALK_THROUGH = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    private ArrayList<String> mDocPath = new ArrayList<>();
    private static final int GALLERY_PICK = 1;
    private static final int MAX_ATTACHMENT_COUNT = 20;
    //private static final int TOTAL_ITEMS_TO_LOAD = 30;

    final java.util.Random rand = new java.util.Random();
    final Set<String> identifiers = new HashSet<>();

    private Map<String, Object> mSeenMap;
    private Dialog mSeenDialog, mReplyDialog, mAttachmentDialog;
    private List<Users> mUsersList = new ArrayList<>();
    private TryAdapter tryAdapter;

    private String mStatusId, mStatusPhone, mStatusType, /*To Remove if crash*/mChatPhone, mConvoRef;

    private Map<String, Object> mChatUsers = new HashMap<>();
    private List<String> mUsers = new ArrayList<>();
    private static final DatabaseReference mStatusReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_status");

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();

//    private final long[] durations = new long[]{
//
//            500L, 1000L, 1500L, 4000L, 5000L, 1000,
//    };

    long pressTime = 0L;
    long limit = 500L;

    List<Status> resources;
    EmojiPopup emojiPopup;

    // TO REPLY
    private LinearLayout mainVLayout,replyLiearLayout;
    private RelativeLayout replyTextLayout;
    private TextView senderOfMessage, messageReceived, audioSent, videoSent, documentSent;
    private ImageView imageSent;
    private ImageButton send_emoji, send_attachment;
    private EmojiEditText send_text;
    private RecordView record_view;
    private RecordButton record_button;
    private boolean isFirstTime = true;
    private static final DatabaseReference mNotificationsDatabase = FirebaseDatabase.getInstance()
            .getReference().child("ads_notifications");

    private MediaPlayer mp1;

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final StorageReference mImagesStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mVideosStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mAudioStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mDocumentsStorage = FirebaseStorage.getInstance().getReference();


    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    storiesProgressView.setVisibility(View.GONE);
                    relLayout.setVisibility(View.GONE);
                    if(videoAdded.isPlaying()){
                        videoAdded.pause();
                    }
                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.setVisibility(View.VISIBLE);
                    relLayout.setVisibility(View.VISIBLE);
                    storiesProgressView.resume();
                    if(!videoAdded.isPlaying()){
                        videoAdded.start();
                    }
                    return limit < now - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        loadLocale();
        setContentView(R.layout.activity_stats);

        resources = (List<Status>) getIntent().getSerializableExtra("statusList");

        mp1 = MediaPlayer.create(StatsActivity.this, R.raw.playsound);

        mSeenDialog = new Dialog(this, R.style.CustomDialogTheme);
        mReplyDialog = new Dialog(this, R.style.CustomDialogTheme1);
        mAttachmentDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        Objects.requireNonNull(mSeenDialog.getWindow()).setGravity(Gravity.BOTTOM);
        Objects.requireNonNull(mReplyDialog.getWindow()).setGravity(Gravity.BOTTOM);


        // Saving the URI returned by the video and image library
        mImagesData = new ArrayList<>();
        mVideosData = new ArrayList<>();

        mFileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
        mFileName += "/" + randomIdentifier() + ".3gp";

        relLayout = findViewById(R.id.relLayout);

        mIm = findViewById(R.id.mIm);
        mName = findViewById(R.id.name);
        mTimestamp = findViewById(R.id.timestamp);

        mAuth = FirebaseAuth.getInstance();
        mPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        tryAdapter = new TryAdapter(mUsersList);

        mLinLayoutForSeen = findViewById(R.id.linLayoutForSeen);
        mNumberOfSeen = findViewById(R.id.numberOfSeen);

        TextView mReplyArrow = findViewById(R.id.reply_arrow);

        storiesProgressView = findViewById(R.id.stories);
        storiesProgressView.setStoriesCount(resources.size());
       // storiesProgressView.setStoryDuration(5000L);
        // or
        // storiesProgressView.setStoriesCountWithDurations(durations);
        storiesProgressView.setStoriesListener(this);
//        storiesProgressView.startStories();
        //counter = 2;
//        storiesProgressView.startStories(counter);

        image = findViewById(R.id.image);
        videoAdded = findViewById(R.id.videoAdded);
        l = findViewById(R.id.l);

        if(resources.get(counter).getFrom().equals("text")){
            if(videoAdded.isPlaying()){
                videoAdded.stopPlayback();
            }
            l.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.get().load(resources.get(counter).getContent()).placeholder(R.drawable.ic_avatar)
                    .into(image);
            storiesProgressView.setStoryDuration(5000L);
            storiesProgressView.startStories(counter);
        }else if(resources.get(counter).getFrom().equals("image")){
            if(videoAdded.isPlaying()){
                videoAdded.stopPlayback();
            }
            l.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            image.setAdjustViewBounds(true);
            image.setBackgroundColor(getResources().getColor(R.color.black));
            Picasso.get().load(resources.get(counter).getContent()).placeholder(R.drawable.ic_avatar)
                    .into(image);
            storiesProgressView.setStoryDuration(5000L);
            storiesProgressView.startStories(counter);
        }else{
            image.setVisibility(View.GONE);
            l.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(resources.get(counter).getContent());
            videoAdded.setVideoURI(videoUri);
            videoAdded.setOnPreparedListener(mp -> {
//                mp.setLooping(true);
                storiesProgressView.setStoryDuration(videoAdded.getDuration());
                storiesProgressView.startStories(counter);
                videoAdded.start();
            });
        }
       // image.setImageResource(resources.get(counter).getContent());
//        Picasso.get().load(resources.get(counter).getContent()).placeholder(R.drawable.ic_avatar)
//        .into(image);

        mRootReference.child("ads_users").child(resources.get(0).getPhoneNumber())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String thumbnail = Objects.requireNonNull(dataSnapshot.child("thumbnail").getValue()).toString();
                        Picasso.get().load(thumbnail).into(mIm);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        mChatPhone = resources.get(0).getPhoneNumber();

        if(resources.get(0).getPhoneNumber().equals(mPhoneNumber)){
            mName.setText(getString(R.string.me_));
        } else{
            mName.setText(Users.getLocalContactList().get(
                    resources.get(0).getPhoneNumber()));
        }


        time = getDate(resources.get(0).getTimestamp());
        mTimestamp.setText(time);
        // bind reverse view
        View reverse = findViewById(R.id.reverse);
        reverse.setOnClickListener(v -> storiesProgressView.reverse());
        reverse.setOnTouchListener(onTouchListener);

        // bind skip view
        View skip = findViewById(R.id.skip);
        skip.setOnClickListener(v -> storiesProgressView.skip());
        skip.setOnTouchListener(onTouchListener);

        if(resources.get(0).getPhoneNumber().equals(mPhoneNumber)) {
            mLinLayoutForSeen.setVisibility(View.VISIBLE);
        }else{
            mReplyArrow.setVisibility(View.VISIBLE);
        }

        mSeenMap = resources.get(0).getSeenBy();
        mStatusId = resources.get(0).getId();
        mStatusType = resources.get(0).getFrom();
        mStatusPhone = resources.get(0).getPhoneNumber();

        mNumberOfSeen.setText(String.valueOf(resources.get(0).getSeenBy().size()));

        //}

        // Add onClick on the layout
        mLinLayoutForSeen.setOnClickListener((view -> {

            //open up dialog
    //        storiesProgressView.pause();
            showCustomDialog();


        }));

        if(mSeenDialog.isShowing()){
            storiesProgressView.pause();
        }else{
            storiesProgressView.resume();
        }

        // Add onClick on the Image
        mReplyArrow.setOnClickListener(view -> {
            // show custom
            askPermission();
        });

        mUsers.add(mPhoneNumber);
        mUsers.add(mChatPhone);

        for (int i = 0; i < mUsers.size(); i++) {
            mChatUsers.put(mUsers.get(i), true);
        }


    }

    private void showCustomReplyDialog(){

        //  TODO: Create view for reply on statuses.
        storiesProgressView.pause();
        mUsersList.clear();

        mReplyDialog.setContentView(R.layout.status_reply_layout);
        mReplyDialog.show();
        mReplyDialog.setCanceledOnTouchOutside(true);
        mReplyDialog.setCancelable(true);

        mainVLayout = mReplyDialog.findViewById(R.id.mainVLayout);
        replyLiearLayout = mReplyDialog.findViewById(R.id.replyLinearLayout);
        replyTextLayout = mReplyDialog.findViewById(R.id.replyTextLayout);
        senderOfMessage = mReplyDialog.findViewById(R.id.senderOfMessage);
        messageReceived = mReplyDialog.findViewById(R.id.messageReceived);
        audioSent = mReplyDialog.findViewById(R.id.audioSent);
        videoSent = mReplyDialog.findViewById(R.id.videoSent);
        documentSent = mReplyDialog.findViewById(R.id.documentSent);
        imageSent = mReplyDialog.findViewById(R.id.imageSent);
        send_emoji = mReplyDialog.findViewById(R.id.send_emoji);
        send_attachment = mReplyDialog.findViewById(R.id.send_attachment);
        send_text = mReplyDialog.findViewById(R.id.send_text);
        record_view = mReplyDialog.findViewById(R.id.record_view);
        record_button = mReplyDialog.findViewById(R.id.record_button);
        mRootView = mReplyDialog.findViewById(R.id.rootView);

        record_button.setRecordView(record_view);
        record_button.setListenForRecord(true);

        send_emoji.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        send_emoji.setOnClickListener(ignore -> emojiPopup.toggle());

        record_view.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {

                //Start Recording..
                startRecording();

                //   mSend.setVisibility(View.GONE);
                send_attachment.setVisibility(View.GONE);
                send_emoji.setVisibility(View.GONE);
                send_text.setVisibility(View.GONE);

                record_view.setVisibility(View.VISIBLE);
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                Toast.makeText(StatsActivity.this, getString(R.string.voice_cancelled), Toast.LENGTH_SHORT).show();

                mRecorder.reset();
                mRecorder.release();
                //  mRecorder.stop();


                //       mSend.setVisibility(View.GONE);
                send_emoji.setVisibility(View.VISIBLE);
                send_attachment.setVisibility(View.VISIBLE);
                send_text.setVisibility(View.VISIBLE);

                record_view.setVisibility(View.GONE);

                Log.d("RecordView", "onCancel");

            }

            @Override
            public void onFinish(long recordTime) {
                //Stop Recording..

                stopRecording();
                Toast.makeText(StatsActivity.this, getString(R.string.voice_recorded), Toast.LENGTH_SHORT).show();
                sendAudio(resources.get(counter).getId());

                //mSend.setVisibility(View.GONE);
                send_attachment.setVisibility(View.VISIBLE);
                send_emoji.setVisibility(View.VISIBLE);
                send_text.setVisibility(View.VISIBLE);

                record_view.setVisibility(View.GONE);

                String time = getHumanTimeText(recordTime);
                Log.d("RecordView", "onFinish");

                Log.d("RecordTime", time);
            }

            @Override
            public void onLessThanSecond() {
                Toast.makeText(StatsActivity.this, getString(R.string.voice_warning),
                        Toast.LENGTH_SHORT).show();

                mRecorder.reset();
                mRecorder.release();
                //  mRecorder.stop();

                record_view.setVisibility(View.GONE);

                send_emoji.setVisibility(View.VISIBLE);
                //    mSend.setVisibility(View.GONE);
                send_emoji.setVisibility(View.VISIBLE);
                send_attachment.setVisibility(View.VISIBLE);
                send_text.setVisibility(View.VISIBLE);

                Log.d("RecordView", "onLessThanSecond");
            }
        });


  //      ListenForRecord must be false ,otherwise onClick will not be called

        record_view.setOnBasketAnimationEndListener(() -> Log.d("RecordView", "Basket Animation Finished"));

        record_view.setCancelBounds(8);
        record_view.setSmallMicColor(Color.parseColor("#FFD700"));


        record_button.setOnRecordClickListener(v -> {
            sendMessage(send_text, resources.get(counter).getId());
            Log.d("RecordButton","RECORD BUTTON CLICKED");
        });

        setUpEmojiPopup(mRootView, send_emoji, send_text);
        send_text.addTextChangedListener(textWatcher);
        senderOfMessage.setText(Users.getLocalContactList().get(mChatPhone) + " " + "•" +" "+ getString(R.string.s));

       if(resources.get(counter).getFrom().equals("text")){
           String text = resources.get(counter).getTextEntered();
           if(text.length() > 25) {
               text = text.substring(0, 21) + "...";
               messageReceived.setText(text);
           } else{
               messageReceived.setText(resources.get(counter).getTextEntered());
           }
       }else if(resources.get(counter).getFrom().equals("image")){
           messageReceived.setVisibility(View.GONE);
           imageSent.setVisibility(View.VISIBLE);
           Picasso.get().load(resources.get(counter).getContent()).placeholder(R.drawable.ic_avatar)
                   .into(imageSent);
       }else{
           messageReceived.setVisibility(View.GONE);
           videoSent.setVisibility(View.VISIBLE);
           videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                   0, 0, 0);
           videoSent.setCompoundDrawablePadding(30);
           videoSent.setText(R.string.v);
       }

        send_attachment.setOnClickListener(view1 -> {
            try {
                new CheckInternet_(internet -> {
                   if(internet){
                       showCustomAttachmentDialog();
                   }else{
                       Toast.makeText(StatsActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                   }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private TextWatcher textWatcher = new TextWatcher(){
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (TextUtils.isEmpty(Objects.requireNonNull(send_text.getText()).toString().trim())) {
                record_button.setVisibility(View.VISIBLE);
                record_button.setListenForRecord(true);
                //          mSend.setVisibility(View.GONE);
                record_button.setImageResource(R.drawable.recv_ic_mic_white);

            }else{

                record_button.setListenForRecord(false);
                record_button.setImageResource(R.drawable.ic_send);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    private void sendMessage(TextView textView, String statusId) {

        try {
            new CheckInternet_(internet -> {
               if(internet){
                       String message = textView.getText().toString().trim();

                       if (!TextUtils.isEmpty(message)) {

                           String myReference = "ads_users/" + mPhoneNumber + "/" + "conversation/";
                           String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                           String chat_reference = "ads_chat/";

                           // Creating conversation_id for each message
                           DatabaseReference conversation_push = mRootReference.child("ads_users")
                                   .child(mPhoneNumber).push();
                           String conversation_id = conversation_push.getKey();

                           final String message_reference = "ads_messages/";

                           // Creating push_id for each message
                           DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                           String push_id = msg_push.getKey();

                           //m.setMessageId(push_id);

                           Map<String, Object> messageMap = new HashMap<>();
                           messageMap.put("content", message);
                           messageMap.put("timestamp", ServerValue.TIMESTAMP);
                           messageMap.put("type", "text");
                           messageMap.put("parent", statusId + "/" +mChatPhone);
                           messageMap.put("visible", true);
                           messageMap.put("from", mPhoneNumber);
                           messageMap.put("seen", false);


                           //Toast.makeText(ChatActivity.this, messageMap.get("timestamp").toString(), Toast.LENGTH_SHORT).show();
                           // Going under my phone and check if there is a child "conversation"
                           mRootReference.child("ads_users").child(mPhoneNumber)
                                   .addListenerForSingleValueEvent(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                           if (dataSnapshot.hasChild("conversation")) {

                                               final Conversation[] c = new Conversation[1];
                                               List<Conversation> listConvo = new ArrayList<>();
                                               final boolean[] isThere = {false};
                                               // final String[] mConvoRef = new String[1];

                                               if (!isFirstTime) {

//                                   Toast.makeText(ChatActivity.this, String.valueOf(m.getTimestamp()), Toast.LENGTH_SHORT).show();
                                                   // Getting reference to conversation_id under ads_user and push new messages
                                                   DatabaseReference addNewMessage =
                                                           mRootReference.child("ads_chat")
                                                                   .child(mConvoRef)
                                                                   .child("messages").child(push_id);

                                                   mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                           .setValue(push_id);

//                                            if (replyLinearLayout.getVisibility() == View.VISIBLE) {
//                                                messageMap.put("parent", Messages.getClickedMessageId());
//                                                // Remove if crashed
//                                                replyLinearLayout.setVisibility(View.GONE);
//                                            }

                                                   Map<String, Object> msgContentMap = new HashMap<>();
                                                   msgContentMap.put(message_reference +
                                                           push_id, messageMap);

                                                   mRootReference.updateChildren(msgContentMap,(databaseError, databaseReference) -> {
                                                   });

                                                   mUsersReference.child(mPhoneNumber).child("conversation")
                                                           .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                   mUsersReference.child(mChatPhone).child("conversation")
                                                           .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                   textView.setText("");

                                                   Map<String, Object> chatUnderId = new HashMap<>();
                                                   chatUnderId.put("msgId", push_id);
                                                   chatUnderId.put("seen", false);
                                                   chatUnderId.put("visible", true);
                                                   chatUnderId.put("timestamp",
                                                           ServerValue.TIMESTAMP);

                                                   addNewMessage.updateChildren(chatUnderId,
                                                           (databaseError, databaseReference) -> {

                                                               HashMap<String, Object> notificationData
                                                                       = new HashMap<>();
                                                               notificationData.put("from",
                                                                       mPhoneNumber);
                                                               notificationData.put("message",
                                                                       message);

                                                               mNotificationsDatabase.child(mChatPhone)
                                                                       .push().setValue(notificationData)
                                                                       .addOnCompleteListener(task1 -> {

                                                                           if (task1.isSuccessful()) {

                                                                               try {
                                                                                   if (mp1.isPlaying()) {
                                                                                       mp1.stop();
                                                                                       mp1.release();

                                                                                   }
                                                                                   mp1.start();
                                                                               } catch (Exception e) {
                                                                                   e.printStackTrace();
                                                                               }
                                                                               //TODO: update message field seen



                                                                               Toast.makeText(
                                                                                       StatsActivity.this,
                                                                                       "Notification Sent",
                                                                                       Toast.LENGTH_SHORT).show();

                                                                           }
                                                                       });
                                                               //mp1.start();
                                                               //TODO: add sent mark

                                                           });

                                               } else {
                                                   mRootReference.child("ads_users")
                                                           .child(mPhoneNumber)
                                                           .child("conversation")
                                                           .addListenerForSingleValueEvent(new ValueEventListener() {
                                                               @Override
                                                               public void onDataChange(DataSnapshot dataSnapshot) {
                                                                   for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                       c[0] = d.getValue(Conversation.class);
                                                                       listConvo.add(c[0]);
                                                                   }
                                                                   for (int i = 0; i < listConvo.size(); i++) {
                                                                       if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                                           isThere[0] = true;
                                                                           mConvoRef = listConvo.get(i).getId();
                                                                           Log.i("mCoonvo", mConvoRef);
                                                                           break;
                                                                       }
                                                                   }
                                                                   isFirstTime = false;
                                                                   if (isThere[0]) {

                                                                       // Getting reference to conversation_id under ads_user and push new messages
                                                                       DatabaseReference addNewMessage =
                                                                               mRootReference.child("ads_chat")
                                                                                       .child(mConvoRef)
                                                                                       .child("messages").child(push_id);

                                                                       mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                                               .setValue(push_id);

//                                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
//                                                                    messageMap.put("parent", Messages.getClickedMessageId());
//                                                                }

                                                                       Map<String, Object> msgContentMap = new HashMap<>();
                                                                       msgContentMap.put(message_reference +
                                                                               push_id, messageMap);

                                                                       mRootReference.updateChildren(msgContentMap,
                                                                               (databaseError, databaseReference) -> {
                                                                               });

                                                                       mUsersReference.child(mPhoneNumber).child("conversation")
                                                                               .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                       mUsersReference.child(mChatPhone).child("conversation")
                                                                               .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                       textView.setText("");

                                                                       Map<String, Object> chatUnderId = new HashMap<>();
                                                                       chatUnderId.put("msgId", push_id);
                                                                       chatUnderId.put("seen", false);
                                                                       chatUnderId.put("visible", true);
                                                                       chatUnderId.put("timestamp",
                                                                               ServerValue.TIMESTAMP);

                                                                       addNewMessage.updateChildren(chatUnderId,
                                                                               (databaseError, databaseReference) -> {

                                                                                   HashMap<String, Object> notificationData
                                                                                           = new HashMap<>();
                                                                                   notificationData.put("from",
                                                                                           mPhoneNumber);
                                                                                   notificationData.put("message",
                                                                                           message);

                                                                                   mNotificationsDatabase.child(mChatPhone)
                                                                                           .push().setValue(notificationData)
                                                                                           .addOnCompleteListener(task1 -> {

                                                                                               if (task1.isSuccessful()) {

                                                                                                   try {
                                                                                                       if (mp1.isPlaying()) {
                                                                                                           mp1.stop();
                                                                                                           mp1.release();

                                                                                                       }
                                                                                                       mp1.start();
                                                                                                   } catch (Exception e) {
                                                                                                       e.printStackTrace();
                                                                                                   }
                                                                                                   //TODO: update message field seen

                                                                                                   Toast.makeText(
                                                                                                           StatsActivity.this,
                                                                                                           "Notification Sent",
                                                                                                           Toast.LENGTH_SHORT).show();

                                                                                               }
                                                                                           });
                                                                                   //mp1.start();
                                                                                   //TODO: add sent mark

                                                                               });

                                                                       // loadMessages();
                                                                       // Chat.setChatListenerCalled(true);
                                                                   } else {


                                                                       /**
                                                                        * Adding the information under ads_users of my phone and the other user,
                                                                        * if the phone number doesn't match the other user
                                                                        *
                                                                        */

                                                                       mConvoRef = conversation_id;

                                                                       Map<String, Object> infoToAddUnderMe
                                                                               = new HashMap<>();
                                                                       infoToAddUnderMe.put("id", conversation_id);
                                                                       infoToAddUnderMe.put("type", "chat");
                                                                       infoToAddUnderMe.put("visible", true);
                                                                       infoToAddUnderMe.put("phone_number",
                                                                               mChatPhone);
                                                                       infoToAddUnderMe.put("timestamp",
                                                                               ServerValue.TIMESTAMP);

                                                                       Map<String, Object> mapForCurrentUser
                                                                               = new HashMap<>();
                                                                       mapForCurrentUser.put(myReference +
                                                                               conversation_id, infoToAddUnderMe);

                                                                       mRootReference.updateChildren(mapForCurrentUser);

                                                                       Map<String, Object> infoToAddUnderOther
                                                                               = new HashMap<>();

                                                                       infoToAddUnderOther.put("id", conversation_id);
                                                                       infoToAddUnderOther.put("type", "chat");
                                                                       infoToAddUnderOther.put("visible", true);
                                                                       infoToAddUnderOther.put("phone_number",
                                                                               mPhoneNumber);
                                                                       infoToAddUnderOther.put("timestamp",
                                                                               ServerValue.TIMESTAMP);

                                                                       Map<String, Object> mapForOtherUser
                                                                               = new HashMap<>();
                                                                       mapForOtherUser.put(otherUserReference +
                                                                               conversation_id, infoToAddUnderOther);

                                                                       mRootReference.updateChildren(mapForOtherUser);

                                                                       /**
                                                                        * Adding information into ads_chat &
                                                                        * ads_messages
                                                                        */

                                                                       DatabaseReference usersInChat = mRootReference
                                                                               .child("ads_chat").child(conversation_id)
                                                                               .child("messages").child(push_id);

//                                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
//                                                                    messageMap.put("parent", Messages.getClickedMessageId());
//                                                                }

                                                                       Map<String, Object> msgContentMap = new HashMap<>();
                                                                       msgContentMap.put(message_reference +
                                                                               push_id, messageMap);

                                                                       //Adding message
                                                                       mRootReference.updateChildren(msgContentMap,
                                                                               (databaseError, databaseReference) -> {
                                                                               });

                                                                       Map<String, Object> chatRefMap = new HashMap<>();
                                                                       chatRefMap.put("users", mChatUsers);
                                                                       chatRefMap.put("seen", false);
                                                                       chatRefMap.put("visible", true);
                                                                       chatRefMap.put("lastMessage", push_id);

                                                                       Map<String, Object> messageUserMap =
                                                                               new HashMap<>();

                                                                       messageUserMap.put(chat_reference +
                                                                               conversation_id, chatRefMap);

                                                                       textView.setText("");
                                                                       mRootReference.updateChildren(messageUserMap);

                                                                       Map<String, Object> chatUnderId = new HashMap<>();
                                                                       chatUnderId.put("msgId", push_id);
                                                                       chatUnderId.put("seen", false);
                                                                       chatUnderId.put("visible", true);
                                                                       chatUnderId.put("timestamp",
                                                                               ServerValue.TIMESTAMP);

                                                                       usersInChat.updateChildren(chatUnderId,
                                                                               (databaseError, databaseReference) -> {

                                                                                   HashMap<String, Object> notificationData
                                                                                           = new HashMap<>();
                                                                                   notificationData.put("from",
                                                                                           mPhoneNumber);
                                                                                   notificationData.put("message",
                                                                                           message);

                                                                                   mNotificationsDatabase.child(mChatPhone)
                                                                                           .push().setValue(notificationData)
                                                                                           .addOnCompleteListener(task1 -> {

                                                                                               if (task1.isSuccessful()) {

                                                                                                   try {
                                                                                                       if (mp1.isPlaying()) {
                                                                                                           mp1.stop();
                                                                                                           mp1.release();

                                                                                                       }
                                                                                                       mp1.start();
                                                                                                   } catch (Exception e) {
                                                                                                       e.printStackTrace();
                                                                                                   }
                                                                                                   //TODO: update message field seen

                                                                                                   Toast.makeText(
                                                                                                           StatsActivity.this,
                                                                                                           "Notification Sent",
                                                                                                           Toast.LENGTH_SHORT).show();

                                                                                               }
                                                                                           });
                                                                                   //mp1.start();
                                                                                   //TODO: add sent mark

                                                                               });
                                                                       // loadMessages();
                                                                       //Chat.setChatListenerCalled(true);
                                                                   }

                                                               }

                                                               @Override
                                                               public void onCancelled(DatabaseError databaseError) {

                                                               }
                                                           });

                                               }
                                           }else{


                                               /**
                                                * Adding the information under ads_users of my phone and the other user,
                                                * if the phone number doesn't match the other user
                                                *
                                                */

                                               mConvoRef = conversation_id;

                                               Map<String, Object> infoToAddUnderMe
                                                       = new HashMap<>();
                                               infoToAddUnderMe.put("id", conversation_id);
                                               infoToAddUnderMe.put("type", "chat");
                                               infoToAddUnderMe.put("visible", true);
                                               infoToAddUnderMe.put("phone_number",
                                                       mChatPhone);
                                               infoToAddUnderMe.put("timestamp",
                                                       ServerValue.TIMESTAMP);

                                               Map<String, Object> mapForCurrentUser
                                                       = new HashMap<>();
                                               mapForCurrentUser.put(myReference +
                                                       conversation_id, infoToAddUnderMe);

                                               mRootReference.updateChildren(mapForCurrentUser);

                                               Map<String, Object> infoToAddUnderOther
                                                       = new HashMap<>();

                                               infoToAddUnderOther.put("id", conversation_id);
                                               infoToAddUnderOther.put("type", "chat");
                                               infoToAddUnderOther.put("visible", true);
                                               infoToAddUnderOther.put("phone_number",
                                                       mPhoneNumber);
                                               infoToAddUnderOther.put("timestamp",
                                                       ServerValue.TIMESTAMP);

                                               Map<String, Object> mapForOtherUser
                                                       = new HashMap<>();
                                               mapForOtherUser.put(otherUserReference +
                                                       conversation_id, infoToAddUnderOther);

                                               mRootReference.updateChildren(mapForOtherUser);

                                               /**
                                                * Adding information into ads_chat &
                                                * ads_messages
                                                */

                                               DatabaseReference usersInChat = mRootReference
                                                       .child("ads_chat").child(conversation_id)
                                                       .child("messages").child(push_id);

//                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
//                                            messageMap.put("parent", Messages.getClickedMessageId());
//                                        }

                                               Map<String, Object> msgContentMap = new HashMap<>();
                                               msgContentMap.put(message_reference +
                                                       push_id, messageMap);

                                               //Adding message
                                               mRootReference.updateChildren(msgContentMap,
                                                       (databaseError, databaseReference) -> {
                                                       });

                                               Map<String, Object> chatRefMap = new HashMap<>();
                                               chatRefMap.put("users", mChatUsers);
                                               chatRefMap.put("seen", false);
                                               chatRefMap.put("visible", true);
                                               chatRefMap.put("lastMessage", push_id);

                                               Map<String, Object> messageUserMap =
                                                       new HashMap<>();

                                               messageUserMap.put(chat_reference +
                                                       conversation_id, chatRefMap);

                                               textView.setText("");
                                               mRootReference.updateChildren(messageUserMap);

                                               mUsersReference.child(mPhoneNumber).child("conversation")
                                                       .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                               mUsersReference.child(mChatPhone).child("conversation")
                                                       .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                               Map<String, Object> chatUnderId = new HashMap<>();
                                               chatUnderId.put("msgId", push_id);
                                               chatUnderId.put("seen", false);
                                               chatUnderId.put("visible", true);
                                               chatUnderId.put("timestamp",
                                                       ServerValue.TIMESTAMP);

                                               usersInChat.updateChildren(chatUnderId,
                                                       (databaseError, databaseReference) -> {

                                                           HashMap<String, Object> notificationData
                                                                   = new HashMap<>();
                                                           notificationData.put("from",
                                                                   mPhoneNumber);
                                                           notificationData.put("message",
                                                                   message);

                                                           mNotificationsDatabase.child(mChatPhone)
                                                                   .push().setValue(notificationData)
                                                                   .addOnCompleteListener(task1 -> {

                                                                       if (task1.isSuccessful()) {

                                                                           try {
                                                                               if (mp1.isPlaying()) {
                                                                                   mp1.stop();
                                                                                   mp1.release();

                                                                               }
                                                                               mp1.start();
                                                                           } catch (Exception e) {
                                                                               e.printStackTrace();
                                                                           }
                                                                           //TODO: update message field seen

                                                                           Toast.makeText(
                                                                                   StatsActivity.this,
                                                                                   "Notification Sent",
                                                                                   Toast.LENGTH_SHORT).show();

                                                                       }
                                                                   });
                                                           //mp1.start();
                                                           //TODO: add sent mark

                                                       });
                                               //oadMessages();
                                               //Chat.setChatListenerCalled(true);
                                           }
                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {

                                       }
                                   });

                       }

               }else{
                   Toast.makeText(StatsActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
               }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mRecorder!= null ){
            try{
                mRecorder.stop();
            }catch(RuntimeException stopException){
                stopException.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }
    }

    private void showCustomDialog() {

        storiesProgressView.pause();
        mUsersList.clear();

        TextView textSeen;
        ImageView delete;// forward;
        RecyclerView seenUpdatesList;

        mSeenDialog.setContentView(R.layout.list_of_seen_layout);
        mSeenDialog.show();
        mSeenDialog.setCancelable(true);
        mSeenDialog.setCanceledOnTouchOutside(true);

        textSeen = mSeenDialog.findViewById(R.id.textSeen);
        delete = mSeenDialog.findViewById(R.id.delete);
     //   forward = mSeenDialog.findViewById(R.id.forward);
        seenUpdatesList = mSeenDialog.findViewById(R.id.seenUpdatesList);

        textSeen.setText(getString(R.string.view__d) + " " + mSeenMap.size());

        seenUpdatesList.setHasFixedSize(true);
        seenUpdatesList.setLayoutManager(new LinearLayoutManager(this));
        seenUpdatesList.setAdapter(tryAdapter);

        for(String s : mSeenMap.keySet()){
            mRootReference.child("ads_users").child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if(u == null){
                        return;
                    }
                    String nameStored = Users.getLocalContactList().get(s);
                    nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                            s;
                    if(s.equals(mPhoneNumber)){
                        u.setNameStoredInPhone(getString(R.string.me_));
                    }else{
                        u.setNameStoredInPhone(nameStored);
                    }
                    u.setThumbnail(u.getThumbnail());
                    u.setTimestamp(castLongObject(mSeenMap.get(s)));

                    mUsersList.add(u);
                    tryAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        delete.setOnClickListener(view -> {
            new TTFancyGifDialog.Builder(this)
                    .setGifResource(R.drawable.gif30)
                    .setTitle(getString(R.string.sta_del_))
                    .setMessage(getString(R.string.st_err_msg))
                    .OnPositiveClicked(()->{
                        mStatusReference.child(mStatusPhone).child("s").child(mStatusId).removeValue().addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(StatsActivity.this, "Done", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    })
                    .OnNegativeClicked(() ->{
                        // Do nothing
                    })
                    .build();


        });

    }

    @Override
    public void onNext() {
        if(counter > resources.size()){
            return;
        }else{
            Picasso.get().load(resources.get(++counter).getContent()).placeholder(R.drawable.ic_avatar)
                .into(image);
            if(resources.get(counter).getFrom().equals("text")){
                if(videoAdded.isPlaying()){
                    videoAdded.stopPlayback();
                }
                l.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.startStories(counter);
            }else if(resources.get(counter).getFrom().equals("image")){
                if(videoAdded.isPlaying()){
                    videoAdded.stopPlayback();
                }
                l.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
                image.setAdjustViewBounds(true);
                image.setBackgroundColor(getResources().getColor(R.color.black));
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.startStories(counter);
            }else{
                image.setVisibility(View.GONE);
                l.setVisibility(View.VISIBLE);
                Uri videoUri = Uri.parse(resources.get(counter).getContent());
                videoAdded.setVideoURI(videoUri);
                videoAdded.setOnPreparedListener(mp -> {
                  //  mp.setLooping(true);
                    storiesProgressView.setStoryDuration(videoAdded.getDuration());
                    storiesProgressView.startStories(counter);
                    videoAdded.start();
                });
            }
            
            String time = getDate(resources.get(counter).getTimestamp());
            mTimestamp.setText(time);
            updateSeen(resources.get(counter));
            mSeenMap = resources.get(counter).getSeenBy();
            mStatusId = resources.get(counter).getId();
            mStatusType = resources.get(counter).getFrom();
            mNumberOfSeen.setText(String.valueOf(resources.get(counter).getSeenBy().size()));
        }
//        Picasso.get().load(resources.get(++counter).getContent()).placeholder(R.drawable.ic_loading)
//                .into(image);

      //  image.setImageResource(resources[++counter]);
    }

    @Override
    public void onPrev() {
        if ((counter - 1) < 0) return;
        //image.setImageResource(resources[--counter]);
        Picasso.get().load(resources.get(--counter).getContent()).placeholder(R.drawable.ic_avatar)
                .into(image);

//        if(resources.get(counter).getFrom().equals("text")){
//            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        }else{
//            image.setAdjustViewBounds(true);
//            image.setBackgroundColor(getResources().getColor(R.color.black));
//        }

        if(resources.get(counter).getFrom().equals("text")){
            if(videoAdded.isPlaying()){
                videoAdded.stopPlayback();
            }
            l.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            storiesProgressView.setStoryDuration(5000L);
            storiesProgressView.startStories(counter);
        }else if(resources.get(counter).getFrom().equals("image")){
            if(videoAdded.isPlaying()){
                videoAdded.stopPlayback();
            }
            l.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);
            image.setAdjustViewBounds(true);
            image.setBackgroundColor(getResources().getColor(R.color.black));
            storiesProgressView.setStoryDuration(5000L);
            storiesProgressView.startStories(counter);
        }else{
            image.setVisibility(View.GONE);
            l.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(resources.get(counter).getContent());
            videoAdded.setVideoURI(videoUri);
            videoAdded.setOnPreparedListener(mp -> {
             //   mp.setLooping(true);
                storiesProgressView.setStoryDuration(videoAdded.getDuration());
                storiesProgressView.startStories(counter);
                videoAdded.start();
            });
        }

        String time = getDate(resources.get(counter).getTimestamp());
        mTimestamp.setText(time);
        mSeenMap = resources.get(counter).getSeenBy();
        mStatusId = resources.get(counter).getId();
        mStatusType = resources.get(counter).getFrom();
        mNumberOfSeen.setText(String.valueOf(resources.get(counter).getSeenBy().size()));
       // updateSeen(resources.get(counter--));
    }

    @Override
    public void onComplete() {
       // recreate();
        finish();
    }

    @Override
    protected void onDestroy() {
        // Very important !
        storiesProgressView.destroy();

        if(mRecorder!= null ){
            try{
                mRecorder.stop();
            }catch(RuntimeException stopException){
                stopException.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }

        super.onDestroy();
    }

    private void updateSeen(Status s){

        if (s.getSeenBy().containsKey(mPhoneNumber)) { return; }
        if (s.getId() == null) { return; }

        mRead.put(mPhoneNumber, ServerValue.TIMESTAMP);

        mStatusReference.child(s.getPhoneNumber()).child("s").child(s.getId()).child("seenBy")
                .updateChildren(mRead);

    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(time);
            return DateFormat.format("MMM dd, hh:mm a", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Long castLongObject(Object object) {
        Long result = 0l;
        try {
            if (object instanceof Long)
                result = ((Long) object).longValue();
            else if (object instanceof Integer) {
                result = ((Integer) object).longValue();
            } else if (object instanceof String) {
                result = Long.valueOf((String) object);
            }
            System.out.println(result);
        } catch (Exception e) {
           e.printStackTrace();
            // do something
        }
        return result;
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


    @Override protected void onStop() {
        if (emojiPopup != null) {
            emojiPopup.dismiss();
        }

        if(mRecorder!= null ){
            try{
                mRecorder.stop();
            }catch(RuntimeException stopException){
                stopException.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }

        super.onStop();
    }

    private void setUpEmojiPopup(View rootView, ImageButton emojiButton, EmojiEditText editText) {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(ignore -> Log.d(TAG, "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d(TAG, "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> emojiButton.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d(TAG, "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> emojiButton.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> Log.d(TAG, "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_slide_animation_style)
             //   .setPageTransformer(new RotateUpTransformer())
                .build(editText);
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSamplingRate(16000);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private String randomIdentifier() {
        StringBuilder builder = new StringBuilder();
        while (builder.toString().length() == 0) {
            int length = rand.nextInt(5) + 5;
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if (identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }

    private void stopRecording() {

        try{
            mRecorder.stop();
        }catch(RuntimeException stopException){
            stopException.printStackTrace();
        }
        mRecorder.release();
        mRecorder = null;
    }

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
    }

    private void sendAudio(String statusId) {

        try {
            new CheckInternet_(internet -> {
               if(internet){
                       String myReference = "ads_users/" + mPhoneNumber + "/" + "conversation/";
                       String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                       String chat_reference = "ads_chat/";

                       final String message_reference = "ads_messages/";

                       DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                       String push_id = msg_push.getKey();

                       StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
                       Uri voiceUri = Uri.fromFile(new File(mFileName));

                       DatabaseReference conversation_push = mRootReference.child("ads_users")
                               .child(mPhoneNumber).push();
                       String conversation_id = conversation_push.getKey();

                       filePath.putFile(voiceUri).addOnCompleteListener(task -> {

                           if (task.isSuccessful()) {
                               String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl())
                                       .toString();

                               mRootReference.child("ads_users").child(mPhoneNumber)
                                       .addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               if (dataSnapshot.hasChild("conversation")) {

                                                   final Conversation[] c = new Conversation[1];
                                                   List<Conversation> listConvo = new ArrayList<>();
                                                   final boolean[] isThere = {false};
                                                   //final String[] mConvoRef = new String[1];

                                                   if(!isFirstTime){

                                                       // Getting reference to push_id under ads_user
                                                       DatabaseReference addNewMessage =
                                                               mRootReference.child("ads_chat")
                                                                       .child(mConvoRef)
                                                                       .child("messages").child(push_id);

                                                       mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                               .setValue(push_id);

                                                       Map<String, Object> messageMap = new HashMap<>();
                                                       messageMap.put("content", downloadUrl);
                                                       messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                       messageMap.put("type", "audio");
                                                       messageMap.put("parent", statusId + "/" +mChatPhone);
                                                       messageMap.put("visible", true);
                                                       messageMap.put("from", mPhoneNumber);
                                                       messageMap.put("seen", false);

                                                       Map<String, Object> msgContentMap = new HashMap<>();
                                                       msgContentMap.put(message_reference +
                                                               push_id, messageMap);

                                                       mRootReference.updateChildren(msgContentMap,
                                                               (databaseError, databaseReference) -> {
                                                               });

                                                       mUsersReference.child(mPhoneNumber).child("conversation")
                                                               .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                       mUsersReference.child(mChatPhone).child("conversation")
                                                               .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                       send_text.setText("");

                                                       Map<String, Object> chatUnderId = new HashMap<>();
                                                       chatUnderId.put("msgId", push_id);
                                                       chatUnderId.put("seen", false);
                                                       chatUnderId.put("visible", true);
                                                       chatUnderId.put("timestamp",
                                                               ServerValue.TIMESTAMP);

                                                       addNewMessage.updateChildren(chatUnderId,
                                                               (databaseError, databaseReference) -> {

                                                                   HashMap<String, Object> notificationData
                                                                           = new HashMap<>();
                                                                   notificationData.put("from",
                                                                           mPhoneNumber);
                                                                   notificationData.put("message",
                                                                           downloadUrl);

                                                                   mNotificationsDatabase.child(mChatPhone)
                                                                           .push().setValue(notificationData)
                                                                           .addOnCompleteListener(task1 -> {

                                                                               if (task1.isSuccessful()) {

                                                                                   try {
                                                                                       if (mp1.isPlaying()) {
                                                                                           mp1.stop();
                                                                                           mp1.release();

                                                                                       }
                                                                                       mp1.start();
                                                                                   } catch (Exception e) {
                                                                                       e.printStackTrace();
                                                                                   }
                                                                                   //TODO: update message field seen

                                                                                   Toast.makeText(
                                                                                           StatsActivity.this,
                                                                                           "Notification Sent",
                                                                                           Toast.LENGTH_SHORT).show();

                                                                               }
                                                                           });
                                                                   //mp1.start();
                                                                   //TODO: add sent mark

                                                               });

                                                   }else{
                                                       mRootReference.child("ads_users")
                                                               .child(mPhoneNumber)
                                                               .child("conversation")
                                                               .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                                       // Retrieving all the conversations underneath my phone number
                                                                       // and check if there is one phone number that matches the other user's phone number
                                                                       for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                           c[0] = d.getValue(Conversation.class);
                                                                           listConvo.add(c[0]);
                                                                       }
                                                                       for (int i = 0; i < listConvo.size(); i++) {
                                                                           if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                                               isThere[0] = true;
                                                                               mConvoRef = listConvo.get(i).getId();
                                                                           }
                                                                       }
                                                                       isFirstTime = false;
                                                                       if (isThere[0]) {

                                                                           // Getting reference to push_id under ads_user
                                                                           DatabaseReference addNewMessage =
                                                                                   mRootReference.child("ads_chat")
                                                                                           .child(mConvoRef)
                                                                                           .child("messages").child(push_id);

                                                                           mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                                                   .setValue(push_id);

                                                                           Map<String, Object> messageMap = new HashMap<>();
                                                                           messageMap.put("content", downloadUrl);
                                                                           messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                           messageMap.put("type", "audio");
                                                                           messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                           messageMap.put("visible", true);
                                                                           messageMap.put("from", mPhoneNumber);
                                                                           messageMap.put("seen", false);

                                                                           Map<String, Object> msgContentMap = new HashMap<>();
                                                                           msgContentMap.put(message_reference +
                                                                                   push_id, messageMap);

                                                                           mRootReference.updateChildren(msgContentMap,
                                                                                   (databaseError, databaseReference) -> {
                                                                                   });


                                                                           mUsersReference.child(mPhoneNumber).child("conversation")
                                                                                   .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                           mUsersReference.child(mChatPhone).child("conversation")
                                                                                   .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                           send_text.setText("");

                                                                           Map<String, Object> chatUnderId = new HashMap<>();
                                                                           chatUnderId.put("msgId", push_id);
                                                                           chatUnderId.put("seen", false);
                                                                           chatUnderId.put("visible", true);
                                                                           chatUnderId.put("timestamp",
                                                                                   ServerValue.TIMESTAMP);

                                                                           addNewMessage.updateChildren(chatUnderId,
                                                                                   (databaseError, databaseReference) -> {

                                                                                       HashMap<String, Object> notificationData
                                                                                               = new HashMap<>();
                                                                                       notificationData.put("from",
                                                                                               mPhoneNumber);
                                                                                       notificationData.put("message",
                                                                                               downloadUrl);

                                                                                       mNotificationsDatabase.child(mChatPhone)
                                                                                               .push().setValue(notificationData)
                                                                                               .addOnCompleteListener(task1 -> {

                                                                                                   if (task1.isSuccessful()) {

                                                                                                       try {
                                                                                                           if (mp1.isPlaying()) {
                                                                                                               mp1.stop();
                                                                                                               mp1.release();

                                                                                                           }
                                                                                                           mp1.start();
                                                                                                       } catch (Exception e) {
                                                                                                           e.printStackTrace();
                                                                                                       }
                                                                                                       //TODO: update message field seen

                                                                                                       Toast.makeText(
                                                                                                               StatsActivity.this,
                                                                                                               "Notification Sent",
                                                                                                               Toast.LENGTH_SHORT).show();

                                                                                                   }
                                                                                               });
                                                                                       //mp1.start();
                                                                                       //TODO: add sent mark

                                                                                   });

                                                                       } else {

                                                                           /**
                                                                            * Adding the information under ads_users
                                                                            */

                                                                           mConvoRef = conversation_id;
                                                                           Map<String, Object> infoToAddUnderMe
                                                                                   = new HashMap<>();
                                                                           infoToAddUnderMe.put("id", conversation_id);
                                                                           infoToAddUnderMe.put("type", "chat");
                                                                           infoToAddUnderMe.put("visible", true);
                                                                           infoToAddUnderMe.put("phone_number",
                                                                                   mChatPhone);
                                                                           infoToAddUnderMe.put("timestamp",
                                                                                   ServerValue.TIMESTAMP);

                                                                           Map<String, Object> mapForCurrentUser
                                                                                   = new HashMap<>();
                                                                           mapForCurrentUser.put(myReference +
                                                                                   conversation_id, infoToAddUnderMe);

                                                                           mRootReference.updateChildren(mapForCurrentUser);

                                                                           Map<String, Object> infoToAddUnderOther
                                                                                   = new HashMap<>();

                                                                           infoToAddUnderOther.put("id", conversation_id);
                                                                           infoToAddUnderOther.put("type", "chat");
                                                                           infoToAddUnderOther.put("visible", true);
                                                                           infoToAddUnderOther.put("phone_number",
                                                                                   mPhoneNumber);
                                                                           infoToAddUnderOther.put("timestamp",
                                                                                   ServerValue.TIMESTAMP);

                                                                           Map<String, Object> mapForOtherUser
                                                                                   = new HashMap<>();
                                                                           mapForOtherUser.put(otherUserReference +
                                                                                   conversation_id, infoToAddUnderOther);

                                                                           mRootReference.updateChildren(mapForOtherUser);

                                                                           /**
                                                                            * Adding information into ads_chat &
                                                                            * ads_messages
                                                                            */

                                                                           DatabaseReference usersInChat = mRootReference
                                                                                   .child("ads_chat").child(conversation_id)
                                                                                   .child("messages").child(push_id);

                                                                           Map<String, Object> messageMap = new HashMap<>();
                                                                           messageMap.put("content", downloadUrl);
                                                                           messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                           messageMap.put("type", "audio");
                                                                           messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                           messageMap.put("visible", true);
                                                                           messageMap.put("from", mPhoneNumber);
                                                                           messageMap.put("seen", false);

                                                                           Map<String, Object> msgContentMap = new HashMap<>();
                                                                           msgContentMap.put(message_reference +
                                                                                   push_id, messageMap);

                                                                           //Adding message
                                                                           mRootReference.updateChildren(msgContentMap,
                                                                                   (databaseError, databaseReference) -> {
                                                                                   });

                                                                           Map<String, Object> chatRefMap = new HashMap<>();
                                                                           chatRefMap.put("users", mChatUsers);
                                                                           chatRefMap.put("seen", false);
                                                                           chatRefMap.put("visible", true);
                                                                           chatRefMap.put("lastMessage", push_id);

                                                                           Map<String, Object> messageUserMap =
                                                                                   new HashMap<>();

                                                                           messageUserMap.put(chat_reference +
                                                                                   conversation_id, chatRefMap);

                                                                           mUsersReference.child(mPhoneNumber).child("conversation")
                                                                                   .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                           mUsersReference.child(mChatPhone).child("conversation")
                                                                                   .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                           send_text.setText("");
                                                                           mRootReference.updateChildren(messageUserMap);

                                                                           Map<String, Object> chatUnderId = new HashMap<>();
                                                                           chatUnderId.put("msgId", push_id);
                                                                           chatUnderId.put("seen", false);
                                                                           chatUnderId.put("visible", true);
                                                                           chatUnderId.put("timestamp",
                                                                                   ServerValue.TIMESTAMP);

                                                                           usersInChat.updateChildren(chatUnderId,
                                                                                   (databaseError, databaseReference) -> {

                                                                                       HashMap<String, Object> notificationData
                                                                                               = new HashMap<>();
                                                                                       notificationData.put("from",
                                                                                               mPhoneNumber);
                                                                                       notificationData.put("message",
                                                                                               downloadUrl);

                                                                                       mNotificationsDatabase.child(mChatPhone)
                                                                                               .push().setValue(notificationData)
                                                                                               .addOnCompleteListener(task1 -> {

                                                                                                   if (task1.isSuccessful()) {

                                                                                                       try {
                                                                                                           if (mp1.isPlaying()) {
                                                                                                               mp1.stop();
                                                                                                               mp1.release();

                                                                                                           }
                                                                                                           mp1.start();
                                                                                                       } catch (Exception e) {
                                                                                                           e.printStackTrace();
                                                                                                       }
                                                                                                       //TODO: update message field seen

                                                                                                       Toast.makeText(
                                                                                                               StatsActivity.this,
                                                                                                               "Notification Sent",
                                                                                                               Toast.LENGTH_SHORT).show();

                                                                                                   }
                                                                                               });
                                                                                       //mp1.start();
                                                                                       //TODO: add sent mark

                                                                                   });
                                                                       }

                                                                   }

                                                                   @Override
                                                                   public void onCancelled(DatabaseError databaseError) {

                                                                   }
                                                               });

                                                   }
                                               }else {

                                                   /**
                                                    * Adding the information under ads_users
                                                    */
                                                   mConvoRef = conversation_id;

                                                   Map<String, Object> infoToAddUnderMe
                                                           = new HashMap<>();
                                                   infoToAddUnderMe.put("id", conversation_id);
                                                   infoToAddUnderMe.put("type", "chat");
                                                   infoToAddUnderMe.put("visible", true);
                                                   infoToAddUnderMe.put("phone_number",
                                                           mChatPhone);
                                                   infoToAddUnderMe.put("timestamp",
                                                           ServerValue.TIMESTAMP);

                                                   Map<String, Object> mapForCurrentUser
                                                           = new HashMap<>();
                                                   mapForCurrentUser.put(myReference +
                                                           conversation_id, infoToAddUnderMe);

                                                   mRootReference.updateChildren(mapForCurrentUser);

                                                   Map<String, Object> infoToAddUnderOther
                                                           = new HashMap<>();

                                                   infoToAddUnderOther.put("id", conversation_id);
                                                   infoToAddUnderOther.put("type", "chat");
                                                   infoToAddUnderOther.put("visible", true);
                                                   infoToAddUnderOther.put("phone_number",
                                                           mPhoneNumber);
                                                   infoToAddUnderOther.put("timestamp",
                                                           ServerValue.TIMESTAMP);

                                                   Map<String, Object> mapForOtherUser
                                                           = new HashMap<>();
                                                   mapForOtherUser.put(otherUserReference +
                                                           conversation_id, infoToAddUnderOther);

                                                   mRootReference.updateChildren(mapForOtherUser);

                                                   /**
                                                    * Adding information into ads_chat &
                                                    * ads_messages
                                                    */

                                                   DatabaseReference usersInChat = mRootReference
                                                           .child("ads_chat").child(conversation_id)
                                                           .child("messages").child(push_id);

                                                   Map<String, Object> messageMap = new HashMap<>();
                                                   messageMap.put("content", downloadUrl);
                                                   messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                   messageMap.put("type", "audio");
                                                   messageMap.put("parent", statusId + "/" +mChatPhone);
                                                   messageMap.put("visible", true);
                                                   messageMap.put("from", mPhoneNumber);
                                                   messageMap.put("seen", false);

                                                   Map<String, Object> msgContentMap = new HashMap<>();
                                                   msgContentMap.put(message_reference +
                                                           push_id, messageMap);

                                                   //Adding message
                                                   mRootReference.updateChildren(msgContentMap,
                                                           (databaseError, databaseReference) -> {
                                                           });

                                                   Map<String, Object> chatRefMap = new HashMap<>();
                                                   chatRefMap.put("users", mChatUsers);
                                                   chatRefMap.put("seen", false);
                                                   chatRefMap.put("visible", true);
                                                   chatRefMap.put("lastMessage", push_id);

                                                   mUsersReference.child(mPhoneNumber).child("conversation")
                                                           .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                   mUsersReference.child(mChatPhone).child("conversation")
                                                           .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                   Map<String, Object> messageUserMap =
                                                           new HashMap<>();

                                                   messageUserMap.put(chat_reference +
                                                           conversation_id, chatRefMap);

                                                   send_text.setText("");
                                                   mRootReference.updateChildren(messageUserMap);

                                                   Map<String, Object> chatUnderId = new HashMap<>();
                                                   chatUnderId.put("msgId", push_id);
                                                   chatUnderId.put("seen", false);
                                                   chatUnderId.put("visible", true);
                                                   chatUnderId.put("timestamp",
                                                           ServerValue.TIMESTAMP);

                                                   usersInChat.updateChildren(chatUnderId,
                                                           (databaseError, databaseReference) -> {

                                                               HashMap<String, Object> notificationData
                                                                       = new HashMap<>();
                                                               notificationData.put("from",
                                                                       mPhoneNumber);
                                                               notificationData.put("message",
                                                                       downloadUrl);

                                                               mNotificationsDatabase.child(mChatPhone)
                                                                       .push().setValue(notificationData)
                                                                       .addOnCompleteListener(task1 -> {

                                                                           if (task1.isSuccessful()) {

                                                                               try {
                                                                                   if (mp1.isPlaying()) {
                                                                                       mp1.stop();
                                                                                       mp1.release();

                                                                                   }
                                                                                   mp1.start();
                                                                               } catch (Exception e) {
                                                                                   e.printStackTrace();
                                                                               }
                                                                               //TODO: update message field seen

                                                                               Toast.makeText(
                                                                                       StatsActivity.this,
                                                                                       "Notification Sent",
                                                                                       Toast.LENGTH_SHORT).show();

                                                                           }
                                                                       });
                                                               //mp1.start();
                                                               //TODO: add sent mark

                                                           });
                                               }
                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       });
                           }

                       });
               }else{
                   Toast.makeText(StatsActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
               }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void showCustomAttachmentDialog() {

        TextView sendImage;
        TextView sendAudio;
        TextView sendDocument;
        TextView sendVideo;

        mAttachmentDialog.setContentView(R.layout.custom_dialog);
        mAttachmentDialog.show();
        mAttachmentDialog.setCanceledOnTouchOutside(true);

        sendImage = mAttachmentDialog.findViewById(R.id.custom_layout_image_send);
        sendAudio = mAttachmentDialog.findViewById(R.id.custom_layout_audio_send);
        sendDocument = mAttachmentDialog.findViewById(R.id.custom_layout_document_send);
        sendVideo = mAttachmentDialog.findViewById(R.id.custom_layout_video_send);

        sendImage.setOnClickListener(view -> {

            try {
                new CheckInternet_(internet -> {
                   if(internet){
                       mAttachmentDialog.dismiss();
                       try {
                           pickImage();
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   }else{
                       Toast.makeText(StatsActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                   }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        sendAudio.setOnClickListener(view -> {
            try {
                new CheckInternet_(internet -> {
                   if(internet){
                       Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                       startActivityForResult(i, GALLERY_PICK);
                       mAttachmentDialog.dismiss();
                   }else{
                       Toast.makeText(this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                   }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        sendVideo.setOnClickListener(view -> {

            try {
                new CheckInternet_(internet -> {
                   if(internet){
                       mAttachmentDialog.dismiss();
                       try {
                           pickVideo();
                       } catch (Exception e) {
                           e.printStackTrace();
                       }
                   }else{
                       Toast.makeText(StatsActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                   }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        sendDocument.setOnClickListener(view -> {

            try {
                new CheckInternet_(internet -> {
                   if(internet){
                       onPickDoc();
                       mAttachmentDialog.dismiss();
                   }else{
                       Toast.makeText(StatsActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                   }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    public void onPickDoc() {
        String[] zips = {".zip", ".rar"};
        String[] pdfs = {".pdf"};
        String[] docs = {".doc", ".docx"};
        String[] excel = {".xlsx", "xlsm"};
        String[] powerpoint = {".pptx", ".ppt"};
        if ((mDocPath.size() == MAX_ATTACHMENT_COUNT)) {
            Toast.makeText(this, "Cannot select more than " + MAX_ATTACHMENT_COUNT + " items",
                    Toast.LENGTH_SHORT).show();
        } else {
            FilePickerBuilder.getInstance()
                    .setMaxCount(MAX_ATTACHMENT_COUNT)
                    .setSelectedFiles(mDocPath)
                    .setActivityTheme(R.style.FilePickerTheme)
                    .setActivityTitle("Please select doc")
                    .addFileSupport("ZIP", zips)
                    .addFileSupport("PDF", pdfs, R.drawable.pdf_blue)
                    .addFileSupport("DOC", docs)
                    .addFileSupport("EXL", excel)
                    .addFileSupport("PPT", powerpoint)
                    .enableDocSupport(false)
                    .enableSelectAll(true)
                    .sortDocumentsBy(SortingTypes.name)
//                    .withOrientation(Orientation.UNSPECIFIED)
                    .pickFile(this);
        }
    }

    private void pickVideo() {
        new VideoPicker.Builder(StatsActivity.this)
                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                .directory(VideoPicker.Directory.DEFAULT)
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build();
    }

    private void pickImage() {
        new ImagePicker.Builder(StatsActivity.this)
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .allowMultipleImages(true)
                // .compressLevel(ImagePicker.ComperesLevel.)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .allowOnlineImages(false)
                .scale(600, 600)
                .allowMultipleImages(true)
                .enableDebuggingMode(true)
                .build();
    }

    private void askPermission() {
        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
            showCustomReplyDialog();
        }
        RunTimePermissionWrapper.handleRunTimePermission(StatsActivity.this,
                RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
            showCustomReplyDialog();
        } else {
            showSnack(ActivityCompat.shouldShowRequestPermissionRationale(this, WALK_THROUGH[0]));
        }
    }

    private void showSnack(final boolean isRationale) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Please provide audio Record permission", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(isRationale ? "VIEW" : "Settings", view -> {
            snackbar.dismiss();

            if (isRationale)
                RunTimePermissionWrapper.handleRunTimePermission(StatsActivity.this, RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
            else
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 1001);
        });

        snackbar.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String statusId = resources.get(counter).getId();

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            mImagesPath = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            loadImages();

            for (int i = 0; i < mImagesData.size(); i++) {
                String myReference = "ads_users/" + mPhoneNumber + "/" + "conversation/";
                String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                String chat_reference = "ads_chat/";

                final String message_reference = "ads_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                String push_id = msg_push.getKey();

                DatabaseReference conversation_push = mRootReference.child("ads_users")
                        .child(mPhoneNumber).push();
                String conversation_id = conversation_push.getKey();

                StorageReference filePath = mImagesStorage.child("ads_messages_images")
                        .child(push_id + ".jpg");
                filePath.putFile(mImagesData.get(i)).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String downloadUrl = Objects.requireNonNull(task.getResult()
                                .getDownloadUrl()).toString();

                        mRootReference.child("ads_users").child(mPhoneNumber)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("conversation")) {

                                            final Conversation[] c = new Conversation[1];
                                            List<Conversation> listConvo = new ArrayList<>();
                                            final boolean[] isThere = {false};
                                            //     final String[] mConvoRef = new String[1];

                                            if(!isFirstTime){

                                                DatabaseReference addNewMessage =
                                                        mRootReference.child("ads_chat")
                                                                .child(mConvoRef)
                                                                .child("messages").child(push_id);

                                                mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                        .setValue(push_id);

                                                Map<String, Object> messageMap = new HashMap<>();
                                                messageMap.put("content", downloadUrl);
                                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                messageMap.put("type", "image");
                                                messageMap.put("parent", statusId + "/" +mChatPhone);
                                                messageMap.put("visible", true);
                                                messageMap.put("from", mPhoneNumber);
                                                messageMap.put("seen", false);

                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                msgContentMap.put(message_reference +
                                                        push_id, messageMap);

                                                mRootReference.updateChildren(msgContentMap,
                                                        (databaseError, databaseReference) -> {
                                                        });


                                                mUsersReference.child(mPhoneNumber).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                mUsersReference.child(mChatPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                send_text.setText("");

                                                Map<String, Object> chatUnderId = new HashMap<>();
                                                chatUnderId.put("msgId", push_id);
                                                chatUnderId.put("seen", false);
                                                chatUnderId.put("visible", true);
                                                chatUnderId.put("timestamp",
                                                        ServerValue.TIMESTAMP);

                                                addNewMessage.updateChildren(chatUnderId,
                                                        (databaseError, databaseReference) -> {

                                                            HashMap<String, Object> notificationData
                                                                    = new HashMap<>();
                                                            notificationData.put("from",
                                                                    mPhoneNumber);
                                                            notificationData.put("message",
                                                                    downloadUrl);

                                                            mNotificationsDatabase.child(mChatPhone)
                                                                    .push().setValue(notificationData)
                                                                    .addOnCompleteListener(task1 -> {

                                                                        if (task1.isSuccessful()) {

                                                                            try {
                                                                                if (mp1.isPlaying()) {
                                                                                    mp1.stop();
                                                                                    mp1.release();

                                                                                }
                                                                                mp1.start();
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            //TODO: update message field seen

                                                                            Toast.makeText(
                                                                                    StatsActivity.this,
                                                                                    "Notification Sent",
                                                                                    Toast.LENGTH_SHORT).show();

                                                                        }
                                                                    });
                                                            //mp1.start();
                                                            //TODO: add sent mark
                                                        });

                                            }else {
                                                mRootReference.child("ads_users")
                                                        .child(mPhoneNumber)
                                                        .child("conversation")
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                    c[0] = d.getValue(Conversation.class);
                                                                    listConvo.add(c[0]);
                                                                }
                                                                for (int i = 0; i < listConvo.size(); i++) {
                                                                    if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                                        isThere[0] = true;
                                                                        mConvoRef = listConvo.get(i).getId();
                                                                    }
                                                                }
                                                                isFirstTime = false;
                                                                if (isThere[0]) {
                                                                    DatabaseReference addNewMessage =
                                                                            mRootReference.child("ads_chat")
                                                                                    .child(mConvoRef)
                                                                                    .child("messages").child(push_id);

                                                                    mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                                            .setValue(push_id);

                                                                    mUsersReference.child(mPhoneNumber).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    Map<String, Object> messageMap = new HashMap<>();
                                                                    messageMap.put("content", downloadUrl);
                                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                    messageMap.put("type", "image");
                                                                    messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mPhoneNumber);
                                                                    messageMap.put("seen", false);

                                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                                    msgContentMap.put(message_reference +
                                                                            push_id, messageMap);

                                                                    mRootReference.updateChildren(msgContentMap,
                                                                            (databaseError, databaseReference) -> {
                                                                            });

                                                                    send_text.setText("");

                                                                    Map<String, Object> chatUnderId = new HashMap<>();
                                                                    chatUnderId.put("msgId", push_id);
                                                                    chatUnderId.put("seen", false);
                                                                    chatUnderId.put("visible", true);
                                                                    chatUnderId.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    addNewMessage.updateChildren(chatUnderId,
                                                                            (databaseError, databaseReference) -> {

                                                                                HashMap<String, Object> notificationData
                                                                                        = new HashMap<>();
                                                                                notificationData.put("from",
                                                                                        mPhoneNumber);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                mNotificationsDatabase.child(mChatPhone)
                                                                                        .push().setValue(notificationData)
                                                                                        .addOnCompleteListener(task1 -> {

                                                                                            if (task1.isSuccessful()) {

                                                                                                try {
                                                                                                    if (mp1.isPlaying()) {
                                                                                                        mp1.stop();
                                                                                                        mp1.release();

                                                                                                    }
                                                                                                    mp1.start();
                                                                                                } catch (Exception e) {
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                                //TODO: update message field seen

                                                                                                Toast.makeText(
                                                                                                        StatsActivity.this,
                                                                                                        "Notification Sent",
                                                                                                        Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        });
                                                                                //mp1.start();
                                                                                //TODO: add sent mark
                                                                            });

                                                                } else {

                                                                    /**
                                                                     * Adding the information under ads_users
                                                                     */

                                                                    mConvoRef = conversation_id;

                                                                    Map<String, Object> infoToAddUnderMe
                                                                            = new HashMap<>();
                                                                    infoToAddUnderMe.put("id", conversation_id);
                                                                    infoToAddUnderMe.put("type", "chat");
                                                                    infoToAddUnderMe.put("visible", true);
                                                                    infoToAddUnderMe.put("phone_number",
                                                                            mChatPhone);
                                                                    infoToAddUnderMe.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    Map<String, Object> mapForCurrentUser
                                                                            = new HashMap<>();
                                                                    mapForCurrentUser.put(myReference +
                                                                            conversation_id, infoToAddUnderMe);

                                                                    mRootReference.updateChildren(mapForCurrentUser);

                                                                    Map<String, Object> infoToAddUnderOther
                                                                            = new HashMap<>();
                                                                    infoToAddUnderOther.put("id", conversation_id);
                                                                    infoToAddUnderOther.put("type", "chat");
                                                                    infoToAddUnderOther.put("visible", true);
                                                                    infoToAddUnderOther.put("phone_number",
                                                                            mPhoneNumber);
                                                                    infoToAddUnderOther.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    Map<String, Object> mapForOtherUser
                                                                            = new HashMap<>();
                                                                    mapForOtherUser.put(otherUserReference +
                                                                            conversation_id, infoToAddUnderOther);

                                                                    mRootReference.updateChildren(mapForOtherUser);

                                                                    /**
                                                                     * Adding information into ads_chat &
                                                                     * ads_messages
                                                                     */

                                                                    mUsersReference.child(mPhoneNumber).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                    DatabaseReference usersInChat = mRootReference
                                                                            .child("ads_chat").child(conversation_id)
                                                                            .child("messages").child(push_id);

                                                                    Map<String, Object> messageMap = new HashMap<>();
                                                                    messageMap.put("content", downloadUrl);
                                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                    messageMap.put("type", "image");
                                                                    messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mPhoneNumber);
                                                                    messageMap.put("seen", false);

                                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                                    msgContentMap.put(message_reference +
                                                                            push_id, messageMap);

                                                                    //Adding message
                                                                    mRootReference.updateChildren(msgContentMap,
                                                                            (databaseError, databaseReference) -> {
                                                                            });

                                                                    Map<String, Object> chatRefMap = new HashMap<>();
                                                                    chatRefMap.put("users", mChatUsers);
                                                                    chatRefMap.put("seen", false);
                                                                    chatRefMap.put("visible", true);
                                                                    chatRefMap.put("lastMessage", push_id);

                                                                    Map<String, Object> messageUserMap =
                                                                            new HashMap<>();

                                                                    messageUserMap.put(chat_reference +
                                                                            conversation_id, chatRefMap);

                                                                    send_text.setText("");
                                                                    mRootReference.updateChildren(messageUserMap);

                                                                    Map<String, Object> chatUnderId = new HashMap<>();
                                                                    chatUnderId.put("msgId", push_id);
                                                                    chatUnderId.put("seen", false);
                                                                    chatUnderId.put("visible", true);
                                                                    chatUnderId.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    usersInChat.updateChildren(chatUnderId,
                                                                            (databaseError, databaseReference) -> {

                                                                                HashMap<String, Object> notificationData
                                                                                        = new HashMap<>();
                                                                                notificationData.put("from",
                                                                                        mPhoneNumber);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                mNotificationsDatabase.child(mChatPhone)
                                                                                        .push().setValue(notificationData)
                                                                                        .addOnCompleteListener(task1 -> {

                                                                                            if (task1.isSuccessful()) {

                                                                                                try {
                                                                                                    if (mp1.isPlaying()) {
                                                                                                        mp1.stop();
                                                                                                        mp1.release();

                                                                                                    }
                                                                                                    mp1.start();
                                                                                                } catch (Exception e) {
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                                //TODO: update message field seen

                                                                                                Toast.makeText(
                                                                                                        StatsActivity.this,
                                                                                                        "Notification Sent",
                                                                                                        Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        });
                                                                                //mp1.start();
                                                                                //TODO: add sent mark

                                                                            });

                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }

                                        } else {

                                            /**
                                             * Adding the information under ads_users
                                             */

                                            mConvoRef = conversation_id;

                                            Map<String, Object> infoToAddUnderMe
                                                    = new HashMap<>();
                                            infoToAddUnderMe.put("id", conversation_id);
                                            infoToAddUnderMe.put("type", "chat");
                                            infoToAddUnderMe.put("visible", true);
                                            infoToAddUnderMe.put("phone_number",
                                                    mChatPhone);
                                            infoToAddUnderMe.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            Map<String, Object> mapForCurrentUser
                                                    = new HashMap<>();
                                            mapForCurrentUser.put(myReference +
                                                    conversation_id, infoToAddUnderMe);

                                            mRootReference.updateChildren(mapForCurrentUser);

                                            Map<String, Object> infoToAddUnderOther
                                                    = new HashMap<>();

                                            infoToAddUnderOther.put("id", conversation_id);
                                            infoToAddUnderOther.put("type", "chat");
                                            infoToAddUnderOther.put("visible", true);
                                            infoToAddUnderOther.put("phone_number",
                                                    mPhoneNumber);
                                            infoToAddUnderOther.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            Map<String, Object> mapForOtherUser
                                                    = new HashMap<>();
                                            mapForOtherUser.put(otherUserReference +
                                                    conversation_id, infoToAddUnderOther);

                                            mRootReference.updateChildren(mapForOtherUser);

                                            /**
                                             * Adding information into ads_chat &
                                             * ads_messages
                                             */

                                            mUsersReference.child(mPhoneNumber).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                            mUsersReference.child(mChatPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                            DatabaseReference usersInChat = mRootReference
                                                    .child("ads_chat").child(conversation_id)
                                                    .child("messages").child(push_id);

                                            Map<String, Object> messageMap = new HashMap<>();
                                            messageMap.put("content", downloadUrl);
                                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                            messageMap.put("type", "image");
                                            messageMap.put("parent", statusId + "/" +mChatPhone);
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mPhoneNumber);
                                            messageMap.put("seen", false);

                                            Map<String, Object> msgContentMap = new HashMap<>();
                                            msgContentMap.put(message_reference +
                                                    push_id, messageMap);

                                            //Adding message
                                            mRootReference.updateChildren(msgContentMap,
                                                    (databaseError, databaseReference) -> {
                                                    });

                                            Map<String, Object> chatRefMap = new HashMap<>();
                                            chatRefMap.put("users", mChatUsers);
                                            chatRefMap.put("seen", false);
                                            chatRefMap.put("visible", true);
                                            chatRefMap.put("lastMessage", push_id);

                                            Map<String, Object> messageUserMap =
                                                    new HashMap<>();

                                            messageUserMap.put(chat_reference +
                                                    conversation_id, chatRefMap);

                                            send_text.setText("");
                                            mRootReference.updateChildren(messageUserMap);

                                            Map<String, Object> chatUnderId = new HashMap<>();
                                            chatUnderId.put("msgId", push_id);
                                            chatUnderId.put("seen", false);
                                            chatUnderId.put("visible", true);
                                            chatUnderId.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            usersInChat.updateChildren(chatUnderId,
                                                    (databaseError, databaseReference) -> {

                                                        HashMap<String, Object> notificationData
                                                                = new HashMap<>();
                                                        notificationData.put("from",
                                                                mPhoneNumber);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        mNotificationsDatabase.child(mChatPhone)
                                                                .push().setValue(notificationData)
                                                                .addOnCompleteListener(task1 -> {

                                                                    if (task1.isSuccessful()) {

                                                                        try {
                                                                            if (mp1.isPlaying()) {
                                                                                mp1.stop();
                                                                                mp1.release();

                                                                            }
                                                                            mp1.start();
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        //TODO: update message field seen

                                                                        Toast.makeText(
                                                                                StatsActivity.this,
                                                                                "Notification Sent",
                                                                                Toast.LENGTH_SHORT).show();

                                                                    }
                                                                });
                                                        //mp1.start();
                                                        //TODO: add sent mark

                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                });

            }

        }
        else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            mVideosPath = (List<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
            loadVideo();

//            String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
//            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";
//
//            String chat_reference = "ads_chat/";
//
//            DatabaseReference conversation_push = mRootReference.child("ads_users")
//                    .child(mCurrentUserPho ne).push();
//            String conversation_id = conversation_push.getKey();
//
//            final String message_reference = "ads_messages/";
//
//            DatabaseReference msg_push = mRootReference.child("ads_messages").push();
//
//            String push_id = msg_push.getKey();
//
//            StorageReference filePath = mVideosStorage.child("messages_videos").child(push_id + ".mp4");
            File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + getPackageName() + "/media/videos");
            if (f.mkdirs() || f.isDirectory()) {
                //compress and output new video specs
                new VideoCompressAsyncTask(this).execute(mVideosData.get(0).toString(), f.getPath());
            }

        }
        else if (requestCode == FilePickerConst.REQUEST_CODE_DOC && resultCode == Activity.RESULT_OK && data != null) {

            mDocPath = new ArrayList<>();

            mDocPath.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
            //loadDocuments();

            for (int i = 0; i < mDocPath.size(); i++) {

                String myReference = "ads_users/" + mPhoneNumber + "/" + "conversation/";
                String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                String chat_reference = "ads_chat/";

                DatabaseReference conversation_push = mRootReference.child("ads_users")
                        .child(mPhoneNumber).push();
                String conversation_id = conversation_push.getKey();

                final String message_reference = "ads_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                String push_id = msg_push.getKey();

                StorageReference filePath = mDocumentsStorage.child("ads_messages_documents").child(push_id + ".docx");
                filePath.putFile(Uri.fromFile(new File(mDocPath.get(i)))).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                        mRootReference.child("ads_users").child(mPhoneNumber)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("conversation")) {

                                            final Conversation[] c = new Conversation[1];
                                            List<Conversation> listConvo = new ArrayList<>();
                                            final boolean[] isThere = {false};
                                            //final String[] mConvoRef = new String[1];

                                            if(!isFirstTime){

                                                // Getting reference to push_id under ads_user
                                                DatabaseReference addNewMessage =
                                                        mRootReference.child("ads_chat")
                                                                .child(mConvoRef)
                                                                .child("messages").child(push_id);

                                                mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                        .setValue(push_id);

                                                Map<String, Object> messageMap = new HashMap<>();
                                                messageMap.put("content", downloadUrl);
                                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                messageMap.put("type", "document");
                                                messageMap.put("parent", statusId + "/" +mChatPhone);
                                                messageMap.put("visible", true);
                                                messageMap.put("from", mPhoneNumber);
                                                messageMap.put("seen", false);

                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                msgContentMap.put(message_reference +
                                                        push_id, messageMap);

                                                mRootReference.updateChildren(msgContentMap,
                                                        (databaseError, databaseReference) -> {
                                                        });


                                                mUsersReference.child(mPhoneNumber).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                mUsersReference.child(mChatPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                send_text.setText("");

                                                Map<String, Object> chatUnderId = new HashMap<>();
                                                chatUnderId.put("msgId", push_id);
                                                chatUnderId.put("seen", false);
                                                chatUnderId.put("visible", true);
                                                chatUnderId.put("timestamp",
                                                        ServerValue.TIMESTAMP);

                                                addNewMessage.updateChildren(chatUnderId,
                                                        (databaseError, databaseReference) -> {

                                                            HashMap<String, Object> notificationData
                                                                    = new HashMap<>();
                                                            notificationData.put("from",
                                                                    mPhoneNumber);
                                                            notificationData.put("message",
                                                                    downloadUrl);

                                                            mNotificationsDatabase.child(mChatPhone)
                                                                    .push().setValue(notificationData)
                                                                    .addOnCompleteListener(task1 -> {

                                                                        if (task1.isSuccessful()) {

                                                                            try {
                                                                                if (mp1.isPlaying()) {
                                                                                    mp1.stop();
                                                                                    mp1.release();

                                                                                }
                                                                                mp1.start();
                                                                            } catch (Exception e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            //TODO: update message field seen

                                                                            Toast.makeText(
                                                                                    StatsActivity.this,
                                                                                    "Notification Sent",
                                                                                    Toast.LENGTH_SHORT).show();

                                                                        }
                                                                    });
                                                            //mp1.start();
                                                            //TODO: add sent mark

                                                        });

                                            }else {
                                                mRootReference.child("ads_users")
                                                        .child(mPhoneNumber)
                                                        .child("conversation")
                                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                    c[0] = d.getValue(Conversation.class);
                                                                    listConvo.add(c[0]);
                                                                }
                                                                for (int i = 0; i < listConvo.size(); i++) {
                                                                    if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                                        isThere[0] = true;
                                                                        mConvoRef = listConvo.get(i).getId();
                                                                    }
                                                                }
                                                                isFirstTime = false;
                                                                if (isThere[0]) {

                                                                    // Getting reference to push_id under ads_user
                                                                    DatabaseReference addNewMessage =
                                                                            mRootReference.child("ads_chat")
                                                                                    .child(mConvoRef)
                                                                                    .child("messages").child(push_id);

                                                                    mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                                            .setValue(push_id);

                                                                    Map<String, Object> messageMap = new HashMap<>();
                                                                    messageMap.put("content", downloadUrl);
                                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                    messageMap.put("type", "document");
                                                                    messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mPhoneNumber);
                                                                    messageMap.put("seen", false);

                                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                                    msgContentMap.put(message_reference +
                                                                            push_id, messageMap);

                                                                    mRootReference.updateChildren(msgContentMap,
                                                                            (databaseError, databaseReference) -> {
                                                                            });


                                                                    mUsersReference.child(mPhoneNumber).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                    send_text.setText("");

                                                                    Map<String, Object> chatUnderId = new HashMap<>();
                                                                    chatUnderId.put("msgId", push_id);
                                                                    chatUnderId.put("seen", false);
                                                                    chatUnderId.put("visible", true);
                                                                    chatUnderId.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    addNewMessage.updateChildren(chatUnderId,
                                                                            (databaseError, databaseReference) -> {

                                                                                HashMap<String, Object> notificationData
                                                                                        = new HashMap<>();
                                                                                notificationData.put("from",
                                                                                        mPhoneNumber);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                mNotificationsDatabase.child(mChatPhone)
                                                                                        .push().setValue(notificationData)
                                                                                        .addOnCompleteListener(task1 -> {

                                                                                            if (task1.isSuccessful()) {

                                                                                                try {
                                                                                                    if (mp1.isPlaying()) {
                                                                                                        mp1.stop();
                                                                                                        mp1.release();

                                                                                                    }
                                                                                                    mp1.start();
                                                                                                } catch (Exception e) {
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                                //TODO: update message field seen

                                                                                                Toast.makeText(
                                                                                                        StatsActivity.this,
                                                                                                        "Notification Sent",
                                                                                                        Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        });
                                                                                //mp1.start();
                                                                                //TODO: add sent mark

                                                                            });

                                                                } else {

                                                                    /**
                                                                     * Adding the information under ads_users
                                                                     */

                                                                    mConvoRef = conversation_id;

                                                                    Map<String, Object> infoToAddUnderMe
                                                                            = new HashMap<>();
                                                                    infoToAddUnderMe.put("id", conversation_id);
                                                                    infoToAddUnderMe.put("type", "chat");
                                                                    infoToAddUnderMe.put("visible", true);
                                                                    infoToAddUnderMe.put("phone_number",
                                                                            mChatPhone);
                                                                    infoToAddUnderMe.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    Map<String, Object> mapForCurrentUser
                                                                            = new HashMap<>();
                                                                    mapForCurrentUser.put(myReference +
                                                                            conversation_id, infoToAddUnderMe);

                                                                    mRootReference.updateChildren(mapForCurrentUser);

                                                                    Map<String, Object> infoToAddUnderOther
                                                                            = new HashMap<>();

                                                                    infoToAddUnderOther.put("id", conversation_id);
                                                                    infoToAddUnderOther.put("type", "chat");
                                                                    infoToAddUnderOther.put("visible", true);
                                                                    infoToAddUnderOther.put("phone_number",
                                                                            mPhoneNumber);
                                                                    infoToAddUnderOther.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    Map<String, Object> mapForOtherUser
                                                                            = new HashMap<>();
                                                                    mapForOtherUser.put(otherUserReference +
                                                                            conversation_id, infoToAddUnderOther);

                                                                    mRootReference.updateChildren(mapForOtherUser);

                                                                    /**
                                                                     * Adding information into ads_chat &
                                                                     * ads_messages
                                                                     */

                                                                    mUsersReference.child(mPhoneNumber).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                    DatabaseReference usersInChat = mRootReference
                                                                            .child("ads_chat").child(conversation_id)
                                                                            .child("messages").child(push_id);

                                                                    Map<String, Object> messageMap = new HashMap<>();
                                                                    messageMap.put("content", downloadUrl);
                                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                    messageMap.put("type", "document");
                                                                    messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mPhoneNumber);
                                                                    messageMap.put("seen", false);

                                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                                    msgContentMap.put(message_reference +
                                                                            push_id, messageMap);

                                                                    //Adding message
                                                                    mRootReference.updateChildren(msgContentMap,
                                                                            (databaseError, databaseReference) -> {
                                                                            });

                                                                    Map<String, Object> chatRefMap = new HashMap<>();
                                                                    chatRefMap.put("users", mChatUsers);
                                                                    chatRefMap.put("seen", false);
                                                                    chatRefMap.put("visible", true);
                                                                    chatRefMap.put("lastMessage", push_id);

                                                                    Map<String, Object> messageUserMap =
                                                                            new HashMap<>();

                                                                    messageUserMap.put(chat_reference +
                                                                            conversation_id, chatRefMap);

                                                                    send_text.setText("");
                                                                    mRootReference.updateChildren(messageUserMap);

                                                                    Map<String, Object> chatUnderId = new HashMap<>();
                                                                    chatUnderId.put("msgId", push_id);
                                                                    chatUnderId.put("seen", false);
                                                                    chatUnderId.put("visible", true);
                                                                    chatUnderId.put("timestamp",
                                                                            ServerValue.TIMESTAMP);

                                                                    usersInChat.updateChildren(chatUnderId,
                                                                            (databaseError, databaseReference) -> {

                                                                                HashMap<String, Object> notificationData
                                                                                        = new HashMap<>();
                                                                                notificationData.put("from",
                                                                                        mPhoneNumber);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                mNotificationsDatabase.child(mChatPhone)
                                                                                        .push().setValue(notificationData)
                                                                                        .addOnCompleteListener(task1 -> {

                                                                                            if (task1.isSuccessful()) {

                                                                                                try {
                                                                                                    if (mp1.isPlaying()) {
                                                                                                        mp1.stop();
                                                                                                        mp1.release();

                                                                                                    }
                                                                                                    mp1.start();
                                                                                                } catch (Exception e) {
                                                                                                    e.printStackTrace();
                                                                                                }
                                                                                                //TODO: update message field seen

                                                                                                Toast.makeText(
                                                                                                        StatsActivity.this,
                                                                                                        "Notification Sent",
                                                                                                        Toast.LENGTH_SHORT).show();

                                                                                            }
                                                                                        });
                                                                                //mp1.start();
                                                                                //TODO: add sent mark

                                                                            });
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                            }

                                        } else {

                                            /**
                                             * Adding the information under ads_users
                                             */

                                            mConvoRef = conversation_id;

                                            Map<String, Object> infoToAddUnderMe
                                                    = new HashMap<>();
                                            infoToAddUnderMe.put("id", conversation_id);
                                            infoToAddUnderMe.put("type", "chat");
                                            infoToAddUnderMe.put("visible", true);
                                            infoToAddUnderMe.put("phone_number",
                                                    mChatPhone);
                                            infoToAddUnderMe.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            Map<String, Object> mapForCurrentUser
                                                    = new HashMap<>();
                                            mapForCurrentUser.put(myReference +
                                                    conversation_id, infoToAddUnderMe);

                                            mRootReference.updateChildren(mapForCurrentUser);

                                            Map<String, Object> infoToAddUnderOther
                                                    = new HashMap<>();

                                            infoToAddUnderOther.put("id", conversation_id);
                                            infoToAddUnderOther.put("type", "chat");
                                            infoToAddUnderOther.put("visible", true);
                                            infoToAddUnderOther.put("phone_number",
                                                    mPhoneNumber);
                                            infoToAddUnderOther.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            Map<String, Object> mapForOtherUser
                                                    = new HashMap<>();
                                            mapForOtherUser.put(otherUserReference +
                                                    conversation_id, infoToAddUnderOther);

                                            mRootReference.updateChildren(mapForOtherUser);

                                            /**
                                             * Adding information into ads_chat &
                                             * ads_messages
                                             */

                                            mUsersReference.child(mPhoneNumber).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                            mUsersReference.child(mChatPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                            DatabaseReference usersInChat = mRootReference
                                                    .child("ads_chat").child(conversation_id)
                                                    .child("messages").child(push_id);

                                            Map<String, Object> messageMap = new HashMap<>();
                                            messageMap.put("content", downloadUrl);
                                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                            messageMap.put("type", "document");
                                            messageMap.put("parent", statusId + "/" +mChatPhone);
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mPhoneNumber);
                                            messageMap.put("seen", false);

                                            Map<String, Object> msgContentMap = new HashMap<>();
                                            msgContentMap.put(message_reference +
                                                    push_id, messageMap);

                                            //Adding message
                                            mRootReference.updateChildren(msgContentMap,
                                                    (databaseError, databaseReference) -> {
                                                    });

                                            Map<String, Object> chatRefMap = new HashMap<>();
                                            chatRefMap.put("users", mChatUsers);
                                            chatRefMap.put("seen", false);
                                            chatRefMap.put("visible", true);
                                            chatRefMap.put("lastMessage", push_id);

                                            Map<String, Object> messageUserMap =
                                                    new HashMap<>();

                                            messageUserMap.put(chat_reference +
                                                    conversation_id, chatRefMap);

                                            send_text.setText("");
                                            mRootReference.updateChildren(messageUserMap);

                                            Map<String, Object> chatUnderId = new HashMap<>();
                                            chatUnderId.put("msgId", push_id);
                                            chatUnderId.put("seen", false);
                                            chatUnderId.put("visible", true);
                                            chatUnderId.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            usersInChat.updateChildren(chatUnderId,
                                                    (databaseError, databaseReference) -> {

                                                        HashMap<String, Object> notificationData
                                                                = new HashMap<>();
                                                        notificationData.put("from",
                                                                mPhoneNumber);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        mNotificationsDatabase.child(mChatPhone)
                                                                .push().setValue(notificationData)
                                                                .addOnCompleteListener(task1 -> {

                                                                    if (task1.isSuccessful()) {

                                                                        try {
                                                                            if (mp1.isPlaying()) {
                                                                                mp1.stop();
                                                                                mp1.release();

                                                                            }
                                                                            mp1.start();
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        //TODO: update message field seen

                                                                        Toast.makeText(
                                                                                StatsActivity.this,
                                                                                "Notification Sent",
                                                                                Toast.LENGTH_SHORT).show();

                                                                    }
                                                                });
                                                        //mp1.start();
                                                        //TODO: add sent mark

                                                    });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                    }

                });

            }

        }
        else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri songUri = null;
            if (data != null) {
                songUri = data.getData();
            }

            String myReference = "ads_users/" + mPhoneNumber + "/" + "conversation/";
            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

            String chat_reference = "ads_chat/";

            final String message_reference = "ads_messages/";

            DatabaseReference conversation_push = mRootReference.child("ads_users")
                    .child(mPhoneNumber).push();
            String conversation_id = conversation_push.getKey();

            DatabaseReference msg_push = mRootReference.child("ads_messages").push();

            String push_id = msg_push.getKey();

            StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
            filePath.putFile(Objects.requireNonNull(songUri)).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                    mRootReference.child("ads_users").child(mPhoneNumber)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("conversation")) {

                                        final Conversation[] c = new Conversation[1];
                                        List<Conversation> listConvo = new ArrayList<>();
                                        final boolean[] isThere = {false};
                                        //final String[] mConvoRef = new String[1];

                                        if(!isFirstTime){

                                            DatabaseReference addNewMessage =
                                                    mRootReference.child("ads_chat")
                                                            .child(mConvoRef)
                                                            .child("messages").child(push_id);

                                            mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                    .setValue(push_id);

                                            Map<String, Object> messageMap = new HashMap<>();
                                            messageMap.put("content", downloadUrl);
                                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                            messageMap.put("type", "audio");
                                            messageMap.put("parent", statusId + "/" +mChatPhone);
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mPhoneNumber);
                                            messageMap.put("seen", false);

                                            Map<String, Object> msgContentMap = new HashMap<>();
                                            msgContentMap.put(message_reference +
                                                    push_id, messageMap);

                                            mRootReference.updateChildren(msgContentMap,
                                                    (databaseError, databaseReference) -> {
                                                    });


                                            mUsersReference.child(mPhoneNumber).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                            mUsersReference.child(mChatPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                            send_text.setText("");

                                            Map<String, Object> chatUnderId = new HashMap<>();
                                            chatUnderId.put("msgId", push_id);
                                            chatUnderId.put("seen", false);
                                            chatUnderId.put("visible", true);
                                            chatUnderId.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            addNewMessage.updateChildren(chatUnderId,
                                                    (databaseError, databaseReference) -> {

                                                        HashMap<String, Object> notificationData
                                                                = new HashMap<>();
                                                        notificationData.put("from",
                                                                mPhoneNumber);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        mNotificationsDatabase.child(mChatPhone)
                                                                .push().setValue(notificationData)
                                                                .addOnCompleteListener(task1 -> {

                                                                    if (task1.isSuccessful()) {

                                                                        try {
                                                                            if (mp1.isPlaying()) {
                                                                                mp1.stop();
                                                                                mp1.release();

                                                                            }
                                                                            mp1.start();
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        //TODO: update message field seen

                                                                        Toast.makeText(
                                                                                StatsActivity.this,
                                                                                "Notification Sent",
                                                                                Toast.LENGTH_SHORT).show();

                                                                    }
                                                                });
                                                        //mp1.start();
                                                        //TODO: add sent mark

                                                    });


                                        }else {
                                            mRootReference.child("ads_users")
                                                    .child(mPhoneNumber)
                                                    .child("conversation")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                c[0] = d.getValue(Conversation.class);
                                                                listConvo.add(c[0]);
                                                            }
                                                            for (int i = 0; i < listConvo.size(); i++) {
                                                                if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                                    isThere[0] = true;
                                                                    mConvoRef = listConvo.get(i).getId();
                                                                }
                                                            }
                                                            isFirstTime = false;
                                                            if (isThere[0]) {

                                                                DatabaseReference addNewMessage =
                                                                        mRootReference.child("ads_chat")
                                                                                .child(mConvoRef)
                                                                                .child("messages").child(push_id);

                                                                mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                                        .setValue(push_id);

                                                                Map<String, Object> messageMap = new HashMap<>();
                                                                messageMap.put("content", downloadUrl);
                                                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                messageMap.put("type", "audio");
                                                                messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mPhoneNumber);
                                                                messageMap.put("seen", false);

                                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                                msgContentMap.put(message_reference +
                                                                        push_id, messageMap);

                                                                mRootReference.updateChildren(msgContentMap,
                                                                        (databaseError, databaseReference) -> {
                                                                        });


                                                                mUsersReference.child(mPhoneNumber).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                mUsersReference.child(mChatPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                send_text.setText("");

                                                                Map<String, Object> chatUnderId = new HashMap<>();
                                                                chatUnderId.put("msgId", push_id);
                                                                chatUnderId.put("seen", false);
                                                                chatUnderId.put("visible", true);
                                                                chatUnderId.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                addNewMessage.updateChildren(chatUnderId,
                                                                        (databaseError, databaseReference) -> {

                                                                            HashMap<String, Object> notificationData
                                                                                    = new HashMap<>();
                                                                            notificationData.put("from",
                                                                                    mPhoneNumber);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            mNotificationsDatabase.child(mChatPhone)
                                                                                    .push().setValue(notificationData)
                                                                                    .addOnCompleteListener(task1 -> {

                                                                                        if (task1.isSuccessful()) {

                                                                                            try {
                                                                                                if (mp1.isPlaying()) {
                                                                                                    mp1.stop();
                                                                                                    mp1.release();

                                                                                                }
                                                                                                mp1.start();
                                                                                            } catch (Exception e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                            //TODO: update message field seen

                                                                                            Toast.makeText(
                                                                                                    StatsActivity.this,
                                                                                                    "Notification Sent",
                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    });
                                                                            //mp1.start();
                                                                            //TODO: add sent mark

                                                                        });

                                                            } else {

                                                                /**
                                                                 * Adding the information under ads_users
                                                                 */

                                                                mConvoRef = conversation_id;

                                                                Map<String, Object> infoToAddUnderMe
                                                                        = new HashMap<>();
                                                                infoToAddUnderMe.put("id", conversation_id);
                                                                infoToAddUnderMe.put("type", "chat");
                                                                infoToAddUnderMe.put("visible", true);
                                                                infoToAddUnderMe.put("phone_number",
                                                                        mChatPhone);
                                                                infoToAddUnderMe.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                Map<String, Object> mapForCurrentUser
                                                                        = new HashMap<>();
                                                                mapForCurrentUser.put(myReference +
                                                                        conversation_id, infoToAddUnderMe);

                                                                mRootReference.updateChildren(mapForCurrentUser);

                                                                Map<String, Object> infoToAddUnderOther
                                                                        = new HashMap<>();

                                                                infoToAddUnderOther.put("id", conversation_id);
                                                                infoToAddUnderOther.put("type", "chat");
                                                                infoToAddUnderOther.put("visible", true);
                                                                infoToAddUnderOther.put("phone_number",
                                                                        mPhoneNumber);
                                                                infoToAddUnderOther.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                Map<String, Object> mapForOtherUser
                                                                        = new HashMap<>();
                                                                mapForOtherUser.put(otherUserReference +
                                                                        conversation_id, infoToAddUnderOther);

                                                                mRootReference.updateChildren(mapForOtherUser);

                                                                /**
                                                                 * Adding information into ads_chat &
                                                                 * ads_messages
                                                                 */

                                                                mUsersReference.child(mPhoneNumber).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                mUsersReference.child(mChatPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                DatabaseReference usersInChat = mRootReference
                                                                        .child("ads_chat").child(conversation_id)
                                                                        .child("messages").child(push_id);

                                                                Map<String, Object> messageMap = new HashMap<>();
                                                                messageMap.put("content", downloadUrl);
                                                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                messageMap.put("type", "audio");
                                                                messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mPhoneNumber);
                                                                messageMap.put("seen", false);

                                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                                msgContentMap.put(message_reference +
                                                                        push_id, messageMap);

                                                                //Adding message
                                                                mRootReference.updateChildren(msgContentMap,
                                                                        (databaseError, databaseReference) -> {
                                                                        });

                                                                Map<String, Object> chatRefMap = new HashMap<>();
                                                                chatRefMap.put("users", mChatUsers);
                                                                chatRefMap.put("seen", false);
                                                                chatRefMap.put("visible", true);
                                                                chatRefMap.put("lastMessage", push_id);

                                                                Map<String, Object> messageUserMap =
                                                                        new HashMap<>();

                                                                messageUserMap.put(chat_reference +
                                                                        conversation_id, chatRefMap);

                                                                send_text.setText("");
                                                                mRootReference.updateChildren(messageUserMap);

                                                                Map<String, Object> chatUnderId = new HashMap<>();
                                                                chatUnderId.put("msgId", push_id);
                                                                chatUnderId.put("seen", false);
                                                                chatUnderId.put("visible", true);
                                                                chatUnderId.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                usersInChat.updateChildren(chatUnderId,
                                                                        (databaseError, databaseReference) -> {

                                                                            HashMap<String, Object> notificationData
                                                                                    = new HashMap<>();
                                                                            notificationData.put("from",
                                                                                    mPhoneNumber);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            mNotificationsDatabase.child(mChatPhone)
                                                                                    .push().setValue(notificationData)
                                                                                    .addOnCompleteListener(task1 -> {

                                                                                        if (task1.isSuccessful()) {

                                                                                            try {
                                                                                                if (mp1.isPlaying()) {
                                                                                                    mp1.stop();
                                                                                                    mp1.release();

                                                                                                }
                                                                                                mp1.start();
                                                                                            } catch (Exception e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                            //TODO: update message field seen

                                                                                            Toast.makeText(
                                                                                                    StatsActivity.this,
                                                                                                    "Notification Sent",
                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    });
                                                                            //mp1.start();
                                                                            //TODO: add sent mark

                                                                        });
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }

                                    } else {

                                        /**
                                         * Adding the information under ads_users
                                         */

                                        mConvoRef = conversation_id;

                                        Map<String, Object> infoToAddUnderMe
                                                = new HashMap<>();
                                        infoToAddUnderMe.put("id", conversation_id);
                                        infoToAddUnderMe.put("type", "chat");
                                        infoToAddUnderMe.put("visible", true);
                                        infoToAddUnderMe.put("phone_number",
                                                mChatPhone);
                                        infoToAddUnderMe.put("timestamp",
                                                ServerValue.TIMESTAMP);

                                        Map<String, Object> mapForCurrentUser
                                                = new HashMap<>();
                                        mapForCurrentUser.put(myReference +
                                                conversation_id, infoToAddUnderMe);

                                        mRootReference.updateChildren(mapForCurrentUser);

                                        Map<String, Object> infoToAddUnderOther
                                                = new HashMap<>();

                                        infoToAddUnderOther.put("id", conversation_id);
                                        infoToAddUnderOther.put("type", "chat");
                                        infoToAddUnderOther.put("visible", true);
                                        infoToAddUnderOther.put("phone_number",
                                                mPhoneNumber);
                                        infoToAddUnderOther.put("timestamp",
                                                ServerValue.TIMESTAMP);

                                        Map<String, Object> mapForOtherUser
                                                = new HashMap<>();
                                        mapForOtherUser.put(otherUserReference +
                                                conversation_id, infoToAddUnderOther);

                                        mRootReference.updateChildren(mapForOtherUser);

                                        /**
                                         * Adding information into ads_chat &
                                         * ads_messages
                                         */

                                        DatabaseReference usersInChat = mRootReference
                                                .child("ads_chat").child(conversation_id)
                                                .child("messages").child(push_id);

                                        Map<String, Object> messageMap = new HashMap<>();
                                        messageMap.put("content", downloadUrl);
                                        messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                        messageMap.put("type", "audio");
                                        messageMap.put("parent", statusId + "/" +mChatPhone);
                                        messageMap.put("visible", true);
                                        messageMap.put("from", mPhoneNumber);
                                        messageMap.put("seen", false);

                                        Map<String, Object> msgContentMap = new HashMap<>();
                                        msgContentMap.put(message_reference +
                                                push_id, messageMap);

                                        //Adding message
                                        mRootReference.updateChildren(msgContentMap,
                                                (databaseError, databaseReference) -> {
                                                });

                                        Map<String, Object> chatRefMap = new HashMap<>();
                                        chatRefMap.put("users", mChatUsers);
                                        chatRefMap.put("seen", false);
                                        chatRefMap.put("visible", true);
                                        chatRefMap.put("lastMessage", push_id);

                                        Map<String, Object> messageUserMap =
                                                new HashMap<>();

                                        messageUserMap.put(chat_reference +
                                                conversation_id, chatRefMap);

                                        mUsersReference.child(mPhoneNumber).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                        mUsersReference.child(mChatPhone).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                        send_text.setText("");
                                        mRootReference.updateChildren(messageUserMap);

                                        Map<String, Object> chatUnderId = new HashMap<>();
                                        chatUnderId.put("msgId", push_id);
                                        chatUnderId.put("seen", false);
                                        chatUnderId.put("visible", true);
                                        chatUnderId.put("timestamp",
                                                ServerValue.TIMESTAMP);

                                        usersInChat.updateChildren(chatUnderId,
                                                (databaseError, databaseReference) -> {

                                                    HashMap<String, Object> notificationData
                                                            = new HashMap<>();
                                                    notificationData.put("from",
                                                            mPhoneNumber);
                                                    notificationData.put("message",
                                                            downloadUrl);

                                                    mNotificationsDatabase.child(mChatPhone)
                                                            .push().setValue(notificationData)
                                                            .addOnCompleteListener(task1 -> {

                                                                if (task1.isSuccessful()) {

                                                                    try {
                                                                        if (mp1.isPlaying()) {
                                                                            mp1.stop();
                                                                            mp1.release();

                                                                        }
                                                                        mp1.start();
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    //TODO: update message field seen

                                                                    Toast.makeText(
                                                                            StatsActivity.this,
                                                                            "Notification Sent",
                                                                            Toast.LENGTH_SHORT).show();

                                                                }
                                                            });
                                                    //mp1.start();
                                                    //TODO: add sent mark

                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

            });

        }
    }

    private void loadVideo() {

        if (mVideosPath != null && mVideosPath.size() > 0) {

            mVideosData.add(Uri.fromFile(new File(mVideosPath.get(0))));

        }

    }

    private void loadImages() {

        if (mImagesPath != null && mImagesPath.size() > 0) {

            for (int i = 0; i < mImagesPath.size(); i++) {
                mImagesData.add(Uri.fromFile(new File(mImagesPath.get(i))));
            }
        }
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context){
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_photo_camera_white_48px));
//            compressionMsg.setVisibility(View.VISIBLE);
//            picDescription.setVisibility(View.GONE);

            // Do something while loading the video
        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            try {

                filePath = KplCompressor.with(mContext).compressVideo(paths[0], paths[1]);

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return  filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            String statusId = resources.get(counter).getId();
            float length = imageFile.length() / 1024f; // Size in KB

            Uri uri = Uri.fromFile(imageFile);
            // Uri uri = Uri.fromFile(new File(f.getAbsolutePath()));
            // UploadTask uploadTask = filePath.putFile(mVideosData.get(0));
            String myReference = "ads_users/" + mPhoneNumber + "/" + "conversation/";
            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

            String chat_reference = "ads_chat/";

            DatabaseReference conversation_push = mRootReference.child("ads_users")
                    .child(mPhoneNumber).push();
            String conversation_id = conversation_push.getKey();

            final String message_reference = "ads_messages/";

            DatabaseReference msg_push = mRootReference.child("ads_messages").push();

            String push_id = msg_push.getKey();
//
            StorageReference filePath = mVideosStorage.child("messages_videos").child(push_id + ".mp4");
            UploadTask uploadTask = filePath.putFile(uri);
            uploadTask.addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
                    mRootReference.child("ads_users").child(mPhoneNumber)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("conversation")) {

                                        final Conversation[] c = new Conversation[1];
                                        List<Conversation> listConvo = new ArrayList<>();
                                        final boolean[] isThere = {false};
                                        //       final String[] mConvoRef = new String[1];

                                        if(!isFirstTime){

                                            // Getting reference to push_id under ads_user
                                            DatabaseReference addNewMessage =
                                                    mRootReference.child("ads_chat")
                                                            .child(mConvoRef)
                                                            .child("messages").child(push_id);

                                            mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                    .setValue(push_id);

                                            Map<String, Object> messageMap = new HashMap<>();
                                            messageMap.put("content", downloadUrl);
                                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                            messageMap.put("type", "video");
                                            messageMap.put("parent", statusId + "/" +mChatPhone);
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mPhoneNumber);
                                            messageMap.put("seen", false);

                                            Map<String, Object> msgContentMap = new HashMap<>();
                                            msgContentMap.put(message_reference +
                                                    push_id, messageMap);

                                            mRootReference.updateChildren(msgContentMap,
                                                    (databaseError, databaseReference) -> {
                                                    });


                                            mUsersReference.child(mPhoneNumber).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                            mUsersReference.child(mChatPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                            send_text.setText("");

                                            Map<String, Object> chatUnderId = new HashMap<>();
                                            chatUnderId.put("msgId", push_id);
                                            chatUnderId.put("seen", false);
                                            chatUnderId.put("visible", true);
                                            chatUnderId.put("timestamp",
                                                    ServerValue.TIMESTAMP);

                                            addNewMessage.updateChildren(chatUnderId,
                                                    (databaseError, databaseReference) -> {

                                                        HashMap<String, Object> notificationData
                                                                = new HashMap<>();
                                                        notificationData.put("from",
                                                                mPhoneNumber);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        mNotificationsDatabase.child(mChatPhone)
                                                                .push().setValue(notificationData)
                                                                .addOnCompleteListener(task1 -> {

                                                                    if (task1.isSuccessful()) {

                                                                        try {
                                                                            if (mp1.isPlaying()) {
                                                                                mp1.stop();
                                                                                mp1.release();

                                                                            }
                                                                            mp1.start();
                                                                        } catch (Exception e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                        //TODO: update message field seen

                                                                        Toast.makeText(
                                                                                StatsActivity.this,
                                                                                "Notification Sent",
                                                                                Toast.LENGTH_SHORT).show();

                                                                    }
                                                                });
                                                        //mp1.start();
                                                        //TODO: add sent mark

                                                    });

                                        }else {
                                            mRootReference.child("ads_users")
                                                    .child(mPhoneNumber)
                                                    .child("conversation")
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                                c[0] = d.getValue(Conversation.class);
                                                                listConvo.add(c[0]);
                                                            }
                                                            for (int i = 0; i < listConvo.size(); i++) {
                                                                if (listConvo.get(i).getPhone_number().equals(mChatPhone)) {
                                                                    isThere[0] = true;
                                                                    mConvoRef = listConvo.get(i).getId();
                                                                }
                                                            }
                                                            isFirstTime = false;
                                                            if (isThere[0]) {

                                                                // Getting reference to push_id under ads_user
                                                                DatabaseReference addNewMessage =
                                                                        mRootReference.child("ads_chat")
                                                                                .child(mConvoRef)
                                                                                .child("messages").child(push_id);

                                                                mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                                        .setValue(push_id);

                                                                Map<String, Object> messageMap = new HashMap<>();
                                                                messageMap.put("content", downloadUrl);
                                                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                messageMap.put("type", "video");
                                                                messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mPhoneNumber);
                                                                messageMap.put("seen", false);

                                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                                msgContentMap.put(message_reference +
                                                                        push_id, messageMap);

                                                                mRootReference.updateChildren(msgContentMap,
                                                                        (databaseError, databaseReference) -> {
                                                                        });


                                                                mUsersReference.child(mPhoneNumber).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                mUsersReference.child(mChatPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                send_text.setText("");

                                                                Map<String, Object> chatUnderId = new HashMap<>();
                                                                chatUnderId.put("msgId", push_id);
                                                                chatUnderId.put("seen", false);
                                                                chatUnderId.put("visible", true);
                                                                chatUnderId.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                addNewMessage.updateChildren(chatUnderId,
                                                                        (databaseError, databaseReference) -> {

                                                                            HashMap<String, Object> notificationData
                                                                                    = new HashMap<>();
                                                                            notificationData.put("from",
                                                                                    mPhoneNumber);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            mNotificationsDatabase.child(mChatPhone)
                                                                                    .push().setValue(notificationData)
                                                                                    .addOnCompleteListener(task1 -> {

                                                                                        if (task1.isSuccessful()) {

                                                                                            try {
                                                                                                if (mp1.isPlaying()) {
                                                                                                    mp1.stop();
                                                                                                    mp1.release();

                                                                                                }
                                                                                                mp1.start();
                                                                                            } catch (Exception e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                            //TODO: update message field seen

                                                                                            Toast.makeText(
                                                                                                    StatsActivity.this,
                                                                                                    "Notification Sent",
                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    });
                                                                            //mp1.start();
                                                                            //TODO: add sent mark

                                                                        });
                                                            } else {

                                                                /**
                                                                 * Adding the information under ads_users
                                                                 */

                                                                mConvoRef = conversation_id;
                                                                Map<String, Object> infoToAddUnderMe
                                                                        = new HashMap<>();
                                                                infoToAddUnderMe.put("id", conversation_id);
                                                                infoToAddUnderMe.put("type", "chat");
                                                                infoToAddUnderMe.put("visible", true);
                                                                infoToAddUnderMe.put("phone_number",
                                                                        mChatPhone);
                                                                infoToAddUnderMe.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                Map<String, Object> mapForCurrentUser
                                                                        = new HashMap<>();
                                                                mapForCurrentUser.put(myReference +
                                                                        conversation_id, infoToAddUnderMe);

                                                                mRootReference.updateChildren(mapForCurrentUser);

                                                                Map<String, Object> infoToAddUnderOther
                                                                        = new HashMap<>();

                                                                infoToAddUnderOther.put("id", conversation_id);
                                                                infoToAddUnderOther.put("type", "chat");
                                                                infoToAddUnderOther.put("visible", true);
                                                                infoToAddUnderOther.put("phone_number",
                                                                        mPhoneNumber);
                                                                infoToAddUnderOther.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                Map<String, Object> mapForOtherUser
                                                                        = new HashMap<>();
                                                                mapForOtherUser.put(otherUserReference +
                                                                        conversation_id, infoToAddUnderOther);

                                                                mRootReference.updateChildren(mapForOtherUser);

                                                                /**
                                                                 * Adding information into ads_chat &
                                                                 * ads_messages
                                                                 */

                                                                DatabaseReference usersInChat = mRootReference
                                                                        .child("ads_chat").child(conversation_id)
                                                                        .child("messages").child(push_id);

                                                                Map<String, Object> messageMap = new HashMap<>();
                                                                messageMap.put("content", downloadUrl);
                                                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                messageMap.put("type", "video");
                                                                messageMap.put("parent", statusId + "/" +mChatPhone);
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mPhoneNumber);
                                                                messageMap.put("seen", false);

                                                                mUsersReference.child(mPhoneNumber).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                mUsersReference.child(mChatPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                                msgContentMap.put(message_reference +
                                                                        push_id, messageMap);

                                                                //Adding message
                                                                mRootReference.updateChildren(msgContentMap,
                                                                        (databaseError, databaseReference) -> {
                                                                        });

                                                                Map<String, Object> chatRefMap = new HashMap<>();
                                                                chatRefMap.put("users", mChatUsers);
                                                                chatRefMap.put("seen", false);
                                                                chatRefMap.put("visible", true);
                                                                chatRefMap.put("lastMessage", push_id);

                                                                Map<String, Object> messageUserMap =
                                                                        new HashMap<>();

                                                                messageUserMap.put(chat_reference +
                                                                        conversation_id, chatRefMap);

                                                                send_text.setText("");
                                                                mRootReference.updateChildren(messageUserMap);

                                                                Map<String, Object> chatUnderId = new HashMap<>();
                                                                chatUnderId.put("msgId", push_id);
                                                                chatUnderId.put("seen", false);
                                                                chatUnderId.put("visible", true);
                                                                chatUnderId.put("timestamp",
                                                                        ServerValue.TIMESTAMP);

                                                                usersInChat.updateChildren(chatUnderId,
                                                                        (databaseError, databaseReference) -> {

                                                                            HashMap<String, Object> notificationData
                                                                                    = new HashMap<>();
                                                                            notificationData.put("from",
                                                                                    mPhoneNumber);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            mNotificationsDatabase.child(mChatPhone)
                                                                                    .push().setValue(notificationData)
                                                                                    .addOnCompleteListener(task1 -> {

                                                                                        if (task1.isSuccessful()) {

                                                                                            try {
                                                                                                if (mp1.isPlaying()) {
                                                                                                    mp1.stop();
                                                                                                    mp1.release();

                                                                                                }
                                                                                                mp1.start();
                                                                                            } catch (Exception e) {
                                                                                                e.printStackTrace();
                                                                                            }
                                                                                            //TODO: update message field seen

                                                                                            Toast.makeText(
                                                                                                    StatsActivity.this,
                                                                                                    "Notification Sent",
                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    });
                                                                            //mp1.start();
                                                                            //TODO: add sent mark

                                                                        });
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                        }
                                    } else {

                                        /**
                                         * Adding the information under ads_users
                                         */

                                        mConvoRef = conversation_id;

                                        Map<String, Object> infoToAddUnderMe
                                                = new HashMap<>();
                                        infoToAddUnderMe.put("id", conversation_id);
                                        infoToAddUnderMe.put("type", "chat");
                                        infoToAddUnderMe.put("visible", true);
                                        infoToAddUnderMe.put("phone_number",
                                                mChatPhone);
                                        infoToAddUnderMe.put("timestamp",
                                                ServerValue.TIMESTAMP);

                                        Map<String, Object> mapForCurrentUser
                                                = new HashMap<>();
                                        mapForCurrentUser.put(myReference +
                                                conversation_id, infoToAddUnderMe);

                                        mRootReference.updateChildren(mapForCurrentUser);

                                        Map<String, Object> infoToAddUnderOther
                                                = new HashMap<>();

                                        infoToAddUnderOther.put("id", conversation_id);
                                        infoToAddUnderOther.put("type", "chat");
                                        infoToAddUnderOther.put("visible", true);
                                        infoToAddUnderOther.put("phone_number",
                                                mPhoneNumber);
                                        infoToAddUnderOther.put("timestamp",
                                                ServerValue.TIMESTAMP);

                                        Map<String, Object> mapForOtherUser
                                                = new HashMap<>();
                                        mapForOtherUser.put(otherUserReference +
                                                conversation_id, infoToAddUnderOther);

                                        mRootReference.updateChildren(mapForOtherUser);

                                        /**
                                         * Adding information into ads_chat &
                                         * ads_messages
                                         */

                                        DatabaseReference usersInChat = mRootReference
                                                .child("ads_chat").child(conversation_id)
                                                .child("messages").child(push_id);

                                        Map<String, Object> messageMap = new HashMap<>();
                                        messageMap.put("content", downloadUrl);
                                        messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                        messageMap.put("type", "video");
                                        messageMap.put("parent", statusId + "/" +mChatPhone);
                                        messageMap.put("visible", true);
                                        messageMap.put("from", mPhoneNumber);
                                        messageMap.put("seen", false);

                                        Map<String, Object> msgContentMap = new HashMap<>();
                                        msgContentMap.put(message_reference +
                                                push_id, messageMap);

                                        //Adding message
                                        mRootReference.updateChildren(msgContentMap,
                                                (databaseError, databaseReference) -> {
                                                });

                                        Map<String, Object> chatRefMap = new HashMap<>();
                                        chatRefMap.put("users", mChatUsers);
                                        chatRefMap.put("seen", false);
                                        chatRefMap.put("visible", true);
                                        chatRefMap.put("lastMessage", push_id);

                                        Map<String, Object> messageUserMap =
                                                new HashMap<>();

                                        messageUserMap.put(chat_reference +
                                                conversation_id, chatRefMap);

                                        mUsersReference.child(mPhoneNumber).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                        mUsersReference.child(mChatPhone).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                        send_text.setText("");
                                        mRootReference.updateChildren(messageUserMap);

                                        Map<String, Object> chatUnderId = new HashMap<>();
                                        chatUnderId.put("msgId", push_id);
                                        chatUnderId.put("seen", false);
                                        chatUnderId.put("visible", true);
                                        chatUnderId.put("timestamp",
                                                ServerValue.TIMESTAMP);

                                        usersInChat.updateChildren(chatUnderId,
                                                (databaseError, databaseReference) -> {

                                                    HashMap<String, Object> notificationData
                                                            = new HashMap<>();
                                                    notificationData.put("from",
                                                            mPhoneNumber);
                                                    notificationData.put("message",
                                                            downloadUrl);

                                                    mNotificationsDatabase.child(mChatPhone)
                                                            .push().setValue(notificationData)
                                                            .addOnCompleteListener(task1 -> {

                                                                if (task1.isSuccessful()) {

                                                                    try {
                                                                        if (mp1.isPlaying()) {
                                                                            mp1.stop();
                                                                            mp1.release();

                                                                        }
                                                                        mp1.start();
                                                                    } catch (Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    //TODO: update message field seen

                                                                    Toast.makeText(
                                                                            StatsActivity.this,
                                                                            "Notification Sent",
                                                                            Toast.LENGTH_SHORT).show();

                                                                }
                                                            });
                                                    //mp1.start();
                                                    //TODO: add sent mark

                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }

            });


        }
    }

}


