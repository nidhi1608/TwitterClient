package com.example.twitterclient.adapters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

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
import android.widget.Toast;

import com.example.twitterclient.R;
import com.example.twitterclient.activities.TweetDetailActivity;
import com.example.twitterclient.helpers.Async;
import com.example.twitterclient.helpers.Constants;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.ViewHolder;
import com.example.twitterclient.models.Tweet;
import com.example.twitterclient.models.Tweet.OnTweetChangedListener;
import com.example.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class TweetsAdapter extends ArrayAdapter<Long> {
	private ImageLoader mImageLoader;
	private View mLastLinkView;

	public TweetsAdapter(Context context) {
		super(context, 0);
	}

	private static final String getRelativeTime(final Context context,
			final Date date) {
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
		final String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		final String month = calendar.getDisplayName(Calendar.MONTH,
				Calendar.SHORT, locale);
		if (year == currentYear) {
			return day + " " + month;
		} else {
			final String yearString = calendar.getDisplayName(Calendar.YEAR,
					Calendar.SHORT, locale);
			return day + " " + month + " " + yearString;
		}
	}

	private OnClickListener populate(final ViewHolder.TweetItem holder,
			final long tweetId) {
		final Tweet tweet = Tweet.getTweet(tweetId);
		if (tweet.retweetedUserId != 0) {
			holder.btTweetStatus.setVisibility(View.VISIBLE);
			holder.btTweetStatus.setText(" "
					+ User.getUser(tweet.retweetedUserId).name + " retweeted");
		} else {
			holder.btTweetStatus.setVisibility(View.GONE);
		}
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
		final User user = User.getUser(tweet.userId);
		mImageLoader.displayImage(user.profileImageUrl, holder.ivProfile);

		String formattedName = "<b>" + user.name + "</b>"
				+ "<small><font color='#777777'> @" + user.screenName
				+ "</font></small>";
		holder.tvName.setText(Html.fromHtml(formattedName));

		holder.tvDate.setText(getRelativeTime(getContext(), tweet.createdDate));

		String body = tweet.body;
		if (tweet.actualUrl != null && tweet.displayUrl != null) {
			body = body.replace(tweet.actualUrl, tweet.displayUrl);
		}
		holder.tvBody.setText(body);
		setLinks(holder.tvBody, holder.tvBody.getText().toString(),
				tweet.tweetId, false, new Async.Block<String, Long>() {
					@Override
					public void call(final String text, final Long tweetId) {
						if (handleLinkClicked(getContext(), text, tweetId)) {
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
					Intent intent = new Intent(getContext(),
							TweetDetailActivity.class);
					intent.putExtra("tweetId", tweet.tweetId);
					getContext().startActivity(intent);
				}
				mLastLinkView = null;
			}
		};
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
		if (tweet.favorited) {
			holder.btFav.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_status_favorite_yellow, 0, 0, 0);
			holder.btFav.setTextColor(Color.parseColor("#efe836"));
		} else {
			holder.btFav.setCompoundDrawablesWithIntrinsicBounds(
					R.drawable.ic_status_favorite, 0, 0, 0);
			holder.btFav.setTextColor(Color.parseColor("#777777"));
		}

		holder.btReply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(getContext(), tweet.tweetId,
						Constants.ACTION_REPLY, 0);
			}
		});
		holder.btRetweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.composeTweet(getContext(), tweet.tweetId,
						Constants.ACTION_RETWEET, 0);
			}
		});
		holder.btFav.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!tweet.favorited) {
					TwitterClientApp.getClient().favoriteTweet(tweet.tweetId,
							new JsonHttpResponseHandler() {
								@Override
								public void onSuccess(JSONObject json) {
									holder.btFav
											.setCompoundDrawablesWithIntrinsicBounds(
													R.drawable.ic_status_favorite_yellow,
													0, 0, 0);
									holder.btFav.setTextColor(Color
											.parseColor("#efe836"));
									tweet.favorited = true;
									tweet.favouritesCount = tweet.favouritesCount + 1;
									holder.btFav.setText(" "
											+ tweet.favouritesCount);
								}

								@Override
								public void onFailure(Throwable e) {
									e.printStackTrace();
								}

								@Override
								protected void handleFailureMessage(
										Throwable arg0, String arg1) {
									Toast.makeText(getContext(),
											arg0.toString(), Toast.LENGTH_LONG)
											.show();
								}
							});
				} else {
					TwitterClientApp.getClient().unFavoriteTweet(tweet.tweetId,
							new JsonHttpResponseHandler() {
								@Override
								public void onSuccess(JSONObject json) {
									holder.btFav
											.setCompoundDrawablesWithIntrinsicBounds(
													R.drawable.ic_status_favorite,
													0, 0, 0);
									holder.btFav.setTextColor(Color
											.parseColor("#777777"));
									tweet.favorited = false;
									tweet.favouritesCount = tweet.favouritesCount - 1;
									if (tweet.favouritesCount > 0) {
										holder.btFav.setText(" "
												+ tweet.favouritesCount);
									} else {
										holder.btFav.setText("");
									}
								}

								@Override
								public void onFailure(Throwable e) {
									e.printStackTrace();
								}

								@Override
								protected void handleFailureMessage(
										Throwable arg0, String arg1) {
									Toast.makeText(getContext(),
											arg0.toString(), Toast.LENGTH_LONG)
											.show();
								}
							});
				}

			}
		});
		holder.ivProfile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TwitterClientApp.showUserProfile(getContext(), tweet.userId);
			}
		});
		return onClickListener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflator = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(R.layout.tweet_item, null);
			final ViewHolder.TweetItem holder = new ViewHolder.TweetItem();
			holder.ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
			holder.tvName = (TextView) view.findViewById(R.id.tvName);
			holder.tvDate = (TextView) view.findViewById(R.id.tvDate);
			holder.tvBody = (TextView) view.findViewById(R.id.tvBody);
			holder.btTweetStatus = (Button) view
					.findViewById(R.id.btTweetStatus);
			holder.btReply = (Button) view
					.findViewById(R.id.button_reply_small);
			holder.btRetweet = (Button) view
					.findViewById(R.id.button_retweet_small);
			holder.btFav = (Button) view
					.findViewById(R.id.button_favorite_small);
			view.setTag(holder);
		}
		final ViewHolder.TweetItem holder = (ViewHolder.TweetItem) view
				.getTag();
		final Long tweetId = getItem(position);
		final OnClickListener listener = populate(holder, tweetId);
		view.setOnClickListener(listener);
		holder.registerListenerForTweetId(tweetId,
				new OnTweetChangedListener() {
					@Override
					public void tweetDidChange(final Long tweetId) {
						populate(holder, tweetId);
					}
				});
		return view;
	}

	public static void setLinks(TextView tv, String text, Long tweetId,
			boolean senseTwitterHandles,
			final Async.Block<String, Long> completion) {
		final ArrayList<String> linkPatterns = new ArrayList<String>();
		linkPatterns
				.add("([Hh][tT][tT][pP][sS]?:\\/\\/[^ ,'\">\\]\\)]*[^\\. ,'\">\\]\\)])");
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
				span.tweetId = tweetId;
				span.completion = completion;
				f.setSpan(span, selectionStart, selectionEnd,
						android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		final Tweet tweet = Tweet.getTweet(tweetId);
		if (tweet.displayUrl != null) {
			int selectionStart = text.indexOf(tweet.displayUrl);
			int selectionEnd = selectionStart + tweet.displayUrl.length();
			InternalURLSpan span = new InternalURLSpan();
			span.text = text.substring(selectionStart, selectionEnd);
			span.tweetId = tweetId;
			span.completion = completion;
			f.setSpan(span, selectionStart, selectionEnd,
					android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		tv.setText(f);
		tv.setLinkTextColor(Color.BLUE);
		tv.setLinksClickable(true);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setFocusable(false);
	}

	public static class InternalURLSpan extends
			android.text.style.ClickableSpan {
		public String text;
		public Long tweetId;
		public Async.Block<String, Long> completion;

		@Override
		public void onClick(View widget) {
			completion.call(text, tweetId);
		}
	}

	public static boolean handleLinkClicked(final Context context,
			String value, Long tweetId) {
		final Tweet tweet = Tweet.getTweet(tweetId);
		if (value.startsWith("http") || value.equals(tweet.displayUrl)) {
			final Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(tweet.displayUrl != null ? tweet.actualUrl
							: value));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			return true;
		} else if (value.startsWith("@")) {
			TwitterClientApp.composeTweet(context, null, value.substring(1),
					Constants.ACTION_REPLY, 0);
		}
		return false;
	}
}
