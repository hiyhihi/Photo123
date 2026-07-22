package com.example.photofilter.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photofilter.R;
import com.example.photofilter.data.HistoryEntry;
import com.example.photofilter.data.HistoryRepository;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Read-only screen listing past saves. Kept deliberately simple (no MVP
 * contract) since it has no business logic beyond "load and display".
 */
public class HistoryActivity extends AppCompatActivity {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private HistoryRepository historyRepository;
    private HistoryAdapter adapter;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        MaterialToolbar toolbar = findViewById(R.id.historyToolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        emptyText = findViewById(R.id.historyEmptyText);
        RecyclerView recyclerView = findViewById(R.id.historyRecyclerView);
        adapter = new HistoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        historyRepository = new HistoryRepository(this);
        loadHistory();
    }

    private void loadHistory() {
        executor.execute(() -> {
            List<HistoryEntry> entries = historyRepository.getAll();
            mainHandler.post(() -> {
                adapter.submitEntries(entries);
                emptyText.setVisibility(entries.isEmpty() ? View.VISIBLE : View.GONE);
            });
        });
    }

    @Override
    protected void onDestroy() {
        executor.shutdown();
        super.onDestroy();
    }
}
