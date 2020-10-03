package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.fragments.ChannelAudioFragment;
import marcelin.thierry.chatapp.fragments.ChannelDocumentFragment;
import marcelin.thierry.chatapp.fragments.ChannelImageFragment;
import marcelin.thierry.chatapp.fragments.ChannelVideoFragment;


public class SharedChannelAdapter extends FragmentPagerAdapter {

    private Context mContext;
    public SharedChannelAdapter(FragmentManager fm, Context mContext) {
        super(fm);
        this.mContext = mContext;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ChannelImageFragment();
            case 1:
                return new ChannelAudioFragment();
            case 2:
                return new ChannelVideoFragment();
            case 3:
                return new ChannelDocumentFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 4 ; // Count the number of tabs
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.im);
            case 1:
                return mContext.getResources().getString(R.string.au);
            case 2:
                return mContext.getResources().getString(R.string.vi);
            case 3:
                return mContext.getResources().getString(R.string.d_);
            default:
                return null;
        }
    }
}