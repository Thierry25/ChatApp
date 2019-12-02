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


            case 1:
            case 0:

                if(!message.isVisible()){
                    ((MessageViewHolder) holder).messageText.setTypeface(null, Typeface.ITALIC);
                    ((MessageViewHolder) holder).messageText.setTextColor(Color.parseColor(("#A9A9A9")));
                    ((MessageViewHolder)holder).messageText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_message_deleted,
                            0, 0, 0);
                    ((MessageViewHolder) holder).messageText.setCompoundDrawablePadding(20);
                    ((MessageViewHolder) holder).messageText.setText(R.string.msg_dell);
                    ((MessageViewHolder) holder).messageLinearLayout.setEnabled(false);
                    ((MessageViewHolder) holder).messageText.setEnabled(false);
                    ((MessageViewHolder) holder).messageTime.setVisibility(View.GONE);
//                    ((MessageViewHolder) holder).messageCheck.setVisibility(View.GONE);
                }
                ((MessageViewHolder) holder).messageText.setText(message.getContent());

//                if (message.getContent().length() > 40) {
//                    ((MessageViewHolder) holder).messageText.setWidth(250);
//                }

                ((MessageViewHolder) holder).messageTime.setText(dateMessageSend);
                ((MessageViewHolder) holder).messageLinearLayout.setOnClickListener(view -> {
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


                if (message.getType().equals("channel_link")) {
                    ((MessageViewHolder) holder).messageText.setTextColor(Color.rgb(0, 100, 0));
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


                /**
                 * Test tonight
                 */
                ((MessageViewHolder) holder).amountOfSeen.setText(String.valueOf(message.getRead_by().size()));

//                if (!message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((MessageViewHolder) holder).sender.setVisibility(View.VISIBLE);
//
//                    String nameStored = Users.getLocalContactList().get(message.getFrom());
//
//                    if(nameStored != null && nameStored.length() > 0){
//                        ((MessageViewHolder) holder).sender.setText(Users.getLocalContactList()
//                                .get(message.getFrom()));
//                        ((MessageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }else{
//                        ((MessageViewHolder) holder).sender.setText(message.getFrom());
//                        ((MessageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }
//
//                }
                break;

            case 2:
            case 3:
                ((ImageViewHolder) holder).messageTime.setText(dateMessageSend);
                ((ImageViewHolder) holder).messageProgress.setVisibility(View.VISIBLE);
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

                ((ImageViewHolder) holder).amountOfSeen.setText(String.valueOf(message.getRead_by().size()));

//                if (!message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((ImageViewHolder) holder).sender.setVisibility(View.VISIBLE);
//                    String nameStored = Users.getLocalContactList().get(message.getFrom());
//
//                    if(nameStored != null && nameStored.length() > 0){
//                        ((ImageViewHolder) holder).sender.setText(Users.getLocalContactList()
//                                .get(message.getFrom()));
//                        ((ImageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }else{
//                        ((ImageViewHolder) holder).sender.setText(message.getFrom());
//                        ((ImageViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }
//
//                }

                ((ImageViewHolder) holder).rLayout.setOnClickListener(view -> {
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

                                        ((ImageViewHolder) holder).rLayout.setEnabled(false);

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

            case 4:
            case 5:

                ((VideoViewHolder) holder).messageTime.setText(dateMessageSend);
                ((VideoViewHolder) holder).playButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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
                    }
                });

                ((VideoViewHolder) holder).amountOfSeen.setText(String.valueOf(message.getRead_by().size()));

                ((VideoViewHolder) holder).rLayout.setOnClickListener(view -> {
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

                                        ((VideoViewHolder) holder).rLayout.setEnabled(false);

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

            case 6:
            case 7:
                //TODO: Investigate dead thread on first media
                MediaPlayer mediaPlayer = new MediaPlayer();
                final boolean[] isReady = {false};
                final int[] duration = {0};
                mediaPlayers.add(mediaPlayer);

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
                        ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_play_copy);

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
                                ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_play_copy);
                                mediaPlayer.pause();
                            } else {
                                ((AudioViewHolder) holder).playAudioFile.setImageResource(R.drawable.ic_pause_copy);
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

//                if (message.isSent() && message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((AudioViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
//                }

                ((AudioViewHolder) holder).amountOfSeen.setText(String.valueOf(message.getRead_by().size()));

//                if (!message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((AudioViewHolder) holder).sender.setVisibility(View.VISIBLE);
//                    String nameStored = Users.getLocalContactList().get(message.getFrom());
//
//                    if(nameStored != null && nameStored.length() > 0){
//                        ((AudioViewHolder) holder).sender.setText(Users.getLocalContactList()
//                                .get(message.getFrom()));
//                        ((AudioViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }else{
//                        ((AudioViewHolder) holder).sender.setText(message.getFrom());
//                        ((AudioViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }
//
//                }


                ((AudioViewHolder) holder).rLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
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

                                            ((AudioViewHolder) holder).rLayout.setEnabled(false);

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


                    }
                });


                break;

            case 8:
            case 9:

                ((DocumentViewHolder) holder).timeMessage.setText(dateMessageSend);
                String s = message.getContent();

                if(s.length() > 25){
                    s = s.substring(0, 21) + "...";
                    ((DocumentViewHolder) holder).documentName.setText(s);
                }else{
                    ((DocumentViewHolder) holder).documentName.setText(message.getContent());
                }
                ((DocumentViewHolder) holder).mainDoc.setOnClickListener(view -> {

                    Intent myIntent = new Intent(Intent.ACTION_VIEW);
                    myIntent.setData(Uri.parse(message.getContent()));
                    Intent docToOpen = Intent.createChooser(myIntent,
                            "Choose an application to open with:");
                    view.getContext().startActivity(docToOpen);

                });

//                ((DocumentViewHolder) holder).messageCheck.setVisibility(View.GONE);
//                if (message.isSeen() && message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((DocumentViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
//                }

//                if (message.isSent() && message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((DocumentViewHolder) holder).messageCheck.setVisibility(View.VISIBLE);
//                }

                ((DocumentViewHolder) holder).amountOfSeen.setText(String.valueOf(message.getRead_by().size()));

//                if (!message.getFrom().equals(Objects.requireNonNull(
//                        mAuth.getCurrentUser()).getPhoneNumber())) {
//                    ((DocumentViewHolder) holder).sender.setVisibility(View.VISIBLE);
//                    String nameStored = Users.getLocalContactList().get(message.getFrom());
//
//                    if(nameStored != null && nameStored.length() > 0){
//                        ((DocumentViewHolder) holder).sender.setText(Users.getLocalContactList()
//                                .get(message.getFrom()));
//                        ((DocumentViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }else{
//                        ((DocumentViewHolder) holder).sender.setText(message.getFrom());
//                        ((DocumentViewHolder) holder).sender.setTextColor(Color.parseColor(message.getColor()));
//                    }
//
//                }

                ((DocumentViewHolder) holder).mainDoc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle(R.string.choose_option)
                                .setItems(R.array.message_options, (dialog, which) -> {
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

                                            mMessageReference.child(message.getMessageId())
                                                    .child("visible").setValue(false);

                                            mMessageReference.child(message.getMessageId())
                                                    .child("content").setValue("Message Deleted");

                                            mMessageReference.child(message.getMessageId())
                                                    .child("type").setValue("text");

                                            ((DocumentViewHolder) holder).mainDoc.setEnabled(false);

                                            Toast.makeText(view.getContext(), "Message deleted",
                                                    Toast.LENGTH_SHORT).show();
                                            break;

                                        default:
                                            return;
                                    }
                                });
                        AlertDialog dialog = builder.create();
                        dialog.show();


                    }
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
                View view1 = inflater.inflate(R.layout.channel_message, parent, false);
                return new MessageViewHolder(view1);

            case 1:
                View view2 = inflater.inflate(R.layout.channel_message_from_me, parent, false);
                return new MessageViewHolder(view2);

            case 2:
                View view3 = inflater.inflate(R.layout.channel_image_from_me, parent, false);
                return new ImageViewHolder(view3);

            case 3:
                View view4 = inflater.inflate(R.layout.channel_image, parent, false);
                return new ImageViewHolder(view4);

            case 4:
                View view5 = inflater.inflate(R.layout.channel_video_from_me, parent, false);
                return new VideoViewHolder(view5);

            case 5:
                View view6 = inflater.inflate(R.layout.channel_video, parent, false);
                return new VideoViewHolder(view6);

            case 6:
                View view7 = inflater.inflate(R.layout.channel_audio_from_me, parent, false);
                return new AudioViewHolder(view7);

            case 7:
                View view8 = inflater.inflate(R.layout.channel_audio, parent, false);
                return new AudioViewHolder(view8);

            case 8:
                View view9 = inflater.inflate(R.layout.channel_document_from_me, parent, false);
                return new DocumentViewHolder(view9);

            case 9:
                View view10 = inflater.inflate(R.layout.channel_document, parent, false);
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

        TextView messageText;
        TextView messageTime;
        TextView sender;

        RelativeLayout messageLinearLayout;

        TextView amountOfSeen;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = itemView.findViewById(R.id.message_text_layout);
            messageTime = itemView.findViewById(R.id.message_time_layout);
            messageLinearLayout = itemView.findViewById(R.id.messageLinearLayout);
            amountOfSeen = itemView.findViewById(R.id.amount_seen);

            sender = itemView.findViewById(R.id.senderOfMessage);

        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView messageImage;
        TextView messageTime;
        ProgressBar messageProgress;
        TextView amountOfSeen;

        TextView sender;
        RelativeLayout rLayout;


        public ImageViewHolder(View itemView) {
            super(itemView);

            messageImage = itemView.findViewById(R.id.messageImage);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageProgress = itemView.findViewById(R.id.messageProgressBar);

            amountOfSeen = itemView.findViewById(R.id.amount_seen);
            sender = itemView.findViewById(R.id.senderOfMessage);

            rLayout = itemView.findViewById(R.id.rLayout);
        }

    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {

        TextView messageTime;
        VideoView messageVideo;
        ImageView playButton;
        TextView amountOfSeen;

        TextView sender;
        RelativeLayout rLayout;

        public VideoViewHolder(View itemView) {
            super(itemView);

            messageTime = itemView.findViewById(R.id.messageTime);
            messageVideo = itemView.findViewById(R.id.messageVideo);
            sender = itemView.findViewById(R.id.senderOfMessage);
            rLayout = itemView.findViewById(R.id.rLayout);
            playButton = itemView.findViewById(R.id.play_button);
            amountOfSeen = itemView.findViewById(R.id.amount_seen);

        }
    }

    public static class AudioViewHolder extends RecyclerView.ViewHolder {

        ImageButton playAudioFile;
        SeekBar seekBarAudio;

        TextView amountOfSeen;
        TextView sender;
        TextView audioTime;

        RelativeLayout rLayout;

        public AudioViewHolder(View itemView) {
            super(itemView);

            playAudioFile = itemView.findViewById(R.id.play_audio);
            seekBarAudio = itemView.findViewById(R.id.audio_seekbar);

            amountOfSeen = itemView.findViewById(R.id.amount_seen);
            sender = itemView.findViewById(R.id.senderOfMessage);

            rLayout = itemView.findViewById(R.id.rLayout);
            audioTime = itemView.findViewById(R.id.audio_time);
        }
    }


    public static class DocumentViewHolder extends RecyclerView.ViewHolder {

        LinearLayout mainDoc;
        TextView documentName;
        TextView timeMessage;

        TextView amountOfSeen;
        TextView sender;


        public DocumentViewHolder(View itemView) {
            super(itemView);

            mainDoc = itemView.findViewById(R.id.mainDoc);
            documentName = itemView.findViewById(R.id.documentName);
            timeMessage = itemView.findViewById(R.id.timeMessage);

            amountOfSeen = itemView.findViewById(R.id.amount_seen);
            sender = itemView.findViewById(R.id.senderOfMessage);

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


}
