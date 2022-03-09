package com.example.getter2.flip

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.getter2.flip.api.FlipRetriever
import com.example.getter2.utils.Utils
import com.example.getter2.R
import com.example.getter2.databinding.FragmentMainBinding
import com.example.getter2.ui.main.PageViewModel
import kotlinx.coroutines.*
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import android.view.animation.Animation
import android.view.animation.AnimationUtils


class FlipFragment : Fragment() {

    private lateinit var pageViewModel: PageViewModel
    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val answerKey = "Answer"
    private val imageKey = "img"

    // Animation
    var animUpDown: Animation? = null
    private lateinit var ctx: Context
    private lateinit var mTextView: TextView
    private lateinit var mFlipImageView: ImageView
    private lateinit var cardView: CardView
    private lateinit var outerCardView: CardView
    private lateinit var frameLayout: FrameLayout
    private var outerCardViewHeight: Int = 0
    private var widthFrame: Int = 0
    private lateinit var flipBtn: ImageButton
    private lateinit var progressBar: ProgressBar
    private var mImage: String? = null
    private var mAnswer: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mImage = savedInstanceState?.getString(imageKey)
        mAnswer = savedInstanceState?.getString(answerKey)
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
        val view: View = inflater.inflate(R.layout.fragment_flip, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTextView = view.findViewById(R.id.flip_tv)
        mTextView.text = getString(R.string.flip_push_the_button)
        mFlipImageView = view.findViewById(R.id.flip_iv)
        flipBtn = view.findViewById(R.id.flip_btn)
        cardView = view.findViewById(R.id.cardView)
        outerCardView = view.findViewById(R.id.cardView_outer)
        frameLayout = view.findViewById(R.id.flip_layout)
        //retrieve frame layout width
        val viewTreeObserver = frameLayout.viewTreeObserver
        if (viewTreeObserver.isAlive) {
            viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    frameLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    widthFrame = frameLayout.width
                }
            })
        }
        // load the animation
        animUpDown = AnimationUtils.loadAnimation(ctx, R.anim.up_down);

        //init progress bar and views visibility
        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.visibility = View.GONE
        outerCardView.visibility = View.GONE
        cardView.visibility = View.GONE
        mTextView.visibility = View.VISIBLE

        setUi()

        // flip button functionality
        flipBtn.setOnClickListener {
            mTextView.text = getString(R.string.flip_progress_bar_msg)
            progressBar.visibility = View.VISIBLE
            outerCardView.visibility = View.GONE
            cardView.visibility = View.GONE
            mTextView.visibility = View.GONE
            flipBtn.startAnimation(animUpDown)

            if (Utils.isNetworkConnected(ctx)) {
                retrieve()
            } else {
                AlertDialog.Builder(ctx).setTitle(getString(R.string.no_internet_connection))
                    .setMessage(getString(R.string.check_internet_connection))
                    .setPositiveButton(R.string.ok) { _, _ -> }
                    .setIcon(R.drawable.ic_apple).show()
            }
        }
    }

    //fetch API data
    private fun retrieve() {
        mTextView.text = getString(R.string.flip_progress_bar_msg)
        // Create a Coroutine scope using a job to be able to cancel when needed
        val flipActivityJob = Job()

        // Handle exceptions if any
        val errorHandler = CoroutineExceptionHandler { _, exception ->
            AlertDialog.Builder(ctx).setTitle(getString(R.string.alert_dialog_error))
                .setMessage(exception.message)
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(R.drawable.ic_apple).show()
        }

        // the Coroutine runs using the Main (UI) dispatcher
        val coroutineScope = CoroutineScope(flipActivityJob + Dispatchers.Main)
        coroutineScope.launch(errorHandler) {

            val result = FlipRetriever().getFlip()
            //set image to ImageView
            mImage = result.image
            val textAnswer = result.answer?.lowercase()
            mAnswer = if (textAnswer.equals("yes")) {
                getString(R.string.yes)
            } else {
                getString(R.string.no)
            }
            setUi()
        }
    }

    //set up UI elements and data
    private fun setUi() {

        //set cardView height for main image
        if (outerCardViewHeight == 300 || outerCardViewHeight == 0) {
            outerCardViewHeight = if (widthFrame == 0) 300 else {
                ((widthFrame - 32) / 1.3).toInt()
            }
        }

        val layoutParams = outerCardView.layoutParams
        layoutParams.height = outerCardViewHeight

        if (mImage != null && mAnswer != null) {
            val options = RequestOptions()
            options.centerCrop()
            Glide.with(ctx)
                .load(mImage)
                .apply(options)
                .into(mFlipImageView)
            outerCardView.visibility = View.VISIBLE
            cardView.visibility = View.VISIBLE
            mTextView.visibility = View.VISIBLE
            // set text
            mTextView.text = mAnswer?.uppercase()
            //hide progress bar
            progressBar.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(answerKey, mAnswer)
        outState.putString(imageKey, mImage)
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val TAG = "FLIP"
        private const val ARG_SECTION_NUMBER = "flip"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): FlipFragment {
            return FlipFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }

}