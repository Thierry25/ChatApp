package marcelin.thierry.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
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
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.CommentAdapter;
import marcelin.thierry.chatapp.adapters.RepliesAdapter;
import marcelin.thierry.chatapp.classes.Chat;
import marcelin.thierry.chatapp.classes.CommentChannel;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.ReplyNotification;
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
    private LinearLayout messageLinLayout;

    private ImageButton mSendEmoji, mSend;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String myPhone;

    private String channelName;
    private String messageId;
    private String channelImage;
    private String messageType;
    private String messageContent;
    private String color;
    private String commentId;
    String commentPosition;

    private boolean isOn;

    long timestamp;
    int likes, comments, seen;

    private LinearLayout rootView;
    private EmojiPopup emojiPopup;


    private ConstraintLayout commentMessage, commentImage, commentAudio, commentVideo, commentDocument;

    private List<CommentChannel> commentsAndReplies = new ArrayList<>();
    //private Map<String, List<Messages>> replyMap = new HashMap<>();
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
        commentAudio = findViewById(R.id.comment_audio);
        commentVideo = findViewById(R.id.comment_video);
        commentDocument = findViewById(R.id.comment_document);
        rootView = findViewById(R.id.rootView);
        messageLinLayout = findViewById(R.id.messageLinLayout);

        channelName = getIntent().getStringExtra("channel_name");
        channelImage = getIntent().getStringExtra("channel_image");
        messageType = getIntent().getStringExtra("message_type");
        messageId = getIntent().getStringExtra("message_id");
        messageContent = getIntent().getStringExtra("message_content");
        color = getIntent().getStringExtra("message_color");
        timestamp = getIntent().getLongExtra("message_timestamp", 0);
        likes = getIntent().getIntExtra("message_like", 0);
        comments = getIntent().getIntExtra("message_comment", 0);
        seen = getIntent().getIntExtra("message_seen", 0);
        String from = getIntent().getStringExtra("from");
        isOn = getIntent().getBooleanExtra("isOn", false);
        commentId = getIntent().getStringExtra("comment_id");
        commentPosition = getIntent().getStringExtra("reply_id");

        mLinearLayoutManager = new LinearLayoutManager(this);

        commentList.setHasFixedSize(true);
        commentList.setLayoutManager(mLinearLayoutManager);
        commentList.setNestedScrollingEnabled(false);

        commentAdapter = new CommentAdapter(commentsAndReplies, this, isOn);
        commentList.setAdapter(commentAdapter);
        nestedView = findViewById(R.id.nestedView);

        Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(profilePic);
        commentName.setText(channelName);

        mTimestamp.setText(getTimeAgo(timestamp, this));
        numberOfSeen.setText(String.valueOf(seen));
        numberOfLikes.setText(String.valueOf(likes));
        numberOfComments.setText(String.valueOf(comments));


        if(from.equals("Adapter")){
            getCommentsAndReplies();
        }else{
            getSingleCommentAndReplies();
        }

        Map<String, Object> repliesMap = new HashMap<>();
        repliesMap.put(myPhone, myPhone);

        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#000000");
        commentContentMap.put("from", myPhone);
        commentContentMap.put("parent", "Default");
        commentContentMap.put("r", repliesMap);
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);


        switch (messageType) {

            case "text":

                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);

                if (color.equals("#7016a8") || color.equals("#FFFFFF")) {
                    textEntered.setTextColor(Color.parseColor("#000000"));
                    messageLayout.setGravity(Gravity.START);
                }

                if (!color.equals("#7016a8") && !color.equals("#FFFFFF") && messageContent.length() < 150) {
                    messageLayout.setBackgroundColor(Color.parseColor(color));
                    ViewGroup.LayoutParams params = messageLayout.getLayoutParams();
                    params.height = 500;
                    messageLayout.setLayoutParams(params);
                    textEntered.setTextColor(Color.parseColor("#FFFFFF"));
                }
                textEntered.setText(messageContent);

                mSend.setOnClickListener(v -> new CheckInternet_(internet -> {
                    if (internet) {

                        send(mSendText, commentContentMap);
//                       if (!TextUtils.isEmpty(message)) {
//                           commentContentMap.put("content", message);
//                           DatabaseReference msg_push = mChannelReference.push();
//                           String push_id = msg_push.getKey();
//                           Map<String, Object> commentMap = new HashMap<>();
//                           commentMap.put(push_id, commentContentMap);
//
//                           mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
//                           });
//
//                           Map<String, Object> commentLink = new HashMap<>();
//                           commentLink.put("msgId", push_id);
//                           commentLink.put("timestamp", ServerValue.TIMESTAMP);
//
//                           Map<String, Object> commentByMap = new HashMap<>();
//                           commentByMap.put(myPhone, ServerValue.TIMESTAMP);
//
//                           mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
//                                   .updateChildren(commentLink, (databaseError, databaseReference) -> {
//                                   });
//
//                           mChannelMessagesReference.child(messageId).child("c").child(push_id)
//                                   .updateChildren(commentByMap, (databaseError, databaseReference) ->{
//                                   });
//                           mSendText.setText("");
                        //     }
//                   else{
//                           Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
//
                    } else {
                        Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                }));

                break;

            case "image":

                TextView mTimestamp, numberOfLikes, numberOfComments, numberOfSeen;
                EmojiTextView mChannelName;
                CircleImageView mChannelImage;
                ImageView mMoreSettings, messageImage;

                mTimestamp = commentImage.findViewById(R.id.timestamp);
                numberOfLikes = commentImage.findViewById(R.id.numberOfLikes);
                numberOfComments = commentImage.findViewById(R.id.numberOfComments);
                numberOfSeen = commentImage.findViewById(R.id.numberOfSeen);
                messageImage = commentImage.findViewById(R.id.messageImage);

                mMoreSettings = commentImage.findViewById(R.id.more_settings);
                mChannelImage = commentImage.findViewById(R.id.channel_image);
                mChannelName = commentImage.findViewById(R.id.channel_name);


                mTimestamp.setText(getTimeAgo(timestamp, this));
                numberOfSeen.setText(String.valueOf(seen));
                numberOfLikes.setText(String.valueOf(likes));
                numberOfComments.setText(String.valueOf(comments));

                commentMessage.setVisibility(View.GONE);
                commentImage.setVisibility(View.VISIBLE);
                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);

                Picasso.get().load(messageContent).placeholder(R.drawable.ic_avatar).into(messageImage);

                mSend.setOnClickListener(v -> new CheckInternet_(internet -> {
                    if (internet) {

                        send(mSendText, commentContentMap);
//                        String message = mSendText.getText().toString().trim();
//
//                        if (!TextUtils.isEmpty(message)) {
//                            commentContentMap.put("content", message);
//                            DatabaseReference msg_push = mChannelReference.push();
//                            String push_id = msg_push.getKey();
//                            Map<String, Object> commentMap = new HashMap<>();
//                            commentMap.put(push_id, commentContentMap);
//
//                            mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
//                            });
//
//                            Map<String, Object> commentLink = new HashMap<>();
//                            commentLink.put("msgId", push_id);
//                            commentLink.put("timestamp", ServerValue.TIMESTAMP);
//
//                            Map<String, Object> commentByMap = new HashMap<>();
//                            commentByMap.put(myPhone, ServerValue.TIMESTAMP);
//
//                            mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentLink, (databaseError, databaseReference) -> {
//                                    });
//
//                            mChannelMessagesReference.child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentByMap, (databaseError, databaseReference) ->{
//                                    });
//                            mSendText.setText("");
//                        }else{
//                            Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                }));

                break;

            case "audio":

                TextView audioTime, audioText;
                ImageView playAudio;
                SeekBar audioSeekbar;

                audioTime = commentAudio.findViewById(R.id.audio_time);
                playAudio = commentAudio.findViewById(R.id.play_audio);
                audioSeekbar = commentAudio.findViewById(R.id.audio_seekbar);
                audioText = commentAudio.findViewById(R.id.audioText);

                commentMessage.setVisibility(View.GONE);
                //  commentImage.setVisibility(View.GONE);
                commentAudio.setVisibility(View.VISIBLE);

                mTimestamp = commentAudio.findViewById(R.id.timestamp);
                numberOfLikes = commentAudio.findViewById(R.id.numberOfLikes);
                numberOfComments = commentAudio.findViewById(R.id.numberOfComments);
                numberOfSeen = commentAudio.findViewById(R.id.numberOfSeen);

                mMoreSettings = commentAudio.findViewById(R.id.more_settings);
                mChannelImage = commentAudio.findViewById(R.id.channel_image);
                mChannelName = commentAudio.findViewById(R.id.channel_name);

                mTimestamp.setText(getTimeAgo(timestamp, this));
                numberOfSeen.setText(String.valueOf(seen));
                numberOfLikes.setText(String.valueOf(likes));
                numberOfComments.setText(String.valueOf(comments));

                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);
                audioSeekbar.setVisibility(View.GONE);
                playAudio.setVisibility(View.GONE);
                audioTime.setVisibility(View.GONE);
                audioText.setVisibility(View.VISIBLE);

                mSend.setOnClickListener(v -> new CheckInternet_(internet -> {
                    if (internet) {
                        send(mSendText, commentContentMap);
//                        String message = mSendText.getText().toString().trim();
//
//                        if (!TextUtils.isEmpty(message)) {
//                            commentContentMap.put("content", message);
//                            DatabaseReference msg_push = mChannelReference.push();
//                            String push_id = msg_push.getKey();
//                            Map<String, Object> commentMap = new HashMap<>();
//                            commentMap.put(push_id, commentContentMap);
//
//                            mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
//                            });
//
//                            Map<String, Object> commentLink = new HashMap<>();
//                            commentLink.put("msgId", push_id);
//                            commentLink.put("timestamp", ServerValue.TIMESTAMP);
//
//                            Map<String, Object> commentByMap = new HashMap<>();
//                            commentByMap.put(myPhone, ServerValue.TIMESTAMP);
//
//                            mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentLink, (databaseError, databaseReference) -> {
//                                    });
//
//                            mChannelMessagesReference.child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentByMap, (databaseError, databaseReference) ->{
//                                    });
//                            mSendText.setText("");
//                        }else{
//                            Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                }));

                break;

            case "video":
                commentMessage.setVisibility(View.GONE);
                commentVideo.setVisibility(View.VISIBLE);
                mTimestamp = commentVideo.findViewById(R.id.timestamp);
                numberOfLikes = commentVideo.findViewById(R.id.numberOfLikes);
                numberOfComments = commentVideo.findViewById(R.id.numberOfComments);
                numberOfSeen = commentVideo.findViewById(R.id.numberOfSeen);

                mMoreSettings = commentVideo.findViewById(R.id.more_settings);
                mChannelImage = commentVideo.findViewById(R.id.channel_image);
                mChannelName = commentVideo.findViewById(R.id.channel_name);

                mTimestamp.setText(getTimeAgo(timestamp, this));
                numberOfSeen.setText(String.valueOf(seen));
                numberOfLikes.setText(String.valueOf(likes));
                numberOfComments.setText(String.valueOf(comments));

                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);

                mSend.setOnClickListener(v -> new CheckInternet_(internet -> {
                    if (internet) {
                        send(mSendText, commentContentMap);
//                        String message = mSendText.getText().toString().trim();
//
//                        if (!TextUtils.isEmpty(message)) {
//                            commentContentMap.put("content", message);
//                            DatabaseReference msg_push = mChannelReference.push();
//                            String push_id = msg_push.getKey();
//                            Map<String, Object> commentMap = new HashMap<>();
//                            commentMap.put(push_id, commentContentMap);
//
//                            mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
//                            });
//
//                            Map<String, Object> commentLink = new HashMap<>();
//                            commentLink.put("msgId", push_id);
//                            commentLink.put("timestamp", ServerValue.TIMESTAMP);
//
//                            Map<String, Object> commentByMap = new HashMap<>();
//                            commentByMap.put(myPhone, ServerValue.TIMESTAMP);
//
//                            mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentLink, (databaseError, databaseReference) -> {
//                                    });
//
//                            mChannelMessagesReference.child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentByMap, (databaseError, databaseReference) ->{
//                                    });
//                            mSendText.setText("");
//                        }else{
//                            Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                }));


                break;

            case "document":

                TextView documentLink = commentDocument.findViewById(R.id.documentLink);
                commentMessage.setVisibility(View.GONE);
                commentDocument.setVisibility(View.VISIBLE);
                documentLink.setVisibility(View.GONE);
                mTimestamp = commentDocument.findViewById(R.id.timestamp);
                numberOfLikes = commentDocument.findViewById(R.id.numberOfLikes);
                numberOfComments = commentDocument.findViewById(R.id.numberOfComments);
                numberOfSeen = commentDocument.findViewById(R.id.numberOfSeen);

                mMoreSettings = commentDocument.findViewById(R.id.more_settings);
                mChannelImage = commentDocument.findViewById(R.id.channel_image);
                mChannelName = commentDocument.findViewById(R.id.channel_name);

                mTimestamp.setText(getTimeAgo(timestamp, this));
                numberOfSeen.setText(String.valueOf(seen));
                numberOfLikes.setText(String.valueOf(likes));
                numberOfComments.setText(String.valueOf(comments));

                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).placeholder(R.drawable.ic_avatar).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);

                mSend.setOnClickListener(v -> new CheckInternet_(internet -> {
                    if (internet) {
                        send(mSendText, commentContentMap);
//                        String message = mSendText.getText().toString().trim();
//
//                        if (!TextUtils.isEmpty(message) ){
//                            commentContentMap.put("content", message);
//                            DatabaseReference msg_push = mChannelReference.push();
//                            String push_id = msg_push.getKey();
//                            Map<String, Object> commentMap = new HashMap<>();
//                            commentMap.put(push_id, commentContentMap);
//
//                            mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
//                            });
//
//                            Map<String, Object> commentLink = new HashMap<>();
//                            commentLink.put("msgId", push_id);
//                            commentLink.put("timestamp", ServerValue.TIMESTAMP);
//
//                            Map<String, Object> commentByMap = new HashMap<>();
//                            commentByMap.put(myPhone, ServerValue.TIMESTAMP);
//
//                            mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentLink, (databaseError, databaseReference) -> {
//                                    });
//
//                            mChannelMessagesReference.child(messageId).child("c").child(push_id)
//                                    .updateChildren(commentByMap, (databaseError, databaseReference) ->{
//                                    });
//                            mSendText.setText("");
//                        }else{
//                            Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
//                        }
                    } else {
                        Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                    }
                }));


                break;

            default:
                Toast.makeText(this, "Pow", Toast.LENGTH_SHORT).show();

        }

        mSendEmoji.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
        mSendEmoji.setOnClickListener(ignore -> emojiPopup.toggle());

        setUpEmojiPopup();

//        if(reply_Id.length() > 0){
//            commentPosition = commentsAndReplies.indexOf(reply_Id);
//        }


    }


    private void send(EmojiEditText mSendText, Map<String, Object> commentContentMap) {
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
                    .updateChildren(commentByMap, (databaseError, databaseReference) -> {
                    });
            mSendText.setText("");
        } else {
            Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
        }
    }

    private void getCommentsAndReplies() {
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
                if (chatRef == null) {
                    return;
                }
                mCommentReference.child(chatRef.getMsgId())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Messages comment = dataSnapshot.getValue(Messages.class);
                                if (comment == null) {
                                    return;
                                }

                                if (comment.getR().size() > 1) {
                                    for (String m : comment.getR().keySet()) {
                                        if (m.length() > 17) {

                                            mCommentReference.child(m).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Messages reply = dataSnapshot.getValue(Messages.class);
                                                    if (reply == null) {
                                                        return;
                                                    }
                                                    mRootReference.child("ads_users").child(reply.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Users user = dataSnapshot.getValue(Users.class);
                                                            if (user == null) {
                                                                return;
                                                            }

                                                            int pos = commentsAndReplies.indexOf(new CommentChannel(reply.getParent()));
                                                            CommentChannel currentCommentChannel = pos > -1 ? commentsAndReplies.get(pos) : new CommentChannel(reply.getParent());
                                                            reply.setName(user.getName());
                                                            reply.setProfilePic(user.getThumbnail());
                                                            reply.setMessageId(m);
                                                            reply.setChannelName(channelName);
                                                            if(!currentCommentChannel.getReplyMessages().contains(reply)) {
                                                                currentCommentChannel.getReplyMessages().add(reply);
                                                                Collections.sort(currentCommentChannel.getReplyMessages());
                                                                Collections.reverse(currentCommentChannel.getReplyMessages());
                                                                commentAdapter.notifyDataSetChanged();

                                                            }
                                                            if (pos < 0) {
                                                                currentCommentChannel.setName(user.getName());
                                                                currentCommentChannel.setProfilePic(user.getThumbnail());
                                                                currentCommentChannel.setCommentId(chatRef.getMsgId());
                                                                currentCommentChannel.setChannelName(channelName);
                                                                currentCommentChannel.setTimestamp(reply.getTimestamp());
                                                                currentCommentChannel.setR(reply.getR());
                                                                currentCommentChannel.setContent(reply.getContent());
                                                                currentCommentChannel.setFrom(reply.getFrom());

                                                                currentCommentChannel.setInitialCommentId(messageId);
                                                                currentCommentChannel.setInitialCommentContent(messageContent);
                                                                currentCommentChannel.setInitialChannelImage(channelImage);
                                                                currentCommentChannel.setInitialMessageType(messageType);
                                                                currentCommentChannel.setInitialColor(color);
                                                                currentCommentChannel.setInitialTimestamp(timestamp);
                                                                currentCommentChannel.setInitialLikesCount(likes);
                                                                currentCommentChannel.setInitialCommentsCount(comments);
                                                                currentCommentChannel.setSeeInitalCount(seen);
                                                                commentsAndReplies.add(currentCommentChannel);

                                                                commentAdapter.notifyDataSetChanged();
                                                            }

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
                                    }
                                }

                                mRootReference.child("ads_users").child(comment.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Users u = dataSnapshot.getValue(Users.class);
                                        if (u == null) {
                                            return;
                                        }

//                                        comment.setName(u.getName());
//                                        comment.setProfilePic(u.getThumbnail());
//                                        comment.setMessageId(chatRef.getMsgId());
//                                        comment.setChannelName(channelName);

                                        CommentChannel currentCommentChannel = new CommentChannel();
                                        //  messagesList.add(comment);
                                        currentCommentChannel.setName(u.getName());
                                        currentCommentChannel.setProfilePic(u.getThumbnail());
                                        currentCommentChannel.setCommentId(chatRef.getMsgId());
                                        currentCommentChannel.setChannelName(channelName);
                                        currentCommentChannel.setTimestamp(comment.getTimestamp());
                                        currentCommentChannel.setR(comment.getR());
                                        //  currentCommentChannel.setParent(comment.getParent());
                                        currentCommentChannel.setContent(comment.getContent());
                                        currentCommentChannel.setFrom(comment.getFrom());
                                        //Set values to be able to pass as intent values when coming to this Activity
                                        currentCommentChannel.setInitialCommentId(messageId);
                                        currentCommentChannel.setInitialCommentContent(messageContent);
                                        currentCommentChannel.setInitialChannelImage(channelImage);
                                        currentCommentChannel.setInitialMessageType(messageType);
                                        currentCommentChannel.setInitialColor(color);
                                        currentCommentChannel.setInitialTimestamp(timestamp);
                                        currentCommentChannel.setInitialLikesCount(likes);
                                        currentCommentChannel.setInitialCommentsCount(comments);
                                        currentCommentChannel.setSeeInitalCount(seen);
                                        commentsAndReplies.add(currentCommentChannel);
                                        commentAdapter.notifyDataSetChanged();
                                        //commentList.scrollToPosition(commentPosition);
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

    private void getSingleCommentAndReplies(){
        mCommentReference.child(commentId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Messages comment = dataSnapshot.getValue(Messages.class);
                        if (comment == null) {
                            return;
                        }

                        if (comment.getR().size() > 1) {
                            for (String m : comment.getR().keySet()) {
                                if (m.length() > 17) {

                                    mCommentReference.child(m).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Messages reply = dataSnapshot.getValue(Messages.class);
                                            if (reply == null) {
                                                return;
                                            }
                                            mRootReference.child("ads_users").child(reply.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Users user = dataSnapshot.getValue(Users.class);
                                                    if (user == null) {
                                                        return;
                                                    }

                                                    int pos = commentsAndReplies.indexOf(new CommentChannel(reply.getParent()));
                                                    CommentChannel currentCommentChannel = pos > -1 ? commentsAndReplies.get(pos) : new CommentChannel(reply.getParent());
                                                    reply.setName(user.getName());
                                                    reply.setProfilePic(user.getThumbnail());
                                                    reply.setMessageId(m);
                                                    reply.setChannelName(channelName);
                                                    if(!currentCommentChannel.getReplyMessages().contains(reply)) {
                                                        currentCommentChannel.getReplyMessages().add(reply);
                                                        Collections.sort(currentCommentChannel.getReplyMessages());
                                                        Collections.reverse(currentCommentChannel.getReplyMessages());
                                                      //  int position = currentCommentChannel.getReplyMessages().indexOf(reply);
                                                        commentList.smoothScrollToPosition(pos);
                                                        commentAdapter.notifyDataSetChanged();
                                                    }
                                                    if (pos < 0) {
                                                        currentCommentChannel.setName(user.getName());
                                                        currentCommentChannel.setProfilePic(user.getThumbnail());
                                                        currentCommentChannel.setCommentId(commentId);
                                                        currentCommentChannel.setChannelName(channelName);
                                                        currentCommentChannel.setTimestamp(reply.getTimestamp());
                                                        currentCommentChannel.setR(reply.getR());
                                                        currentCommentChannel.setContent(reply.getContent());
                                                        currentCommentChannel.setFrom(reply.getFrom());

                                                        currentCommentChannel.setInitialCommentId(messageId);
                                                        currentCommentChannel.setInitialCommentContent(messageContent);
                                                        currentCommentChannel.setInitialChannelImage(channelImage);
                                                        currentCommentChannel.setInitialMessageType(messageType);
                                                        currentCommentChannel.setInitialColor(color);
                                                        currentCommentChannel.setInitialTimestamp(timestamp);
                                                        currentCommentChannel.setInitialLikesCount(likes);
                                                        currentCommentChannel.setInitialCommentsCount(comments);
                                                        currentCommentChannel.setSeeInitalCount(seen);
                                                        commentsAndReplies.add(currentCommentChannel);

                                                        commentAdapter.notifyDataSetChanged();
                                                    }

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
                            }
                        }

                        mRootReference.child("ads_users").child(comment.getFrom()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Users u = dataSnapshot.getValue(Users.class);
                                if (u == null) {
                                    return;
                                }
                                CommentChannel currentCommentChannel = new CommentChannel();
                                currentCommentChannel.setName(u.getName());
                                currentCommentChannel.setProfilePic(u.getThumbnail());
                                currentCommentChannel.setCommentId(commentId);
                                currentCommentChannel.setChannelName(channelName);
                                currentCommentChannel.setTimestamp(comment.getTimestamp());
                                currentCommentChannel.setR(comment.getR());
                                currentCommentChannel.setContent(comment.getContent());
                                currentCommentChannel.setFrom(comment.getFrom());
                                //Set values to be able to pass as intent values when coming to this Activity
                                currentCommentChannel.setInitialCommentId(messageId);
                                currentCommentChannel.setInitialCommentContent(messageContent);
                                currentCommentChannel.setInitialChannelImage(channelImage);
                                currentCommentChannel.setInitialMessageType(messageType);
                                currentCommentChannel.setInitialColor(color);
                                currentCommentChannel.setInitialTimestamp(timestamp);
                                currentCommentChannel.setInitialLikesCount(likes);
                                currentCommentChannel.setInitialCommentsCount(comments);
                                currentCommentChannel.setSeeInitalCount(seen);
                                commentsAndReplies.add(currentCommentChannel);
                                commentAdapter.notifyDataSetChanged();
                                //commentList.scrollToPosition(commentPosition);
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

    private void setUpEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(rootView)
                .setOnEmojiBackspaceClickListener(ignore -> Log.d("ChannelAdminChatAct", "Clicked on Backspace"))
                .setOnEmojiClickListener((ignore, ignore2) -> Log.d("ChannelAdminChatAct", "Clicked on emoji"))
                .setOnEmojiPopupShownListener(() -> mSendEmoji.setImageResource(R.drawable.ic_keyboard))
                .setOnSoftKeyboardOpenListener(ignore -> Log.d("ChannelAdminChatAct", "Opened soft keyboard"))
                .setOnEmojiPopupDismissListener(() -> mSendEmoji.setImageResource(R.drawable.emoji_ios_category_smileysandpeople))
                .setOnSoftKeyboardCloseListener(() -> Log.d("ChannelAdminChatAct", "Closed soft keyboard"))
                .setKeyboardAnimationStyle(R.style.emoji_slide_animation_style)
                //   .setPageTransformer(new RotateUpTransformer())
                .build(mSendText);
    }

}
