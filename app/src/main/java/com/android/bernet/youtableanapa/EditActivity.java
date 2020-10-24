package com.android.bernet.youtableanapa;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.android.bernet.youtableanapa.adapter.ImageAdapter;
import com.android.bernet.youtableanapa.screens.ChooseImagesActivity;
import com.android.bernet.youtableanapa.utils.MyConstants;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditActivity extends AppCompatActivity {

    private StorageReference mStorageRef;
    private String[] uploadUri = new String[3];
    private Spinner spinner;
    private DatabaseReference dRef;
    private FirebaseAuth mAuth;
    private EditText edTitle, edPrice, edTel, edDisc;
    private boolean edit_state = false;
    private String temp_cat = "";
    private String temp_uid = "";
    private String temp_time = "";
    private String temp_key = "";
    private String temp_total_views = "";
    private String temp_image_url = "";
    private boolean is_image_update = false;
    private ProgressDialog pd;
    private int load_image_counter = 0;
    private List<String> imagesUris;
    private ImageAdapter imAdapter;
    private TextView tvImagesCounter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_layout);
        init();
    }

    private void init() {

        tvImagesCounter = findViewById(R.id.tvImagesCounter);
        imagesUris = new ArrayList<>();

        ViewPager vp = findViewById(R.id.view_pager);
        imAdapter = new ImageAdapter(this);
        vp.setAdapter(imAdapter);
        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                String dataText = position + 1 +"/" + imagesUris.size();
                tvImagesCounter.setText(dataText);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        uploadUri[0] = "empty";
        uploadUri[1] = "empty";
        uploadUri[2] = "empty";

        pd = new ProgressDialog(this);
        pd.setMessage("Идёт загрузка...");
        edTitle = findViewById(R.id.edTitle);
        edTel = findViewById(R.id.edTel);
        edPrice = findViewById(R.id.edPrice);
        edDisc = findViewById(R.id.edDisc);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.category_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        getMyIntent();
    }

    private void getMyIntent() {
        if (getIntent() != null) {
            Intent i = getIntent();
            edit_state = i.getBooleanExtra(MyConstants.EDIT_STATE, false);
            if (edit_state) setDataAds(i);
        }
    }

    private void setDataAds(Intent i) {
        // Picasso.get().load(i.getStringExtra(MyConstants.IMAGE_ID)).into(imItem);
        edTel.setText(i.getStringExtra(MyConstants.TEL));
        edTitle.setText(i.getStringExtra(MyConstants.TITLE));
        edPrice.setText(i.getStringExtra(MyConstants.PRICE));
        edDisc.setText(i.getStringExtra(MyConstants.DISC));
        temp_cat = i.getStringExtra(MyConstants.CAT);
        temp_uid = i.getStringExtra(MyConstants.UID);
        temp_time = i.getStringExtra(MyConstants.TIME);
        temp_key = i.getStringExtra(MyConstants.KEY);
        temp_image_url = i.getStringExtra(MyConstants.IMAGE_ID);
        temp_total_views = i.getStringExtra(MyConstants.TOTAL_VIEWS);
    }

    private void uploadImage() {

        if (load_image_counter < uploadUri.length) {
            if (!uploadUri[load_image_counter].equals("empty")) {
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(uploadUri[load_image_counter]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                assert bitmap != null;
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                byte[] byteArray = out.toByteArray();
                final StorageReference mRef = mStorageRef.child(System.currentTimeMillis() + "_image");
                UploadTask up = mRef.putBytes(byteArray);
                Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        return mRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.getResult() == null) return;
                        uploadUri[load_image_counter] = task.getResult().toString();
                        assert uploadUri != null;
                        load_image_counter++;
                        if (load_image_counter < uploadUri.length) {
                            uploadImage();
                        } else {
                            savePost();
                            Toast.makeText(EditActivity.this,
                                    "Upload done!", Toast.LENGTH_SHORT).show();
                            finish();
                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            } else {
                load_image_counter++;
                uploadImage();
            }
        } else {
            savePost();
            finish();
        }

    }

    private void uploadUpdateImage() {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(uploadUri[load_image_counter]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        byte[] byteArray = out.toByteArray();
        final StorageReference mRef = FirebaseStorage.getInstance().getReferenceFromUrl(temp_image_url);
        UploadTask up = mRef.putBytes(byteArray);
        Task<Uri> task = up.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                return mRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                uploadUri[0] = task.getResult().toString();
                assert uploadUri != null;
                temp_image_url = uploadUri.toString();
                updatePost();
                Toast.makeText(EditActivity.this,
                        "Upload done!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public void onClickSavePost(View view) {
        pd.show();
        if (!edit_state) {

            uploadImage();
        } else {
            if (is_image_update) {
                uploadUpdateImage();
            } else {
                updatePost();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 15 && data != null) {
            if (resultCode == RESULT_OK) {
                uploadUri[0] = data.getStringExtra("uriMain");
                uploadUri[1] = data.getStringExtra("uri2");
                uploadUri[2] = data.getStringExtra("uri3");
                imagesUris.clear();
                for (String s : uploadUri) {
                    if (!s.equals("empty")) imagesUris.add(s);
                }
                imAdapter.updateImages(imagesUris);
                String dataText = 1 +"/" + imagesUris.size();
                tvImagesCounter.setText(dataText);

            }
        }
    }

    public void onClickImage(View view) {
        Intent i = new Intent(EditActivity.this, ChooseImagesActivity.class);
        startActivityForResult(i, 15);
    }


    private void updatePost() {
        dRef = FirebaseDatabase.getInstance().getReference(temp_cat);

        NewPost post = new NewPost();

        post.setImageId(temp_image_url);
        post.setTitle(edTitle.getText().toString());
        post.setTel(edTel.getText().toString());
        post.setPrice(edPrice.getText().toString());
        post.setDisc(edDisc.getText().toString());
        post.setKey(temp_key);
        post.setCat(temp_cat);
        post.setTime(temp_time);
        post.setUid(temp_uid);
        post.setTotal_views(temp_total_views);

        dRef.child(temp_key).child("anuncio").setValue(post);
        finish();

    }

    private void savePost() {
        dRef = FirebaseDatabase.getInstance().getReference(spinner.getSelectedItem().toString());
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getUid() != null) {
            String key = dRef.push().getKey();
            NewPost post = new NewPost();

            post.setImageId(uploadUri[0]);
            post.setImageId2(uploadUri[1]);
            post.setImageId3(uploadUri[2]);
            post.setTitle(edTitle.getText().toString());
            post.setTel(edTel.getText().toString());
            post.setPrice(edPrice.getText().toString());
            post.setDisc(edDisc.getText().toString());
            post.setKey(key);
            post.setCat(spinner.getSelectedItem().toString());
            post.setTime(String.valueOf(System.currentTimeMillis()));
            post.setUid(mAuth.getUid());
            post.setTotal_views("0");

            if (key != null) dRef.child(key).child("anuncio").setValue(post);
            Intent i = new Intent();
            i.putExtra("cat", spinner.getSelectedItem().toString());
            setResult(RESULT_OK, i);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pd.dismiss();
    }
}
