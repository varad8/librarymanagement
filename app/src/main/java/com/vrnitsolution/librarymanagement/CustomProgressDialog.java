package com.vrnitsolution.librarymanagement;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_progress_dialog);
        setCancelable(false);
    }

    public void setMessage(String message) {
        TextView textView = findViewById(R.id.customProgressText);
        if (textView != null) {
            textView.setText(message);
        }
    }
}
