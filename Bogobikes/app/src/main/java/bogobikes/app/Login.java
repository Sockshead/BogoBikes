package bogobikes.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private EditText mUser, mPassword;
    private Button mLogin,mRegister;
    private ProgressDialog mProgress;
    private String TAG = "Login";

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
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private void Login() {
        String email = mUser.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        //Check if fields are empty.
        if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)) {
            mProgress.setMessage("Iniciando Sesión");
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


}




