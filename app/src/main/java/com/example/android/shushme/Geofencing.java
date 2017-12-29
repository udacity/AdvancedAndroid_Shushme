package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Erfan on 29/12/2017.
 */

public class Geofencing implements ResultCallback<Status>
{
    private static final String TAG = Geofencing.class.getSimpleName();
    private List<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private int GEOFENCE_RADIUS = 10;
    private Long GEOFENCE_TIMEOUT = Geofence.NEVER_EXPIRE;

    public Geofencing(Context context, GoogleApiClient googleApiClient)
    {
        this.mContext = context;
        this.mGoogleApiClient = googleApiClient;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void registerAllGeofences()
    {
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofenceList == null || mGeofenceList.size() == 0)
            return;
        try
        {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()).setResultCallback(this);
        }
        catch (SecurityException securityException)
        {
            Log.e(TAG, securityException.getMessage());
        }
    }


    public void unregisterAllGeofences()
    {
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;
        try
        {
            LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                    getGeofencePendingIntent()).setResultCallback(this);
        }
        catch (SecurityException securityException)
        {
            Log.e(TAG, securityException.getMessage());
        }
    }

    public void updateGeofenceList(PlaceBuffer places)
    {
        mGeofenceList = new ArrayList<>();
        if(places == null || places.getCount() == 0)
            return;
        for(Place place : places)
        {
            String placeUid = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLong = place.getLatLng().longitude;
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUid)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLong, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeofenceList.add(geofence);

        }
    }
    private GeofencingRequest getGeofencingRequest()
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
    private PendingIntent getGeofencePendingIntent()
    {
        if(mGeofencePendingIntent != null)
            return mGeofencePendingIntent;
        Intent intent = new Intent(mContext, GeofenceBroadcastReciver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    @Override
    public void onResult(@NonNull Status status)
    {
        Log.e(TAG, String.format("Error adding/removing geofence : %s", status.getStatus().toString()));
    }
}
