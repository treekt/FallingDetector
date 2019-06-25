package pl.treekt.fallingdetector.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pl.treekt.fallingdetector.R;

public class SingleShotLocationProvider {

    private static final int GEOCODER_NUMBER_OF_RESULTS = 1;
    private static final int GEOCODER_RESULT_INDEX = 0;

    @SuppressLint("MissingPermission")
    public static void requestSingleUpdate(Context context, LocationCallback callback) {
        final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Criteria criteria = new Criteria();
        if (isGPSEnabled) {
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
        } else if (isNetworkEnabled) {
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        }
        locationManager.requestSingleUpdate(criteria, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Address geocoderAddress = getGeocoderAddress(location, context);
                DetectorLocationObject detectorLocationObject = prepareLocationProperties(geocoderAddress);
                callback.onNewLocationAvailable(detectorLocationObject);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        }, null);

    }

    private static DetectorLocationObject prepareLocationProperties(@NonNull Address address) {
        DetectorLocationObject detectorLocationObject = new DetectorLocationObject();

        detectorLocationObject.setCity(address.getLocality());
        detectorLocationObject.setCountry(address.getCountryName());
        detectorLocationObject.setLatitude(address.getLatitude());
        detectorLocationObject.setLongitude(address.getLongitude());
        detectorLocationObject.setPostalCode(address.getPostalCode());
        detectorLocationObject.setStreet(address.getThoroughfare());
        detectorLocationObject.setStreetNumber(address.getSubThoroughfare());

        return detectorLocationObject;
    }

    private static Address getGeocoderAddress(Location location, Context context) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        try {
            List<Address> geoCoderAddresses;
            geoCoderAddresses = geocoder.getFromLocation(latitude, longitude, GEOCODER_NUMBER_OF_RESULTS);

            return geoCoderAddresses.get(GEOCODER_RESULT_INDEX);
        } catch (IOException e) {
            Log.e(Geocoder.class.getName(), context.getString(R.string.location_service_geocoder), e);
            e.printStackTrace();
        }

        return null;
    }

    public interface LocationCallback {
        void onNewLocationAvailable(DetectorLocationObject location);
    }
}
