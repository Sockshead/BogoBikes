package bogobikes.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Perfil extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    TextView name,cedula,email;
    ImageView imgProf;
    private int CAMERA_REQUEST_CODE = 0;
    private ProgressDialog mProgressDialog;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myDBRef;
    private StorageReference mySRef;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mySRef =mStorage.getReference();
        mDatabase = FirebaseDatabase.getInstance();
        myDBRef = mDatabase.getReference().child("Users");
        name = findViewById(R.id.line);
        cedula = findViewById(R.id.line2);
        email = findViewById(R.id.line3);
        imgProf = findViewById(R.id.imgProf);
        mProgressDialog = new ProgressDialog(this);

        imgProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if(intent.resolveActivity(getPackageManager())!= null){
                    startActivityForResult(Intent.createChooser(intent,"Seleccione una imagen de perfil"),CAMERA_REQUEST_CODE);
                }
            }
        });
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!=null){
                   myDBRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                       @Override
                       public void onDataChange(DataSnapshot dataSnapshot) {
                           mProgressDialog.setMessage("Cargando Perfil");
                           mProgressDialog.show();
                           name.setText(String.valueOf(dataSnapshot.child("Name").getValue()));
                           cedula.setText(String.valueOf(dataSnapshot.child("Cedula").getValue()));
                           email.setText(String.valueOf(dataSnapshot.child("Email").getValue()));
                           String imgURL = String.valueOf(dataSnapshot.child("Profile Image").getValue());
                           if(URLUtil.isValidUrl(imgURL)){
                               Picasso.with(Perfil.this).load(Uri.parse(imgURL)).fit().centerCrop().into(imgProf);
                           }
                           else{
                               System.out.println("URL no valida");
                           }
                           mProgressDialog.dismiss();

                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });

                }
                else{
                    startActivity(new Intent(Perfil.this, Login.class));
                    finish();
                }
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            if(mAuth.getCurrentUser()==null)
                return;
            mProgressDialog.setMessage("Subiendo Imagen");
            mProgressDialog.show();
            final Uri uri = data.getData();
            if(uri==null){
                mProgressDialog.dismiss();
                return;
            }
            if(mySRef == null){
                mySRef = mStorage.getReference();
            }
            if(myDBRef == null){
                myDBRef = mDatabase.getReference().child("Users");
            }

            final StorageReference filePath = mySRef.child("Photos").child(getRandomString());
            final DatabaseReference currentUserDB = myDBRef.child(mAuth.getCurrentUser().getUid());
            currentUserDB.child("Profile Image").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image =dataSnapshot.getValue().toString();
                    if(!image.equals("Default")&&!image.isEmpty()){
                        Task<Void> task = FirebaseStorage.getInstance().getReferenceFromUrl(image).delete();
                        task.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Perfil.this,"Imagen Borrada correctamente",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(Perfil.this,"Error al eliminar Imagen",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    currentUserDB.child("Profile Image").removeEventListener(this);
                    filePath.putFile(uri).addOnSuccessListener(Perfil.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.dismiss();
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            Toast.makeText(Perfil.this,"Finished",Toast.LENGTH_SHORT).show();
                            Picasso.with(Perfil.this).load(uri).fit().centerCrop().into(imgProf);
                            DatabaseReference currentUserDB = myDBRef.child(mAuth.getCurrentUser().getUid());
                            currentUserDB.child("Profile Image").setValue(downloadUri.toString());
                        }
                    }).addOnFailureListener(Perfil.this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(Perfil.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
    }
    public String getRandomString() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
