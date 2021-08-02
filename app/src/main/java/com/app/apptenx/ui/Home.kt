package com.app.apptenx.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.apptenx.R
import com.app.apptenx.data.network.NetworkState
import com.app.apptenx.databinding.HomeFragmentBinding
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class Home : Fragment() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var latitude : Double? = null
    private var longitude: Double? = null
    private var cityname : String? = null
    private lateinit var binding : HomeFragmentBinding
    private lateinit var mViewModel : HomeViewModel



    @SuppressLint("SimpleDateFormat")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        mViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        binding.vm = mViewModel
        binding.lifecycleOwner = this

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        getLastLocation()

        if (!allPermissionsGranted()){
            ActivityCompat.requestPermissions(
                requireActivity(),
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        mViewModel.networkcheck().observe(requireActivity(), {
            if (it == NetworkState.ERROR) {
                Toast.makeText(requireActivity(), "Failed to load", Toast.LENGTH_SHORT).show()
            }
            if (it == NetworkState.LOADED) {
                Toast.makeText(requireActivity(), "Loaded", Toast.LENGTH_SHORT).show()
            }
            if (it == NetworkState.LOADING) {
                Toast.makeText(requireActivity(), "Loading", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        if (isLocationEnabled()) {
            mFusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener<Location?> { task ->
                val location = task.result
                if (location == null) {
                    requestNewLocationData()
                } else {
                    latitude = location.latitude.toDouble()
                    longitude = location.longitude.toDouble()

                    Log.d("Logtag2", "$latitude , $longitude")

//                    binding.location.text = "$latitude , $longitude"
                    val addresses: List<Address>
                    val geocoder: Geocoder = Geocoder(requireActivity(), Locale.getDefault())

                    addresses = geocoder.getFromLocation(
                        latitude!!,
                        longitude!!,
                        1
                    ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    cityname = addresses[0].locality
                    if (!cityname.isNullOrEmpty()){
                        mViewModel.getforecast(cityname!!)
                        mViewModel.getweather(cityname!!)
                        mViewModel.getweather(cityname.toString())?.observe(requireActivity(), {
                            binding.progressBar.visibility = View.GONE
                            binding.layFrag.visibility = View.VISIBLE
                            mViewModel.name.postValue(it.name)
                            mViewModel.temp.postValue((it.main.temp.toInt().toString()) + "\u00B0")
                        })

                        mViewModel.getforecast(this.cityname.toString())?.observe(requireActivity(), {
                            binding?.laycard.visibility = View.VISIBLE
                            val arrday = ArrayList<String>()
                            val arrtemp = ArrayList<String>()

                            for (i in 0..it!!.list.lastIndex) {
                                val date = DateFormat.format("EEEE", Date(it.list[i].dt.toLong() * 1000))
                                if (!arrday.contains(date)) {
                                    arrtemp.add(it.list[i].main.temp.toInt().toString())
                                    arrday.add(date.toString())
                                }
                            }
                            mViewModel.day1.postValue(arrday[1])
                            mViewModel.day2.postValue(arrday[2])
                            mViewModel.day3.postValue(arrday[3])
                            mViewModel.day4.postValue(arrday[4])
                            //Temp
                            mViewModel.day1temp.postValue(arrtemp[1]+ "\u00B0"+"C")
                            mViewModel.day2temp.postValue(arrtemp[2]+ "\u00B0"+"C")
                            mViewModel.day3temp.postValue(arrtemp[3]+ "\u00B0"+"C")
                            mViewModel.day4temp.postValue(arrtemp[4]+ "\u00B0"+"C")
                        })
                    } else {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            REQUIRED_PERMISSIONS,
                            REQUEST_CODE_PERMISSIONS
                        )
                    }

                }
            })
        } else {
            Toast.makeText(context, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                .show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)

        }
    }

    // if location is enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        // Initializing LocationRequest
        // object with appropriate methods
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1
        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            latitude = mLastLocation.latitude
            longitude = mLastLocation.longitude
            Log.d("Logtag12", "$latitude , $longitude")
        }
    }
}