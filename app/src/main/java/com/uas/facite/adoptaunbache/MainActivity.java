package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private NavigationView navegacion;
    private ImageButton botonMenu;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //identificamos el drawer layout
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        //identificamos el diseño del menu lateral
        navegacion = (NavigationView)findViewById(R.id.nav_view);
        navegacion.setNavigationItemSelectedListener(this);
        //identificamos el boton del menu
        botonMenu = (ImageButton)findViewById(R.id.botonMenu);
        //aplicamos el evento del botonmenu para abrir el drawer
        botonMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drawer.isDrawerOpen(Gravity.LEFT))
                    drawer.closeDrawer(Gravity.LEFT);
                else
                    drawer.openDrawer(Gravity.LEFT);
            }
        });

        //Fragmento inicial de la aplicacion
        setFragment(3);
    }

    public void setFragment(int position) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        switch (position) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MapsActivity GoogleMapsFragment = new MapsActivity();
                fragmentTransaction.replace(R.id.fragment, GoogleMapsFragment);
                fragmentTransaction.commit();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case 1:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                OSMActivity OSMFragment = new OSMActivity();
                fragmentTransaction.replace(R.id.fragment, OSMFragment);
                fragmentTransaction.commit();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case 2:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                ArcgisActivity ArcGisFragment = new ArcgisActivity();
                fragmentTransaction.replace(R.id.fragment, ArcGisFragment);
                fragmentTransaction.commit();
                drawer.closeDrawer(Gravity.LEFT);
                break;
            case 3:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MapBoxActivity MapBoxFragment = new MapBoxActivity();
                fragmentTransaction.replace(R.id.fragment, MapBoxFragment);
                fragmentTransaction.commit();
                drawer.closeDrawer(Gravity.LEFT);
                break;

        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch(id)
        {
            case R.id.nav_usuarios:
                //mandamos la alerta bonita con el error
                new SweetAlertDialog(MainActivity.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Navegacion")
                        .setContentText("Activity para usuarios")
                        .show();

                break;
            case R.id.nav_osm:
                //abrir la ventana de google maps
                //Intent intentosm = new Intent(MainActivity.this, OSMActivity.class);
                //startActivity(intentosm);
                setFragment(1);
                break;
            case R.id.nav_MapBox:
                //abrir la ventana de google maps
                //Intent intentmb = new Intent(MainActivity.this, MapBoxActivity.class);
                //startActivity(intentmb);
                setFragment(3);
                break;

            case R.id.nav_google:
                //abrir la ventana de google maps
                //Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                //startActivity(intent);
                setFragment(0);
                break;
            case R.id.nav_arcgis:
                //abrir la ventana de google maps
                //Intent intentarcgis = new Intent(MainActivity.this, ArcgisActivity.class);
                //startActivity(intentarcgis);
                setFragment(2);
                break;
            default:
                return true;
        }

        return true;
    }
}

