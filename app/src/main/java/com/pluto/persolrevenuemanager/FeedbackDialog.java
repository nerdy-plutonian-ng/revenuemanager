package com.pluto.persolrevenuemanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import static com.pluto.persolrevenuemanager.Constants.BUSINESSES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.ID;
import static com.pluto.persolrevenuemanager.Constants.LONG;
import static com.pluto.persolrevenuemanager.Constants.NAME;
import static com.pluto.persolrevenuemanager.Constants.PROPERTIES_TABLE;

public class FeedbackDialog extends DialogFragment {

    private RevenueManager revenueManager;
    private Item item;
    private Utils utils;
    private Business business = null;
    private Property property = null;
    private boolean isBusiness = false;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        revenueManager = new RevenueManager(getActivity());
        utils = new Utils(getActivity());
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.feedback_layout, null);
        Spinner itemsSpinner = view.findViewById(R.id.itemsSpinner);
        final SearchView searchView = view.findViewById(R.id.searchView);
        final TextView userName = view.findViewById(R.id.userTV);
        final ListView resultsView = view.findViewById(R.id.resultsListview);
        final ScrollView resultsScrollview = view.findViewById(R.id.feedback_Scroll);
        final TextInputEditText feedbackEt = view.findViewById(R.id.feedbackEt);
        final TextInputEditText remarkEt = view.findViewById(R.id.remarksEt);
        final MaterialButton saveBtn = view.findViewById(R.id.saveBtn);

        Cursor cursor = revenueManager.getItemsCursor();
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
                cursor,
                new String[]{NAME,ID},
                new int[]{android.R.id.text1},
                0);
        itemsSpinner.setAdapter(cursorAdapter);
        itemsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = revenueManager.getItem((int)id);
                if(item.getName().toLowerCase().contains("business")){
                    searchView.setVisibility(View.VISIBLE);
                    isBusiness = true;
                    return;
                }
                if(item.getName().toLowerCase().contains("property")){
                    searchView.setVisibility(View.VISIBLE);
                    isBusiness = false;
                    return;
                }
                searchView.setVisibility(View.GONE);
                isBusiness = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(query.length() < 3){
                    utils.toastMessage("Please enter 3 or more letters to search", Toast.LENGTH_LONG);
                    resultsScrollview.setVisibility(View.GONE);
                    resultsView.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.GONE);
                    userName.setText("");
                    return false;
                }
                Cursor cursor;
                if(item.getName().toLowerCase().contains("business")){
                    cursor = revenueManager.getBusPropCursor(BUSINESSES_TABLE,query);
                } else {
                    cursor = revenueManager.getBusPropCursor(PROPERTIES_TABLE,query);
                }
                SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
                        cursor,
                        new String[]{NAME,ID},
                        new int[]{android.R.id.text1},
                        0);
                resultsView.setAdapter(cursorAdapter);
                resultsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        resultsView.setVisibility(View.GONE);
                        if(isBusiness){
                            business = revenueManager.getBusiness(String.valueOf(id));
                            userName.setText(business.getName());
                        } else {
                            property = revenueManager.getProperty(String.valueOf(id));
                            userName.setText(property.getName());
                        }
                        searchView.setQuery("",false);
                        userName.setVisibility(View.VISIBLE);
                        resultsScrollview.setVisibility(View.VISIBLE);
                        saveBtn.setVisibility(View.VISIBLE);
                    }
                });
                resultsScrollview.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
                resultsView.setVisibility(View.VISIBLE);
                userName.setText("");
                if(cursor.getCount() < 1){
                    Toast.makeText(getActivity(), R.string.no_data_found, Toast.LENGTH_LONG).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(feedbackEt.getText().toString().isEmpty()){
                    utils.toastMessage("Feedback field can not be empty",LONG);
                    return;
                }
                double[] coors = revenueManager.getGPSCoor();
                String locationStr = coors[0] + "," + coors[1];
                boolean result = revenueManager.saveFeedback(revenueManager.getCurrentAgentId(),
                        item.getItemId(),feedbackEt.getText().toString().trim(),Utils.getISOdate(),
                        locationStr,isBusiness ? business.getAccountCode() : property.getAccountCode());
                if(result){
                    utils.toastMessage("Feedback saved successfully",LONG);
                    FeedbackDialog.this.getDialog().cancel();
                } else {
                    utils.toastMessage("Failed saving feedback",LONG);
                }
            }
        });
        builder.setView(view)
                // Add action buttons
                .setTitle("Log Feedback")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        FeedbackDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
