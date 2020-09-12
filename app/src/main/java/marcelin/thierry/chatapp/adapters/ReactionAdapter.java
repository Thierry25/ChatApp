package marcelin.thierry.chatapp.adapters;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Messages;

public class ReactionAdapter extends RecyclerView.Adapter<ReactionAdapter.MessageViewHolder> {

    private List<Messages> messagesList;
    private FirebaseAuth mAuth;

    public ReactionAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ReactionAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        switch (viewType){
            case 0:
                View view = layoutInflater.inflate(R.layout.react_lay, parent, false);
                return new MessageViewHolder(view);

            case 1:
                View view1 = layoutInflater.inflate(R.layout.react_lay_1, parent, false);
                return new MessageViewHolder(view1);

            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ReactionAdapter.MessageViewHolder holder, int position) {
        Messages mCurrentMessage = messagesList.get(position);
        holder.messageTextLayout.setText(mCurrentMessage.getContent());
        holder.messageTimeLayout.setText(getDate(mCurrentMessage.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        EmojiTextView messageTextLayout;
        TextView messageTimeLayout;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageTextLayout = itemView.findViewById(R.id.message_text_layout);
            messageTimeLayout = itemView.findViewById(R.id.message_time_layout);
        }
    }

    @Override
    public int getItemViewType(int position) {
        mAuth = FirebaseAuth.getInstance();
        String from = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();
        final Messages m = messagesList.get(position);
        String from_user = m.getFrom();

        if(Objects.requireNonNull(from).equals(from_user)){
            return 0;
        }else {
            return 1;
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
}
