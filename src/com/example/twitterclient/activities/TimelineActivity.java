package com.example.twitterclient.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.widget.SearchView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.internal.view.menu.MenuItemWrapper;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.example.twitterclient.R;
import com.example.twitterclient.fragments.TweetsListFragment;
import com.example.twitterclient.helpers.Constants;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.Utils;
import com.example.twitterclient.helpers.Utils.TimelineType;

public class TimelineActivity extends AppActivity  {
	private final int REQUEST_CODE = 20;
	private com.actionbarsherlock.internal.view.menu.MenuItemWrapper mSearchMenuItem;
	private String mCurrentQuery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		setContentView(R.layout.activity_timeline);
		setTitle("");
		setupTabs(savedInstanceState);
		handleIntent(getIntent());
	}
	
    private void setupTabs(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        
        for (TimelineType type : TimelineType.values()) {
			if (type == TimelineType.USER) continue;
			final String typeString = getResources().getString(Utils.getNameResource(type));
			Tab tweetsListTab = actionBar.newTab()
						.setText(typeString)
						.setTabListener(
                (TabListener) new SherlockTabListener<TweetsListFragment>(R.id.frame_container, typeString, type));
			actionBar.addTab(tweetsListTab);
		}

		if (savedInstanceState == null)
			actionBar.selectTab(actionBar.getTabAt(0));
    }
    

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.timeline, menu);
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    mSearchMenuItem = (MenuItemWrapper) menu.findItem(R.id.miSearch);
	    SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setIconifiedByDefault(false);
		searchView.setIconified(false);
		searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + getResources().getString(R.string.etSearchHint) + "</font>"));
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.miComposetweet:
			TwitterClientApp.composeTweet(TimelineActivity.this, null,
					Constants.ACTION_NONE, REQUEST_CODE);
			return true;
		case R.id.miProfile:
			TwitterClientApp.showUserProfile(TimelineActivity.this, TwitterClientApp.getCurrentUser().userId);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			final String typeString = getResources().getString(Utils.getNameResource(TimelineType.HOME));
			TweetsListFragment fragmentTweets = (TweetsListFragment) getSupportFragmentManager().findFragmentByTag(typeString);
			fragmentTweets.loadLatestTweets();
			fragmentTweets.getAdapter().notifyDataSetChanged();			
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		this.getActionBar().setSelectedNavigationItem(0);
    }
	
	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
	
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			if (mSearchMenuItem != null) {
				mSearchMenuItem.getActionView().clearFocus();
			}
			if (mCurrentQuery == null || !mCurrentQuery.equals(query)) {
				TwitterClientApp.showUserTweets(TimelineActivity.this, 0, query);
			}
		}
	}
}
