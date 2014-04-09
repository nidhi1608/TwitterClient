package com.example.twitterclient.activities;

import java.text.DecimalFormat;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.twitterclient.R;
import com.example.twitterclient.fragments.TweetsListFragment;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.Utils.RelatedUserType;
import com.example.twitterclient.helpers.Utils.TimelineType;
import com.example.twitterclient.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ProfileActivity extends AppActivity {
	private ImageView ivProfile;
	private TextView tvName;
	private TextView tvScreenname;
	private TextView tvTweetsCount;
	private TextView tvTweets;
	private TextView tvFollowingCount;
	private TextView tvFollowersCount;
	private LinearLayout layoutTweets;
	private LinearLayout layoutFollowing;
	private LinearLayout layoutFollowers;
	private User user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile);
		setViews();
		fillViews();
	}

	private void setViews() {
		ivProfile = (ImageView) findViewById(R.id.ivProfile);
		tvName = (TextView) findViewById(R.id.tvName);
		tvScreenname = (TextView) findViewById(R.id.tvScreenName);
		tvTweetsCount = (TextView) findViewById(R.id.tvTweetsCount);
		tvTweets = (TextView) findViewById(R.id.tvTweets);
		tvFollowingCount = (TextView) findViewById(R.id.tvFollowingCount);
		tvFollowersCount = (TextView) findViewById(R.id.tvFollowersCount);
		layoutTweets = (LinearLayout) findViewById(R.id.layout_tweets);
		layoutFollowing = (LinearLayout) findViewById(R.id.layout_following);
		layoutFollowers = (LinearLayout) findViewById(R.id.layout_followers);
	}

	private void fillViews() {
		Long userId = getIntent().getLongExtra("userId", 0);
		user = User.getUser(userId);
		setTitle(R.string.profile);

		ImageLoader.getInstance().displayImage(user.profileImageUrl, ivProfile);
		tvName.setText(user.name);
		tvScreenname.setText("@" + user.screenName);
		tvTweetsCount.setText(formatNumber(Integer.toString(user.numTweets)));
		tvFollowingCount.setText(formatNumber(Integer
				.toString(user.friendsCount)));
		tvFollowersCount.setText(formatNumber(Integer
				.toString(user.followersCount)));

		TweetsListFragment fragmentTweets = TweetsListFragment.newInstance(
				TimelineType.USER, userId, null);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.frame_container, fragmentTweets);
		ft.commit();

		setTweetsListener(layoutTweets, user.userId);
		setRelatedUserListener(layoutFollowing, user.userId,
				RelatedUserType.FRIENDS);
		setRelatedUserListener(layoutFollowers, user.userId,
				RelatedUserType.FOLLOWERS);
	}

	private void setTweetsListener(final View view, final Long userId) {
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.showTweets(ProfileActivity.this, userId,
						null);
			}
		});
	}

	private void setRelatedUserListener(final View view, final Long userId,
			final RelatedUserType relatedUserType) {
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.showRelatedUsers(ProfileActivity.this, userId,
						relatedUserType);
			}
		});
	}

	private String formatNumber(String num) {
		DecimalFormat formatter = new DecimalFormat("#,###");
		Long longNum = Long.parseLong(num);
		if (longNum <= 9999) {
			return formatter.format(longNum);
		}
		int exp = (int) (Math.log(longNum) / Math.log(1000));

		return String.format(Locale.US, "%.1f %c",
				longNum / Math.pow(1000, exp), "kMGTPE".charAt(exp - 1));
	}
}
