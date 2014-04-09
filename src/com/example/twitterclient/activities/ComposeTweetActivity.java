package com.example.twitterclient.activities;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twitterclient.R;
import com.example.twitterclient.helpers.Constants;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ComposeTweetActivity extends AppActivity {
	ImageView ivUserProfile;
	TextView tvUserName;
	TextView tvScreenName;
	TextView tvCharacterCount;
	EditText etTweetText;
	private static final String PADDING = "  ";
	MenuItem miTweet;
	Long refTweetId;
	String screenname;
	int action;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refTweetId = getIntent().getLongExtra("tweetId", 0);
		action = getIntent().getIntExtra("action", 0);
		screenname = getIntent().getStringExtra("screenname");
		setContentView(R.layout.activity_compose_tweet);
		setViews();
		setUserInfo();
	}

	private void setViews() {
		ivUserProfile = (ImageView) findViewById(R.id.ivUserProfile);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvScreenName = (TextView) findViewById(R.id.tvScreenName);
		etTweetText = (EditText) findViewById(R.id.etTweetText);
		final Tweet refTweet = Tweet.getTweet(refTweetId);
		if (refTweet != null || screenname != null) {
			String text = "";
			if (action == Constants.ACTION_REPLY) {
				if (screenname != null) {
					text = "@" + screenname;
				} else {
					final User user = User.getUser(refTweet.userId);
					text = "@" + user.screenName;
				}
			} else if (action == Constants.ACTION_RETWEET) {
				if (refTweet != null) {
					text = refTweet.body;
				}
			}
			etTweetText.setText(text);
			etTweetText.setSelection(text.length());
		}
		textDidChange();
	}

	private void setUserInfo() {
		User currentUser = TwitterClientApp.getCurrentUser();
		if (currentUser != null) {
			ImageLoader mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(ImageLoaderConfiguration
					.createDefault(ComposeTweetActivity.this));
			mImageLoader.displayImage(currentUser.profileImageUrl,
					ivUserProfile);

			tvUserName.setText(currentUser.name);
			tvScreenName.setText("@" + currentUser.screenName);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.compose_tweet, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem miCharacterCount = menu.findItem(R.id.miCharacterCount);
		miTweet = menu.findItem(R.id.miTweet);
		miCharacterCount.setEnabled(false);
		miTweet.setEnabled(false);
		tvCharacterCount = (TextView) miCharacterCount.getActionView();
		etTweetText.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable arg0) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2,
					int arg3) {
				textDidChange();
			}
		});
		textDidChange();
		return super.onPrepareOptionsMenu(menu);
	}

	public void textDidChange() {
		if (miTweet == null || tvCharacterCount == null || etTweetText == null)
			return;
		final String tweetText = etTweetText.getText().toString();
		if (tweetText != null && tweetText.length() > 0) {
			int remainingChars = Constants.MAX_CHARS - tweetText.length();
			tvCharacterCount.setText(Integer.toString(remainingChars) + " "
					+ PADDING);
			if (remainingChars <= 0 || remainingChars == Constants.MAX_CHARS) {
				miTweet.setEnabled(false);
			} else {
				miTweet.setEnabled(true);
			}
		} else {
			tvCharacterCount.setText("140" + PADDING);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.miTweet:
			String tweetText = etTweetText.getText().toString();
			if (tweetText != null && tweetText.length() > 0) {
				TwitterClientApp.getClient().updateStatus(tweetText,
						new JsonHttpResponseHandler() {
							@Override
							public void onSuccess(JSONObject jsonObject) {
								Intent data = new Intent();
								final Tweet tweet = Tweet.fromJson(jsonObject);
								tweet.save();
								data.putExtra("tweetId", tweet.tweetId);
								setResult(RESULT_OK, data);
								finish();
							}

							@Override
							public void onFailure(Throwable e) {
								e.printStackTrace();
							}

						});
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
