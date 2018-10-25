package com.danthecodinggui.recipes.view;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchHelperAdapter;
import com.danthecodinggui.recipes.view.ItemTouchHelper.ItemTouchSwipeHelper;

/**
 * Group of utility callback methods to enable swipe and drag & drop features to RecyclerView
 */
public class HomeItemTouchHelperCallback  extends ItemTouchHelper.Callback {

    private final ItemTouchHelperAdapter touchHelperAdapter;

    HomeItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
        touchHelperAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }
    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(0, swipeFlags);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        touchHelperAdapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        //Pass % swiped over to change card view
        float percent = Math.abs(dX / c.getWidth());

        if (viewHolder instanceof ItemTouchSwipeHelper) {
            ItemTouchSwipeHelper helper = (ItemTouchSwipeHelper) viewHolder;
            helper.onItemSwipe(percent);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
