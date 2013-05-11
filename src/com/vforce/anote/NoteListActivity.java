package com.vforce.anote;

import gueei.binding.observables.IntegerObservable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * An activity representing a list of Notes. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link NoteDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link NoteListFragment} and the item details (if present) is a
 * {@link NoteDetailFragment}.
 * <p>
 * This activity also implements the required {@link NoteListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class NoteListActivity extends FragmentActivity implements
		NoteListFragment.Callbacks {
	
	private static final String NOTE_FILE_POSTFIX = ".note";
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);

		if (findViewById(R.id.note_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((NoteListFragment) getSupportFragmentManager().findFragmentById(
					R.id.note_list)).setActivateOnItemClick(true);
		}
		
		try {
			readFiles();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_add:
	            addNote();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void addNote() {
		Intent intent = new Intent(this, AddNoteActivity.class);
		startActivity(intent);
	}

	/**
	 * Callback method from {@link NoteListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(NoteDetailFragment.ARG_ITEM_ID, id);
			NoteDetailFragment fragment = new NoteDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.note_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, NoteDetailActivity.class);
			detailIntent.putExtra(NoteDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
	
	private List<Note> readFiles() throws IOException {
		String[] files = getAssets().list("");
		List<Note> notes = new ArrayList<Note>();
		for (String fileName : files) {
			if(fileName.endsWith(NOTE_FILE_POSTFIX)){
				notes.addAll(readFile(fileName));
			}
		}
		return notes;
	}

	private List<Note> readFile(String fileName) throws IOException {
		InputStream in = getAssets().open(fileName);
		List<Note> notes = new ArrayList<Note>();
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(in, "UTF-8"));
		
		String line;
		while((line = reader.readLine()) != null){
			String[] split = line.split(":");
			String name = split[0];
			String d = split[1];
			try {
				Date date = new SimpleDateFormat("dd.mm.yyyy", Locale.ENGLISH).parse(d);
				notes.add(new Note(name, date));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return notes;
	}
}
