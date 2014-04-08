package com.example.twitterclient.activities;

import java.text.SimpleDateFormat;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.example.twitterclient.R;
import com.example.twitterclient.adapters.TweetsAdapter;
import com.example.twitterclient.helpers.Async;
import com.example.twitterclient.helpers.Constants;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.Tweet.OnTweetChangedListener;
import com.example.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TweetDetailActivity extends AppActivity {
	Long mTweetId;
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
	OnTweetChangedListener listener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_detail);
		setTitle("Tweet");
		mTweetId = getIntent().getLongExtra("tweetId", 0);
		listener = new OnTweetChangedListener() {
			@Override
			public void tweetDidChange(Long tweetId) {
				fillViews();
			}
		};
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
			}
	
	private void setBottomBarMetadata() {
		final Tweet tweet = Tweet.getTweet(mTweetId);
		getProgressBar().setVisibility(View.INVISIBLE);
		tvRetweetsCount.setText(Integer.toString(tweet.retweetCount));
		tvFavouritesCount.setText(Integer.toString(tweet.favouritesCount));
		if (tweet.retweetCount <= 0) {
			tvRetweetsCount.setVisibility(View.GONE);
			tvRetweets.setVisibility(View.GONE);
		} else {
			tvRetweetsCount.setVisibility(View.VISIBLE);
			tvRetweets.setVisibility(View.VISIBLE);
		}
		if (tweet.favouritesCount <= 0) {
			tvFavouritesCount.setVisibility(View.GONE);
			tvFavourites.setVisibility(View.GONE); 
		} else {
			tvFavouritesCount.setVisibility(View.VISIBLE);
			tvFavourites.setVisibility(View.VISIBLE);
		}
		if (tweet.retweetCount <= 0 && tweet.favouritesCount <= 0) {
			vTopSeparator.setVisibility(View.GONE);
			vBottomSeparator.setVisibility(View.GONE);
		} else {
			vTopSeparator.setVisibility(View.VISIBLE);
			vBottomSeparator.setVisibility(View.VISIBLE);
		}
	}
	
	private void fillViews() {
		final Tweet tweet = Tweet.getTweet(mTweetId);
		if (tweet.favorited) {
			tvActionFavorite.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_favorite_yellow, 0, 0, 0);
			tvActionFavorite.setTextColor(Color.parseColor("#efe836"));

		} else {
			tvActionFavorite.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_favorite, 0, 0, 0);
		}
		tvActionReply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(TweetDetailActivity.this, mTweetId, Constants.ACTION_REPLY, 0);
			}
		});
		tvActionRetweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(TweetDetailActivity.this, mTweetId, Constants.ACTION_RETWEET, 0);
			}
		});
		tvActionFavorite.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!tweet.favorited) {
					TwitterClientApp.getClient().favoriteTweet(tweet.tweetId,
							new JsonHttpResponseHandler() {
								@Override
								public void onSuccess(JSONObject json) {
									tvActionFavorite
											.setCompoundDrawablesWithIntrinsicBounds(
													R.drawable.ic_favorite_yellow,
													0, 0, 0);
									tvActionFavorite.setTextColor(Color.parseColor("#efe836"));
									tweet.favorited = true;
									
									tweet.favouritesCount = tweet.favouritesCount + 1;
									tvFavouritesCount.setText(" "
											+ tweet.favouritesCount);
									tvFavouritesCount.setVisibility(View.VISIBLE);
									tvFavourites.setVisibility(View.VISIBLE);
									tweet.markDirty();
								}

								@Override
								public void onFailure(Throwable e) {
									e.printStackTrace();
								}
								
								@Override
								protected void handleFailureMessage(
										Throwable arg0, String arg1) {
									Toast.makeText(TweetDetailActivity.this, arg0.toString(), Toast.LENGTH_LONG).show();
								}
							});
				} else {
					TwitterClientApp.getClient().unFavoriteTweet(tweet.tweetId,
							new JsonHttpResponseHandler() {
								@Override
								public void onSuccess(JSONObject json) {
									tvActionFavorite
											.setCompoundDrawablesWithIntrinsicBounds(
													R.drawable.ic_favorite,
													0, 0, 0);
									tweet.favorited = false;
									tweet.favouritesCount = tweet.favouritesCount - 1;
									if (tweet.favouritesCount > 0) {
										tvFavouritesCount.setText(" "
												+ tweet.favouritesCount);
									} else {
										tvFavouritesCount.setText("");
										tvFavouritesCount.setVisibility(View.GONE);
										tvFavourites.setVisibility(View.GONE); 
									}
									tweet.markDirty();
								}

								@Override
								public void onFailure(Throwable e) {
									e.printStackTrace();
								}
								
								@Override
								protected void handleFailureMessage(
										Throwable arg0, String arg1) {
									Toast.makeText(TweetDetailActivity.this, arg0.toString(), Toast.LENGTH_LONG).show();
								}
							});
				}				
			}
		});
		tvActionShare.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {	
				final User user = User.getUser(tweet.userId);
				SimpleDateFormat formatTime = new SimpleDateFormat("h:mm a", Locale.US);
				String strTime = formatTime.format(tweet.createdDate);
				SimpleDateFormat formatDate = new SimpleDateFormat("dd MMM yy", Locale.US);
				String strDate = formatDate.format(tweet.createdDate);		
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.setType("text/plain");
				String strShareText = user.name + " (@" + user.screenName + ") tweeted at " + strTime + " on " + strDate + ": " + tweet.body;   
				shareIntent.putExtra(Intent.EXTRA_TEXT, strShareText);
				startActivity(Intent.createChooser(shareIntent, "Share"));
			}
		});
		setBottomBarMetadata();
		ImageLoader mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(TweetDetailActivity.this));
		final User user = User.getUser(tweet.userId);
		mImageLoader.displayImage(user.profileImageUrl, ivProfile);
		if (tweet.retweetedUserId != 0) {
			btTweetStatus.setVisibility(View.VISIBLE);
			btTweetStatus.setText(" " + User.getUser(tweet.retweetedUserId).name + " retweeted");
		} else {
			btTweetStatus.setVisibility(View.GONE);
		}
		tvName.setText(user.name);
		tvScreenName.setText("@" + user.screenName);
		String body = tweet.body;
		if (tweet.actualUrl != null && tweet.displayUrl != null) {
			body = body.replace(tweet.actualUrl, tweet.displayUrl);
		}
		tvBody.setText(body);
		TweetsAdapter.setLinks(tvBody, tvBody.getText().toString(), tweet.tweetId, true, new Async.Block<String, Long>() {
			@Override
			public void call(final String text, final Long tweetId) {
				TweetsAdapter.handleLinkClicked(TweetDetailActivity.this, text, tweetId);
			}
		});
		SimpleDateFormat formatTime = new SimpleDateFormat("h:mm a", Locale.US);
		String strTime = formatTime.format(tweet.createdDate);
		SimpleDateFormat formatDate = new SimpleDateFormat("dd MMM yy", Locale.US);
		String strDate = formatDate.format(tweet.createdDate);		
		tvDate.setText(strTime + " • " + strDate);		
		if(tweet.actualUrl != null && tweet.actualUrl.length() > 0) {
			wvMedia.setVisibility(View.VISIBLE);
			wvMedia.getSettings().setJavaScriptEnabled(true);
			wvMedia.loadUrl(tweet.actualUrl);
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
		getSupportMenuInflater().inflate(R.menu.tweet_detail, menu);
		return true;
	}
}
