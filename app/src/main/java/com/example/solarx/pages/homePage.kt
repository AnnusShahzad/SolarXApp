package com.example.solarx.pages

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PathMeasure
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solarx.R
import com.example.solarx.generateRandomNumber
import com.example.solarx.pages.viewM.responseViewModel
import com.example.solarx.ui.theme.SolarXTheme
import kotlinx.coroutines.delay
import kotlin.math.abs

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
//@Preview(device= Devices.PIXEL_3)
@Composable
fun HomePage(modifier: Modifier = Modifier) {
    val nav2 = rememberNavController()
    val SharedPref = nav2.context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val SharedIPAddress = SharedPref.getString("IPAddress", "192.168.10.10").toString()
    val SharedPocketWIFISN = SharedPref.getString("Serial", "").toString()
    SolarXTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavBar(nav2)
            }
        )
        {
            paddingValues ->
            NavHost(navController = nav2, startDestination = "DashboardView") {
                composable(route="DashboardView")
                {
                    DashboardView(SharedIPAddress,SharedPocketWIFISN,paddingValues = paddingValues)
                }
                composable(route="DetailsView")
                {
                    DetailView(SharedIPAddress,SharedPocketWIFISN,paddingValues = paddingValues)
                }
                composable(route="SettingsView")
                {
                    SettingsView(paddingValues)
                }
            }
        }
    }
}

@Composable
fun IconMapper(
    modifier: Modifier = Modifier,
    ImageId: Int,
    text: String
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = ImageId),
            contentDescription = ""
        )
        Text(
            text = text,
            color = Color.White,
            fontSize = 6.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp),
        )
    }
}

@Composable
fun NavBar(navHostController: NavHostController,modifier: Modifier = Modifier) {
    var currentIndex by remember { mutableStateOf(1)}
    NavigationBar() {
        NavigationBarItem(
            selected = currentIndex == 1,
            onClick = {
                currentIndex = 1
                navHostController.navigate("DashboardView")
            },
            icon = {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.baseline_dashboard_24),
                    contentDescription = ""
                )
            },
            label = { Text(text = "Dashboard")}
        )
        NavigationBarItem(
            selected = currentIndex == 2,
            onClick = {
                currentIndex = 2
                navHostController.navigate("DetailsView")
            },
            icon = {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Data"
                )
            },
            label = {Text("Details")}
        )
        NavigationBarItem(
            selected = currentIndex == 3,
            onClick = {
                currentIndex = 3
                navHostController.navigate("SettingsView")
            },
            icon = {
                androidx.compose.material3.Icon(
                    painter = painterResource(id = R.drawable.baseline_settings_24),
                    contentDescription = "Settings"
                )
            },
            label = {Text("Settings")}
        )
    }
}

@Composable
fun CreateAnim(data: Float, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (500/abs(data) * 1000).toInt(),
                easing = EaseInSine
            ),
            repeatMode = RepeatMode.Restart,
        ),
        targetValue = 1f, label = "Moving Electricity"
    )
}

fun bit32(b:Int, c:Int): Number
{
        if (c < 32768)
            return b + c * 65536;
        else
            return b + c * 65536 - 4294967296;
}

@Composable
fun DashboardView(IP: String, SN: String, paddingValues: PaddingValues, modifier: Modifier = Modifier) {
    val scaleBy: Float = 2f
    val context = LocalContext.current
    var WAPDA: Int by remember { mutableIntStateOf(0) }
    var SOLAR: Int by remember { mutableIntStateOf(0) }
    val t = responseViewModel(IP, SN)
    LaunchedEffect(Unit) {
        while (true)
        {
            try {
                t.getRealTimeData {
                        result ->
                    WAPDA = (bit32(result[29],result[30]).toInt() * 0.1).toInt()
                    SOLAR = result[9]
                }
            }
            catch (ex: Exception) {
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                WAPDA = generateRandomNumber().GridValue.toInt()
                SOLAR = generateRandomNumber().SolarValue.toInt()
            }
            delay(1000)
        }
    }
    var BoxSize by remember { mutableStateOf(Size.Zero)}
    val SolarToGrid by remember {
        mutableStateOf(Path())
    }
    val GridToLoad by remember {
        mutableStateOf(Path())
    }
    val SolarToLoad by remember {
        mutableStateOf(Path())
    }
    val infiniteTransition = rememberInfiniteTransition()
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 5000,
                easing = EaseInSine
            ),
            repeatMode = RepeatMode.Restart,

            ),
        targetValue = 1f, label = "Moving Electricity2"
    )
    val GridRelatedPos = FloatArray(2)
    val GridRelatedTan = FloatArray(2)
    val SolarRelatedPos = FloatArray(2)
    val SolarRelatedTan = FloatArray(2)
    if (BoxSize != Size.Zero) {
        SolarToGrid.moveTo(BoxSize.width/2+50, 0f)
        SolarToGrid.lineTo(BoxSize.width/2+50, BoxSize.height/2-150)
        SolarToGrid.arcTo(
            rect = Rect(Offset(BoxSize.width/2+50+100f, BoxSize.height/2-150), 100f),
            startAngleDegrees = -180f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )
        SolarToGrid.lineTo(BoxSize.width, BoxSize.height/2-50)
        SolarToGrid.moveTo(BoxSize.width, BoxSize.height/2-50)
        SolarToGrid.close()
        GridToLoad.moveTo(BoxSize.width, BoxSize.height/2+50)
        GridToLoad.lineTo(BoxSize.width/2+150, BoxSize.height/2+50)
        GridToLoad.arcTo(
            rect = Rect(Offset(BoxSize.width/2+150, BoxSize.height/2+150), 100f),
            startAngleDegrees = -90f,
            sweepAngleDegrees = -90f,
            forceMoveTo = false
        )
        GridToLoad.lineTo(BoxSize.width/2+50, BoxSize.height)
        GridToLoad.moveTo(BoxSize.width/2+50, BoxSize.height/2+150)
        GridToLoad.close()
        SolarToLoad.moveTo(BoxSize.width / 2, 0f)
        SolarToLoad.lineTo(BoxSize.width / 2, BoxSize.height)
        SolarToLoad.moveTo(BoxSize.width / 2, BoxSize.height)
        SolarToLoad.close()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
//        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box (
            modifier = Modifier
                .weight(5f)
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(50.dp)
                .drawBehind {
                    BoxSize = size
                    PathMeasure().apply {
                        setPath((if (WAPDA < 0) GridToLoad else SolarToGrid).asAndroidPath(), false)
                        getPosTan(length * progress, GridRelatedPos, GridRelatedTan)
                    }
                    drawPath(
                        color = Color.Black,
                        path = SolarToGrid,
                        style = Stroke(width = 8f)
                    )
                    drawLine(
                        color = Color.Black,
                        start = Offset(BoxSize.width / 2, 0f),
                        end = Offset(BoxSize.width / 2, BoxSize.height),
                        strokeWidth = 8f
                    )
                    drawPath(
                        color = Color.Black,
                        path = GridToLoad,
                        style = Stroke(width = 8f)
                    )
                    if(WAPDA != 0) {
                        drawCircle(
                            color = if (WAPDA < 0) Color.Red else Color.Blue,
                            radius = 6.dp.toPx(),
                            center = Offset(GridRelatedPos[0], GridRelatedPos[1])
                        )
                    }
                    if (SOLAR > 0) {
                        PathMeasure().apply {
                            setPath(SolarToLoad.asAndroidPath(), false)
                            getPosTan(length * progress, SolarRelatedPos, SolarRelatedTan)
                        }
                        drawCircle(
                            color = Color.Yellow,
                            radius = 6.dp.toPx(),
                            center = Offset(SolarRelatedPos[0], SolarRelatedPos[1])
                        )
                    }
                }
        ) {
            IconMapper(
                ImageId = R.mipmap.solaricon,
                text = "${SOLAR} W",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .scale(scaleBy)
            )
            IconMapper(
                ImageId = R.mipmap.gridicon,
                text = "${WAPDA} W",
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .scale(scaleBy)
            )
            IconMapper(
                ImageId = R.mipmap.loadicon,
                text = "${SOLAR-WAPDA} W",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .scale(scaleBy)
            )
        }
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier
                    .fillMaxSize()
//                    .background(Color.)
                    .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = (if(WAPDA < 0) "From" else "To") + " Grid",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${WAPDA} W",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = FontWeight.Light
                    )
                }
            }
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier
                    .fillMaxSize()
//                    .background(Color.)
                    .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "From Solar",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${SOLAR} W",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

data class RandomValues(val GridValue: Number, val SolarValue: Number, val LoadValue:Number)

@Composable
fun DetailView(IP: String, SN: String, paddingValues: PaddingValues, modifier: Modifier = Modifier) {

    var WAPDA: Int by remember { mutableIntStateOf(500) }
    var SOLAR: Int by remember { mutableIntStateOf(500) }
    var context = LocalContext.current
    val t = responseViewModel(IP, SN)
    LaunchedEffect(Unit) {
        while (true)
        {
            try {
                t.getRealTimeData {
                    result ->
                    WAPDA = (bit32(result[29],result[30]).toInt() * 0.1).toInt()
                    SOLAR = result[9]
                }
            }
            catch (ex: Exception) {
                Toast.makeText(context, ex.message, Toast.LENGTH_SHORT).show()
                WAPDA = generateRandomNumber().GridValue.toInt()
                SOLAR = generateRandomNumber().SolarValue.toInt()
            }
            delay(1000)
        }
    }
    val defaultColor by remember {
        mutableStateOf(ListItemColors(
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified
    ))
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(0.2f))
            .padding(18.dp)
        ) {
            Text(
                modifier = Modifier,
                text = "Detailed Data View",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        HorizontalDivider()
        DetailEntity(
            titleText = (if(WAPDA < 0) "From" else "To") + " Grid",
            data = "${WAPDA} W",
            imageVector = ImageVector.vectorResource(R.drawable.electric_tower)
        )
        DetailEntity(
            titleText = "From Solar",
            data = "${SOLAR} W",
            imageVector = ImageVector.vectorResource(R.drawable.solar_panel)
        )
        DetailEntity(
            titleText = "Home Load",
            data = "${WAPDA+SOLAR} W",
            imageVector = ImageVector.vectorResource(R.drawable.electricity)
        )
    }
}

@Composable
fun DetailEntity(
    titleText: String = "Title Heading",
    data: String = "Data",
    imageVector: ImageVector,
    defaultColor: ListItemColors = ListItemColors(
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified,
        Color.Unspecified
    )
) {
    ListItem(
            leadingContent = {
                androidx.compose.material3.Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp)
                ) },
            headlineContent = { Text(text = titleText.uppercase(), fontWeight = FontWeight.Medium, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface) },
            supportingContent = { Text(text = data, fontWeight = FontWeight.Light, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface) },
            colors = defaultColor,
        )
        HorizontalDivider()
}