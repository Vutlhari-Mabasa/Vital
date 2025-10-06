package com.example.vital

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var progressBar: ProgressBar
    private lateinit var nameInput: EditText
    private lateinit var ageInput: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var heightInput: EditText
    private lateinit var weightInput: EditText
    private lateinit var googleFitSwitch: Switch
    private lateinit var googleFitStatusText: TextView
    private lateinit var googleFitButton: Button

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        
        initializeViews()
        setupGenderSpinner()
        loadUserProfile()
        setupClickListeners()
        checkGoogleFitPermissions()
    }

    private fun initializeViews() {
        progressBar = findViewById(R.id.progress)
        nameInput = findViewById(R.id.inputName)
        ageInput = findViewById(R.id.inputAge)
        genderSpinner = findViewById(R.id.spinnerGender)
        heightInput = findViewById(R.id.inputHeight)
        weightInput = findViewById(R.id.inputWeight)
        googleFitSwitch = findViewById(R.id.switchGoogleFit)
        googleFitStatusText = findViewById(R.id.textGoogleFitStatus)
        googleFitButton = findViewById(R.id.btnManageGoogleFit)
    }

    private fun setupGenderSpinner() {
        val genderOptions = arrayOf("Select Gender", "Male", "Female", "Other", "Prefer not to say")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = adapter
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener { saveProfile() }
        findViewById<Button>(R.id.btnChangePassword).setOnClickListener { showChangePasswordDialog() }
        findViewById<Button>(R.id.btnChangeEmail).setOnClickListener { showChangeEmailDialog() }
        findViewById<Button>(R.id.btnManageGoogleFit).setOnClickListener { manageGoogleFitPermissions() }
        findViewById<Button>(R.id.btnLogout).setOnClickListener { logout() }
        
        // Navigate to Meals screen
        val btnMeals = Button(this).apply {
            text = "Meals"
            setBackgroundColor(resources.getColor(R.color.orange))
            setTextColor(resources.getColor(R.color.white))
        }
        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(btnMeals, container.childCount - 1)
        btnMeals.setOnClickListener {
            startActivity(Intent(this, MealsActivity::class.java))
        }
        
        googleFitSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requestGoogleFitPermissions()
            } else {
                revokeGoogleFitPermissions()
            }
        }
    }

    private fun loadUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            nameInput.setText(user.displayName ?: "")
            
            // Load additional profile data from Firestore
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
            // Update Firebase Auth profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            
            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Save additional data to Firestore
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

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
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

    private fun changePassword(currentPassword: String, newPassword: String) {
        if (newPassword.length < 6) {
            toast("Password must be at least 6 characters")
            return
        }

        setLoading(true)
        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Re-authenticate user before changing password
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

    private fun showChangeEmailDialog() {
        val dialogView = layoutInflater.inflate(android.R.layout.simple_list_item_2, null)
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

    private fun changeEmail(newEmail: String, password: String) {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
            toast("Please enter a valid email")
            return
        }

        setLoading(true)
        val user = auth.currentUser
        if (user != null && user.email != null) {
            // Re-authenticate user before changing email
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

    private fun checkGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        val hasPermissions = GoogleSignIn.hasPermissions(account, fitnessOptions)
        
        googleFitSwitch.isChecked = hasPermissions
        updateGoogleFitStatus(hasPermissions)
    }

    private fun updateGoogleFitStatus(hasPermissions: Boolean) {
        if (hasPermissions) {
            googleFitStatusText.text = "Google Fit connected"
            googleFitButton.text = "Manage Google Fit Permissions"
        } else {
            googleFitStatusText.text = "Google Fit not connected"
            googleFitButton.text = "Connect Google Fit"
        }
    }

    private fun requestGoogleFitPermissions() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        GoogleSignIn.requestPermissions(
            this,
            REQUEST_CODE_GOOGLE_FIT_PERMISSIONS,
            account,
            fitnessOptions
        )
    }

    private fun manageGoogleFitPermissions() {
        if (googleFitSwitch.isChecked) {
            // Open Google Fit app or show permissions management
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

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        private const val REQUEST_CODE_GOOGLE_FIT_PERMISSIONS = 1001
    }
}
