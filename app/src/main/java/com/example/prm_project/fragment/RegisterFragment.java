package com.example.prm_project.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.prm_project.MainActivity;
import com.example.prm_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore; // THÊM IMPORT NÀY

import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private EditText edFullName, edEmail, edPhone, edPassword, edConfirmPassword, edAddress;
    private Button btnRegister;
    private TextView btnBackToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db; // THÊM BIẾN NÀY

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        // Khởi tạo views
        edFullName = view.findViewById(R.id.edFullName);
        edEmail = view.findViewById(R.id.edEmail);
        edPhone = view.findViewById(R.id.edPhone);
        edPassword = view.findViewById(R.id.edPassword);
        edConfirmPassword = view.findViewById(R.id.edConfirmPassword);
        edAddress = view.findViewById(R.id.edAddress);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnBackToLogin = view.findViewById(R.id.btnBackToLogin);
        
        // Khởi tạo Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // THÊM DÒNG NÀY

        btnRegister.setOnClickListener(v -> registerUser());

        // Xử lý button quay lại đăng nhập
        btnBackToLogin.setOnClickListener(v -> {
            // Quay lại LoginFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new LoginFragment())
                    .commit();
        });

        return view;
    }

    private void registerUser() {
        // Lấy tất cả thông tin từ form
        String fullName = edFullName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();
        String address = edAddress.getText().toString().trim();

        // Validation đầy đủ
        if (TextUtils.isEmpty(fullName)) {
            edFullName.setError("Họ tên không được để trống");
            edFullName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            edEmail.setError("Email không được để trống");
            edEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Email không hợp lệ");
            edEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            edPassword.setError("Mật khẩu không được để trống");
            edPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            edPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            edConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            edConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            edConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            edConfirmPassword.requestFocus();
            return;
        }

        // Validation phone (optional nhưng nếu có thì phải đúng format)
        if (!TextUtils.isEmpty(phone) && (phone.length() < 10 || phone.length() > 11)) {
            edPhone.setError("Số điện thoại không hợp lệ");
            edPhone.requestFocus();
            return;
        }

        // Disable button và show loading
        btnRegister.setEnabled(false);
        btnRegister.setText("Đang đăng ký...");

        // Đăng ký Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Đăng ký thành công
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cập nhật displayName trong Firebase Auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        // Lưu thông tin chi tiết vào Firestore (dù update profile có thành công hay không)
                                        saveUserToFirestore(user.getUid(), fullName, email, phone, address);
                                    });
                        }
                    } else {
                        // Đăng ký thất bại
                        btnRegister.setEnabled(true);
                        btnRegister.setText("ĐĂNG KÝ");

                        String errorMessage = "Đăng ký thất bại";
                        if (task.getException() != null) {
                            String exception = task.getException().getMessage();
                            if (exception != null) {
                                if (exception.contains("email address is already in use")) {
                                    errorMessage = "Email này đã được sử dụng";
                                } else if (exception.contains("weak password")) {
                                    errorMessage = "Mật khẩu quá yếu";
                                } else if (exception.contains("malformed email")) {
                                    errorMessage = "Email không hợp lệ";
                                } else {
                                    errorMessage = exception;
                                }
                            }
                        }
                        Toast.makeText(getContext(), "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String userId, String fullName, String email, String phone, String address) {
        // Tạo user data với tất cả thông tin
        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone); // Lưu phone từ form
        userData.put("address", address); // Lưu address từ form
        userData.put("createdAt", System.currentTimeMillis());
        userData.put("role", "user"); // Mặc định là user

        // Lưu vào Firestore
        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("ĐĂNG KÝ");

                    Toast.makeText(getContext(), "✅ Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                    // Chuyển về LoginFragment
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new LoginFragment())
                            .commit();
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    btnRegister.setText("ĐĂNG KÝ");

                    Toast.makeText(getContext(), "❌ Lỗi lưu thông tin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}