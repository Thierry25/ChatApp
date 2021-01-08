package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.ChatAdapter;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Messages;

public class SubscriptionActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private Toolbar mSubsToolbar;
    private RecyclerView mSubsList;

    private TextView mTextToSee;

    private static final DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");

    private static final DatabaseReference mChannelMessageReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel_messages");

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private List<Conversation> mSubscriptionList = new ArrayList<>();
    private ChatAdapter mSubsAdapter;

    private int z;

    private TextView title;
    private ImageView backButton;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_subscription);

        mSubsToolbar = findViewById(R.id.subs_bar);

        setSupportActionBar(mSubsToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.your_subs);
       // getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSubsAdapter = new ChatAdapter(mSubscriptionList, this);

        mSubsList = findViewById(R.id.subs_list);
        mSubsList.setHasFixedSize(true);
        mSubsList.setLayoutManager(new LinearLayoutManager(this));
        mSubsList.setAdapter(mSubsAdapter);

        mTextToSee = findViewById(R.id.textToSee);


        title = findViewById(R.id.title);
        profileImage = findViewById(R.id.profileImage);

        title.setTextSize(32);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.allura);
        title.setTypeface(typeface);
        title.setText(R.string.your_subs);
        profileImage.setVisibility(View.GONE);

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

        getMySubscriptions();
    }

    private void getMySubscriptions() {
        mSubscriptionList.clear();

        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        mUserReference.child(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("conversation")) {
                    mUserReference.child(phone).child("conversation").addChildEventListener(
                            new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    Conversation c = dataSnapshot.getValue(Conversation.class);
                                    if (c == null) {
                                        return;
                                    }

                                    if (c.getType().equals("channel")) {
//                                        c.setName(c.getId());
//                                        mSubscriptionList.add(c);
//                                        mSubsAdapter.notifyDataSetChanged();

                                        mChannelReference.child(c.getId()).addValueEventListener
                                                (new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Channel chan = dataSnapshot.getValue(Channel.class);
                                                        if (chan != null) {
                                                            c.setProfile_image(chan.getThumbnail());
                                                            if (chan.getNewName().equals("")) {
                                                                c.setName(chan.getName());
                                                            } else {
                                                                c.setName(chan.getNewName());
                                                            }
                                                        }

                                                        if (chan != null) {
                                                            if (!chan.getLastMessage().equals("Default")) {
                                                                mChannelMessageReference.child(chan.getLastMessage())
                                                                        .addValueEventListener(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Messages m = dataSnapshot.getValue(Messages.class);
                                                                                if (m != null) {
                                                                                    switch (m.getType()) {
                                                                                        case "text":
                                                                                            String content = m.getContent();
                                                                                            if (content.length() > 25) {
                                                                                                content = content
                                                                                                        .substring(0, 21) + "...";
                                                                                            }
                                                                                            c.setLastMessage(content);
                                                                                            c.setFrom(m.getFrom());
                                                                                            Log.i("BARCA", m.getFrom());
                                                                                            break;

                                                                                        case "audio":
                                                                                            content = "<font color=\"#FFA500\"<b>" +
                                                                                                    "Audio</b></font>";
                                                                                            //content = "Audio";
                                                                                            c.setLastMessage(content);
                                                                                            c.setFrom(m.getFrom());
                                                                                            break;

                                                                                        case "image":
                                                                                            content = "<font color=\"#7016a8\"<b>" +
                                                                                                    "Image</b></font>";
                                                                                            // content = "Image";
                                                                                            c.setLastMessage(content);
                                                                                            c.setFrom(m.getFrom());
                                                                                            break;

                                                                                        case "video":
                                                                                            content = "<font color=\"#0929b1\"<b>" +
                                                                                                    "Video</b></font>";
                                                                                            //content = "Video";
                                                                                            c.setLastMessage(content);
                                                                                            c.setFrom(m.getFrom());
                                                                                            break;

                                                                                        case "document":
                                                                                            content = "<font color=\"#dabf0f\"<b>" +
                                                                                                    "Document</b></font>";
                                                                                            //content = "Document";
                                                                                            c.setLastMessage(content);
                                                                                            c.setFrom(m.getFrom());
                                                                                            break;

                                                                                        case "channel_link":
                                                                                            content = "<font color=\"#018c06\"<b>" +
                                                                                                    "Channel invitation</b></font>";
                                                                                            // content = "Channel invitation";
                                                                                            if (!m.isVisible()) {
                                                                                                c.setLastMessage("Message Deleted");
                                                                                                c.setFrom(m.getFrom());
                                                                                            } else {
                                                                                                c.setLastMessage(content);
                                                                                                c.setFrom(m.getFrom());

                                                                                            }

                                                                                            break;

                                                                                        case "group_link":
                                                                                            content = "<font color=\"#018c06\"<b>" +
                                                                                                    "Group invitation</b></font>";
                                                                                            //   content ="Group Invitation";

                                                                                            if (!m.isVisible()) {
                                                                                                c.setLastMessage("Message Deleted");
                                                                                                c.setFrom(m.getFrom());

                                                                                            } else {
                                                                                                c.setLastMessage(content);
                                                                                                c.setFrom(m.getFrom());

                                                                                            }
                                                                                            break;

                                                                                        default:
                                                                                            return;

                                                                                    }
                                                                                    c.setMessageTimestamp(m.getTimestamp());

                                                                                    if (!m.getFrom().equals(phone)) {

                                                                                        if (!m.getRead_by().containsKey(phone)) {
                                                                                            z += 1;
                                                                                            c.setUnreadMessages(z / 2);
                                                                                        } else {
                                                                                            z = 0;
                                                                                            c.setUnreadMessages(z);
                                                                                        }
                                                                                    }

                                                                                    mSubscriptionList.remove(c);

                                                                                    // mConvoList.add(c);
                                                                                    mSubscriptionList.add(c);
                                                                                    mSubsAdapter.notifyDataSetChanged();
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                            } else {
                                                                c.setProfile_image(chan.getThumbnail());
                                                                c.setName(chan.getName());
                                                                c.setLastMessage(getString(R.string.no_msg_yet));

                                                                c.setFrom("#00000000");

                                                                mSubscriptionList.remove(c);
                                                                c.setMessageTimestamp(c.getTimestamp());
                                                                mSubscriptionList.add(c);

                                                                mSubsAdapter.notifyDataSetChanged();
                                                            }
                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                    }
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            }
                    );
                } else {
                    mTextToSee.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.ic_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userEntry = newText.toLowerCase();
        List<Conversation> newConvo = new ArrayList<>();

        for (Conversation c : mSubscriptionList) {
            if (c.getType().equals("channel")) {
                if (c.getName().toLowerCase().contains(userEntry)) {
                    newConvo.add(c);
                }
            }
        }

        mSubsAdapter.updateList(newConvo);
        return true;
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
