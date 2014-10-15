//package com.nguyenmp.gauchodroid.account;
//
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.preference.PreferenceManager;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.actionbarsherlock.app.SherlockActivity;
//import com.nguyenmp.gauchodroid.R;
//
//public class AccountActivity extends SherlockActivity {
//	public static final String KEY_USERNAME = "key_web_access_username",
//			KEY_PASSWORD = "key_web_access_password";
//	
//	@Override
//	public void onCreate(Bundle inState) {
//		super.onCreate(inState);
//		setContentView(R.layout.account);
//		
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//
//		final EditText usernameEditText = (EditText) findViewById(R.id.account_username_edit_text);
//		final EditText passwordEditText = (EditText) findViewById(R.id.account_password_edit_text);
//
//		if (prefs.contains(AccountActivity.KEY_USERNAME))
//			usernameEditText.setText(prefs.getString(AccountActivity.KEY_USERNAME, ""));
//		if (prefs.contains(AccountActivity.KEY_PASSWORD))
//			passwordEditText.setText(prefs.getString(AccountActivity.KEY_PASSWORD, ""));
//		
//		final Button storeButton = (Button) findViewById(R.id.account_store_button);
//		
//		storeButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
//				prefs.edit().putString(KEY_USERNAME, usernameEditText.getText().toString())
//							.putString(KEY_PASSWORD, passwordEditText.getText().toString())
//							.commit();
//				AccountActivity.this.setResult(RESULT_OK);
//				Toast.makeText(AccountActivity.this, "Stored credentials!  You're ready.", Toast.LENGTH_SHORT).show();
//			}
//		});
//	}
//}
