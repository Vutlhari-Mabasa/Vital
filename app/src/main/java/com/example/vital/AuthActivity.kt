package com.example.vital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.IntentSenderRequest

class AuthActivity : AppCompatActivity() {

    // Firebase authentication instance
    private lateinit var auth: FirebaseAuth

    // Google One Tap sign-in client
    private lateinit var oneTapClient: SignInClient

    // Sign-in request configuration for Google sign-in
    private lateinit var signInRequest: BeginSignInRequest

    // Launcher to handle Google sign-in result
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        try {
            // Check if the result is OK and data is not null
            if (result.resultCode != RESULT_OK || result.data == null) {
                toast("Google sign in canceled")
                setLoading(false)
                return@registerForActivityResult
            }

            // Get Google ID token from result
            val credential = Identity.getSignInClient(this).getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken

            if (idToken != null) {
                // Use Google ID token to sign in with Firebase
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) onSignedIn(auth.currentUser) else toast("Google sign in failed")
                }
            } else {
                setLoading(false)
                toast("Google sign in canceled")
            }
        } catch (e: Exception) {
            setLoading(false)
            toast("Google sign in failed")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Initialize Firebase authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Google One Tap client
        oneTapClient = Identity.getSignInClient(this)

        // Configure Google sign-in options
        signInRequest = BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()

        // Set up button click listeners
        findViewById<com.google.android.gms.common.SignInButton>(R.id.btnGoogle).setOnClickListener { googleSignIn() }
        findViewById<Button>(R.id.btnRegister).setOnClickListener { emailRegister() }
        findViewById<Button>(R.id.btnLogin).setOnClickListener { emailLogin() }

        // Handle email link sign-in if app opened via email link
        handleEmailLinkSignIn(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) handleEmailLinkSignIn(intent)
    }

    // Handle email link sign-in flow
    private fun handleEmailLinkSignIn(intent: Intent) {
        val data: Uri? = intent.data
        if (data != null && auth.isSignInWithEmailLink(data.toString())) {
            val emailField = findViewById<EditText>(R.id.inputEmail)
            val email = emailField.text.toString()
            val stored = getSharedPreferences("auth", MODE_PRIVATE).getString("email_link_pending", "") ?: ""
            val finalEmail = if (email.isNotEmpty()) email else stored

            if (finalEmail.isNotEmpty()) {
                setLoading(true)
                // Sign in with email link
                auth.signInWithEmailLink(finalEmail, data.toString()).addOnCompleteListener { task ->
                    setLoading(false)
                    if (task.isSuccessful) onSignedIn(auth.currentUser) else toast("Email link sign-in failed")
                }
            } else {
                toast("Enter your email to complete sign-in")
            }
        }
    }

    // Initiate Google sign-in flow
    private fun googleSignIn() {
        setLoading(true)
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val request = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                googleLauncher.launch(request)
            }
            .addOnFailureListener {
                setLoading(false)
                toast("Google sign in unavailable")
            }
    }

    // Register new user with email and password
    private fun emailRegister() {
        val email = findViewById<EditText>(R.id.inputEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.inputPassword).text.toString().trim()

        if (!isValidEmail(email) || password.length < 6) {
            toast("Enter valid email and 6+ char password")
            return
        }

        setLoading(true)
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            setLoading(false)
            if (task.isSuccessful) onSignedIn(auth.currentUser) else toast("Registration failed")
        }
    }

    // Login user with email and password
    private fun emailLogin() {
        val email = findViewById<EditText>(R.id.inputEmail).text.toString().trim()
        val password = findViewById<EditText>(R.id.inputPassword).text.toString().trim()

        if (!isValidEmail(email) || password.isEmpty()) {
            toast("Enter valid email and password")
            return
        }

        setLoading(true)
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            setLoading(false)
            if (task.isSuccessful) onSignedIn(auth.currentUser) else toast("Login failed")
        }
    }

    // Send magic link (email link) for sign-in
    private fun sendEmailLink() {
        val email = findViewById<EditText>(R.id.inputEmail).text.toString().trim()
        if (!isValidEmail(email)) {
            toast("Enter a valid email")
            return
        }

        // Configure the ActionCodeSettings for email link sign-in
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setHandleCodeInApp(true)
            .setAndroidPackageName(packageName, true, null)
            .setUrl("https://vital.example.com/auth")
            .build()

        setLoading(true)
        // Send the sign-in link to the user’s email
        auth.sendSignInLinkToEmail(email, actionCodeSettings).addOnCompleteListener { task ->
            setLoading(false)
            if (task.isSuccessful) {
                // Save email to shared preferences for later completion
                getSharedPreferences("auth", MODE_PRIVATE).edit().putString("email_link_pending", email).apply()
                toast("Sign-in link sent. Check your email.")
            } else {
                toast("Failed to send link")
            }
        }
    }

    // Called after successful authentication
    private fun onSignedIn(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
            finish()
        }
    }

    // Validate email format
    private fun isValidEmail(email: String): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // Show or hide loading indicator
    private fun setLoading(loading: Boolean) {
        findViewById<ProgressBar>(R.id.progress).visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    // Display toast message
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
