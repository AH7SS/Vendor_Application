package com.app.shopifyuser.user;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.ActiveOrdersAdapter;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.shared.LocalSave;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ActiveOrdersAcitivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        ActiveOrdersAdapter.CancelOrderListener {


    private static final int ORDERS_PAGE_LIMIT = 10;
    private int currentUserId;



    private ArrayList<DeliveryOrder> deliveryOrders;
    private ActiveOrdersAdapter activeOrdersAdapter;
    private ScrollListener scrollListener;



    private SweetAlertDialog sweetAlertDialog;
    private Toolbar activeToolbar;
    private RecyclerView activeOrdersRv;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Query deliveryQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingDeliveryItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_orders);


        initViews();
        initItems();
        initClicks();


    }

    private void initViews() {
        activeToolbar = findViewById(R.id.activeToolbar);
        activeOrdersRv = findViewById(R.id.activeOrdersRv);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    }


    private void initItems() {

        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        currentUserId = LocalSave.getInstance(this).getCurrentUser().getId();

        deliveryOrders = new ArrayList<>();
        activeOrdersAdapter = new ActiveOrdersAdapter(this, deliveryOrders, this);
        activeOrdersRv.setAdapter(activeOrdersAdapter);

        final List<Integer> activeStates = new ArrayList<>(2);
        activeStates.add(DeliveryOrder.STATUS_PENDING);
        activeStates.add(DeliveryOrder.STATUS_PICKUP);

        deliveryQuery = FirebaseFirestore.getInstance().collection("Deliveries")
                .whereEqualTo("toUser", String.valueOf(currentUserId))
                .whereIn("status", activeStates)
                .limit(ORDERS_PAGE_LIMIT);

        getMoreDeliveries(true);


    }

    private void getMoreDeliveries(boolean isInitial) {

        isLoadingDeliveryItems = true;

        swipeRefreshLayout.setRefreshing(true);

        Query updatedQuery = deliveryQuery;

        if (lastDocSnap != null) {
            updatedQuery = deliveryQuery.startAfter(lastDocSnap);
        }

        updatedQuery.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {

                lastDocSnap = queryDocumentSnapshots.getDocuments().get(
                        queryDocumentSnapshots.size() - 1
                );

                if (isInitial) {
                    deliveryOrders.addAll(queryDocumentSnapshots.toObjects(DeliveryOrder.class));
                } else {
                    deliveryOrders.addAll(deliveryOrders.size(),
                            queryDocumentSnapshots.toObjects(DeliveryOrder.class));
                }
            }
        }).addOnCompleteListener(task -> {
            if (isInitial) {

                activeOrdersAdapter.notifyDataSetChanged();


                if (task.getResult().size() == ORDERS_PAGE_LIMIT && scrollListener == null) {
                    activeOrdersRv.addOnScrollListener(scrollListener = new ScrollListener());
                }

            } else {

                final int resultSize = task.getResult().size();

                activeOrdersAdapter.notifyItemRangeInserted(
                        deliveryOrders.size() - resultSize, resultSize);
                if (resultSize < ORDERS_PAGE_LIMIT && scrollListener != null) {
                    activeOrdersRv.removeOnScrollListener(scrollListener);
                }
            }

            sweetAlertDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);

            isLoadingDeliveryItems = false;
        });


    }

    @Override
    public void removeCartItem(int itemId, int position) {
    }

    private class ScrollListener extends RecyclerView.OnScrollListener {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (!isLoadingDeliveryItems &&
                    !recyclerView.canScrollVertically(1) &&
                    newState == RecyclerView.SCROLL_STATE_IDLE) {

                getMoreDeliveries(false);

            }
        }
    }


    private void initClicks() {
        activeToolbar.setNavigationOnClickListener(v -> finish());
        swipeRefreshLayout.setOnRefreshListener(this);
    }


    @Override
    public void onRefresh() {

        deliveryOrders.clear();
        activeOrdersAdapter.notifyDataSetChanged();
        lastDocSnap = null;
        getMoreDeliveries(true);

    }
}