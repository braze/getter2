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
    private val dbString = "DataBaseString"
    private lateinit var ctx: Context
    private lateinit var mMeme: TextView
    private lateinit var mFooter: TextView
    private lateinit var progressBar: ProgressBar
    private var mResponse: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mResponse = savedInstanceState?.getString(dbString) ?: getString(R.string.connected_to_database)
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
        return inflater.inflate(R.layout.fragment_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFooter = view.findViewById(R.id.footer)
        mFooter.text = getString(R.string.magnifier)
        mMeme = view.findViewById(R.id.advice_body_tv)
        mMeme.movementMethod = ScrollingMovementMethod()
        //init progress bar
        progressBar = view.findViewById(R.id.progress_bar)
        mResponse = getString(R.string.connected_to_database)
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
        outState.putString(dbString, mResponse)
        super.onSaveInstanceState(outState)
    }

    private fun retrieve(fullName: String) {
        progressBar.visibility = View.VISIBLE
        try {

            // Create a Coroutine scope using a job to be able to cancel when needed
            val adviceActivityJob = Job()

            // Handle exceptions if any
            val errorHandler = CoroutineExceptionHandler { _, exception ->
                AlertDialog.Builder(ctx).setTitle(getString(R.string.database_error))
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
                val countriesForFullName =
                    getCountriesList(result.data[0].country.alternative_countries)
                val possibleCountry =
                    if (result.data[0].name.lastname.country_code == null) getString(R.string.no_data)
                    else result.data[0].name.lastname.country_code

                mResponse = getString(R.string.salutation)
                    .plus(result.data[0].salutation.salutation)
                    .plus(" ")
                    .plus(result.data[0].salutation.lastname).plus("\n")
                    .plus(getString(R.string.first_name))
                    .plus(result.data[0].name.firstname.name).plus("\n")
                    .plus(getString(R.string.last_name))
                    .plus(result.data[0].name.lastname.name).plus("\n")
                    .plus(getString(R.string.international))
                    .plus(result.data[0].name.firstname.name_ascii).plus(" ")
                    .plus(result.data[0].name.lastname.name_ascii).plus("\n")
                    .plus(getString(R.string.first_name_validated))
                    .plus(result.data[0].name.firstname.validated).plus("\n")
                    .plus(getString(R.string.first_name_gender))
                    .plus(result.data[0].name.firstname.gender_formatted).plus("\n")
                    .plus(getString(R.string.first_name_deviation))
                    .plus(result.data[0].name.firstname.gender_deviation).plus("\n")
                    .plus(getString(R.string.unisex_name))
                    .plus(result.data[0].name.firstname.unisex).plus("\n")
                    .plus(getString(R.string.possible_country))
                    .plus(result.data[0].name.firstname.country_code).plus("\n")
                    .plus(getString(R.string.certainty_country))
                    .plus(result.data[0].name.firstname.country_certainty).plus("%\n")
                    .plus(getString(R.string.frequency_in_country))
                    .plus(result.data[0].name.firstname.country_frequency).plus("\n")
                    .plus(getString(R.string.frequency_in_country))
                    .plus(countriesForFirstName).plus("\n\n")
                    .plus(getString(R.string.last_name))
                    .plus(result.data[0].name.lastname.name).plus("\n")
                    .plus(getString(R.string.international))
                    .plus(result.data[0].name.lastname.name_ascii).plus("\n")
                    .plus(getString(R.string.last_name_validated))
                    .plus(result.data[0].name.lastname.validated).plus("\n")
                    .plus(getString(R.string.possible_country))
                    .plus(possibleCountry).plus("\n")
                    .plus(getString(R.string.certainty_country))
                    .plus(result.data[0].name.lastname.country_certainty).plus("%\n")
                    .plus(getString(R.string.frequency_in_country))
                    .plus(result.data[0].name.lastname.country_frequency).plus("\n")
                    .plus(getString(R.string.country))
                    .plus(result.data[0].country.name).plus("\n")
                    .plus(getString(R.string.country_code))
                    .plus(result.data[0].country.country_code).plus("\n")
                    .plus(getString(R.string.country_alpha_code))
                    .plus(result.data[0].country.country_code_alpha).plus("\n")
                    .plus(getString(R.string.continent))
                    .plus(result.data[0].country.continent).plus("\n")
                    .plus(getString(R.string.language))
                    .plus(result.data[0].country.name).plus("\n")
                    .plus(getString(R.string.currency))
                    .plus(result.data[0].country.currency).plus("\n")
                    .plus(getString(R.string.certainty_country))
                    .plus(result.data[0].country.country_certainty).plus("\n")
                    .plus(getString(R.string.other_possible_country))
                    .plus(countriesForFullName)

                setUi()
            }
        } catch (e: Exception) {
            mResponse = getString(R.string.check_input)
        }

    }

    private fun setUi() {
        if (mResponse.equals(getString(R.string.connected_to_database))) {
            mFooter.visibility = View.VISIBLE;
        } else {
            mFooter.visibility = View.GONE
            mMeme.text = mResponse
            progressBar.visibility = View.GONE
        }
    }

    private fun getCountriesList(jsonObject: JsonObject): String {
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
            //Can only access if attached to MainActivity
            ?: error(getString(R.string.sunset_error))

        activityFab.setOnClickListener(View.OnClickListener {
            //Inflate the dialog with custom view
            val mDialogView = LayoutInflater.from(ctx).inflate(R.layout.search_advice_dialog, null)
            //AlertDialogBuilder
            val mBuilder = AlertDialog.Builder(ctx)
                .setView(mDialogView)
                .setTitle(getString(R.string.private_person_data))
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
                        AlertDialog.Builder(ctx).setTitle(getString(R.string.no_internet_connection))
                            .setMessage(getString(R.string.check_internet_connection))
                            .setPositiveButton(R.string.ok) { _, _ -> }
                            .setIcon(R.drawable.ic_apple).show()
                    }
                } else {
                    Toast.makeText(ctx, getString(R.string.check_input), Toast.LENGTH_LONG).show()
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