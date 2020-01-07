package com.benefit;

/**
 * Represents a UI component of an item: the rounded rectangle with a photo and title.
 * Used in the RecyclerView as the object of the CardView.
 */
public class ClothingItem {

    private int mImageResource;
    private String mTitle;

    ClothingItem() {
    }

    ClothingItem(int imageResource, String title) {
        mImageResource = imageResource;
        mTitle = title;
    }


    int getmImageResource() {
        return mImageResource;
    }

    public void setmImageResource(int mImageResource) {
        this.mImageResource = mImageResource;
    }


    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }
}
