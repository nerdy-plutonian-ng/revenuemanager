package com.pluto.persolrevenuemanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SimpleRevenueDialog extends DialogFragment {

    private RevenueManager revenueManager;
    private int itemId;
    private ArrayList<Rate> rateArrayList;
    private String[] rates;
    private double defaultRate = 1;

    public SimpleRevenueDialog(int itemId) {
        this.itemId = itemId;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        revenueManager = new RevenueManager(getActivity());

        final Item item = revenueManager.getItem(itemId);
        rateArrayList = revenueManager.getRates(item.getItemId());
        rates = new String[rateArrayList.size()];
        for(int i = 0;i < rateArrayList.size();i++){
            rates[i] = rateArrayList.get(i).getName();
        }
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.simple_revenue_layout, null);
        final TextInputEditText amountEt = view.findViewById(R.id.amountEt);
        Spinner rateSpinner = view.findViewById(R.id.rateCatSpinner);
        TextView ratesLabel = view.findViewById(R.id.rateLabel);
        if(rateArrayList.size() == 0){
            rateSpinner.setVisibility(View.GONE);
            ratesLabel.setVisibility(View.GONE);
        } else {
            rateSpinner.setVisibility(View.VISIBLE);
            ratesLabel.setVisibility(View.VISIBLE);
            ArrayAdapter<String> ratesAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item,android.R.id.text1,rates);
            rateSpinner.setAdapter(ratesAdapter);
            rateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    defaultRate = rateArrayList.get(position).getRate();
                    amountEt.setText(Utils.formatMoney(item.getBaseAmount()*defaultRate));
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
        builder.setView(view)
                // Add action buttons
                .setTitle(item.getName())
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String receiptNo = revenueManager.generateReceipt();
                        double[] coors = revenueManager.getGPSCoor();
                        String locationStr = coors[0] + "," + coors[1];
                        if(revenueManager.saveTransaction(item.getItemId(),item.getBaseAmount()*defaultRate,
                                revenueManager.getCurrentAgentId(),"CASH",receiptNo,
                                UUID.randomUUID().toString(), new Date().toString(),0,locationStr)){
                            Toast.makeText(getActivity(), "Transaction Successful", Toast.LENGTH_SHORT).show();
                            revenueManager.printReceipt(receiptNo,item.getName(),(item.getBaseAmount()*defaultRate),"CASH");
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SimpleRevenueDialog.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }
}