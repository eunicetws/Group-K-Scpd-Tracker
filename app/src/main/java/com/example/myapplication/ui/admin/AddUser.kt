package com.example.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.R
import com.example.myapplication.courses.Course
import com.example.myapplication.courses.CourseManager
import com.example.myapplication.courses.SchoolManager
import com.example.myapplication.courses.School
import com.example.myapplication.login.AuthManager
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun AddUser(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var year by rememberSaveable { mutableStateOf("") }
    var libNum by rememberSaveable { mutableStateOf("") }

    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    var clubs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var clubId by rememberSaveable { mutableStateOf<String?>(null) }
    var clubSearch by rememberSaveable { mutableStateOf("") }
    var clubDropdown by remember { mutableStateOf(false) }
    val noneChosen = clubSearch.trim().equals("None", ignoreCase = true)
    var searchText = clubSearch.trim()
    if (noneChosen) {
        searchText = ""
    }

    val clubMatches = mutableListOf<Pair<String, String>>()
    for (club in clubs) {
        if (club.second.contains(searchText, ignoreCase = true)) {
            clubMatches.add(club)
        }
    }

    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var schoolCode by rememberSaveable { mutableStateOf<String?>(null) }
    var schoolSearch by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var schoolDropdown by remember { mutableStateOf(false) }
    val schoolSearchText = schoolSearch.text.trim()
    val schoolMatches = mutableListOf<School>()
    for (school in schools) {
        if (schoolCode != null) {
            schoolMatches.add(school)
        } else if ((school.schoolID + " - " + school.schoolName).contains(schoolSearchText, ignoreCase = true)) {
            schoolMatches.add(school)
        }
    }

    var courseId by rememberSaveable { mutableStateOf<String?>(null) }
    var courseSearch by rememberSaveable { mutableStateOf("") }
    var courseDropdown by remember { mutableStateOf(false) }
    val courseSearchText = courseSearch.trim()
    var schoolCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    val courseMatches = mutableListOf<Course>()
    for (course in schoolCourses) {
        if ((course.courseID + " - " + course.courseName).contains(courseSearchText, ignoreCase = true)) {
            courseMatches.add(course)
        }
    }

    LaunchedEffect(Unit) {
        AuthManager.getClubs { list, error ->
            clubs = list
            if (error != null) {
                message = "Could not load clubs: $error"
            }
        }
        SchoolManager.getSchools(activeFilter = true) { list, error ->
            schools = list
            if (error != null) {
                message = "Could not load schools: $error"
            }
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = email,
                onValueChange = { newEmail -> email = newEmail },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { newPassword -> password = newPassword },
                label = { Text("Password (min 6 characters)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedTextField(
                    value = schoolSearch,
                    onValueChange = { newSearch ->
                        if (newSearch.text != schoolSearch.text) {
                            schoolCode = null
                            schoolDropdown = true
                        }
                        schoolSearch = newSearch
                    },
                    label = { Text("School") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    placeholder = { Text("School") },
                    trailingIcon = {
                        IconButton(onClick = {
                            schoolDropdown = !schoolDropdown
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.icon_arrowdown),
                                contentDescription = "Show schools",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focus ->
                            if (focus.isFocused) {
                                // highlight the whole school name so typing replaces it
                                schoolSearch = schoolSearch.copy(selection = TextRange(0, schoolSearch.text.length))
                            } else {
                                schoolDropdown = false
                            }
                        }
                )

                DropdownMenu(
                    expanded = schoolDropdown,
                    onDismissRequest = { schoolDropdown = false },
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = false),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    for ((code, name1) in schoolMatches) {
                        DropdownMenuItem(
                            text = { Text("$code - $name1") },
                            onClick = {
                                if (code != schoolCode) {
                                    courseId = null
                                    courseSearch = ""
                                    schoolCourses = emptyList()
                                }
                                schoolCode = code
                                schoolSearch = TextFieldValue("$code - $name1")
                                schoolDropdown = false
                            }
                        )
                    }
                    if (schoolMatches.isEmpty() && schoolSearchText.isNotBlank()) {
                        DropdownMenuItem(
                            text = { Text("No matching school") },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }

            Box {
                OutlinedTextField(
                    value = courseSearch,
                    onValueChange = { newSearch ->
                        courseSearch = newSearch
                        courseId = null
                        if (!courseDropdown) {
                            CourseManager.getCourse(schoolID = schoolCode, activeFilter = true) { list, error ->
                                schoolCourses = list
                                if (error != null) {
                                    message = "Could not load courses: $error"
                                }
                            }
                        }
                        courseDropdown = true
                    },
                    label = { Text("Course") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    placeholder = {
                        if (schoolCode == null) {
                            Text("Choose a school first")
                        } else {
                            Text("Course name")
                        }
                    },
                    trailingIcon = { //Click v to open dropdown
                        IconButton(onClick = {
                            if (courseDropdown) {
                                courseDropdown = false
                            } else {
                                CourseManager.getCourse(schoolID = schoolCode, activeFilter = true) { list, error ->
                                    schoolCourses = list
                                    if (error != null) {
                                        message = "Could not load courses: $error"
                                    }
                                }
                                courseDropdown = true
                            }
                        }, enabled = schoolCode != null) {
                            Icon(
                                painter = painterResource(R.drawable.icon_arrowdown),
                                contentDescription = "Show courses",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    enabled = schoolCode != null, // need a school before picking a course
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focus ->
                            if (!focus.isFocused) {
                                courseDropdown = false
                            }
                        }
                )

                DropdownMenu(
                    expanded = courseDropdown,
                    onDismissRequest = { courseDropdown = false },
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = false),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    for ((id, name1) in courseMatches) {
                        DropdownMenuItem(
                            text = { Text("$id - $name1") },
                            onClick = {
                                courseId = id
                                courseSearch = "$id - $name1"
                                courseDropdown = false
                            }
                        )
                    }
                    if (courseMatches.isEmpty() && courseSearchText.isNotBlank()) {
                        DropdownMenuItem(
                            text = { Text("No matching course") },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }

            OutlinedTextField(
                value = year,
                onValueChange = { newYear ->
                    // keep numbers only
                    var digits = ""
                    for (letter in newYear) {
                        if (letter.isDigit()) {
                            digits += letter
                        }
                    }
                    year = digits
                },
                label = { Text("Year") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = libNum,
                onValueChange = { newLibNum ->
                    var digits = ""
                    for (letter in newLibNum) {
                        if (letter.isDigit()) {
                            digits += letter
                        }
                    }
                    libNum = digits
                },
                label = { Text("Library number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    placeholder = { Text("Club name") },
                    trailingIcon = {
                        IconButton(onClick = {
                            clubDropdown = !clubDropdown
                        }) {
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
                        .onFocusChanged { focus ->
                            if (!focus.isFocused) { //if focus not on club, close dropdown
                                clubDropdown = false
                            }
                        }
                )

                DropdownMenu(
                    expanded = clubDropdown,
                    onDismissRequest = { clubDropdown = false },
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = false), //so keyboard or popup don't keep closing when typing
                    modifier = Modifier.heightIn(max = 200.dp)
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
                    if (schoolSearchText.isNotBlank() && schoolCode == null) {
                        message = "Pick a school from the list, or leave it empty"
                        return@Button
                    }
                    if (courseSearchText.isNotBlank() && courseId == null) {
                        message = "Pick a course from the list, or leave it empty"
                        return@Button
                    }
                    if (libNum.isNotBlank() && libNum.toIntOrNull() == null) {
                        message = "Library number is too long"
                        return@Button
                    }
                    saving = true
                    message = "Creating user..."

                    AuthManager.createUser(
                        context,
                        email,
                        password,
                        name,
                        clubId,
                        school = schoolCode,
                        course = courseId,
                        year = year.toIntOrNull(),
                        libNum = libNum.toIntOrNull()
                    ) { success, result ->
                        saving = false
                        if (success) {
                            message = "Created user (ID: $result)"
                            name = ""
                            email = ""
                            password = ""
                            schoolCode = null
                            schoolSearch = TextFieldValue("")
                            courseId = null
                            courseSearch = ""
                            year = ""
                            libNum = ""
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
