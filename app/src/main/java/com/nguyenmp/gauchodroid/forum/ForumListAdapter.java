package com.nguyenmp.gauchodroid.forum;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.StringUtils;
import com.nguyenmp.gauchospace.thing.Discussion;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ForumListAdapter extends BaseAdapter {
	private final List<Discussion> mDiscussions;
	private final Context mContext;
	
	ForumListAdapter(List<Discussion> discussions, Context context) {
		mDiscussions = discussions;
		mContext = context;
	}
	
	@Override
	public int getCount() {
		return mDiscussions.size();
	}

	@Override
	public Object getItem(int position) {
		return mDiscussions.get(position);
	}

	@Override
	public long getItemId(int id) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup container) {
		//Get the discussion we are supposed to represent
		Discussion discussion = mDiscussions.get(position);
		
		//If we are not recycling a view
		//Then generate a new view
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item_discussion, container, false);
		}
		
		//Initialize view with discussion's data
		TextView nameTextView = (TextView) convertView.findViewById(R.id.list_item_discussion_name);
		nameTextView.setText(discussion.name);
		
		TextView repliesTextView = (TextView) convertView.findViewById(R.id.list_item_discussion_replies);
		repliesTextView.setText(discussion.replies + " people have replied!");
		
		TextView lastPostTextView = (TextView) convertView.findViewById(R.id.list_item_discussion_last_post);
		DateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy, hh:mm aa");
		try {
			Date date = dateFormat.parse(discussion.lastPost.timestamp);
			String timeSinceEpoch = StringUtils.timeSinceEpochPrecise(date.getTime()/1000L);
			lastPostTextView.setText(timeSinceEpoch);
		} catch (ParseException e) {
			e.printStackTrace();
			lastPostTextView.setText(discussion.lastPost.timestamp);
		}
		
		//Return the generated view
		return convertView;
	}

}
