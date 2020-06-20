package com.example.babyimhome;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.util.zip.Inflater;

import static android.content.Context.MODE_PRIVATE;


public class SMSPopup extends AppCompatDialogFragment {
    private final static String TITLE = "Add a new phone number";
    private SharedPreferences sharedPreferences;
    public SMSPopup(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }
    private EditText phoneNum;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.popup_layout,null);
        phoneNum = view.findViewById(R.id.editText);
        builder.setView(view)
        .setTitle(TITLE)
        .setPositiveButton("Set number", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                sharedPreferences.edit().putString(LocalSendSmsBroadcastReceiver.PHONE,phoneNum.getText().toString()).apply();
            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        return builder.create();
    }
}
