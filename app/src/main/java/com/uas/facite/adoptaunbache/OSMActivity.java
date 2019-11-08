package com.uas.facite.adoptaunbache;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class OSMActivity extends Fragment {
    MapView map = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        return inflater.inflate(R.layout.activity_osm, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getActivity();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is
        map = (MapView)getView().findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //activar el zoom con multitouch
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        //mover a un punto especifico del mapa
        IMapController mapController = map.getController();
        //mapController.setZoom(14);
        GeoPoint startPoint = new GeoPoint(24.8206351, -107.380578);
        mapController.setCenter(startPoint);

        mapController.zoomTo(14);
        map.getController().animateTo(startPoint);
        //CARGAR LOS PUNTOS DEL GeoJson de nuestra Api
        new CargarBaches().execute("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php");
    }

    public void onResume(){
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private class CargarBaches extends AsyncTask<String, String, String> {
        SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
        protected void onPreExecute() {
            super.onPreExecute();
            //cargar progressbar
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#B87624"));
            pDialog.setTitleText("Cargando Baches...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... params) {
            //CREAMOS UN OBJETO DE LA CLASE RequestHandler
            RequestHandler requestHandler = new RequestHandler();
            //Crear los parametros que se enviaran al web service
            HashMap<String, String> parametros = new HashMap<>();
            //retornamos la respuesta del web service
            return requestHandler.sendPostRequest(params[0], parametros);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
                //quitar la barra
                pDialog.dismiss();
                //Creamos el icono personalizado para nuestros marcadores (puntos)
                //BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.problem32);
                Drawable defaultMarker = getResources().getDrawable(R.drawable.problem32);
                Bitmap defaultBitmap = ((BitmapDrawable)defaultMarker).getBitmap();
                Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);

                //Creamos la capa
                KmlDocument kmlDocument = new KmlDocument();
                kmlDocument.parseGeoJSON(result);
                FolderOverlay myOverLay = (FolderOverlay)kmlDocument.mKmlRoot.buildOverlay(map,defaultStyle,null,kmlDocument);
                map.getOverlays().add(myOverLay );
                map.invalidate();
        }
    }
}
