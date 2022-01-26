package com.example.getter2.name

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.getter2.name.api.NameRetriever
import com.example.getter2.utils.Utils
import com.example.getter2.R
import com.example.getter2.databinding.FragmentMainBinding
import com.example.getter2.ui.main.PageViewModel
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import com.example.getter2.MainActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class NameFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val DB_STRING = "DataBaseString"
    private lateinit var ctx: Context
    private lateinit var mMeme: TextView
    private lateinit var mFooter: TextView
    private lateinit var progressBar: ProgressBar
    private var mResponse: String? = "Connected to the database."


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mResponse = savedInstanceState?.getString(DB_STRING) ?: "Connected to the database."
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java).apply {
            setIndex(arguments?.getInt(NameFragment.ARG_SECTION_NUMBER) ?: 1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ctx = container?.context as Context
        return inflater.inflate(R.layout.fragment_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFooter = view.findViewById(R.id.footer)
        mFooter.text = "Use magnifier for the search"
        mMeme = view.findViewById(R.id.advice_body_tv)
        mMeme.movementMethod = ScrollingMovementMethod()
        //init progress bar
        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.visibility = View.GONE
        setUi()
    }

    companion object {
        /**
         * The fragment argument representing the section number for this fragment.
         */
        private const val ARG_SECTION_NUMBER = "name"

        /**
         * Returns a new instance of this fragment for the given section number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): NameFragment {
            return NameFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(DB_STRING, mResponse)
        super.onSaveInstanceState(outState)
    }

    private fun retrieve(fullName: String) {
        progressBar.visibility = View.VISIBLE
        try {

            // Create a Coroutine scope using a job to be able to cancel when needed
            val adviceActivityJob = Job()

            // Handle exceptions if any
            val errorHandler = CoroutineExceptionHandler { _, exception ->
                AlertDialog.Builder(ctx).setTitle("CIA DATABASE ERROR")
                    .setMessage(exception.message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                    .setIcon(R.drawable.ic_apple).show()
                progressBar.visibility = View.GONE
            }

            // the Coroutine runs using the Main (UI) dispatcher
            val coroutineScope = CoroutineScope(adviceActivityJob + Dispatchers.Main)
            coroutineScope.launch(errorHandler) {
                val result = NameRetriever().getNameInformation(fullName)
                // set text
                val countriesForFirstName =
                    getCountriesList(result.data[0].name.firstname.alternative_countries)
//                val countriesForLastName =
//                    getCountriesList(result.data[0].name.lastname.alternative_countries)
                val countriesForFullName =
                    getCountriesList(result.data[0].country.alternative_countries)
                val possibleCountry =
                    if (result.data[0].name.lastname.country_code == null) "no data"
                    else result.data[0].name.lastname.country_code

                mResponse = "Salutation: ${result.data[0].salutation.salutation} " +
                        "${result.data[0].salutation.lastname}\n" +
                        "First name: ${result.data[0].name.firstname.name}\n" +
                        "Last name: ${result.data[0].name.lastname.name}\n" +
                        "International: ${result.data[0].name.firstname.name_ascii} ${result.data[0].name.lastname.name_ascii}\n" +
                        "First name validated: ${result.data[0].name.firstname.validated}\n" +
                        "First name gender: ${result.data[0].name.firstname.gender_formatted}\n" +
                        "First name gender deviation ${result.data[0].name.firstname.gender_deviation}\n" +
                        "Is name unisex: ${result.data[0].name.firstname.unisex}\n" +
                        "Possible country: ${result.data[0].name.firstname.country_code}\n" +
                        "Certainty country: ${result.data[0].name.firstname.country_certainty}%\n" +
                        "Frequency in the country: ${result.data[0].name.firstname.country_frequency}\n" +
                        "Other possible countries for the first name: $countriesForFirstName" +
                        "\nLast name: ${result.data[0].name.lastname.name}\n" +
                        "International: ${result.data[0].name.lastname.name_ascii}\n" +
                        "Last name validated: ${result.data[0].name.lastname.validated}\n" +
                        "Possible country: ${possibleCountry}\n" +
                        "Certainty country: ${result.data[0].name.lastname.country_certainty}%\n" +
                        "Frequency in the country: ${result.data[0].name.lastname.country_frequency}\n" +
//                        "Other possible countries for the last name: $countriesForLastName" +
                        "\nCountry: ${result.data[0].country.name}\n" +
                        "Country code: ${result.data[0].country.country_code}\n" +
                        "Country alpha code: ${result.data[0].country.country_code_alpha}\n" +
                        "Country continent: ${result.data[0].country.continent}\n" +
                        "Country primary language: ${result.data[0].country.name}\n" +
                        "Country currency: ${result.data[0].country.currency}\n" +
                        "Country certainty: ${result.data[0].country.country_certainty}\n" +
                        "Other possible countries: $countriesForFullName"
                setUi()
            }
        } catch (e: Exception) {
            mResponse = "check your input"
        }

    }

    private fun setUi() {
        if (mResponse.equals("Connected to the database.")) {
            mFooter.visibility = View.VISIBLE;
        } else {
            mFooter.visibility = View.GONE
            mMeme.text = mResponse
            progressBar.visibility = View.GONE
        }
    }

    private fun getCountriesList(jsonObject: JsonObject): String {
        Log.d("TAG", "START getCountriesList method $jsonObject")
        var string = "\n"
        jsonObject.keySet().forEach {
            string = string.plus("  ").plus(it).plus(" : ").plus(jsonObject.get(it)).plus("%\n")
        }
        return string
    }

    override fun setUserVisibleHint(visible: Boolean) {
        super.setUserVisibleHint(visible)
        if (visible && isResumed) {
            onResume()
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
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(ctx).inflate(R.layout.search_advice_dialog, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(ctx)
                .setView(mDialogView)
                .setTitle("private person data")
            //show dialog
            val mAlertDialog = mBuilder.show()
            //login button click of custom layout
            val searchBtn = mDialogView.findViewById<Button>(R.id.dialog_search_btn)
            val firstNameTextEdit = mDialogView.findViewById<EditText>(R.id.dialog_first_name)
            val lastNameTextEdit = mDialogView.findViewById<EditText>(R.id.dialog_last_name)
            searchBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
                //get text from EditTexts of custom layout
                val firstName: String = firstNameTextEdit.text.toString()
                val lastName: String = lastNameTextEdit.text.toString()
                val fullName = "$firstName $lastName"

                if (firstName.isNotEmpty() && lastName.isNotEmpty() && fullName.isNotEmpty()) {
                    //if OK than search
                    if (Utils.isNetworkConnected(ctx)) {
                        retrieve(fullName)
                    } else {
                        AlertDialog.Builder(ctx).setTitle("No Internet Connection")
                            .setMessage("Please check your internet connection and try again")
                            .setPositiveButton(R.string.ok) { _, _ -> }
                            .setIcon(R.drawable.ic_apple).show()
                    }
                } else {
                    Toast.makeText(ctx, "Check your input", Toast.LENGTH_LONG).show()
                }
            }

            //cancel button click of custom layout
            val cancelBtn = mDialogView.findViewById<Button>(R.id.dialog_cancel_btn)
            cancelBtn.setOnClickListener {
                //dismiss dialog
                mAlertDialog.dismiss()
            }
        })
    }


}