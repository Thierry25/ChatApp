package marcelin.thierry.chatapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.vanniktech.emoji.EmojiPopup;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import marcelin.thierry.chatapp.R;

public class AddTextActivity extends AppCompatActivity {

  private int counter = 0;
  private RelativeLayout mRelLayout;
  private EditText mTextAdded;

  private ImageView mChangeColor, mSmiley, mImageUpload;
  private FloatingActionButton mSendStatus;

  private static final DatabaseReference mStatusReference = FirebaseDatabase.getInstance()
          .getReference().child("ads_status");
  private FirebaseAuth mAuth;
  EmojiPopup emojiPopup;


  private String mCurrentUserPhone;
  private ProgressDialog mProgressDialog;

  private final String[] WALK_THROUGH = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};
  private static final StorageReference mImagesStorage = FirebaseStorage.getInstance().getReference();

  private final int[] resources = new int[]{

          R.color.colorDarkGreen,
          R.color.colorGray,
          R.color.colorPink,
          R.color.colorOrange,
          R.color.colorBlack,
          R.color.colorPurple,
          R.color.colorRed,
          R.color.colorTeal,
          R.color.colorBrown,
          R.color.colorPinkFade,
          R.color.colorLightPurple,
          R.color.colorLDarkBlue,
          R.color.colorRedLight

  };

  private final String[] resStrings = new String[]{
          "#00574B",
          "#808080",
          "#D81B60",
          "#FFA500",
          "#000000",
          "#4B0082",
          "#B22222",
          "#008080",
          "#e5b895",
          "#ffaabb",
          "#9b8dc2",
          "#6a7a94",
          "#ee134a"
  };

//    private final String[] fontStrings = new String[]{
//
//            "sans-serif-thin",
//            "sans-serif-light",
//            "sans-serif",
//            "sans-serif-medium",
//            "sans-serif-black",
//            "sans-serif-condensed-light",
//            "sans-serif-condensed",
//            "sans-serif-condensed-medium",
//            "serif",
//            "monospace",
//            "serif-monospace",
//            "casual",
//            "cursive",
//            "sans-serif-smallcaps"
//
//    };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loadLocale();
    setContentView(R.layout.activity_add_text);

    mRelLayout = findViewById(R.id.rel_layout);
    mTextAdded = findViewById(R.id.textAdded);
    mChangeColor = findViewById(R.id.changeColor);
    mSmiley = findViewById(R.id.smiley);

    mSendStatus = findViewById(R.id.sendStatus);
    mImageUpload = findViewById(R.id.imageToUpload);

    mAuth = FirebaseAuth.getInstance();
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.stt_upload_msg));

    mCurrentUserPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

    Map<String, Object> seenBy = new HashMap<>();
    seenBy.put(mCurrentUserPhone, ServerValue.TIMESTAMP);

    mChangeColor.setOnClickListener(view -> {
      counter += 1;
      if (counter > 12) {
        counter = 0;
      }
      mRelLayout.setBackgroundResource(resources[counter]);

    });

    mSendStatus.setOnClickListener(view -> {
      mProgressDialog.show();
      mImageUpload.setVisibility(View.VISIBLE);
      String text = mTextAdded.getText().toString();
      int width = mTextAdded.getWidth();

      //background color red
      int color = Color.parseColor(resStrings[counter]);
      Bitmap img = drawText(text, width, color);
      // Send to database

      DatabaseReference statusPush = mStatusReference.push();
      String pushId = statusPush.getKey();

      StorageReference filePath = mImagesStorage.child("ads_status_images")
              .child(pushId + ".jpg");
      Uri statusUpload = getImageUri(this, img);
      filePath.putFile(statusUpload).addOnCompleteListener(task -> {

        if(task.isSuccessful()){
          String downloadUrl = Objects.requireNonNull(task.getResult()
                  .getDownloadUrl()).toString();

          Map<String, Object> statusMap = new HashMap<>();

          statusMap.put("seenBy", seenBy);
          statusMap.put("content", downloadUrl);
          statusMap.put("timestamp", ServerValue.TIMESTAMP);
          statusMap.put("phoneNumber", mCurrentUserPhone);
          statusMap.put("id", pushId);
          statusMap.put("from", "text");
          statusMap.put("textEntered", text);

          mStatusReference.child(mCurrentUserPhone).child("s").child(pushId).updateChildren(statusMap).addOnCompleteListener(task1 -> {
            if(task.isSuccessful()){
              finish();
            }
          });

          mProgressDialog.dismiss();
        }

      });

//            mImageUpload.setImageBitmap(img);

    });

//        mSmiley.setColorFilter(ContextCompat.getColor(this, R.color.emoji_icons), PorterDuff.Mode.SRC_IN);
//        mSmiley.setOnClickListener(ignore -> emojiPopup.toggle());

  }

  public Bitmap drawText(String text, int textWidth  , int color) {

    Rect bounds = new Rect();

    // Get text dimensions
    TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
    textPaint.setStyle(Paint.Style.FILL);
    textPaint.setColor(Color.parseColor("#FFFFFF"));
    //  textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
    textPaint.setTextSize(70);

    StaticLayout mTextLayout = new StaticLayout(text, textPaint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.00f, false);

    // Create bitmap and canvas to draw toq
    Bitmap b = Bitmap.createBitmap(textWidth, 1820, Bitmap.Config.ARGB_4444);
    Canvas c = new Canvas(b);

    // Draw background
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.LINEAR_TEXT_FLAG);
    paint.setStyle(Paint.Style.FILL);

    // Just added
    paint.setTextAlign(Paint.Align.CENTER);
    // paint.getTextBounds(text, 0, text.length(), bounds);


    paint.setColor(color);
    c.drawPaint(paint);

    // Draw text
    c.save();
    //  c.translate(0, 0);
    // c.translate(0, 0);

    //int x = (b.getWidth() - bounds.width()) / 2;
    int y = (b.getHeight() + bounds.height()) / 2;

    c.translate(0, y);
    mTextLayout.draw(c);
    c.restore();

    return b;
  }

  public Uri getImageUri(Context inContext, Bitmap inImage) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
    String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
    return Uri.parse(path);
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
