package com.example.twitterclient.activities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.activeandroid.ActiveAndroid;
import com.example.twitterclient.R;
import com.example.twitterclient.activities.adapters.TweetsAdapter;
import com.example.twitterclient.activities.helpers.Async;
import com.example.twitterclient.activities.helpers.Constants;
import com.example.twitterclient.activities.helpers.CustomDateComparator;
import com.example.twitterclient.activities.helpers.TwitterClientApp;
import com.example.twitterclient.activities.helpers.Utils;
import com.example.twitterclient.activities.models.Tweet;
import com.example.twitterclient.activities.models.User;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

public class TimelineActivity extends Activity implements
		PullToRefreshBase.OnLastItemVisibleListener,
		PullToRefreshBase.OnRefreshListener<ListView> {
	private final int REQUEST_CODE = 20;
	private PullToRefreshListView lvTweets;
	private TweetsAdapter tweetsAdapter;
	private static final CustomDateComparator sTweetComparator = new CustomDateComparator();
	
	private ProgressBar mProgressBar;

	@Override
	public void setContentView(View view) {
		init().addView(view);
	}

	@Override
	public void setContentView(int layoutResID) {
		getLayoutInflater().inflate(layoutResID, init(), true);
	}

	@Override
	public void setContentView(View view, ViewGroup.LayoutParams params) {
		init().addView(view, params);
	}

	private ViewGroup init() {
		super.setContentView(R.layout.progress);
		mProgressBar = (ProgressBar) findViewById(R.id.activity_bar);
		mProgressBar.setVisibility(View.INVISIBLE);
		return (ViewGroup) findViewById(R.id.activity_frame);
	}

	protected ProgressBar getProgressBar() {
		return mProgressBar;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timeline);
		lvTweets = (PullToRefreshListView) findViewById(R.id.lvTweets);
		lvTweets.setOnLastItemVisibleListener(this);
		lvTweets.setOnRefreshListener(this);
		setTitle("Home");
		if (!Utils.isNetworkAvailable(this)) {
			Async.dispatch(new Runnable() {
				@Override
				public void run() {
					User.loadAll();
					final List<Tweet> tweets = Tweet.getRecentTweets(100);
					Async.dispatchMain(new Runnable() {
						@Override
						public void run() {
							didLoadTweets(tweets);
						}
					});
				}
			});
		} else {
			loadPreviousTweets();
		}
	}

	private void loadLatestTweets() {
		long sinceId = 0;
		if (tweetsAdapter != null && tweetsAdapter.getCount() > 0) {
			sinceId = tweetsAdapter.getItem(0).tweetId;
		}
		getHomeTimeline(0, sinceId);
	}

	private void loadPreviousTweets() {
		long maxId = 0;
		if (tweetsAdapter != null && tweetsAdapter.getCount() > 0) {
			maxId = tweetsAdapter.getItem(tweetsAdapter.getCount() - 1).tweetId;
		}
		getHomeTimeline(maxId, 0);
	}

	private void getHomeTimeline(final long maxId, final long sinceId) {
		mProgressBar.setVisibility(View.VISIBLE);
		LoginActivity.loginOrSignupWithCompletion(this, new Runnable() {
			@Override
			public void run() {
				if (!LoginActivity.isAuthenticated(TimelineActivity.this))
					return;
				TwitterClientApp.getClient().getHomeTimeline(maxId, sinceId,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(final JSONArray jsonTweets) {
								Async.dispatch(new Runnable() {
									@Override
									public void run() {
										final ArrayList<Tweet> tweets = Tweet
												.fromJson(jsonTweets);
										ActiveAndroid.beginTransaction();
										try {
											for (final Tweet tweet : tweets) {
												tweet.save();
											}
											User.saveAll();
											ActiveAndroid
													.setTransactionSuccessful();
										} finally {
											ActiveAndroid.endTransaction();
										}
										Async.dispatchMain(new Runnable() {
											@Override
											public void run() {
												didLoadTweets(tweets);
											}
										});
									}
								});
							}

							@Override
							public void onFailure(Throwable e) {
								e.printStackTrace();
							}
						});
			}
		});
	}

	private void didLoadTweets(final List<Tweet> tweets) {
		mProgressBar.setVisibility(View.INVISIBLE);
		if (tweetsAdapter == null) {
			tweetsAdapter = new TweetsAdapter(TimelineActivity.this);
			lvTweets.setAdapter(tweetsAdapter);
		}
		final HashSet<Long> currentTweets = new HashSet<Long>();
		for (int i = 0, size = tweetsAdapter.getCount(); i < size; ++i) {
			currentTweets.add(tweetsAdapter.getItem(i).tweetId);
		}
		final HashSet<Tweet> tweetsToAdd = new HashSet<Tweet>();
		for (final Tweet tweet : tweets) {
			if (currentTweets.contains(tweet.tweetId)) {
				tweetsAdapter.remove(tweet);
			}
			if (!tweetsToAdd.contains(tweet)) {
				tweetsToAdd.add(tweet);
			}
		}
		tweetsAdapter.addAll(tweetsToAdd);
		tweetsAdapter.sort(sTweetComparator);
		onEndRefresh();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.timeline, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.miComposetweet:
			TwitterClientApp.composeTweet(TimelineActivity.this, null,
					Constants.ACTION_NONE, REQUEST_CODE);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLastItemVisible() {
		loadPreviousTweets();
	}

	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		loadLatestTweets();
	}

	public void onEndRefresh() {
		if (lvTweets.isRefreshing())
			lvTweets.onRefreshComplete();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			loadLatestTweets();
			tweetsAdapter.notifyDataSetChanged();
		}
	}
}
