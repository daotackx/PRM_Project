package com.example.prm_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.adapter.OrderDetailAdapter;
import com.example.prm_project.model.Order;
import com.example.prm_project.model.OrderItem;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderDetailFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";

    private ImageView btnBack;
    private TextView tvOrderId, tvOrderStatus, tvOrderDate, tvCustomerName, tvCustomerPhone, tvCustomerAddress;
    private TextView tvSubtotal, tvShipping, tvTotal, tvPaymentMethod, tvNote;
    private RecyclerView rvOrderItems;
    private View progressBar;

    private FirebaseFirestore db;
    private OrderDetailAdapter adapter;
    private List<OrderItem> orderItemList;
    private String orderId;

    public static OrderDetailFragment newInstance(String orderId) {
        OrderDetailFragment fragment = new OrderDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_detail, container, false);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Setup click listeners
        setupClickListeners();

        // Load order details
        loadOrderDetails();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        tvOrderId = view.findViewById(R.id.tvOrderId);
        tvOrderStatus = view.findViewById(R.id.tvOrderStatus);
        tvOrderDate = view.findViewById(R.id.tvOrderDate);
        tvCustomerName = view.findViewById(R.id.tvCustomerName);
        tvCustomerPhone = view.findViewById(R.id.tvCustomerPhone);
        tvCustomerAddress = view.findViewById(R.id.tvCustomerAddress);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvTotal = view.findViewById(R.id.tvTotal);
        tvPaymentMethod = view.findViewById(R.id.tvPaymentMethod);
        tvNote = view.findViewById(R.id.tvNote);
        rvOrderItems = view.findViewById(R.id.rvOrderItems);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        orderItemList = new ArrayList<>();
        adapter = new OrderDetailAdapter(orderItemList);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    private void loadOrderDetails() {
        if (orderId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            order.setId(documentSnapshot.getId());
                            displayOrderDetails(order);
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayOrderDetails(Order order) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        // Order basic info
        tvOrderId.setText("Đơn hàng #" + order.getId().substring(0, 8).toUpperCase());
        tvOrderStatus.setText(getStatusText(order.getStatus()));
        tvOrderStatus.setTextColor(getStatusColor(order.getStatus()));
        tvOrderDate.setText(sdf.format(new Date(order.getCreatedAt())));

        // Customer info (từ Firebase Auth hoặc order data)
        tvCustomerName.setText("Khách hàng"); // Có thể load từ user data
        tvCustomerPhone.setText(order.getPhone() != null ? order.getPhone() : "Chưa có");
        tvCustomerAddress.setText(order.getAddress() != null ? order.getAddress() : "Chưa có");

        // Order items
        if (order.getItems() != null) {
            orderItemList.clear();
            orderItemList.addAll(order.getItems());
            adapter.notifyDataSetChanged();
        }

        // Order summary
        tvSubtotal.setText(formatter.format(order.getSubtotal()));
        tvShipping.setText(formatter.format(order.getShipping()));
        tvTotal.setText(formatter.format(order.getTotal()));
        tvPaymentMethod.setText(getPaymentMethodText(order.getPaymentMethod()));
        tvNote.setText(order.getNote() != null && !order.getNote().isEmpty() ? order.getNote() : "Không có ghi chú");
    }

    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Chờ xác nhận";
            case "confirmed": return "Đã xác nhận";
            case "shipping": return "Đang giao hàng";
            case "delivered": return "Đã giao hàng";
            case "cancelled": return "Đã hủy";
            default: return "Không xác định";
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "pending": return getResources().getColor(android.R.color.holo_orange_dark);
            case "confirmed": return getResources().getColor(android.R.color.holo_blue_dark);
            case "shipping": return getResources().getColor(android.R.color.holo_purple);
            case "delivered": return getResources().getColor(android.R.color.holo_green_dark);
            case "cancelled": return getResources().getColor(android.R.color.holo_red_dark);
            default: return getResources().getColor(android.R.color.darker_gray);
        }
    }

    private String getPaymentMethodText(String paymentMethod) {
        switch (paymentMethod) {
            case "cod": return "💰 Thanh toán khi nhận hàng (COD)";
            case "online": return "💳 Thanh toán online";
            default: return "Không xác định";
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}