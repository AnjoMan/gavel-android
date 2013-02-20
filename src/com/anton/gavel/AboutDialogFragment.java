package com.anton.gavel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class AboutDialogFragment extends DialogFragment{
	
	public AboutDialogFragment(){
		//empty constructor
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View about = inflater.inflate(R.layout.dialog_about, null);
		
		//make links clickable
		TextView linkView = (TextView)about.findViewById(R.id.about_textview);
	    linkView.setMovementMethod(LinkMovementMethod.getInstance());
		
		builder.setView(about)
			.setTitle("About Gavel")
			.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
	            	AboutDialogFragment.this.getDialog().cancel();
		        }
			});
		
		return builder.create();
	}

}
