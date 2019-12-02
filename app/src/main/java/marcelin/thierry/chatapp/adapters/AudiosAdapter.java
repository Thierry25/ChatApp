package marcelin.thierry.chatapp.adapters;

import android.media.AudioManager;
import android.media.MediaPlayer;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Messages;

public class AudiosAdapter extends RecyclerView.Adapter<AudiosAdapter.AudioViewHolder> {

    private List<Messages> messagesList;
    private static List<MediaPlayer> mediaPlayers = new ArrayList<>();

    public AudiosAdapter(List<Messages> messagesList) {
        this.messagesList = messagesList;
        mediaPlayers.clear();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.audio_layout, parent,
                false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {

        Messages message = messagesList.get(position);

        MediaPlayer mediaPlayer = new MediaPlayer();
        final boolean[] isReady = {false};
        final int[] duration = {0};
        mediaPlayers.add(mediaPlayer);


        try {
            Log.i("((MediaPlayer))", "MediaPlayer called");
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(message.getContent());
            mediaPlayer.setOnPreparedListener(mediaPlayer12 -> {
                isReady[0] = true;
                duration[0] = mediaPlayer12.getDuration();
                Log.i("((MediaPlayer))", "Duration of message: " + duration[0]);

                String time = String.format("%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(duration[0]),
                        TimeUnit.MILLISECONDS.toSeconds(duration[0]) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration[0]))
                );

                holder.audioTime.setText(time);

                holder.audioSeekbar.setMax(duration[0]);

                holder.timestamp.setText(getDate(message.getTimestamp()));

                ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                service.scheduleWithFixedDelay(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        holder.audioSeekbar.setProgress(mediaPlayer12.getCurrentPosition());
                    }
                }, 1, 1, TimeUnit.MICROSECONDS);

                // ((AudioViewHolder) holder).seekBarAudio.setProgress(mediaPlayer12.getCurrentPosition());
            });

            mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
                mediaPlayer1.pause();
                holder.playAudio.setImageResource(R.drawable.ic_play_copy);

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

            holder.playAudio.setOnClickListener((View view) -> {
                Log.i("((MediaPlayer))", "Play Button clicked");
                if (isReady[0]) {
                    if (mediaPlayer.isPlaying()) {
                        holder.playAudio.setImageResource(R.drawable.ic_play_copy);
                        mediaPlayer.pause();
                    } else {
                        holder.playAudio.setImageResource(R.drawable.ic_pause_copy);
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

            holder.audioSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean input) {

                    if (input) {
                        mediaPlayer.seekTo(progress);
                    }

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            Log.e("MediaPlayerException", e.getMessage());
            e.printStackTrace();
        }



    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder {

        private ImageButton playAudio;
        private SeekBar audioSeekbar;
        private TextView audioTime;
        private TextView timestamp;

        public AudioViewHolder(View itemView) {
            super(itemView);

            playAudio = itemView.findViewById(R.id.play_audio);
            audioSeekbar = itemView.findViewById(R.id.audio_seekbar);
            audioTime = itemView.findViewById(R.id.audio_time);
            timestamp = itemView.findViewById(R.id.timestamp);

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

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(time);
            return DateFormat.format("MMM dd, HH:mm", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
