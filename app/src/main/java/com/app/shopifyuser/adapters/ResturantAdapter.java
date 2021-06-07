package com.app.shopifyuser.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopifyuser.R;
import com.app.shopifyuser.Utils.FullScreenImagesUtil;
import com.app.shopifyuser.model.Resturant;
import com.app.shopifyuser.user.MenuActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ResturantAdapter extends RecyclerView.Adapter<ResturantAdapter.MyViewHolder> {
    Context context;
    ArrayList<Resturant> resturants;

    public ResturantAdapter(Context context, ArrayList<Resturant> resturants) {
        this.context = context;
        this.resturants = resturants;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_resturant,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Resturant resturant = resturants.get(position);
        holder.tvResTime.setText(resturant.getTime());
        holder.tvResName.setText(resturant.getName());
        Picasso.get().load(resturant.getImage()).fit().centerCrop().into(holder.ivResImage);

        holder.ivResImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullScreenImagesUtil.showImageFullScreen(context, resturant.getImage(), null);
            }
        });


        holder.tvResCategory.setText(resturant.getCategory());
        holder.clResturant.setOnClickListener(v -> {
            context.startActivity(new Intent(context, MenuActivity.class).putExtra("id", resturant.getId()));
            Log.v("ttt", resturant.getId() + "");
        });
    }

    @Override
    public int getItemCount() {
        return resturants.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView ivResImage;
        TextView tvResName,tvResTime,tvResCategory;
        ConstraintLayout clResturant;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            ivResImage = itemView.findViewById(R.id.ivResturant);
            tvResName  = itemView.findViewById(R.id.tvResName);
            tvResCategory = itemView.findViewById(R.id.tvResCategory);
            tvResTime = itemView.findViewById(R.id.tvResTime);
            clResturant = itemView.findViewById(R.id.clResturant);
        }
    }
}
