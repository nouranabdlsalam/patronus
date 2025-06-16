package com.example.patronus;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
    private static final String API_TOKEN = "Bearer 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";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
// private EditText inputEditText;
    // private Button sendButton;
    // private TextView chatOutput;


    private EditText inputEditText;
    private Button sendButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    ImageButton back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        back = findViewById(R.id.helpback);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout nav_home= findViewById(R.id.nav_home);
        nav_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Help.this, HomeScreenActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_settings= findViewById(R.id.nav_settings);
        nav_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Help.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout nav_scan= findViewById(R.id.nav_scan);
        nav_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Help.this, ScanScreenActivity.class);
                startActivity(intent);
            }
        });
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

                // Build JSON body
                JSONObject jsonInput = new JSONObject();
                jsonInput.put("model", "mistral/ministral-8b"); // must match OpenRouter model name

                JSONArray messages = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", params[0]);
                messages.put(userMessage);

                jsonInput.put("messages", messages);
                jsonInput.put("temperature", 0.7); // optional
                jsonInput.put("max_tokens", 300); // optional


                JSONObject userMsg = new JSONObject();
                userMsg.put("role", "user");
                userMsg.put("content", params[0]);
                messages.put(userMsg);
                jsonInput.put("messages", messages);
                Log.d("API Request JSON", jsonInput.toString());

                // Send the request
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,
                        "UTF-8"));
                writer.write(jsonInput.toString());
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    InputStream errorStream = connection.getErrorStream();
                    BufferedReader errorReader = new BufferedReader(new
                            InputStreamReader(errorStream));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                    return "HTTP error " + responseCode + ": " + errorResponse.toString();
                }

                // Read response
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

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.length() > 0) {
                    JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                    return message.getString("content").trim();
                }

                return "No response from the model.";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error talking to the model: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            chatMessages.add(new ChatMessage(result, ChatMessage.TYPE_BOT));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        }
    }
}