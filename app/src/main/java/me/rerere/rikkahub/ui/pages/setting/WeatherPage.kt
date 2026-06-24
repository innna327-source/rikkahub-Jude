package me.rerere.rikkahub.ui.pages.setting

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.ai.tools.LocalToolOption
import me.rerere.rikkahub.data.datastore.getCurrentAssistant
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Cloud
import me.rerere.hugeicons.stroke.CloudFastWind
import me.rerere.hugeicons.stroke.CloudSnow
import me.rerere.hugeicons.stroke.Droplet
import me.rerere.hugeicons.stroke.FastWind
import me.rerere.hugeicons.stroke.Humidity
import me.rerere.hugeicons.stroke.MapPin
import me.rerere.hugeicons.stroke.Rain
import me.rerere.hugeicons.stroke.Sun03
import me.rerere.hugeicons.stroke.SunCloud02
import me.rerere.hugeicons.stroke.SunCloudAngledZap02
import me.rerere.hugeicons.stroke.Thermometer
import me.rerere.weather.WeatherReport
import me.rerere.weather.WeatherRepository
import me.rerere.weather.description
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun WeatherPage(vm: SettingVM = koinViewModel()) {
    val context = LocalContext.current
    val settings by vm.settings.collectAsStateWithLifecycle()
    val assistant = settings.getCurrentAssistant()
    val weatherRepository = koinInject<WeatherRepository>()
    val scope = rememberCoroutineScope()
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var report by remember { mutableStateOf<WeatherReport?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission =
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    fun queryWeather() {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            )
            return
        }
        scope.launch {
            loading = true
            error = null
            runCatching {
                weatherRepository.loadLocalWeather()
            }.onSuccess {
                report = it
            }.onFailure {
                error = it.message ?: it.javaClass.name
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("天气") },
                navigationIcon = { BackButton() },
                scrollBehavior = scrollBehavior,
                colors = CustomColors.topBarColors,
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = CustomColors.topBarColors.containerColor,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("本机天气", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (hasLocationPermission) {
                                "已获得定位权限，可按本机位置查询天气。"
                            } else {
                                "需要定位权限来获取本机位置，然后由 App 直接查询天气。"
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { queryWeather() },
                            enabled = !loading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (loading) {
                                CircularProgressIndicator()
                            } else {
                                Text(if (hasLocationPermission) "查询天气" else "授权定位并查询")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("允许当前助手读取天气")
                            Text(
                                text = "开启后，连接的 AI 可以请求本机天气；每次实际调用前仍会弹出确认。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = assistant.localTools.contains(LocalToolOption.Weather),
                            onCheckedChange = { enabled ->
                                val localTools = if (enabled) {
                                    assistant.localTools + LocalToolOption.Weather
                                } else {
                                    assistant.localTools - LocalToolOption.Weather
                                }
                                vm.updateSettings(
                                    settings.copy(
                                        assistants = settings.assistants.map {
                                            if (it.id == assistant.id) {
                                                it.copy(localTools = localTools.distinct())
                                            } else {
                                                it
                                            }
                                        }
                                    )
                                )
                            }
                        )
                    }
                }
            }

            report?.let { weather ->
                item {
                    WeatherReportCard(weather)
                }
            }

            error?.let { message ->
                item {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherReportCard(report: WeatherReport) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WeatherIconBubble(
                    icon = weatherIcon(report.weatherCode),
                    modifier = Modifier.size(64.dp),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text("当前天气", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = report.temperature.format("℃"),
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        text = report.weatherCode.description(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            HorizontalDivider()

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeatherMetric(
                    icon = HugeIcons.Thermometer,
                    label = "体感",
                    value = report.apparentTemperature.format("℃"),
                    progress = report.apparentTemperature.progressIn(-20.0, 45.0),
                    modifier = Modifier.weight(1f),
                )
                WeatherMetric(
                    icon = HugeIcons.Humidity,
                    label = "湿度",
                    value = report.humidity.format("%"),
                    progress = report.humidity.progressIn(0.0, 100.0),
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeatherMetric(
                    icon = HugeIcons.Droplet,
                    label = "降水",
                    value = report.precipitation.format("mm"),
                    progress = report.precipitation.progressIn(0.0, 20.0),
                    modifier = Modifier.weight(1f),
                )
                WeatherMetric(
                    icon = HugeIcons.FastWind,
                    label = "风速",
                    value = report.windSpeed.format("km/h"),
                    progress = report.windSpeed.progressIn(0.0, 80.0),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HugeIcons.MapPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = "%.4f, %.4f  ${report.timezone}".format(report.latitude, report.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (report.daily.isNotEmpty()) {
                HorizontalDivider()
                report.daily.forEach { daily ->
                    DailyWeatherRow(
                        date = daily.date?.toString() ?: "",
                        icon = weatherIcon(daily.weatherCode),
                        description = daily.weatherCode.description(),
                        temperatureRange = "${daily.minTemperature.format("℃")} - ${daily.maxTemperature.format("℃")}",
                        precipitation = daily.precipitation.format("mm"),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherIconBubble(
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
private fun WeatherMetric(
    icon: ImageVector,
    label: String,
    value: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
        )
    }
}

@Composable
private fun DailyWeatherRow(
    date: String,
    icon: ImageVector,
    description: String,
    temperatureRange: String,
    precipitation: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = date, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = temperatureRange, style = MaterialTheme.typography.labelLarge)
            Text(
                text = "降水 $precipitation",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun weatherIcon(code: Int?): ImageVector {
    return when (code) {
        0 -> HugeIcons.Sun03
        1, 2, 3 -> HugeIcons.SunCloud02
        45, 48 -> HugeIcons.CloudFastWind
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> HugeIcons.Rain
        71, 73, 75, 77, 85, 86 -> HugeIcons.CloudSnow
        95, 96, 99 -> HugeIcons.SunCloudAngledZap02
        else -> HugeIcons.Cloud
    }
}

private fun Double?.progressIn(min: Double, max: Double): Float {
    val value = this ?: return 0f
    return ((value - min) / (max - min)).toFloat().coerceIn(0f, 1f)
}

private fun Double?.format(unit: String): String {
    return this?.let { "%.1f%s".format(it, unit) } ?: "--"
}
