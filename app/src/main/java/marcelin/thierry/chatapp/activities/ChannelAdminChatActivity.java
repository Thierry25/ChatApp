package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.islamassem.voicemessager.VoiceMessagerFragment;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.ChannelInteractionAdapter;
import marcelin.thierry.chatapp.adapters.NotificationAdapter;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.NotificationDropDownMenu;
import marcelin.thierry.chatapp.classes.ReplyNotification;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.CheckInternet_;
import marcelin.thierry.chatapp.utils.RecyclerItemClickListener;
import pl.droidsonroids.gif.GifImageView;

import static marcelin.thierry.chatapp.activities.MainActivity.getDateDiff;

public class ChannelAdminChatActivity extends AppCompatActivity implements VoiceMessagerFragment.OnControllerClick {

    private static final String[] WALK_THROUGH = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private String mChannelName;
    private String mChannelId;
    private String mChannelImage;
    private ArrayList<String> mChannelAdmins;


    private Map<String, Object> mRead = new HashMap<>();
    private Map<String, Object> likesMap = new HashMap<>();
    private Map<String, Object> commentsMap = new HashMap<>();
    private Map<String, Object> repliesMap = new HashMap<>();

    private String mCurrentUserPhone;
    private static String mFileName = null;
    //  private static final String LOG_TAG = "AudioRecordTest";

    //  private int itemPosition = 0;
    private static final int GALLERY_PICK = 1;
    private static final int MAX_ATTACHMENT_COUNT = 1;
    private static final int TOTAL_ITEMS_TO_LOAD = 30;
//    private int seen = 0;


    private List<String> mImagesPath;
    private List<String> mVideosPath;
    private List<Uri> mImagesData;
    private List<Uri> mVideosData;
    private ArrayList<String> mDocPath = new ArrayList<>();
    private List<Messages> messagesList = new ArrayList<>();

    private List<ReplyNotification> replyNotificationsList = new ArrayList<>();

    private View mRootView;
    private CircleImageView mProfileImage;
    private RecyclerView mMessagesList;

    // to changef
    private ChannelInteractionAdapter mChannelInteractionAdapter;
    private NotificationAdapter mNotificationAdapter;

    //private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLinearLayoutManager;

    private Dialog mDialog, questionDialog, addDescriptionDialog;

    private EmojiEditText mTextToSend;
    private EmojiPopup emojiPopup;

    private ImageButton mSendEmoji;
    private ImageButton mSendAttachment;
    private MediaPlayer mp1;
    private MediaRecorder mRecorder;
    private Toolbar mChatToolbar, mToolBar;
    private ImageView mSendVoice;
    private ImageView notification_bell;
    private TextView unseenReplies;

    private static final StorageReference mImagesStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mVideosStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mAudioStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mDocumentsStorage = FirebaseStorage.getInstance().getReference();


    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mNotificationsDatabase = FirebaseDatabase.getInstance()
            .getReference().child("ads_notifications");
    private static final DatabaseReference mMessageReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel_messages");
    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    private NestedScrollView nested;

    private TextView title;
    private CircleImageView profileImage;
    private ImageView backButton;

    private Fragment fragment;
    private LinearLayout linearLayout, recycler_layout;

    // Newly Added for adding color to background
    private ImageView selectColor, mCloseEditMode;
    private HorizontalScrollView horizontalScrollView;
    private ImageView whiteBg, grayBg, greenBg, pinkBg, orangeBg, orangeRedBg, blackBg,
            purpleBg, redBg, tealBg, philBg, brownBg, pinkFadeBg, lightPurpleBg, lDarkBlueBg,
            redLightBg;

    private int colorToUpdate = 0;
    private int tag = 0;

    private RecyclerItemClickListener recyclerItemClickListener;

    private LinearLayout mUploadLayout;
    private TextView mFilePath, mFileSize, mFilePercentage;
    private ProgressBar mProgress;


    private String[] colorList = new String[]{
            "#FFFFFF",
            "#808080",
            "#00574B",
            "#D81B60",
            "#FFA500",
            "#FF4500",
            "#000000",
            "#4B0082",
            "#B22222",
            "#008080",
            "#ffeedd",
            "#e5b895",
            "#ffaabb",
            "#9b8dc2",
            "#6a7a94",
            "#ee134a"
    };

    private boolean isShown = false;

    private boolean editModeIsOn = false;
    private String clickedMessageId = "";
    private Long clickedMessageTimeStamp = 0L;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_chat);

        // Values to change
        Intent i = getIntent();
        mChannelName = i.getStringExtra("Channel_id");

        mChannelId = i.getStringExtra("chat_id");

        mChannelImage = i.getStringExtra("profile_image");

        mChannelAdmins = i.getStringArrayListExtra("admins");

        mChatToolbar = findViewById(R.id.chat_bar_main);
        mChatToolbar.setVisibility(View.GONE);

        mToolBar = findViewById(R.id.toolbar);
        mToolBar.setVisibility(View.VISIBLE);
        setSupportActionBar(mToolBar);
        //  mSend = findViewById(R.id.send);
        fragment = VoiceMessagerFragment.build(this, true);

        // Saving the URI returned by the video and image library
        mImagesData = new ArrayList<>();
        mVideosData = new ArrayList<>();

        // Data for audio
        mFileName = Objects.requireNonNull(getExternalCacheDir()).getAbsolutePath();
        mFileName += "/" + randomIdentifier() + ".3gp";

        askPermission();

        mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        mProfileImage = mToolBar.findViewById(R.id.profileImage);
        //mProfileName = findViewById(R.id.chat_bar_name);

        mTextToSend = findViewById(R.id.send_text);
        mTextToSend.addTextChangedListener(textWatcher);

        mMessagesList = findViewById(R.id.messages_list);
        nested = findViewById(R.id.swipe_layout);
        nested.postDelayed(() -> {
            // listener.setAppBarExpanded(false, true); //appbar.setExpanded(expanded, animated);
            nested.fling(0);
            nested.fullScroll(View.FOCUS_DOWN);
        }, 200);

        title = mToolBar.findViewById(R.id.title);

        //actionBar.setTitle(mChannelId);
        title.setText(mChannelId);
        linearLayout = findViewById(R.id.linearLayout);
        recycler_layout = findViewById(R.id.recycler_layout);

        mLinearLayoutManager = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);
        mMessagesList.setNestedScrollingEnabled(false);

        ViewCompat.setNestedScrollingEnabled(nested, true);

        mChannelInteractionAdapter = new ChannelInteractionAdapter(messagesList, this, this);
        mNotificationAdapter = new NotificationAdapter(this, replyNotificationsList);
        mMessagesList.setAdapter(mChannelInteractionAdapter);

        mp1 = MediaPlayer.create(ChannelAdminChatActivity.this, R.raw.playsound);
        mSendEmoji = findViewById(R.id.send_emoji);
        mSendAttachment = findViewById(R.id.send_attachment);

        mDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        questionDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        addDescriptionDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        mRootView = findViewById(R.id.rootView);
        mRootView.setBackgroundColor(ContextCompat.getColor(this, R.color.channel_background));
        mSendVoice = findViewById(R.id.send_voice);

        selectColor = findViewById(R.id.selectColor);
        selectColor.setVisibility(View.VISIBLE);
        notification_bell = mToolBar.findViewById(R.id.notification_bell);
        unseenReplies = findViewById(R.id.unseenReplies);

        mUploadLayout = findViewById(R.id.uploadLayout);
        mFilePath = findViewById(R.id.filePath);
        mFileSize = findViewById(R.id.fileSize);
        mFilePercentage = findViewById(R.id.filePercentage);
        mProgress = findViewById(R.id.progress);

        loadMessages();
        getComments();
//
        Picasso.get().load(mChannelImage).placeholder(R.drawable.ic_avatar).into(mProfileImage);

        mToolBar.setOnClickListener(view13 -> {

            // TODO: Handle view of new layout

            Intent intent = new Intent(ChannelAdminChatActivity.this,
                    SettingsChannelActivity.class);
            intent.putExtra("Channel_id", mChannelName);
            intent.putExtra("chat_id", mChannelId);
            intent.putExtra("profille_image", mChannelImage);

            startActivity(intent);
            finish();

        });

        listenerOnMessage();

        mSendAttachment.setOnClickListener(view1 -> {
            try {
                new CheckInternet_(internet -> {
                    if (internet) {
                        showCustomDialog();
                    } else {
                        Toast.makeText(ChannelAdminChatActivity.this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        backButton = mToolBar.findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        mCloseEditMode = findViewById(R.id.closeEditMode);

        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        whiteBg = findViewById(R.id.whiteBg);
        grayBg = findViewById(R.id.grayBg);
        greenBg = findViewById(R.id.greenBg);
        pinkBg = findViewById(R.id.pinkBg);
        orangeBg = findViewById(R.id.orangeBg);
        orangeRedBg = findViewById(R.id.orangeRedBg);
        blackBg = findViewById(R.id.blackBg);
        purpleBg = findViewById(R.id.purpleBg);
        redBg = findViewById(R.id.redBg);
        tealBg = findViewById(R.id.tealBg);
        philBg = findViewById(R.id.philBg);
        brownBg = findViewById(R.id.brownBg);
        pinkFadeBg = findViewById(R.id.pinkFadeBg);
        lightPurpleBg = findViewById(R.id.lightPurpleBg);
        lDarkBlueBg = findViewById(R.id.lDarkBlueBg);
        redLightBg = findViewById(R.id.redLightBg);

        whiteBg.setOnClickListener(v -> {
            colorToUpdate = 0;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.white);
        });
        grayBg.setOnClickListener(v -> {
            colorToUpdate = 1;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorGray);
        });
        greenBg.setOnClickListener(v -> {
            colorToUpdate = 2;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorDarkGreen);
        });
        pinkBg.setOnClickListener(v -> {
            colorToUpdate = 3;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorPink);

        });
        orangeBg.setOnClickListener(v -> {
            colorToUpdate = 4;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorOrange);

        });
        orangeRedBg.setOnClickListener(v -> {
            colorToUpdate = 5;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorOrangered);

        });
        blackBg.setOnClickListener(v -> {
            colorToUpdate = 6;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorBlack);

        });

        purpleBg.setOnClickListener(v -> {
            colorToUpdate = 7;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorPurple);

        });
        redBg.setOnClickListener(v -> {
            colorToUpdate = 8;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorRed);

        });
        tealBg.setOnClickListener(v -> {
            colorToUpdate = 9;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorTeal);

        });
        philBg.setOnClickListener(v -> {
            colorToUpdate = 10;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorPhil);

        });
        brownBg.setOnClickListener(v -> {
            colorToUpdate = 11;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorBrown);

        });
        pinkFadeBg.setOnClickListener(v -> {
            colorToUpdate = 12;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorPinkFade);

        });
        lightPurpleBg.setOnClickListener(v -> {
            colorToUpdate = 13;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorLightPurple);

        });
        lDarkBlueBg.setOnClickListener(v -> {
            colorToUpdate = 14;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorLDarkBlue);

        });
        redLightBg.setOnClickListener(v -> {
            colorToUpdate = 15;
            horizontalScrollView.setVisibility(View.GONE);
            selectColor.setImageResource(R.color.colorRedLight);

        });

        mSendEmoji.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        mSendEmoji.setOnClickListener(ignore -> emojiPopup.toggle());

        setUpEmojiPopup();

        mSendVoice.setOnClickListener(v -> {
            if (mSendVoice.getTag().equals("sendAudio")) {
                linearLayout.setVisibility(View.GONE);
                record();
            } else {
                sendMessage(mTextToSend);
            }
        });


        selectColor.setOnClickListener(v -> {
            isShown = !isShown;
            if (isShown) {
                horizontalScrollView.setVisibility(View.VISIBLE);
            } else {
                horizontalScrollView.setVisibility(View.GONE);
            }
        });

        // TODO: Notification
        notification_bell.setVisibility(View.VISIBLE);

        notification_bell.setOnClickListener(v -> {
            if (unseenReplies.getVisibility() == View.VISIBLE) {
                showNotifications();
            } else {
                Toast.makeText(this, R.string.no_notif, Toast.LENGTH_SHORT).show();
            }
        });

        mRootView.setBackgroundColor(Color.parseColor("#ececec"));

        recyclerItemClickListener = new RecyclerItemClickListener(this, mMessagesList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Messages message = messagesList.get(position);
                if (message.isVisible() && message.getType().equals("text")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.admin_channel_options, (dialog, which) -> {
                                switch (which) {
                                    case 0:
                                        Intent goToCommentActivity = new Intent(ChannelAdminChatActivity.this, CommentActivity.class);
                                        goToCommentActivity.putExtra("channel_name", message.getChannelName());
                                        goToCommentActivity.putExtra("channel_image", message.getChannelImage());
                                        goToCommentActivity.putExtra("message_type", message.getType());
                                        goToCommentActivity.putExtra("message_id", message.getMessageId());
                                        goToCommentActivity.putExtra("message_content", message.getContent());
                                        goToCommentActivity.putExtra("message_timestamp", message.getTimestamp());
                                        goToCommentActivity.putExtra("message_color", message.getColor());
                                        goToCommentActivity.putExtra("message_like", message.getL().size());
                                        goToCommentActivity.putExtra("message_comment", message.getC().size());
                                        goToCommentActivity.putExtra("message_seen", message.getRead_by().size());
                                        goToCommentActivity.putExtra("message_edited", message.isEdited());
                                        goToCommentActivity.putExtra("from", "Adapter");
                                        goToCommentActivity.putExtra("isOn", false);
                                        startActivity(goToCommentActivity);

                                        break;

                                    case 1:
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
                                    case 2:
                                        if (!message.getFrom().equals(Objects.requireNonNull
                                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                                            // TODO: Verify
                                            Toast.makeText(view.getContext(), R.string.not_admin, Toast.LENGTH_SHORT).show();
                                        } else {
                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");

                                            //((MessageViewHolder) holder).messageLinearLayout.setVisibility(View.GONE);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;

                                    case 3:

                                        long millis = System.currentTimeMillis();
                                        long minutes = 1200000L;

                                        long result = getDateDiff(message.getTimestamp(), millis, TimeUnit.MILLISECONDS);
                                        if (result < minutes) {
                                            if (message.getFrom().equals(mCurrentUserPhone)) {
                                                if (message.getType().equals("text")) {
                                                    Toast.makeText(ChannelAdminChatActivity.this, R.string.edit_mode, Toast.LENGTH_SHORT).show();

                                                    editModeIsOn = true;
                                                    provideCorrectUI();
                                                    clickedMessageId = message.getMessageId();
                                                    clickedMessageTimeStamp = message.getTimestamp();
                                                    mTextToSend.setText(message.getContent());
                                                } else {
                                                    Toast.makeText(ChannelAdminChatActivity.this, "Cannot be edited", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Toast.makeText(ChannelAdminChatActivity.this, R.string.c_sent_by_you, Toast.LENGTH_SHORT).show();
                                            }

//                                                mMessagesList.removeOnItemTouchListener(recyclerItemClickListener);
                                        } else {
                                            Toast.makeText(ChannelAdminChatActivity.this, R.string.edit_limit, Toast.LENGTH_SHORT).show();
                                        }
                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mMessagesList.addOnItemTouchListener(recyclerItemClickListener);
        mCloseEditMode.setOnClickListener(v -> {
            editModeIsOn = false;
            provideCorrectUI();
            Toast.makeText(this, "Edit mode off", Toast.LENGTH_SHORT).show();
        });
    }

    private void getComments() {
        mUsersReference.child(mCurrentUserPhone).child("r").child(mChannelName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()) {
                    ReplyNotification replyNotification = dataSnapshot.getValue(ReplyNotification.class);
                    if (replyNotification == null) {
                        return;
                    }
                    //      if(!replyNotification.getRe().equals(mCurrentUserPhone)){
                    if (!replyNotification.isSe()) {
                        unseenReplies.setVisibility(View.VISIBLE);
                        mUsersReference.child(replyNotification.getRe()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Users u = dataSnapshot.getValue(Users.class);
                                if (u == null) {
                                    return;
                                }
                                // TODO :VERIFYYY
                                if (!u.getPhone().equals(mCurrentUserPhone)) {
                                    replyNotification.setReplyImage(u.getThumbnail());
                                    replyNotification.setReplierName(u.getName());
                                    replyNotificationsList.add(replyNotification);
                                    mNotificationAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    //    }
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
    }

    private void showNotifications() {
        final NotificationDropDownMenu menu = new NotificationDropDownMenu(this, replyNotificationsList);
        menu.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        menu.setWidth(getPxFromDp());
        menu.setOutsideTouchable(true);
        menu.setFocusable(true);
        menu.showAsDropDown(notification_bell);
        //menu.setAnimationStyle(R.style.emoji_fade_animation_style);
        menu.setNotificationSelectedListener((position, replyNotification) -> {
            menu.dismiss();
            // New Intent to CommentActivity
            mUsersReference.child(mCurrentUserPhone).child("r").child(mChannelName).child(replyNotification.getR()).child("se").setValue(true);
            Intent goToCommentActivity = new Intent(ChannelAdminChatActivity.this, CommentActivity.class);
            goToCommentActivity.putExtra("channel_name", replyNotification.getCh());
            goToCommentActivity.putExtra("channel_image", replyNotification.getChi());
            goToCommentActivity.putExtra("message_type", replyNotification.getTy());
            goToCommentActivity.putExtra("message_id", replyNotification.getId());
            goToCommentActivity.putExtra("message_color", replyNotification.getCol());
            goToCommentActivity.putExtra("message_content", replyNotification.getCo());
            goToCommentActivity.putExtra("message_timestamp", replyNotification.getT2());
            goToCommentActivity.putExtra("message_like", replyNotification.getL());
            goToCommentActivity.putExtra("message_comment", replyNotification.getCc());
            goToCommentActivity.putExtra("message_seen", replyNotification.getS());
            goToCommentActivity.putExtra("reply_id", replyNotification.getR());
            goToCommentActivity.putExtra("from", "Activity");
            goToCommentActivity.putExtra("isOn", true);
            goToCommentActivity.putExtra("comment_id", replyNotification.getC());
            // goToCommentActivity.putExtra("message_color", replyNotification.getCol());
            startActivity(goToCommentActivity);

        });
    }

    private int getPxFromDp() {
        return (int) (350 * getResources().getDisplayMetrics().density);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void sendAudio(File file) {

        try {
            new CheckInternet_(internet -> {
                if (internet) {
                    final String message_reference = "ads_channel_messages/";

                    DatabaseReference msg_push = mRootReference.child("ads_channel").child(mChannelName).push();

                    String push_id = msg_push.getKey();

                    DatabaseReference usersInGroup = mRootReference.child("ads_channel").child(mChannelName)
                            .child("messages").child(push_id);

                    StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
                    Uri voiceUri = Uri.fromFile(new File(file.getAbsolutePath()));
                    mUploadLayout.setVisibility(View.VISIBLE);

                    filePath.putFile(voiceUri).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            mFilePath.setText(push_id + ".gp3");
                            String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl())
                                    .toString();

                            mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                            likesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            repliesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            commentsMap.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                            Map<String, Object> messageMap = new HashMap<>();
                            messageMap.put("content", downloadUrl);
                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                            messageMap.put("type", "audio");
                            messageMap.put("parent", "Default");
                            messageMap.put("visible", true);
                            messageMap.put("from", mCurrentUserPhone);
                            messageMap.put("seen", false);
                            messageMap.put("edited", false);
                            messageMap.put("read_by", mRead);
                            messageMap.put("color", "#7016a8");
                            messageMap.put("c", commentsMap);
                            messageMap.put("l", likesMap);
                            messageMap.put("r", repliesMap);

                            Map<String, Object> msgContentMap = new HashMap<>();
                            msgContentMap.put(message_reference + push_id, messageMap);

                            // Setting the lastMessage field in the ads_channel

                            mRootReference.child("ads_channel").child(mChannelName).child("lastMessage")
                                    .setValue(push_id);

                            //Adding message
                            mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                //TODO: when completed, insert into table ads_chat. On error, remove from db
                            });

                            Map<String, Object> chatRefMap = new HashMap<>();
                            chatRefMap.put("msgId", push_id);
                            chatRefMap.put("seen", false);
                            chatRefMap.put("visible", true);
                            chatRefMap.put("timestamp", ServerValue.TIMESTAMP);

                            mTextToSend.setText("");

                            usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                                HashMap<String, Object> notificationData = new HashMap<>();
                                notificationData.put("from", mCurrentUserPhone);
                                notificationData.put("message", downloadUrl);

                                mNotificationsDatabase.child(mChannelName).push().setValue(notificationData)
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

                                                Toast.makeText(ChannelAdminChatActivity.this,
                                                        "Notification Sent",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                //mp1.start();
                                //TODO: add sent mark

                            });
                            mUploadLayout.setVisibility(View.GONE);
                        }

                    })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Errata", Toast.LENGTH_SHORT).show();
                                mUploadLayout.setVisibility(View.GONE);
                            })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mProgress.setProgress((int) progress);
                                String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                                mFileSize.setText(progressText);
                                mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                            });
                } else {
                    Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
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
            tag = 1;
            showDescriptionDialog(mImagesData.get(0), mImagesPath.get(0));


        } else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            tag = 2;
            mVideosPath = (List<String>) data.getSerializableExtra(VideoPicker.EXTRA_VIDEO_PATH);
            loadVideo();

            showDescriptionDialog(mVideosData.get(0), mVideosPath.get(0));


        } else if (requestCode == FilePickerConst.REQUEST_CODE_DOC && resultCode ==
                Activity.RESULT_OK && data != null) {
            tag = 3;
            mDocPath = new ArrayList<>();

            mDocPath.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
            //loadDocuments();
            Uri uri = Uri.fromFile(new File(mDocPath.get(0)));
            showDescriptionDialog(uri, mDocPath.get(0));


        } else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            tag = 4;
            Uri songUri = data.getData();
            showDescriptionDialog(songUri, mChannelId);
        }
    }

    private void showDescriptionDialog(Uri uri, String picture) {

        GifImageView imageSelected, sendDescription;
        ImageButton sendEmoji;
        EmojiEditText sendText;
        LinearLayout rootView;


        addDescriptionDialog.setContentView(R.layout.image_dialog);
        addDescriptionDialog.show();

        imageSelected = addDescriptionDialog.findViewById(R.id.imageSelected);
        sendDescription = addDescriptionDialog.findViewById(R.id.send_description);
        sendEmoji = addDescriptionDialog.findViewById(R.id.send_emoji);
        sendText = addDescriptionDialog.findViewById(R.id.send_text);
        rootView = addDescriptionDialog.findViewById(R.id.rootView);

        // sendEmoji.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        sendEmoji.setOnClickListener(ignore -> emojiPopup.toggle());


        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(ignore -> Log.d("ChannelAdminChatAct", "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d("ChannelAdminChatAct", "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> sendEmoji.setImageResource(R.drawable.ic_white_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d("ChannelAdminChatAct", "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> sendEmoji.setImageResource(R.drawable.ic_mood_bad))
                .setOnSoftKeyboardCloseListener(() -> Log.d("ChannelAdminChatAct", "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_slide_animation_style)
                //   .setPageTransformer(new RotateUpTransformer())
                .build(sendText);

        final String message_reference = "ads_channel_messages/";

        DatabaseReference msg_push = mRootReference.child("ads_channel_messages").push();

        String push_id = msg_push.getKey();


        switch (tag) {

            case 1:

                Picasso.get().load(new File(picture)).into(imageSelected);
                sendDescription.setOnClickListener(v -> {
                    mUploadLayout.setVisibility(View.VISIBLE);
                    addDescriptionDialog.dismiss();

                    String pictureDescription = "%" + sendText.getText().toString().trim();
                    DatabaseReference usersInGroup = mRootReference.child("ads_channel")
                            .child(mChannelName).child("messages").child(push_id);

                    StorageReference filePath = mImagesStorage.child("ads_messages_images")
                            .child(push_id + ".jpg");
//                    mUploadLayout.setVisibility(View.VISIBLE);
                    filePath.putFile(uri).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            mFilePath.setText(push_id + ".jpg");
                            String downloadUrl = Objects.requireNonNull(task.getResult()
                                    .getDownloadUrl()).toString();

                            mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                            likesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            repliesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            commentsMap.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                            Map<String, Object> messageMap = new HashMap<>();
                            messageMap.put("content", downloadUrl);
                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                            messageMap.put("type", "image");
                            messageMap.put("parent", "Default" + pictureDescription);
                            messageMap.put("visible", true);
                            messageMap.put("from", mCurrentUserPhone);
                            messageMap.put("seen", false);
                            messageMap.put("edited", false);
                            messageMap.put("read_by", mRead);
                            messageMap.put("c", commentsMap);
                            messageMap.put("l", likesMap);
                            messageMap.put("r", repliesMap);
                            messageMap.put("color", "#7016a8");

                            Map<String, Object> msgContentMap = new HashMap<>();
                            msgContentMap.put(message_reference + push_id, messageMap);

                            mRootReference.child("ads_channel").child(mChannelName).child("lastMessage")
                                    .setValue(push_id);

                            //Adding message
                            mRootReference.updateChildren(msgContentMap,
                                    (databaseError, databaseReference) -> {
                                        //TODO: completed, insert into table ads_chat.On error, remove from db
                                    });

                            Map<String, Object> chatRefMap = new HashMap<>();
                            chatRefMap.put("msgId", push_id);
                            chatRefMap.put("seen", false);
                            chatRefMap.put("visible", true);
                            chatRefMap.put("timestamp", ServerValue.TIMESTAMP);

                            mTextToSend.setText("");

                            usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference)
                                    -> {

                                HashMap<String, Object> notificationData = new HashMap<>();
                                notificationData.put("from", mCurrentUserPhone);
                                notificationData.put("message", downloadUrl);

                                mNotificationsDatabase.child(mChannelName).push()
                                        .setValue(notificationData)
                                        .addOnCompleteListener(task1 -> {

                                            if (task1.isSuccessful()) {
                                                try {
                                                    if (mp1.isPlaying()) {
                                                        mp1.stop();
                                                        mp1.release();

                                                    }
                                                    mp1.start();
//                                                    addDescriptionDialog.dismiss();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                //TODO: update message field seen

                                                Toast.makeText(ChannelAdminChatActivity.this,
                                                        "Notification Sent",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                //mp1.start();
                                //TODO: add sent mark

                            });
                            mUploadLayout.setVisibility(View.GONE);
                        }

                    })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Errata", Toast.LENGTH_SHORT).show();
                                mUploadLayout.setVisibility(View.GONE);
                            })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mProgress.setProgress((int) progress);
                                String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                                mFileSize.setText(progressText);
                                mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                            });

                });
                break;

            case 2:
                Picasso.get().load(new File(picture)).into(imageSelected);
                sendDescription.setOnClickListener(v -> {
                    mUploadLayout.setVisibility(View.VISIBLE);
                    addDescriptionDialog.dismiss();

                    String pictureDescription = "%" + sendText.getText().toString().trim();
                    DatabaseReference usersInGroup = mRootReference.child("ads_channel").child(mChannelName)
                            .child("messages").child(push_id);

                    StorageReference filePath = mVideosStorage.child("messages_videos")
                            .child(push_id + ".mp4");
                    StorageReference videoThumb = mVideosStorage.child("messages_videos").child(push_id + ".jpg");


                    Bitmap bMap = null;
                    try {
                        bMap = ThumbnailUtils.createVideoThumbnail(picture, MediaStore.Video.Thumbnails.MINI_KIND);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Objects.requireNonNull(bMap).compress(Bitmap.CompressFormat.JPEG, 40, baos);
                    final byte[] thum_byte = baos.toByteArray();

                    UploadTask uploadTask = filePath.putFile(uri);
                    uploadTask.addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            mFilePath.setText(push_id + ".mp4");
                            String downloadUrl = Objects.requireNonNull(task.getResult()
                                    .getDownloadUrl()).toString();

                            UploadTask uploadTask1 = videoThumb.putBytes(thum_byte);
                            uploadTask1.addOnCompleteListener(task1 -> {
                                String thum_url = task1.getResult().getDownloadUrl().toString();

                                mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                                likesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                                repliesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                                commentsMap.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                                Map<String, Object> messageMap = new HashMap<>();
                                messageMap.put("content", downloadUrl);
                                messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                messageMap.put("type", "video");
                                messageMap.put("parent", "Default" + pictureDescription);
                                messageMap.put("visible", true);
                                messageMap.put("from", mCurrentUserPhone);
                                messageMap.put("seen", false);
                                messageMap.put("edited", false);
                                messageMap.put("thumb", thum_url);
                                messageMap.put("read_by", mRead);
                                messageMap.put("c", commentsMap);
                                messageMap.put("l", likesMap);
                                messageMap.put("r", repliesMap);
                                messageMap.put("color", "#7016a8");

                                Map<String, Object> msgContentMap = new HashMap<>();
                                msgContentMap.put(message_reference + push_id, messageMap);

                                mRootReference.child("ads_channel").child(mChannelName).child("lastMessage")
                                        .setValue(push_id);

                                //Adding message
                                mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference)
                                        -> {
                                    //TODO: when completed, insert into table ads_chat. On error, remove from db
                                });

                                Map<String, Object> chatRefMap = new HashMap<>();
                                chatRefMap.put("msgId", push_id);
                                chatRefMap.put("seen", false);
                                chatRefMap.put("visible", true);
                                chatRefMap.put("timestamp", ServerValue.TIMESTAMP);

                                mTextToSend.setText("");

                                usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                                    HashMap<String, Object> notificationData = new HashMap<>();
                                    notificationData.put("from", mCurrentUserPhone);
                                    notificationData.put("message", downloadUrl);

                                    mNotificationsDatabase.child(mChannelName).push().setValue(notificationData)
                                            .addOnCompleteListener(task12 -> {

                                                if (task12.isSuccessful()) {
                                                    try {
                                                        if (mp1.isPlaying()) {
                                                            mp1.stop();
                                                            mp1.release();

                                                        }
                                                        mp1.start();
                                                   //     addDescriptionDialog.dismiss();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    //TODO: update message field seen

                                                    Toast.makeText(ChannelAdminChatActivity.this,
                                                            "Notification Sent",
                                                            Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                    //mp1.start();
                                    //TODO: add sent mark

                                });
                            });
                            mUploadLayout.setVisibility(View.GONE);
                        }
                    })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Errata", Toast.LENGTH_SHORT).show();
                                mUploadLayout.setVisibility(View.GONE);
                            })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mProgress.setProgress((int) progress);
                                String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                                mFileSize.setText(progressText);
                                mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                            });
                });
                break;

            case 3:
                imageSelected.setImageResource(R.drawable.documentgif);
                sendDescription.setOnClickListener(v -> {
                    addDescriptionDialog.dismiss();
                    mUploadLayout.setVisibility(View.VISIBLE);
                    String pictureDescription = "%" + sendText.getText().toString().trim();
                    DatabaseReference usersInGroup = mRootReference.child("ads_channel")
                            .child(mChannelName).child("messages").child(push_id);

                    StorageReference filePath = mDocumentsStorage.child("ads_messages_documents")
                            .child(push_id + ".docx");
                 //   mUploadLayout.setVisibility(View.VISIBLE);
                    filePath.putFile(uri)
                            .addOnCompleteListener(task -> {

                                if (task.isSuccessful()) {
                                    mFilePath.setText(push_id + ".docx");

                                    String downloadUrl = Objects.requireNonNull(task.getResult()
                                            .getDownloadUrl()).toString();

                                    mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                                    likesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                                    repliesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                                    commentsMap.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                                    Map<String, Object> messageMap = new HashMap<>();
                                    messageMap.put("content", downloadUrl);
                                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                                    messageMap.put("type", "document");
                                    messageMap.put("parent", "Default" + pictureDescription);
                                    messageMap.put("visible", true);
                                    messageMap.put("from", mCurrentUserPhone);
                                    messageMap.put("seen", false);
                                    messageMap.put("edited", false);
                                    messageMap.put("read_by", mRead);
                                    messageMap.put("c", commentsMap);
                                    messageMap.put("l", likesMap);
                                    messageMap.put("r", repliesMap);
                                    messageMap.put("color", "#7016a8");

                                    Map<String, Object> msgContentMap = new HashMap<>();
                                    msgContentMap.put(message_reference + push_id, messageMap);

                                    mRootReference.child("ads_channel").child(mChannelName).child("lastMessage")
                                            .setValue(push_id);

                                    //Adding message
                                    mRootReference.updateChildren(msgContentMap,
                                            (databaseError, databaseReference) -> {
                                                //TODO:completed, insert into table ads_chat. On error, remove from db
                                            });

                                    Map<String, Object> chatRefMap = new HashMap<>();
                                    chatRefMap.put("msgId", push_id);
                                    chatRefMap.put("seen", false);
                                    chatRefMap.put("visible", true);
                                    chatRefMap.put("timestamp", ServerValue.TIMESTAMP);

                                    mTextToSend.setText("");

                                    usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference)
                                            -> {

                                        HashMap<String, Object> notificationData = new HashMap<>();
                                        notificationData.put("from", mCurrentUserPhone);
                                        notificationData.put("message", downloadUrl);

                                        mNotificationsDatabase.child(mChannelName).push()
                                                .setValue(notificationData)
                                                .addOnCompleteListener(task13 -> {

                                                    if (task13.isSuccessful()) {
                                                        try {
                                                            if (mp1.isPlaying()) {
                                                                mp1.stop();
                                                                mp1.release();

                                                            }
                                                            mp1.start();
                                                      //      addDescriptionDialog.dismiss();
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        //TODO: update message field seen

                                                        Toast.makeText(ChannelAdminChatActivity.this,
                                                                "Notification Sent",
                                                                Toast.LENGTH_SHORT).show();

                                                    }
                                                });
                                        //mp1.start();
                                        //TODO: add sent mark

                                    });
                                    mUploadLayout.setVisibility(View.GONE);
                                }

                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Errata", Toast.LENGTH_SHORT).show();
                                mUploadLayout.setVisibility(View.GONE);
                            })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mProgress.setProgress((int) progress);
                                String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                                mFileSize.setText(progressText);
                                mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                            });
                });
                break;

            case 4:
                imageSelected.setImageResource(R.drawable.audio_bg);
                sendDescription.setOnClickListener(v -> {
                    mUploadLayout.setVisibility(View.VISIBLE);
                    addDescriptionDialog.dismiss();


                    String pictureDescription = "%" + sendText.getText().toString().trim();
                    DatabaseReference usersInGroup = mRootReference.child("ads_channel")
                            .child(mChannelName).child("messages").child(push_id);

                    StorageReference filePath = mAudioStorage.child("ads_messages_audio")
                            .child(push_id + ".gp3");
                  //  mUploadLayout.setVisibility(View.VISIBLE);
                    filePath.putFile(Objects.requireNonNull(uri)).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            mFilePath.setText(push_id + ".gp3");

                            String downloadUrl = Objects.requireNonNull(task.getResult()
                                    .getDownloadUrl()).toString();

                            mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                            likesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            repliesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            commentsMap.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                            Map<String, Object> messageMap = new HashMap<>();
                            messageMap.put("content", downloadUrl);
                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                            messageMap.put("type", "audio");
                            messageMap.put("parent", "Default" + pictureDescription);
                            messageMap.put("visible", true);
                            messageMap.put("from", mCurrentUserPhone);
                            messageMap.put("seen", false);
                            messageMap.put("edited", false);
                            messageMap.put("read_by", mRead);
                            messageMap.put("c", commentsMap);
                            messageMap.put("l", likesMap);
                            messageMap.put("r", repliesMap);
                            messageMap.put("color", "#7016a8");

                            Map<String, Object> msgContentMap = new HashMap<>();
                            msgContentMap.put(message_reference + push_id, messageMap);

                            mRootReference.child("ads_channel").child(mChannelName).child("lastMessage")
                                    .setValue(push_id);

                            //Adding message
                            mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference)
                                    -> {
                                //TODO: when completed, insert into table ads_chat. On error, remove from db
                            });

                            Map<String, Object> chatRefMap = new HashMap<>();
                            chatRefMap.put("msgId", push_id);
                            chatRefMap.put("seen", false);
                            chatRefMap.put("visible", true);
                            chatRefMap.put("timestamp", ServerValue.TIMESTAMP);

                            mTextToSend.setText("");

                            usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                                HashMap<String, Object> notificationData = new HashMap<>();
                                notificationData.put("from", mCurrentUserPhone);
                                notificationData.put("message", downloadUrl);

                                mNotificationsDatabase.child(mChannelName).push().setValue(notificationData)
                                        .addOnCompleteListener(task14 -> {

                                            if (task14.isSuccessful()) {
                                                try {
                                                    if (mp1.isPlaying()) {
                                                        mp1.stop();
                                                        mp1.release();

                                                    }
                                                    mp1.start();
                                                 //   addDescriptionDialog.dismiss();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                //TODO: update message field seen

                                                Toast.makeText(ChannelAdminChatActivity.this,
                                                        "Notification Sent",
                                                        Toast.LENGTH_SHORT).show();

                                            }
                                        });
                                //mp1.start();
                                //TODO: add sent mark

                            });
                            mUploadLayout.setVisibility(View.GONE);
                        }

                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Errata", Toast.LENGTH_SHORT).show();
                        mUploadLayout.setVisibility(View.GONE);
                    })
                            .addOnProgressListener(taskSnapshot -> {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                mProgress.setProgress((int) progress);
                                String progressText = taskSnapshot.getBytesTransferred() / 1024 + "KB/" + taskSnapshot.getTotalByteCount() / 1024 + "KB";
                                mFileSize.setText(progressText);
                                mFilePercentage.setText(MessageFormat.format("{0}%", (int) progress));
                            });
                });
                break;
        }
    }

    private void loadVideo() {

        if (mVideosPath != null && mVideosPath.size() > 0) {
            mVideosData.add(Uri.fromFile(new File(mVideosPath.get(0))));
        }

    }

    private void loadImages() {

        if (mImagesPath != null && mImagesPath.size() > 0) {
            mImagesData.add(Uri.fromFile(new File(mImagesPath.get(0))));
        }
    }

    private void showCustomDialog() {

        TextView sendImage;
        TextView sendAudio;
        TextView sendDocument;
        TextView sendVideo;

        mDialog.setContentView(R.layout.custom_dialog);
        mDialog.show();

        sendImage = mDialog.findViewById(R.id.custom_layout_image_send);
        sendAudio = mDialog.findViewById(R.id.custom_layout_audio_send);
        sendDocument = mDialog.findViewById(R.id.custom_layout_document_send);
        sendVideo = mDialog.findViewById(R.id.custom_layout_video_send);

        sendImage.setOnClickListener(view -> {

            mDialog.dismiss();
            try {
                pickImage();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        sendAudio.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, GALLERY_PICK);
            mDialog.dismiss();
        });

        sendVideo.setOnClickListener(view -> {
            mDialog.dismiss();
            try {
                pickVideo();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        sendDocument.setOnClickListener(view -> {

            onPickDoc();
            mDialog.dismiss();
        });

    }


    public void onPickDoc() {
        String[] zips = {".zip", ".rar"};
        String[] pdfs = {".pdf"};
        String[] docs = {".doc", ".docx"};
        String[] excel = {".xlsx", "xlsm"};
        String[] powerpoint = {".pptx", ".ppt"};
        if ((mDocPath.size() == MAX_ATTACHMENT_COUNT)) {
            Toast.makeText(this, getString(R.string.warn) + MAX_ATTACHMENT_COUNT + getString(R.string.items),
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
        new VideoPicker.Builder(ChannelAdminChatActivity.this)
                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                .directory(VideoPicker.Directory.DEFAULT)
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build();

    }

    private void pickImage() {
        new ImagePicker.Builder(ChannelAdminChatActivity.this)
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .allowMultipleImages(false)
                .compressLevel(ImagePicker.ComperesLevel.NONE)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .allowOnlineImages(false)
                .scale(200, 200)
                .allowMultipleImages(true)
                .enableDebuggingMode(true)
                .build();
    }


    private void sendMessage(EmojiEditText mTextToSend) {

        String message = mTextToSend.getText().toString().trim();
        if (editModeIsOn) {
            new CheckInternet_(internet -> {
                if (internet) {

                    long millis = System.currentTimeMillis();
                    long minutes = 1200000L;

                    long result = getDateDiff(clickedMessageTimeStamp, millis, TimeUnit.MILLISECONDS);
                    if (result < minutes) {
                        if (!message.isEmpty()) {
                            mMessageReference.child(clickedMessageId)
                                    .child("content").setValue(message);
                            mMessageReference.child(clickedMessageId).child("edited").setValue(true);
                            mMessageReference.child(clickedMessageId).child("timestamp").setValue(ServerValue.TIMESTAMP);
                            mChannelReference.child(mChannelName).child("messages").child(clickedMessageId).child("timestamp").setValue(ServerValue.TIMESTAMP);
//                            messagesList.clear();
//                            loadMessages();
                            mTextToSend.setText("");
                            editModeIsOn = false;
                            provideCorrectUI();
                        } else {
                            Toast.makeText(this, "Please enter a text", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, R.string.edit_limit, Toast.LENGTH_SHORT).show();
                        editModeIsOn = false;
                        provideCorrectUI();
                    }

                } else {
                    Toast.makeText(this, getString(R.string.no_internet_error), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            try {
                new CheckInternet_(internet -> {
                    if (internet) {


                        if (!TextUtils.isEmpty(message)) {

                            final String message_reference = "ads_channel_messages/";

                            DatabaseReference msg_push = mRootReference.child("ads_channel_messages").push();

                            String push_id = msg_push.getKey();

                            if (mChannelName == null || mChannelName.length() < 1) {
                                Log.i("channelName", "empty");
                            }

                            DatabaseReference usersInGroup = mRootReference.child("ads_channel").child(mChannelName)
                                    .child("messages").child(push_id);

                            mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                            likesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            repliesMap.put(mCurrentUserPhone, mCurrentUserPhone);
                            commentsMap.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                            Map<String, Object> messageMap = new HashMap<>();
                            messageMap.put("content", message);
                            messageMap.put("timestamp", ServerValue.TIMESTAMP);
                            messageMap.put("type", "text");
                            messageMap.put("parent", "Default");
                            messageMap.put("visible", true);
                            messageMap.put("from", mCurrentUserPhone);
                            messageMap.put("seen", false);
                            messageMap.put("edited", false);
                            messageMap.put("read_by", mRead);
                            messageMap.put("c", commentsMap);
                            messageMap.put("l", likesMap);
                            messageMap.put("r", repliesMap);
                            messageMap.put("color", colorList[colorToUpdate]);

                            Map<String, Object> msgContentMap = new HashMap<>();
                            msgContentMap.put(message_reference + push_id, messageMap);

                            mRootReference.child("ads_channel").child(mChannelName).child("lastMessage")
                                    .setValue(push_id);

                            //Adding message
                            mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                //TODO: when completed, insert into table ads_chat. On error, remove from db
                            });

                            Map<String, Object> chatRefMap = new HashMap<>();
                            chatRefMap.put("msgId", push_id);
                            chatRefMap.put("seen", false);
                            chatRefMap.put("visible", true);
                            chatRefMap.put("timestamp", ServerValue.TIMESTAMP);

                            mRootReference.child("ads_channel").child(mChannelName).child("subscribers")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                                            for (String s : m.keySet()) {
                                                mRootReference.child("ads_users").child(s).child("conversation")
                                                        .child("C-" + mChannelName).child("timestamp").setValue(ServerValue.TIMESTAMP);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            mTextToSend.setText("");

                            usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                                HashMap<String, Object> notificationData = new HashMap<>();
                                notificationData.put("from", mCurrentUserPhone);
                                notificationData.put("message", message);

                                mNotificationsDatabase.child(mChannelName).push().setValue(notificationData)
                                        .addOnCompleteListener(task -> {

                                            if (task.isSuccessful()) {
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

                                                Toast.makeText(ChannelAdminChatActivity.this,
                                                        "Notification Sent",
                                                        Toast.LENGTH_SHORT).show();

                                                mTextToSend.requestFocus();
                                            }
                                        });
                                //mp1.start();
                                //TODO: add sent mark

                            });
                        }
                        colorToUpdate = 0;
                    } else {
                        Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadMessages() {

        DatabaseReference conversationRef;
        try {

            conversationRef = mRootReference.child("ads_channel")
                    .child(mChannelName).child("messages");
            conversationRef.keepSynced(true);

        } catch (Exception e) {
            return;
        }

        DatabaseReference messageRef = mRootReference.child("ads_channel_messages");
        messageRef.keepSynced(true);

        Query conversationQuery = conversationRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

        conversationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Chat chatRef = dataSnapshot.getValue(Chat.class);
                if (chatRef == null) {
                    return;
                }
                Log.i("BlocChatRef", String.valueOf(chatRef.isSeen()));
                messageRef.child(chatRef.getMsgId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Messages m = dataSnapshot.getValue(Messages.class);

                                if (m == null) {
                                    return;
                                }

                                m.setMessageId(chatRef.getMsgId());
                                m.setChannelName(mChannelName);
                                m.setChannelImage(mChannelImage);
                                m.setAdmins(mChannelAdmins);

                                int pos = messagesList.indexOf(m);
                                Log.i("XXA", String.valueOf(pos));
                                if (pos < 0) {
                                    return;
                                }
                                messagesList.set(pos, m);
                                mChannelInteractionAdapter.notifyDataSetChanged();
                                mMessagesList.scrollToPosition(messagesList.size() - 1);
                                //  mSwipeRefreshLayout.setRefreshing(false);

                                nested.postDelayed(() -> {
                                    // listener.setAppBarExpanded(false, true); //appbar.setExpanded(expanded, animated);
                                    nested.fullScroll(View.FOCUS_DOWN);
                                }, 200);

                            }


                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chatRef = dataSnapshot.getValue(Chat.class);
                if (chatRef == null) {
                    return;
                }

                messageRef.child(chatRef.getMsgId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Messages m = dataSnapshot.getValue(Messages.class);
                        if (m == null) {
                            return;
                        }

//                        seen = m.getRead_by().size();
//                        Toast.makeText(ChannelAdminChatActivity.this, String.valueOf(seen),
//                                Toast.LENGTH_SHORT).show();
                        m.setMessageId(chatRef.getMsgId());

                        m.setChannelName(mChannelName);
                        m.setChannelImage(mChannelImage);
                        m.setAdmins(mChannelAdmins);

                        messagesList.add(m);
                        mChannelInteractionAdapter.notifyDataSetChanged();
                        mMessagesList.scrollToPosition(messagesList.size() - 1);
                        //mSwipeRefreshLayout.setRefreshing(false);

                        nested.postDelayed(() -> {
                            // listener.setAppBarExpanded(false, true); //appbar.setExpanded(expanded, animated);
                            nested.fullScroll(View.FOCUS_DOWN);
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
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.channell_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.lock:

                mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Channel c = dataSnapshot.getValue(Channel.class);
                        if (c == null) {
                            return;
                        }
                        if (c.getPassword().length() == 0) {
                            Toast.makeText(ChannelAdminChatActivity.this, R.string.no_password, Toast.LENGTH_SHORT).show();

                            new TTFancyGifDialog.Builder(ChannelAdminChatActivity.this)
                                    .setTitle(getString(R.string.lo___))
                                    .setMessage(getString(R.string.no_prior_password_lock))
                                    .isCancellable(false)
                                    .setGifResource(R.drawable.gif16)
                                    .OnPositiveClicked(() -> {
                                        Intent addPasswordIntent = new Intent(ChannelAdminChatActivity.this, LockChannelActivity.class);
                                        addPasswordIntent.putExtra("Channel_id", mChannelName);
                                        startActivity(addPasswordIntent);
                                    })
                                    .OnNegativeClicked(() -> {

                                    })
                                    .build();
                        } else {
                            new TTFancyGifDialog.Builder(ChannelAdminChatActivity.this)
                                    .setTitle(getString(R.string.lo___))
                                    .setMessage(getString(R.string.chan_lool__))
                                    .isCancellable(false)
                                    .setGifResource(R.drawable.gif16)

                                    .OnPositiveClicked(() -> {
                                        mChannelReference.child(mChannelName).child("locked").setValue("no").addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(ChannelAdminChatActivity.this, R.string.suc__loc_, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    })

                                    .OnNegativeClicked(() -> {

                                    })
                                    .build();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                break;

            case R.id.passwordChange:

                questionDialog.setContentView(R.layout.change_password);
                // questionDialog.show();


                EditText oldPassword = questionDialog.findViewById(R.id.oldPassword);
                Button btnNext = questionDialog.findViewById(R.id.next_Button);

                mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Channel c = dataSnapshot.getValue(Channel.class);
                        if (c == null) {
                            return;
                        }

                        if (c.getPassword().length() == 0) {
                            Toast.makeText(ChannelAdminChatActivity.this, R.string.no_password, Toast.LENGTH_SHORT).show();
                        } else {
                            questionDialog.show();
                            // Ask for old password and save new password
                            btnNext.setOnClickListener(view1 -> {
                                if (c.getPassword().equals(oldPassword.getText().toString().trim())) {
                                    oldPassword.setText("");
                                    oldPassword.setHint(R.string.nw_pasw__);
                                    btnNext.setText(getString(R.string.save));

                                    btnNext.setOnClickListener(view -> {
                                        if (TextUtils.isEmpty(oldPassword.getText().toString().trim())) {
                                            Toast.makeText(ChannelAdminChatActivity.this, getString(R.string.nqq_qw), Toast.LENGTH_SHORT).show();
                                        } else if (oldPassword.getText().toString().trim().length() < 4) {
                                            Toast.makeText(ChannelAdminChatActivity.this, getString(R.string.characters_error), Toast.LENGTH_SHORT).show();
                                        } else if (oldPassword.getText().toString().trim().equals(c.getPassword())) {
                                            Toast.makeText(ChannelAdminChatActivity.this, getString(R.string.pass_err___), Toast.LENGTH_SHORT).show();
                                        } else {
                                            new TTFancyGifDialog.Builder(ChannelAdminChatActivity.this)
                                                    .setTitle(getString(R.string.change_password__))
                                                    .setMessage(getString(R.string.sur__quq_) + " " + oldPassword.getText().toString().trim())
                                                    .isCancellable(false)
                                                    .setGifResource(R.drawable.gif16)
                                                    .OnPositiveClicked(() -> {

                                                        ProgressDialog progressDialog = new ProgressDialog(ChannelAdminChatActivity.this);
                                                        progressDialog.setTitle(getString(R.string.saving));
                                                        progressDialog.setMessage(getString(R.string.pas__sav__));
                                                        progressDialog.show();


                                                        mChannelReference.child(mChannelName)
                                                                .child("password")
                                                                .setValue(oldPassword.getText().toString().trim())
                                                                .addOnCompleteListener(task -> {

                                                                    if (task.isSuccessful()) {
                                                                        progressDialog.dismiss();
                                                                        Toast.makeText(ChannelAdminChatActivity.this,
                                                                                R.string.sux__saq_
                                                                                , Toast.LENGTH_SHORT).show();
                                                                        questionDialog.dismiss();
                                                                    }

                                                                });

                                                    })
                                                    .OnNegativeClicked(() -> {
                                                        questionDialog.dismiss();
                                                    })
                                                    .build();
                                        }
                                    });

                                } else {
                                    Toast.makeText(ChannelAdminChatActivity.this, R.string.wr_ans___, Toast.LENGTH_SHORT).show();
                                }

                            });
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                break;

            case R.id.info:

                questionDialog.setContentView(R.layout.info_layout);
                // questionDialog.show();
                questionDialog.setCanceledOnTouchOutside(true);

                EditText passwordEntered = questionDialog.findViewById(R.id.passwordEntered);
                Button getInfo = questionDialog.findViewById(R.id.get_info);
                LinearLayout linearLayout = questionDialog.findViewById(R.id.linearLayout);

                TextView password = questionDialog.findViewById(R.id.password);
                TextView question = questionDialog.findViewById(R.id.question);
                TextView answer = questionDialog.findViewById(R.id.answer);
                TextView email = questionDialog.findViewById(R.id.email);
                TextView link = questionDialog.findViewById(R.id.link);
                TextView description = questionDialog.findViewById(R.id.description);
                TextView subscribers = questionDialog.findViewById(R.id.subscribers);
                CircleImageView chanProfile = questionDialog.findViewById(R.id.chanProfile);

                mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Channel c = dataSnapshot.getValue(Channel.class);

                        if (c == null) {
                            return;
                        }
                        Picasso.get().load(c.getThumbnail()).placeholder(R.drawable.ic_avatar).into(chanProfile);
                        link.setText(String.format("%s%s", getString(R.string.channel_link), c.getLink()));
                        description.setText(MessageFormat.format("{0}:{1}", getString(R.string.description), c.getDescription()));
                        subscribers.setText(MessageFormat.format("{0}:{1}", getString(R.string.subscribers), c.getSubscribers().size()));

                        if (c.getPassword().length() == 0) {

                            linearLayout.setVisibility(View.GONE);
                            chanProfile.setVisibility(View.VISIBLE);
                            link.setVisibility(View.VISIBLE);
                            description.setVisibility(View.VISIBLE);
                            subscribers.setVisibility(View.VISIBLE);

                        } else {
                            questionDialog.show();
                            linearLayout.setVisibility(View.VISIBLE);
                            getInfo.setOnClickListener(view -> {
                                if (passwordEntered.getText().toString().trim().equals(c.getPassword())) {
                                    // Show the according info of the channel

                                    linearLayout.setVisibility(View.GONE);
                                    chanProfile.setVisibility(View.VISIBLE);
                                    link.setVisibility(View.VISIBLE);
                                    description.setVisibility(View.VISIBLE);
                                    subscribers.setVisibility(View.VISIBLE);

                                    if (c.getEmail().length() > 1) {
                                        // only show email field and password
                                        password.setVisibility(View.VISIBLE);
                                        email.setVisibility(View.VISIBLE);

                                        password.setText(String.format("%s %s", getString(R.string.pas_is_), c.getPassword()));
                                        email.setText(String.format("%s %s", getString(R.string.email_is__), c.getEmail()));
                                        //  email.setText(String.format("%d %s", getString(R.string.email_is__), c.getEmail()));

                                    } else {
                                        // show password, question and answer to question
                                        password.setVisibility(View.VISIBLE);
                                        question.setVisibility(View.VISIBLE);
                                        answer.setVisibility(View.VISIBLE);

                                        password.setText(String.format("%s %s", getString(R.string.pas_is_), c.getPassword()));
                                        question.setText(String.format("%s %s", getString(R.string.ques__is_), c.getQuestion()));
                                        answer.setText(c.getAnswer());
                                    }

                                }
                            });

                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


                break;

            default:
                return false;

        }

        return true;
    }

    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

    final java.util.Random rand = new java.util.Random();

    final Set<String> identifiers = new HashSet<>();

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


    private void askPermission() {
        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
            return;
        }
        RunTimePermissionWrapper.handleRunTimePermission(ChannelAdminChatActivity.this,
                RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
        } else {
            showSnack(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    WALK_THROUGH[0]));
        }
    }

    private void showSnack(final boolean isRationale) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(),
                "Please provide audio Record permission", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(isRationale ? "VIEW" : "Settings", view -> {
            snackbar.dismiss();

            if (isRationale)
                RunTimePermissionWrapper.handleRunTimePermission(ChannelAdminChatActivity.this,
                        RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
            else
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS),
                        1001);
        });

        snackbar.show();
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
                    mChannelInteractionAdapter.notifyDataSetChanged();

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

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
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

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
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
                .setOnEmojiBackspaceClickListener(ignore -> Log.d("ChannelAdminChatAct", "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d("ChannelAdminChatAct", "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> mSendEmoji.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d("ChannelAdminChatAct", "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> mSendEmoji.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> Log.d("ChannelAdminChatAct", "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_slide_animation_style)
                //   .setPageTransformer(new RotateUpTransformer())
                .build(mTextToSend);
    }


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
        if (getMicrophoneAvailable(this)) {
            findViewById(R.id.fragment_contaainer).setVisibility(View.VISIBLE);
            showFragment(fragment);
        } else
            Toast.makeText(this, "Microphone not available...", Toast.LENGTH_SHORT).show();
    }

    //returns whether the microphone is available
    public static boolean getMicrophoneAvailable(Context context) {
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

    public void provideCorrectUI() {
        if (editModeIsOn) {
            recycler_layout.setAlpha(0.8f);
            recycler_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            mMessagesList.removeOnItemTouchListener(recyclerItemClickListener);
            mCloseEditMode.setVisibility(View.VISIBLE);
            mSendAttachment.setVisibility(View.GONE);
            mTextToSend.removeTextChangedListener(textWatcher);
            mSendVoice.setImageResource(R.drawable.ic_send);
            mMessagesList.setEnabled(false);
            mSendVoice.setTag("sendMessage");
        } else {
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

