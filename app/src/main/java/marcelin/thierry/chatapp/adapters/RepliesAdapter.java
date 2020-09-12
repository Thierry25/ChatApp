package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.utils.CheckInternet_;
import pl.droidsonroids.gif.GifTextView;

public class RepliesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Messages> messages;
    private Context mContext;
    private LinearLayout mLinearLayout;

    private DatabaseReference mCommentReference = FirebaseDatabase.getInstance().getReference().child("c");
    private DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference().child("ads_users");
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    private static List<MediaPlayer> mediaPlayers = new ArrayList<>();


    public RepliesAdapter(Context mContext, List<Messages> messages, LinearLayout mLinearLayout) {
        this.mContext = mContext;
        this.messages = messages;
        this.mLinearLayout = mLinearLayout;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType){
            case 0:
                View view = inflater.inflate(R.layout.comment_layout, parent, false);
                return new RepliesViewHolder(view);

            case 1:
                View view1= inflater.inflate(R.layout.comment_audio_layout, parent, false);
                return new AudioCommentViewHolder(view1);

            default:
                return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages currentReply = messages.get(position);
        String myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
        Map<String, Object> repliesMap = new HashMap<>();
        repliesMap.put(Objects.requireNonNull(myPhone), myPhone);

        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#000000");
        commentContentMap.put("from", myPhone);
        commentContentMap.put("r", repliesMap);
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);

        switch (holder.getItemViewType()){
            case 0:
                ((RepliesViewHolder) holder).setProfilePic(currentReply.getProfilePic());
                if(currentReply.getContent().contains("@")){
                    String[] parts = currentReply.getContent().split("`");
                    String part1 = getColoredSpanned(parts[0], "#20BF9F");
                    String part2 = getColoredSpanned(parts[1], "#000000");


                    ((RepliesViewHolder) holder).comment.setText(Html.fromHtml(part1+ part2));
                }else{
                    ((RepliesViewHolder) holder).comment.setText(currentReply.getContent());
                }
                //holder.comment.setText(currentReply.getContent());
                ((RepliesViewHolder) holder).commentName.setText(currentReply.getName());
                ((RepliesViewHolder) holder).time.setText(getTimeAgo(currentReply.getTimestamp(), mContext));
                ((RepliesViewHolder) holder).seeReplies.setVisibility(View.GONE);

                ((RepliesViewHolder) holder).replyText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (TextUtils.isEmpty(Objects.requireNonNull(((RepliesViewHolder) holder).replyText.getText()).toString().trim())) {
                            ((RepliesViewHolder) holder).sendReply.setText(mContext.getString(R.string.cancel));
                        } else {
                            ((RepliesViewHolder) holder).sendReply.setText(mContext.getString(R.string.reply));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });


                // holder.reply.setVisibility(View.GONE);
                ((RepliesViewHolder) holder).reply.setOnClickListener(v -> {
                    ((RepliesViewHolder) holder).replyLayout.setVisibility(View.VISIBLE);
                    mLinearLayout.setVisibility(View.GONE);
                    ((RepliesViewHolder) holder).replyingTo.setText(MessageFormat.format("{0}{1}", "@", currentReply.getName()));
                    ((RepliesViewHolder) holder).replyText.setHint("");
                    ((RepliesViewHolder) holder).replyingTo.setVisibility(View.VISIBLE);
                    ((RepliesViewHolder) holder).sendReply.setOnClickListener(v1 ->{
                        if (((RepliesViewHolder) holder).sendReply.getText().equals(mContext.getString(R.string.cancel))) {
                            ((RepliesViewHolder) holder).replyLayout.setVisibility(View.GONE);
                            mLinearLayout.setVisibility(View.VISIBLE);
                        }else{
                            new CheckInternet_(internet -> {
                                if(internet){
                                    String textEntered = ((RepliesViewHolder) holder).replyingTo.getText().toString()
                                            + "` "
                                            + Objects.requireNonNull(((RepliesViewHolder) holder).replyText.getText().toString().trim());
                                    commentContentMap.put("parent", currentReply.getParent());
                                    commentContentMap.put("content", textEntered);

                                    DatabaseReference msg_push = mCommentReference.push();
                                    String push_id = msg_push.getKey();
                                    Map<String, Object> commentMap = new HashMap<>();
                                    commentMap.put(push_id, commentContentMap);

                                    mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                                    });

                                    Map<String, Object> commentByMap = new HashMap<>();
                                    commentByMap.put(myPhone, ServerValue.TIMESTAMP);

                                    mCommentReference.child(currentReply.getParent()).child("r").child(push_id)
                                            .updateChildren(commentByMap, (databaseError, databaseReference) -> {
                                                if(databaseError == null){
                                                    Toast.makeText(mContext, mContext.getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                                                    ((RepliesViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                                    mLinearLayout.setVisibility(View.VISIBLE);
                                                }else{
                                                    Toast.makeText(mContext, databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    Map<String, Object> notificationMap = new HashMap<>();
                                    notificationMap.put(/* replier phone number*/"re", myPhone);
                                    notificationMap.put("t1", ServerValue.TIMESTAMP);
                                    notificationMap.put(/* comment_id*/"c", currentReply.getParent());
                                    notificationMap.put(/* reply_id*/"r", push_id);
                                    notificationMap.put(/*channel_name*/"ch", currentReply.getChannelName());
                                    notificationMap.put(/*channel_image*/"chi", currentReply.getInitialChannelImage());
                                    notificationMap.put(/*initial_post_id*/"id", currentReply.getInitialCommentId());
                                    notificationMap.put(/*initial_post_content*/"co", currentReply.getInitialCommentContent());
                                    notificationMap.put(/*initial_post_color*/"col", currentReply.getInitialColor());
                                    notificationMap.put(/*initial_post_type*/"ty", currentReply.getInitialMessageType());
                                    notificationMap.put(/*initial_post_timestamp*/"t2", currentReply.getInitialTimestamp());
                                    notificationMap.put(/*initial_post_likes*/"l", currentReply.getInitialLikesCount());
                                    notificationMap.put(/*initial_post_comments_count*/"cc", currentReply.getInitialCommentsCount());
                                    notificationMap.put(/*initial_post_comments_seen*/"s", currentReply.getSeeInitalCount());
                                    notificationMap.put(/*seen_or_not*/"se", false);

                                    Map<String, Object> msgContentMap = new HashMap<>();
                                    msgContentMap.put(push_id, notificationMap);
                                    mUsersReference.child(currentReply.getFrom()).child("r").child(currentReply.getChannelName()).updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                    });
                                }else{
                                    Toast.makeText(mContext, "No internet Connectivity", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                });
                break;

            case 1:
                MediaPlayer mediaPlayer = new MediaPlayer();
                final boolean[] isReady = {false};
                final int[] duration = {0};
                mediaPlayers.add(mediaPlayer);
                final String[] time = new String[1];

                try {
                    Log.i("((MediaPlayer))", "MediaPlayer call-ed");
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(currentReply.getContent());

                    mediaPlayer.setOnPreparedListener(mediaPlayer12 -> {
                        isReady[0] = true;
                        duration[0] = mediaPlayer12.getDuration();
                        Log.i("((MediaPlayer))", "Duration of message: " + duration[0]);

                        time[0] = String.format("%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(duration[0]),
                                TimeUnit.MILLISECONDS.toSeconds(duration[0]) -
                                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration[0]))
                        );


                        ((AudioCommentViewHolder) holder).audio_seekbar.setMax(duration[0]);
                        ((AudioCommentViewHolder) holder).audio_time.setText(time[0]);
//                        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
//                        service.scheduleWithFixedDelay(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                ((AudioCommentViewHolder)holder).audio_seekbar.setProgress(mediaPlayer12.getCurrentPosition());
//                            }
//                        }, 1, 1, TimeUnit.MICROSECONDS);

                        // ((AudioViewHolder) holder).seekBarAudio.setProgress(mediaPlayer12.getCurrentPosition());
                    });

                    mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                        mediaPlayer1.pause();
                        ((AudioCommentViewHolder) holder).play_audio.setImageResource(R.drawable.ic_play_circle_filled);
                        ((AudioCommentViewHolder) holder).audio_seekbar.setVisibility(View.VISIBLE);
                        ((AudioCommentViewHolder) holder).gif.setVisibility(View.GONE);
                    });

                    mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                        mp.reset();
                        try {
                            mp.setDataSource(currentReply.getContent());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    });

                    ((AudioCommentViewHolder) holder).play_audio.setOnClickListener((View view) -> {
                        Log.i("((MediaPlayer))", "Play Button clicked");
                        if (isReady[0]) {
                            if (mediaPlayer.isPlaying()) {
                                ((AudioCommentViewHolder) holder).play_audio.setImageResource(R.drawable.ic_play_circle_filled);
                                ((AudioCommentViewHolder) holder).audio_seekbar.setVisibility(View.VISIBLE);
                                ((AudioCommentViewHolder) holder).gif.setVisibility(View.GONE);
                                mediaPlayer.pause();
                            } else {
                                ((AudioCommentViewHolder) holder).play_audio.setImageResource(R.drawable.ic_pause_circle_filled);
                                ((AudioCommentViewHolder) holder).audio_seekbar.setVisibility(View.GONE);
                                ((AudioCommentViewHolder) holder).gif.setVisibility(View.VISIBLE);
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
//
//                    ((AudioCommentViewHolder) holder).audio_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//                        @Override
//                        public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {
//
//                            if (input) {
//                                mediaPlayer.seekTo((progress) * 100);
//                            }else{
//                                return;
//                            }
//
//                        }
//
//                        @Override
//                        public void onStartTrackingTouch(SeekBar seekBar) {
//
//                        }
//
//                        @Override
//                        public void onStopTrackingTouch(SeekBar seekBar) {
//                        }
//                    });

                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    Log.e("MediaPlayerException", e.getMessage());
                    e.printStackTrace();
                }

                ((AudioCommentViewHolder) holder).setProfilePic(currentReply.getProfilePic());
                ((AudioCommentViewHolder) holder).commentName.setText(currentReply.getName());


                ((AudioCommentViewHolder) holder).time.setText(getTimeAgo(currentReply.getTimestamp(), mContext));

                LinearLayoutManager mLinearLayoutManager1 = new LinearLayoutManager(mContext);

                ((AudioCommentViewHolder) holder).repliesList.setHasFixedSize(true);
                ((AudioCommentViewHolder) holder).repliesList.setLayoutManager(mLinearLayoutManager1);
                ((AudioCommentViewHolder) holder).repliesList.setNestedScrollingEnabled(false);

                ((AudioCommentViewHolder) holder).replyText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (TextUtils.isEmpty(Objects.requireNonNull(((AudioCommentViewHolder) holder).replyText.getText()).toString().trim())) {
                            ((AudioCommentViewHolder) holder).sendReply.setText(mContext.getString(R.string.cancel));
                        } else {
                            ((AudioCommentViewHolder) holder).sendReply.setText(mContext.getString(R.string.reply));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                ((AudioCommentViewHolder) holder).reply.setOnClickListener(v -> {
                    ((AudioCommentViewHolder) holder).replyLayout.setVisibility(View.VISIBLE);
                    mLinearLayout.setVisibility(View.GONE);
                    ((AudioCommentViewHolder) holder).replyingTo.setText(MessageFormat.format("{0}{1}", "@", currentReply.getName()));
                    ((AudioCommentViewHolder) holder).replyText.setHint("");
                    ((AudioCommentViewHolder) holder).replyingTo.setVisibility(View.VISIBLE);
                    ((AudioCommentViewHolder) holder).sendReply.setOnClickListener(v1 ->{
                        if (((AudioCommentViewHolder) holder).sendReply.getText().equals(mContext.getString(R.string.cancel))) {
                            ((AudioCommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                            mLinearLayout.setVisibility(View.VISIBLE);
                        }else{
                            new CheckInternet_(internet -> {
                                if(internet){
                                    String textEntered = ((AudioCommentViewHolder) holder).replyingTo.getText().toString()
                                            + "` "
                                            + Objects.requireNonNull(((AudioCommentViewHolder) holder).replyText.getText().toString().trim());
                                    commentContentMap.put("parent", currentReply.getParent());
                                    commentContentMap.put("content", textEntered);

                                    DatabaseReference msg_push = mCommentReference.push();
                                    String push_id = msg_push.getKey();
                                    Map<String, Object> commentMap = new HashMap<>();
                                    commentMap.put(push_id, commentContentMap);

                                    mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                                    });

                                    Map<String, Object> commentByMap = new HashMap<>();
                                    commentByMap.put(myPhone, ServerValue.TIMESTAMP);

                                    mCommentReference.child(currentReply.getParent()).child("r").child(push_id)
                                            .updateChildren(commentByMap, (databaseError, databaseReference) -> {
                                                if(databaseError == null){
                                                    Toast.makeText(mContext, mContext.getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                                                    ((AudioCommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                                    mLinearLayout.setVisibility(View.VISIBLE);
                                                }else{
                                                    Toast.makeText(mContext, databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                                                }
                                            });

                                    Map<String, Object> notificationMap = new HashMap<>();
                                    notificationMap.put(/* replier phone number*/"re", myPhone);
                                    notificationMap.put("t1", ServerValue.TIMESTAMP);
                                    notificationMap.put(/* comment_id*/"c", currentReply.getParent());
                                    notificationMap.put(/* reply_id*/"r", push_id);
                                    notificationMap.put(/*channel_name*/"ch", currentReply.getChannelName());
                                    notificationMap.put(/*channel_image*/"chi", currentReply.getInitialChannelImage());
                                    notificationMap.put(/*initial_post_id*/"id", currentReply.getInitialCommentId());
                                    notificationMap.put(/*initial_post_content*/"co", currentReply.getInitialCommentContent());
                                    notificationMap.put(/*initial_post_color*/"col", currentReply.getInitialColor());
                                    notificationMap.put(/*initial_post_type*/"ty", currentReply.getInitialMessageType());
                                    notificationMap.put(/*initial_post_timestamp*/"t2", currentReply.getInitialTimestamp());
                                    notificationMap.put(/*initial_post_likes*/"l", currentReply.getInitialLikesCount());
                                    notificationMap.put(/*initial_post_comments_count*/"cc", currentReply.getInitialCommentsCount());
                                    notificationMap.put(/*initial_post_comments_seen*/"s", currentReply.getSeeInitalCount());
                                    notificationMap.put(/*seen_or_not*/"se", false);

                                    Map<String, Object> msgContentMap = new HashMap<>();
                                    msgContentMap.put(push_id, notificationMap);
                                    mUsersReference.child(currentReply.getFrom()).child("r").child(currentReply.getChannelName()).updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                    });
                                }else{
                                    Toast.makeText(mContext, "No internet Connectivity", Toast.LENGTH_SHORT).show();
                                }

                            });
                        }
                    });
                });


                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class RepliesViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profilePic;
        EmojiTextView commentName, comment, time, seeReplies, reply, replyingTo;
        RelativeLayout replyLayout;
        Button sendReply;
        EmojiEditText replyText;


        public RepliesViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            comment = itemView.findViewById(R.id.comment);
            commentName = itemView.findViewById(R.id.commentName);
            time = itemView.findViewById(R.id.time);
            seeReplies = itemView.findViewById(R.id.seeReplies);
            reply = itemView.findViewById(R.id.reply);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            replyText = itemView.findViewById(R.id.replyText);
            sendReply = itemView.findViewById(R.id.sendReply);
            replyingTo = itemView.findViewById(R.id.replying_to);
        }

        public void setProfilePic(String thumbnail) {

            profilePic = itemView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(profilePic);
        }

    }

    public class AudioCommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        EmojiTextView commentName, seeReplies, reply, time, replyingTo;
        EmojiEditText replyText;
        RelativeLayout replyLayout;
        Button sendReply;
        RecyclerView repliesList;
        ImageButton play_audio;
        SeekBar audio_seekbar;
        TextView audio_time;
        GifTextView gif;

        public AudioCommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            commentName = itemView.findViewById(R.id.commentName);
            seeReplies = itemView.findViewById(R.id.seeReplies);
            reply = itemView.findViewById(R.id.reply);
            time = itemView.findViewById(R.id.time);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            replyText = itemView.findViewById(R.id.replyText);
            sendReply = itemView.findViewById(R.id.sendReply);
            repliesList = itemView.findViewById(R.id.repliesList);

            play_audio = itemView.findViewById(R.id.play_audio);
            audio_seekbar = itemView.findViewById(R.id.audio_seekbar);
            audio_time = itemView.findViewById(R.id.audio_time);
            gif = itemView.findViewById(R.id.gif);
            replyingTo = itemView.findViewById(R.id.replying_to);
        }

        public void setProfilePic(String thumbnail) {

            profilePic = itemView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(profilePic);
        }

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
    private String getColoredSpanned(String text, String color) {
        return "<font color=" + color + ">" + text + "</font>";
    }

    @Override
    public int getItemViewType(int position) {
        Messages currentMessage = messages.get(position);
        String color = "#FFFFFF";
        if(!currentMessage.getColor().equals(color)){
            return 0;
        }else{
            return 1;
        }
    }
}
