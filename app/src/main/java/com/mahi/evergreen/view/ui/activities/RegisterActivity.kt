package com.mahi.evergreen.view.ui.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.mahi.evergreen.R
import com.mahi.evergreen.network.Callback
import com.mahi.evergreen.network.FirebaseDatabaseService
import java.util.*

/***
 * UI interface that helps with user account creation
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var etRegisterUsername: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private val databaseService: FirebaseDatabaseService = FirebaseDatabaseService()
    private lateinit var database: FirebaseDatabase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = databaseService.database

        etRegisterUsername = findViewById(R.id.etRegisterUsername)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)

        val tvLinkFromRegisterToLogin : TextView = findViewById(R.id.tvLinkFromRegisterToLogin)

        tvLinkFromRegisterToLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize Firebase Auth
        auth = Firebase.auth

        val buttonActivityRegister : Button = findViewById(R.id.buttonActivityRegister)
        buttonActivityRegister.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: String = etRegisterUsername.text.toString()
        val userEmail: String = etRegisterEmail.text.toString()
        val userPassword: String = etRegisterPassword.text.toString()
        /*
           Overview: // Username Should Only contains alphanumeric characters, underscore and dot.
               [a-zA-Z0-9] an alphanumeric THEN (
               (_(?!([._])) a _ not followed by a . OR
               \.(?!([_.])) a . not followed by a _ OR
               [a-zA-Z0-9] an alphanumeric ) FOR
               {2,8} minimum 2 to maximum 8 times THEN
               [a-zA-Z0-9] an alphanumeric

               result:

               First character is alphanum, then 2 to 8 characters, last character is alphanum,
               minimum characters in total 4, maximum characters in total 10
       */
        val usernameRegExpression = Regex("^[a-zA-Z0-9](_(?!([._]))|\\.(?!([_.]))|[a-zA-Z0-9]){2,8}[a-zA-Z0-9]\$", setOf(RegexOption.IGNORE_CASE))

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
                    username.length > 10 -> {
                        Toast.makeText(this@RegisterActivity, "El nombre de usuario debe tener maximo 10 caracteres", Toast.LENGTH_LONG).show()
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
                                                databaseService.writeNewUser(user.uid, username, userEmail, object: Callback<Boolean> {
                                                    override fun onSuccess(result: Boolean?) {
                                                        val intent = Intent(this@RegisterActivity, MainActivity2::class.java)
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

    /*
    private fun writeNewUser(userId: String, username: String, email: String) {
        val defaultProfileImage = "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/user_default.png?alt=media&token=53382a72-8fc1-4a11-b63f-0cb342bb02c6"
        val defaultCoverImage = "https://firebasestorage.googleapis.com/v0/b/evergreen-app-bdbc2.appspot.com/o/profile_cover_default.jpg?alt=media&token=07a0cfd5-a6df-4877-8ea6-8e0ecc2d98fb"
        val user = User(
                username,
                email,
                defaultProfileImage,
                defaultCoverImage,
                "offline",
                username.toLowerCase(Locale.ROOT),
                userId
        )
        Log.w("FireBaseLogs", "Start Writing $userId")

        dbReference = database.reference
        dbReference.child(USERS_REFERENCE).child(userId).setValue(user)
                .addOnSuccessListener {
                    // Write was successful!
                    Log.w("FireBaseLogs", "Write was successful")

                    val intent = Intent(this@RegisterActivity, MainActivity2::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    // Write failed
                    Log.w("FireBaseLogs", "Write failed")
                }
    }*/
}