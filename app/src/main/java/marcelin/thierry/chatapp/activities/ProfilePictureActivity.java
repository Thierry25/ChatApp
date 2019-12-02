package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import java.util.Locale;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.TouchImageView;

public class ProfilePictureActivity extends AppCompatActivity {

    private TouchImageView mProfilePicture;
    private Toolbar mProfileBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_profile_picture);

        mProfileBar = findViewById(R.id.profile_bar);
        mProfilePicture = findViewById(R.id.profileThumbnail);

        String pictureFromIntent = getIntent().getStringExtra("user_picture");
        String nameFromIntent = getIntent().getStringExtra("user_name");

        setSupportActionBar(mProfileBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(nameFromIntent);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Picasso.get().load(pictureFromIntent).placeholder(R.drawable.ic_avatar).into(mProfilePicture);
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
