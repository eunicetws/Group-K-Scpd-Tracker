package com.example.myapplication.ui.login

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.foundation.text.input.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import com.example.myapplication.R
import com.example.myapplication.Role
import com.example.myapplication.login.AuthManager
import com.example.myapplication.ui.components.Header
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.Roboto

// Email: hfysj7@notts.edu.my
// Password: A1234567890.
// Login sucessful doesn't show anything currently
//

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    onLoginSuccess: (role: Role) -> Unit = {}
) {
    var role by rememberSaveable { mutableStateOf(Role.STUDENT) }
    val emailState = rememberTextFieldState()
    val passwordState = rememberTextFieldState()
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Header()

        Column( // Body
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 40.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo_scpd),
                contentDescription = "sCPD logo",
                modifier = Modifier
                    .width(230.dp)
                    .padding(vertical = 40.dp)
            )

            Column( // Choose role
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Label("Login as:")
                Dropdown(
                    selected = role.label,
                    options = Role.entries.map { r -> r.label },
                    onSelect = { label -> role = Role.entries.first { r -> r.label == label } },
                    background = Color.LightGray,
                    modifier = Modifier.width(250.dp)
                )
            }

            HorizontalDivider(thickness = 3.dp, color = LightBlue)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Label("Email:")
                TextBox(
                    state = emailState,
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.width(250.dp)
                )

                Label("Password:")
                TextBox(
                    state = passwordState,
                    keyboardType = KeyboardType.Password,
                    modifier = Modifier.width(250.dp),
                    isPrivate = true
                )

                Button(
                    onClick = {
                        val email = emailState.text.toString()
                        val password = passwordState.text.toString()
                        if (email.isBlank() || password.isEmpty()) {
                            message = "Enter your email and password"
                            return@Button
                        }
                        loading = true
                        message = ""
                        AuthManager.logIn(role, email, password) { success, error ->
                            loading = false
                            if (success) {
                                onLoginSuccess(role)
                            } else {
                                if (error != null) {
                                    message = error
                                } else {
                                    message = "Login failed"
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(width = 125.dp, height = 52.dp),
                    enabled = !loading,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LightBlue,
                        contentColor = Color.Black,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.6f),
                        disabledContentColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF555555))
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    }
                }

                if (message.isNotEmpty()) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = Roboto,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(
        text = text,
        color = Color.Black,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = Roboto
    )
}

// Rounded text box
@Composable
private fun TextBox(
    state: TextFieldState,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier,
    isPrivate: Boolean = false
) {
    val textStyle = TextStyle(
        color = Color.Black,
        fontSize = 15.sp,
        fontFamily = Roboto,
        textAlign = TextAlign.Center
    )
    val keyboardOptions = KeyboardOptions(keyboardType = keyboardType)

    val textBoxUi = TextFieldDecorator { innerTextField ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .border(2.dp, Navy, RoundedCornerShape(50))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) { innerTextField() }
    }

    if (isPrivate) {
        BasicSecureTextField(
            state = state,
            modifier = modifier,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            decorator = textBoxUi
        )
    } else {
        BasicTextField(
            state = state,
            modifier = modifier,
            textStyle = textStyle,
            keyboardOptions = keyboardOptions,
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = textBoxUi
        )
    }
}

@Composable
private fun Dropdown(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    background: Color,
    modifier: Modifier = Modifier,
    border: BorderStroke = BorderStroke(2.dp, Color.Black)
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(50)
    var dropdownWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .onSizeChanged { size -> dropdownWidth = with(density) { size.width.toDp() } }
                .clip(shape)
                .background(background)
                .border(border, shape)
                .clickable { expanded = true }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selected,
                modifier = Modifier.weight(1f),
                color = Color.Black,
                fontSize = 16.sp,
                fontFamily = Roboto
            )
            Icon(
                painter = painterResource(R.drawable.icon_arrowdown),
                contentDescription = "Open",
                modifier = Modifier.size(16.dp),
                tint = Color.Black
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(dropdownWidth)
                .heightIn(max = 200.dp),
            containerColor = Color(0xFFDCE6F2)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.Black, fontFamily = Roboto) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginPagePreview() {
    LoginPage()
}
