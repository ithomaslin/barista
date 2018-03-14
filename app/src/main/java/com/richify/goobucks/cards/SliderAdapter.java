package com.richify.goobucks.cards;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.richify.goobucks.R;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

/**
 * Created by thomaslin on 11/03/2018.
 *
 */

public class SliderAdapter extends RecyclerView.Adapter<SliderCard> {

    private int count;
    private List<Uri> uris;
    private View.OnClickListener listener;

    public SliderAdapter(List<Uri> uris, int count, View.OnClickListener listener) {
        this.uris = uris;
        this.count = count;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SliderCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.slider_card, parent, false);

        if (listener != null) {
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(view);
                }
            });
        }

        return new SliderCard(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderCard holder, int position) {
        holder.indicatorView.setIndicator("BallScaleMultipleIndicator");
        holder.setContentWithUri(uris.get(position % uris.size()));
    }

    @Override
    public void onViewRecycled(@NonNull SliderCard holder) {
        holder.clearContent();
    }

    @Override
    public int getItemCount() {
        return count;
    }

}
