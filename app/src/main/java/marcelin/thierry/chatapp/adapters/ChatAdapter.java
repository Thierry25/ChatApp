package marcelin.thierry.chatapp.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.ChannelAdminChatActivity;
import marcelin.thierry.chatapp.activities.ChannelSubscriberActivity;
import marcelin.thierry.chatapp.activities.ChatActivity;
import marcelin.thierry.chatapp.activities.GroupChatActivity;
import marcelin.thierry.chatapp.activities.GroupSettings;
import marcelin.thierry.chatapp.activities.OneToOneChatSettings;
import marcelin.thierry.chatapp.activities.ProfilePictureActivity;
import marcelin.thierry.chatapp.classes.Channel;
import marcelin.thierry.chatapp.classes.Conversation;


// TODO:

  public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.UserviewHolder> implements AdapterView.OnItemSelectedListener {

  private List<Conversation> mUserConv;
  private Activity mContext;
  private Dialog mDialog;
  private EditText mPasswordEntered;
  private TextView mRecoverPasswordButton;
  private Button mUnlockButton;

  private String mItemSelected;
  private  Conversation lastSelectedConv;

  private FirebaseAuth mAuth = FirebaseAuth.getInstance();
  public boolean isClickable = true;

  private static final DatabaseReference mChannelReference = FirebaseDatabase
          .getInstance().getReference().child("ads_channel");

  public ChatAdapter(List<Conversation> mUserConv, Activity mContext) {
    this.mUserConv = mUserConv;
    this.mContext = mContext;
    setHasStableIds(true);
  }

  @NonNull
  @Override
  public UserviewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_conversation,
            parent, false);
    return new UserviewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull UserviewHolder holder, final int position) {

    Conversation currentConv = mUserConv.get(position);
    lastSelectedConv = currentConv;
    String mCurrentPhone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

    holder.mContactName.setText(currentConv.getName());
    holder.setProfilePic(currentConv.getProfile_image());

    if (currentConv.getFrom().equals(mCurrentPhone) && !currentConv.getLastMessage().equals("<font color=\"#FF5733\"<b>" +
            "Message Deleted</b></font>")) {
      holder.checkMark.setVisibility(View.VISIBLE);
      if(currentConv.isSeen()){
        holder.checkMark.setImageResource(R.drawable.ic_fragment_double_check);
      }else{
        holder.checkMark.setImageResource(R.drawable.ic_fragment_check);
      }
    }else{
      holder.checkMark.setVisibility(View.GONE);
    }

    //   holder.mProfileStatus.setText(Html.fromHtml(currentConv.getLastMessage()));
    if (currentConv.getLastMessage() == null) {
      Log.i("TestingPurposes-null", currentConv.getId());
    }
    Log.i("TestingPurposes", currentConv.getLastMessage());
    switch (currentConv.getLastMessage()) {
      case  "<font color=\"#FF5733\"<b>" +
              "Message Deleted</b></font>":
      case "<font color=\"#FFA500\"<b>" +
              "Audio</b></font>":

      case "<font color=\"#7016a8\"<b>" +
              "Image</b></font>":

      case "<font color=\"#0929b1\"<b>" +
              "Video</b></font>":
      case "<font color=\"#018c06\"<b>" +
              "Channel invitation</b></font>":
      case "<font color=\"#018c06\"<b>" +
              "Group invitation</b></font>":
      case "<font color=\"#dabf0f\"<b>" +
              "Document</b></font>":

      case "<font color=\"#e74c3c\"<b>" +
              "Contact</b></font>":

        holder.mProfileStatus.setText(Html.fromHtml(currentConv.getLastMessage()));
        break;
      default:

        holder.mProfileStatus.setText(currentConv.getLastMessage());
        break;
    }
    holder.mProfileName.setText(getDate(currentConv.getMessageTimestamp()));

    if (currentConv.getUnreadMessages() == 0) {
      holder.tvNum.setVisibility(View.GONE);
    } else {
      holder.tvNum.setVisibility(View.VISIBLE);
      holder.tvNum.setText(String.valueOf(currentConv.getUnreadMessages()));
    }

    switch (currentConv.getType()) {
      case "chat":
        holder.imageViewOfChat.setVisibility(View.GONE);
        break;
      case "group":
        holder.imageViewOfChat.setVisibility(View.VISIBLE);
        holder.imageViewOfChat.setBackgroundResource(R.drawable.if_group_172474);
        break;
      default:
        holder.imageViewOfChat.setVisibility(View.VISIBLE);
        holder.imageViewOfChat.setBackgroundResource(R.drawable.if_announcement_728997);
        break;
    }

    holder.userProfilePic.setOnClickListener(view -> {

      if(!isClickable){
        return;
      }

      switch (currentConv.getType()) {

        case "chat":
          Intent goToChatInfo = new Intent(view.getContext(), OneToOneChatSettings.class);
          goToChatInfo.putExtra("user_name", currentConv.getName());
          goToChatInfo.putExtra("user_phone", currentConv.getPhone_number());
          goToChatInfo.putExtra("user_picture", currentConv.getProfile_image());
          goToChatInfo.putExtra("chat_id", currentConv.getId());
          view.getContext().startActivity(goToChatInfo);

          break;

        case "channel":

          Intent goToProfilePicture = new Intent(view.getContext(), ProfilePictureActivity.class);
          goToProfilePicture.putExtra("user_picture", currentConv.getProfile_image());
          goToProfilePicture.putExtra("user_name", currentConv.getName());
          view.getContext().startActivity(goToProfilePicture);

          break;

        case "group":
          Intent goToGroupChat = new Intent(holder.mView.getContext(), GroupSettings.class);
          goToGroupChat.putExtra("Group_name", currentConv.getId());
          goToGroupChat.putExtra("Group_image", currentConv.getProfile_image());
          goToGroupChat.putExtra("chat_id", currentConv.getName());
          holder.mView.getContext().startActivity(goToGroupChat);

          break;

        default:
          return;

      }
    });

    holder.mainCLayout.setOnClickListener(view -> {

      if(!isClickable){
        return;
      }

      switch (currentConv.getType()) {
        case "channel":
          if (currentConv.getName() != null && currentConv.getName().length() > 0) {

            mChannelReference.child(currentConv.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
              @Override
              public void onDataChange(DataSnapshot dataSnapshot) {
                Channel c = dataSnapshot.getValue(Channel.class);
                if (c == null) {
                  return;
                }

                //  Toast.makeText(view.getContext(), c.getPassword(), Toast.LENGTH_SHORT).show();

//                                if(c.getLocked().equals("yes")){
//                                    Toast.makeText(view.getContext(), "truue", Toast.LENGTH_SHORT).show();
//                                }

//                                Log.i("ChannelId", dataSnapshot.getKey());
//                                Log.i("ChannelValue", dataSnapshot.getValue().toString());

                if (c.getAdmins().containsKey(mCurrentPhone)) {
                  if(c.getLocked().equals("yes")){

                    Intent goToAdminChannelChat =
                            new Intent(view.getContext(), ChannelAdminChatActivity.class);
                    goToAdminChannelChat.putExtra("Channel_id", currentConv.getId());
                    goToAdminChannelChat.putExtra("chat_id", currentConv.getName());
                    goToAdminChannelChat.putExtra("profile_image", currentConv.getProfile_image());
                    goToAdminChannelChat.putStringArrayListExtra("admins", (ArrayList<String>)currentConv.getAdmins());
                    view.getContext().startActivity(goToAdminChannelChat);

                  }else{
                    if(c.getPassword().length() < 3){

                      Intent goToAdminChannelChat =
                              new Intent(view.getContext(), ChannelAdminChatActivity.class);
                      goToAdminChannelChat.putExtra("Channel_id", currentConv.getId());
                      goToAdminChannelChat.putExtra("chat_id", currentConv.getName());
                      goToAdminChannelChat.putExtra("profile_image", currentConv.getProfile_image());
                      goToAdminChannelChat.putStringArrayListExtra("admins", (ArrayList<String>)currentConv.getAdmins());
                      view.getContext().startActivity(goToAdminChannelChat);
                    }else {

                      // Ask for password
                      new TTFancyGifDialog.Builder(mContext)
                              .isCancellable(true)
                              .setTitle(mContext.getString(R.string.chan_lock_))
                              .setMessage(mContext.getString(R.string.enter_pass_un))
                              .setPositiveBtnText(mContext.getString(R.string.un_))
                              .setGifResource(R.drawable.gif14)
                              .OnPositiveClicked(() ->{

                                mDialog = new Dialog(mContext, android.R.style.Theme_Material_NoActionBar_TranslucentDecor);
                                mDialog.setContentView(R.layout.unlock_channel_layout);
                                mDialog.show();

                                mPasswordEntered = mDialog.findViewById(R.id.passwordEntered);
                                mRecoverPasswordButton = mDialog.findViewById(R.id.recoverPasswordButton);
                                mUnlockButton = mDialog.findViewById(R.id.unlockButton);

                                mUnlockButton.setOnClickListener(view -> {
                                  if(!TextUtils.isEmpty(mPasswordEntered.getText().toString().trim()) &&
                                          mPasswordEntered.getText().toString().equals(c.getPassword())){
                                    Toast.makeText(view.getContext(), R.string.suc_un_, Toast.LENGTH_SHORT).show();
                                    mChannelReference.child(currentConv.getId()).child("locked").setValue("yes");

                                    Intent goToAdminChannelChat =
                                            new Intent(view.getContext(), ChannelAdminChatActivity.class);
                                    goToAdminChannelChat.putExtra("Channel_id", currentConv.getId());
                                    goToAdminChannelChat.putExtra("chat_id", currentConv.getName());
                                    goToAdminChannelChat.putExtra("profile_image", currentConv.getProfile_image());
                                    goToAdminChannelChat.putStringArrayListExtra("admins", (ArrayList<String>)currentConv.getAdmins());
                                    view.getContext().startActivity(goToAdminChannelChat);
                                    mContext.finish();
                                  }else{
                                    Toast.makeText(view.getContext(), R.string.incorrect_pass, Toast.LENGTH_SHORT).show();
                                  }
                                });

                                mRecoverPasswordButton.setOnClickListener(view -> {

                                  if(c.getQuestion().length() < 1){

                                    mDialog.setContentView(R.layout.email_layout);
                                    EditText emailEntered = mDialog.findViewById(R.id.emailEntered);
                                    Button verifyButton = mDialog.findViewById(R.id.verify_btn);

                                    verifyButton.setOnClickListener(view1 -> {

                                      if(TextUtils.isEmpty(emailEntered.getText().toString().trim())){
                                        Toast.makeText(view.getContext(), R.string.ent_em__, Toast.LENGTH_SHORT).show();
                                      }else{
                                        if(c.getEmail().equals(emailEntered.getText().toString().trim())){

                                          new TTFancyGifDialog.Builder(mContext)
                                                  .isCancellable(false)
                                                  .setTitle(mContext.getString(R.string.ch__pa__))
                                                  .setMessage(mContext.getString(R.string.pass__is_) + " " + c.getPassword())
                                                  .setGifResource(R.drawable.gif19)
                                                  .OnPositiveClicked(() ->{
                                                    mDialog.dismiss();
                                                  })

                                                  .OnNegativeClicked(() ->{
                                                    mDialog.dismiss();
                                                  })
                                                  .build();

                                        }else{
                                          Toast.makeText(view.getContext(), R.string.wrong_email_, Toast.LENGTH_SHORT).show();
                                        }
                                      }

                                    });

                                  }else{
                                    // Ask for the question that locked the channel
                                    mDialog.setContentView(R.layout.question_layout);
                                    Spinner questionSpinner = mDialog.findViewById(R.id.questionSpinner);
                                    EditText questionAnswered = mDialog.findViewById(R.id.questionAnswered);
                                    Button verifyButton = mDialog.findViewById(R.id.verify_btn);

                                    questionSpinner.setOnItemSelectedListener(ChatAdapter.this);

                                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mContext,
                                            R.array.questions_array, android.R.layout.simple_spinner_item);

                                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                    questionSpinner.setAdapter(adapter);

                                    verifyButton.setOnClickListener(view2 ->{
                                      String x = questionAnswered.getText().toString().toLowerCase().trim();
                                      if(c.getQuestion().equals(mItemSelected) && c.getAnswer().equals(x)){

                                        new TTFancyGifDialog.Builder(mContext)
                                                .isCancellable(false)
                                                .setTitle(mContext.getString(R.string.ch__pa__))
                                                .setMessage(mContext.getString(R.string.pass__is_) + " " + c.getPassword())
                                                .setGifResource(R.drawable.gif19)
                                                .OnPositiveClicked(() ->{
                                                  mDialog.dismiss();
                                                })

                                                .OnNegativeClicked(() ->{
                                                  mDialog.dismiss();
                                                })
                                                .build();

                                      }else{
                                        if(!c.getQuestion().equals(mItemSelected)){
                                          Toast.makeText(view2.getContext(), R.string.wr_que__, Toast.LENGTH_SHORT).show();
                                        }
                                        if(!questionAnswered.getText().toString().trim().equals(c.getPassword())){
                                          Toast.makeText(view2.getContext(), R.string.wr_ans___, Toast.LENGTH_SHORT).show();
                                        }
                                      }

                                    });

                                  }

                                });

                              })
                              .OnNegativeClicked(() ->{

                              })
                              .build();


                    }
                  }
                } else {
                  // go to SubsAdminChat

                  Toast.makeText(view.getContext(), R.string.no_ad,
                          Toast.LENGTH_SHORT).show();
                  Intent goToSubscriberChannelChat = new Intent
                          (view.getContext(), ChannelSubscriberActivity.class);
                  goToSubscriberChannelChat.putExtra("Channel_id", currentConv.getId());
                  goToSubscriberChannelChat.putExtra("chat_id", currentConv.getName());
                  goToSubscriberChannelChat.putExtra("profile_image", currentConv.getProfile_image());
                  goToSubscriberChannelChat.putStringArrayListExtra("admins", (ArrayList<String>)currentConv.getAdmins());
                  view.getContext().startActivity(goToSubscriberChannelChat);
                }

              }

              @Override
              public void onCancelled(DatabaseError databaseError) {

              }
            });
          } else {
            Toast.makeText(view.getContext(), R.string.chan_no_,
                    Toast.LENGTH_SHORT).show();
          }


          break;
        case "group":

          if (currentConv.getId() == null || currentConv.getId().length() == 0) {

            Toast.makeText(view.getContext(), R.string.group_no, Toast.LENGTH_SHORT)
                    .show();

          } else {

            Intent goToGroupChat = new Intent(holder.mView.getContext(), GroupChatActivity.class);
            goToGroupChat.putExtra("Group_id", currentConv.getId());
            goToGroupChat.putExtra("Group_image", currentConv.getProfile_image());
            goToGroupChat.putExtra("chat_id", currentConv.getName());
            holder.mView.getContext().startActivity(goToGroupChat);
          }
          break;
        default:
          if (currentConv.getId() == null || currentConv.getId().length() == 0) {

            Toast.makeText(view.getContext(), R.string.convo_no, Toast.LENGTH_SHORT)
                    .show();

          } else {

            Intent goToChat = new Intent(holder.mView.getContext(), ChatActivity.class);
            goToChat.putExtra("user_name", currentConv.getName());
            goToChat.putExtra("user_phone", currentConv.getPhone_number());
            goToChat.putExtra("user_picture", currentConv.getProfile_image());
            goToChat.putExtra("chat_id", currentConv.getId());
            holder.mView.getContext().startActivity(goToChat);

          }

          break;
      }

    });


  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getItemViewType(int position) {
    return position;
  }

  @Override
  public int getItemCount() {
    return mUserConv.size();
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

    mItemSelected = adapterView.getItemAtPosition(position).toString();

  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }

  public Conversation getCurrent() {
    return lastSelectedConv;
  }

  public static class UserviewHolder extends RecyclerView.ViewHolder {

    private View mView;

    private TextView mContactName;
    private EmojiTextView mProfileStatus;
    private TextView mProfileName;
    private TextView tvNum;
    private TextView imageViewOfChat;

    private CircleImageView userProfilePic;

    private ConstraintLayout mainCLayout;

    private ImageView checkMark;

    public UserviewHolder(View itemView) {
      super(itemView);

      mView = itemView;

      mContactName = mView.findViewById(R.id.contactName);
      mProfileStatus = mView.findViewById(R.id.profileStatus);
      mProfileName = mView.findViewById(R.id.profileName);
      userProfilePic = mView.findViewById(R.id.profilePic);
      imageViewOfChat = mView.findViewById(R.id.imageOfChat);
      mainCLayout = mView.findViewById(R.id.mainCLayout);
      tvNum = mView.findViewById(R.id.tv_num);
      checkMark = mView.findViewById(R.id.checkMark);
    }

    public void setProfilePic(String thumbnail) {

      userProfilePic = mView.findViewById(R.id.profilePic);

      Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(userProfilePic);

    }
  }

  private String getDate(long time) {
    Calendar cal = Calendar.getInstance(Locale.ENGLISH);
    try {
      cal.setTimeInMillis(time);
      return DateFormat.format("hh:mm a", cal).toString();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void updateList(List<Conversation> newList) {
    mUserConv = new ArrayList<>();
    mUserConv.addAll(newList);
    notifyDataSetChanged();
  }
}


