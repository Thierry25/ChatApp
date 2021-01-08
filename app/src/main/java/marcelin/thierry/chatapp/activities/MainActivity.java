package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deep.videotrimmer.utils.FileUtils;
import com.fxn.pix.Options;
import com.fxn.pix.Pix;
import com.fxn.pixeditor.EditOptions;
import com.fxn.pixeditor.PixEditorJave;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.vanniktech.emoji.EmojiEditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.videotrimming.VideoPicker;
import marcelin.thierry.chatapp.activities.videotrimming.VideoTrimmerActivity;
import marcelin.thierry.chatapp.adapters.ChatAdapter;
import marcelin.thierry.chatapp.adapters.CropAndWriteTextAdapter;
import marcelin.thierry.chatapp.adapters.StatusAdapter;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Group;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import marcelin.thierry.chatapp.classes.Status;
import marcelin.thierry.chatapp.classes.UserStatus;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.CheckInternet_;
import marcelin.thierry.chatapp.utils.RecyclerItemClickListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip;

import static marcelin.thierry.chatapp.activities.videotrimming.Constants.EXTRA_VIDEO_PATH;

public class MainActivity extends AppCompatActivity {

    private static final String SHOWCASE_ID = "menuIsClicked_";
    private static final String SHOWCASE_ID_status = "status_example_Final_";
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");
    private static final DatabaseReference mMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_messages");
    private static final DatabaseReference mChannelMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel_messages");
    private static final DatabaseReference mGroupMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group_messages");
    private static final DatabaseReference mChatReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_chat");
    private static final DatabaseReference mGroupReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group");
    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");


    private static final StorageReference mVideosStorage = FirebaseStorage.getInstance().getReference();
    private static final StorageReference mImagesStorage = FirebaseStorage.getInstance().getReference();

    private static final DatabaseReference mStatusReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_status");

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private final int REQUEST_CODE = 102;
    private final String[] WALK_THROUGH = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final String[] PROJECTION = new String[]{
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
          //  ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
//             ContactsContract.RawContacts.ACCOUNT_TYPE + " = 'com.google' "
    };

    private RecyclerItemClickListener recyclerItemClickListener;
    private Uri resultUri;
  //  private String picturePath;
    private Uri picturePath;
    private ArrayList<String> returnValue;

            EditOptions editoptions;
    // Remove following if crashes
    //public static final int PERMISSION_STORAGE = 100;
    private final int REQUEST_VIDEO_TRIMMER_RESULT = 342;
    private final int REQUEST_VIDEO_TRIMMER = 0x12;
    //private TextView mStatusText;
    private ChatAdapter mChatAdapter;
    // private static final String SHOWCASE_ID = "simple example";
    private RecyclerView mConversationList, mStatusRecyclerView;
    private String mCurrentUserPhone;
    private LinearLayout mAddStatusLayout;
    // private ProgressBar mProgressBar;
    private FirebaseUser currrentUser;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    // Getting my phoneNumber
    private Dialog mDialog;
    private CardView mTextCardView, mImageCardView, mVideoCardView;
    private FloatingActionButton mPlusButton, mChannelButton, mGroupButton, mMoodButton, mContactButton, mSettingsButton, mDoneBtn;
    private List<Conversation> mConvoList = new ArrayList<>();
    private Animation mFabOpen, mFabClose, mFabClockWise, mFabAntiClockWise;
    private RelativeLayout mBackgroundLayout, myStatusLayout, backgroundLayoutForStatus;
    private LinearLayout mLin, mLin1, mLin2, mLin3, mLin4;
    private boolean isOpen = false;
    private CircleImageView mProfilePic;
    private Map<String, Object> mRead = new HashMap<>();
    private StatusAdapter mStatusAdapter;
    private int isClicked = 0;
    private ImageView mCloseLayout;
    private List<UserStatus> mStatusList = new ArrayList<>();
    private TextView mStatusPrivacy;
    private  ExecutorService es1, es2;

    private LinearLayout crop_add_text;
    private ImageView crop_btn, pic;
    private ImageButton send_emoji;
    private EmojiEditText send_text;
    private RecyclerView recycler_view;
    private List<Uri> images = new ArrayList<>();

    private CropAndWriteTextAdapter mCropAndWriteTextAdapter;
    boolean doubleBackToExitPressedOnce = false;


    private ProgressDialog mProgressDialog;


    public static long getDateDiff(long date1, long date2, TimeUnit timeUnit) {
        long diffInMillies = (date2 - date1);
        return timeUnit.convert(diffInMillies, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //TODO: [fm] detach listeners here if exists

        //TODO: [fm] kill executor services
        if (currrentUser != null) {
            es1.shutdown();
            es2.shutdown();
            //mUsersReference.removeEventListener(conversationAddedListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Conversation c = mChatAdapter.getCurrent();
        if (c != null) {
            Log.i("CONVERSATION_ON_RES", c.getId());
            if (mConvoList.isEmpty()) { return; }
            if (mConvoList.contains(c)) {
                Log.i("CONVERSATION_ON_RES", "CONVERSATION TO UPDATE");
                int pos = mConvoList.indexOf(c);
                Conversation conversation = mConvoList.get(pos);
                conversation.setUnreadMessages(0);
                mConvoList.remove(c);
                mConvoList.add(pos, conversation);
                mChatAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainOnCreate", "method called");
        loadLocale();

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        editoptions = EditOptions.init();
        editoptions.setRequestCode(124);

        long startTime = System.nanoTime();
        askPermission();
        long endTime = System.nanoTime();
        Log.i("STOPWATCH_PERMISSION", Long.toString((endTime - startTime) / 1000000));
        int countContact = Users.getLocalContactList().size();
        Log.i("CONTACT_LIST_SIZE", Integer.toString(countContact));

        currrentUser = mAuth.getCurrentUser();

        mConversationList = findViewById(R.id.conversation_list);
        mStatusRecyclerView = findViewById(R.id.statusList);

        // FAB
        mPlusButton = findViewById(R.id.plusButton);
        mMoodButton = findViewById(R.id.moodButton);
        mChannelButton = findViewById(R.id.channelButton);
        mGroupButton = findViewById(R.id.groupButton);
        mContactButton = findViewById(R.id.contactButton);
        mSettingsButton = findViewById(R.id.settingsButton);

        //Layouts
        mBackgroundLayout = findViewById(R.id.backgroundLayout);
        mLin = findViewById(R.id.lin);
        mLin1 = findViewById(R.id.lin1);
        mLin2 = findViewById(R.id.lin2);
        mLin3 = findViewById(R.id.lin3);
        mLin4 = findViewById(R.id.lin4);

        mProfilePic = findViewById(R.id.profileImage);
        myStatusLayout = findViewById(R.id.myStatusLayout);
        mAddStatusLayout = findViewById(R.id.addStatusLayout);
        backgroundLayoutForStatus = findViewById(R.id.backgroundLayoutForStatus);
        mCloseLayout = findViewById(R.id.closeLayout);
        mStatusPrivacy = findViewById(R.id.status_privacy);

        presentShowcaseSeq();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        mChatAdapter = new ChatAdapter(mConvoList, this);
        //[fm] adding new adapter type
        //mChatAdapter = new ChatAdapter(new DiffConversationCallBack());
        //mChatAdapter.setContext(this);
        //mChatAdapter.setmUserConv(mConvoList);
        mConversationList.setAdapter(mChatAdapter);

        mConversationList.setHasFixedSize(true);
        mConversationList.setLayoutManager(linearLayoutManager);


        if (currrentUser != null) {
            mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
            //TODO: [fm] gol-a-main -> adding own phone number in the list
            if (!Users.getLocalContactList().containsKey(mCurrentUserPhone)) {
                Users.getLocalContactList().put(mCurrentUserPhone, "Me");
            }
            //getStatus();
            //loadConversations();
            es1 = Executors.newFixedThreadPool(3);
            es2 = Executors.newFixedThreadPool(2);
            //fetchAllConversations();
            es1.execute(this::fetchAllConversations);
            es2.execute(this::fetchStatus);
        }

        mFabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        mFabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);
        mFabClockWise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        mFabAntiClockWise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);

        mDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mDialog.setCancelable(true);
        mStatusAdapter = new StatusAdapter(mStatusList);
        mStatusRecyclerView.setAdapter(mStatusAdapter);

        mStatusRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mStatusRecyclerView.setLayoutManager(layoutManager);

        mTextCardView = findViewById(R.id.textCardView);
        mImageCardView = findViewById(R.id.imageCardView);
        mVideoCardView = findViewById(R.id.videoCardView);

        mPlusButton.setOnClickListener(view -> {
            //  startActivity(new Intent(getActivity(), ContactsActivity.class));
            if (isOpen) {

                mMoodButton.startAnimation(mFabClose);
                mGroupButton.startAnimation(mFabClose);
                mChannelButton.startAnimation(mFabClose);
                mContactButton.startAnimation(mFabClose);
                mSettingsButton.startAnimation(mFabClose);

                mPlusButton.startAnimation(mFabAntiClockWise);

                mMoodButton.setClickable(false);
                mGroupButton.setClickable(false);
                mChannelButton.setClickable(false);
                mContactButton.setClickable(false);

                mBackgroundLayout.setVisibility(View.GONE);
                mChatAdapter.isClickable = true;
                mLin.setVisibility(View.GONE);
                mLin1.setVisibility(View.GONE);
                mLin2.setVisibility(View.GONE);
                mLin3.setVisibility(View.GONE);
                mLin4.setVisibility(View.GONE);

                isOpen = false;
            } else {

                mBackgroundLayout.setVisibility(View.VISIBLE);

                mLin.setVisibility(View.VISIBLE);
                mLin1.setVisibility(View.VISIBLE);
                mLin2.setVisibility(View.VISIBLE);
                mLin3.setVisibility(View.VISIBLE);
                mLin4.setVisibility(View.VISIBLE);

                mMoodButton.startAnimation(mFabOpen);
                mGroupButton.startAnimation(mFabOpen);
                mChannelButton.startAnimation(mFabOpen);
                mContactButton.startAnimation(mFabOpen);
                mSettingsButton.startAnimation(mFabOpen);

                mPlusButton.startAnimation(mFabClockWise);

                mMoodButton.setClickable(true);
                mGroupButton.setClickable(true);
                mChannelButton.setClickable(true);
                mContactButton.setClickable(true);

                mChatAdapter.isClickable = false;
                isOpen = true;
            }
        });

        mContactButton.setOnClickListener(v -> {
            isOpen = true;
            startActivity(new Intent(getApplicationContext(), ContactsActivity.class));
        });

        mMoodButton.setOnClickListener(v -> {
            isOpen = true;
            startActivity(new Intent(getApplicationContext(), MoodActivity.class));
        });

        mGroupButton.setOnClickListener(v -> {
            isOpen = true;
            startActivity(new Intent(getApplicationContext(), GroupActivity.class));
        });

        mChannelButton.setOnClickListener(v -> {
            isOpen = true;

            mDialog.setContentView(R.layout.channel_layout);
            mDialog.show();
            mDialog.setCanceledOnTouchOutside(true);

            Button reconnectToChannel, newChannel, mySubscriptions, searchChannel;
            reconnectToChannel = mDialog.findViewById(R.id.reconnectToChannel);
            newChannel = mDialog.findViewById(R.id.newChannel);
            mySubscriptions = mDialog.findViewById(R.id.mySubscriptions);
            searchChannel = mDialog.findViewById(R.id.searchChannel);

            reconnectToChannel.setOnClickListener(v1 -> {
                startActivity(new Intent(this, ChannelReconnectActivity.class));
                mDialog.dismiss();
            });

            newChannel.setOnClickListener(v1 -> {
                startActivity(new Intent(this, ChannelActivity.class));
                mDialog.dismiss();
            });

            mySubscriptions.setOnClickListener(v1 -> {
                startActivity(new Intent(this, SubscriptionActivity.class));
                mDialog.dismiss();
            });

            searchChannel.setOnClickListener(v1 -> {
                startActivity(new Intent(this, SearchActivity.class));
                mDialog.dismiss();
            });

        });

        mSettingsButton.setOnClickListener(v1 ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        if (currrentUser != null) {
            mUsersReference.child(mCurrentUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
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
        }

        mUsersReference.keepSynced(true);
        mMessageReference.keepSynced(true);
        mChannelMessageReference.keepSynced(true);
        mGroupMessageReference.keepSynced(true);
        mChatReference.keepSynced(true);
        mGroupReference.keepSynced(true);
        mChannelReference.keepSynced(true);

        myStatusLayout.setOnClickListener(v -> {
            backgroundLayoutForStatus.setVisibility(View.VISIBLE);
            mAddStatusLayout.setVisibility(View.VISIBLE);
            mStatusPrivacy.setVisibility(View.VISIBLE);
            mChatAdapter.isClickable = false;
            presentShowcaseSequence();

        });

        mCloseLayout.setOnClickListener(v -> {
            backgroundLayoutForStatus.setVisibility(View.GONE);
            mAddStatusLayout.setVisibility(View.GONE);
            mStatusPrivacy.setVisibility(View.GONE);
            mChatAdapter.isClickable = true;
        });

        mTextCardView.setOnClickListener(v -> {
            isClicked = 1;
            askPermission();
        });

        mImageCardView.setOnClickListener(v -> {
            isClicked = 2;
            askPermission();
        });

        mVideoCardView.setOnClickListener(v -> {
            isClicked = 3;
            askPermission();
        });

        mStatusAdapter.setOnStatusClickListener((circularStatusView, pos) -> {
            UserStatus userStatus = mStatusList.get(pos);

            Intent intent = new Intent(this, StatsActivity.class);
            intent.putExtra("statusList", (Serializable) userStatus.getStatusList());
            startActivity(intent);

            if (!userStatus.areAllSeen()) {
                for (int i = 0; i < userStatus.getStatusList().size(); i++) {
                    Status status = userStatus.getStatusList().get(i);
                    updateSeen(status);
                    if (status.getSeenBy().containsKey(mCurrentUserPhone)) {
                        status.setSeen(true);
                        mStatusAdapter.notifyDataSetChanged();
                    }

                    if (!status.isSeen()) {
                        circularStatusView.setPortionColorForIndex(i, R.color.colorAccent);
                        status.setSeen(true);
                        //open the drawer to show the status
                        break;
                    }
                }
            }
        });

        mStatusPrivacy.setOnClickListener(v-> startActivity(new Intent(this, PrivacyActivity.class)));
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(getString(R.string.st_updt_));
        mProgressDialog.setMessage(getString(R.string.stt_upload_msg));

        // Cropping
        crop_add_text = findViewById(R.id.crop_add_text);
        crop_btn = findViewById(R.id.crop_btn);
        pic = findViewById(R.id.pic);
        send_emoji = findViewById(R.id.send_emoji);
        send_text = findViewById(R.id.send_text);
        recycler_view = findViewById(R.id.recycler_view);
        mDoneBtn = findViewById(R.id.done_btn);
        recycler_view.setHasFixedSize(true);
    //    recycler_view.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager
                (this, LinearLayoutManager.HORIZONTAL, false);
        recycler_view.setLayoutManager(horizontalLayoutManager);
    }


    /*
     * Fetches all conversation (including chats, group chats and channels)
     * @author fm
     */
    private void  fetchAllConversations() {
        Log.i("CONVERSATION", "FUNCTION CALLED");
        mConvoList.clear();
        mUsersReference.child(mCurrentUserPhone)
                //Todo: [fm] add listener variable
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(getApplicationContext(), "You have no conversation at this time, " +
                                    "please start a new conversation", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!dataSnapshot.hasChild("conversation")) {
                            Toast.makeText(getApplicationContext(), "You have no conversation at this time, " +
                                    "please start a new conversation", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Log.i("CONVERSATION_USR_RAW", dataSnapshot.getValue().toString());
                        Long userFetchStart = System.nanoTime();
                        Users me = dataSnapshot.getValue(Users.class);
                        Long userFetchEnd = System.nanoTime();
                        Log.i("STOPWATCH_USR_FETCH", Long.toString((userFetchEnd - userFetchStart) / 1000000));
                        Log.i("CONVERSATION_ME", me.getName());
                        if (me.getConversations() == null || me.getConversations().isEmpty()) {
                            Log.i("CONVERSATION_EMPTY", "EMPTY");
                            Toast.makeText(getApplicationContext(), "You have no conversation at this time, " +
                                    "please start a new conversation", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //[fm] Iterates through conversation list
                        Log.i("CONVERSATION_B4_LOOP", "LOOPING BELOW");
                        Long start = System.nanoTime();
                        for (Map.Entry<String, Conversation> entry : me.getConversations().entrySet()) {
                            Conversation c = entry.getValue();
                            Log.i("CONVERSATION_EACH", c.getId());
                            String key = "";
                            if (c.getType().equals("chat")) {
                                key = "U-" + c.getPhone_number();
                            } else if (c.getType().equals("channel")) {
                                key = "C-" + c.getId();
                            } else if (c.getType().equals("group")) {
                                key = "G-" + c.getId();
                            }
                            Log.i("CONVERSATION_TYPE", key);
                            //[fm] Test if conversation has been muted
                            Long timestampOfMood = -1L;
                            if (me.getExceptions() != null && me.getExceptions().containsKey(key)) {
                                Log.i("CONVERSATION_IN_EXCLUDE", Long.toString(me.getExceptions().get(key)));
                                timestampOfMood = me.getExceptions().get(key);
                            }
                            Log.i("CONVERSATION_E_TIME", Long.toString(timestampOfMood));

                            //[fm] Get conversation content
                            Query cQuery;
                            switch (c.getType()) {
                                case "channel":
                                    cQuery = (timestampOfMood > 0L) ? mChannelReference.child(c.getId()).child("messages")
                                            .orderByChild("timestamp").endAt(timestampOfMood).limitToLast(1) :
                                            mChannelReference.child(c.getId()).child("messages").orderByChild("timestamp")
                                                    .limitToLast(1);
                                    break;
                                case "group":
                                    cQuery = (timestampOfMood > 0L) ? mGroupReference.child(c.getId()).child("messages")
                                            .orderByChild("timestamp").endAt(timestampOfMood).limitToLast(1) :
                                            mGroupReference.child(c.getId()).child("messages").orderByChild("timestamp")
                                                    .limitToLast(1);
                                    break;
                                case "chat":
                                default:
                                    cQuery = (timestampOfMood > 0L) ? mChatReference.child(c.getId()).child("messages")
                                            .orderByChild("timestamp").endAt(timestampOfMood).limitToLast(1) :
                                            mChatReference.child(c.getId()).child("messages").orderByChild("timestamp")
                                                    .limitToLast(1);
                                    break;
                            }

                            //TODO: add listener variable
                            ValueEventListener listener = new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Long chatStart = System.nanoTime();
                                    GenericTypeIndicator<Map<String, Chat>> m =
                                            new GenericTypeIndicator<Map<String, Chat>>() {};

                                    Map<String, Chat> map = dataSnapshot.getValue(m);
                                    String lastMessageId;
                                    if (map == null) {
                                        Log.i("CONVERSATION_MSG_NULL", "NULL");
                                        lastMessageId = "";
                                    } else {
                                        List<Chat> msg = new ArrayList<>(map.values());
                                        lastMessageId =  msg.get(0).getMsgId();
                                    }
                                    Long chatEnd = System.nanoTime();
                                    Log.i("STOPWATCH_CHAT_FETCH", Long.toString((chatEnd - chatStart) / 1000000));
                                    //Test if message is empty
                                    Log.i("CONVERSATION_LAST_MSG", lastMessageId);

                                    //TODO: Test if messageId is empty
                                    if (lastMessageId.equals("")) {
                                        Log.i("CONVERSATION_LAST_EMPTY", "empty");
                                        c.setLastMessage(getString(R.string.no_msg_yet));
                                        c.setSeen(false);
                                        c.setFrom("+50900000000");
                                        c.setMessageTimestamp(c.getTimestamp());

                                        Query messageKindQuery = c.getType().equals("group") ? mGroupReference.child(c.getId()) :
                                                c.getType().equals("channel") ? mChannelReference.child(c.getId()) :
                                                        mChatReference.child(c.getId());

                                        messageKindQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.getValue() == null) { return; }
                                                Log.i("CONVERSATION_XTRA_CONF", dataSnapshot.getValue().toString());
                                                if (c.getType().equals("group")) {
                                                    Group g = dataSnapshot.getValue(Group.class);
                                                    c.setProfile_image(g.getThumbnail());
                                                    c.setName(g.getNewName().equals("") ? g.getName() : g.getNewName());
                                                } else if (c.getType().equals("channel")) {
                                                    Channel ch = dataSnapshot.getValue(Channel.class);
                                                    c.setProfile_image(ch.getThumbnail());
                                                    c.setName(ch.getNewName().equals("") ? ch.getName() : ch.getNewName());
                                                    List<String> l = new ArrayList<>(ch.getAdmins().keySet());
                                                    c.setAdmins(l);
                                                }

                                                mConvoList.remove(c);
                                                mConvoList.add(c);
                                                mChatAdapter.notifyDataSetChanged();
                                                //mChatAdapter.submitList(mConvoList);
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Query messageQuery = c.getType().equals("group") ? mGroupMessageReference
                                                .child(lastMessageId) : c.getType().equals("channel") ? mChannelMessageReference
                                                .child(lastMessageId) : mMessageReference.child(lastMessageId);

                                        //messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            messageQuery.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                //Log.i("CONVERSATION_COMP_MSG",  dataSnapshot.getValue().toString());
                                                Long msgStart = System.nanoTime();
                                                Messages m = dataSnapshot.getValue(Messages.class);
                                                if (m != null) {
                                                    Long msgEnd = System.nanoTime();
                                                    Log.i("STOPWATCH_MSG_FETCH", Long.toString((msgEnd - msgStart) / 1000000));
                                                    Log.i("CONVERSATION_DECORATION", "CALLING DECORATION");
                                                    String content = getMessageDecoration(m);
                                                   // Log.i("CONVERSATION_DECOR_MSG", content);

                                                    c.setMessageTimestamp(m.getTimestamp());
                                                    c.setLastMessage(content);
                                                    c.setSeen(m.isSeen());
                                                    c.setFrom(m.getFrom());
                                                    mChatAdapter.notifyDataSetChanged();

                                                    if (!m.getFrom().equals(mCurrentUserPhone)) {
                                                        //TODO: adding logic for unseen messages
                                                        int count = 0;
                                                        if (mConvoList.contains(c) ) {
                                                            count = getUnreadMessageCount(c.getId());
                                                        }
                                                        if (!m.isSeen()) {
                                                            count++;
                                                            setUnreadMessageCount(c.getId(), count);
                                                        }
                                                        c.setUnreadMessages(count);
                                                    }
                                                }

                                                if (c.getType().equals("chat")) {
                                                    Query userQuery = mUsersReference.child(c.getPhone_number());
                                                    userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        //userQuery.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Log.i("CONVERSATION_PROFILE", "FETCHING PROFILE PIC OF THE OTHER PERSON");
                                                            String image = Objects.requireNonNull
                                                                    (dataSnapshot.child("image").getValue()).toString();
                                                            c.setProfile_image(image);

                                                            String nameStored = Users.getLocalContactList().get(c.getPhone_number());
                                                            nameStored = nameStored != null && nameStored.length() > 0 ?
                                                                    nameStored : dataSnapshot.getKey();
                                                            c.setName(nameStored);

                                                            Long addToListStart = System.nanoTime();
                                                            mConvoList.remove(c);

                                                            addToList(c, mConvoList);
                                                            mChatAdapter.notifyDataSetChanged();
                                                            //mChatAdapter.submitList(mConvoList);
                                                            Long addToListEnd = System.nanoTime();
                                                            Log.i("STOPWATCH_ADD_LIST_U", Long.toString((addToListEnd - addToListStart) / 1000000));
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {
                                                        }
                                                    });
                                                } else {
                                                    Query messageKindQuery = c.getType().equals("group") ? mGroupReference.child(c.getId()) :
                                                            c.getType().equals("channel") ? mChannelReference.child(c.getId()) :
                                                                    mChatReference.child(c.getId());

                                                    messageKindQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        //messageKindQuery.addValueEventListener(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.getValue() == null) { return; }
                                                            Log.i("CONVERSATION_XTRA_CONF", dataSnapshot.getValue().toString());
                                                            if (c.getType().equals("group")) {
                                                                Group g = dataSnapshot.getValue(Group.class);
                                                                c.setProfile_image(g.getThumbnail());
                                                                c.setName(g.getNewName().equals("") ? g.getName() : g.getNewName());
                                                         //       c.setLastMessage(g.getLastMessage());
                                                            } else if (c.getType().equals("channel")) {
                                                                Channel ch = dataSnapshot.getValue(Channel.class);
                                                                c.setProfile_image(ch.getThumbnail());
                                                                c.setName(ch.getNewName().equals("") ? ch.getName() : ch.getNewName());
                                                               // c.setLastMessage(c.getLastMessage());
                                                                List<String> l = new ArrayList<>(ch.getAdmins().keySet());
                                                                c.setAdmins(l);
                                                            }
                                                            Long addToListStart = System.nanoTime();
                                                            mConvoList.remove(c);

                                                            addToList(c, mConvoList);
                                                       //     mChatAdapter.notifyDataSetChanged();
                                                           // mChatAdapter.submitList(mConvoList);
                                                            Long addToListEnd = System.nanoTime();
                                                            Log.i("STOPWATCH_ADD_LIST_CG", Long.toString((addToListEnd - addToListStart) / 1000000));
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
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            };

                            cQuery.addListenerForSingleValueEvent(listener);
                            //TODO: [fm] add to list of handles
                        }
                        Long end = System.nanoTime();
                        Log.i("STOPWATCH_LIST_CONV", Long.toString((end - start) / 1000000));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.i("CONVERSATION_DB_ERROR", databaseError.getDetails());
                    }
                });

    }

    private void addToList(Conversation c, List<Conversation> l) {
        l.add(c);
        Collections.sort(l, Collections.reverseOrder());
    }



    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User isn't logged in
            goToMain();

            // if user is among admin to channel to set channel to locked out
        }
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

    //TODO: [fm] Remove sharedPreference occurence in future version
    private int getUnreadMessageCount(String conversationId) {
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        return prefs.getInt(conversationId, 0);
    }

    private void setUnreadMessageCount(String conversationId, int count) {
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putInt(conversationId, count);
        editor.apply();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {

            //Thread t = new Thread(() -> getContacts());
            //t.start();getContacts();
            getContacts();

        } else {
            showSnack(ActivityCompat.shouldShowRequestPermissionRationale(this, WALK_THROUGH[0]));
        }
    }

    private void goToMain() {

        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    /*
     * Private method that verifies whether permission to read contacts has been granted to get
     * contact from phone
     */
    private void askPermission() {
        if (RunTimePermissionWrapper.isAllPermissionGranted(this, WALK_THROUGH)) {
            getContacts();

            switch (isClicked) {
                case 1:
                    try {
                        new CheckInternet_(internet -> {
                            if(internet){
                                startActivity(new Intent(this, AddTextActivity.class));
                            }else{
                                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 2:
                    try {
                        new CheckInternet_(internet -> {
                           if(internet){
                               choseMedia();
                           }else{
                               Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                           }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case 3:
                    try {
                        new CheckInternet_(internet -> {
                           if(internet){
                               selectVideoDialog();
                           }else{
                               Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                           }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                default:
                    return;
            }

        } else {
            RunTimePermissionWrapper.handleRunTimePermission(MainActivity.this, RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
        }
    }

    private void showSnack(final boolean isRationale) {
        final Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.contact_permission, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(isRationale ? "VIEW" : "Settings", view -> {
            snackbar.dismiss();

            if (isRationale)
                RunTimePermissionWrapper.handleRunTimePermission(MainActivity.this, RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
            else
                startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 1001);
        });

        snackbar.show();
    }

//    private void getContacts() {
//
//        Log.i("MainGetContact", "method called. id: " + Thread.currentThread().getId());
//
//        if (Users.isReady()) {
//            return;
//        }
//
//        Users.getLocalContactList().clear();
//
//        ContentResolver cr = getContentResolver();
//        // Read Contacts
//        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                new String[] {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME,
//                        ContactsContract.CommonDataKinds.Phone.NUMBER,
//                        ContactsContract.RawContacts.ACCOUNT_TYPE},
////                ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
//                ContactsContract.RawContacts.ACCOUNT_TYPE + " = 'com.google' ",
//                null, null);
//        if (cursor.getCount() > 0) {
//            while (cursor.moveToNext()) {
//                String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                Users.getLocalContactList().put(phoneNumber.replace("-", "")
//                        .replace("(", "")
//                        .replace(")", "")
//                        .replace(".", "")
//                        .replace("#", "")
//                        .replace("$", "")
//                        .replace("[", "")
//                        .replace("]", "")
//                        .replaceAll("\\s+", ""), name);
//            }
//        }
//
//        assert cursor != null;
//        cursor.close();
//        Users.setIsReady(true);
//    }

    private void getContacts(){

        if (Users.isReady()) {
            return;
        }

        Users.getLocalContactList().clear();

        ContentResolver cr = getContentResolver();

        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null,
                ContactsContract.RawContacts.ACCOUNT_TYPE + " = 'com.google' ");
        if (cursor != null) {
            HashSet<String> mobileNoSet = new HashSet<String>();
            try {
                final int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                String name, number;
                while (cursor.moveToNext()) {
                    name = cursor.getString(nameIndex);
                    number = cursor.getString(numberIndex);
                    number = number.replace(" ", "")
                            .replace("-", "")
                            .replace("(", "")
                        .replace(")", "")
                        .replace(".", "")
                        .replace("#", "")
                        .replace("$", "")
                        .replace("[", "")
                        .replace("]", "")
                        .replaceAll("\\s+", "");
                    if (!mobileNoSet.contains(number)) {
                        Users.getLocalContactList().put(number,name);
                        mobileNoSet.add(number);
                        Log.d("hvy", "onCreaterrView  Phone Number: name = " + name
                                + " No = " + number);
                    }
                }
            } finally {
                cursor.close();
                Users.setIsReady(true);
            }
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    private void presentShowcaseSeq() {

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID);

        sequence.setOnItemShownListener((itemView, position) -> {
        });

        sequence.setConfig(config);

        ShowcaseTooltip toolTip1 = ShowcaseTooltip.build(this)
                .corner(30)
                .textColor(Color.parseColor("#007686"))
                .text(getString(R.string.start_convo_));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(mPlusButton)
                        .setToolTip(toolTip1)
                        .setTooltipMargin(30)
                        .setShapePadding(50)
                        .setDismissText(R.string.got_it)
                        .setMaskColour(getResources().getColor(R.color.tooltip_mask))
                        .build()

        );

        //  sequence.addSequenceItem(mAddTextButton, getString(R.string.text_status_), getString(R.string.got_it));

        ShowcaseTooltip toolTip2 = ShowcaseTooltip.build(this)
                .corner(30)
                .textColor(Color.parseColor("#007686"))
                .text(getString(R.string.ad_sta_));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(myStatusLayout)
                        .setToolTip(toolTip2)
                        .setTooltipMargin(30)
                        .setShapePadding(50)
                        .setDismissText(R.string.got_it)
                        .setMaskColour(getResources().getColor(R.color.tooltip_mask))
                        .build()
        );

//        ShowcaseTooltip toolTip3 = ShowcaseTooltip.build(this)
//                .corner(30)
//                .textColor(Color.parseColor("#007686"))
//                .text(getString(R.string.status_pri));
//
//        sequence.addSequenceItem(
//                new MaterialShowcaseView.Builder(this)
//                        .setTarget(mStatusText)
//                        .setToolTip(toolTip3)
//                        .setTooltipMargin(30)
//                        .setShapePadding(50)
//                        .setDismissText(R.string.got_it)
//                        .setMaskColour(getResources().getColor(R.color.tooltip_mask))
//                        .build()
//        );


        sequence.start();

    }

    private void choseMedia() {
        Pix.start(MainActivity.this, Options.init()
        .setRequestCode(REQUEST_CODE)
        .setCount(10));
        Toast.makeText(this, R.string.pic_restr, Toast.LENGTH_SHORT).show();
//        FilePickerBuilder.getInstance().setMaxCount(1)
//                .setActivityTheme(R.style.LibAppTheme)
//                .pickPhoto(this);
//        startActivity(new Intent(this, EditImageActivity.class));
    }

    private void selectVideoDialog() {
        new VideoPicker(this) {
            @Override
            protected void onCameraClicked() {
                openVideoCapture();
            }

            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            protected void onGalleryClicked() {
                Intent intent = new Intent();
                intent.setTypeAndNormalize("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_video)), REQUEST_VIDEO_TRIMMER);
            }
        }.show();
    }

    private void openVideoCapture() {
        Intent videoCapture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(videoCapture, REQUEST_VIDEO_TRIMMER);
    }

    private void startTrimActivity(@NonNull Uri uri) {
        Intent intent = new Intent(this, VideoTrimmerActivity.class);
        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri));
        startActivityForResult(intent, REQUEST_VIDEO_TRIMMER_RESULT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_VIDEO_TRIMMER:
                    final Uri selectedUri = data.getData();
                    if (selectedUri != null) {
                        startTrimActivity(selectedUri);
                    } else {
                        Toast.makeText(this, getString(R.string.toast_cannot_retrieve_selected_video), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_VIDEO_TRIMMER_RESULT:
                    final Uri selectedVideoUri = data.getData();
                    if (selectedVideoUri != null) {

                        Map<String, Object> seenBy = new HashMap<>();
                        seenBy.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

                        DatabaseReference statusPush = mStatusReference.push();
                        String pushId = statusPush.getKey();

                        StorageReference filePath = mVideosStorage.child("ads_status_videos").child(pushId + ".mp4");
                        UploadTask uploadTask = filePath.putFile(Uri.fromFile(new File(selectedVideoUri.getPath())));

                        uploadTask.addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            Toast.makeText(this, MessageFormat.format("{0}%", (int) progress), Toast.LENGTH_LONG).show();
                        });

                        uploadTask.addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String downloadUrl = Objects.requireNonNull(task.getResult()
                                        .getDownloadUrl()).toString();

                                Map<String, Object> statusMap = new HashMap<>();

                                statusMap.put("seenBy", seenBy);
                                statusMap.put("content", downloadUrl);
                                statusMap.put("timestamp", ServerValue.TIMESTAMP);
                                statusMap.put("phoneNumber", mCurrentUserPhone);
                                statusMap.put("id", pushId);
                                statusMap.put("from", "video");
                                statusMap.put("textEntered", pushId);

                                mStatusReference.child(mCurrentUserPhone).child("s").child(pushId).updateChildren(statusMap);

                            }
                        });

                        uploadTask.addOnFailureListener(e -> {
                            Toast.makeText(this, "Amaterasu", Toast.LENGTH_LONG).show();
                        });
                    } else {
                        Toast.makeText(this, getString(R.string.toast_cannot_retrieve_selected_video), Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
        if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            final ArrayList<String> returnValue = Objects.requireNonNull(data).getStringArrayListExtra(Pix.IMAGE_RESULTS);
            ArrayList<String> compressedImages = new ArrayList<>();
            for (String s : Objects.requireNonNull(returnValue)) {
                compressedImages.add(compressImage(s));
            }
            editoptions.setSelectedlist(compressedImages);
            PixEditorJave.start(MainActivity.this, editoptions);
        } if (resultCode == Activity.RESULT_OK && requestCode == 124) {
            backgroundLayoutForStatus.setVisibility(View.GONE);
            mAddStatusLayout.setVisibility(View.GONE);
            mStatusPrivacy.setVisibility(View.GONE);

            // Passing the images to the adapter
            returnValue = data.getStringArrayListExtra(PixEditorJave.IMAGE_RESULTS);

            for (String s : Objects.requireNonNull(returnValue)) {
                images.add(Uri.fromFile(new File(s)));
            }
            mCropAndWriteTextAdapter = new CropAndWriteTextAdapter(images);
            recycler_view.setAdapter(mCropAndWriteTextAdapter);
            mCropAndWriteTextAdapter.notifyDataSetChanged();
            crop_add_text.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, R.string.btm_pic, Toast.LENGTH_SHORT).show();
            pic.setOnClickListener(v-> Toast.makeText(this, R.string.btm_pic, Toast.LENGTH_SHORT).show());
            mDoneBtn.setVisibility(View.VISIBLE);
            List<Integer> indexes = new ArrayList<>();
            Map<Integer, Object> textes = new HashMap<>();


            recyclerItemClickListener = new RecyclerItemClickListener(this, recycler_view, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    picturePath = images.get(position);
                    indexes.add(position);
                    if(indexes.size() > 1){
                        textes.put(indexes.get(indexes.size() -2),send_text.getText().toString());
                    }
                    send_text.setText("");
                        for(Integer key : textes.keySet()){
                            if(position == key){
                                send_text.setText((String)textes.get(position));
                            }
                        }

                    Picasso.get().load(picturePath).into(pic);
//                    Toast.makeText(MainActivity.this, String.valueOf(indexes), Toast.LENGTH_SHORT).show();
//                    pic.setContentDescription(send_text.getText().toString());

                    crop_btn.setOnClickListener(v-> CropImage.activity(picturePath).start(MainActivity.this));

                    mDoneBtn.setOnClickListener(v->{
                        crop_add_text.setVisibility(View.GONE);
                        mDoneBtn.setVisibility(View.GONE);
                        mChatAdapter.isClickable = true;
                        textes.put(position, send_text.getText().toString());
                        mProgressDialog.show();
                        for(int i = 0; i < images.size(); i++){
                            Map<String, Object> seenBy = new HashMap<>();
                            seenBy.put(mCurrentUserPhone, ServerValue.TIMESTAMP);
                            DatabaseReference statusPush = mStatusReference.push();
                            String pushId = statusPush.getKey();

                            StorageReference filePath = mImagesStorage.child("ads_status_images")
                                    .child(pushId + ".jpg");
                            int finalI = i;
                            filePath.putFile(images.get(i)).addOnCompleteListener(task -> {
                                if(task.isSuccessful()){
                                    String downloadUrl = Objects.requireNonNull(task.getResult())
                                            .getDownloadUrl().toString();

                                    Map<String, Object> statusMap = new HashMap<>();
                                    statusMap.put("seenBy", seenBy);
                                    statusMap.put("content", downloadUrl);
                                    statusMap.put("timestamp", ServerValue.TIMESTAMP);
                                    statusMap.put("phoneNumber", mCurrentUserPhone);
                                    statusMap.put("id", pushId);
                                    statusMap.put("from", "image");
                                    statusMap.put("textEntered",textes.get(finalI));

                                    mStatusReference.child(mCurrentUserPhone).child("s").child(pushId).updateChildren(statusMap).addOnCompleteListener(task1 -> {
                                        if (task.isSuccessful()) {
                                            mProgressDialog.dismiss();
                                            //  finish();
                                        }
                                    });
                                }
                            });
                        }
                        images.clear();
                        returnValue.clear();
                    });
                }
                @Override
                public void onItemLongClick(View view, int position) {}

            });
            recycler_view.addOnItemTouchListener(recyclerItemClickListener);

        }
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK){
                int pos = images.indexOf(picturePath);
                resultUri = result.getUri();
                images.set(pos, resultUri);
                Picasso.get().load(resultUri).into(pic);
                mCropAndWriteTextAdapter.notifyDataSetChanged();
            }
        }
        }


    private void updateSeen(Status s) {

        if (s.getSeenBy().containsKey(mCurrentUserPhone)) {
            return;
        }
        if (s.getId() == null) {
            return;
        }

        mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

        mRootReference.child("ads_status").child(s.getPhoneNumber()).child("s").child(s.getId()).child("seenBy")
                .updateChildren(mRead);

    }

    /*
     * Fetches all statuses
     * @author fm
     */
    private void fetchStatus() {
        Log.i("STATUS_FN", "CALLED");
        if (Users.getLocalContactList() == null || Users.getLocalContactList().isEmpty()) {
            return;
        }
        mStatusList.clear();
        Set<Map.Entry<String, String>> myList = Users.getLocalContactList().entrySet();
        for (Map.Entry<String, String> val : myList) {
            String phoneNumber = val.getKey();

            Query q = mStatusReference.child(phoneNumber).child("s").orderByChild("timestamp").startAt(getStartTimestamp());

            q.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (amIExcludedFrom(phoneNumber)) {
                        return;
                    }

                    GenericTypeIndicator<Map<String, Status>> gti = new GenericTypeIndicator<Map<String, Status>>() {
                    };
                    Map<String, Status> map = dataSnapshot.getValue(gti);
                    if (map == null) {
                        return;
                    }

                    String localContactName = val.getValue();
                    List<Status> l = new ArrayList<>(map.values());

                    UserStatus userStatus = new UserStatus(phoneNumber);
                    Log.i("STATUS_F_NAME ", localContactName);


                    localContactName = localContactName.length() > 12 ? localContactName
                            .substring(0, 8) + "..." : localContactName;
                    userStatus.setNameStoredInPhone(localContactName);
                    Log.i("STATUS_F_LIST_SIZE_LESS", Integer.toString(l.size()));
                    if (!l.isEmpty()) {
                        Collections.sort(l);
                        Status tmp = l.get(l.size() - 1);
                        userStatus.setContent(tmp.getContent());
                        userStatus.setTimestamp(tmp.getTimestamp());
                        userStatus.setStatusList(l);
                        if (userStatus.getPhoneNumber().equals(mCurrentUserPhone)) {
                            userStatus.setNameStoredInPhone(getString(R.string.me_));
                        }
                        mStatusList.remove(userStatus);
                        mStatusList.add(userStatus);
                    }
                    Log.i("STATUS_F_LIST_RECYCLER", Integer.toString(mStatusList.size()));

                    Collections.reverse(mStatusList);
                    Log.i("STATUS_F_DATASET", "CALLED");
                    mStatusAdapter.notifyDataSetChanged();
                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    /*
     * Returns the string that decorates the conversation list
     * @param m the message
     * @return the string to show
     * @author fm
     */
    private String getMessageDecoration(Messages m) {
        Long start = System.nanoTime();
        String content;
        switch (m.getType()) {
            case "text":
                content = (m.getContent()).length() > 25 ?
                        (m.getContent()).substring(0, 21) + "..." :
                        m.getContent();
                break;
            case "audio":
                content = "<font color=\"#FFA500\"<b>" +
                        "Audio</b></font>";
                break;
            case "image":
                content = "<font color=\"#7016a8\"<b>" +
                        "Image</b></font>";
                break;
            case "video":
                content = "<font color=\"#0929b1\"<b>" +
                        "Video</b></font>";
                break;
            case "document":
                content = "<font color=\"#dabf0f\"<b>" +
                        "Document</b></font>";
                break;
            case "channel_link":
                content = m.isVisible() ? "<font color=\"#018c06\"<b>" +
                        "Channel invitation</b></font>" :
                        "<font color=\"#FF5733\"<b>" +
                                "Message Deleted</b></font>";
                break;
            case "group_link":
                content = m.isVisible() ? "<font color=\"#018c06\"<b>" +
                        "Group invitation</b></font>" :
                        "<font color=\"#FF5733\"<b>" +
                                "Message Deleted</b></font>";
                break;
            case "contact":
                content = m.isVisible() ? "<font color=\"#e74c3c\"<b>" +
                        "Contact</b></font>" : "<font color=\"#FF5733\"<b>" +
                        "Message Deleted</b></font>";
                break;
            default:
                content = "No message yet";
        }
        Long end = System.nanoTime();
        Log.i("STOPWATCH_DECORATION", Long.toString((end - start) / 1000000));
        return content;
    }

    /*
     * Returns whether the current user phone number has been excluded
     * to view a status
     */
    private boolean amIExcludedFrom(String phoneNumber) {
        final boolean[] isExcluded = {false};

        mStatusReference.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("e")) {
                    Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                    if (m == null) {
                        return;
                    }

                    if (m.containsKey(mCurrentUserPhone)) {
                        isExcluded[0] = true;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return isExcluded[0];
    }

    /*
     * Returns the timestamp from yesterday same time
     * @return the {@link Long} timestamp
     * @author fm
     */
    private Long getStartTimestamp() {
        //TODO: [FM] Should not rely on system time due to timezone, and user manually changing phone date and time
        return System.currentTimeMillis() - 86400000L;
    }

    private void presentShowcaseSequence() {

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, SHOWCASE_ID_status);

        sequence.setOnItemShownListener((itemView, position) -> {
            //      Toast.makeText(itemView.getContext(), "Item #" + position, Toast.LENGTH_SHORT).show();
        });

        sequence.setConfig(config);

        ShowcaseTooltip toolTip1 = ShowcaseTooltip.build(this)
                .corner(30)
                .textColor(Color.parseColor("#007686"))
                .text(getString(R.string.text_status_));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(mTextCardView)
                        .setToolTip(toolTip1)
                        .setTooltipMargin(30)
                        .setShapePadding(50)
                        .setDismissText(R.string.got_it)
                        .setMaskColour(getResources().getColor(R.color.tooltip_mask))
                        .build()

        );

        //  sequence.addSequenceItem(mAddTextButton, getString(R.string.text_status_), getString(R.string.got_it));

        ShowcaseTooltip toolTip2 = ShowcaseTooltip.build(this)
                .corner(30)
                .textColor(Color.parseColor("#007686"))
                .text(getString(R.string.image_status__));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(mImageCardView)
                        .setToolTip(toolTip2)
                        .setTooltipMargin(30)
                        .setShapePadding(50)
                        .setDismissText(R.string.got_it)
                        .setMaskColour(getResources().getColor(R.color.tooltip_mask))
                        .build()
        );

        ShowcaseTooltip toolTip3 = ShowcaseTooltip.build(this)
                .corner(30)
                .textColor(Color.parseColor("#007686"))
                .text(getString(R.string.stats_video_));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(this)
                        .setTarget(mVideoCardView)
                        .setToolTip(toolTip3)
                        .setTooltipMargin(30)
                        .setShapePadding(50)
                        .setDismissText(R.string.got_it)
                        .setMaskColour(getResources().getColor(R.color.tooltip_mask))
                        .build()
        );

        sequence.start();
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filename;

    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "MyFolder/Images");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = Math.min(heightRatio, widthRatio);
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        crop_add_text.setVisibility(View.GONE);
        mDoneBtn.setVisibility(View.GONE);
        images.clear();
        returnValue.clear();
        mChatAdapter.isClickable = true;
        Toast.makeText(this, R.string.back, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }
}