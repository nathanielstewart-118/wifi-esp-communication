package com.prtech.spiapp.utils.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.core.content.ContextCompat;

import com.prtech.spiapp.R;

public class CustomLoadingButton extends FrameLayout {

    private Button button;
    private ProgressBar progressBar;
    private String originalText;

    public CustomLoadingButton(Context context) {
        super(context);
        init(context);
    }

    public CustomLoadingButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomLoadingButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setActive(boolean isActive) {
        if (isActive) {
            // Use your theme's primary or success color
            button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ripple_info_button));
            button.setTextColor(ContextCompat.getColor(getContext(), android.R.color.white));
        } else {
            // Reset to default button background (optional)
            button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ripple_gray_button));
        }
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_custom_loading_button, this, true);
        button = findViewById(R.id.customButton);
        button.setEnabled(false);
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.basic_button_text_color));
        button.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.ripple_gray_button));
        progressBar = findViewById(R.id.customProgressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(
                ContextCompat.getColor(getContext(), R.color.bs_light),
                android.graphics.PorterDuff.Mode.SRC_IN
        );
    }

    public void setOnClickListener(OnClickListener listener) {
        button.setOnClickListener(listener);
    }

    public void showLoading() {
        originalText = button.getText().toString();
        button.setText("");
        button.setEnabled(false);
        progressBar.setVisibility(VISIBLE);
    }

    public void setText(CharSequence sequence) {
        button.setText(sequence);
    }

    public void hideLoading() {
        button.setText(originalText);
        button.setEnabled(true);
        progressBar.setVisibility(GONE);
    }
}
