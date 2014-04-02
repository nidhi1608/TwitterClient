package com.example.twitterclient.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.codepath.oauth.OAuthLoginActivity;
import com.example.twitterclient.R;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.models.User;
import com.example.twitterclient.net.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class LoginActivity extends OAuthLoginActivity<TwitterClient> {
	
	private static Class sClass;
	
	public static void loginOrSignupWithCompletion(final Activity context, final Runnable runnable) {
		if (isAuthenticated(context)) {
			verifyCredentialsWithCompletion(runnable);
		} else {
			sClass = context.getClass();
			final Intent intent = new Intent(context, LoginActivity.class);
			context.startActivityForResult(intent, 0);
		}
	}
	
	public static boolean isAuthenticated(final Context context) {
		return TwitterClient.getInstance(context).isAuthenticated();
	}
	
	private static boolean mLoggingIn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	    this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		if (!mLoggingIn) {
			setContentView(R.layout.activity_login);
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
				}
			}, 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public static void verifyCredentialsWithCompletion(final Runnable completion) {
		TwitterClientApp.getClient().verifyCredentials(new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int arg0, JSONObject jsonObject) {
				TwitterClientApp.setCurrentUser(User.fromJson(jsonObject));
				completion.run();
			}
			
			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				completion.run();
			}
		});
	}

	@Override
	public void onLoginSuccess() {
		mLoggingIn = false;
		verifyCredentialsWithCompletion(new Runnable() {
			@Override
			public void run() {
				callCompletion(LoginActivity.this);
			}});
	}
	
	public static void callCompletion(final LoginActivity activity) {
		final Intent intent = new Intent(activity, sClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		activity.startActivity(intent);
		activity.finish();
		activity.overridePendingTransition(0, 0);
	}

	@Override
	public void onLoginFailure(Exception e) {
		e.printStackTrace();		
	}
	
	public void loginToRest(View view) {
		mLoggingIn = true;
		getClient().connect();
	}

}
