package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;
import com.vanniktech.emoji.EmojiEditText;

import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Conversation;

// LoginActivity initially extends from AppCompatActivity
public class LoginActivity extends AppCompatActivity{

    private CountryCodePicker mCountryCodePicker;
    private EditText mEditTextCarrierNumber;
    private EditText mEditTextCodeReceived;
    private EmojiEditText mEditTextDisplayName;

    private LinearLayout mPhoneLayout;
    private LinearLayout mNameLayout;

    private ProgressDialog mLoginProgress;
    private ProgressDialog mCodeProgress;
    private ProgressDialog mPhoneProgress;
    // Video Call Purposes

    private TextView mEditTextError;
   // private TextView mWelcomeText;

    private Button mSendButton;
    private FloatingActionButton mNextButton;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private DatabaseReference mDatabaseReference;

    private DatabaseReference mChannelReference = FirebaseDatabase.getInstance()
            .getReference().child("ads_channel");

    private String mVerificationId;
  //  private PhoneAuthProvider.ForceResendingToken mResendToken;

    // Boolean to be able to attach different action on the sendButton
    private boolean isZero = true;

  //  private Toolbar mToolbar;

    // TODO: Make sure that later on, to verify if a number is active or not, if by 6 months, it is not active, erase all data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_login);


//        mToolbar = findViewById(R.id.login_bar);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle("Register in ADS");

        // Added Code for video call
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.READ_PHONE_STATE},100);
        }
        mCountryCodePicker = findViewById(R.id.ccp);
        mEditTextCarrierNumber = findViewById(R.id.editText_carrierNumber);

        //Attach CarrierNumber editText to CCP.
        mCountryCodePicker.registerCarrierNumberEditText(mEditTextCarrierNumber);

        mEditTextCodeReceived = findViewById(R.id.editText_codeReceived);
        mEditTextDisplayName = findViewById(R.id.editText_displayName);

        mEditTextDisplayName.addTextChangedListener(textWatcher);

        mPhoneLayout = findViewById(R.id.phone_layout);
        mNameLayout = findViewById(R.id.nameLayout);

        mEditTextError = findViewById(R.id.editText_error);
      //  mWelcomeText = findViewById(R.id.welcome_text);

        mSendButton = findViewById(R.id.send_verification_btn);
        mNextButton = findViewById(R.id.next_Button);

        mLoginProgress = new ProgressDialog(this);
        mPhoneProgress = new ProgressDialog(this);
        mCodeProgress = new ProgressDialog(this);

        //
        mNextButton.setOnClickListener(view -> {
            if(TextUtils.isEmpty(mEditTextDisplayName.getText().toString().trim())){
                Toast.makeText(LoginActivity.this, R.string.enter_a_name,
                        Toast.LENGTH_SHORT).show();
            }else{

                mNameLayout.setVisibility(View.GONE);
              //  mWelcomeText.setText(getString(R.string.main_text));
                mPhoneLayout.setVisibility(View.VISIBLE);
                mSendButton.setVisibility(View.VISIBLE);
            }
        });


        // Verification purposes
        mSendButton.setOnClickListener(view -> {

            if (TextUtils.isEmpty(mEditTextCarrierNumber.getText().toString().trim())) {
                Toast.makeText(LoginActivity.this, R.string.enter_phone, Toast.LENGTH_SHORT).show();
            }else{
            if (isZero) {
                if (mCountryCodePicker.isValidFullNumber()) {

                    // Get the full number entered
                    String phoneFullNumber = mCountryCodePicker.getFullNumberWithPlus();

                    AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
                    alert.setIcon(R.drawable.ic_about);
                    alert.setTitle(R.string.verify_phone);
                    alert.setMessage(getString(R.string.verify) + " "+ phoneFullNumber  +
                          " " +  getString(R.string.y_phone));

                    alert.setPositiveButton(R.string.ok, (dial, id) -> {

                        mPhoneProgress.setTitle(getString(R.string.code_sending));
                        mPhoneProgress.setMessage(getString(R.string.code_generation));
                        mPhoneProgress.setCanceledOnTouchOutside(false);
                        mPhoneProgress.show();

                        mCountryCodePicker.setCcpClickable(false);
                        mEditTextCarrierNumber.setEnabled(false);
                        mSendButton.setEnabled(false);
                        mEditTextDisplayName.setEnabled(false);

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneFullNumber,
                                90,
                                TimeUnit.SECONDS,
                                LoginActivity.this,
                                mCallbacks
                        );
                    });

                    alert.setNegativeButton(R.string.cancel, (dial, id) ->{

                    });

                    AlertDialog dialog = alert.create();
                    dialog.show();

                } else
                    Toast.makeText(LoginActivity.this, R.string.p_not_valid, Toast.LENGTH_SHORT).show();
            } else {

                mSendButton.setEnabled(false);

                mCodeProgress.setTitle(R.string.verify_code);
                mCodeProgress.setMessage(getString(R.string.code_verifying));
                mCodeProgress.setCanceledOnTouchOutside(false);
                mCodeProgress.show();

                mEditTextCodeReceived.setEnabled(false);

                // Retrieve code entered by user
                String valueEntered = mEditTextCodeReceived.getText().toString().trim();

                mLoginProgress.setTitle(getString(R.string.re_user));
                mLoginProgress.setMessage(getString(R.string.w_user));
                mLoginProgress.setCanceledOnTouchOutside(false);
                mLoginProgress.show();

                if (valueEntered.length() > 0) {
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, valueEntered);
                    signInWithPhoneAuthCredential(credential);
                } else {
                    Toast.makeText(LoginActivity.this, R.string.en_co_rec,
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                mEditTextError.setText(R.string.error_text);
                mEditTextError.setVisibility(View.VISIBLE);

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                //  qsuper.onCodeSent(verificationId, token);
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                 Log.d("Code", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                //mResendToken = token;

                isZero = false;

                // Stop the loading process

                mPhoneProgress.dismiss();

                // Creating a place for the user to enter the code
                mEditTextCodeReceived.setVisibility(View.VISIBLE);

                // Changing text of button
                mSendButton.setText(R.string.verify_code);

                mSendButton.setEnabled(true);

                // ...
            }

        };

    }


    /***
     * Added for video call purposes
     */

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information

                        FirebaseUser user = task.getResult().getUser();

                        // Writing additional values to Realtime Database
                        String currentUserPhone = mCountryCodePicker.getFullNumberWithPlus();

                        // Pointing to root and created a child 'Users' and a sub-child with user's uid
                        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("ads_users").child(currentUserPhone);

                        String tokenID = FirebaseInstanceId.getInstance().getToken();

                        //TODO: check in local database for user_id. If user_id blank, then issue an archive command in firebase.

                        HashMap<String, Object> users_upload = new HashMap<>();
                        users_upload.put("name", mEditTextDisplayName.getText().toString().trim());
                        users_upload.put("status", "Happy to be using ADS Messenger");
                        users_upload.put("image", "Default");
                        users_upload.put("phone", currentUserPhone);
                        users_upload.put("thumbnail", "Default");
                        users_upload.put("uid", user.getUid());
                        users_upload.put("deviceToken", tokenID);



                        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild("conversation")){
                                    mDatabaseReference.child("conversation").addChildEventListener(new ChildEventListener() {
                                        @Override
                                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                            Conversation c = dataSnapshot.getValue(Conversation.class);
                                            if(c == null){
                                                return;
                                            }

                                            if(c.getType().equals("channel")){
                                                mChannelReference.child(c.getId()).child("locked").setValue("no");
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
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mDatabaseReference.updateChildren(users_upload).addOnCompleteListener(userCompletion -> {
                            if (userCompletion.isSuccessful()) {

                                mLoginProgress.dismiss();
                                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                        });
                        // ...
                    } else {
                        // Sign in failed, display a message and update the UI

                        mEditTextError.setText(R.string.failed_sign_in);
                        mEditTextError.setVisibility(View.VISIBLE);


                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            Toast.makeText(LoginActivity.this, R.string.co_en_invalid, Toast.LENGTH_SHORT).show();
                            mEditTextDisplayName.setText("");
                            mEditTextDisplayName.setEnabled(true);
                            mEditTextCarrierNumber.setText("");
                            mEditTextCarrierNumber.setEnabled(true);
                            mSendButton.setEnabled(true);
                        }
                    }
                });
    }

    private TextWatcher textWatcher = new TextWatcher(){

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            if(mEditTextDisplayName.getText().toString().trim().length() >= 4 && mEditTextDisplayName.getText().toString().trim().length() < 10 ){
                mNextButton.show();//setVisibility(View.VISIBLE);
            }else{
                mNextButton.hide();//setVisibility(View.GONE);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {

        }
    };

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
