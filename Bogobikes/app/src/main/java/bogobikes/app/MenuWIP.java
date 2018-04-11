package bogobikes.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuWIP extends AppCompatActivity {

    private Button perfil,mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_wip);
        perfil = findViewById(R.id.btnProf);
        mapa = findViewById(R.id.btnMap);

        perfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Prof = new Intent(MenuWIP.this,Perfil.class);
                startActivity(Prof);
            }
        });
        mapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent Prof = new Intent(MenuWIP.this,mapaParq.class);
                startActivity(Prof);
            }
        });
    }
}
