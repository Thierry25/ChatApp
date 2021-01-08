package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.UserSelectionAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class PrivacyActivity extends AppCompatActivity {

    private UserSelectionAdapter mUserSelectionAdapter;
    private List<Users> mUsersList = new ArrayList<>();
    private Map<String, Object> mSelectedPhoneNumbers = new HashMap<>();

    private FirebaseAuth mAuth;
    private static final DatabaseReference fDB = FirebaseDatabase
            .getInstance().getReference().child("ads_users");

    private static final DatabaseReference mStatusReference = FirebaseDatabase
            .getInstance().getReference().child("ads_status");

    private String phoneNumber;

    private TextView title;
    private ImageView backButton;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_user_selection);

        Toolbar mUserSelectionBar = findViewById(R.id.userSelectionBar);
        setSupportActionBar(mUserSelectionBar);

        profileImage = findViewById(R.id.profileImage);
        title = findViewById(R.id.title);
        title.setTextSize(32);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.allura);
        title.setTypeface(typeface);
        title.setText(R.string.except);
        profileImage.setVisibility(View.GONE);

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.except);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserSelectionAdapter = new UserSelectionAdapter(mUsersList);

        RecyclerView mRecyclerUsersList = findViewById(R.id.usersList);
        mRecyclerUsersList.setHasFixedSize(true);
        mRecyclerUsersList.setItemAnimator(new DefaultItemAnimator());
        mRecyclerUsersList.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerUsersList.setAdapter(mUserSelectionAdapter);

        mAuth = FirebaseAuth.getInstance();
        phoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        FloatingActionButton mDoneButton = findViewById(R.id.doneButton);
        mDoneButton.setOnClickListener(view -> {
            mSelectedPhoneNumbers.clear();
            for(Users u : mUsersList){
                if(u.isSelected()){
                    mSelectedPhoneNumbers.put(u.getPhoneNumber(), true);
                    mUserSelectionAdapter.notifyDataSetChanged();
                  //  Toast.makeText(PrivacyActivity.this, mSelectedPhoneNumbers.toString(), Toast.LENGTH_SHORT).show();
                }
            }

//            if(mSelectedPhoneNumbers.size() == 0){
//                Toast.makeText(this, "Skkrr", Toast.LENGTH_SHORT).show();
//            }else{
                if (phoneNumber != null) {
//                    mExceptReference.child(phoneNumber).updateChildren(mSelectedPhoneNumbers);
                    mStatusReference.child(phoneNumber).child("e").setValue(mSelectedPhoneNumbers);
                }
            //}
            finish();
        });

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        getContacts();

        if (phoneNumber != null) {
            mStatusReference.child(phoneNumber).child("e").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(Users u : mUsersList){
                        if(dataSnapshot.hasChild(u.getPhoneNumber())){
                            u.setSelected(true);
                            mUserSelectionAdapter.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
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

    private void getContacts() {
        mUsersList.clear();
        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        if (Users.getLocalContactList() == null || Users.getLocalContactList().isEmpty()) {
            //TODO: try to see if thread successfully loaded the users
            Log.i("ContactList", "user list is empty");
            return;
        }

        Set<Map.Entry<String, String>> myList = Users.getLocalContactList().entrySet();
        for (Map.Entry<String, String> val : myList) {

            String phoneNumber = val.getKey();
            String localContactName = val.getValue();


            fDB.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Users u = dataSnapshot.getValue(Users.class);
                    if (u == null) {
                        Log.i("FirebaseEmpty", "object is null");
                        return;
                    }
                    Log.i("FirebaseList", "data found. user uid: " + u.getUid());

                    u.setNameStoredInPhone(localContactName);
                    u.setPhoneNumber(phoneNumber);
                    mUsersList.add(u);

                    if(u.getPhoneNumber().equals(phone)){
                        mUsersList.remove(u);
                    }

                    mUserSelectionAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }

}
