package com.example.projectafd.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.projectafd.R;
import com.example.projectafd.models.DialogBuilderResultObject;

public class DialogBuilders {

    public static DialogBuilderResultObject openNameInputDialog(Context context, LayoutInflater inflater, String message,
                                                                DialogInterface.OnClickListener onClickListener) {
        return openNameInputDialog(context, inflater, message, "Create", onClickListener);
    }

    public static DialogBuilderResultObject openNameInputDialog(Context context, LayoutInflater inflater, String message,
                                                                String positiveButtonText, DialogInterface.OnClickListener onClickListener) {
        return openNameInputDialog(context, inflater, message, positiveButtonText, onClickListener, ((dialog, which) -> {} ));
    }

    public static DialogBuilderResultObject openNameInputDialog(Context context, LayoutInflater inflater, String message,
                                                                String positiveButtonText, DialogInterface.OnClickListener onClickListener,
                                                                DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(message);

        View alertDialogView = inflater.inflate(R.layout.alert_dialog_name_input, null);
        EditText nameInputAlertDialogEditText = alertDialogView.findViewById(R.id.alertDialogNameInputEditText);
        alertDialogBuilder.setView(alertDialogView);

        alertDialogBuilder.setPositiveButton(positiveButtonText, onClickListener);
        alertDialogBuilder.setNegativeButton("Cancel", onNegativeClickListener);

        alertDialogBuilder.create().show();
        return DialogBuilderResultObject.builder()
                .nameInputAlertDialogEditText(nameInputAlertDialogEditText)
                .build();
    }

    public static void openConfirmDialog(Context context, LayoutInflater inflater, String message,
                                         DialogInterface.OnClickListener onClickListener) {
        openConfirmDialog(context, inflater, message, "Cancel", onClickListener,
                (dialog, which) -> { });
    }

    public static void openConfirmDialog(Context context, LayoutInflater inflater, String message,
                                         String negativeButtonText, DialogInterface.OnClickListener onClickListener,
                                         DialogInterface.OnClickListener onNegativeClickListener) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(message);

        alertDialogBuilder.setView(inflater.inflate(R.layout.alert_dialog_confirm, null));

        alertDialogBuilder.setPositiveButton("Confirm", onClickListener);
        alertDialogBuilder.setNegativeButton(negativeButtonText, onNegativeClickListener);

        alertDialogBuilder.create().show();
    }
}
