package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Conversation;
import marcelin.thierry.chatapp.classes.Users;

public class SettingsActivity extends AppCompatActivity{

    // Firebase
    private DatabaseReference mDatabaseRef;
    private FirebaseUser mCurrentUser;
    private StorageReference mStorageRef;
    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    final Context context = this;

    private ImageView mProfilePic;
    private TextView mProfileName;
    private TextView mProfileStatus;

    private ImageView mDeletePic;
    private ImageView mChangeProfilePic, mBackButton;

  //  private Toolbar mSettingsBar;

    private TextView mAccount, mInviteFriends, mDeleteAccount, mAboutAds;

    private ProgressBar mImageUploadProgress;

    private Dialog mDialog;
    
    private String mCurrentUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings);

      //  mSettingsBar = findViewById(R.id.settings_bar);
//        setSupportActionBar(mSettingsBar);
//        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.ac_settings);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProfilePic = findViewById(R.id.profile_image);
        mProfileName = findViewById(R.id.profile_name);
        mProfileStatus = findViewById(R.id.profile_status);
        mDeletePic = findViewById(R.id.delete_pic);
        mChangeProfilePic = findViewById(R.id.edit_pic);
        mImageUploadProgress = findViewById(R.id.image_uploadProgress);

        mBackButton = findViewById(R.id.backButton);
        mAccount = findViewById(R.id.account);
        mInviteFriends = findViewById(R.id.inviteFriends);
        mDeleteAccount = findViewById(R.id.deleteAccount);
        mAboutAds = findViewById(R.id.aboutAds);

        mDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        assert mCurrentUser != null;
        mCurrentUserPhone = mCurrentUser.getPhoneNumber();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("ads_users").child(mCurrentUserPhone);
        mDatabaseRef.keepSynced(true);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();
                String name = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
                String status = Objects.requireNonNull(dataSnapshot.child("status").getValue()).toString();
                // Store a thumbnail instead of real picture
               // String thumbnail = dataSnapshot.child("thumbnail").getValue().toString();

                mProfileName.setText(name);
                mProfileStatus.setText(status);


                mProfilePic.setOnClickListener(view -> {
                    Intent i = new Intent(view.getContext(), FullScreenImageActivity.class);
                    i.putExtra("image_shown", image);
                    startActivity(i);
                });

                // TODO: Fix code with Loader

                if(!image.equals("Default")) {

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_avatar)
                            .into(mProfilePic, new Callback() {
                                @Override
                                public void onSuccess() {

                                    mImageUploadProgress.setVisibility(View.GONE);

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(image)
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(mProfilePic, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                    mImageUploadProgress.setVisibility(View.GONE);

                                                }

                                                @Override
                                                public void onError(Exception e) {

                                                }
                                            });

                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mDeletePic.setOnClickListener(view -> {

            new TTFancyGifDialog.Builder(SettingsActivity.this)
                    .isCancellable(true)
                    .setTitle(getString(R.string.delete))
                    .setMessage(getString(R.string.delete_pic))
                    .OnNegativeClicked(() -> {

                    })
                    .OnPositiveClicked(() -> {

                    })
                    .setGifResource(R.drawable.gif13)
                    .build();

        });

        mBackButton.setOnClickListener( v ->{
            finish();
        });

        mProfileStatus.setOnClickListener(view -> {

            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            alert.setTitle(R.string.stat_chn);
            final EditText statusEntered = new EditText(context);
            statusEntered.setPadding(20, 40, 20, 20);
            statusEntered.setText(mProfileStatus.getText().toString());
            alert.setView(statusEntered);
            alert.setPositiveButton(R.string.ok, (dialog, whichButton) -> {
                //Put actions for OK button here
               // String newStatus = statusEntered.getText().toString();
                if (!statusEntered.getText().toString().equals(mProfileStatus.getText().toString())) {
                    final ProgressDialog saveProgress = new ProgressDialog(context);
                    saveProgress.setTitle(R.string.saving);
                    saveProgress.setMessage(getString(R.string.stat_sav));
                    saveProgress.show();


                    mDatabaseRef.child("status").setValue(statusEntered.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                                saveProgress.dismiss();
                            else
                                Toast.makeText(context, R.string.there_was_an_error, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else
                    Toast.makeText(context, R.string.no_changes, Toast.LENGTH_SHORT).show();
            });
            alert.setNegativeButton("Cancel", (dialog, whichButton) -> {
                //Put actions for CANCEL button here, or leave in blank
            });
            alert.show();
        });


        mChangeProfilePic.setOnClickListener(view ->
              //  CropImage.activity()

                CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(SettingsActivity.this));
        mAccount.setOnClickListener(view -> showAccountSettings());

        mInviteFriends.setOnClickListener(v ->{
            showChangeLanguageDialog();
        });
        mDeleteAccount.setOnClickListener(view -> {
           new TTFancyGifDialog.Builder(SettingsActivity.this)
                   .setGifResource(R.drawable.gif30)
                   .setTitle(getString(R.string.del_acc))
                   .isCancellable(true)
                   .setMessage(getString(R.string.del_warn))
                   .OnPositiveClicked(() ->{
                       mUsersReference.child("ads_users").child(mCurrentUserPhone).removeValue();
                       finishAffinity();
                       System.exit(0);
                   })
                   .OnNegativeClicked(() ->{
                       Toast.makeText(view.getContext(), R.string.cancel, Toast.LENGTH_SHORT).show();
                   })
                   .build();


            });

        mAboutAds.setOnClickListener(view -> showAdsAbout());
    }

    private void showAdsAbout() {
        Toast.makeText(context, "Skuu", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                final ProgressDialog uploadDialog = new ProgressDialog(SettingsActivity.this);
                uploadDialog.setTitle(R.string.picture_upload);
                uploadDialog.setMessage(getString(R.string.upload_text));
                uploadDialog.setCanceledOnTouchOutside(false);
                uploadDialog.show();

                mImageUploadProgress.setVisibility(View.VISIBLE);

                Uri resultUri = result.getUri();

                // Getting the actual file from the path
                File thumb_imagePath = new File(resultUri.getPath());


                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_imagePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                assert thumb_bitmap != null;
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thum_byte = baos.toByteArray();

                // Adding an image to firebase storage

                final StorageReference image_path = mStorageRef.child("profile_pics").child(mCurrentUser.getUid() + ".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_pics").child("thumbs").child(mCurrentUser.getUid() + ".jpg");

                image_path.putFile(resultUri).addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        final String download_url = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = thumb_filepath.putBytes(thum_byte);
                        uploadTask.addOnCompleteListener(thumb_task -> {

                            String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                            if (thumb_task.isSuccessful()) {

                                Map updateHashMap = new HashMap();
                                updateHashMap.put("image", download_url);
                                updateHashMap.put("thumbnail", thumb_downloadUrl);

                                mDatabaseRef.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task1) {

                                        if (task1.isSuccessful()) {

                                            uploadDialog.dismiss();
                                            Toast.makeText(SettingsActivity.this, R.string.upload_success, Toast.LENGTH_SHORT).show();

                                        } else {
                                            Toast.makeText(context, R.string.upload_error, Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.thumbnail_error, Toast.LENGTH_SHORT).show();
                                uploadDialog.dismiss();
                            }

                        });

                    } else {
                        // TODO: Learn about the specific errors that can happen to handle them more efficiently
                        Toast.makeText(context, R.string.err, Toast.LENGTH_SHORT).show();
                        uploadDialog.dismiss();
                    }


                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    private void showAccountSettings(){

        CircleImageView mProfilePic;
        TextView mProfileName;
        TextView mProfileStatus;
        TextView mPhoneNumber;
        TextView mNumberOfConversation;
        TextView mBlockedUsers;

        mDialog.setContentView(R.layout.account_settings_layout);
        mDialog.show();

        mProfilePic = mDialog.findViewById(R.id.profilePic);
        mProfileName = mDialog.findViewById(R.id.name);
        mProfileStatus = mDialog.findViewById(R.id.status);
        mPhoneNumber = mDialog.findViewById(R.id.phone_number);
        mNumberOfConversation = mDialog.findViewById(R.id.numberConversation);
        mBlockedUsers = mDialog.findViewById(R.id.numberOfBlockedUsers);

        mUsersReference.child(mCurrentUserPhone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Users u = dataSnapshot.getValue(Users.class);
                if(u == null){
                    return;
                }
                Picasso.get().load(u.getImage()).placeholder(R.drawable.ic_avatar).into(mProfilePic);
                mProfileName.setText(mProfileName.getText() + " :" + u.getName());
                mProfileStatus.setText(mProfileStatus.getText() + " :" + u.getStatus());
                mPhoneNumber.setText(mCurrentUserPhone);

                if(dataSnapshot.hasChild("conversation")){
                    mUsersReference.child(mCurrentUserPhone).child("conversation").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Conversation c = dataSnapshot.getValue(Conversation.class);
                                    if(c == null){
                                        return;
                                    }
                                    int size = (int) dataSnapshot.getChildrenCount();
                                    mNumberOfConversation.setText(mNumberOfConversation.getText() + ": " + size);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }else{
                    mNumberOfConversation.setText(mNumberOfConversation.getText() + ": 0");
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBlockedUsers.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), BlockedUsersActivity.class);
            startActivity(i);
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

    private void showChangeLanguageDialog() {
        final String[] listLanguages = {getString(R.string.haitian), getString(R.string.french), getString(R.string.english), getString(R.string.spanish)};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(SettingsActivity.this);
        mBuilder.setTitle(getString(R.string.choose_language));
        mBuilder.setSingleChoiceItems(listLanguages, -1, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    setLocale("ht");
                    recreate();
                    break;

                case 1:
                    setLocale("fr");
                    recreate();
                    break;

                case 2:
                    setLocale("en");
                    recreate();
                    break;

                case 3:
                    setLocale("es");
                    recreate();
                    break;

                default:
                    return;
            }

            dialogInterface.dismiss();
        });

        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

}

