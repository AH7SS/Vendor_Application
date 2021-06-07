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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;

public class DeliveryReceiptsAdapter extends RecyclerView.Adapter<DeliveryReceiptsAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<DeliveryOrder> deliveryOrders;
    private final CollectionReference usersRef;


    public DeliveryReceiptsAdapter(Context context, ArrayList<DeliveryOrder> deliveryOrders) {
        this.context = context;
        this.deliveryOrders = deliveryOrders;
        usersRef = FirebaseFirestore.getInstance().collection("users");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_delivery_receipt,
                parent, false));
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

        private final ImageView showLocationIv;
        private final TextView nameTv, orderedAtTv, scheduledForTv, deliveredAtTv, totalCostTv;
        private final Button showCartBtn;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            showLocationIv = itemView.findViewById(R.id.showLocationIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            orderedAtTv = itemView.findViewById(R.id.orderedAtTv);
            scheduledForTv = itemView.findViewById(R.id.scheduledForTv);
            deliveredAtTv = itemView.findViewById(R.id.deliveredAtTv);
            totalCostTv = itemView.findViewById(R.id.totalCostTv);
            showCartBtn = itemView.findViewById(R.id.showCartBtn);
        }

        private void bind(DeliveryOrder deliveryOrder) {

            if (deliveryOrder.getUserName() == null) {
                getUserInfo(deliveryOrder.getToUser(), getAdapterPosition(), this);
            } else {
                nameTv.setText("Delivered to: " + deliveryOrder.getUserName());
            }

            orderedAtTv.setText("Ordered at: " +
                    TimeFormatter.formatWithPattern(deliveryOrder.getOrderedAt(),
                            TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

            scheduledForTv.setText("Scheduled for: " +
                    TimeFormatter.formatWithPattern(deliveryOrder.getScheduledTime(),
                            TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

            deliveredAtTv.setText("Delivered at: " +
                    TimeFormatter.formatWithPattern(deliveryOrder.getDeliveredAt(),
                            TimeFormatter.MONTH_DAY_YEAR_HOUR_MINUTE));

            totalCostTv.setText("Total cost: " + deliveryOrder.getTotalPrice() + "SAR");

            showLocationIv.setOnClickListener(this);
            showCartBtn.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            if (v.getId() == showLocationIv.getId()) {

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


    private void getUserInfo(String userId, int pos, MyViewHolder holder) {

        usersRef.document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if (snapshot.exists()) {
                            final String username = snapshot.getString("username");
                            deliveryOrders.get(pos).setUserName(username);
                            holder.nameTv.setText("Delivered to: " + username);
                        }
                    }
                });


    }

}
