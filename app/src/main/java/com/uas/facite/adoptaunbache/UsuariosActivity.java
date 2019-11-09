package com.uas.facite.adoptaunbache;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class UsuariosActivity extends Fragment {

    private ListView lista_usuarios;

    // URL para obtener los usuarios registrados
    private static String url = "http://facite.uas.edu.mx/adoptaunbache/api/listar_usuarios.php";
    ArrayList<HashMap<String, String>> usuariosList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Cargar el archivo del layout en el fragmento
        return inflater.inflate(R.layout.activity_usuarios, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //realizar cualquier accion despues de que se encuentre cargado el fragmento
        usuariosList = new ArrayList<>();
        lista_usuarios = (ListView)getView().findViewById(R.id.listaUsuarios);
        new CargarUsuarios().execute();
    }

    private class CargarUsuarios extends AsyncTask<Void, Void, String> {

        SweetAlertDialog pDialog = new SweetAlertDialog(getActivity(), SweetAlertDialog.PROGRESS_TYPE);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //cargar progressbar
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#B87624"));
            pDialog.setTitleText("Cargando Usuarios Registrados...");
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
                JSONArray usuarios = jsonObj.getJSONArray("usuarios");

                // looping through All Usuarios
                for (int i = 0; i < usuarios.length(); i++) {
                    JSONObject c = usuarios.getJSONObject(i);
                    String cod = c.getString("codigo");
                    String nom = c.getString("nombre");
                    String usu = c.getString("usuario");
                    String pas = c.getString("password");

                    // tmp hash map for single contact
                    HashMap<String, String> usuario = new HashMap<>();
                    // adding each child node to HashMap key => value
                    usuario.put("codigo", cod);
                    usuario.put("nombre", nom);
                    usuario.put("usuario", usu);
                    usuario.put("password", pas);
                    // adding usuario to usuarios list
                    usuariosList.add(usuario);
                }

                /**
                 * Updating parsed JSON data into ListView
                 * */
                ListAdapter adapter = new SimpleAdapter(
                        getContext(), usuariosList,
                        R.layout.listausuarios_item, new String[]{"codigo", "nombre",
                        "usuario"}, new int[]{R.id.listcodigo,
                        R.id.listnombre, R.id.listusuario});

                lista_usuarios.setAdapter(adapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

}
