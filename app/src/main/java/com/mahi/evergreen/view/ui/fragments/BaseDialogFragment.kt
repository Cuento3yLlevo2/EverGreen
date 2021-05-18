package com.mahi.evergreen.view.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.mahi.evergreen.view.ui.activities.MainActivity

abstract class BaseDialogFragment: DialogFragment() {

    protected open var bottomNavigationViewVisibility = View.VISIBLE

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // get the reference of the parent activity and call the setBottomNavigationVisibility method.
        if (activity is MainActivity) {
            var  mainActivity = activity as MainActivity
            mainActivity.setBottomNavigationVisibility(bottomNavigationViewVisibility)
        }
    }


    override fun onResume() {
        super.onResume()
        if (activity is MainActivity) {
            var  mainActivity = activity as MainActivity
            mainActivity.setBottomNavigationVisibility(bottomNavigationViewVisibility)
        }
    }

   override fun onDestroy() {
       super.onDestroy()
       var  mainActivity = activity as MainActivity
       mainActivity.setBottomNavigationVisibility(bottomNavigationViewVisibility)
   }
}