package com.nguyenmp.gauchodroid;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * A container class that holds the data of a photo download 
 * from gauchospace.  It holds the bitmap of the image, the 
 * name of the uploader (display name), as well as the title 
 * of the image.
 * @author Mark Nguyen
 */
public class PhotoDownload implements Parcelable {

	/** the display name of the uploader */
	public final String displayName;
	
	/** the title of the image */
	public final String title;
	
	/** the bitmap of the actual image */
	public final Bitmap bitmap;
	
	/** the url of the image */
	public final String url;
	
	/**
	 * Creates a new PhotoDownload container holding the given parameters
	 * @param displayName the display name of the uploader (username)
	 * @param title the title of the piece
	 * @param bitmap the bitmap of the image
	 */
	PhotoDownload(String displayName, String title, Bitmap bitmap, String url) {
		this.displayName = displayName;
		this.title = title;
		this.bitmap = bitmap;
		this.url = url;
	}

	@Override
	public int describeContents() {
		return super.hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		//Ignore the flag
		
		//Store the fields into the parcel
		dest.writeString(title);
		dest.writeString(displayName);
		dest.writeParcelable(bitmap, flags);
		dest.writeString(url);
	}
	
	public PhotoDownload(Parcel in) {
		title = in.readString();
		displayName = in.readString();
		bitmap = in.readParcelable(Bitmap.class.getClassLoader());
		url = in.readString();
	}
	
	public static final Parcelable.Creator<PhotoDownload> CREATOR = 
			new Parcelable.Creator<PhotoDownload>() {
				@Override
				public PhotoDownload createFromParcel(Parcel source) {
					return new PhotoDownload(source);
				}

				@Override
				public PhotoDownload[] newArray(int size) {
					return new PhotoDownload[size];
				}
			};
}