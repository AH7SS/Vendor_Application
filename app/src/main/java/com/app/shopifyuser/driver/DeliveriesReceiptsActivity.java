package com.app.shopifyuser.driver;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.DeliveryReceiptsAdapter;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.shared.LocalSave;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class DeliveriesReceiptsActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener {

    private int currentUserId;

    private static final int RECEIPTS_PAGE_LIMIT = 10;


    private ArrayList<DeliveryOrder> deliveryOrders;
    private DeliveryReceiptsAdapter deliveryReceiptsAdapter;
    private ScrollListener scrollListener;


    private SweetAlertDialog sweetAlertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Toolbar receiptsToolbar;
    private RecyclerView deliveriesReceiptsRv;


    private Query deliveryQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingDeliveryItems;
    private CollectionReference deliveriesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliveries_receipts);

        initViews();
        initItem();
        initClicks();

    }

    private void initViews() {

        receiptsToolbar = findViewById(R.id.receiptsToolbar);
        deliveriesReceiptsRv = findViewById(R.id.deliveriesReceiptsRv);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

    }


    private void initClicks() {
        receiptsToolbar.setNavigationOnClickListener(v -> finish());
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void initItem() {

        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();


        currentUserId = LocalSave.getInstance(this).getCurrentUser().getId();


        deliveriesRef = FirebaseFirestore.getInstance().collection("Deliveries");

        deliveryOrders = new ArrayList<>();
        deliveryReceiptsAdapter = new DeliveryReceiptsAdapter(this, deliveryOrders);
        deliveriesReceiptsRv.setAdapter(deliveryReceiptsAdapter);

        deliveryQuery =
                deliveriesRef.whereEqualTo("byUser", String.valueOf(currentUserId))
                        .whereEqualTo("status", DeliveryOrder.STATUS_DELIVERED)
                        .limit(RECEIPTS_PAGE_LIMIT);



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
                        queryDocumentSnapshots.size() - 1);

                if (isInitial) {
                    deliveryOrders.addAll(queryDocumentSnapshots.toObjects(DeliveryOrder.class));
                } else {
                    deliveryOrders.addAll(deliveryOrders.size(),
                            queryDocumentSnapshots.toObjects(DeliveryOrder.class));
                }
            }
        }).addOnCompleteListener(task -> {
            if (isInitial) {

                deliveryReceiptsAdapter.notifyDataSetChanged();

                if (task.getResult().size() == RECEIPTS_PAGE_LIMIT && scrollListener == null) {
                    deliveriesReceiptsRv.addOnScrollListener(scrollListener = new ScrollListener());
                }

                if (deliveryOrders.isEmpty()) {
                    swipeRefreshLayout.setRefreshing(false);
                    sweetAlertDialog.dismiss();
                }

            } else {

                final int resultSize = task.getResult().size();

                deliveryReceiptsAdapter.notifyItemRangeInserted(
                        deliveryOrders.size() - resultSize, resultSize);
                if (resultSize < RECEIPTS_PAGE_LIMIT && scrollListener != null) {
                    deliveriesReceiptsRv.removeOnScrollListener(scrollListener);
                }
            }

            sweetAlertDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);

            isLoadingDeliveryItems = false;
        });


    }

    @Override
    public void onRefresh() {
        deliveryOrders.clear();
        deliveryReceiptsAdapter.notifyDataSetChanged();
        lastDocSnap = null;
        getMoreDeliveries(true);
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


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}