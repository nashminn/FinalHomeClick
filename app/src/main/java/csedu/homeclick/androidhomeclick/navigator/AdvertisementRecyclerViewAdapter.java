package csedu.homeclick.androidhomeclick.navigator;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import csedu.homeclick.androidhomeclick.R;
import csedu.homeclick.androidhomeclick.structure.Advertisement;

public class AdvertisementRecyclerViewAdapter extends RecyclerView.Adapter<AdvertisementRecyclerViewAdapter.ViewHolder> {

    private List<Advertisement> advertisementArrayList = new ArrayList<>();
    private OnAdCardClickListener onAdCardClickListener;

    public AdvertisementRecyclerViewAdapter() {
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_card, parent, false);
        ViewHolder holder = new ViewHolder(view, onAdCardClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.areaName.setText(advertisementArrayList.get(position).getAreaName());
        holder.adType.setText(advertisementArrayList.get(position).getAdType());
        holder.fullAddress.setText(advertisementArrayList.get(position).getFullAddress());
        holder.bedroom.setText(String.format(Locale.getDefault(), "%d", advertisementArrayList.get(position).getNumberOfBedrooms()));
        holder.bathroom.setText(String.format(Locale.getDefault(), "%d", advertisementArrayList.get(position).getNumberOfBathrooms()));

        Boolean gasAvail = advertisementArrayList.get(position).getGasAvailability();
        if(advertisementArrayList.get(position).getGasAvailability() == null) {
            Log.i("adapter","something is wrong");
        }
        String gasString;
        if(gasAvail) gasString = "Available";
        else gasString = "Not Available";

        holder.gasAvailability.setText(gasString);
        String payAmount = String.format(Locale.getDefault(), "%d", advertisementArrayList.get(position).getPaymentAmount()) + " BDT";
        holder.paymentAmount.setText(payAmount);
    }

    @Override
    public int getItemCount() {
        return advertisementArrayList.size();
    }

    public void setAdvertisementArrayList(List<Advertisement> advertisementArrayList) {
        this.advertisementArrayList = advertisementArrayList;
        notifyDataSetChanged();
    }

    public void setAdCardListener(OnAdCardClickListener adCardListener) {
        this.onAdCardClickListener = adCardListener;
    }


    //TODO: needs checking
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private TextView areaName, fullAddress, bedroom, bathroom, gasAvailability, paymentAmount, adType;
        OnAdCardClickListener onAdCardClickListener;


        public ViewHolder(@NonNull @NotNull View itemView, OnAdCardClickListener onAdCardClickListener) {
            super(itemView);

            bindWidgets(itemView, onAdCardClickListener);

            itemView.setOnClickListener(this);
        }

        private void bindWidgets(View view, OnAdCardClickListener onAdCardClickListener) {
            areaName = view.findViewById(R.id.card_area_name);
            fullAddress = view.findViewById(R.id.card_full_address);
            bedroom = view.findViewById(R.id.card_bedroom);
            bathroom = view.findViewById(R.id.card_bathroom);
            gasAvailability = view.findViewById(R.id.card_gas);
            paymentAmount = view.findViewById(R.id.card_payment);
            adType = view.findViewById(R.id.card_ad_type);

            this.onAdCardClickListener = onAdCardClickListener;
        }

        @Override
        public void onClick(View v) {
            onAdCardClickListener.onAdClick(getAdapterPosition());
        }
    }

    public interface OnAdCardClickListener{
        void onAdClick(int position);
    }
}
