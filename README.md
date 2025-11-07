

# Vital – Android Fitness App

Vital is a comprehensive fitness tracking app designed to help users monitor their daily activity, nutrition, and health metrics. Built with Android and Google Fit integration, Vital provides a holistic approach to personal wellness through tracking, goal setting, and gamification.

---


## **Features**

* Secure user authentication via email/password or Google Sign-In
* Profile management with personal details and settings
* Real-time activity tracking through Google Fit API
* Nutrition and calorie tracking with optional food database integration
* Health metrics monitoring (heart rate, sleep, weight)
* Customizable fitness goals and progress tracking
* Gamification with points, levels, badges, and streaks
* Interactive dashboards and reports with historical data
* Push notifications for reminders and achievements

---

## **App Requirements**

### **1. Authentication & Accounts**

* Sign up via email/password or Google Sign-In (Firebase Auth)
* Secure login/logout
* Password reset via email
* Account deletion

---

### **2. User Profile & Settings**

* Input/edit: Name, Age, Gender, Height, Weight
* Optional profile picture
* Change email & password
* Manage app permissions (Google Fit data access)

---

### **3. Activity Tracking (via Google Fit API)**

* Steps (daily & historical)
* Calories burned
* Distance traveled
* Active minutes
* Workouts: type, duration, calories, heart rate (if available)
* Real-time or periodic data sync

---

### **4. Health Tracking**

* Heart rate monitoring (device dependent)
* Sleep tracking (duration & quality)
* Weight history

---

### **5. Nutrition Tracking**

* Manual meal input: food name, calories, macros (carbs, protein, fats)
* Daily calorie calculation & energy balance (calories in vs calories burned)
* Optional integration: Nutritionix API or Edamam API

---

### **6. Goals & Progress**

* Daily steps, calories burned, weight target, workout frequency
* Visual progress bars
* Push notifications for goal reminders

---

### **7. Gamification**

* Points system (e.g., 1 point per 100 steps)
* Levels and progression
* Badges & achievements (e.g., “First 5K Steps”, “7-Day Streak”)
* Streak tracking
* Optional leaderboards

---

### **8. Dashboard & Reports**

* Home screen: steps, calories, distance, active time
* Graphs: daily, weekly, monthly trends
* Nutrition dashboard: calorie intake & macros breakdown
* Historical reports for activity & meals

---

### **9. System Architecture**

* **Database:** Firebase Firestore
* **Authentication:** Firebase Auth (Google + email/password)
* **APIs:** Google Fit API
* **Hosting:** Firebase Hosting + Cloud Functions
* **Notifications:** Firebase Cloud Messaging

---

### **10. Non-Functional Requirements**

* Android first (Google Fit), iOS later (Apple Health)
* Scalable via Firebase
* Secure via Firebase Auth tokens
* Responsive UI with smooth performance

---

## **Installation**

1. Clone the repository:

   ```bash
   git clone https://github.com/yourusername/vital.git
   ```
2. Open the project in **Android Studio**
3. Configure Firebase project and `google-services.json`
4. Build and run the app on a connected device

---

## **Future Enhancements**

* Social features: friend leaderboards, challenges
* Integration with Apple Health (iOS version)
* AI-powered workout recommendations
* Nutrition database autocomplete for faster meal logging

---
Here’s a full **README** file summarizing all the **Vital App updates** you’ve built so far — each step includes the **feature name**, **file paths**, **description**, and **functionality overview**.
You can copy this directly into your project’s `README.md` file.

---

# 🩺 Vital App — Feature Expansion README

### 📱 Overview

**Vital** is a health and fitness tracking Android application built with **Kotlin**.
The app provides a holistic wellness experience, helping users monitor sleep, stress, activity, nutrition, and social fitness engagement.

This README covers all the new updates and modules implemented in recent development phases.

---

## 🧩 1. Sleep Tracking Module

**Path:**

```
app/src/main/java/com/example/vital/ui/sleep/SleepTrackingActivity.kt
app/src/main/res/layout/activity_sleep_tracking.xml
```

### 💡 Description

This feature analyzes and visualizes **sleep quality, duration, and patterns**.

### 🛠 Functionality

* Tracks total sleep time.
* Simulates or retrieves sleep stage data (light, deep, REM).
* Displays graphs or textual summaries of sleep quality.
* Can later integrate with **wearable data** (e.g., Fitbit, Galaxy Watch).

### 🔍 Example

Users can open the *Sleep Tracking* page to see:

> “You slept 7h 32m last night — 2h deep sleep, 5h light sleep.”

---

## 🌿 2. Stress Monitoring Module

**Path:**

```
app/src/main/java/com/example/vital/ui/stress/StressMonitoringActivity.kt
app/src/main/res/layout/activity_stress_monitoring.xml
```

### 💡 Description

Estimates user stress levels using **heart rate variability (HRV)** and provides **guided breathing** exercises.

### 🛠 Functionality

* Continuously estimates stress via simulated biometric data.
* Displays color-coded stress levels (Low, Moderate, High).
* Offers breathing exercises for relaxation.
* Includes animated breathing guides (expand/contract visuals).

### 🔍 Example

When stress level is high, a message prompts:

> “High stress detected — try a 2-minute breathing session.”

---

## ⚡ 3. Energy Score Metric

**Path:**

```
app/src/main/java/com/example/vital/ui/energy/EnergyScoreActivity.kt
app/src/main/res/layout/activity_energy_score.xml
```

### 💡 Description

Calculates a user’s **readiness score** (0–100) based on previous day’s **sleep, activity, and stress** metrics.

### 🛠 Functionality

* Combines multiple health metrics into a single “Energy Score.”
* Color-codes scores:

  * 🟢 70–100 → “Ready for the day!”
  * 🟡 40–69 → “Moderate energy.”
  * 🔴 Below 40 → “Rest recommended.”
* Encourages daily motivation through personalized feedback.

---

## 🏋️‍♂️ 4. Fitness Programs, Coaching & Challenges

**Path:**

```
app/src/main/java/com/example/vital/ui/fitness/FitnessProgramsActivity.kt
app/src/main/res/layout/activity_fitness_programs.xml
```

### 💡 Description

Provides structured **fitness programs** and **real-time workout coaching** options.

### 🛠 Functionality

* Users can select workout plans: *Endurance*, *Weight Loss*, *Balance Training*, etc.
* Displays short program descriptions, goals, and progress.
* Can later connect to guided videos or sensors for live feedback.

### 🔍 Example

Selecting *Endurance Training* shows a 4-week running plan with daily goals.

---

## 🤝 5. Social & Community Features (“Together” Mode)

**Path:**

```
app/src/main/java/com/example/vital/ui/social/TogetherModeActivity.kt
app/src/main/res/layout/activity_together_mode.xml
```

### 💡 Description

Encourages social motivation through **shared fitness challenges**.

### 🛠 Functionality

* Invite friends to step or calorie challenges.
* Displays leaderboard (sorted by activity points).
* Shows motivational messages and challenge duration.
* Lays the groundwork for a future backend with real user profiles and chats.

### 🔍 Example

“🏆 Stacey leads with 8,500 steps today! Can you beat her?”

---

## 🏠 6. UI Home Page with Progress Bars

**Path:**

```
app/src/main/java/com/example/vital/ui/home/HomeActivity.kt
app/src/main/res/layout/activity_home.xml
```

### 💡 Description

The main dashboard showing quick summaries of **sleep**, **stress**, **energy**, and **activity**.

### 🛠 Functionality

* Displays progress bars for each metric.
* Provides daily snapshot of user wellness.
* Includes navigation to other modules (Sleep, Stress, Meals, etc.).
* Simple, clean, and minimal UI using accent colors.

### 🔍 Example

“Energy: 78% | Sleep: 7h 30m | Stress: Moderate | Steps: 6,400”

---

## 🥇 7. Fitness Badges

**Path:**

```
app/src/main/java/com/example/vital/ui/badges/FitnessBadgesActivity.kt
app/src/main/res/layout/activity_fitness_badges.xml
```

### 💡 Description

Gamifies progress by rewarding users with **badges** for achievements.

### 🛠 Functionality

* Displays a grid of earned/unlocked badges.
* Triggers a badge animation when milestones are achieved (e.g., “10,000 steps in a day”).
* Encourages consistent engagement.

### 🔍 Example

“🎖️ You earned the *Consistency Champ* badge for 7 days of workouts!”

---

## 🗓️ 8. Exercise Tracking Calendar

**Path:**

```
app/src/main/java/com/example/vital/ui/calendar/ExerciseCalendarActivity.kt
app/src/main/res/layout/activity_exercise_calendar.xml
```

### 💡 Description

A calendar view that lets users review their workout history.

### 🛠 Functionality

* Uses a calendar interface to select dates.
* Displays all logged workouts (e.g., walking, cycling, yoga).
* Provides daily and weekly summaries.
* Can later integrate with Google Fit / device sensors for automatic sync.

### 🔍 Example

Tapping a date shows:

> “Workouts: 1h cycling, 30m yoga — 600 kcal burned.”

---

## 🍎 9. Meals Page

**Path:**

```
app/src/main/java/com/example/vital/ui/meals/MealsActivity.kt
app/src/main/java/com/example/vital/ui/meals/MealsAdapter.kt
app/src/main/res/layout/activity_meals.xml
app/src/main/res/layout/item_meal.xml
```

### 💡 Description

Allows users to log and view daily meals, along with calories and nutrition info.

### 🛠 Functionality

* Displays a list of meals (Breakfast, Lunch, Dinner, Snacks).
* Each entry shows calories and macronutrients (protein, carbs, fats).
* Includes date picker to view previous meals.
* “Add Meal” button inserts a new item (dummy for now, expandable later).

### 🔍 Example

Users can track:

> “Lunch: Grilled Chicken Salad — 450 kcal (P: 35g, C: 30g, F: 12g)”

---

## 🌐 Future Enhancements

* 🧘 **Guided meditation & mindfulness** integrations
* 📊 **Health trend charts** across weeks/months
* ☁️ **Cloud sync / user authentication**
* ⌚ **Integration with wearable devices (Fitbit, Galaxy Watch, Apple Watch)**
* 🔔 **Daily health reminders & motivational notifications**

---

## 💻 Tech Stack

* **Language:** Kotlin
* **Architecture:** MVVM-ready modular structure
* **UI Framework:** XML Views (Activities)
* **UI Components:** RecyclerView, ProgressBar, CardView, ConstraintLayout
* **Simulation:** Mock data placeholders ready for real sensor/DB integration
* **IDE:** Android Studio / Visual Studio 2022 (compatible)

---

## 🏁 Summary

Each feature builds toward making **Vital** a comprehensive **wellness assistant** app that tracks not just physical health, but emotional and social wellness too.
You now have all the **core health-tracking modules** and **UI foundations** ready for integration with live sensors, APIs, and authentication systems.

---


