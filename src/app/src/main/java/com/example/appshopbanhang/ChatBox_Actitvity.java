package com.example.appshopbanhang;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBox_Actitvity extends AppCompatActivity {

    private EditText editText;
    private ImageButton buttonSend;
    private ListView listView;
    private List<String> messageList = new ArrayList<>();
    private ChatBoxAdapter adapter;

    private final String GEMINI_API_KEY = "AIzaSyATpiosqBR55Pcg_B3GOg5c8ZjcSFi2ogg";

    // RAG components
    private DatabaseHelper databaseHelper;
    private ProductContextBuilder contextBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_box_actitvity);

        editText = findViewById(R.id.tinnhan);
        buttonSend = findViewById(R.id.btnsend);
        listView = findViewById(R.id.listview);
        ImageButton btnback = findViewById(R.id.btnback);

        adapter = new ChatBoxAdapter(this, messageList);
        listView.setAdapter(adapter);

        // Initialize RAG components
        databaseHelper = new DatabaseHelper(this);
        contextBuilder = new ProductContextBuilder();

        sendGreetingMessage();

        // Lấy tên đăng nhập
        TextView textTendn = findViewById(R.id.tendn);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String tendn = sharedPreferences.getString("tendn", null);

        if (tendn != null) {
            textTendn.setText(tendn);
        } else {
            startActivity(new Intent(this, Login_Activity.class));
            finish();
            return;
        }

        btnback.setOnClickListener(view -> {
            Intent intent = new Intent(ChatBox_Actitvity.this, TrangchuNgdung_Activity.class);
            intent.putExtra("tendn", tendn);
            startActivity(intent);
        });

        buttonSend.setOnClickListener(v -> {
            String userInput = editText.getText().toString().trim();
            if (!userInput.isEmpty()) {
                messageList.add("Bạn: " + userInput);
                adapter.notifyDataSetChanged();
                listView.setSelection(messageList.size() - 1);

                sendToChatGPT(userInput);
                editText.setText("");
            }
        });
    }

    // Gửi tin chào
    private void sendGreetingMessage() {
        String greetingMessage = "Xin chào! Tôi là chatbot hỗ trợ mua hàng. Bạn muốn hỏi gì?";
        messageList.add("Bot: " + greetingMessage);
        adapter.notifyDataSetChanged();
    }

    // Gửi API đến Gemini với RAG
    private void sendToChatGPT(String userText) {

        GeminiApiService apiService = RetrofitClient.getClient().create(GeminiApiService.class);

        // RAG: Lấy sản phẩm liên quan từ database
        ArrayList<SanPham> relevantProducts = contextBuilder.getRelevantProducts(userText, databaseHelper);

        // Build product context
        String productContext = contextBuilder.buildProductContext(relevantProducts);

        // Build full prompt với system instructions + product context + user query
        String fullPrompt = contextBuilder.buildFullPrompt(userText, productContext);

        GeminiRequest request = new GeminiRequest(fullPrompt);

        apiService.sendMessage(GEMINI_API_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            try {
                                String reply = response.body().candidates.get(0).content.parts.get(0).text;
                                messageList.add("Bot: " + reply);
                            } catch (Exception e) {
                                messageList.add("Bot: Lỗi khi đọc phản hồi.");
                                android.util.Log.e("Gemini_Error", "Error parsing response", e);
                            }
                        } else {
                            // Log chi tiết lỗi để debug
                            String errorMsg = "Bot: Lỗi " + response.code();
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    android.util.Log.e("Gemini_Error", "Code: " + response.code() + ", Body: " + errorBody);
                                    errorMsg += " - Vui lòng thử lại sau.";
                                }
                            } catch (Exception e) {
                                android.util.Log.e("Gemini_Error", "Error reading error body", e);
                            }
                            messageList.add(errorMsg);
                        }
                        adapter.notifyDataSetChanged();
                        listView.setSelection(messageList.size() - 1);
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        messageList.add("Bot: Lỗi kết nối - " + t.getMessage());
                        android.util.Log.e("Gemini_Error", "Connection error", t);
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
