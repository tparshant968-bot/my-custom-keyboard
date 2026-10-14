package com.example.clipboardkeyboard.ui;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.clipboardkeyboard.R;
import com.example.clipboardkeyboard.data.DatabaseHelper;
import com.example.clipboardkeyboard.data.TagItem;
import java.util.List;

public class EditItemActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private TextView tvScreenTitle, tvSelectedTag;
    private View viewSelectedColor;
    private GridLayout gridColorPalette;
    private Button btnDelete;
    private DatabaseHelper dbHelper;

    private int itemId = -1;
    private int selectedColor;
    private String selectedTag = "Story95";

    private final int[] PALETTE_COLORS = {
            Color.parseColor("#3F51B5"), Color.parseColor("#4CAF50"),
            Color.parseColor("#B71C1C"), Color.parseColor("#455A64"),
            Color.parseColor("#0288D1"), Color.parseColor("#D32F2F"),
            Color.parseColor("#F57C00"), Color.parseColor("#8E24AA"),
            Color.parseColor("#2E7D32"), Color.parseColor("#C2185B")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        dbHelper = new DatabaseHelper(this);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvSelectedTag = findViewById(R.id.tvSelectedTag);
        viewSelectedColor = findViewById(R.id.viewSelectedColor);
        gridColorPalette = findViewById(R.id.gridColorPalette);
        btnDelete = findViewById(R.id.btnDelete);

        selectedColor = PALETTE_COLORS[0];
        setupPaletteGrid();

        if (getIntent().hasExtra("item_id")) {
            itemId = getIntent().getIntExtra("item_id", -1);
            tvScreenTitle.setText("Edit Item");
            etTitle.setText(getIntent().getStringExtra("title"));
            etContent.setText(getIntent().getStringExtra("content"));
            selectedTag = getIntent().getStringExtra("tag");
            selectedColor = getIntent().getIntExtra("color", selectedColor);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            tvScreenTitle.setText("New Item");
            if (getIntent().hasExtra("current_tag")) {
                selectedTag = getIntent().getStringExtra("current_tag");
            }
            btnDelete.setVisibility(View.GONE);
        }

        tvSelectedTag.setText(selectedTag);
        viewSelectedColor.setBackgroundColor(selectedColor);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.rowTag).setOnClickListener(v -> showTagSelectionDialog());

        findViewById(R.id.rowColor).setOnClickListener(v -> {
            gridColorPalette.setVisibility(gridColorPalette.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        findViewById(R.id.btnSave).setOnClickListener(v -> saveItem());

        btnDelete.setOnClickListener(v -> {
            if (itemId != -1) {
                dbHelper.deleteItem(itemId);
                Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void setupPaletteGrid() {
        gridColorPalette.removeAllViews();
        for (int color : PALETTE_COLORS) {
            View dot = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 72;
            params.height = 72;
            params.setMargins(12, 12, 12, 12);
            dot.setLayoutParams(params);
            dot.setBackgroundColor(color);
            dot.setOnClickListener(v -> {
                selectedColor = color;
                viewSelectedColor.setBackgroundColor(color);
                gridColorPalette.setVisibility(View.GONE);
            });
            gridColorPalette.addView(dot);
        }
    }

    private void showTagSelectionDialog() {
        List<TagItem> tags = dbHelper.getAllTags();
        String[] tagNames = new String[tags.size() + 1];
        for (int i = 0; i < tags.size(); i++) {
            tagNames[i] = tags.get(i).name;
        }
        tagNames[tags.size()] = "+ Create Tag";

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Tag");
        builder.setItems(tagNames, (dialog, which) -> {
            if (which == tags.size()) {
                showCreateTagPrompt();
            } else {
                selectedTag = tagNames[which];
                tvSelectedTag.setText(selectedTag);
            }
        });
        builder.show();
    }

    private void showCreateTagPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create Tag");
        final EditText input = new EditText(this);
        input.setHint("Typing a new tag");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String newTag = input.getText().toString().trim();
            if (!newTag.isEmpty()) {
                dbHelper.insertTag(newTag);
                selectedTag = newTag;
                tvSelectedTag.setText(selectedTag);
            }
        });
        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveItem() {
        String content = etContent.getText().toString().trim();
        if (content.isEmpty()) {
            Toast.makeText(this, "Content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            title = content.split("\n")[0];
        }

        if (itemId == -1) {
            dbHelper.insertItem(title, content, selectedTag, selectedColor);
        } else {
            dbHelper.updateItem(itemId, title, content, selectedTag, selectedColor);
        }

        finish();
    }
}
