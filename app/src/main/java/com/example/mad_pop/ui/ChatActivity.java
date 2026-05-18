package com.example.mad_pop.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.adapter.MessageAdapter;
import com.example.mad_pop.data.ChatRepository;
import com.example.mad_pop.model.Message;
import com.example.mad_pop.util.SessionManager;

import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private ChatRepository chatRepository;
    private MessageAdapter adapter;
    private long myId;
    private long peerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatRepository = new ChatRepository(this);
        SessionManager sessionManager = new SessionManager(this);
        myId = sessionManager.getUserId();
        peerId = getIntent().getLongExtra("peer_id", -1);
        String peerName = getIntent().getStringExtra("peer_name");

        TextView tvTitle = findViewById(R.id.tvChatTitle);
        tvTitle.setText(peerName == null ? "Chat" : peerName);

        RecyclerView recyclerMessages = findViewById(R.id.recyclerMessages);
        recyclerMessages.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MessageAdapter(myId);
        recyclerMessages.setAdapter(adapter);

        EditText etMessage = findViewById(R.id.etMessage);
        Button btnSend = findViewById(R.id.btnSend);

        btnSend.setOnClickListener(v -> {
            String body = etMessage.getText().toString().trim();
            if (body.isEmpty()) {
                return;
            }
            boolean ok = chatRepository.sendMessage(myId, peerId, body);
            if (!ok) {
                Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show();
                return;
            }
            etMessage.setText("");
            loadMessages();
            recyclerMessages.scrollToPosition(Math.max(adapter.getItemCount() - 1, 0));
        });

        loadMessages();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void loadMessages() {
        List<Message> messages = chatRepository.getConversation(myId, peerId);
        adapter.submitList(messages);
    }

}
