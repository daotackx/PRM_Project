package com.example.prm_project.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.adapter.CartAdapter;
import com.example.prm_project.model.Book;
import com.example.prm_project.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {
    private static final String TAG = "CartFragment";
    private static final String COLLECTION_CARTS = "carts";
    private static final String COLLECTION_BOOKS = "books";

    private ImageView btnBack;
    private LinearLayout layoutEmptyCart, layoutCartContent, layoutBottomSummary;
    private CheckBox cbSelectAll;
    private RecyclerView rvCartItems;
    private TextView tvTotalPrice;
    private LinearLayout btnCheckout;
    private ProgressBar progressBar;

    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        initFirebase();
        initViews(view);
        setupRecyclerView();
        setupClickListeners();
        loadCartFromFirebase();

        return view;
    }

    private void initFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        layoutCartContent = view.findViewById(R.id.layoutCartContent);
        layoutBottomSummary = view.findViewById(R.id.layoutBottomSummary);
        cbSelectAll = view.findViewById(R.id.cbSelectAll);
        rvCartItems = view.findViewById(R.id.rvCartItems);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);
        // progressBar = view.findViewById(R.id.progressBar); // Add to layout if needed
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, new CartAdapter.OnCartItemListener() {
            @Override
            public void onQuantityChanged(CartItem item, int newQuantity) {
                Log.d(TAG, "onQuantityChanged called: " + item.getBook().getName() + ", new quantity: " + newQuantity);
                
                if (item.getBook() != null && newQuantity > item.getBook().getQuantity()) {
                    Toast.makeText(getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
                    return;
                }

                item.setQuantity(newQuantity);
                updateCartItemInFirebase(item);
                updateTotalPrice();
            }

            @Override
            public void onItemDeleted(CartItem item) {
                deleteCartItemFromFirebase(item);
            }

            @Override
            public void onItemSelected(CartItem item, boolean isSelected) {
                item.setSelected(isSelected);
                updateCartItemInFirebase(item);
                updateSelectAllState();
                updateTotalPrice();
            }
        });

        rvCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectAllItems(isChecked);
        });

        btnCheckout.setOnClickListener(v -> processCheckout());
    }

    private void loadCartFromFirebase() {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            checkEmptyState();
            return;
        }

        showLoading(true);
        cartItems.clear();

        db.collection(COLLECTION_CARTS)
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showLoading(false);
                        checkEmptyState();
                        return;
                    }

                    int totalItems = queryDocumentSnapshots.size();
                    int[] loadedCount = { 0 }; // Use array to modify in lambda

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        CartItem cartItem = doc.toObject(CartItem.class);

                        // Load Book data
                        loadBookForCartItem(cartItem, () -> {
                            loadedCount[0]++;
                            if (loadedCount[0] == totalItems) {
                                // All items loaded
                                showLoading(false);
                                cartAdapter.notifyDataSetChanged();
                                updateTotalPrice();
                                updateSelectAllState();
                                checkEmptyState();
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading cart", e);
                    showLoading(false);
                    Toast.makeText(getContext(), "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    checkEmptyState();
                });
    }

    private void loadBookForCartItem(CartItem cartItem, Runnable onComplete) {
        if (cartItem.getBookId() == null) {
            onComplete.run();
            return;
        }

        db.collection(COLLECTION_BOOKS)
                .document(cartItem.getBookId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Book book = documentSnapshot.toObject(Book.class);
                        if (book != null) {
                            book.setId(documentSnapshot.getId());
                            cartItem.setBook(book);
                            cartItems.add(cartItem);
                        }
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading book: " + cartItem.getBookId(), e);
                    onComplete.run();
                });
    }

    private void selectAllItems(boolean selected) {
        for (CartItem item : cartItems) {
            item.setSelected(selected);
        }
        cartAdapter.notifyDataSetChanged();
        updateAllCartItemsInFirebase();
        updateTotalPrice();
    }

    private void processCheckout() {
        List<CartItem> selectedItems = getSelectedItems();
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn sản phẩm để thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to checkout with selected items
        CheckoutFragment checkoutFragment = CheckoutFragment.newInstance(new ArrayList<>(selectedItems));

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, checkoutFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showCheckoutDialog(List<CartItem> selectedItems) {
        int totalPrice = calculateSelectedTotal();
        int totalItems = selectedItems.size();

        String message = String.format(
                "Bạn có chắc muốn thanh toán?\n\n" +
                        "Số sản phẩm: %d\n" +
                        "Tổng tiền: $%,d",
                totalItems, totalPrice);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận thanh toán")
                .setMessage(message)
                .setPositiveButton("Thanh toán", (dialog, which) -> {
                    processPayment(selectedItems);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void processPayment(List<CartItem> selectedItems) {
        // TODO: Implement payment processing

        // Tạm thời xóa các item đã thanh toán khỏi cart
        for (CartItem item : selectedItems) {
            deleteCartItemFromFirebase(item);
        }

        int totalPrice = calculateSelectedTotal();
        Toast.makeText(getContext(),
                "✅ Thanh toán thành công!\nTổng tiền: " + String.format("$%,d", totalPrice),
                Toast.LENGTH_LONG).show();
    }

    private void updateCartItemInFirebase(CartItem item) {
        if (currentUserId == null || item.getBookId() == null)
            return;

        db.collection(COLLECTION_CARTS)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("bookId", item.getBookId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        item.touch(); // Update timestamp

                        db.collection(COLLECTION_CARTS)
                                .document(docId)
                                .set(item)
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error updating cart item", e);
                                });
                    }
                });
    }

    private void updateAllCartItemsInFirebase() {
        for (CartItem item : cartItems) {
            updateCartItemInFirebase(item);
        }
    }

    private void deleteCartItemFromFirebase(CartItem item) {
        if (currentUserId == null || item.getBookId() == null)
            return;

        db.collection(COLLECTION_CARTS)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("bookId", item.getBookId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection(COLLECTION_CARTS)
                                .document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    cartItems.remove(item);
                                    cartAdapter.notifyDataSetChanged();
                                    updateTotalPrice();
                                    checkEmptyState();
                                    updateSelectAllState();
                                    Toast.makeText(getContext(), "Đã xóa khỏi giỏ hàng", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error deleting cart item", e);
                                    Toast.makeText(getContext(), "Không thể xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                });
                    }
                });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void checkEmptyState() {
        boolean isEmpty = cartItems.isEmpty();
        layoutEmptyCart.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        layoutCartContent.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutBottomSummary.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void updateSelectAllState() {
        if (cartItems.isEmpty()) {
            cbSelectAll.setChecked(false);
            return;
        }

        boolean allSelected = cartItems.stream().allMatch(CartItem::isSelected);

        cbSelectAll.setOnCheckedChangeListener(null);
        cbSelectAll.setChecked(allSelected);
        cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> selectAllItems(isChecked));
    }

    private List<CartItem> getSelectedItems() {
        return cartItems.stream()
                .filter(CartItem::isSelected)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private int calculateSelectedTotal() {
        return cartItems.stream()
                .filter(CartItem::isSelected)
                .mapToInt(CartItem::getTotalPrice)
                .sum();
    }

    private void updateTotalPrice() {
        int totalPrice = calculateSelectedTotal();
        tvTotalPrice.setText(String.format("$%,d", totalPrice));
    }

    // Public method để thêm item từ bên ngoài
    public void addItemToCart(Book book, int quantity) {
        if (currentUserId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        if (book == null) {
            Toast.makeText(getContext(), "Sản phẩm không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra stock
        if (quantity > book.getQuantity()) {
            Toast.makeText(getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra item đã tồn tại
        CartItem existingItem = findCartItemByBookId(book.getId());
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > book.getQuantity()) {
                Toast.makeText(getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
                return;
            }
            existingItem.setQuantity(newQuantity);
            updateCartItemInFirebase(existingItem);
            cartAdapter.notifyDataSetChanged();
        } else {
            addNewCartItemToFirebase(book, quantity);
        }

        updateTotalPrice();
        Toast.makeText(getContext(), "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
    }

    private void addNewCartItemToFirebase(Book book, int quantity) {
        CartItem newItem = new CartItem(book, quantity, currentUserId);

        db.collection(COLLECTION_CARTS)
                .add(newItem)
                .addOnSuccessListener(documentReference -> {
                    cartItems.add(newItem);
                    cartAdapter.notifyDataSetChanged();
                    checkEmptyState();
                    updateSelectAllState();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding cart item", e);
                    Toast.makeText(getContext(), "Không thể thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private CartItem findCartItemByBookId(String bookId) {
        return cartItems.stream()
                .filter(item -> item.getBookId() != null && item.getBookId().equals(bookId))
                .findFirst()
                .orElse(null);
    }
}