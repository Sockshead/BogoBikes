package bogobikes.app;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Registro extends AppCompatActivity {

    private static final String TAG ="Register" ;
    private FirebaseAuth mAuth;
    private EditText mName,mUser,mPassword,mCedula;
    private Button mRegister;
    private ProgressDialog mProgress;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef,myRefCU;
    private FirebaseStorage mStorage;
    private StorageReference mySRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mySRef =mStorage.getReference();
        myRef = mDatabase.getReference().child("Users");
        mName = findViewById(R.id.txtNombre);
        mUser = findViewById(R.id.txtEmail);
        mCedula = findViewById(R.id.txtCedula);
        mPassword = findViewById(R.id.txtPass);
        mAuth = FirebaseAuth.getInstance();
        mRegister = findViewById(R.id.btnReg);
        mProgress = new ProgressDialog(this);
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        FirebaseUser currentUser = mAuth.getCurrentUser();

    }

    public void Register(){
        final String name = mName.getText().toString().trim();
        final String email = mUser.getText().toString().trim();
        final String cedula = mCedula.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();
        //Check if fields are empty.
        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)&&!TextUtils.isEmpty(name)&&!TextUtils.isEmpty(cedula)){
            mProgress.setMessage("Registrando nuevo usuario...");
            mProgress.show();
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            mProgress.dismiss();
                            if (task.isSuccessful()) {
                                // Sign in success.
                                Log.d(TAG, "Usuario Creado Correctamente");
                                final FirebaseUser user = mAuth.getCurrentUser();
                                qrCode(user);
                                mProgress.setMessage("Iniciando Sesión...");
                                mProgress.show();
                                loginR(email,password,name,Integer.parseInt(cedula));
                            } else {
                                // If sign in fails.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Registro.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }

                            // ...
                        }
                    });

        }else{
            if(TextUtils.isEmpty(email)){
                mUser.setError("Ingrese un Email");
            }
            if(TextUtils.isEmpty(password)){
                mPassword.setError("Ingrese una Contraseña");
            }
            if(TextUtils.isEmpty(password)){
                mName.setError("Ingrese un Nombre");
            }
            if(TextUtils.isEmpty(password)){
                mCedula.setError("Ingrese un numero de Cedula");
            }
        }
    }
    private void loginR (final String emailL, String passwordL, final String nameL, final int cedulaL){
        mAuth.signInWithEmailAndPassword(emailL, passwordL)
                .addOnCompleteListener(Registro.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        mProgress.dismiss();
                        mProgress.setMessage("Iniciando Sesión...");
                        mProgress.show();
                        if (task.isSuccessful()) {
                            // Sign in success.
                            myRefCU = myRef.child(user.getUid());
                            myRefCU.child("Name").setValue(nameL);
                            myRefCU.child("Email").setValue(emailL);
                            myRefCU.child("Cedula").setValue(cedulaL);
                            myRefCU.child("Profile Image").setValue("Default");
                            mProgress.dismiss();
                            Log.d(TAG, "signInWithEmail:success");

                        } else {
                            // If sign in fails.
                            mProgress.dismiss();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(Registro.this, "Datos Incorrectos",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }
    private void qrCode(FirebaseUser user){
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try{
            BitMatrix bitMatrix = multiFormatWriter.encode(user.getUid().toString().trim(), BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

                final StorageReference filePath = mySRef.child("QRCodes").child(getRandomString());

            UploadTask uploadTask = filePath.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(Registro.this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(Registro.this,"Finished",Toast.LENGTH_SHORT).show();
                    DatabaseReference currentUserDB = myRef.child(mAuth.getCurrentUser().getUid());
                    currentUserDB.child("QR Code").setValue(downloadUrl.toString());
                }
            });
        }catch(WriterException e){
            e.printStackTrace();

        }
    }
    public String getRandomString() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }
}
