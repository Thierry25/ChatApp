package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.ReconnectAdapter;
import marcelin.thierry.chatapp.classes.Channel;

public class ChannelReconnectActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Toolbar mSettingsBar;
    private RecyclerView mFoundChannelList;

    private ReconnectAdapter mReconnectAdapter;
    private List<Channel> mChannelToTakeInfo = new ArrayList<>();

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance().getReference();
    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");

    private ProgressDialog mSearchDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_search);

        mSettingsBar = findViewById(R.id.settings_bar);
        mFoundChannelList = findViewById(R.id.foundChannelList);

        mReconnectAdapter = new ReconnectAdapter(mChannelToTakeInfo, this);

        setSupportActionBar(mSettingsBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.chan_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFoundChannelList.setHasFixedSize(true);
        mFoundChannelList.setLayoutManager(new LinearLayoutManager(this));
        mFoundChannelList.setAdapter(mReconnectAdapter);

        mSearchDialog = new ProgressDialog(this);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        mChannelToTakeInfo.clear();

        mSearchDialog.setTitle("Searching Channel...");
        mSearchDialog.setMessage("Please wait, searching for channel");
        mSearchDialog.setCanceledOnTouchOutside(false);
        mSearchDialog.show();

        final String[] dbVal = {""};
        final String[] userEntry = {""};


        //  List<String> l = new ArrayList<>();
        mRootReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("public_channel")){

                    DatabaseReference channelRef = mRootReference.child("public_channel");
                    channelRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                            if(m == null){
                                return;
                            }

                            // l.add(m.keySet());

                            for(String s : m.keySet()){
                                dbVal[0] = s.toLowerCase();
                                userEntry[0] = query.toLowerCase();

                                if(dbVal[0].contains(userEntry[0])){

                                    mChannelReference.child(s)
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshoti) {

                                                    Channel c = dataSnapshoti.getValue(Channel.class);
                                                    if (c != null) {
                                                        Log.i("Follow", c.getName());

                                                        mChannelToTakeInfo.add(c);
                                                        mReconnectAdapter.notifyDataSetChanged();
                                                        mSearchDialog.dismiss();

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                }else{
                                    mSearchDialog.dismiss();
                                    Toast.makeText(ChannelReconnectActivity.this
                                            , getString(R.string.chan_er_) + " "
                                                    + userEntry[0]
                                            , Toast.LENGTH_SHORT).show();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else{
                    Toast.makeText(ChannelReconnectActivity.this
                            , R.string.no_pub_chan
                            , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
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
