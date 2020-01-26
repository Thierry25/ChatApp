package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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

import app.frantic.kplcompressor.KplCompressor;
import de.hdodenhof.circleimageview.CircleImageView;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
//import droidninja.filepicker.utils.Orientation;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.GroupMessageAdapter;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.CheckInternetAsyncTask;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.AlertDialogHelper;
import marcelin.thierry.chatapp.utils.Constant;
import marcelin.thierry.chatapp.utils.RecyclerItemClickListener;

public class GroupChatActivity extends AppCompatActivity implements AlertDialogHelper.AlertDialogListener, SearchView.OnQueryTextListener, VoiceMessagerFragment.OnControllerClick {

    private static final String[] WALK_THROUGH = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    private String mGroupName;
    private String mChatPicture;
    private String mCurrentUserPhone;
    private String mChatId;

    private static boolean isOnActivity = false;

    private Map<String, Object> mRead = new HashMap<>();

    private static String mFileName = null;
    private static final String LOG_TAG = "AudioRecordTest";

    private int itemPosition = 0;
    private static final int GALLERY_PICK = 1;
    private static final int MAX_ATTACHMENT_COUNT = 20;
    private static final int TOTAL_ITEMS_TO_LOAD = 30;


    private List<String> mImagesPath;
    private List<String> mVideosPath;
    private List<Uri> mImagesData;
    private List<Uri> mVideosData;
    private ArrayList<String> mDocPath = new ArrayList<>();
    private List<Messages> messagesList = new ArrayList<>();

    private View mRootView;
    private CircleImageView mProfileImage;
    private RecyclerView mMessagesList;

    private GroupMessageAdapter mGroupMessageAdapter;

    //private SwipeRefreshLayout mSwipeRefreshLayout;
    private NestedScrollView nested;
    private LinearLayoutManager mLinearLayoutManager;

    private Dialog mDialog;
    private EmojiEditText mTextToSend;
    private EmojiPopup emojiPopup;
    //  private EmojIconActions emojIcon;
    private ImageButton mSend;
    private ImageButton mSendEmoji;
    private ImageButton mSendAttachment;
    private MediaPlayer mp1;
    private MediaRecorder mRecorder;
    private Toolbar mChatToolbar;

    private LinearLayout mainVLayout;
    private LinearLayout replyLinearLayout;
    // Long click on messages
    private ActionMode mActionMode;
    private Menu mContextMenu;
    private boolean isMultiSelect = false;
    private AlertDialogHelper mAlertDialogHelper;
    private List<Messages> mSelectedMessages = new ArrayList<>();

    private TextView title;
    private CircleImageView profileImage;
    private ImageView backButton;
    private ImageView mSendVoice;



    /***
     * Reply feature
     */
    private TextView senderOfMessage;
    private TextView messageReceived;

    private ImageView close_reply;
    private ImageView imageSent;

    private TextView videoSent;
    private TextView audioSent;
    private TextView documentSent;

    private LinearLayout messageLinLayout;
    private RelativeLayout replyTextLayout;

    //Copy Feature

    private String mFinalCopiedMessages = "";
    private LinearLayout linearLayout;
    private Fragment fragment;



    private static final StorageReference mImagesStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mVideosStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mAudioStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mDocumentsStorage = FirebaseStorage.getInstance().getReference();


    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mNotificationsDatabase = FirebaseDatabase.getInstance()
            .getReference().child("ads_notifications");
    private static final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_group_messages");

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_chat);

        mAlertDialogHelper = new AlertDialogHelper(this);
        fragment = VoiceMessagerFragment.build(this, true);

        // Reply
        imageSent = findViewById(R.id.imageSent);
        audioSent = findViewById(R.id.audioSent);
        videoSent = findViewById(R.id.videoSent);
        documentSent = findViewById(R.id.documentSent);

        senderOfMessage = findViewById(R.id.senderOfMessage);
        messageReceived = findViewById(R.id.messageReceived);
        close_reply = findViewById(R.id.close_reply);

        Intent i = getIntent();
        mGroupName = i.getStringExtra("Group_id");
        mChatPicture = i.getStringExtra("Group_image");
        mChatId = i.getStringExtra("chat_id");

        mChatToolbar = findViewById(R.id.chat_bar_main);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);

        // mSend = findViewById(R.id.send);
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_try));

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

        mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        mProfileImage = findViewById(R.id.profileImage);
        //mProfileName = findViewById(R.id.chat_bar_name);

        mTextToSend = findViewById(R.id.send_text);
        mTextToSend.addTextChangedListener(textWatcher);

        title = findViewById(R.id.title);
        //   profileImage = findViewById(R.id.profileImage);
        backButton = findViewById(R.id.backButton);

        title.setText(mChatId);


        mMessagesList = findViewById(R.id.messages_list);
        nested = findViewById(R.id.swipe_layout);

        nested.postDelayed(() -> {
            // listener.setAppBarExpanded(false, true); //appbar.setExpanded(expanded, animated);
            nested.fullScroll(View.FOCUS_DOWN);
        },200);

        backButton.setOnClickListener(v -> {
            finish();
        });
        // actionBar.setTitle(mChatId);

        mLinearLayoutManager = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);
        mainVLayout = findViewById(R.id.mainVLayout);
        replyLinearLayout = findViewById(R.id.replyLinearLayout);

        mGroupMessageAdapter = new GroupMessageAdapter(messagesList, mainVLayout, mSelectedMessages, this, this);
        mMessagesList.setAdapter(mGroupMessageAdapter);

        replyTextLayout = findViewById(R.id.replyTextLayout);

        mp1 = MediaPlayer.create(GroupChatActivity.this, R.raw.playsound);
        mSendEmoji = findViewById(R.id.send_emoji);
        mSendAttachment = findViewById(R.id.send_attachment);

        mMessagesList.addOnItemTouchListener(new RecyclerItemClickListener(this, mMessagesList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if(isMultiSelect){
                    multi_select(position);
                }else{
                    final Messages message = messagesList.get(position);

                    if(message.getType().equals("text") && message.isVisible() || message.getType().equals("channel_link")
                            && message.isVisible() ||  message.getType().equals("group_link")  && message.isVisible()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle(R.string.choose_option)
                                .setItems(R.array.options, (dialog, which) -> {
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
                                            }else{
                                                switch (message.getType()) {
                                                    case "group_link":
                                                    case "channel_link":

                                                    case "text": {

                                                        mMessagesReference.child(message.getMessageId())
                                                                .child("visible").setValue(false);

                                                        mMessagesReference.child(message.getMessageId())
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
                                                senderOfMessage.setTextColor(Color.parseColor("#FFD700"));
                                            } else {
                                                senderOfMessage.setTextColor(Color.parseColor("#FF4500"));
                                            }

                                            Messages.setClickedMessageId(message.getMessageId());

                                            close_reply.setOnClickListener(view12 -> {
                                                messageLinLayout.setBackgroundResource(R.drawable.border);
                                                replyLayout.setVisibility(View.GONE);
                                            });
                                    }

                                });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if(!isMultiSelect){
                    mSelectedMessages = new ArrayList<>();
                    isMultiSelect = true;

                    if (mActionMode == null) {
                        mActionMode = startActionMode(mActionModeCallback);
                    }
                }

                multi_select(position);

            }
        }));


        mDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);

        mRootView = findViewById(R.id.rootView);
        mSendVoice = findViewById(R.id.send_voice);

        mSendVoice.setOnClickListener(v -> {
            if(mSendVoice.getTag().equals("sendAudio")){
                linearLayout.setVisibility(View.GONE);
                record();
            }else{
                sendMessage();
            }
        });

        loadMessages();
//
        Picasso.get().load(mChatPicture).placeholder(R.drawable.ic_avatar).into(mProfileImage);

        listenerOnMessage();
        mChatToolbar.setOnClickListener(view13 -> {

            // TODO: Handle view of new layout

            Intent intent = new Intent(GroupChatActivity.this, GroupSettings.class);
            intent.putExtra("Group_name", mGroupName);
            intent.putExtra("Group_image", mChatPicture);
            intent.putExtra("chat_id", mChatId);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left );
            startActivity(intent);
            finish();
        });

        mSendAttachment.setOnClickListener(view1 -> {
            try {
                if(new CheckInternetAsyncTask(GroupChatActivity.this).execute().get()){
                    showCustomDialog();
                }else{
                    Toast.makeText(GroupChatActivity.this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });

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
        Log.i("ChatActivity", "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOnActivity = false;
        mGroupMessageAdapter.stopMediaPlayers();

    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnActivity = false;
        Log.i("ChatActivity", "onPause() called");
    }


    private void sendAudio(File file) {

        // String referenceInGroup = "ads_group/" + mCurrentUserPhone + "/" + mChatPhone;

        try {
            if(new CheckInternetAsyncTask(this).execute().get()){
                final String message_reference = "ads_group_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_group").child(mGroupName).push();

                String push_id = msg_push.getKey();

                DatabaseReference usersInGroup = mRootReference.child("ads_group").child(mGroupName)
                        .child("messages").child(push_id);

                StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
                Uri voiceUri = Uri.fromFile(new File(file.getAbsolutePath()));


                filePath.putFile(voiceUri).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl())
                                .toString();

                        mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("content", downloadUrl);
                        messageMap.put("timestamp", ServerValue.TIMESTAMP);
                        messageMap.put("type", "audio");
                        messageMap.put("parent", "Default");
                        messageMap.put("visible", true);
                        messageMap.put("from", mCurrentUserPhone);
                        messageMap.put("seen", false);
                        messageMap.put("read_by", mRead);

                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                            messageMap.put("parent", Messages.getClickedMessageId());
                            // Remove if crashed
                            replyLinearLayout.setVisibility(View.GONE);
                        }

                        Map<String, Object> msgContentMap = new HashMap<>();
                        msgContentMap.put(message_reference + push_id, messageMap);

                        mRootReference.child("ads_group").child(mGroupName).child("lastMessage")
                                .setValue(push_id);

                        //Adding message
                        mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                            //TODO: when completed, insert into table ads_chat. On error, remove from db
                        });

                        Map<String, Object> chatRefMap = new HashMap<>();
                        chatRefMap.put("msgId", push_id);
                        chatRefMap.put("seen", false);
                        chatRefMap.put("visible", true);

                        mTextToSend.setText("");

                        usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                            HashMap<String, Object> notificationData = new HashMap<>();
                            notificationData.put("from", mCurrentUserPhone);
                            notificationData.put("message", downloadUrl);

                            mNotificationsDatabase.child(mGroupName).push().setValue(notificationData)
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

                                            Toast.makeText(GroupChatActivity.this,
                                                    "Notification Sent",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });
                            //mp1.start();
                            //TODO: add sent mark

                        });
                    }

                });

            }else{
                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            mImagesPath = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            loadImages();

            for (int i = 0; i < mImagesData.size(); i++) {

                final String message_reference = "ads_group_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_group_messages").push();

                String push_id = msg_push.getKey();

                DatabaseReference usersInGroup = mRootReference.child("ads_group").child(mGroupName)
                        .child("messages").child(push_id);

                StorageReference filePath = mImagesStorage.child("ads_messages_images")
                        .child(push_id + ".jpg");
                filePath.putFile(mImagesData.get(i)).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String downloadUrl = Objects.requireNonNull(task.getResult()
                                .getDownloadUrl()).toString();

                        mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("content", downloadUrl);
                        messageMap.put("timestamp", ServerValue.TIMESTAMP);
                        messageMap.put("parent", "Default");
                        messageMap.put("type", "image");
                        messageMap.put("visible", true);
                        messageMap.put("from", mCurrentUserPhone);
                        messageMap.put("seen", false);
                        messageMap.put("read_by", mRead);

                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                            messageMap.put("parent", Messages.getClickedMessageId());
                            // Remove if crashed
                            replyLinearLayout.setVisibility(View.GONE);
                        }

                        Map<String, Object> msgContentMap = new HashMap<>();
                        msgContentMap.put(message_reference + push_id, messageMap);

                        mRootReference.child("ads_group").child(mGroupName).child("lastMessage")
                                .setValue(push_id);

                        //Adding message
                        mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                            //TODO: when completed, insert into table ads_chat. On error, remove from db
                        });

                        Map<String, Object> chatRefMap = new HashMap<>();
                        chatRefMap.put("msgId", push_id);
                        chatRefMap.put("seen", false);
                        chatRefMap.put("visible", true);

                        mTextToSend.setText("");

                        usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                            HashMap<String, Object> notificationData = new HashMap<>();
                            notificationData.put("from", mCurrentUserPhone);
                            notificationData.put("message", downloadUrl);

                            mNotificationsDatabase.child(mGroupName).push().setValue(notificationData)
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

                                            Toast.makeText(GroupChatActivity.this,
                                                    "Notification Sent",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });
                            //mp1.start();
                            //TODO: add sent mark

                        });
                    }

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
        else if (requestCode == FilePickerConst.REQUEST_CODE_DOC && resultCode ==
                Activity.RESULT_OK && data != null) {

            mDocPath = new ArrayList<>();

            mDocPath.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
            //loadDocuments();

            for (int i = 0; i < mDocPath.size(); i++) {

                final String message_reference = "ads_group_messages/";

                DatabaseReference msg_push = mRootReference.child("ads_group_messages").push();

                String push_id = msg_push.getKey();

                DatabaseReference usersInGroup = mRootReference.child("ads_group").child(mGroupName)
                        .child("messages").child(push_id);

                StorageReference filePath = mDocumentsStorage.child("ads_messages_documents")
                        .child(push_id + ".docx");
                filePath.putFile(Uri.fromFile(new File(mDocPath.get(i)))).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        String downloadUrl = Objects.requireNonNull(task.getResult()
                                .getDownloadUrl()).toString();

                        mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                        Map<String, Object> messageMap = new HashMap<>();
                        messageMap.put("content", downloadUrl);
                        messageMap.put("timestamp", ServerValue.TIMESTAMP);
                        messageMap.put("parent", "Default");
                        messageMap.put("type", "document");
                        messageMap.put("visible", true);
                        messageMap.put("from", mCurrentUserPhone);
                        messageMap.put("seen", false);
                        messageMap.put("read_by", mRead);

                        if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                            messageMap.put("parent", Messages.getClickedMessageId());
                            // Remove if crashed
                            replyLinearLayout.setVisibility(View.GONE);
                        }

                        Map<String, Object> msgContentMap = new HashMap<>();
                        msgContentMap.put(message_reference + push_id, messageMap);

                        mRootReference.child("ads_group").child(mGroupName).child("lastMessage")
                                .setValue(push_id);

                        //Adding message
                        mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                            //TODO: when completed, insert into table ads_chat. On error, remove from db
                        });

                        Map<String, Object> chatRefMap = new HashMap<>();
                        chatRefMap.put("msgId", push_id);
                        chatRefMap.put("seen", false);
                        chatRefMap.put("visible", true);

                        mTextToSend.setText("");

                        usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                            HashMap<String, Object> notificationData = new HashMap<>();
                            notificationData.put("from", mCurrentUserPhone);
                            notificationData.put("message", downloadUrl);

                            mNotificationsDatabase.child(mGroupName).push().setValue(notificationData)
                                    .addOnCompleteListener(task13 -> {

                                        if (task13.isSuccessful()) {
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

                                            Toast.makeText(GroupChatActivity.this,
                                                    "Notification Sent",
                                                    Toast.LENGTH_SHORT).show();

                                        }
                                    });
                            //mp1.start();
                            //TODO: add sent mark

                        });

                    }

                });

            }

        } else if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri songUri = data.getData();

            final String message_reference = "ads_group_messages/";

            DatabaseReference msg_push = mRootReference.child("ads_group_messages").push();

            String push_id = msg_push.getKey();

            DatabaseReference usersInGroup = mRootReference.child("ads_group").child(mGroupName).child("messages").child(push_id);

            StorageReference filePath = mAudioStorage.child("ads_messages_audio").child(push_id + ".gp3");
            filePath.putFile(Objects.requireNonNull(songUri)).addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                    mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("content", downloadUrl);
                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                    messageMap.put("parent", "Default");
                    messageMap.put("visible", true);
                    messageMap.put("type", "audio");
                    messageMap.put("from", mCurrentUserPhone);
                    messageMap.put("seen", false);
                    messageMap.put("read_by", mRead);

                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                        messageMap.put("parent", Messages.getClickedMessageId());
                        // Remove if crashed
                        replyLinearLayout.setVisibility(View.GONE);
                    }

                    Map<String, Object> msgContentMap = new HashMap<>();
                    msgContentMap.put(message_reference + push_id, messageMap);

                    mRootReference.child("ads_group").child(mGroupName).child("lastMessage")
                            .setValue(push_id);

                    //Adding message
                    mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                        //TODO: when completed, insert into table ads_chat. On error, remove from db
                    });

                    Map<String, Object> chatRefMap = new HashMap<>();
                    chatRefMap.put("msgId", push_id);
                    chatRefMap.put("seen", false);
                    chatRefMap.put("visible", true);

                    mTextToSend.setText("");

                    usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                        HashMap<String, Object> notificationData = new HashMap<>();
                        notificationData.put("from", mCurrentUserPhone);
                        notificationData.put("message", downloadUrl);

                        mNotificationsDatabase.child(mGroupName).push().setValue(notificationData)
                                .addOnCompleteListener(task14 -> {

                                    if(task14.isSuccessful()) {
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

                                        Toast.makeText(GroupChatActivity.this, "Notification Sent",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
                        //mp1.start();
                        //TODO: add sent mark

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
            Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
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
            Toast.makeText(this, getString(R.string.cannot) + MAX_ATTACHMENT_COUNT + getString(R.string.items),
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
        new VideoPicker.Builder(GroupChatActivity.this)
                .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                .directory(VideoPicker.Directory.DEFAULT)
                .extension(VideoPicker.Extension.MP4)
                .enableDebuggingMode(true)
                .build();
    }

    private void pickImage() {
        new ImagePicker.Builder(GroupChatActivity.this)
                .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                .allowMultipleImages(true)
                .compressLevel(ImagePicker.ComperesLevel.NONE)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .allowOnlineImages(false)
                .scale(600, 600)
                .allowMultipleImages(true)
                .enableDebuggingMode(true)
                .build();
    }


    private void sendMessage() {

        try {
            if(new CheckInternetAsyncTask(this).execute().get()){

                String message = mTextToSend.getText().toString().trim();

                if (!TextUtils.isEmpty(message)) {

                    final String message_reference = "ads_group_messages/";

                    DatabaseReference msg_push = mRootReference.child("ads_group_messages").push();

                    String push_id = msg_push.getKey();

                    if(mGroupName == null || mGroupName.length() < 1) {
                        Log.i("groupName", "empty");
                    }

                    DatabaseReference usersInGroup = mRootReference.child("ads_group").child(mGroupName)
                            .child("messages").child(push_id);

                    mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("content", message);
                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                    messageMap.put("parent", "Default");
                    messageMap.put("visible", true);
                    messageMap.put("type", "text");
                    messageMap.put("from", mCurrentUserPhone);
                    messageMap.put("seen", false);
                    messageMap.put("read_by", mRead);

                    Map<String, Object> msgContentMap = new HashMap<>();
                    msgContentMap.put(message_reference + push_id, messageMap);

                    mRootReference.child("ads_group").child(mGroupName).child("lastMessage")
                            .setValue(push_id);

                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                        messageMap.put("parent", Messages.getClickedMessageId());
                    }
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
                        notificationData.put("message", message);

                        mNotificationsDatabase.child(mGroupName).push().setValue(notificationData)
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

                                        Toast.makeText(GroupChatActivity.this, "Notification Sent",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
                        //mp1.start();
                        //TODO: add sent mark

                    });
                }

            } else{
                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages(){

        DatabaseReference conversationRef;
        DatabaseReference colorRef;
        try{

            //
            conversationRef = mRootReference.child("ads_group").child(mGroupName).child("messages");
            conversationRef.keepSynced(true);

            colorRef = mRootReference.child("ads_group").child(mGroupName).child("users")
                    .child(mCurrentUserPhone);
            colorRef.keepSynced(true);

        }catch (Exception e){
            return;
        }

        DatabaseReference messageRef = mRootReference.child("ads_group_messages");
        messageRef.keepSynced(true);

        Query conversationQuery = conversationRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

        colorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String color = dataSnapshot.getValue(String.class);

                conversationQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
                                //TODO: add except logic
                                mRootReference.child(Constant.ROOT_REF).child(mCurrentUserPhone)
                                        .child(Constant.USR_MOOD_EXCEPT_NODE).child("G-"+ mGroupName)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.exists()) {
                                                    Log.i("GROUP_IN_EXCEPT", dataSnapshot.getValue().toString());
                                                    Long mutedTimestamp = (Long) dataSnapshot.getValue();
                                                    if (mutedTimestamp < m.getTimestamp()) {
                                                        Log.i("GROUP_MSG_MUTED", "message muted on "
                                                                + mutedTimestamp);
                                                        return;
                                                    }
                                                }
                                                m.setColor(color);
                                                m.setMessageId(chatRef.getMsgId());
                                                if (isOnActivity && !m.getFrom().equals(mCurrentUserPhone)) {
                                                    updateSeen(m);
                                                }
                                                messagesList.add(m);

                                                mGroupMessageAdapter.notifyDataSetChanged();
                                                mMessagesList.scrollToPosition(messagesList.size() - 1);
                                                //mSwipeRefreshLayout.setRefreshing(false);

                                                if(!m.getParent().equals("Default") && m.isVisible()){
                                                    m.setReplyOn(true);
                                                }

                                                nested.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // listener.setAppBarExpanded(false, true); //appbar.setExpanded(expanded, animated);
                                                        nested.fullScroll(View.FOCUS_DOWN);
                                                    }
                                                },200);
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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void loadMessagesDeprecated(){

        DatabaseReference conversationRef;
        DatabaseReference colorRef;
        try{

            //
            conversationRef = mRootReference.child("ads_group").child(mGroupName).child("messages");
            conversationRef.keepSynced(true);

            colorRef = mRootReference.child("ads_group").child(mGroupName).child("users")
                    .child(mCurrentUserPhone);
            colorRef.keepSynced(true);

        }catch (Exception e){
            return;
        }

        DatabaseReference messageRef = mRootReference.child("ads_group_messages");
        messageRef.keepSynced(true);

        Query conversationQuery = conversationRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

        colorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String color = dataSnapshot.getValue(String.class);

                conversationQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

                                m.setColor(color);
                                m.setMessageId(chatRef.getMsgId());
                                if (isOnActivity && !m.getFrom().equals(mCurrentUserPhone)) {
                                    updateSeen(m);
                                }
                                messagesList.add(m);

                                mGroupMessageAdapter.notifyDataSetChanged();
                                mMessagesList.scrollToPosition(messagesList.size() - 1);
                                //mSwipeRefreshLayout.setRefreshing(false);

                                if(!m.getParent().equals("Default") && m.isVisible()){
                                    m.setReplyOn(true);
                                }

                                nested.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        // listener.setAppBarExpanded(false, true); //appbar.setExpanded(expanded, animated);
                                        nested.fullScroll(View.FOCUS_DOWN);
                                    }
                                },200);

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
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        RunTimePermissionWrapper.handleRunTimePermission(GroupChatActivity.this,
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
                RunTimePermissionWrapper.handleRunTimePermission(GroupChatActivity.this,
                        RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
            else
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS),
                        1001);

        });

        snackbar.show();
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userEntry = newText.toLowerCase();
        List<Messages> newMessages = new ArrayList<>();

        for(Messages m : messagesList){
            if(m.getType().equals("text")){
                if(m.getContent().toLowerCase().contains(userEntry)){
                    newMessages.add(m);
                }
            }
        }

        mGroupMessageAdapter.updateList(newMessages);
        return true;
    }

    private void updateSeen(Messages m) {

        if (m.getRead_by().containsKey(mCurrentUserPhone)) { return; }
        if (m.getMessageId() == null) { return; }

        mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

        mRootReference.child("ads_group_messages").child(m.getMessageId()).child("read_by")
                .updateChildren(mRead);

        //    mRootReference.child("ads_channel_messages").child(m.getMessageId()).updateChildren(mRead);

    }

    private void listenerOnMessage(){

        mMessagesReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Log.i("DatS", dataSnapshot.getKey());
                Messages m = dataSnapshot.getValue(Messages.class);
                if(m == null){
                    return;
                }

                Log.i("Msg", String.valueOf(m.isVisible()));
                if(!m.isVisible()){
                    Log.i("MsgV", m.getContent());
                    m.setMessageId(dataSnapshot.getKey());
                    int pos = messagesList.indexOf(m);
                    Log.i("XXA", String.valueOf(pos));
                    if (pos < 0) { return; }
                    messagesList.set(pos, m);
                    mGroupMessageAdapter.notifyDataSetChanged();

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

        if(from == 1){
            if(mSelectedMessages.size() > 0){
                for(Messages m : mSelectedMessages){

                    if (!m.getFrom().equals(Objects.requireNonNull
                            (mAuth.getCurrentUser()).getPhoneNumber())) {
                        Toast.makeText(this, "Cannot delete message " +
                                "not coming from you", Toast.LENGTH_SHORT).show();
                    } else if (m.getType().equals("group_link") || m.getType().equals("channel_link")) {
                        mMessagesReference.child(m.getMessageId())
                                .child("visible").setValue(false);

                        mMessagesReference.child(m.getMessageId())
                                .child("content").setValue("Message Deleted");

                        //  ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);

                        Toast.makeText(this, "Message deleted",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        mMessagesReference.child(m.getMessageId())
                                .child("visible").setValue(false);

                        mMessagesReference.child(m.getMessageId())
                                .child("content").setValue("Message Deleted");

                        // ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);

                        Toast.makeText(this,  "Message deleted",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
            if (mActionMode != null) {
                mActionMode.finish();
            }
        }else if(from == 2){

            for(Messages m : mSelectedMessages){
                if(m.getType().equals("audio") || m.getType().equals("video") || m.getType().equals("image")){
                    Toast.makeText(this, "Can only copy text messages", Toast.LENGTH_SHORT).show();
                    mActionMode.finish();
                    return;
                }
                if(m.getFrom().equals(mCurrentUserPhone)){
                    mFinalCopiedMessages = mFinalCopiedMessages + "[" + getDate(m.getTimestamp())
                            + "]" + "You: " + m.getContent() + "\n";
                }else{
                    String nameStored = Users.getLocalContactList().get(m.getFrom());
                    nameStored = nameStored != null && nameStored.length() > 0  ? nameStored :
                            m.getFrom();
                    mFinalCopiedMessages = mFinalCopiedMessages + "[" + getDate(m.getTimestamp())
                            + "]" +  nameStored + ": " + m.getContent() + "\n";
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
        if(!m.isVisible()){
            Toast.makeText(GroupChatActivity.this, "Can't be selected", Toast.LENGTH_SHORT).show();
        }else{
            if (mActionMode != null) {
                if (mSelectedMessages.contains(messagesList.get(position))){
                    mSelectedMessages.remove(messagesList.get(position));
                }else {
                    mSelectedMessages.add(messagesList.get(position));
                }

                if (mSelectedMessages.size() > 0){
                    mActionMode.setTitle("" + mSelectedMessages.size());
                }else {
                    return;
                }
                refreshAdapter();

            }
        }

    }


    public void refreshAdapter()
    {
        mGroupMessageAdapter.mSelectedMessagesList = mSelectedMessages;
        mGroupMessageAdapter.mMessagesList = messagesList;
        mGroupMessageAdapter.notifyDataSetChanged();
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
                            "Delete Messages?","DELETE","CANCEL",
                            1,false);
                    return true;

                case R.id.action_copy:
                    mAlertDialogHelper.showAlertDialog("",
                            "Copy Messages?", "YES", "NO", 2, false);

//                case R.id.action_forward:
//                    mAlertDialogHelper.showAlertDialog("","Forward Messages?", "YES", "NO"
//                            ,3, false);
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

    private String getHumanTimeText(long milliseconds) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));
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

    @Override
    public void onRecordClick() {

    }

    @Override
    public void onFinishRecording(File file) {

    }

    @Override
    public void onSendClick(File file) {
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
            float length = imageFile.length() / 1024f; // Size in KB
            Uri uri = Uri.fromFile(imageFile);

            final String message_reference = "ads_group_messages/";

            DatabaseReference msg_push = mRootReference.child("ads_group_messages").push();

            String push_id = msg_push.getKey();

            DatabaseReference usersInGroup = mRootReference.child("ads_group").child(mGroupName)
                    .child("messages").child(push_id);

            StorageReference filePath = mVideosStorage.child("messages_videos")
                    .child(push_id + ".mp4");

            UploadTask uploadTask = filePath.putFile(uri);
            uploadTask.addOnCompleteListener(task -> {

                if (task.isSuccessful()) {

                    String downloadUrl = Objects.requireNonNull(task.getResult()
                            .getDownloadUrl()).toString();

                    mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                    Map<String, Object> messageMap = new HashMap<>();
                    messageMap.put("content", downloadUrl);
                    messageMap.put("timestamp", ServerValue.TIMESTAMP);
                    messageMap.put("parent", "Default");
                    messageMap.put("visible", true);
                    messageMap.put("type", "video");
                    messageMap.put("from", mCurrentUserPhone);
                    messageMap.put("seen", false);
                    messageMap.put("read_by", mRead);

                    Map<String, Object> msgContentMap = new HashMap<>();
                    msgContentMap.put(message_reference + push_id, messageMap);

                    if (replyLinearLayout.getVisibility() == View.VISIBLE) {
                        messageMap.put("parent", Messages.getClickedMessageId());
                        // Remove if crashed
                        replyLinearLayout.setVisibility(View.GONE);
                    }

                    mRootReference.child("ads_group").child(mGroupName).child("lastMessage")
                            .setValue(push_id);

                    //Adding message
                    mRootReference.updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                        //TODO: when completed, insert into table ads_chat. On error, remove from db
                    });

                    Map<String, Object> chatRefMap = new HashMap<>();
                    chatRefMap.put("msgId", push_id);
                    chatRefMap.put("seen", false);
                    chatRefMap.put("visible", true);

                    mTextToSend.setText("");

                    usersInGroup.updateChildren(chatRefMap, (databaseError, databaseReference) -> {

                        HashMap<String, Object> notificationData = new HashMap<>();
                        notificationData.put("from", mCurrentUserPhone);
                        notificationData.put("message", downloadUrl);

                        mNotificationsDatabase.child(mGroupName).push().setValue(notificationData)
                                .addOnCompleteListener(task12 -> {

                                    if (task12.isSuccessful()) {
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

                                        Toast.makeText(GroupChatActivity.this, "Notification Sent",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                });
                        //mp1.start();
                        //TODO: add sent mark

                    });

                }


            });
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

    public void showFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        if (fragment.isAdded())
            fragmentTransaction.show( fragment );
        else
            fragmentTransaction.add(R.id.fragment_contaainer, fragment , "h").addToBackStack(null);
        fragmentTransaction.commit();
    }
    public void remove(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment.isAdded())
            fragmentTransaction.remove( fragment );
        fragmentTransaction.commit();
    }

    public void record() {
        if (getMicrophoneAvailable(this)){
            findViewById(R.id.fragment_contaainer).setVisibility(View.VISIBLE);
            showFragment(fragment);}
        else
            Toast.makeText(this,"Microphone not available...",Toast.LENGTH_SHORT).show();
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

        }
        catch (Exception exception) {
            available = false;
        }
        recorder.release();
        return available;
    }
}

