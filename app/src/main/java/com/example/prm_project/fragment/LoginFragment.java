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

public class LoginFragment extends Fragment {

    private EditText edEmail, edPassword;
    private Button btnLogin;
    private TextView btnSignup, btnForgotPassword;
    private FirebaseAuth mAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        edEmail = view.findViewById(R.id.edEmail);
        edPassword = view.findViewById(R.id.edPassword);
        btnLogin = view.findViewById(R.id.btnLoginWithEmail);
        btnSignup = view.findViewById(R.id.btnSignup);
        btnForgotPassword = view.findViewById(R.id.btnForgotPassword);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            // Validation
            if (TextUtils.isEmpty(email)) {
                edEmail.setError("Vui lòng nhập email");
                edEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edEmail.setError("Email không hợp lệ");
                edEmail.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                edPassword.setError("Vui lòng nhập mật khẩu");
                edPassword.requestFocus();
                return;
            }

            // Vô hiệu hóa button để tránh click nhiều lần
            btnLogin.setEnabled(false);
            btnLogin.setText("Đang đăng nhập...");

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        // Kích hoạt lại button
                        btnLogin.setEnabled(true);
                        btnLogin.setText("ĐĂNG NHẬP");

                        if (task.isSuccessful()) {
                            // Đăng nhập thành công
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(getContext(), "🎉 Chào mừng " +
                                    (user.getDisplayName() != null ? user.getDisplayName() : "bạn") +
                                    " đã trở lại!", Toast.LENGTH_SHORT).show();

                            // Cập nhật UI và chuyển về trang chủ
                            if (getActivity() instanceof MainActivity) {
                                ((MainActivity) getActivity()).showLoggedInMenu();
                            }

                            // Chuyển về HomeFragment
                            requireActivity().getSupportFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragmentContainer, new HomeFragment())
                                    .commit();

                        } else {
                            // Đăng nhập thất bại
                            String errorMessage = "❌ Đăng nhập thất bại";
                            if (task.getException() != null) {
                                String firebaseError = task.getException().getMessage();
                                if (firebaseError.contains("user not found")) {
                                    errorMessage = "❌ Tài khoản không tồn tại";
                                } else if (firebaseError.contains("wrong password")) {
                                    errorMessage = "❌ Mật khẩu không đúng";
                                } else if (firebaseError.contains("invalid email")) {
                                    errorMessage = "❌ Email không hợp lệ";
                                } else if (firebaseError.contains("network error")) {
                                    errorMessage = "❌ Lỗi kết nối mạng";
                                }
                            }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

                            // Focus vào password field để user thử lại
                            edPassword.requestFocus();
                        }
                    });
        });

        btnSignup.setOnClickListener(v -> {
            // Mở RegisterFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Xử lý button quên mật khẩu
        btnForgotPassword.setOnClickListener(v -> {
            // Mở ForgotPasswordFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new ForgotPasswordFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
