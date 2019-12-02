package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.TouchImageView;

public class FullScreenImageActivity extends AppCompatActivity {

    private TouchImageView mImageFromChat;
    // private Toolbar mToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_full_screen_image);

        String image_shown = getIntent().getStringExtra("image_shown");
        // TODO: To be fixed
        //   String name = getNameToShow.mChatName;

        mImageFromChat = findViewById(R.id.imageFromChat);

        Picasso.get().load(image_shown)
                .placeholder(R.drawable.ic_avatar)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(mImageFromChat);


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
