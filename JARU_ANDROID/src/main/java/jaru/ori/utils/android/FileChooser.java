package jaru.ori.utils.android;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import jaru.ori.gui.gpslog.android.R;
import jaru.ori.utils.Utilidades;

/**
 * Actividad que gestiona la visualizaciÃ³n, navegaciÃ³n y selecciÃ³n de ficheros
 * por el sistema de ficheros del SO
 * @author javier.arufe
 */
public class FileChooser extends ListActivity {
    private File currentDir;
    private FileArrayAdapter adapter;
    /**
     * MÃ©todo que se lanza la primera vez que se ejecuta la actividad
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //El directorio por defecto es el que tiene configurado el usuario
        currentDir = new File(Utilidades.getCDirActual());
        fill(currentDir);
    }
    /**
     * MÃ©todo que rellena los datos del listado con datos del fichero actual
     * @param f File
     */
    private void fill(File f) {
        File[]dirs = f.listFiles();
        this.setTitle("Current Dir: "+f.getName());
        List<Option>dir = new ArrayList<Option>();
        List<Option>fls = new ArrayList<Option>();
        try{
            for(File ff: dirs)
            {
                if(ff.isDirectory())
                    dir.add(new Option(ff.getName(),"Folder",ff.getAbsolutePath()));
                else
                {
                    fls.add(new Option(ff.getName(),"File Size: "+ff.length(),ff.getAbsolutePath()));
                }
            }
        }catch(Exception e)
        {

        }
        Collections.sort(dir);
        Collections.sort(fls);
        dir.addAll(fls);
        if(!f.getName().equalsIgnoreCase("sdcard"))
            dir.add(0,new Option("..","Parent Directory",f.getParent()));
        adapter = new FileArrayAdapter(FileChooser.this,R.layout.file_view,dir);
        this.setListAdapter(adapter);
    }
    /**
     * MÃ©todo que se lanza cuando el usuario selecciona un fichero
     * @param l ListView
     * @param v View
     * @param position int
     * @param id long
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);
        Option o = adapter.getItem(position);
        if(o.getData().equalsIgnoreCase("folder")||o.getData().equalsIgnoreCase("parent directory")){
            currentDir = new File(o.getPath());
            fill(currentDir);
        }
        else
        {
            onFileClick(o);
        }
    }
    /**
     * Cuando el usuario selecciona un fichero, este mÃ©todo guarda los valores para poder
     * encontrarlo y avisa de que se ha seleccionado. AsÃ­, la actividad que haga
     * uso de esta clase podrÃ¡ actuar sobre el fichero (para abrirlo, leerlo, borrarlo,
     * o lo que se necesite de Ã©l)
     * @param o Option
     */
    private void onFileClick(Option o)
    {
        Toast.makeText(this, "File Clicked: " + o.getPath(), Toast.LENGTH_SHORT).show();
        Utilidades.setCFicheroSel(o.getPath());
        Utilidades.setCFicheroSelNombre(o.getName());
        this.finish();
    }
}
