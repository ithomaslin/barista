package com.richify.goobucks.cards;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.richify.goobucks.R;

/**
 * Created by thomaslin on 11/03/2018.
 *
 */

public class SliderAdapter extends RecyclerView.Adapter<SliderCard> {

    private final int count;
    private final int[] content;
    private final Uri[] uris;
    private final View.OnClickListener listener;

    public SliderAdapter(@Nullable int[] content, @Nullable Uri[] uris, int count, View.OnClickListener listener) {
        this.content = content;
        this.uris = uris;
        this.count = count;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SliderCard onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(SliderCard holder, int position) {
//        holder.setContent(content[position % content.length]);
        holder.setContentWithUri(uris[position % uris.length]);
    }

    @Override
    public void onViewRecycled(SliderCard holder) {
        holder.clearContent();
    }

    @Override
    public int getItemCount() {
        return count;
    }
}
