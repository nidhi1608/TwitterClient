package com.example.twitterclient.helpers;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

public class Constants {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
    public static final String REST_URL = "https://api.twitter.com/1.1";
    public static final String REST_CONSUMER_KEY = "loADtOB9BeCpg1C17Fdzow";       
    public static final String REST_CONSUMER_SECRET = "56PZSQClrWUPbmcZRAQxRCIFWzbJaxg0b2UKCKPXKA"; 
    public static final String REST_CALLBACK_URL = "oauth://twitterclient";
    public static final String REST_GET_HOMETIMELINE_URL = "statuses/home_timeline.json"; 
    public static final String REST_VERIFY_CREDENTIALS_URL = "account/verify_credentials.json";
    public static final String REST_UPDATE_STATUS_URL = "statuses/update.json";
    public static final String REST_SHOW_TWEET_URL = "statuses/show.json";
    public static final String TWITTER_CLIENT_DATE_FORMAT = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
    public static final String TWEET_DATE_FORMAT = "h:ma, MMMMM d yyyy";    
    public static final int MAX_CHARS = 140;
    public static final String NO_RECORDS = "25";
    public static final int ACTION_NONE = 0;
    public static final int ACTION_REPLY = 1;
	public static final int ACTION_RETWEET = 2;
}
