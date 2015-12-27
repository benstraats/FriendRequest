package com.Straats.FriendRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.Straats.FriendRequest.R;
import com.Straats.FriendRequest.Utils.DownloadWebpageTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends Activity implements Utils.AsyncResponse{

	//The list of users friends
	private ListView friendList;
	//The number of pending friends the user has
	private int pendingFriends = 0;
	//The loading symbol control
	private ProgressBar loadBar;
	
	//The pending friends count server request object
	private DownloadWebpageTask pendCount;
	
	//Done adding all friends or not
	private boolean doneSearch = false;
	//if we are currently asking the server for friend list info
	private boolean searching = true;
	//The index of how many friends we have loaded
	private int index = 0;
	
	/**
	 * Creates a listener for when a user is clicked on the friend page to open 
	 * their profile.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		loadBar = (ProgressBar)findViewById(R.id.progressBar1);
		
		friendList = (ListView) findViewById(R.id.friendList);
		
		friendList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
			{
				Map<String, String> selectedFriend = (Map<String, String>)adapter.getItemAtPosition(position);
				Intent intent = new Intent(v.getContext(), ViewFriend.class);
				intent.putExtra(Utils.PACKAGE_NAME, selectedFriend.get("Name"));
				intent.putExtra("user", selectedFriend.get("Username"));
				startActivity(intent);
			}
		});
		
		friendList.setOnScrollListener(listScroll);
	}
	
	/**
	 * Creates a scroll listener for when the user scrolls to the bottom of their
	 * current friend list to ask the server for 20 more friend results.
	 */
	OnScrollListener listScroll = new OnScrollListener() 
	{
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
			//if its at the bottom, its not already searching, and there is more to search
			if (view.getLastVisiblePosition() + 1 == view.getCount() && !searching && !doneSearch) {
				
				index += 20;
				
				try {
					
					String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
	    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&index=" + index;
	    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "getfriends.php");
	    			searching = true;
	    			loadBar.setVisibility(View.VISIBLE);
	 
				} catch (UnsupportedEncodingException e) {
					Utils.ShortToast(view.getContext(), "Failed to Connect");
				}
			}
		}
	};


	/**
	 * Sets the delegate to this class and asks the server for the first 20 friends of the current user.
	 */
    @Override
	protected void onStart(){
    	super.onStart();
		Utils.DownloadWebpageTask.delegate = this;
		loadBar.setVisibility(View.VISIBLE);
		index = 0;
		searching = true;
		doneSearch = false;
		Utils.WipeList(this, friendList);
		
		if (Utils.CheckNetwork(this))
		{
			try 
    		{
				searching = true;
    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&index=" + index;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "getfriends.php");
    			
    			pendCount = new Utils.DownloadWebpageTask();
    			pendCount.execute(Utils.serverIP + "pendingfriendcount.php");
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to Get Friends List");
    		}
		}
	}
    
    /**
     * Creates a drop down menu of options for other activities the user can access.
     */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
    	menu.clear();
    	menu.add("Edit Profile");
    	menu.add("Add Friends");
    	if (pendingFriends != 0) {
    		menu.add("Pending Friends (" + pendingFriends + ")");
    	}
    	else {
    		menu.add("Pending Friends");
    	}
    	menu.add("FAQ");
		menu.add("Log Out");
		return true;
	}

    /**
     * Listener for when one of the drop down menu items is selected.
     * Opens the respective activity.
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		Utils.cancelAsync();
		pendCount.cancel(true);
		if (item.getTitle().equals("Edit Profile")) {
			Intent intent = new Intent(this, MyProfile.class);
			startActivity(intent);
			return true;
		}
		else if (item.getTitle().equals("Add Friends")) {
			Intent intent = new Intent(this, FindFriend.class);
			startActivity(intent);
			return true;
		}
		else if (item.getTitle().toString().contains("Pending Friends")) {
			Intent intent = new Intent(this, PendingFriends.class);
			startActivity(intent);
			return true;
		}
		else if (item.getTitle().equals("FAQ")) {
			Intent intent = new Intent(this, FAQ.class);
			startActivity(intent);
			return true;
		}
		else if (item.getTitle().equals("Log Out")) {
			Utils.RemoveUser(this);
			Utils.user = null;
			Utils.password = null;
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
	/**
	 * When back button is pressed will cancel any asynchronouse server requests and close
	 * the app fully. We do not want to return to the login or sign up page.
	 */
    @Override
    public void onBackPressed(){
    	Utils.cancelAsync();
    	pendCount.cancel(true);
    	Utils.WipeList(this, friendList);
    	Intent intent = new Intent(Intent.ACTION_MAIN);
    	intent.addCategory(Intent.CATEGORY_HOME);
    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    	startActivity(intent);
    }
	
    /**
     * Processes the returned output from the server.
     * Handles any outstanding toasts that the user needs to be modified about and handles
     * any additional friends that need to be added to the list. Finally it also handles
     * the pending friend requests count.
     */
	@Override
	public void processFinish(String output) {
		
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		//if we are getting the friend names/usernames
		else if (output.trim().equals("") || output.contains(",") || output.contains("\n")){
			Utils.SetList(this, friendList, Utils.ProcessList(output));
			loadBar.setVisibility(View.INVISIBLE);
			if (output.contains("\n")) {
				doneSearch = true;
			}
			searching = false;
		}
		else {
			pendingFriends = Integer.parseInt(output.trim());
			invalidateOptionsMenu();//refresh the drop down menu
		}
	}
}
