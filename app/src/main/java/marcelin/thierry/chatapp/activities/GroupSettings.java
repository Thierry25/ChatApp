package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.adapters.UsersInCurrentGroupAdapter;
import marcelin.thierry.chatapp.classes.Users;

public class GroupSettings extends AppCompatActivity {

    private String mGroupName;
    private String mGroupPicture;
    private String mChatId;


    private StorageReference mStorageRef;

    private Toolbar mSettingsToolbar;
    private ImageView mGroupImage;
    private ImageView mUpdateImage;
    private RecyclerView mUsersInGroup;

    private List<Users> mListOfUsers = new ArrayList<>();
    private List<String> mUsersNumber = new ArrayList<>();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String mCurrentPhoneNumber = "";

    private final static DatabaseReference mGroupReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_group");

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private UsersInCurrentGroupAdapter mUsersInCurrentGroup;

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();

    private TextView title;
    private ImageView backButton;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_group_settings);

        mGroupName = getIntent().getStringExtra("Group_name");
        mGroupPicture = getIntent().getStringExtra("Group_image");
        mChatId = getIntent().getStringExtra("chat_id");

        mSettingsToolbar = findViewById(R.id.groupSettingsBar);
        mGroupImage = findViewById(R.id.groupImage);
        mUpdateImage = findViewById(R.id.updateImage);
        mUsersInGroup = findViewById(R.id.usersInGroup);

        mUsersInCurrentGroup = new UsersInCurrentGroupAdapter(mListOfUsers, this);

        mUsersInGroup.setHasFixedSize(true);
        mUsersInGroup.setLayoutManager(new LinearLayoutManager(this));
        mUsersInGroup.setAdapter(mUsersInCurrentGroup);

        setSupportActionBar(mSettingsToolbar);

        Objects.requireNonNull(getSupportActionBar()).setTitle(mChatId);
     //   getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        profileImage = findViewById(R.id.profileImage);
        title = findViewById(R.id.title);
        title.setTextSize(32);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.allura);
        title.setTypeface(typeface);
        title.setText(mChatId);
        profileImage.setVisibility(View.GONE);

        mSettingsToolbar.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(GroupSettings.this);
            dialog.setIcon(R.drawable.ic_about);
            dialog.setTitle(R.string.change_group_name);

            EditText changeGroupName = new EditText(GroupSettings.this);
            setMargins(changeGroupName, 10, 10, 10, 10);
            changeGroupName.setPadding(40, 40, 40, 40);
            dialog.setView(changeGroupName);

            changeGroupName.setText(mSettingsToolbar.getTitle());

            dialog.setPositiveButton(R.string.ok, (id, dial) ->{
                if(!changeGroupName.getText().toString().trim().contentEquals(mSettingsToolbar.getTitle())){
                    ProgressDialog progressDialog = new ProgressDialog(GroupSettings.this);
                    progressDialog.setTitle(getString(R.string.saving));
                    progressDialog.setMessage(getString(R.string.changing_group_name));
                    progressDialog.show();

                    mGroupReference.child(mGroupName).child("newName")
                            .setValue(changeGroupName.getText().toString().trim()).addOnCompleteListener(task -> {

                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    mSettingsToolbar.setTitle(changeGroupName.getText().toString().trim());
                                    Toast.makeText(GroupSettings.this, R.string.group_name_success,
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(GroupSettings.this, R.string.group_error_name, Toast.LENGTH_SHORT).show();
                                }

                            });


                }else{
                    Toast.makeText(GroupSettings.this, R.string.no_changes, Toast.LENGTH_SHORT).show();
                }
            });

            dialog.setNegativeButton(R.string.cancel, (id, dial) ->{

            });

            AlertDialog alert = dialog.create();
            alert.show();
        });

        mCurrentPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        mUsersReference.keepSynced(true);
        mGroupReference.keepSynced(true);

        Picasso.get().load(mGroupPicture).placeholder(R.drawable.ic_avatar).into(mGroupImage);

        mGroupReference.keepSynced(true);

        mGroupReference.child(mGroupName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.i("ValueFound", Objects.requireNonNull(dataSnapshot.getValue()).toString());

                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                mGroupImage.setOnClickListener(view -> {
                    Intent i = new Intent(view.getContext(), FullScreenImageActivity.class);
                    i.putExtra("image_shown", image);
                    startActivity(i);
                });

                if (!image.equals("Default")) {

                    Picasso.get().load(image)
                            .placeholder(R.drawable.ic_avatar)
                            .into(mGroupImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                    Toast.makeText(GroupSettings.this, "Done", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(image)
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(mGroupImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                    // Toast.makeText(GroupSettings.this, "Skrrt", Toast.LENGTH_SHORT).show();

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

        mGroupReference.keepSynced(true);


        mGroupReference.child(mGroupName).child("admins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> adm = (Map<String, Object>) dataSnapshot.getValue();
                if (adm != null) {

                    //Searching in users
                    mGroupReference.child(mGroupName).child("users").addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();

                                    Log.i("KeyValue", dataSnapshot.getKey());
                                    Log.i("ValValue", dataSnapshot.getValue().toString());
                                    if (m != null) {
                                        mUsersNumber.addAll(m.keySet());

                                        Log.i("Usernumbers", mUsersNumber.toString());
                                    }

                                    for (String st : mUsersNumber) {
                                        mUsersReference.child(st).addListenerForSingleValueEvent
                                                (new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Users u = dataSnapshot.getValue(Users.class);

                                                        if (u != null) {
                                                            u.setAdmin(false);
                                                            if (adm.containsKey(dataSnapshot.getKey())) {
                                                                u.setAdmin(true);
                                                            }
                                                            String s = Users.getLocalContactList()
                                                                    .get(st);
                                                            if (st.equals(mCurrentPhoneNumber)) {
                                                                u.setNameStoredInPhone(getString(R.string.you));
                                                            } else {

                                                                s = s != null && s.length() > 0 ? s : st;
                                                                u.setNameStoredInPhone(s);

                                                                if (s.equals(st)) {
                                                                    u.setSavedInContact(false);
                                                                }
                                                            }

                                                            u.setPhoneNumber(st);
                                                            u.setChatId(mGroupName);
                                                            //u.setConvoId(Users.);

                                                            mListOfUsers.add(u);
                                                            Log.i("ListUsers", mListOfUsers.toString());
                                                            mUsersInCurrentGroup.notifyDataSetChanged();
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

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUpdateImage.setOnClickListener(view -> mGroupReference.child(mGroupName).child("admins").addListenerForSingleValueEvent
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(mCurrentPhoneNumber)) {
                            CropImage.activity()
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setAspectRatio(1, 1)
                                    .start(GroupSettings.this);
                        } else {
                            Toast.makeText(GroupSettings.this
                                    , R.string.not_admin
                                    , Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }));

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            finish();
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                final ProgressDialog uploadDialog = new ProgressDialog(GroupSettings.this);
                uploadDialog.setTitle(getString(R.string.picture_upload));
                uploadDialog.setMessage(getString(R.string.upload_text));
                uploadDialog.setCanceledOnTouchOutside(false);
                uploadDialog.show();

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

                final StorageReference image_path = mStorageRef.child("profile_pics").child(randomIdentifier() + ".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_pics").child("thumbs").child(randomIdentifier() + ".jpg");

                image_path.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()) {
                            final String download_url = Objects.requireNonNull(task.getResult().getDownloadUrl()).toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thum_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                    if (thumb_task.isSuccessful()) {

                                        Map updateHashMap = new HashMap();
                                        updateHashMap.put("image", download_url);
                                        updateHashMap.put("thumbnail", thumb_downloadUrl);

                                        mGroupReference.child(mGroupName).updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    uploadDialog.dismiss();
                                                    Toast.makeText(GroupSettings.this, R.string.upload_success, Toast.LENGTH_SHORT).show();

                                                } else {
                                                    Toast.makeText(GroupSettings.this, R.string.upload_error, Toast.LENGTH_SHORT).show();
                                                }

                                            }
                                        });
                                    } else {
                                        Toast.makeText(getApplicationContext(), R.string.thumbnail_error, Toast.LENGTH_SHORT).show();
                                        uploadDialog.dismiss();
                                    }

                                }
                            });

                        } else {
                            // TODO: Learn about the specific errors that can happen to handle them more efficiently
                            Toast.makeText(GroupSettings.this, R.string.err,
                                    Toast.LENGTH_SHORT).show();
                            uploadDialog.dismiss();
                        }


                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.menu_invite_link) {
            mGroupReference.child(mGroupName).child("admins").addListenerForSingleValueEvent
                    (new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(mCurrentPhoneNumber)) {
                                mGroupReference.child(mGroupName).child("link").addListenerForSingleValueEvent
                                        (new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String s = dataSnapshot.getValue(String.class);
                                                Intent goToForwardActivity = new Intent(GroupSettings.this,
                                                        GroupForwardActivity.class);
                                                goToForwardActivity.putExtra("link_to_send", s);
                                                startActivity(goToForwardActivity);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                            } else {
                                Toast.makeText(GroupSettings.this, R.string.not_admin,
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        } else if (item.getItemId() == R.id.menu_delete_group) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> {

                mGroupReference.child(mGroupName).child("admins").addListenerForSingleValueEvent
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild(mCurrentPhoneNumber)) {

                                    mGroupReference.child(mGroupName).child("users").addListenerForSingleValueEvent
                                            (new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                                                    if (m == null) {
                                                        return;
                                                    }
                                                    for (String s : m.keySet()) {
                                                        mUsersReference.child(s).child("conversation")
                                                                .child("G-" + mGroupName).removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });


                                    mRootReference.child("group_link").child(mGroupName).removeValue();
                                    mGroupReference.child(mGroupName).removeValue();

                                    Intent i = new Intent(GroupSettings.this, MainActivity.class);
                                    startActivity(i);
                                    overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
                                    finish();


                                } else {
                                    Toast.makeText(GroupSettings.this, R.string.not_admin,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

            });

            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {

            });

            AlertDialog dial = builder.create();
            dial.setIcon(R.drawable.ic_warning);
            dial.setTitle(getString(R.string.group_deletion));
            dial.setMessage(getString(R.string.group_deletion_text) + mGroupName + "?" + "\n"
                    + getString(R.string.group_deletion_error));
            dial.show();

        } else if (item.getItemId() == R.id.menu_leave_group) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> {

                mGroupReference.child(mGroupName).child("admins").addListenerForSingleValueEvent
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                                if (m == null) {
                                    return;
                                }
                                if (m.size() > 1) {
                                    mGroupReference.child(mGroupName).child("users").child(mCurrentPhoneNumber)
                                            .removeValue();
                                    mUsersReference.child(mCurrentPhoneNumber).child("conversation")
                                            .child("G-" + mGroupName).removeValue();
                                } else if (m.size() == 1) {

                                    if (m.keySet().toArray()[0].equals(mCurrentPhoneNumber)) {
                                        AlertDialog.Builder bu = new AlertDialog.Builder(GroupSettings.this);
                                        bu.setPositiveButton(R.string.ok, (dial, id1) -> {

                                            mGroupReference.child(mGroupName).child("users").addListenerForSingleValueEvent
                                                    (new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                                                            if (m == null) {
                                                                return;
                                                            }
                                                            for (String s : m.keySet()) {
                                                                mUsersReference.child(s).child("conversation")
                                                                        .child("G-" + mGroupName).removeValue();
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                            mGroupReference.child(mGroupName).removeValue();
                                            mRootReference.child("group_link").child(mGroupName).removeValue();

                                        });
                                        bu.setNegativeButton(R.string.cancel, (dial, id1) -> {

                                            Toast.makeText(GroupSettings.this, "Canceled", Toast.LENGTH_SHORT)
                                                    .show();

                                        });

                                        AlertDialog aD = bu.create();
                                        aD.setIcon(R.drawable.ic_warning);
                                        aD.setTitle(getString(R.string.leave_group));
                                        aD.setMessage(getString(R.string.leave_group_text) +
                                                "\n" + getString(R.string.sure) + mGroupName
                                                + "?");
                                        aD.show();
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
            });

            builder.setNegativeButton(R.string.cancel, (dialog, id) -> {

                Toast.makeText(GroupSettings.this, R.string.cancel, Toast.LENGTH_SHORT).show();
            });

            AlertDialog dial = builder.create();
            dial.setIcon(R.drawable.ic_warning);
            dial.setTitle("Leave Group");
            dial.setMessage(getString(R.string.leave_group_) + mGroupName + "?");
            dial.show();

        } else if(item.getItemId() == R.id.menu_media){
            AlertDialog.Builder x = new AlertDialog.Builder(this);
            x.setPositiveButton(R.string.ok, (id, dialog_) -> {

                Intent intent = new Intent(this, GroupSharedActivity.class);
                intent.putExtra("chat_id", mGroupName);
                intent.putExtra("chat_name", mGroupName);
                startActivity(intent);

            });
            x.setNegativeButton(R.string.cancel, (id, dialog_) -> {

            });

            AlertDialog a = x.create();
            a.setIcon(R.drawable.ic_warning);
            a.setTitle(getString(R.string.media_loading));
            a.setMessage(getString(R.string.r_sure) + mChatId + "?"
                    + getString(R.string.process_time));
            a.show();
        }

        return true;
    }

    final String lexicon = "ABCDEFGHIJKLMNOPQRSTUVWXYZ12345674890";

    final java.util.Random rand = new java.util.Random();

    final Set<String> identifiers = new HashSet<>();

    private String randomIdentifier() {
        StringBuilder builder = new StringBuilder();
        while (builder.toString().length() == 0) {
            int length = rand.nextInt(5) + 5;
            for (int i = 0; i < length; i++) {
                builder.append(lexicon.charAt(rand.nextInt(lexicon.length())));
            }
            if (identifiers.contains(builder.toString())) {
                builder = new StringBuilder();
            }
        }
        return builder.toString();
    }

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
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


}
