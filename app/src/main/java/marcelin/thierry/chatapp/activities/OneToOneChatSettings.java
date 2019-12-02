package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
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


public class OneToOneChatSettings extends AppCompatActivity {

    private Toolbar mOneToOneBar;

    private TextView sendMessage;
    private TextView callUser;
    private TextView videoCallUser;
    private TextView infoAboutUser;
    private CircleImageView mProfileImage;

    private String mUserPhone;
    private String mUserName;
    private String mUserPicture;
    private String mChatId;


    private ProgressBar mImageProgress;

    private Dialog mDialog;

    private static final DatabaseReference mChatReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_chat");

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_messages");

    int x;

    private String mCurrentPhone;
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_one_to_one_chat_settings);

        mUserPhone = getIntent().getStringExtra("user_phone");
        mUserName = getIntent().getStringExtra("user_name");
        mUserPicture = getIntent().getStringExtra("user_picture");
        mChatId = getIntent().getStringExtra("chat_id");
        mCurrentPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();


        mOneToOneBar = findViewById(R.id.one_to_one_bar);
        setSupportActionBar(mOneToOneBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(mUserName);
     //   getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sendMessage = findViewById(R.id.sendMessage);
        callUser = findViewById(R.id.callUser);
        videoCallUser = findViewById(R.id.videoCallUser);
        infoAboutUser = findViewById(R.id.infoAboutUser);

        mImageProgress = findViewById(R.id.imageProgress);

        mProfileImage = findViewById(R.id.user_profile_image);

        callUser.setOnClickListener(view ->
                Toast.makeText(OneToOneChatSettings.this, R.string.in_dev
                        , Toast.LENGTH_SHORT).show());

        videoCallUser.setOnClickListener(view ->
                Toast.makeText(OneToOneChatSettings.this, R.string.in_dev
                        , Toast.LENGTH_SHORT).show());

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

        sendMessage.setOnClickListener(view -> {

            Intent goBackToChat = new Intent(OneToOneChatSettings.this,
                    ChatActivity.class);
            goBackToChat.putExtra("user_name", mUserName);
            goBackToChat.putExtra("user_phone", mUserPhone);
            goBackToChat.putExtra("user_picture", mUserPicture);
            goBackToChat.putExtra("chat_id", mChatId);

            startActivity(goBackToChat);
            finish();

        });

        mDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);


        infoAboutUser.setOnClickListener(view -> showCustomDialog());

    }

    private void showCustomDialog() {

        TextView phoneNumber;
        TextView media;
        TextView delete;
        TextView block;

        mDialog.setContentView(R.layout.chat_settings_layout);
        mDialog.show();

        phoneNumber = mDialog.findViewById(R.id.phone_number);
        media = mDialog.findViewById(R.id.media);
        delete = mDialog.findViewById(R.id.delete);
        block = mDialog.findViewById(R.id.block);

        mUsersReference.child(mCurrentPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("blocked")){
                    mUsersReference.child(mCurrentPhone).child("blocked").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(mUserPhone)){
                                        block.setText(R.string.unblock);
                                    }else{
                                        block.setText(R.string.block);
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


        phoneNumber.setText(mUserPhone);

        media.setOnClickListener(view -> {

            AlertDialog.Builder build = new AlertDialog.Builder(OneToOneChatSettings.this,
                    R.style.AppCompatAlertDialogStyle);
            build.setPositiveButton(R.string.ok, (dialog, id) -> {

//                    Toast.makeText(OneToOneChatSettings.this, "In development", Toast.LENGTH_SHORT)
//                            .show();

                Intent i = new Intent(OneToOneChatSettings.this, SharedActivity.class);
                i.putExtra("chat_name", mUserPhone);
                i.putExtra("chat_id", mChatId);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);

            });

            build.setNegativeButton(R.string.cancel, (dialog, id) ->{

                Toast.makeText(OneToOneChatSettings.this, "load cancelled",
                        Toast.LENGTH_SHORT).show();

            });

            AlertDialog al = build.create();
            al.setIcon(R.drawable.ic_warning);
            al.setTitle(R.string.media_loading);
            al.setMessage(getString(R.string.sure_load));
            al.show();


        });


        delete.setOnClickListener(view -> {

          new TTFancyGifDialog.Builder(this)
                  .setTitle(getString(R.string.delete))
                  .setMessage(getString(R.string.delete_conversation))
                  .setGifResource(R.drawable.gif30)
                  .OnPositiveClicked(()->{

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
              });
          })
          .OnNegativeClicked(() ->{

          })
          .build();

//          builder.setNegativeButton(R.string.cancel, (dialog,id) ->
//                  Toast.makeText(OneToOneChatSettings.this, "Cancelled", Toast.LENGTH_SHORT)
//                  .show());
//
//          AlertDialog dialog = builder.create();
//          dialog.setIcon(R.drawable.ic_warning);
//          dialog.setTitle(R.string.delete);
//          dialog.setMessage(getString(R.string.delete_conversation));
//          dialog.show();

        });

        block.setOnClickListener(view -> {

            if(block.getText().equals(getString(R.string.block))){

                new TTFancyGifDialog.Builder(this)
                        .setTitle(getString(R.string.block))
                        .setMessage(getString(R.string.block_msg))
                        .setGifResource(R.drawable.gif30)
                        .OnPositiveClicked(()->{

                    Map<String, Object> m = new HashMap<>();
                    m.put(mUserPhone, true);
                    mUsersReference.child(mCurrentPhone).child("blocked").updateChildren(m);

                    Map<String, Object> m1 = new HashMap<>();
                    m1.put(mCurrentPhone, true);
                    mUsersReference.child(mUserPhone).child("blocked_by").updateChildren(m1);
                    mDialog.dismiss();
                    Toast.makeText(this,R.string.user_blocked_msg, Toast.LENGTH_SHORT)
                            .show();

                })
                .OnNegativeClicked(() ->{

                })
                .build();
            }else{

               new TTFancyGifDialog.Builder(this)
                       .setTitle(getString(R.string.unblock))
                       .setMessage(getString(R.string.unb_text))
                       .setGifResource(R.drawable.gif30)
                       .OnPositiveClicked(() -> {

                    mUsersReference.child(mCurrentPhone).child("blocked").child(mUserPhone).removeValue();
                    mUsersReference.child(mUserPhone).child("blocked_by").child(mCurrentPhone).removeValue();
                    mDialog.dismiss();
                    Toast.makeText(this, R.string.u_unblock, Toast.LENGTH_SHORT).show();

                })
               .OnNegativeClicked(() ->{

               })
               .build();

            }

        });
    }


    public void getImage(MyCallback myCallback){
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

//    public void getMessage(MyCallback_ myCallback){
//
//        mChatReference.child(mChatId).child("messages").addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                mMessagesReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent
//                        (new ValueEventListener() {
//                            @Override
//                            public void onDataChange(DataSnapshot dataSnapshot) {
//                                Messages m = dataSnapshot.getValue(Messages.class);
//                                if(m == null){
//                                    return;
//                                }
//                                myCallback.onCallback(m);
////                                switch (m.getType()) {
////                                    case "image":
////                                        imagesList.add(m.getContent());
////                                        break;
////                                    case "audio":
////                                        audioList.add(m.getContent());
////                                        break;
////                                    case "video":
////                                        videoList.add(m.getContent());
////                                        break;
////                                    case "document":
////                                        documentList.add(m.getContent());
////                                        break;
////                                    default:
////                                        return;
////                                }
//
//                            }
//
//                            @Override
//                            public void onCancelled(DatabaseError databaseError) {
//
//                            }
//                        });
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }});
//
//    }

//    public interface MyCallback_ {
//        void onCallback(Messages m);
//    }

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
