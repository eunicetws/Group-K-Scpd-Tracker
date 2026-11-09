package com.example.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.R
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

    var clubs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var clubId by rememberSaveable { mutableStateOf<String?>(null) }
    var clubSearch by rememberSaveable { mutableStateOf("") }
    var clubDropdown by remember { mutableStateOf(false) }
    var clubFieldWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val noneChosen = clubSearch.trim().equals("None", ignoreCase = true)
    val searchText = if (noneChosen) "" else clubSearch.trim()
    val clubMatches = clubs.filter { club -> club.second.contains(searchText, ignoreCase = true) }

    LaunchedEffect(Unit) {
        AuthManager.getClubs { list, error ->
            clubs = list
            if (error != null) message = "Could not load clubs: $error"
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { newName -> name = newName },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { newEmail -> email = newEmail },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { newPassword -> password = newPassword },
                label = { Text("Password (min 6 characters)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedTextField(
                    value = clubSearch,
                    onValueChange = { newSearch ->
                        clubSearch = newSearch
                        clubId = null
                        clubDropdown = true
                    },
                    label = { Text("Club") },
                    placeholder = { Text("Club name") },
                    trailingIcon = { //Click v to open dropdown
                        IconButton(onClick = { clubDropdown = !clubDropdown }) {
                            Icon(
                                painter = painterResource(R.drawable.icon_arrowdown),
                                contentDescription = "Show clubs",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        // size change according to textbox size
                        .onSizeChanged { size -> clubFieldWidth = with(density) { size.width.toDp() } }
                        .onFocusChanged { focus -> if (!focus.isFocused) clubDropdown = false } //if focus not on club, close dropdown
                )

                DropdownMenu(
                    expanded = clubDropdown,
                    onDismissRequest = { clubDropdown = false },
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = false), //so keyboard or popup don't keep closing when typing
                    modifier = Modifier
                        .width(clubFieldWidth)
                        .heightIn(max = 200.dp)
                ) {
                    DropdownMenuItem( // if text is none, shows none and club id has nothing
                        text = { Text("None") },
                        onClick = {
                            clubId = null
                            clubSearch = "None"
                            clubDropdown = false
                        }
                    )
                    for ((id, clubName) in clubMatches) { // show club and record club id
                        DropdownMenuItem(
                            text = { Text(clubName) },
                            onClick = {
                                clubId = id
                                clubSearch = clubName
                                clubDropdown = false
                            }
                        )
                    }
                    if (clubMatches.isEmpty() && searchText.isNotBlank()) { //make sure something is chosen, if nothing is just none
                        DropdownMenuItem(
                            text = { Text("No matching club") },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank() || password.isEmpty()) {
                        message = "Fill in name, email and password"
                        return@Button
                    }
                    if (searchText.isNotBlank() && clubId == null) {
                        message = "Pick a club. If there is no club, pick 'None'"
                        return@Button
                    }
                    saving = true
                    message = "Creating user..."

                    AuthManager.createUser(context, email, password, name, clubId) { success, result ->
                        saving = false
                        if (success) {
                            message = "Created user (ID: $result)"
                            name = ""
                            email = ""
                            password = ""
                            clubId = null
                            clubSearch = ""
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
