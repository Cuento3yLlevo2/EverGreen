package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService


@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {

    @Suppress("PrivatePropertyName")
    private val GOOGLE_SIGN_IN = 999
    // Initialize Facebook Login button
    private val callbackManager = CallbackManager.Factory.create()

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText

    private val databaseService: DatabaseService = DatabaseService()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = Firebase.auth
        // get elements from layout
        etLoginEmail = findViewById(R.id.etLoginEmail)
        etLoginPassword = findViewById(R.id.etLoginPassword)
        val tvLinkFromLoginToRegister : TextView = findViewById(R.id.tvLinkFromLoginToRegister)
        val buttonActivityLoginGoogle : Button = findViewById(R.id.buttonActivityLoginGoogle)
        val buttonActivityLoginFacebook : Button = findViewById(R.id.buttonActivityLoginFacebook)
        val buttonActivityLogin : Button = findViewById(R.id.buttonActivityLogin)


        // btn to start login process with Email and Password credentials
        buttonActivityLogin.setOnClickListener {
            loginUserWithEmail()
        }



        // btn to start login process with Google Singin
        buttonActivityLoginGoogle.setOnClickListener {
            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleClient = GoogleSignIn.getClient(this, gso)

            val signInIntent = googleClient.signInIntent
            googleClient.signOut()
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }

        // btn to start login process with Facebook Singin
        buttonActivityLoginFacebook.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email"))

            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult>{
                    override fun onSuccess(loginResult: LoginResult?) {
                        Log.d("firebaseAuth", "facebook:onSuccess:$loginResult")
                        if (loginResult != null) {
                            handleFacebookAccessToken(loginResult.accessToken)
                        }
                    }

                    override fun onCancel() {
                        Log.d("firebaseAuth", "facebook:onCancel")
                    }

                    override fun onError(error: FacebookException?) {
                        Log.d("firebaseAuth", "facebook:onError", error)
                    }

                }
            )

        }

        // Btn that sends users to Register Activity in case they haven't register
        tvLinkFromLoginToRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("firebaseAuth", "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("firebaseAuth", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("firebaseAuth", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)

        super.onActivityResult(requestCode, resultCode, data)



        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("firebaseAuth", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("firebaseAuth", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("firebaseAuth", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("firebaseAuth", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null){
            val uid = user.uid
            databaseService.userAlreadyExists(uid, object: Callback<Boolean> {
                override fun onSuccess(result: Boolean?) {
                    if (result == true) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        databaseService.writeNewUser(user.uid, user.displayName, user.email, user.photoUrl.toString(), object: Callback<Boolean> {
                            override fun onSuccess(result: Boolean?) {
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }

                            override fun onFailure(exception: Exception) {
                                Log.w("FireBaseLogs", "Write failed")
                            }
                        })
                    }
                }
                override fun onFailure(exception: Exception) {
                    Log.w("FireBaseLogs", "userAlreadyExists:onFailure", exception)
                }
            })


        }
    }


    private fun loginUserWithEmail() {
        val userEmail: String = etLoginEmail.text.toString()
        val userPassword: String = etLoginPassword.text.toString()

        when {
            userEmail == "" -> {
                Toast.makeText(this@LoginActivity, "introduce tu correo electrónico", Toast.LENGTH_LONG).show()
            }
            userPassword == "" -> {
                Toast.makeText(this@LoginActivity, "introduce tu contraseña", Toast.LENGTH_LONG).show()
            }
            else -> {
                auth.signInWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            lateinit var errorCode: String
                            when(task.exception as FirebaseException){
                                is FirebaseAuthException -> {
                                    errorCode = (task.exception as FirebaseAuthException).errorCode
                                    when(errorCode){
                                        "ERROR_WRONG_PASSWORD" -> Toast.makeText(this@LoginActivity, "La contraseña que ingresaste es incorrecta.", Toast.LENGTH_LONG).show()
                                        "ERROR_USER_NOT_FOUND" -> Toast.makeText(this@LoginActivity, "El correo electrónico que ingresaste no pertenece a ninguna cuenta.", Toast.LENGTH_LONG).show()
                                        "ERROR_USER_DISABLED" -> Toast.makeText(this@LoginActivity, "La cuenta de usuario ha sido deshabilitada", Toast.LENGTH_LONG).show()
                                        "ERROR_USER_TOKEN_EXPIRED" -> Toast.makeText(this@LoginActivity, "El inicio de sesión ha caducado.", Toast.LENGTH_LONG).show()
                                        "ERROR_INVALID_USER_TOKEN" -> Toast.makeText(this@LoginActivity, "Error desconocido, imposible iniciar sesión.", Toast.LENGTH_LONG).show()
                                        else -> Toast.makeText(this@LoginActivity, "Error: $errorCode", Toast.LENGTH_LONG).show()
                                    }
                                }
                                is FirebaseTooManyRequestsException -> Toast.makeText(this@LoginActivity, "Demasiados intentos consecutivos. Vuelve a intentarlo más tarde.", Toast.LENGTH_LONG).show()
                                else -> Toast.makeText(this@LoginActivity, "Error: ${(task.exception as FirebaseException).message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
            }
        }
    }

}