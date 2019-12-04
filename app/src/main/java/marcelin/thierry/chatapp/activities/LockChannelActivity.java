package marcelin.thierry.chatapp.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import marcelin.thierry.chatapp.R;

public class LockChannelActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private LinearLayout mPasswordLayout;
    private LinearLayout mRecoverLayout;
    private LinearLayout mAnswerLayout;
    private LinearLayout mEmailLayout;

    private EditText mPass1;
    private EditText mPass2;
    private EditText mQuestionAnswered;
    private EditText mEmailEdit;

    private TextView mSecurityText;
    private TextView mEmailText;

    private Button mBtnNext;
    private Button mQuestButton;
    private Button mEmailButton;

    private Spinner mSecuritySpinner;

    private ImageView mSeen1;
    private ImageView mSeen2;

    private String mItemSelected;

    private String mChannelId;

    private ProgressDialog mLockDialog;
    private ProgressDialog mEmailDialog;

    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_lock_channel);

        mChannelId = getIntent().getStringExtra("Channel_id");

        mPasswordLayout = findViewById(R.id.passwordLayout);
        mRecoverLayout = findViewById(R.id.recoverPasswordLayout);
        mAnswerLayout = findViewById(R.id.answerLayout);
        mEmailLayout = findViewById(R.id.emailLayout);

        mPass1 = findViewById(R.id.pass1);
        mPass2 = findViewById(R.id.pass2);
        mQuestionAnswered = findViewById(R.id.questionAnswered);
        mEmailEdit = findViewById(R.id.emailEdit);

        mSecurityText = findViewById(R.id.security_text);
        mEmailText = findViewById(R.id.email_text);

        mBtnNext = findViewById(R.id.btn_next);
        mQuestButton = findViewById(R.id.questButton);
        mEmailButton = findViewById(R.id.emailButton);

        mSecuritySpinner = findViewById(R.id.security_spinner);
        mSecuritySpinner.setOnItemSelectedListener(this);

        mSeen1 = findViewById(R.id.seen1);
        mSeen2 = findViewById(R.id.seen2);

        mSeen1.setOnClickListener(view -> {
//
            if(mPass1.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                mPass1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mPass1.setSelection(mPass1.getSelectionStart(), mPass1.getSelectionEnd());
            }else{
                mPass1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                mPass1.setSelection(mPass1.getSelectionStart(), mPass1.getSelectionEnd());
            }

           // mEtPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        });

        mSeen2.setOnClickListener(view -> {
            if(mPass2.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){
                mPass2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mPass2.setSelection(mPass2.getSelectionStart(), mPass2.getSelectionEnd());
            }else{
                mPass2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                mPass2.setSelection(mPass2.getSelectionStart(), mPass2.getSelectionEnd());
            }
        });

        mBtnNext.setOnClickListener(view -> {
            if(mPass1.getText().toString().trim().length() >= 4 &&
                    mPass1.getText().toString().trim().equals(mPass2.getText().toString().trim())){

                mPasswordLayout.setVisibility(View.GONE);

                mRecoverLayout.setVisibility(View.VISIBLE);

                mSecurityText.setOnClickListener(view1 -> {
                    mRecoverLayout.setVisibility(View.GONE);
                    mAnswerLayout.setVisibility(View.VISIBLE);

                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                            R.array.questions_array, android.R.layout.simple_spinner_item);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    mSecuritySpinner.setAdapter(adapter);
                    mQuestButton.setOnClickListener(view2 -> {
                        if(!TextUtils.isEmpty(mQuestionAnswered.getText().toString().trim())){

                            new TTFancyGifDialog.Builder(LockChannelActivity.this)
                                    .isCancellable(false)
                                    .setTitle(getString(R.string.channel_lock))
                                    .setGifResource(R.drawable.gif16)
                                    .setMessage(getString(R.string.lock_m_1) + " "+
                                            mPass1.getText().toString() + " " + getString(R.string.lock_m_2) +
                                            " " + mItemSelected + " " +getString(R.string.lock_m_3))

                                    .OnPositiveClicked(() ->{

                                        mLockDialog = new ProgressDialog(this);
                                        mLockDialog.setTitle(getString(R.string.chan_locking));
                                        mLockDialog.setMessage(getString(R.string.chan_lockin_msg));
                                        mLockDialog.show();

                                        Map<String, Object> m = new HashMap<>();

                                        m.put("password", mPass1.getText().toString().trim());
                                        m.put("question", mItemSelected);
                                        m.put("locked", "yes");
                                        m.put("answer", mQuestionAnswered.getText().toString().trim().toLowerCase());

                                        mChannelReference.child(mChannelId).updateChildren(m).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                mLockDialog.dismiss();

                                                Intent goToAdminChat = new Intent(LockChannelActivity.this, ChannelAdminChatActivity.class);
                                                goToAdminChat.putExtra("Channel_id", mChannelId);
                                                goToAdminChat.putExtra("chat_id", mChannelId);
                                                startActivity(goToAdminChat);
                                                finish();
                                            }
                                        });

                                    })
                                    .OnNegativeClicked(() ->{

                                    })
                                    .build();

                        }else{
                            Toast.makeText(this, R.string.quest_answer,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                });

                mEmailText.setOnClickListener(view1 -> {
                    mRecoverLayout.setVisibility(View.GONE);
                    mEmailLayout.setVisibility(View.VISIBLE);

                    mEmailButton.setOnClickListener(view2 -> {
                        if(isValidEmail(mEmailEdit.getText().toString().trim())){

                            new TTFancyGifDialog.Builder(LockChannelActivity.this)
                                .setGifResource(R.drawable.gif16)
                                .setTitle(getString(R.string.email_entered))
                                    .setMessage(getString(R.string.su)
                                            + " " + mEmailEdit.getText().toString().trim() + " " +
                                            getString(R.string.recover))

                                    .OnPositiveClicked(() -> {

                                        mEmailDialog = new ProgressDialog(this);
                                        mEmailDialog.setTitle(getString(R.string.email_registration));
                                        mEmailDialog.setMessage(getString(R.string.sv_info));
                                        mEmailDialog.show();

                                        Map<String, Object> m = new HashMap<>();
                                        m.put("password", mPass1.getText().toString().trim());
                                        m.put("email", mEmailEdit.getText().toString().trim());
                                        m.put("locked", "yes");

                                        mChannelReference.child(mChannelId).updateChildren(m).addOnCompleteListener(task -> {
                                            if(task.isSuccessful()){
                                                mEmailDialog.dismiss();
                                                Intent goToAdminChat = new Intent(LockChannelActivity.this, ChannelAdminChatActivity.class);
                                                goToAdminChat.putExtra("Channel_id", mChannelId);
                                                goToAdminChat.putExtra("chat_id", mChannelId);
                                                startActivity(goToAdminChat);
                                                finish();
                                            }
                                        });

                                    })
                                    .OnNegativeClicked(() -> {

                                    })
                                    .isCancellable(false)
                                    .build();
                        }
                    });
                });

            }else{
                if(!mPass1.getText().toString().trim().equals(mPass2.getText().toString().trim())){
                    Toast.makeText(this, R.string.err_pass, Toast.LENGTH_SHORT).show();
                }else if(mPass1.getText().toString().trim().length() < 4){
                    Toast.makeText(this, R.string.characters_error, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public final static boolean isValidEmail(CharSequence target){
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

        mItemSelected = adapterView.getItemAtPosition(pos).toString();

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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
