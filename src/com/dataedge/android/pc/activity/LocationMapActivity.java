package com.dataedge.android.pc.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dataedge.android.pc.Codes;
import com.dataedge.android.pc.R;
import com.dataedge.android.pc.Utils;
import com.dataedge.android.pc.model.LocationModel;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class LocationMapActivity extends MapActivity {
    String TAG = LocationMapActivity.class.getName();
    MapView mapView;
    MapController mc;
    GeoPoint point;
    GeoPoint x;
    List<Overlay> overlays;
    MyItemizedOverlay itemizedOverlay;
    OverlayItem overlayItem;
    Drawable drawable;

    String locatorCode;
    String reportStatus;
    String locatorFileExt;
    String reportTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        setContentView(R.layout.location);

        //

    }

    protected void onStart() {
        Log.i(TAG, "onStart()");

        super.onStart();

        // get passing extras
        locatorCode = getIntent().getStringExtra("LOCATOR_CODE");
        reportStatus = getIntent().getStringExtra("REPORT_STATUS");
        locatorFileExt = getIntent().getStringExtra("FILE_EXT");
        reportTitle = getIntent().getStringExtra("REPORT_TITLE");

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.setBuiltInZoomControls(true);
        mapView.displayZoomControls(true);

        // where to ..
        mc = mapView.getController();
        point = getHomeLocation();
        mc.animateTo(point);
        mc.setZoom(17);

        // setup gesture-capturing-overlay
        overlays = mapView.getOverlays();
        overlays.clear();
        overlays.add(new MapGestureDetectorOverlay(new GestureDetector.SimpleOnGestureListener()));

        // prepare the pin marker
        drawable = this.getResources().getDrawable(R.drawable.pin_down);
        itemizedOverlay = new MyItemizedOverlay(drawable, this);

        // map view type button
        // button to report
        final Button btnMapViewType = (Button) findViewById(R.id.btnMapViewType);

        // default to map
        mapView.setSatellite(false);
        btnMapViewType.setText(Codes.MAP_VIEW_TYPE_SATELLITE);

        btnMapViewType.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                // Perform action on clicks
                if (btnMapViewType.getText().equals(Codes.MAP_VIEW_TYPE_SATELLITE)) {
                    mapView.setSatellite(true);
                    btnMapViewType.setText(Codes.MAP_VIEW_TYPE_MAP);
                } else if (btnMapViewType.getText().equals(Codes.MAP_VIEW_TYPE_MAP)) {
                    mapView.setSatellite(false);
                    btnMapViewType.setText(Codes.MAP_VIEW_TYPE_SATELLITE);
                }
                mapView.displayZoomControls(true);
            }
        });

        ImageButton btnHomeLocation = (ImageButton) findViewById(R.id.btnHomeLocation);
        ImageButton btnSaveLocation = (ImageButton) findViewById(R.id.btnSaveLocation);
        if (reportStatus != null && !reportStatus.equals(Codes.RPT_STATUS_CURRENT)) {
            btnHomeLocation.setImageResource(R.drawable.globe_off);
            btnHomeLocation.setEnabled(false);
            btnSaveLocation.setImageResource(R.drawable.pin_black_btn_off);
            btnSaveLocation.setEnabled(false);
        }
        // home click listener
        btnHomeLocation.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                point = getHomeLocation();
                mc.animateTo(point);
                mc.setZoom(17);
            }
        });

        // save click listener
        btnSaveLocation.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(LocationMapActivity.this, "Saved!", Toast.LENGTH_SHORT).show();

            }
        });

    }

    // go to device location
    private GeoPoint getHomeLocation() {

        LocationModel lm = Utils.getLocationAttributes(getApplicationContext());
        if (lm != null) {
            double lat = lm.getLatitude();
            double lng = lm.getLongitude();

            return new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
        } else {
            return new GeoPoint(19240000, -99120000); // Mexico City

        }

    }

    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onBackPressed() {
        // start report activity
        Intent myIntent = new Intent(getApplicationContext(), ReportActivity.class);
        myIntent.putExtra("LOCATOR_CODE", locatorCode);
        myIntent.putExtra("REPORT_STATUS", reportStatus);
        myIntent.putExtra("FILE_EXT", locatorFileExt);
        myIntent.putExtra("REPORT_TITLE", reportTitle);

        startActivityForResult(myIntent, 0);
    }

    // inner class to handle gesture detection
    class MapGestureDetectorOverlay extends Overlay implements OnGestureListener {
        private GestureDetector gestureDetector;
        private OnGestureListener onGestureListener;

        public MapGestureDetectorOverlay() {
            gestureDetector = new GestureDetector(this);
        }

        public MapGestureDetectorOverlay(OnGestureListener onGestureListener) {
            this();
            setOnGestureListener(onGestureListener);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            if (onGestureListener != null) {
                return onGestureListener.onDown(e);
            }
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (onGestureListener != null) {
                return onGestureListener.onFling(e1, e2, velocityX, velocityY);
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (onGestureListener != null) {
                onGestureListener.onLongPress(e);
                point = mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY());
                Context context = itemizedOverlay.getContext(); // had to force
                                                                // the same
                                                                // context
                overlays.remove(itemizedOverlay);
                itemizedOverlay = new MyItemizedOverlay(drawable, context);
                // get address
                String line1 = null;
                String line2 = null;
                try {
                    final Geocoder coder = new Geocoder(getApplicationContext(),
                            Locale.getDefault());
                    Double lat = new Double(point.getLatitudeE6());
                    Double lon = new Double(point.getLongitudeE6());
                    List<Address> geocodeResults = coder.getFromLocation(
                            lat.doubleValue() / 1000000, lon.doubleValue() / 1000000, 1);
                    Iterator<Address> locations = geocodeResults.iterator();

                    if (locations.hasNext()) {
                        Address loc = locations.next();
                        if (!Utils.isNumeric(loc.getFeatureName())) {
                            line1 = (loc.getFeatureName() == null ? "" : loc.getFeatureName());
                        }
                        line2 = (loc.getAddressLine(0) == null ? "" : loc.getAddressLine(0)) + "\n"
                                + (loc.getLocality() == null ? "" : loc.getLocality() + " ")
                                + (loc.getAdminArea() == null ? "" : loc.getAdminArea() + " ")
                                + (loc.getPostalCode() == null ? "" : loc.getPostalCode()) + "\n"
                                + (loc.getCountryName() == null ? "" : loc.getCountryName());

                    }

                } catch (Exception ge) {

                    Log.e(TAG, ge.toString());
                }

                //
                overlayItem = new OverlayItem(point, line1, line2);
                itemizedOverlay.addOverlay(overlayItem);
                // add the overlay
                overlays.add(itemizedOverlay);

            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (onGestureListener != null) {
                onGestureListener.onScroll(e1, e2, distanceX, distanceY);
            }
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            if (onGestureListener != null) {
                onGestureListener.onShowPress(e);
            }
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (onGestureListener != null) {
                onGestureListener.onSingleTapUp(e);
            }
            return false;
        }

        public boolean isLongpressEnabled() {
            return gestureDetector.isLongpressEnabled();
        }

        public void setIsLongpressEnabled(boolean isLongpressEnabled) {
            gestureDetector.setIsLongpressEnabled(isLongpressEnabled);
        }

        public OnGestureListener getOnGestureListener() {
            return onGestureListener;
        }

        public void setOnGestureListener(OnGestureListener onGestureListener) {
            this.onGestureListener = onGestureListener;
        }
    }

    //
    @SuppressWarnings("rawtypes")
    class MyItemizedOverlay extends ItemizedOverlay {
        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
        Context mContext;

        public MyItemizedOverlay(Drawable defaultMarker) {
            super(boundCenterBottom(defaultMarker));
        }

        public MyItemizedOverlay(Drawable defaultMarker, Context context) {
            super(boundCenterBottom(defaultMarker));
            mContext = context;
        }

        public void addOverlay(OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mOverlays.get(i);
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        protected boolean onTap(int index) {
            OverlayItem item = mOverlays.get(index);
            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle(item.getTitle());
            dialog.setMessage(item.getSnippet());
            dialog.show();
            return true;
        }

        public Context getContext() {
            return mContext;
        }

    }

}
