package com.android.bernet.youtableanapa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.bernet.youtableanapa.utils.MyConstants;
import com.squareup.picasso.Picasso;

public class ShowLayoutActivity extends AppCompatActivity {
    private TextView tvTitle, tvPrice, tvDisc, tvTel;
    private ImageView imMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_layout_activity);
        init();
    }
    private void init()
    {
        tvTitle = findViewById(R.id.tvTitle1);
        tvTel = findViewById(R.id.tvTel);
        tvPrice = findViewById(R.id.tvPrice);
        tvDisc = findViewById(R.id.tvDisc1);
        imMain = findViewById(R.id.imMain);
        if (getIntent()!=null)
        {
            Intent i = getIntent();
            tvTitle.setText(i.getStringExtra(MyConstants.TITLE));
            tvTel.setText(i.getStringExtra(MyConstants.TEL));
            tvPrice.setText(i.getStringExtra(MyConstants.PRICE));
            tvDisc.setText(i.getStringExtra(MyConstants.DISC));
            Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imMain);
        }

    }
}