package com.uas.facite.adoptaunbache;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class TransporteActivity extends Fragment {

    private ListView lista_rutas;

    // URL para obtener los usuarios registrados
    private static String url = "http://facite.uas.edu.mx/transportes/api/get_rutas.php";
    ArrayList<HashMap<String, String>> rutasList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Cargar el archivo del layout en el fragmento
        return inflater.inflate(R.layout.activity_transporte, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //realizar cualquier accion despues de que se encuentre cargado el fragmento
        rutasList = new ArrayList<>();
        lista_rutas = (ListView)getView().findViewById(R.id.listaRutas);
        new TransporteActivity.CargarRutas().execute();

        lista_rutas.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
            {
                //sacar el id de la ruta y mandarlo al nuevo fragment
                String ruta_id = ((TextView) arg1.findViewById(R.id.listcodigoruta)).getText().toString();
                Bundle bundle = new Bundle();
                bundle.putString("ruta_id",ruta_id); // Put anything what you want
                //abrir el fragment del mapa con las rutas
                MapBoxActivity mapFrag = new MapBoxActivity();
                mapFrag.setArguments(bundle);

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, mapFrag, null)
                        .addToBackStack(null)
                        .commit();

                //Intent intent=new Intent(getActivity(), DisplayDetails.class);
                //intent.putExtra("text", historyListView.getItemAtPosition((int) l).toString());
                //startActivity(intent);
                /*Snackbar.make(view, "Ruta id:" + ruta_id, Snackbar.LENGTH_LONG)
                        .show();*/
            }
        });
    }



    private class CargarRutas extends AsyncTask<Void, Void, String> {

        SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //cargar progressbar
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#B87624"));
            pDialog.setTitleText("Cargando rutas...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            //creating request handler object
            RequestHandler requestHandler = new RequestHandler();
            //creating request parameters
            HashMap<String, String> params = new HashMap<>();
            //returing the response
            return requestHandler.sendPostRequest(url, params);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            try {
                //converting response to json object
                JSONObject jsonObj = new JSONObject(s);

                // Getting JSON Array node
                JSONArray rutas = jsonObj.getJSONArray("rutas");

                // looping through All Usuarios
                for (int i = 0; i < rutas.length(); i++) {
                    JSONObject c = rutas.getJSONObject(i);
                    String cod = c.getString("id_ruta");
                    String nom = c.getString("nombre");

                    // tmp hash map for single contact
                    HashMap<String, String> ruta = new HashMap<>();
                    // adding each child node to HashMap key => value
                    ruta.put("id_ruta", cod);
                    ruta.put("nombre", nom);
                    // adding usuario to rutas list
                    rutasList.add(ruta);
                }

                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        getContext(), rutasList,
                        R.layout.listarutas_item, new String[]{"id_ruta", "nombre"}, new int[]{R.id.listcodigoruta,
                        R.id.listnombreruta});

                lista_rutas.setAdapter(adapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
