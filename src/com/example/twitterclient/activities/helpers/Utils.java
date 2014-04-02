package com.example.twitterclient.activities.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	public static Date convertToDate(String date) throws ParseException {
		SimpleDateFormat sf = new SimpleDateFormat(Constants.TWITTER_CLIENT_DATE_FORMAT, Locale.US);
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
}
