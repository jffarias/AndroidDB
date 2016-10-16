import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.i5.crudlista.addeditRecuerdame.AddEditRecuerdameActivity;
import com.example.i5.crudlista.data.Recuerdame;
import com.example.i5.crudlista.data.RecuerdameDbHelper;

public class MainActivity extends AppCompatActivity{
    String[] arreglo;
    ListView lvTareas;
    protected Object noActionMode;
    public int selectedItem=-1;

    public static final String EXTRA_RECUERDAME_ID = "extra_lawyer_id";
    public static final int REQUEST_UPDATE_DELETE_LAWYER = 2;
    private RecuerdameDbHelper mRecuerdosDbHelper;
    private RecuerdameCursorAdapter mRecuerdosAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecuerdosDbHelper = new RecuerdameDbHelper(this);

        RecuerdameFragment recuerdameFragment = (RecuerdameFragment)
                getSupportFragmentManager().findFragmentById(R.id.recuerdame_container);
        if(recuerdameFragment == null){
            recuerdameFragment = RecuerdameFragment.newInstance();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.recuerdame_container, recuerdameFragment)
                    .commit();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddScreen();
            }
        });

        lvTareas = (ListView) findViewById(R.id.lista);
        lvTareas.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvTareas.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (noActionMode != null) {
                    return false;
                }
                selectedItem = position;
                //menuTarea
                noActionMode = MainActivity.this.startActionMode(menuMain);
                view.setSelected(true);
                view.setBackgroundColor(Color.CYAN);
                return true;
            }
        });

        lvTareas.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                selectedItem = position;
                int idModificar = Integer.parseInt(arreglo[position].split(" ")[0]);
                Intent intent = new Intent(MainActivity.this, Modificar.class);
                intent.putExtra("idModificar", idModificar);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //cargar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_listar) {
            //return true;
            //startActivity(new Intent(this, Segunda.class));
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void cargar(){
        BaseHelper dbhelper = new BaseHelper(this, "DBAPP", null, 1);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        if(db != null){
            Cursor c = db.rawQuery("select * from Tareas", null);
            int cantidad = c.getCount();
            int j=0;
            arreglo = new String[cantidad];
            if(c.moveToFirst()){
                do{
                    String linea = c.getInt(0) + " " + c.getString(1);
                    arreglo[j] = linea;
                    j++;
                }while(c.moveToNext());
            }
            c.close();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, arreglo);
            ListView lista = (ListView) findViewById(R.id.lista);
            lista.setAdapter(adapter);
        }
    }

    private ActionMode.Callback menuMain = new ActionMode.Callback(){

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch(item.getItemId()){
                case R.id.menu_borrar:
                    Borrar();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            noActionMode = null;
            selectedItem = -1;
        }
    };

    private void Borrar(){
        BaseHelper dbhelper = new BaseHelper(this, "DBAPP", null, 1);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        int id= Integer.parseInt(arreglo[selectedItem].split(" ")[0]);
        if(db != null){
            long res = db.delete("Tareas", "Id="+id, null);
            if(res > 0){
                Toast.makeText(getApplicationContext(), "Tarea Eliminada "+id, Toast.LENGTH_SHORT).show();
                //cargar();
            }
        }
    }
    
     public void ModificarDatos(View v){
        String NombreTarea = et_tarea.getText().toString();
        BaseHelper dbhelper = new BaseHelper(this, "DBAPP", null, 1);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        if(db != null){
            ContentValues ModificarReg = new ContentValues();
            ModificarReg.put("Nombre", NombreTarea);
            long i = db.update("Tareas", ModificarReg, "Id="+Id,null);
            if(i>0){
                Toast.makeText(this, "Recuerdo Modificado", Toast.LENGTH_SHORT).show();
                super.onBackPressed();
            }
        }
    }

    public void RecuperarInfo(int id){
        BaseHelper dbhelper = new BaseHelper(this, "DBAPP", null, 1);
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        if(db != null) {
            Cursor c = db.rawQuery("select Id, Nombre from Tareas where Id="+id, null);
            try{
                if(c.moveToFirst()){
                    Id = c.getInt(0);
                    et_tarea.setText(c.getString(1));
                }
            }finally {
                c.close();
            }
        }
    }
    
    public void GuardarDatos(){
        String NombreTarea = et_tarea.getText().toString();
        BaseHelper dbhelper = new BaseHelper(this, "DBAPP", null, 1);
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        if(db != null){
            ContentValues NuevoRegistro = new ContentValues();
            NuevoRegistro.put("Nombre", NombreTarea);
            long i = db.insert("Tareas", null, NuevoRegistro);
            if(i>0){
                Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show();
                //agregar un registros
                //firebase.setValue(NombreTarea);
                //agregar varios registros
                new Firebase("https://recuerdameapp.firebaseio.com/items")
                        .push()
                        .child("todo")
                        .setValue(et_tarea.getText().toString());
                et_tarea.setText("");
            }
        }
    }
}
