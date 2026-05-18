package com.example.mad_pop.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mad_pop.R;
import com.example.mad_pop.adapter.UserAdapter;
import com.example.mad_pop.data.ChatRepository;
import com.example.mad_pop.model.User;
import com.example.mad_pop.util.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MentorChatsActivity extends AppCompatActivity {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mentor_chats);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();

        ChatRepository repository = new ChatRepository(this);
        UserAdapter adapter = new UserAdapter(this::openChat);

        RecyclerView recyclerView = findViewById(R.id.recyclerMentorChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        executorService.execute(() -> {
            List<User> mentees = repository.getChatPartners(userId, "MENTEE");
            runOnUiThread(() -> adapter.submitList(mentees));
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void openChat(User user) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("peer_id", user.getId());
        intent.putExtra("peer_name", user.getFullName());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

}
