package marcelin.thierry.chatapp.adapters;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.CommentActivity;
import marcelin.thierry.chatapp.activities.ForwardMessageActivity;
import marcelin.thierry.chatapp.activities.FullScreenImageActivity;
import marcelin.thierry.chatapp.activities.VideoPlayerActivity;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Group;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.RunTimePermissionWrapper;
import pl.droidsonroids.gif.GifImageView;

public class ChannelInteractionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Messages> mMessagesList;
    private Context mContext;
    private FirebaseAuth mAuth;
    private Activity mActivity;

    private static List<MediaPlayer> mediaPlayers = new ArrayList<>();
    private final String[] WALK_THROUGH = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

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
            .getReference().child("ads_channel_messages");

    public ChannelInteractionAdapter(List<Messages> mMessagesList, Context mContext, Activity mActivity) {
        this.mMessagesList = mMessagesList;
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

            case 0:

                if(!message.isVisible()){
                    ((MessageViewHolder) holder).textEntered.setTypeface(null, Typeface.ITALIC);
                    ((MessageViewHolder) holder).textEntered.setTextColor(Color.parseColor(("#A9A9A9")));
                    ((MessageViewHolder)holder).textEntered.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_message_deleted,
                            0, 0, 0);
                    ((MessageViewHolder) holder).textEntered.setCompoundDrawablePadding(50);
                    ((MessageViewHolder) holder).textEntered.setText(R.string.msg_dell);
                    ((MessageViewHolder) holder).textEntered.setEnabled(false);
                    ((MessageViewHolder) holder).numberOfSeen.setVisibility(View.GONE);
                    ((MessageViewHolder) holder).numberOfLikes.setVisibility(View.GONE);
                    ((MessageViewHolder) holder).numberOfComments.setVisibility(View.GONE);
//                    ((MessageViewHolder) holder).messageCheck.setVisibility(View.GONE);

                    ((MessageViewHolder) holder).main.setEnabled(false);
                }
                ((MessageViewHolder) holder).textEntered.setText(message.getContent());

                if(message.getContent().length() < 50){
                    ((MessageViewHolder) holder).textEntered.setTextSize(20);
                }else{
                    ((MessageViewHolder) holder).textEntered.setTextSize(16);
                }

                ((MessageViewHolder) holder).numberOfComments.setOnClickListener(v ->{
                    Intent goToCommentActivity = new Intent(v.getContext(), CommentActivity.class);
                    goToCommentActivity.putExtra("channel_name", message.getChannelName());
                    goToCommentActivity.putExtra("channel_image", message.getChannelImage());
                    goToCommentActivity.putExtra("message_type", message.getType());
                    goToCommentActivity.putExtra("message_id", message.getMessageId());
                    goToCommentActivity.putExtra("message_content", message.getContent());
                    goToCommentActivity.putExtra("message_timestamp", message.getTimestamp());
                    goToCommentActivity.putExtra("message_color", message.getColor());
                    v.getContext().startActivity(goToCommentActivity);
                });

                ((MessageViewHolder) holder).channelName.setText(message.getChannelName());
                ((MessageViewHolder) holder).setProfilePic(message.getChannelImage());

                if(message.getColor().equals("#7016a8") || message.getColor().equals("#FFFFFF") || !message.isVisible() ){
                //    ((MessageViewHolder) holder).messageLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    ((MessageViewHolder) holder).textEntered.setTextColor(Color.parseColor("#000000"));
                    ((MessageViewHolder) holder).messageLayout.setGravity(Gravity.START);
                }
                if(!message.getColor().equals("#7016a8") && (!message.getColor().equals("#FFFFFF")&& message.getContent().length() < 150 && message.isVisible())){
                    ((MessageViewHolder) holder).messageLayout.setBackgroundColor(Color.parseColor(message.getColor()));
                    ViewGroup.LayoutParams params = ((MessageViewHolder) holder).messageLayout.getLayoutParams();
                    params.height = 500;
                    ((MessageViewHolder) holder).messageLayout.setLayoutParams(params);
                    ((MessageViewHolder) holder).textEntered.setTextColor(Color.parseColor("#FFFFFF"));
                }

//                if (message.getContent().length() > 40) {
//                    ((MessageViewHolder) holder).messageText.setWidth(250);
//                }

                ((MessageViewHolder) holder).timestamp.setText(dateMessageSend);
                ((MessageViewHolder) holder).moreSettings.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.message_options, (dialog, which) -> {
                                switch (which){
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

                                    case 1:
                                        if (!message.getFrom().equals(Objects.requireNonNull
                                                (mAuth.getCurrentUser()).getPhoneNumber())) {
                                            Toast.makeText(view.getContext(), R.string.cannot +
                                                   R.string.coming, Toast.LENGTH_SHORT).show();
                                        }else{
                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");

                                            //((MessageViewHolder) holder).messageLinearLayout.setVisibility(View.GONE);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        break;

                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();


                });


//                if (message.getType().equals("channel_link")) {
//                    ((MessageViewHolder) holder).messageText.setTextColor(Color.rgb(0, 100, 0));
//                }

                ((MessageViewHolder) holder).textEntered.setOnClickListener(view -> {
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
                                                            Toast.makeText(view.getContext(), R.string.already,
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
                                                            String s = mContext.getResources()
                                                            .getString(R.string.subs_to);
                                                            dialog.setMessage(s +
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
                                                            Toast.makeText(view.getContext(), R.string.already_,
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
                                                            String s = mContext.getResources()
                                                            .getString(R.string.enter);
                                                            dialog.setMessage(s +
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
                
                ((MessageViewHolder) holder).numberOfSeen.setText(String.valueOf(message.getRead_by().size()));

                break;

            case 1:
                ((ImageViewHolder) holder).timestamp.setText(dateMessageSend);
                ((ImageViewHolder) holder).messageProgress.setVisibility(View.VISIBLE);
                ((ImageViewHolder) holder).channelName.setText(message.getChannelName());
                ((ImageViewHolder) holder).setProfilePic(message.getChannelImage());
                Picasso.get().load(message.getContent())
                        .placeholder(R.drawable.ic_avatar)
                        .into(((ImageViewHolder) holder).messageImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                ((ImageViewHolder) holder).messageProgress.setVisibility(View.GONE);

                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(holder.itemView.getContext(),
                                        (R.string.unf_erre), Toast.LENGTH_SHORT).show();
                            }
                        });

                ((ImageViewHolder) holder).messageImage.setOnClickListener(view -> {

                    Intent fullScreen = new Intent(view.getContext(), FullScreenImageActivity.class);
                    //
                    //fullScreen.putExtra("name_shown", user.getName());
                    fullScreen.putExtra("image_shown", message.getContent());
                    view.getContext().startActivity(fullScreen);

                });

                ((ImageViewHolder) holder).numberOfSeen.setText(String.valueOf(message.getRead_by().size()));

                showTextEntered(message, ((ImageViewHolder) holder).textEntered);
              
                ((ImageViewHolder) holder).moreSettings.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.channel_options, (dialog, which) -> {
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

                                        mMessageReference.child(message.getMessageId())
                                                .child("visible").setValue(false);
                                        mMessageReference.child(message.getMessageId())
                                                .child("type").setValue("text");

                                        mMessageReference.child(message.getMessageId())
                                                .child("content").setValue("Message Deleted");

                                    //    ((ImageViewHolder) holder).rLayout.setEnabled(false);

                                        Toast.makeText(view.getContext(), "Message deleted",
                                                Toast.LENGTH_SHORT).show();
                                        break;

                                    case 2:
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

            case 2:

                ((VideoViewHolder) holder).timestamp.setText(dateMessageSend);
                ((VideoViewHolder) holder).setProfilePic(message.getChannelImage());
                ((VideoViewHolder) holder).channelName.setText(message.getChannelName());
                ((VideoViewHolder) holder).messageLayout.setOnClickListener(view -> {
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

                ((VideoViewHolder) holder).numberOfSeen.setText(String.valueOf(message.getRead_by().size()));
                
                showTextEntered(message, ((VideoViewHolder) holder).textEntered);
                ((VideoViewHolder) holder).moreSettings.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.channel_options, (dialog, which) -> {
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

                                        mMessageReference.child(message.getMessageId())
                                                .child("visible").setValue(false);
                                        mMessageReference.child(message.getMessageId())
                                                .child("type").setValue("text");

                                        mMessageReference.child(message.getMessageId())
                                                .child("content").setValue("Message Deleted");

                                  //      ((VideoViewHolder) holder).rLayout.setEnabled(false);

                                        Toast.makeText(view.getContext(), "Message deleted",
                                                Toast.LENGTH_SHORT).show();
                                        break;

                                    case 2:
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

            case 3:

                ((AudioViewHolder) holder).timestamp.setText(dateMessageSend);
                ((AudioViewHolder) holder).setProfilePic(message.getChannelImage());
                ((AudioViewHolder) holder).channelName.setText(message.getChannelName());
                

                //TODO: Investigate dead thread on first media
                MediaPlayer mediaPlayer = new MediaPlayer();
                final boolean[] isReady = {false};
                final int[] duration = {0};
                mediaPlayers.add(mediaPlayer);

                showTextEntered(message, ((AudioViewHolder) holder).textEntered);
                
                try {
                    Log.i("((MediaPlayer))", "MediaPlayer called");
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

                        String time = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration[0]),
                                TimeUnit.MILLISECONDS.toSeconds(duration[0]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration[0]))
                        );

                        ((AudioViewHolder) holder).audioTime.setText(time);

                        ((AudioViewHolder) holder).audioSeekbar.setMax(duration[0]);

                        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                        service.scheduleWithFixedDelay(() ->
                                ((AudioViewHolder)holder).audioSeekbar.setProgress(mediaPlayer12.getCurrentPosition()), 1, 1, TimeUnit.MICROSECONDS);

                        // ((AudioViewHolder) holder).seekBarAudio.setProgress(mediaPlayer12.getCurrentPosition());
                    });

                    mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                        mediaPlayer1.pause();
                        //((AudioViewHolder) holder).playAudio.setImageResource(R.drawable.ic_play_copy);
                        ((AudioViewHolder) holder).pauseAudio.setVisibility(View.GONE);
                        ((AudioViewHolder) holder).btfBackground.setVisibility(View.GONE);
                        ((AudioViewHolder) holder).playAudio.setImageResource(R.drawable.ic_channel_play);
                        ((AudioViewHolder) holder).playAudio.setVisibility(View.VISIBLE);

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

                    ((AudioViewHolder) holder).pauseAudio.setOnClickListener(v ->{
                        if(isReady[0]){
                            if (mediaPlayer.isPlaying()) {
                                ((AudioViewHolder) holder).playAudio.setImageResource(R.drawable.ic_channel_play);
                                ((AudioViewHolder) holder).pauseAudio.setVisibility(View.GONE);
                                ((AudioViewHolder) holder).btfBackground.setVisibility(View.GONE);
                                ((AudioViewHolder) holder).audioTime.setVisibility(View.GONE);
                                ((AudioViewHolder) holder).audioSeekbar.setVisibility(View.GONE);
                                mediaPlayer.pause();
                            }
                        }
                    });

                    ((AudioViewHolder) holder).playAudio.setOnClickListener((View view) -> {
                        Log.i("((MediaPlayer))", "Play Button clicked");
                        if (isReady[0]) {
                            if (!mediaPlayer.isPlaying()) {
                                ((AudioViewHolder) holder).playAudio.setImageResource(R.drawable.ic_channel_pause);
                                ((AudioViewHolder) holder).pauseAudio.setVisibility(View.VISIBLE);
                                ((AudioViewHolder) holder).btfBackground.setVisibility(View.VISIBLE);
                                ((AudioViewHolder) holder).audioTime.setVisibility(View.GONE);
                                ((AudioViewHolder) holder).audioSeekbar.setVisibility(View.GONE);
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

                    ((AudioViewHolder) holder).audioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                ((AudioViewHolder) holder).numberOfSeen.setText(String.valueOf(message.getRead_by().size()));

                ((AudioViewHolder) holder).moreSettings.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.channel_options, (dialog, which) -> {
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

                                        mMessageReference.child(message.getMessageId())
                                                .child("visible").setValue(false);

                                        mMessageReference.child(message.getMessageId())
                                                .child("content").setValue("Message Deleted");
                                        mMessageReference.child(message.getMessageId())
                                                .child("type").setValue("text");

                               //         ((AudioViewHolder) holder).rLayout.setEnabled(false);

                                        Toast.makeText(view.getContext(), "Message deleted",
                                                Toast.LENGTH_SHORT).show();
                                        break;

                                    case 2:

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


                break;

            case 4:
                ((DocumentViewHolder) holder).timestamp.setText(dateMessageSend);
                ((DocumentViewHolder) holder).setProfilePic(message.getChannelImage());
                ((DocumentViewHolder) holder).channelName.setText(message.getChannelName());
             //   ((DocumentViewHolder) holder).textEntered()
                String s = message.getContent();

                showTextEntered(message, ((DocumentViewHolder) holder).textEntered);
                
                if(s.length() >60){
                    s = s.substring(0, 56) + "...";
                    ((DocumentViewHolder) holder).documentLink.setText(s);
                }else{
                    ((DocumentViewHolder) holder).documentLink.setText(message.getContent());
                }
                ((DocumentViewHolder) holder).documentGif.setOnClickListener(view -> {

                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                    myIntent.setData(Uri.parse(message.getContent()));
                    Intent docToOpen = Intent.createChooser(myIntent,
                            "Choose an application to open with:");
                    view.getContext().startActivity(docToOpen);

                });
                ((DocumentViewHolder) holder).numberOfSeen.setText(String.valueOf(message.getRead_by().size()));
                ((DocumentViewHolder) holder).moreSettings.setOnClickListener(view -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(R.string.choose_option)
                            .setItems(R.array.message_options, (dialog, which) -> {
                                switch (which){
                                    case 0:

                                        Intent i = new Intent(view.getContext(),
                                                ForwardMessageActivity.class);
                                        String s1 = "document";
                                        i.putExtra("type", s1);
                                        i.putExtra("message", message.getContent());
                                        view.getContext().startActivity(i);

                                        break;

                                    case 1:

                                        mMessageReference.child(message.getMessageId())
                                                .child("visible").setValue(false);

                                        mMessageReference.child(message.getMessageId())
                                                .child("content").setValue("Message Deleted");

                                        mMessageReference.child(message.getMessageId())
                                                .child("type").setValue("text");

                                       // ((DocumentViewHolder) holder).mainDoc.setEnabled(false);

                                        Toast.makeText(view.getContext(), "Message deleted",
                                                Toast.LENGTH_SHORT).show();
                                        break;

                                    default:
                                        return;
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();


                });

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
                View view = inflater.inflate(R.layout.channel_message, parent, false);
                return new MessageViewHolder(view);

            case 1:
                View view1 = inflater.inflate(R.layout.channel_image, parent, false);
                return new ImageViewHolder(view1);

            case 2:
                View view2 = inflater.inflate(R.layout.channel_video, parent, false);
                return new VideoViewHolder(view2);

            case 3:
                View view3 = inflater.inflate(R.layout.channel_audio, parent, false);
                return new AudioViewHolder(view3);

            case 4:
                View view4 = inflater.inflate(R.layout.channel_document, parent, false);
                return new DocumentViewHolder(view4);
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {

        mAuth = FirebaseAuth.getInstance();

        //String user_phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        final Messages message = mMessagesList.get(position);

      //  String from_user = message.getFrom();

        String type = message.getType();

        if (type.equals("text") || type.equals("channel_link")
                || type.equals("group_link"))
            return 0;
        else if (type.equals("image"))
            return 1;
        else if (type.equals("video"))
            return 2;
        else if (type.equals("audio"))
            return 3;
        else if (type.equals("document"))
            return 4;
        return -1;

    }


    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        CircleImageView channelImage;
        EmojiTextView channelName, textEntered;
        TextView timestamp, numberOfLikes, numberOfComments, numberOfSeen;
        ImageView moreSettings;
        ConstraintLayout main;
        LinearLayout messageLayout;

        public MessageViewHolder(View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channel_image);
            channelName = itemView.findViewById(R.id.channel_name);
            textEntered = itemView.findViewById(R.id.textEntered);

            timestamp = itemView.findViewById(R.id.timestamp);
            numberOfComments = itemView.findViewById(R.id.numberOfComments);
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);
            numberOfSeen = itemView.findViewById(R.id.numberOfSeen);

            moreSettings = itemView.findViewById(R.id.more_settings);
            main = itemView.findViewById(R.id.main);
            messageLayout = itemView.findViewById(R.id.messageLayout);

        }

        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channel_image);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        CircleImageView channelImage;
        EmojiTextView channelName, textEntered;
        TextView timestamp, numberOfLikes, numberOfComments, numberOfSeen;
        ImageView moreSettings, messageImage;
        ProgressBar messageProgress;


        public ImageViewHolder(View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channel_image);
            channelName = itemView.findViewById(R.id.channel_name);
            textEntered = itemView.findViewById(R.id.textEntered);
            timestamp = itemView.findViewById(R.id.timestamp);
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);
            numberOfComments = itemView.findViewById(R.id.numberOfComments);
            numberOfSeen = itemView.findViewById(R.id.numberOfSeen);
            moreSettings = itemView.findViewById(R.id.more_settings);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageProgress = itemView.findViewById(R.id.messageProgressBar);
        }

        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channel_image);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

        }

    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        CircleImageView channelImage;
        EmojiTextView channelName, textEntered;
        TextView timestamp, numberOfLikes, numberOfComments, numberOfSeen;
        ImageView moreSettings, messageImage;
        GifImageView messageLayout;


        public VideoViewHolder(View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channel_image);
            channelName = itemView.findViewById(R.id.channel_name);
            textEntered = itemView.findViewById(R.id.textEntered);
            timestamp = itemView.findViewById(R.id.timestamp);
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);
            numberOfComments = itemView.findViewById(R.id.numberOfComments);
            numberOfSeen = itemView.findViewById(R.id.numberOfSeen);
            moreSettings = itemView.findViewById(R.id.more_settings);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageLayout = itemView.findViewById(R.id.messageLayout);

        }


        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channel_image);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

        }
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {

        CircleImageView channelImage;
        EmojiTextView channelName, textEntered;
        TextView timestamp, numberOfLikes, numberOfComments, numberOfSeen, audioTime;
        ImageView moreSettings, playAudio, pauseAudio;
        RelativeLayout messsageLayout;
        GifImageView btfBackground;
        SeekBar audioSeekbar;


        public AudioViewHolder(View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channel_image);
            channelName = itemView.findViewById(R.id.channel_name);
            textEntered = itemView.findViewById(R.id.textEntered);
            timestamp = itemView.findViewById(R.id.timestamp);
            timestamp = itemView.findViewById(R.id.timestamp);
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);
            numberOfComments = itemView.findViewById(R.id.numberOfComments);
            numberOfSeen = itemView.findViewById(R.id.numberOfSeen);
            moreSettings = itemView.findViewById(R.id.more_settings);
            audioTime = itemView.findViewById(R.id.audio_time);
            playAudio = itemView.findViewById(R.id.play_audio);
            pauseAudio = itemView.findViewById(R.id.pause_audio);
            messsageLayout = itemView.findViewById(R.id.messageLayout);
            btfBackground = itemView.findViewById(R.id.btf_bg);
            audioSeekbar = itemView.findViewById(R.id.audio_seekbar);

        }

        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channel_image);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

        }
    }


    public static class DocumentViewHolder extends RecyclerView.ViewHolder {

        CircleImageView channelImage;
        EmojiTextView channelName;
        TextView timestamp, numberOfLikes, textEntered, numberOfComments, numberOfSeen, documentLink;
        ImageView moreSettings;
        GifImageView documentGif;


        public DocumentViewHolder(View itemView) {
            super(itemView);

            channelImage = itemView.findViewById(R.id.channel_image);
            channelName = itemView.findViewById(R.id.channel_name);
            timestamp = itemView.findViewById(R.id.timestamp);
            numberOfLikes = itemView.findViewById(R.id.numberOfLikes);
            numberOfComments = itemView.findViewById(R.id.numberOfComments);
            numberOfSeen = itemView.findViewById(R.id.numberOfSeen);
            moreSettings = itemView.findViewById(R.id.more_settings);
            documentGif = itemView.findViewById(R.id.documentGif);
            documentLink = itemView.findViewById(R.id.documentLink);
            textEntered = itemView.findViewById(R.id.textEntered);

        }

        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channel_image);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

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

    private void showTextEntered(Messages message, TextView textview){
        String text = message.getParent();
        if (text.length() > 7 && text.contains("Default%")) {
            String[] parts = text.split("Default%");
            String part1 = parts[0];
            String part2 = parts[1];

            textview.setText(part2);
            textview.setVisibility(View.VISIBLE);
        }
    }
}
