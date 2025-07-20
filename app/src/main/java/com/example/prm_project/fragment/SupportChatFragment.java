package com.example.prm_project.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prm_project.R;
import com.example.prm_project.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.widget.TextView;
import android.util.Log;
import android.widget.LinearLayout;
import android.view.Gravity;
import android.content.Context;
import android.os.Handler;
import android.view.inputmethod.InputMethodManager;

public class SupportChatFragment extends Fragment {
    private RecyclerView rvMessages;
    private EditText edMessage;
    private Button btnSend;
    private ChatMessageAdapter adapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private String conversationId;
    private FirebaseUser currentUser;

    private static final String API_BASE_URL = "http://10.0.2.2:5000/api"; // Đổi thành IP backend thực tế

    private android.os.Handler handler = new android.os.Handler();
    private Runnable pollingRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_support_chat, container, false);
        rvMessages = view.findViewById(R.id.rvMessages);
        edMessage = view.findViewById(R.id.edMessage);
        btnSend = view.findViewById(R.id.btnSend);
        adapter = new ChatMessageAdapter(messageList);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để sử dụng chat hỗ trợ", Toast.LENGTH_SHORT).show();
            btnSend.setEnabled(false);
            return view;
        }

        // Tạo hoặc lấy conversationId
        createOrGetConversation();
        // Bắt đầu polling tin nhắn
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (conversationId != null) loadMessages();
                handler.postDelayed(this, 3000);
            }
        };
        handler.postDelayed(pollingRunnable, 3000);

        btnSend.setOnClickListener(v -> {
            // Force commit text from IME
            InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(edMessage.getWindowToken(), 0);
            edMessage.clearFocus();
            new Handler().postDelayed(() -> {
                String text = edMessage.getText().toString().trim();
                Log.d("SupportChat", "Bấm gửi: text=" + text + ", conversationId=" + conversationId);
                if (!TextUtils.isEmpty(text)) {
                    Toast.makeText(getContext(), "Đã bấm gửi: " + text, Toast.LENGTH_SHORT).show();
                    sendMessage(text);
                } else {
                    Toast.makeText(getContext(), "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }, 100);
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(pollingRunnable);
    }

    private void createOrGetConversation() {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/messages/conversations");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                JSONObject body = new JSONObject();
                body.put("userId", currentUser.getUid());
                body.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());
                conn.getOutputStream().write(body.toString().getBytes("UTF-8"));
                int responseCode = conn.getResponseCode();
                Log.d("SupportChat", "createOrGetConversation responseCode=" + responseCode);
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();
                    Log.d("SupportChat", "createOrGetConversation response=" + response);
                    JSONObject resp = new JSONObject(response.toString());
                    conversationId = resp.getString("conversationId");
                    getActivity().runOnUiThread(this::loadMessages);
                } else {
                    Log.e("SupportChat", "Không lấy được conversationId, responseCode=" + responseCode);
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Không lấy được conversationId, responseCode=" + responseCode, Toast.LENGTH_LONG).show());
                }
            } catch (Exception e) {
                Log.e("SupportChat", "Lỗi khi tạo/lấy conversationId: " + e.getMessage(), e);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Lỗi lấy conversationId: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void loadMessages() {
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/messages/conversations/" + conversationId + "/messages");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();
                    JSONArray arr = new JSONArray(response.toString());
                    messageList.clear();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject obj = arr.getJSONObject(i);
                        messageList.add(new ChatMessage(obj.getString("text"), obj.getString("senderName"), obj.getString("senderType"), obj.getLong("createdAt")));
                    }
                    getActivity().runOnUiThread(() -> adapter.notifyDataSetChanged());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage(String text) {
        if (conversationId == null) return;
        btnSend.setEnabled(false);
        new Thread(() -> {
            try {
                URL url = new URL(API_BASE_URL + "/messages/conversations/" + conversationId + "/messages");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);
                JSONObject body = new JSONObject();
                body.put("text", text);
                body.put("senderName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : currentUser.getEmail());
                body.put("senderType", "user");
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    getActivity().runOnUiThread(() -> {
                        edMessage.setText("");
                        loadMessages();
                        btnSend.setEnabled(true);
                    });
                } else {
                    getActivity().runOnUiThread(() -> btnSend.setEnabled(true));
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> btnSend.setEnabled(true));
            }
        }).start();
    }

    // ChatMessage & Adapter nội bộ (có thể tách file riêng nếu cần)
    public static class ChatMessage {
        public String text, senderName, senderType;
        public long createdAt;
        public ChatMessage(String text, String senderName, String senderType, long createdAt) {
            this.text = text;
            this.senderName = senderName;
            this.senderType = senderType;
            this.createdAt = createdAt;
        }
    }

    public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageViewHolder> {
        private final List<ChatMessage> messages;
        public ChatMessageAdapter(List<ChatMessage> messages) { this.messages = messages; }
        @NonNull
        @Override
        public ChatMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ChatMessageViewHolder(v);
        }
        @Override
        public void onBindViewHolder(@NonNull ChatMessageViewHolder holder, int position) {
            ChatMessage msg = messages.get(position);
            holder.bind(msg);
        }
        @Override
        public int getItemCount() { return messages.size(); }
    }

    public class ChatMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage, tvSender, tvTime;
        private final LinearLayout layoutMessageContainer;
        public ChatMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvTime = itemView.findViewById(R.id.tvTime);
            layoutMessageContainer = itemView.findViewById(R.id.layoutMessageContainer);
        }
        public void bind(ChatMessage msg) {
            tvMessage.setText(msg.text);
            tvSender.setText(msg.senderName + ("user".equals(msg.senderType) ? " (Bạn)" : " (Admin)"));
            tvTime.setText(android.text.format.DateFormat.format("HH:mm dd/MM", msg.createdAt));
            // Căn phải nếu là user, căn trái nếu là admin
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutMessageContainer.getLayoutParams();
            if ("user".equals(msg.senderType)) {
                layoutMessageContainer.setGravity(Gravity.END);
                params.gravity = Gravity.END;
            } else {
                layoutMessageContainer.setGravity(Gravity.START);
                params.gravity = Gravity.START;
            }
            layoutMessageContainer.setLayoutParams(params);
        }
    }
} 