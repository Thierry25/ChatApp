package marcelin.thierry.chatapp.adapters;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import java.util.List;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.VideoPlayerActivity;
import marcelin.thierry.chatapp.classes.Messages;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {

    private List<Messages> mMessagesList;

    public VideosAdapter(List<Messages> mMessagesList) {
        this.mMessagesList = mMessagesList;
    }

    @NonNull
    @Override
    public VideosAdapter.VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_layout, parent,
                false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideosAdapter.VideoViewHolder holder, int position) {

        Messages m = mMessagesList.get(position);

        holder.cstLayout.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), VideoPlayerActivity.class);
            intent.putExtra("video", m.getContent());
            view.getContext().startActivity(intent);
        });

        holder.playButton.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), VideoPlayerActivity.class);
            i.putExtra("video", m.getContent());
            view.getContext().startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout cstLayout;
        private VideoView mainVideoView;
        private ImageView playButton;

        public VideoViewHolder(View itemView) {
            super(itemView);

            cstLayout = itemView.findViewById(R.id.cstLayout);
            mainVideoView = itemView.findViewById(R.id.mainVideo);
            playButton = itemView.findViewById(R.id.play_button);
        }
    }
}
