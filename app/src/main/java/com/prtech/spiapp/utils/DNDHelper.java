package com.prtech.spiapp.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.graphics.Color;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.prtech.spiapp.db.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DNDHelper {

    private static TableRow draggedRow = null;

    public interface OnOrderChangedListener<T> {
        void onOrderChanged(List<T> newOrder);
    }

    @SuppressLint("ClickableViewAccessibility")
    public static <T> void enableRowDragAndDrop(
            View dragHandle,
            TableRow row,
            TableLayout tableLayout,
            List<T> rowItem,
            OnOrderChangedListener<T> onOrderChanged
    ) {
        // Start dragging on long press
        dragHandle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                draggedRow = row;
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(row);
                row.startDragAndDrop(data, shadowBuilder, row, 0);
                return true;
            }
            return false;
        });

        // Handle drop logic
        row.setOnDragListener((v, event) -> {
            try {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackgroundColor(Color.LTGRAY);
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;

                    case DragEvent.ACTION_DROP:
                        if (draggedRow != null) {
                            TableRow target = (TableRow) v;
                            int targetIndex = tableLayout.indexOfChild(target);
                            tableLayout.removeView(draggedRow);
                            tableLayout.addView(draggedRow, targetIndex);

                            // Update order in memory and notify
                            List<T> newOrder = new ArrayList<>();
                            for (int i = 1; i < tableLayout.getChildCount(); i++) {
                                TableRow r = (TableRow) tableLayout.getChildAt(i);
                                TextView orderView = (TextView) r.getChildAt(0);
                                List<T> filtered = rowItem
                                    .stream()
                                    .filter(one -> {
                                        BaseEntity baseEntity = (BaseEntity) one;
                                        return baseEntity.getId() == Long.parseLong(orderView.getText().toString().trim());
                                    })
                                    .collect(Collectors.toList());
                                if (!filtered.isEmpty()) {
                                    newOrder.add(i, filtered.get(0));
                                }
                                orderView.setText(String.valueOf(i));
                            }
                            onOrderChanged.onOrderChanged(newOrder);
                        }
                        break;
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}

