package com.example.twitterclient.helpers;


import android.content.Context;
import android.content.SearchRecentSuggestionsProvider;
import android.provider.SearchRecentSuggestions;

public class TwitterClientSearchSuggestions extends
		SearchRecentSuggestionsProvider {
	
	public TwitterClientSearchSuggestions() {
		setupSuggestions(TwitterClientSearchSuggestions.class.getName(), DATABASE_MODE_QUERIES);
	}
	
	public static SearchRecentSuggestions createSearchRecentSuggestions(final Context context) {
		return new SearchRecentSuggestions(context, TwitterClientSearchSuggestions.class.getName(), DATABASE_MODE_QUERIES);
	}

}
