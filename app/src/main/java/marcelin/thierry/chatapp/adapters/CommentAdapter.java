package marcelin.thierry.chatapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vanniktech.emoji.EmojiTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Messages;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private List<Messages> messagesList;

    public CommentAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
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


    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        CircleImageView profilePic;
        EmojiTextView commentName, comment, seeReplies, reply;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePic = itemView.findViewById(R.id.profilePic);
            commentName = itemView.findViewById(R.id.commentName);
            comment = itemView.findViewById(R.id.comment);
            seeReplies = itemView.findViewById(R.id.seeReplies);
            reply = itemView.findViewById(R.id.reply);

        }
    }
}
