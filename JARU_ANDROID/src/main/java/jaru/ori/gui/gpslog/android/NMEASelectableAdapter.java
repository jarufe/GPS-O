package jaru.ori.gui.gpslog.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jaru.ori.logic.gpslog.Registro;

public class NMEASelectableAdapter extends RecyclerView.Adapter<NMEASelectableAdapter.ViewHolder> {
    private List<Registro> data;
    private Set<Integer> selectedPositions = new HashSet<>();

    public NMEASelectableAdapter(List<Registro> data) {
        this.data = data;
    }

    public List<Registro> getSeleccionados() {
        List<Registro> seleccionados = new ArrayList<>();
        for (Integer pos : selectedPositions) {
            if (pos >= 0 && pos < data.size()) {
                seleccionados.add(data.get(pos));
            }
        }
        return seleccionados;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.eliminar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Registro item = data.get(position);
        holder.textView.setText(item.getCTipoOCAD() + " - " + item.getCDesc());
        holder.checkBox.setOnCheckedChangeListener(null); // Evita efectos de reciclado
        holder.checkBox.setChecked(selectedPositions.contains(position));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedPositions.add(position);
            } else {
                selectedPositions.remove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        CheckBox checkBox;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            checkBox = itemView.findViewById(R.id.checkBox);
        }
    }
}
