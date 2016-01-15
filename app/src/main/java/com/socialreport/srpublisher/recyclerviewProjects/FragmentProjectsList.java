package com.socialreport.srpublisher.recyclerviewProjects;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.socialreport.srpublisher.DB.Project;
import com.socialreport.srpublisher.DB.ProjectDAO;
import com.socialreport.srpublisher.R;

import java.util.ArrayList;

public class FragmentProjectsList extends Fragment implements OnItemRecycleViewClickListener {

    final String LOG_TAG = "FragmentProjectsList";

    RecyclerView mRecyclerView;
    private ArrayList<Project> projects;
    ProjectDAO projectDAO;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.projects_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        Log.i(LOG_TAG, "onViewCreated");

        projectDAO = new ProjectDAO(view.getContext());

        projects = projectDAO.getProjectList();

        Log.i(LOG_TAG, "onViewCreated projects.size(): " + projects.size());

        mRecyclerView = (RecyclerView) view.findViewById(R.id.projects_recycler_view);
        GridLayoutManager mGridLayout = new GridLayoutManager(view.getContext(), 1);
        MyLinearLayoutManager mLinearLM = new MyLinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);


        //mRecyclerView.setLayoutManager(mGridLayout);
        mRecyclerView.setLayoutManager(mLinearLM);
        mRecyclerView.setAdapter(new RecyclerAdapter(projects, this));

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onItemClicked(int position, RecyclerAdapter mAdapter) {
        Log.i(LOG_TAG, "onItemClicked position: " + position);

        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), projects.get(position).getID(), getActivity().getIntent());
        }
    }

}
