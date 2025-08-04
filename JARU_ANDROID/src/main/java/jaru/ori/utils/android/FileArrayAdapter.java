package jaru.ori.utils.android;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jaru.ori.gui.gpslog.android.R;

/**
 * Clase adaptadora para la utilidad de selecciÃ³n de ficheros.
 * @author javier.arufe
 */
public class FileArrayAdapter extends ArrayAdapter<Option>{

    private Context c;
    private int id;
    private List<Option>items;
    /**
     * Constructor de la clase
     * @param context Context
     * @param textViewResourceId int
     * @param objects List<Option>
     */
    public FileArrayAdapter(Context context, int textViewResourceId,
                            List<Option> objects) {
        super(context, textViewResourceId, objects);
        c = context;
        id = textViewResourceId;
        items = objects;
    }
    /**
     * Devuelve el objeto que contiene los datos de una fila de la lista
     * @param i int PosiciÃ³n dentro de la lista
     * @return Option
     */
    public Option getItem(int i) {
        return items.get(i);
    }
    /**
     * Devuelve un objeto que representa a la vista en pantalla
     * @param position int
     * @param convertView View
     * @param parent ViewGroup
     * @return View
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(id, null);
        }
        final Option o = items.get(position);
        if (o != null) {
            TextView t1 = (TextView) v.findViewById(R.id.TextView01);
            TextView t2 = (TextView) v.findViewById(R.id.TextView02);
            if(t1!=null)
                t1.setText(o.getName());
            if(t2!=null)
                t2.setText(o.getData());
        }
        return v;
    }

}