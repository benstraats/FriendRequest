package com.Straats.FriendRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.Straats.FriendRequest.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

public class LogIn extends Activity implements Utils.AsyncResponse{

	//for when we read from memory the login details
	private boolean memRead = false;
	//The sign up button
	private Button signUp;
	//The confirm button
	private Button confirm;
	//The loading symbol control
	private ProgressBar loadBar;
	
	/**
	 * Sets the delegate to this class, and tries the username and password combo
	 * if they are saved in memory.
	 */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
   
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        
        signUp = (Button)findViewById(R.id.button1);
    	confirm = (Button)findViewById(R.id.button2);
    	loadBar = (ProgressBar)findViewById(R.id.progressBar1);
    	
    	showButtons();
        
        Utils.DownloadWebpageTask.delegate = this;
        String userName = Utils.GetUser(this);
        if (userName != null){
        	showLoad();
        	String userUTF;
        	String passUTF;
			try {
				Utils.user = userName;
				Utils.password = Utils.GetPassword(this);
				userUTF = URLEncoder.encode(userName, "UTF-8");
				passUTF = URLEncoder.encode(Utils.GetPassword(this), "UTF-8");
				Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&password=" + passUTF;
				Utils.newAsync(new Utils.DownloadWebpageTask(),Utils.serverIP + "login.php");
				memRead = true;
			}
			catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to LogIn");
    		}
        }
    }
    
    /**
     * Sets the username and password fields if this class is accessed again,
     * since that means a user has explicitly clicked Log Out.
     */
    @Override
	protected void onStart(){
    	super.onStart();
		Utils.DownloadWebpageTask.delegate = this;
		
		//Clear the text fields and check box
		EditText usernameBox = (EditText) findViewById(R.id.editBox1);
    	EditText passwordBox = (EditText) findViewById(R.id.editBox2);
    	usernameBox.setText("");
    	passwordBox.setText("");
    	CheckBox rememberMe = (CheckBox)findViewById(R.id.checkBox1);
    	rememberMe.setChecked(false);
    	
    	if (memRead) {
    		showLoad();
    	}
    	else {
    		showButtons();
    	}
	}
    
    /**
     * Shows the loading controls.
     */
    private void showLoad() {
    	signUp.setVisibility(View.INVISIBLE);
		confirm.setVisibility(View.INVISIBLE);
		loadBar.setVisibility(View.VISIBLE);
    }
    
    /**
     * Hides the loading controls.
     */
    private void showButtons() {
    	signUp.setVisibility(View.VISIBLE);
		confirm.setVisibility(View.VISIBLE);
		loadBar.setVisibility(View.INVISIBLE);
    }
    
    /**
     * Does initially checking of the input strings then if they pass
     * that the fields are sent to the server to see if they work.
     */
    public void logUserIn(View view){
    	
    	//Get text field controls
    	EditText usernameBox = (EditText) findViewById(R.id.editBox1);
    	EditText passwordBox = (EditText) findViewById(R.id.editBox2);
    	
    	//Get the info
    	String userName = usernameBox.getText().toString();
    	String passWord = passwordBox.getText().toString();
    	
    	if (userName.length() < 3){
    		String text = "Invalid Username";
    		Utils.ShortToast(this, text);
    	}
    	
    	else if (passWord.length() < 6){
    		String text = "Invalid password";
			Utils.ShortToast(this, text);
    	}
    	
    	else if (Utils.CheckNetwork(this)){
    		try 
    		{
    			String userUTF = URLEncoder.encode(userName, "UTF-8");
    			String passUTF = URLEncoder.encode(passWord, "UTF-8");
    			Utils.user = userName;
    			Utils.password = passWord;
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&password=" + passUTF;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "login.php");
    			showLoad();
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to LogIn");
    		}
    	}
    }
    
    /**
     * User has clicked the Sign Up button, cancel any login requests and open the sign up activity.
     */
    public void signUserUp(View view){
    	
    	Utils.cancelAsync();
    	Intent intent = new Intent(this, SignUp.class);
    	startActivity(intent);
    	
    }

    /**
     * Handler for messages that have come from the server. 
     * Handles any outstanding friend request messages as well as handles for successful and failed
     * attempts at loging in.
     */
	@Override
	public void processFinish(String output) {
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		else if (output.contains("True")){
			Utils.ShortToast(this, "Successfully Logged In");
			Intent intent = new Intent (this, MainActivity.class);
			Utils.InitializeValues();
			CheckBox rememberMe = (CheckBox)findViewById(R.id.checkBox1);
			if (rememberMe.isChecked() || memRead){
				//save the data for permanent use until logout
				Utils.SaveUser(this);
				memRead = false;
			}
			else{
				Utils.RemoveUser(this);
			}
			startActivity(intent);
		}
		else {
			Utils.ShortToast(this, "Invalid Username/Password");
			Utils.RemoveUser(this);
			showButtons();
		}
	}
	
	/**
	 * Cancels the current asynchronous server request and calls super.
	 */
	@Override
	 public void onBackPressed(){
	    Utils.cancelAsync();
	    super.onBackPressed();
	 }
}
