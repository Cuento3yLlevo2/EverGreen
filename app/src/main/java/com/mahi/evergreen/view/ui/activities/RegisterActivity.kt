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
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.DatabaseService
import java.util.*

/***
 * UI interface that helps with user account creation
 */
@Suppress("DEPRECATION")
class RegisterActivity : AppCompatActivity() {

    @Suppress("PrivatePropertyName")
    private val GOOGLE_REGISTER = 988
    private lateinit var googleClient: GoogleSignInClient
    // Initialize Facebook Login button
    private val callbackManager = CallbackManager.Factory.create()

    private lateinit var auth: FirebaseAuth
    private lateinit var etRegisterUsername: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private val databaseService: DatabaseService = DatabaseService()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        etRegisterUsername = findViewById(R.id.etRegisterUsername)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        val buttonActivityRegister : Button = findViewById(R.id.buttonActivityRegister)
        val tvLinkFromRegisterToLogin : TextView = findViewById(R.id.tvLinkFromRegisterToLogin)
        val buttonActivityRegisterGoogle : Button = findViewById(R.id.buttonActivityRegisterGoogle)
        val buttonActivityRegisterFacebook : Button = findViewById(R.id.buttonActivityRegisterFacebook)

        tvLinkFromRegisterToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        // btn to start register process with Email and Password credentials
        buttonActivityRegister.setOnClickListener {
            registerUserWithEmail()
        }

        // btn to start login process with Google Singin
        buttonActivityRegisterGoogle.setOnClickListener {
            // Configure Google Sign In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleClient = GoogleSignIn.getClient(this, gso)

            val signInIntent = googleClient.signInIntent
            startActivityForResult(signInIntent, GOOGLE_REGISTER)
        }

        // btn to start login process with Facebook Singin
        buttonActivityRegisterFacebook.setOnClickListener {

            LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))

            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
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
        if (requestCode == GOOGLE_REGISTER) {
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
                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        databaseService.writeNewUser(user.uid, user.displayName, user.email, user.photoUrl.toString(), object: Callback<Boolean> {
                            override fun onSuccess(result: Boolean?) {
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
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

    private fun registerUserWithEmail() {
        val username: String = etRegisterUsername.text.toString()
        val userEmail: String = etRegisterEmail.text.toString()
        val userPassword: String = etRegisterPassword.text.toString()
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
            username == "" -> {
                Toast.makeText(this@RegisterActivity, "introduce un nombre de usuario", Toast.LENGTH_LONG).show()
            }
            userEmail == "" -> {
                Toast.makeText(this@RegisterActivity, "introduce un correo electrónico", Toast.LENGTH_LONG).show()
            }
            userPassword == "" -> {
                Toast.makeText(this@RegisterActivity, "introduce una contraseña", Toast.LENGTH_LONG).show()
            }
            !usernameRegExpression.matches(username) -> {
                when {
                    username.length < 4 -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario debe tener mínimo 4 caracteres", Toast.LENGTH_LONG).show()
                    }
                    username.length > 20 -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario debe tener maximo 20 caracteres", Toast.LENGTH_LONG).show()
                    }
                    username.startsWith("_") or username.startsWith(".") -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario no debe comenzar por '_' o '.'", Toast.LENGTH_LONG).show()
                    }
                    username.endsWith("_") or username.endsWith(".") -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario no debe terminar con '_' o '.'", Toast.LENGTH_LONG).show()
                    }
                    username.contains("_.") or username.contains("._") -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario no debe contener un '_' y '.' juntos", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario solo debe contener caracteres alfanuméricos, '_' y '.'", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {

                databaseService.usernameAlreadyExists(username, object: Callback<Boolean> {
                    override fun onSuccess(result: Boolean?) {
                        if (result == true) {
                            Toast.makeText(this@RegisterActivity, "Este nombre de usuario ya está en uso!", Toast.LENGTH_LONG).show()
                        } else {
                            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful){
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d("FireBaseLogs", "createUserWithEmail:success")
                                            val user = auth.currentUser
                                            if (user != null) {
                                                databaseService.writeNewUser(user.uid, username, userEmail, null, object: Callback<Boolean> {
                                                    override fun onSuccess(result: Boolean?) {
                                                        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        startActivity(intent)
                                                        finish()
                                                    }

                                                    override fun onFailure(exception: Exception) {
                                                        Log.w("FireBaseLogs", "Write failed")
                                                    }
                                                })
                                            }
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w("FireBaseLogs", "createUserWithEmail:failure", task.exception)
                                            lateinit var errorCode: String
                                            when(task.exception as FirebaseException){
                                                is FirebaseAuthWeakPasswordException -> Toast.makeText(this@RegisterActivity, "La contraseña es demasiado corta. Debe tener más de 6 caracteres.", Toast.LENGTH_LONG).show()
                                                is FirebaseAuthUserCollisionException -> {
                                                    errorCode = (task.exception as FirebaseAuthUserCollisionException).errorCode
                                                    when(errorCode){
                                                        "ERROR_EMAIL_ALREADY_IN_USE" -> Toast.makeText(this@RegisterActivity, "El correo electrónico ya está siendo utilizado por otra cuenta.", Toast.LENGTH_LONG).show()
                                                        else -> Toast.makeText(this@RegisterActivity, "Error: $errorCode", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                                is FirebaseAuthInvalidCredentialsException -> Toast.makeText(this@RegisterActivity, "Mmm...eso no parece una dirección de correo.", Toast.LENGTH_LONG).show()
                                                else -> Toast.makeText(this@RegisterActivity, "Error: ${(task.exception as FirebaseException).message}", Toast.LENGTH_LONG).show()
                                            }
                                        }
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

}