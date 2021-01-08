package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
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

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.BlockedUsersAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class BlockedUsersActivity extends AppCompatActivity {

    private Toolbar mBlockedBar;
    private TextView mNoUserBlocked;
    private RecyclerView mBlockedList;
    private List<Users> mBlockedUsers = new ArrayList<>();
    private BlockedUsersAdapter mBlockedAdapter;

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_blocked_users);
        TextView title = findViewById(R.id.title);
        ImageView backButton = findViewById(R.id.backButton);
        CircleImageView profileImage = findViewById(R.id.profileImage);
        profileImage.setVisibility(View.GONE);

        title.setTextSize(20);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.capriola);
        title.setTypeface(typeface);
        title.setText(R.string.blocked_users);

        backButton.setOnClickListener(v->{
            finish();
        });

        mBlockedBar = findViewById(R.id.blockedBar);
        setSupportActionBar(mBlockedBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.blocked_users);

        mBlockedAdapter = new BlockedUsersAdapter(mBlockedUsers, this);
        mBlockedList = findViewById(R.id.blockedList);
        mBlockedList.setHasFixedSize(true);
        mBlockedList.setLayoutManager(new LinearLayoutManager(this));
        mBlockedList.setAdapter(mBlockedAdapter);

        mNoUserBlocked = findViewById(R.id.noUserBlocked);

        loadBlockedUsers();
    }

    private void loadBlockedUsers() {

        String mCurrentPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        mUsersReference.child(mCurrentPhoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("blocked")){

                    mUsersReference.child(mCurrentPhoneNumber).child("blocked").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                            if(m == null){
                                return;
                            }

                            for(String s : m.keySet()){
                                mUsersReference.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Users u = dataSnapshot.getValue(Users.class);
                                        if(u == null){
                                            return;
                                        }

                                        String nameStored = Users.getLocalContactList().get(s);
                                        nameStored = nameStored != null && nameStored.length() > 0  ? nameStored :
                                               s;
                                        u.setNameStoredInPhone(nameStored);
                                        u.setPhoneNumber(s);

                                        mBlockedUsers.add(u);
                                        mBlockedAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                    mNoUserBlocked.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
