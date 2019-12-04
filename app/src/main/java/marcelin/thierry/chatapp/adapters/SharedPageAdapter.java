package marcelin.thierry.chatapp.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import marcelin.thierry.chatapp.R;
import marcelin.thierry.chatapp.fragments.AudioFragment;
import marcelin.thierry.chatapp.fragments.DocumentFragment;
import marcelin.thierry.chatapp.fragments.ImageFragment;
import marcelin.thierry.chatapp.fragments.VideoFragment;


public class SharedPageAdapter extends FragmentPagerAdapter {

    private Context mContext;

    public SharedPageAdapter(FragmentManager fm, Context mContext) {
        super(fm);

        this.mContext = mContext;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new ImageFragment();
            case 1:
                return new AudioFragment();
            case 2:
                return new VideoFragment();
            case 3:
                return new DocumentFragment();
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