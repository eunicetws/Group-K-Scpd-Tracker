package com.example.myapplication.ui.events

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.myapplication.events.EventField
import com.example.myapplication.events.EventForm
import com.example.myapplication.events.EventFormInput
import com.example.myapplication.events.EventMedia
import com.example.myapplication.events.EventOptions
import com.example.myapplication.events.EventSession
import com.example.myapplication.events.FileKind
import com.example.myapplication.events.FileUpload
import com.example.myapplication.login.AuthManager
import com.example.myapplication.ui.components.HeaderBack
import com.example.myapplication.ui.theme.Black
import com.example.myapplication.ui.theme.Grey
import com.example.myapplication.ui.theme.LightBlue
import com.example.myapplication.ui.theme.LightGrey
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.Navy
import com.example.myapplication.ui.theme.Red
import com.example.myapplication.ui.theme.White
import com.example.myapplication.ui.theme.boxColors
import com.example.myapplication.ui.theme.DialogTitleStyle
import com.example.myapplication.ui.theme.DialogHeadlineStyle
import com.example.myapplication.ui.theme.ButtonStyle
import com.example.myapplication.ui.theme.ErrorStyle
import com.example.myapplication.ui.theme.HeadingStyle
import com.example.myapplication.ui.theme.LabelStyle
import com.example.myapplication.ui.theme.LabelWhiteStyle
import com.example.myapplication.ui.theme.PillButtonStyle
import com.example.myapplication.ui.theme.PlaceholderStyle
import com.example.myapplication.ui.theme.SectionTitleStyle
import com.example.myapplication.ui.theme.ValueStyle
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private val LABEL_WIDTH = 120.dp
private const val LOWER_LABEL_WEIGHT = 1.2f
@Composable
fun CreateEventPage(
    state: CreateEventUiState,
    onChange: (EventField, String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    clubName: String = "",
    onBack: () -> Unit = {},
    onAddPoster: () -> Unit = {},
    onRemovePoster: (Int) -> Unit = {},
    onPosterAlternativeTextChange: (Int, String) -> Unit = { _, _ -> },
    onPickEpf: () -> Unit = {},
    onRemoveEpf: () -> Unit = {},
    onAddAttachment: () -> Unit = {},
    onRemoveAttachment: (Int) -> Unit = {},
    onAddSessions: (List<EventSession>) -> Unit = {},
    onRemoveSession: (Int) -> Unit = {}
) {
    var posterIndex by remember { mutableIntStateOf(0) }
    var showFull by remember { mutableStateOf(false) }
    var addDate by remember { mutableStateOf(false) }
    var venueIsOther by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val options = remember { EventOptions.load(context) }

    var shownIndex = posterIndex
    if (shownIndex > state.posters.size - 1) {
        shownIndex = state.posters.size - 1
    }
    if (shownIndex < 0) {
        shownIndex = 0
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        HeaderBack(onBackClick = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PosterCarousel(
                posters = state.posters,
                index = shownIndex,
                onIndexChange = { newIndex -> posterIndex = newIndex },
                onPosterClick = { showFull = true }
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.posters.isNotEmpty()) {
                    RemoveButton(text = "Remove", onClick = { onRemovePoster(shownIndex) })
                    Spacer(Modifier.width(8.dp))
                }
                AddButton(
                    text = "Add Poster",
                    icon = R.drawable.icon_document_add,
                    onClick = onAddPoster,
                    enabled = state.posters.size < EventMedia.MAXIMUM_POSTERS
                )
            }
            if (state.posters.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = state.posters[shownIndex].alternativeText,
                    onValueChange = { newText -> onPosterAlternativeTextChange(shownIndex, newText) },
                    label = { Text("Poster Description for screen readers") },
                    textStyle = ValueStyle,
                    colors = boxColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(20.dp))

            DetailRow("Title", state.errors[EventField.TITLE], required = true) {
                UnderlineField(
                    value = state.form.title,
                    onValueChange = { newValue -> onChange(EventField.TITLE, newValue) },
                    placeholder = "Event title",
                    isError = state.errors.containsKey(EventField.TITLE),
                    textStyle = ValueStyle
                )
            }
            Spacer(Modifier.height(12.dp))
            ClubRow(clubName)

            SectionDivider()

            var dateError = state.errors[EventField.START]
            if (dateError == null) {
                dateError = state.errors[EventField.END]
            }
            DetailRow("Date, Time", dateError, required = true) {
                Column {
                    SessionList(sessions = state.form.sessions, onRemove = onRemoveSession)
                    Spacer(Modifier.height(8.dp))
                    AddButton(text = "Add Date", icon = R.drawable.icon_calander, onClick = { addDate = true })
                }
            }
            val venueOptions = options.venues.toMutableList()
            if (!venueOptions.contains(OTHER)) {
                venueOptions.add(OTHER)
            }
            var showVenueBox = venueIsOther
            if (state.form.venue.isNotBlank() && !options.venues.contains(state.form.venue)) {
                showVenueBox = true
            }
            var shownVenue = state.form.venue
            if (showVenueBox) {
                shownVenue = OTHER
            }
            DetailRow("Venue", state.errors[EventField.VENUE], required = true) {
                Column {
                    SearchableDropdown(
                        value = shownVenue,
                        options = venueOptions,
                        placeholder = "Search venues",
                        onSelect = { picked ->
                            if (picked == OTHER) {
                                venueIsOther = true
                                onChange(EventField.VENUE, "")
                            } else {
                                venueIsOther = false
                                onChange(EventField.VENUE, picked)
                            }
                        },
                        isError = state.errors.containsKey(EventField.VENUE),
                        textStyle = ValueStyle
                    )
                    if (showVenueBox) {
                        Spacer(Modifier.height(8.dp))
                        UnderlineField(
                            value = state.form.venue,
                            onValueChange = { newValue -> onChange(EventField.VENUE, newValue) },
                            placeholder = "Type the venue",
                            isError = state.errors.containsKey(EventField.VENUE),
                            textStyle = ValueStyle
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
            DetailRow("Member", state.errors[EventField.MEMBER_PRICE]) {
                UnderlineField(
                    value = state.form.memberPrice,
                    onValueChange = { newValue -> onChange(EventField.MEMBER_PRICE, newValue) },
                    placeholder = "0 (free)",
                    prefix = "RM",
                    keyboardType = KeyboardType.Decimal,
                    isError = state.errors.containsKey(EventField.MEMBER_PRICE),
                    textStyle = ValueStyle
                )
            }
            DetailRow("Non-member", state.errors[EventField.NON_MEMBER_PRICE]) {
                UnderlineField(
                    value = state.form.nonMemberPrice,
                    onValueChange = { newValue -> onChange(EventField.NON_MEMBER_PRICE, newValue) },
                    placeholder = "0 (free)",
                    prefix = "RM",
                    keyboardType = KeyboardType.Decimal,
                    isError = state.errors.containsKey(EventField.NON_MEMBER_PRICE),
                    textStyle = ValueStyle
                )
            }
            DetailRow("Scpd", state.errors[EventField.POINTS]) {
                UnderlineField(
                    value = state.form.points,
                    onValueChange = { newValue -> onChange(EventField.POINTS, newValue) },
                    placeholder = "0",
                    keyboardType = KeyboardType.Number,
                    isError = state.errors.containsKey(EventField.POINTS),
                    textStyle = ValueStyle
                )
            }

            SectionDivider()

            OutlinedTextField(
                value = state.form.description,
                onValueChange = { newValue -> onChange(EventField.DESCRIPTION, newValue) },
                label = { Text("Description") },
                minLines = 5,
                textStyle = ValueStyle,
                colors = boxColors(),
                modifier = Modifier.fillMaxWidth()
            )

            SectionDivider(color = Navy)

            ClubViewRow("Target Participants", state.errors[EventField.TARGET_PARTICIPANTS], required = true) {
                UnderlineField(
                    value = state.form.targetParticipants,
                    onValueChange = { newValue -> onChange(EventField.TARGET_PARTICIPANTS, newValue) },
                    placeholder = "40",
                    keyboardType = KeyboardType.Number,
                    isError = state.errors.containsKey(EventField.TARGET_PARTICIPANTS),
                    textStyle = ValueStyle
                )
            }
            ClubViewRow("Maximum Sign Up", state.errors[EventField.CAPACITY]) {
                UnderlineField(
                    value = state.form.capacity,
                    onValueChange = { newValue -> onChange(EventField.CAPACITY, newValue) },
                    placeholder = "50",
                    keyboardType = KeyboardType.Number,
                    isError = state.errors.containsKey(EventField.CAPACITY),
                    textStyle = ValueStyle
                )
            }

            SectionDivider()

            ClubViewRow("Category", state.errors[EventField.CATEGORY], required = true) {
                SearchableDropdown(
                    value = state.form.category,
                    options = options.categories,
                    placeholder = "Search",
                    onSelect = { picked -> onChange(EventField.CATEGORY, picked) },
                    isError = state.errors.containsKey(EventField.CATEGORY),
                    textStyle = ValueStyle
                )
            }
            ClubViewRow("Activity Type", state.errors[EventField.ACTIVITY_TYPE], required = true) {
                SearchableDropdown(
                    value = state.form.activityType,
                    options = options.activityTypes,
                    placeholder = "Search",
                    onSelect = { picked -> onChange(EventField.ACTIVITY_TYPE, picked) },
                    isError = state.errors.containsKey(EventField.ACTIVITY_TYPE),
                    textStyle = ValueStyle
                )
            }

            SectionDivider()

            FileSection(title = "Upload EPF", required = true, error = state.errors[EventField.EPF]) {
                val epf = state.epf
                if (epf == null) {
                    AddButton(text = "Upload EPF", icon = R.drawable.icon_document_add, onClick = onPickEpf)
                } else {
                    Documents(epf, onRemove = onRemoveEpf)
                }
            }
            Spacer(Modifier.height(16.dp))
            FileSection(title = "Additional Attachments") {
                for (index in state.attachments.indices) {
                    val file = state.attachments[index]
                    Documents(file, onRemove = { onRemoveAttachment(index) })
                    Spacer(Modifier.height(8.dp))
                }
                AddButton(text = "Add Attachments", icon = R.drawable.icon_document_add, onClick = onAddAttachment)
            }

            SectionDivider()

            val message = state.message
            if (message != null) {
                Text(message, style = ErrorStyle, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
            }
            if (state.submitted) {
                Text("Submitted for approval. Status: Pending approval.", style = ValueStyle, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
            }

            var submitText = "Submit"
            if (state.isSubmitting) {
                submitText = "Submitting..."
            }
            PillButton(text = submitText, onClick = onSubmit, enabled = !state.isSubmitting)
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showFull && state.posters.isNotEmpty()) {
        PosterFullScreen(state.posters[shownIndex], onClose = { showFull = false })
    }

    if (addDate) {
        AddDatePopup(
            onPicked = { newSession ->
                onAddSessions(listOf(newSession))
                addDate = false
            },
            onDismiss = { addDate = false }
        )
    }
}


@Composable
private fun SectionDivider(color: Color = LightBlue) {
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        thickness = 3.dp,
        color = color
    )
}

@Composable
private fun ErrorText(error: String?) {
    if (error != null) {
        Text(
            error,
            style = ErrorStyle,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun DetailRow(
    label: String,
    error: String?,
    required: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row {
            Text(
                labelText(label, required),
                style = LabelStyle,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .width(LABEL_WIDTH)
                    .alignByBaseline()
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline()
            ) {
                content()
            }
        }
        Row {
            Spacer(Modifier.width(LABEL_WIDTH))
            ErrorText(error)
        }
    }
}

@Composable
private fun ClubViewRow(
    label: String,
    error: String?,
    required: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                labelText(label, required),
                style = ValueStyle,
                modifier = Modifier.weight(LOWER_LABEL_WEIGHT)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                content()
            }
        }
        Row {
            Spacer(Modifier.weight(LOWER_LABEL_WEIGHT))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            ) {
                ErrorText(error)
            }
        }
    }
}

@Composable
private fun ClubRow(clubName: String) {
    var name = clubName.trim()
    if (name.isEmpty()) {
        name = "Your club"
    }
    var initials = ""
    for (word in name.split(" ")) {
        if (word.isNotEmpty() && initials.length < 2) {
            initials += word.first().uppercase()
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Navy),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, style = LabelWhiteStyle)
        }
        Spacer(Modifier.width(14.dp))
        Text(name, style = HeadingStyle)
    }
}

@Composable
private fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    prefix: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    trailing: @Composable (() -> Unit)? = null
) {
    var lineColor = LightBlue
    if (isError) {
        lineColor = Red
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(Navy),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerField ->
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (prefix != null) {
                        Text("$prefix ", style = textStyle)
                    }
                    Box(Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(placeholder, style = PlaceholderStyle)
                        }
                        innerField()
                    }
                    if (trailing != null) {
                        trailing()
                    }
                }
                Spacer(Modifier.height(4.dp))
                HorizontalDivider(thickness = 1.dp, color = lineColor)
            }
        }
    )
}

private const val OTHER = "Other"

@Composable
private fun SearchableDropdown(
    value: String,
    options: List<String>,
    placeholder: String,
    onSelect: (String) -> Unit,
    textStyle: TextStyle,
    isError: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var search by remember(value) { mutableStateOf(value) }

    val matches = mutableListOf<String>()
    val searchText = search.trim()
    for (option in options) {
        if (searchText.isEmpty() || searchText == value || option.contains(searchText, ignoreCase = true)) {
            matches.add(option)
        }
    }

    Box {
        UnderlineField(
            value = search,
            onValueChange = { newText ->
                search = newText
                expanded = true
            },
            placeholder = placeholder,
            textStyle = textStyle,
            isError = isError,
            modifier = Modifier.onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    expanded = true
                }
            },
            trailing = {
                Icon(
                    painter = painterResource(R.drawable.icon_arrowdown),
                    contentDescription = "Show choices",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(6.dp)
                        .clickable { expanded = !expanded },
                    tint = Black
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                search = value
            },
            containerColor = White,
            properties = PopupProperties(focusable = false),
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            if (matches.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No match", style = PlaceholderStyle) },
                    onClick = {},
                    enabled = false
                )
            }
            for (option in matches) {
                DropdownMenuItem(
                    text = { Text(option, style = ValueStyle) },
                    onClick = {
                        onSelect(option)
                        search = option
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SessionList(sessions: List<EventSession>, onRemove: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (sessions.isEmpty()) {
            Text("No dates yet", style = PlaceholderStyle)
        }
        for (index in sessions.indices) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    EventForm.formatSession(sessions[index]),
                    style = ValueStyle,
                    modifier = Modifier.weight(1f)
                )
                CrossButton(description = "Remove date", onClick = { onRemove(index) })
            }
        }
    }
}

@Composable
private fun CrossButton(description: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_cross),
            contentDescription = description,
            modifier = Modifier.size(14.dp),
            tint = Black
        )
    }
}

@Composable
private fun AddButton(text: String, icon: Int, onClick: () -> Unit, enabled: Boolean = true) {
    var borderColor = Navy
    if (!enabled) {
        borderColor = LightGrey
    }
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = Black,
            disabledContainerColor = White,
            disabledContentColor = Grey
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = ButtonStyle)
    }
}

@Composable
private fun RemoveButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, Red),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = White,
            contentColor = Red
        ),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_cross),
            contentDescription = null,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(text, style = ButtonStyle)
    }
}

@Composable
private fun AddDatePopup(onPicked: (EventSession) -> Unit, onDismiss: () -> Unit) {
    var startDay by remember { mutableStateOf<Long?>(null) }
    var endDay by remember { mutableStateOf<Long?>(null) }
    var startHour by remember { mutableIntStateOf(9) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(10) }
    var endMinute by remember { mutableIntStateOf(0) }
    var pickingStartDay by remember { mutableStateOf(false) }
    var pickingEndDay by remember { mutableStateOf(false) }

    var session: EventSession? = null
    var error: String? = null
    val pickedStartDay = startDay
    if (pickedStartDay == null) {
        error = "Pick a start date"
    } else {
        var lastDay = pickedStartDay
        val pickedEndDay = endDay
        if (pickedEndDay != null) {
            lastDay = pickedEndDay
        }
        session = EventForm.makeSession(
            pickedStartDay,
            startHour * 60 + startMinute,
            lastDay,
            endHour * 60 + endMinute
        )
        if (session == null) {
            error = "Pick a start date"
        } else if (session.endTime <= session.startTime) {
            error = "End must be after the start"
        }
    }
    val errorMessage = error
    val newSession = session

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = White,
        title = { Text("Add Date", style = DialogTitleStyle) },
        text = {
            Column {
                DateTimeRow(
                    label = "Start date",
                    day = startDay,
                    dayPlaceholder = "Pick date",
                    hour = startHour,
                    minute = startMinute,
                    onDayClick = { pickingStartDay = true },
                    onHourPick = { hour -> startHour = hour },
                    onMinutePick = { minute -> startMinute = minute }
                )
                Spacer(Modifier.height(16.dp))
                DateTimeRow(
                    label = "End date",
                    day = endDay,
                    dayPlaceholder = "Same day",
                    hour = endHour,
                    minute = endMinute,
                    onDayClick = { pickingEndDay = true },
                    onHourPick = { hour -> endHour = hour },
                    onMinutePick = { minute -> endMinute = minute }
                )
                if (errorMessage != null && pickedStartDay != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, style = ErrorStyle)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newSession != null) {
                        onPicked(newSession)
                    }
                },
                enabled = errorMessage == null
            ) { Text("Add", style = ButtonStyle) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", style = ButtonStyle) }
        }
    )

    if (pickingStartDay) {
        DayPickerDialog(
            onPicked = { day ->
                startDay = day
                pickingStartDay = false
            },
            onDismiss = { pickingStartDay = false }
        )
    }
    if (pickingEndDay) {
        DayPickerDialog(
            onPicked = { day ->
                endDay = day
                pickingEndDay = false
            },
            onDismiss = { pickingEndDay = false }
        )
    }
}

@Composable
private fun DateTimeRow(
    label: String,
    day: Long?,
    dayPlaceholder: String,
    hour: Int,
    minute: Int,
    onDayClick: () -> Unit,
    onHourPick: (Int) -> Unit,
    onMinutePick: (Int) -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = LabelStyle, modifier = Modifier.width(DATE_BOX_WIDTH))
            Spacer(Modifier.width(12.dp))
            Text("Time", style = LabelStyle)
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            var dayText = dayPlaceholder
            var dayStyle = PlaceholderStyle
            if (day != null) {
                dayText = EventForm.formatDay(day)
                dayStyle = ValueStyle
            }
            PickerBox(text = dayText, style = dayStyle, width = DATE_BOX_WIDTH, onClick = onDayClick)
            Spacer(Modifier.width(12.dp))
            NumberDropdown(value = hour, count = 24, onPick = onHourPick)
            Text(" : ", style = ValueStyle)
            NumberDropdown(value = minute, count = 60, onPick = onMinutePick)
        }
    }
}

private val DATE_BOX_WIDTH = 120.dp

@Composable
private fun PickerBox(text: String, style: TextStyle, width: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .border(1.dp, Black, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = style, maxLines = 1)
    }
}

@Composable
private fun NumberDropdown(value: Int, count: Int, onPick: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        PickerBox(
            text = EventForm.twoDigits(value),
            style = ValueStyle,
            width = 52.dp,
            onClick = { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = White,
            modifier = Modifier.heightIn(max = 240.dp)
        ) {
            for (number in 0 until count) {
                DropdownMenuItem(
                    text = { Text(EventForm.twoDigits(number), style = ValueStyle) },
                    onClick = {
                        onPick(number)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DayPickerDialog(onPicked: (Long) -> Unit, onDismiss: () -> Unit) {
    val dayState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val day = dayState.selectedDateMillis
                    if (day != null) {
                        onPicked(day)
                    }
                },
                enabled = dayState.selectedDateMillis != null
            ) { Text("OK", style = ButtonStyle) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", style = ButtonStyle) }
        }
    ) {
        DatePicker(
            state = dayState,
            headline = {
                var headlineText = "Pick a date"
                val selectedDay = dayState.selectedDateMillis
                if (selectedDay != null) {
                    headlineText = EventForm.formatDay(selectedDay)
                }
                Text(
                    headlineText,
                    style = DialogHeadlineStyle,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                )
            },
            showModeToggle = false
        )
    }
}

@Composable
private fun FileSection(
    title: String,
    required: Boolean = false,
    error: String? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row{
            Text(labelText(title, required), style = SectionTitleStyle)

        }
        Spacer(Modifier.height(10.dp))
        content()
        ErrorText(error)
    }
}

private fun labelText(label: String, required: Boolean): AnnotatedString {
    return buildAnnotatedString {
        append("$label:")
        if (required) {
            withStyle(SpanStyle(color = Red)) {
                append(" *")
            }
        }
    }
}

@Composable
private fun Documents(file: FileUpload, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightBlue, RoundedCornerShape(12.dp))
            .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            file.name,
            style = ValueStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        CrossButton(description = "Remove " + file.name, onClick = onRemove)
    }
}

@Composable
private fun PillButton(text: String, onClick: () -> Unit, icon: Int? = null, enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = LightBlue,
            contentColor = Black
        ),
        elevation = null,
        border = BorderStroke(1.5.dp, Navy),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = PillButtonStyle)
    }
}

private fun showFileError(viewModel: CreateEventViewModel, error: String?) {
    if (error == null) {
        viewModel.showMessage("Could not read the file")
    } else {
        viewModel.showMessage(error)
    }
}

@Composable
fun CreateEventPageFirebase(modifier: Modifier = Modifier, onBack: () -> Unit = {}) {
    val inPreviewPane = LocalInspectionMode.current
    val viewModel = remember { CreateEventViewModel() }
    var clubId by remember { mutableStateOf("") }
    var clubName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (inPreviewPane) {
            return@LaunchedEffect
        }
        val user = AuthManager.currentUser ?: return@LaunchedEffect
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { userDocument ->
                val club = userDocument.getString("club")
                if (!club.isNullOrEmpty()) {
                    clubId = club
                    firestore.collection("clubs").document(club).get()
                        .addOnSuccessListener { clubDocument ->
                            val name = clubDocument.getString("name")
                            if (name != null) {
                                clubName = name
                            }
                        }
                }
            }
    }

    var pickPoster: () -> Unit = {}
    var pickEpf: () -> Unit = {}
    var pickAttachment: () -> Unit = {}

    if (!inPreviewPane) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        val posterLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                scope.launch {
                    val poster = withContext(Dispatchers.Default) { EventMedia.posterFromUri(context, uri) }
                    if (poster == null) {
                        viewModel.showMessage("Could not use this file, try another image or PDF")
                    } else {
                        viewModel.addPoster(poster)
                    }
                }
            }
        }
        pickPoster = {
            posterLauncher.launch(arrayOf("image/*", "application/pdf"))
        }

        val epfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                scope.launch {
                    val result = withContext(Dispatchers.IO) { EventMedia.fileFromUri(context, uri, FileKind.EPF) }
                    val file = result.file
                    if (file == null) {
                        showFileError(viewModel, result.error)
                    } else {
                        viewModel.setEpf(file)
                    }
                }
            }
        }
        pickEpf = { epfLauncher.launch(arrayOf("application/pdf")) }

        val attachmentLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenMultipleDocuments()
        ) { uris ->
            scope.launch {
                for (uri in uris) {
                    val result = withContext(Dispatchers.IO) {
                        EventMedia.fileFromUri(context, uri, FileKind.ATTACHMENT)
                    }
                    val file = result.file
                    if (file == null) {
                        showFileError(viewModel, result.error)
                    } else {
                        viewModel.addAttachment(file)
                    }
                }
            }
        }
        pickAttachment = { attachmentLauncher.launch(arrayOf("*/*")) }
    }

    CreateEventPage(
        state = viewModel.ui,
        onChange = viewModel::onFieldChange,
        onSubmit = { viewModel.submit(clubId) },
        modifier = modifier,
        clubName = clubName,
        onBack = onBack,
        onAddPoster = pickPoster,
        onRemovePoster = viewModel::removePoster,
        onPosterAlternativeTextChange = viewModel::setPosterAlternativeText,
        onPickEpf = pickEpf,
        onRemoveEpf = { viewModel.setEpf(null) },
        onAddAttachment = pickAttachment,
        onRemoveAttachment = viewModel::removeAttachment,
        onAddSessions = viewModel::addSessions,
        onRemoveSession = viewModel::removeSession
    )
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPagePreview() {
    MyApplicationTheme { CreateEventPageFirebase() }
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPageBlankPreview() {
    MyApplicationTheme { CreateEventPage(CreateEventUiState(), { _, _ -> }, {}) }
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPageErrorsPreview() {
    val state = CreateEventUiState(
        form = EventFormInput(title = "", venue = "Hall A", capacity = "0"),
        errors = mapOf(
            EventField.TITLE to "Title is required",
            EventField.START to "Add at least one date",
            EventField.CAPACITY to "Capacity must be a whole number from 1 to 1000",
            EventField.TARGET_PARTICIPANTS to "Target participants is required",
            EventField.CATEGORY to "Pick a category",
            EventField.ACTIVITY_TYPE to "Pick an activity type",
            EventField.EPF to "Upload the EPF"
        )
    )
    MyApplicationTheme { CreateEventPage(state, { _, _ -> }, {}) }
}

@Preview(showSystemUi = true)
@Composable
fun CreateEventPageSubmittedPreview() {
    MyApplicationTheme { CreateEventPage(CreateEventUiState(submitted = true), { _, _ -> }, {}) }
}

@Preview(name = "Create event - filled", showBackground = true, heightDp = 2000)
@Composable
fun CreateEventPageFilledPreview() {
    val context = LocalContext.current
    val righteous = ResourcesCompat.getFont(context, R.font.righteous)

    val sessions = mutableListOf<EventSession>()
    val firstStart = EventForm.parseDateTime("2026-10-24 12:00")
    val firstEnd = EventForm.parseDateTime("2026-10-24 13:00")
    val secondStart = EventForm.parseDateTime("2026-10-25 21:00")
    val secondEnd = EventForm.parseDateTime("2026-10-25 23:00")
    if (firstStart != null && firstEnd != null && secondStart != null && secondEnd != null) {
        sessions.add(EventSession(firstStart, firstEnd))
        sessions.add(EventSession(secondStart, secondEnd))
    }

    val state = CreateEventUiState(
        form = EventFormInput(
            title = "Music Festival",
            description = "A whole new world is waiting for you. Step into a realm where magic meets melody!",
            venue = "F3C09",
            sessions = sessions,
            capacity = "100",
            points = "2",
            memberPrice = "30",
            nonMemberPrice = "35",
            targetParticipants = "50",
            category = "Entertainment",
            activityType = "Festival"
        ),
        posters = listOf(
            samplePoster("MUSIC", LightBlue.toArgb(), Navy.toArgb(), righteous),
            samplePoster("FESTIVAL", Navy.toArgb(), White.toArgb(), righteous)
        ),
        epf = FileUpload(FileKind.EPF, "EPF_MusicFestival.pdf", "application/pdf", ""),
        attachments = listOf(
            FileUpload(FileKind.ATTACHMENT, "Budget.xlsx", "application/vnd.ms-excel", "")
        )
    )
    MyApplicationTheme {
        CreateEventPage(state, { _, _ -> }, {}, clubName = "Music Society")
    }
}

@Preview(name = "Create event - other venue", showBackground = true, heightDp = 1200)
@Composable
fun CreateEventPageOtherVenuePreview() {
    val state = CreateEventUiState(
        form = EventFormInput(
            title = "Rooftop Movie Night",
            venue = "Rooftop Garden, Block F",
            capacity = "60",
            targetParticipants = "40",
            category = "Entertainment",
            activityType = "Social Gathering"
        )
    )
    MyApplicationTheme {
        CreateEventPage(state, { _, _ -> }, {}, clubName = "Film Society")
    }
}
