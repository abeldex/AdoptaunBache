package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.net.URI;
import java.net.URISyntaxException;


public class MapBoxActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private MapView mapa;
    private MapboxMap mapboxMap;

    private NavigationView navegacion;
    private ImageButton botonMenu;

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key = getString(R.string.MapboxKey);
        //creamos una instancia de mapbox
        Mapbox.getInstance(this,key);
        setContentView(R.layout.activity_map_box);

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

        //identificamos el visor de nuestro diseño
        mapa = findViewById(R.id.mapViewMapBox);
        mapa.onCreate(savedInstanceState);

        mapa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        //CARGAR LOS PUNTOS DEL GeoJson de nuestra Api
                        try {
                            style.addSource(new GeoJsonSource("GEOJSON_PUNTOS",
                                    new URI("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php")));
                        } catch (URISyntaxException e) {
                           Log.i("ERROR GEOJSON:", e.toString());
                        }
                        //Creamos el icono personalizado para nuestros marcadores (puntos)
                        Bitmap icono = BitmapFactory.decodeResource(getResources(), R.drawable.alarm);
                        //agregar el icono al estilo del mapa
                        style.addImage("BACHE_ICONO", icono);
                        //Crear una capa layer con los datos cargados desde geojson
                        SymbolLayer BachesCapa = new SymbolLayer("BACHES", "GEOJSON_PUNTOS");
                        //Asignamos el icono personalizado a la capa de baches
                        BachesCapa.setProperties(PropertyFactory.iconImage("BACHE_ICONO"));
                        //Asignamos la capa de baches al mapa
                        style.addLayer(BachesCapa);
                    }
                });

                //llevar a la posicion de culiacan
                CameraPosition posicion = new CameraPosition.Builder()
                        .target(new LatLng(24.8087148, -107.3941223)) //estalece la posicion
                        .zoom(10) //establecer el zoom
                        //.bearing(180) //rota la camara
                        .tilt(80) //angulo de inclinacion
                        .build();

                //mover la posicion del mapa
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(posicion), 5000);


            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapa.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapa.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapa.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapa.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapa.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapa.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapa.onSaveInstanceState(outState);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch(id)
        {
            case R.id.nav_usuarios:
                //mandamos la alerta bonita con el error
                new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Navegacion")
                        .setContentText("Activity para usuarios")
                        .show();

                break;
            case R.id.nav_MapBox:
                new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Navegacion")
                        .setContentText("Activity para el mapa de baches")
                        .show();
                break;

            case R.id.nav_google:
                //abrir la ventana de google maps
                Intent intent = new Intent(MapBoxActivity.this, MapsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_config:
                new SweetAlertDialog(MapBoxActivity.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("Navegacion")
                        .setContentText("Activity para la configuracion")
                        .show();
                break;
            default:
                return true;
        }


        return true;
    }
}
