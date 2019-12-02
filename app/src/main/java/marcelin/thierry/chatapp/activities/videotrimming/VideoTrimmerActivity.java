package marcelin.thierry.chatapp.activities.videotrimming;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.deep.videotrimmer.DeepVideoTrimmer;
import com.deep.videotrimmer.interfaces.OnTrimVideoListener;
import com.deep.videotrimmer.view.RangeSeekBarView;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.databinding.ActivityVideoTrimmerBinding;

import static marcelin.thierry.chatapp.activities.videotrimming.Constants.EXTRA_VIDEO_PATH;


public class VideoTrimmerActivity extends BaseActivity implements OnTrimVideoListener {
    ActivityVideoTrimmerBinding mBinder;
    private DeepVideoTrimmer mVideoTrimmer;
    TextView textSize, tvCroppingMessage;
    RangeSeekBarView timeLineBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinder = DataBindingUtil.setContentView(this, R.layout.activity_video_trimmer);

        Intent extraIntent = getIntent();
        String path = "";

        if (extraIntent != null) {
            path = extraIntent.getStringExtra(EXTRA_VIDEO_PATH);
        }

        mVideoTrimmer = findViewById(R.id.timeLine);
        timeLineBar = findViewById(R.id.timeLineBar);
        textSize =  findViewById(R.id.textSize);
        tvCroppingMessage =  findViewById(R.id.tvCroppingMessage);

        if (mVideoTrimmer != null && path != null) {
            mVideoTrimmer.setMaxDuration(30);
            mVideoTrimmer.setOnTrimVideoListener(this);
            mVideoTrimmer.setVideoURI(Uri.parse(path));
        } else {
            showToastLong(getString(R.string.toast_cannot_retrieve_selected_video));
        }
    }

    @Override
    public void getResult(final Uri uri) {
        runOnUiThread(() -> tvCroppingMessage.setVisibility(View.GONE));
        Constants.croppedVideoURI = uri.toString();
        Intent intent = new Intent();
        intent.setData(uri);
        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void cancelAction() {
        mVideoTrimmer.destroy();
        runOnUiThread(() -> tvCroppingMessage.setVisibility(View.GONE));
        finish();
    }
}
