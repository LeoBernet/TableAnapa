package com.android.bernet.youtableanapa.screens;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.android.bernet.youtableanapa.R;

public class ChooseImagesActivity extends AppCompatActivity {

    private String uriMain ="empty",uri2 ="empty",uri3 ="empty";
    private ImageView imMain,im2,im3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_images);
        init();

    }
    private void init()
    {
        imMain = findViewById(R.id.mainImage);
        im2 = findViewById(R.id.image2);
        im3 = findViewById(R.id.image3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && data.getData() !=null)
        {
            switch (requestCode)
            {
                case 1:
                    uriMain = data.getData().toString();
                    imMain.setImageURI(data.getData());
                    break;
                case 2:
                    uri2 = data.getData().toString();
                    im2.setImageURI(data.getData());
                    break;
                case 3:
                    uri3 = data.getData().toString();
                    im3.setImageURI(data.getData());
                    break;
            }
        }
    }

    public void onClickMainImage(View view)
    {
        getImage(1);
    }
    public void onClickImage2(View view)
    {
        getImage(2);
    }
    public void onClickImage3(View view)
    {
        getImage(3);
    }
    public void onClickBack(View view)
    {
        Intent i = new Intent();
        i.putExtra("uriMain",uriMain);
        i.putExtra("uri2",uri2);
        i.putExtra("uri3",uri3);
        setResult(RESULT_OK,i);
        finish();
    }
    private void getImage(int index) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, index);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    public void onClickDeleteMyImage(View view)
    {
        imMain.setImageResource(android.R.drawable.ic_menu_add);
        uriMain = "empty";
    }

    public void onClickDeleteImage2(View view)
    {
        im2.setImageResource(android.R.drawable.ic_menu_add);
        uri2 = "empty";
    }

    public void onClickDeleteImage3(View view)
    {
        im3.setImageResource(android.R.drawable.ic_menu_add);
        uri3 = "empty";
    }
}