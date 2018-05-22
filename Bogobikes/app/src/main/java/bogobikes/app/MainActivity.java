package bogobikes.app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.SessionManager;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgress;
    private ImageView mProfImg;
    private TextView mPname, mPEmail;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myDBRef;
    private StorageReference mySRef;
    private int currentMenuItem;
    private Fragment fragmentCurrent;

    private MapFragment mapFragment = new MapFragment();
    private ProfileFragment profileFragment = new ProfileFragment();
    private QRcodeFragment qrCodeFragment = new QRcodeFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mStorage = FirebaseStorage.getInstance();
        mySRef = mStorage.getReference();
        mDatabase = FirebaseDatabase.getInstance();
        myDBRef = mDatabase.getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        mProgress = new ProgressDialog(MainActivity.this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        currentMenuItem = R.id.nav_Mapa;
        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            addFragment(mapFragment);
        }

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
                            if (imgURL.equalsIgnoreCase("Default") == false) {
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
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (fragmentCurrent.equals(mapFragment)) {
                //getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                finish();
            } else {
                getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                replaceFragment(mapFragment);
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        fragmentCurrent = fragment;
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, fragment)
                .addToBackStack(null).commit();
    }

    private void addFragment(Fragment fragment) {
        fragmentCurrent = fragment;
        getSupportFragmentManager().beginTransaction().add(R.id.frame_container, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragmentSelected = null;
        int id = item.getItemId();

        if (id == currentMenuItem) {
            drawer.closeDrawer(GravityCompat.START);
            return false;
        }
        if (id == R.id.nav_Mapa) {
            fragmentSelected = mapFragment;
        } else if (id == R.id.nav_prof) {
            fragmentSelected = profileFragment;
        } else if (id == R.id.nav_parq) {

        } else if (id == R.id.nav_qrcode) {
            fragmentSelected = qrCodeFragment;
        } else if (id == R.id.nav_signOff) {
            mProgress.setMessage("Cerrando Sesión...");
            mProgress.show();
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            SessionManager<TwitterSession> sessionManager = TwitterCore.getInstance().getSessionManager();
            sessionManager.clearActiveSession();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragmentSelected).commit();
            Intent logIn = new Intent(MainActivity.this, Login.class);
            startActivity(logIn);
            mProgress.dismiss();
        }
        /*if (fragmentSelected != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.frame_container, fragmentSelected).commit();
        }*/

        currentMenuItem = id;
        replaceFragment(fragmentSelected);

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}