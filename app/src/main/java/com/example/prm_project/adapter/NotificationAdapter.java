package com.example.prm_project.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prm_project.R;
import com.example.prm_project.model.NotificationModel;
import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    
    private Context context;
    private List<NotificationModel> notificationList;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationModel notification);
        void onMarkAsReadClick(NotificationModel notification);
    }

    public NotificationAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        android.util.Log.d("NotificationAdapter", "onBindViewHolder called for position: " + position + "/" + notificationList.size());
        
        NotificationModel notification = notificationList.get(position);
        android.util.Log.d("NotificationAdapter", "Binding notification: " + notification.getTitle());
        
        // Kiểm tra null và set giá trị mặc định
        holder.tvTitle.setText(notification.getTitle() != null ? notification.getTitle() : "Thông báo");
        holder.tvMessage.setText(notification.getMessage() != null ? notification.getMessage() : "");
        holder.tvTime.setText(notification.getFormattedDate() != null ? notification.getFormattedDate() : "");
        holder.tvOrderId.setText("Đơn hàng: #" + (notification.getOrderId() != null ? notification.getOrderId() : "N/A"));
        
        // Hiển thị trạng thái
        String status = notification.getStatus() != null ? notification.getStatus().toUpperCase() : "UNKNOWN";
        holder.tvStatus.setText(status);
        
        // Hiển thị thông tin đơn hàng nếu có
        if (notification.getOrderDetails() != null) {
            holder.tvOrderTotal.setText("Tổng tiền: " + notification.getOrderTotal());
            holder.tvOrderTotal.setVisibility(View.VISIBLE);
        } else {
            holder.tvOrderTotal.setVisibility(View.GONE);
        }

        // Thay đổi giao diện dựa trên trạng thái đã đọc/chưa đọc
        if (notification.isRead()) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#F8F9FA"));
            holder.tvNewBadge.setVisibility(View.GONE);
            holder.tvTitle.setTextColor(Color.parseColor("#6C757D"));
            android.util.Log.d("NotificationAdapter", "Notification " + notification.getId() + " is READ - showing gray style");
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.tvNewBadge.setVisibility(View.VISIBLE);
            holder.tvTitle.setTextColor(Color.parseColor("#333333"));
            android.util.Log.d("NotificationAdapter", "Notification " + notification.getId() + " is UNREAD - showing white style");
        }

        // Màu sắc theo loại thông báo
        int borderColor;
        String notificationType = notification.getType() != null ? notification.getType() : "";
        switch (notificationType) {
            case "order_confirmed":
                borderColor = Color.parseColor("#28A745");
                break;
            case "order_cancelled":
                borderColor = Color.parseColor("#DC3545");
                break;
            default:
                borderColor = Color.parseColor("#007BFF");
                break;
        }
        
        // Set border color (cần custom view hoặc drawable)
        holder.leftBorder.setBackgroundColor(borderColor);

        // Click events
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                android.util.Log.d("NotificationAdapter", "Clicked notification: " + notification.getId() + ", isRead: " + notification.isRead());
                listener.onNotificationClick(notification);
                if (!notification.isRead()) {
                    android.util.Log.d("NotificationAdapter", "Marking notification as read: " + notification.getId());
                    listener.onMarkAsReadClick(notification);
                } else {
                    android.util.Log.d("NotificationAdapter", "Notification already read, skipping mark as read");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = notificationList.size();
        android.util.Log.d("NotificationAdapter", "getItemCount() returning: " + count);
        return count;
    }

    public void updateNotifications(List<NotificationModel> newNotifications) {
        android.util.Log.d("NotificationAdapter", "updateNotifications called with " + newNotifications.size() + " items");
        android.util.Log.d("NotificationAdapter", "Current adapter list size: " + this.notificationList.size());
        
        // Tạo copy mới thay vì sử dụng reference
        List<NotificationModel> copyList = new ArrayList<>();
        for (NotificationModel notification : newNotifications) {
            copyList.add(notification);
        }
        
        this.notificationList.clear();
        this.notificationList.addAll(copyList);
        
        android.util.Log.d("NotificationAdapter", "Adapter list size after update: " + this.notificationList.size());
        
        // Debug: Log each notification in adapter list
        for (int i = 0; i < this.notificationList.size(); i++) {
            NotificationModel n = this.notificationList.get(i);
            android.util.Log.d("NotificationAdapter", "Item " + i + ": " + n.getTitle());
        }
        
        android.util.Log.d("NotificationAdapter", "Calling notifyDataSetChanged()");
        
        notifyDataSetChanged();
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvMessage, tvTime, tvOrderId, tvOrderTotal, tvStatus, tvNewBadge;
        View leftBorder;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvNewBadge = itemView.findViewById(R.id.tvNewBadge);
            leftBorder = itemView.findViewById(R.id.leftBorder);
        }
    }
}
