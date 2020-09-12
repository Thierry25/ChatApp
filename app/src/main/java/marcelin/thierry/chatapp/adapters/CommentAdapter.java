package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.islamassem.voicemessager.VoiceMessagerFragment;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;


import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.CommentChannel;
import marcelin.thierry.chatapp.utils.CheckInternet_;
import pl.droidsonroids.gif.GifTextView;

public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements VoiceMessagerFragment.OnControllerClick {
    private List<CommentChannel> messagesList;
    private Context mContext;
    private boolean isOn;
    private LinearLayout mLinearLayout;
    private FrameLayout frameLayout;
    private boolean available;

    private DatabaseReference mCommentReference = FirebaseDatabase.getInstance().getReference().child("c");
    private DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference().child("ads_users");
    private static List<MediaPlayer> mediaPlayers = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private Fragment fragment = VoiceMessagerFragment.build(this, true);
    private static final StorageReference mAudioStorage = FirebaseStorage.getInstance().getReference();


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    private static String mFileName = null;

    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";
    final java.util.Random rand = new java.util.Random();
    final Set<String> identifiers = new HashSet<>();
    private CommentChannel commentChannel;

    //  private boolean isOn = false;

    public CommentAdapter(List<CommentChannel> messagesList, Context mContext, boolean isOn, LinearLayout mLinearLayout, Boolean available, FrameLayout frameLayout) {
        this.messagesList = messagesList;
        this.mContext = mContext;
        this.isOn = isOn;
        this.mLinearLayout = mLinearLayout;
        this.available = available;
        this.frameLayout = frameLayout;

        mediaPlayers.clear();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case 0:
                View view = inflater.inflate(R.layout.comment_layout, parent, false);
                return new CommentViewHolder(view);

            case 1:
                View view1 = inflater.inflate(R.layout.comment_audio_layout, parent, false);
                return new AudioCommentViewHolder(view1);

            default:
                return viewHolder;
        }

    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentChannel message = messagesList.get(position);
        String myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        Map<String, Object> repliesMap = new HashMap<>();
        repliesMap.put(Objects.requireNonNull(myPhone), myPhone);

        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#000000");
        commentContentMap.put("from", myPhone);
        commentContentMap.put("r", repliesMap);
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);

        switch (holder.getItemViewType()) {
            case 0:
                ((CommentViewHolder) holder).setProfilePic(message.getProfilePic());
                ((CommentViewHolder) holder).commentName.setText(message.getName());
                ((CommentViewHolder) holder).comment.setText(message.getContent());


                ((CommentViewHolder) holder).time.setText(getTimeAgo(message.getTimestamp(), mContext));

                LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mContext);

                ((CommentViewHolder) holder).repliesList.setHasFixedSize(true);
                ((CommentViewHolder) holder).repliesList.setLayoutManager(mLinearLayoutManager);
                ((CommentViewHolder) holder).repliesList.setNestedScrollingEnabled(false);

                ((CommentViewHolder) holder).replyText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (TextUtils.isEmpty(Objects.requireNonNull(((CommentViewHolder) holder).replyText.getText()).toString().trim())) {
                            ((CommentViewHolder) holder).sendReply.setText(mContext.getString(R.string.cancel));
                        } else {
                            ((CommentViewHolder) holder).sendReply.setText(mContext.getString(R.string.reply));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });


                int count = message.getR().size();
                if (count > 1) {
                    //holder.seeReplies.setText(String.format("%s(%s)", holder.seeReplies.getText().toString(), String.valueOf(count - 1)));
                    if (isOn) {
                        ((CommentViewHolder) holder).repliesList.setVisibility(View.VISIBLE);
                        ((CommentViewHolder) holder).seeReplies.setText(R.string.close_replies);
                        RepliesAdapter repliesAdapter = new RepliesAdapter(mContext, message.getReplyMessages(), mLinearLayout);
                        ((CommentViewHolder) holder).repliesList.setAdapter(repliesAdapter);
                        repliesAdapter.notifyDataSetChanged();
                    } else {
                        ((CommentViewHolder) holder).seeReplies.setText(MessageFormat.format("{0}({1})", mContext.getString(R.string.see_replies), count - 1));
                    }
                    // On click to render the next messages
                    ((CommentViewHolder) holder).seeReplies.setOnClickListener(v -> {
                        if (!isOn) {
                            ((CommentViewHolder) holder).seeReplies.setText(MessageFormat.format("{0}({1})", mContext.getString(R.string.see_replies), count - 1));
                            ((CommentViewHolder) holder).repliesList.setVisibility(View.GONE);
                        } else {
                            ((CommentViewHolder) holder).repliesList.setVisibility(View.VISIBLE);
                            ((CommentViewHolder) holder).seeReplies.setText(R.string.close_replies);
                            RepliesAdapter repliesAdapter = new RepliesAdapter(mContext, message.getReplyMessages(), mLinearLayout);
                            ((CommentViewHolder) holder).repliesList.setAdapter(repliesAdapter);
                            repliesAdapter.notifyDataSetChanged();

                        }
                        isOn = !isOn;
                    });
                } else {
                    ((CommentViewHolder) holder).seeReplies.setVisibility(View.GONE);
                }

                ((CommentViewHolder) holder).reply.setOnClickListener(v -> {
                    ((CommentViewHolder) holder).choices.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).delete.setOnClickListener(v1 -> {
                        ((CommentViewHolder) holder).choices.setVisibility(View.GONE);
                    });

                    ((CommentViewHolder) holder).text.setOnClickListener(v1 -> {
                        mLinearLayout.setVisibility(View.GONE);
                        ((CommentViewHolder) holder).choices.setVisibility(View.GONE);
                        ((CommentViewHolder) holder).replyLayout.setVisibility(View.VISIBLE);
                        ((CommentViewHolder) holder).replyText.setHint(mContext.getString(R.string.replying_to) + message.getName());
                        ((CommentViewHolder) holder).sendReply.setOnClickListener(v2 -> {
                            if (((CommentViewHolder) holder).sendReply.getText().equals(mContext.getString(R.string.cancel))) {
                                ((CommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                mLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                new CheckInternet_(internet -> {
                                    if (internet) {
                                        String textEntered = Objects.requireNonNull(((CommentViewHolder) holder).replyText.getText()).toString().trim();
                                        // Toast.makeText(mContext, message.getCommentId(), Toast.LENGTH_SHORT).show();
                                        // put message id as parent
                                        commentContentMap.put("parent", message.getCommentId());
                                        commentContentMap.put("content", textEntered);

                                        DatabaseReference msg_push = mCommentReference.push();
                                        String push_id = msg_push.getKey();
                                        Map<String, Object> commentMap = new HashMap<>();
                                        commentMap.put(push_id, commentContentMap);

                                        mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                                        });

                                        Map<String, Object> commentByMap = new HashMap<>();
                                        commentByMap.put(myPhone, ServerValue.TIMESTAMP);

                                        mCommentReference.child(message.getCommentId()).child("r").child(push_id)
                                                .updateChildren(commentByMap, (databaseError, databaseReference) -> {
                                                    if (databaseError == null) {
                                                        Toast.makeText(mContext, mContext.getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                                                        ((CommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                                        mLinearLayout.setVisibility(View.VISIBLE);
                                                    } else {
                                                        Toast.makeText(mContext, databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        Map<String, Object> notificationMap = new HashMap<>();
                                        notificationMap.put(/* replier phone number*/"re", myPhone);
                                        notificationMap.put("t1", ServerValue.TIMESTAMP);
                                        notificationMap.put(/* comment_id*/"c", message.getCommentId());
                                        notificationMap.put(/* reply_id*/"r", push_id);
                                        notificationMap.put(/*channel_name*/"ch", message.getChannelName());
                                        notificationMap.put(/*channel_image*/"chi", message.getInitialChannelImage());
                                        notificationMap.put(/*initial_post_id*/"id", message.getInitialCommentId());
                                        notificationMap.put(/*initial_post_content*/"co", message.getInitialCommentContent());
                                        notificationMap.put(/*initial_post_color*/"col", message.getInitialColor());
                                        notificationMap.put(/*initial_post_type*/"ty", message.getInitialMessageType());
                                        notificationMap.put(/*initial_post_timestamp*/"t2", message.getInitialTimestamp());
                                        notificationMap.put(/*initial_post_likes*/"l", message.getInitialLikesCount());
                                        notificationMap.put(/*initial_post_comments_count*/"cc", message.getInitialCommentsCount());
                                        notificationMap.put(/*initial_post_comments_seen*/"s", message.getSeeInitalCount());
                                        notificationMap.put(/*seen_or_not*/"se", false);

                                        Map<String, Object> msgContentMap = new HashMap<>();
                                        msgContentMap.put(push_id, notificationMap);

                                        mUsersReference.child(message.getFrom()).child("r").child(message.getChannelName()).updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                        });
                                        //  mSendText.setText("");
                                        // TODO: Remove reply text along with whole layout to reply to messages. After check that this new code works properly.

                                    } else {
                                        Toast.makeText(mContext, "No internet Connectivity", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    });

                    ((CommentViewHolder) holder).audio.setOnClickListener(v1 -> {
                        new CheckInternet_(internet -> {
                           if(internet){
                               ((CommentViewHolder) holder).choices.setVisibility(View.GONE);
                               record(frameLayout);
                               mLinearLayout.setVisibility(View.GONE);
                               commentChannel = messagesList.get(position);
                           }
                        });

                    });

                });
                break;

            case 1:
                // Code for audio to be rendered here
                MediaPlayer mediaPlayer = new MediaPlayer();
                final boolean[] isReady = {false};
                final int[] duration = {0};
                mediaPlayers.add(mediaPlayer);
                final String[] time = new String[1];

                try {
                    Log.i("((MediaPlayer))", "MediaPlayer call-ed");
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(message.getContent());

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
                            mp.setDataSource(message.getContent());
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

                ((AudioCommentViewHolder) holder).setProfilePic(message.getProfilePic());
                ((AudioCommentViewHolder) holder).commentName.setText(message.getName());


                ((AudioCommentViewHolder) holder).time.setText(getTimeAgo(message.getTimestamp(), mContext));

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


                int count1 = message.getR().size();
                if (count1 > 1) {
                    //holder.seeReplies.setText(String.format("%s(%s)", holder.seeReplies.getText().toString(), String.valueOf(count - 1)));
                    if (isOn) {
                        ((AudioCommentViewHolder) holder).repliesList.setVisibility(View.VISIBLE);
                        ((AudioCommentViewHolder) holder).seeReplies.setText(R.string.close_replies);
                        RepliesAdapter repliesAdapter = new RepliesAdapter(mContext, message.getReplyMessages(), mLinearLayout);
                        ((AudioCommentViewHolder) holder).repliesList.setAdapter(repliesAdapter);
                        repliesAdapter.notifyDataSetChanged();
                    } else {
                        ((AudioCommentViewHolder) holder).seeReplies.setText(MessageFormat.format("{0}({1})", mContext.getString(R.string.see_replies), count1 - 1));
                    }
                    // On click to render the next messages
                    ((AudioCommentViewHolder) holder).seeReplies.setOnClickListener(v -> {
                        if (!isOn) {
                            ((AudioCommentViewHolder) holder).seeReplies.setText(MessageFormat.format("{0}({1})", mContext.getString(R.string.see_replies), count1 - 1));
                            ((AudioCommentViewHolder) holder).repliesList.setVisibility(View.GONE);
                        } else {
                            ((AudioCommentViewHolder) holder).repliesList.setVisibility(View.VISIBLE);
                            ((AudioCommentViewHolder) holder).seeReplies.setText(R.string.close_replies);
                            RepliesAdapter repliesAdapter = new RepliesAdapter(mContext, message.getReplyMessages(), mLinearLayout);
                            ((AudioCommentViewHolder) holder).repliesList.setAdapter(repliesAdapter);
                            repliesAdapter.notifyDataSetChanged();

                        }
                        isOn = !isOn;
                    });
                } else {
                    ((AudioCommentViewHolder) holder).seeReplies.setVisibility(View.GONE);
                }

                ((AudioCommentViewHolder) holder).reply.setOnClickListener(v -> {
                    ((AudioCommentViewHolder) holder).choices.setVisibility(View.VISIBLE);
                    ((AudioCommentViewHolder) holder).delete.setOnClickListener(v1 -> {
                        ((AudioCommentViewHolder) holder).choices.setVisibility(View.GONE);
                    });

                    ((AudioCommentViewHolder) holder).text.setOnClickListener(v1 -> {
                        mLinearLayout.setVisibility(View.GONE);
                        ((AudioCommentViewHolder) holder).choices.setVisibility(View.GONE);
                        ((AudioCommentViewHolder) holder).replyLayout.setVisibility(View.VISIBLE);
                        ((AudioCommentViewHolder) holder).replyText.setHint(mContext.getString(R.string.replying_to) + message.getName());
                        ((AudioCommentViewHolder) holder).sendReply.setOnClickListener(v2 -> {
                            if (((AudioCommentViewHolder) holder).sendReply.getText().equals(mContext.getString(R.string.cancel))) {
                                ((AudioCommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                mLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                new CheckInternet_(internet -> {
                                    if (internet) {
                                        String textEntered = Objects.requireNonNull(((AudioCommentViewHolder) holder).replyText.getText()).toString().trim();
                                        // Toast.makeText(mContext, message.getCommentId(), Toast.LENGTH_SHORT).show();
                                        // put message id as parent
                                        commentContentMap.put("parent", message.getCommentId());
                                        commentContentMap.put("content", textEntered);

                                        DatabaseReference msg_push = mCommentReference.push();
                                        String push_id = msg_push.getKey();
                                        Map<String, Object> commentMap = new HashMap<>();
                                        commentMap.put(push_id, commentContentMap);

                                        mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                                        });

                                        Map<String, Object> commentByMap = new HashMap<>();
                                        commentByMap.put(myPhone, ServerValue.TIMESTAMP);

                                        mCommentReference.child(message.getCommentId()).child("r").child(push_id)
                                                .updateChildren(commentByMap, (databaseError, databaseReference) -> {
                                                    if (databaseError == null) {
                                                        Toast.makeText(mContext, mContext.getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                                                        ((AudioCommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                                        mLinearLayout.setVisibility(View.VISIBLE);
                                                    } else {
                                                        Toast.makeText(mContext, databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                        Map<String, Object> notificationMap = new HashMap<>();
                                        notificationMap.put(/* replier phone number*/"re", myPhone);
                                        notificationMap.put("t1", ServerValue.TIMESTAMP);
                                        notificationMap.put(/* comment_id*/"c", message.getCommentId());
                                        notificationMap.put(/* reply_id*/"r", push_id);
                                        notificationMap.put(/*channel_name*/"ch", message.getChannelName());
                                        notificationMap.put(/*channel_image*/"chi", message.getInitialChannelImage());
                                        notificationMap.put(/*initial_post_id*/"id", message.getInitialCommentId());
                                        notificationMap.put(/*initial_post_content*/"co", message.getInitialCommentContent());
                                        notificationMap.put(/*initial_post_color*/"col", message.getInitialColor());
                                        notificationMap.put(/*initial_post_type*/"ty", message.getInitialMessageType());
                                        notificationMap.put(/*initial_post_timestamp*/"t2", message.getInitialTimestamp());
                                        notificationMap.put(/*initial_post_likes*/"l", message.getInitialLikesCount());
                                        notificationMap.put(/*initial_post_comments_count*/"cc", message.getInitialCommentsCount());
                                        notificationMap.put(/*initial_post_comments_seen*/"s", message.getSeeInitalCount());
                                        notificationMap.put(/*seen_or_not*/"se", false);

                                        Map<String, Object> msgContentMap = new HashMap<>();
                                        msgContentMap.put(push_id, notificationMap);

                                        mUsersReference.child(message.getFrom()).child("r").child(message.getChannelName()).updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                                        });
                                        //  mSendText.setText("");
                                        // TODO: Remove reply text along with whole layout to reply to messages. After check that this new code works properly.

                                    } else {
                                        Toast.makeText(mContext, "No internet Connectivity", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    });

                    ((AudioCommentViewHolder) holder).audio.setOnClickListener(v1 -> {
                        new CheckInternet_(internet -> {
                            if(internet){
                                ((AudioCommentViewHolder) holder).choices.setVisibility(View.GONE);
                                record(frameLayout);
                                mLinearLayout.setVisibility(View.GONE);
                                commentChannel = messagesList.get(position);
                            }
                        });

                    });

                });


                break;

            default:

        }


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        final CommentChannel currentCommentChannel = messagesList.get(position);
        String color = "#FFFFFF";
        if (!currentCommentChannel.getColor().equals(color)) {
            // Message comment
            return 0;
        } else {
            // Audio comment
            return 1;
        }
    }

    @Override
    public void onRecordClick() {
    }

    @Override
    public void onFinishRecording(File file) {

    }

    @Override
    public void onSendClick(File file, int duration) {
        if(duration > 0 && duration < 11) {
            sendAudio(file, commentChannel);
            remove(fragment);
            mLinearLayout.setVisibility(View.VISIBLE);
        }else{
            Toast.makeText(mContext, R.string.audio_not_sent, Toast.LENGTH_SHORT).show();
            remove(fragment);
            mLinearLayout.setVisibility(View.VISIBLE);
        }
    }

    private void sendAudio(File file, CommentChannel message) {
        String myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
        Map<String, Object> repliesMap = new HashMap<>();
        repliesMap.put(Objects.requireNonNull(myPhone), myPhone);

        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#FFFFFF");
        commentContentMap.put("from", myPhone);
        commentContentMap.put("r", repliesMap);
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);
        commentContentMap.put("parent", message.getCommentId());


        DatabaseReference msg_push1 = mCommentReference.push();

        String push_id1= msg_push1.getKey();

        StorageReference filePath = mAudioStorage.child("c_au").child(push_id1+ ".gp3");

        Uri voiceUri = Uri.fromFile(new File(file.getAbsolutePath()));
        filePath.putFile(voiceUri).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                String downloadUrl = Objects.requireNonNull(task.getResult().getDownloadUrl())
                        .toString();
                commentContentMap.put("content", downloadUrl);

                DatabaseReference msg_push = mCommentReference.push();
                String push_id = msg_push.getKey();
                Map<String, Object> commentMap = new HashMap<>();
                commentMap.put(push_id, commentContentMap);

                mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                });

                Map<String, Object> commentByMap = new HashMap<>();
                commentByMap.put(myPhone, ServerValue.TIMESTAMP);

                mCommentReference.child(message.getCommentId()).child("r").child(push_id)
                        .updateChildren(commentByMap, (databaseError, databaseReference) -> {
                            if (databaseError == null) {
                                Toast.makeText(mContext, mContext.getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                                //((CommentViewHolder) holder).replyLayout.setVisibility(View.GONE);
                                mLinearLayout.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(mContext, databaseError.getDetails(), Toast.LENGTH_SHORT).show();
                            }
                        });


                Map<String, Object> notificationMap = new HashMap<>();
                notificationMap.put(/* replier phone number*/"re", myPhone);
                notificationMap.put("t1", ServerValue.TIMESTAMP);
                notificationMap.put(/* comment_id*/"c", message.getCommentId());
                notificationMap.put(/* reply_id*/"r", push_id);
                notificationMap.put(/*channel_name*/"ch", message.getChannelName());
                notificationMap.put(/*channel_image*/"chi", message.getInitialChannelImage());
                notificationMap.put(/*initial_post_id*/"id", message.getInitialCommentId());
                notificationMap.put(/*initial_post_content*/"co", message.getInitialCommentContent());
                notificationMap.put(/*initial_post_color*/"col", message.getInitialColor());
                notificationMap.put(/*initial_post_type*/"ty", message.getInitialMessageType());
                notificationMap.put(/*initial_post_timestamp*/"t2", message.getInitialTimestamp());
                notificationMap.put(/*initial_post_likes*/"l", message.getInitialLikesCount());
                notificationMap.put(/*initial_post_comments_count*/"cc", message.getInitialCommentsCount());
                notificationMap.put(/*initial_post_comments_seen*/"s", message.getSeeInitalCount());
                notificationMap.put(/*seen_or_not*/"se", false);

                Map<String, Object> msgContentMap = new HashMap<>();
                msgContentMap.put(push_id, notificationMap);

                mUsersReference.child(message.getFrom()).child("r").child(message.getChannelName()).updateChildren(msgContentMap, (databaseError, databaseReference) -> {
                });
            }else{
                Toast.makeText(mContext,"Errrr", Toast.LENGTH_SHORT).show();

            }
        });



    }

    @Override
    public void onCloseClick() {
        remove(fragment);
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        EmojiTextView commentName, comment, seeReplies, reply, time;
        EmojiEditText replyText;
        RelativeLayout replyLayout;
        Button sendReply;
        RecyclerView repliesList;
        LinearLayout choices;
        TextView audio, text;
        ImageView delete;
//        FrameLayout fragment_contaainer;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            commentName = itemView.findViewById(R.id.commentName);
            comment = itemView.findViewById(R.id.comment);
            seeReplies = itemView.findViewById(R.id.seeReplies);
            reply = itemView.findViewById(R.id.reply);
            time = itemView.findViewById(R.id.time);
            replyLayout = itemView.findViewById(R.id.replyLayout);
            replyText = itemView.findViewById(R.id.replyText);
            sendReply = itemView.findViewById(R.id.sendReply);
            repliesList = itemView.findViewById(R.id.repliesList);
            choices = itemView.findViewById(R.id.choices);
            audio = itemView.findViewById(R.id.audio);
            text = itemView.findViewById(R.id.text);
            delete = itemView.findViewById(R.id.delete);
//            fragment_contaainer = itemView.findViewById(R.id.fragment_container);
        }

        public void setProfilePic(String thumbnail) {

            profilePic = itemView.findViewById(R.id.profilePic);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(profilePic);
        }

    }

    public static class AudioCommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        EmojiTextView commentName, seeReplies, reply, time;
        EmojiEditText replyText;
        RelativeLayout replyLayout;
        Button sendReply;
        RecyclerView repliesList;
        ImageButton play_audio;
        SeekBar audio_seekbar;
        TextView audio_time;
        GifTextView gif;
        LinearLayout choices;
        TextView audio, text;
        ImageView delete;

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
            choices = itemView.findViewById(R.id.choices);
            audio = itemView.findViewById(R.id.audio);
            text = itemView.findViewById(R.id.text);
            delete = itemView.findViewById(R.id.delete);
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

    public void stopMediaPlayers() {
        if (!mediaPlayers.isEmpty()) {
            for (MediaPlayer p : mediaPlayers) {
                if (p != null) {
                    p.release();
                }
            }
        }
    }

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

    public void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        if (fragment.isAdded())
            fragmentTransaction.show(fragment);
        else
            fragmentTransaction.add(R.id.fragment_contaainer, fragment, "h").addToBackStack(null);
        fragmentTransaction.commit();
    }

    public void remove(Fragment fragment) {
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

        if (fragment.isAdded())
            fragmentTransaction.remove(fragment);
        fragmentTransaction.commit();
    }

    public void record(FrameLayout frameLayout) {
        if (getMicrophoneAvailable()) {
            frameLayout.setVisibility(View.VISIBLE);
//            findViewById(R.id.fragment_contaainer).setVisibility(View.VISIBLE);
            showFragment(fragment);
        } else
            Toast.makeText(mContext, "Microphone not available...", Toast.LENGTH_SHORT).show();
    }

    //returns whether the microphone is available
    public boolean getMicrophoneAvailable() {
        MediaRecorder recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
//        recorder.setOutputFile(new File(context.getCacheDir(), "MediaUtil#micAvailTestFile").getAbsolutePath());
        mFileName = Objects.requireNonNull(mContext.getExternalCacheDir()).getAbsolutePath();
        mFileName += "/" + randomIdentifier() + ".3gp";

        recorder.setOutputFile(mFileName);
        available = true;
        try {
            recorder.prepare();
            recorder.start();

        } catch (Exception exception) {
//            available = false;
            exception.printStackTrace();
        }
        recorder.release();
        return available;
    }

}
