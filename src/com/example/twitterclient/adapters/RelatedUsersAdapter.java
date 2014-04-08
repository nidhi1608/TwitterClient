package com.example.twitterclient.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twitterclient.R;
import com.example.twitterclient.activities.ProfileActivity;
import com.example.twitterclient.activities.TweetDetailActivity;
import com.example.twitterclient.helpers.TwitterClientApp;
import com.example.twitterclient.helpers.ViewHolder;
import com.example.twitterclient.models.User;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class RelatedUsersAdapter extends ArrayAdapter<User>{
	private ImageLoader mImageLoader;
	public String next_cursor;

	public RelatedUsersAdapter(Context context) {
		super(context, 0);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if(view == null) {
			LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflator.inflate(R.layout.related_user_item, null);
			final ViewHolder.UserItem holder = new ViewHolder.UserItem();
			holder.ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
			holder.tvName = (TextView) view.findViewById(R.id.tvName);
			holder.tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
			holder.tvDescription = (TextView) view.findViewById(R.id.tvDescription);
			view.setTag(holder);
		}
		final ViewHolder.UserItem holder = (ViewHolder.UserItem) view.getTag();
		final User user = (User) getItem(position);
		
		holder.ivProfile = (ImageView) view.findViewById(R.id.ivProfile);
		mImageLoader = ImageLoader.getInstance();
		mImageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
		mImageLoader.displayImage(user.profileImageUrl, holder.ivProfile);
		
		holder.tvName = (TextView) view.findViewById(R.id.tvName);
		String formattedName = "<b>" + user.name + "</b>";
		holder.tvName.setText(Html.fromHtml(formattedName));
		
		holder.tvScreenName = (TextView) view.findViewById(R.id.tvScreenName);
		String formattedScreenName = "<small><font color='#777777'> @" + user.screenName + "</font></small>";
		holder.tvScreenName.setText(Html.fromHtml(formattedScreenName));
		
		holder.tvDescription = (TextView) view.findViewById(R.id.tvDescription);
		holder.tvDescription.setText(user.description);
		
		View.OnClickListener onClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {				
				TwitterClientApp.showUserProfile(getContext(), user.userId);
			}
		};
		view.setOnClickListener(onClickListener);
		return view;
	}

}
