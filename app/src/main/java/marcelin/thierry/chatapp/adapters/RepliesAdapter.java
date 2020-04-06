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

public class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder> {

    private List<Messages> messages;
    private Context mContext;

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;


    public RepliesAdapter(Context mContext, List<Messages> messages) {
        this.mContext = mContext;
        this.messages = messages;
    }


    @NonNull
    @Override
    public RepliesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_layout, parent, false);
        return new RepliesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RepliesViewHolder holder, int position) {
        Messages currentReply = messages.get(position);
        holder.setProfilePic(currentReply.getProfilePic());
        holder.comment.setText(currentReply.getContent());
        holder.commentName.setText(currentReply.getName());
        holder.time.setText(getTimeAgo(currentReply.getTimestamp(), mContext));
        holder.seeReplies.setVisibility(View.GONE);
        holder.reply.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class RepliesViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profilePic;
        EmojiTextView commentName, comment, time, seeReplies, reply;

        public RepliesViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            comment = itemView.findViewById(R.id.comment);
            commentName = itemView.findViewById(R.id.commentName);
            time = itemView.findViewById(R.id.time);
            seeReplies = itemView.findViewById(R.id.seeReplies);
            reply = itemView.findViewById(R.id.reply);

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
