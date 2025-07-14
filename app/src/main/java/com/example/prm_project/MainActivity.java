package com.example.prm_project;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.prm_project.utils.FragmentFactory;
import com.example.prm_project.utils.PreferenceManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

// THÊM CÁC IMPORT FIREBASE
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fix user data trước khi load UI
        fixCurrentUserData();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Kiểm tra trạng thái đăng nhập và load menu tương ứng
        PreferenceManager pref = new PreferenceManager(this);
        setupNavigationMenu(pref.isLoggedIn());

        // Fragment mặc định là Home
        if (savedInstanceState == null) {
            loadFragment(FragmentFactory.createFragment(FragmentFactory.FragmentType.HOME));
        }

        // Xử lý sự kiện chọn menu
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            // Sử dụng Factory Pattern để tạo fragment
            if (itemId == R.id.nav_home) {
                fragment = FragmentFactory.createFragment(FragmentFactory.FragmentType.HOME);
            } else if (itemId == R.id.nav_cart) {
                fragment = FragmentFactory.createFragment(FragmentFactory.FragmentType.CART);
            } else if (itemId == R.id.nav_map) {
                fragment = FragmentFactory.createFragment(FragmentFactory.FragmentType.MAP); // MAP SẼ HOẠT ĐỘNG
            } else if (itemId == R.id.nav_account) {
                // Chỉ có khi đã login
                fragment = FragmentFactory.createFragment(FragmentFactory.FragmentType.ACCOUNT);
            } else if (itemId == R.id.nav_login) {
                // Chỉ có khi chưa login
                fragment = FragmentFactory.createFragment(FragmentFactory.FragmentType.LOGIN);
            }

            return loadFragment(fragment);
        });
    }

    // Hàm setup navigation menu dựa vào trạng thái đăng nhập
    private void setupNavigationMenu(boolean isLoggedIn) {
        bottomNavigationView.getMenu().clear();
        if (isLoggedIn) {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_logged_in);
        } else {
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_logged_out);
        }
    }

    // THÊM METHOD NÀY
    private void fixCurrentUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Log.d("FIX_USER", "Đang kiểm tra user: " + currentUser.getEmail());

            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String fullName = documentSnapshot.getString("fullName");
                            String phone = documentSnapshot.getString("phone");
                            String address = documentSnapshot.getString("address");

                            // Kiểm tra và cập nhật các field bị thiếu
                            Map<String, Object> updates = new HashMap<>();
                            boolean needUpdate = false;

                            if (fullName == null) {
                                String displayName = currentUser.getDisplayName();
                                String email = currentUser.getEmail();
                                updates.put("fullName", displayName != null ? displayName : email.split("@")[0]);
                                needUpdate = true;
                                Log.d("FIX_USER", "Thêm fullName");
                            }

                            if (phone == null) {
                                updates.put("phone", "");
                                needUpdate = true;
                                Log.d("FIX_USER", "Thêm phone");
                            }

                            if (address == null) {
                                updates.put("address", "");
                                needUpdate = true;
                                Log.d("FIX_USER", "Thêm address");
                            }

                            if (needUpdate) {
                                updates.put("updatedAt", System.currentTimeMillis());

                                db.collection("users").document(currentUser.getUid())
                                        .set(updates, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("FIX_USER", "✅ Đã cập nhật thông tin user");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("FIX_USER", "❌ Lỗi cập nhật: " + e.getMessage());
                                        });
                            } else {
                                Log.d("FIX_USER", "✅ Dữ liệu đã đầy đủ");
                            }
                        } else {
                            // Document không tồn tại, tạo mới
                            Log.d("FIX_USER", "Tạo document mới cho user");
                            createNewUserDocument(currentUser);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FIX_USER", "❌ Lỗi kiểm tra user: " + e.getMessage());
                    });
        }
    }

    // THÊM METHOD NÀY
    private void createNewUserDocument(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", user.getDisplayName() != null ? user.getDisplayName() : user.getEmail().split("@")[0]);
        userData.put("email", user.getEmail());
        userData.put("phone", "");
        userData.put("address", "");
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("role", "user");

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FIX_USER", "✅ Đã tạo document mới cho user");
                })
                .addOnFailureListener(e -> {
                    Log.e("FIX_USER", "❌ Lỗi tạo document: " + e.getMessage());
                });
    }

    // Hàm được gọi sau khi đăng xuất
    public void showLoggedOutMenu() {
        PreferenceManager pref = new PreferenceManager(this);
        pref.setLoggedIn(false);

        setupNavigationMenu(false);
        loadFragment(FragmentFactory.createFragment(FragmentFactory.FragmentType.HOME));
    }

    // Hàm được gọi sau khi đăng nhập thành công
    public void showLoggedInMenu() {
        PreferenceManager pref = new PreferenceManager(this);
        pref.setLoggedIn(true);

        setupNavigationMenu(true);
        loadFragment(FragmentFactory.createFragment(FragmentFactory.FragmentType.HOME));
    }

    // Hàm load Fragment vào container
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
