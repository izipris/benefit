package com.benefit.services;

import com.benefit.drivers.DatabaseDriver;
import com.benefit.model.Category;
import com.google.firebase.firestore.CollectionReference;

import java.util.List;

public class CategoryService {
    private DatabaseDriver databaseDriver;
    private CollectionReference categoriesCollection;
    private static final String TAG = "CategoryService";
    private static final String COLLECTION_NAME = "categories";

    public CategoryService(DatabaseDriver databaseDriver) {
        this.databaseDriver = databaseDriver;
        this.categoriesCollection = this.databaseDriver.getCollectionByName("categories");
    }

    public void addCategory(Category category) {
        this.categoriesCollection.add(category);
    }

    public Category getCategoryById(int categoryId) {
        List<Category> categoriesList = this.databaseDriver.getDocumentsByField(
                COLLECTION_NAME, "id", categoryId, Category.class);
        if (categoriesList.isEmpty()) {
            return null;
        }
        return categoriesList.get(0);
    }

    public List<Category> getAllMetaCategories() {
        return getCategoriesByField("level", 0);
    }

    public List<Category> getCategoriesByField(String fieldName, Object fieldValue) {
        return this.databaseDriver.getDocumentsByField(COLLECTION_NAME, fieldName, fieldValue, Category.class);
    }
}
