package com.example.prm_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.example.prm_project.model.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onItemDeleted(CartItem item);
        void onItemSelected(CartItem item, boolean isSelected);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private CheckBox cbSelectItem;
        private ImageView ivBookCover, btnDecrease, btnIncrease, btnDelete;
        private TextView tvBookTitle, tvBookPrice, tvQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelectItem = itemView.findViewById(R.id.cbSelectItem);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookPrice = itemView.findViewById(R.id.tvBookPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(CartItem item) {
            // Kiểm tra Book đã được load chưa
            if (!item.hasValidBook()) {
                tvBookTitle.setText("Đang tải...");
                tvBookPrice.setText("$0");
                tvQuantity.setText(String.valueOf(item.getQuantity()));
                cbSelectItem.setChecked(item.isSelected());
                ivBookCover.setImageResource(R.drawable.default_book_cover);

                // Disable controls khi chưa load xong
                btnDecrease.setEnabled(false);
                btnIncrease.setEnabled(false);
                btnDelete.setEnabled(false);
                return;
            }

            // Enable controls
            btnDecrease.setEnabled(true);
            btnIncrease.setEnabled(true);
            btnDelete.setEnabled(true);

            // Bind data
            tvBookTitle.setText(item.getBookTitle());
            tvBookPrice.setText(item.getFormattedPrice());
            tvQuantity.setText(String.valueOf(item.getQuantity()));
            cbSelectItem.setChecked(item.isSelected());

            // Load image
            if (item.getBookImageUrl() != null && !item.getBookImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getBookImageUrl())
                        .placeholder(R.drawable.default_book_cover)
                        .error(R.drawable.default_book_cover)
                        .into(ivBookCover);
            } else {
                ivBookCover.setImageResource(R.drawable.default_book_cover);
            }

            // Click listeners
            cbSelectItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onItemSelected(item, isChecked);
                }
            });

            btnDecrease.setOnClickListener(v -> {
                android.util.Log.d("CartAdapter", "btnDecrease clicked for: " + item.getBook().getName() + ", current quantity: " + item.getQuantity());
                if (item.getQuantity() > 1 && listener != null) {
                    listener.onQuantityChanged(item, item.getQuantity() - 1);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                android.util.Log.d("CartAdapter", "btnIncrease clicked for: " + item.getBook().getName() + ", current quantity: " + item.getQuantity());
                if (listener != null) {
                    // Kiểm tra stock từ Book object
                    if (item.getBook() != null && item.getQuantity() < item.getBook().getQuantity()) {
                        listener.onQuantityChanged(item, item.getQuantity() + 1);
                    } else {
                        Toast.makeText(itemView.getContext(), "Không đủ hàng trong kho", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDeleted(item);
                }
            });
        }
    }
}