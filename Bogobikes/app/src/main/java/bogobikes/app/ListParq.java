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

import java.util.Map;


public class ListParq extends Fragment {

    MapFragment mapFragment = new MapFragment();
    private Button unicentro;
    private Button portN1;
    private Button portN2;

    public ListParq() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_list_parq, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Parqueaderos");

        unicentro = view.findViewById(R.id.btn_unicentro);
        portN1 = view.findViewById(R.id.btn_portal1);
        portN2 = view.findViewById(R.id.btn_portal2);

        unicentro.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mapFragment.encontrarMarc("Unicentro");

                MapFragment posMap = mapFragment;

                fragmentTransaction.replace(R.id.frame_container, posMap);
                fragmentTransaction.commit();
            }
        });

        portN1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){

            }
        });

        portN2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){

            }
        });

        return view;
    }
}
