package com.example.twitterclient.helpers;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.Tweet.OnTweetChangedListener;

public class ViewHolder {
	public static class TweetItem {
		public ImageView ivProfile;
		public TextView btTweetStatus;
		public TextView tvName;
		public TextView tvDate;
		public TextView tvBody;
		public Button btReply;
		public Button btRetweet;
		public Button btFav;
		private OnTweetChangedListener listener;
		
		public void registerListenerForTweetId(final Long tweetId, final OnTweetChangedListener listener) {
			this.listener = listener;
			Tweet.registerListener(listener, tweetId);
		}
	}
	
	public static class UserItem {
		public ImageView ivProfile;
		public TextView tvName;
		public TextView tvScreenName;
		public TextView tvDescription;
	}
}
