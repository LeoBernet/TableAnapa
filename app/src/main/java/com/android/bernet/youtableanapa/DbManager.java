package com.android.bernet.youtableanapa;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.bernet.youtableanapa.adapter.DataSender;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DbManager {
    private Context context;
    private Query mQuery;
    private List<NewPost> newPostList;
    private DataSender dataSender;
    private FirebaseDatabase db;
    private FirebaseStorage fs;
    private int cat_ads_counter = 0;
    private String[] category_ads = {"Машины","Компьютеры","Смартфоны","Бытовая техника"};
    private int deleteImageCounter = 0;

    public void deleteItem(final NewPost newPost)
    {
        StorageReference sRef = null;
        switch (deleteImageCounter)
        {
            case 0:
                if (!newPost.getImageId().equals("empty")){
                    sRef = fs.getReferenceFromUrl(newPost.getImageId());
            }
                else
                {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                break;
            case 1:
                if (!newPost.getImageId2().equals("empty")){
                    sRef = fs.getReferenceFromUrl(newPost.getImageId2());
                }
                else
                {
                    deleteImageCounter++;
                    deleteItem(newPost);
                }
                break;
            case 2:
                if (!newPost.getImageId3().equals("empty")){
                    sRef = fs.getReferenceFromUrl(newPost.getImageId3());
                }
                else
                {
                    deleteDbItem(newPost);
                    sRef = null;
                    deleteImageCounter = 0;
                }

                break;
        }
        if (sRef == null)return;

        sRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid)
            {
                deleteImageCounter++;
                if (deleteImageCounter < 3)
                {
                    deleteItem(newPost);
                }
                else
                {
                    deleteImageCounter = 0;
                    deleteDbItem(newPost);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Ошибка, изображение не было удалено!", Toast.LENGTH_SHORT).show();

            }
        });

    }
    private void deleteDbItem (NewPost newPost)
    {
        DatabaseReference dbRef = db.getReference(newPost.getCat());
        dbRef.child(newPost.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(context, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Ошибка, item не было удалено!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void updateTotalViews(final NewPost newPost)
    {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference(newPost.getCat());
        int total_views;
        try
        {
            total_views = Integer.parseInt(newPost.getTotal_views());
        }
        catch (NumberFormatException e)
        {
            total_views = 0;
        }
        total_views ++;
        dRef.child(newPost.getKey()).child("anuncio/total_views")
                .setValue(String.valueOf(total_views));
    }

    public DbManager(DataSender dataSender,Context context) {
        this.dataSender = dataSender;
        this.context = context;
        newPostList = new ArrayList<>();
        db = FirebaseDatabase.getInstance();
        fs = FirebaseStorage.getInstance();
    }

    public void getDataFromDb(String path) {
        
        DatabaseReference dbRef = db.getReference(path);
        mQuery = dbRef.orderByChild("anuncio/time");
        readDataUpdate();

    }
    public void getMyAdsDataFromDb(String uid) {
        if (newPostList.size()>0 )newPostList.clear();
        DatabaseReference dbRef = db.getReference(category_ads[0]);
        mQuery = dbRef.orderByChild("anuncio/uid").equalTo(uid);
        readMyAdsDataUpdate(uid);
        cat_ads_counter ++;

    }

    public void readDataUpdate() {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (newPostList.size() > 0) newPostList.clear();
                for (DataSnapshot ds : snapshot.getChildren())
                {
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    newPostList.add(newPost);
                }

                dataSender.onDataRecived(newPostList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void readMyAdsDataUpdate(final String uid) {
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot ds : snapshot.getChildren()) {
                    NewPost newPost = ds.child("anuncio").getValue(NewPost.class);
                    newPostList.add(newPost);
                }
                if (cat_ads_counter > 3)
                {
                    dataSender.onDataRecived(newPostList);
                    newPostList.clear();
                    cat_ads_counter = 0;
                }
                else
                {
                    DatabaseReference dbRef = db.getReference(category_ads[cat_ads_counter]);
                    mQuery = dbRef.orderByChild("anuncio/uid").equalTo(uid);
                    readMyAdsDataUpdate(uid);
                    cat_ads_counter ++;
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
