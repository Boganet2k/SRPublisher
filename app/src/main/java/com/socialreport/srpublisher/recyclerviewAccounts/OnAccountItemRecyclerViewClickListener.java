package com.socialreport.srpublisher.recyclerviewAccounts;

import com.socialreport.srpublisher.DB.Project;

/**
 * Created by bb on 25.09.15.
 */
public interface OnAccountItemRecyclerViewClickListener {
    public void onAccountsSelected(Project project, RecyclerAdapterAccount mAdapter);
    public void onSelectBoards(RecyclerAdapterAccount mAdapter);
}