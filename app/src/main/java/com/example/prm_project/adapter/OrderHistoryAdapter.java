package com.example.prm_project.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.model.Order;
import com.example.prm_project.model.OrderItem;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    private List<Order> orderList;
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderHistoryAdapter(List<Order> orderList, OnOrderClickListener listener) {
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private CardView cardOrder;
        private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvOrderTotal, tvOrderItems;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardOrder = itemView.findViewById(R.id.cardOrder);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderItems = itemView.findViewById(R.id.tvOrderItems);
        }

        public void bind(Order order) {
            // Order ID
            tvOrderId.setText("Đơn hàng #" + order.getId().substring(0, 8).toUpperCase());

            // Order Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(order.getCreatedAt()));
            tvOrderDate.setText(dateStr);

            // Order Status
            String statusText = getStatusText(order.getStatus());
            tvOrderStatus.setText(statusText);
            tvOrderStatus.setTextColor(getStatusColor(order.getStatus()));

            // Order Total
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvOrderTotal.setText(formatter.format(order.getTotal()));

            // Order Items Summary - SỬA LẠI ĐÂY
            int totalItems = 0;
            String firstBookTitle = "";
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                for (OrderItem item : order.getItems()) {
                    totalItems += item.getQuantity();
                }
                firstBookTitle = order.getItems().get(0).getBookTitle();
            }

            if (totalItems > 1) {
                tvOrderItems.setText(firstBookTitle + " và " + (totalItems - 1) + " sản phẩm khác");
            } else if (totalItems == 1) {
                tvOrderItems.setText(firstBookTitle);
            } else {
                tvOrderItems.setText("Không có sản phẩm");
            }

            // Click listener
            cardOrder.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
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
                case "pending": return itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark);
                case "confirmed": return itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark);
                case "shipping": return itemView.getContext().getResources().getColor(android.R.color.holo_purple);
                case "delivered": return itemView.getContext().getResources().getColor(android.R.color.holo_green_dark);
                case "cancelled": return itemView.getContext().getResources().getColor(android.R.color.holo_red_dark);
                default: return itemView.getContext().getResources().getColor(android.R.color.darker_gray);
            }
        }
    }
}