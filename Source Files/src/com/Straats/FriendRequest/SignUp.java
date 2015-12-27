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

public class SignUp extends Activity implements Utils.AsyncResponse{
	
	//The sign up button
	private Button confirm;
	//The loading symbol control
	private ProgressBar loadBar;
	
	/**
	 * Sets the delegate and sets the globals.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_up);
		Utils.DownloadWebpageTask.delegate = this;
		
		confirm = (Button)findViewById(R.id.button1);
    	loadBar = (ProgressBar)findViewById(R.id.progressBar1);
    	
    	showButtons();
	}
	
	/**
	 * Listener for when create user is clicked
	 * Checks the information for being valid then sends it to the server.
	 * @param view
	 */
	public void createUser(View view)
	{
		
		EditText usernameBox = (EditText) findViewById(R.id.editBox1);
		EditText fullNameBox = (EditText) findViewById(R.id.editBox2);
		EditText passwordBox = (EditText) findViewById(R.id.editBox3);
		EditText retypePassBox = (EditText) findViewById(R.id.editBox4);
		
		String usernameText = usernameBox.getText().toString();
		String fullNameText = fullNameBox.getText().toString();
		String passwordText = passwordBox.getText().toString();
		String retypePassText = retypePassBox.getText().toString();
		
		//regex copied from user daan on stackoverflow, can be found here:
		//http://stackoverflow.com/questions/275160/regex-for-names
		String namePattern = "^([ \u00c0-\u01ffa-zA-Z'-])+$";
		
		String userPattern = "^[A-Za-z0-9]+$";
		
    	if (usernameText.length() < 3){
			String text = "Username must be atleast 3 characters in length";
			Utils.ShortToast(this, text);
		}
    	if (!usernameText.matches(userPattern)) {
    		String text = "Invalid Character in Name";
    		Utils.ShortToast(this, text);
    	}
		else if (fullNameText.length() < 3){
			String text = "Name must be atleast 3 characters in length";
			Utils.ShortToast(this, text);
		}
		else if (!fullNameText.matches(namePattern)){
			String text = "Invalid Character in Name";
			Utils.ShortToast(this, text);
		}
		else if (passwordText.length() < 6){
			String text = "Password must be atleast 6 characters in length";
			Utils.ShortToast(this, text);
		}
		else if (!retypePassText.equals(passwordText)){
			String text = "Passwords do not match";
			Utils.ShortToast(this, text);
		}
		else if (Utils.CheckNetwork(this))
		{
			try 
    		{
    			String userUTF = URLEncoder.encode(usernameText, "UTF-8");
    			String nameUTF = URLEncoder.encode(fullNameText, "UTF-8");
    			String passUTF = URLEncoder.encode(passwordText, "UTF-8");
    			Utils.user = usernameText;
    			Utils.password = passwordText;
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&name=" + nameUTF + "&password=" + passUTF;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "register.php");
    			showLoad();
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to LogIn");
    		}
		}
	}
	
	/**
	 * Shows the load symbol control and hides the button
	 */
	private void showLoad() {
		confirm.setVisibility(View.INVISIBLE);
		loadBar.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Shows the buttons and hides the load control
	 */
	private void showButtons() {
		confirm.setVisibility(View.VISIBLE);
		loadBar.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Handler for creating user as well as handling other old requests to server.
	 */
	@Override
	public void processFinish(String output) {
		
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		
		else if (output.contains("True")){
			Utils.ShortToast(this, "Account Created");
			Intent intent = new Intent (this, MainActivity.class);
			Utils.InitializeValues();
			CheckBox rememberMe = (CheckBox)findViewById(R.id.checkBox1);
			if (rememberMe.isChecked()){
				Utils.SaveUser(this);
			}
			startActivity(intent);
		}
		
		else {
			Utils.ShortToast(this, "Username Taken or Internet Connection Failure");
			showButtons();
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
