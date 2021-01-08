package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.islamassem.voicemessager.VoiceMessagerFragment;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import app.frantic.kplcompressor.KplCompressor;
import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.MessageAdapter;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.AlertDialogHelper;
import marcelin.thierry.chatapp.utils.CheckInternet_;
import marcelin.thierry.chatapp.utils.RecyclerItemClickListener;

import static marcelin.thierry.chatapp.activities.MainActivity.getDateDiff;

public class ChatActivity extends AppCompatActivity implements AlertDialogHelper.AlertDialogListener,
        SearchView.OnQueryTextListener, Serializable, VoiceMessagerFragment.OnControllerClick {


    private static final String[] WALK_THROUGH = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    //    private static final String LOG_TAG = "AudioRecordTest";
    private static final int GALLERY_PICK = 1;
    private static final int MAX_ATTACHMENT_COUNT = 20;
    private static final int TOTAL_ITEMS_TO_LOAD = 30;
    private static final int PICK_IMAGE_REQUEST = 1001;

    private static final StorageReference mImagesStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mVideosStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mAudioStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mDocumentsStorage = FirebaseStorage.getInstance().getReference();

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mNotificationsDatabase = FirebaseDatabase.getInstance()
            .getReference().child("ads_notifications");

    private static final DatabaseReference mMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_messages");
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final DatabaseReference mCallReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_call_notifications");

    private static final DatabaseReference mVideoCallReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_video_call_notifications");

//    private static final DatabaseReference mExceptReference = FirebaseDatabase.getInstance()
//            .getReference().child("ads_except");

    private static boolean isOnActivity = false;
    private Uri mImageUri;


    private static String mFileName = null;
    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
    final java.util.Random rand = new java.util.Random();
    final Set<String> identifiers = new HashSet<>();

    private String mChatName;
    private String mChatPhone;
    private String mChatPicture;

    private String mCurrentUserPhone;
    private String mChatId;

    private List<String> mImagesPath;
    private List<String> mVideosPath;
    private List<Uri> mImagesData;
    private List<Uri> mVideosData;
    private ArrayList<String> mDocPath = new ArrayList<>();
    private List<Messages> messagesList = new ArrayList<>();

    private View mRootView;
    private CircleImageView mProfileImage;
    private RecyclerView mMessagesList;
    private MessageAdapter mMessageAdapter;

    private Dialog mDialog;
    private EmojiEditText mTextToSend;
    private EmojiPopup emojiPopup;

    private ImageButton mSendEmoji;
    private ImageButton mSendAttachment;
    private ImageView mCloseEditMode;

    private MediaPlayer mp1;
    private MediaRecorder mRecorder;
    private Toolbar mChatToolbar;
    private Map<String, Object> mChatUsers = new HashMap<>();
    private List<String> mUsers = new ArrayList<>();
    private boolean isFirstTime = true;

    private String mConvoRef;

    private LinearLayout mainVLayout;
    private LinearLayout replyLinearLayout;

    // Long click on messages
    private ActionMode mActionMode;
    private boolean isMultiSelect = false;
    private AlertDialogHelper mAlertDialogHelper;
    private List<Messages> mSelectedMessages = new ArrayList<>();

    private NestedScrollView nested;

    /***
     * Reply feature
     */
    private TextView senderOfMessage;
    private TextView messageReceived;

    private ImageView close_reply;
    private ImageView imageSent;
    private ImageView backButton;
    private ImageView mSendVoice;

    private TextView videoSent;
    private TextView audioSent;
    private TextView documentSent;
    private TextView mBatKo;
    private TextView title;

    private LinearLayout messageLinLayout, recycler_layout;
    private RelativeLayout replyTextLayout;

    private ProgressDialog progressDialog;
    //Copy Feature

    private String mFinalCopiedMessages = "";

    private RecyclerItemClickListener recyclerItemClickListener;

    private boolean testingFlag = false;

    private ChildEventListener conversationListener;
    private Fragment fragment;
    private LinearLayout linearLayout;
    private boolean editModeIsOn = false;
    private String clickedMessageId = "";
    private Long clickedMessageTimeStamp = 0L;

    private LinearLayout mUploadLayout;
    private TextView mFilePath, mFileSize, mFilePercentage;
    private ProgressBar mProgress;


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_chat);
        mRootView = findViewById(R.id.rootView);
        getImageBackground();
        recycler_layout = findViewById(R.id.recycler_layout);

        mAlertDialogHelper = new AlertDialogHelper(this);

        Log.i("testingTag", String.valueOf(testingFlag));

        fragment = VoiceMessagerFragment.build(this, true);

        // Reply
        imageSent = findViewById(R.id.imageSent);
        audioSent = findViewById(R.id.audioSent);
        videoSent = findViewById(R.id.videoSent);
        documentSent = findViewById(R.id.documentSent);
        mCloseEditMode = findViewById(R.id.closeEditMode);

        senderOfMessage = findViewById(R.id.senderOfMessage);
        messageReceived = findViewById(R.id.messageReceived);
        close_reply = findViewById(R.id.close_reply);
        title = findViewById(R.id.title);
        linearLayout = findViewById(R.id.linearLayout);

        mChatName = getIntent().getStringExtra("user_name");
        mChatPhone = getIntent().getStringExtra("user_phone");
        mChatPicture = getIntent().getStringExtra("user_picture");
        mChatId = getIntent().getStringExtra("chat_id");
        //  Log.i("CHAT_ID", mChatId);

        mBatKo = findViewById(R.id.batKo);

        mChatToolbar = findViewById(R.id.chat_bar_main);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.contacts);

        // Firebase
        mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        // Saving the URI returned by the video and image library
        mImagesData = new ArrayList<>();
        mVideosData = new ArrayList<>();

        // Data for audio
        mFileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
        mFileName += "/" + randomIdentifier() + ".3gp";

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.chat_bar, null);

        actionBar.setCustomView(view);

        askPermission();

        mp1 = MediaPlayer.create(ChatActivity.this, R.raw.playsound);

        mProfileImage = findViewById(R.id.profileImage);

        mMessagesList = findViewById(R.id.messages_list);

        nested = findViewById(R.id.swipe_layout);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });


//        nested.postDelayed(() -> {
//            nested.fling(0);
//            nested.fullScroll(View.FOCUS_DOWN);
//        }, 200);

        title.setText(mChatName);
        mTextToSend = findViewById(R.id.send_text);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setNestedScrollingEnabled(false);
        mMessagesList.setLayoutManager(mLinearLayoutManager);

        ViewCompat.setNestedScrollingEnabled(nested, true);

        mainVLayout = findViewById(R.id.mainVLayout);
        replyLinearLayout = findViewById(R.id.replyLinearLayout);
        Handler handler = new Handler();

        mMessageAdapter = new MessageAdapter(messagesList, mainVLayout, mSelectedMessages, this, this, handler);
        new ItemTouchHelper(mItemTouchHelperCallback).attachToRecyclerView(mMessagesList);
        mMessagesList.setItemAnimator(new DefaultItemAnimator());

        mMessagesList.setAdapter(mMessageAdapter);
        replyTextLayout = findViewById(R.id.replyTextLayout);

        loadMessages();


        mTextToSend.addTextChangedListener(textWatcher);

        progressDialog = new ProgressDialog(this);

        recyclerItemClickListener = new RecyclerItemClickListener(this, mMessagesList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect) {
                    multi_select(position);
                } else {
                    final Messages message = messagesList.get(position);
                    if (message.getType().equals("text") && message.isVisible() || message.getType().equals("channel_link")
                            && message.isVisible() || message.getType().equals("group_link") && message.isVisible()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle(R.string.choose_option)
                                .setItems(R.array.options_msg, (dialog, which) -> {
                                    switch (which) {
                                        case 0:
                                            switch (message.getType()) {
                                                case "text": {

                                                    Intent i = new Intent(view.getContext(),
                                                            ForwardMessageActivity.class);
                                                    String s = "text";
                                                    i.putExtra("type", s);
                                                    i.putExtra("message", message.getContent());
                                                    view.getContext().startActivity(i);

                                                    break;
                                                }
                                                case "channel_link": {

                                                    Intent i = new Intent(view.getContext(),
                                                            ForwardMessageActivity.class);
                                                    String s = "channel_link";
                                                    i.putExtra("type", s);
                                                    i.putExtra("message", message.getContent());
                                                    view.getContext().startActivity(i);

                                                    break;
                                                }
                                                case "group_link": {

                                                    Intent i = new Intent(view.getContext(),
                                                            ForwardMessageActivity.class);
                                                    String s = "group_link";
                                                    i.putExtra("type", s);
                                                    i.putExtra("message", message.getContent());
                                                    view.getContext().startActivity(i);

                                                    break;
                                                }
                                            }

                                            break;

                                        case 1:

                                            //TODO: Poul from Fa: Search the ID from ads_users and do the trick
                                            if (!message.getFrom().equals(Objects.requireNonNull
                                                    (mAuth.getCurrentUser()).getPhoneNumber())) {
                                                Toast.makeText(view.getContext(), getString(R.string.cannot) +
                                                        getString(R.string.coming), Toast.LENGTH_SHORT).show();
                                            } else {
                                                switch (message.getType()) {
                                                    case "group_link":
                                                    case "channel_link":

                                                    case "text": {

                                                        mMessageReference.child(message.getMessageId())
                                                                .child("visible").setValue(false);

                                                        mMessageReference.child(message.getMessageId())
                                                                .child("content").setValue("Message Deleted");

                                                        Toast.makeText(view.getContext(), "Message deleted",
                                                                Toast.LENGTH_SHORT).show();

                                                        break;
                                                    }
                                                }
                                            }
                                            break;
                                        case 2:

                                            messageLinLayout = findViewById(R.id.messageLinLayout);
                                            LinearLayout replyLayout = findViewById(R.id.replyLinearLayout);

                                            String nameStored = Users.getLocalContactList().get(message.getFrom());
                                            senderOfMessage.setText(message.getFrom().equals(mCurrentUserPhone) ? getString(R.string.you) :
                                                    nameStored != null && nameStored.length() > 0 ? nameStored : message.getFrom());

                                            messageReceived.setVisibility(View.VISIBLE);
                                            imageSent.setVisibility(View.GONE);
                                            audioSent.setVisibility(View.GONE);
                                            videoSent.setVisibility(View.GONE);
                                            documentSent.setVisibility(View.GONE);

                                            messageReceived.setText(message.getContent());

                                            messageLinLayout.setBackgroundResource(R.drawable.new_border);

                                            replyLayout.setVisibility(View.VISIBLE);

                                            if (senderOfMessage.getText().toString().trim().equals("You")) {
                                                senderOfMessage.setTextColor(Color.parseColor("#20BF9F"));
                                            } else {
                                                senderOfMessage.setTextColor(Color.parseColor("#FF4500"));
                                            }

                                            Messages.setClickedMessageId(message.getMessageId());

                                            close_reply.setOnClickListener(view12 -> {
                                                messageLinLayout.setBackgroundResource(R.drawable.border);
                                                replyLayout.setVisibility(View.GONE);
                                            });
                                            break;

                                        case 3:

                                            long millis = System.currentTimeMillis();
                                            long minutes = 1200000L;

                                            long result = getDateDiff(message.getTimestamp(), millis, TimeUnit.MILLISECONDS);
                                            if(result < minutes){
                                                if (message.getFrom().equals(mCurrentUserPhone)) {
                                                    if(message.getType().equals("text")) {
                                                        Toast.makeText(ChatActivity.this, R.string.edit_mode, Toast.LENGTH_SHORT).show();

                                                        editModeIsOn = true;
                                                        provideCorrectUI();
                                                        clickedMessageId = message.getMessageId();
                                                        clickedMessageTimeStamp = message.getTimestamp();
                                                        mTextToSend.setText(message.getContent());
                                                    }else{
                                                        Toast.makeText(ChatActivity.this, "Cannot be edited", Toast.LENGTH_SHORT).show();
                                                    }
                                                }else{
                                                    Toast.makeText(ChatActivity.this, R.string.c_sent_by_you, Toast.LENGTH_SHORT).show();
                                                }

//                                                mMessagesList.removeOnItemTouchListener(recyclerItemClickListener);
                                            }else{
                                                Toast.makeText(ChatActivity.this, R.string.edit_limit, Toast.LENGTH_SHORT).show();
                                            }

                                            break;

                                        case 4:

                                            // Allow users to add messages to their favorites
                                            Map<String, Object> favoriteMessages = new HashMap<>();
                                            favoriteMessages.put("id", message.getMessageId());
                                            mUsersReference.child(mCurrentUserPhone).child("conversation").child(mChatId).child("f").updateChildren(favoriteMessages);

                                    }

                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //TODO: Remove if not working properly
                if (!testingFlag) {
                    if (!isMultiSelect) {
                        mSelectedMessages = new ArrayList<>();
                        isMultiSelect = true;

                        if (mActionMode == null) {
                            mActionMode = startActionMode(mActionModeCallback);
                        }
                    }
                }

                multi_select(position);

            }


        });

        mMessagesList.addOnItemTouchListener(recyclerItemClickListener);

        mSendEmoji = findViewById(R.id.send_emoji);
        mSendAttachment = findViewById(R.id.send_attachment);

        mDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);

        mSendVoice = findViewById(R.id.send_voice);

        mSendVoice.setOnClickListener(v -> {
            if (mSendVoice.getTag().equals("sendAudio")) {
                linearLayout.setVisibility(View.GONE);
                record();
            } else {
                sendMessage(mTextToSend);
            }
        });


        mUsers.add(mCurrentUserPhone);
        mUsers.add(mChatPhone);

        for (int i = 0; i < mUsers.size(); i++) {
            mChatUsers.put(mUsers.get(i), true);
        }

        listenerOnMessage();

        mUsersReference.keepSynced(true);
        mUsersReference.child(mCurrentUserPhone).addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("blocked")) {
                            mUsersReference.child(mCurrentUserPhone).child("blocked")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (mChatPhone == null) {
                                                return;
                                            }
                                            if (dataSnapshot.hasChild(mChatPhone)) {
                                                Toast.makeText(ChatActivity.this, R.string.user_blocked_msg,
                                                        Toast.LENGTH_SHORT).show();
                                                mTextToSend.setText(R.string.message_err);
                                                mTextToSend.setEnabled(false);
                                                //         mSend.setEnabled(false);
                                                mSendVoice.setEnabled(false);
                                                mSendEmoji.setEnabled(false);
                                                mSendAttachment.setEnabled(false);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        mUsersReference.child(mCurrentUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("blocked_by")) {
                    mUsersReference.child(mCurrentUserPhone).child("blocked_by").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(mChatPhone)) {
                                        mTextToSend.setText(R.string.user_not_available);
                                        mTextToSend.setEnabled(false);
                                        Picasso.get().load(mChatId).placeholder(R.drawable.ic_avatar)
                                                .into(mProfileImage);
                                        mSendVoice.setEnabled(false);
                                        mSendAttachment.setEnabled(false);
                                        mSendEmoji.setEnabled(false);
                                        // mSend.setEnabled(false);
                                        mChatToolbar.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUsersReference.child(mCurrentUserPhone).child("e").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("U-" + mChatPhone)) {
                    mTextToSend.setFocusable(false);
                    mTextToSend.setEnabled(false);
                    mSendVoice.setEnabled(false);
                    mChatToolbar.setEnabled(false);
                    mChatToolbar.setFocusable(false);

                    mBatKo.setVisibility(View.VISIBLE);
                    mBatKo.setOnClickListener(view -> {
                        new TTFancyGifDialog.Builder(ChatActivity.this)
                                .setTitle(getString(R.string.r_data))
                                .setMessage(getString(R.string.dis_tr_))
                                .setGifResource(R.drawable.gif30)
                                .isCancellable(true)
                                .OnPositiveClicked(() -> {

                                    progressDialog.setTitle(getString(R.string.r_data));
                                    progressDialog.setMessage(getString(R.string.dis_tr_));
                                    progressDialog.show();

                                    mUsersReference.child(mCurrentUserPhone).child("e").child("U-" + mChatPhone)
                                            .removeValue().addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            progressDialog.dismiss();
                                            finish();
                                        }
                                    });
                                })
                                .OnNegativeClicked(() -> {

                                })
                                .build();
                    });
                } else {
                    mBatKo.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Picasso.get().load(mChatPicture).placeholder(R.drawable.ic_avatar).into(mProfileImage);

        mChatToolbar.setOnClickListener(view13 -> {

            Intent goToChatSettings = new Intent(ChatActivity.this,
                    OneToOneChatSettings.class);
            goToChatSettings.putExtra("user_phone", mChatPhone);
            goToChatSettings.putExtra("user_name", mChatName);
            goToChatSettings.putExtra("user_picture", mChatPicture);
            goToChatSettings.putExtra("chat_id", mChatId);
            startActivity(goToChatSettings);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            finish();

        });

        mSendAttachment.setOnClickListener(view1 -> {
            try {
                new CheckInternet_(internet -> {
                    if (internet) {
                        showCustomDialog();
                    } else {
                        Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // To Remove
        mSendEmoji.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        mSendEmoji.setOnClickListener(ignore -> emojiPopup.toggle());

        setUpEmojiPopup();

        mCloseEditMode.setOnClickListener(v ->{
            editModeIsOn = false;
            provideCorrectUI();
            Toast.makeText(this, "Edit mode off", Toast.LENGTH_SHORT).show();
        });

        mUploadLayout = findViewById(R.id.uploadLayout);
        mFilePath = findViewById(R.id.filePath);
        mFileSize = findViewById(R.id.fileSize);
        mFilePercentage = findViewById(R.id.filePercentage);
        mProgress = findViewById(R.id.progress);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnActivity = true;

        Log.i("ChatOnResume", "called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        isOnActivity = false;

        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (RuntimeException stopException) {
                stopException.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }

        Log.i("ChatActivity", "onStop() called");

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOnActivity = false;
        mMessageAdapter.stopMediaPlayers();

        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (RuntimeException stopException) {
                stopException.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }

//        mRootReference.removeEventListener(conversationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnActivity = false;

        Log.i("ChatActivity", "onPause() called");

        if (mRecorder != null) {
            try {
                mRecorder.stop();
            } catch (RuntimeException stopException) {
                stopException.printStackTrace();
            }
            mRecorder.release();
            mRecorder = null;
        }
    }

    /**
     * Starting from here to change the values regarding ads_chat
     */

    private void sendAudio(File file) {

        try {
            new CheckInternet_(internet -> {
                if (internet) {
                    testingFlag = false;
                    String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
                    String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                    String chat_reference = "ads_chat/";

                    final String message_reference = "ads_messages/";

                    DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                    String push_id = msg_push.getKey();

                    StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
                    Uri voiceUri = Uri.fromFile(new File(file.getAbsolutePath()));

                    DatabaseReference conversation_push = mRootReference.child("ads_users")
                            .child(mCurrentUserPhone).push();
                    String conversation_id = conversation_push.getKey();
                    mUploadLayout.setVisibility(View.VISIBLE);

                    filePath.putFile(voiceUri).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            mFilePath.setText(push_id+".gp3");
                            String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl())
                                    .toString();

                            mRootReference.child("ads_users").child(mCurrentUserPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("conversation")) {

                                                final Conversation[] c = new Conversation[1];
                                                List<Conversation> listConvo = new ArrayList<>();
                                                final boolean[] isThere = {false};
                                                //final String[] mConvoRef = new String[1];

                                                if (!isFirstTime) {

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
                                                    messageMap.put("parent", "Default");
                                                    messageMap.put("visible", true);
                                                    messageMap.put("from", mCurrentUserPhone);
                                                    messageMap.put("seen", false);
                                                    messageMap.put("edited", false);


                                                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                        messageMap.put("parent", Messages.getClickedMessageId());
                                                        // Remove if crashed
                                                        replyLinearLayout.setVisibility(View.GONE);
                                                    }

                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                    msgContentMap.put(message_reference +
                                                            push_id, messageMap);

                                                    mRootReference.updateChildren(msgContentMap,
                                                            (databaseError, databaseReference) -> {
                                                            });

                                                    mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                    // mTextToSend.setText("");

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
                                                                        mCurrentUserPhone);
                                                                notificationData.put("message",
                                                                        downloadUrl);

                                                                //TODO: [fm] if user has been muted, don't push notification data
                                                                mUsersReference.child(mCurrentUserPhone).child("e")
                                                                        .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        if (!dataSnapshot.exists()) {
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
                                                                                                    ChatActivity.this,
                                                                                                    "Notification Sent",
                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                        }
                                                                                    });
                                                                            //mp1.start();
                                                                            //TODO: add sent mark
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });


                                                            });

                                                } else {
                                                    mRootReference.child("ads_users")
                                                            .child(mCurrentUserPhone)
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
                                                                        messageMap.put("parent", "Default");
                                                                        messageMap.put("visible", true);
                                                                        messageMap.put("from", mCurrentUserPhone);
                                                                        messageMap.put("seen", false);
                                                                        messageMap.put("edited", false);


                                                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                            messageMap.put("parent", Messages.getClickedMessageId());
                                                                            // Remove if crashed
                                                                            replyLinearLayout.setVisibility(View.GONE);
                                                                        }

                                                                        Map<String, Object> msgContentMap = new HashMap<>();
                                                                        msgContentMap.put(message_reference +
                                                                                push_id, messageMap);

                                                                        mRootReference.updateChildren(msgContentMap,
                                                                                (databaseError, databaseReference) -> {
                                                                                });


                                                                        mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                        mUsersReference.child(mChatPhone).child("conversation")
                                                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                        //   mTextToSend.setText("");

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
                                                                                            mCurrentUserPhone);
                                                                                    notificationData.put("message",
                                                                                            downloadUrl);

                                                                                    //TODO: [fm] if user has been muted, don't push notification data
                                                                                    mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                            .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                            if (!dataSnapshot.exists()) {
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
                                                                                                                        ChatActivity.this,
                                                                                                                        "Notification Sent",
                                                                                                                        Toast.LENGTH_SHORT).show();

                                                                                                            }
                                                                                                        });
                                                                                                //mp1.start();
                                                                                                //TODO: add sent mark
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });
                                                                                });

                                                                    } else {

                                                                        /**
                                                                         * Adding the information under ads_users
                                                                         */

                                                                        mConvoRef = conversation_id;
                                                                        mChatId = conversation_id;
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
                                                                                mCurrentUserPhone);
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
                                                                        messageMap.put("parent", "Default");
                                                                        messageMap.put("visible", true);
                                                                        messageMap.put("from", mCurrentUserPhone);
                                                                        messageMap.put("seen", false);
                                                                        messageMap.put("edited", false);


                                                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                            messageMap.put("parent", Messages.getClickedMessageId());
                                                                            // Remove if crashed
                                                                            replyLinearLayout.setVisibility(View.GONE);
                                                                        }

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

                                                                        mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                        mUsersReference.child(mChatPhone).child("conversation")
                                                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

//                                                                            mTextToSend.setText("");
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
                                                                                            mCurrentUserPhone);
                                                                                    notificationData.put("message",
                                                                                            downloadUrl);

                                                                                    //TODO: [fm] if user has been muted, don't push notification data
                                                                                    mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                            .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                            if (!dataSnapshot.exists()) {
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
                                                                                                                        ChatActivity.this,
                                                                                                                        "Notification Sent",
                                                                                                                        Toast.LENGTH_SHORT).show();

                                                                                                            }
                                                                                                        });
                                                                                                //mp1.start();
                                                                                                //TODO: add sent mark
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });

                                                                                });
                                                                        loadMessages();
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
                                                mChatId = conversation_id;
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
                                                        mCurrentUserPhone);
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
                                                messageMap.put("parent", "Default");
                                                messageMap.put("visible", true);
                                                messageMap.put("from", mCurrentUserPhone);
                                                messageMap.put("seen", false);
                                                messageMap.put("edited", false);


                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                    // Remove if crashed
                                                    replyLinearLayout.setVisibility(View.GONE);
                                                }

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

                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                mUsersReference.child(mChatPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                Map<String, Object> messageUserMap =
                                                        new HashMap<>();

                                                messageUserMap.put(chat_reference +
                                                        conversation_id, chatRefMap);

//                                                    mTextToSend.setText("");
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
                                                                    mCurrentUserPhone);
                                                            notificationData.put("message",
                                                                    downloadUrl);

                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                ChatActivity.this,
                                                                                                "Notification Sent",
                                                                                                Toast.LENGTH_SHORT).show();

                                                                                    }
                                                                                });
                                                                        //mp1.start();
                                                                        //TODO: add sent mark
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });


                                                        });
                                                loadMessages();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            mUploadLayout.setVisibility(View.GONE);
                        }

                    })
                            .addOnFailureListener(e ->{
                                Toast.makeText(ChatActivity.this, "Errata", Toast.LENGTH_SHORT).show();
                                mUploadLayout.setVisibility(View.GONE);
                            })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mProgress.setProgress((int)progress);
                                String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                                mFileSize.setText(progressText);
                                mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                            });


                    mTextToSend.setText("");
                } else {
                    Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            mImagesPath = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            loadImages();
            mUploadLayout.setVisibility(View.VISIBLE);

            for (int i = 0; i < mImagesData.size(); i++) {
                String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
                String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                String chat_reference = "ads_chat/";

                final String message_reference = "ads_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                String push_id = msg_push.getKey();

                DatabaseReference conversation_push = mRootReference.child("ads_users")
                        .child(mCurrentUserPhone).push();
                String conversation_id = conversation_push.getKey();

                StorageReference filePath = mImagesStorage.child("ads_messages_images")
                        .child(push_id + ".jpg");
                filePath.putFile(mImagesData.get(i)).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mFilePath.setText(push_id+".jpg");

                        String downloadUrl = Objects.requireNonNull(task.getResult()
                                .getDownloadUrl()).toString();

                        mRootReference.child("ads_users").child(mCurrentUserPhone)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("conversation")) {

                                            final Conversation[] c = new Conversation[1];
                                            List<Conversation> listConvo = new ArrayList<>();
                                            final boolean[] isThere = {false};
                                            //     final String[] mConvoRef = new String[1];

                                            if (!isFirstTime) {

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
                                                messageMap.put("parent", "Default");
                                                messageMap.put("visible", true);
                                                messageMap.put("from", mCurrentUserPhone);
                                                messageMap.put("seen", false);
                                                messageMap.put("edited", false);


                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                    // Remove if crashed
                                                    replyLinearLayout.setVisibility(View.GONE);
                                                }

                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                msgContentMap.put(message_reference +
                                                        push_id, messageMap);

                                                mRootReference.updateChildren(msgContentMap,
                                                        (databaseError, databaseReference) -> {
                                                        });


                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                mUsersReference.child(mChatPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                //  mTextToSend.setText("");

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
                                                                    mCurrentUserPhone);
                                                            notificationData.put("message",
                                                                    downloadUrl);

                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                ChatActivity.this,
                                                                                                "Notification Sent",
                                                                                                Toast.LENGTH_SHORT).show();

                                                                                    }
                                                                                });
                                                                        //mp1.start();
                                                                        //TODO: add sent mark
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                        });

                                            } else {
                                                mRootReference.child("ads_users")
                                                        .child(mCurrentUserPhone)
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

                                                                    mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    Map<String, Object> messageMap = new HashMap<>();
                                                                    messageMap.put("content", downloadUrl);
                                                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                                                    messageMap.put("type", "image");
                                                                    messageMap.put("parent", "Default");
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mCurrentUserPhone);
                                                                    messageMap.put("seen", false);
                                                                    messageMap.put("edited", false);


                                                                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                        messageMap.put("parent", Messages.getClickedMessageId());
                                                                        // Remove if crashed
                                                                        replyLinearLayout.setVisibility(View.GONE);
                                                                    }

                                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                                    msgContentMap.put(message_reference +
                                                                            push_id, messageMap);

                                                                    mRootReference.updateChildren(msgContentMap,
                                                                            (databaseError, databaseReference) -> {
                                                                            });


                                                                    // mTextToSend.setText("");

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
                                                                                        mCurrentUserPhone);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                //TODO: [fm] if user has been muted, don't push notification data
                                                                                mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                        .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if (!dataSnapshot.exists()) {
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
                                                                                                                    ChatActivity.this,
                                                                                                                    "Notification Sent",
                                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    });
                                                                                            //mp1.start();
                                                                                            //TODO: add sent mark
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            });

                                                                } else {

                                                                    /**
                                                                     * Adding the information under ads_users
                                                                     */

                                                                    mConvoRef = conversation_id;
                                                                    mChatId = conversation_id;
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
                                                                            mCurrentUserPhone);
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

                                                                    mUsersReference.child(mCurrentUserPhone).child("conversation")
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
                                                                    messageMap.put("parent", "Default");
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mCurrentUserPhone);
                                                                    messageMap.put("seen", false);
                                                                    messageMap.put("edited", false);


                                                                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                        messageMap.put("parent", Messages.getClickedMessageId());
                                                                        // Remove if crashed
                                                                        replyLinearLayout.setVisibility(View.GONE);
                                                                    }

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

                                                                    //   mTextToSend.setText("");
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
                                                                                        mCurrentUserPhone);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                //TODO: [fm] if user has been muted, don't push notification data
                                                                                mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                        .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if (!dataSnapshot.exists()) {
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
                                                                                                                    ChatActivity.this,
                                                                                                                    "Notification Sent",
                                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    });
                                                                                            //mp1.start();
                                                                                            //TODO: add sent mark
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            });
                                                                    loadMessages();
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
                                            mChatId = conversation_id;
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
                                                    mCurrentUserPhone);
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

                                            mUsersReference.child(mCurrentUserPhone).child("conversation")
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
                                            messageMap.put("parent", "Default");
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mCurrentUserPhone);
                                            messageMap.put("seen", false);
                                            messageMap.put("edited", false);

                                            if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                messageMap.put("parent", Messages.getClickedMessageId());
                                                // Remove if crashed
                                                replyLinearLayout.setVisibility(View.GONE);
                                            }

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

                                            //   mTextToSend.setText("");
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
                                                                mCurrentUserPhone);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        //TODO: [fm] if user has been muted, don't push notification data
                                                        mUsersReference.child(mCurrentUserPhone).child("e")
                                                                .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (!dataSnapshot.exists()) {
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
                                                                                            ChatActivity.this,
                                                                                            "Notification Sent",
                                                                                            Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            });
                                                                    //mp1.start();
                                                                    //TODO: add sent mark
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    });
                                            loadMessages();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                        mUploadLayout.setVisibility(View.GONE);
                    }

                })
                        .addOnFailureListener(e ->{
                            Toast.makeText(ChatActivity.this, "Errata", Toast.LENGTH_SHORT).show();
                            mUploadLayout.setVisibility(View.GONE);
                        })
                        .addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            mProgress.setProgress((int)progress);
                            String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                            mFileSize.setText(progressText);
                            mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                        });

            }

        }
        else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            mVideosPath = (List<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
            loadVideo();

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
            mUploadLayout.setVisibility(View.VISIBLE);

            for (int i = 0; i < mDocPath.size(); i++) {

                String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
                String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                String chat_reference = "ads_chat/";

                DatabaseReference conversation_push = mRootReference.child("ads_users")
                        .child(mCurrentUserPhone).push();
                String conversation_id = conversation_push.getKey();

                final String message_reference = "ads_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_messages").push();

                String push_id = msg_push.getKey();

                StorageReference filePath = mDocumentsStorage.child("ads_messages_documents").child(push_id + ".docx");
                filePath.putFile(Uri.fromFile(new File(mDocPath.get(i)))).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        mFilePath .setText(push_id+".docx");
                        String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                        mRootReference.child("ads_users").child(mCurrentUserPhone)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("conversation")) {

                                            final Conversation[] c = new Conversation[1];
                                            List<Conversation> listConvo = new ArrayList<>();
                                            final boolean[] isThere = {false};
                                            //final String[] mConvoRef = new String[1];

                                            if (!isFirstTime) {

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
                                                messageMap.put("parent", "Default");
                                                messageMap.put("visible", true);
                                                messageMap.put("from", mCurrentUserPhone);
                                                messageMap.put("seen", false);
                                                messageMap.put("edited", false);


                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                    // Remove if crashed
                                                    replyLinearLayout.setVisibility(View.GONE);
                                                }

                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                msgContentMap.put(message_reference +
                                                        push_id, messageMap);

                                                mRootReference.updateChildren(msgContentMap,
                                                        (databaseError, databaseReference) -> {
                                                        });


                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                mUsersReference.child(mChatPhone).child("conversation")
                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                //   mTextToSend.setText("");

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
                                                                    mCurrentUserPhone);
                                                            notificationData.put("message",
                                                                    downloadUrl);

                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                ChatActivity.this,
                                                                                                "Notification Sent",
                                                                                                Toast.LENGTH_SHORT).show();

                                                                                    }
                                                                                });
                                                                        //mp1.start();
                                                                        //TODO: add sent mark
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                        });

                                            } else {
                                                mRootReference.child("ads_users")
                                                        .child(mCurrentUserPhone)
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
                                                                    messageMap.put("parent", "Default");
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mCurrentUserPhone);
                                                                    messageMap.put("seen", false);
                                                                    messageMap.put("edited", false);


                                                                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                        messageMap.put("parent", Messages.getClickedMessageId());
                                                                        // Remove if crashed
                                                                        replyLinearLayout.setVisibility(View.GONE);
                                                                    }

                                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                                    msgContentMap.put(message_reference +
                                                                            push_id, messageMap);

                                                                    mRootReference.updateChildren(msgContentMap,
                                                                            (databaseError, databaseReference) -> {
                                                                            });


                                                                    mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                                                    //       mTextToSend.setText("");

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
                                                                                        mCurrentUserPhone);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                //TODO: [fm] if user has been muted, don't push notification data
                                                                                mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                        .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if (!dataSnapshot.exists()) {
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
                                                                                                                    ChatActivity.this,
                                                                                                                    "Notification Sent",
                                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    });
                                                                                            //mp1.start();
                                                                                            //TODO: add sent mark
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });
                                                                            });

                                                                } else {

                                                                    /**
                                                                     * Adding the information under ads_users
                                                                     */

                                                                    mConvoRef = conversation_id;
                                                                    mChatId = conversation_id;
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
                                                                            mCurrentUserPhone);
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

                                                                    mUsersReference.child(mCurrentUserPhone).child("conversation")
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
                                                                    messageMap.put("parent", "Default");
                                                                    messageMap.put("visible", true);
                                                                    messageMap.put("from", mCurrentUserPhone);
                                                                    messageMap.put("seen", false);

                                                                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                        messageMap.put("parent", Messages.getClickedMessageId());
                                                                        // Remove if crashed
                                                                        replyLinearLayout.setVisibility(View.GONE);
                                                                    }

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

                                                                    //    mTextToSend.setText("");
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
                                                                                        mCurrentUserPhone);
                                                                                notificationData.put("message",
                                                                                        downloadUrl);

                                                                                //TODO: [fm] if user has been muted, don't push notification data
                                                                                mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                        .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if (!dataSnapshot.exists()) {
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
                                                                                                                    ChatActivity.this,
                                                                                                                    "Notification Sent",
                                                                                                                    Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    });
                                                                                            //mp1.start();
                                                                                            //TODO: add sent mark
                                                                                        }
                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });
                                                                            });
                                                                    loadMessages();
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
                                            mChatId = conversation_id;
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
                                                    mCurrentUserPhone);
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

                                            mUsersReference.child(mCurrentUserPhone).child("conversation")
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
                                            messageMap.put("parent", "Default");
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mCurrentUserPhone);
                                            messageMap.put("seen", false);
                                            messageMap.put("edited", false);


                                            if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                messageMap.put("parent", Messages.getClickedMessageId());
                                                // Remove if crashed
                                                replyLinearLayout.setVisibility(View.GONE);
                                            }

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

                                            mTextToSend.setText("");
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
                                                                mCurrentUserPhone);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        //TODO: [fm] if user has been muted, don't push notification data
                                                        mUsersReference.child(mCurrentUserPhone).child("e")
                                                                .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (!dataSnapshot.exists()) {
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
                                                                                            ChatActivity.this,
                                                                                            "Notification Sent",
                                                                                            Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            });
                                                                    //mp1.start();
                                                                    //TODO: add sent mark
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    });
                                            loadMessages();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                        mUploadLayout.setVisibility(View.GONE);
                    }

                })
                        .addOnFailureListener(e ->{
                            Toast.makeText(ChatActivity.this, "Errata", Toast.LENGTH_SHORT).show();
                            mUploadLayout.setVisibility(View.GONE);
                        })
                        .addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            mProgress.setProgress((int)progress);
                            String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                            mFileSize.setText(progressText);
                            mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                        });
            }

        }
        else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri songUri = data.getData();

            String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

            String chat_reference = "ads_chat/";

            final String message_reference = "ads_messages/";

            DatabaseReference conversation_push = mRootReference.child("ads_users")
                    .child(mCurrentUserPhone).push();
            String conversation_id = conversation_push.getKey();

            DatabaseReference msg_push = mRootReference.child("ads_messages").push();

            String push_id = msg_push.getKey();
            mUploadLayout.setVisibility(View.VISIBLE);

            StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
            filePath.putFile(Objects.requireNonNull(songUri)).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    mFilePath.setText(push_id+".gp3");

                    String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                    mRootReference.child("ads_users").child(mCurrentUserPhone)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("conversation")) {

                                        final Conversation[] c = new Conversation[1];
                                        List<Conversation> listConvo = new ArrayList<>();
                                        final boolean[] isThere = {false};
                                        //final String[] mConvoRef = new String[1];

                                        if (!isFirstTime) {

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
                                            messageMap.put("parent", "Default");
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mCurrentUserPhone);
                                            messageMap.put("seen", false);
                                            messageMap.put("edited", false);


                                            if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                messageMap.put("parent", Messages.getClickedMessageId());
                                                // Remove if crashed
                                                replyLinearLayout.setVisibility(View.GONE);
                                            }

                                            Map<String, Object> msgContentMap = new HashMap<>();
                                            msgContentMap.put(message_reference +
                                                    push_id, messageMap);

                                            mRootReference.updateChildren(msgContentMap,
                                                    (databaseError, databaseReference) -> {
                                                    });


                                            mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                            mUsersReference.child(mChatPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                                            mTextToSend.setText("");

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
                                                                mCurrentUserPhone);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        //TODO: [fm] if user has been muted, don't push notification data
                                                        mUsersReference.child(mCurrentUserPhone).child("e")
                                                                .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (!dataSnapshot.exists()) {
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
                                                                                            ChatActivity.this,
                                                                                            "Notification Sent",
                                                                                            Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            });
                                                                    //mp1.start();
                                                                    //TODO: add sent mark
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                                    });


                                        } else {
                                            mRootReference.child("ads_users")
                                                    .child(mCurrentUserPhone)
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
                                                                messageMap.put("parent", "Default");
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mCurrentUserPhone);
                                                                messageMap.put("seen", false);
                                                                messageMap.put("edited", false);

                                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                                    // Remove if crashed
                                                                    replyLinearLayout.setVisibility(View.GONE);
                                                                }

                                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                                msgContentMap.put(message_reference +
                                                                        push_id, messageMap);

                                                                mRootReference.updateChildren(msgContentMap,
                                                                        (databaseError, databaseReference) -> {
                                                                        });


                                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                mUsersReference.child(mChatPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                                                                mTextToSend.setText("");

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
                                                                                    mCurrentUserPhone);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                                ChatActivity.this,
                                                                                                                "Notification Sent",
                                                                                                                Toast.LENGTH_SHORT).show();

                                                                                                    }
                                                                                                });
                                                                                        //mp1.start();
                                                                                        //TODO: add sent mark
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                        });

                                                            } else {

                                                                /**
                                                                 * Adding the information under ads_users
                                                                 */

                                                                mConvoRef = conversation_id;
                                                                mChatId = conversation_id;
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
                                                                        mCurrentUserPhone);
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

                                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
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
                                                                messageMap.put("parent", "Default");
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mCurrentUserPhone);
                                                                messageMap.put("seen", false);
                                                                messageMap.put("edited", false);


                                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                                    // Remove if crashed
                                                                    replyLinearLayout.setVisibility(View.GONE);
                                                                }

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

//                                                                mTextToSend.setText("");
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
                                                                                    mCurrentUserPhone);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                                ChatActivity.this,
                                                                                                                "Notification Sent",
                                                                                                                Toast.LENGTH_SHORT).show();

                                                                                                    }
                                                                                                });
                                                                                        //mp1.start();
                                                                                        //TODO: add sent mark
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                        });
                                                                loadMessages();
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
                                        mChatId = conversation_id;
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
                                                mCurrentUserPhone);
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
                                        messageMap.put("parent", "Default");
                                        messageMap.put("visible", true);
                                        messageMap.put("from", mCurrentUserPhone);
                                        messageMap.put("seen", false);
                                        messageMap.put("edited", false);


                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                            messageMap.put("parent", Messages.getClickedMessageId());
                                            // Remove if crashed
                                            replyLinearLayout.setVisibility(View.GONE);
                                        }

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

                                        mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                        mUsersReference.child(mChatPhone).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                        //   mTextToSend.setText("");
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
                                                            mCurrentUserPhone);
                                                    notificationData.put("message",
                                                            downloadUrl);

                                                    //TODO: [fm] if user has been muted, don't push notification data
                                                    mUsersReference.child(mCurrentUserPhone).child("e")
                                                            .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (!dataSnapshot.exists()) {
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
                                                                                        ChatActivity.this,
                                                                                        "Notification Sent",
                                                                                        Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });
                                                                //mp1.start();
                                                                //TODO: add sent mark

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                });
                                        loadMessages();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                    mUploadLayout.setVisibility(View.GONE);
                }

            })
        .addOnFailureListener(e ->{
                Toast.makeText(ChatActivity.this, "Errata", Toast.LENGTH_SHORT).show();
                mUploadLayout.setVisibility(View.GONE);
            })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgress.setProgress((int)progress);
                        String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                        mFileSize.setText(progressText);
                        mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                    });

        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();

                // Removes Uri Permission so that when you restart the device, it will be allowed to reload.
                this.grantUriPermission(this.getPackageName(), mImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                this.getContentResolver().takePersistableUriPermission(mImageUri, takeFlags);

                // Saves image URI as string to Default Shared Preferences
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("image", String.valueOf(mImageUri));
                editor.apply();

                Picasso.get().load(mImageUri).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        mRootView.setBackground(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                    }

                    @Override
                    public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        Log.d("TAG", "FAILED");

                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        Log.d("TAG", "Prepare Load");

                    }
                });

            }
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

    private void showCustomDialog() {

        TextView sendImage;
        TextView sendAudio;
        TextView sendDocument;
        TextView sendVideo;

        mDialog.setContentView(R.layout.custom_dialog);
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(true);

        sendImage = mDialog.findViewById(R.id.custom_layout_image_send);
        sendAudio = mDialog.findViewById(R.id.custom_layout_audio_send);
        sendDocument = mDialog.findViewById(R.id.custom_layout_document_send);
        sendVideo = mDialog.findViewById(R.id.custom_layout_video_send);

        sendImage.setOnClickListener(view -> {

            try {
                new CheckInternet_(internet -> {
                    if (internet) {
                        mDialog.dismiss();
                        try {
                            pickImage();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


        sendAudio.setOnClickListener(view -> {
            new CheckInternet_(internet -> {
                if (internet) {
                    Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(i, GALLERY_PICK);
                    mDialog.dismiss();
                } else {
                    Toast.makeText(this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                }
            });
        });

        sendVideo.setOnClickListener(view -> {
            new CheckInternet_(internet -> {
                if (internet) {
                    mDialog.dismiss();
                    pickVideo();
                } else {
                    Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                }
            });
        });

        sendDocument.setOnClickListener(view -> {
            new CheckInternet_(internet -> {
                if (internet) {
                    onPickDoc();
                    mDialog.dismiss();
                } else {
                    Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                }
            });
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
        new VideoPicker.Builder(ChatActivity.this)
                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                .directory(VideoPicker.Directory.DEFAULT)
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build();
    }

    private void loadMessages() {
        if (mChatId == null) {
            return;
        }

        //[fm] setting unread message to 0
        setUnreadMessageCount(mChatId, 0);

        Query conversationQ = mRootReference.child("ads_chat").child(mChatId).child("messages")
                .limitToLast(TOTAL_ITEMS_TO_LOAD);
        conversationListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chatRef = dataSnapshot.getValue(Chat.class);
                if (chatRef == null) {
                    return;
                }
                Log.i("CHAT_MSG_ID", chatRef.getMsgId());
                Query chatQ = mMessageReference.child(chatRef.getMsgId());
                chatQ.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Messages m = dataSnapshot.getValue(Messages.class);
                        if (m == null) {
                            return;
                        }
                        //TODO: Add Except logic
                        mUsersReference.child(mCurrentUserPhone).child("e").child("U-" + mChatPhone)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Log.i("CHAT_IN_EXCEPT", dataSnapshot.getValue().toString());
                                            Long mutedTimestamp = (Long) dataSnapshot.getValue();
                                            if (mutedTimestamp < m.getTimestamp()) {
                                                Log.i("CHAT_MSG_MUTED", "message muted on "
                                                        + mutedTimestamp);
                                                return;
                                            }
                                        }
                                        Log.i("CHAT_ALLOWED", m.getContent());
                                        m.setMessageId(chatRef.getMsgId());
                                        if (isOnActivity && !m.getFrom().equals(mCurrentUserPhone)) {
                                            updateSeen(m, mConvoRef);
                                        }
                                        //comment: [fm] not sure what that is
                                        m.setSent(true);

                                        if (!m.getParent().equals("Default") && m.isVisible()) {
                                            m.setReplyOn(true);
                                        }
                                        messagesList.add(m);
                                        mMessageAdapter.notifyDataSetChanged();

                                        nested.postDelayed(() -> {
                                            nested.fullScroll(View.FOCUS_DOWN);
                                        }, 200);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
             //   messagesList.clear();
                Chat chatRef = dataSnapshot.getValue(Chat.class);
                if (chatRef == null) {
                    return;
                }
                Log.i("CHAT_CHANGED", "CALLED");
                Query chatQ = mMessageReference.child(chatRef.getMsgId());
                chatQ.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Messages m = dataSnapshot.getValue(Messages.class);
                        if (m == null) {
                            return;
                        }

                        m.setMessageId(chatRef.getMsgId());
                        int pos = messagesList.indexOf(m);
                        Log.i("CHAT_CHANGED_POS", String.valueOf(pos));
                        if (pos < 0) {
                            return;
                        }
                        messagesList.set(pos, m);
//                        messagesList.add(m);
                        mMessageAdapter.notifyDataSetChanged();
                        nested.postDelayed(() -> {
                            nested.pageScroll(View.FOCUS_DOWN);
                        }, 200);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
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
        };

        conversationQ.addChildEventListener(conversationListener);
    }

    private void pickImage() {
        new ImagePicker.Builder(ChatActivity.this)
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

    private void sendMessage(EmojiEditText mTextToSend) {
        String message = Objects.requireNonNull(mTextToSend.getText()).toString().trim();
        if(editModeIsOn){
            new CheckInternet_(internet -> {
               if(internet){

                   long millis = System.currentTimeMillis();
                   long minutes = 1200000L;

                   long result = getDateDiff(clickedMessageTimeStamp, millis, TimeUnit.MILLISECONDS);
                   if(result < minutes){
                       if(!message.isEmpty()){
                           mMessageReference.child(clickedMessageId)
                                   .child("content").setValue(message);
                           mMessageReference.child(clickedMessageId).child("edited").setValue(true);
                           mMessageReference.child(clickedMessageId).child("timestamp").setValue(ServerValue.TIMESTAMP);
                           mRootReference.child("ads_chat").child(mChatId).child("messages").child(clickedMessageId).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                           messagesList.clear();
//                           loadMessages();
                           mTextToSend.setText("");
                           editModeIsOn = false;
                           provideCorrectUI();
                       }else{
                           Toast.makeText(ChatActivity.this, "Please enter a text", Toast.LENGTH_SHORT).show();
                       }
                   }else{
                       Toast.makeText(ChatActivity.this, R.string.edit_limit, Toast.LENGTH_SHORT).show();
                       editModeIsOn = false;
                       provideCorrectUI();
                   }

               }else{
                   Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
               }
            });
        }else {
            try {
//TODO:5 // to remove if not working
                new CheckInternet_(internet -> {
                    if (internet) {
                        testingFlag = false;
//                    String message = Objects.requireNonNull(mTextToSend.getText()).toString().trim();

                        if (!TextUtils.isEmpty(message)) {

                            String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
                            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

                            String chat_reference = "ads_chat/";

                            // Creating conversation_id for each message
                            DatabaseReference conversation_push = mRootReference.child("ads_users")
                                    .child(mCurrentUserPhone).push();
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
                            messageMap.put("parent", "Default");
                            messageMap.put("visible", true);
                            messageMap.put("from", mCurrentUserPhone);
                            messageMap.put("seen", false);
                            messageMap.put("edited", false);

                            //Toast.makeText(ChatActivity.this, messageMap.get("timestamp").toString(), Toast.LENGTH_SHORT).show();
                            // Going under my phone and check if there is a child "conversation"
                            mRootReference.child("ads_users").child(mCurrentUserPhone)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("conversation")) {

                                                final Conversation[] c = new Conversation[1];
                                                List<Conversation> listConvo = new ArrayList<>();
                                                final boolean[] isThere = {false};
                                                // final String[] mConvoRef = new String[1];

                                                if (!isFirstTime) {

                                                    DatabaseReference addNewMessage =
                                                            mRootReference.child("ads_chat")
                                                                    .child(mConvoRef)
                                                                    .child("messages").child(push_id);

                                                    mRootReference.child("ads_chat").child(mConvoRef).child("lastMessage")
                                                            .setValue(push_id);

                                                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                        messageMap.put("parent", Messages.getClickedMessageId());
                                                        // Remove if crashed
                                                        replyLinearLayout.setVisibility(View.GONE);
                                                    }

                                                    Map<String, Object> msgContentMap = new HashMap<>();
                                                    msgContentMap.put(message_reference +
                                                            push_id, messageMap);

                                                    mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                                    });

                                                    mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                    mUsersReference.child(mChatPhone).child("conversation")
                                                            .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                    //   mTextToSend.setText("");

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
                                                                        mCurrentUserPhone);
                                                                notificationData.put("message",
                                                                        message);


                                                                /**
                                                                 * Newly added
                                                                 */
                                                /*
                                                mExceptReference.child(mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(!dataSnapshot.hasChild(mCurrentUserPhone)){
                                                            //SendNotification
                                                        }else{
                                                            //Verify timestamp

                                                        }
                                                    }


                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
*/
                                                                mUsersReference.child(mCurrentUserPhone).child("e")
                                                                        .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        if (!dataSnapshot.exists()) {
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
                                                                                                    ChatActivity.this,
                                                                                                    "Notification Sent",
                                                                                                    Toast.LENGTH_SHORT).show();
                                                                                            mTextToSend.requestFocus();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                                //mp1.start();
                                                                //TODO: add sent mark

                                                            });
                                                    mTextToSend.setText("");
//                                                mTextToSend.setFocusableInTouchMode(true);
//                                                mTextToSend.setFocusable(true);
                                                } else {
                                                    mRootReference.child("ads_users")
                                                            .child(mCurrentUserPhone)
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

                                                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                            messageMap.put("parent", Messages.getClickedMessageId());
                                                                        }

                                                                        Map<String, Object> msgContentMap = new HashMap<>();
                                                                        msgContentMap.put(message_reference +
                                                                                push_id, messageMap);

                                                                        mRootReference.updateChildren(msgContentMap,
                                                                                (databaseError, databaseReference) -> {
                                                                                });

                                                                        mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                        mUsersReference.child(mChatPhone).child("conversation")
                                                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                                                                    mTextToSend.setText("");

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
                                                                                            mCurrentUserPhone);
                                                                                    notificationData.put("message",
                                                                                            message);

                                                                                    //TODO: [fm] if user has been muted, don't push notification data
                                                                                    mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                            .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                            if (!dataSnapshot.exists()) {
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
                                                                                                                        ChatActivity.this,
                                                                                                                        "Notification Sent",
                                                                                                                        Toast.LENGTH_SHORT).show();
                                                                                                                mTextToSend.requestFocus();

                                                                                                            }
                                                                                                        });
                                                                                                //mp1.start();
                                                                                                //TODO: add sent mark
                                                                                            }

                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });

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
                                                                        mChatId = conversation_id;
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
                                                                                mCurrentUserPhone);
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

                                                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                            messageMap.put("parent", Messages.getClickedMessageId());
                                                                        }

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

//                                                                    mTextToSend.setText("");
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
                                                                                            mCurrentUserPhone);
                                                                                    notificationData.put("message",
                                                                                            message);

                                                                                    //TODO: [fm] if user has been muted, don't push notification data
                                                                                    mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                            .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                        @Override
                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                            if (!dataSnapshot.exists()) {
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
                                                                                                                        ChatActivity.this,
                                                                                                                        "Notification Sent",
                                                                                                                        Toast.LENGTH_SHORT).show();
                                                                                                                mTextToSend.requestFocus();
                                                                                                            }
                                                                                                        });
                                                                                                //mp1.start();
                                                                                                //TODO: add sent mark
                                                                                            }
                                                                                        }

                                                                                        @Override
                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                        }
                                                                                    });

                                                                                });
                                                                        /////    loadMessages();
                                                                        //Chat.setChatListenerCalled(true);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                    mTextToSend.setText("");
//                                                mTextToSend.setFocusableInTouchMode(true);
//                                                mTextToSend.setFocusable(true);
                                                }
                                            } else {


                                                /**
                                                 * Adding the information under ads_users of my phone and the other user,
                                                 * if the phone number doesn't match the other user
                                                 *
                                                 */

                                                mConvoRef = conversation_id;
                                                mChatId = conversation_id;
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
                                                        mCurrentUserPhone);
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

                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                }

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

//                                            mTextToSend.setText("");
                                                mRootReference.updateChildren(messageUserMap);

                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
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
                                                                    mCurrentUserPhone);
                                                            notificationData.put("message",
                                                                    message);

                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                ChatActivity.this,
                                                                                                "Notification Sent",
                                                                                                Toast.LENGTH_SHORT).show();
                                                                                        mTextToSend.requestFocus();
                                                                                    }
                                                                                });
                                                                        //mp1.start();
                                                                        //TODO: add sent mark
                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });


                                                        });
                                                //       loadMessages();
                                                //   mTextToSend.setText("");
//                                            mTextToSend.setFocusableInTouchMode(true);
//                                            mTextToSend.setFocusable(true);
                                                //Chat.setChatListenerCalled(true);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                        }
                        mTextToSend.setText("");
//                    mTextToSend.setFocusableInTouchMode(true);
//                    mTextToSend.setFocusable(true);
                    } else {
                        Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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


    protected String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    private void askPermission() {
        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
            return;
        }
        RunTimePermissionWrapper.handleRunTimePermission(ChatActivity.this,
                RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
        } else {
            showSnack(ActivityCompat.shouldShowRequestPermissionRationale(this, WALK_THROUGH[0]));
        }
    }

    private void showSnack(final boolean isRationale) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), "Please provide audio Record permission", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(isRationale ? "VIEW" : "Settings", view -> {
            snackbar.dismiss();

            if (isRationale)
                RunTimePermissionWrapper.handleRunTimePermission(ChatActivity.this, RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
            else
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 1001);
        });

        snackbar.show();
    }

    private void updateSeen(Messages m, String chatId) {

        if (m.isSeen()) {
            return;
        }
        if (m.getMessageId() == null) {
            return;
        }
        Log.i("updateSeen", "updating msg_id: " + m.getMessageId() + " conversation ref: " + chatId);
        m.setSeen(true);
        //Update the message in firebase
        mRootReference.child("ads_chat/" + chatId + "/messages/" + m.getMessageId() + "/seen").setValue(true);
        mRootReference.child("ads_messages/" + m.getMessageId() + "/seen").setValue(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.trial_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

//        mCall = menu.getItem(1);
        return true;
    }
//

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {

            case android.R.id.home:
                this.finish();
                return true;


//            case R.id.menu_call:
//                try {
//                    new CheckInternet_(internet -> {
//                        if (internet) {
//                            Map<String, Object> m = new HashMap<>();
//                            m.put("from", mCurrentUserPhone);
//
//                            String voiceChannelId = randomIdentifier();
//                            mCallReference.child(mChatPhone).child(voiceChannelId).setValue(m).addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    Intent goToPhoneCall = new Intent(ChatActivity.this, CallScreenActivity.class);
//                                    goToPhoneCall.putExtra("user_phone", mChatPhone);
//                                    goToPhoneCall.putExtra("channel_id", voiceChannelId);
//                                    startActivity(goToPhoneCall);
//                                }
//                            });
//                        } else {
//                            Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;
//
//            case R.id.menu_video:
//                try {
//                    new CheckInternet_(internet -> {
//                        if (internet) {
//                            DatabaseReference msg_push = mVideoCallReference.child(mChatPhone).push();
//
//                            String push_id = msg_push.getKey();
//
//                            Map<String, Object> m1 = new HashMap<>();
//                            m1.put("from", mCurrentUserPhone);
//
//                            mVideoCallReference.child(mChatPhone).child(push_id).setValue(m1).addOnCompleteListener(task -> {
//
//                                if (task.isSuccessful()) {
//
//                                    Intent goToVideoCall = new Intent(ChatActivity.this, VideoCallActivity.class);
//                                    goToVideoCall.putExtra("user_phone", mChatPhone);
//                                    goToVideoCall.putExtra("channel_id", push_id);
//                                    startActivity(goToVideoCall);
//                                }
//                            });
//                        } else {
//                            Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                break;

            case R.id.menu_send_contact:
                try {
                    new CheckInternet_(internet -> {
                        if (internet) {
                            Intent sendContact = new Intent(this, SendContactActivity.class);
                            sendContact.putExtra("user_phone", mChatPhone);
                            startActivity(sendContact);
                        } else {
                            Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.menu_media:
                try {
                    new CheckInternet_(internet -> {
                        imageSelect();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.menu_live:
                try {
                    new CheckInternet_(internet -> {
                        if (internet) {
                            Map<String, Object> m = new HashMap<>();
                            m.put("from", mCurrentUserPhone);

                            String voiceChannelId = getSaltString();
                            mCallReference.child(mChatPhone).child(voiceChannelId).setValue(m).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Intent goToPhoneCall = new Intent(ChatActivity.this, ReactionActivity.class);
                                    goToPhoneCall.putExtra("userPhone", mChatPhone);
                                    goToPhoneCall.putExtra("user_my_phone", mCurrentUserPhone);
                                    goToPhoneCall.putExtra("channel_id_chat_activity", voiceChannelId);
                                    goToPhoneCall.putExtra("from_me", true);
                                    startActivity(goToPhoneCall);
                                }
                            });
                        } else {
                            Toast.makeText(ChatActivity.this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            default:
                return true;
        }


        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userEntry = newText.toLowerCase();
        List<Messages> newMessages = new ArrayList<>();

        for (Messages m : messagesList) {
            if (m.getType().equals("text")) {
                if (m.getContent().toLowerCase().contains(userEntry)) {
                    newMessages.add(m);
                }
            }
        }

        mMessageAdapter.updateList(newMessages);
        return true;
    }


    private void listenerOnMessage() {

        mMessageReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.i("DatS", dataSnapshot.getKey());
                Messages m = dataSnapshot.getValue(Messages.class);
                if (m == null) {
                    return;
                }

                Log.i("Msg", String.valueOf(m.isVisible()));
                if (!m.isVisible()) {
                    Log.i("MsgV", m.getContent());
                    m.setMessageId(dataSnapshot.getKey());
                    int pos = messagesList.indexOf(m);
                    Log.i("XXA", String.valueOf(pos));
                    if (pos < 0) {
                        return;
                    }
                    messagesList.set(pos, m);
                    mMessageAdapter.notifyDataSetChanged();
                }
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


    @Override
    public void onPositiveClick(int from) {

        switch (from) {
            case 1:
                if (mSelectedMessages.size() > 0) {
                    for (Messages m : mSelectedMessages) {

                        if (!m.getFrom().equals(Objects.requireNonNull
                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                            Toast.makeText(this, "Cannot delete message " +
                                    "not coming from you", Toast.LENGTH_SHORT).show();
                        } else if (m.getType().equals("group_link") || m.getType().equals("channel_link")) {
                            mMessageReference.child(m.getMessageId())
                                    .child("visible").setValue(false);

                            mMessageReference.child(m.getMessageId())
                                    .child("content").setValue("Message Deleted");

                            //  ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);

                            Toast.makeText(this, "Message deleted",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            mMessageReference.child(m.getMessageId())
                                    .child("visible").setValue(false);

                            mMessageReference.child(m.getMessageId())
                                    .child("content").setValue("Message Deleted");

                            // ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);

                            Toast.makeText(this, "Message deleted",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                break;

            case 2:

                for (Messages m : mSelectedMessages) {
                    if (m.getType().equals("audio") || m.getType().equals("video") || m.getType().equals("image")) {
                        Toast.makeText(this, "Can only copy text messages", Toast.LENGTH_SHORT).show();
                        mActionMode.finish();
                        return;
                    }
                    if (m.getFrom().equals(mCurrentUserPhone)) {
                        mFinalCopiedMessages = mFinalCopiedMessages + "[" + getDate(m.getTimestamp())
                                + "]" + "You: " + m.getContent() + "\n";
                    } else {
                        String nameStored = Users.getLocalContactList().get(m.getFrom());
                        nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                m.getFrom();
                        mFinalCopiedMessages = mFinalCopiedMessages + "[" + getDate(m.getTimestamp())
                                + "]" + nameStored + ": " + m.getContent() + "\n";
                    }
                }

                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("simpleText", mFinalCopiedMessages);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                }

                if (mActionMode != null) {
                    mActionMode.finish();
                }
                break;

            case 3:

                Intent intent = new Intent(this, ForwardListMessageActivity.class);
                intent.putExtra("messagesList", (Serializable) mSelectedMessages);
                startActivity(intent);

                break;

            default:
                return;
        }
    }

    @Override
    public void onNegativeClick(int from) {
    }

    @Override
    public void onNeutralClick(int from) {
    }

    public void multi_select(int position) {
        final Messages m = messagesList.get(position);
        if (!m.isVisible()) {
            Toast.makeText(ChatActivity.this, "Can't be selected", Toast.LENGTH_SHORT).show();
        } else {
            if (mActionMode != null) {
                if (mSelectedMessages.contains(messagesList.get(position))) {
                    mSelectedMessages.remove(messagesList.get(position));
                } else {
                    mSelectedMessages.add(messagesList.get(position));
                }

                if (mSelectedMessages.size() > 0) {
                    mActionMode.setTitle("" + mSelectedMessages.size());
                } else {
                    return;
                }
                refreshAdapter();

            }
        }

    }


    public void refreshAdapter() {
        mMessageAdapter.mSelectedMessagesList = mSelectedMessages;
        mMessageAdapter.mMessagesList = messagesList;
        mMessageAdapter.notifyDataSetChanged();
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            //    mChatToolbar.setVisibility(View.GONE);
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_multi_select, menu);
            // mContextMenu = menu;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    mAlertDialogHelper.showAlertDialog("",
                            "Delete Messages?", "DELETE", "CANCEL",
                            1, false);
                    return true;

                case R.id.action_copy:
                    mAlertDialogHelper.showAlertDialog("",
                            "Copy Messages?", "YES", "NO", 2, false);
                    return true;

                case R.id.action_forward:
                    mAlertDialogHelper.showAlertDialog("", "Forward Messages?", "YES", "NO"
                            , 3, false);
                    return true;


                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            isMultiSelect = false;
            mSelectedMessages = new ArrayList<>();
            refreshAdapter();
            mChatToolbar.setVisibility(View.VISIBLE);
        }
    };

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

    private TextWatcher textWatcher = new TextWatcher() {

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if (TextUtils.isEmpty(mTextToSend.getText().toString().trim())) {
                mSendVoice.setImageResource(R.drawable.mic);
                mSendVoice.setTag("sendAudio");
            } else {
                mSendVoice.setImageResource(R.drawable.ic_send);
                mSendVoice.setTag("sendMessage");
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

    @Override
    public void onRecordClick() {

    }

    @Override
    public void onFinishRecording(File file) {

    }

    @Override
    public void onSendClick(File file, int duration) {
        sendAudio(file);
        findViewById(R.id.fragment_contaainer).setVisibility(View.GONE);
        remove(fragment);
        linearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCloseClick() {
        findViewById(R.id.fragment_contaainer).setVisibility(View.GONE);
        remove(fragment);
        linearLayout.setVisibility(View.VISIBLE);
    }

     class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


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
            return filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            Toast.makeText(ChatActivity.this, String.valueOf(length), Toast.LENGTH_SHORT).show();

            Bitmap bMap = null;
            try{
               bMap = ThumbnailUtils.createVideoThumbnail(compressedFilePath, MediaStore.Video.Thumbnails.MINI_KIND);
            }catch (Exception e){
                e.printStackTrace();
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Objects.requireNonNull(bMap).compress(Bitmap.CompressFormat.JPEG, 40, baos);
            final byte[] thum_byte = baos.toByteArray();

            Uri uri = Uri.fromFile(imageFile);

            String myReference = "ads_users/" + mCurrentUserPhone + "/" + "conversation/";
            String otherUserReference = "ads_users/" + mChatPhone + "/" + "conversation/";

            String chat_reference = "ads_chat/";

            DatabaseReference conversation_push = mRootReference.child("ads_users")
                    .child(mCurrentUserPhone).push();
            String conversation_id = conversation_push.getKey();

            final String message_reference = "ads_messages/";

            DatabaseReference msg_push = mRootReference.child("ads_messages").push();

            String push_id = msg_push.getKey();
//
            StorageReference filePath = mVideosStorage.child("messages_videos").child(push_id + ".mp4");
            StorageReference videoThumb = mVideosStorage.child("messages_videos").child(push_id + ".jpg");
            UploadTask uploadTask = filePath.putFile(uri);
            mUploadLayout.setVisibility(View.VISIBLE);
            uploadTask.addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                     mFilePath.setText(push_id+".mp4");
                    UploadTask uploadTask1 = videoThumb.putBytes(thum_byte);
                    uploadTask1.addOnCompleteListener(task1 -> {
                        String thum_url = task1.getResult().getDownloadUrl().toString();

                    String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();
                    mRootReference.child("ads_users").child(mCurrentUserPhone)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("conversation")) {

                                        final Conversation[] c = new Conversation[1];
                                        List<Conversation> listConvo = new ArrayList<>();
                                        final boolean[] isThere = {false};
                                        //       final String[] mConvoRef = new String[1];

                                        if (!isFirstTime) {

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
                                            messageMap.put("parent", "Default");
                                            messageMap.put("visible", true);
                                            messageMap.put("from", mCurrentUserPhone);
                                            messageMap.put("seen", false);
                                            messageMap.put("edited", false);
                                            messageMap.put("thumb", thum_url);

                                            if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                messageMap.put("parent", Messages.getClickedMessageId());
                                                // Remove if crashed
                                                replyLinearLayout.setVisibility(View.GONE);
                                            }

                                            Map<String, Object> msgContentMap = new HashMap<>();
                                            msgContentMap.put(message_reference +
                                                    push_id, messageMap);

                                            mRootReference.updateChildren(msgContentMap,
                                                    (databaseError, databaseReference) -> {
                                                    });


                                            mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                            mUsersReference.child(mChatPhone).child("conversation")
                                                    .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                                            mTextToSend.setText("");

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
                                                                mCurrentUserPhone);
                                                        notificationData.put("message",
                                                                downloadUrl);

                                                        //TODO: [fm] if user has been muted, don't push notification data
                                                        mUsersReference.child(mCurrentUserPhone).child("e")
                                                                .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (!dataSnapshot.exists()) {
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
                                                                                            ChatActivity.this,
                                                                                            "Notification Sent",
                                                                                            Toast.LENGTH_SHORT).show();

                                                                                }
                                                                            });
                                                                    //mp1.start();
                                                                    //TODO: add sent mark
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    });

                                        } else {
                                            mRootReference.child("ads_users")
                                                    .child(mCurrentUserPhone)
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
                                                                messageMap.put("parent", "Default");
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mCurrentUserPhone);
                                                                messageMap.put("seen", false);
                                                                messageMap.put("edited", false);
                                                                messageMap.put("thumb", thum_url);


                                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                                    // Remove if crashed
                                                                    replyLinearLayout.setVisibility(View.GONE);
                                                                }

                                                                Map<String, Object> msgContentMap = new HashMap<>();
                                                                msgContentMap.put(message_reference +
                                                                        push_id, messageMap);

                                                                mRootReference.updateChildren(msgContentMap,
                                                                        (databaseError, databaseReference) -> {
                                                                        });


                                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                                                mUsersReference.child(mChatPhone).child("conversation")
                                                                        .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                                                                mTextToSend.setText("");

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
                                                                                    mCurrentUserPhone);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                                ChatActivity.this,
                                                                                                                "Notification Sent",
                                                                                                                Toast.LENGTH_SHORT).show();

                                                                                                    }
                                                                                                });
                                                                                        //mp1.start();
                                                                                        //TODO: add sent mark
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });


                                                                        });
                                                            } else {

                                                                /**
                                                                 * Adding the information under ads_users
                                                                 */

                                                                mConvoRef = conversation_id;
                                                                mChatId = conversation_id;
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
                                                                        mCurrentUserPhone);
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
                                                                messageMap.put("parent", "Default");
                                                                messageMap.put("visible", true);
                                                                messageMap.put("from", mCurrentUserPhone);
                                                                messageMap.put("seen", false);
                                                                messageMap.put("edited", false);
                                                                messageMap.put("thumb", thum_url);

                                                                if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                                                    messageMap.put("parent", Messages.getClickedMessageId());
                                                                    // Remove if crashed
                                                                    replyLinearLayout.setVisibility(View.GONE);
                                                                }

                                                                mUsersReference.child(mCurrentUserPhone).child("conversation")
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

//                                                                mTextToSend.setText("");
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
                                                                                    mCurrentUserPhone);
                                                                            notificationData.put("message",
                                                                                    downloadUrl);

                                                                            //TODO: [fm] if user has been muted, don't push notification data
                                                                            mUsersReference.child(mCurrentUserPhone).child("e")
                                                                                    .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    if (!dataSnapshot.exists()) {
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
                                                                                                                ChatActivity.this,
                                                                                                                "Notification Sent",
                                                                                                                Toast.LENGTH_SHORT).show();

                                                                                                    }
                                                                                                });
                                                                                        //mp1.start();
                                                                                        //TODO: add sent mark
                                                                                    }
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });


                                                                        });
                                                                loadMessages();
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
                                        mChatId = conversation_id;
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
                                                mCurrentUserPhone);
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
                                        messageMap.put("parent", "Default");
                                        messageMap.put("visible", true);
                                        messageMap.put("from", mCurrentUserPhone);
                                        messageMap.put("seen", false);
                                        messageMap.put("edited", false);
                                        messageMap.put("thumb", thum_url);


                                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                                            messageMap.put("parent", Messages.getClickedMessageId());
                                            // Remove if crashed
                                            replyLinearLayout.setVisibility(View.GONE);
                                        }

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

                                        mUsersReference.child(mCurrentUserPhone).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);

                                        mUsersReference.child(mChatPhone).child("conversation")
                                                .child(mConvoRef).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                                        mTextToSend.setText("");
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
                                                            mCurrentUserPhone);
                                                    notificationData.put("message",
                                                            downloadUrl);

                                                    //TODO: [fm] if user has been muted, don't push notification data
                                                    mUsersReference.child(mCurrentUserPhone).child("e")
                                                            .child("U-" + mChatPhone).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (!dataSnapshot.exists()) {
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
                                                                                        ChatActivity.this,
                                                                                        "Notification Sent",
                                                                                        Toast.LENGTH_SHORT).show();

                                                                            }
                                                                        });
                                                                //mp1.start();
                                                                //TODO: add sent mark
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                });
                                        loadMessages();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                });
                    mUploadLayout.setVisibility(View.GONE);
                }

            })
                    .addOnFailureListener(e ->{
                        Toast.makeText(ChatActivity.this, "Errata", Toast.LENGTH_SHORT).show();
                        mUploadLayout.setVisibility(View.GONE);
                    })
                    .addOnProgressListener(taskSnapshot -> {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        mProgress.setProgress((int)progress);
                        String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                        mFileSize.setText(progressText);
                        mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                    });

        }
    }

    private void setUnreadMessageCount(String conversationId, int count) {
        Log.i("UNREAD_MSG", "CALLED");
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putInt(conversationId, count);
        editor.apply();
    }


    private void setLocale(String lang) {
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
    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(mRootView)
                .setOnEmojiBackspaceClickListener(ignore -> Log.d("ChatActivity", "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d("ChatActivity", "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> mSendEmoji.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d("ChatActivity", "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> mSendEmoji.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> Log.d("ChatActivity", "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_slide_animation_style)
                //   .setPageTransformer(new RotateUpTransformer())
                .build(mTextToSend);
    }


    ItemTouchHelper.SimpleCallback mItemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            testingFlag = actionState == ItemTouchHelper.ACTION_STATE_SWIPE;
            if (!messagesList.get(viewHolder.getAdapterPosition()).isVisible()) {
                return;
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX / 12, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //    mMessagesList.removeOnItemTouchListener(recyclerItemClickListener);
            messageLinLayout = findViewById(R.id.messageLinLayout);
            LinearLayout replyLayout = findViewById(R.id.replyLinearLayout);
            if (!messagesList.get(viewHolder.getAdapterPosition()).isVisible()) {
                Toast.makeText(ChatActivity.this, "", Toast.LENGTH_SHORT).show();
            } else {
                String nameStored = Users.getLocalContactList().get(messagesList.get(viewHolder.getAdapterPosition()).getFrom());

                senderOfMessage.setText(messagesList.get(viewHolder.getAdapterPosition()).getFrom().equals(mCurrentUserPhone) ? getString(R.string.you) :
                        nameStored != null && nameStored.length() > 0 ? nameStored : messagesList.get(viewHolder.getAdapterPosition()).getFrom());


                Messages.setClickedMessageId(messagesList.get(viewHolder.getAdapterPosition()).getMessageId());

                switch (messagesList.get(viewHolder.getAdapterPosition()).getType()) {
                    case "audio":

                        messageReceived.setVisibility(View.GONE);
                        imageSent.setVisibility(View.GONE);
                        audioSent.setVisibility(View.VISIBLE);
                        videoSent.setVisibility(View.GONE);
                        documentSent.setVisibility(View.GONE);

                        MediaMetadataRetriever mmr;

                        audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                0, 0, 0);
                        audioSent.setCompoundDrawablePadding(30);

                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                        replyLayout.setVisibility(View.VISIBLE);
                        try {

                            mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(messagesList.get(viewHolder.getAdapterPosition()).getContent());
                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            Toast.makeText(ChatActivity.this, duration, Toast.LENGTH_SHORT).show();
                            audioSent.setText(formatTimeOfAudio(duration));
                            mmr.release();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "image":
                        messageReceived.setVisibility(View.GONE);
                        imageSent.setVisibility(View.VISIBLE);
                        audioSent.setVisibility(View.GONE);
                        videoSent.setVisibility(View.GONE);
                        documentSent.setVisibility(View.GONE);

                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                        replyLayout.setVisibility(View.VISIBLE);

                        Picasso.get().load(messagesList.get(viewHolder.getAdapterPosition()).getContent()).placeholder(R.drawable.ic_avatar)
                                .into(imageSent);
                        break;
                    case "video":
                        messageReceived.setVisibility(View.GONE);
                        imageSent.setVisibility(View.GONE);
                        audioSent.setVisibility(View.GONE);
                        videoSent.setVisibility(View.VISIBLE);
                        documentSent.setVisibility(View.GONE);

                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                        replyLayout.setVisibility(View.VISIBLE);

                        videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                0, 0, 0);
                        videoSent.setCompoundDrawablePadding(30);
                        videoSent.setText(R.string.v);

                        break;

                    case "document":
                        audioSent.setVisibility(View.GONE);
                        imageSent.setVisibility(View.GONE);
                        documentSent.setVisibility(View.VISIBLE);
                        videoSent.setVisibility(View.GONE);

                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                        replyLayout.setVisibility(View.VISIBLE);


                        documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                0, 0, 0);
                        documentSent.setCompoundDrawablePadding(30);
                        documentSent.setText(R.string.d);

                        break;

                    case "text":
                    case "group_link":
                    case "channel_link":
                        //case "contact":

                        messageReceived.setVisibility(View.VISIBLE);
                        imageSent.setVisibility(View.GONE);
                        audioSent.setVisibility(View.GONE);
                        videoSent.setVisibility(View.GONE);
                        documentSent.setVisibility(View.GONE);

                        messageReceived.setText(messagesList.get(viewHolder.getAdapterPosition()).getContent());

                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                        replyLayout.setVisibility(View.VISIBLE);

                        if (senderOfMessage.getText().toString().trim().equals("You")) {
                            senderOfMessage.setTextColor(Color.parseColor("#20BF9F"));
                        } else {
                            senderOfMessage.setTextColor(Color.parseColor("#FF4500"));
                        }

                        break;
                }

                close_reply.setOnClickListener(view12 -> {
                    testingFlag = !testingFlag;
                    messageLinLayout.setBackgroundResource(R.drawable.border);
                    replyLayout.setVisibility(View.GONE);
                });
                mMessageAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
            }
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.1f;
        }
    };

    public String formatTimeOfAudio(String duration) {

        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(duration)),
                TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(duration)) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(duration))
                        ));
    }

    public void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        if (fragment.isAdded())
            fragmentTransaction.show(fragment);
        else
            fragmentTransaction.add(R.id.fragment_contaainer, fragment, "h").addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void remove(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment.isAdded())
            fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    public void record() {
        if (getMicrophoneAvailable()) {
            findViewById(R.id.fragment_contaainer).setVisibility(View.VISIBLE);
            showFragment(fragment);
        } else
            Toast.makeText(this, "Microphone not available...", Toast.LENGTH_SHORT).show();
    }

    //returns whether the microphone is available
    public static boolean getMicrophoneAvailable() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        recorder.setOutputFile(new File(context.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
        recorder.setOutputFile(mFileName);
        boolean available = true;
        try {
            recorder.prepare();
            recorder.start();

        } catch (Exception exception) {
            available = false;
        }
        recorder.release();
        return available;
    }

    public void imageSelect() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.sel_pic)),
                PICK_IMAGE_REQUEST);
    }

    private void getImageBackground() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String mImageUri = preferences.getString("image", null);
        if (mImageUri != null) {
            Picasso.get().load(Uri.parse(mImageUri)).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mRootView.setBackground(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.d("TAG", "FAILED");

                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    Log.d("TAG", "Prepare Load");

                }
            });
        }
    }

    public void provideCorrectUI(){
        if(editModeIsOn){
            recycler_layout.setAlpha(0.8f);
            recycler_layout.setBackgroundColor(ContextCompat.getColor(ChatActivity.this, R.color.colorPrimary));
            mMessagesList.removeOnItemTouchListener(recyclerItemClickListener);
            mCloseEditMode.setVisibility(View.VISIBLE);
            mSendAttachment.setVisibility(View.GONE);
            mTextToSend.removeTextChangedListener(textWatcher);
            mSendVoice.setImageResource(R.drawable.ic_send);
            mMessagesList.setEnabled(false);
            mSendVoice.setTag("sendMessage");
        }else{
            recycler_layout.setAlpha(1f);
            recycler_layout.setBackgroundColor(Color.TRANSPARENT);
            mMessagesList.addOnItemTouchListener(recyclerItemClickListener);
            mCloseEditMode.setVisibility(View.GONE);
            mTextToSend.setText("");
            mSendAttachment.setVisibility(View.VISIBLE);
            mTextToSend.addTextChangedListener(textWatcher);
            mSendVoice.setImageResource(R.drawable.mic);
            mMessagesList.setEnabled(true);
            mSendVoice.setTag("sendAudio");

        }
    }
}