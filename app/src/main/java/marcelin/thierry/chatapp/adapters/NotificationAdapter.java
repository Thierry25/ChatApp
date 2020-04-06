package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.ReplyNotification;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private Context mContext;
    private List<ReplyNotification> replyNotificationList;
    private NotificationSelectedListener notificationSelectedListener;


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    public NotificationAdapter(Context mContext, List<ReplyNotification> replyNotificationList) {
        this.mContext = mContext;
        this.replyNotificationList = replyNotificationList;
    }

    public void setNotificationSelectedListener(NotificationAdapter.NotificationSelectedListener notificationSelectedListener) {
        this.notificationSelectedListener = notificationSelectedListener;
    }


    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notidication_row, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        ReplyNotification currentReplyNotification = replyNotificationList.get(position);
        holder.timestamp.setText(getTimeAgo(currentReplyNotification.getT1(),mContext));
        holder.setProfilePic(currentReplyNotification.getReplyImage());
        holder.notification_information.setText(MessageFormat.format("{0} {1}", currentReplyNotification.getReplierName(), mContext.getString(R.string.replied)));

        holder.itemView.setOnClickListener(v -> {
            if(notificationSelectedListener != null){
                notificationSelectedListener.onNotificationSelected(position, currentReplyNotification);
            }
        });

    }

    @Override
    public int getItemCount() {
        return replyNotificationList.size();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {

        CircleImageView notification_profile;
        EmojiTextView notification_information;
        TextView timestamp;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            notification_information = itemView.findViewById(R.id.notification_information);
            notification_profile = itemView.findViewById(R.id.notification_profile);
            timestamp = itemView.findViewById(R.id.timestamp);

        }

        public void setProfilePic(String thumbnail){

            notification_profile = itemView.findViewById(R.id.notification_profile);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(notification_profile);

        }
    }

    public interface NotificationSelectedListener {
        void onNotificationSelected(int position, ReplyNotification replyNotification);
    }

    private static String getTimeAgo(long date, Context context) {
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
