package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Messages;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Messages> messagesList;
    private Context mContext;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;


    public CommentAdapter(List<Messages> messagesList, Context mContext) {
        this.messagesList = messagesList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Messages message = messagesList.get(position);
        holder.setProfilePic(message.getProfilePic());
        holder.commentName.setText(message.getName());
        holder.comment.setText(message.getContent());
        holder.time.setText(getTimeAgo(message.getTimestamp(), mContext));

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        EmojiTextView commentName, comment, seeReplies, reply, time;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            commentName = itemView.findViewById(R.id.commentName);
            comment = itemView.findViewById(R.id.comment);
            seeReplies = itemView.findViewById(R.id.seeReplies);
            reply = itemView.findViewById(R.id.reply);
            time = itemView.findViewById(R.id.time);

        }

        public void setProfilePic(String thumbnail){

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
