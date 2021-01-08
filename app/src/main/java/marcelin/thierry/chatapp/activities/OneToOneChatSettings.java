package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.utils.CheckInternet_;


public class OneToOneChatSettings extends AppCompatActivity {

    private CircleImageView mProfileImage;

    private String mUserPhone;
    private String mUserName;
    private String mChatId;
    private ProgressBar mImageProgress;
    private TextView mBlockedText;

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final DatabaseReference mChatReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_chat");

    private static final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_messages");

    private String mCurrentPhone;
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_one_to_one_chat_settings);

        mUserPhone = getIntent().getStringExtra("user_phone");
        mUserName = getIntent().getStringExtra("user_name");
       // String mUserPicture = getIntent().getStringExtra("user_picture");
        mChatId = getIntent().getStringExtra("chat_id");
        mCurrentPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        MaterialCardView mSharedItems = findViewById(R.id.sharedItems);
        MaterialCardView mBlocked = findViewById(R.id.blocked);
        MaterialCardView mDeleteConversation = findViewById(R.id.deleteConversation);
        mBlockedText = findViewById(R.id.blockText);

        TextView name = findViewById(R.id.name);
        name.setText(mUserName);
        TextView phone_number = findViewById(R.id.phone_number);
        phone_number.setText(mUserPhone);

        mImageProgress = findViewById(R.id.imageProgress);

        mProfileImage = findViewById(R.id.user_profile_image);

        mUsersReference.keepSynced(true);

        mUsersReference.child(mUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_avatar).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        mImageProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileImage.setOnClickListener(view -> getImage(image -> {
            Intent goToFullScreen = new Intent(OneToOneChatSettings.this,
                    ProfilePictureActivity.class);
            goToFullScreen.putExtra("user_picture", image);
            goToFullScreen.putExtra("user_name", mUserName);
            startActivity(goToFullScreen);
        }));

        mUsersReference.child(mCurrentPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("blocked")) {
                    mUsersReference.child(mCurrentPhone).child("blocked").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(mUserPhone)) {
                                        mBlockedText.setText(R.string.unblock);
                                    } else {
                                        mBlockedText.setText(R.string.block);
                                    }
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

        mSharedItems.setOnClickListener(v -> new CheckInternet_(internet -> {
            if (internet) {
                getSharedItems();
            } else {
                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        }));

        mDeleteConversation.setOnClickListener(v -> new CheckInternet_(internet -> {
            if (internet) {
                deleteConversation();
            } else {
                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        }));

        mBlocked.setOnClickListener(v -> new CheckInternet_(internet -> {
            if (internet) {
                blockOrUnblock();
            } else {
                Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void getSharedItems() {
        AlertDialog.Builder build = new AlertDialog.Builder(OneToOneChatSettings.this,
                R.style.AppCompatAlertDialogStyle);
        build.setPositiveButton(R.string.ok, (dialog, id) -> {
            Intent i = new Intent(OneToOneChatSettings.this, SharedActivity.class);
            i.putExtra("chat_name", mUserPhone);
            i.putExtra("chat_id", mChatId);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
        });

        build.setNegativeButton(R.string.cancel, (dialog, id) -> Toast.makeText(OneToOneChatSettings.this, "load cancelled",
                Toast.LENGTH_SHORT).show());

        AlertDialog al = build.create();
        al.setIcon(R.drawable.ic_warning);
        al.setTitle(R.string.media_loading);
        al.setMessage(getString(R.string.sure_load));
        al.show();
    }

    private void deleteConversation() {
        new TTFancyGifDialog.Builder(this)
                .setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_conversation))
                .setGifResource(R.drawable.gif30)
                .OnPositiveClicked(() -> {
                    mUsersReference.child(Objects.requireNonNull(mCurrentPhone))
                            .child("conversation").child(mChatId).removeValue();
                    mUsersReference.child(mUserPhone).child("conversation").child(mChatId).removeValue();

                    mChatReference.child(mChatId).child("messages").addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            mMessagesReference.child(dataSnapshot.getKey()).removeValue().addOnCompleteListener(task -> {

                            });
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
                    });

                    mChatReference.child(mChatId).removeValue().addOnCompleteListener(task -> {
                        Toast.makeText(this, "Conversation Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                        recreate();
                    });
                })
                .OnNegativeClicked(() -> {

                })
                .build();
    }


    private void blockOrUnblock() {
        if (mBlockedText.getText().equals(getString(R.string.block))) {

            new TTFancyGifDialog.Builder(this)
                    .setTitle(getString(R.string.block))
                    .setMessage(getString(R.string.block_msg))
                    .setGifResource(R.drawable.gif30)
                    .OnPositiveClicked(() -> {

                        Map<String, Object> m = new HashMap<>();
                        m.put(mUserPhone, true);
                        mUsersReference.child(mCurrentPhone).child("blocked").updateChildren(m);

                        Map<String, Object> m1 = new HashMap<>();
                        m1.put(mCurrentPhone, true);
                        mUsersReference.child(mUserPhone).child("blocked_by").updateChildren(m1);
                        Toast.makeText(this, R.string.user_blocked_msg, Toast.LENGTH_SHORT)
                                .show();
                        finish();

                    })
                    .OnNegativeClicked(() -> {

                    })
                    .build();
        } else {

            new TTFancyGifDialog.Builder(this)
                    .setTitle(getString(R.string.unblock))
                    .setMessage(getString(R.string.unb_text))
                    .setGifResource(R.drawable.gif30)
                    .OnPositiveClicked(() -> {

                        mUsersReference.child(mCurrentPhone).child("blocked").child(mUserPhone).removeValue();
                        mUsersReference.child(mUserPhone).child("blocked_by").child(mCurrentPhone).removeValue();
                        Toast.makeText(this, R.string.u_unblock, Toast.LENGTH_SHORT).show();
                         finish();
                    })
                    .OnNegativeClicked(() -> {

                    })
                    .build();

        }
    }

    public void getImage(MyCallback myCallback) {
        mUsersReference.child(mUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                myCallback.onCallback(image);

                Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_avatar).into(mProfileImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        mImageProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public interface MyCallback {
        void onCallback(String image);
    }

    private void setLocale(String lang) {
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
    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }


}
