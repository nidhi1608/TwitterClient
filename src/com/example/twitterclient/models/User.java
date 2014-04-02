package com.example.twitterclient.models;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.support.v4.util.LongSparseArray;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

@Table(name = "Users")
public class User extends Model implements Serializable {
	private static final long serialVersionUID = 1987797657500194901L;
	
	@Column(name = "name")
	public String name;
	
	@Column(name = "userId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
	public long userId;
	
	@Column(name = "screenName")
	public String screenName;
	
	@Column(name = "profileImageUrl")
	public String profileImageUrl;
	
	@Column(name = "numTweets")
	public int numTweets;
	
	@Column(name = "followersCount")
	public int followersCount;
	
	@Column(name = "friendsCount")
	public int friendsCount;
	
	private static LongSparseArray<User> sUsers = new LongSparseArray<User>();
	
	public User() {
		super();
	}
	
	public static User getUser(long userId) {
		return sUsers.get(userId);
	}
	
	public static void loadAll() {
		final List<User> users = new Select().from(User.class).execute();
		for (final User user : users) {
			sUsers.put(user.userId, user);
		}
	}
	
	public static void saveAll() {
		for (int i = 0, size = sUsers.size(); i < size; ++i) {
			final User user = sUsers.valueAt(i);
			user.save();
		}
	}

    public static User fromJson(JSONObject json) {
        User user = new User();
        try {
        	user.name = json.getString("name");
        	user.userId = json.getLong("id");
        	user.screenName = json.getString("screen_name");
        	user.profileImageUrl = json.getString("profile_image_url");
        	user.profileImageUrl = user.profileImageUrl.replace("_normal.", "_bigger.");
        	user.numTweets = json.getInt("statuses_count");
        	user.followersCount = json.getInt("followers_count");
        	user.friendsCount = json.getInt("friends_count");
        	sUsers.put(user.userId, user);
        } catch (JSONException e) {
            e.printStackTrace();
        } 
        return user;
    }
    
    public static User fromUserId(Long userId) {
		return new Select()
				.from(User.class)
				.where("userId = ?", userId)
				.limit(1).executeSingle();
	}
}
