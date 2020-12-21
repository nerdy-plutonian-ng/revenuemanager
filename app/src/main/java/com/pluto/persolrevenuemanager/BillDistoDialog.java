package com.pluto.persolrevenuemanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import static com.pluto.persolrevenuemanager.Constants.BILL_DISTRIBUTION;
import static com.pluto.persolrevenuemanager.Constants.BUSINESSES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.ID;
import static com.pluto.persolrevenuemanager.Constants.ITEMS_TABLE;
import static com.pluto.persolrevenuemanager.Constants.LONG;
import static com.pluto.persolrevenuemanager.Constants.NAME;
import static com.pluto.persolrevenuemanager.Constants.PROPERTIES_TABLE;

public class BillDistoDialog extends DialogFragment {

    private RevenueManager revenueManager;
    private int itemId = 0;
    private String accountcode;
    private Utils utils;
    private Business business;
    private Property property;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.bill_distro_layout, null);
        revenueManager = new RevenueManager(getActivity());
        utils = new Utils(getActivity());
        final ListView resultListView = view.findViewById(R.id.bopPropListview);
        final Spinner itemsSpinner = view.findViewById(R.id.revenueTypeBillDistroSpinner);
        final Spinner reasonsSpinner = view.findViewById(R.id.reasonsBillDistroSpinner);
        final SearchView searchView = view.findViewById(R.id.searchview_billDistro);
        final ConstraintLayout successLayout = view.findViewById(R.id.success_layout);
        final ConstraintLayout failedLayout = view.findViewById(R.id.failed_layout);
        final TextInputLayout feedbackTIL = view.findViewById(R.id.feedbackTIL);
        final TextInputLayout remarkTIL = view.findViewById(R.id.remarksTIL);
        final TextInputEditText feedbackEt = view.findViewById(R.id.feedbackEt);
        final TextInputEditText remarkEt = view.findViewById(R.id.remarksEt);
        final TextInputLayout recNameTIL = view.findViewById(R.id.nameTIL);
        final TextInputLayout recPhoneTIL = view.findViewById(R.id.phoneTIL);
        final TextInputLayout recEmailTIL = view.findViewById(R.id.emailTIL);
        final TextInputEditText recPhoneEt = view.findViewById(R.id.phoneEt);
        final TextInputEditText recNameEt = view.findViewById(R.id.nameEt);
        final TextInputEditText recEmailEt = view.findViewById(R.id.emailEt);
        final ScrollView resultScrollView = view.findViewById(R.id.resultScrollview);
        final TextView busPropNameTv = view.findViewById(R.id.bopPropName);
        final CheckBox failedDistroCheckbox = view.findViewById(R.id.failedDistroCheckbox);
        final Button saveBtn = view.findViewById(R.id.saveBtn);

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
                itemId = (int)id;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() < 3){
                    utils.toastMessage("Please enter 3 or more letters to search",Toast.LENGTH_LONG);
                    resultScrollView.setVisibility(View.GONE);
                    return false;
                }

                if(revenueManager.getName(ITEMS_TABLE,NAME,ID, String.valueOf(itemsSpinner.getSelectedItemId())).toLowerCase().contains("business")){
                    Cursor cursor = revenueManager.getBusPropCursor(BUSINESSES_TABLE,query);
                    SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
                            cursor,
                            new String[]{NAME,ID},
                            new int[]{android.R.id.text1},
                            0);
                    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            resultListView.setVisibility(View.GONE);
                            searchView.setQuery("",false);
                            business = revenueManager.getBusiness(String.valueOf(id));
                            property = null;
                            busPropNameTv.setText(business.getName());
                            resultScrollView.setVisibility(View.VISIBLE);
                            successLayout.setVisibility(View.VISIBLE);
                            failedLayout.setVisibility(View.GONE);
                            saveBtn.setVisibility(View.VISIBLE);
                            failedDistroCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked){
                                        successLayout.setVisibility(View.GONE);
                                        failedLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        failedLayout.setVisibility(View.GONE);
                                        successLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    };
                    resultListView.setAdapter(cursorAdapter);
                    resultListView.setOnItemClickListener(itemClickListener);
                    resultScrollView.setVisibility(View.GONE);
                    resultListView.setVisibility(View.VISIBLE);
                    busPropNameTv.setText("");
                    if(cursor.getCount() < 1){
                        Toast.makeText(getActivity(), R.string.no_data_found, Toast.LENGTH_LONG).show();
                    }
                    return false;
                }

                if(revenueManager.getName(ITEMS_TABLE,NAME,ID, String.valueOf(itemsSpinner.getSelectedItemId())).toLowerCase().contains("property")){
                    Cursor cursor = revenueManager.getBusPropCursor(PROPERTIES_TABLE,query);
                    SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
                            cursor,
                            new String[]{NAME,ID},
                            new int[]{android.R.id.text1},
                            0);
                    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            resultListView.setVisibility(View.GONE);
                            searchView.setQuery("",false);
                            property = revenueManager.getProperty(String.valueOf(id));
                            business = null;
                            busPropNameTv.setText(property.getName());
                            resultScrollView.setVisibility(View.VISIBLE);
                            successLayout.setVisibility(View.VISIBLE);
                            failedLayout.setVisibility(View.GONE);
                            saveBtn.setVisibility(View.VISIBLE);
                            failedDistroCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if(isChecked){
                                        successLayout.setVisibility(View.GONE);
                                        failedLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        failedLayout.setVisibility(View.GONE);
                                        successLayout.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    };
                    resultListView.setAdapter(cursorAdapter);
                    resultListView.setOnItemClickListener(itemClickListener);
                    resultScrollView.setVisibility(View.GONE);
                    resultListView.setVisibility(View.VISIBLE);
                    busPropNameTv.setText("");
                    return false;
                }

                utils.toastMessage("No data",Toast.LENGTH_LONG);
                resultScrollView.setVisibility(View.GONE);
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
                if(busPropNameTv.getText().toString().isEmpty()){
                    utils.toastMessage("Please select a business or property first",LONG);
                    return;
                }

                if(!failedDistroCheckbox.isChecked() && ( recNameEt.getText().toString().isEmpty() ||
                        recPhoneEt.getText().toString().isEmpty())){
                    utils.toastMessage("Please enter recipient's name and phone number or email",LONG);
                    return;
                }
                double[] coors = revenueManager.getGPSCoor();
                String locationStr = coors[0] + "," + coors[1];
                if(!failedDistroCheckbox.isChecked()){
                    boolean distribution = revenueManager.saveBillDistroTransaction(business == null ? property.getAccountCode() : business.getAccountCode(),
                            recNameEt.getText().toString().trim(),recPhoneEt.getText().toString(),recEmailEt.getText().toString().trim(),
                            locationStr,feedbackEt.getText().toString().trim(),remarkEt.getText().toString().trim(),
                            "",Utils.getISOdate(),revenueManager.getCurrentAgentId(),itemId,1,1);
                    if(distribution){
                        BillDistoDialog.this.getDialog().cancel();
                        utils.toastMessage("Success Saving Bill",LONG);
                    } else {
                        utils.toastMessage("Failed saving transaction",LONG);
                    }
                } else {

                    boolean distribution = revenueManager.saveBillDistroTransaction(business == null ? property.getAccountCode() : business.getAccountCode(),
                            "","","",locationStr,feedbackEt.getText().toString().trim(),remarkEt.getText().toString().trim(),
                            "",Utils.getISOdate(),revenueManager.getCurrentAgentId(),itemId,0,1);
                    if(distribution){
                        BillDistoDialog.this.getDialog().cancel();
                        utils.toastMessage("Success Saving Bill",LONG);
                    } else {
                        utils.toastMessage("Failed saving transaction",LONG);
                    }
                }
            }
        });

        builder.setView(view)
                // Add action buttons
                .setTitle(BILL_DISTRIBUTION)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BillDistoDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}
