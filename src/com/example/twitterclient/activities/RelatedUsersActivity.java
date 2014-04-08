package com.example.twitterclient.activities;

import java.util.ArrayList;
import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;
import com.example.twitterclient.R;
import com.example.twitterclient.adapters.RelatedUsersAdapter;
import com.example.twitterclient.helpers.Async;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.Utils;
import com.example.twitterclient.helpers.Utils.RelatedUserType;
import com.example.twitterclient.models.User;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.loopj.android.http.JsonHttpResponseHandler;

public class RelatedUsersActivity extends AppActivity implements 
PullToRefreshBase.OnLastItemVisibleListener,
PullToRefreshBase.OnRefreshListener<ListView> {
	private Utils.RelatedUserType relatedUserType;
	private PullToRefreshListView lvRelatedUsers;
	private RelatedUsersAdapter relatedUsersAdapter;
	private Long userId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_related_users);
		lvRelatedUsers = (PullToRefreshListView) findViewById(R.id.lvRelatedUsers);
		lvRelatedUsers.setOnRefreshListener(this);
		lvRelatedUsers.setOnLastItemVisibleListener(this);
		userId = getIntent().getLongExtra("userId", 0L);
		relatedUserType = (RelatedUserType) getIntent().getSerializableExtra("relatedUserType");
		relatedUsersAdapter = new RelatedUsersAdapter(this);
		lvRelatedUsers.setAdapter(relatedUsersAdapter);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(relatedUserType == RelatedUserType.FRIENDS ? "Following" : "Followers");
		loadNextPage();
	}
	
	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		refresh();
	}

	@Override
	public void onLastItemVisible() {
		loadNextPage();
	}
	
	private void refresh() {
		relatedUsersAdapter.next_cursor = null;
		performRequest(true);
	}	
	
	private void performRequest(final boolean clearOnSuccess) {
		getProgressBar().setVisibility(View.VISIBLE);
		getRelatedUsers(this, relatedUsersAdapter, userId, relatedUserType, clearOnSuccess, new Runnable() {
			@Override
			public void run() {
				getProgressBar().setVisibility(View.INVISIBLE);
				if (lvRelatedUsers.isRefreshing()) {
					lvRelatedUsers.onRefreshComplete();
				}
			}
		});
	}
	
	private void loadNextPage() {
		performRequest(false);
	}
	
	private static void getRelatedUsers(final Context context, final RelatedUsersAdapter adapter,
			final long userId, final RelatedUserType relatedUserType, final boolean clearOnSuccess, final Runnable completion) {
		final String relatedUser = relatedUserType == RelatedUserType.FRIENDS ? "friends" : "followers";
		TwitterClientApp.getClient().getRelatedUsers(userId, relatedUser, adapter.next_cursor, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(final JSONObject response) {
				if (!response.has("users")) return;
				try {
					final String nextCursor = response.has("next_cursor") ? response.getString("next_cursor") : null;
					final JSONArray jsonUsers = response.getJSONArray("users");
					Async.dispatch(new Runnable() {
						@Override
						public void run() {
							final ArrayList<User> users = User.fromJson(jsonUsers);
							ActiveAndroid.beginTransaction();
							try {
								User.saveAll();
								ActiveAndroid.setTransactionSuccessful();
							} finally {
								ActiveAndroid.endTransaction();
							}
							Async.dispatchMain(new Runnable() {
								@Override
								public void run() {
									if (clearOnSuccess) {
										adapter.clear();
									}
									final HashSet<Long> existingUserIds = new HashSet<Long>();
									for (int i = 0, count = adapter.getCount(); i < count; ++i) {
										existingUserIds.add(adapter.getItem(i).userId);
									}
									final ArrayList<User> usersToAdd = new ArrayList<User>();
									for (final User user : users) {
										if (existingUserIds.contains(user.userId)) continue;
										usersToAdd.add(user);
									}
									adapter.addAll(usersToAdd);
									adapter.next_cursor = nextCursor;
									if (completion != null) {
										completion.run();
									}
								}
							});
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					if (completion != null) {
						completion.run();
					}
				}
			} 

			@Override
			public void onFailure(Throwable e) {
				e.printStackTrace();
				if (completion != null) {
					completion.run();
				}
			}
		});
	}

}
