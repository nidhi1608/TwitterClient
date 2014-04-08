package com.example.twitterclient.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import com.example.twitterclient.R;
import com.example.twitterclient.fragments.TweetsListFragment;
import com.example.twitterclient.helpers.Utils.TimelineType;

public class ProfileTweetsActivity extends AppActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_profile_tweets);
		setTitle(R.string.tweets_label);
		Long userId = getIntent().getLongExtra("userId", 0);
		String query = getIntent().getStringExtra("query");
		if (query != null) {
			setTitle(query);
		}
		final TweetsListFragment fragmentTweets = TweetsListFragment.newInstance(TimelineType.USER, userId, query);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.frame_container, fragmentTweets);
		ft.commit();		
	}
}
