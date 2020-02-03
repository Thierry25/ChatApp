package marcelin.thierry.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiTextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.utils.CheckInternet_;

public class CommentActivity extends AppCompatActivity {

    private CircleImageView mChannelImage;
    private EmojiTextView mChannelName, textEntered;
    private EmojiEditText mSendText;
    private TextView mTimestamp;
    private ImageView mMoreSettings;
    private LinearLayout messageLayout;

    private ImageButton mSendEmoji, mSend;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String myPhone;

    private DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference().child("ads_channel");
    private DatabaseReference mCommentReference = FirebaseDatabase.getInstance().getReference().child("c");
    private boolean isShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        mChannelImage = findViewById(R.id.channel_image);
        mChannelName = findViewById(R.id.channel_name);
        mTimestamp = findViewById(R.id.timestamp);
        mMoreSettings = findViewById(R.id.more_settings);
        textEntered = findViewById(R.id.textEntered);
        messageLayout = findViewById(R.id.messageLayout);
        mSendEmoji = findViewById(R.id.send_emoji);
        mSendText = findViewById(R.id.send_text);
        mSend = findViewById(R.id.send);
        myPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        String channelName = getIntent().getStringExtra("channel_name");
        String channelImage = getIntent().getStringExtra("channel_image");
        String messageType = getIntent().getStringExtra("message_type");
        String messageId = getIntent().getStringExtra("message_id");
        String messageContent = getIntent().getStringExtra("message_content");
        String color = getIntent().getStringExtra("message_color");
        long timestamp = getIntent().getLongExtra("message_timestamp", 0);

        String textToSend = mSendText.getText().toString();


        Map<String, Object> commentContentMap = new HashMap<>();
        commentContentMap.put("color", "#000000");
        commentContentMap.put("content", textToSend);
        commentContentMap.put("from", myPhone);
        commentContentMap.put("parent", "Default");
        commentContentMap.put("timestamp", ServerValue.TIMESTAMP);

        switch (messageType){

            case "text":
                mChannelName.setText(channelName);
                Picasso.get().load(channelImage).into(mChannelImage);
                mMoreSettings.setVisibility(View.GONE);
                mTimestamp.setText(getDate(timestamp));

                if(color.equals("#7016a8") || color.equals("#FFFFFF")){
                    textEntered.setTextColor(Color.parseColor("#000000"));
                    messageLayout.setGravity(Gravity.START);
                }

                if(!color.equals("#7016a8") && !color.equals("#FFFFFF") && messageContent.length() < 150){
                    messageLayout.setBackgroundColor(Color.parseColor(color));
                    ViewGroup.LayoutParams params = messageLayout.getLayoutParams();
                    params.height = 500;
                    messageLayout.setLayoutParams(params);
                    textEntered.setTextColor(Color.parseColor("#FFFFFF"));
                }
                textEntered.setText(messageContent);

                mSend.setOnClickListener(v ->{
                    new CheckInternet_(internet -> {
                       if(internet){
                           String message = mSendText.getText().toString().trim();

                           if (!TextUtils.isEmpty(message)) {
                               DatabaseReference msg_push = mChannelReference.push();
                               String push_id = msg_push.getKey();
                               Map<String, Object> commentMap = new HashMap<>();
                               commentMap.put(push_id, commentContentMap);

                               mCommentReference.updateChildren(commentMap, (databaseError, databaseReference) -> {
                               });

                               Map<String, Object> commentLink = new HashMap<>();
                               commentLink.put("commentID", push_id);
                               commentLink.put("timestamp", ServerValue.TIMESTAMP);

                               mChannelReference.child(channelName).child("messages").child(messageId).child("c").child(push_id)
                                       .updateChildren(commentLink, (databaseError, databaseReference) -> {
                                       });
                               mSendText.setText("");
                           }else{
                               Toast.makeText(this, "Please enter a comment before sending", Toast.LENGTH_SHORT).show();
                           }
                       }else{
                           Toast.makeText(this, R.string.no_internet_error, Toast.LENGTH_SHORT).show();
                       }
                    });

                });

                break;

            case "image":

        }

    }
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        try {
            cal.setTimeInMillis(time);
            return DateFormat.format("MMM dd, hh:mm a", cal).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
