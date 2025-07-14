package com.example.prm_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.example.prm_project.model.CartItem;

import java.util.List;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.CheckoutViewHolder> {

    private List<CartItem> checkoutItems;

    public CheckoutAdapter(List<CartItem> checkoutItems) {
        this.checkoutItems = checkoutItems;
    }

    @NonNull
    @Override
    public CheckoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout, parent, false);
        return new CheckoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutViewHolder holder, int position) {
        CartItem item = checkoutItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return checkoutItems.size();
    }

    static class CheckoutViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBookCover;
        private TextView tvBookTitle, tvBookPrice, tvQuantity, tvTotalPrice;

        public CheckoutViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            tvBookTitle = itemView.findViewById(R.id.tvBookTitle);
            tvBookPrice = itemView.findViewById(R.id.tvBookPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
        }

        public void bind(CartItem item) {
            if (item.hasValidBook()) {
                tvBookTitle.setText(item.getBookTitle());
                tvBookPrice.setText(item.getFormattedPrice());
                tvQuantity.setText("x" + item.getQuantity());
                tvTotalPrice.setText(item.getFormattedTotalPrice());

                // Load book cover
                Glide.with(itemView.getContext())
                        .load(item.getBookImageUrl())
                        .placeholder(R.drawable.banner_placeholder)
                        .error(R.drawable.banner_placeholder)
                        .into(ivBookCover);
            } else {
                tvBookTitle.setText("Đang tải...");
                tvBookPrice.setText("$0");
                tvQuantity.setText("x" + item.getQuantity());
                tvTotalPrice.setText("$0");
                ivBookCover.setImageResource(R.drawable.banner_placeholder);
            }
        }
    }
}