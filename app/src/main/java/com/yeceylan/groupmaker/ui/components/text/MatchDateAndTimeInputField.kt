package com.yeceylan.groupmaker.ui.components.text

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.yeceylan.groupmaker.R
import com.yeceylan.groupmaker.ui.theme.Dimen
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@Composable
fun MatchDateInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("Europe/Istanbul")
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val formattedDate =
                String.format(Locale.getDefault(), "%02d-%02d-%d", dayOfMonth, month + 1, year)
            onValueChange(formattedDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

    Box(
        modifier = modifier
            .padding(vertical = Dimen.spacing_xxs)
            .clickable { datePickerDialog.show() }
            .background(Color.LightGray, RoundedCornerShape(Dimen.spacing_xs))
            .border(Dimen.spacing_xxxs, Color.Gray, RoundedCornerShape(Dimen.spacing_xs))
            .padding(Dimen.spacing_xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = null,
                Modifier.size(Dimen.spacing_m2)
            )
            Spacer(modifier = Modifier.width(Dimen.spacing_xs))
            Text(
                text = value.ifEmpty { label },
                color = if (value.isEmpty()) Color.Gray else Color.Black
            )
        }
    }
}

@Composable
fun MatchTimeInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    matchDate: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getTimeZone("Europe/Istanbul")
    }

    if (matchDate.isNotEmpty()) {
        val dateParts = matchDate.split("-")
        if (dateParts.size == 3) {
            calendar.set(Calendar.YEAR, dateParts[2].toInt())
            calendar.set(Calendar.MONTH, dateParts[1].toInt() - 1)
            calendar.set(Calendar.DAY_OF_MONTH, dateParts[0].toInt())
        }
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            val selectedCalendar =
                Calendar.getInstance(TimeZone.getTimeZone("Europe/Istanbul")).apply {
                    set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                    set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                }

            if (selectedCalendar.timeInMillis < System.currentTimeMillis()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.past_time_selected_select_valid_time),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val formattedTime = String.format("%02d:%02d", hour, minute)
                onValueChange(formattedTime)
            }
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Box(
        modifier = modifier
            .padding(vertical = Dimen.spacing_xxs)
            .clickable { timePickerDialog.show() }
            .background(Color.LightGray, RoundedCornerShape(Dimen.spacing_xs))
            .border(Dimen.spacing_xxxs, Color.Gray, RoundedCornerShape(Dimen.spacing_xs))
            .padding(Dimen.spacing_xs)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.ic_clock),
                contentDescription = null,
                Modifier.size(Dimen.spacing_m2)
            )
            Spacer(modifier = Modifier.width(Dimen.spacing_xs))
            Text(
                text = value.ifEmpty { label },
                color = if (value.isEmpty()) Color.Gray else Color.Black
            )
        }
    }
}