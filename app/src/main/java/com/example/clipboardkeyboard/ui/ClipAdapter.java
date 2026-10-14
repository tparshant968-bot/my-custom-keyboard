package com.example.clipboardkeyboard.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.clipboardkeyboard.R;
import com.example.clipboardkeyboard.data.ClipItem;
import com.example.clipboardkeyboard.data.DatabaseHelper;
import java.util.List;

public class ClipAdapter extends RecyclerView.Adapter<ClipAdapter.ViewHolder> {

    private Context context;
    private List<ClipItem> itemList;
    private DatabaseHelper dbHelper;
    private Runnable onDataChanged;

    public ClipAdapter(Context context, List<ClipItem> itemList, DatabaseHelper dbHelper, Runnable onDataChanged) {
        this.context = context;
        this.itemList = itemList;
        this.dbHelper = dbHelper;
        this.onDataChanged = onDataChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClipItem item = itemList.get(position);

        String displayTitle = (item.title != null && !item.title.trim().isEmpty()) 
                ? item.title 
                : (item.content != null ? item.content.split("\n")[0] : "");

        holder.tvTitle.setText(displayTitle);
        holder.tvContentPreview.setText(item.content);
        holder.cardView.setCardBackgroundColor(item.color != 0 ? item.color : Color.parseColor("#3F51B5"));

        holder.btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenu().add("Edit");
            popup.getMenu().add("Top");
            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(menuItem -> {
                String title = menuItem.getTitle().toString();
                if (title.equals("Edit")) {
                    Intent intent = new Intent(context, EditItemActivity.class);
                    intent.putExtra("item_id", item.id);
                    intent.putExtra("title", item.title);
                    intent.putExtra("content", item.content);
                    intent.putExtra("tag", item.tag);
                    intent.putExtra("color", item.color);
                    context.startActivity(intent);
                } else if (title.equals("Top")) {
                    dbHelper.setItemToTop(item.id);
                    if (onDataChanged != null) onDataChanged.run();
                } else if (title.equals("Delete")) {
                    dbHelper.deleteItem(item.id);
                    if (onDataChanged != null) onDataChanged.run();
                }
                return true;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvContentPreview, btnMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContentPreview = itemView.findViewById(R.id.tvContentPreview);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }
    }
}
