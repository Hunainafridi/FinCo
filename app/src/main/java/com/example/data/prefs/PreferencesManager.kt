package com.example.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PreferencesManager(context: Context) {
    private val sharedPrefs = context.getSharedPreferences("finco_user_prefs", Context.MODE_PRIVATE)

    private val _appLanguage = MutableStateFlow(sharedPrefs.getString("app_language", "en") ?: "en")
    val appLanguage: StateFlow<String> = _appLanguage

    private val _startingBalance = MutableStateFlow(sharedPrefs.getFloat("starting_balance", 25000.0f).toDouble())
    val startingBalance: StateFlow<Double> = _startingBalance

    private val _hasOnboarded = MutableStateFlow(sharedPrefs.getBoolean("has_onboarded", false))
    val hasOnboarded: StateFlow<Boolean> = _hasOnboarded

    private val _pinPasscode = MutableStateFlow(sharedPrefs.getString("pin_passcode", null))
    val pinPasscode: StateFlow<String?> = _pinPasscode

    private val _smsAutoLoggingEnabled = MutableStateFlow(sharedPrefs.getBoolean("sms_auto_logging", false))
    val smsAutoLoggingEnabled: StateFlow<Boolean> = _smsAutoLoggingEnabled

    private val _isSandboxActive = MutableStateFlow(sharedPrefs.getBoolean("is_sandbox_active", false))
    val isSandboxActive: StateFlow<Boolean> = _isSandboxActive

    private val _sandboxEmail = MutableStateFlow(sharedPrefs.getString("sandbox_email", null))
    val sandboxEmail: StateFlow<String?> = _sandboxEmail

    fun setSandboxSession(active: Boolean, email: String?) {
        sharedPrefs.edit()
            .putBoolean("is_sandbox_active", active)
            .putString("sandbox_email", email)
            .apply()
        _isSandboxActive.value = active
        _sandboxEmail.value = email
    }

    fun setAppLanguage(language: String) {
        sharedPrefs.edit().putString("app_language", language).apply()
        _appLanguage.value = language
    }

    fun setStartingBalance(balance: Double) {
        sharedPrefs.edit().putFloat("starting_balance", balance.toFloat()).apply()
        _startingBalance.value = balance
    }

    fun setHasOnboarded(onboarded: Boolean) {
        sharedPrefs.edit().putBoolean("has_onboarded", onboarded).apply()
        _hasOnboarded.value = onboarded
    }

    fun setPinPasscode(pin: String?) {
        sharedPrefs.edit().putString("pin_passcode", pin).apply()
        _pinPasscode.value = pin
    }

    fun setSmsAutoLoggingEnabled(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("sms_auto_logging", enabled).apply()
        _smsAutoLoggingEnabled.value = enabled
    }

    // Secure local credentials database for sandbox testing when Firebase keys are unconfigured
    private val sandboxCreds = context.getSharedPreferences("finco_sandbox_creds", Context.MODE_PRIVATE)

    fun registerSandboxUser(email: String, pword: String): Boolean {
        sandboxCreds.edit().putString(email.lowercase().trim(), pword).apply()
        return true
    }

    fun verifySandboxUser(email: String, pword: String): Boolean {
        val saved = sandboxCreds.getString(email.lowercase().trim(), null)
        return saved != null && saved == pword
    }
}
