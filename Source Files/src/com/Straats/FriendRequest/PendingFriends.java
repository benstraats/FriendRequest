package com.Straats.FriendRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.Straats.FriendRequest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class PendingFriends extends Activity implements Utils.AsyncResponse {

	//The list of pending friends
	private ListView friendList;
	//The loading symbol control
	private ProgressBar loadBar;
	
	//If the list of requested friends has finished loading fully
	private boolean doneSearch = false;
	//If the current list of 20 requested friends has been finished
	private boolean searching = true;
	//What index of friends do we need to load from
	private int index = 0;
	
	//needed static string to be accessed in dialog onclick
	private static String selectedUser;
	
	/**
	 * Sets the delegate to this class.
	 * Automatically gets the first 20 requested users.
	 * Sets an onclick listener for when the user clicks a user in the list it will
	 * create a dialogue that the user can choose to accept or decline the request.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pending_friends);
		Utils.DownloadWebpageTask.delegate = this;
		loadBar = (ProgressBar)findViewById(R.id.progressBar1);
		loadBar.setVisibility(View.VISIBLE);
		
		friendList = (ListView) findViewById(R.id.friendList);
		
		Utils.WipeList(this, friendList);
		
		index = 0;
		
		if (Utils.CheckNetwork(this))
		{
			try 
    		{
    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&index=" + index;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "pendingfriends.php");
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to Connect");
    		}
		}
		
		friendList.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
			{
				Map<String, String> selectedFriend = (Map<String, String>)adapter.getItemAtPosition(position);
				selectedUser = selectedFriend.get("Username");
				
				AlertDialog.Builder alert = Utils.CreateDialog(PendingFriends.this, "Friend Request Response", "Accept or Decline request from " + selectedFriend.get("Name") + ".");
				alert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (Utils.CheckNetwork(PendingFriends.this))
						{
							try 
				    		{
								
				    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
				    			String friendUserUTF = URLEncoder.encode(selectedUser, "UTF-8");
				    			String passUTF = URLEncoder.encode(Utils.password, "UTF-8");
					   			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&fuser=" + friendUserUTF + "&pass=" + passUTF;
					   			new Utils.DownloadWebpageTask().execute(Utils.serverIP + "acceptfriend.php");
					   			
					   		}
					    	catch (UnsupportedEncodingException e) {
					    		Utils.ShortToast(PendingFriends.this, "test");
					    	}
						}
					}
				});
				
				alert.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (Utils.CheckNetwork(PendingFriends.this))
						{
							try 
				    		{
								
				    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
				    			String friendUserUTF = URLEncoder.encode(selectedUser, "UTF-8");
				    			String passUTF = URLEncoder.encode(Utils.password, "UTF-8");
					   			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&fuser=" + friendUserUTF + "&pass=" + passUTF;
					   			new Utils.DownloadWebpageTask().execute(Utils.serverIP + "declinefriend.php");
					   			
					   		}
					    	catch (UnsupportedEncodingException e) {
					    		Utils.ShortToast(PendingFriends.this, "test");
					    	}
						}
					}
				});
				
				alert.show();
			}
		});
		friendList.setOnScrollListener(listScroll);
	}
	
	/**
	 * A listener for when the user scrolls to the bottom of the list to get the next 20 results.
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
	 * Handler for handling the text that the server returns.
	 */
	@Override
	public void processFinish(String output) {
		
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		//TODO: remove user that has been accepted/declined from the list. (Requires server to send back username instead of name)
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		//Populate the list
		else if (output.trim().equals("") || output.trim().contains(",") || output.contains("\n")) {
			Utils.SetList(this, friendList, Utils.ProcessList(output));
			if (output.contains("\n")) {
				doneSearch = true;
			}
			searching = false;
		}
		loadBar.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Cancels the current async request and calls super.
	 */
	@Override
	 public void onBackPressed(){
	    Utils.cancelAsync();
	    Utils.WipeList(this, friendList);
	    super.onBackPressed();
	 }
}
