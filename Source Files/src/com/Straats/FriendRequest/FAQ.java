package com.Straats.FriendRequest;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class FAQ extends Activity {

	/**
	 * Sets the FAQ activity to have String faq printed on screen
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_faq);
		
		TextView text = (TextView)findViewById(R.id.textView1);
		
		String faq = "Who can see my profile?\n" + 
				"Anyone who is friends with you is able to view your profile.\n\n" +
				"How can I delete an item from my profile?\n" + 
				"Leave the text box for the row empty and save.\n\n" +
				"How can I add another row to my profile?\n" + 
				"Click the add button when the text box to the left is empty.\n\n" + 
				"How can I add an item to the drop down boxes?\n" +
				"Type the item you want in the text box beside the add button then click add.\n\n" + 
				"How can I send feedback/bugs/suggestions?\n" +
				"Please Email contactfriendrequest@gmail.com\n\n" +
				"App created by Ben Straatsma.\n" +
				"Special thanks to Nick and John for testing.";
		
		text.setText(faq);
	}
}