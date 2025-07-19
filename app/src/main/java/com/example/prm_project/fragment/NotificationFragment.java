package com.example.prm_project.fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.prm_project.R;
import com.example.prm_project.adapter.NotificationAdapter;
import com.example.prm_project.model.NotificationModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class NotificationFragment extends Fragment implements NotificationAdapter.OnNotificationClickListener {

    private static final String TAG = "NotificationFragment";
    private static final String CHANNEL_ID = "order_notifications";

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private TextView tvEmptyState, tvUnreadCount;
    private Button btnMarkAllRead;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ListenerRegistration notificationListener;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        
        // Get current user
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        }
        
        // Create notification channel
        createNotificationChannel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "NotificationFragment onCreateView called, currentUserId: " + currentUserId);
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        
        if (currentUserId != null) {
            Log.d(TAG, "User logged in, setting up realtime listener for userId: " + currentUserId);
            setupRealtimeListener();
        } else {
            Log.w(TAG, "User not logged in, showing empty state");
            showEmptyState("Vui lòng đăng nhập để xem thông báo");
        }
        
        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);
        tvUnreadCount = view.findViewById(R.id.tvUnreadCount);
        btnMarkAllRead = view.findViewById(R.id.btnMarkAllRead);
        
        // Debug: Check if views are found
        Log.d(TAG, "Views found - recyclerView: " + (recyclerView != null) + 
                  ", swipeRefresh: " + (swipeRefreshLayout != null) +
                  ", progressBar: " + (progressBar != null) +
                  ", tvEmptyState: " + (tvEmptyState != null));
        
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        } else {
            Log.e(TAG, "btnMarkAllRead not found in layout!");
        }
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList);
        adapter.setOnNotificationClickListener(this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (currentUserId != null) {
                loadNotifications();
            } else {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupRealtimeListener() {
        if (currentUserId == null) return;
        
        showLoading(true);
        Log.d(TAG, "Setting up real-time listener for user: " + currentUserId);
        
        Query query = db.collection("notifications")
                .whereEqualTo("userId", currentUserId);
        
        notificationListener = query.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Log.e(TAG, "Listen failed.", e);
                showError("Lỗi khi lắng nghe thông báo: " + e.getMessage());
                return;
            }
            
            if (queryDocumentSnapshots != null) {
                List<NotificationModel> notifications = new ArrayList<>();
                
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    try {
                        NotificationModel notification = doc.toObject(NotificationModel.class);
                        notification.setId(doc.getId());
                        notifications.add(notification);
                    } catch (Exception ex) {
                        Log.e(TAG, "Error parsing notification", ex);
                    }
                }
                
                // Sort by creation time (newest first)
                Collections.sort(notifications, (a, b) -> {
                    if (a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return 0;
                });
                
                updateNotifications(notifications);
                
                // Check for new notifications to show system notification
                for (DocumentChange dc : queryDocumentSnapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        NotificationModel newNotification = dc.getDocument().toObject(NotificationModel.class);
                        newNotification.setId(dc.getDocument().getId()); // SET ID ĐỂ TRÁNH NULL
                        Log.d(TAG, "New notification added: " + newNotification.getTitle() + ", isRead: " + newNotification.isRead());
                        if (!newNotification.isRead()) {
                            showSystemNotification(newNotification);
                        }
                    } else if (dc.getType() == DocumentChange.Type.MODIFIED) {
                        NotificationModel modifiedNotification = dc.getDocument().toObject(NotificationModel.class);
                        modifiedNotification.setId(dc.getDocument().getId());
                        Log.d(TAG, "Notification modified: " + modifiedNotification.getId() + ", isRead: " + modifiedNotification.isRead());
                    }
                }
                
                Log.d(TAG, "Received " + notifications.size() + " notifications");
            }
            
            showLoading(false);
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void updateNotifications(List<NotificationModel> notifications) {
        Log.d(TAG, "Updating notifications UI with " + notifications.size() + " items");
        
        // Debug: Log trạng thái isRead của từng notification
        for (NotificationModel n : notifications) {
            Log.d(TAG, "Notification " + n.getId() + ": isRead = " + n.isRead() + ", title = " + n.getTitle());
        }
        
        this.notificationList.clear();
        this.notificationList.addAll(notifications);
        
        Log.d(TAG, "Local notificationList size after update: " + this.notificationList.size());
        
        if (adapter != null) {
            // Tạo copy để tránh reference issue
            List<NotificationModel> copyForAdapter = new ArrayList<>();
            for (NotificationModel notification : notificationList) {
                copyForAdapter.add(notification);
            }
            
            Log.d(TAG, "Calling adapter.updateNotifications with " + copyForAdapter.size() + " items");
            adapter.updateNotifications(copyForAdapter);
        } else {
            Log.e(TAG, "Adapter is null! Cannot update notifications");
        }
        
        updateUnreadCount();
        
        if (notifications.isEmpty()) {
            showEmptyState("Chưa có thông báo nào");
        } else {
            showEmptyState(null);
        }
    }

    private void updateUnreadCount() {
        int unreadCount = 0;
        for (NotificationModel notification : notificationList) {
            if (!notification.isRead()) {
                unreadCount++;
            }
        }
        
        Log.d(TAG, "Unread count: " + unreadCount + "/" + notificationList.size());
        
        if (unreadCount > 0) {
            tvUnreadCount.setVisibility(View.VISIBLE);
            tvUnreadCount.setText(unreadCount + " chưa đọc");
            btnMarkAllRead.setVisibility(View.VISIBLE);
        } else {
            tvUnreadCount.setVisibility(View.GONE);
            btnMarkAllRead.setVisibility(View.GONE);
        }
    }

    private void loadNotifications() {
        // This method can be used for manual refresh if needed
        // The real-time listener will handle automatic updates
    }

    private void showLoading(boolean show) {
        Log.d(TAG, "showLoading called with show: " + show + ", progressBar: " + (progressBar != null));
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        } else {
            Log.e(TAG, "ProgressBar is null!");
        }
    }

    private void showEmptyState(String message) {
        Log.d(TAG, "showEmptyState called with message: " + message);
        Log.d(TAG, "tvEmptyState is null: " + (tvEmptyState == null));
        Log.d(TAG, "recyclerView is null: " + (recyclerView == null));
        
        if (tvEmptyState != null) {
            if (message != null) {
                tvEmptyState.setText(message);
                tvEmptyState.setVisibility(View.VISIBLE);
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
                Log.d(TAG, "Showing empty state: " + message);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "Hiding empty state, showing RecyclerView");
            }
        } else {
            Log.e(TAG, "tvEmptyState is null, cannot show empty state!");
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
        showLoading(false);
    }

    private void markAllAsRead() {
        List<NotificationModel> unreadNotifications = new ArrayList<>();
        for (NotificationModel notification : notificationList) {
            if (!notification.isRead()) {
                unreadNotifications.add(notification);
            }
        }
        
        if (unreadNotifications.isEmpty()) {
            return;
        }
        
        for (NotificationModel notification : unreadNotifications) {
            db.collection("notifications").document(notification.getId())
                    .update("isRead", true, "readAt", new Date())
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Notification marked as read: " + notification.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error marking notification as read", e);
                    });
        }
        
        Toast.makeText(getContext(), "Đã đánh dấu tất cả thông báo đã đọc", Toast.LENGTH_SHORT).show();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notifications";
            String description = "Notifications for order updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showSystemNotification(NotificationModel notification) {
        if (getContext() == null || notification == null) return;
        
        // Kiểm tra dữ liệu notification trước khi sử dụng
        String title = notification.getTitle() != null ? notification.getTitle() : "Thông báo";
        String message = notification.getMessage() != null ? notification.getMessage() : "";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        
        NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Sử dụng ID an toàn để tránh NullPointerException
        int notificationId = notification.getId() != null ? notification.getId().hashCode() : (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void onNotificationClick(NotificationModel notification) {
        // Handle notification click - could navigate to order details
        Toast.makeText(getContext(), "Clicked: " + notification.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkAsReadClick(NotificationModel notification) {
        if (notification.getId() == null) {
            Log.e(TAG, "Cannot mark notification as read: ID is null");
            Toast.makeText(getContext(), "Lỗi: Không thể cập nhật thông báo", Toast.LENGTH_SHORT).show();
            return;
        }
        
        db.collection("notifications").document(notification.getId())
                .update("isRead", true, "readAt", new Date())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Notification marked as read: " + notification.getId());
                    
                    // Cập nhật local state ngay lập tức để UI responsive hơn
                    notification.setIsRead(true);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    updateUnreadCount();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error marking notification as read", e);
                    Toast.makeText(getContext(), "Lỗi khi đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }
}
