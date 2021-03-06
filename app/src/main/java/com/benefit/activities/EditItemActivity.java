package com.benefit.activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.benefit.R;
import com.benefit.drivers.StorageDriver;
import com.benefit.model.Category;
import com.benefit.model.Product;
import com.benefit.model.PropertyName;
import com.benefit.model.User;
import com.benefit.services.CategoryService;
import com.benefit.services.ProductService;
import com.benefit.utilities.Factory;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This activity is used when the user wants to edit item in his profile.
 */
public class EditItemActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int FIRST_CHILD_INDEX = 0;

    private StorageDriver storageDriver;
    private CategoryService categoryService;
    private ProductService productService;
    private LayoutInflater inflater;

    private Dialog mThankYouDailog;
    private Dialog mNoPhotoOrTitleAlertrDialog;
    private LinearLayout mActivityRootLinearLayout;
    private String brandAsString;

    private boolean mHaveCategoriesBeenInflatedOnce = false;

    // the following group are fields for creating a Product
    private Product mProductToEdit;
    private User mUser;
    private Uri mImageUri;
    private String mImageUrl;
    private EditText mEdTextTitle;
    private EditText mEdTextBrand;
    private ImageButton mImageButtonUpload;
    private LinearLayout mBrandLayout;
    private int mMetaCategory;
    private int mCategoryGroupIndexInLayout;
    private int mCategory;
    private Map<String, List<String>> mProperties = new HashMap<>(); // to create a Product object
    private SortedMap<String, ChipGroup> mPropertiesChipGroupsBuffer = new TreeMap<>(); // to create UI


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createServicesAndDrivers();
        extractExtras();  // holds setContentView()
        instantiateDataMembers();
        createActivityByMode();
    }

    /**
     * =========================== Initializing the Activity ======================================
     */


    private void extractExtras() {
        Bundle bundle = getIntent().getExtras();
        String userKey = getString(R.string.user_relay);
        String productKey = getString(R.string.product_relay);
        if (bundle != null) {
            Set<String> bundleKeySet = bundle.keySet();
            if (bundleKeySet.contains(userKey)) {
                User userFromExtra = (User) bundle.getSerializable(userKey);
                if (userFromExtra != null) {
                    mUser = userFromExtra;
                } else {
                    makeToast(this.getString(R.string.intent_extra_null_user_toast));
                }
            } else {
                makeToast(this.getString(R.string.intent_extra_no_user_key_toast));
            }
            if (bundleKeySet.contains(productKey)) {
                mProductToEdit = (Product) bundle.getSerializable(productKey);
                setContentView(R.layout.activity_edit_item);
            } else {
                makeToast(this.getString(R.string.intent_extra_no_product_key_toast));
            }
        } else {
            makeToast(this.getString(R.string.intent_extra_no_bundle_toast));
        }
    }


    private void createServicesAndDrivers() {
        this.productService = ViewModelProviders.of(this,
                Factory.getProductServiceFactory()).get(ProductService.class);
        this.categoryService = ViewModelProviders.of(this,
                Factory.getCategoryServiceFactory()).get(CategoryService.class);
        this.storageDriver = ViewModelProviders.of(this,
                Factory.getStorageDriverFactory()).get(StorageDriver.class);
    }


    private void instantiateDataMembers() {
        mEdTextTitle = findViewById(R.id.item_title_text);
        brandAsString = getResources().getString(R.string.brand_property_name);
        mImageButtonUpload = findViewById(R.id.image_button_choose_image);
        mActivityRootLinearLayout = findViewById(R.id.activity_root_linear_layout);
        inflater = LayoutInflater.from(this);
        View brandLayout = inflater.inflate(R.layout.brand_text_input_layout, null);
        mEdTextBrand = brandLayout.findViewById(R.id.item_brand);
        mBrandLayout = (LinearLayout) brandLayout;

    }

    private void createActivityByMode() {
        loadProductFromExtras();
        setMetaCategoryId(); // contains createAttributeChips
    }

    private void loadBrandOnEdit() {
        String brand = mProductToEdit.getProperties().get(brandAsString).get(0);
        if (brand != null)
            mEdTextBrand.setText(brand);

    }

    private void loadImageOnEdit() {
        mImageUrl = mProductToEdit.getImageResource();
        mImageUri = Uri.parse(mImageUrl);
        loadImageIntoButtonImage();
    }

    private void setMetaCategoryId() {
        final Observer<Category> categoryObserver = new Observer<Category>() {
            @Override
            public void onChanged(Category observedCategory) {
                mMetaCategory = observedCategory.getParentId();
                createAttributeChips();
            }
        };
        categoryService.getCategoryById(mCategory).observe(this, categoryObserver);
    }


    private void loadProductFromExtras() {
        mEdTextTitle.setText(mProductToEdit.getTitle());
        mCategory = mProductToEdit.getCategoryId();
        mProperties = mProductToEdit.getProperties();
        loadBrandOnEdit();
        loadImageOnEdit();
    }


    /**
     * =========================== Creating All Chip Groups ======================================
     */

    private void createAttributeChips() {
        ChipGroup chipGroup = createMetaCategoriesObserver();
        mActivityRootLinearLayout.addView(chipGroup);
        mCategoryGroupIndexInLayout = mActivityRootLinearLayout.getChildCount();
    }

    private ChipGroup createMetaCategoriesObserver() {
        ChipGroup chipGroup = createChipGroup(this.getString(R.string.metacategory_chip_group_heading));
        setOnCheckedChangeListenerForMetaCategories(chipGroup);
        final List<Category> metaCategories = new LinkedList<>();
        final Observer<List<Category>> metaCategoriesObserver = new Observer<List<Category>>() {

            @Override
            public void onChanged(List<Category> observedCategories) {
                metaCategories.addAll(observedCategories);
                createChipGroupFromCategories(metaCategories, chipGroup);
            }
        };
        categoryService.getAllMetaCategories().observe(this, metaCategoriesObserver);
        return chipGroup;
    }

    private void setOnCheckedChangeListenerForMetaCategories(ChipGroup chipGroup) {
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
                Chip chip = chipGroup.findViewById(i);
                if (chip != null) {
                    mMetaCategory = ((Category) chip.getTag()).getIdAsInt();
                    createCategoriesObserver();
                }
            }
        });
    }

    private void createChipGroupFromCategories(List<Category> categories, ChipGroup chipGroup) {
        int chipIndexToCheck = FIRST_CHILD_INDEX;
        for (Category category : categories) {
            addCategoryAsChipToChipGroup(category, chipGroup);
            if (isChipMatchingProductToEdit(category.getLevel(), category.getId())) {
                chipIndexToCheck = categories.indexOf(category);
            }
        }
        checkChipOfIndex(chipGroup, chipIndexToCheck);
    }

    private void createCategoriesObserver() {
        ChipGroup chipGroup = createChipGroup(this.getString(R.string.categories_chip_group_heading));
        setOnCheckedChangeListenerForCategories(chipGroup);
        final List<Category> categories = new LinkedList<>();
        final Observer<List<Category>> categoriesObserver = new Observer<List<Category>>() {

            @Override
            public void onChanged(List<Category> observedCategories) {
                categories.addAll(observedCategories);
                createChipGroupFromCategories(categories, chipGroup);
            }
        };
        categoryService.getChildrenByParentId(mMetaCategory).observe(this, categoriesObserver);
    }

    private void setOnCheckedChangeListenerForCategories(ChipGroup chipGroup) {
        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
                Chip chip = chipGroup.findViewById(i);
                if (chip != null) {
                    Category checkedCategory = (Category) chip.getTag();
                    mCategory = checkedCategory.getIdAsInt();
                    createPropertiesChips(checkedCategory.getIdAsInt(), chipGroup);
                }
            }
        });
    }

    private void addCategoryAsChipToChipGroup(Category category, ChipGroup chipGroup) {
        View chipAsView = inflater.inflate(R.layout.chip_layout, null);
        Chip chip = (Chip) chipAsView;
        chip.setText(category.getName());
        chip.setTag(category);
        chipGroup.addView(chip);
    }

    private boolean isPropertyValueToCheck(PropertyName propertyName, String propertyValue) {
        String propertyNameBeingCreated = propertyName.getName();
        if (!propertyNameBeingCreated.isEmpty() && !mProperties.isEmpty()) {
            List<String> propertyValues = mProperties.get(propertyNameBeingCreated);
            if (propertyValues != null) {
                // for now assuming single property
                String propertyToLoad = propertyValues.get(0);
                return (propertyToLoad != null && propertyToLoad.equals(propertyValue));
            }
        }
        return false;
    }

    private boolean isChipMatchingProductToEdit(int level, long categoryId) {
        // if it is a meta category and match or a category and match
        return ((level == 0 && categoryId == mMetaCategory) ||
                (level == 1 && categoryId == mCategory));
    }

    private void createPropertiesChips(int categoryId, ChipGroup chipGroup) {

        final List<PropertyName> observedPropertyNames = new LinkedList<>();
        final Observer<List<PropertyName>> propertyNamesObserver = new Observer<List<PropertyName>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onChanged(List<PropertyName> propertyNames) {
                observedPropertyNames.addAll(propertyNames);
                createChipGroupsFromProperties(observedPropertyNames, chipGroup);
            }
        };
        categoryService.getAllPropertiesByCategoryId(categoryId).observe(this, propertyNamesObserver);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createChipGroupsFromProperties(List<PropertyName> propertyNames,
                                                ChipGroup categoryChipGroup) {
        for (PropertyName propertyName : propertyNames) {
            createChipGroupForPropertyName(propertyName);
        }
        addCategoriesAndPropertiesChipGroupsToLayout(categoryChipGroup);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private int addPropertiesAsChipsToChipGroup(PropertyName propertyName,
                                                ChipGroup propertyGroup) {

        List<String> propertyValues = propertyName.getValidValues();
        int chipIndexToCheck = FIRST_CHILD_INDEX;
        for (String value : propertyValues) {
            addPropertyAsChipToChipGroup(value, propertyName, propertyGroup);
            if (isPropertyValueToCheck(propertyName, value)) {
                chipIndexToCheck = propertyValues.indexOf(value);
            }
        }
        return chipIndexToCheck;
    }

    private void addPropertyAsChipToChipGroup(String propertyValue, PropertyName propertyName,
                                              ChipGroup propertyGroup) {
        View chipAsView = inflater.inflate(R.layout.chip_layout, null);
        Chip chip = (Chip) chipAsView;
        chip.setText(propertyValue);
        chip.setTag(propertyName);
        propertyGroup.addView(chip);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void createChipGroupForPropertyName(PropertyName propertyName) {

        if (propertyName.getValidValues() == null)
            return; // brand, for example, has free text input so we create it in instantiation

        ChipGroup propertyGroup = createChipGroup(propertyName.getName());
        mPropertiesChipGroupsBuffer.put(propertyName.getName(), propertyGroup);

        int chipIndexToCheck = addPropertiesAsChipsToChipGroup(propertyName, propertyGroup);
        setPropertyGroupOnCheckedChangeListener(propertyGroup);
        checkChipOfIndex(propertyGroup, chipIndexToCheck);
    }

    private void setPropertyGroupOnCheckedChangeListener(ChipGroup propertyGroup) {
        propertyGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup chipGroup, int i) {
                Chip chip = chipGroup.findViewById(i);
                if (chip != null) {
                    updatePropertyOnCheckedChanged(chip);
                }
            }
        });
    }

    private void updatePropertyOnCheckedChanged(Chip chip) {
        String chipPropertyName = ((PropertyName) chip.getTag()).getName();
        mProperties.put(chipPropertyName,
                Collections.singletonList(chip.getText().toString()));
    }


    private void checkChipOfIndex(ChipGroup chipGroup, int chipIndexToCheck) {
        // "chipIndexToCheck + 1": because of chipgroup holding textView first
        ((Chip) chipGroup.getChildAt(chipIndexToCheck + 1)).setChecked(true);
    }


    private void addPropertiesChipGroupsToLayout() {
        for (Map.Entry<String, ChipGroup> entry : mPropertiesChipGroupsBuffer.entrySet()) {
            mActivityRootLinearLayout.addView(entry.getValue());
        }
        mActivityRootLinearLayout.addView(mBrandLayout);
        mPropertiesChipGroupsBuffer.clear();
    }

    private void addCategoriesAndPropertiesChipGroupsToLayout(ChipGroup chipGroup) {
        if (mHaveCategoriesBeenInflatedOnce) {
            mActivityRootLinearLayout.removeViewsInLayout(mCategoryGroupIndexInLayout,
                    mActivityRootLinearLayout.getChildCount() - mCategoryGroupIndexInLayout);
            mActivityRootLinearLayout.addView(chipGroup, mCategoryGroupIndexInLayout);
            addPropertiesChipGroupsToLayout();
        } else {
            mActivityRootLinearLayout.addView(chipGroup, mCategoryGroupIndexInLayout);
            addPropertiesChipGroupsToLayout();
            mHaveCategoriesBeenInflatedOnce = true;
        }
    }


    private ChipGroup createChipGroup(String groupName) {
        View chipGroupAsView = inflater.inflate(R.layout.chip_group_layout, null);
        ChipGroup chipGroup = (ChipGroup) chipGroupAsView;
        ((TextView) chipGroup.getChildAt(0)).setText(groupName + ": ");
        return chipGroup;
    }

    /**
     * =========================== Methods for loading images ======================================
     */

    public void openFileChooser(View v) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // switch bellow will add a camera capture option in the future
        switch (requestCode) {
            case PICK_IMAGE:
                loadImageFromGallery(resultCode, data);
                break;
            default:
                Log.d("ImplicitIntents", "Can't handle this intent!");
                break;
        }
    }

    private void loadImageFromGallery(int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            mImageUri = data.getData();
            loadUriAsUrlToButton();
            loadImageIntoButtonImage();
        }
    }

    private void loadImageIntoButtonImage() {
        Picasso.get()
                .load(mImageUri)
                .centerCrop()
                .fit()
                .into(mImageButtonUpload);
    }

    private void loadUriAsUrlToButton() {
        final Observer<String> urlObserver = new Observer<String>() {
            @Override
            public void onChanged(String url) {
                mImageUrl = url;

            }
        };
        storageDriver.uploadImage(mImageUri).observe(this, urlObserver);
    }

    /**
     * =========================== Defining Activity Click events ================================
     */

    public void onClickDone(View view) {
        if (mImageUri == null || isThereNoTitle()) {
            createNoPhotoOrTitleDialog();
        } else {
            String itemTitle = mEdTextTitle.getText().toString();
            String itemDescription = this.getString(R.string.no_description_value);
            Date date = Calendar.getInstance().getTime();
            mProperties.put(this.getString(R.string.brand_property_name),
                    Collections.singletonList(mEdTextBrand.getText().toString()));
            List<String> imagesUrls = loadImagesUrls();
            Product productToAdd = new Product(mCategory, mUser.getUid(),
                    itemTitle, itemDescription, 0, 0, date, mProperties, imagesUrls);

            productService.addProduct(productToAdd);
            productService.deleteProduct(mProductToEdit.getId());
            Toast.makeText(this, "Changes Saved!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    private boolean isThereNoTitle() {
        return (mEdTextTitle.getText() == null || mEdTextTitle.getText().toString().equals(""));
    }


    private List<String> loadImagesUrls() {
        List<String> imagesUrls = new LinkedList<>();
        imagesUrls.add(mImageUrl);
        return imagesUrls;
    }


    private void paintRedMissingFields() {
        if (isThereNoTitle()) {
            mEdTextTitle.setBackgroundResource(R.drawable.rounded_edittext_error);
            mEdTextTitle.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mEdTextTitle.setBackgroundResource(R.drawable.rounded_edittext);
                }
            });
        }
        if (mImageUri == null) {
            mImageButtonUpload.setImageResource(R.drawable.ic_add_image_error);
        }
    }

    private void createNoPhotoOrTitleDialog() {
        mNoPhotoOrTitleAlertrDialog = new Dialog(this);
        mNoPhotoOrTitleAlertrDialog.setContentView((R.layout.dialog_no_photo_or_title));
        mNoPhotoOrTitleAlertrDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button gotItButton = mNoPhotoOrTitleAlertrDialog.findViewById(R.id.got_it_button);
        gotItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paintRedMissingFields();
                mNoPhotoOrTitleAlertrDialog.dismiss();
            }
        });
        mNoPhotoOrTitleAlertrDialog.show();
    }

    public void onClickBack(View view) {
        finish();
    }

    @Override
    public void startActivity(Intent intent) {
        intent.putExtra(getString(R.string.user_relay), mUser);
        super.startActivity(intent);
    }

    private void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}