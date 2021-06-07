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
import com.app.shopifyuser.adapters.ActiveOrdersAdapter;
import com.app.shopifyuser.model.DeliveryOrder;
import com.app.shopifyuser.shared.LocalSave;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActiveOrdersFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        ActiveOrdersAdapter.CancelOrderListener {

    private static final int ORDERS_PAGE_LIMIT = 10;



    private ArrayList<DeliveryOrder> deliveryOrders;
    private ActiveOrdersAdapter activeOrdersAdapter;
    private ScrollListener scrollListener;



    private SweetAlertDialog sweetAlertDialog;
    private RecyclerView activeOrdersRv;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Query deliveryQuery;
    private DocumentSnapshot lastDocSnap;
    private boolean isLoadingDeliveryItems;


    public ActiveOrdersFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        sweetAlertDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        sweetAlertDialog.setCancelable(false);
        sweetAlertDialog.show();

        int currentUserId = LocalSave.getInstance(requireContext()).getCurrentUser().getId();

        deliveryOrders = new ArrayList<>();
        activeOrdersAdapter = new ActiveOrdersAdapter(requireContext(), deliveryOrders, this);

        final List<Integer> activeStates = new ArrayList<>(2);
        activeStates.add(DeliveryOrder.STATUS_PENDING);
        activeStates.add(DeliveryOrder.STATUS_PICKUP);

        deliveryQuery = FirebaseFirestore.getInstance().collection("Deliveries")
                .whereEqualTo("toUser", String.valueOf(currentUserId))
                .whereIn("status", activeStates)
                .limit(ORDERS_PAGE_LIMIT);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_orders, container, false);

        activeOrdersRv = view.findViewById(R.id.ordersRv);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        activeOrdersRv.setAdapter(activeOrdersAdapter);

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
    public void onRefresh() {

        deliveryOrders.clear();
        activeOrdersAdapter.notifyDataSetChanged();
        lastDocSnap = null;
        getMoreDeliveries(true);

    }

    @Override
    public void removeCartItem(int itemId, int position) {

    }
}