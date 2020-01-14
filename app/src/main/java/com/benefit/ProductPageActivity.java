package com.benefit;

import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.benefit.model.Product;
import com.benefit.model.User;
import com.benefit.services.Factory;
import com.benefit.services.ProductService;
import com.benefit.services.UserService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductPageActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int productId = 1;
    private ProductService productService;
    private UserService userService;

    private GoogleMap mMap;
    private Location mapLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_page);
        this.productService = ViewModelProviders.of(this, Factory.getProductServiceFactory()).get(ProductService.class);
        this.userService = ViewModelProviders.of(this, Factory.getUserServiceFactory()).get(UserService.class);
        final Observer<Product> productObserver = this::displayProductOnPage;
        productService.getProductById(this.productId).observe(this, productObserver);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void displayProductOnPage(Product product) {
        TextView textViewTitle = findViewById(R.id.textviewProductPageTitle);
        TextView textViewDescription = findViewById(R.id.textviewProductPageDescription);
        TextView textViewProperties = findViewById(R.id.textviewProductPageProperties);
        textViewTitle.setText(product.getTitle());
        textViewDescription.setText(product.getDescription());
        textViewProperties.setText(getPropertiesString(product.getProperties()));
        final Observer<User> userObserver = this::displayMap;
        userService.getUserById(product.getSellerId()).observe(this, userObserver);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getPropertiesString(Map<String, List<String>> propertiesMap) {
        StringBuilder propertiesString = new StringBuilder();
        for (String name : propertiesMap.keySet()) {
            propertiesString
                    .append(name)
                    .append(":")
                    .append(" ")
                    .append(String.join(getString(R.string.productPagePropertiesValuesSeparator) + " ", propertiesMap.get(name)));
            propertiesString
                    .append(" ")
                    .append(getString(R.string.productPagePropertiesSeparator))
                    .append(" ");
        }
        return propertiesString.toString();
    }

    private void displayMap(User user) {
        this.mapLocation = new Location(LocationManager.GPS_PROVIDER);
        this.mapLocation.setLongitude(user.getLocationLongitude());
        this.mapLocation.setLatitude(user.getLocationLatitude());
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.productPageMapFragment);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;
        LatLng coordinates = new LatLng(mapLocation.getLatitude(), mapLocation.getLongitude());
        mMap.addMarker(new MarkerOptions()
                .position(coordinates)
                .title("Marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
    }
}
