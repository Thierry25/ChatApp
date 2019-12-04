package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.format.DateFormat;
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
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
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
import marcelin.thierry.chatapp.adapters.UsersInCurrentChannelAdapter;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Users;

public class SettingsChannelActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    private String mChannelName;
    private String mChannelId;

//    private String z = getString(R.string.you);

    private TextView mChanName;
    private TextView mTimeCreated;
    private TextView mAmountOfSubscribers;

    private CircleImageView mPicId;

    private StorageReference mStorageRef;

    private RecyclerView mChannelList;

    private Toolbar mSettingsToolbar;

    private List<Users> mUsersInChannel = new ArrayList<>();
    private List<String> mUsersNumber = new ArrayList<>();

    private ImageView mUpdateImage;

    private UsersInCurrentChannelAdapter mUsersInCurrentChannel;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String mCurrentPhoneNumber;

   // private int useris;


    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_users");

    private static final DatabaseReference mRootReference = FirebaseDatabase.getInstance()
            .getReference();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_settings_channel);

        mChannelName = getIntent().getStringExtra("Channel_id");
        mChannelId = getIntent().getStringExtra("chat_id");

        mChanName = findViewById(R.id.chan_name);
        mTimeCreated = findViewById(R.id.time_created);
        mAmountOfSubscribers = findViewById(R.id.amount_subscribers);

        mSettingsToolbar = findViewById(R.id.settings_channel_bar);

        mPicId = findViewById(R.id.picId);
        mChannelList = findViewById(R.id.channel_list);

        mCurrentPhoneNumber = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        mUsersInCurrentChannel = new UsersInCurrentChannelAdapter(mUsersInChannel, this);

        mUpdateImage = findViewById(R.id.updateImage);

        mChannelList.setHasFixedSize(true);
        mChannelList.setLayoutManager(new LinearLayoutManager(this));
        mChannelList.setAdapter(mUsersInCurrentChannel);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        setSupportActionBar(mSettingsToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(mChannelId);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSettingsToolbar.setOnClickListener(view -> {
            AlertDialog.Builder dial = new AlertDialog.Builder(SettingsChannelActivity.this);
            dial.setIcon(R.drawable.ic_about);

            EditText changeChannelName = new EditText(SettingsChannelActivity.this);
            setMargins(changeChannelName, 10, 10, 10, 10);
            changeChannelName.setPadding(40, 40, 40, 40);
            dial.setView(changeChannelName);

            changeChannelName.setText(mSettingsToolbar.getTitle());

            dial.setPositiveButton(R.string.ok, (id, dia) ->{

                if(!changeChannelName.getText().toString().trim().contentEquals(mSettingsToolbar.getTitle())){
                    ProgressDialog progressDialog = new ProgressDialog(SettingsChannelActivity.this);
                    progressDialog.setTitle(R.string.saving);
                    progressDialog.setMessage(getString(R.string.chan_na_chgn));
                    progressDialog.show();

                    mChannelReference.child(mChannelName).child("newName")
                            .setValue(changeChannelName.getText().toString().trim()).addOnCompleteListener(task -> {

                                if(task.isSuccessful()){
                                    progressDialog.dismiss();
                                    mSettingsToolbar.setTitle(changeChannelName.getText().toString().trim());
                                    Toast.makeText(SettingsChannelActivity.this, R.string.chan_n_succ,
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(SettingsChannelActivity.this, R.string.group_error_name, Toast.LENGTH_SHORT).show();
                                }

                            });


                }else{
                    Toast.makeText(SettingsChannelActivity.this, R.string.no_changes, Toast.LENGTH_SHORT).show();
                }

            });

            dial.setNegativeButton(R.string.cancel, (id, dia) ->{

            });

            AlertDialog dialog = dial.create();
            dialog.setTitle(getString(R.string.change_channel_name));
            dialog.show();
        });

        mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String image = Objects.requireNonNull(dataSnapshot.child("image").getValue()).toString();

                mPicId.setOnClickListener(view ->{
                    Intent i = new Intent(view.getContext(), FullScreenImageActivity.class);
                    i.putExtra("image_shown", image);
                    startActivity(i);
                });

                if(!image.equals("Default")) {

                    Picasso.get().load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.ic_avatar)
                            .into(mPicId, new Callback() {
                                @Override
                                public void onSuccess() {

                                    Toast.makeText(SettingsChannelActivity.this,
                                            "Done", Toast.LENGTH_SHORT).show();

                                }

                                @Override
                                public void onError(Exception e) {

                                    Picasso.get().load(image)
                                            .placeholder(R.drawable.ic_avatar)
                                            .into(mPicId, new Callback() {
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


        mChannelReference.child(mChannelName).child("subscribers").addValueEventListener
                (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();

                        if (m != null) {
                            mUsersNumber.addAll(m.keySet());
                         //   users = mUsersNumber.size();
                        }

                        for(String st: mUsersNumber) {
                            mUsersReference.child(st).addListenerForSingleValueEvent
                                    (new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Users u = dataSnapshot.getValue(Users.class);

                                            if (u != null) {

                                                String s = Users.getLocalContactList()
                                                        .get(st);
                                                if(st.equals(mCurrentPhoneNumber)){
                                                    u.setNameStoredInPhone(getString(R.string.you));
                                                }else {
                                                    s = s != null && s.length() > 0 ? s : st;
                                                    u.setNameStoredInPhone(s);

                                                    u.setSavedInContact(true);
                                                    if(s.equals(st)){
                                                        // Open up functionality to save to contacts
                                                        u.setSavedInContact(false);
                                                        u.setNameStoredInPhone(st);
                                                    }
                                                }

                                                u.setPhoneNumber(st);

                                                u.setChatId(mChannelName);
                                               // u.setTitle(mChannelName);

                                                mUsersInChannel.add(u);
                                                Log.i("ListUsers", mUsersInChannel.toString());
                                                mUsersInCurrentChannel.notifyDataSetChanged();
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

        mChannelReference.child(mChannelName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Channel c = dataSnapshot.getValue(Channel.class);
                String name;
                if (c != null) {
                    int subscribers = c.getSubscribers().size();
                    if(c.getNewName().equals("")){
                         name = c.getName();
                    }else{
                         name = c.getNewName();
                    }

                    String date = getDate(c.getTimestamp());

                    mChanName.setText(name);

                    mTimeCreated.setText(mTimeCreated.getText() + " " + date);

                    mAmountOfSubscribers.setText(subscribers + " "  + mAmountOfSubscribers.getText());

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mUpdateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1, 1)
                        .start(SettingsChannelActivity.this);


            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.channel_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch(item.getItemId()){
            case R.id.menu_invite_link:
                mChannelReference.child(mChannelName).child("link").addListenerForSingleValueEvent
                        (new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String s = dataSnapshot.getValue(String.class);
                                Intent goToForwardActivity = new Intent(SettingsChannelActivity.this,
                                        ForwardActivity.class);
                                goToForwardActivity.putExtra("link_to_send", s);
                                startActivity(goToForwardActivity);
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                break;
            case R.id.menu_delete_channel:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setPositiveButton(R.string.ok, (dialog, id) ->{

                    mChannelReference.child(mChannelName).child("subscribers").addListenerForSingleValueEvent
                            (new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Map<String, Object> m = (Map<String, Object>) dataSnapshot.getValue();
                                    if (m == null) {
                                        return;
                                    }
                                    for(String s : m.keySet()){
                                        mUsersReference.child(s).child("conversation").child("C-"+mChannelName)
                                                .removeValue();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                    mChannelReference.child(mChannelName).removeValue();
                    mRootReference.child("channel_link").child(mChannelName).removeValue();
                    mRootReference.child("public_channel").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(mChannelName)){
                                mRootReference.child("public_channel").child(mChannelName)
                                        .removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                });

                builder.setNegativeButton(R.string.cancel, (dialog, id) ->{

                    Toast.makeText(this, R.string.deletion_c, Toast.LENGTH_SHORT)
                            .show();

                });

                AlertDialog dialog = builder.create();
                dialog.setIcon(R.drawable.ic_warning);
                dialog.setTitle(R.string.delete_channel);
                dialog.setMessage(getString(R.string.sure_del) + " " + mChannelName
                + "?" + "\n" + getString(R.string.time_takin));
                dialog.show();
                break;

            case R.id.menu_media:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setPositiveButton(R.string.ok, (id, dialog_) ->{

                    Intent intent = new Intent(this, ChannelSharedActivity.class);
                    intent.putExtra("chat_id", mChannelName);
                    intent.putExtra("chat_name", mChannelName);
                    startActivity(intent);

                });
                builder1.setNegativeButton(R.string.cancel, (id, dialog_) ->{
                    Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show();
                });

                AlertDialog dial = builder1.create();
                dial.setIcon(R.drawable.ic_warning);
                dial.setTitle(R.string.media_loading);
                dial.setMessage((getString(R.string.r_sure) + mChannelName
                + "?" +"\n" + getString(R.string.time_takin)));
                dial.show();

                default:
                    return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                final ProgressDialog uploadDialog = new ProgressDialog(SettingsChannelActivity.this);
                uploadDialog.setTitle(R.string.picture_upload);
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

                final StorageReference image_path = mStorageRef.child("profile_pics").child(randomIdentifier()+  ".jpg");
                final StorageReference thumb_filepath = mStorageRef.child("profile_pics").child("thumbs").child(randomIdentifier()+  ".jpg");

                image_path.putFile(resultUri).addOnCompleteListener(task -> {

                    if(task.isSuccessful()){
                        final String download_url = task.getResult().getDownloadUrl().toString();

                        UploadTask uploadTask = thumb_filepath.putBytes(thum_byte);
                        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                if(thumb_task.isSuccessful()){

                                    Map<String, Object> updateHashMap = new HashMap();
                                    updateHashMap.put("image", download_url);
                                    updateHashMap.put("thumbnail", thumb_downloadUrl);

                                    mChannelReference.child(mChannelName).updateChildren(updateHashMap)
                                            .addOnCompleteListener(task1 -> {

                                                if(task1.isSuccessful()){

                                                    uploadDialog.dismiss();
                                                    Toast.makeText(SettingsChannelActivity.this,
                                                            R.string.upload_success,
                                                            Toast.LENGTH_SHORT).show();

                                                }else{
                                                    Toast.makeText(SettingsChannelActivity.this,
                                                            R.string.upload_error,
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                            });
                                }else{
                                    Toast.makeText(getApplicationContext(), R.string.thumbnail_error,
                                            Toast.LENGTH_SHORT).show();
                                    uploadDialog.dismiss();
                                }

                            }
                        });

                    }else{
                        // TODO: Learn about the specific errors that can happen to handle them more efficiently
                        Toast.makeText(SettingsChannelActivity.this, R.string.err, Toast.LENGTH_SHORT).show();
                        uploadDialog.dismiss();
                    }


                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }


    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(time);
            return DateFormat.format("MMM dd, HH:mm", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userEntry = newText.toLowerCase();
        List<Users> newUsers = new ArrayList<>();

        for(Users u : mUsersInChannel){
            if(u.getNameStoredInPhone().toLowerCase().contains(userEntry)){
                newUsers.add(u);
            }
        }

        mUsersInCurrentChannel.updateList(newUsers);
        return true;
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
