package marcelin.thierry.chatapp.fragments;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.activities.ChannelSharedActivity;
import marcelin.thierry.chatapp.adapters.AudiosAdapter;
import marcelin.thierry.chatapp.classes.Messages;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChannelAudioFragment extends Fragment {

    private String mChatId;
    private View mMainView;

    private RecyclerView mAudioList;

    private AudiosAdapter mAudiosAdapter;

    private List<Messages> mAudioMessagesList = new ArrayList<>();

    private TextView mMessageEmpty;

    private final static DatabaseReference mChatReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");

    private final static DatabaseReference mMessagesReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel_messages");

    public ChannelAudioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mMainView = inflater.inflate(R.layout.fragment_audio, container, false);

        mAudioList = mMainView.findViewById(R.id.audiosList);
        mMessageEmpty = mMainView.findViewById(R.id.message_empty);

        ChannelSharedActivity activity = (ChannelSharedActivity) getActivity();

        Bundle results = Objects.requireNonNull(activity).getMyData();
        mChatId = results.getString("chat_id");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mAudiosAdapter = new AudiosAdapter(mAudioMessagesList);
        mAudioList.setAdapter(mAudiosAdapter);

        mAudioList.setHasFixedSize(true);
        mAudioList.setLayoutManager(linearLayoutManager);

        getAudio();

        return mMainView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudiosAdapter.stopMediaPlayers();
        //listenerOnMessage() detachListener

    }

    private void getAudio() {

        mAudioMessagesList.clear();

        mChatReference.child(mChatId).child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                mMessagesReference.child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Messages m = dataSnapshot.getValue(Messages.class);
                        if(m == null){
                            return;
                        }
                        if(m.getType().equals("audio")){

                            mMessageEmpty.setVisibility(View.GONE);
                            mAudioMessagesList.add(m);
                            mAudiosAdapter.notifyDataSetChanged();

                        }

                        if((mAudioMessagesList.size()) == 0){
                            mMessageEmpty.setVisibility(View.VISIBLE);
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
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

    }


}
