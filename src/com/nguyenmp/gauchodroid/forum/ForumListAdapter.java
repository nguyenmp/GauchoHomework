package com.nguyenmp.gauchodroid.forum;

import java.util.List;

import org.holoeverywhere.LayoutInflater;
import org.holoeverywhere.widget.TextView;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchospace.thing.Discussion;

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
		repliesTextView.setText("Replies: " + discussion.replies);
		
		TextView lastPostTextView = (TextView) convertView.findViewById(R.id.list_item_discussion_last_post);
		lastPostTextView.setText(discussion.lastPost.timestamp);
		
		//Return the generated view
		return convertView;
	}

}
