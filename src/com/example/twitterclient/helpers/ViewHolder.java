package com.example.twitterclient.helpers;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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
	}
}
