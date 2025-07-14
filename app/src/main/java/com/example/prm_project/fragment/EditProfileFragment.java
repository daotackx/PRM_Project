package com.example.prm_project.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {

    private EditText edFullName, edPhone, edAddress;
    private TextView tvEmail;
    private CircleImageView ivAvatar;
    private ImageView btnBack, btnEditAvatar;
    private Button btnSave, btnCancel;
    
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Khởi tạo views
        initViews(view);
        
        // Setup Activity Result Launchers
        setupActivityResultLaunchers();
        
        // Load dữ liệu hiện tại
        loadCurrentUserData();
        
        // Setup click listeners
        setupClickListeners();

        return view;
    }

    private void initViews(View view) {
        edFullName = view.findViewById(R.id.edFullName);
        edPhone = view.findViewById(R.id.edPhone);
        edAddress = view.findViewById(R.id.edAddress);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivAvatar = view.findViewById(R.id.ivAvatar);
        btnBack = view.findViewById(R.id.btnBack);
        btnEditAvatar = view.findViewById(R.id.btnEditAvatar);
        btnSave = view.findViewById(R.id.btnSave);
        btnCancel = view.findViewById(R.id.btnCancel);
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

        // Camera launcher
        cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = getBitmapFromBundle(extras, "data");
                        if (imageBitmap != null) {
                            ivAvatar.setImageBitmap(imageBitmap);
                            Toast.makeText(getContext(), "✅ Ảnh đã được cập nhật!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }

    private void loadCurrentUserData() {
        if (currentUser != null) {
            // Load basic info từ Firebase Auth
            String displayName = currentUser.getDisplayName();
            String email = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            // Set email (readonly)
            tvEmail.setText(email != null ? email : "");

            // Set avatar
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.default_avatar_circle)
                    .error(R.drawable.default_avatar_circle)
                    .into(ivAvatar);
            } else {
                ivAvatar.setImageResource(R.drawable.default_avatar_circle);
            }

            // Load từ Firestore
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String fullName = document.getString("fullName");
                            String phone = document.getString("phone");
                            String address = document.getString("address");

                            // Populate form
                            edFullName.setText(fullName != null ? fullName : "");
                            edPhone.setText(phone != null ? phone : "");
                            edAddress.setText(address != null ? address : "");
                        }
                    }
                });
        }
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Edit avatar
        btnEditAvatar.setOnClickListener(v -> showAvatarDialog());

        // Save button
        btnSave.setOnClickListener(v -> saveUserProfile());
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

    private void saveUserProfile() {
        String fullName = edFullName.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String address = edAddress.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(fullName)) {
            edFullName.setError("Vui lòng nhập họ và tên");
            edFullName.requestFocus();
            return;
        }

        if (!TextUtils.isEmpty(phone) && phone.length() < 10) {
            edPhone.setError("Số điện thoại phải có ít nhất 10 số");
            edPhone.requestFocus();
            return;
        }

        // Disable button khi đang lưu
        btnSave.setEnabled(false);
        btnSave.setText("Đang lưu...");

        if (currentUser != null) {
            // Update displayName trong Firebase Auth
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Update thông tin trong Firestore
                        updateFirestoreData(fullName, phone, address);
                    } else {
                        btnSave.setEnabled(true);
                        btnSave.setText("LƯU THAY ĐỔI");
                        Toast.makeText(getContext(), "❌ Lỗi cập nhật profile", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void updateFirestoreData(String fullName, String phone, String address) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("email", currentUser.getEmail());

        db.collection("users").document(currentUser.getUid())
            .set(userData)
            .addOnSuccessListener(aVoid -> {
                btnSave.setEnabled(true);
                btnSave.setText("LƯU THAY ĐỔI");
                Toast.makeText(getContext(), "✅ Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                
                // Quay lại ProfileDetailFragment
                requireActivity().getSupportFragmentManager().popBackStack();
            })
            .addOnFailureListener(e -> {
                btnSave.setEnabled(true);
                btnSave.setText("LƯU THAY ĐỔI");
                Toast.makeText(getContext(), "❌ Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
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

    private Bitmap getBitmapFromBundle(Bundle bundle, String key) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return bundle.getParcelable(key, Bitmap.class);
        } else {
            return bundle.getParcelable(key);
        }
    }
}