package com.example.prm_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.adapter.OrderHistoryAdapter;
import com.example.prm_project.model.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryFragment extends Fragment {

    private ImageView btnBack;
    private RecyclerView rvOrderHistory;
    private LinearLayout llEmptyState; // ĐỔI TỪ TextView THÀNH LinearLayout
    private View progressBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private OrderHistoryAdapter adapter;
    private List<Order> orderList;

    public OrderHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        // Initialize views
        initViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load order history
        loadOrderHistory();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        rvOrderHistory = view.findViewById(R.id.rvOrderHistory);
        llEmptyState = view.findViewById(R.id.llEmptyState); // ĐỔI ID VÀ KIỂU DỮ LIỆU
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        orderList = new ArrayList<>();
        adapter = new OrderHistoryAdapter(orderList, this::onOrderClick);
        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderHistory.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void loadOrderHistory() {
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        showLoading(true);

        // DEBUG: In ra userId để check
        System.out.println("DEBUG: Current User ID: " + currentUser.getUid());

        db.collection("orders")
                .whereEqualTo("userId", currentUser.getUid())
                .get() // Bỏ orderBy tạm thời để test
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    showLoading(false);
                    orderList.clear();

                    System.out.println("DEBUG: Query success, documents count: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        System.out.println("DEBUG: Document ID: " + document.getId());
                        System.out.println("DEBUG: Document data: " + document.getData());
                        
                        Order order = document.toObject(Order.class);
                        if (order != null) {
                            order.setId(document.getId());
                            orderList.add(order);
                            System.out.println("DEBUG: Added order: " + order.getId());
                        }
                    }

                    if (orderList.isEmpty()) {
                        System.out.println("DEBUG: Order list is empty");
                        showEmptyState();
                    } else {
                        System.out.println("DEBUG: Order list size: " + orderList.size());
                        showOrderList();
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    System.out.println("DEBUG: Query failed: " + e.getMessage());
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void onOrderClick(Order order) {
        // Navigate to order detail
        OrderDetailFragment fragment = OrderDetailFragment.newInstance(order.getId());
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvOrderHistory.setVisibility(show ? View.GONE : View.VISIBLE);
        llEmptyState.setVisibility(View.GONE); // SỬA THÀNH llEmptyState
    }

    private void showEmptyState() {
        progressBar.setVisibility(View.GONE);
        rvOrderHistory.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.VISIBLE); // SỬA THÀNH llEmptyState
    }

    private void showOrderList() {
        progressBar.setVisibility(View.GONE);
        rvOrderHistory.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE); // SỬA THÀNH llEmptyState
    }
}