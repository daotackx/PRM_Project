package com.example.prm_project.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.prm_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileDetailFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvFullName, tvPhone, tvAddress, tvEmailDetail;
    private CircleImageView ivAvatar;
    private ImageView btnBack, btnEditAvatar;
    private Button btnEditProfile;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    public ProfileDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_detail, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Khởi tạo views
        initViews(view);
        
        // Setup Activity Result Launchers
        setupActivityResultLaunchers();
        
        // Load dữ liệu người dùng
        loadUserData();
        
        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFullName = view.findViewById(R.id.tvFullName);
        tvPhone = view.findViewById(R.id.tvPhone);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvEmailDetail = view.findViewById(R.id.tvEmailDetail);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnBack = view.findViewById(R.id.btnBack);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
    }

    private void setupActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        ivAvatar.setImageURI(imageUri);
                        updateUserAvatar(imageUri);
                    }
                }
            }
        );

        // Camera launcher - Sửa lại cách xử lý
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = getBitmapFromBundle(extras, "data");
                        if (imageBitmap != null) {
                            ivAvatar.setImageBitmap(imageBitmap);
                            // Convert bitmap to URI if needed
                            // Uri imageUri = getImageUriFromBitmap(imageBitmap);
                            // updateUserAvatar(imageUri);
                            Toast.makeText(getContext(), "✅ Ảnh đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void loadUserData() {
        if (currentUser != null) {
            // Load basic info từ Firebase Auth
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            // Set basic info
            tvUserName.setText(displayName != null ? displayName : "Chưa cập nhật");
            tvUserEmail.setText(email != null ? email : "");
            tvEmailDetail.setText(email != null ? email : "");

            // Set avatar với Glide
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.default_avatar_circle)
                    .error(R.drawable.default_avatar_circle)
                    .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.default_avatar_circle);
            }

            // Load additional info từ Firestore
            loadUserDataFromFirestore();
        }
    }

    private void loadUserDataFromFirestore() {
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Lấy dữ liệu từ Firestore
                            String fullName = document.getString("fullName");
                            String phone = document.getString("phone");
                            String address = document.getString("address");

                            // Cập nhật UI
                            tvFullName.setText(fullName != null ? fullName : "Chưa cập nhật");
                            tvPhone.setText(phone != null ? phone : "Chưa cập nhật");
                            tvAddress.setText(address != null ? address : "Chưa cập nhật");
                            
                            // Cập nhật tên ở header nếu có
                            if (fullName != null && !fullName.isEmpty()) {
                                tvUserName.setText(fullName);
                            }
                        } else {
                            // Document không tồn tại, tạo mới với thông tin cơ bản
                            createUserDocument();
                        }
                    } else {
                        Toast.makeText(getContext(), "❌ Lỗi tải thông tin: " + 
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void createUserDocument() {
        if (currentUser != null) {
            // Tạo document mới với thông tin cơ bản từ Firebase Auth
            String email = currentUser.getEmail();
            String displayName = currentUser.getDisplayName();

            db.collection("users").document(currentUser.getUid())
                .set(new User(displayName, email, "", ""))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "✅ Đã tạo hồ sơ người dùng", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "❌ Lỗi tạo hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Edit avatar
        btnEditAvatar.setOnClickListener(v -> showAvatarDialog());

        // Edit profile button - CẬP NHẬT PHẦN NÀY
        btnEditProfile.setOnClickListener(v -> {
            // Mở EditProfileFragment
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new EditProfileFragment())
                .addToBackStack(null)
                .commit();
        });
    }

    private void showAvatarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn ảnh đại diện");
        
        String[] options = {"📷 Chụp ảnh", "🖼️ Chọn từ thư viện"};
        
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Camera
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        cameraLauncher.launch(cameraIntent);
                    } else {
                        Toast.makeText(getContext(), "❌ Camera không khả dụng", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 1: // Gallery
                     Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    galleryLauncher.launch(galleryIntent);
                    break;
            }
        });
        
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void updateUserAvatar(Uri imageUri) {
        if (currentUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(imageUri)
                .build();

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "✅ Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "❌ Lỗi cập nhật avatar", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    // User data class
    public static class User {
        public String fullName;
        public String email;
        public String phone;
        public String address;

        public User() {} // Empty constructor for Firestore

        public User(String fullName, String email, String phone, String address) {
            this.fullName = fullName;
            this.email = email;
            this.phone = phone;
            this.address = address;
        }
    }

    private Bitmap getBitmapFromBundle(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable(key, Bitmap.class);
        } else {
            return bundle.getParcelable(key);
        }
    }
}