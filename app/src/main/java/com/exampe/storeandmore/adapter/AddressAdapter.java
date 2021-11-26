package com.exampe.storeandmore.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import com.exampe.storeandmore.R;
import com.exampe.storeandmore.activity.MainActivity;
import com.exampe.storeandmore.fragment.AddressAddUpdateFragment;
import com.exampe.storeandmore.fragment.AddressListFragment;
import com.exampe.storeandmore.helper.ApiConfig;
import com.exampe.storeandmore.helper.Constant;
import com.exampe.storeandmore.helper.Session;
import com.exampe.storeandmore.model.Address;

public class AddressAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final Activity activity;
    final Context context;
    final ArrayList<Address> addresses;
    String id = "0";

    public AddressAdapter(Context context, Activity activity, ArrayList<Address> addresses) {
        this.context = context;
        this.activity = activity;
        this.addresses = addresses;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, final int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.lyt_address_list, parent, false);
        return new AddressItemHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NotNull RecyclerView.ViewHolder holderParent, final int position) {
        final AddressItemHolder holder = (AddressItemHolder) holderParent;
        final Address address = addresses.get(position);
        id = address.getId();

        holder.setIsRecyclable(false);

        if (Constant.selectedAddressId.equals(id)) {
            new Session(activity).setData(Constant.LONGITUDE, address.getLongitude());
            new Session(activity).setData(Constant.LATITUDE, address.getLatitude());

            AddressListFragment.selectedAddress = address.getAddress() + ", " + address.getLandmark() + ", " + address.getCity_name() + ", " + address.getArea_name() + ", " + address.getState() + ", " + address.getCountry() + ", " + activity.getString(R.string.pincode_) + address.getPincode();
            Constant.DefaultPinCode = address.getPincode();
            Constant.DefaultCity = address.getCity_name();
            holder.tvName.setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary));

            holder.tvAddressType.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.right_btn_bg, null));
            holder.tvDefaultAddress.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.right_btn_bg, null));

            holder.imgSelect.setImageResource(R.drawable.ic_check_circle);
            holder.lytMain.setBackgroundResource(R.drawable.selected_shadow);

            if (new Session(activity).getData(Constant.area_wise_delivery_charge).equals("1")) {
                Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY = Double.parseDouble(address.getMinimum_free_delivery_order_amount());
                Constant.SETTING_DELIVERY_CHARGE = Double.parseDouble(address.getDelivery_charges());
            }

        } else {
            holder.tvName.setTextColor(ContextCompat.getColor(activity, R.color.gray));
            holder.tvAddressType.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.left_btn_bg, null));
            holder.tvDefaultAddress.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.left_btn_bg, null));
            holder.imgSelect.setImageResource(R.drawable.ic_uncheck_circle);
            holder.lytMain.setBackgroundResource(R.drawable.address_card_shadow);
        }

        holder.tvAddress.setText(address.getAddress() + ", " + address.getLandmark() + ", " + address.getCity_name() + ", " + address.getArea_name() + ", " + address.getState() + ", " + address.getCountry() + ", " + activity.getString(R.string.pincode_) + address.getPincode());
        Constant.DefaultPinCode = address.getPincode();
        Constant.DefaultCity = address.getCity_name();
        if (address.getIs_default().equals("1")) {
            holder.tvDefaultAddress.setVisibility(View.VISIBLE);
        }

        holder.lytMain.setPadding((int) activity.getResources().getDimension(R.dimen.dimen_15dp), (int) activity.getResources().getDimension(R.dimen.dimen_15dp), (int) activity.getResources().getDimension(R.dimen.dimen_15dp), (int) activity.getResources().getDimension(R.dimen.dimen_15dp));
        holder.tvName.setText(address.getName());
        if (!address.getType().equalsIgnoreCase("")) {
            holder.tvAddressType.setText(address.getType());
        }

        holder.tvMobile.setText(address.getMobile());

        holder.imgDelete.setOnClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getResources().getString(R.string.delete_address));
            builder.setIcon(R.drawable.ic_delete1);
            builder.setMessage(activity.getResources().getString(R.string.delete_address_msg));

            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.remove), (dialog, which) -> {
                if (ApiConfig.isConnected(activity)) {
                    addresses.remove(address);
                    notifyItemRemoved(position);
                    ApiConfig.removeAddress(activity, address.getId());
                }
                if (addresses.size() == 0) {
                    AddressListFragment.selectedAddress = "";
                    AddressListFragment.tvAlert.setVisibility(View.VISIBLE);
                } else {
                    AddressListFragment.tvAlert.setVisibility(View.GONE);
                }
            });

            builder.setNegativeButton(activity.getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();

        });

        holder.lytMain.setOnClickListener(v -> {
            Constant.selectedAddressId = address.getId();
            new Session(activity).setData(Constant.LONGITUDE, address.getLongitude());
            new Session(activity).setData(Constant.LATITUDE, address.getLatitude());
            if (new Session(context).getData(Constant.area_wise_delivery_charge).equals("1")) {
                Constant.SETTING_MINIMUM_AMOUNT_FOR_FREE_DELIVERY = Double.parseDouble(address.getMinimum_free_delivery_order_amount());
                Constant.SETTING_DELIVERY_CHARGE = Double.parseDouble(address.getDelivery_charges());
            }
            notifyDataSetChanged();
        });

        holder.imgEdit.setOnClickListener(view -> {
            if (ApiConfig.isConnected(activity)) {
                Fragment fragment = new AddressAddUpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("model", address);
                bundle.putString("for", "update");
                bundle.putInt("position", position);
                fragment.setArguments(bundle);
                MainActivity.fm.beginTransaction().add(R.id.container, fragment).addToBackStack(null).commit();
            }
        });


    }

    @Override
    public int getItemCount() {
        return addresses.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    static class AddressItemHolder extends RecyclerView.ViewHolder {

        final TextView tvName;
        final TextView tvAddress;
        final TextView tvAddressType;
        final TextView tvMobile;
        final TextView tvDefaultAddress;
        final ImageView imgEdit;
        final ImageView imgDelete;
        final ImageView imgSelect;
        final LinearLayout lytMain;

        public AddressItemHolder(@NonNull View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);
            tvName = itemView.findViewById(R.id.tvName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvAddressType = itemView.findViewById(R.id.tvAddressType);
            tvMobile = itemView.findViewById(R.id.tvMobile);
            tvDefaultAddress = itemView.findViewById(R.id.tvDefaultAddress);

            imgEdit = itemView.findViewById(R.id.imgEdit);
            imgSelect = itemView.findViewById(R.id.imgSelect);
            imgDelete = itemView.findViewById(R.id.imgDelete);


        }
    }
}