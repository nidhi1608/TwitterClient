package com.example.twitterclient.helpers;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import com.example.twitterclient.models.Tweet;

public class CustomDateComparator implements Comparator<Long> {

	@Override
	public int compare(Long tweetId1, Long tweetId2) {
		final Tweet tweet1 = Tweet.getTweet(tweetId1);
		final Tweet tweet2 = Tweet.getTweet(tweetId2);
		Calendar calendar1 = Calendar.getInstance();
		calendar1.setTime(tweet1.createdDate);
		Calendar calendar2 = Calendar.getInstance();
		calendar2.setTime(tweet2.createdDate);
		return new Date(calendar2.getTimeInMillis()).compareTo(new Date(
				calendar1.getTimeInMillis()));
	}

}
