package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.ChannelInteractionAdapter;
import marcelin.thierry.chatapp.adapters.NotificationAdapter;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.NotificationDropDownMenu;
import marcelin.thierry.chatapp.classes.ReplyNotification;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.Constant;
import marcelin.thierry.chatapp.utils.RecyclerItemClickListener;

public class ChannelSubscriberActivity extends AppCompatActivity {

    // private String mLastKey = "";
    // private String mPrevKey = "";
    private String mCurrentUserPhone;

    private String mChannelName;
    private String mChannelImage;
    private ArrayList<String> mChannelAdmins;

    //private int mCurrentPage = 1;
    private int itemPosition = 0;

    private ChannelInteractionAdapter mChannelInteractionAdapter;

//    private NestedScrollView mSwipeRefreshLayout;
    private LinearLayoutManager mLinearLayoutManager;

    private Toolbar mChatToolbar;

    private List<Messages> messagesList = new ArrayList<>();
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private View mRootView;
    private CircleImageView mProfileImage;
    private RecyclerView mMessagesList;

    private Map<String, Object> mRead = new HashMap<>();

    private static boolean isOnActivity = false;

    private static final int TOTAL_ITEMS_TO_LOAD = 30;
    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();
    private static final DatabaseReference mMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel_messages");
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    private TextView title;
    private CircleImageView profileImage;
    private ImageView backButton;
    private ImageView notification_bell;
    private TextView unseenReplies;

    private List<ReplyNotification> replyNotificationsList = new ArrayList<>();
    private NotificationAdapter mNotificationAdapter;

    private RecyclerItemClickListener recyclerItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_channel_subscriber);

        mChannelName = getIntent().getStringExtra("Channel_id");
        mChannelImage = getIntent().getStringExtra("profile_image");
        mChannelAdmins = getIntent().getStringArrayListExtra("admins");
        mChatToolbar = findViewById(R.id.chat_bar_main);
        //   setSupportActionBar(mChatToolbar);

        mProfileImage = findViewById(R.id.profileImage);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setDisplayShowCustomEnabled(true);
        mMessagesList = findViewById(R.id.messages_list);
//        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);

        mRootView = findViewById(R.id.rootView);
        mRootView.setBackgroundColor(ContextCompat.getColor(this, R.color.channel_background));
        title = findViewById(R.id.title);
        backButton = findViewById(R.id.backButton);
        // actionBar.setTitle(mChannelName);

        title.setText(mChannelName);

        mLinearLayoutManager = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayoutManager);

        mChannelInteractionAdapter = new ChannelInteractionAdapter(messagesList, this, this);
        mNotificationAdapter = new NotificationAdapter(this, replyNotificationsList);
        mMessagesList.setAdapter(mChannelInteractionAdapter);

        //  getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_try));

        mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        notification_bell = findViewById(R.id.notification_bell);
        unseenReplies = findViewById(R.id.unseenReplies);

        loadMessages();
        getComments();

//        mSwipeRefreshLayout.setOnRefreshListener(() -> {
//
//            //      mCurrentPage++;
//
//            itemPosition = 0;
//
//            loadMoreMessages();
//
//        });

        Picasso.get().load(mChannelImage).into(mProfileImage);

        listenerOnMessage();

        mChatToolbar.setOnClickListener(view -> {
            Intent goToSubActivitySettings = new Intent(ChannelSubscriberActivity.this,
                    SubSettingsActivity.class);
            goToSubActivitySettings.putExtra("chat_id", mChannelName);
            startActivity(goToSubActivitySettings);

        });

        backButton.setOnClickListener(v ->{
            finish();
        });

        notification_bell.setVisibility(View.VISIBLE);

        notification_bell.setOnClickListener(v ->{
            if(unseenReplies.getVisibility() == View.VISIBLE){
                showNotifications();
            }else{
                Toast.makeText(this, R.string.no_notif, Toast.LENGTH_SHORT).show();
            }
        });
        mRootView.setBackgroundColor(Color.parseColor("#ececec"));

        recyclerItemClickListener = new RecyclerItemClickListener(this, mMessagesList, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Messages message = messagesList.get(position);
                if(message.isVisible() && message.getType().equals("text")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.subscriber_channel_options, ((dialog, which) -> {
                                switch (which){
                                    case 0:
                                        Intent goToCommentActivity = new Intent(ChannelSubscriberActivity.this, CommentActivity.class);
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
                                }
                            }));

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });
        mMessagesList.addOnItemTouchListener(recyclerItemClickListener);
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
        //mMessageAdapter.stopMediaPlayers();
        Log.i("ChatActivity", "onStop() called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isOnActivity = false;
//        mChannelInteractionAdapter.stopMediaPlayers();
        //listenerOnMessage() detachListener

    }

    @Override
    protected void onPause() {
        super.onPause();
        isOnActivity = false;
        Log.i("ChatActivity", "onPause() called");
    }

    private void getComments(){
        mUsersReference.child(mCurrentUserPhone).child("r").child(mChannelName).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    ReplyNotification replyNotification = dataSnapshot.getValue(ReplyNotification.class);
                    if(replyNotification == null){
                        return;
                    }
                    if(!replyNotification.getRe().equals(mCurrentUserPhone)){
                        if(!replyNotification.isSe()){
                            unseenReplies.setVisibility(View.VISIBLE);
                            mUsersReference.child(replyNotification.getRe()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Users u = dataSnapshot.getValue(Users.class);
                                    if(u == null){
                                        return;
                                    }
                                    replyNotification.setReplyImage(u.getThumbnail());
                                    replyNotification.setReplierName(u.getName());
                                    replyNotificationsList.add(replyNotification);
                                    mNotificationAdapter.notifyDataSetChanged();

                                    if(replyNotificationsList.isEmpty()){
                                        // do something in the list
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                    }
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
        menu.setWidth(getPxFromDp(350));
        menu.setOutsideTouchable(true);
        menu.setFocusable(true);
        menu.showAsDropDown(notification_bell);
    //    menu.setAnimationStyle(R.style.emoji_fade_animation_style);
        menu.setNotificationSelectedListener((position, replyNotification) -> {
            menu.dismiss();
            // New Intent to CommentActivity
            mUsersReference.child(mCurrentUserPhone).child("r").child(mChannelName).child(replyNotification.getR()).child("se").setValue(true);
            Intent goToCommentActivity = new Intent(ChannelSubscriberActivity.this, CommentActivity.class);
            goToCommentActivity.putExtra("channel_name", replyNotification.getCh());
            goToCommentActivity.putExtra("channel_image",replyNotification.getChi());
            goToCommentActivity.putExtra("message_type", replyNotification.getTy());
            goToCommentActivity.putExtra("message_id",replyNotification.getId());
            goToCommentActivity.putExtra("message_color",replyNotification.getCol());
            goToCommentActivity.putExtra("message_content",replyNotification.getCo());
            goToCommentActivity.putExtra("message_timestamp", replyNotification.getT2());
            goToCommentActivity.putExtra("message_like", replyNotification.getL());
            goToCommentActivity.putExtra("message_comment", replyNotification.getCc());
            goToCommentActivity.putExtra("message_seen", replyNotification.getS());
            goToCommentActivity.putExtra("reply_id", replyNotification.getR());
            goToCommentActivity.putExtra("from", "Activity");
            goToCommentActivity.putExtra("isOn", true);
            goToCommentActivity.putExtra("comment_id", replyNotification.getC());
          //  goToCommentActivity.putExtra("message_color", replyNotification.getCol());
            startActivity(goToCommentActivity);

        });
    }

    private int getPxFromDp(int i) {
        return (int) (i * getResources().getDisplayMetrics().density);
    }

    private void loadMessages() {

        DatabaseReference conversationRef;
        try{

            conversationRef = mRootReference.child("ads_channel")
                    .child(mChannelName).child("messages");
            conversationRef.keepSynced(true);

        }catch (Exception e){
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
                        //TODO: adding logic
                        mRootReference.child(Constant.ROOT_REF).child(mCurrentUserPhone)
                                .child(Constant.USR_MOOD_EXCEPT_NODE).child("C-"+ mChannelName)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            Log.i("CHANNEL_IN_EXCEPT", dataSnapshot.getValue().toString());
                                            Long mutedTimestamp = (Long) dataSnapshot.getValue();
                                            if (mutedTimestamp < m.getTimestamp()) {
                                                Log.i("CHANNEL_MSG_MUTED", "message muted on "
                                                        + mutedTimestamp);
                                                return;
                                            }
                                        }
                                        m.setMessageId(chatRef.getMsgId());
                                        if (isOnActivity){// && !m.getFrom().equals(mCurrentUserPhone)) {
                                            updateSeen(m);
                                        }
                                        m.setChannelName(mChannelName);
                                        m.setChannelImage(mChannelImage);
                                        m.setAdmins(mChannelAdmins);
                                        messagesList.add(m);
                                        mChannelInteractionAdapter.notifyDataSetChanged();
                                        mMessagesList.scrollToPosition(messagesList.size() - 1);
//                                        mSwipeRefreshLayout.setRefreshing(false);
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

//    private void loadMoreMessages() {
//
//        DatabaseReference conversationRef = mRootReference.child("ads_channel").child(mChannelName)
//                .child("messages");
//        conversationRef.keepSynced(true);
//
//        DatabaseReference messageRef = mRootReference.child("ads_channel_messages");
//        messageRef.keepSynced(true);
//
//        String mLastKey = messagesList.get(0).getMessageId();
//        Query conversationQuery = conversationRef.orderByKey().endAt(mLastKey).limitToLast(10);
//
//        conversationQuery.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                Chat chatRef = dataSnapshot.getValue(Chat.class);
//                if (chatRef == null) {
//                    return;
//                }
//                messageRef.child(chatRef.getMsgId()).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot) {
//                        Messages m = dataSnapshot.getValue(Messages.class);
//                        if (m == null) {
//                            return;
//                        }
//
//                        m.setMessageId(chatRef.getMsgId());
//                        if (!m.getMessageId().equals(mLastKey)) {
//                            messagesList.add(itemPosition++, m);
//                            mChannelInteractionAdapter.notifyDataSetChanged();
//                        }
//
//                        mLinearLayoutManager.scrollToPositionWithOffset(10, 0);
//                        mSwipeRefreshLayout.setRefreshing(false);
//
//
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError) {
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//    }

    private void updateSeen(Messages m) {

        if (m.getRead_by().containsKey(mCurrentUserPhone)) { return; }
        if (m.getMessageId() == null) { return; }

        mRead.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

        mRootReference.child("ads_channel_messages").child(m.getMessageId()).child("read_by")
                .updateChildren(mRead);

        //    mRootReference.child("ads_channel_messages").child(m.getMessageId()).updateChildren(mRead);

    }

    private void listenerOnMessage(){

        mMessageReference.addChildEventListener(new ChildEventListener() {
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
