package com.mahi.evergreen.view.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.facebook.login.LoginManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.databinding.FragmentProfileSettingsDetailDialogBinding
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import com.mahi.evergreen.view.ui.activities.WelcomeActivity


class ProfileSettingsDetailDialogFragment : BaseDialogFragment() {

    override var bottomNavigationViewVisibility: Int = View.GONE

    private var editProfileVisible = false
    private val databaseService: DatabaseService = DatabaseService()
    private lateinit var auth: FirebaseAuth

    private var _binding: FragmentProfileSettingsDetailDialogBinding? = null
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
        _binding = FragmentProfileSettingsDetailDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbarSettings.navigationIcon = ContextCompat.getDrawable(view.context, R.drawable.arrow_backwards)
        binding.toolbarSettings.setNavigationOnClickListener {
            if(editProfileVisible){
                binding.rlEditProfile.visibility = View.GONE
                editProfileVisible = false
            } else {
                findNavController().popBackStack()
                dismiss()
            }
        }
        binding.toolbarSettings.title = resources.getString(R.string.profile_settings_text)

        binding.tvSingOut.setOnClickListener {
            signOut()
        }

        binding.tvEditProfile.setOnClickListener {
            binding.rlEditProfile.visibility = View.VISIBLE
            editProfileVisible = true
        }

        binding.tvNotifications.setOnClickListener {
            Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.bEditProfileSaveBtn.setOnClickListener {
            updateUserProfile()
        }
    }

    private fun updateUserProfile() {

        val newUsername = binding.etEditProfileUsername.text.toString()
        /*
           Overview: // Username Should Only contains alphanumeric characters, underscore and dot.
               [a-zA-Z0-9] an alphanumeric THEN (
               (_(?!([._])) a _ not followed by a . OR
               \.(?!([_.])) a . not followed by a _ OR
               [a-zA-Z0-9] an alphanumeric ) FOR
               {2,18} minimum 2 to maximum 18 times THEN
               [a-zA-Z0-9] an alphanumeric

               result:

               First character is alphanum, then 2 to 18 characters, last character is alphanum,
               minimum characters in total 4, maximum characters in total 20
       */
        val usernameRegExpression = Regex("^[a-zA-Z0-9](_(?!([._]))|\\.(?!([_.]))|[a-zA-Z0-9]){2,18}[a-zA-Z0-9]\$", setOf(RegexOption.IGNORE_CASE))

        when {
            newUsername == "" -> {
                Toast.makeText(context, "introduce un nombre de usuario", Toast.LENGTH_LONG).show()
            }
            !usernameRegExpression.matches(newUsername) -> {
                when {
                    newUsername.length < 4 -> {
                        Toast.makeText(context, "El nombre de usuario debe tener mínimo 4 caracteres", Toast.LENGTH_LONG).show()
                    }
                    newUsername.length > 20 -> {
                        Toast.makeText(context, "El nombre de usuario debe tener maximo 20 caracteres", Toast.LENGTH_LONG).show()
                    }
                    newUsername.startsWith("_") or newUsername.startsWith(".") -> {
                        Toast.makeText(context, "El nombre de usuario no debe comenzar por '_' o '.'", Toast.LENGTH_LONG).show()
                    }
                    newUsername.endsWith("_") or newUsername.endsWith(".") -> {
                        Toast.makeText(context, "El nombre de usuario no debe terminar con '_' o '.'", Toast.LENGTH_LONG).show()
                    }
                    newUsername.contains("_.") or newUsername.contains("._") -> {
                        Toast.makeText(context, "El nombre de usuario no debe contener un '_' y '.' juntos", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context, "El nombre de usuario solo debe contener caracteres alfanuméricos, '_' y '.'", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {

                databaseService.usernameAlreadyExists(newUsername, object: Callback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        if (result == true) {
                            Toast.makeText(context, "Este nombre de usuario ya está en uso!", Toast.LENGTH_LONG).show()
                        } else {

                            val user = auth.currentUser

                            if (user != null) {

                                databaseService.updateUsername(user.uid, newUsername, object:
                                    Callback<Boolean> {
                                    override fun onSuccess(result: Boolean?) {
                                        Toast.makeText(context, "El nombre de usuario ha sido modificado correctamente", Toast.LENGTH_SHORT).show()
                                    }

                                    override fun onFailure(exception: Exception) {
                                        Log.w("FireBaseLogs", "Write failed")
                                    }
                                })
                            }
                        }
                    }
                    override fun onFailure(exception: Exception) {
                        Log.w("FireBaseLogs", "Write failed")
                    }
                })
            }
        }
    }

    private fun signOut() {
        val firebaseUser = Firebase.auth.currentUser
        firebaseUser?.let {
            for (profile in it.providerData) {
                when(profile.providerId){
                    GoogleAuthProvider.PROVIDER_ID -> {
                        FirebaseAuth.getInstance().signOut()
                    }
                    EmailAuthProvider.PROVIDER_ID -> {
                        FirebaseAuth.getInstance().signOut()
                    }
                    FacebookAuthProvider.PROVIDER_ID -> {
                        LoginManager.getInstance().logOut()
                        FirebaseAuth.getInstance().signOut()
                    }
                }
            }
        }
        val intent = Intent(context, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        setHasOptionsMenu(false)
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