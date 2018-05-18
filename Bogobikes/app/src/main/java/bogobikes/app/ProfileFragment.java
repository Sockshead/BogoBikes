package bogobikes.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import static android.app.Activity.RESULT_OK;
import static com.facebook.login.widget.ProfilePictureView.TAG;

public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    TextView name, cedula, email;
    ImageView imgProf;
    private int CAMERA_REQUEST_CODE = 0;
    private ProgressDialog mProgressDialog;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myDBRef;
    private StorageReference mySRef;
    /*public ProfileFragment() {
        // Required empty public constructor
    }
    /*@Override
    protected void onStart() {
       // super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Perfil");

        //setContentView(R.layout.activity_perfil);
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mySRef = mStorage.getReference();
        mDatabase = FirebaseDatabase.getInstance();
        myDBRef = mDatabase.getReference().child("Users");
        name = view.findViewById(R.id.line);
        cedula = view.findViewById(R.id.line2);
        email = view.findViewById(R.id.line3);
        imgProf = view.findViewById(R.id.imgProf);
        mProgressDialog = new ProgressDialog(getActivity());



        imgProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(Intent.createChooser(intent, "Seleccione una imagen de perfil"), CAMERA_REQUEST_CODE);
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState){
        Log.w(TAG, "Choko marik2");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    Log.w(TAG, "Choko marik");
                    myDBRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mProgressDialog.setMessage("Cargando Perfil");
                            mProgressDialog.show();
                            name.setTextColor(0);
                            name.setText(String.valueOf(dataSnapshot.child("Name").getValue()));
                            cedula.setText(String.valueOf(dataSnapshot.child("Cedula").getValue()));
                            email.setText(String.valueOf(dataSnapshot.child("Email").getValue()));
                            String imgURL = String.valueOf(dataSnapshot.child("Profile Image").getValue());
                            if (URLUtil.isValidUrl(imgURL)) {
                                Picasso.with(getActivity().getApplicationContext()).load(Uri.parse(imgURL)).fit().centerCrop().into(imgProf);
                            } else {
                                System.out.println("URL no valida");
                            }
                            mProgressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                } else {
                    startActivity(new Intent(getActivity(), Login.class));
                    getActivity().finish();
                }
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            if (mAuth.getCurrentUser() == null)
                return;
            mProgressDialog.setMessage("Subiendo Imagen");
            mProgressDialog.show();
            final Uri uri = data.getData();
            if (uri == null) {
                mProgressDialog.dismiss();
                return;
            }
            if (mySRef == null) {
                mySRef = mStorage.getReference();
            }
            if (myDBRef == null) {
                myDBRef = mDatabase.getReference().child("Users");
            }

            final StorageReference filePath = mySRef.child("Photos").child(getRandomString());
            final DatabaseReference currentUserDB = myDBRef.child(mAuth.getCurrentUser().getUid());
            currentUserDB.child("Profile Image").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String image = dataSnapshot.getValue().toString();
                    if (!image.equals("Default") && !image.isEmpty()) {
                        Task<Void> task = FirebaseStorage.getInstance().getReferenceFromUrl(image).delete();
                        task.addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Imagen Borrada correctamente", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Error al eliminar Imagen", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    currentUserDB.child("Profile Image").removeEventListener(this);
                    filePath.putFile(uri).addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mProgressDialog.dismiss();
                            Uri downloadUri = taskSnapshot.getDownloadUrl();
                            Toast.makeText(getActivity(), "Finished", Toast.LENGTH_SHORT).show();
                            Picasso.with(getActivity().getApplicationContext()).load(uri).fit().centerCrop().into(imgProf);
                            DatabaseReference currentUserDB = myDBRef.child(mAuth.getCurrentUser().getUid());
                            currentUserDB.child("Profile Image").setValue(downloadUri.toString());
                        }
                    }).addOnFailureListener(getActivity(), new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mProgressDialog.dismiss();
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();

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
