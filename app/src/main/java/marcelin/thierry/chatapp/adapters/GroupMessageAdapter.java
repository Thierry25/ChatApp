package marcelin.thierry.chatapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.ForwardMessageActivity;
import marcelin.thierry.chatapp.activities.FullScreenImageActivity;
import marcelin.thierry.chatapp.activities.VideoPlayerActivity;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Group;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import marcelin.thierry.chatapp.classes.TouchImageView;
import marcelin.thierry.chatapp.classes.Users;

public class GroupMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<Messages> mMessagesList;
    public List<Messages> mSelectedMessagesList;
    private Context mContext;

    private FirebaseAuth mAuth;
    private LinearLayout mMainVLayout;
    private Activity mActivity;
    private final String[] WALK_THROUGH = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};


    private static List<MediaPlayer> mediaPlayers = new ArrayList<>();

    private final static DatabaseReference mChannelReference = FirebaseDatabase.getInstance().
            getReference().child("ads_channel");

    private final static DatabaseReference mLinkReference = FirebaseDatabase.getInstance().
            getReference().child("channel_link");

    private final static DatabaseReference mUsersReference = FirebaseDatabase.getInstance().
            getReference().child("ads_users");

    private final static DatabaseReference mGroupReference = FirebaseDatabase.getInstance().
            getReference().child("ads_group");

    private final static DatabaseReference mGroupLinkReference = FirebaseDatabase.getInstance().
            getReference().child("group_link");

    private final static DatabaseReference mRootReference = FirebaseDatabase.getInstance().
            getReference();
    private final static DatabaseReference mMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group_messages");

    public GroupMessageAdapter(List<Messages> mMessagesList, LinearLayout mMainVLayout, List<Messages> mSelectedMessagesList,
                               Context mContext, Activity mActivity) {

        this.mMessagesList = mMessagesList;
        this.mMainVLayout = mMainVLayout;
        this.mSelectedMessagesList = mSelectedMessagesList;
        this.mContext = mContext;
        this.mActivity = mActivity;

        mediaPlayers.clear();
        setHasStableIds(true);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //       mHolderList.add(holder);

        final Messages message = mMessagesList.get(position);
        //
//        final Users user = users.get(position);

        String dateMessageSend = getDate(message.getTimestamp());

        switch (holder.getItemViewType()) {


            case 1:
            case 0:
                String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
//                if(!message.isVisible()){
//                    ((MessageViewHolder) holder).messageText.setTypeface(null, Typeface.ITALIC);
//                    ((MessageViewHolder) holder).messageText.setTextColor(Color.parseColor(("#A9A9A9")));
//                    ((MessageViewHolder)holder).messageText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_message_deleted,
//                            0, 0, 0);
//                    ((MessageViewHolder) holder).messageText.setCompoundDrawablePadding(20);
//                    ((MessageViewHolder) holder).messageText.setText(R.string.msg_dell);
//                    ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);
//                    ((MessageViewHolder) holder).messageText.setEnabled(false);
//                    ((MessageViewHolder) holder).messageTime.setVisibility(View.GONE);
//                    ((MessageViewHolder) holder).messageCheck.setVisibility(View.GONE);
//
////                   ((MessageViewHolder) holder).messageLinearLayout.setVisibility(View.GONE);
//                }

                if (!message.isVisible()) {
                    if (!message.getFrom().equals(phone)) {
                        ((MessageViewHolder) holder).messageText.setTypeface(null, Typeface.ITALIC);
                        ((MessageViewHolder) holder).messageText.setTextColor(Color.parseColor(("#A9A9A9")));
                        ((MessageViewHolder) holder).messageText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_message_deleted,
                                0, 0, 0);
                        ((MessageViewHolder) holder).messageText.setCompoundDrawablePadding(20);
                        ((MessageViewHolder) holder).messageText.setText(R.string.msg_dell);
                        ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);
                        ((MessageViewHolder) holder).messageText.setEnabled(false);
                        ((MessageViewHolder) holder).messageTime.setVisibility(View.GONE);
                        ((MessageViewHolder) holder).messageCheck.setVisibility(View.GONE);
                    } else {
                        ((MessageViewHolder) holder).messageText.setTextColor(Color.parseColor(("#CCCCCC")));
                        ((MessageViewHolder) holder).messageText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_cancel,
                                0, 0, 0);
                        ((MessageViewHolder) holder).messageText.setCompoundDrawablePadding(20);
                        ((MessageViewHolder) holder).messageText.setText(R.string.msg_dell);
                        ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);
                        ((MessageViewHolder) holder).messageText.setEnabled(false);
                        ((MessageViewHolder) holder).messageTime.setVisibility(View.GONE);
                        ((MessageViewHolder) holder).messageCheck.setVisibility(View.GONE);
                    }
//                   ((MessageViewHolder) holder).messageLinearLayout.setVisibility(View.GONE);
//                    notifyItemRangeChanged(position, mMessagesList.size());
                }
//                if(mSelectedMessagesList.contains(mMessagesList.get(position))){
//
//                    ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
//                    if(message.isReplyOn()){
//                        ((MessageViewHolder) holder).cLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
//                    }
//                }else{
//                    if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())){
//                        ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
//                    }else{
//                        ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_text_background);
//                    }
//                }

                if (mSelectedMessagesList.contains(mMessagesList.get(position))) {

                    ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                    if (message.isReplyOn()) {
                        ((MessageViewHolder) holder).cLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                        ((MessageViewHolder) holder).replyLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                        ((MessageViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                        ((MessageViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                    }
                } else {
                    if(message.isReplyOn()){
                        if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser().getPhoneNumber()))){
                            ((MessageViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
                            ((MessageViewHolder) holder).replyLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((MessageViewHolder) holder).cLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                        }else{
                            ((MessageViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);
                            ((MessageViewHolder) holder).replyLayout.setBackgroundResource(R.drawable.message_text_background);
                            ((MessageViewHolder) holder).cLayout.setBackgroundResource(R.drawable.message_text_background);
                            ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_text_background);
                        }
                    }else{
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((MessageViewHolder) holder).cLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                        } else {
                            ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.message_text_background);
                            ((MessageViewHolder) holder).cLayout.setBackgroundResource(R.drawable.message_text_background);
//
                        }
                    }
                }
                ((MessageViewHolder) holder).messageText.setText(message.getContent());

//                if (message.getContent().length() > 40) {
//                    ((MessageViewHolder) holder).messageText.setWidth(250);
//                }

                ((MessageViewHolder) holder).messageTime.setText(dateMessageSend);
//                ((MessageViewHolder) holder).messageLinearLayout.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                        builder.setTitle(R.string.choose_option)
//                                .setItems(R.array.options, (dialog, which) -> {
//                                    switch (which){
//                                        case 0:
//                                            switch (message.getType()) {
//                                                case "text": {
//
//                                                    Intent i = new Intent(view.getContext(),
//                                                            ForwardMessageActivity.class);
//                                                    String s = "text";
//                                                    i.putExtra("type", s);
//                                                    i.putExtra("message", message.getContent());
//                                                    view.getContext().startActivity(i);
//
//                                                    break;
//                                                }
//                                                case "channel_link": {
//
//                                                    Intent i = new Intent(view.getContext(),
//                                                            ForwardMessageActivity.class);
//                                                    String s = "channel_link";
//                                                    i.putExtra("type", s);
//                                                    i.putExtra("message", message.getContent());
//                                                    view.getContext().startActivity(i);
//
//                                                    break;
//                                                }
//                                                case "group_link": {
//
//                                                    Intent i = new Intent(view.getContext(),
//                                                            ForwardMessageActivity.class);
//                                                    String s = "group_link";
//                                                    i.putExtra("type", s);
//                                                    i.putExtra("message", message.getContent());
//                                                    view.getContext().startActivity(i);
//
//                                                    break;
//                                                }
//                                            }
//
//                                        case 1:
//                                            if (!message.getFrom().equals(Objects.requireNonNull
//                                                    (mAuth.getCurrentUser()).getPhoneNumber())) {
//                                                Toast.makeText(view.getContext(), R.string.cannot +
//                                                        R.string.coming, Toast.LENGTH_SHORT).show();
//                                            }else{
//                                                mMessageReference.child(message.getMessageId())
//                                                        .child("visible").setValue(false);
//
//                                                mMessageReference.child(message.getMessageId())
//                                                        .child("content").setValue("Message Deleted");
//
//                                                //((MessageViewHolder) holder).messageLinearLayout.setVisibility(View.GONE);
//
//                                                Toast.makeText(view.getContext(), "Message deleted",
//                                                        Toast.LENGTH_SHORT).show();
//                                            }
//                                            break;
//
//                                        case 2:
//                                            break;
//
//                                        default:
//                                            return;
//                                    }
//                                });
//                        AlertDialog dialog = builder.create();
//                        dialog.show();
//
//
//                    }
//                })


                if (message.getType().equals("channel_link")) {
                    ((MessageViewHolder) holder).messageText.setTextColor(Color.rgb(0, 100, 0));
                }else if(message.getType().equals("group_link")){
                    ((MessageViewHolder) holder).messageText.setTextColor(Color.rgb(0,100,80));
                }

                ((MessageViewHolder) holder).messageText.setOnClickListener(view -> {
                    String st = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
                    if(message.getType().equals("channel_link")){
                        mLinkReference.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                s = dataSnapshot.getValue(String.class);
                                if (s != null && s.equals(message.getContent())) {

                                    mChannelReference.child(dataSnapshot.getKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Channel c = dataSnapshot.getValue(Channel.class);
                                                    if (c != null) {
                                                        if(c.getSubscribers().containsKey(st)){
                                                            Toast.makeText(view.getContext(),R.string.already,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            AlertDialog.Builder builder = new AlertDialog.
                                                                    Builder(view.getContext());
                                                            builder.setPositiveButton(R.string.ok,
                                                                    (dialog, id) -> {
                                                                        Map<String, Object> m = new HashMap<>();
                                                                        m.put(st, ServerValue.TIMESTAMP);

                                                                        Map<String, Object> v = new HashMap<>();
                                                                        v.put("id", c.getName());
                                                                        v.put("phone_number", "");
                                                                        v.put("timestamp", ServerValue.TIMESTAMP);
                                                                        v.put("type", "channel");
                                                                        v.put("visible", true);


                                                                        mUsersReference.child(st)
                                                                                .child("conversation")
                                                                                .child("C-"+c.getName())
                                                                                .updateChildren(v);

                                                                        mRootReference.child("ads_channel")
                                                                                .child(c.getName())
                                                                                .child("subscribers")
                                                                                .updateChildren
                                                                                        (m, (databaseError, databaseReference) -> {});

                                                                    });
                                                            builder.setNegativeButton(R.string.cancel,
                                                                    (dialog, id) -> {
                                                                        // User cancelled the dialog
                                                                    });
                                                            AlertDialog dialog = builder.create();
                                                            dialog.setTitle(R.string.subscribe);
                                                            dialog.setMessage(mContext.getResources()
                                                                    .getString(R.string.subs_to) +
                                                                    " " + c.getName());
                                                            dialog.show();
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
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

                    }else if(message.getType().equals("group_link")){
                        mGroupLinkReference.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                s = dataSnapshot.getValue(String.class);
                                if (s != null && s.equals(message.getContent())) {

                                    mGroupReference.child(dataSnapshot.getKey())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Group g = dataSnapshot.getValue(Group.class);
                                                    if (g != null) {
                                                        if(g.getUsers().containsKey(st)){
                                                            Toast.makeText(view.getContext(),R.string.already_,
                                                                    Toast.LENGTH_SHORT).show();
                                                        }else{
                                                            AlertDialog.Builder builder = new AlertDialog.
                                                                    Builder(view.getContext());
                                                            builder.setPositiveButton(R.string.ok,
                                                                    (dialog, id) -> {
                                                                        Map<String, Object> m = new HashMap<>();
                                                                        m.put(st, ServerValue.TIMESTAMP);

                                                                        Map<String, Object> v = new HashMap<>();
                                                                        v.put("id", g.getName());
                                                                        v.put("phone_number", "");
                                                                        v.put("timestamp", ServerValue.TIMESTAMP);
                                                                        v.put("type", "group");
                                                                        v.put("visible", true);


                                                                        mUsersReference.child(st)
                                                                                .child("conversation")
                                                                                .child("G-"+g.getName())
                                                                                .updateChildren(v);

                                                                        mRootReference.child("ads_group")
                                                                                .child(g.getName())
                                                                                .child("users")
                                                                                .updateChildren
                                                                                        (m, (databaseError, databaseReference) -> {});

                                                                    });
                                                            builder.setNegativeButton(R.string.cancel,
                                                                    (dialog, id) -> {
                                                                        // User cancelled the dialog
                                                                    });
                                                            AlertDialog dialog = builder.create();
                                                            dialog.setTitle(R.string.subscribe);
                                                            dialog.setMessage(mContext.getResources()
                                                                    .getString(R.string.enter)+
                                                                    " " + g.getName());
                                                            dialog.show();
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
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
                });

//                ((MessageViewHolder) holder).messageCheck.setVisibility(View.GONE);
//                if (message.isSeen() && message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((MessageViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
//                }

                if (message.isSent() && message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((MessageViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
                }

                if (!message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((MessageViewHolder) holder).sender.setVisibility(View.VISIBLE);

                    String nameStored = Users.getLocalContactList().get(message.getFrom());

                    if(nameStored != null && nameStored.length() > 0){
                        ((MessageViewHolder) holder).sender.setText(Users.getLocalContactList()
                                .get(message.getFrom()));
                        ((MessageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }else{
                        ((MessageViewHolder) holder).sender.setText(message.getFrom());
                        ((MessageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }

                }

                if(message.isReplyOn()){
                    if(message.getFrom().equals(mAuth.getCurrentUser().getPhoneNumber())){
                        ((MessageViewHolder) holder).replyLayout.setVisibility(View.VISIBLE);
//                        ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.reply_lay);
//                        ((MessageViewHolder) holder).replyLayout.setBackgroundResource(R.drawable.reply_lay_upper);

                    }else{

                        ((MessageViewHolder) holder).replyLayout.setVisibility(View.VISIBLE);
//                        ((MessageViewHolder) holder).messageLinearLayout.setBackgroundResource(R.drawable.reply_layy);
//                        ((MessageViewHolder) holder).replyLayout.setBackgroundResource(R.drawable.reply_lay_upperr);
                    }



//                    if(mSelectedMessagesList.contains(mMessagesList.get(position))){
//                       mMainVLayout.setBackgroundResource(R.drawable.message_selected_layout);
//                    }else{
//                        if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())){
//                            mMainVLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
//                        }else{
//                            mMainVLayout.setBackgroundResource(R.drawable.message_text_background);
//                        }
//                    }

                    mMessageReference.child(message.getParent()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Messages m = dataSnapshot.getValue(Messages.class);
                            if(m == null){
                                return;
                            }
                            String nameStored = Users.getLocalContactList().get(m.getFrom());
                            nameStored = nameStored != null && nameStored.length() > 0  ? nameStored :
                                    m.getFrom();
                            if(m.getFrom().equals(mAuth.getCurrentUser().getPhoneNumber())){
                                ((MessageViewHolder) holder).senderOfMessage.setText(mContext.getResources().getString(R.string.you));
//                                ((MessageViewHolder) holder).senderOfMessage.setTextColor
//                                        (Color.parseColor("#FFD700"));
//
//                                ((MessageViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
//                                ((MessageViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout_from_me);
                            }else{
                                ((MessageViewHolder) holder).senderOfMessage.setText(nameStored);
//                                ((MessageViewHolder) holder).senderOfMessage.setTextColor
//                                        (Color.parseColor("#FF4500"));
//                                ((MessageViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.other_linear_background);
//                                ((MessageViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);

                            }

                            switch (m.getType()){

                                case "text":
                                case "channel_link":
                                case "group_link":

                                    ((MessageViewHolder) holder).messageContent.setVisibility(View.VISIBLE);
                                    ((MessageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    String st = m.getContent();
                                    if(st.length() > 35){
                                        st = st.substring(0, 31) + "...";
                                        ((MessageViewHolder) holder).messageContent.setText(st);
                                    }else{
                                        ((MessageViewHolder) holder).messageContent.setText(m.getContent());
                                    }
                                    break;


                                case "image":

                                    ((MessageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    ((MessageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((MessageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    Picasso.get().load(m.getContent()).placeholder(R.drawable.ic_avatar)
                                            .into(((MessageViewHolder) holder).imageSent);

                                    break;

                                case "audio":

                                    ((MessageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).audioSent.setVisibility(View.VISIBLE);
                                    ((MessageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((MessageViewHolder) holder).audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                            0, 0, 0);
                                    ((MessageViewHolder) holder).audioSent.setCompoundDrawablePadding(30);

                                    MediaMetadataRetriever mmr;

                                    try{

                                        mmr = new MediaMetadataRetriever();
                                        if (Build.VERSION.SDK_INT >= 14) {

                                            mmr.setDataSource(m.getContent(), new HashMap<String, String>());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                                            ((MessageViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            // ((MessageViewHolder) holder).audioSent.setTextColor(R.color.bg_gray);
                                            mmr.release();
                                        } else{

                                            mmr.setDataSource(m.getContent());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                            ((MessageViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            mmr.release();
                                        }

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    break;

                                case "video":

                                    ((MessageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).videoSent.setVisibility(View.VISIBLE);

                                    ((MessageViewHolder) holder).videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                            0, 0, 0);
                                    ((MessageViewHolder) holder).videoSent.setCompoundDrawablePadding(30);
                                    ((MessageViewHolder) holder).videoSent.setText(R.string.v);

                                    break;

                                case "document":

                                    ((MessageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((MessageViewHolder) holder).documentSent.setVisibility(View.VISIBLE);
                                    ((MessageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((MessageViewHolder) holder).documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                            0, 0, 0);
                                    ((MessageViewHolder) holder).documentSent.setCompoundDrawablePadding(30);
                                    ((MessageViewHolder) holder).documentSent.setText(R.string.d);

                                    break;

                                default:
                                    return;
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


                break;

            case 2:
            case 3:
                ((ImageViewHolder) holder).messageTime.setText(dateMessageSend);
                ((ImageViewHolder) holder).messageProgress.setVisibility(View.VISIBLE);

                if(message.isReplyOn()){
                    ViewGroup.LayoutParams params = ((ImageViewHolder) holder).rLayout.getLayoutParams();
                    params.height = 685;
                    params.width = 965;
                    ((ImageViewHolder) holder).rLayout.setLayoutParams(params);
                    if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                        ((ImageViewHolder) holder).infoLayout.setVisibility(View.VISIBLE);
                    } else {
                        ((ImageViewHolder) holder).inforLayout.setVisibility(View.VISIBLE);
                    }
                    mMessageReference.child(message.getParent()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Messages m = dataSnapshot.getValue(Messages.class);
                            if (m == null) {
                                return;
                            }
                            String nameStored = Users.getLocalContactList().get(m.getFrom());
                            nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                    m.getFrom();

                            if (m.getFrom().equals(mAuth.getCurrentUser().getPhoneNumber())) {
                                ((ImageViewHolder) holder).senderOfMessage.setText(mContext.getResources().getString(R.string.you));
                            } else {
                                ((ImageViewHolder) holder).senderOfMessage.setText(nameStored);

                            }

                            switch (m.getType()) {

                                case "text":
                                case "channel_link":
                                case "group_link":
                                case "contact":

                                    ((ImageViewHolder) holder).messageContent.setVisibility(View.VISIBLE);
                                    ((ImageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    String st = m.getContent();
                                    if (st.length() > 35) {
                                        st = st.substring(0, 31) + "...";
                                        ((ImageViewHolder) holder).messageContent.setText(st);
                                    } else {
                                        ((ImageViewHolder) holder).messageContent.setText(m.getContent());
                                    }
                                    break;


                                case "image":

                                    ((ImageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    ((ImageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    Picasso.get().load(m.getContent()).placeholder(R.drawable.ic_avatar)
                                            .into(((ImageViewHolder) holder).imageSent);

                                    break;

                                case "audio":

                                    ((ImageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).audioSent.setVisibility(View.VISIBLE);
                                    ((ImageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((ImageViewHolder) holder).audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                            0, 0, 0);
                                    ((ImageViewHolder) holder).audioSent.setCompoundDrawablePadding(30);

                                    MediaMetadataRetriever mmr;

                                    try {

                                        mmr = new MediaMetadataRetriever();
                                        if (Build.VERSION.SDK_INT >= 14) {

                                            mmr.setDataSource(m.getContent(), new HashMap<String, String>());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                                            ((ImageViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            //     ((MessageViewHolder) holder).audioSent.setTextColor(R.color.bg_gray);
                                            mmr.release();
                                        } else {

                                            mmr.setDataSource(m.getContent());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                            ((ImageViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            mmr.release();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "video":

                                    ((ImageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).videoSent.setVisibility(View.VISIBLE);

                                    ((ImageViewHolder) holder).videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                            0, 0, 0);
                                    ((ImageViewHolder) holder).videoSent.setCompoundDrawablePadding(30);
                                    ((ImageViewHolder) holder).videoSent.setText(R.string.v);

                                    break;

                                case "document":

                                    ((ImageViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((ImageViewHolder) holder).documentSent.setVisibility(View.VISIBLE);
                                    ((ImageViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((ImageViewHolder) holder).documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                            0, 0, 0);
                                    ((ImageViewHolder) holder).documentSent.setCompoundDrawablePadding(30);
                                    ((ImageViewHolder) holder).documentSent.setText(R.string.d);

                                    break;

                                default:
                                    return;
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }


                Picasso.get().load(message.getContent())
                        .placeholder(R.drawable.ic_avatar)
                        .into(((ImageViewHolder) holder).messageImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                ((ImageViewHolder) holder).messageProgress.setVisibility(View.GONE);

                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(holder.itemView.getContext(), R.string.unf_erre,
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

                ((ImageViewHolder) holder).messageImage.setOnClickListener(view -> {

                    Intent fullScreen = new Intent(view.getContext(), FullScreenImageActivity.class);
                    //
                    //fullScreen.putExtra("name_shown", user.getName());
                    fullScreen.putExtra("image_shown", message.getContent());
                    view.getContext().startActivity(fullScreen);

                });


                if (message.isSent() && message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((ImageViewHolder) holder).checkbar.setVisibility(View.VISIBLE);
                }

                if (!message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((ImageViewHolder) holder).sender.setVisibility(View.VISIBLE);
                    String nameStored = Users.getLocalContactList().get(message.getFrom());

                    if(nameStored != null && nameStored.length() > 0){
                        ((ImageViewHolder) holder).sender.setText(Users.getLocalContactList()
                                .get(message.getFrom()));
                        ((ImageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }else{
                        ((ImageViewHolder) holder).sender.setText(message.getFrom());
                        ((ImageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }

                }

//                if(mSelectedMessagesList.contains(mMessagesList.get(position))){
//
//                    ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
//
//                }else{
//                    if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())){
//                        ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
//                    }else{
//                        ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
//                    }
//                }

                if (mSelectedMessagesList.contains(mMessagesList.get(position))) {
                    ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                    if(message.isReplyOn()){
                        ((ImageViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                        ((ImageViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                    }
                } else {
                    if(message.isReplyOn()){
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((ImageViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
                        } else {
                            ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
                            ((ImageViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);
                        }
                    }else{
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                        } else {
                            ((ImageViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
                        }
                    }
                }

                ((ImageViewHolder) holder).rLayout.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.options_audio_video_image, (dialog, which) -> {
                                switch (which){
                                    case 0:

                                        Intent i = new Intent(view.getContext(),
                                                ForwardMessageActivity.class);
                                        String s = "image";
                                        i.putExtra("type", s);
                                        i.putExtra("message", message.getContent());
                                        view.getContext().startActivity(i);

                                        break;

                                    case 1:
                                        if (!message.getFrom().equals(Objects.requireNonNull
                                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                                            Toast.makeText(view.getContext(), mContext.getString(R.string.cannot) +
                                                    mContext.getString(R.string.coming), Toast.LENGTH_SHORT).show();
                                        }else {
                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);
                                            mMessageReference.child(message.getMessageId())
                                                    .child("type").setValue("text");

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");

                                            ((ImageViewHolder) holder).rLayout.setEnabled(false);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;

                                    case 2:

                                        TextView sender = mMainVLayout.findViewById(R.id.senderOfMessage);
                                        TextView messageV = mMainVLayout.findViewById(R.id.messageReceived);
                                        ImageView closeReply = mMainVLayout.findViewById(R.id.close_reply);
                                        ImageView imageSent = mMainVLayout.findViewById(R.id.imageSent);
                                        TextView audioSent = mMainVLayout.findViewById(R.id.audioSent);
                                        TextView videoSent = mMainVLayout.findViewById(R.id.videoSent);
                                        TextView documentSent = mMainVLayout.findViewById(R.id.documentSent);

                                        String nameStored = Users.getLocalContactList().get(message.getFrom());

                                        sender.setText(message.getFrom().equals(
                                                Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber()) ?
                                                mContext.getResources().getString(R.string.you) :
                                                nameStored != null && nameStored.length() > 0 ? nameStored : message.getFrom());

                                        messageV.setVisibility(View.GONE);
                                        audioSent.setVisibility(View.GONE);
                                        videoSent.setVisibility(View.GONE);
                                        documentSent.setVisibility(View.GONE);
                                        imageSent.setVisibility(View.VISIBLE);

                                        Picasso.get().load(message.getContent()).placeholder(R.drawable.ic_avatar)
                                                .into(imageSent);

                                        LinearLayout messageLinLayout = mMainVLayout.findViewById(R.id.messageLinLayout);
                                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                                        LinearLayout replyLayout = mMainVLayout.findViewById(R.id.replyLinearLayout);
                                        replyLayout.setVisibility(View.VISIBLE);

//                                        if (sender.getText().toString().trim().equals(mContext.getResources().getString(R.string.you))) {
//                                            sender.setTextColor(Color.parseColor("#FFD700"));
//                                        } else {
//                                            sender.setTextColor(Color.parseColor("#FF4500"));
//                                        }

                                        Messages.setClickedMessageId(message.getMessageId());

                                        closeReply.setOnClickListener(view12 -> {
                                            messageLinLayout.setBackgroundResource(R.drawable.border);
                                            replyLayout.setVisibility(View.GONE);
                                        });
                                        break;

                                    case 3:
                                        File rootFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + "/media/images");
                                        File adsFiles = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ADS Files");
                                        if (rootFiles.mkdirs() || rootFiles.isDirectory() && adsFiles.mkdirs() || adsFiles.isDirectory()){
                                            askPermission(mContext, message, ".jpg", rootFiles.getAbsolutePath());
                                            askPermission(mContext, message, ".jpg", adsFiles.getAbsolutePath());
                                        }
                                        break;
                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();


                });

                break;

            case 4:
            case 5:

                ((VideoViewHolder) holder).messageTime.setText(dateMessageSend);
                ((VideoViewHolder) holder).playButton.setOnClickListener(view -> {
                    Intent i = new Intent(view.getContext(), VideoPlayerActivity.class);
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + "/media/videos");
                    if (f.mkdirs() || f.isDirectory()) {
                        String alreadyThere = f.getAbsolutePath() + "/" + message.getMessageId() + ".mp4";
                        String vid = message.getMessageId() + ".mp4";
                        String[] videoList = f.list();
                        List<String> list = new ArrayList<>(Arrays.asList(videoList));
                        if (list.contains(vid)) {
                            i.putExtra("video", alreadyThere);
                        } else {
//                            String fileName = downloadFile(mContext, message.getMessageId(),
//                                    ".mp4", f.getAbsolutePath(), message.getContent());
//                            i.putExtra("video", f.getAbsolutePath() + "/" + fileName);
                            i.putExtra("video", message.getContent());
                        }
                    }
                    //  i.putExtra("time", getDate(message.getTimestamp()));
                    view.getContext().startActivity(i);
                });

//                if(mSelectedMessagesList.contains(mMessagesList.get(position))){
//
//                    ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
//
//                }else{
//                    if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())){
//                        ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
//                    }else{
//                        ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
//                    }
//                }

                if (mSelectedMessagesList.contains(mMessagesList.get(position))) {

                    ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                    if(message.isReplyOn()){
                        ((VideoViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                        ((VideoViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                    }
                } else {
                    if(message.isReplyOn()){
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((VideoViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
                        } else {
                            ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
                            ((VideoViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);
                        }
                    }else{
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                        } else {
                            ((VideoViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
                        }
                    }
                }

                if (!message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((VideoViewHolder) holder).sender.setVisibility(View.VISIBLE);
                    String nameStored = Users.getLocalContactList().get(message.getFrom());

                    if(nameStored != null && nameStored.length() > 0){
                        ((VideoViewHolder) holder).sender.setText(Users.getLocalContactList()
                                .get(message.getFrom()));
                        ((VideoViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }else{
                        ((VideoViewHolder) holder).sender.setText(message.getFrom());
                        ((VideoViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }

                }

                if(message.isReplyOn()){
                    if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                        ((VideoViewHolder) holder).infoLayout.setVisibility(View.VISIBLE);
                    } else {
                        ((VideoViewHolder) holder).inforLayout.setVisibility(View.VISIBLE);
                    }
                    mMessageReference.child(message.getParent()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Messages m = dataSnapshot.getValue(Messages.class);
                            if (m == null) {
                                return;
                            }
                            String nameStored = Users.getLocalContactList().get(m.getFrom());
                            nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                    m.getFrom();
                            if (m.getFrom().equals(mAuth.getCurrentUser().getPhoneNumber())) {
                                ((VideoViewHolder) holder).senderOfMessage.setText(mContext.getResources().getString(R.string.you));
//                                    ((VideoViewHolder) holder).senderOfMessage.setTextColor
//                                            (Color.parseColor("#20BF9F"));
//
//                                    ((VideoViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
//                                    ((VideoViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout_from_me);
                            } else {
                                ((VideoViewHolder) holder).senderOfMessage.setText(nameStored);
//                                    ((VideoViewHolder) holder).senderOfMessage.setTextColor
//                                            (Color.parseColor("#FFFFFF"));
//                                    ((VideoViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
//                                    ((VideoViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);

                            }

                            switch (m.getType()) {

                                case "text":
                                case "channel_link":
                                case "group_link":
                                case "contact":

                                    ((VideoViewHolder) holder).messageContent.setVisibility(View.VISIBLE);
                                    ((VideoViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    String st = m.getContent();
                                    if (st.length() > 35) {
                                        st = st.substring(0, 31) + "...";
                                        ((VideoViewHolder) holder).messageContent.setText(st);
                                    } else {
                                        ((VideoViewHolder) holder).messageContent.setText(m.getContent());
                                    }
                                    break;


                                case "image":

                                    ((VideoViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    ((VideoViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((VideoViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    Picasso.get().load(m.getContent()).placeholder(R.drawable.ic_avatar)
                                            .into(((VideoViewHolder) holder).imageSent);

                                    break;

                                case "audio":

                                    ((VideoViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).audioSent.setVisibility(View.VISIBLE);
                                    ((VideoViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((VideoViewHolder) holder).audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                            0, 0, 0);
                                    ((VideoViewHolder) holder).audioSent.setCompoundDrawablePadding(30);

                                    MediaMetadataRetriever mmr;

                                    try {

                                        mmr = new MediaMetadataRetriever();
                                        if (Build.VERSION.SDK_INT >= 14) {

                                            mmr.setDataSource(m.getContent(), new HashMap<String, String>());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                                            ((VideoViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            //     ((MessageViewHolder) holder).audioSent.setTextColor(R.color.bg_gray);
                                            mmr.release();
                                        } else {

                                            mmr.setDataSource(m.getContent());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                            ((VideoViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            mmr.release();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "video":

                                    ((VideoViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).videoSent.setVisibility(View.VISIBLE);

                                    ((VideoViewHolder) holder).videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                            0, 0, 0);
                                    ((VideoViewHolder) holder).videoSent.setCompoundDrawablePadding(30);
                                    ((VideoViewHolder) holder).videoSent.setText(R.string.v);

                                    break;

                                case "document":

                                    ((VideoViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((VideoViewHolder) holder).documentSent.setVisibility(View.VISIBLE);
                                    ((VideoViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((VideoViewHolder) holder).documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                            0, 0, 0);
                                    ((VideoViewHolder) holder).documentSent.setCompoundDrawablePadding(30);
                                    ((VideoViewHolder) holder).documentSent.setText(R.string.d);

                                    break;

                                default:
                                    return;
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                ((VideoViewHolder) holder).rLayout.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.options, (dialog, which) -> {
                                switch (which){
                                    case 0:

                                        Intent i = new Intent(view.getContext(),
                                                ForwardMessageActivity.class);
                                        String s = "video";
                                        i.putExtra("type", s);
                                        i.putExtra("message", message.getContent());
                                        view.getContext().startActivity(i);


                                        break;

                                    case 1:
                                        if (!message.getFrom().equals(Objects.requireNonNull
                                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                                            Toast.makeText(view.getContext(), mContext.getString(R.string.cannot) +
                                                    mContext.getString(R.string.coming), Toast.LENGTH_SHORT).show();
                                        }else {
                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);
                                            mMessageReference.child(message.getMessageId())
                                                    .child("type").setValue("text");

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");

                                            ((VideoViewHolder) holder).rLayout.setEnabled(false);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;

                                    case 2:

                                        TextView sender = mMainVLayout.findViewById(R.id.senderOfMessage);
                                        TextView messageV = mMainVLayout.findViewById(R.id.messageReceived);
                                        TextView audioSent = mMainVLayout.findViewById(R.id.audioSent);
                                        TextView videoSent = mMainVLayout.findViewById(R.id.videoSent);
                                        TextView documentSent = mMainVLayout.findViewById(R.id.documentSent);

                                        ImageView closeReply = mMainVLayout.findViewById(R.id.close_reply);
                                        ImageView imageSent = mMainVLayout.findViewById(R.id.imageSent);

                                        String nameStored = Users.getLocalContactList().get(message.getFrom());

                                        sender.setText(message.getFrom().equals(
                                                Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber()) ?
                                                mContext.getResources().getString(R.string.you) :
                                                nameStored != null && nameStored.length() > 0 ? nameStored : message.getFrom());

                                        messageV.setVisibility(View.GONE);
                                        audioSent.setVisibility(View.GONE);
                                        documentSent.setVisibility(View.GONE);
                                        imageSent.setVisibility(View.GONE);

                                        videoSent.setVisibility(View.VISIBLE);

                                        //  MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//                                        retriever.setDataSource(message.getContent());
                                        // String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                        //long timeInMillisec = Long.parseLong(time);

                                        //retriever.release();

                                        videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                                0, 0, 0);
                                        videoSent.setCompoundDrawablePadding(30);
                                        //  String timeToShow = String.valueOf(1000 * timeInMillisec);
                                        videoSent.setText(R.string.v);

                                        LinearLayout messageLinLayout = mMainVLayout.findViewById(R.id.messageLinLayout);
                                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                                        LinearLayout replyLayout = mMainVLayout.findViewById(R.id.replyLinearLayout);
                                        replyLayout.setVisibility(View.VISIBLE);

                                        if (sender.getText().toString().trim().equals(mContext.getResources().getString(R.string.you))) {
                                            sender.setTextColor(Color.parseColor("#FFD700"));
                                        } else {
                                            sender.setTextColor(Color.parseColor("#FF4500"));
                                        }

                                        Messages.setClickedMessageId(message.getMessageId());

                                        closeReply.setOnClickListener(view1 -> {
                                            messageLinLayout.setBackgroundResource(R.drawable.border);
                                            replyLayout.setVisibility(View.GONE);
                                        });

                                        break;

                                    case 3:

                                        File rootFil = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + "/media/videos");
                                        File adsFil = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ADS Files");
                                        if (rootFil.mkdirs() || rootFil.isDirectory() && adsFil.mkdirs() || adsFil.isDirectory()){
                                            askPermission(mContext, message, ".mp4", rootFil.getAbsolutePath());
                                            askPermission(mContext, message, ".mp4", adsFil.getAbsolutePath());
                                        }
                                        break;

                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });


                break;

            case 6:
            case 7:
                MediaPlayer mediaPlayer = new MediaPlayer();
                final boolean[] isReady = {false};
                final int[] duration = {0};
                mediaPlayers.add(mediaPlayer);
                final String[] time = new String[1];

                try {
                    Log.i("((MediaPlayer))", "MediaPlayer call-ed");
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + "/media/audios");
                    if (f.mkdirs() || f.isDirectory()) {
                        String alreadyThere = f.getAbsolutePath() + "/" + message.getMessageId() + ".gp3";
                        String mess = message.getMessageId() + ".gp3";
                        String[] listAudio = f.list();
                        List<String> list = new ArrayList<>(Arrays.asList(listAudio));
                        if (list.contains(mess)) {
                            mediaPlayer.setDataSource(alreadyThere);
                        } else {
//                            String fileName = downloadFile(mContext, message.getMessageId(),
//                                    ".gp3", f.getAbsolutePath(), message.getContent());
//
//                            mediaPlayer.setDataSource(f.getAbsolutePath() + "/" + fileName);
                            mediaPlayer.setDataSource(message.getContent());
                        }
                    }
                    mediaPlayer.setOnPreparedListener(mediaPlayer12 -> {
                        isReady[0] = true;
                        duration[0] = mediaPlayer12.getDuration();
                        Log.i("((MediaPlayer))", "Duration of message: " + duration[0]);

                        time[0] = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration[0]),
                                TimeUnit.MILLISECONDS.toSeconds(duration[0]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration[0]))
                        );


                        ((AudioViewHolder) holder).seekBarAudio.setMax(duration[0]);

                        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                        service.scheduleWithFixedDelay(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                ((AudioViewHolder)holder).seekBarAudio.setProgress(mediaPlayer12.getCurrentPosition());
                            }
                        }, 1, 1, TimeUnit.MICROSECONDS);

                        // ((AudioViewHolder) holder).seekBarAudio.setProgress(mediaPlayer12.getCurrentPosition());
                    });

                    mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                        mediaPlayer1.pause();
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_play_circle_filled);
                        } else {
                            ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_play_copy);
                        }
                    });

                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        mp.reset();
                        try {
                            mp.setDataSource(message.getContent());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    });

                    ((AudioViewHolder) holder).playAudioFile.setOnClickListener((View view) -> {
                        Log.i("((MediaPlayer))", "Play Button clicked");
                        if (isReady[0]) {
                            if (mediaPlayer.isPlaying()) {
                                if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                                    ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_play_circle_filled);
                                } else {
                                    ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_play_copy);
                                }
                                mediaPlayer.pause();
                            } else {
                                if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                                    ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_pause_circle_filled);
                                } else {
                                    ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_pause_copy);
                                }
                                for (MediaPlayer m : mediaPlayers) {
                                    if (m == null || !m.isPlaying()) {
                                        continue;
                                    }
                                    m.stop();
                                }
                                mediaPlayer.start();
                            }
                        }

                    });

                    if(message.isReplyOn()){
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((AudioViewHolder) holder).infoLayout.setVisibility(View.VISIBLE);
                        } else {
                            ((AudioViewHolder) holder).inforLayout.setVisibility(View.VISIBLE);
                        }
                        mMessageReference.child(message.getParent()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Messages m = dataSnapshot.getValue(Messages.class);
                                if (m == null) {
                                    return;
                                }
                                String nameStored = Users.getLocalContactList().get(m.getFrom());
                                nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                        m.getFrom();
                                if (m.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                                    ((AudioViewHolder) holder).senderOfMessage.setText(mContext.getResources().getString(R.string.you));
//                                        ((AudioViewHolder) holder).senderOfMessage.setTextColor
//                                                (Color.parseColor("#FFD700"));
//
//                                        ((AudioViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
//                                        ((AudioViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout_from_me);
                                } else {
                                    ((AudioViewHolder) holder).senderOfMessage.setText(nameStored);
//                                        ((AudioViewHolder) holder).senderOfMessage.setTextColor
//                                                (Color.parseColor("#FF4500"));
//                                        ((AudioViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.other_linear_background);
//                                        ((AudioViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);

                                }

                                switch (m.getType()) {

                                    case "text":
                                    case "channel_link":
                                    case "group_link":
                                    case "contact":

                                        ((AudioViewHolder) holder).messageContent.setVisibility(View.VISIBLE);
                                        ((AudioViewHolder) holder).audioSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).imageSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).documentSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).videoSent.setVisibility(View.GONE);

                                        String st = m.getContent();
                                        if (st.length() > 35) {
                                            st = st.substring(0, 31) + "...";
                                            ((AudioViewHolder) holder).messageContent.setText(st);
                                        } else {
                                            ((AudioViewHolder) holder).messageContent.setText(m.getContent());
                                        }
                                        break;


                                    case "image":

                                        ((AudioViewHolder) holder).messageContent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).audioSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                        ((AudioViewHolder) holder).documentSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).videoSent.setVisibility(View.GONE);

                                        ((AudioViewHolder) holder).messageContent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                        Picasso.get().load(m.getContent()).placeholder(R.drawable.ic_avatar)
                                                .into(((AudioViewHolder) holder).imageSent);

                                        break;

                                    case "audio":

                                        ((AudioViewHolder) holder).messageContent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).audioSent.setVisibility(View.VISIBLE);
                                        ((AudioViewHolder) holder).imageSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).documentSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).videoSent.setVisibility(View.GONE);

                                        ((AudioViewHolder) holder).audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                                0, 0, 0);
                                        ((AudioViewHolder) holder).audioSent.setCompoundDrawablePadding(30);

                                        MediaMetadataRetriever mmr;

                                        try {

                                            mmr = new MediaMetadataRetriever();
                                            if (Build.VERSION.SDK_INT >= 14) {

                                                mmr.setDataSource(m.getContent(), new HashMap<>());
                                                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                                                ((AudioViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                                //     ((MessageViewHolder) holder).audioSent.setTextColor(R.color.bg_gray);
                                                mmr.release();
                                            } else {

                                                mmr.setDataSource(m.getContent());
                                                String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                                ((AudioViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                                mmr.release();
                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        break;

                                    case "video":

                                        ((AudioViewHolder) holder).messageContent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).audioSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).imageSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).documentSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).videoSent.setVisibility(View.VISIBLE);

                                        ((AudioViewHolder) holder).videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                                0, 0, 0);
                                        ((AudioViewHolder) holder).videoSent.setCompoundDrawablePadding(30);
                                        ((AudioViewHolder) holder).videoSent.setText(R.string.v);

                                        break;

                                    case "document":

                                        ((AudioViewHolder) holder).messageContent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).audioSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).imageSent.setVisibility(View.GONE);
                                        ((AudioViewHolder) holder).documentSent.setVisibility(View.VISIBLE);
                                        ((AudioViewHolder) holder).videoSent.setVisibility(View.GONE);

                                        ((AudioViewHolder) holder).documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                                0, 0, 0);
                                        ((AudioViewHolder) holder).documentSent.setCompoundDrawablePadding(30);
                                        ((AudioViewHolder) holder).documentSent.setText(R.string.d);

                                        break;

                                    default:
                                        return;
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                    ((AudioViewHolder) holder).seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {

                            if (input) {
                                mediaPlayer.seekTo(progress);
                            }

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                        }
                    });

                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Log.e("MediaPlayerException", e.getMessage());
                    e.printStackTrace();
                }

//                ((AudioViewHolder) holder).messageCheck.setVisibility(View.GONE);
//                if (message.isSeen() && message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((AudioViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
//                }

                if (message.isSent() && message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((AudioViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
                }

                if (!message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((AudioViewHolder) holder).sender.setVisibility(View.VISIBLE);
                    String nameStored = Users.getLocalContactList().get(message.getFrom());

                    if(nameStored != null && nameStored.length() > 0){
                        ((AudioViewHolder) holder).sender.setText(Users.getLocalContactList()
                                .get(message.getFrom()));
                        ((AudioViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }else{
                        ((AudioViewHolder) holder).sender.setText(message.getFrom());
                        ((AudioViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }

                }



                ((AudioViewHolder) holder).rLayout.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.options, (dialog, which) -> {
                                switch (which){
                                    case 0:

                                        Intent i = new Intent(view.getContext(),
                                                ForwardMessageActivity.class);
                                        String s = "audio";
                                        i.putExtra("type", s);
                                        i.putExtra("message", message.getContent());
                                        view.getContext().startActivity(i);


                                        break;

                                    case 1:
                                        if (!message.getFrom().equals(Objects.requireNonNull
                                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                                            Toast.makeText(view.getContext(), mContext.getString(R.string.cannot) +
                                                    mContext.getString(R.string.coming), Toast.LENGTH_SHORT).show();
                                        }else {
                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");
                                            mMessageReference.child(message.getMessageId())
                                                    .child("type").setValue("text");

                                            ((AudioViewHolder) holder).rLayout.setEnabled(false);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;

                                    case 2:

                                        TextView sender = mMainVLayout.findViewById(R.id.senderOfMessage);
                                        TextView messageV = mMainVLayout.findViewById(R.id.messageReceived);
                                        TextView audioSent = mMainVLayout.findViewById(R.id.audioSent);
                                        TextView videoSent = mMainVLayout.findViewById(R.id.videoSent);
                                        TextView documentSent = mMainVLayout.findViewById(R.id.documentSent);

                                        ImageView closeReply = mMainVLayout.findViewById(R.id.close_reply);
                                        ImageView imageSent = mMainVLayout.findViewById(R.id.imageSent);

                                        String nameStored = Users.getLocalContactList().get(message.getFrom());

                                        sender.setText(message.getFrom().equals(
                                                Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber()) ?
                                                mContext.getResources().getString(R.string.you) :
                                                nameStored != null && nameStored.length() > 0 ? nameStored : message.getFrom());

                                        messageV.setVisibility(View.GONE);
                                        documentSent.setVisibility(View.GONE);
                                        imageSent.setVisibility(View.GONE);
                                        videoSent.setVisibility(View.GONE);

                                        audioSent.setVisibility(View.VISIBLE);

                                        audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                                0, 0, 0);
                                        audioSent.setCompoundDrawablePadding(30);

                                        audioSent.setText(time[0]);

                                        LinearLayout messageLinLayout = mMainVLayout.findViewById(R.id.messageLinLayout);
                                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                                        LinearLayout replyLayout = mMainVLayout.findViewById(R.id.replyLinearLayout);
                                        replyLayout.setVisibility(View.VISIBLE);

                                        if (sender.getText().toString().trim().equals(mContext.getResources().getString(R.string.you))) {
                                            sender.setTextColor(Color.parseColor("#FFD700"));
                                        } else {
                                            sender.setTextColor(Color.parseColor("#FF4500"));
                                        }

                                        Messages.setClickedMessageId(message.getMessageId());

                                        closeReply.setOnClickListener(view13 -> {
                                            messageLinLayout.setBackgroundResource(R.drawable.border);
                                            replyLayout.setVisibility(View.GONE);
                                        });

                                    break;

                                    case 3:
                                        File rootFolder= new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + mContext.getPackageName() + "/media/audios");
                                        File adsFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ADS Files");
                                        if (rootFolder.mkdirs() || rootFolder.isDirectory() && adsFolder.mkdirs() || adsFolder.isDirectory()){
                                            askPermission(mContext, message, ".gp3", rootFolder.getAbsolutePath());
                                            askPermission(mContext, message, ".gp3", adsFolder.getAbsolutePath());
                                        }
                                        break;

                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                });

//                if(mSelectedMessagesList.contains(mMessagesList.get(position))){
//
//                    ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
//
//                }else{
//                    if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())){
//                        ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
//                    }else{
//                        ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
//                    }
//                }

                if (mSelectedMessagesList.contains(mMessagesList.get(position))) {

                    ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                    if(message.isReplyOn()){
                        ((AudioViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                        ((AudioViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                    }
                } else {
                    if(message.isReplyOn()){
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((AudioViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
                        } else {
                            ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
                            ((AudioViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);
                        }
                    }else{
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background_for_me);
                        } else {
                            ((AudioViewHolder) holder).rLayout.setBackgroundResource(R.drawable.message_text_background);
                        }
                    }
                }

                break;

            case 8:
            case 9:

                ((DocumentViewHolder) holder).timeMessage.setText(dateMessageSend);
                String st = message.getContent();

                if(st.length() > 25){
                    st = st.substring(0, 21) + "...";
                    ((DocumentViewHolder) holder).documentName.setText(st);
                }else{
                    ((DocumentViewHolder) holder).documentName.setText(message.getContent());
                }

                ((DocumentViewHolder) holder).mainDoc.setOnClickListener(view -> {

                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                    myIntent.setData(Uri.parse(message.getContent()));
                    Intent docToOpen = Intent.createChooser(myIntent,
                            mContext.getResources().getString(R.string.choose_app));
                    view.getContext().startActivity(docToOpen);

                });



//                ((DocumentViewHolder) holder).messageCheck.setVisibility(View.GONE);
//                if (message.isSeen() && message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((DocumentViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
//                }

                if (message.isSent() && message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((DocumentViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
                }

                if (!message.getFrom().equals(Objects.requireNonNull(
                        mAuth.getCurrentUser()).getPhoneNumber())) {
                    ((DocumentViewHolder) holder).sender.setVisibility(View.VISIBLE);
                    String nameStored = Users.getLocalContactList().get(message.getFrom());

                    if(nameStored != null && nameStored.length() > 0){
                        ((DocumentViewHolder) holder).sender.setText(Users.getLocalContactList()
                                .get(message.getFrom()));
                        ((DocumentViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }else{
                        ((DocumentViewHolder) holder).sender.setText(message.getFrom());
                        ((DocumentViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
                    }

                }

                ((DocumentViewHolder) holder).mainDoc.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.options, (dialog, which) -> {
                                switch (which){
                                    case 0:

                                        Intent i = new Intent(view.getContext(),
                                                ForwardMessageActivity.class);
                                        String s = "document";
                                        i.putExtra("type", s);
                                        i.putExtra("message", message.getContent());
                                        view.getContext().startActivity(i);

                                        break;

                                    case 1:
                                        if (!message.getFrom().equals(Objects.requireNonNull
                                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                                            Toast.makeText(view.getContext(), mContext.getString(R.string.cannot) +
                                                    mContext.getString(R.string.coming), Toast.LENGTH_SHORT).show();
                                        }else {
                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");

                                            mMessageReference.child(message.getMessageId())
                                                    .child("type").setValue("text");

                                            ((DocumentViewHolder) holder).mainDoc.setEnabled(false);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;

                                    case 2:

                                        TextView sender = mMainVLayout.findViewById(R.id.senderOfMessage);
                                        TextView messageV = mMainVLayout.findViewById(R.id.messageReceived);
                                        TextView audioSent = mMainVLayout.findViewById(R.id.audioSent);
                                        TextView videoSent = mMainVLayout.findViewById(R.id.videoSent);
                                        TextView documentSent = mMainVLayout.findViewById(R.id.documentSent);

                                        ImageView closeReply = mMainVLayout.findViewById(R.id.close_reply);
                                        ImageView imageSent = mMainVLayout.findViewById(R.id.imageSent);

                                        String nameStored = Users.getLocalContactList().get(message.getFrom());

                                        sender.setText(message.getFrom().equals(
                                                Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber()) ?
                                                mContext.getResources().getString(R.string.you) :
                                                nameStored != null && nameStored.length() > 0 ? nameStored : message.getFrom());

                                        messageV.setVisibility(View.GONE);
                                        imageSent.setVisibility(View.GONE);
                                        videoSent.setVisibility(View.GONE);
                                        audioSent.setVisibility(View.GONE);

                                        documentSent.setVisibility(View.VISIBLE);

                                        documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                                0, 0, 0);
                                        documentSent.setCompoundDrawablePadding(30);

                                        String name = message.getContent();

                                        if(name.length() > 25){
                                            name = name.substring(0, 21) + "...";
                                            documentSent.setText(name);
                                        }else{
                                            documentSent.setText(message.getContent());
                                        }

                                        LinearLayout messageLinLayout = mMainVLayout.findViewById(R.id.messageLinLayout);
                                        messageLinLayout.setBackgroundResource(R.drawable.new_border);

                                        LinearLayout replyLayout = mMainVLayout.findViewById(R.id.replyLinearLayout);
                                        replyLayout.setVisibility(View.VISIBLE);

                                        if (sender.getText().toString().trim().equals(mContext.getResources().getString(R.string.you))) {
                                            sender.setTextColor(Color.parseColor("#FFD700"));
                                        } else {
                                            sender.setTextColor(Color.parseColor("#FF4500"));
                                        }

                                        Messages.setClickedMessageId(message.getMessageId());

                                        closeReply.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                messageLinLayout.setBackgroundResource(R.drawable.border);
                                                replyLayout.setVisibility(View.GONE);
                                            }
                                        });
                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                });

                if(message.isReplyOn()){
                    if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                        ((DocumentViewHolder) holder).infoLayout.setVisibility(View.VISIBLE);
                    } else {
                        ((DocumentViewHolder) holder).inforLayout.setVisibility(View.VISIBLE);
                    }
                    mMessageReference.child(message.getParent()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Messages m = dataSnapshot.getValue(Messages.class);
                            if (m == null) {
                                return;
                            }
                            String nameStored = Users.getLocalContactList().get(m.getFrom());
                            nameStored = nameStored != null && nameStored.length() > 0 ? nameStored :
                                    m.getFrom();
                            if (m.getFrom().equals(mAuth.getCurrentUser().getPhoneNumber())) {
                                ((DocumentViewHolder) holder).senderOfMessage.setText(mContext.getResources().getString(R.string.you));
//                                    ((DocumentViewHolder) holder).senderOfMessage.setTextColor
//                                            (Color.parseColor("#FFD700"));
//
//                                    ((DocumentViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
//                                    ((DocumentViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout_from_me);
                            } else {
                                ((DocumentViewHolder) holder).senderOfMessage.setText(nameStored);
//                                    ((DocumentViewHolder) holder).senderOfMessage.setTextColor
//                                            (Color.parseColor("#FF4500"));
//                                    ((DocumentViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.other_linear_background);
//                                    ((DocumentViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);

                            }

                            switch (m.getType()) {

                                case "text":
                                case "channel_link":
                                case "group_link":
                                case "contact":

                                    ((DocumentViewHolder) holder).messageContent.setVisibility(View.VISIBLE);
                                    ((DocumentViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    String st = m.getContent();
                                    if (st.length() > 35) {
                                        st = st.substring(0, 31) + "...";
                                        ((DocumentViewHolder) holder).messageContent.setText(st);
                                    } else {
                                        ((DocumentViewHolder) holder).messageContent.setText(m.getContent());
                                    }
                                    break;


                                case "image":

                                    ((DocumentViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    ((DocumentViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((DocumentViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).imageSent.setVisibility(View.VISIBLE);
                                    Picasso.get().load(m.getContent()).placeholder(R.drawable.ic_avatar)
                                            .into(((DocumentViewHolder) holder).imageSent);

                                    break;

                                case "audio":

                                    ((DocumentViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).audioSent.setVisibility(View.VISIBLE);
                                    ((DocumentViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((DocumentViewHolder) holder).audioSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_audio,
                                            0, 0, 0);
                                    ((DocumentViewHolder) holder).audioSent.setCompoundDrawablePadding(30);

                                    MediaMetadataRetriever mmr;

                                    try {

                                        mmr = new MediaMetadataRetriever();
                                        if (Build.VERSION.SDK_INT >= 14) {

                                            mmr.setDataSource(m.getContent(), new HashMap<String, String>());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                                            ((DocumentViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            //     ((MessageViewHolder) holder).audioSent.setTextColor(R.color.bg_gray);
                                            mmr.release();
                                        } else {

                                            mmr.setDataSource(m.getContent());
                                            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                                            ((DocumentViewHolder) holder).audioSent.setText(formatTimeOfAudio(duration));
                                            mmr.release();
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;

                                case "video":

                                    ((DocumentViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).documentSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).videoSent.setVisibility(View.VISIBLE);

                                    ((DocumentViewHolder) holder).videoSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new__video,
                                            0, 0, 0);
                                    ((DocumentViewHolder) holder).videoSent.setCompoundDrawablePadding(30);
                                    ((DocumentViewHolder) holder).videoSent.setText(R.string.v);

                                    break;

                                case "document":

                                    ((DocumentViewHolder) holder).messageContent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).audioSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).imageSent.setVisibility(View.GONE);
                                    ((DocumentViewHolder) holder).documentSent.setVisibility(View.VISIBLE);
                                    ((DocumentViewHolder) holder).videoSent.setVisibility(View.GONE);

                                    ((DocumentViewHolder) holder).documentSent.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_new_document,
                                            0, 0, 0);
                                    ((DocumentViewHolder) holder).documentSent.setCompoundDrawablePadding(30);
                                    ((DocumentViewHolder) holder).documentSent.setText(R.string.d);

                                    break;

                                default:
                                    return;
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

//                if(mSelectedMessagesList.contains(mMessagesList.get(position))){
//
//                    ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_selected_layout_for_me);
//
//                }else{
//                    if(message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())){
//                        ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_text_background_for_me);
//                    }else{
//                        ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_text_background);
//                    }
//                }

                if (mSelectedMessagesList.contains(mMessagesList.get(position))) {

                    ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_selected_layout_for_me);
                    if(message.isReplyOn()){
                        ((DocumentViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                        ((DocumentViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.reply_is_on_and_selected);
                    }
                } else {
                    if(message.isReplyOn()){
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_text_background_for_me);
                            ((DocumentViewHolder) holder).infoLayout.setBackgroundResource(R.drawable.linear_background);
                        } else {
                            ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_text_background);
                            ((DocumentViewHolder) holder).inforLayout.setBackgroundResource(R.drawable.final_lin_layout);
                        }
                    }else{
                        if (message.getFrom().equals(Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber())) {
                            ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_text_background_for_me);
                        } else {
                            ((DocumentViewHolder) holder).mainDoc.setBackgroundResource(R.drawable.message_text_background);
                        }
                    }
                }

                break;
        }
    }


    public void stopMediaPlayers() {
        if (!mediaPlayers.isEmpty()) {
            for (MediaPlayer p : mediaPlayers) {
                if (p != null) {
                    p.release();
                }
            }
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());


        switch (viewType) {

            case 0:
                View view1 = inflater.inflate(R.layout.group_message_main_layout, parent, false);
                return new MessageViewHolder(view1);

            case 1:
                View view2 = inflater.inflate(R.layout.group_message_main_layout_for_me, parent, false);
                return new MessageViewHolder(view2);

            case 2:
                View view3 = inflater.inflate(R.layout.group_image_main_layout_from_me, parent, false);
                return new ImageViewHolder(view3);

            case 3:
                View view4 = inflater.inflate(R.layout.group_image_main_layout, parent, false);
                return new ImageViewHolder(view4);

            case 4:
                View view5 = inflater.inflate(R.layout.group_video_main_layout_from_me, parent, false);
                return new VideoViewHolder(view5);

            case 5:
                View view6 = inflater.inflate(R.layout.group_video_main_layout, parent, false);
                return new VideoViewHolder(view6);

            case 6:
                View view7 = inflater.inflate(R.layout.group_audio_main_layout_from_me, parent, false);
                return new AudioViewHolder(view7);

            case 7:
                View view8 = inflater.inflate(R.layout.group_audio_main_layout, parent, false);
                return new AudioViewHolder(view8);

            case 8:
                View view9 = inflater.inflate(R.layout.group_document_main_layout_from_me, parent, false);
                return new DocumentViewHolder(view9);

            case 9:
                View view10 = inflater.inflate(R.layout.group_document_main_layout, parent, false);
                return new DocumentViewHolder(view10);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {

        mAuth = FirebaseAuth.getInstance();

        String user_phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        final Messages message = mMessagesList.get(position);

        String from_user = message.getFrom();

        String type = message.getType();

        if (from_user.equals(user_phone) && type.equals("text") || type.equals("channel_link")
                || type.equals("group_link"))
            return 1;
        else if (!from_user.equals(user_phone) && type.equals("text") || type.equals("channel_link")
                || type.equals("group_link"))
            return 0;
        else if (from_user.equals(user_phone) && type.equals("image"))
            return 2;
        else if (!from_user.equals(user_phone) && type.equals("image"))
            return 3;
        else if (from_user.equals(user_phone) && type.equals("video"))
            return 4;
        else if (!from_user.equals(user_phone) && type.equals("video"))
            return 5;
        else if (from_user.equals(user_phone) && type.equals("audio"))
            return 6;
        else if (!from_user.equals(user_phone) && type.equals("audio"))
            return 7;
        else if (from_user.equals(user_phone) && type.equals("document"))
            return 8;
        else if (!from_user.equals(user_phone) && type.equals("document"))
            return 9;

        return -1;


    }


    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        EmojiTextView messageText;
        TextView messageTime;
        TextView sender;
        TextView senderOfMessage;
        TextView messageContent;
        TextView audioSent;
        TextView documentSent;
        TextView videoSent;

        RelativeLayout messageLinearLayout;

        ImageView messageCheck;
        TouchImageView imageSent;

        ConstraintLayout replyLayout;
        ConstraintLayout cLayout;

        LinearLayout infoLayout;
        LinearLayout inforLayout;


        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_layout);
            messageTime = itemView.findViewById(R.id.message_time_layout);
            messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout);
            messageCheck = itemView.findViewById(R.id.messageCheck);

            sender = itemView.findViewById(R.id.groupSenderMessage);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            cLayout = itemView.findViewById(R.id.cLayout);

            audioSent = itemView.findViewById(R.id.audioSent);
            documentSent = itemView.findViewById(R.id.documentSent);
            videoSent = itemView.findViewById(R.id.videoSent);
            imageSent = itemView.findViewById(R.id.imageSent);

            senderOfMessage = itemView.findViewById(R.id.senderOfMessage);
            messageContent = itemView.findViewById(R.id.messageContent);

            infoLayout = itemView.findViewById(R.id.infoLayout);
            inforLayout = itemView.findViewById(R.id.inforLayout);

        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView messageImage;
        TextView messageTime;
        ProgressBar messageProgress;
        ImageView checkbar;

        TextView sender;
        TextView senderOfMessage;
        TextView messageContent;
        TextView audioSent;
        TextView documentSent;
        TextView videoSent;

        RelativeLayout rLayout;
        TouchImageView imageSent;

        LinearLayout infoLayout;
        LinearLayout inforLayout;


        public ImageViewHolder(View itemView) {
            super(itemView);

            messageImage = itemView.findViewById(R.id.messageImage);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageProgress = itemView.findViewById(R.id.messageProgressBar);

            checkbar = itemView.findViewById(R.id.checkbar);
            sender = itemView.findViewById(R.id.groupSenderMessage);

            senderOfMessage = itemView.findViewById(R.id.senderOfMessage);
            messageContent = itemView.findViewById(R.id.messageContent);
            imageSent = itemView.findViewById(R.id.imageSent);
            audioSent = itemView.findViewById(R.id.audioSent);
            documentSent = itemView.findViewById(R.id.documentSent);
            videoSent = itemView.findViewById(R.id.videoSent);

            rLayout = itemView.findViewById(R.id.rLayout);
            infoLayout = itemView.findViewById(R.id.infoLayout);
            inforLayout = itemView.findViewById(R.id.inforLayout);
        }

    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView messageTime;
        ImageView messageVideo;

        ImageView playButton;

        TextView sender;
        RelativeLayout rLayout;

        TextView senderOfMessage;
        TextView messageContent;
        TextView audioSent;
        TextView documentSent;
        TextView videoSent;

        TouchImageView imageSent;

        LinearLayout infoLayout;
        LinearLayout inforLayout;

        public VideoViewHolder(View itemView) {
            super(itemView);

            messageTime = itemView.findViewById(R.id.messageTime);
            messageVideo = itemView.findViewById(R.id.messageVideo);
            sender = itemView.findViewById(R.id.groupSenderMessage);
            rLayout = itemView.findViewById(R.id.rLayout);
            playButton = itemView.findViewById(R.id.play_button);
            senderOfMessage = itemView.findViewById(R.id.senderOfMessage);
            messageContent = itemView.findViewById(R.id.messageContent);
            audioSent = itemView.findViewById(R.id.audioSent);
            documentSent = itemView.findViewById(R.id.documentSent);
            videoSent = itemView.findViewById(R.id.videoSent);
            imageSent = itemView.findViewById(R.id.imageSent);
            infoLayout = itemView.findViewById(R.id.infoLayout);
            inforLayout = itemView.findViewById(R.id.inforLayout);

        }
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {

        ImageButton playAudioFile;
        SeekBar seekBarAudio;

        ImageView messageCheck;
        TextView sender, senderOfMessage , messageContent, audioSent, documentSent,
                videoSent;
        TouchImageView imageSent;
        LinearLayout rLayout, inforLayout, infoLayout;

        public AudioViewHolder(View itemView) {
            super(itemView);

            playAudioFile = itemView.findViewById(R.id.play_audio);
            seekBarAudio = itemView.findViewById(R.id.audio_seekbar);

            messageCheck = itemView.findViewById(R.id.checkbar);
            sender = itemView.findViewById(R.id.groupSenderMessage);
            senderOfMessage = itemView.findViewById(R.id.senderOfMessage);
            messageContent = itemView.findViewById(R.id.messageContent);
            audioSent = itemView.findViewById(R.id.audioSent);
            documentSent = itemView.findViewById(R.id.documentSent);
            videoSent = itemView.findViewById(R.id.videoSent);
            imageSent = itemView.findViewById(R.id.imageSent);

            infoLayout = itemView.findViewById(R.id.infoLayout);
            inforLayout = itemView.findViewById(R.id.inforLayout);

            rLayout = itemView.findViewById(R.id.rLayout);

        }
    }


    public static class DocumentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mainDoc;
        TextView documentName;
        TextView timeMessage;

        ImageView messageCheck;
        TextView sender;
        TextView senderOfMessage;
        TextView messageContent;
        TextView audioSent;
        TextView documentSent;
        TextView videoSent;
        TouchImageView imageSent;

        LinearLayout infoLayout;
        LinearLayout inforLayout;


        public DocumentViewHolder(View itemView) {
            super(itemView);

            mainDoc = itemView.findViewById(R.id.mainDoc);
            documentName = itemView.findViewById(R.id.documentName);
            timeMessage = itemView.findViewById(R.id.timeMessage);

            messageCheck = itemView.findViewById(R.id.messageCheck);
            sender = itemView.findViewById(R.id.groupSenderMessage);

            senderOfMessage = itemView.findViewById(R.id.senderOfMessage);
            messageContent = itemView.findViewById(R.id.messageContent);
            audioSent = itemView.findViewById(R.id.audioSent);
            documentSent = itemView.findViewById(R.id.documentSent);
            videoSent = itemView.findViewById(R.id.videoSent);
            imageSent = itemView.findViewById(R.id.imageSent);
            infoLayout = itemView.findViewById(R.id.infoLayout);
            inforLayout = itemView.findViewById(R.id.inforLayout);

        }

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

    public void updateList(List<Messages> newList){
        mMessagesList = new ArrayList<>();
        mMessagesList.addAll(newList);
        notifyDataSetChanged();
    }

    public String formatTimeOfAudio(String duration){

        String result = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(duration)),
                TimeUnit.MILLISECONDS.toSeconds(Long.parseLong(duration)) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(Long.parseLong(duration))
                        ));

        return result;

    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
                DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(new File(destinationDirectory, fileName + fileExtension)));
        if (downloadmanager != null) {
            downloadmanager.enqueue(request);
        }

        //   return fileName + fileExtension;
    }


    private void askPermission(Context context, Messages message, String fileExtension, String destinationDirectory) {
        if (RunTimePermissionWrapper.isAllPermissionGranted(mActivity, WALK_THROUGH)) {

            downloadFile(context, message.getMessageId(), fileExtension, destinationDirectory, message.getContent());
        }
        RunTimePermissionWrapper.handleRunTimePermission(mActivity,
                RunTimePermissionWrapper.REQUEST_CODE.MULTIPLE_WALKTHROUGH, WALK_THROUGH);
    }


}
