package com.example.clipboardkeyboard;

import android.graphics.Color;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.clipboardkeyboard.data.ClipItem;
import com.example.clipboardkeyboard.data.DatabaseHelper;
import com.example.clipboardkeyboard.data.TagItem;
import java.util.List;

public class CustomKeyboardService extends InputMethodService {

    private LinearLayout kbTabContainer;
    private GridLayout kbButtonGrid;
    private TextView btnKbGridSwitch;
    private DatabaseHelper dbHelper;

    private String currentSelectedTag = "All List";
    private int currentGridColumns = 3; // Default 3 columns

    @Override
    public View onCreateInputView() {
        View view = getLayoutInflater().inflate(R.layout.keyboard_service_view, null);

        dbHelper = new DatabaseHelper(this);

        kbTabContainer = view.findViewById(R.id.kbTabContainer);
        kbButtonGrid = view.findViewById(R.id.kbButtonGrid);
        btnKbGridSwitch = view.findViewById(R.id.btnKbGridSwitch);

        Button btnKbDelete = view.findViewById(R.id.btnKbDelete);
        Button btnKbSwitch = view.findViewById(R.id.btnKbSwitch);
        Button btnKbSpace = view.findViewById(R.id.btnKbSpace);
        Button btnKbSend = view.findViewById(R.id.btnKbSend);

        // Switch to regular typing keyboard
        btnKbSwitch.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.showInputMethodPicker();
        });

        // Space bar
        btnKbSpace.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.commitText(" ", 1);
        });

        // Backspace / Delete
        btnKbDelete.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
        });

        // Enter / Send Button
        btnKbSend.setOnClickListener(v -> {
            InputConnection ic = getCurrentInputConnection();
            if (ic != null) {
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
            }
        });

        // Toggle Grid Columns between 3 and 4 in keyboard view
        btnKbGridSwitch.setOnClickListener(v -> {
            currentGridColumns = (currentGridColumns == 3) ? 4 : 3;
            kbButtonGrid.setColumnCount(currentGridColumns);
            loadButtonsForTag(currentSelectedTag);
        });

        setupTabs();
        return view;
    }

    @Override
    public void onStartInputView(EditorInfo info, boolean restarting) {
        super.onStartInputView(info, restarting);
        setupTabs();
    }

    private void setupTabs() {
        if (kbTabContainer == null) return;
        kbTabContainer.removeAllViews();

        List<TagItem> tags = dbHelper.getAllTags();

        addTabButton("Clipboard");
        addTabButton("All List");

        for (TagItem tag : tags) {
            addTabButton(tag.name);
        }

        loadButtonsForTag(currentSelectedTag);
    }

    private void addTabButton(String tabTitle) {
        TextView tab = new TextView(this);
        tab.setText(tabTitle);
        tab.setTextSize(14);
        tab.setPadding(24, 12, 24, 12);

        if (tabTitle.equals(currentSelectedTag)) {
            tab.setTextColor(Color.parseColor("#1B1B1F"));
            tab.setTypeface(null, android.graphics.Typeface.BOLD);
            tab.setBackgroundColor(Color.parseColor("#E0E3EC"));
        } else {
            tab.setTextColor(Color.parseColor("#757575"));
            tab.setBackgroundColor(Color.TRANSPARENT);
        }

        tab.setOnClickListener(v -> {
            currentSelectedTag = tabTitle;
            setupTabs();
        });

        kbTabContainer.addView(tab);
    }

    private void loadButtonsForTag(String tag) {
        if (kbButtonGrid == null) return;
        kbButtonGrid.removeAllViews();
        kbButtonGrid.setColumnCount(currentGridColumns);

        List<ClipItem> items = dbHelper.getItemsByTag(tag);

        for (ClipItem item : items) {
            Button btn = new Button(this);

            String displayTitle = (item.title != null && !item.title.trim().isEmpty())
                    ? item.title
                    : (item.content != null ? item.content.split("\n")[0] : "");

            btn.setText(displayTitle);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(11);
            btn.setMaxLines(2);
            btn.setBackgroundColor(item.color != 0 ? item.color : Color.parseColor("#3F51B5"));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 140; // compact height for options & questions
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(6, 6, 6, 6);
            btn.setLayoutParams(params);

            // Single-Tap Paste Logic
            btn.setOnClickListener(v -> {
                InputConnection ic = getCurrentInputConnection();
                if (ic != null && item.content != null) {
                    ic.commitText(item.content, 1);
                }
            });

            kbButtonGrid.addView(btn);
        }
    }
}
