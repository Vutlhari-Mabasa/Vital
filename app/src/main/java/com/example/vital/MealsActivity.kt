package com.example.vital

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.result.DailyTotalResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MealsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

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

    private data class MealItem(val id: String, val line: String, val kcal: Int, val c: Double, val p: Double, val f: Double)
    private val meals = mutableListOf<MealItem>()
    private lateinit var adapter: ArrayAdapter<String>

    private var totalCalories = 0
    private var totalCarbs = 0.0
    private var totalProtein = 0.0
    private var totalFat = 0.0
    private var caloriesBurned = 0.0

    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meals)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupList()
        setupListeners()
        setupMealType()

        loadMealsForToday()
        fetchCaloriesBurnedToday()
    }

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

    private fun setupList() {
        adapter = buildMealsAdapter()
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            showMealOptions(position)
        }
    }

    private fun buildMealsAdapter(): ArrayAdapter<String> {
        val items = meals.map { it.line }
        return object : ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                val tv = v.findViewById<TextView>(android.R.id.text1)
                tv.setTextColor(resources.getColor(R.color.black))
                return v
            }
        }
    }

    private fun setupListeners() {
        addButton.setOnClickListener { addMeal() }
    }

    private fun setupMealType() {
        val types = arrayOf("Breakfast", "Lunch", "Dinner", "Snack")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mealTypeSpinner = findViewById(R.id.spinnerMealType)
        mealTypeSpinner.adapter = adapter
    }

    private fun addMeal() {
        val name = foodName.text.toString().trim()
        val kcal = calories.text.toString().toIntOrNull() ?: 0
        val c = carbs.text.toString().toDoubleOrNull() ?: 0.0
        val p = protein.text.toString().toDoubleOrNull() ?: 0.0
        val f = fat.text.toString().toDoubleOrNull() ?: 0.0
        val mealType = mealTypeSpinner.selectedItem?.toString() ?: "Meal"

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
                    val mt = doc.getString("mealType") ?: "Meal"
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

    private fun fetchCaloriesBurnedToday() {
        val account = GoogleSignIn.getAccountForExtension(this, fitnessOptions)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            burnedText.text = "Burned: connect Google Fit"
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
                burnedText.text = "Burned: ${total.toInt()} kcal"
                updateTotals()
            }
            .addOnFailureListener {
                burnedText.text = "Burned: unavailable"
                updateTotals()
            }
    }

    private fun updateTotals() {
        totalsText.text = "Totals: ${totalCalories} kcal | C:${format(totalCarbs)}g P:${format(totalProtein)}g F:${format(totalFat)}g"
        val net = totalCalories - caloriesBurned
        netText.text = "Net: ${net.toInt()} kcal"
    }

    private fun refreshAdapter() {
        listView.adapter = buildMealsAdapter()
    }

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

    private fun editMeal(position: Int) {
        val item = meals[position]
        val parts = item.line
        val dialog = android.app.AlertDialog.Builder(this)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 20, 50, 20)
        }
        val nameInput = EditText(this).apply { hint = "Food" }
        val kcalInput = EditText(this).apply { hint = "Calories"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val carbsInput = EditText(this).apply { hint = "Carbs"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val proteinInput = EditText(this).apply { hint = "Protein"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        val fatInput = EditText(this).apply { hint = "Fat"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
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
                // adjust totals: remove old and add new
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

    private fun deleteMeal(position: Int) {
        val user = auth.currentUser ?: return
        val id = meals[position].id
        setLoading(true)
        firestore.collection("users").document(user.uid)
            .collection("nutrition").document(id)
            .delete()
            .addOnSuccessListener {
                // adjust totals
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

    private fun clearInputs() {
        foodName.text.clear()
        calories.text.clear()
        carbs.text.clear()
        protein.text.clear()
        fat.text.clear()
    }

    private fun getTodayKey(): String {
        val cal = Calendar.getInstance()
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", y, m, d)
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.INVISIBLE
    }

    private fun format(value: Double): String = String.format("%.0f", value)

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}


