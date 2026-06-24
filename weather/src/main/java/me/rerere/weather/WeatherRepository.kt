package me.rerere.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.LocalDate
import kotlin.coroutines.resume

class WeatherRepository(
    context: Context,
    private val okHttpClient: OkHttpClient,
) {
    private val appContext = context.applicationContext
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun getLastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null
        val locationManager = appContext.getSystemService(LocationManager::class.java) ?: return null
        return runCatching {
            locationManager.getProviders(true)
                .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
                .maxByOrNull { it.time }
        }.getOrNull()
    }

    suspend fun loadLocalWeather(): WeatherReport {
        val location = getDeviceLocation()
            ?: error("没有可用的本机定位。请先授权定位，并确认系统定位服务已开启。")
        return loadWeather(location.latitude, location.longitude)
    }

    private suspend fun getDeviceLocation(): Location? {
        getLastKnownLocation()?.let { return it }
        if (!hasLocationPermission()) return null
        val locationManager = appContext.getSystemService(LocationManager::class.java) ?: return null
        val provider = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER,
        ).firstOrNull { provider ->
            runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
        } ?: return null

        return withTimeoutOrNull(15_000L) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (continuation.isActive) {
                            continuation.resume(location)
                        }
                        runCatching { locationManager.removeUpdates(this) }
                    }
                }
                runCatching {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                    continuation.invokeOnCancellation {
                        runCatching { locationManager.removeUpdates(listener) }
                    }
                }.onFailure {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    suspend fun loadWeather(latitude: Double, longitude: Double): WeatherReport = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter(
                "current",
                listOf(
                    "temperature_2m",
                    "relative_humidity_2m",
                    "apparent_temperature",
                    "precipitation",
                    "weather_code",
                    "wind_speed_10m"
                ).joinToString(",")
            )
            .addQueryParameter(
                "daily",
                listOf(
                    "weather_code",
                    "temperature_2m_max",
                    "temperature_2m_min",
                    "precipitation_sum"
                ).joinToString(",")
            )
            .addQueryParameter("forecast_days", "3")
            .addQueryParameter("timezone", "auto")
            .build()
        val request = Request.Builder().url(url).build()
        val body = okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("天气接口请求失败：HTTP ${response.code}")
            response.body.string()
        }
        val root = json.parseToJsonElement(body).jsonObject
        val current = root["current"]?.jsonObject ?: JsonObject(emptyMap())
        val daily = root["daily"]?.jsonObject ?: JsonObject(emptyMap())
        WeatherReport(
            latitude = root["latitude"]?.jsonPrimitive?.doubleOrNull ?: latitude,
            longitude = root["longitude"]?.jsonPrimitive?.doubleOrNull ?: longitude,
            timezone = root["timezone"]?.jsonPrimitive?.contentOrNull ?: "",
            currentTime = current["time"]?.jsonPrimitive?.contentOrNull ?: "",
            temperature = current["temperature_2m"]?.jsonPrimitive?.doubleOrNull,
            apparentTemperature = current["apparent_temperature"]?.jsonPrimitive?.doubleOrNull,
            humidity = current["relative_humidity_2m"]?.jsonPrimitive?.doubleOrNull,
            precipitation = current["precipitation"]?.jsonPrimitive?.doubleOrNull,
            windSpeed = current["wind_speed_10m"]?.jsonPrimitive?.doubleOrNull,
            weatherCode = current["weather_code"]?.jsonPrimitive?.doubleOrNull?.toInt(),
            daily = parseDaily(daily),
        )
    }

    private fun parseDaily(daily: JsonObject): List<DailyWeather> {
        val dates = daily["time"]?.jsonArray.orEmpty()
        val codes = daily["weather_code"]?.jsonArray.orEmpty()
        val maxTemps = daily["temperature_2m_max"]?.jsonArray.orEmpty()
        val minTemps = daily["temperature_2m_min"]?.jsonArray.orEmpty()
        val precipitation = daily["precipitation_sum"]?.jsonArray.orEmpty()
        return dates.indices.map { index ->
            DailyWeather(
                date = dates[index].jsonPrimitive.contentOrNull?.let {
                    runCatching { LocalDate.parse(it) }.getOrNull()
                },
                weatherCode = codes.getOrNull(index)?.jsonPrimitive?.doubleOrNull?.toInt(),
                maxTemperature = maxTemps.getOrNull(index)?.jsonPrimitive?.doubleOrNull,
                minTemperature = minTemps.getOrNull(index)?.jsonPrimitive?.doubleOrNull,
                precipitation = precipitation.getOrNull(index)?.jsonPrimitive?.doubleOrNull,
            )
        }
    }
}

data class WeatherReport(
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val currentTime: String,
    val temperature: Double?,
    val apparentTemperature: Double?,
    val humidity: Double?,
    val precipitation: Double?,
    val windSpeed: Double?,
    val weatherCode: Int?,
    val daily: List<DailyWeather>,
) {
    fun toJson(): JsonObject = buildJsonObject {
        put("latitude", latitude)
        put("longitude", longitude)
        put("timezone", timezone)
        put("current_time", currentTime)
        put("weather", weatherCode.description())
        temperature?.let { put("temperature_c", it) }
        apparentTemperature?.let { put("apparent_temperature_c", it) }
        humidity?.let { put("humidity_percent", it) }
        precipitation?.let { put("precipitation_mm", it) }
        windSpeed?.let { put("wind_speed_kmh", it) }
        put("daily", buildJsonArray {
            daily.forEach { item ->
                add(
                    buildJsonObject {
                        put("date", item.date?.toString() ?: "")
                        put("weather", item.weatherCode.description())
                        item.maxTemperature?.let { put("temperature_max_c", it) }
                        item.minTemperature?.let { put("temperature_min_c", it) }
                        item.precipitation?.let { put("precipitation_mm", it) }
                    }
                )
            }
        })
    }
}

data class DailyWeather(
    val date: LocalDate?,
    val weatherCode: Int?,
    val maxTemperature: Double?,
    val minTemperature: Double?,
    val precipitation: Double?,
)

fun Int?.description(): String = when (this) {
    0 -> "晴"
    1, 2 -> "少云"
    3 -> "阴"
    45, 48 -> "雾"
    51, 53, 55 -> "毛毛雨"
    56, 57 -> "冻毛毛雨"
    61, 63, 65 -> "雨"
    66, 67 -> "冻雨"
    71, 73, 75 -> "雪"
    77 -> "雪粒"
    80, 81, 82 -> "阵雨"
    85, 86 -> "阵雪"
    95 -> "雷暴"
    96, 99 -> "雷暴伴冰雹"
    else -> "未知"
}
