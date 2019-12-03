package com.harshit.instagramclone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class loggedIn extends AppCompatActivity  {

    Intent intent;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    DatabaseReference userDatabase, imageDatabase;
    ArrayList<String> userList = new ArrayList<>();
    ArrayAdapter arrayAdapter;
    ListView listView;
    Uri selectedImage;              // Uniform Resource identifier
    int start, end, i=0;
    ImageView imageView;
    String string = "";
    LinearLayout linearLayout;

    public void uploadPhoto(){

        Intent photoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(photoIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            try {
                selectedImage = data.getData();    //gives the path of selected image in storage

                final Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage); //bitmap.toString() = (android.graphics.Bitmap@6211898)

                StorageReference storageRef = FirebaseStorage.getInstance().getReference();     // gs://insta-6969.appspot.com/
                start = selectedImage.toString().length()-3;
                end = selectedImage.toString().length();
                StorageReference mountainsRef = storageRef.child(currentUser.getEmail()).child(selectedImage.toString().substring(start, end));  // gs://insta-6969.appspot.com/currentUserImages.jpg
                // StorageReference mountainImagesRef = storageRef.child("images/mountains.jpg");  //     gs://insta-6969.appspot.com/images/mountains.jpg

                // Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);  //bitmap can also be used
                final byte[] dat = baos.toByteArray();

                UploadTask uploadTask = mountainsRef.putBytes(dat);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(loggedIn.this, exception.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        userDatabase = FirebaseDatabase.getInstance().getReference();
                        userDatabase.child("Images").child(currentUser.getUid()).child(selectedImage.toString().substring(start, end)).setValue(selectedImage.toString().substring(start, end));
                        imageView.setImageBitmap(bitmap1);
                        imageView.setVisibility(View.VISIBLE);
                        Toast.makeText(loggedIn.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);               // ask the user to grant permission by popping dialogue box
        if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            uploadPhoto();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){

            case R.id.logOut :
                mAuth.signOut();
                startActivity(intent);
                break;
            case R.id.userList :
                imageView.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                if( linearLayout.getChildCount() > 0)
                    (linearLayout).removeAllViews();
                linearLayout.setVisibility(View.INVISIBLE);
                listView.setVisibility(View.VISIBLE);
                break;
            case R.id.shareImage :
                listView.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.images);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        uploadPhoto();
                    }
                }else
                    uploadPhoto();
                break;
        }
        return true;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                // If method overrides one of its superclass's methods, then invoke the overridden method through the use of the keyword super
        setContentView(R.layout.activity_logged_in);

        intent = new Intent(this, MainActivity.class);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userDatabase = FirebaseDatabase.getInstance().getReference("Users/");  // or FirebaseDatabase.getInstance().getReference().child("Users");
        imageDatabase = FirebaseDatabase.getInstance().getReference("Images/");

        linearLayout = findViewById(R.id.linearLayout);               // for selected user's images
        linearLayout.setVisibility(View.INVISIBLE);

        listView = findViewById(R.id.listView);                        // for users list
        imageView = findViewById(R.id.imageView);
        imageView.setVisibility(View.INVISIBLE);

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, userList);
        listView.setAdapter(arrayAdapter);
        userDatabase.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                /*
                    Log.i("dataSnapshot",dataSnapshot.getValue().toString());   {E-mail=khushal@gmail.com, Name=khushal}, {E-mail=harshit@gm.com, Name=harshit}
                    dataSnapshot read one child ({E-mail=khushal@gmail.com, Name=khushal}) at a time
                    nd then second child ({E-mail=harshit@gm.com, Name=harshit})
                    it works as loop
                 */
                if(dataSnapshot.getValue().toString().contains(currentUser.getEmail())){
                    userList.add("My Account (" + currentUser.getEmail() + ")");
                }else {
                    for (DataSnapshot a : dataSnapshot.getChildren()) {
                        if(i<=2) {

                            if (a.getKey().equals("Name")) {
                                String temp = string;
                                string = a.getValue().toString() + " (" + temp;
                            }
                            if (a.getKey().equals("E-mail"))
                                string = a.getValue().toString() + ")";
                            i++;
                            if(i==2) {
                                i = 0;
                                userList.add(string);
                                string="";
                            }
                        }

                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,final int position, long id) {

                Toast.makeText(loggedIn.this,"Please Wait while we are loading images", Toast.LENGTH_LONG).show();
                linearLayout.setVisibility(View.VISIBLE);

                final int startIndex = userList.get(position).indexOf("(") + 1, endIndex = userList.get(position).indexOf(")");
                imageDatabase.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        for (final DataSnapshot ds : dataSnapshot.getChildren()) {
                            StorageReference imageReference = FirebaseStorage.getInstance().getReference(userList.get(position).substring(startIndex, endIndex) + "/" +
                                    ds.getKey());                                                    // imageReference contains the path of image
                            imageReference.getBytes(1024 * 1024)
                                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {

                                            ImageView userImages = new ImageView(getApplicationContext());

                                            listView.setVisibility(View.INVISIBLE);
                                            imageView.setVisibility(View.INVISIBLE);

                                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                            DisplayMetrics dm = new DisplayMetrics();
                                            getWindowManager().getDefaultDisplay().getMetrics(dm);

                                            userImages.setMinimumHeight(dm.heightPixels-550);
                                            userImages.setMinimumWidth(dm.widthPixels);
                                            userImages.setImageBitmap(bm);
                             /*
                                                userImages.setLayoutParams(new ViewGroup.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,                         // width
                                                        ViewGroup.LayoutParams.WRAP_CONTENT                          // height
                                                )); */

                                            try {
                                                linearLayout.addView(userImages);
                                            }catch(Exception e) {
                                                Toast.makeText(loggedIn.this,e.getMessage() , Toast.LENGTH_LONG).show();
                                            }

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //Toast.makeText(loggedIn.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onChildChanged (@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                    }

                    @Override
                    public void onChildRemoved (@NonNull DataSnapshot dataSnapshot){
                    }

                    @Override
                    public void onChildMoved (@NonNull DataSnapshot dataSnapshot, @Nullable String s){
                    }

                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError){
                    }
                });
            }
        });

    }

}
