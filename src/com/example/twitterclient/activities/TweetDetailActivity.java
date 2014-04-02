package com.example.twitterclient.activities;

import java.text.SimpleDateFormat;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.twitterclient.R;
import com.example.twitterclient.adapters.TweetsAdapter;
import com.example.twitterclient.helpers.Async;
import com.example.twitterclient.helpers.Constants;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TweetDetailActivity extends Activity {
	Tweet mTweet;
	ImageView ivProfile;
	TextView tvName;
	TextView tvScreenName;
	TextView tvBody;
	TextView tvDate;
	TextView tvRetweetsCount;
	TextView tvFavouritesCount;
	TextView tvRetweets;
	TextView tvFavourites;
	Button btTweetStatus;
	Button tvActionReply;
	Button tvActionRetweet;
	Button tvActionFavorite;
	Button tvActionShare;
	View vTopSeparator;
	View vBottomSeparator;
	WebView wvMedia;
	ScrollView scrollView;
	
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
		setContentView(R.layout.activity_tweet_detail);
		setTitle("Tweet");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		final Tweet tweet = (Tweet) getIntent().getSerializableExtra("tweet");
		mTweet = tweet;
		setViews();
		fillViews();
	}
	
	private void setViews() {
		scrollView = (ScrollView) findViewById(R.id.scrollViewContainer);
		ivProfile = (ImageView) findViewById(R.id.ivProfile);
		btTweetStatus = (Button) findViewById(R.id.btTweetStatusDetail);
		tvName = (TextView) findViewById(R.id.tvName);
		tvScreenName = (TextView) findViewById(R.id.tvScreenName);
		tvBody = (TextView) findViewById(R.id.tvBody);
		tvDate = (TextView) findViewById(R.id.tvDate);
		wvMedia = (WebView) findViewById(R.id.wvMedia);
		tvRetweetsCount = (TextView) findViewById(R.id.tvRetweetsCount);
		tvFavouritesCount = (TextView) findViewById(R.id.tvFavouritesCount);
		tvRetweets = (TextView) findViewById(R.id.tvRetweets);
		tvFavourites = (TextView) findViewById(R.id.tvFavourites);
		vTopSeparator = (View) findViewById(R.id.vTopSeparator);
		vBottomSeparator = (View) findViewById(R.id.vBottomSeparator);
		tvActionReply = (Button) findViewById(R.id.button_reply);
		tvActionRetweet = (Button) findViewById(R.id.button_retweet);
		tvActionFavorite = (Button) findViewById(R.id.button_favorite);
		tvActionShare = (Button) findViewById(R.id.button_share);
		tvActionReply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(TweetDetailActivity.this, mTweet, Constants.ACTION_REPLY, 0);
			}
		});
		tvActionRetweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(TweetDetailActivity.this, mTweet, Constants.ACTION_RETWEET, 0);
			}
		});
		tvActionShare.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				final User user = User.getUser(mTweet.userId);
				SimpleDateFormat formatTime = new SimpleDateFormat("h:mm a", Locale.US);
				String strTime = formatTime.format(mTweet.createdDate);
				SimpleDateFormat formatDate = new SimpleDateFormat("dd MMM yy", Locale.US);
				String strDate = formatDate.format(mTweet.createdDate);		
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				String strShareText = user.name + " (@" + user.screenName + ") tweeted at " + strTime + " on " + strDate + ": " + mTweet.body;   
				shareIntent.putExtra(Intent.EXTRA_TEXT, strShareText);
				startActivity(Intent.createChooser(shareIntent, "Share"));
			}
		});
		setBottomBarMetadata();
	}
	
	private void setBottomBarMetadata() {
		mProgressBar.setVisibility(View.INVISIBLE);
		tvRetweetsCount.setText(Integer.toString(mTweet.retweetCount));
		tvFavouritesCount.setText(Integer.toString(mTweet.favouritesCount));
		if (mTweet.retweetCount <= 0) {
			tvRetweetsCount.setVisibility(View.GONE);
			tvRetweets.setVisibility(View.GONE);
		} else {
			tvRetweetsCount.setVisibility(View.VISIBLE);
			tvRetweets.setVisibility(View.VISIBLE);
		}
		if (mTweet.favouritesCount <= 0) {
			tvFavouritesCount.setVisibility(View.GONE);
			tvFavourites.setVisibility(View.GONE); 
		} else {
			tvFavouritesCount.setVisibility(View.VISIBLE);
			tvFavourites.setVisibility(View.VISIBLE);
		}
		if (mTweet.retweetCount <= 0 && mTweet.favouritesCount <= 0) {
			vTopSeparator.setVisibility(View.GONE);
			vBottomSeparator.setVisibility(View.GONE);
		} else {
			vTopSeparator.setVisibility(View.VISIBLE);
			vBottomSeparator.setVisibility(View.VISIBLE);
		}
	}
	
	private void fillViews() { 
		ImageLoader mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(TweetDetailActivity.this));
		final User user = User.getUser(mTweet.userId);
		mImageLoader.displayImage(user.profileImageUrl, ivProfile);
		if (mTweet.retweetedUserId != 0) {
			btTweetStatus.setVisibility(View.VISIBLE);
			btTweetStatus.setText(" " + User.getUser(mTweet.retweetedUserId).name + " retweeted");
		} else {
			btTweetStatus.setVisibility(View.GONE);
		}
		tvName.setText(user.name);
		tvScreenName.setText("@" + user.screenName);
		final Tweet tweet = mTweet;
		String body = tweet.body;
		if (tweet.actualUrl != null && tweet.displayUrl != null) {
			body = body.replace(tweet.actualUrl, tweet.displayUrl);
		}
		tvBody.setText(body);
		TweetsAdapter.setLinks(tvBody, tvBody.getText().toString(), tweet, true, new Async.Block<String, Tweet>() {
			@Override
			public void call(final String text, final Tweet result) {
				TweetsAdapter.handleLinkClicked(TweetDetailActivity.this, text, result);
			}
		});
		SimpleDateFormat formatTime = new SimpleDateFormat("h:mm a", Locale.US);
		String strTime = formatTime.format(mTweet.createdDate);
		SimpleDateFormat formatDate = new SimpleDateFormat("dd MMM yy", Locale.US);
		String strDate = formatDate.format(mTweet.createdDate);		
		tvDate.setText(strTime + " • " + strDate);		
		if(mTweet.actualUrl != null && mTweet.actualUrl.length() > 0) {
			wvMedia.setVisibility(View.VISIBLE);
			wvMedia.getSettings().setJavaScriptEnabled(true);
			wvMedia.loadUrl(mTweet.actualUrl);
			wvMedia.setWebViewClient(new WebViewClient(){
				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					view.loadUrl(url);
					return false;
				}
			});
		} else {
			wvMedia.setVisibility(View.GONE);
		}
		scrollView.post(new Runnable() {
			@Override
			public void run() {
				scrollView.scrollTo(0, 0);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet_detail, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; goto parent activity.
	            this.finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
