package bogobikes.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mUser, mPassword;
    private Button mLogin,mRegister;
    private ProgressDialog mProgress;
    private CallbackManager mCallbackManager;
    private String TAG = "Login";
    private LoginButton loginButton;
    private Boolean logedIn;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef,myRefCU;
    private FirebaseStorage mStorage;
    private StorageReference mySRef;

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in.
       /* FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            logedIn = true;
            Intent afterLog = new Intent(Login.this,MenuWIP.class);
            startActivity(afterLog);
        }
        else {
            logedIn=false;
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgress = new ProgressDialog(this);
        mUser = findViewById(R.id.txtUsuario);
        mPassword = findViewById(R.id.txtPassword);
        mLogin = findViewById(R.id.btnLogin);
        mRegister = findViewById(R.id.btnCrear);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mySRef =mStorage.getReference();
        myRef = mDatabase.getReference().child("Users");
        loginButton = findViewById(R.id.btnFace);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }
        });
        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Reg = new Intent(Login.this,Registro.class);
                startActivity(Reg);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                facebookLogin();
                }
        }
        );
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "bogobikes.app",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }



    private void Login() {
        String email = mUser.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        //Check if fields are empty.
        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)) {
            mProgress.setMessage("Iniciando Sesión...");
            mProgress.show();
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success.
                                mProgress.dismiss();
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(Login.this, "Datos Correctos",
                                        Toast.LENGTH_SHORT).show();

                                /*Intent afterLog = new Intent(Login.this,mapaParq.class);
                                startActivity(afterLog);*/


                                Intent afterLog = new Intent(Login.this,MainActivity.class);
                                startActivity(afterLog);
                                mUser.setText("");
                                mPassword.setText("");
                            } else {
                                // If sign in fails.
                                mProgress.dismiss();
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, "Datos Incorrectos",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        else{
            if(TextUtils.isEmpty(email)){
                mUser.setError("Ingrese un Email");
            }
            if(TextUtils.isEmpty(password)){
                mPassword.setError("Ingrese una Contraseña");
        }
        }

    }
    private void facebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });


    }
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }





    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            myRefCU = myRef.child(user.getUid());
                            myRefCU.child("Name").setValue(user.getDisplayName());
                            myRefCU.child("Email").setValue(user.getEmail());
                            // myRefCU.child("Cedula").setValue(cedulaL);
                            myRefCU.child("Profile Image").setValue(user.getPhotoUrl().toString());
                            Intent afterLog = new Intent(Login.this,MainActivity.class);
                            startActivity(afterLog);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

}




