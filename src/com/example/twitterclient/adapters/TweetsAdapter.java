package com.example.twitterclient.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twitterclient.R;
import com.example.twitterclient.activities.TweetDetailActivity;
import com.example.twitterclient.helpers.Async;
import com.example.twitterclient.helpers.Constants;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.ViewHolder;
import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TweetsAdapter extends ArrayAdapter<Tweet>{
	private ImageLoader mImageLoader;
	private View mLastLinkView;

	public TweetsAdapter(Context context) {
		super(context, 0);
	}
	
	private static final String getRelativeTime(final Context context, final Date date) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		final long currentTime = System.currentTimeMillis();
		final long time = calendar.getTimeInMillis();
		final long deltaSeconds = (currentTime - time) / 1000;
		if (deltaSeconds < 10) {
			return "Now";
		}
		if (deltaSeconds < 60) {
			return deltaSeconds + "s";
		}
		final long deltaMins = deltaSeconds / 60;
		if (deltaMins < 60) {
			return deltaMins + "m";
		}
		final long deltaHrs = deltaMins / 60;
		if (deltaHrs < 24) {
			return deltaHrs + "h";
		}
		final long deltaDays = deltaHrs / 24;
		if (deltaDays < 7) {
			return deltaDays + "d";
		}
		calendar.setTimeInMillis(currentTime);
		final long currentYear = calendar.get(Calendar.YEAR);
		calendar.setTime(date);
		final long year = calendar.get(Calendar.YEAR);
		final Locale locale = Locale.getDefault();
		final String day = calendar.getDisplayName(Calendar.DAY_OF_MONTH, Calendar.SHORT, locale);
		final String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale); 
		if (year == currentYear) {
			return  day + " " + month;
		} else {
			final String yearString = calendar.getDisplayName(Calendar.YEAR, Calendar.SHORT, locale);
			return day + " " + month + " " + yearString;
		}
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(R.layout.tweet_item, null);
			final ViewHolder.TweetItem holder = new ViewHolder.TweetItem();
			holder.ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
			holder.tvName = (TextView) view.findViewById(R.id.tvName);
			holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
			holder.tvBody = (TextView) view.findViewById(R.id.tvBody);
			holder.btTweetStatus = (Button) view.findViewById(R.id.btTweetStatus);
			holder.btReply = (Button) view.findViewById(R.id.button_reply_small);
			holder.btRetweet = (Button) view.findViewById(R.id.button_retweet_small);
			holder.btFav = (Button) view.findViewById(R.id.button_favorite_small);
			view.setTag(holder);
		}
		final ViewHolder.TweetItem holder = (ViewHolder.TweetItem) view.getTag();
		final Tweet tweet = getItem(position);
		if (tweet.retweetedUserId != 0) {
			holder.btTweetStatus.setVisibility(View.VISIBLE);
			holder.btTweetStatus.setText(" " + User.getUser(tweet.retweetedUserId).name + " retweeted");
		} else {
			holder.btTweetStatus.setVisibility(View.GONE);
		}
		holder.ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
		final User user = User.getUser(tweet.userId);
		mImageLoader.displayImage(user.profileImageUrl, holder.ivProfile);
		
		holder.tvName = (TextView) view.findViewById(R.id.tvName);
		String formattedName = "<b>" + user.name + "</b>" + "<small><font color='#777777'> @" + user.screenName + "</font></small>";
		holder.tvName.setText(Html.fromHtml(formattedName));
		
		holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
		holder.tvDate.setText(getRelativeTime(getContext(), tweet.createdDate));
		
		holder.tvBody = (TextView) view.findViewById(R.id.tvBody);
		String body = tweet.body;
		if (tweet.actualUrl != null && tweet.displayUrl != null) {
			body = body.replace(tweet.actualUrl, tweet.displayUrl);
		}
		holder.tvBody.setText(body);
		setLinks(holder.tvBody, holder.tvBody.getText().toString(), tweet, false, new Async.Block<String, Tweet>() {
			@Override
			public void call(final String text, final Tweet result) {
				if (handleLinkClicked(getContext(), text, tweet)) {
	            	mLastLinkView = holder.tvBody;
	            } else {
	            	mLastLinkView = null;
	            }
			}
		});
		View.OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mLastLinkView != v) {
					Intent intent = new Intent(getContext(), TweetDetailActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra("tweet", tweet);
					getContext().startActivity(intent);
				}
				mLastLinkView = null;
			}
		};
		view.setOnClickListener(onClickListener);
		holder.tvBody.setOnClickListener(onClickListener);
		if (tweet.retweetCount > 0) {
			holder.btRetweet.setText(" " + tweet.retweetCount);
		} else {
			holder.btRetweet.setText("");
		}
		if (tweet.favouritesCount > 0) {
			holder.btFav.setText(" " + tweet.favouritesCount);
		} else {
			holder.btFav.setText("");
		}
		
		holder.btReply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(getContext(), tweet, Constants.ACTION_REPLY, 0);
			}
		});
		holder.btRetweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(getContext(), tweet, Constants.ACTION_RETWEET, 0);
			}
		});
		return view;
	}
	
	public static void setLinks(TextView tv, String text, Tweet tweet, boolean senseTwitterHandles, final Async.Block<String, Tweet> completion) {
		final ArrayList<String> linkPatterns = new ArrayList<String>();
		linkPatterns.add("([Hh][tT][tT][pP][sS]?:\\/\\/[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])");
		if (senseTwitterHandles) {
			linkPatterns.add("@[\\w]+");
		}
        final SpannableString f = new SpannableString(text);
        for (String str : linkPatterns) {
            Pattern pattern = Pattern.compile(str);
            Matcher matcher = pattern.matcher(tv.getText());
            while (matcher.find()) {
                int selectionStart = matcher.start();
                int selectionEnd = matcher.end();
                InternalURLSpan span = new InternalURLSpan();
                span.text = text.substring(selectionStart, selectionEnd);
                span.tweet = tweet;
                span.completion = completion;
                f.setSpan(span, selectionStart, selectionEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        if (tweet.displayUrl != null) {
        	int selectionStart = text.indexOf(tweet.displayUrl);
        	int selectionEnd = selectionStart + tweet.displayUrl.length();
            InternalURLSpan span = new InternalURLSpan();
            span.text = text.substring(selectionStart, selectionEnd);
            span.tweet = tweet;
            span.completion = completion;
            f.setSpan(span, selectionStart, selectionEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(f);
        tv.setLinkTextColor(Color.BLUE);
        tv.setLinksClickable(true);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setFocusable(false);
    }
	
	public static class InternalURLSpan extends android.text.style.ClickableSpan {
        public String text;
        public Tweet tweet;
        public Async.Block<String, Tweet> completion;

        @Override
        public void onClick(View widget) {
            completion.call(text, tweet);
        }
    }
	
	public static boolean handleLinkClicked(final Context context, String value, Tweet tweet) {
	    if (value.startsWith("http") || value.equals(tweet.displayUrl)) {
	    	final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(tweet.displayUrl != null ? tweet.actualUrl : value));
	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	context.startActivity(intent);
	    	return true;
	    } else if (value.startsWith("@")) {
	    	TwitterClientApp.composeTweet(context, null, value.substring(1), Constants.ACTION_REPLY, 0);
	    }
	    return false;
	}
}
