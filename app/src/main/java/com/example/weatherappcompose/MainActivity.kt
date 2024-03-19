package com.example.weatherappcompose

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherappcompose.data.WeatherModel
import com.example.weatherappcompose.screens.DialogSearch
import com.example.weatherappcompose.screens.MainCard
import com.example.weatherappcompose.screens.TabLayout
import com.example.weatherappcompose.ui.theme.WeatherAppComposeTheme
import org.json.JSONObject


const val API_KEY = "b77464815cb64028860184018241103"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppComposeTheme {
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogState = remember {
                    mutableStateOf(false)
                }
                val currentDay = remember {
                    mutableStateOf(
                        WeatherModel(
                            "", "", "10.0", "", "",
                            "10.0", "10.0", ""
                        )
                    )
                }
                if (dialogState.value) {
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, this@MainActivity, daysList, currentDay)
                    })
                }
                getData("Saint Petersburg", this, daysList, currentDay)
                Image(
                    painter = painterResource(id = R.drawable.weather_bg),
                    contentDescription = "background",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.5f),
                    contentScale = ContentScale.Crop
                )
                Column {
                    MainCard(currentDay, onClickSync = {
                        getData("Saint Petersburg", this@MainActivity, daysList, currentDay)
                    },
                        onClickSearch = {
                            dialogState.value = true
                        })
                    TabLayout(daysList, currentDay)
                }
            }
        }
    }
}

private fun getData(
    city: String, context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "12" +
            "&aqi=no" +
            "&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        { error ->
            Log.d("MyLog", "getData error: $error")
        }
    )
    queue.add(sRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return emptyList()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city = city,
                time = item.getString("date"),
                currentTemp = "",
                conditionText = item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("text"),
                conditionIcon = item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("icon"),
                maxTemp = item.getJSONObject("day").getString("maxtemp_c"),
                minTemp = item.getJSONObject("day").getString("mintemp_c"),
                hours = item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c")
    )
    return list
}
