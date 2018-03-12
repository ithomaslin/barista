package com.richify.goobucks;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;
import com.richify.goobucks.cards.SliderAdapter;
import com.richify.goobucks.model.Barista;
import com.richify.goobucks.util.DecodeBitmapTask;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private final int[] pics = {R.drawable.r1, R.drawable.r2, R.drawable.r3, R.drawable.r4, R.drawable.r5};
    private Uri[] uris = {
            Uri.parse("https://graph.facebook.com/10213189667194885/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D"),
            Uri.parse("https://graph.facebook.com/10213189667194885/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D"),
            Uri.parse("https://graph.facebook.com/10213189667194885/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D"),
            Uri.parse("https://graph.facebook.com/10213189667194885/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D"),
            Uri.parse("https://graph.facebook.com/10213189667194885/picture?height=200&width=200&migration_overrides=%7Boctober_2012%3Atrue%7D")
    };
    private final int[] descriptions = {R.string.text1, R.string.text2, R.string.text3, R.string.text4, R.string.text5};
    private final String[] baristaNames = {"Felix Lin", "Thomas Lin", "David Dai", "Ashley Hsieh", "Steven Tzou"};
    private final String[] places = {"Taipei", "Taipei", "Taipei", "Taipei", "Taipei"};
    private final String[] ratings = {"4.1", "4.7", "4.3", "4.2", "4.5"};
    private final String[] times = {"Mon - Fri    12:00-14:00", "Mon - Fri    12:00-14:00", "Mon - Fri    12:00-14:00"};

    private List<String> bns;

    private List<Barista> baristas;

    private final SliderAdapter sliderAdapter = new SliderAdapter(pics, uris, 5, new OnCardClickListener());

    private CardSliderLayoutManager layoutManger;
    private RecyclerView recyclerView;
    private TextSwitcher ratingsSwitcher;
    private TextSwitcher placeSwitcher;
    private TextSwitcher clockSwitcher;
    private TextSwitcher descriptionsSwitcher;

    private TextView barista1TextView;
    private TextView barista2TextView;
    private int baristaOffset1;
    private int baristaOffset2;
    private long baristaAnimDuration;
    private int currentPosition;
    private DatabaseReference mDatabaseRef;
    private final String BARISTA = "barista";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing Firebase database reference
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        baristas = new ArrayList<>();
        populateBaristaInfo();

        bns = new ArrayList<>();
        bns.add("Felix Lin");
        bns.add("Thomas");
        bns.add("David");
        bns.add("Ashley");
        bns.add("Steven");

        initRecyclerView();
        initCountryText();
        initSwitchers();
    }

    private void populateBaristaInfo() {
        mDatabaseRef.child(BARISTA).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Barista barista = snapshot.getValue(Barista.class);
                    if (barista != null) {
                        Log.i(TAG, barista.getDisplayName());
                        baristas.add(barista);
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(sliderAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    onActiveCardChange();
                }
            }
        });

        layoutManger = (CardSliderLayoutManager) recyclerView.getLayoutManager();
        new CardSnapHelper().attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void initSwitchers() {
        ratingsSwitcher = (TextSwitcher) findViewById(R.id.barista_ratings);
        ratingsSwitcher.setFactory(new TextViewFactory(R.style.TemperatureTextView, true));
        ratingsSwitcher.setCurrentText(ratings[0]);

        placeSwitcher = (TextSwitcher) findViewById(R.id.ts_place);
        placeSwitcher.setFactory(new TextViewFactory(R.style.PlaceTextView, false));
        placeSwitcher.setCurrentText(places[0]);

        clockSwitcher = (TextSwitcher) findViewById(R.id.ts_clock);
        clockSwitcher.setFactory(new TextViewFactory(R.style.ClockTextView, false));
        clockSwitcher.setCurrentText(times[0]);

        descriptionsSwitcher = (TextSwitcher) findViewById(R.id.ts_description);
        descriptionsSwitcher.setInAnimation(this, android.R.anim.fade_in);
        descriptionsSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        descriptionsSwitcher.setFactory(new TextViewFactory(R.style.DescriptionTextView, false));
        descriptionsSwitcher.setCurrentText(getString(descriptions[0]));

    }

    private void initCountryText() {
        baristaAnimDuration = getResources().getInteger(R.integer.labels_animation_duration);
        baristaOffset1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        baristaOffset2 = getResources().getDimensionPixelSize(R.dimen.card_width);
        barista1TextView = (TextView) findViewById(R.id.barista_name_1);
        barista2TextView = (TextView) findViewById(R.id.barista_name_2);

        barista1TextView.setX(baristaOffset1);
        barista2TextView.setX(baristaOffset2);
        barista1TextView.setText(bns.get(0));
        barista2TextView.setAlpha(0f);

        barista1TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
        barista2TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
    }

    private void setCountryText(String text, boolean left2right) {
        final TextView invisibleText;
        final TextView visibleText;
        if (barista1TextView.getAlpha() > barista2TextView.getAlpha()) {
            visibleText = barista1TextView;
            invisibleText = barista2TextView;
        } else {
            visibleText = barista2TextView;
            invisibleText = barista1TextView;
        }

        final int vOffset;
        if (left2right) {
            invisibleText.setX(0);
            vOffset = baristaOffset2;
        } else {
            invisibleText.setX(baristaOffset2);
            vOffset = 0;
        }

        invisibleText.setText(text);

        final ObjectAnimator iAlpha = ObjectAnimator.ofFloat(invisibleText, "alpha", 1f);
        final ObjectAnimator vAlpha = ObjectAnimator.ofFloat(visibleText, "alpha", 0f);
        final ObjectAnimator iX = ObjectAnimator.ofFloat(invisibleText, "x", baristaOffset1);
        final ObjectAnimator vX = ObjectAnimator.ofFloat(visibleText, "x", vOffset);

        final AnimatorSet animSet = new AnimatorSet();
        animSet.playTogether(iAlpha, vAlpha, iX, vX);
        animSet.setDuration(baristaAnimDuration);
        animSet.start();
    }

    private void onActiveCardChange() {
        final int pos = layoutManger.getActiveCardPosition();
        if (pos == RecyclerView.NO_POSITION || pos == currentPosition) {
            return;
        }

        onActiveCardChange(pos);
    }

    private void onActiveCardChange(int pos) {
        int animH[] = new int[] {R.anim.slide_in_right, R.anim.slide_out_left};
        int animV[] = new int[] {R.anim.slide_in_top, R.anim.slide_out_bottom};

        final boolean left2right = pos < currentPosition;
        if (left2right) {
            animH[0] = R.anim.slide_in_left;
            animH[1] = R.anim.slide_out_right;

            animV[0] = R.anim.slide_in_bottom;
            animV[1] = R.anim.slide_out_top;
        }

        setCountryText(bns.get(pos % bns.size()), left2right);

        ratingsSwitcher.setInAnimation(MainActivity.this, animH[0]);
        ratingsSwitcher.setOutAnimation(MainActivity.this, animH[1]);
        ratingsSwitcher.setText(ratings[pos % ratings.length]);

        placeSwitcher.setInAnimation(MainActivity.this, animV[0]);
        placeSwitcher.setOutAnimation(MainActivity.this, animV[1]);
        placeSwitcher.setText(places[pos % places.length]);

        clockSwitcher.setInAnimation(MainActivity.this, animV[0]);
        clockSwitcher.setOutAnimation(MainActivity.this, animV[1]);
        clockSwitcher.setText(times[pos % times.length]);

        descriptionsSwitcher.setText(getString(descriptions[pos % descriptions.length]));

        currentPosition = pos;
    }

    private class TextViewFactory implements  ViewSwitcher.ViewFactory {

        @StyleRes
        final int styleId;
        final boolean center;

        TextViewFactory(@StyleRes int styleId, boolean center) {
            this.styleId = styleId;
            this.center = center;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View makeView() {
            final TextView textView = new TextView(MainActivity.this);

            if (center) {
                textView.setGravity(Gravity.CENTER);
            }

            textView.setTextAppearance(styleId);

            return textView;
        }

    }

    private class OnCardClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {}
    }
}
