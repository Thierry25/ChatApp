package marcelin.thierry.chatapp.adapters;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;

import java.util.List;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.FullScreenImageActivity;
import marcelin.thierry.chatapp.classes.Messages;
import marcelin.thierry.chatapp.classes.TouchImageView;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private List<Messages> messagesList;

    public ImagesAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_layout, parent,
                false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        Messages currentMessage = messagesList.get(position);

        Picasso.get().load(currentMessage.getContent()).placeholder(R.drawable.ic_avatar)
                .into(holder.mImageFromChat);

        holder.mImageFromChat.setOnClickListener(view -> {

            Intent goToProfilePicture = new Intent(view.getContext(), FullScreenImageActivity.class);
            goToProfilePicture.putExtra("image_shown", currentMessage.getContent());
            view.getContext().startActivity(goToProfilePicture);

        });

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private TouchImageView mImageFromChat;

        public ImageViewHolder(View itemView) {
            super(itemView);
            mImageFromChat = itemView.findViewById(R.id.image_from_chat);

        }
    }
}
