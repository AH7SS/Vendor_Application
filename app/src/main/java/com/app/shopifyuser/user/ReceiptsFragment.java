package com.app.shopifyuser.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.shopifyuser.R;
import com.app.shopifyuser.adapters.DeliveryReceiptsAdapter;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.shared.LocalSave;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ReceiptsFragment extends Fragment implements
        SwipeRefreshLayout.OnRefreshListener {

    private int currentUserId;

    private static final int RECEIPTS_PAGE_LIMIT = 10;


    private ArrayList<DeliveryOrder> deliveryOrders;
    private DeliveryReceiptsAdapter deliveryReceiptsAdapter;
    private ScrollListener scrollListener;


    private SweetAlertDialog sweetAlertDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView ordersRv;


    private Query deliveryQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingDeliveryItems;
    private CollectionReference deliveriesRef;


    public ReceiptsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();


        currentUserId = LocalSave.getInstance(requireContext()).getCurrentUser().getId();

        deliveriesRef = FirebaseFirestore.getInstance().collection("Deliveries");

        deliveryOrders = new ArrayList<>();
        deliveryReceiptsAdapter = new DeliveryReceiptsAdapter(requireContext(), deliveryOrders);


        deliveryQuery =
                deliveriesRef.whereEqualTo("toUser", String.valueOf(currentUserId))
                        .whereEqualTo("status", DeliveryOrder.STATUS_DELIVERED)
                        .limit(RECEIPTS_PAGE_LIMIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_orders, container, false);

        ordersRv = view.findViewById(R.id.ordersRv);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        ordersRv.setAdapter(deliveryReceiptsAdapter);

        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                    ordersRv.addOnScrollListener(scrollListener = new ScrollListener());
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
                    ordersRv.removeOnScrollListener(scrollListener);
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
}