package com.example.prm_project.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.example.prm_project.model.Book;
import com.example.prm_project.model.CartItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class BookDetailFragment extends Fragment {

    private static final String ARG_BOOK_ID = "book_id";
    private static final String TAG = "BookDetailFragment";
    private static final String COLLECTION_CARTS = "carts";

    private String bookId;
    private Book currentBook;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;
    
    // UI Components
    private ImageView ivBookCover, btnBack, btnDecrease, btnIncrease;
    private TextView tvBookTitle, tvBookPrice, tvBookStock, tvBookAuthor, tvBookCategory, tvBookDescription;
    private TextView tvQuantity, tvTotalPrice;
    private LinearLayout btnAddToCart, btnBuyNow;
    private int selectedQuantity = 1;

    public static BookDetailFragment newInstance(String bookId) {
        BookDetailFragment fragment = new BookDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_BOOK_ID, bookId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookId = getArguments().getString(ARG_BOOK_ID);
        }
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_detail, container, false);

        initViews(view);
        setupClickListeners();
        loadBookData();

        return view;
    }

    private void initViews(View view) {
        // Header
        btnBack = view.findViewById(R.id.btnBack);
        
        // Book info
        ivBookCover = view.findViewById(R.id.ivBookCover);
        tvBookTitle = view.findViewById(R.id.tvBookTitle);
        tvBookPrice = view.findViewById(R.id.tvBookPrice);
        tvBookStock = view.findViewById(R.id.tvBookStock);
        tvBookAuthor = view.findViewById(R.id.tvBookAuthor);
        tvBookCategory = view.findViewById(R.id.tvBookCategory);
        tvBookDescription = view.findViewById(R.id.tvBookDescription);
        
        // Quantity controls
        btnDecrease = view.findViewById(R.id.btnDecrease);
        btnIncrease = view.findViewById(R.id.btnIncrease);
        tvQuantity = view.findViewById(R.id.tvQuantity);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        
        // Action buttons
        btnAddToCart = view.findViewById(R.id.btnAddToCart);
        btnBuyNow = view.findViewById(R.id.btnBuyNow);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnDecrease.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                updateQuantityDisplay();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (currentBook != null && selectedQuantity < currentBook.getQuantity()) {
                selectedQuantity++;
                updateQuantityDisplay();
            } else {
                Toast.makeText(getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            addToCart();
        });

        btnBuyNow.setOnClickListener(v -> {
            buyNow();
        });
    }

    private void updateQuantityDisplay() {
        tvQuantity.setText(String.valueOf(selectedQuantity));
        
        if (currentBook != null) {
            int totalPrice = currentBook.getPrice() * selectedQuantity;
            tvTotalPrice.setText(String.format("$%,d", totalPrice));
        }
    }

    private void loadBookData() {
        if (bookId == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy thông tin sách", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoading(true);

        db.collection("books").document(bookId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    showLoading(false);
                    
                    if (documentSnapshot.exists()) {
                        currentBook = documentSnapshot.toObject(Book.class);
                        if (currentBook != null) {
                            currentBook.setId(documentSnapshot.getId());
                            displayBookData();
                        }
                    } else {
                        Toast.makeText(getContext(), "Không tìm thấy sách", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "Error loading book data", e);
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUIState() {
        if (currentBook == null) return;
        
        // Enable/disable buttons based on stock
        boolean hasStock = currentBook.getQuantity() > 0;
        btnIncrease.setEnabled(hasStock);
        btnAddToCart.setEnabled(hasStock);
        btnBuyNow.setEnabled(hasStock);

        if (!hasStock) {
            tvBookStock.setText("Hết hàng");
            if (getContext() != null) {
                tvBookStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_color));
            }
        } else {
            tvBookStock.setText(currentBook.getQuantity() + " cuốn");
            if (getContext() != null) {
                tvBookStock.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_color));
            }
        }
    }

    private void displayBookData() {
        if (currentBook == null) return;

        // Display book info
        tvBookTitle.setText(currentBook.getName());
        tvBookPrice.setText(formatPrice(currentBook.getPrice()));
        tvBookStock.setText(currentBook.getQuantity() + " cuốn");
        tvBookAuthor.setText(currentBook.getAuthor());
        tvBookDescription.setText(currentBook.getDescription());

        // Display category
        if (currentBook.getBookType() != null) {
            tvBookCategory.setText(currentBook.getBookType().getName());
        } else {
            tvBookCategory.setText("Chưa phân loại");
        }

        // Load book cover
        loadBookCover();

        // Update quantity display
        updateQuantityDisplay();

        // Update UI state
        updateUIState();
    }

    private void loadBookCover() {
        if (currentBook == null) return;
        
        String imageUrl = currentBook.getImageUrl();
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Load từ URL
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.default_book_cover)
                    .error(R.drawable.default_book_cover)
                    .into(ivBookCover);
        } else {
            ivBookCover.setImageResource(R.drawable.default_book_cover);
        }
    }

    private String formatPrice(int price) {
        return String.format("$%,d", price);
    }

    private void showLoading(boolean show) {
        if (show) {
            tvBookTitle.setText("Đang tải...");
            tvBookPrice.setText("...");
            tvBookStock.setText("...");
            tvBookAuthor.setText("...");
            tvBookCategory.setText("...");
            tvBookDescription.setText("...");
        }
    }

    private void addToCart() {
        if (currentBook == null) {
            Toast.makeText(getContext(), "Thông tin sách không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra stock
        if (selectedQuantity > currentBook.getQuantity()) {
            Toast.makeText(getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button để tránh double click
        btnAddToCart.setEnabled(false);
        
        // Kiểm tra xem item đã tồn tại trong cart chưa
        checkExistingCartItem();
    }

    private void checkExistingCartItem() {
        db.collection(COLLECTION_CARTS)
                .whereEqualTo("userId", currentUserId)
                .whereEqualTo("bookId", currentBook.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Item đã tồn tại, update quantity
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        CartItem existingItem = queryDocumentSnapshots.getDocuments().get(0).toObject(CartItem.class);
                        
                        int newQuantity = existingItem.getQuantity() + selectedQuantity;
                        
                        // Kiểm tra stock
                        if (newQuantity > currentBook.getQuantity()) {
                            btnAddToCart.setEnabled(true);
                            Toast.makeText(getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        // Update quantity
                        updateExistingCartItem(docId, newQuantity);
                    } else {
                        // Item chưa tồn tại, tạo mới
                        createNewCartItem();
                    }
                })
                .addOnFailureListener(e -> {
                    btnAddToCart.setEnabled(true);
                    Log.e(TAG, "Error checking existing cart item", e);
                    Toast.makeText(getContext(), "Lỗi khi kiểm tra giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateExistingCartItem(String docId, int newQuantity) {
        db.collection(COLLECTION_CARTS)
                .document(docId)
                .update(
                        "quantity", newQuantity,
                        "updatedAt", System.currentTimeMillis()
                )
                .addOnSuccessListener(aVoid -> {
                    btnAddToCart.setEnabled(true);
                    showAddToCartSuccess();
                })
                .addOnFailureListener(e -> {
                    btnAddToCart.setEnabled(true);
                    Log.e(TAG, "Error updating cart item", e);
                    Toast.makeText(getContext(), "Lỗi khi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void createNewCartItem() {
        CartItem newCartItem = new CartItem(currentUserId, currentBook.getId(), selectedQuantity);
        newCartItem.setSelected(true); // Mặc định được chọn
        
        db.collection(COLLECTION_CARTS)
                .add(newCartItem)
                .addOnSuccessListener(documentReference -> {
                    btnAddToCart.setEnabled(true);
                    showAddToCartSuccess();
                })
                .addOnFailureListener(e -> {
                    btnAddToCart.setEnabled(true);
                    Log.e(TAG, "Error adding to cart", e);
                    Toast.makeText(getContext(), "Lỗi khi thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAddToCartSuccess() {
        Toast.makeText(getContext(), 
                "✅ Đã thêm " + selectedQuantity + " cuốn \"" + currentBook.getName() + "\" vào giỏ hàng",
                Toast.LENGTH_SHORT).show();
        
        // Reset quantity về 1
        selectedQuantity = 1;
        updateQuantityDisplay();
        
        Log.d(TAG, "Successfully added to cart: " + currentBook.getName() + " x " + selectedQuantity);
    }

    private void buyNow() {
        if (currentBook == null) return;

        if (currentUserId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button
        btnBuyNow.setEnabled(false);
        
        // Thêm vào giỏ hàng và chuyển đến checkout
        addToCartAndCheckout();
    }

    private void addToCartAndCheckout() {
        CartItem cartItem = new CartItem(currentUserId, currentBook.getId(), selectedQuantity);
        cartItem.setSelected(true);
        
        db.collection(COLLECTION_CARTS)
                .add(cartItem)
                .addOnSuccessListener(documentReference -> {
                    btnBuyNow.setEnabled(true);
                    
                    // TODO: Navigate to checkout screen
                    Toast.makeText(getContext(), 
                            "🚀 Đang chuyển đến trang thanh toán...",
                            Toast.LENGTH_SHORT).show();
                    
                    // Tạm thời chuyển đến CartFragment
                    navigateToCart();
                })
                .addOnFailureListener(e -> {
                    btnBuyNow.setEnabled(true);
                    Log.e(TAG, "Error in buy now", e);
                    Toast.makeText(getContext(), "Lỗi khi mua hàng", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToCart() {
        // TODO: Implement navigation to CartFragment
        // Example using FragmentTransaction:
        /*
        CartFragment cartFragment = new CartFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, cartFragment)
                .addToBackStack(null)
                .commit();
        */
        
        Toast.makeText(getContext(), "Chuyển đến giỏ hàng...", Toast.LENGTH_SHORT).show();
    }
}