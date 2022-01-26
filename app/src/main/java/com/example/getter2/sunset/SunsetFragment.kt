package com.example.getter2.sunset

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.getter2.sunset.api.SunsetRetriever
import com.example.getter2.utils.Utils
import com.example.getter2.BuildConfig
import com.example.getter2.MainActivity
import com.example.getter2.R
import com.example.getter2.databinding.FragmentMainBinding
import com.example.getter2.ui.main.PageViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue


class SunsetFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null

    private val sunsets = arrayOf(
        R.drawable.ss01,
        R.drawable.ss02,
        R.drawable.ss03
    )

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var ctx: Context
    private val SUNSET_STRING = "SunsetString"
    private lateinit var sunsetView: FrameLayout
    private lateinit var mSunset: TextView
    private lateinit var mTimer: TextView
    private lateinit var mSunsetImageView: ImageView
    private lateinit var imgCardView: CardView
    private var cardViewHeight: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var frameLayout: FrameLayout
    private var widthFrame: Int = 0
    private var timeOfSunset: String? = null
    private var sunsetMessage: String? = null


    // The Fused Location Provider provides access to location APIs.
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(ctx)
    }

    // Allows class to cancel the location request if it exits the activity.
    // Typically, you use one cancellation source per lifecycle.
    private var cancellationTokenSource = CancellationTokenSource()

    // If the user denied a previous permission request, but didn't check "Don't ask again", this
    // Snackbar provides an explanation for why user should approve, i.e., the additional rationale.
    private val fineLocationRationalSnackbar by lazy {
        Snackbar.make(
            sunsetView,
            R.string.fine_location_permission_rationale,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.ok) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        timeOfSunset = savedInstanceState?.getString(SUNSET_STRING)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ctx = container?.context as Context
        return inflater.inflate(R.layout.fragment_sunset, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sunsetView = view.findViewById(R.id.container)
        mSunset = view.findViewById(R.id.sunset_tv)
        mTimer = view.findViewById(R.id.sunset_timer_tv)
        mSunsetImageView = view.findViewById(R.id.sunset_iv)
        progressBar = view.findViewById(R.id.progress_bar)
        frameLayout = view.findViewById(R.id.container)
        imgCardView = view.findViewById(R.id.cardView3_img)
        calculationAndSetTimer()
        progressBar.visibility = View.GONE

        val viewTreeObserver = frameLayout.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    widthFrame = frameLayout.width
                }
            })
        }

    }

    override fun onResume() {
        super.onResume()
        if (!userVisibleHint) {
            return
        }
        val activityFab: FloatingActionButton = (activity as? MainActivity)?.binding?.fab
            ?: error("Can only access if attached to MainActivity")

        activityFab.setOnClickListener(View.OnClickListener {
            if (timeOfSunset != null) {
                calculationAndSetTimer()
            } else {
                getLocationRequest()
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SUNSET_STRING, timeOfSunset)
    }

    override fun onStop() {
        super.onStop()
        // Cancels location request (if in flight).
        cancellationTokenSource.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult: ")
        if (requestCode == REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() ->
                    // If user interaction was interrupted, the permission request
                    // is cancelled and you receive an empty array.
                    Log.d(TAG, "User interaction was cancelled.")

                grantResults[0] == PackageManager.PERMISSION_GRANTED ->
                    Snackbar.make(
                        sunsetView,
//                        findViewById(R.id.container),
                        R.string.permission_approved_explanation,
                        Snackbar.LENGTH_LONG
                    ).show()

                else -> {
                    //Permission denied
                    Snackbar.make(
                        sunsetView,
//                        findViewById(R.id.container),
                        R.string.fine_permission_denied_explanation,
                        Snackbar.LENGTH_LONG
                    ).setAction(R.string.app_name) {

                        //Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }.show()
                }
            }
        }
    }

    private fun retrieve(latitude: Double, longitude: Double) {
        Log.d(TAG, "retrieve: GETTING COORDINATES")
        // Create a Coroutine scope using a job to be able to cancel when needed
        val adviceActivityJob = Job()

        // Handle exceptions if any
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(ctx).setTitle("Error")
                .setMessage(exception.message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(R.drawable.ic_apple).show()
        }

        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(adviceActivityJob + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {
            val result = SunsetRetriever().getSunset(latitude, longitude)
            // set text
            timeOfSunset = result.results.sunset.toDate().formatTo("HH:mm:ss")
            Log.d(TAG, "retrieve: $timeOfSunset")
            Log.d(TAG, "retrieve, orig: ${result.results.sunset}")
            Log.d(TAG, "retrieve, toDate: ${result.results.sunset.toDate()}")
            calculationAndSetTimer()
        }
    }

    private fun getLocationRequest() {
        Log.d(TAG, "getLocationRequest: ")
        val permissionApproved =
            ctx.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionApproved) {
            requestCurrentLocation()
        } else {
            requestPermissionWithRationale(
                Manifest.permission.ACCESS_FINE_LOCATION,
                REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE,
                fineLocationRationalSnackbar
            )
        }
    }

    /**
     * Gets current location.
     * Note: The code checks for permission before calling this method, that is, it's never called
     * from a method with a missing permission. Also, I include a second check with my extension
     * function in case devs just copy/paste this code.
     */
    @SuppressLint("MissingPermission")
    private fun requestCurrentLocation() {
        Log.d(TAG, "requestCurrentLocation: ")
        progressBar.visibility = View.VISIBLE
        if (ctx.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Returns a single current location fix on the device. Unlike getLastLocation() that
            // returns a cached location, this method could cause active location computation on the
            // device. A single fresh location will be returned if the device location can be
            // determined within reasonable time (tens of seconds), otherwise null will be returned.
            //
            // Both arguments are required.
            // PRIORITY type is self-explanatory. (Other options are PRIORITY_BALANCED_POWER_ACCURACY,
            // PRIORITY_LOW_POWER, and PRIORITY_NO_POWER.)
            // The second parameter, [CancellationToken] allows the activity to cancel the request
            // before completion.
            val currentLocationTask: Task<Location> = fusedLocationClient.getCurrentLocation(
                PRIORITY_HIGH_ACCURACY, cancellationTokenSource.token
            )

            currentLocationTask.addOnCompleteListener { task: Task<Location> ->
                Log.d(TAG, "task is Successful ${task.isSuccessful} ")
                Log.d(TAG, "task result is not null ${task.result != null} ")
                val result = if (task.isSuccessful && task.result != null) {
                    val result: Location = task.result
                    Log.d(TAG, "Location (success): ${result.latitude}, ${result.longitude}")
                    Log.d(TAG, "Internet: ${Utils.isNetworkConnected(ctx)} ")
                    if (Utils.isNetworkConnected(ctx)) {
                        Log.d(TAG, "requestCurrentLocation: retrieve")
                        retrieve(result.latitude, result.longitude)
                    } else {
                        Log.d(TAG, "requestCurrentLocation: no Internet connection")
                        AlertDialog.Builder(ctx).setTitle("No Internet Connection")
                            .setMessage("Please check your internet connection and try again")
                            .setPositiveButton(R.string.ok) { _, _ -> }
                            .setIcon(R.drawable.ic_apple).show()
                    }
//                    mLatitude = result.latitude.toString()
//                    mLongitude = result.longitude.toString()
                } else {
                    val exception = task.exception
                    "Location (failure): $exception"
                    Log.d(TAG, "requestCurrentLocation: Location (failure): $exception")
                }

                Log.d(TAG, "getCurrentLocation() result: $result")
            }
        } else {
            Log.d(TAG, "requestCurrentLocation: SOMETHING WRONG WITH PERMISSIONS")
        }
        progressBar.visibility = View.GONE
    }

    /**
     * Helper functions to simplify permission checks/requests.
     */
    private fun Context.hasPermission(permission: String): Boolean {
        Log.d(TAG, "hasPermission:")

        // Background permissions didn't exit prior to Q, so it's approved by default.
        if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
            android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
        ) {
            return true
        }
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests permission and if the user denied a previous request, but didn't check
     * "Don't ask again", we provide additional rationale.
     *
     * Note: The [Snackbar] should have an action to request the permission.
     */
    private fun requestPermissionWithRationale(
        permission: String,
        requestCode: Int,
        snackbar: Snackbar
    ) {
        Log.d(TAG, "requestPermissionWithRationale: ")
        val provideRationale = shouldShowRequestPermissionRationale(permission)
        if (provideRationale) {
            snackbar.show()
        } else {
            requestPermissions(arrayOf(permission), requestCode)
        }
    }

    private fun setUi() {

        //set cardview height for main image
        if (cardViewHeight == 300 || cardViewHeight == 0) {
            cardViewHeight = if (widthFrame == 0) 300 else {
                ((widthFrame - 32) / 1.3).toInt()
            }
        }

        val layoutParams = imgCardView.layoutParams
        layoutParams.height = cardViewHeight

        val options = RequestOptions()
        options.centerCrop()
        Glide.with(mSunsetImageView.context)
            .load(sunsets.random())
            .apply(options)
            .into(mSunsetImageView)
        mSunset.text = sunsetMessage
    }

    private fun calculationAndSetTimer() {
        Log.d(TAG, "calculationAndSetTimer: $timeOfSunset")

        if (timeOfSunset != null) {
            val currentTime = getCurrentDateTime().toString("HH:mm:ss")
            Log.d(TAG, "current time: $currentTime")
            val sunsetTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(timeOfSunset)
                .toString("HH:mm:ss")
            Log.d(TAG, "sunset time: $sunsetTime")
            val currentDtm = currentTime.toDate("HH:mm:ss")
            val sunsetDtm = sunsetTime.toDate("HH:mm:ss")
            val diffInMilliSec: Long = (currentDtm.time - sunsetDtm.time).absoluteValue
            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMilliSec)
            Log.d(TAG, "difference in min: $diffInMin")

            if (diffInMin < 15 && currentDtm.before(sunsetDtm)) {
                sunsetMessage = "The sunset will begin at $timeOfSunset. Hurry up."
                startTimer(diffInMilliSec)

            } else if (currentDtm.before(sunsetDtm)) {
                sunsetMessage =
                    "The sunset will be at $timeOfSunset. You have enough time to see it"
                startTimer(diffInMilliSec)

            } else if (sunsetDtm.after(currentDtm) && diffInMin < 15) {
                sunsetMessage = "The sunset has been started. Just find the closest spot and enjoy."
                startTimer(diffInMilliSec)

            } else {
                sunsetMessage = "The sunset was at $timeOfSunset. You lost this sunset. It's gone."
            }
        } else {
            sunsetMessage = "Want to enjoy the sunset? Push the button."
        }
        setUi()
    }

    private fun startTimer(timeToSunsetMills: Long) {
        val timer = object : CountDownTimer(timeToSunsetMills, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val f: NumberFormat = DecimalFormat("00")
                val hour = millisUntilFinished / 3600000 % 24
                val min = millisUntilFinished / 60000 % 60
                val sec = millisUntilFinished / 1000 % 60
                mTimer.text = f.format(hour)
                    .plus(":")
                    .plus(f.format(min))
                    .plus(":")
                    .plus(f.format(sec))
            }

            override fun onFinish() {
                mTimer.text = "Now it's time to enjoy your sunset"
            }
        }
        timer.start()
    }

    override fun setUserVisibleHint(visible: Boolean) {
        super.setUserVisibleHint(visible)
        if (visible && isResumed) {
            onResume()
        }
    }

    private fun String.toDate(
        dateFormat: String = "hh:mm:ss a",
        timeZone: TimeZone = TimeZone.getTimeZone("UTC")
    ): Date {
        val parser = SimpleDateFormat(dateFormat, Locale.getDefault())
        parser.timeZone = timeZone
        return parser.parse(this)
    }

    private fun Date.formatTo(
        dateFormat: String,
        timeZone: TimeZone = TimeZone.getDefault()
    ): String {
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = timeZone
        return formatter.format(this)
    }

    private fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    private fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

    companion object {
        private const val TAG = "SunsetActivity"
        private const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "name"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): SunsetFragment {
            return SunsetFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

}