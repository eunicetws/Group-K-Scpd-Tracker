package com.example.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.example.myapplication.R
import com.example.myapplication.courses.Course
import com.example.myapplication.courses.CourseManager
import com.example.myapplication.courses.School
import com.example.myapplication.courses.SchoolManager
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun AddCourse(modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var schoolCode by rememberSaveable { mutableStateOf<String?>(null) }
    var schoolSearch by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var schoolDropdown by remember { mutableStateOf(false) }
    val schoolMatches = mutableListOf<School>()

    // check school dropdown, to filter course by school
    for (school in schools) {
        if (schoolCode != null) {
            schoolMatches.add(school)
        } else if ((school.schoolID + " - " + school.schoolName).contains(schoolSearch.text.trim(), ignoreCase = true)) {
            schoolMatches.add(school)
        }
    }

    var courseName by rememberSaveable { mutableStateOf("") }
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var activeFilter by rememberSaveable { mutableStateOf(true) }

    // Get list of schools
    LaunchedEffect(Unit) {
        SchoolManager.getSchools(activeFilter = true) { list, error ->
            schools = list
            if (error != null) {
                message = "Could not load schools: $error"
            }
        }
        // schoolCode null - all schools, otherwise only that school
        CourseManager.getCourse(schoolID = schoolCode, activeFilter = activeFilter) { list, error ->
            courses = list
            if (error != null) {
                message = "Could not load courses: $error"
            }
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // School
            Box {
                OutlinedTextField(
                    value = schoolSearch,
                    onValueChange = { newSearch ->
                        // clear school if text changed
                        if (newSearch.text != schoolSearch.text) {
                            // show all courses if no school picked
                            if (schoolCode != null) {
                                CourseManager.getCourse(schoolID = null, activeFilter = activeFilter) { list, error ->
                                    courses = list
                                    if (error != null) {
                                        message = "Could not load courses: $error"
                                    }
                                }
                            }
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
                                // highlight the textbox
                                schoolSearch = schoolSearch.copy(selection = TextRange(0, schoolSearch.text.length))
                            } else {
                                schoolDropdown = false
                            }
                        }
                )

                DropdownMenu(
                    expanded = schoolDropdown,
                    onDismissRequest = {
                        schoolDropdown = false
                    },
                    properties = PopupProperties(focusable = false, dismissOnClickOutside = false),
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    // show school and record school code
                    for ((code, name1) in schoolMatches) {
                        DropdownMenuItem(
                            text = { Text("$code - $name1") },
                            onClick = {
                                schoolCode = code
                                schoolSearch = TextFieldValue("$code - $name1")
                                schoolDropdown = false
                                // only show this school's courses
                                CourseManager.getCourse(schoolID = code, activeFilter = activeFilter) { list, error ->
                                    courses = list
                                    if (error != null) {
                                        message = "Could not load courses: $error"
                                    }
                                }
                            }
                        )
                    }
                    if (schoolMatches.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No matching school") },
                            onClick = {},
                            enabled = false
                        )
                    }
                }
            }

            // Course name
            OutlinedTextField(
                value = courseName,
                onValueChange = { text ->
                    courseName = text
                },
                label = { Text("Course name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // last box
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                modifier = Modifier.align(Alignment.End),   // stick to the right
                onClick = {
                    val code = schoolCode
                    if (code == null) {
                        message = "Pick a school"
                        return@Button
                    }
                    if (courseName.isBlank()) {
                        message = "Enter a course name"
                        return@Button
                    }

                    // check course name already exists in this school
                    var exists = false
                    for ((_, courseName1, schoolID) in courses) {
                        if (schoolID == code) {
                            if (courseName1.equals(courseName.trim(), ignoreCase = true)) {
                                exists = true
                            }
                        }
                    }
                    if (exists) {
                        message = "Course " + courseName.trim() + " already exists"
                        return@Button
                    }

                    saving = true
                    CourseManager.saveCourse(code, courseName) { _, error ->
                        saving = false
                        if (error != null) {
                            message = "Could not save course: $error"
                        } else {
                            message = ""
                            courseName = ""
                            CourseManager.getCourse(schoolID = schoolCode, activeFilter = activeFilter) { list, loadError ->
                                courses = list
                                if (loadError != null) {
                                    message = "Could not load courses: $loadError"
                                }
                            }
                        }
                    }
                },
                enabled = !saving
            ) {
                Text("Add")
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show active only", modifier = Modifier.weight(1f))
                Switch(
                    checked = activeFilter,
                    onCheckedChange = { on ->
                        activeFilter = on
                        CourseManager.getCourse(schoolID = schoolCode, activeFilter = activeFilter) { list, error ->
                            courses = list
                            if (error != null) {
                                message = "Could not load courses: $error"
                            }
                        }
                    }
                )
            }

            if (courses.isEmpty()) {
                Text("No courses")
            }

            for ((courseID, courseName1, _, active) in courses) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        // space inside the card
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(courseID)
                        Text(courseName1)
                        Spacer(modifier = Modifier.height(5.dp))
                        if (active) {
                            Text("Active")
                        } else {
                            Text("Inactive")
                        }
                    }
                }
            }

            if (message.isNotEmpty()) {
                Text(message)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AddCoursePreview() {
    MyApplicationTheme {
        AddCourse()
    }
}
