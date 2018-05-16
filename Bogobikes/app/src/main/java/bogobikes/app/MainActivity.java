package bogobikes.app;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgress;
    private ImageView mProfImg;
    private TextView mPname,mPEmail;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myDBRef;
    private StorageReference mySRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStorage = FirebaseStorage.getInstance();
        mySRef =mStorage.getReference();
        mDatabase = FirebaseDatabase.getInstance();
        myDBRef = mDatabase.getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(MainActivity.this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View hView = navigationView.getHeaderView(0);
        mPEmail = hView.findViewById(R.id.pEmail);
        mProfImg = hView.findViewById(R.id.imgProfM);
        mPname = hView.findViewById(R.id.pName);
        this.loadUserData();


        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
        mAuth.addAuthStateListener(mAuthListener);
    }
    private void loadUserData() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() != null) {
                    myDBRef.child(firebaseAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            mPname.setText(String.valueOf(dataSnapshot.child("Name").getValue()));
                            mPEmail.setText(String.valueOf(dataSnapshot.child("Email").getValue()));
                            String imgURL = String.valueOf(dataSnapshot.child("Profile Image").getValue());
                            if(imgURL.equalsIgnoreCase("Default")==false) {
                                if (URLUtil.isValidUrl(imgURL)) {
                                    Picasso.with(MainActivity.this).load(Uri.parse(imgURL)).fit().centerCrop().into(mProfImg);
                                } else {
                                    Toast.makeText(MainActivity.this, "unable to load profile image.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finish();
                }

            }
        };
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        if (id == R.id.nav_prof) {
            fragment = new ProfileFragment();
            /*Intent Prof = new Intent(MainActivity.this,Perfil.class);
            startActivity(Prof);*/
        } else if (id == R.id.nav_parq) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_signOff) {
            mProgress.setMessage("Cerrando Sesión...");
            mProgress.show();
            mAuth.signOut();
            Intent logIn = new Intent(MainActivity.this,Login.class);
            startActivity(logIn);
            mProgress.dismiss();
        }
        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragment).commit();

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}