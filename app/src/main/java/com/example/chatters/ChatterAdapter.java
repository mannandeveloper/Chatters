package com.example.chatters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class ChatterAdapter extends FragmentPagerAdapter
{

    public ChatterAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int i)
    {
        switch (i)
        {
            case 0 :
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;

            case 1 :
                GroupFragment groupFragment = new GroupFragment();
                return groupFragment;

            case 2 :
                ContactFragment contactFragment = new ContactFragment();
                return contactFragment;

                default:
                    return null;
        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0 :

                return "Chats";

            case 1 :

                return "Groups";

            case 2 :

                return "Contacts";

            default:
                return null;
        }

    }
}
