package com.nguyenmp.gauchodroid;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A container for the published event datas including:
 * title
 * time
 * url
 * image
 * description
 * @author Mark Nguyen
 *
 */
public class EventDownload implements Parcelable {
	public final String title, time, url, description;
	public final Bitmap image;
	
	public EventDownload(String title, String time, String description) {
		this(title, time, description, null, null);
	}
	
	public EventDownload(String title, String time, String description, String url, Bitmap image) {
		this.title = title;
		this.time = time;
		this.description = description;
		this.url = url;
		this.image = image;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(title);
		dest.writeString(time);
		dest.writeString(description);
		dest.writeString(url);
		dest.writeParcelable(image, flags);
	}
	
	public EventDownload(Parcel source) {
		title = source.readString();
		time = source.readString();
		description = source.readString();
		url = source.readString();
		image = source.readParcelable(Bitmap.class.getClassLoader());
	}
	
	public static final Parcelable.Creator<EventDownload> CREATOR = 
			new Parcelable.Creator<EventDownload>() {
				@Override
				public EventDownload createFromParcel(Parcel source) {
					return new EventDownload(source);
				}

				@Override
				public EventDownload[] newArray(int size) {
					return new EventDownload[size];
				}
			};
}