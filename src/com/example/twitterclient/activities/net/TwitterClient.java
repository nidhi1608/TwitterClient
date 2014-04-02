package com.example.twitterclient.activities.net;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.example.twitterclient.activities.helpers.Constants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TwitterClient extends OAuthBaseClient {	

	public TwitterClient(Context c) {
		super(c, Constants.REST_API_CLASS, Constants.REST_URL, Constants.REST_CONSUMER_KEY, Constants.REST_CONSUMER_SECRET, Constants.REST_CALLBACK_URL);
	}
	
	public static TwitterClient getInstance(Context context) {
		return (TwitterClient) OAuthBaseClient.getInstance(TwitterClient.class, context);
	}

	public void getHomeTimeline(long maxId, long sinceId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl(Constants.REST_GET_HOMETIMELINE_URL);
		RequestParams params = new RequestParams();
		params.put("count", Constants.NO_RECORDS);
		if(maxId > 0) {
			params.put("max_id", Long.toString(maxId));
		}
		if(sinceId > 0) {
			params.put("since_id", Long.toString(sinceId));
		}
		client.get(apiUrl, params, handler);
	}
	
	public void verifyCredentials(AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl(Constants.REST_VERIFY_CREDENTIALS_URL);
		client.get(apiUrl, handler);
	}
	
	public void updateStatus(String status, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl(Constants.REST_UPDATE_STATUS_URL);
		RequestParams params = new RequestParams();
		params.put("status", status);
		client.post(apiUrl, params, handler);
	}
	
	public void showTweet(long id, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl(Constants.REST_SHOW_TWEET_URL);
		RequestParams params = new RequestParams();
		params.put("id", Long.toString(id));
		client.get(apiUrl, params, handler);
	}
}
