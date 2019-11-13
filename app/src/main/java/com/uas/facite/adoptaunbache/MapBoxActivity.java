package com.uas.facite.adoptaunbache;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.match;
import static com.mapbox.mapboxsdk.style.expressions.Expression.rgb;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


public class MapBoxActivity extends Fragment implements MapboxMap.OnMapClickListener {
    //variables para el visor de mapbox
    private MapView mapa;
    private MapboxMap mapboxMap;
    private HashMap<String, View> viewMap;


    //variables para agregar los marcadores al mapa
    private FloatingActionButton selectLocationButton;
    private static final String DROPPED_MARKER_LAYER_ID = "DROPPED_MARKER_LAYER_ID";
    private ImageView pinMarker;
    private Layer droppedMarkerLayer;

    //variable para el diseño de la direccion del punto
    BottomSheetBehavior sheetBehavior;
    LinearLayout layoutBottomSheet;

    TextView txtdir, txtlat, txtlang;
    Button BtnAdoptar;
    ImageButton BtnImagen;
    ImageView ImagenCamara;
    CardView cardImagen;

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 507;

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

        txtdir = getView().findViewById(R.id.txtCalle);
        txtlat = getView().findViewById(R.id.txtLat);
        txtlang = getView().findViewById(R.id.txtLong);
        ImagenCamara = getView().findViewById(R.id.imgBache);
        cardImagen = getView().findViewById(R.id.roundCardView);

        layoutBottomSheet = getView().findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);

        sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        //realizar acciones cuando se abra
                        //btnBottomSheet.setText("Close Sheet");

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        //realizar acciones cuando se cierre
                        //btnBottomSheet.setText("Expand Sheet");
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        //boton Adoptar click
        BtnAdoptar = getView().findViewById(R.id.boton_Adoptar);
        BtnAdoptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //convertimos a base64 la imagen
                //encode image to base64 string
                Bitmap bitmap = ((BitmapDrawable)ImagenCamara.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                //agregamos el punto a la base de datos
                RegistroBache registroObject = new RegistroBache(txtdir.getText().toString(), txtlang.getText().toString(),txtlat.getText().toString(),imageString);
                registroObject.execute();
                //cerramos el bottom Shet
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //limpiamos los elementos del bottom shet
                txtlat.setText("");
                txtlang.setText("");
                txtdir.setText("");
                //escondemos el boton de la camara y mostramos la imagen
                BtnImagen.setVisibility(View.VISIBLE);
                cardImagen.setVisibility(View.GONE);
            }
        });

        //Boton imagen click
        BtnImagen = getView().findViewById(R.id.boton_Imagen);
        BtnImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionarImagen();
            }
        });

        //identificamos el visor de nuestro diseño
        mapa = getView().findViewById(R.id.mapViewMapBox);
        mapa.onCreate(savedInstanceState);
        //obtener los datos cachados si es que se enviaron
        Bundle bundle = this.getArguments();

        mapa.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                //referencia hacia el mapa
                MapBoxActivity.this.mapboxMap = mapboxMap;
                SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);
                //Mostrar las indicaciones para agregar un marcador nuevo
                /*new SweetAlertDialog(getActivity(), SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                        .setTitleText("Instrucciones para Adoptar un Bache...")
                        .setContentText("Para adoptar un bache posiciona el marcador en la ubicacion del mapa y presiona el boton de la parte inferior derecha")
                        .setCustomImage(R.drawable.problem32)
                        .show();
                */


                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        mapboxMap.addOnMapClickListener(MapBoxActivity.this::onMapClick);
                        //CARGAR LOS PUNTOS DEL GeoJson de nuestra Api
                        try {
                            //cargar progressbar
                            pDialog.getProgressHelper().setBarColor(Color.parseColor("#B87624"));
                            pDialog.setTitleText("Cargando Baches...");
                            pDialog.setCancelable(false);
                            pDialog.show();
                            style.addSource(new GeoJsonSource("GEOJSON_PUNTOS",
                                    new URI("http://facite.uas.edu.mx/adoptaunbache/api/getlugares.php")));

                            if(bundle != null){
                                // handle your code here.
                                //cargar la ruta
                                style.addSource(new GeoJsonSource("GEOJSON_RUTA_BARRA",
                                        new URI("http://facite.uas.edu.mx/transportes/api/get_ruta.php?id=" + bundle.get("ruta_id"))));

                                // The layer properties for our line. This is where we make the line dotted, set the color, etc.
                                style.addLayer(new LineLayer("layerruta", "GEOJSON_RUTA_BARRA").withProperties(
                                        PropertyFactory.lineColor(
                                                match(get("color"), rgb(35, 152, 239),
                                                        stop("red", rgb(247, 69, 93)),
                                                        stop("blue", rgb(51, 201, 235)))),
                                        PropertyFactory.visibility(Property.VISIBLE),
                                        PropertyFactory.lineWidth(3f)
                                ));

                            }

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
                        BachesCapa.setProperties(iconImage("BACHE_ICONO"));
                        //Asignamos la capa de baches al mapa
                        style.addLayer(BachesCapa);
                        //Agregar capa de la ruta

                        //quitar la barra
                        pDialog.dismiss();
                        //imagen para el pin que nos servira para agregaremos un nuevo punto al mapa
                        pinMarker = new ImageView(getContext());
                        pinMarker.setImageResource(R.drawable.ic_pinwarning);
                        //agregamos el pin al centro del mapa
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                        pinMarker.setLayoutParams(params);
                        mapa.addView(pinMarker);

                        // identificar el boton para agregar el marcador.
                        selectLocationButton = getView().findViewById(R.id.boton_agregar_mapbox);
                        selectLocationButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                obtenerDireccion();
                                final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;
                                //Mostramos el bottom Shet
                                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                txtlat.setText("Latitud: " + mapTargetLatLng.getLongitude());
                                txtlang.setText("Longitud:" + mapTargetLatLng.getLatitude());

                                //Mandamos un mensaje con la posicion de las coordenadas selecionadas
                                /*new SweetAlertDialog(getActivity(), SweetAlertDialog.NORMAL_TYPE)
                                        .setTitleText("¿Adoptar un bache en la posicion?")
                                        .setContentText("Latitud: " + mapTargetLatLng.getLongitude() + " Longitud:" + mapTargetLatLng.getLatitude())
                                        .setConfirmText("Si Adoptar!")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {

                                                sDialog
                                                        .setTitleText("Excelente!")
                                                        .setContentText("Acabas de adoptar un bache!")
                                                        .setConfirmText("Yei")
                                                        .setConfirmClickListener(null)
                                                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                //abrir el bottom sheet

                                            }
                                        })
                                        .show();*/


                            }
                        });
                    }
                });

                //llevar a la posicion de culiacan
                CameraPosition posicion = new CameraPosition.Builder()
                        .target(new LatLng(24.8087148, -107.3941223)) //estalece la posicion
                        .zoom(12) //establecer el zoom
                        //.bearing(180) //rota la camara
                        .build();

                //mover la posicion del mapa
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(posicion), 2000);

            }
        });
    }

    //solicitar permisos



    //metodo para guardar una imagen de la camara
    private File savebitmap(Bitmap bmp) {
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        OutputStream outStream = null;
        // String temp = null;
        File file = new File(extStorageDirectory, "temp.png");
        if (file.exists()) {
            file.delete();
            file = new File(extStorageDirectory, "temp.png");

        }

        try {
            outStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return file;
    }

    //metodo para seleccionar una imagen
    public void seleccionarImagen()
    {

        final CharSequence[] options = { "Tomar Fotografia", "Elegir de la Galeria","Cancelar" };
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Agregar Foto del Bache!");
        builder.setItems(options,new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                if(options[which].equals("Tomar Fotografia"))
                {
                    //solicitar permisos para la camara en caso de que no lo tenga
                    //Se realiza la petición de permisos para dispositivos con OS >= 6.0
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            // Se tiene permiso
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, 1);
                        }else{
                            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                            return;
                        }
                    }else{

                        // No se necesita requerir permiso, OS menor a 6.0.
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, 1);
                    }


                }
                else if(options[which].equals("Elegir de la Galeria"))
                {
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        // Se tiene permiso
                        Intent intent=new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, 2);
                    }else{
                        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);
                        return;
                    }

                }
                else if(options[which].equals("Cancelar"))
                {
                    dialog.dismiss();
                }

            }
        });
        builder.show();
    }

    public void onActivityResult(int requestcode,int resultcode,Intent intent)
    {
        super.onActivityResult(requestcode, resultcode, intent);
        if(resultcode==getActivity().RESULT_OK)
        {
            if(requestcode==1)
            {
                Bitmap photo = (Bitmap)intent.getExtras().get("data");
                Drawable drawable=new BitmapDrawable(photo);
                ImagenCamara.setImageDrawable(drawable);
                //escondemos el boton de la camara y mostramos la imagen
                BtnImagen.setVisibility(View.GONE);
                cardImagen.setVisibility(View.VISIBLE);


            }
            else if(requestcode==2)
            {
                Uri selectedImage = intent.getData();
                String[] filePath = { MediaStore.Images.Media.DATA };
                Cursor c = getActivity().getApplicationContext().getContentResolver().query(selectedImage,filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Drawable drawable=new BitmapDrawable(thumbnail);
                ImagenCamara.setImageDrawable(drawable);
                //escondemos el boton de la camara y mostramos la imagen
                BtnImagen.setVisibility(View.GONE);
                cardImagen.setVisibility(View.VISIBLE);
            }
        }
    }

    //Metodo para obtener la direccion a partir de un punto en el mapa LatLng
    public void obtenerDireccion() {
        try {
            // Obtenemos las coordenadas de la camara en el mapa
            final LatLng mapTargetLatLng = mapboxMap.getCameraPosition().target;
            final Point point = Point.fromLngLat(mapTargetLatLng.getLongitude(), mapTargetLatLng.getLatitude());

            MapboxGeocoding client = MapboxGeocoding.builder()
                    .accessToken("pk.eyJ1IjoiYWJlbGRleCIsImEiOiJjajEydmd0d3IwMDV2MndwMGVzeTNmYWVnIn0.AjKpr4EPMxx5KcL-zqJUdQ")
                    .query(Point.fromLngLat(point.longitude(), point.latitude()))
                    .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
                    .build();

            client.enqueueCall(new Callback<GeocodingResponse>() {
                @Override
                public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                    if (response.body() != null) {
                        List<CarmenFeature> results = response.body().features();
                        if (results.size() > 0) {
                            CarmenFeature feature = results.get(0);

                            // If the geocoder returns a result, we take the first in the list and show a Toast with the place name.
                            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                                @Override
                                public void onStyleLoaded(@NonNull Style style) {
                                    //Snackbar.make(getView(), "Direccion: " + feature.placeName(), Snackbar.LENGTH_LONG)
                                      //      .show();

                                    txtdir.setText(feature.placeName());
                                    //PLACE_DIR = feature.placeName();
                                    /*if (style.getLayer(DROPPED_MARKER_LAYER_ID) != null) {

                                        //txtbuscar.setText(feature.placeName());
                                        //mostrar el pin de nuevo
                                        //hoveringMarker.setVisibility(View.VISIBLE);
                                    }*/

                            }
                            });

                        } else {
                        //    Snackbar.make(getView(), "Sin resultados ", Snackbar.LENGTH_LONG)
                        //.show();
                            //PLACE_DIR = "Sin resultados";
                            txtdir.setText("Sin resultados");
                        }
                    }
                }

                @Override
                public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                    Log.i("Geocoding Failure: %s", throwable.getMessage());
                }
            });
            //return PLACE_DIR;
        } catch (ServicesException servicesException) {
            Log.i("Error geocoding: %s", servicesException.toString());
            servicesException.printStackTrace();
            //return servicesException.toString();
        }
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

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        //Detectamos si se hizo click en el marcador para mostrar su informacion
        PointF screenPoint = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(screenPoint, "BACHES");
        if (!features.isEmpty()) {
            Feature selectedFeature = features.get(0);
            String title = selectedFeature.getStringProperty("nombre");
            //Toast.makeText(context, "You selected " + title, Toast.LENGTH_SHORT).show();
            Snackbar.make(getView(), "Bache: " + title, Snackbar.LENGTH_LONG)
            .show();

            //mostrar el info_bache layout
            View view = getLayoutInflater().inflate(R.layout.info_bache, null);
            TextView titleTv = (TextView)view.findViewById(R.id.title);
            titleTv.setText(title);

            String cod = selectedFeature.getStringProperty("cod_lugar");
            TextView codTv = (TextView) view.findViewById(R.id.style);
            codTv.setText(cod);

            ImageView imageView = (ImageView) view.findViewById(R.id.MarcadorImagen);
            String imagenBache = selectedFeature.getStringProperty("imagen");
            if(imagenBache != ""){
                //decode base64 string to image
                byte[] imageBytes = Base64.decode(imagenBache, Base64.DEFAULT);
                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                imageView.setImageBitmap(decodedImage);
            }

            Bitmap bitmap = SymbolGenerator.generate(view);

            //poner la imagen en el marcador
            Style style = mapboxMap.getStyle();

            style.addImage("img"+cod, bitmap);

            SymbolLayer calloutLayer = new SymbolLayer(cod, "GEOJSON_PUNTOS");
            //Asignamos el icono personalizado a la capa de baches
            calloutLayer.setProperties(iconImage("img"+cod));
            calloutLayer.setProperties(iconAnchor(Property.ICON_ANCHOR_BOTTOM_LEFT));
            calloutLayer.setProperties(iconOffset(new Float[] {-20.0f, -10.0f}));
            //Asignamos la capa de baches al mapa
            style.addLayer(calloutLayer);

        }

        return true;
    }

    //CLASE PARA HACER LA PETICION AL WEB SERVICE EN SEGUNDO PLANO
    class RegistroBache extends AsyncTask<Void, Void, String> {
        String nombre, lat, lang, img;
        SweetAlertDialog pDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.PROGRESS_TYPE);
        //Constructor de la clase Usuario REgistro
        RegistroBache(String nombre, String lat, String lang, String img){
            this.nombre = nombre;
            this.lat = lat;
            this.lang = lang;
            this.img = img;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText("Adoptando Bache...");
            pDialog.setCancelable(false);
            pDialog.show();

        }
        @Override
        protected String doInBackground(Void... voids) {
            //CREAMOS UN OBJETO DE LA CLASE RequestHandler
            RequestHandler requestHandler = new RequestHandler();
            //Crear los parametros que se enviaran al web service
            HashMap<String, String> parametros = new HashMap<>();
            parametros.put("nombre", nombre);
            parametros.put("lat", lat);
            parametros.put("lon", lang);
            parametros.put("img", img);
            //retornamos la respuesta del web service
            return requestHandler.sendPostRequest("http://facite.uas.edu.mx/adoptaunbache/api/insertar_bache.php", parametros);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            pDialog.dismiss();
            //convertir la respuesta del web service a un objeto JSON
            try {
                JSONObject obj = new JSONObject(s);
                if (obj.getInt("status" ) == 1){
                    //mandamos la alerta bonita
                    new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Excelente")
                            .setContentText(obj.getString("message"))
                            .show();
                }
                else
                {
                    //mandamos la alerta bonita con el error
                    new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Ups!")
                            .setContentText(obj.getString("message"))
                            .show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Utility class to generate Bitmaps for Symbol.
     * <p>
     * Bitmaps can be added to the map with
     * </p>
     */
    private static class SymbolGenerator {

        /**
         * Generate a Bitmap from an Android SDK View.
         *
         * @param view the View to be drawn to a Bitmap
         * @return the generated bitmap
         */
        static Bitmap generate(@NonNull View view) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(measureSpec, measureSpec);

            int measuredWidth = view.getMeasuredWidth();
            int measuredHeight = view.getMeasuredHeight();

            view.layout(0, 0, measuredWidth, measuredHeight);
            Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
    }

}
