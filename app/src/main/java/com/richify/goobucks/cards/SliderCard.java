package com.richify.goobucks.cards;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.richify.goobucks.R;
import com.richify.goobucks.util.DecodeBitmapTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

/**
 * Created by thomaslin on 11/03/2018.
 *
 */

public class SliderCard extends RecyclerView.ViewHolder implements DecodeBitmapTask.Listener {

    private static int viewWidth = 0;
    private static int viewHeight = 0;
    private final ImageView imageView;
    private DecodeBitmapTask task;
    private Context context;
    public AVLoadingIndicatorView indicatorView;

    public SliderCard(View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.image);
        this.context = imageView.getContext();

        indicatorView = (AVLoadingIndicatorView) itemView.findViewById(R.id.indicator);
        indicatorView.smoothToShow();
    }

    public void setContent(@DrawableRes final int resId) {
        if (viewWidth == 0) {
            itemView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    viewWidth = itemView.getWidth();
                    viewHeight = itemView.getHeight();
                    loadBitmap(resId);
                }
            });
        } else {
            loadBitmap(resId);
        }
    }

    public void setContentWithUri(final Uri uri) {
        if (viewWidth == 0) {
            itemView.getViewTreeObserver()
                    .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            viewWidth = itemView.getWidth();
                            viewHeight = itemView.getHeight();

                            Picasso.with(context)
                                    .load(uri)
                                    .into(imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            indicatorView.smoothToHide();
                                        }

                                        @Override
                                        public void onError() {

                                        }
                                    });
                        }
                    });
        } else {
            Picasso.with(context)
                    .load(uri)
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            indicatorView.smoothToHide();
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
    }

    void clearContent() {
        if (task != null) {
            task.cancel(true);
        }
    }

    private void loadBitmap(@DrawableRes int resId) {
        task = new DecodeBitmapTask(itemView.getResources(), resId, viewWidth, viewHeight, this);
        task.execute();
    }

    @Override
    public void onPostExecuted(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
