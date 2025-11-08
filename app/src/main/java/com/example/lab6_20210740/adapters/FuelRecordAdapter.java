package com.example.lab6_20210740.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20210740.R;
import com.example.lab6_20210740.models.FuelRecord;

import java.util.List;

public class FuelRecordAdapter extends RecyclerView.Adapter<FuelRecordAdapter.FuelRecordViewHolder> {

    private List<FuelRecord> fuelRecords;

    public FuelRecordAdapter(List<FuelRecord> fuelRecords) {
        this.fuelRecords = fuelRecords;
    }

    @NonNull
    @Override
    public FuelRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fuel_record, parent, false);
        return new FuelRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuelRecordViewHolder holder, int position) {
        FuelRecord record = fuelRecords.get(position);
        holder.tvRecordId.setText("ID: " + record.getRecordId());
        holder.tvVehicleId.setText("Vehículo: " + record.getVehicleId());
        holder.tvDate.setText("Fecha: " + record.getDate());
        holder.tvLiters.setText(String.format("%.2f L", record.getLiters()));
        holder.tvKilometers.setText(String.format("%.0f km", record.getCurrentKilometers()));
        holder.tvPrice.setText(String.format("S/ %.2f", record.getTotalPrice()));
        holder.tvFuelType.setText(record.getFuelType());
    }

    @Override
    public int getItemCount() {
        return fuelRecords.size();
    }

    static class FuelRecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecordId;
        TextView tvVehicleId;
        TextView tvDate;
        TextView tvLiters;
        TextView tvKilometers;
        TextView tvPrice;
        TextView tvFuelType;

        public FuelRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecordId = itemView.findViewById(R.id.tv_record_id);
            tvVehicleId = itemView.findViewById(R.id.tv_vehicle_id);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvLiters = itemView.findViewById(R.id.tv_liters);
            tvKilometers = itemView.findViewById(R.id.tv_kilometers);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvFuelType = itemView.findViewById(R.id.tv_fuel_type);
        }
    }
}

