package com.example.vital

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : BaseActivity() {

    // Firebase authentication and Firestore instances
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // UI elements
    private lateinit var progressBar: ProgressBar
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var googleFitSwitch: Switch
    private lateinit var googleFitStatusText: TextView
    private lateinit var googleFitButton: Button

    // Google Fit permissions configuration
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase and UI components
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupGenderSpinner()
        loadUserProfile()
        setupClickListeners()
        checkGoogleFitPermissions()

        // Setup bottom navigation for screen navigation
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_profile
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_profile -> true
                R.id.nav_meals -> { startActivity(Intent(this, MealsActivity::class.java)); true }
                R.id.nav_fitness -> { startActivity(Intent(this, FitnessActivity::class.java)); true }
                else -> false
            }
        }
    }

    // Initialize all view components
    private fun initializeViews() {
        progressBar = findViewById(R.id.progress)
        nameInput = findViewById(R.id.nameInput)
        ageInput = findViewById(R.id.ageInput)
        genderSpinner = findViewById(R.id.genderSpinner)
        heightInput = findViewById(R.id.heightInput)
        weightInput = findViewById(R.id.weightInput)
        googleFitSwitch = findViewById(R.id.googleFitSwitch)
        googleFitStatusText = findViewById(R.id.googleFitStatusText)
        googleFitButton = findViewById(R.id.googleFitButton)
    }

    // Setup gender dropdown options
    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other", "Prefer not to say")
        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, genderOptions) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                val tv = v.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(resources.getColor(R.color.black))
                return v
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getDropDownView(position, convertView, parent)
                val tv = v.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(resources.getColor(R.color.black))
                return v
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }

    // Set up click listeners for buttons and switches
    private fun setupClickListeners() {
        findViewById<Button>(R.id.saveButton).setOnClickListener { saveProfile() }
        findViewById<Button>(R.id.changePasswordButton).setOnClickListener { showChangePasswordDialog() }
        findViewById<Button>(R.id.changeEmailButton).setOnClickListener { showChangeEmailDialog() }
        findViewById<Button>(R.id.changeLanguageButton).setOnClickListener { showLanguageSelectionDialog() }
        findViewById<Button>(R.id.btnGoMeals).setOnClickListener { 
            startActivity(Intent(this, MealsActivity::class.java))
        }
        findViewById<Button>(R.id.googleFitButton).setOnClickListener { manageGoogleFitPermissions() }
        findViewById<Button>(R.id.logoutButton).setOnClickListener { logout() }

        // Handle Google Fit toggle switch
        googleFitSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestGoogleFitPermissions()
            } else {
                revokeGoogleFitPermissions()
            }
        }
    }

    // Load current user's profile data from Firebase
    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            nameInput.setText(user.displayName ?: "")

            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        document.data?.let { data ->
                            ageInput.setText(data["age"]?.toString() ?: "")
                            heightInput.setText(data["height"]?.toString() ?: "")
                            weightInput.setText(data["weight"]?.toString() ?: "")

                            val gender = data["gender"]?.toString() ?: ""
                            val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other", "Prefer not to say")
                            val genderIndex = genderOptions.indexOf(gender)
                            if (genderIndex > 0) {
                                genderSpinner.setSelection(genderIndex)
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    toast("Failed to load profile data")
                }
        }
    }

    // Save updated profile data to Firebase
    private fun saveProfile() {
        val name = nameInput.text.toString().trim()
        val age = ageInput.text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val height = heightInput.text.toString().trim()
        val weight = weightInput.text.toString().trim()

        if (name.isEmpty()) {
            toast("Please enter your name")
            return
        }

        setLoading(true)
        val user = auth.currentUser
        if (user != null) {
            // Update Firebase Auth display name
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Save additional details in Firestore
                        val userData = hashMapOf(
                            "name" to name,
                            "age" to age.toIntOrNull(),
                            "gender" to if (gender != "Select Gender") gender else null,
                            "height" to height.toDoubleOrNull(),
                            "weight" to weight.toDoubleOrNull(),
                            "lastUpdated" to System.currentTimeMillis()
                        )

                        firestore.collection("users").document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                setLoading(false)
                                toast("Profile saved successfully")
                            }
                            .addOnFailureListener {
                                setLoading(false)
                                toast("Failed to save profile")
                            }
                    } else {
                        setLoading(false)
                        toast("Failed to update profile")
                    }
                }
        }
    }

    // Show password change dialog
    private fun showChangePasswordDialog() {
        val currentPasswordInput = EditText(this).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        val newPasswordInput = EditText(this).apply {
            hint = "New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(currentPasswordInput)
            addView(newPasswordInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                changePassword(currentPassword, newPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Change user password after re-authentication
    private fun changePassword(currentPassword: String, newPassword: String) {
        if (newPassword.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        setLoading(true)
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                setLoading(false)
                                if (updateTask.isSuccessful) {
                                    toast("Password changed successfully")
                                } else {
                                    toast("Failed to change password")
                                }
                            }
                    } else {
                        setLoading(false)
                        toast("Current password is incorrect")
                    }
                }
        }
    }

    // Show email change dialog
    private fun showChangeEmailDialog() {
        val newEmailInput = EditText(this).apply {
            hint = "New Email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val passwordInput = EditText(this).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
            addView(newEmailInput)
            addView(passwordInput)
        }

        AlertDialog.Builder(this)
            .setTitle("Change Email")
            .setView(layout)
            .setPositiveButton("Change") { _, _ ->
                val newEmail = newEmailInput.text.toString()
                val password = passwordInput.text.toString()
                changeEmail(newEmail, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Change user email after verifying password
    private fun changeEmail(newEmail: String, password: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            toast("Please enter a valid email")
            return
        }

        setLoading(true)
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        user.updateEmail(newEmail)
                            .addOnCompleteListener { updateTask ->
                                setLoading(false)
                                if (updateTask.isSuccessful) {
                                    toast("Email changed successfully. Please verify your new email.")
                                } else {
                                    toast("Failed to change email")
                                }
                            }
                    } else {
                        setLoading(false)
                        toast("Password is incorrect")
                    }
                }
        }
    }

    // Check if Google Fit permissions are already granted
    private fun checkGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)

        googleFitSwitch.isChecked = hasPermissions
        updateGoogleFitStatus(hasPermissions)
    }

    // Update Google Fit connection status text and button
    private fun updateGoogleFitStatus(hasPermissions: Boolean) {
        if (hasPermissions) {
            googleFitStatusText.text = getString(R.string.google_fit_connected)
            googleFitButton.text = getString(R.string.manage_google_fit_permissions)
        } else {
            googleFitStatusText.text = getString(R.string.google_fit_not_connected)
            googleFitButton.text = getString(R.string.connect_google_fit)
        }
    }

    // Request Google Fit permissions
    private fun requestGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        GoogleSignIn.requestPermissions(
            this,
            REQUEST_CODE_GOOGLE_FIT_PERMISSIONS,
            account,
            fitnessOptions
        )
    }

    // Manage Google Fit permission settings or open app
    private fun manageGoogleFitPermissions() {
        if (googleFitSwitch.isChecked) {
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.fitness")
            if (intent != null) {
                startActivity(intent)
            } else {
                toast("Google Fit app not installed")
            }
        } else {
            requestGoogleFitPermissions()
        }
    }

    // Revoke Google Fit permissions
    private fun revokeGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        Fitness.getConfigClient(this, account)
            .disableFit()
            .addOnSuccessListener {
                updateGoogleFitStatus(false)
                toast("Google Fit permissions revoked")
            }
            .addOnFailureListener {
                toast("Failed to revoke Google Fit permissions")
            }
    }

    // Logout confirmation dialog
    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                auth.signOut()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Handle result from Google Fit permission request
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_GOOGLE_FIT_PERMISSIONS) {
            if (resultCode == RESULT_OK) {
                updateGoogleFitStatus(true)
                toast("Google Fit permissions granted")
            } else {
                googleFitSwitch.isChecked = false
                updateGoogleFitStatus(false)
                toast("Google Fit permissions denied")
            }
        }
    }

    // Show or hide loading indicator
    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    // Show language selection dialog
    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.language_english),
            getString(R.string.language_afrikaans),
            getString(R.string.language_zulu)
        )
        
        val currentLanguage = LocaleHelper.getSavedLanguage(this)
        val currentIndex = when (currentLanguage) {
            "af" -> 1
            "zu" -> 2
            else -> 0
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = when (which) {
                    1 -> "af" // Afrikaans
                    2 -> "zu" // Zulu
                    else -> "en" // English
                }
                changeLanguage(selectedLanguage)
                dialog.dismiss()
            }
            .setNegativeButton(getString(android.R.string.cancel), null)
            .show()
    }

    // Change app language
    private fun changeLanguage(languageCode: String) {
        LocaleHelper.saveLanguage(this, languageCode)
        // Recreate activity to apply language change app-wide
        recreate()
    }

    // Display toast message helper
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        private const val REQUEST_CODE_GOOGLE_FIT_PERMISSIONS = 1001
    }
}
