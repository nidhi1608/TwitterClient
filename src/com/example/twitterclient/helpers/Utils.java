package com.example.twitterclient.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.example.twitterclient.R;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	public static Date convertToDate(String date) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat(
				Constants.TWITTER_CLIENT_DATE_FORMAT, Locale.US);
		sf.setLenient(true);
		return sf.parse(date);
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null
				&& activeNetworkInfo.isConnectedOrConnecting();
	}

	public static enum TimelineType {
		HOME, MENTIONS, USER
	}

	public static enum RelatedUserType {
		FRIENDS, FOLLOWERS
	}

	public static int getNameResource(Utils.TimelineType type) {
		if (type.equals(Utils.TimelineType.HOME))
			return R.string.home;
		else if (type.equals(Utils.TimelineType.MENTIONS))
			return R.string.mentions;
		else if (type.equals(Utils.TimelineType.USER))
			return R.string.profile;
		return -1;
	}
}
