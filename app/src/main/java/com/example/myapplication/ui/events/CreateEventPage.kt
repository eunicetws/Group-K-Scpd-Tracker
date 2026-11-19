package com.example.myapplication.ui.events

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
import com.example.myapplication.events.EventFormInput
import com.example.myapplication.ui.theme.MyApplicationTheme

@Composable
fun CreateEventPage(
    state: CreateEventUiState,
    onChange: (EventField, String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Create event", style = MaterialTheme.typography.headlineSmall)

        FormField("Title", EventField.TITLE, state.form.title, state, onChange)
        FormField(
            "Description (optional)", EventField.DESCRIPTION, state.form.description, state, onChange,
            singleLine = false, minLines = 3
        )
        FormField("Venue", EventField.VENUE, state.form.venue, state, onChange)
        FormField(
            "Start", EventField.START, state.form.start, state, onChange,
            placeholder = EventForm.DATE_FORMAT
        )
        FormField(
            "End", EventField.END, state.form.end, state, onChange,
            placeholder = EventForm.DATE_FORMAT
        )
        FormField(
            "Capacity", EventField.CAPACITY, state.form.capacity, state, onChange,
            keyboardType = KeyboardType.Number
        )
        FormField(
            "S-CPD points requested", EventField.POINTS, state.form.points, state, onChange,
            keyboardType = KeyboardType.Number
        )

        state.message?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        if (state.submitted) {
            Text("Submitted for approval. Status: Pending approval.")
        }

        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isSubmitting) "Submitting..." else "Submit for approval")
        }
    }
}

@Composable
private fun FormField(
    label: String,
    field: EventField,
    value: String,
    state: CreateEventUiState,
    onChange: (EventField, String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(field, it) },
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        isError = state.errors.containsKey(field),
        supportingText = state.errors[field]?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPagePreview() {
    MyApplicationTheme { CreateEventPage(CreateEventUiState(), { _, _ -> }, {}) }
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPageErrorsPreview() {
    val state = CreateEventUiState(
        form = EventFormInput(title = "", venue = "Hall A", start = "tomorrow", capacity = "0"),
        errors = mapOf(
            EventField.TITLE to "Title is required",
            EventField.START to "Enter the start as ${EventForm.DATE_FORMAT}",
            EventField.CAPACITY to "Capacity must be a whole number from 1 to 1000"
        )
    )
    MyApplicationTheme { CreateEventPage(state, { _, _ -> }, {}) }
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPageSubmittedPreview() {
    MyApplicationTheme { CreateEventPage(CreateEventUiState(submitted = true), { _, _ -> }, {}) }
}