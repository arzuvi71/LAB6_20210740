package com.example.lab6_20210740.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6_20210740.R;
import com.example.lab6_20210740.models.Vehicle;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicles;
    private OnQRClickListener qrClickListener;
    private OnEditClickListener editClickListener;
    private OnDeleteClickListener deleteClickListener;

    public interface OnQRClickListener {
        void onQRClick(Vehicle vehicle);
    }

    public interface OnEditClickListener {
        void onEditClick(Vehicle vehicle);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Vehicle vehicle);
    }

    public VehicleAdapter(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void setOnQRClickListener(OnQRClickListener listener) {
        this.qrClickListener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.tvVehicleId.setText(vehicle.getId());
        holder.tvVehicleInfo.setText(vehicle.getBrand() + " " + vehicle.getModel() + " " + vehicle.getYear());
        holder.tvVehiclePlate.setText(vehicle.getPlate());
        holder.tvTechnicalReview.setText("Rev. Técnica: " + vehicle.getTechnicalReviewDate());

        holder.btnGenerateQR.setOnClickListener(v -> {
            if (qrClickListener != null) {
                qrClickListener.onQRClick(vehicle);
            }
        });

        holder.btnEditVehicle.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.onEditClick(vehicle);
            }
        });

        holder.btnDeleteVehicle.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.onDeleteClick(vehicle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    static class VehicleViewHolder extends RecyclerView.ViewHolder {
        TextView tvVehicleId;
        TextView tvVehicleInfo;
        TextView tvVehiclePlate;
        TextView tvTechnicalReview;
        Button btnGenerateQR;
        Button btnEditVehicle;
        Button btnDeleteVehicle;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleId = itemView.findViewById(R.id.tv_vehicle_id);
            tvVehicleInfo = itemView.findViewById(R.id.tv_vehicle_info);
            tvVehiclePlate = itemView.findViewById(R.id.tv_vehicle_plate);
            tvTechnicalReview = itemView.findViewById(R.id.tv_technical_review);
            btnGenerateQR = itemView.findViewById(R.id.btn_generate_qr);
            btnEditVehicle = itemView.findViewById(R.id.btn_edit_vehicle);
            btnDeleteVehicle = itemView.findViewById(R.id.btn_delete_vehicle);
        }
    }
}

