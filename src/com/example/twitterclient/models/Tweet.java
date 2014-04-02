package com.example.twitterclient.models;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.text.Spanned;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.example.twitterclient.helpers.Utils;

@Table(name = "Tweets")
public class Tweet extends Model implements Serializable {
	private static final long serialVersionUID = 7383038490057198946L;
	
	@Column(name = "body")
	public String body;
	
	@Column(name = "tweetId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	public long tweetId;
	
	@Column(name = "createdDate")
	public Date createdDate;
	
	@Column(name = "favorited")
	public boolean favorited;
	
	@Column(name = "retweeted")
	public boolean retweeted;
	
	@Column(name = "retweetCount")
	public int retweetCount;
	
	@Column(name = "favouritesCount")
	public int favouritesCount;
	
	@Column(name = "userId")
	public long userId;
	
	@Column(name = "retweetedUserId")
	public long retweetedUserId;
	
	@Column(name="displayUrl")
	public String displayUrl;
	
	@Column(name="actualUrl")
	public String actualUrl;
    
    public Tweet() {
		super();
	}
    
	public Spanned getFormattedDate(String dateFormatString) {
		String formatString = "<small style=\"color:#888\"><em>%s</em></small>";
		SimpleDateFormat formatter = new SimpleDateFormat(dateFormatString, Locale.US);		
		String formattedDate = formatter.format(createdDate);
		return Html.fromHtml(String.format(formatString, formattedDate));
	}

    public static Tweet fromJson(JSONObject jsonObject) {
        Tweet tweet = new Tweet();
        try {
        	tweet.body = jsonObject.getString("text");
        	tweet.tweetId = jsonObject.getLong("id");
        	tweet.createdDate = Utils.convertToDate(jsonObject.getString("created_at"));
        	tweet.favorited = jsonObject.getBoolean("favorited");
        	tweet.retweeted = jsonObject.getBoolean("retweeted");
            final User user = User.fromJson(jsonObject.getJSONObject("user"));
            tweet.userId = user.userId;
            tweet.retweetCount = jsonObject.getInt("retweet_count");
            tweet.favouritesCount = jsonObject.getInt("favorite_count");
            if (jsonObject.has("retweeted_status")) {
            	final JSONObject retweetedStatus = jsonObject.getJSONObject("retweeted_status");
            	final User retweetedUser = User.fromJson(retweetedStatus.getJSONObject("user"));
            	tweet.userId = retweetedUser.userId;
            	tweet.retweetedUserId = user.userId;
            	tweet.body = retweetedStatus.getString("text");
            	tweet.retweetCount = retweetedStatus.getInt("retweet_count");
        		tweet.favouritesCount = retweetedStatus.getInt("favorite_count");
            }
            if (jsonObject.has("entities")) {
            	final JSONObject entity = jsonObject.getJSONObject("entities");
            	if (entity.has("urls")) {
            		final JSONArray urls = entity.getJSONArray("urls");
            		if (urls.length() == 1) {
            			tweet.displayUrl = urls.getJSONObject(0).getString("display_url");
            			tweet.actualUrl = urls.getJSONObject(0).getString("url");
            		}
            	}
            }
        } catch (JSONException e) {
            e.printStackTrace(); 
            return null;
        } catch (ParseException e) {
			e.printStackTrace();
		}
        return tweet;
    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());
        for (int i=0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Tweet tweet = Tweet.fromJson(tweetJson);
            if (tweet != null) {
                tweets.add(tweet);
            }
        }
        return tweets;
    }
    
    public static List<Tweet> getRecentTweets(Integer limit) {
		return new Select()
			.from(Tweet.class)
			.orderBy("createdDate DESC")
			.limit(limit.toString()).execute();
	}
}


