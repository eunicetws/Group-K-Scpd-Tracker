package com.example.myapplication.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.courses.SchoolManager
import com.example.myapplication.courses.School
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun Courses(modifier: Modifier = Modifier) {
    var schools by remember { mutableStateOf<List<School>>(emptyList()) }
    var message by remember { mutableStateOf("") }

    var activeFilter by rememberSaveable { mutableStateOf(true) }   // start on active schools

    fun load() {
        SchoolManager.getSchools(activeFilter = activeFilter) { list, error ->
            schools = list.sortedBy { school -> school.schoolID } // A to Z
            if (error != null) message = "Could not load schools: $error"
        }
    }

    LaunchedEffect(Unit) { load() }

    var schoolCode by remember { mutableStateOf("") }
    var schoolName by remember { mutableStateOf("") }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->

        // Add School
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column() {
                Row(modifier = Modifier.padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = schoolCode,
                        onValueChange = { newCode -> schoolCode = newCode.uppercase() }, // all caps
                        label = { Text("Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters, imeAction = ImeAction.Next),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = schoolName,
                        onValueChange = { text -> schoolName = text },
                        label = { Text("School name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), // last box
                        modifier = Modifier.weight(2f)
                    )
                }
                Button(
                    modifier = Modifier.align(Alignment.End),   // stick to the right
                    onClick = {
                    if (schoolCode.isBlank() || schoolName.isBlank()) {
                        message = "Enter a school code and name"
                    } else {
                        // load ALL schools
                        SchoolManager.getSchools(activeFilter = null) { allSchools, error ->
                            if (error != null) {
                                message = "Could not load schools: $error"
                            } else {

                                // check school exists
                                var exists = false
                                for ((schoolID) in allSchools) {
                                    if (schoolID == schoolCode.trim()) {
                                        exists = true
                                        message =
                                            "School code " + schoolCode.trim() + " already exists"
                                    }
                                }

                                // Add new
                                if (!exists) {
                                    val new = mutableListOf<School>()
                                    new.addAll(allSchools)
                                    new.add(School(schoolCode.trim(), schoolName.trim()))

                                    // write to Firestore, then reload once the save is done
                                    SchoolManager.saveSchools(new) { success, saveError ->
                                        if (success) {
                                            message = ""
                                            schoolCode = ""
                                            schoolName = ""
                                            load()
                                        } else {
                                            message = "Could not save school: $saveError"
                                        }
                                    }
                                }
                            }
                        }
                    }
                }) {
                    Text("Add")
                }
            }

            HorizontalDivider()

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Show active only", modifier = Modifier.weight(1f))
                Switch(
                    checked = activeFilter,
                    onCheckedChange = { on ->
                        activeFilter = on
                        load()
                    }
                )
            }

            if (schools.isEmpty()) {
                Text("No schools")
            }

            for ((schoolID, schoolName1, _, active) in schools) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)   // space inside the card
                    ) {
                        Text(schoolID)
                        Text(schoolName1)
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
fun AddSchoolPreview() {
    MyApplicationTheme {
        Courses()
    }
}
