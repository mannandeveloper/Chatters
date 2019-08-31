package com.example.chatters;


import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment
{
    private View groupFragmentView;
    private ListView groupListView;
    private ArrayAdapter<String> groupArrayAdapter;
    private ArrayList<String> listOfGroup = new ArrayList<>();
    private DatabaseReference groupDatabaseReference;


    public GroupFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        groupFragmentView =  inflater.inflate(R.layout.fragment_group, container, false);
        groupDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Groups");

        initializedFields();

        retrieveAndDisplayGroups();

       groupListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
       {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id)
           {
                String currentGroupName = parent.getItemAtPosition(position).toString();
               Intent groupChatIntent = new Intent(getContext() , GroupChatActivity.class);
               groupChatIntent.putExtra("Group Name" , currentGroupName);
               startActivity(groupChatIntent);
           }
       });

        return groupFragmentView;
    }


    private void initializedFields()
    {
        groupListView = groupFragmentView.findViewById(R.id.group_list_view);
        groupArrayAdapter = new ArrayAdapter<String>(getContext() , android.R.layout.simple_expandable_list_item_1 , listOfGroup);
        groupListView.setAdapter(groupArrayAdapter);
    }


    private void retrieveAndDisplayGroups()
    {
        groupDatabaseReference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while (iterator.hasNext())
                {
                    set.add(((DataSnapshot)iterator.next()).getKey());

                    listOfGroup.clear();
                    listOfGroup.addAll(set);
                    groupArrayAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

}
