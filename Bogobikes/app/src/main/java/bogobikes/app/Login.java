package bogobikes.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mUser, mPassword;
    private Button mLogin,mRegister,mGoogle;
    private ProgressDialog mProgress;
    private CallbackManager mCallbackManager;
    private String TAG = "Login";
    private LoginButton facebookLoginButton;
    private Boolean logedIn;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef,myRefCU;
    private FirebaseStorage mStorage;
    private StorageReference mySRef;
    private TwitterLoginButton twitterLoginButton;
    private String loginMethod="Email";

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in.
       FirebaseUser currentUser = mAuth.getCurrentUser();
        /*if(currentUser != null){
            logedIn = true;
            finish();
        }
        else {
            logedIn=false;
        }*/
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY)
                ,getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET));
        TwitterConfig twitterConfig = new TwitterConfig.Builder(this).twitterAuthConfig(authConfig).build();
        Twitter.initialize(twitterConfig);

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
        mGoogle = findViewById(R.id.btnGoogle);
        myRef = mDatabase.getReference().child("Users");
        facebookLoginButton = findViewById(R.id.btnFace);
        twitterLoginButton = findViewById(R.id.btnTwitter);
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

        facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginMethod="Facebook";
                facebookLogin();
                }
        }
        );

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);

            }
        });
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
        mGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("Google Login");
                builder.setMessage("Google Auth will be able soon.");
                builder.setPositiveButton("OK", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



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
        facebookLoginButton.setReadPermissions("email", "public_profile");
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
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

            if(this.loginMethod.equalsIgnoreCase("Facebook")){
                // Pass the activity result back to the Facebook SDK
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
            else{
                twitterLoginButton.onActivityResult(requestCode,resultCode,data);
            }
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
                            mProgress.setMessage("Iniciando Sesión via Facebook...");
                            mProgress.show();
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            myRefCU = myRef.child(user.getUid());
                            myRefCU.child("Name").setValue(user.getDisplayName());
                            myRefCU.child("Email").setValue(user.getEmail());
                            myRefCU.child("Cedula").setValue("Facebook User");
                            myRefCU.child("Profile Image").setValue(user.getPhotoUrl().toString());
                            qrCode(user);
                            mProgress.dismiss();
                            Intent afterLog = new Intent(Login.this,MainActivity.class);
                            startActivity(afterLog);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(Login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            if(task.getException().getMessage()
                                    .equalsIgnoreCase("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.")){
                                Toast.makeText(Login.this, "Email already registered.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                });
    }


    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);


        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token, session.getAuthToken().secret);

        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    myRefCU = myRef.child(user.getUid());
                    myRefCU.child("Name").setValue(user.getDisplayName());
                    myRefCU.child("Email").setValue(user.getEmail());
                    myRefCU.child("Cedula").setValue("Twitter User");
                    myRefCU.child("Profile Image").setValue(user.getPhotoUrl().toString());
                    qrCode(user);
                    Intent afterLog = new Intent(Login.this, MainActivity.class);
                    startActivity(afterLog);

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(Login.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();

                    if(task.getException().getMessage()
                            .equalsIgnoreCase("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.")){
                        Toast.makeText(Login.this, "Email already registered.",
                                Toast.LENGTH_SHORT).show();
                    }


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
                    Toast.makeText(Login.this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(Login.this,"Finished",Toast.LENGTH_SHORT).show();
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




