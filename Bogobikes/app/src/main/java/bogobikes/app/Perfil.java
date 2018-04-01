package bogobikes.app;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

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
        myDBRef = mDatabase.getReference().child("users");
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
                           name.setText(String.valueOf(dataSnapshot.child("Name").getValue()));
                           cedula.setText(String.valueOf(dataSnapshot.child("Cedula").getValue()));
                           email.setText(String.valueOf(dataSnapshot.child("Email").getValue()));
                           String imgURL = String.valueOf(dataSnapshot.child("Profile Image"));
                           if(URLUtil.isValidUrl(imgURL)){
                               Picasso.with(Perfil.this).load(Uri.parse(imgURL)).into(imgProf);
                           }

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
}
