package com.nguyenmp.gauchodroid.course;

import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.http.client.CookieStore;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.nguyenmp.gauchodroid.R;
import com.nguyenmp.gauchodroid.common.HandledThread;
import com.nguyenmp.gauchodroid.login.LoginManager;
import com.nguyenmp.gauchospace.GauchoSpaceClient;
import com.nguyenmp.gauchospace.thing.grade.GradeFolder;
import com.nguyenmp.gauchospace.thing.grade.GradeItem;

public class GradesFragment extends SherlockFragment implements GradesDownloadListener {
	public static final String ARGUMENT_COURSE_ID = "argument_course_id";
	private LinearLayout mContentView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle inState) {
		mContentView = (LinearLayout) inflater.inflate(R.layout.fragment_grades, container, false);
		
		int courseID = getArguments().getInt(ARGUMENT_COURSE_ID);
		CookieStore cookies = LoginManager.getCookies(getActivity());
		Handler downloadHandler = new GradesDownloadHandler(this);
		Thread downloadThread = new GradesDownloadThread(courseID, cookies, downloadHandler);
		downloadThread.start();
		
		return mContentView;
	}
	
	private static class GradesDownloadThread extends HandledThread {
		private final int mCourseID;
		private final CookieStore mCookies;
		
		GradesDownloadThread(int courseID, CookieStore cookies, Handler handler) {
			setHandler(handler);
			mCourseID = courseID;
			mCookies = cookies;
		}
		
		@Override
		public void run() {
			try {
				GradeFolder grades = GauchoSpaceClient.getGrade(mCourseID, mCookies);
				dispatchMessage(grades);
			} catch (SAXNotRecognizedException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (SAXNotSupportedException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (IOException e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (TransformerFactoryConfigurationError e) {
				dispatchMessage(e);
				e.printStackTrace();
			} catch (TransformerException e) {
				dispatchMessage(e);
				e.printStackTrace();
			}
		}
	}
	
	private static class GradesDownloadHandler extends Handler {
		private final GradesDownloadListener mListener;
		
		GradesDownloadHandler(GradesDownloadListener listener) {
			mListener = listener;
		}
		
		@Override
		public void handleMessage(Message message) {
			if (message.obj == null) {
				//TODO: Handle the null case
			} else if (message.obj instanceof GradeFolder) {
				mListener.onDownloaded((GradeFolder) message.obj);
			} else if (message.obj instanceof Exception) {
				//TODO: Handle exception
			} else {
				//TODO: Handle unknown error
			}
		}
	}
	
	@Override
	public void onDownloaded(GradeFolder grades) {
		setGrades(grades);
	}

	private void setGrades(GradeFolder grades) {
		mContentView.addView(getGradesView(grades));
	}
	
	private View getGradesView(GradeFolder grades) {
		//Create a new view
		View inflatedView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_grade_folder, null, false);
		
		//Initialize the title
		TextView titleTextView = (TextView) inflatedView.findViewById(R.id.list_item_grade_folder_title);
		titleTextView.setText(grades.getTitle());
		
		//Add the children to the view
		LinearLayout contentView = (LinearLayout) inflatedView.findViewById(R.id.list_item_grade_folder_content);
		for (GradeItem item : grades.getItems()) {
			//Declare the item's view
			View itemView = null;
			
			//Generate view based on item type
			if (item instanceof GradeFolder) {
				itemView = getGradesView((GradeFolder) item);
			} else {
				itemView = getGradesView(item);
			}
			
			//Add view to parent
			contentView.addView(itemView);
		}
		
		//Initialize the details field
		TextView detailTextView = (TextView) inflatedView.findViewById(R.id.list_item_grade_folder_details);
		detailTextView.setText(String.format("%s %s %s %s %s", grades.getName(), grades.getGrade(), grades.getRange(), grades.getPercentage(), grades.getFeedback()));
		
		return inflatedView;
	}
	
	private View getGradesView(GradeItem item) {
		//Inflate new view
		View inflatedView = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_grade_item, null, false);
		
		//Initialize the title
		TextView titleTextView = (TextView) inflatedView.findViewById(R.id.list_item_grade_item_title);
		titleTextView.setText(String.format("%s %s %s %s %s", item.getName(), item.getGrade(), item.getRange(), item.getPercentage(), item.getFeedback()));
		
		//Return the inflated view
		return inflatedView;
	}
}