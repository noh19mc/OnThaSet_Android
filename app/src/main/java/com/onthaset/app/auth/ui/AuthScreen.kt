package com.onthaset.app.auth.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.onthaset.app.auth.AuthViewModel

private val Yellow = Color(0xFFFFD600)
private val FieldBg = Color(0x14FFFFFF)

private enum class Mode { SignIn, SignUp }

@Composable
fun AuthScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    var mode by remember { mutableStateOf(Mode.SignIn) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val form by viewModel.form.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            OnThaSetShield(size = 100.dp)

            ModeToggle(mode = mode, onChange = { mode = it })

            FieldLabel("Email")
            DarkOutlinedField(
                value = email,
                onValueChange = { email = it },
                placeholder = "your@email.com",
                keyboardType = KeyboardType.Email,
            )

            FieldLabel("Password")
            DarkOutlinedField(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                keyboardType = KeyboardType.Password,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            tint = Color.Gray,
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                        )
                    }
                },
            )

            if (mode == Mode.SignUp) {
                FieldLabel("Confirm Password")
                DarkOutlinedField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    placeholder = "••••••••",
                    keyboardType = KeyboardType.Password,
                    visualTransformation = PasswordVisualTransformation(),
                )
            }

            if (mode == Mode.SignIn) {
                TextButton(
                    onClick = { showResetDialog = true },
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Forgot Password?", color = Yellow.copy(alpha = 0.85f), fontSize = 13.sp)
                }
            }

            Button(
                onClick = {
                    when (mode) {
                        Mode.SignIn -> viewModel.signIn(email, password)
                        Mode.SignUp -> viewModel.signUp(email, password, confirm)
                    }
                },
                enabled = !form.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Yellow, contentColor = Color.Black),
            ) {
                if (form.isLoading) {
                    CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        if (mode == Mode.SignIn) "SIGN IN" else "CREATE ACCOUNT",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            form.error?.let { msg ->
                Text(msg, color = Color(0xFFFF6B6B), fontSize = 13.sp)
            }

            TextButton(onClick = onBack) {
                Text("GO BACK", color = Yellow, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showResetDialog) {
        ResetPasswordDialog(
            initialEmail = email,
            onDismiss = { showResetDialog = false },
            onSend = { addr ->
                showResetDialog = false
                viewModel.resetPassword(addr)
            },
        )
    }

    if (form.resetEmailSent) {
        AlertDialog(
            onDismissRequest = viewModel::acknowledgeResetSent,
            confirmButton = {
                TextButton(onClick = viewModel::acknowledgeResetSent) { Text("OK") }
            },
            title = { Text("Email Sent") },
            text = { Text("Check your inbox for a password reset link.") },
        )
    }
}

@Composable
private fun ModeToggle(mode: Mode, onChange: (Mode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(FieldBg, RoundedCornerShape(10.dp)),
    ) {
        ModeTab("Sign In", selected = mode == Mode.SignIn) { onChange(Mode.SignIn) }
        ModeTab("Sign Up", selected = mode == Mode.SignUp) { onChange(Mode.SignUp) }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ModeTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) Yellow else Color.Transparent
    val fg = if (selected) Color.Black else Color.Gray
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = bg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .weight(1f)
            .height(44.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DarkOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = FieldBg,
            unfocusedContainerColor = FieldBg,
            cursorColor = Yellow,
            focusedBorderColor = Yellow,
            unfocusedBorderColor = Yellow.copy(alpha = 0.3f),
        ),
    )
}

@Composable
private fun ResetPasswordDialog(
    initialEmail: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
) {
    var addr by remember { mutableStateOf(initialEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter your email and we'll send a reset link.", fontSize = 13.sp)
                Spacer(Modifier.size(12.dp))
                OutlinedTextField(
                    value = addr,
                    onValueChange = { addr = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    placeholder = { Text("your@email.com") },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(addr) }) { Text("Send Reset Link") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
