package marcelin.thierry.chatapp.fragments;


import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
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
import marcelin.thierry.chatapp.activities.GroupSharedActivity;
import marcelin.thierry.chatapp.adapters.ImagesAdapter;
import marcelin.thierry.chatapp.classes.Messages;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupImageFragment extends Fragment {

    private String mChatId;
    private View mMainView;
    private static final int NUMBER_OF_COLUMNS = 3;

    private static final DatabaseReference mChatReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_group");
    private static final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance().getReference()
            .child("ads_group_messages");

    private List<Messages> mMessagesList = new ArrayList<>();

    private RecyclerView mImagesList;

    private ImagesAdapter mImagesAdapter;

    private TextView mMessageEmpty;

    public GroupImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Context context = inflater.getContext();
        mMainView = inflater.inflate(R.layout.fragment_image, container, false);
        mImagesList = mMainView.findViewById(R.id.imagesList);

        mImagesAdapter = new ImagesAdapter(mMessagesList);
        mImagesList.setAdapter(mImagesAdapter);

        mMessageEmpty = mMainView.findViewById(R.id.message_empty);

        GroupSharedActivity activity = (GroupSharedActivity) getActivity();

        Bundle results = Objects.requireNonNull(activity).getMyData();
        mChatId = results.getString("chat_id");

        getImage();
        mImagesList.setLayoutManager(new GridLayoutManager(context
                , NUMBER_OF_COLUMNS));

        return mMainView;
    }


    public void getImage(){

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
                        if(m.getType().equals("image")){

                            mMessageEmpty.setVisibility(View.GONE);
                            mMessagesList.add(m);
                            mImagesAdapter.notifyDataSetChanged();

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
