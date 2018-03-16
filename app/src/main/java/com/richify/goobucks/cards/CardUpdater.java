package com.richify.goobucks.cards;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import com.ramotion.cardslider.DefaultViewUpdater;

/**
 * Created by thomaslin on 11/03/2018.
 *
 */

public class CardUpdater extends DefaultViewUpdater {

    @Override
    public void updateView(@NonNull View view, float position) {
        super.updateView(view, position);

        final CardView card = ((CardView) view);
        final View alphaView = card.getChildAt(1);
        final View imageView = card.getChildAt(0);

        if (position < 0) {
            final float alpha = view.getAlpha();
            view.setAlpha(1f);
            alphaView.setAlpha(0.9f - alpha);
            imageView.setAlpha(0.3f + alpha);
        } else {
            alphaView.setAlpha(0f);
            imageView.setAlpha(1f);
        }


    }
}
