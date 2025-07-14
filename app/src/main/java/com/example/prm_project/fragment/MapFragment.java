package com.example.prm_project.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.prm_project.R;
import com.example.prm_project.model.StoreLocation;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements LocationListener {

    private MapView mapView;
    private ImageView btnBack, btnMyLocation;
    private MyLocationNewOverlay myLocationOverlay;
    private LocationManager locationManager;
    private List<StoreLocation> storeLocations;
    private IMapController mapController;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Configure osmdroid
        Configuration.getInstance().load(getContext(), 
            getContext().getSharedPreferences("osmdroid", 0));

        View view = inflater.inflate(R.layout.fragment_map, container, false);

        initViews(view);
        initStoreLocations();
        setupMap();
        setupClickListeners();
        checkLocationPermission();

        return view;
    }

    private void initViews(View view) {
        mapView = view.findViewById(R.id.mapView);
        btnBack = view.findViewById(R.id.btnBack);
        btnMyLocation = view.findViewById(R.id.btnMyLocation);
    }

    private void initStoreLocations() {
        storeLocations = new ArrayList<>();
        
        // Thêm các chi nhánh cửa hàng sách
        storeLocations.add(new StoreLocation(
            "📚 BookStore Quận 1", 
            "123 Nguyễn Huệ, Bến Nghé, Quận 1, TP.HCM",
            "08:00 - 22:00",
            "0283 123 4567",
            10.7769, 106.7009
        ));
        
        storeLocations.add(new StoreLocation(
            "📚 BookStore Quận 3", 
            "456 Võ Văn Tần, Phường 5, Quận 3, TP.HCM",
            "08:00 - 21:00",
            "0283 234 5678",
            10.7829, 106.6928
        ));
        
        storeLocations.add(new StoreLocation(
            "📚 BookStore Thủ Đức", 
            "789 Võ Văn Ngân, Linh Chiều, Thủ Đức, TP.HCM",
            "08:00 - 21:30",
            "0283 345 6789",
            10.8231, 106.7694
        ));
        
        storeLocations.add(new StoreLocation(
            "📚 BookStore Hà Nội", 
            "321 Hoàng Diệu, Ngọc Hà, Ba Đình, Hà Nội",
            "08:00 - 21:00",
            "024 456 7890",
            21.0285, 105.8542
        ));
        
        storeLocations.add(new StoreLocation(
            "📚 BookStore Đà Nẵng", 
            "654 Trần Phú, Thạch Thang, Hải Châu, Đà Nẵng",
            "08:00 - 21:00",
            "0236 567 8901",
            16.0544, 108.2022
        ));

        storeLocations.add(new StoreLocation(
            "📚 BookStore Cần Thơ", 
            "159 Hùng Vương, Thới Bình, Ninh Kiều, Cần Thơ",
            "08:00 - 20:30",
            "0292 678 9012",
            10.0452, 105.7469
        ));
    }

    private void setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        mapController = mapView.getController();
        mapController.setZoom(6.0);
        
        // Center on Vietnam
        GeoPoint startPoint = new GeoPoint(16.0544, 108.2022);
        mapController.setCenter(startPoint);

        // Add my location overlay
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Add store markers
        addStoreMarkers();
    }

    private void addStoreMarkers() {
        for (StoreLocation store : storeLocations) {
            Marker marker = new Marker(mapView);
            marker.setPosition(new GeoPoint(store.getLatitude(), store.getLongitude()));
            marker.setTitle(store.getName());
            marker.setSubDescription(store.getAddress());
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            
            // Set marker icon (you can create custom drawable)
            marker.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_mylocation));
            
            marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    showStoreDetails(store);
                    
                    // Zoom to marker
                    mapController.animateTo(marker.getPosition());
                    mapController.setZoom(15.0);
                    
                    return true;
                }
            });
            
            mapView.getOverlays().add(marker);
        }
        
        // Refresh map
        mapView.invalidate();
    }

    private void showStoreDetails(StoreLocation store) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("🏪 " + store.getName());
        builder.setMessage(
            "📍 Địa chỉ: " + store.getAddress() + "\n\n" +
            "🕒 Giờ hoạt động: " + store.getWorkingHours() + "\n\n" +
            "📞 Điện thoại: " + store.getPhone() + "\n\n" +
            "💡 Nhấn các nút bên dưới để thực hiện hành động"
        );

        builder.setPositiveButton("📞 Gọi ngay", (dialog, which) -> {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + store.getPhone()));
            startActivity(callIntent);
        });

        builder.setNegativeButton("🗺️ Chỉ đường", (dialog, which) -> {
            openDirections(store);
        });

        builder.setNeutralButton("📍 Zoom đến", (dialog, which) -> {
            GeoPoint storePoint = new GeoPoint(store.getLatitude(), store.getLongitude());
            mapController.animateTo(storePoint);
            mapController.setZoom(17.0);
        });

        builder.show();
    }

    private void openDirections(StoreLocation store) {
        // Try to open Google Maps first
        Intent mapIntent = new Intent(Intent.ACTION_VIEW,
            Uri.parse("google.navigation:q=" + store.getLatitude() + "," + store.getLongitude()));
        mapIntent.setPackage("com.google.android.apps.maps");
        
        if (mapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback to generic map intent
            Intent genericMapIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:" + store.getLatitude() + "," + store.getLongitude() + "?q=" + store.getName()));
            
            if (genericMapIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(genericMapIntent);
            } else {
                Toast.makeText(getContext(), "❌ Không thể mở ứng dụng bản đồ", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        btnMyLocation.setOnClickListener(v -> {
            if (myLocationOverlay.getMyLocation() != null) {
                mapController.animateTo(myLocationOverlay.getMyLocation());
                mapController.setZoom(15.0);
                Toast.makeText(getContext(), "📍 Đã zoom đến vị trí của bạn", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "❌ Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            
            requestPermissions(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableLocationFeatures();
        }
    }

    private void enableLocationFeatures() {
        if (myLocationOverlay != null) {
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures();
                Toast.makeText(getContext(), "✅ Đã cấp quyền vị trí", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "❌ Cần quyền vị trí để hiển thị vị trí hiện tại", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        // Handle location updates if needed
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}