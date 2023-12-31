package com.example.quixhouse

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.quixhouse.databinding.ActivityPostBinding
import com.example.quixhouse.model.Post
import com.example.quixhouse.utils.PermissionUtils
import com.example.quixhouse.utils.PermissionUtils.PermissionDeniedDialog.Companion.newInstance
import com.example.quixhouse.utils.PermissionUtils.isPermissionGranted
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for [Manifest.permission.ACCESS_FINE_LOCATION] and [Manifest.permission.ACCESS_COARSE_LOCATION]
 * are requested at run time. If either permission is not granted, the Activity is finished with an error message.
 */
class PostActivity : AppCompatActivity(),
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener, OnMapReadyCallback,
    OnRequestPermissionsResultCallback {

    private lateinit var binding: ActivityPostBinding
    private var latLng: LatLng? = null
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * [.onRequestPermissionsResult].
     */
    private var permissionDenied = false
    private lateinit var map: GoogleMap
    private val ZOOM_LEVEL = 20f

    private lateinit var post: Post

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        post = intent.getParcelableExtra("post_data", Post::class.java)!!

        if (post != null) {
            binding.descriptionPost.text = post.description
            Glide.with(this)
                .load(post.image)
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.ic_image_default) // Imagem de placeholder, se desejar
                        .error(R.drawable.ic_image_not_supported) // Imagem de erro, se desejar
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                ) // Estratégia de armazenamento em cache
                .into(binding.imagePost)

            binding.addrPost.text = "${post.locationAddress.address}, ${post.locationAddress.neighborhood},  Nº ${post.locationAddress.numberAp}"
            binding.cityEstatePost.text = "${post.locationAddress.city}  -  ${post.locationAddress.state}"
        }

        initClicks()
    }

    private fun initClicks() {
        binding.btnOpenMaps.setOnClickListener {
            latLng?.let  {
                val latitude = it.latitude
                val longitude = it.longitude

                // Cria uma Uri com as coordenadas do marcador
//                val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude")
                val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")

                // Cria uma intenção para abrir o Google Maps
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")

                // Verifica se o Google Maps está instalado no dispositivo
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent) // Inicie a intenção
                } else {
                    Toast.makeText(
                        this, "Google Maps não está instalado no seu dispositivo", Toast.LENGTH_SHORT
                    ).show()                }
            }
        }

    }



    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

//        googleMap.setOnMyLocationButtonClickListener(this)
//        googleMap.setOnMyLocationClickListener(this)
//        enableMyLocation()
        // Desabilita interações no mapa
        map.uiSettings.isZoomControlsEnabled = false
        map.uiSettings.isZoomGesturesEnabled = false
        map.uiSettings.isScrollGesturesEnabled = false
        map.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = false
        map.uiSettings.isTiltGesturesEnabled = false
        map.uiSettings.isRotateGesturesEnabled = false
        latLng = post?.let {
            LatLng(
                it.locationAddress.latitude!!,
                it.locationAddress.longitude!!
            )
        }
        latLng?.let {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, ZOOM_LEVEL))
            map.addMarker(MarkerOptions().position(it))
        }

    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {

        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            return
        }

        // 2. If if a permission rationale dialog should be shown
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            PermissionUtils.RationaleDialog.newInstance(
                LOCATION_PERMISSION_REQUEST_CODE, true
            ).show(supportFragmentManager, "dialog")
            return
        }

        // 3. Otherwise, request permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_REQUEST_CODE
        )
        // [END maps_check_location_permission]
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    // [START maps_check_location_permission_result]
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            return
        }

        if (isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || isPermissionGranted(
                permissions,
                grantResults,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation()
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true
            // [END_EXCLUDE]
        }
    }

    // [END maps_check_location_permission_result]
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError()
            permissionDenied = false
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        newInstance(true).show(supportFragmentManager, "dialog")
    }

    companion object {
        /**
         * Request code for location permission request.
         *
         * @see .onRequestPermissionsResult
         */
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}