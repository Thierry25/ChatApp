package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.github.rtoshiro.view.video.FullscreenVideoLayout;

import java.io.IOException;
import java.util.Locale;

import marcelin.thierry.chatapp.R;

public class VideoPlayerActivity extends AppCompatActivity {

    private String mVideoPath;
    private String mTimestamp;

    private TextView mTime;
    private FullscreenVideoLayout mVideoLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_video_player);

        mVideoPath = getIntent().getStringExtra("video");
       // mTimestamp = getIntent().getStringExtra("time");

        mTime = findViewById(R.id.timestamp);
      //  mTime.setText("Received on " + mTimestamp);

        mVideoLayout = findViewById(R.id.videoview);
        mVideoLayout.setActivity(this);

        Uri videoUri = Uri.parse(mVideoPath);
        try {
            mVideoLayout.setVideoURI(videoUri);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    // Load Language saved in shared preferences
    public void loadLocale(){
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }

}
