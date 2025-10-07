

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
*eat me
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
