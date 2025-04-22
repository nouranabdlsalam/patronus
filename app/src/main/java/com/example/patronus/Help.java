package com.example.patronus;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
public class Help extends AppCompatActivity {
    private static final String API_TOKEN = "Bearer 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"; // Replace this
    private static final String API_URL = "https://apiinference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3";
    // private EditText inputEditText;
    // private Button sendButton;
    // private TextView chatOutput;
    private EditText inputEditText;
    private Button sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.sendButton);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);
        sendButton.setOnClickListener(v -> {
            String userInput = inputEditText.getText().toString();
            if (!userInput.isEmpty()) {
                chatMessages.add(new ChatMessage(userInput, ChatMessage.TYPE_USER));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                inputEditText.setText("");
                new QueryLLM().execute(userInput);
            }
        });
    }

    private class QueryLLM extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Authorization", API_TOKEN);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("inputs", "Human: " + params[0] + "\nAI:");
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,
                        "UTF-8"));
                writer.write(jsonInput.toString());
                writer.flush();
                writer.close();
                os.close();
                // Check the response code
                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return "HTTP error code: " + responseCode;
                }
                // Read the API response
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                inputStream.close();
                // Handle array response format
                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject obj = jsonArray.getJSONObject(0);
                    if (obj.has("generated_text")) {
                        String generatedText = obj.getString("generated_text");
                        // Clean the response to show only the AI's answer
                        // Remove "Human: <question>\nAI:" from the generated text
                        String aiResponse = generatedText.replaceFirst("Human:.*\nAI:", "").trim();
                        return aiResponse;
                    }
                }
                return "Error: No response from the AI"; // Fallback in case the response is not
            } catch (Exception e) {
                e.printStackTrace();
                return "Error talking to chatbot!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("API Response", result);
            chatMessages.add(new ChatMessage(result, ChatMessage.TYPE_BOT));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        }
    }
}