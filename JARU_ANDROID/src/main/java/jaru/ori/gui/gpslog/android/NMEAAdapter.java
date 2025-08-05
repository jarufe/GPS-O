package jaru.ori.gui.gpslog.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NMEAAdapter extends RecyclerView.Adapter<NMEAAdapter.ViewHolder> {

    private final List<String> datos;
    private final OnItemClickListener listener;

    // Interfaz para manejar clics
    public interface OnItemClickListener {
        void onItemClick(String item, int position);
    }

    // Constructor
    public NMEAAdapter(List<String> datos, OnItemClickListener listener) {
        this.datos = datos;
        this.listener = listener;
    }

    // ViewHolder interno
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtDato;

        public ViewHolder(View itemView) {
            super(itemView);
            txtDato = itemView.findViewById(android.R.id.text1);
        }
    }

    @NonNull
    @Override
    public NMEAAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String item = datos.get(position);
        holder.txtDato.setText(item);

        // Manejo del clic
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }
}
