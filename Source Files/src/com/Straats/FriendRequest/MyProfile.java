package com.Straats.FriendRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.Straats.FriendRequest.R;

import android.support.v7.app.ActionBarActivity;
import android.text.InputFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;

public class MyProfile extends ActionBarActivity implements Utils.AsyncResponse{
	
	//For knowing if we are currently saving the profile or not
	private boolean saving = false;
	//Save button
	private Button save;
	//Add more button
	private Button add;
	//The text box for adding unique fields to drop down boxes
	private EditText addText;
	//Tables that holds text boxes and drop down menus
	private TableLayout table;
	//The loading the profile symbol control
	private ProgressBar loadBarMain;
	//The saving the profile symbol control
	private ProgressBar loadBarSave;
	
	/**
	 * Sets the delegate and all the global controls.
	 * Automatically fetches the profile information from the server,
	 * and populates the drop down boxes respectively.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_profile);
		Utils.DownloadWebpageTask.delegate = this;
		
		//set globals
		save = (Button)findViewById(R.id.button1);
		add = (Button)findViewById(R.id.button2);
		addText = (EditText)findViewById(R.id.EditText02);
		TableRow tRow = (TableRow)findViewById(R.id.tableRow1);
		table = (TableLayout) tRow.getParent();
		loadBarMain = (ProgressBar)findViewById(R.id.progressBar1);
		loadBarSave = (ProgressBar)findViewById(R.id.progressBar2);
		
		hideLoadSave();
		showLoadMain();
		
		//Getting the profile needs to be more secure
		if (Utils.CheckNetwork(this)){
    		try 
    		{
    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
    			String passUTF = URLEncoder.encode(Utils.password, "UTF-8");
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&currUser=" + userUTF + "&currPass=" + passUTF;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "getprofile.php");
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to LogIn");
    		}
    	}
		
		//populate the spinners
		ArrayAdapter <CharSequence> adapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i=0; i<Utils.spinnerValues.size(); i++){
			adapter.add(Utils.spinnerValues.get(i));
		}
		Spinner s = (Spinner) findViewById(R.id.spinner1);
		s.setAdapter(adapter);
	}
	
	/**
	 * Shows the profile loading controls.
	 */
	private void showLoadMain() {
		save.setVisibility(View.INVISIBLE);
		add.setVisibility(View.INVISIBLE);
		addText.setVisibility(View.INVISIBLE);
		table.setVisibility(View.INVISIBLE);
		loadBarMain.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Hides the profile loading controls.
	 */
	private void hideLoadMain() {
		save.setVisibility(View.VISIBLE);
		add.setVisibility(View.VISIBLE);
		addText.setVisibility(View.VISIBLE);
		table.setVisibility(View.VISIBLE);
		loadBarMain.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Shows the saving loading controls
	 */
	private void showLoadSave() {
		save.setVisibility(View.INVISIBLE);
		loadBarSave.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Hides the saving loading controls.
	 */
	private void hideLoadSave() {
		save.setVisibility(View.VISIBLE);
		loadBarSave.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Formats the profiles information and sends it to the server.
	 * @param view
	 */
	public void saveProfile(View view){
		if (Utils.CheckNetwork(this)){
    		try 
    		{
    			String jsonProfile = "{\"profile\" : [";
    									
    			int j = table.getChildCount();
    			
    			for (int i=0; i<j; i++) {
    				TableRow row = (TableRow) table.getChildAt(i);
    				EditText eText = (EditText) row.getChildAt(0);
    				Spinner spinner = (Spinner) row.getChildAt(1);
    				
    				//this is shit, we should move to JSON boys
    				String field = spinner.getSelectedItem().toString().trim();
    				String value = eText.getText().toString().replace("\\", "").trim();
    				
    				jsonProfile += "{\"" + field + "\" : \"" + value + "\"},";
    			}
    			
    			//remove last comma
    			jsonProfile = jsonProfile.substring(0, jsonProfile.length() - 1);
    			jsonProfile += "]}";
    			
    			String userUTF = URLEncoder.encode(Utils.user, "UTF-8");
    			String passUTF = URLEncoder.encode(Utils.password, "UTF-8");
    			String profileUTF = URLEncoder.encode(jsonProfile, "UTF-8");
    			saving = true;
    			Utils.DownloadWebpageTask.urlParams = "user=" + userUTF + "&password=" + passUTF + "&profile=" + profileUTF;
    			Utils.newAsync(new Utils.DownloadWebpageTask(), Utils.serverIP + "saveprofile.php");
    			showLoadSave();
    		}
    		catch (UnsupportedEncodingException e) {
    			Utils.ShortToast(this, "Failed to LogIn");
    		}
    	}
	}
	
	/**
	 * Handling for when the add button is pressed.
	 * If the text box beside it has text in it add that text to the drop down boxes
	 * and remove the text. If there is no text then add a new row in the profile.
	 * @param view
	 */
	public void addField(View view){
		
		String text = addText.getText().toString();
		if ((text == null || text.equals("")) && table.getChildCount() < 27){
			addControls();
		}
		else if (text == null || text.equals("")) {
			Utils.ShortToast(this, "Max Row Count Reached");
		}
		else {
			addText.setText("");
			Utils.spinnerValues.add(text.replace("\\", "").trim());
			reloadSpinners();
		}
		
	}

	/**
	 * Handles for users loading and saving the profile as well as global accepting/requesting/rejecting friend requests
	 */
	@Override
	public void processFinish(String output) {
		//Special handling for friend accepts, requests, and declines. notify the user no matter what activity
		//Also these requests will never be cancelled
		if (output.contains("Friend Request Accepted For") || output.contains("Friend Request Sent To") || output.contains("Friend Request Declined For")) {
			Utils.ShortToast(this, output.trim());
		}
		else if (saving){
			if (output.contains("True")) {
				Utils.ShortToast(this, "Successfully Saved");
			}
			else {
				Utils.ShortToast(this, "Failed To Save");
			}
			saving = false;
			hideLoadSave();
		}
		else {
			hideLoadMain();
			//spinnervalue1:textfield1;spinnervalue2:textfield2;
			//parse the string
			List<String[]> data = new ArrayList<String[]>();
			
			if (output.contains("\n")){
				String[] dataMaps = output.split("\n");
				
				for (int i=0; i<dataMaps.length-1; i++) {
					String[] userData = dataMaps[i].split("\t");
					data.add(userData);
					//Add it to the spinner values if not already in there
					if (!Utils.spinnerValues.contains(userData[0])){
						Utils.spinnerValues.add(userData[0]);
					}
				}
				
				//reload the first spinner
				reloadSpinners();
				//set the first values
				EditText existingText = (EditText)findViewById(R.id.editText1);
				Spinner spinner1 = (Spinner)findViewById(R.id.spinner1);
				existingText.setText(data.get(0)[1]);
				int position = Utils.spinnerValues.indexOf(data.get(0)[0]);
				spinner1.setSelection(position);
				
				//add fields and set them
				for (int i=1; i<data.size(); i++){
					addControls();
					TableRow tRow = (TableRow) table.getChildAt(i);
					EditText eText = (EditText) tRow.getChildAt(0);
					eText.setText(data.get(i)[1]);
					Spinner spinnerI = (Spinner) tRow.getChildAt(1);
					position = Utils.spinnerValues.indexOf(data.get(i)[0]);
					spinnerI.setSelection(position);
				}
			}
		}
	}
	
	/**
	 * Adds a text field and drop down on the same row. Drop down box will have
	 * the same options as the other ones.
	 */
	private void addControls(){
		
		TableRow tRow = new TableRow(this);
		table.addView(tRow);
			
		EditText newText = new EditText(this);
		newText.setSingleLine(true);
		newText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(30)});
		tRow.addView(newText);
			
		Spinner newSpinner = new Spinner(this);
			
		ArrayAdapter <CharSequence> adapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i=0; i<Utils.spinnerValues.size(); i++){
			adapter.add(Utils.spinnerValues.get(i));
		}
		newSpinner.setAdapter(adapter);
			
		tRow.addView(newSpinner);
		
	}
	
	/**
	 * Reloads all drop down boxes so they all have the same options.
	 */
	private void reloadSpinners(){
		
		for (int i=0; i<table.getChildCount(); i++) {
			TableRow row = (TableRow) table.getChildAt(i);
			Spinner spinner = (Spinner) row.getChildAt(1);
			int choice = spinner.getSelectedItemPosition();
			
			//slow
			ArrayAdapter <CharSequence> adapter = new ArrayAdapter <CharSequence> (this, android.R.layout.simple_spinner_item );
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			for (int j=0; j<Utils.spinnerValues.size(); j++){
				adapter.add(Utils.spinnerValues.get(j));
			}
			spinner.setAdapter(adapter);
			
			spinner.setSelection(choice);
		}
	}
	
	/**
	 * Cancels current async request to server and calls super.
	 */
	@Override
	 public void onBackPressed(){
	    Utils.cancelAsync();
	    super.onBackPressed();
	 }
}
