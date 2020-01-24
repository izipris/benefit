package com.benefit.ui.Items;

import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.benefit.R;
import com.benefit.ui.Displayable;
import com.benefit.ui.DisplayableRecycleAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * The UI for the part of the screen that displays the items (categories/products)
 */
public class ItemsDisplay {

    private View view;

    private List<Displayable> displayableItems;

    private RecyclerView mRecyclerView;
    private DisplayableRecycleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private int typeOfDisplay;


    public ItemsDisplay(View view, int typeOfDisplay) {
        this.view = view;
        this.typeOfDisplay = typeOfDisplay;
        displayableItems = new ArrayList<>();
    }

    public <T extends Displayable> void populateDisplayTable(List<T> displayableItems) {
//        this.displayableItems.addAll(displayableItems);
        addItemsToDisplayableItems(displayableItems);
        mRecyclerView = view.findViewById(R.id.categories);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(view.getContext(), 2);
        mAdapter = new DisplayableRecycleAdapter(this.displayableItems, typeOfDisplay);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    private <T extends Displayable> void addItemsToDisplayableItems(List<T> displayableItems) {
        for (Displayable item:displayableItems){
            if (!itemInList(item)){
                this.displayableItems.add(item);
            }
        }
    }

    private boolean itemInList(Displayable item) {
        for (Displayable displayable:this.displayableItems){
            if (item.getName().equals(displayable.getName()) &&
                    item.getImageResource().equals(displayable.getImageResource())){
                return true;
            }
        }
        return false;
    }

    public DisplayableRecycleAdapter getmAdapter() {
        return mAdapter;
    }

    public List<? extends Displayable> getDisplayableItems() {
        return displayableItems;
    }

    public <T extends Displayable> void refreshDisplay() {
        mRecyclerView = view.findViewById(R.id.categories);
        mRecyclerView.removeAllViews();
        this.displayableItems.clear();
//        populateDisplayTable(displayableItems);
    }
}
