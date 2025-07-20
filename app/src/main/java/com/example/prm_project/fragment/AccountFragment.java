package com.example.prm_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.prm_project.MainActivity;
import com.example.prm_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountFragment extends Fragment {

    private CircleImageView ivUserAvatar;
    private TextView tvUserName, tvUserRole;
    private LinearLayout layoutPersonalInfo, layoutOrderHistory, layoutSupport, layoutLogout;
    
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        initFirebase();
        initViews(view);
        setupClickListeners();
        loadUserProfile();

        return view;
    }

    private void initFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initViews(View view) {
        ivUserAvatar = view.findViewById(R.id.ivUserAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        layoutPersonalInfo = view.findViewById(R.id.layoutPersonalInfo);
        layoutOrderHistory = view.findViewById(R.id.layoutOrderHistory);
        layoutSupport = view.findViewById(R.id.layoutSupport);
        layoutLogout = view.findViewById(R.id.layoutLogout);
    }

    private void setupClickListeners() {
        layoutPersonalInfo.setOnClickListener(v -> navigateToProfileDetail());
        layoutOrderHistory.setOnClickListener(v -> {
            // Navigate to order history
            OrderHistoryFragment fragment = new OrderHistoryFragment();
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        });
        layoutSupport.setOnClickListener(v -> {
            // Mở màn hình chat hỗ trợ
            SupportChatFragment fragment = new SupportChatFragment();
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        });
        layoutLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                tvUserName.setText(displayName);
            } else {
                tvUserName.setText("Người dùng");
            }

            tvUserRole.setText("Người làm tự do");

            // Load avatar
            loadUserAvatar(currentUser);
        }
    }

    private void loadUserAvatar(FirebaseUser user) {
        if (user.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(user.getPhotoUrl())
                    .placeholder(R.drawable.default_avatar_circle)
                    .error(R.drawable.default_avatar_circle)
                    .into(ivUserAvatar);
        } else {
            db.collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String avatarUrl = documentSnapshot.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this)
                                        .load(avatarUrl)
                                        .placeholder(R.drawable.default_avatar_circle)
                                        .error(R.drawable.default_avatar_circle)
                                        .into(ivUserAvatar);
                            } else {
                                ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
                            }
                        } else {
                            ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
                        }
                    })
                    .addOnFailureListener(e -> {
                        ivUserAvatar.setImageResource(R.drawable.default_avatar_circle);
                    });
        }
    }

    private void navigateToProfileDetail() {
        ProfileDetailFragment fragment = new ProfileDetailFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToOrderHistory() {
        // TODO: Implement order history navigation
    }

    private void navigateToSupport() {
        // TODO: Implement support navigation
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> {
                    auth.signOut();
                    
                    // Gọi method showLoggedOutMenu của MainActivity để chuyển về menu logged out
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showLoggedOutMenu();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}