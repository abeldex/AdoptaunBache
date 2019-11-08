package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


public class MapBoxActivity extends Fragment {

    private MapView mapa;
    private MapboxMap mapboxMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        //change R.layout.yourlayoutfilename for each of your fragments
        String key = getString(R.string.MapboxKey);
        //creamos una instancia de mapbox
        Mapbox.getInstance(getActivity(),key);
        return inflater.inflate(R.layout.activity_map_box, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //identificamos el visor de nuestro diseño
        mapa = getView().findViewById(R.id.mapViewMapBox);
        mapa.onCreate(savedInstanceState);

        mapa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
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
                        Bitmap icono = BitmapFactory.decodeResource(getResources(), R.drawable.problem32);
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
                        .zoom(12) //establecer el zoom
                        //.bearing(180) //rota la camara
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
    public void onDestroy() {
        super.onDestroy();
        mapa.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapa.onSaveInstanceState(outState);
    }

}
