package com.example.vital

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.result.DailyTotalResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MealsActivity : BaseActivity() {

    // Firebase and Google Fit setup
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // UI components
    private lateinit var foodName: EditText
    private lateinit var calories: EditText
    private lateinit var carbs: EditText
    private lateinit var protein: EditText
    private lateinit var fat: EditText
    private lateinit var addButton: Button
    private lateinit var mealTypeSpinner: Spinner
    private lateinit var listView: ListView
    private lateinit var totalsText: TextView
    private lateinit var burnedText: TextView
    private lateinit var netText: TextView
    private lateinit var progress: ProgressBar

    // Data model for meals
    private data class MealItem(val id: String, val line: String, val kcal: Int, val c: Double, val p: Double, val f: Double)
    private val meals = mutableListOf<MealItem>()
    private lateinit var adapter: ArrayAdapter<String>

    // Nutrient totals
    private var totalCalories = 0
    private var totalCarbs = 0.0
    private var totalProtein = 0.0
    private var totalFat = 0.0
    private var caloriesBurned = 0.0

    // Google Fit permissions
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meals)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup UI and listeners
        initializeViews()
        setupList()
        setupListeners()
        setupMealType()

        // Load meal data and burned calories
        loadMealsForToday()
        fetchCaloriesBurnedToday()

        // Setup bottom navigation
        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.selectedItemId = R.id.nav_meals
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); true }
                R.id.nav_meals -> true
                R.id.nav_fitness -> { startActivity(Intent(this, FitnessActivity::class.java)); true }
                else -> false
            }
        }
    }

    // Initialize all UI elements
    private fun initializeViews() {
        foodName = findViewById(R.id.inputFoodName)
        calories = findViewById(R.id.inputCalories)
        carbs = findViewById(R.id.inputCarbs)
        protein = findViewById(R.id.inputProtein)
        fat = findViewById(R.id.inputFat)
        addButton = findViewById(R.id.btnAddMeal)
        listView = findViewById(R.id.listMeals)
        totalsText = findViewById(R.id.textTotals)
        burnedText = findViewById(R.id.textCaloriesBurned)
        netText = findViewById(R.id.textNet)
        progress = findViewById(R.id.progress)
    }

    // Setup the list adapter and click listeners
    private fun setupList() {
        adapter = buildMealsAdapter()
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            showMealOptions(position)
        }
    }

    // Build adapter to display meal items in the list
    private fun buildMealsAdapter(): ArrayAdapter<String> {
        val items = meals.map { it.line }
        return object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                val tv = v.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(resources.getColor(R.color.black))
                tv.textSize = 14f
                tv.setPadding(16, 16, 16, 16)
                tv.typeface = resources.getFont(R.font.alan_sans_regular)
                v.setBackgroundResource(R.drawable.card_background)
                v.setPadding(16, 16, 16, 16)
                return v
            }
        }
    }

    // Setup add button click listener
    private fun setupListeners() {
        addButton.setOnClickListener { addMeal() }
    }

    // Setup meal type dropdown (spinner)
    private fun setupMealType() {
        val types = arrayOf(
            getString(R.string.breakfast),
            getString(R.string.lunch),
            getString(R.string.dinner),
            getString(R.string.snack)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealTypeSpinner = findViewById(R.id.spinnerMealType)
        mealTypeSpinner.adapter = adapter
    }

    // Add new meal to Firestore and update UI
    private fun addMeal() {
        val name = foodName.text.toString().trim()
        val kcal = calories.text.toString().toIntOrNull() ?: 0
        val c = carbs.text.toString().toDoubleOrNull() ?: 0.0
        val p = protein.text.toString().toDoubleOrNull() ?: 0.0
        val f = fat.text.toString().toDoubleOrNull() ?: 0.0
        val mealType = mealTypeSpinner.selectedItem?.toString() ?: getString(R.string.meal)

        if (name.isEmpty() || kcal <= 0) {
            toast("Enter food name and calories")
            return
        }

        setLoading(true)
        val user = auth.currentUser ?: return
        val dateKey = getTodayKey()
        val meal = hashMapOf(
            "date" to dateKey,
            "mealType" to mealType,
            "food" to name,
            "calories" to kcal,
            "carbs" to c,
            "protein" to p,
            "fats" to f
        )

        // Add meal to Firestore
        firestore.collection("users").document(user.uid)
            .collection("nutrition")
            .add(meal)
            .addOnSuccessListener { ref ->
                meals.add(MealItem(ref.id, "$dateKey • $mealType • $name - ${kcal}kcal  C:${c}g P:${p}g F:${f}g", kcal, c, p, f))
                refreshAdapter()
                totalCalories += kcal
                totalCarbs += c
                totalProtein += p
                totalFat += f
                updateTotals()
                clearInputs()
                setLoading(false)
            }
            .addOnFailureListener {
                setLoading(false)
                toast("Failed to add meal")
            }
    }

    // Load today's meals from Firestore
    private fun loadMealsForToday() {
        setLoading(true)
        val user = auth.currentUser ?: return
        val dateKey = getTodayKey()
        firestore.collection("users").document(user.uid)
            .collection("nutrition")
            .whereEqualTo("date", dateKey)
            .get()
            .addOnSuccessListener { snapshot ->
                meals.clear()
                totalCalories = 0
                totalCarbs = 0.0
                totalProtein = 0.0
                totalFat = 0.0
                for (doc in snapshot) {
                    val name = doc.getString("food") ?: "Meal"
                    val kcal = doc.getLong("calories")?.toInt() ?: 0
                    val c = doc.getDouble("carbs") ?: 0.0
                    val p = doc.getDouble("protein") ?: 0.0
                    val f = (doc.getDouble("fats") ?: doc.getDouble("fat") ?: 0.0)
                    val mt = doc.getString("mealType") ?: getString(R.string.meal)
                    meals.add(MealItem(doc.id, "$mt • $name - ${kcal}kcal  C:${c}g P:${p}g F:${f}g", kcal, c, p, f))
                    totalCalories += kcal
                    totalCarbs += c
                    totalProtein += p
                    totalFat += f
                }
                refreshAdapter()
                updateTotals()
                setLoading(false)
            }
            .addOnFailureListener {
                setLoading(false)
                toast("Failed to load meals")
            }
    }

    // Get calories burned today from Google Fit
    private fun fetchCaloriesBurnedToday() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            burnedText.text = getString(R.string.burned_connect)
            updateTotals()
            return
        }

        Fitness.getHistoryClient(this, account)
            .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
            .addOnSuccessListener { result ->
                val total = if (result != null && !result.isEmpty) {
                    result.dataPoints.first().getValue(Field.FIELD_CALORIES).asFloat().toDouble()
                } else 0.0
                caloriesBurned = total
                burnedText.text = getString(R.string.burned_format, total.toInt())
                updateTotals()
            }
            .addOnFailureListener {
                burnedText.text = getString(R.string.burned_unavailable)
                updateTotals()
            }
    }

    // Update totals and display them
    private fun updateTotals() {
        totalsText.text = getString(R.string.totals_format, totalCalories, totalCarbs, totalProtein, totalFat)
        val net = totalCalories - caloriesBurned
        netText.text = getString(R.string.net_format, net.toInt())
    }

    // Refresh list adapter after data changes
    private fun refreshAdapter() {
        listView.adapter = buildMealsAdapter()
    }

    // Show edit/delete options for meal item
    private fun showMealOptions(position: Int) {
        val item = meals[position]
        val options = arrayOf("Edit", "Delete")
        android.app.AlertDialog.Builder(this)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> editMeal(position)
                    1 -> deleteMeal(position)
                }
            }
            .show()
    }

    // Display edit dialog for selected meal
    private fun editMeal(position: Int) {
        val item = meals[position]
        val parts = item.line
        val dialog = android.app.AlertDialog.Builder(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val nameInput = EditText(this).apply { hint = getString(R.string.food) }
        val kcalInput = EditText(this).apply { hint = getString(R.string.calories); inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val carbsInput = EditText(this).apply { hint = getString(R.string.carbs); inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val proteinInput = EditText(this).apply { hint = getString(R.string.protein); inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val fatInput = EditText(this).apply { hint = getString(R.string.fat); inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        layout.addView(nameInput)
        layout.addView(kcalInput)
        layout.addView(carbsInput)
        layout.addView(proteinInput)
        layout.addView(fatInput)

        dialog.setTitle("Edit Meal")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = nameInput.text.toString().ifEmpty { return@setPositiveButton }
                val newKcal = kcalInput.text.toString().toIntOrNull() ?: return@setPositiveButton
                val newC = carbsInput.text.toString().toDoubleOrNull() ?: 0.0
                val newP = proteinInput.text.toString().toDoubleOrNull() ?: 0.0
                val newF = fatInput.text.toString().toDoubleOrNull() ?: 0.0
                updateMeal(position, newName, newKcal, newC, newP, newF)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Update meal entry in Firestore and totals
    private fun updateMeal(position: Int, name: String, kcal: Int, c: Double, p: Double, f: Double) {
        val user = auth.currentUser ?: return
        val id = meals[position].id
        setLoading(true)
        firestore.collection("users").document(user.uid)
            .collection("nutrition").document(id)
            .update(mapOf(
                "food" to name,
                "calories" to kcal,
                "carbs" to c,
                "protein" to p,
                "fats" to f
            ))
            .addOnSuccessListener {
                // Update local totals and UI
                totalCalories += (kcal - meals[position].kcal)
                totalCarbs += (c - meals[position].c)
                totalProtein += (p - meals[position].p)
                totalFat += (f - meals[position].f)
                meals[position] = MealItem(id, "${meals[position].line.substringBefore(" • ")} • $name - ${kcal}kcal  C:${c}g P:${p}g F:${f}g", kcal, c, p, f)
                refreshAdapter()
                updateTotals()
                setLoading(false)
            }
            .addOnFailureListener {
                setLoading(false)
                toast("Failed to update meal")
            }
    }

    // Delete meal entry from Firestore
    private fun deleteMeal(position: Int) {
        val user = auth.currentUser ?: return
        val id = meals[position].id
        setLoading(true)
        firestore.collection("users").document(user.uid)
            .collection("nutrition").document(id)
            .delete()
            .addOnSuccessListener {
                // Adjust totals after deletion
                totalCalories -= meals[position].kcal
                totalCarbs -= meals[position].c
                totalProtein -= meals[position].p
                totalFat -= meals[position].f
                meals.removeAt(position)
                refreshAdapter()
                updateTotals()
                setLoading(false)
            }
            .addOnFailureListener {
                setLoading(false)
                toast("Failed to delete meal")
            }
    }

    // Clear input fields after adding meal
    private fun clearInputs() {
        foodName.text.clear()
        calories.text.clear()
        carbs.text.clear()
        protein.text.clear()
        fat.text.clear()
    }

    // Generate today's date as key (e.g., 2025-10-14)
    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", y, m, d)
    }

    // Show or hide loading spinner
    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    // Format double values (no decimals)
    private fun format(value: Double): String = String.format("%.0f", value)

    // Display a short message to user
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
