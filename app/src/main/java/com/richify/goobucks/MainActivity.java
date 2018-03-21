package com.richify.goobucks;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramotion.cardslider.CardSliderLayoutManager;
import com.ramotion.cardslider.CardSnapHelper;
import com.richify.goobucks.cards.SliderAdapter;
import com.richify.goobucks.cards.SliderCard;
import com.richify.goobucks.model.Barista;
import com.richify.goobucks.model.Order;
import com.richify.goobucks.util.CircleTransform;
import com.richify.goobucks.util.DecodeBitmapTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = "MainActivity";
    private final String BARISTA = "barista";
    private final String COFFEE = "coffee";
    private final String TYPES = "types";
    private final String ORDERS = "orders";
    private final String HAS_OPEN_ORDER = "hasOpenOrder";
    private final String OPEN_ORDER_KEY = "openOrderKey";

    private final String[] times = {"Mon - Fri    12:00-14:00", "Mon - Fri    12:00-14:00", "Mon - Fri    12:00-14:00"};
    private LinkedHashMap<String, Barista> baristaMap;

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
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<Barista, SliderCard> mFirebaseAdapter;

    private final int TYPE_NOT_SELECTED = 10000;
    private View positiveAction;
    private boolean isDecaf = false;
    private int coffeeType = TYPE_NOT_SELECTED;

    private SharedPreferences userSharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        baristaMap = new LinkedHashMap<>();

        // initialize FirebaseAuth and current user
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        userSharedPreference = getBaseContext().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE
        );

        // Initializing Firebase database reference
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        SnapshotParser<Barista> parser = new SnapshotParser<Barista>() {
            @NonNull
            @Override
            public Barista parseSnapshot(@NonNull DataSnapshot snapshot) {
                Barista barista = snapshot.getValue(Barista.class);
                if (barista != null) {
                    barista.setUid(snapshot.getKey());
                }
                return barista;
            }
        };

        DatabaseReference baristaRef = mFirebaseDatabaseReference.child(BARISTA);
        FirebaseRecyclerOptions<Barista> options = new FirebaseRecyclerOptions.Builder<Barista>()
                .setQuery(baristaRef, parser)
                .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<Barista, SliderCard>(options) {
            @Override
            protected void onBindViewHolder(@NonNull SliderCard holder, int position, @NonNull Barista model) {
                if (model.getProfilePictureUri() != null) {
                    holder.setContentWithUri(Uri.parse(model.getProfilePictureUri()));
                } else {
                    Uri profileUri = Uri.parse(model.getProfilePictureUri());
                    Picasso.with(holder.imageView.getContext())
                            .load(profileUri)
                            .into(holder.imageView);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int clickPosition = recyclerView.getChildAdapterPosition(v);
                        if (clickPosition == currentPosition) {
                            Log.i(TAG, (new ArrayList<>(baristaMap.values())).get(clickPosition).getUid());
                            showOrderDialogue();
                        }
                    }
                });
            }

            @NonNull
            @Override
            public SliderCard onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.slider_card, parent, false);
                return new SliderCard(view);
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.scrollToPosition(0);
                onActiveCardChange();
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                if (payload != null) {
                    Log.i(TAG, payload.toString());
                }
            }
        });
        initRecyclerView();

        mFirebaseDatabaseReference.child(BARISTA)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Barista _barista = snapshot.getValue(Barista.class);
                    String key = snapshot.getKey();
                    baristaMap.put(key, _barista);
                }

                initBaristaName();
                initSwitchers();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mFirebaseDatabaseReference.child(BARISTA).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    String key = dataSnapshot.getKey();
                    if (!baristaMap.containsKey(key)) {
                        Barista _b = dataSnapshot.getValue(Barista.class);
                        baristaMap.put(key, _b);
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    String key = dataSnapshot.getKey();
                    if (baristaMap.containsKey(key)) {
                        Barista _b = dataSnapshot.getValue(Barista.class);
                        baristaMap.put(key, _b);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    String key = dataSnapshot.getKey();
                    baristaMap.remove(key);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button orderButton = findViewById(R.id.order_button);
        orderButton.setOnClickListener((v) -> showOrderDialogue());

    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mFirebaseAdapter);
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
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    private void initSwitchers() {
        ratingsSwitcher = (TextSwitcher) findViewById(R.id.barista_ratings);
        ratingsSwitcher.setFactory(new TextViewFactory(R.style.TemperatureTextView, true));
        ratingsSwitcher.setCurrentText((new ArrayList<>(baristaMap.values())).get(0).getRating().toString());

        placeSwitcher = (TextSwitcher) findViewById(R.id.ts_place);
        placeSwitcher.setFactory(new TextViewFactory(R.style.PlaceTextView, false));
        placeSwitcher.setCurrentText((new ArrayList<>(baristaMap.values())).get(0).getLocation());

        clockSwitcher = (TextSwitcher) findViewById(R.id.ts_clock);
        clockSwitcher.setFactory(new TextViewFactory(R.style.ClockTextView, false));
        clockSwitcher.setCurrentText(times[0]);

        descriptionsSwitcher = (TextSwitcher) findViewById(R.id.ts_description);
        descriptionsSwitcher.setInAnimation(this, android.R.anim.fade_in);
        descriptionsSwitcher.setOutAnimation(this, android.R.anim.fade_out);
        descriptionsSwitcher.setFactory(new TextViewFactory(R.style.DescriptionTextView, false));
        descriptionsSwitcher.setCurrentText((new ArrayList<>(baristaMap.values())).get(0).getDescription());

    }

    private void initBaristaName() {
        baristaAnimDuration = getResources().getInteger(R.integer.labels_animation_duration);
        baristaOffset1 = getResources().getDimensionPixelSize(R.dimen.left_offset);
        baristaOffset2 = getResources().getDimensionPixelSize(R.dimen.card_width);
        barista1TextView = (TextView) findViewById(R.id.barista_name_1);
        barista2TextView = (TextView) findViewById(R.id.barista_name_2);

        barista1TextView.setX(baristaOffset1);
        barista2TextView.setX(baristaOffset2);
        barista1TextView.setText((new ArrayList<>(baristaMap.values())).get(0).getDisplayName());
        barista2TextView.setAlpha(0f);

        barista1TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
        barista2TextView.setTypeface(Typeface.createFromAsset(getAssets(), "open-sans-extrabold.ttf"));
    }

    private void setBaristaText(String text, boolean left2right) {
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

        setBaristaText((new ArrayList<>(baristaMap.values())).get(pos % baristaMap.size()).getDisplayName(), left2right);

        ratingsSwitcher.setInAnimation(MainActivity.this, animH[0]);
        ratingsSwitcher.setOutAnimation(MainActivity.this, animH[1]);
        ratingsSwitcher.setText((new ArrayList<>(baristaMap.values())).get(pos % baristaMap.size()).getRating().toString());

        placeSwitcher.setInAnimation(MainActivity.this, animV[0]);
        placeSwitcher.setOutAnimation(MainActivity.this, animV[1]);
        placeSwitcher.setText((new ArrayList<>(baristaMap.values())).get(pos % baristaMap.size()).getLocation());

        clockSwitcher.setInAnimation(MainActivity.this, animV[0]);
        clockSwitcher.setOutAnimation(MainActivity.this, animV[1]);
        clockSwitcher.setText(times[pos % times.length]);

        descriptionsSwitcher.setText((new ArrayList<>(baristaMap.values())).get(pos % baristaMap.size()).getDescription());

        currentPosition = pos;
    }

    private void showOrderDialogue() {
        Barista _b = (new ArrayList<>(baristaMap.values())).get(currentPosition);

        Picasso.with(this)
                .load(Uri.parse(_b.getProfilePictureUri()))
                .transform(new CircleTransform())
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        Drawable drawable = new BitmapDrawable(
                                getBaseContext().getResources(),
                                bitmap
                        );
                        showOrderDialogueWithImageAndBarista(drawable, _b);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        // TODO: handle failed situation
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        // TODO: placeholder for prepare load
                    }
                });
    }

    private void showOrderDialogueWithImageAndBarista(Drawable imageDrawable, Barista barista) {

        MaterialDialog orderDialog = new MaterialDialog.Builder(this)
                .customView(R.layout.order_dialogue, true)
                .positiveText(R.string.order_dialog_submit)
                .negativeText(R.string.order_dialog_cancel)
                .positiveColor(getColor(R.color.primary))
                .negativeColor(getColor(R.color.secondary_text))
                .onPositive(
                        (dialog, which) -> onDialogPositiveClicked(isDecaf, coffeeType, barista.getUid())
                )
                .build();

        positiveAction = orderDialog.getActionButton(DialogAction.POSITIVE);

        ImageView profileImageView = orderDialog
                .getCustomView().findViewById(R.id.profile_image);
        profileImageView.setImageDrawable(imageDrawable);

        TextView titleTextView = orderDialog.getCustomView().findViewById(R.id.order_dialog_title);
        titleTextView.setText(getString(R.string.order_title, barista.getDisplayName()));

        RadioGroup radioGroup = orderDialog.getCustomView().findViewById(R.id.coffee_type_group);
        radioGroup = insertCoffeeTypesButtons(radioGroup);

        SwitchCompat switchCompat = orderDialog.getCustomView().findViewById(R.id.decaf_switch);
        switchCompat.setOnCheckedChangeListener(
                (buttonView, isChecked) -> decafSetter(isChecked)
        );

        radioGroup.setOnCheckedChangeListener(
                (group, checkedId) -> coffeeTypeSetter(checkedId)
        );

        orderDialog.show();
        positiveAction.setEnabled(false);
    }

    private void decafSetter(boolean isDecaf) {
        this.isDecaf = isDecaf;
    }

    private void coffeeTypeSetter(int type) {
        this.coffeeType = type;
        positiveAction.setEnabled(true);
    }

    private void onDialogPositiveClicked(boolean isDecaf, int type, String assigneeId) {
        if (type == TYPE_NOT_SELECTED)
            return;

        Order order = new Order(
                mUser.getUid(),
                assigneeId,
                type, Order.OrderStatus.PENDING.getOrderStatus(),
                0, "", isDecaf, new Timestamp(System.currentTimeMillis())
        );

        mFirebaseDatabaseReference.child(ORDERS)
                .push()
                .setValue(order, (DatabaseError databaseError, DatabaseReference databaseReference) -> {
                                SharedPreferences.Editor editor = userSharedPreference.edit();
                                editor.putBoolean(HAS_OPEN_ORDER, true);
                                editor.putString(OPEN_ORDER_KEY, databaseReference.getKey());
                                editor.commit();

                                Log.i(TAG, userSharedPreference.getString(OPEN_ORDER_KEY, "error retrieving key"));
                            }
                        );
    }

    private RadioGroup insertCoffeeTypesButtons(final RadioGroup radioGroup) {
        mFirebaseDatabaseReference
                .child(COFFEE)
                .child(TYPES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                            RadioGroup.LayoutParams.MATCH_PARENT,
                            RadioGroup.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(0, 10, 0, 10);

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        RadioButton radioButton = new RadioButton(getBaseContext());
                        radioButton.setLayoutParams(params);
                        radioButton.setText(snapshot.getValue().toString());
                        radioButton.setId(Integer.parseInt(snapshot.getKey()));

                        radioGroup.addView(radioButton, 0);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return radioGroup;
    }

    private class TextViewFactory implements ViewSwitcher.ViewFactory {

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
        public void onClick(View view) {
            final CardSliderLayoutManager sliderLayoutManager = (CardSliderLayoutManager)
                    recyclerView.getLayoutManager();

            if (sliderLayoutManager.isSmoothScrolling()) {
                return;
            }

            final int activeCardPosition = sliderLayoutManager.getActiveCardPosition();
            if (activeCardPosition == RecyclerView.NO_POSITION) {
                return;
            }

            int clickPosition = recyclerView.getChildAdapterPosition(view);
            if (clickPosition == activeCardPosition) {
                Log.i(TAG, (new ArrayList<>(baristaMap.values())).get(currentPosition).getUid());
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
