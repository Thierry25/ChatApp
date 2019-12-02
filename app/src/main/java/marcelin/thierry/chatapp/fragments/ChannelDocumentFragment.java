package marcelin.thierry.chatapp.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import marcelin.thierry.chatapp.adapters.DocsAdapter;
import marcelin.thierry.chatapp.classes.Messages;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChannelDocumentFragment extends Fragment {

    private String mChatId;
    private View mMainView;

    private static final DatabaseReference mChatReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel");
    private static final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_channel_messages");

    private List<Messages> mMessagesList = new ArrayList<>();

    private RecyclerView mDocsList;

    private DocsAdapter mDocsAdapter;

    private TextView mMessageEmpty;

    public ChannelDocumentFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Context context = inflater.getContext();
        mMainView = inflater.inflate(R.layout.fragment_document, container, false);
        mDocsList = mMainView.findViewById(R.id.docsList);

        mMessageEmpty = mMainView.findViewById(R.id.message_empty);

        ChannelSharedActivity activity = (ChannelSharedActivity) getActivity();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);


        Bundle results = Objects.requireNonNull(activity).getMyData();
        mChatId = results.getString("chat_id");

        mDocsAdapter = new DocsAdapter(mMessagesList, getContext());
        mDocsList.setAdapter(mDocsAdapter);

        getDocument();
        mDocsList.setLayoutManager(linearLayoutManager);

        return mMainView;
    }


    public void getDocument(){

        mMessagesList.clear();


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
                        if(m.getType().equals("document")){

                            mMessageEmpty.setVisibility(View.GONE);
                            mMessagesList.add(m);
                            mDocsAdapter.notifyDataSetChanged();

                        }

                        if((mMessagesList.size()) == 0){
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
