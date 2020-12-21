package com.pluto.persolrevenuemanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ReprintDialog extends DialogFragment {

    private TextInputLayout gcrNoTIL;
    private TextInputEditText gcrNoET;
    private MaterialButton reprintBtn;
    private RevenueManager revenueManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.reprint_card, null);

        revenueManager = new RevenueManager(getActivity());

        gcrNoTIL = view.findViewById(R.id.gcrnumberTIL);
        gcrNoET = view.findViewById(R.id.gcrnumberEt);
        reprintBtn = view.findViewById(R.id.reprintBtn);

        reprintBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gcrNoET.getText().toString().trim().isEmpty()){
                    gcrNoTIL.setError("This field can not be empty");
                    return;
                }
                revenueManager.getReprintTransaction(gcrNoET.getText().toString().trim());
            }
        });

        builder.setView(view)
                // Add action buttons
                .setTitle("Reprint a Transaction")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ReprintDialog.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }
}
