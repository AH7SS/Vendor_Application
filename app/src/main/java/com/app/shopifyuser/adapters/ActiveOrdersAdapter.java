package com.app.shopifyuser.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.shopifyuser.R;
import com.app.shopifyuser.Utils.TimeFormatter;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.user.CartInfoActivity;
import com.app.shopifyuser.user.MapsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class ActiveOrdersAdapter extends RecyclerView.Adapter<ActiveOrdersAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<DeliveryOrder> deliveryOrders;
    private final CancelOrderListener cancelListener;

    public interface CancelOrderListener {
        void removeCartItem(int itemId, int position);
    }

    public ActiveOrdersAdapter(Context context, ArrayList<DeliveryOrder> deliveryOrders, CancelOrderListener cancelListener) {
        this.context = context;
        this.deliveryOrders = deliveryOrders;
        this.cancelListener = cancelListener;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_active_orders, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        final DeliveryOrder deliveryOrder = deliveryOrders.get(position);
        holder.bind(deliveryOrder);

    }

    @Override
    public int getItemCount() {
        return deliveryOrders.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView orderedAtTv, scheduledAtTv, totalCostTv;
        private final Button showCartBtn;
        private final ImageView viewLocationIv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            orderedAtTv = itemView.findViewById(R.id.orderedAtTv);
            scheduledAtTv = itemView.findViewById(R.id.scheduledAtTv);
            totalCostTv = itemView.findViewById(R.id.totalCostTv);
            showCartBtn = itemView.findViewById(R.id.showCartBtn);
            viewLocationIv = itemView.findViewById(R.id.viewLocationIv);
        }

        private void bind(DeliveryOrder deliveryOrder) {

            orderedAtTv.setText("Ordered at: " + TimeFormatter.formatTime(deliveryOrder.getOrderedAt()));
            scheduledAtTv.setText("Scheduled for: " + TimeFormatter.formatTime(deliveryOrder.getScheduledTime()));
            totalCostTv.setText("Total cost: " + deliveryOrder.getTotalPrice() + "SAR");


            viewLocationIv.setOnClickListener(this);
            showCartBtn.setOnClickListener(this);
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == viewLocationIv.getId()) {

                final GeoPoint geoPoint = deliveryOrders.get(getAdapterPosition()).getLocation();
                final Intent mapIntent = new Intent(context, MapsActivity.class);
                mapIntent.putExtra("mapType", MapsActivity.MAP_TYPE_MARK_LOCATION);
                mapIntent.putExtra("deliveryLatLng",
                        new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude()));
                context.startActivity(mapIntent);

            } else if (v.getId() == showCartBtn.getId()) {

                final Intent intent = new Intent(context, CartInfoActivity.class);
                intent.putExtra("cartId", deliveryOrders.get(getAdapterPosition()).getId());
                context.startActivity(intent);

            }

        }
    }

}
