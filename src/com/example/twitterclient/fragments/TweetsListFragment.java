package com.example.twitterclient.fragments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.activeandroid.ActiveAndroid;
import com.example.twitterclient.R;
import com.example.twitterclient.activities.AppActivity;
import com.example.twitterclient.activities.LoginActivity;
import com.example.twitterclient.adapters.TweetsAdapter;
import com.example.twitterclient.helpers.Async;
import com.example.twitterclient.helpers.CustomDateComparator;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.Utils;
import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.User;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

public class TweetsListFragment extends SherlockFragment implements
		PullToRefreshBase.OnLastItemVisibleListener,
		PullToRefreshBase.OnRefreshListener<ListView> {
	private PullToRefreshListView lvTweets;
	private TweetsAdapter tweetsAdapter;
	private static final CustomDateComparator sTweetComparator = new CustomDateComparator();
	private AppActivity mActivity;
	private Utils.TimelineType mTimelineType;
	private Long mUserId;
	private String mQuery;
	
	public static TweetsListFragment newInstance(Utils.TimelineType timelineType, Long userId, String query) {
		TweetsListFragment fragmentTweets = new TweetsListFragment();
		Bundle args = new Bundle();
		args.putSerializable("timelineType", timelineType);
		if(userId != null) {
			args.putLong("userId", userId);
		}
		if(query != null && query.length() > 0) {
			args.putString("query", query);
		}
		fragmentTweets.setArguments(args);
		return fragmentTweets;
	}	
	
	public TweetsAdapter getAdapter() {
		return tweetsAdapter;
	}

	@Override
	public void onAttach(final Activity activity) {
		mActivity = (AppActivity) activity;
		super.onAttach(activity);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTimelineType = (Utils.TimelineType) getArguments().getSerializable(
				"timelineType");
		mUserId = getArguments().getLong("userId");
		mQuery = getArguments().getString("query");
		tweetsAdapter = new TweetsAdapter(getActivity());
		if (!Utils.isNetworkAvailable(getActivity())) {
			Async.dispatch(new Runnable() {
				@Override
				public void run() {
					User.loadAll();
					final List<Tweet> tweets = Tweet.getRecentTweets(100);
					final ArrayList<Long> tweetIds = new ArrayList<Long>();
					for (final Tweet tweet : tweets) {
						tweetIds.add(tweet.tweetId);
					}
					Async.dispatchMain(new Runnable() {
						@Override
						public void run() {
							didLoadTweets(tweetIds);
						}
					});
				}
			});
		} else {
			loadPreviousTweets();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inf, ViewGroup parent,
			Bundle savedInstanceState) {
		final View v = inf
				.inflate(R.layout.fragment_tweets_list, parent, false);
		lvTweets = (PullToRefreshListView) v.findViewById(R.id.lvTweets);
		lvTweets.setOnLastItemVisibleListener(this);
		lvTweets.setOnRefreshListener(this);
		lvTweets.setAdapter(tweetsAdapter);
		return v;
	}

	public void loadLatestTweets() {
		long sinceId = 0;
		if (tweetsAdapter != null && tweetsAdapter.getCount() > 0) {
			sinceId = tweetsAdapter.getItem(0);
		}
		loadTweets(0, sinceId);
	}

	private void loadPreviousTweets() {
		long maxId = 0;
		if (tweetsAdapter != null && tweetsAdapter.getCount() > 0) {
			maxId = tweetsAdapter.getItem(tweetsAdapter.getCount() - 1);
		}
		loadTweets(maxId, 0);
	}

	public void loadTweets(final long maxId, final long sinceId) {	
		getTimeline(mQuery, mUserId, mTimelineType.name().toLowerCase(Locale.US), maxId, sinceId);	
	}

	private void getTimeline(final String query, final long userId, final String timelineType, final long maxId, final long sinceId) {
		mActivity.getProgressBar().setVisibility(View.VISIBLE);
		LoginActivity.loginOrSignupWithCompletion(getActivity(),
				new Runnable() { 
					@Override
					public void run() {
						if (!LoginActivity.isAuthenticated(getActivity()))
							return;
						TwitterClientApp.getClient().getTimeline(query, userId, timelineType, maxId, sinceId, new JsonHttpResponseHandler() {				
									@Override
									public void onSuccess(final JSONArray jsonTweets) {
										Async.dispatch(new Runnable() {
											@Override
											public void run() {
												final ArrayList<Tweet> tweets = Tweet.fromJson(jsonTweets);
												final ArrayList<Long> tweetIds = new ArrayList<Long>();
												for (final Tweet tweet : tweets) {
													tweetIds.add(tweet.tweetId);
												}
												ActiveAndroid.beginTransaction();
												try {
													for (final Tweet tweet : tweets) {
														tweet.save();
													}
													User.saveAll();
													ActiveAndroid.setTransactionSuccessful();
												} finally {
													ActiveAndroid.endTransaction();
												}
												Async.dispatchMain(new Runnable() {
													@Override
													public void run() {
														didLoadTweets(tweetIds);
													}
												});
											}
										});
									} 
									
									@Override
									public void onSuccess(JSONObject arg0) {
										try {
											onSuccess(arg0.getJSONArray("statuses"));
										} catch (JSONException e) {
											e.printStackTrace();
										}
									}
									
									@Override
									protected void handleFailureMessage(Throwable arg0,
									String arg1) {
										Toast.makeText(mActivity, arg0.toString(), Toast.LENGTH_LONG).show();
									}

									@Override
									public void onFailure(Throwable e) {
										e.printStackTrace();
									}
								});
					}
				});
	}

	private void didLoadTweets(final List<Long> tweets) {
		mActivity.getProgressBar().setVisibility(View.INVISIBLE);
		final HashSet<Long> currentTweets = new HashSet<Long>();
		for (int i = 0, size = tweetsAdapter.getCount(); i < size; ++i) {
			currentTweets.add(tweetsAdapter.getItem(i));
		}
		final HashSet<Long> tweetsToAdd = new HashSet<Long>();
		for (final Long tweetId : tweets) {
			if (currentTweets.contains(tweetId)) {
				tweetsAdapter.remove(tweetId);
			}
			if (!tweetsToAdd.contains(tweetId)) {
				tweetsToAdd.add(tweetId);
			}
		}
		tweetsAdapter.addAll(tweetsToAdd);
		tweetsAdapter.sort(sTweetComparator);
		onEndRefresh();
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
		if (lvTweets != null) {
			if (lvTweets.isRefreshing()) {
				lvTweets.onRefreshComplete();
			}
		}
	}
}
