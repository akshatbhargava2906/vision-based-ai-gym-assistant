package com.gymcoach.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

private val Ink = Color(0xFF111512)
private val Panel = Color(0xFF1B211D)
private val Mint = Color(0xFFB8F36B)
private val Muted = Color(0xFFA0AAA1)
private val ErrorRed = Color(0xFFFF897D)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("gym_coach_session", MODE_PRIVATE)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Mint,
                    onPrimary = Ink,
                    background = Ink,
                    surface = Panel,
                    onSurface = Color.White,
                    onSurfaceVariant = Muted,
                ),
            ) {
                AuthApp(
                    savedToken = preferences.getString("access_token", null),
                    onToken = { token -> preferences.edit().putString("access_token", token).apply() },
                    onLogout = { preferences.edit().remove("access_token").apply() },
                )
            }
        }
    }
}

@Composable
private fun AuthApp(
    savedToken: String?,
    onToken: (String) -> Unit,
    onLogout: () -> Unit,
) {
    var token by remember { mutableStateOf<String?>(null) }
    var checkingSession by remember { mutableStateOf(savedToken != null) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegister by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var signedInEmail by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(savedToken) {
        if (savedToken != null) {
            val session = runCatching {
                withContext(Dispatchers.IO) { fetchCurrentUser(savedToken) }
            }.getOrNull()
            if (session != null) {
                token = savedToken
                signedInEmail = session
            } else {
                onLogout()
            }
        }
        checkingSession = false
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
        if (checkingSession) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Mint)
            }
        } else if (token != null) {
            WelcomeScreen(
                email = signedInEmail ?: "Your account",
                onLogout = {
                    token = null
                    signedInEmail = null
                    error = null
                    onLogout()
                },
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Ink)
                    .padding(horizontal = 26.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Mint, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("G", color = Ink, fontWeight = FontWeight.Black, fontSize = 24.sp)
                    }
                    Text(
                        "  FORM / FUNCTION",
                        color = Muted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.6.sp,
                    )
                }
                Spacer(Modifier.height(36.dp))
                Text(
                    if (isRegister) "Build your\nstronger self." else "Welcome\nback.",
                    color = Color.White,
                    fontSize = 38.sp,
                    lineHeight = 42.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    if (isRegister) "Create an account to start your training profile."
                    else "Sign in to pick up where your training left off.",
                    color = Muted,
                    fontSize = 15.sp,
                )
                Spacer(Modifier.height(28.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email address") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(14.dp),
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    supportingText = if (isRegister) ({ Text("Use at least 10 characters") }) else null,
                    shape = RoundedCornerShape(14.dp),
                )
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error.orEmpty(), color = ErrorRed, fontSize = 13.sp)
                }
                Spacer(Modifier.height(22.dp))
                Button(
                    onClick = {
                        error = validate(email, password, isRegister)
                        if (error != null) return@Button
                        loading = true
                        scope.launch {
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    authenticate(email.trim(), password, isRegister)
                                }
                            }.onSuccess { result ->
                                token = result.first
                                signedInEmail = result.second
                                onToken(result.first)
                            }.onFailure { failure ->
                                error = failure.message ?: "Could not reach the server. Try again."
                            }
                            loading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !loading,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = Ink),
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = Ink,
                        )
                    } else {
                        Text(
                            if (isRegister) "Create account" else "Sign in",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        if (isRegister) "Already have an account?" else "New to Gym Coach?",
                        color = Muted,
                        fontSize = 14.sp,
                    )
                    TextButton(onClick = { isRegister = !isRegister; error = null }) {
                        Text(
                            if (isRegister) "Sign in" else "Create account",
                            color = Mint,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    "Your sets. Your progress. Coaching grounded in what you do.",
                    color = Muted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun WelcomeScreen(email: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(26.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("YOU’RE IN", color = Mint, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
        Spacer(Modifier.height(14.dp))
        Text("Welcome to\nyour training space.", color = Color.White, fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text(email, color = Muted, fontSize = 15.sp)
        Spacer(Modifier.height(30.dp))
        Text("Next up", color = Mint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Surface(color = Panel, shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Set up your training profile", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Text("Goals, experience, and measurements will live here.", color = Muted, fontSize = 14.sp)
            }
        }
        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onLogout) { Text("Sign out", color = Mint) }
    }
}

private fun validate(email: String, password: String, register: Boolean): String? {
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
        return "Enter a valid email address."
    }
    if (password.isBlank()) return "Enter your password."
    if (register && password.length < 10) return "Use at least 10 characters for your password."
    return null
}

private fun authenticate(email: String, password: String, register: Boolean): Pair<String, String> {
    val endpoint = if (register) "/api/v1/auth/register" else "/api/v1/auth/login"
    val connection = (URL(BuildConfig.API_BASE_URL + endpoint).openConnection() as HttpURLConnection)
    connection.requestMethod = "POST"
    connection.connectTimeout = 10_000
    connection.readTimeout = 15_000
    connection.doOutput = true
    connection.setRequestProperty("Content-Type", "application/json")
    try {
        val body = JSONObject().put("email", email).put("password", password).toString()
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val responseText = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        val response = JSONObject(responseText.ifBlank { "{}" })
        if (status !in 200..299) {
            val detail = response.optString("detail", "Sign in failed. Please try again.")
            throw IllegalStateException(detail)
        }
        return response.getString("access_token") to response.getJSONObject("user").getString("email")
    } finally {
        connection.disconnect()
    }
}

private fun fetchCurrentUser(token: String): String {
    val connection = (URL(BuildConfig.API_BASE_URL + "/api/v1/auth/me").openConnection() as HttpURLConnection)
    connection.requestMethod = "GET"
    connection.connectTimeout = 8_000
    connection.readTimeout = 10_000
    connection.setRequestProperty("Authorization", "Bearer $token")
    try {
        val status = connection.responseCode
        if (status !in 200..299) error("Session expired")
        val responseText = connection.inputStream.bufferedReader().use { it.readText() }
        return JSONObject(responseText).getString("email")
    } finally {
        connection.disconnect()
    }
}
