package com.exampe.storeandmore.fragment;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static com.exampe.storeandmore.helper.ApiConfig.AddOrRemoveFavorite;
import static com.exampe.storeandmore.helper.ApiConfig.GetSettings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.airbnb.lottie.LottieAnimationView;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.adapter.AdapterStyle1;
import com.exampe.storeandmore.adapter.ReviewAdapter;
import com.exampe.storeandmore.adapter.SliderAdapter;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.DatabaseHelper;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.helper.Utils;
import com.exampe.storeandmore.model.FlashSale;
import com.exampe.storeandmore.model.Product;
import com.exampe.storeandmore.model.Review;
import com.exampe.storeandmore.model.Slider;
import com.exampe.storeandmore.model.Variants;

public class ProductDetailFragment extends Fragment {
    static ArrayList<Slider> sliderArrayList;
    TextView showDiscount, tvMfg, tvMadeIn, tvProductName, tvQuantity, tvPrice, tvOriginalPrice, tvMeasurement, tvStatus, tvTitleMadeIn, tvTitleMfg, tvTimer, tvTimerTitle;
    WebView webDescription;
    ViewPager viewPager;
    Spinner spinner;
    LinearLayout lytSpinner;
    ImageView imgIndicator;
    LinearLayout mMarkersLayout, lytMfg, lytMadeIn;
    RelativeLayout lytMainPrice, lytQuantity;
    ScrollView scrollView;
    Session session;
    boolean isFavorite;
    ImageView imgFav;
    ImageButton imgAdd, imgMinus;
    LinearLayout lytShare, lytSave, lytSimilar;
    int count;
    View root;
    int variantPosition;
    String from, id;
    boolean isLogin;
    Product product;
    DatabaseHelper databaseHelper;
    int position = 0;
    Button btnCart;
    Activity activity;
    RecyclerView recyclerView, recyclerViewReview;
    RelativeLayout relativeLayout, lytTimer;
    TextView tvMore;
    ImageView imgReturnable, imgCancellable;
    TextView tvReturnable, tvCancellable;
    String taxPercentage;
    LottieAnimationView lottieAnimationView;
    ShimmerFrameLayout mShimmerViewContainer;
    Button btnAddToCart;
    ArrayList<Review> reviewArrayList;
    ReviewAdapter reviewAdapter;
    RatingBar ratingProduct_, ratingProduct;
    TextView tvRatingProductCount, tvRatingCount, tvMoreReview, tvReviewDetail;
    LinearLayout lytProductRatings;
    RelativeLayout lytReview;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_product_detail, container, false);

        setHasOptionsMenu(true);
        activity = getActivity();

        Constant.CartValues = new HashMap<>();

        session = new Session(activity);
        isLogin = session.getBoolean(Constant.IS_USER_LOGIN);
        databaseHelper = new DatabaseHelper(activity);

        assert getArguments() != null;
        from = getArguments().getString(Constant.FROM);


        taxPercentage = "0";

        variantPosition = getArguments().getInt("variantPosition", 0);
        id = getArguments().getString("id");

        if (from.equals("favorite") || from.equals("fragment") || from.equals("sub_cate") || from.equals("product") || from.equals("search") || from.equals("flash_sale")) {
            position = getArguments().getInt("position");
        }

        lytQuantity = root.findViewById(R.id.lytQuantity);
        scrollView = root.findViewById(R.id.scrollView);
        mMarkersLayout = root.findViewById(R.id.layout_markers);
        sliderArrayList = new ArrayList<>();
        viewPager = root.findViewById(R.id.viewPager);
        tvProductName = root.findViewById(R.id.tvProductName);
        tvOriginalPrice = root.findViewById(R.id.tvOriginalPrice);
        webDescription = root.findViewById(R.id.txtDescription);
        tvPrice = root.findViewById(R.id.tvPrice);
        tvMeasurement = root.findViewById(R.id.tvMeasurement);
        imgFav = root.findViewById(R.id.imgFav);
        lytMainPrice = root.findViewById(R.id.lytMainPrice);
        tvQuantity = root.findViewById(R.id.tvQuantity);
        tvStatus = root.findViewById(R.id.tvStatus);
        imgAdd = root.findViewById(R.id.btnAddQty);
        imgMinus = root.findViewById(R.id.btnMinusQty);
        spinner = root.findViewById(R.id.spinner);
        lytSpinner = root.findViewById(R.id.lytSpinner);
        imgIndicator = root.findViewById(R.id.imgIndicator);
        showDiscount = root.findViewById(R.id.showDiscount);
        lytShare = root.findViewById(R.id.lytShare);
        lytSave = root.findViewById(R.id.lytSave);
        lytSimilar = root.findViewById(R.id.lytSimilar);
        tvMadeIn = root.findViewById(R.id.tvMadeIn);
        tvTitleMadeIn = root.findViewById(R.id.tvTitleMadeIn);
        tvMfg = root.findViewById(R.id.tvMfg);
        tvTitleMfg = root.findViewById(R.id.tvTitleMfg);
        tvTimer = root.findViewById(R.id.tvTimer);
        tvTimerTitle = root.findViewById(R.id.tvTimerTitle);
        lytMfg = root.findViewById(R.id.lytMfg);
        lytMadeIn = root.findViewById(R.id.lytMadeIn);
        btnCart = root.findViewById(R.id.btnCart);
        recyclerView = root.findViewById(R.id.recyclerView);
        recyclerViewReview = root.findViewById(R.id.recyclerViewReview);
        relativeLayout = root.findViewById(R.id.relativeLayout);
        lytTimer = root.findViewById(R.id.lytTimer);
        tvMore = root.findViewById(R.id.tvMore);

        ratingProduct_ = root.findViewById(R.id.ratingProduct_);
        ratingProduct = root.findViewById(R.id.ratingProduct);
        tvRatingProductCount = root.findViewById(R.id.tvRatingProductCount);
        tvRatingCount = root.findViewById(R.id.tvRatingCount);
        tvReviewDetail = root.findViewById(R.id.tvReviewDetail);
        tvMoreReview = root.findViewById(R.id.tvMoreReview);
        lytProductRatings = root.findViewById(R.id.lytProductRatings);
        lytReview = root.findViewById(R.id.lytReview);

        tvReturnable = root.findViewById(R.id.tvReturnable);
        tvCancellable = root.findViewById(R.id.tvCancellable);
        imgReturnable = root.findViewById(R.id.imgReturnable);
        imgCancellable = root.findViewById(R.id.imgCancellable);
        btnAddToCart = root.findViewById(R.id.btnAddToCart);

        lottieAnimationView = root.findViewById(R.id.lottieAnimationView);
        lottieAnimationView.setAnimation("add_to_wish_list.json");

        mShimmerViewContainer = root.findViewById(R.id.mShimmerViewContainer);

        recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewReview.setLayoutManager(new LinearLayoutManager(activity));

        if (session.getData(Constant.ratings).equals("1")) {
            lytProductRatings.setVisibility(View.VISIBLE);
            lytReview.setVisibility(View.VISIBLE);
        } else {
            lytProductRatings.setVisibility(View.GONE);
            lytReview.setVisibility(View.GONE);
        }

        GetProductDetail(id);
        GetSettings(activity);

        lytMainPrice.setOnClickListener(view -> spinner.performClick());

        tvMore.setOnClickListener(v -> ShowSimilar());

        tvMoreReview.setOnClickListener(v -> {
            Fragment fragment = new ReviewFragment();
            Bundle bundle = new Bundle();
            bundle.putString(Constant.FROM, from);
            bundle.putString(Constant.ID, id);
            fragment.setArguments(bundle);
            MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
        });

        lytSimilar.setOnClickListener(view -> ShowSimilar());

        btnCart.setOnClickListener(v -> MainActivity.fm.beginTransaction().add(R.id.container, new CartFragment()).addToBackStack(null).commit());

        lytShare.setOnClickListener(view -> {
            String message = Constant.WebsiteUrl + "product/" + product.getSlug();
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            sendIntent.putExtra(Intent.EXTRA_TEXT, message);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_via));
            startActivity(shareIntent);
        });

        lytSave.setOnClickListener(view -> {
            if (isLogin) {
                isFavorite = product.getIs_favorite();
                if (ApiConfig.isConnected(activity)) {
                    if (isFavorite) {
                        isFavorite = false;
                        lottieAnimationView.setVisibility(View.GONE);
                        product.setIs_favorite(false);
                        imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                    } else {
                        isFavorite = true;
                        product.setIs_favorite(true);
                        lottieAnimationView.setVisibility(View.VISIBLE);
                        lottieAnimationView.playAnimation();
                    }
                    AddOrRemoveFavorite(activity, session, product.getVariants().get(0).getProduct_id(), isFavorite);
                }
            } else {
                isFavorite = databaseHelper.getFavoriteById(product.getId());
                if (isFavorite) {
                    isFavorite = false;
                    lottieAnimationView.setVisibility(View.GONE);
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                } else {
                    isFavorite = true;
                    lottieAnimationView.setVisibility(View.VISIBLE);
                    lottieAnimationView.playAnimation();
                }
                databaseHelper.AddOrRemoveFavorite(product.getVariants().get(0).getProduct_id(), isFavorite);
            }
            switch (from) {
                case "fragment":
                case "sub_cate":
                case "search":
                    ProductListFragment.productArrayList.get(position).setIs_favorite(isFavorite);
                    ProductListFragment.productLoadMoreAdapter.notifyDataSetChanged();
                    break;
                case "favorite":
                    product.setIs_favorite(isFavorite);
                    if (isFavorite) {
                        FavoriteFragment.productArrayList.add(product);
                    } else {
                        FavoriteFragment.productArrayList.remove(position);
                    }
                    FavoriteFragment.productLoadMoreAdapter.notifyDataSetChanged();
                    break;
            }
        });

        return root;
    }

    public void ShowSimilar() {
        Fragment fragment = new ProductListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", product.getId());
        bundle.putString("cat_id", product.getCategory_id());
        bundle.putString(Constant.FROM, "similar");
        bundle.putString("name", "Similar Products");
        fragment.setArguments(bundle);
        MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
    }


    void GetSimilarData(Product product) {
        ArrayList<Product> productArrayList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SIMILAR_PRODUCT, Constant.GetVal);
        params.put(Constant.PRODUCT_ID, product.getId());
        params.put(Constant.CATEGORY_ID, product.getCategory_id());
        params.put(Constant.USER_ID, session.getData(Constant.ID));
        params.put(Constant.LIMIT, "" + Constant.LOAD_ITEM_LIMIT);

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArray = jsonObject1.getJSONArray(Constant.DATA);
                        try {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Product product1 = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                                productArrayList.add(product1);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        AdapterStyle1 adapter = new AdapterStyle1(activity, activity, productArrayList, R.layout.offer_layout);
                        recyclerView.setAdapter(adapter);
                        relativeLayout.setVisibility(View.VISIBLE);
                    } else {
                        relativeLayout.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, activity, Constant.GET_SIMILAR_PRODUCT_URL, params, false);
    }

    public void NotifyData(int count) {
        switch (from) {
            case "fragment":
            case "flash_sale":
            case "search":
                ProductListFragment.productArrayList.get(position).getVariants().get(variantPosition).setCart_count("" + count);
                ProductListFragment.productLoadMoreAdapter.notifyItemChanged(position, ProductListFragment.productArrayList.get(position));
                if (isLogin) {
                    ApiConfig.getCartItemCount(activity, session);
                } else {
                    databaseHelper.getTotalItemOfCart(activity);
                }
                break;
            case "product":
                if (isLogin) {
                    FavoriteFragment.productArrayList.get(position).getVariants().get(variantPosition).setCart_count("" + count);
                    FavoriteFragment.productLoadMoreAdapter.notifyItemChanged(position, FavoriteFragment.productArrayList.get(position));
                } else {
                    FavoriteFragment.productArrayList.get(position).getVariants().get(variantPosition).setCart_count("" + count);
                    FavoriteFragment.productLoadMoreAdapter.notifyItemChanged(position, FavoriteFragment.productArrayList.get(position));
                    databaseHelper.getTotalItemOfCart(activity);
                }
                break;
            case "section":
            case "share":
                if (!isLogin) {
                    databaseHelper.getTotalItemOfCart(activity);
                } else {
                    ApiConfig.getCartItemCount(activity, session);
                }
                break;
        }
    }

    void GetProductDetail(final String productId) {
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        if (from.equals("share")) {
            params.put(Constant.SLUG, productId);
        } else {
            params.put(Constant.PRODUCT_ID, productId);
        }
        if (isLogin) {
            params.put(Constant.USER_ID, session.getData(Constant.ID));
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject1 = new JSONObject(response);
                    if (!jsonObject1.getBoolean(Constant.ERROR)) {
                        JSONObject object = new JSONObject(response);
                        JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            product = new Gson().fromJson(jsonArray.getJSONObject(i).toString(), Product.class);
                        }
                        SetProductDetails(product);
                        GetSimilarData(product);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                scrollView.setVisibility(View.VISIBLE);
                mShimmerViewContainer.setVisibility(View.GONE);
                mShimmerViewContainer.stopShimmer();
            }
        }, activity, Constant.GET_PRODUCT_DETAIL_URL, params, false);
    }

    void GetReviews(final String productId) {
        reviewArrayList = new ArrayList<>();
        scrollView.setVisibility(View.GONE);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_PRODUCT_REVIEW, Constant.GetVal);
        params.put(Constant.LIMIT, "5");
        params.put(Constant.OFFSET, "0");
        if (from.equals("share")) {
            params.put(Constant.SLUG, productId);
        } else {
            params.put(Constant.PRODUCT_ID, productId);
        }

        ApiConfig.RequestToVolley((result, response) -> {
            if (result) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (!jsonObject.getBoolean(Constant.ERROR)) {
                        JSONArray jsonArrayReviews = jsonObject.getJSONArray(Constant.PRODUCT_REVIEW);

                        for (int i = 0; i < (Math.min(jsonArrayReviews.length(), 5)); i++) {
                            Review review = new Gson().fromJson(jsonArrayReviews.getJSONObject(i).toString(), Review.class);
                            reviewArrayList.add(review);
                        }
                        reviewAdapter = new ReviewAdapter(activity, reviewArrayList);
                        recyclerViewReview.setAdapter(reviewAdapter);
                    } else {
                        lytReview.setVisibility(View.GONE);
                    }
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                } catch (JSONException e) {
                    scrollView.setVisibility(View.VISIBLE);
                    mShimmerViewContainer.setVisibility(View.GONE);
                    mShimmerViewContainer.stopShimmer();
                }
            }
        }, activity, Constant.GET_ALL_PRODUCTS_URL, params, false);
    }


    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    void SetProductDetails(final Product product) {
        try {

            Variants variants = product.getVariants().get(variantPosition);

            tvProductName.setText(product.getName());
            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            ratingProduct_ = root.findViewById(R.id.ratingProduct_);
            ratingProduct = root.findViewById(R.id.ratingProduct);
            tvRatingProductCount = root.findViewById(R.id.tvRatingProductCount);
            tvRatingCount = root.findViewById(R.id.tvRatingCount);
            tvReviewDetail = root.findViewById(R.id.tvReviewDetail);
            tvMoreReview = root.findViewById(R.id.tvMoreReview);

            if (session.getData(Constant.ratings).equals("1")) {
                ratingProduct_.setRating(Float.parseFloat(product.getRatings()));
                ratingProduct.setRating(Float.parseFloat(product.getRatings()));

                tvRatingProductCount.setText(product.getNumber_of_ratings());
                tvRatingCount.setText(product.getRatings() + getString(R.string.out_of_5));

                tvReviewDetail.setText(product.getNumber_of_ratings() + getString(R.string.global_ratings));
            }

            ArrayList<String> arrayList = product.getOther_images();

            sliderArrayList.add(new Slider(product.getImage()));

            if (product.getMade_in().length() > 0) {
                lytMadeIn.setVisibility(View.VISIBLE);
                tvMadeIn.setText(product.getMade_in());
            }

            if (product.getManufacturer().length() > 0) {
                lytMfg.setVisibility(View.VISIBLE);
                tvMfg.setText(product.getManufacturer());
            }

            if (isLogin) {
                if (product.getIs_favorite()) {
                    isFavorite = true;
                    imgFav.setImageResource(R.drawable.ic_is_favorite);
                } else {
                    isFavorite = false;
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                }
            } else {
                if (databaseHelper.getFavoriteById(product.getId())) {
                    imgFav.setImageResource(R.drawable.ic_is_favorite);
                } else {
                    imgFav.setImageResource(R.drawable.ic_is_not_favorite);
                }
            }

            if (isLogin) {
                if (Constant.CartValues.containsKey(variants.getId())) {
                    tvQuantity.setText("" + Constant.CartValues.get(variants.getId()));
                } else {
                    tvQuantity.setText(variants.getCart_count());
                }
            } else {
                tvQuantity.setText(databaseHelper.CheckCartItemExist(variants.getId(), variants.getProduct_id()));
            }

            if (product.getReturn_status().equalsIgnoreCase("1")) {
                imgReturnable.setImageResource(R.drawable.ic_returnable);
                tvReturnable.setText(Integer.parseInt(session.getData(Constant.max_product_return_days)) + " Days Returnable.");
            } else {
                imgReturnable.setImageResource(R.drawable.ic_not_returnable);
                tvReturnable.setText("Not Returnable.");
            }

            if (product.getCancelable_status().equalsIgnoreCase("1")) {
                imgCancellable.setImageResource(R.drawable.ic_cancellable);
                tvCancellable.setText("Order Can Cancel Till Order " + ApiConfig.toTitleCase(product.getTill_status()) + ".");
            } else {
                imgCancellable.setImageResource(R.drawable.ic_not_cancellable);
                tvCancellable.setText("Non Cancellable.");
            }


            for (int i = 0; i < arrayList.size(); i++) {
                sliderArrayList.add(new Slider(arrayList.get(i)));
            }

            if (product.getSize_chart() != null && !product.getSize_chart().equals("")) {
                sliderArrayList.add(new Slider(product.getSize_chart()));
            }

            viewPager.setAdapter(new SliderAdapter(sliderArrayList, activity, R.layout.lyt_detail_slider, "detail"));
            ApiConfig.addMarkers(0, sliderArrayList, mMarkersLayout, activity);


            if (product.getVariants().size() == 1) {
                spinner.setVisibility(View.INVISIBLE);
                lytSpinner.setVisibility(View.INVISIBLE);
                lytMainPrice.setEnabled(false);
                session.setData(Constant.PRODUCT_VARIANT_ID, "" + 0);
                SetSelectedData(variants);
            }

            if (!product.getIndicator().equals("0")) {
                imgIndicator.setVisibility(View.VISIBLE);
                if (product.getIndicator().equals("1"))
                    imgIndicator.setImageResource(R.drawable.ic_veg_icon);
                else if (product.getIndicator().equals("2"))
                    imgIndicator.setImageResource(R.drawable.ic_non_veg_icon);
            }
            CustomAdapter customAdapter = new CustomAdapter();
            spinner.setAdapter(customAdapter);

            webDescription.setVerticalScrollBarEnabled(true);
            webDescription.loadDataWithBaseURL("", product.getDescription(), "text/html", "UTF-8", "");
            webDescription.setBackgroundColor(ContextCompat.getColor(activity, R.color.white));
            tvProductName.setText(product.getName());

            spinner.setSelection(variantPosition);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                }

                @Override
                public void onPageSelected(int position) {
                    ApiConfig.addMarkers(position, sliderArrayList, mMarkersLayout, activity);
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                }
            });

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    variantPosition = i;
                    session.setData(Constant.PRODUCT_VARIANT_ID, "" + i);
                    SetSelectedData(product.getVariants().get(i));
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
            if (session.getData(Constant.ratings).equals("1")) {
                GetReviews(id);
            } else {
                scrollView.setVisibility(View.VISIBLE);
                mShimmerViewContainer.setVisibility(View.GONE);
                mShimmerViewContainer.stopShimmer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Constant.TOOLBAR_TITLE = getString(R.string.app_name);
        activity.invalidateOptionsMenu();
        hideKeyboard();
    }

    public void hideKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            assert inputMethodManager != null;
            inputMethodManager.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressLint("SetTextI18n")
    public void SetSelectedData(Variants variants) {
        try {

            tvMeasurement.setText(" ( " + variants.getMeasurement() + variants.getMeasurement_unit_name() + " ) ");

            imgMinus.setOnClickListener(view -> {
                if (ApiConfig.isConnected(activity)) {
                    Constant.CLICK = true;
                    count = Integer.parseInt(tvQuantity.getText().toString());
                    if (!(count <= 0)) {
                        count--;
                        if (count == 0) {
                            btnAddToCart.setVisibility(View.VISIBLE);
                        }
                        tvQuantity.setText("" + count);
                        if (isLogin) {
                            if (Constant.CartValues.containsKey(product.getVariants().get(variantPosition).getId())) {
                                Constant.CartValues.replace(product.getVariants().get(variantPosition).getId(), "" + count);
                            } else {
                                Constant.CartValues.put(product.getVariants().get(variantPosition).getId(), "" + count);
                            }

                            ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                        } else {
                            databaseHelper.AddToCart(product.getVariants().get(variantPosition).getId(), variants.getProduct_id(), "" + count);
                        }
                        NotifyData(count);

                    }
                }

            });

            imgAdd.setOnClickListener(view -> {
                if (ApiConfig.isConnected(activity)) {
                    count = Integer.parseInt(tvQuantity.getText().toString());
                    if (!(count >= Float.parseFloat(product.getVariants().get(variantPosition).getStock()))) {
                        if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                            Constant.CLICK = true;
                            count++;
                            tvQuantity.setText("" + count);
                            if (isLogin) {
                                if (Constant.CartValues.containsKey(product.getVariants().get(variantPosition).getId())) {
                                    Constant.CartValues.replace(product.getVariants().get(variantPosition).getId(), "" + count);
                                } else {
                                    Constant.CartValues.put(product.getVariants().get(variantPosition).getId(), "" + count);
                                }
                                ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                            } else {
                                databaseHelper.AddToCart(product.getVariants().get(variantPosition).getId(), variants.getProduct_id(), "" + count);
                            }
                        } else {
                            Toast.makeText(activity, getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                    }
                    NotifyData(count);
                }
            });

            btnAddToCart.setOnClickListener(v -> {
                if (ApiConfig.isConnected(activity)) {
                    count = 0;
                    if (!(count >= Float.parseFloat(product.getVariants().get(variantPosition).getStock()))) {
                        if (count < Integer.parseInt(session.getData(Constant.max_cart_items_count))) {
                            Constant.CLICK = true;
                            count++;
                            tvQuantity.setText("" + count);
                            if (isLogin) {
                                if (Constant.CartValues.containsKey(product.getVariants().get(variantPosition).getId())) {
                                    Constant.CartValues.replace(product.getVariants().get(variantPosition).getId(), "" + count);
                                } else {
                                    Constant.CartValues.put(product.getVariants().get(variantPosition).getId(), "" + count);
                                }
                                ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
                            } else {
                                databaseHelper.AddToCart(product.getVariants().get(variantPosition).getId(), variants.getProduct_id(), "" + count);
                            }
                            btnAddToCart.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(activity, getString(R.string.limit_alert), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(activity, getString(R.string.stock_limit), Toast.LENGTH_SHORT).show();
                    }
                    NotifyData(count);
                }
            });

            if (isLogin) {
                if (variants.getCart_count().equals("0")) {
                    btnAddToCart.setVisibility(View.VISIBLE);
                } else {
                    btnAddToCart.setVisibility(View.GONE);
                }
            } else {
                if (!databaseHelper.CheckCartItemExist(variants.getId(), variants.getProduct_id()).equals("0") || databaseHelper.CheckCartItemExist(variants.getId(), variants.getProduct_id()) == null) {
                    btnAddToCart.setVisibility(View.GONE);
                } else {
                    btnAddToCart.setVisibility(View.VISIBLE);
                }
            }

            double OriginalPrice, DiscountedPrice;
            String taxPercentage = "0";
            try {
                taxPercentage = (Double.parseDouble(product.getTax_percentage()) > 0 ? product.getTax_percentage() : "0");
            } catch (Exception e) {
                e.printStackTrace();
            }

            DiscountedPrice = ((Float.parseFloat(variants.getDiscounted_price()) + ((Float.parseFloat(variants.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
            OriginalPrice = (Float.parseFloat(variants.getPrice()) + ((Float.parseFloat(variants.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

            if (variants.getIs_flash_sales().equalsIgnoreCase("false")) {
                lytTimer.setVisibility(View.GONE);
                DiscountedPrice = ((Float.parseFloat(variants.getDiscounted_price()) + ((Float.parseFloat(variants.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                OriginalPrice = (Float.parseFloat(variants.getPrice()) + ((Float.parseFloat(variants.getPrice()) * Float.parseFloat(taxPercentage)) / 100));

                if (variants.getDiscounted_price().equals("0") || variants.getDiscounted_price().equals("")) {
                    showDiscount.setVisibility(View.GONE);
                    tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                } else {
                    showDiscount.setVisibility(View.VISIBLE);

                }
            } else {
                lytTimer.setVisibility(View.VISIBLE);
//                Variants variants = product.getVariants().get(0);
                FlashSale flashSale = variants.getFlash_sales().get(0);

                Date startDate, endDate;
                long different;

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                if (flashSale.isIs_start()) {

                    startDate = df.parse(session.getData(Constant.current_date));
                    endDate = df.parse(flashSale.getEnd_date());
                    different = (endDate != null ? endDate.getTime() : 0) - (startDate != null ? startDate.getTime() : 0);
                    long days = (different / (60 * 60 * 24 * 1000));
                    if (Utils.setFormatTime(days).equalsIgnoreCase("00")) {
                        StartTimer(variants,different);
                    }else {
                        tvTimer.setText(ApiConfig.CalculateDays(activity, days));
                    }
                    tvTimerTitle.setText(activity.getString(R.string.ends_in));
                    if (flashSale.getDiscounted_price().equals("0") || flashSale.getDiscounted_price().equals("")) {
                        showDiscount.setVisibility(View.GONE);
                        tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    } else {
                        showDiscount.setVisibility(View.VISIBLE);
                        DiscountedPrice = ((Float.parseFloat(flashSale.getDiscounted_price()) + ((Float.parseFloat(flashSale.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                        OriginalPrice = (Float.parseFloat(flashSale.getPrice()) + ((Float.parseFloat(flashSale.getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                        tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                        tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                    }

                } else {
                    startDate = df.parse(session.getData(Constant.current_date));
                    endDate = df.parse(flashSale.getStart_date());
                    different = (endDate != null ? endDate.getTime() : 0) - (startDate != null ? startDate.getTime() : 0);
                    long days = (different / (60 * 60 * 24 * 1000));

                    if (Utils.setFormatTime(days).equalsIgnoreCase("00")) {
                        StartTimer(variants,different);
                    }else {
                        tvTimer.setText(ApiConfig.CalculateDays(activity, days));
                    }
                    tvTimerTitle.setText(activity.getString(R.string.starts_in));
                    tvTimer.setText(ApiConfig.CalculateDays(activity, days));
                    if (variants.getDiscounted_price().equals("0") || variants.getDiscounted_price().equals("")) {
                        showDiscount.setVisibility(View.GONE);
                        tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                    } else {
                        showDiscount.setVisibility(View.VISIBLE);
                        DiscountedPrice = ((Float.parseFloat(variants.getDiscounted_price()) + ((Float.parseFloat(variants.getDiscounted_price()) * Float.parseFloat(taxPercentage)) / 100)));
                        OriginalPrice = (Float.parseFloat(variants.getPrice()) + ((Float.parseFloat(variants.getPrice()) * Float.parseFloat(taxPercentage)) / 100));
                        tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
                        tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
                    }
                }
                showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));

            }

            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvOriginalPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + OriginalPrice));
            tvPrice.setText(session.getData(Constant.currency) + ApiConfig.StringFormat("" + DiscountedPrice));
            showDiscount.setText("-" + ApiConfig.GetDiscount(OriginalPrice, DiscountedPrice));

            if (isLogin) {
                //            System.out.println("priceVariation.getId()) : " + Constant.CartValues);
                if (Constant.CartValues.containsKey(variants.getId())) {
                    tvQuantity.setText(Constant.CartValues.get(variants.getId()));
                } else {
                    tvQuantity.setText(variants.getCart_count());
                }
            } else {
                tvQuantity.setText(databaseHelper.CheckCartItemExist(variants.getId(), variants.getProduct_id()));
            }

            if (variants.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                tvStatus.setVisibility(View.VISIBLE);
                lytQuantity.setVisibility(View.GONE);
            } else {
                tvStatus.setVisibility(View.GONE);
                lytQuantity.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.toolbar_layout).setVisible(false);
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.toolbar_cart).setVisible(true);
        menu.findItem(R.id.toolbar_sort).setVisible(false);
        menu.findItem(R.id.toolbar_search).setVisible(true);
        menu.findItem(R.id.toolbar_cart).setIcon(ApiConfig.buildCounterDrawable(Constant.TOTAL_CART_ITEM, activity));
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();
        ApiConfig.AddMultipleProductInCart(session, activity, Constant.CartValues);
    }

    public class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return product.getVariants().size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"ViewHolder", "SetTextI18n", "InflateParams"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.lyt_spinner_item, null);
            TextView measurement = view.findViewById(R.id.tvMeasurement);

            Variants variants = product.getVariants().get(i);
            measurement.setText(variants.getMeasurement() + " " + variants.getMeasurement_unit_name());

            if (variants.getServe_for().equalsIgnoreCase(Constant.SOLD_OUT_TEXT)) {
                measurement.setTextColor(ContextCompat.getColor(activity, R.color.red));
            } else {
                measurement.setTextColor(ContextCompat.getColor(activity, R.color.black));
            }

            return view;
        }
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n", "SimpleDateFormat"})
    public void StartTimer(Variants variants, long duration) {
        try{

            new CountDownTimer(duration, 1000) {
                @Override
                public void onTick(long different) {
                    int seconds = (int) (different / 1000) % 60;
                    int minutes = (int) ((different / (1000 * 60)) % 60);
                    int hours = (int) ((different / (1000 * 60 * 60)) % 24);

                    tvTimer.setText(Utils.setFormatTime(hours) + ":" + Utils.setFormatTime(minutes) + ":" + Utils.setFormatTime(seconds));

                }

                @Override
                public void onFinish() {
                    if(!variants.getFlash_sales().get(0).isIs_start()){
                        variants.getFlash_sales().get(0).setIs_start(true);
                        variants.setIs_flash_sales("true");
                    }else{
                        variants.setIs_flash_sales("false");
                    }
                    SetSelectedData(variants);
                }
            }.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}