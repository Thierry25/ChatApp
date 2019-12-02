package com.islamassem.voicemessager;

import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.Formatter;
import java.util.Locale;

import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.islamassem.voicemessager.TimeType.Second;

public class VoiceMessagerFragment extends Fragment {
    public static final int RequestPermissionCode = 1001;
    private final static String TAG = "test";
    TextView recordBtn;
    TextView time;
    TextView play_time;
    ImageView recordBtnImage, play, cancel, send;
    SeekBar seekbar;
    ImageView recordingIndicator;
    boolean autoRecord, isPlaying, isRecording;
    TimerCounter timerCounter;
    OnControllerClick onControllerClick;
    Recorder recorder;
    Handler handler;
    private String voiceFile = "tawseel.mp3";
    private SimpleExoPlayer exoPlayer;
    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, int reason) {
            Log.i(TAG, "onTimelineChanged");
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG, "onLoadingChanged");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, "onPlayerStateChanged: playWhenReady = " + String.valueOf(playWhenReady)
                    + " playbackState = " + playbackState);
            switch (playbackState) {
                case ExoPlayer.STATE_ENDED:
                    Log.i(TAG, "Playback ended!");
                    //Stop playback and return to start position
                    setPlayPause(false);
                    exoPlayer.seekTo(0);
                    setProgresssEnded();
                    break;
                case ExoPlayer.STATE_READY:
                    Log.i(TAG, "ExoPlayer ready! pos: " + exoPlayer.getCurrentPosition() + " max: " + stringForTime((int) exoPlayer.getDuration()));
                    setProgresss();
                    break;
                case ExoPlayer.STATE_BUFFERING:
                    Log.i(TAG, "Playback buffering!");
                    break;
                case ExoPlayer.STATE_IDLE:
                    Log.i(TAG, "ExoPlayer idle!");
                    setProgresss();
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, "onPlaybackError: " + error.getMessage());
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.i(TAG, "onPositionDiscontinuity");
        }
    };
    View.OnClickListener onDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            record();
        }
    };
    View.OnClickListener onRecordBtn = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            record();
        }
    };

    public static VoiceMessagerFragment build(OnControllerClick onControllerClick, boolean autoRecord) {
        VoiceMessagerFragment voiceMessagerFragment = new VoiceMessagerFragment();
        voiceMessagerFragment.onControllerClick = onControllerClick;
        voiceMessagerFragment.autoRecord = autoRecord;
        return voiceMessagerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.voice_message_fragment, container, false);
        recordBtn = root.findViewById(R.id.start_recording);
        time = root.findViewById(R.id.time);
        send = root.findViewById(R.id.send);
        play_time = root.findViewById(R.id.play_time);
        cancel = root.findViewById(R.id.cancel);
        recordBtnImage = root.findViewById(R.id.start_recording_image);
        play = root.findViewById(R.id.play);
        seekbar = root.findViewById(R.id.seekbar);
        recordingIndicator = root.findViewById(R.id.recording_indicator);
        recordBtnImage.setOnClickListener(onDoneClickListener);
        recordBtn.setOnClickListener(onRecordBtn);
        recordBtn.setOnClickListener(onRecordBtn);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onControllerClick != null)
                    onControllerClick.onSendClick(file());
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onControllerClick != null)
                    onControllerClick.onCloseClick();
            }
        });
        Handler handler = new Handler();
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initPlayButtonn();
            }
        });
        timerCounter = new TimerCounter(new TimerCounter.OnTimerTick() {

            @Override
            public void onTimerTick(String time) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String t = "";
                        Integer i = Integer.valueOf(time);
                        if (i < 10)
                            t = t.concat("00:0" + time);
                        else if (i < 60)

                            t = t.concat("00:" + time);
                        else {
                            int m = i / 60;
                            if (m < 10)
                                t = t.concat("0" + m);
                            else t = t.concat("" + m);
                            i = i % 60;
                            if (i < 10)
                                t = t.concat(":0" + i);
                            else t = t.concat(":" + i);
                        }
                        VoiceMessagerFragment.this.time.setText(t);
                    }
                });
            }
        }, Second);
        if (!autoRecord)
            recordingIndicator.setSelected(false);
        else {
            recordingIndicator.setSelected(false);
            record();
        }
        return root;
    }
//called to stop recording
    public void stopRecord() {
        try {
            try {
                isPlaying = false;
                play.setVisibility(VISIBLE);
                seekbar.setVisibility(VISIBLE);
                play_time.setVisibility(VISIBLE);
                play_time.setText(time.getText());
                send.setVisibility(VISIBLE);
                seekbar.setProgress(0);
                recorder.stopRecording();
                play.setImageResource(android.R.drawable.ic_media_play);
                cancel.setVisibility(VISIBLE);
                recordBtn.setVisibility(GONE);
                recordBtnImage.setVisibility(GONE);
                recordingIndicator.setVisibility(GONE);
                time.setVisibility(GONE);
                timerCounter.cancel();
                isRecording = false;
                recorder = null;
                if (onControllerClick != null)
                    onControllerClick.onFinishRecording(file());
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        recordBtnImage.post(new Runnable() {
            @Override
            public void run() {
                animateVoice(0);
            }
        });
        prepareExoPlayerFromFileUri(Uri.fromFile(file()));
    }

    //animate recording imageview
    private void animateVoice(final float maxPeak) {
        recordBtnImage.animate().scaleX(1 + maxPeak).scaleY(1 + maxPeak).setDuration(10).start();
    }

    //called to start recording if not recording otherwise stop recording
    public void record() {
        if (checkPermission()) {
            if (isRecording) {
                recordingIndicator.setSelected(false);
                stopRecord();
                return;
            }
            try {

                File f = file();
                if (f.exists())
                    f.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            recordingIndicator.setSelected(true);
            isPlaying = false;
            setupRecorder();
            //recordButton.setVisibility(View.GONE);
            play.setVisibility(GONE);
            send.setVisibility(GONE);
            seekbar.setVisibility(GONE);
            play_time.setVisibility(GONE);
            cancel.setVisibility(GONE);
            recordBtnImage.setImageResource(R.drawable.ic_record_icon);
            recordBtn.setVisibility(VISIBLE);
            recordBtn.setText("Cancel");
            recordingIndicator.setVisibility(VISIBLE);
            recordBtnImage.setVisibility(VISIBLE);
            time.setVisibility(VISIBLE);
            timerCounter.startTimer();
            isRecording = true;
            recorder.startRecording();
            if (onControllerClick != null)
                onControllerClick.onRecordClick();
            // remove.setVisibility(VISIBLE);

        } else {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new
                String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    private void setupRecorder() {
        recorder = OmRecorder.wav(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                        animateVoice((float) (audioChunk.maxAmplitude() / 200.0));
                    }
                }), file());
    }

    private File file() {
        return new File(getActivity().getCacheDir(), "voice message.mp3");
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 44100
                )
        );
    }

    public boolean checkPermission() {
        int result1 = ContextCompat.checkSelfPermission(getContext(),
                RECORD_AUDIO);
        return
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean StoragePermission = grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] ==
                            PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        record();
                    } else {
                        Toast.makeText(getContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    /**
     * Prepares exoplayer for audio playback from a local file
     *
     * @param uri
     */
    private void prepareExoPlayerFromFileUri(Uri uri) {
        exoPlayer = ExoPlayerFactory.newSimpleInstance(getActivity(),
                new DefaultTrackSelector(), new DefaultLoadControl());
        exoPlayer.addListener(eventListener);

        DataSpec dataSpec = new DataSpec(uri);
        final FileDataSource fileDataSource = new FileDataSource();
        try {
            fileDataSource.open(dataSpec);
        } catch (FileDataSource.FileDataSourceException e) {
            e.printStackTrace();
        }

        DataSource.Factory factory = new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                return fileDataSource;
            }
        };
        MediaSource audioSource = new ExtractorMediaSource(fileDataSource.getUri(),
                factory, new DefaultExtractorsFactory(), null, null);

        exoPlayer.prepare(audioSource);
        seekbar.requestFocus();
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    // We're not interested in programmatically generated changes to
                    // the progress bar's position.
                    return;
                }
                exoPlayer.seekTo(progress * 1000);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbar.setMax(0);
        seekbar.setMax((int) exoPlayer.getDuration() / 1000);
    }
    public void initPlayButtonn() {
        play.requestFocus();
        setPlayPause(!isPlaying);

    }

    /**
     * Starts or stops playback. Also takes care of the Play/Pause button toggling
     *
     * @param playy True if playback should be started
     */
    private void setPlayPause(boolean playy) {
        isPlaying = playy;
        exoPlayer.setPlayWhenReady(playy);
        if (!isPlaying) {
            play.setImageResource(android.R.drawable.ic_media_play);
        } else {
            setProgresss();
            play.setImageResource(android.R.drawable.ic_media_pause);
        }
    }

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder;
        Formatter mFormatter;
        mFormatBuilder = new StringBuilder();
        mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    //called to update seekbar and time text
    private void setProgresss() {
        if (handler == null) handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (exoPlayer != null && isPlaying) {
                    seekbar.setMax((int) exoPlayer.getDuration() / 1000);
                    int mCurrentPosition = (int) exoPlayer.getCurrentPosition() / 1000;
                    seekbar.setProgress(mCurrentPosition);
                    play_time.setText(stringForTime((int) exoPlayer.getCurrentPosition()));
                    handler.postDelayed(this, 1000);
                }
            }
        });
    }

    //called to update seekbar and time text after play ended
    private void setProgresssEnded() {
        seekbar.setMax((int) exoPlayer.getDuration() / 1000);
        Log.e("testSeek", "setProgresssEnded");
        if (handler == null) handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                {
                    seekbar.setMax((int) exoPlayer.getDuration() / 1000);
                    seekbar.setProgress(0);
                    play_time.setText(stringForTime(seekbar.getMax()));
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            exoPlayer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * interface used for interaction between fragment and hosting activity
     * */
    public interface OnControllerClick {
        //called when user click record button
        void onRecordClick();

        //called when user stop recording
        void onFinishRecording(File file);

        //called when user click send button
        void onSendClick(File file);

        //called when user want to close message fragment ,you should be removing the fragment here
        void onCloseClick();
    }
}
