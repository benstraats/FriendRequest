package com.Straats.FriendRequest;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.Straats.FriendRequest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class Utils {

	//The package of the app
	protected final static String PACKAGE_NAME = "straats.FR.FrontEnd";
	//The server IP
	protected final static String serverIP = "http://54.68.30.203/";
	
	//The current user
	protected static String user;
	//The current users password
	protected static String password;//This may be a bad idea
	//The list of drop down menu values
	protected static ArrayList<String> spinnerValues = new ArrayList<String>();
	//The current asynchronous request to the server
	private static DownloadWebpageTask asyncTask;
	//The data for the current activities list
	private static List<Map<String,String>> data = new ArrayList<Map<String,String>>();

	/**
	 * Initializes the basic spinner values
	 */
	protected static void InitializeValues(){

		spinnerValues.add("Email");
		spinnerValues.add("Google+");
		spinnerValues.add("LinkedIn");
		spinnerValues.add("Phone");
		spinnerValues.add("Pinterest");
		spinnerValues.add("PSN");
		spinnerValues.add("Snapchat");
		spinnerValues.add("Steam");
		spinnerValues.add("Tumblr");
		spinnerValues.add("Twitch");
		spinnerValues.add("Twitter");
		spinnerValues.add("Vine");
		spinnerValues.add("Xbox Live");
		spinnerValues.add("YouTube");
		spinnerValues.add("Other");
		
	}
	
	
	/**
	 * Cancels the current async tasj and creates a new one with the given URL
	 * @param newAsync
	 * The asynnc request to be canceled
	 * @param url
	 * The url for the new async task
	 */
	protected static void newAsync(DownloadWebpageTask newAsync, String url) {
		cancelAsync();
		asyncTask = newAsync;
		asyncTask.execute(url);
	}
	
	/**
	 * Cancels the current async task.
	 */
	protected static void cancelAsync() {
		if (asyncTask != null) {
			asyncTask.cancel(true);
		}
	}
	
	
	/**
	 * Savess the current users password and username into memory
	 * @param c
	 * Used to get the context that we can save into the phone
	 */
	protected static void SaveUser(Context c){
		
		SharedPreferences mySharedPreferences = c.getSharedPreferences("userdetails", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString("username", user);
		editor.putString("password", password);
		editor.commit();
		
	}
	

	/**
	 * Gets the users username that was saved in memory
	 * @param c
	 * The context of the app, used for accessing memory
	 * @return
	 * Returns the username of the user saved in memory
	 */
	protected static String GetUser(Context c){
		
		SharedPreferences mySharedPreferences = c.getSharedPreferences("userdetails", Activity.MODE_PRIVATE);
		String user = mySharedPreferences.getString("username", null);
		return user;
		
	}
	

	/**
	 * Gets the users password that was saved in memory
	 * @param c
	 * The context of the app, used for accessing memory
	 * @return
	 * Returns the password of the user saved in memory
	 */
	protected static String GetPassword(Context c){
		
		SharedPreferences mySharedPreferences = c.getSharedPreferences("userdetails", Activity.MODE_PRIVATE);
		String password = mySharedPreferences.getString("password", null);
		return password;
		
	}
	

	/**
	 * Removes the saved username and password stored in memory
	 * @param c
	 * The context of the app, used for accessing memory
	 */
	protected static void RemoveUser(Context c){
		
		SharedPreferences mySharedPreferences = c.getSharedPreferences("userdetails", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = mySharedPreferences.edit();
		editor.putString("username", null);
		editor.putString("password", null);
		editor.commit();
		
	}
	

	/**
	 * Creates an adapter for the lists found in Main, FindFriends, and PendingFriends
	 * Then automatically sets it as the source of the list to generate it.
	 * 
	 * @param context
	 * The current state of the app.
	 * @param friendList
	 * The list we are trying to populate.
	 * @param newData
	 * The data we want to append to the list.
	 */
	public static void SetList(Context context, ListView friendList, List<Map<String,String>> newData){
		
		if (newData == null)
			return;
		
		String[] keys = new String[] {"Name", "Username"};
		
		data.addAll(newData);
		
		SimpleAdapter adapter = new SimpleAdapter(context, data, 
				android.R.layout.simple_list_item_2, keys,
				new int[] {android.R.id.text1, android.R.id.text2});
			
		friendList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
	}
	
	/**
	 * Creates a dialog box with respective elements.
	 * 
	 * @param context
	 * The context of the app.
	 * @param title
	 * The Title for the dialog box
	 * @param message
	 * The message of the dialog box
	 * @return
	 * Returns the dialog box for further customization.
	 */
	protected static Builder CreateDialog(Context context, String title, String message) {
		
		AlertDialog.Builder aDia = new AlertDialog.Builder(context);
		aDia.setTitle(title);
		aDia.setMessage(message);
		return aDia;
		
	}
	
	/**
	 * Wipes the current list of all data.
	 * Since all the lists are connected this must be done between activites.
	 * 
	 * @param context
	 * The context of the app, ie the current state.
	 * @param friendList
	 * The list we are currently on.
	 */
	protected static void WipeList(Context context, ListView friendList) {
		
		String[] keys = new String[] {"Name", "Username"};
		
		data.clear();
		
		SimpleAdapter adapter = new SimpleAdapter(context, data, 
				android.R.layout.simple_list_item_2, keys,
				new int[] {android.R.id.text1, android.R.id.text2});
			
		friendList.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	
	/**
	 * Parse a string and create a list out of the items.
	 * @param dataString
	 * The string that was returned from the server
	 * @return
	 * A list of maps from a string to a string.
	 */
	public static List<Map<String,String>> ProcessList(String dataString){
		
		List<Map<String,String>> data = new ArrayList<Map<String, String>>();
		
		String[] dataMaps = dataString.split(";");
		
		for (int i=0; i<dataMaps.length-1; i++) {
			String[] userData = dataMaps[i].split(",");
			Map<String, String> userMap = new HashMap<String,String>();
			userMap.put("Name", userData[1]);
			userMap.put("Username", userData[0]);
			if (!userData[0].equals(Utils.user)){//Dont add own user
				data.add(userMap);
			}
		}
		
		return data;
		
	}
	
	/**
	 * Creates a short toast with the given text.
	 * @param context
	 * The current state of the app, so we know how to create a toast.
	 * @param text
	 * The text we want to show the user.
	 */
	public static void ShortToast (Context context, String text){
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * Creates a long toast with the given text.
	 * @param context
	 * The current state of the app, so we know how to create a toast.
	 * @param text
	 * The text we want to show the user.
	 */
	public static void LongToast (Context context, String text){
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	/**
	 * Checks the network to see if we're connected
	 * @param context
	 * The current state of the app/phone.
	 * @return
	 * True if connected to the internet, false otherwise.
	 */
	public static boolean CheckNetwork (Context context) {
		ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    	
    	if (networkInfo == null || !networkInfo.isConnected()){
    		String text = "No Network Connection";
			Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
			toast.show();
			return false;
    	}
    	return true;
	}
	
	/**
	 * Creates a notification with the given message
	 * @param context
	 * The context of the app.
	 * @param message
	 * The message the notification should display
	 */
	public static void CreateNotification (Context context, String message){
		
		NotificationCompat.Builder mBuilder =
    		    new NotificationCompat.Builder(context)
    		    .setSmallIcon(R.drawable.main_icon)
    		    .setContentTitle("Friend Request")
    		    .setContentText(message);
		
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.notify(001, mBuilder.build());
	}
	
	/**
	 * Class for creating and sending asynchronous web requests.
	 * 
	 * @author Taken from stack overflow
	 *
	 */
	public static class DownloadWebpageTask extends AsyncTask<String, Void, String> {
	    
		//Which class to call back to
		public static AsyncResponse delegate=null;
		//The POST params
		public static String urlParams = "";
		
		@Override
	    protected String doInBackground(String... urls) {
	            
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
	            return "Unable to retrieve web page. URL may be invalid.";
			}
	           
		}
	       
		@Override
		protected void onPostExecute(String result) {
			//do not send a result if cancelled
			if (isCancelled()) {
				return;
			}
	        delegate.processFinish(result);
		}
	       
		private String downloadUrl(String myurl) throws IOException {
	    	   
			InputStream is = null;
			DataOutputStream wr = null;
			//worst size return for returning 20 users and names and characters to split them and null character at end
	    	int len = 1642;
	  	         
	  	   	try {
	  	     		
	  	   		URL url = new URL(myurl);
	  	     	HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	  	     		
	  	     	conn.setReadTimeout(10000 /* milliseconds */);
	  	     	conn.setConnectTimeout(15000 /* milliseconds */);
	  	     	conn.setRequestMethod("POST");
	  	     	conn.setDoInput(true);
	  	     	conn.setDoOutput(true);
	  	     	
	  	     	//Not sure when to do this or at all
	  	     	conn.connect();
	  	     	
	  	     	wr = new DataOutputStream(conn.getOutputStream());
	  	     	wr.writeBytes(urlParams);
	  	     	wr.flush();
	  	     	wr.close();
	  	     	
	  	     	int response = conn.getResponseCode();
	  	     	Log.d("HTTP DEGUB", "Call to " + myurl + ". The response is: " + response);
	  	     	is = conn.getInputStream();
	  	     	
	  	     	String contentAsString = readIt(is, len);
	  	     	return contentAsString;
	  	     		
	  	     } finally {
	  	     	if (is != null) {
	  	     		is.close();
	  	     	}
	  	     	if (wr != null) {
	  	     		wr.close();
	  	     	}
	  	     }
		}
	  	 
	  	// Reads an InputStream and converts it to a String.
	  	public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
	  		Reader reader = null;
	  	   	reader = new InputStreamReader(stream, "UTF-8");        
	  	   	char[] buffer = new char[len];
	  	   	reader.read(buffer);
	  	   	return new String(buffer);
	  	}
	}
	
	/**
	 * Overridden by each class, this is why the delegate is important.
	 */
    public interface AsyncResponse {
        void processFinish(String output);
    }
    
}