package com.exampe.storeandmore.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.adapter.FlashSaleAdapter;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.model.Product;


public class FlashSaleFragment extends Fragment {
    View root;
    JSONObject jsonObject;
    RecyclerView recyclerView;
    Activity activity;
    ArrayList<Product> productArrayList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            root = inflater.inflate(R.layout.fragment_flash_sale, container, false);
            recyclerView = root.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            activity = getActivity();
            productArrayList = new ArrayList<>();

            assert getArguments() != null;
            jsonObject = new JSONObject(getArguments().getString("data"));
            JSONArray jsonArray = jsonObject.getJSONArray(Constant.PRODUCTS);
            for (int i = 0; i < jsonArray.length(); i++) {
                Product product = new Gson().fromJson(jsonArray.get(i).toString(), Product.class);
                productArrayList.add(product);
            }

            FlashSaleAdapter flashSaleAdapter = new FlashSaleAdapter(activity, productArrayList);
            recyclerView.setAdapter(flashSaleAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static FlashSaleFragment AddFragment(JSONObject jsonObject) {
        FlashSaleFragment fragment = new FlashSaleFragment();
        Bundle args = new Bundle();
        args.putString("data", jsonObject.toString());
        fragment.setArguments(args);
        return fragment;
    }
}