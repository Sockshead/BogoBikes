package bogobikes.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class ListParq extends Fragment {

    private Button unicentro;
    private Button portN1;
    private Button portN2;
    private MapFragment mapFragment;
    private FirebaseDatabase mDatabase;
    private DatabaseReference myRef;
    final ArrayList<LatLng> mlatLng = new ArrayList<>();
    final ArrayList<String> mlatLngS = new ArrayList<>();



    public ListParq() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_parq, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Parqueaderos");
        mDatabase = FirebaseDatabase.getInstance();
        myRef = mDatabase.getReference().child("Parking lots");
        mapFragment = new MapFragment(true);
        loadPark();
        unicentro = view.findViewById(R.id.btn_unicentro);
        portN1 = view.findViewById(R.id.btn_portal1);
        portN2 = view.findViewById(R.id.btn_portal2);


        unicentro.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                LatLng pos = encontrarMarc("Unicentro");
                mapFragment = new MapFragment(pos);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_container, mapFragment);
                fragmentTransaction.commit();

            }
        });

        portN1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                LatLng pos = encontrarMarc("Portal Norte");
                mapFragment = new MapFragment(pos);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_container, mapFragment);
                fragmentTransaction.commit();

            }
        });

        portN2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                LatLng pos = encontrarMarc("Portal Norte2");
                mapFragment = new MapFragment(pos);
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.frame_container, mapFragment);
                fragmentTransaction.commit();

            }
        });

        return view;
    }

    private void loadPark() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int number = Integer.parseInt(String.valueOf(dataSnapshot.child("Number").getValue()));
                final int finalNumber = number;
                for (int a = 0; a<finalNumber; a++) {
                    final int finalA = a;
                    double v, v1;
                    String nameP = String.valueOf(dataSnapshot.child("Coordinates").child("PAR" + (finalA +1)).child("Name").getValue());
                    v = Double.parseDouble(String.valueOf(dataSnapshot.child("Coordinates").child("PAR" + (finalA+1)).child("v").getValue()));
                    v1 = Double.parseDouble(String.valueOf(dataSnapshot.child("Coordinates").child("PAR" + (finalA+1)).child("v1").getValue()));
                    LatLng PAR = new LatLng(v,v1);
                    mlatLng.add(PAR);
                    mlatLngS.add(nameP);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public LatLng encontrarMarc(String str){
        LatLng pos=null;
        for(int i = 0; i < mlatLng.size();i++ ) {
            if (mlatLngS.get(i).equalsIgnoreCase(str)) {
                pos = mlatLng.get(i);
            }
        }
        return pos;
    }
}
