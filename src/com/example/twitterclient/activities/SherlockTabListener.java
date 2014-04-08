package com.example.twitterclient.activities;

import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragment;
import com.example.twitterclient.fragments.TweetsListFragment;
import com.example.twitterclient.helpers.Utils;

public class SherlockTabListener<T extends SherlockFragment> implements TabListener {
	private SherlockFragment mFragment;
	private final String mTag;
	private Utils.TimelineType mTimelineType;
	private final int mfragmentContainerId;

	// This version defaults to replacing the entire activity content area
	// new SherlockTabListener<SomeFragment>(this, "first", SomeFragment.class))
	public SherlockTabListener(String tag, Utils.TimelineType timelineType) {
		mTag = tag;
		mTimelineType = timelineType;
		mfragmentContainerId = android.R.id.content;
	}

	// This version supports specifying the container to replace with fragment content
	// new SherlockTabListener<SomeFragment>(R.id.flContent, this, "first", SomeFragment.class))
	public SherlockTabListener(int fragmentContainerId, String tag, Utils.TimelineType timelineType) {
		mTag = tag;
		mTimelineType = timelineType;
		mfragmentContainerId = fragmentContainerId;
	}

	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// Check if the fragment is already initialized
		if (mFragment == null) {
			// If not, instantiate and add it to the activity
			mFragment = TweetsListFragment.newInstance(mTimelineType, null, null);
			ft.add(mfragmentContainerId, mFragment, mTag);
		} else {
			// If it exists, simply attach it in order to show it
			ft.attach(mFragment);
		}
	}

	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		if (mFragment != null) {
			// Detach the fragment, because another one is being attached
			ft.detach(mFragment);
		}
	}

	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// User selected the already selected tab. Usually do nothing.
	}
}
