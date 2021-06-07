package com.app.shopifyuser.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopifyuser.R;
import com.app.shopifyuser.Utils.FullScreenImagesUtil;
import com.app.shopifyuser.model.Menu;
import com.app.shopifyuser.user.OrderActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MyViewHolder> {
    Context context;
    ArrayList<Menu> menus;

    public MenuAdapter(Context context, ArrayList<Menu> menus) {
        this.context = context;
        this.menus = menus;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_menu,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Menu menu = menus.get(position);

        Picasso.get().load(menu.getImage()).fit().centerCrop().into(holder.ivMenuImage);

        holder.ivMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenImagesUtil.showImageFullScreen(context, menu.getImage(), null);
            }
        });

        holder.tvMenuName.setText(menu.getName());
        holder.tvMenuPrice.setText(menu.getPrice() + "SAR");
        holder.btnMenuOrder.setOnClickListener(v -> {
            context.startActivity(new Intent(context, OrderActivity.class).putExtra("position", position));
        });
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuImage;
        TextView tvMenuName,tvMenuPrice;
        AppCompatButton btnMenuOrder;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMenuImage = itemView.findViewById(R.id.ivMenImage);
            tvMenuName  = itemView.findViewById(R.id.tvMenuName);
            tvMenuPrice = itemView.findViewById(R.id.tvMenuPrice);
            btnMenuOrder = itemView.findViewById(R.id.btnMenuOrder);
        }
    }
}
