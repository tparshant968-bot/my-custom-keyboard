package com.example.clipboardkeyboard.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clipboardkeyboard.R;
import com.example.clipboardkeyboard.data.ClipItem;
import com.example.clipboardkeyboard.data.DatabaseHelper;
import com.example.clipboardkeyboard.data.TagItem;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvHeaderDropdown, tvEmptyHint;
    private RecyclerView recyclerView;
    private DatabaseHelper dbHelper;
    private ClipAdapter adapter;
    private List<ClipItem> currentList = new ArrayList<>();

    private String currentTag = "All List";
    private int currentGridSpan = 2; // Default 2 columns

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Preload Story 94 & 95 demo tags if empty
        if (dbHelper.getAllTags().isEmpty()) {
            dbHelper.insertTag("Story 94");
            dbHelper.insertTag("Story95");
        }

        tvHeaderDropdown = findViewById(R.id.tvHeaderDropdown);
        tvEmptyHint = findViewById(R.id.tvEmptyHint);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new GridLayoutManager(this, currentGridSpan));
        adapter = new ClipAdapter(this, currentList, dbHelper, this::loadData);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
            startActivity(intent);
        });

        findViewById(R.id.btnGridSwitch).setOnClickListener(v -> cycleGridSpan());

        tvHeaderDropdown.setOnClickListener(v -> showDropdownFilter());

        findViewById(R.id.fabAdd).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditItemActivity.class);
            intent.putExtra("current_tag", currentTag.equals("All List") ? "Story95" : currentTag);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        currentList.clear();
        currentList.addAll(dbHelper.getItemsByTag(currentTag));
        adapter.notifyDataSetChanged();

        if (currentList.isEmpty()) {
            tvEmptyHint.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyHint.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void cycleGridSpan() {
        // Toggle between 1, 2, 3, 4 columns
        if (currentGridSpan == 2) currentGridSpan = 3;
        else if (currentGridSpan == 3) currentGridSpan = 4;
        else if (currentGridSpan == 4) currentGridSpan = 1;
        else currentGridSpan = 2;

        recyclerView.setLayoutManager(new GridLayoutManager(this, currentGridSpan));
    }

    private void showDropdownFilter() {
        List<TagItem> tags = dbHelper.getAllTags();
        String[] options = new String[tags.size() + 1];
        options[0] = "All List";
        for (int i = 0; i < tags.size(); i++) {
            options[i + 1] = tags.get(i).name;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter by Tag");
        builder.setItems(options, (dialog, which) -> {
            currentTag = options[which];
            tvHeaderDropdown.setText(currentTag + " ⌄");
            loadData();
        });
        builder.show();
    }
}
