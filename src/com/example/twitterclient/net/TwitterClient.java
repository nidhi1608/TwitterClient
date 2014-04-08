package com.example.twitterclient.net;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Context;

import com.codepath.oauth.OAuthBaseClient;
import com.example.twitterclient.helpers.Constants;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class TwitterClient extends OAuthBaseClient {	

	public TwitterClient(Context c) {
		super(c, Constants.REST_API_CLASS, Constants.REST_URL, Constants.REST_CONSUMER_KEY, Constants.REST_CONSUMER_SECRET, Constants.REST_CALLBACK_URL);
	}
	
	public static TwitterClient getInstance(Context context) {
		return (TwitterClient) OAuthBaseClient.getInstance(TwitterClient.class, context);
	}

	public void getTimeline(String query, long userId, String timelineType, long maxId, long sinceId, AsyncHttpResponseHandler handler) {
		String apiUrl;
		if (query != null && query.length() > 0) {
			apiUrl = getApiUrl("search/tweets.json");
		} else {
			apiUrl = getApiUrl(String.format("statuses/%s_timeline.json", timelineType));
		}
		RequestParams params = new RequestParams();
		params.put("count", Constants.NO_RECORDS);
		if(query != null) {
			params.put("q", query);
		}
		if(userId > 0) {
			params.put("user_id", Long.toString(userId));
		}
		if(maxId > 0) {
			params.put("max_id", Long.toString(maxId));
		}
		if(sinceId > 0) {
			params.put("since_id", Long.toString(sinceId));
		}
		client.get(apiUrl, params, handler);
	}
	
	public void getRelatedUsers(long userId, String relatedUserType, String nextCursor, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl(String.format("%s/list.json", relatedUserType));
		RequestParams params = new RequestParams();
		if(userId > 0) {
			params.put("user_id", Long.toString(userId));
		}
		if (nextCursor != null) {
			params.put("next_cursor", nextCursor);
		}
		client.get(apiUrl, params, handler);
	}
	
	public void favoriteTweet(long tweetId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/create.json");
		RequestParams params = new RequestParams();
		if(tweetId > 0) {
			params.put("id", Long.toString(tweetId));
		}
		client.post(apiUrl, params, handler);
	}
	
	public void unFavoriteTweet(long tweetId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("favorites/destroy.json");
		RequestParams params = new RequestParams();
		if(tweetId > 0) {
			params.put("id", Long.toString(tweetId));
		}
		client.post(apiUrl, params, handler);
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
