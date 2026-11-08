package com.example.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.login.AuthManager
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun AddUser(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min 6 characters)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isEmpty()) {
                    message = "Fill in name, email and password"
                    return@Button
                }
                saving = true
                message = "Creating user..."

                AuthManager.createUser(context, email, password, name) { success, result ->
                    saving = false
                    if (success) {
                        message = "Created user (ID: $result)"
                        name = ""
                        email = ""
                        password = ""
                    } else {
                        message = "Failed: $result"
                    }
                }
            },
            enabled = !saving
        ) {
            Text("Create user")
        }

        if (message.isNotEmpty()) {
            Text(message)
        }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AddUserPreview() {
    MyApplicationTheme {
        AddUser()
    }
}
