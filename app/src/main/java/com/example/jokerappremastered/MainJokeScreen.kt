package com.example.jokerappremastered

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainJokeScreen() {
    var programmingChecked by remember { mutableStateOf(false) }
    var miscChecked by remember { mutableStateOf(false) }
    var darkChecked by remember { mutableStateOf(false) }

    var singleChecked by remember { mutableStateOf(false) }
    var twoPartChecked by remember { mutableStateOf(false) }

    var nsfwChecked by remember { mutableStateOf(false) }
    var religiousChecked by remember { mutableStateOf(false) }

    var jokeResult by remember { mutableStateOf("And the joke is...") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Customize your joke",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Categories Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Categories:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CheckboxItem(
                    text = "Programming",
                    checked = programmingChecked,
                    onCheckedChange = { programmingChecked = it }
                )
                CheckboxItem(
                    text = "Misc",
                    checked = miscChecked,
                    onCheckedChange = { miscChecked = it }
                )
                CheckboxItem(
                    text = "Dark",
                    checked = darkChecked,
                    onCheckedChange = { darkChecked = it }
                )
            }
        }

        // Joke Type Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Select Joke Type:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CheckboxItem(
                    text = "Single",
                    checked = singleChecked,
                    onCheckedChange = { singleChecked = it }
                )
                CheckboxItem(
                    text = "Two Part",
                    checked = twoPartChecked,
                    onCheckedChange = { twoPartChecked = it }
                )
            }
        }

        // Blacklist Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Blacklist:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CheckboxItem(
                    text = "NSFW",
                    checked = nsfwChecked,
                    onCheckedChange = { nsfwChecked = it }
                )
                CheckboxItem(
                    text = "Religious",
                    checked = religiousChecked,
                    onCheckedChange = { religiousChecked = it }
                )
            }
        }

        // Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = {
                    // Submit logic
                    val selectedCategories = mutableListOf<String>().apply {
                        if (programmingChecked) add("Programming")
                        if (miscChecked) add("Misc")
                        if (darkChecked) add("Dark")
                    }

                    if (selectedCategories.isEmpty()) {
                        jokeResult = "Please select at least one category."
                        return@Button
                    }

                    val jokeType = when {
                        singleChecked && !twoPartChecked -> "single"
                        !singleChecked && twoPartChecked -> "twopart"
                        singleChecked && twoPartChecked -> null
                        else -> null
                    }

                    val blacklistFlags = mutableListOf<String>().apply {
                        if (nsfwChecked) add("nsfw")
                        if (religiousChecked) add("religious")
                    }

                    val categoriesString = selectedCategories.joinToString(",")
                    val blacklistString = if (blacklistFlags.isNotEmpty()) blacklistFlags.joinToString(",") else null

                    fetchJokeCompose(categoriesString, jokeType, blacklistString) { result ->
                        jokeResult = result
                    }
                },
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text("Send Request")
            }

            Button(
                onClick = {
                    // Clear logic
                    programmingChecked = false
                    miscChecked = false
                    darkChecked = false
                    singleChecked = false
                    twoPartChecked = false
                    nsfwChecked = false
                    religiousChecked = false
                    jokeResult = "And the joke is..."
                }
            ) {
                Text("Clear Form")
            }
        }

        // Result Text
        Text(
            text = jokeResult,
            fontSize = 18.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        )
    }
}

@Composable
fun CheckboxItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// Helper function for fetching jokes
fun fetchJokeCompose(
    categories: String?,
    type: String?,
    blacklist: String?,
    onResult: (String) -> Unit
) {
    val call = RetrofitInstance.api.getJoke(categories?.ifEmpty { null }, type, blacklist?.ifEmpty { null })
    call.enqueue(object : retrofit2.Callback<JokeResponse> {
        override fun onResponse(call: retrofit2.Call<JokeResponse>, response: retrofit2.Response<JokeResponse>) {
            val body = response.body()
            val result = if (body != null) {
                when (body.type) {
                    "single" -> body.joke ?: "No joke found."
                    "twopart" -> "${body.setup}\n\n${body.delivery}"
                    else -> "No joke found."
                }
            } else {
                "No joke found, sorry buddy."
            }
            onResult(result)
        }

        override fun onFailure(call: retrofit2.Call<JokeResponse>, t: Throwable) {
            onResult("Sorry no jokes for you: ${t.message}")
        }
    })
}