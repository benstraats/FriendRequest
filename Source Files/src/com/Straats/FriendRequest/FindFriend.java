package com.Straats.FriendRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import com.Straats.FriendRequest.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ProgressBar;

public class FindFriend extends Activity implements Utils.AsyncResponse {
	
	//The list of searched people
	private ListView friendList;
	//The search button to start a new search
	private Button search;
	//The loading bar smybol
	private ProgressBar loadBar;
	//Used if current search has no more results to give
	private boolean doneSearch = false;
	//Used to see if we are already waiting for a response from the server
	private boolean searching = true;
	//How many users we have gotten from the server
	private int index = 0;
	//What the user has searched for
	private String searchStr = "";

	/**
	 * Create a listener for the list of users that were searched for.
	 * When a user is clicked it will send a request to the user to be a friend.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_find_friend);
		Utils.DownloadWebpageTask.delegate = this;
		
		friendList = (ListView) findViewById(R.id.friendList);
		search = (Button)findViewById(R.id.button1);
		loadBar = (ProgressBar)findViewById(R.id.progressBar1);
		
		hideLoad();
		
		friendList.setOnItemClickListener(new OnItemClickListener(){
			
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3)
			{
				//check this?
				Map<String, String> selectedFriend = (Map<String, String>)adapter.getItemAtPosition(position);
				String friendUser = selectedFriend.get("Username");
				
				//Save friendUser's entry in requests table
				if (Utils.CheckNetwork(v.getContext()))
				{
					try 
		    		{
		    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
		    			String friendUserUTF = URLEncoder.encode(friendUser, "UTF-8");
		    			String passUTF = URLEncoder.encode(Utils.password, "UTF-8");
		    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&fuser=" + friendUserUTF + "&pass=" + passUTF;
		    			new Utils.DownloadWebpageTask().execute(Utils.serverIP + "requestfriend.php");
		    		}
		    		catch (UnsupportedEncodingException e) {
		    			Utils.ShortToast(v.getContext(), "Failed to LogIn");
		    		}
				}
			}
		});
		
		friendList.setOnScrollListener(listScroll);
	}
	
	/**
	 * Create a listener for when the user scrolls to the bottom of the current list.
	 * When it happens ask the server for 20 more results of the same query and add
	 * them to the current list.
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
					String searchUTF = URLEncoder.encode(searchStr, "UTF-8");
					Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&search=" + searchUTF + "&index=" + index;
	    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "search.php");
	    			searching = true;
	    			showLoad();
	 
				} catch (UnsupportedEncodingException e) {
					Utils.ShortToast(view.getContext(), "Failed to Connect");
				}
			}
		}
	};
	
	/**
	 * Shows the loading symbols.
	 */
	private void showLoad() {
		search.setVisibility(View.INVISIBLE);
		loadBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Hides the loading symbols.
	 */
	private void hideLoad() {
		search.setVisibility(View.VISIBLE);
		loadBar.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Sets the delegate to this class.
	 */
    @Override
	protected void onStart(){
    	super.onStart();
		Utils.DownloadWebpageTask.delegate = this;
	}
	
    /**
     * Initializes a new search, gets the first 20 results.
     * 
     * @param view
     * The current state of the app, used to check network and toast.
     */
	public void SearchDB(View view){
		
		Utils.WipeList(this, friendList);
		doneSearch = false;
		searching = true;
		index = 0;
		
		EditText searchBox = (EditText) findViewById(R.id.editBox1);
		String searchString = searchBox.getText().toString();
		searchStr = searchString;
		
		if (Utils.CheckNetwork(view.getContext()))
		{
			try 
    		{
				String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
    			String searchUTF = URLEncoder.encode(searchString, "UTF-8");
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&search=" + searchUTF + "&index=" + index;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "search.php");
    			showLoad();
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to Connect");
    		}
		}
		
	}
	
	/**
	 * Handler for returned message from server.
	 * Handles for confirming/rejecting friends, and adding new search query output to current one.
	 * 
	 * @param output
	 * The text that the server has returned to us.
	 */
	@Override
	public void processFinish(String output) {
		
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		
		else if (output.trim().equals("") || output.contains(",") || output.contains("\n")) {//If we ran a search query
			friendList = (ListView) findViewById(R.id.friendList);
			Utils.SetList(this, friendList, Utils.ProcessList(output));
			hideLoad();
			if (output.contains("\n")) {
				doneSearch = true;
			}
			searching = false;
		}
		
		else {//If we added a friend query
			Utils.ShortToast(this, output.trim());
		}
	}
	
	/**
	 * Cancels the Asynchronous server request and then calls super class.
	 */
	 @Override
	 public void onBackPressed(){
		 Utils.WipeList(this, friendList);
	     Utils.cancelAsync();
	     super.onBackPressed();
	 }
}
