package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.CommentChannel;
import marcelin.thierry.chatapp.utils.CheckInternet_;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<CommentChannel> messagesList;
    private Context mContext;
    private boolean isOn;

    private DatabaseReference mCommentReference = FirebaseDatabase.getInstance().getReference().child("c");
    private DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference().child("ads_users");

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

  //  private boolean isOn = false;


    public CommentAdapter(List<CommentChannel> messagesList, Context mContext, boolean isOn) {
        this.messagesList = messagesList;
        this.mContext = mContext;
        this.isOn = isOn;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        CommentChannel message = messagesList.get(position);
        holder.setProfilePic(message.getProfilePic());
        holder.commentName.setText(message.getName());
        holder.comment.setText(message.getContent());
        holder.time.setText(getTimeAgo(message.getTimestamp(), mContext));

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(mContext);

        holder.repliesList.setHasFixedSize(true);
        holder.repliesList.setLayoutManager(mLinearLayoutManager);
        holder.repliesList.setNestedScrollingEnabled(false);

        holder.replyText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(Objects.requireNonNull(holder.replyText.getText()).toString().trim())) {
                    holder.sendReply.setText(mContext.getString(R.string.cancel));
                } else {
                    holder.sendReply.setText(mContext.getString(R.string.reply));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        int count = message.getR().size();
        if (count > 1) {
            //holder.seeReplies.setText(String.format("%s(%s)", holder.seeReplies.getText().toString(), String.valueOf(count - 1)));
            if(isOn){
                holder.repliesList.setVisibility(View.VISIBLE);
                holder.seeReplies.setText(R.string.close_replies);
                RepliesAdapter repliesAdapter = new RepliesAdapter(mContext, message.getReplyMessages());
                holder.repliesList.setAdapter(repliesAdapter);
                repliesAdapter.notifyDataSetChanged();
            }else {
                holder.seeReplies.setText(MessageFormat.format("{0}({1})", mContext.getString(R.string.see_replies), count - 1));
            }
            // On click to render the next messages
            holder.seeReplies.setOnClickListener(v -> {
                if (!isOn) {
                    holder.seeReplies.setText(MessageFormat.format("{0}({1})",mContext.getString(R.string.see_replies), count - 1));
                    holder.repliesList.setVisibility(View.GONE);
                } else {
                    holder.repliesList.setVisibility(View.VISIBLE);
                    holder.seeReplies.setText(R.string.close_replies);
                    RepliesAdapter repliesAdapter = new RepliesAdapter(mContext, message.getReplyMessages());
                    holder.repliesList.setAdapter(repliesAdapter);
                    repliesAdapter.notifyDataSetChanged();

                }
                isOn = !isOn;
            });
        } else {
            holder.seeReplies.setVisibility(View.GONE);
        }

        String myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
        Map<String, Object> repliesMap = new HashMap<>();
        repliesMap.put(Objects.requireNonNull(myPhone), myPhone);

        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#000000");
        commentContentMap.put("from", myPhone);
        commentContentMap.put("r", repliesMap);
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);

        holder.reply.setOnClickListener(v -> {
            // Put a reply message
            holder.replyLayout.setVisibility(View.VISIBLE);
            holder.replyText.setHint(mContext.getString(R.string.replying_to) + message.getName());
            holder.sendReply.setOnClickListener(v1 -> {
                if (holder.sendReply.getText().equals(mContext.getString(R.string.cancel))) {
                    holder.replyLayout.setVisibility(View.GONE);
                } else {
                    new CheckInternet_(internet -> {
                        if (internet) {
                            String textEntered = Objects.requireNonNull(holder.replyText.getText()).toString().trim();
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
                                        if(databaseError == null){
                                            Toast.makeText(mContext, mContext.getString(R.string.message_sent), Toast.LENGTH_SHORT).show();
                                            holder.replyLayout.setVisibility(View.GONE);
                                        }else{
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

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        EmojiTextView commentName, comment, seeReplies, reply, time;
        EmojiEditText replyText;
        RelativeLayout replyLayout;
        Button sendReply;
        RecyclerView repliesList;

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

}
