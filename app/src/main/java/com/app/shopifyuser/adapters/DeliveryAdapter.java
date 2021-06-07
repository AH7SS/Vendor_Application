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
import com.app.shopifyuser.Utils.FullScreenImagesUtil;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DeliveryAdapter extends RecyclerView.Adapter<DeliveryAdapter.MyViewHolder> {

    private final Context context;
    private final ArrayList<DeliveryOrder> deliveryOrders;
    private final CollectionReference usersRef;
    private final DeliveryStatusListener deliveryStatusListener;

    public interface DeliveryStatusListener {

        void setStatusPickedUp(int position);

        void setStatusDelivered(int position);

    }

    public DeliveryAdapter(Context context, ArrayList<DeliveryOrder> deliveryOrders, DeliveryStatusListener deliveryStatusListener) {
        this.context = context;
        this.deliveryOrders = deliveryOrders;
        this.deliveryStatusListener = deliveryStatusListener;
        usersRef = FirebaseFirestore.getInstance().collection("users");
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_delivery, parent, false));
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

        private final ImageView userIv, showLocationIv;
        private final TextView nameTv, orderedAtTv, scheduledForTv, totalCostTv;
        private final Button showCartBtn, pickUpBtn, deliveredBtn;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            userIv = itemView.findViewById(R.id.userIv);
            showLocationIv = itemView.findViewById(R.id.showLocationIv);
            nameTv = itemView.findViewById(R.id.nameTv);
            orderedAtTv = itemView.findViewById(R.id.orderedAtTv);
            scheduledForTv = itemView.findViewById(R.id.scheduledForTv);
            totalCostTv = itemView.findViewById(R.id.totalCostTv);
            showCartBtn = itemView.findViewById(R.id.showCartBtn);
            pickUpBtn = itemView.findViewById(R.id.pickUpBtn);
            deliveredBtn = itemView.findViewById(R.id.deliveredBtn);
        }

        private void bind(DeliveryOrder deliveryOrder) {

            if (deliveryOrder.getUserName() == null) {
                getUserInfo(deliveryOrder.getToUser(), getAdapterPosition(), this);
            } else {
                Picasso.get().load(deliveryOrder.getUserImageUrl()).fit().centerCrop().into(userIv);
                nameTv.setText(deliveryOrder.getUserName());
            }

            orderedAtTv.setText(TimeFormatter.formatTime(deliveryOrder.getOrderedAt()));
            scheduledForTv.setText(TimeFormatter.formatTime(deliveryOrder.getScheduledTime()));
            totalCostTv.setText("Total cost: " + deliveryOrder.getTotalPrice() + "SAR");

            showLocationIv.setOnClickListener(this);
            showCartBtn.setOnClickListener(this);
            pickUpBtn.setOnClickListener(this);
            deliveredBtn.setOnClickListener(this);
            userIv.setOnClickListener(this);

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


            } else if (v.getId() == pickUpBtn.getId()) {

                deliveryStatusListener.setStatusPickedUp(getAdapterPosition());


            } else if (v.getId() == deliveredBtn.getId()) {

                deliveryStatusListener.setStatusDelivered(getAdapterPosition());

            } else if (v.getId() == userIv.getId()) {


                if (deliveryOrders.get(getAdapterPosition()).getUserImageUrl() != null) {

                    FullScreenImagesUtil.showImageFullScreen(context,
                            deliveryOrders.get(getAdapterPosition()).getUserImageUrl(), null);

                }

            }

        }
    }


    private void getUserInfo(String userId, int pos, MyViewHolder holder) {

        usersRef.document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {

                        if (snapshot.exists()) {

                            final String imageUrl = snapshot.getString("imageUrl");
                            final String username = snapshot.getString("username");


                            deliveryOrders.get(pos).setUserImageUrl(imageUrl);
                            deliveryOrders.get(pos).setUserName(username);

                            holder.nameTv.setText(username);
                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl).fit().centerCrop().into(holder.userIv);
                            }

                        }

                    }
                });


    }

}
