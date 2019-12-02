package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import java.util.Locale;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.SharedChannelAdapter;

public class ChannelSharedActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private SharedChannelAdapter mPagerAdapter;
    private TabLayout mTabLayout;

    private String mChatId;
    private String mChatName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_shared);

        mToolbar = findViewById(R.id.main_toolbar_layout);
        setSupportActionBar(mToolbar);
        mChatName = getIntent().getStringExtra("chat_name");
            Objects.requireNonNull(getSupportActionBar()).setTitle(mChatName);
        mViewPager = findViewById(R.id.tabPager);
        mPagerAdapter = new SharedChannelAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);

        mTabLayout = findViewById(R.id.main_tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);

        mChatId = getIntent().getStringExtra("chat_id");
        //  Toast.makeText(this, mChatId, Toast.LENGTH_SHORT).show();

//        Bundle bundle = new Bundle();
//        bundle.putString("chat_id", mChatId);
//
//        ImageFragment imF = new ImageFragment();
//        imF.setArguments(bundle);
    }

    public Bundle getMyData() {
        Bundle hm = new Bundle();
        hm.putString("chat_id", mChatId);
        return hm;
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
