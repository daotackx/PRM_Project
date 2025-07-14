package com.example.prm_project.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
import android.os.Looper;
import androidx.core.content.ContextCompat;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.adapter.CheckoutAdapter;
import com.example.prm_project.model.CartItem;
import com.example.prm_project.model.Order;
import com.example.prm_project.model.OrderItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";
    private static final String ARG_CART_ITEMS = "cart_items";

    private ImageView btnBack;
    private RecyclerView rvCheckoutItems;
    private EditText edAddress, edPhone, edNote;
    private RadioGroup rgPaymentMethod;
    private TextView tvSubtotal, tvShipping, tvTotal;
    private Button btnPlaceOrder;

    private CheckoutAdapter checkoutAdapter;
    private List<CartItem> checkoutItems;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private int subtotal = 0;
    private int shipping = 30000; // 30k shipping fee
    private int total = 0;

    public static CheckoutFragment newInstance(ArrayList<CartItem> cartItems) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_CART_ITEMS, cartItems);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (getArguments() != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkoutItems = getArguments().getParcelableArrayList(ARG_CART_ITEMS, CartItem.class);
            } else {
                checkoutItems = getArguments().getParcelableArrayList(ARG_CART_ITEMS);
            }

        }

        if (checkoutItems == null) {
            checkoutItems = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        calculateTotal();

        return view;
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        rvCheckoutItems = view.findViewById(R.id.rvCheckoutItems);
        edAddress = view.findViewById(R.id.edAddress);
        edPhone = view.findViewById(R.id.edPhone);
        edNote = view.findViewById(R.id.edNote);
        rgPaymentMethod = view.findViewById(R.id.rgPaymentMethod);
        tvSubtotal = view.findViewById(R.id.tvSubtotal);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvTotal = view.findViewById(R.id.tvTotal);
        btnPlaceOrder = view.findViewById(R.id.btnPlaceOrder);
    }

    private void setupRecyclerView() {
        checkoutAdapter = new CheckoutAdapter(checkoutItems);
        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCheckoutItems.setAdapter(checkoutAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnPlaceOrder.setOnClickListener(v -> {
            processOrder();
        });
    }

    private void calculateTotal() {
        subtotal = 0;
        for (CartItem item : checkoutItems) {
            subtotal += item.getTotalPrice();
        }

        total = subtotal + shipping;

        // Format currency properly
        tvSubtotal.setText(String.format("%,dđ", subtotal));
        tvShipping.setText(String.format("%,dđ", shipping));
        tvTotal.setText(String.format("%,dđ", total));
    }

    private void processOrder() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Disable button to prevent double click
        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText("Đang xử lý...");

        // Create order
        createOrder();
    }

    private boolean validateInputs() {
        String address = edAddress.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();

        if (address.isEmpty()) {
            edAddress.setError("Vui lòng nhập địa chỉ giao hàng");
            edAddress.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            edPhone.setError("Vui lòng nhập số điện thoại");
            edPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            edPhone.setError("Số điện thoại không hợp lệ");
            edPhone.requestFocus();
            return false;
        }

        if (rgPaymentMethod.getCheckedRadioButtonId() == -1) {
            Toast.makeText(getContext(), "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createOrder() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            resetOrderButton();
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String address = edAddress.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String note = edNote.getText().toString().trim();

        // Get payment method
        RadioButton selectedPayment = requireView().findViewById(rgPaymentMethod.getCheckedRadioButtonId());
        String paymentMethod = selectedPayment.getText().toString();

        // Create order items
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : checkoutItems) {
            OrderItem orderItem = new OrderItem(
                    cartItem.getBookId(),
                    cartItem.getBookTitle(),
                    cartItem.getBookPrice(),
                    cartItem.getQuantity(),
                    cartItem.getTotalPrice());
            orderItems.add(orderItem);
        }

        // Create order
        Order order = new Order(
                userId,
                orderItems,
                subtotal,
                shipping,
                total,
                address,
                phone,
                note,
                paymentMethod,
                "pending" // Order status
        );

        // Save order to Firestore
        saveOrderToFirestore(order);
    }

    private void saveOrderToFirestore(Order order) {
        // Generate order ID
        String orderId = db.collection("orders").document().getId();
        order.setId(orderId);

        // Start by saving the order
        db.collection("orders").document(orderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Order created successfully: " + orderId);

                    // Remove items from cart after successful order
                    removeItemsFromCart();

                    // Update book quantities
                    updateBookQuantities();

                    showOrderSuccess(orderId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating order", e);
                    Toast.makeText(getContext(), "Lỗi tạo đơn hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    resetOrderButton();
                });
    }

    private void removeItemsFromCart() {
        if (auth.getCurrentUser() == null)
            return;

        String userId = auth.getCurrentUser().getUid();

        for (CartItem cartItem : checkoutItems) {
            db.collection("carts")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("bookId", cartItem.getBookId())
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (!querySnapshot.isEmpty()) {
                            String cartDocId = querySnapshot.getDocuments().get(0).getId();
                            db.collection("carts").document(cartDocId).delete();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error removing cart item: " + e.getMessage());
                    });
        }
    }

    private void updateBookQuantities() {
        for (CartItem cartItem : checkoutItems) {
            if (cartItem.getBook() != null) {
                int newQuantity = cartItem.getBook().getQuantity() - cartItem.getQuantity();
                db.collection("books").document(cartItem.getBookId())
                        .update("quantity", Math.max(0, newQuantity))
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error updating book quantity: " + e.getMessage());
                        });
            }
        }
    }

    private void showOrderSuccess(String orderId) {
        // Show success dialog
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(
                requireContext());
        builder.setTitle("🎉 Đặt hàng thành công!")
                .setMessage(
                        "Mã đơn hàng: " + orderId + "\n\nCảm ơn bạn đã mua hàng! Đơn hàng sẽ được giao trong 2-3 ngày.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back to home
                    requireActivity().getSupportFragmentManager().popBackStack();
                    requireActivity().getSupportFragmentManager().popBackStack();
                })
                .setCancelable(false)
                .show();
    }

    private void resetOrderButton() {
        btnPlaceOrder.setEnabled(true);
        btnPlaceOrder.setText("Đặt hàng");
    }
}