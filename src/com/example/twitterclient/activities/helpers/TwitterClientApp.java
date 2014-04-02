package com.example.twitterclient.activities.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.twitterclient.R;
import com.example.twitterclient.activities.ComposeTweetActivity;
import com.example.twitterclient.activities.models.Tweet;
import com.example.twitterclient.activities.models.User;
import com.example.twitterclient.activities.net.TwitterClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TwitterClientApp extends com.activeandroid.app.Application {
	private static Context context;
	private static User user;
	
	@Override
	public void onCreate() {
		super.onCreate();
		context = this;
		
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).defaultDisplayImageOptions(defaultOptions).build();
		ImageLoader.getInstance().init(config);
	}
	
	public static TwitterClient getClient() {
		return TwitterClient.getInstance(context);
	}
	
	public static User getCurrentUser() {
		return user;
	}

	public static void setCurrentUser(User user) {
		TwitterClientApp.user = user;
	}
	
	public static void composeTweet(final Context context, final Tweet refTweet, final int action, final int requestCode) {
		composeTweet(context, refTweet, null, action, requestCode);
	}
	
	public static void composeTweet(final Context context, final Tweet refTweet, final String screenname, final int action, final int requestCode) {
		Intent intent = new Intent(context, ComposeTweetActivity.class);
		if (refTweet != null) {
			intent.putExtra("tweet", refTweet);
		}
		if (screenname != null) {
			intent.putExtra("screenname", screenname);
		}
		intent.putExtra("action", action);
		if (context instanceof Activity) {
			final Activity activity = (Activity)context;
			activity.startActivityForResult(intent, requestCode);
			activity.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.slide_out_to_top);
		} else {
			context.startActivity(intent);
		}
	}
}
