package com.example.projectafd.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.projectafd.models.ActionResult;

public class ToastBuilders {

    public static void openShortToast(Context context, ActionResult actionResult) {
        openShortToast(context, actionResult.getMessage());
    }

    public static void openShortToast(Context context, String textToShow) {
        Toast t = Toast.makeText(context, textToShow, Toast.LENGTH_SHORT);
        t.show();
    }
}
