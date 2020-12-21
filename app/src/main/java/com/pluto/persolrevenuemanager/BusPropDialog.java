package com.pluto.persolrevenuemanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Date;

import static com.pluto.persolrevenuemanager.Constants.BUSINESSES_TABLE;
import static com.pluto.persolrevenuemanager.Constants.ID;
import static com.pluto.persolrevenuemanager.Constants.LONG;
import static com.pluto.persolrevenuemanager.Constants.NAME;
import static com.pluto.persolrevenuemanager.Constants.PROPERTIES_TABLE;

public class BusPropDialog extends DialogFragment {

    private RevenueManager revenueManager;
    private int itemId;
    private Utils utils;
    private Business newBusiness;
    private Property newProperty;
    private boolean isBusiness;

    public BusPropDialog(int itemId) {
        this.itemId = itemId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        utils = new Utils(getActivity());
        revenueManager = new RevenueManager(getActivity());
        final Item item = revenueManager.getItem(itemId);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.busprop_layout, null);
        final SearchView searchView = view.findViewById(R.id.searchBar);
        final ListView resultsView = view.findViewById(R.id.bopPropListview);
        final ScrollView transScroll = view.findViewById(R.id.transactionScroll);
        final TextView nameTv = view.findViewById(R.id.nameTV);
        final TextView amountDueTv =view.findViewById(R.id.amountTV);
        final TextInputEditText gcrNoEt = view.findViewById(R.id.gcrEt);
        final TextInputEditText amountEt = view.findViewById(R.id.amountEt);
        final Button saveBtn = view.findViewById(R.id.saveBtn);
        final CheckBox momoCheckBox = view.findViewById(R.id.momoCheckbox);
        final TextInputEditText momoEt = view.findViewById(R.id.momoEt);
        final TextInputLayout momoTIL = view.findViewById(R.id.momoTIL);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(query.length() < 3){
                    utils.toastMessage("Please enter 3 or more letters to search",Toast.LENGTH_LONG);
                    resultsView.setVisibility(View.GONE);
                    transScroll.setVisibility(View.GONE);
                    saveBtn.setVisibility(View.GONE);
                    return false;
                }
                Cursor cursor = revenueManager.getBusPropCursor(item.getName().toLowerCase().contains("business") ? BUSINESSES_TABLE : PROPERTIES_TABLE,query);
                SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(getActivity(),android.R.layout.simple_list_item_1,
                        cursor,
                        new String[]{NAME,ID},
                        new int[]{android.R.id.text1},
                        0);
                AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        resultsView.setVisibility(View.GONE);
                        searchView.setQuery("",false);
                        amountEt.getText().clear();
                        gcrNoEt.getText().clear();
                        saveBtn.setVisibility(View.VISIBLE);
                        if(item.getName().toLowerCase().contains("business")){
                            isBusiness = true;
                            newBusiness =revenueManager.getBusiness(String.valueOf(id));
                            nameTv.setText(newBusiness.getName());
                            amountDueTv.setText(Utils.formatMoney(newBusiness.getCurrentCharge()+newBusiness.getOutBalance()-newBusiness.getPaid()));
                        } else {
                            isBusiness = false;
                            newProperty = revenueManager.getProperty(String.valueOf(id));
                            nameTv.setText(newProperty.getName());
                            amountDueTv.setText(Utils.formatMoney(newProperty.getCurrentCharge()+newProperty.getOutBalance()-newProperty.getPaid()));
                        }
                        transScroll.setVisibility(View.VISIBLE);
                    }
                };
                resultsView.setAdapter(cursorAdapter);
                resultsView.setOnItemClickListener(itemClickListener);
                resultsView.setVisibility(View.VISIBLE);
                transScroll.setVisibility(View.GONE);
                saveBtn.setVisibility(View.GONE);
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

        amountEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    amountEt.setText(Utils.formatMoney(Double.parseDouble(amountEt.getText().toString())));
                }
            }
        });

        momoCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    momoTIL.setVisibility(View.VISIBLE);
                } else {
                    momoTIL.setVisibility(View.GONE);
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(amountEt.getText().toString().trim().isEmpty()){
                    utils.toastMessage("Please fill the amount field",LONG);
                    return;
                }

                if(gcrNoEt.getText().toString().trim().isEmpty()){
                    utils.toastMessage("Please fill the GCR Number field",LONG);
                    return;
                }
                double[] coors = revenueManager.getGPSCoor();
                String locationStr = coors[0] + "," + coors[1];
                String receiptNo = revenueManager.generateReceipt();
                if(momoCheckBox.isChecked() && momoEt.getText().toString().trim().isEmpty()){
                    utils.toastMessage("Please fill the mobile money field",LONG);
                    return;
                }
                if(momoCheckBox.isChecked()){
                    if(revenueManager.saveTransaction(item.getItemId(),Utils.removeFormatting(amountEt.getText().toString()),
                            revenueManager.getCurrentAgentId(),isBusiness ? newBusiness.getAccountCode() : newProperty.getAccountCode(),
                            receiptNo, gcrNoEt.getText().toString(),new Date().toString(),1,locationStr)){
                        revenueManager.printReceipt(receiptNo,item.getName(),Double.parseDouble(amountEt.getText().toString()),
                                nameTv.getText().toString().trim());
                        revenueManager.dialNumber(revenueManager.getCurrentAgentId(),isBusiness ? newBusiness.getAccountCode() : newProperty.getAccountCode(),item.getItemId(),
                                momoEt.getText().toString().trim(),Double.parseDouble(amountEt.getText().toString().trim()));
                        Toast.makeText(getActivity(), "Transaction Successful", Toast.LENGTH_SHORT).show();
                    };
                    return;
                }
                if(revenueManager.saveTransaction(item.getItemId(),Utils.removeFormatting(amountEt.getText().toString()),
                        revenueManager.getCurrentAgentId(),isBusiness ? newBusiness.getAccountCode() : newProperty.getAccountCode(),
                        receiptNo, gcrNoEt.getText().toString(),new Date().toString(),0,locationStr)){
                    revenueManager.printReceipt(receiptNo,item.getName(),Utils.removeFormatting(amountEt.getText().toString()),
                            nameTv.getText().toString().trim());
                    Toast.makeText(getActivity(), "Transaction Successful", Toast.LENGTH_SHORT).show();
                };
                BusPropDialog.this.getDialog().cancel();

            }
        });

        builder.setView(view)
                // Add action buttons
                .setTitle(item.getName())
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BusPropDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}

