package com.Straats.FriendRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.Straats.FriendRequest.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewFriend extends Activity implements Utils.AsyncResponse{
	
	//Loading control
	private ProgressBar loadBar;
	//Current user we are viewing
	private String friendUser;
	
	/**
	 * Sets the delegate and automatically gets the users profile.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_friend);
		Utils.DownloadWebpageTask.delegate = this;
		
		Intent intent = getIntent();
		//fix these
		String friendName = intent.getStringExtra(Utils.PACKAGE_NAME);
		friendUser = intent.getStringExtra("user");
		
		setTitle(friendName);
		
		loadBar = (ProgressBar)findViewById(R.id.progressBar1);
		loadBar.setVisibility(View.VISIBLE);
		
		if (Utils.CheckNetwork(this))
		{
			try 
    		{
    			String userUTF = URLEncoder.encode(friendUser, "UTF-8");
    			String currUser = URLEncoder.encode(Utils.user, "UTF-8");
    			String currPass = URLEncoder.encode(Utils.password, "UTF-8");
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&currUser=" + currUser + "&currPass=" + currPass;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "getprofile.php");
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to get Friends Profile");
    		}
		}
	}

	/**
	 * Removes the user from the current users friend list
	 * @param view
	 */
	public void removeUser(View view) {
		
		if (Utils.CheckNetwork(this))
		{
			try 
    		{
    			String userUTF = URLEncoder.encode(friendUser, "UTF-8");
    			String currUser = URLEncoder.encode(Utils.user, "UTF-8");
    			String currPass = URLEncoder.encode(Utils.password, "UTF-8");
    			
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&currUser=" + currUser + "&currPass=" + currPass;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "removefriend.php");
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to remove friend");
    		}
		}
		
	}
	
	/**
	 * Handles for removing users and getting user info.
	 */
	@Override
	public void processFinish(String output) {
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		else if (output.trim().equals("true") || output.trim().equals("false")) {
			Utils.ShortToast(this, "User has been removed from friends");
			//TODO: kick user back to main page
		}
		else {
			TextView text = (TextView) findViewById(R.id.textView2);
			text.setText(output);
			loadBar.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * Cancels async request and calls super.
	 */
	@Override
	 public void onBackPressed(){
	    Utils.cancelAsync();
	    super.onBackPressed();
	 }
}