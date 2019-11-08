package com.uas.facite.adoptaunbache;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class ArcgisActivity extends Fragment {

    private MapView mMapView;
    GraphicsOverlay graphicsLayer = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //returning our layout file
        return inflater.inflate(R.layout.activity_arcgis, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMapView = getView().findViewById(R.id.mapViewArcgis);
        ArcGISMap map = new ArcGISMap(Basemap.Type.TOPOGRAPHIC, 24.8087148, -107.3941223, 13);
        mMapView.setMap(map);

        // create a graphics overlay
        graphicsLayer = new GraphicsOverlay();
        // add graphics overlay to the map view
        mMapView.getGraphicsOverlays().add(graphicsLayer);
        //CARGAR LOS PUNTOS DEL GeoJson de nuestra Api
        new CargarBaches().execute("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php");
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
            //convertir la respuesta del web service a un objeto JSON
            try {
                JSONObject lugares = new JSONObject(result);
                JSONArray feature_arr = lugares.getJSONArray("features");
                for (int i = 0; i < feature_arr.length(); i++) {
                    // Getting the coordinates and projecting them to map's spatial
                    // reference
                    JSONObject obj_geometry = feature_arr.getJSONObject(i)
                            .getJSONObject("geometry");

                    //Crear un punto a partir de las coordenadas del geojson
                    double lon = Double.parseDouble(obj_geometry.getJSONArray("coordinates")
                            .get(0).toString());
                    double lat = Double.parseDouble(obj_geometry.getJSONArray("coordinates")
                            .get(1).toString());
                    SpatialReference SPATIAL_REFERENCE = SpatialReferences.getWgs84();
                    Point bachepunto = new Point(lon, lat, SPATIAL_REFERENCE);
                    // create a red (0xFFFF0000) circle simple marker symbol
                    //Creamos el icono personalizado para nuestros marcadores (puntos)
                    BitmapDrawable BacheDrawable = (BitmapDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.problem32);
                    final PictureMarkerSymbol bacheSymbol = new PictureMarkerSymbol(BacheDrawable);
                    SimpleMarkerSymbol redCircleSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.DIAMOND, 0xFFFF0000, 10);

                    //obtener los demas campos del geojson
                    JSONObject obj_properties = feature_arr.getJSONObject(i)
                            .getJSONObject("properties");
                    Map<String, Object> attr = new HashMap<String, Object>();

                    String nombre = obj_properties.getString("nombre").toString();
                    attr.put("nombre", nombre);

                    // create graphics and add to graphics overlay
                    Graphic puntoGraphic = new Graphic(bachepunto, bacheSymbol);

                    //agregar el punto a la capa de puntos
                    graphicsLayer.getGraphics().add(puntoGraphic);
                }

                } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
