package com.nguyenmp.gauchodroid.common;

public class StringUtils {
	
	/** returns a string representing the time since Unix Epoch. ex:
	 * <ul>
	 * <li> 3 minutes ago.</li>
	 * <li> 25 years ago. </li>
	 * <li> 9000 years ago. </li>
	 * </ul>
	 * @param created UTC of the item created since Epoch in seconds.*/
	public static String timeSinceEpoch(long created) {
		//Set the timestamp string
		long time = System.currentTimeMillis()/1000L - created;
		String timeString = "";
		if (time >= 31536000) timeString = (time / 31536000) + " years ago";
		else if (time >= 2592000) timeString = (time / 2592000) + " months ago";
		else if (time >= 86400) timeString = (time / 86400) + " days ago";
		else if (time >= 3600) timeString = (time / 3600) + " hours ago";
		else if (time >= 60) timeString = (time / 60) + " minutes ago";
		else timeString = time + " seconds ago";
		
		return timeString;
	}
	
	/** returns a string representing the time since Unix Epoch. ex:
	 * <ul>
	 * <li> 3 minutes ago.</li>
	 * <li> 25 years ago. </li>
	 * <li> 9000 years ago. </li>
	 * </ul>
	 * @param created UTC of the item created since Epoch in seconds.*/
	public static String timeSinceEpochPrecise(long created) {
		//Set the timestamp string
		long time = System.currentTimeMillis()/1000L - created;
		String timeString = "";
		if (time >= 31536000) timeString = (time / 31536000) + " years and " + (time % 31536000) / 2592000 + " months ago";
		else if (time >= 2592000) timeString = (time / 2592000) + " months and " + (time % 2592000) / 86400 + " days ago";
		else if (time >= 86400) timeString = (time / 86400) + " days and " + (time % 86400) / 3600 + " hours ago";
		else if (time >= 3600) timeString = (time / 3600) + " hours and " + (time % 3600) / 60 + " minutes ago";
		else if (time >= 60) timeString = (time / 60) + " minutes and " + (time % 60) / 1 + " seconds ago";
		else timeString = time + " seconds ago";
		
		return timeString;
	}
}
