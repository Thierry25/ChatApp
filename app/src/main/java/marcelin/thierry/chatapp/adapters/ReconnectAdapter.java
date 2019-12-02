package marcelin.thierry.chatapp.adapters;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bestsoft32.tt_fancy_gif_dialog_lib.TTFancyGifDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.hbb20.CountryCodePicker;
import com.squareup.picasso.Picasso;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.classes.Channel;

public class ReconnectAdapter extends RecyclerView.Adapter<ReconnectAdapter.UserViewHolder> {

    private List<Channel> mFoundChannel;
    private Activity mContext;

    private Dialog mDialog;

    private static final DatabaseReference mUsersReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_users");

    private static final DatabaseReference mChannelReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");

    public ReconnectAdapter(List<Channel> mFoundChannel, Activity mContext) {
        this.mFoundChannel = mFoundChannel;
        this.mContext = mContext;
    }

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    @NonNull
    @Override
    public ReconnectAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.found_channel_layout, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReconnectAdapter.UserViewHolder holder, int position) {

        Channel currentChannel = mFoundChannel.get(position);

        String phone = Objects.requireNonNull(mAuth.getCurrentUser()).getPhoneNumber();

        holder.setProfilePic(currentChannel.getThumbnail());
        holder.channelSubscribers.setText(MessageFormat.format("{0}{1}", currentChannel.getSubscribers().size(), mContext.getString(R.string.subscribers)));
        holder.channelName.setText(currentChannel.getName());


        holder.channelLayout.setOnClickListener(view -> {

            new TTFancyGifDialog.Builder(mContext)
                    .isCancellable(true)
                    .setTitle(mContext.getString(R.string.chan_recon))
                    .setGifResource(R.drawable.gif10)
                    .setMessage(mContext.getString(R.string.chan_rec_msg))

                    .OnPositiveClicked(() ->{

                        CountryCodePicker ccp;
                        EditText enteredPhone;
                        EditText enteredPassword;
                        FloatingActionButton nextButton;

                        Button recoverButton;

                        mDialog = new Dialog(mContext, android.R.style.Theme_Translucent_NoTitleBar);
                        mDialog.setContentView(R.layout.reconnect_layout);
                        mDialog.show();

                        ccp = mDialog.findViewById(R.id.ccp);
                        enteredPhone = mDialog.findViewById(R.id.enteredPhone);
                        nextButton = mDialog.findViewById(R.id.nextButton);
                        recoverButton = mDialog.findViewById(R.id.recoverButton);
                        enteredPassword = mDialog.findViewById(R.id.enteredPassword);

                        ccp.registerCarrierNumberEditText(enteredPhone);

                        nextButton.setOnClickListener(view1 ->{
                            String phoneFullNumber = ccp.getFullNumberWithPlus();
                            if(ccp.isValidFullNumber() && !phoneFullNumber.equals(phone) &&
                                    currentChannel.getAdmins().containsKey(phoneFullNumber)) {
                                // goToNext Screen
                                ccp.setVisibility(View.GONE);
                                enteredPhone.setVisibility(View.GONE);
                                nextButton.setVisibility(View.GONE);

                                enteredPassword.setVisibility(View.VISIBLE);
                                recoverButton.setVisibility(View.VISIBLE);

                                recoverButton.setOnClickListener(view2 ->{

                                    if(!TextUtils.isEmpty(enteredPassword.getText().toString().trim())
                                        && enteredPassword.getText().toString().trim().equals(currentChannel.getPassword())){

                                        ProgressDialog progressDialog = new ProgressDialog(mContext);
                                        progressDialog.setTitle(mContext.getString(R.string.rec_to_chan));
                                        progressDialog.setMessage(mContext.getString(R.string.wait_chan__));
                                        progressDialog.show();

                                        for(String s : currentChannel.getAdmins().keySet()){
                                            mUsersReference.child(s).child("conversation").child("C-"+currentChannel.getName()).removeValue();
                                        }

                                        Map<String, Object> m = new HashMap<>();
                                        m.put("id", currentChannel.getName());
                                        m.put("phone_number", "");
                                        m.put("timestamp", ServerValue.TIMESTAMP);
                                        m.put("type", "channel");
                                        m.put("visible", true);

                                        Map<String, Object> map = new HashMap<>();
                                        map.put(phone, ServerValue.TIMESTAMP);

                                        mChannelReference.child(currentChannel.getName()).child("admins").setValue(map);

                                        if (phone != null) {
                                            mUsersReference.child(phone).child("conversation").child("C-"+currentChannel.getName())
                                                    .updateChildren(m).addOnCompleteListener(task -> {

                                                        if(task.isSuccessful()){
                                                            Toast.makeText(mContext, R.string.rest_suc, Toast.LENGTH_SHORT).show();
                                                            progressDialog.dismiss();
                                                            mDialog.dismiss();
                                                        }

                                                    });
                                        }
                                    }else{
                                        if(TextUtils.isEmpty(enteredPassword.getText().toString().trim())){
                                            Toast.makeText(mContext, R.string.empty_field_err_, Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(mContext, R.string.pass_no_match, Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                });

                            }else{
                                if(!ccp.isValidFullNumber()){
                                    Toast.makeText(mContext, R.string.inv_ph_, Toast.LENGTH_SHORT).show();
                                }else if (phone != null) {
                                    if(phone.equals(phoneFullNumber) && currentChannel.getAdmins().containsKey(phone)){
                                        Toast.makeText(mContext, R.string.chan_un_name, Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Toast.makeText(mContext, R.string.ch_er___, Toast.LENGTH_SHORT).show();
                                }
                            }

                        });

                    })

                    .OnNegativeClicked(() ->{

                    })
                    .build();

        });
    }

    @Override
    public int getItemCount() {
        return mFoundChannel.size();
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        LinearLayout channelLayout;
        CircleImageView channelImage;
        TextView channelName;
        TextView channelSubscribers;

        public UserViewHolder(View itemView) {
            super(itemView);

            channelLayout = itemView.findViewById(R.id.channelLayout);
            channelImage = itemView.findViewById(R.id.channelImage);
            channelName = itemView.findViewById(R.id.channel_name);
            channelSubscribers = itemView.findViewById(R.id.channel_subscribers);

        }

        public void setProfilePic(String thumbnail){

            channelImage = itemView.findViewById(R.id.channelImage);

            Picasso.get().load(thumbnail).placeholder(R.drawable.ic_avatar).into(channelImage);

        }
    }
}
