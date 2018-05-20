package bogobikes.app;

import android.app.ProgressDialog;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DataBaseManager {

    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef,myRefCU;
    private FirebaseStorage mStorage;
    private StorageReference mySRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;



    private DataBaseManager (FirebaseUser user){
        this.user = user;
        this.mDatabase = FirebaseDatabase.getInstance();
        this.mStorage = FirebaseStorage.getInstance();
        this.mySRef =mStorage.getReference();
        this.myRef = mDatabase.getReference().child("Users");

    }

    

}
