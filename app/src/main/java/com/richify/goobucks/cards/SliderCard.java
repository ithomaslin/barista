package com.richify.goobucks.cards;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.richify.goobucks.R;
import com.richify.goobucks.util.DecodeBitmapTask;

/**
 * Created by thomaslin on 11/03/2018.
 */

public class SliderCard extends RecyclerView.ViewHolder implements DecodeBitmapTask.Listener {

    private static int viewWidth = 0;
    private static int viewHeight = 0;
    private final ImageView imageView;
    private DecodeBitmapTask task;

    public SliderCard(View itemView) {
        super(itemView);
        imageView = (ImageView) itemView.findViewById(R.id.image);
    }

    @Override
    public void onPostExecuted(Bitmap bitmap) {

    }
}
