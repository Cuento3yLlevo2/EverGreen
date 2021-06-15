package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentProfileSupportDetailDialogBinding

class ProfileSupportDetailDialogFragment : BaseDialogFragment() {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private var webViewVisible = false
    private var _binding: FragmentProfileSupportDetailDialogBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSupportDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myWebView: WebView = binding.webviewSupport

        binding.toolbarSupport.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarSupport.setNavigationOnClickListener {
            if (webViewVisible){
                binding.rlWebviewSupport.visibility = View.GONE
                webViewVisible = false
            } else {
                findNavController().popBackStack()
                dismiss()
            }
        }
        binding.toolbarSupport.title = resources.getString(R.string.profile_support_text)

        binding.tvSupportFAQs.setOnClickListener {
            myWebView.loadUrl("https://appsupportevergreen.wordpress.com/")
            binding.rlWebviewSupport.visibility = View.VISIBLE
            webViewVisible = true
        }

        binding.tvSupportContact.setOnClickListener {
            myWebView.loadUrl("https://appsupportevergreen.wordpress.com/contact/")
            binding.rlWebviewSupport.visibility = View.VISIBLE
            webViewVisible = true
        }

        binding.tvSupportLegal.setOnClickListener {
            Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    override fun onDestroyView() {
        bottomNavigationViewVisibility = View.VISIBLE
        _binding = null
        super.onDestroyView()
    }
}