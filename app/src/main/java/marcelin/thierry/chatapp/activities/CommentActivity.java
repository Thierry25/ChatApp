package marcelin.thierry.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.CommentAdapter;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.Users;
import marcelin.thierry.chatapp.utils.CheckInternet_;

public class CommentActivity extends AppCompatActivity {

    private CircleImageView mChannelImage, profilePic;
    private EmojiTextView mChannelName, textEntered, commentName;
    private EmojiEditText mSendText;
    private TextView mTimestamp, numberOfLikes, numberOfComments, numberOfSeen;
    private ImageView mMoreSettings;
    private LinearLayout messageLayout;
    private RecyclerView commentList;
    private LinearLayoutManager mLinearLayoutManager;

    private ImageButton mSendEmoji, mSend;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String myPhone;

    String channelName;
    String messageId;

    private ConstraintLayout commentMessage, commentImage;

    private List<Messages> messagesList = new ArrayList<>();
    private CommentAdapter commentAdapter;

    private DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference().child("ads_channel");
    private DatabaseReference mCommentReference = FirebaseDatabase.getInstance().getReference().child("c");
    private DatabaseReference mChannelMessagesReference = FirebaseDatabase.getInstance().getReference().child("ads_channel_messages");

    private DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();

    private static final int TOTAL_ITEMS_TO_LOAD = 30;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    private NestedScrollView nestedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mChannelImage = findViewById(R.id.channel_image);
        mChannelName = findViewById(R.id.channel_name);
        mTimestamp = findViewById(R.id.timestamp);
        mMoreSettings = findViewById(R.id.more_settings);
        textEntered = findViewById(R.id.textEntered);
        messageLayout = findViewById(R.id.messageLayout);
        mSendEmoji = findViewById(R.id.send_emoji);
        mSendText = findViewById(R.id.send_text);
        mSend = findViewById(R.id.send);
        myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
        commentList = findViewById(R.id.commentList);
        profilePic = findViewById(R.id.profilePic);
        commentName = findViewById(R.id.commentName);
        numberOfComments = findViewById(R.id.numberOfComments);
        numberOfLikes = findViewById(R.id.numberOfLikes);
        numberOfSeen = findViewById(R.id.numberOfSeen);
        commentMessage = findViewById(R.id.comment_message);
        commentImage = findViewById(R.id.comment_image);

        channelName = getIntent().getStringExtra("channel_name");
        String channelImage = getIntent().getStringExtra("channel_image");
        String messageType = getIntent().getStringExtra("message_type");
        messageId = getIntent().getStringExtra("message_id");
        String messageContent = getIntent().getStringExtra("message_content");
        String color = getIntent().getStringExtra("message_color");
        long timestamp = getIntent().getLongExtra("message_timestamp", 0);
        int likes = getIntent().getIntExtra("message_like", 0);
        int comments = getIntent().getIntExtra("message_comment", 0);
        int seen = getIntent().getIntExtra("message_seen", 0);

        mLinearLayoutManager = new LinearLayoutManager(this);

        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(mLinearLayoutManager);
        commentList.setNestedScrollingEnabled(false);

        commentAdapter = new CommentAdapter(messagesList, this);
        commentList.setAdapter(commentAdapter);
        nestedView = findViewById(R.id.nestedView);

        Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(profilePic);
        commentName.setText(channelName);

        mTimestamp.setText(getTimeAgo(timestamp, this));
        numberOfSeen.setText(String.valueOf(seen));
        numberOfLikes.setText(String.valueOf(likes));
        numberOfComments.setText(String.valueOf(comments));

        getComments();

        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#000000");
        commentContentMap.put("from", myPhone);
        commentContentMap.put("parent", "Default");
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);


        switch (messageType){

            case "text":
                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);

                if(color.equals("#7016a8") || color.equals("#FFFFFF")){
                    textEntered.setTextColor(Color.parseColor("#000000"));
                    messageLayout.setGravity(Gravity.START);
                }

                if(!color.equals("#7016a8") && !color.equals("#FFFFFF") && messageContent.length() < 150){
                    messageLayout.setBackgroundColor(Color.parseColor(color));
                    ViewGroup.LayoutParams params = messageLayout.getLayoutParams();
                    params.height = 500;
                    messageLayout.setLayoutParams(params);
                    textEntered.setTextColor(Color.parseColor("#FFFFFF"));
                }
                textEntered.setText(messageContent);

                mSend.setOnClickListener(v -> new CheckInternet_(internet -> {
                   if(internet){
                       String message = mSendText.getText().toString().trim();

                       if (!TextUtils.isEmpty(message)) {
                           commentContentMap.put("content", message);
                           DatabaseReference msg_push = mChannelReference.push();
                           String push_id = msg_push.getKey();
                           Map<String, Object> commentMap = new HashMap<>();
                           commentMap.put(push_id, commentContentMap);

                           mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                           });

                           Map<String, Object> commentLink = new HashMap<>();
                           commentLink.put("msgId", push_id);
                           commentLink.put("timestamp", ServerValue.TIMESTAMP);

                           Map<String, Object> commentByMap = new HashMap<>();
                           commentByMap.put(myPhone, ServerValue.TIMESTAMP);

                           mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
                                   .updateChildren(commentLink, (databaseError, databaseReference) -> {
                                   });

                           mChannelMessagesReference.child(messageId).child("c").child(push_id)
                                   .updateChildren(commentByMap, (databaseError, databaseReference) ->{
                                   });
                           mSendText.setText("");
                       }else{
                           Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
                       }
                   }else{
                       Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                   }
                }));

                break;

            case "image":

                commentMessage.setVisibility(View.GONE);
                commentImage.setVisibility(View.VISIBLE);


                break;

        }

    }

    private void getComments(){
        DatabaseReference conversationRef;
        try {

            conversationRef = mRootReference.child("ads_channel")
                    .child(channelName).child("messages").child(messageId).child("c");
            conversationRef.keepSynced(true);
            nestedView.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            nestedView.setVisibility(View.GONE);
            return;
        }

        mCommentReference.keepSynced(true);

        Query conversationQuery = conversationRef.limitToLast(TOTAL_ITEMS_TO_LOAD);

        conversationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Chat chatRef = dataSnapshot.getValue(Chat.class);
                if(chatRef == null){
                    return;
                }
                mCommentReference.child(chatRef.getMsgId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Messages message = dataSnapshot.getValue(Messages.class);
                                if(message == null){
                                    return;
                                }

                                mRootReference.child("ads_users").child(message.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Users u = dataSnapshot.getValue(Users.class);
                                        if(u == null){
                                            return;
                                        }

                                        message.setName(u.getName());
                                        message.setProfilePic(u.getThumbnail());
                                        message.setMessageId(chatRef.getMsgId());
                                        messagesList.add(message);
                                        commentAdapter.notifyDataSetChanged();
                                        commentList.scrollToPosition(messagesList.size() - 1);
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


    public static String getTimeAgo(long date, Context context) {
        Date now = Calendar.getInstance().getTime();
        final long diff = now.getTime() - date;

        if (diff < SECOND_MILLIS) {
            return context.getString(R.string.just_now);
        } else if (diff < MINUTE_MILLIS) {
            return diff / SECOND_MILLIS + context.getString(R.string.seconds_ago);
        } else if (diff < 2 * MINUTE_MILLIS) {
            return context.getString(R.string.a_minute_ago);
        } else if (diff < 59 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + context.getString(R.string.minutes_ago);
        } else if (diff < 90 * MINUTE_MILLIS) {
            return context.getString(R.string.an_hour_ago);
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + context.getString(R.string.hours_ago);
        } else if (diff < 48 * HOUR_MILLIS) {
            return context.getString(R.string.yesterday);
        } else if (diff < 6 * DAY_MILLIS) {
            return diff / DAY_MILLIS + context.getString(R.string.days_ago);
        } else if (diff < 11 * DAY_MILLIS) {
            return context.getString(R.string.a_week_ago);
        } else {
            return diff / WEEK_MILLIS + context.getString(R.string.weeks_ago);
        }
    }

}
