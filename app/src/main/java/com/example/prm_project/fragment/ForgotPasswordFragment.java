package com.example.prm_project.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import com.example.prm_project.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordFragment extends Fragment {

    private EditText edEmail;
    private Button btnSendReset;
    private TextView btnBackToLogin, tvInstructions, tvCheckSpam; // Sửa lại kiểu của btnBackToLogin thành TextView
    private FirebaseAuth mAuth;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        // Khởi tạo views
        edEmail = view.findViewById(R.id.edEmail);
        btnSendReset = view.findViewById(R.id.btnSendReset);
        btnBackToLogin = view.findViewById(R.id.btnBackToLogin); // Đúng kiểu TextView
        tvInstructions = view.findViewById(R.id.tvInstructions);
        tvCheckSpam = view.findViewById(R.id.tvCheckSpam);

        mAuth = FirebaseAuth.getInstance();

        // Xử lý gửi email reset password
        btnSendReset.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();

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

            // Disable button và hiển thị loading
            btnSendReset.setEnabled(false);
            btnSendReset.setText("Đang gửi...");

            // Gửi email reset password
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        // Enable lại button
                        btnSendReset.setEnabled(true);
                        btnSendReset.setText("GỬI EMAIL");

                        if (task.isSuccessful()) {
                            // Thành công
                            Toast.makeText(getContext(),
                                    "✅ Email đặt lại mật khẩu đã được gửi!\n" +
                                            "Vui lòng kiểm tra hộp thư của bạn.",
                                    Toast.LENGTH_LONG).show();

                            // Hiển thị hướng dẫn
                            tvInstructions.setVisibility(View.VISIBLE);
                            tvCheckSpam.setVisibility(View.VISIBLE);

                            // Ẩn form và hiển thị thông báo thành công
                            edEmail.setVisibility(View.GONE);
                            btnSendReset.setVisibility(View.GONE);

                            // Tự động quay lại login sau 5 giây
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                 Handler sliderHandler = new Handler(Looper.getMainLooper());
                                if (getActivity() != null) {
                                    requireActivity().getSupportFragmentManager()
                                            .beginTransaction()
                                            .replace(R.id.fragmentContainer, new LoginFragment())
                                            .commit();
                                }
                            }, 5000);

                        } else {
                            // Thất bại
                            String errorMessage = "❌ Gửi email thất bại";
                            if (task.getException() != null) {
                                String firebaseError = task.getException().getMessage();
                                if (firebaseError.contains("user not found") ||
                                        firebaseError.contains("no user record")) {
                                    errorMessage = "❌ Email chưa được đăng ký";
                                } else if (firebaseError.contains("invalid email")) {
                                    errorMessage = "❌ Email không hợp lệ";
                                } else if (firebaseError.contains("too many requests")) {
                                    errorMessage = "❌ Quá nhiều yêu cầu. Vui lòng thử lại sau.";
                                } else if (firebaseError.contains("network")) {
                                    errorMessage = "❌ Lỗi kết nối mạng";
                                }
                            }
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

                            // Focus lại email field
                            edEmail.requestFocus();
                        }
                    });
        });

        // Xử lý button quay lại login
        btnBackToLogin.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new LoginFragment())
                    .commit();
        });

        return view;
    }
}
