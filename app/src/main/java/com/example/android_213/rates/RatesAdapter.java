package com.example.android_213.rates;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_213.R;
import com.example.android_213.orm.NbuRate;

import java.util.List;

public class RatesAdapter extends RecyclerView.Adapter<RatesAdapter.RateViewHolder> {

    private List<NbuRate> rates;
    private final OnRateClickListener listener;

    public RatesAdapter(List<NbuRate> rates, OnRateClickListener listener) {
        this.rates = rates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rate_item, parent, false);
        return new RateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RateViewHolder holder, int position) {
        holder.bind(rates.get(position));
    }

    @Override
    public int getItemCount() {
        return rates.size();
    }

    public void updateRates(List<NbuRate> newRates) {
        this.rates = newRates;
        notifyDataSetChanged();
    }

    class RateViewHolder extends RecyclerView.ViewHolder {
        TextView rateCc, rateValue;

        RateViewHolder(@NonNull View itemView) {
            super(itemView);
            rateCc = itemView.findViewById(R.id.rateCc);
            rateValue = itemView.findViewById(R.id.rateValue);
        }

        void bind(NbuRate rate) {
            rateCc.setText(rate.getCc());
            rateValue.setText(String.valueOf(rate.getRate()));

            itemView.setOnClickListener(v -> listener.onRateClick(rate));
        }
    }

    public interface OnRateClickListener {
        void onRateClick(NbuRate rate);
    }
}
