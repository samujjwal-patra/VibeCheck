package com.example.vibecheck

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import android.net.Uri
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.vibecheck.ui.theme.DarkCharcoal
import com.example.vibecheck.ui.theme.NeonCyan
import com.example.vibecheck.ui.theme.VibeCheckTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var currentTheme by remember { mutableStateOf("Cyan") }
            VibeCheckTheme(themeMode = currentTheme) {
                MainContent(onThemeChange = { currentTheme = it }, currentTheme = currentTheme)
            }
        }
    }
}

@Composable
fun MainContent(onThemeChange: (String) -> Unit, currentTheme: String) {
    var showSplash by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkCharcoal
    ) {
        if (showSplash) {
            SplashScreen(onAnimationFinished = { showSplash = false })
        } else {
            DashboardScreen(onThemeChange = onThemeChange, currentTheme = currentTheme)
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var phase by remember { mutableIntStateOf(1) }

    // Phase 1: Pulsing Icon/Radar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )

    // Glitch Effect States
    var glitchOffset by remember { mutableStateOf(0.dp) }
    var glitchAlpha by remember { mutableFloatStateOf(0f) }

    // Phase 2: Branding Reveal
    val brandingAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(800),
        label = "brandingAlpha"
    )
    val brandingScale by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "brandingScale"
    )

    // Scanning Beam
    val beamPosition by animateFloatAsState(
        targetValue = if (phase >= 2) 1.5f else -0.5f,
        animationSpec = tween(3000, easing = LinearOutSlowInEasing),
        label = "beamPosition"
    )

    // Background Particle States (Simulated)
    val particleAlpha by animateFloatAsState(
        targetValue = if (phase < 4) 0.4f else 0f,
        animationSpec = tween(1000),
        label = "particleAlpha"
    )

    LaunchedEffect(Unit) {
        delay(1200)
        phase = 2 // INITIALIZING
        
        repeat(5) {
            delay((50..150).random().toLong())
            glitchOffset = ((-8)..8).random().dp
            glitchAlpha = 0.6f
            delay(40)
            glitchOffset = 0.dp
            glitchAlpha = 0f
        }
        
        delay(1000)
        phase = 3 // READY
        delay(800)
        phase = 4 // TRANSITION
        delay(300)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f * particleAlpha)) {
            val step = 40.dp.toPx()
            for (i in 0..size.width.toInt() step step.toInt()) {
                drawLine(NeonCyan, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 1f)
            }
            for (i in 0..size.height.toInt() step step.toInt()) {
                drawLine(NeonCyan, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 1f)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(if(phase >= 4) 0.9f else 1f).alpha(if(phase >= 4) 0f else 1f)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale * 1.1f)
                        .alpha(pulseAlpha * 0.2f)
                        .background(NeonCyan, RoundedCornerShape(100.dp))
                        .blur(40.dp)
                )

                Canvas(modifier = Modifier.size(120.dp)) {
                    rotate(radarRotation) {
                        drawCircle(
                            color = NeonCyan,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                        )
                        drawArc(
                            brush = Brush.sweepGradient(
                                0f to Color.Transparent,
                                0.8f to NeonCyan.copy(alpha = 0.1f),
                                1f to NeonCyan
                            ),
                            startAngle = 0f,
                            sweepAngle = 90f,
                            useCenter = true
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .size(66.dp)
                        .scale(pulseScale),
                    shape = RoundedCornerShape(33.dp),
                    color = NeonCyan.copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(2.dp, NeonCyan)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = when(phase) {
                                1 -> "SCAN"
                                2 -> "INITIALIZING"
                                else -> "READY"
                            },
                            color = NeonCyan,
                            fontSize = if(phase == 2) 7.sp else 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .offset(x = glitchOffset)
                    .scale(brandingScale)
                    .alpha(brandingAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VIBECHECK",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 8.sp
                )

                if (glitchAlpha > 0f) {
                    Text(
                        text = "VIBECHECK",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonCyan.copy(alpha = glitchAlpha),
                        letterSpacing = 8.sp,
                        modifier = Modifier.offset(x = glitchOffset * 2)
                    )
                }

                BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                    val width = maxWidth
                    Box(
                        modifier = Modifier
                            .offset(x = width * beamPosition - (width/2))
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color.Transparent, NeonCyan, Color.Transparent)
                                )
                            )
                            .blur(4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "NEURAL AURA INTERFACE v2.4",
                color = NeonCyan.copy(alpha = brandingAlpha * 0.6f),
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onThemeChange: (String) -> Unit, currentTheme: String) {
    var visible by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAdminPasswordDialog by remember { mutableStateOf(false) }
    var showAdminWelcome by remember { mutableStateOf(false) }
    var showAdminScreen by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var isGenderExpanded by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showGenderAlert by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("VibeAdminPrefs", Context.MODE_PRIVATE) }
    
    BackHandler {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (showResult) {
            showResult = false
            name = ""
            gender = ""
        } else if (isScanning) {
            isScanning = false
        } else {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = DarkCharcoal,
            title = { Text("EXIT SYSTEM?", color = Color.White) },
            text = { Text("ARE YOU SURE YOU WANT TO TERMINATE THE NEURAL LINK?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { (context as? Activity)?.finish() }) {
                    Text("YES, TERMINATE", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("NO, STAY", color = Color.Gray)
                }
            }
        )
    }

    if (showGenderAlert) {
        AlertDialog(
            onDismissRequest = { showGenderAlert = false },
            containerColor = DarkCharcoal,
            title = { Text("NEURAL MISMATCH", color = Color.Red) },
            text = { Text("INCORRECT GENDER DATA DETECTED FOR SUBJECT. PLEASE VERIFY AND ENTER CORRECT GENDER TO PROCEED.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = { showGenderAlert = false }) {
                    Text("RETRY", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) isScanning = true
    }

    LaunchedEffect(Unit) { visible = true }





    if (showAdminPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showAdminPasswordDialog = false; adminPassword = "" },
            containerColor = DarkCharcoal,
            title = { Text("ENCRYPTED ACCESS", color = Color.White) },
            text = {
                Column {
                    Text("ENTER ADMIN DECRYPTION KEY", color = Color.Gray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = adminPassword,
                        onValueChange = { adminPassword = it },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    if (adminPassword == "VibeSetUp") {
                        showAdminPasswordDialog = false
                        showAdminWelcome = true
                        adminPassword = ""
                    }
                }) {
                    Text("DECRYPT", color = MaterialTheme.colorScheme.primary)
                }
            }
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = DarkCharcoal,
            title = { Text("SELECT SYSTEM AURA", color = Color.White) },
            text = {
                Column {
                    listOf("Cyan", "Crimson", "Golden").forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeChange(theme); showThemeDialog = false }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        when(theme) {
                                            "Crimson" -> com.example.vibecheck.ui.theme.CrimsonRed
                                            "Golden" -> com.example.vibecheck.ui.theme.GoldenYellow
                                            else -> com.example.vibecheck.ui.theme.NeonCyan
                                        },
                                        RoundedCornerShape(12.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(theme.uppercase(), color = Color.White, letterSpacing = 2.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("CLOSE", color = NeonCyan)
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isScanning && !showResult,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White.copy(alpha = 0.05f),
                drawerContentColor = Color.White,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .graphicsLayer { 
                        clip = true
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp) 
                    }
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.3f), 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), 
                                Color.White.copy(alpha = 0.05f)
                            )
                        ),
                        shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .background(Brush.horizontalGradient(listOf(DarkCharcoal, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))))
                        .padding(vertical = 48.dp, horizontal = 24.dp)
                ) {
                    Column {
                        Text("VIBECHECK", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 4.sp)
                        Text("NEURAL INTERFACE", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                    }
                }
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("SYSTEM SETTINGS", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, letterSpacing = 2.sp)
                NavigationDrawerItem(
                    label = { Text("Theme Selection", letterSpacing = 1.sp) },
                    selected = false,
                    onClick = { showThemeDialog = true; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedIconColor = MaterialTheme.colorScheme.primary, unselectedTextColor = Color.White),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("ADMIN Controls", letterSpacing = 1.sp) },
                    selected = false,
                    onClick = { showAdminPasswordDialog = true; scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent, unselectedIconColor = MaterialTheme.colorScheme.primary, unselectedTextColor = Color.White),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(horizontal = 16.dp))
                Text("VERSION 2.4.0", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp, color = Color.Gray, letterSpacing = 2.sp)
            }
        }
    ) {
        val drawerBlur by animateDpAsState(
            targetValue = if (drawerState.targetValue == DrawerValue.Open) 16.dp else 0.dp,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "drawer_blur"
        )
        
        Scaffold(
            modifier = Modifier.blur(drawerBlur),
            topBar = {
                if (!isScanning && !showResult && !showAdminScreen && !showAdminWelcome) {
                    CenterAlignedTopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            },
            containerColor = DarkCharcoal
        ) { padding ->
            val primaryColor = MaterialTheme.colorScheme.primary
            Box(modifier = Modifier.fillMaxSize()) {
                // Ambient Dashboard Background Grid
                Canvas(modifier = Modifier.fillMaxSize().alpha(0.08f)) {
                    val step = 40.dp.toPx()
                    for (i in 0..size.width.toInt() step step.toInt()) {
                        drawLine(Color.White, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 1f)
                    }
                    for (i in 0..size.height.toInt() step step.toInt()) {
                        drawLine(Color.White, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 1f)
                    }
                }
                
                // Subtle Pulsing Background Scanner
                val dashboardTransition = rememberInfiniteTransition(label = "dashboard_bg")
                val bgPulseAlpha by dashboardTransition.animateFloat(
                    initialValue = 0.02f,
                    targetValue = 0.12f,
                    animationSpec = infiniteRepeatable(tween(2500, easing = EaseInOutQuad), RepeatMode.Reverse),
                    label = "bg_pulse"
                )
                
                Canvas(modifier = Modifier.fillMaxSize().alpha(bgPulseAlpha)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, Color.Transparent),
                            center = center,
                            radius = size.maxDimension / 1.5f
                        )
                    )
                }
                
                // Occasional Dashboard Scan Line
                val scanLinePos by dashboardTransition.animateFloat(
                    initialValue = -0.1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing, delayMillis = 1000),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "dash_scan_line"
                )
                
                Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
                    val y = size.height * scanLinePos
                    drawLine(
                        brush = Brush.horizontalGradient(listOf(Color.Transparent, primaryColor, Color.Transparent)),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                AnimatedVisibility(
                    visible = visible && !isScanning && !showResult && !showAdminScreen && !showAdminWelcome,
                    enter = fadeIn(animationSpec = tween(800, easing = LinearOutSlowInEasing)) + 
                            slideInVertically(
                                initialOffsetY = { it / 4 }, 
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy, 
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Decorative Top metadata
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("STATUS: BIO-LINK READY", color = primaryColor, fontSize = 8.sp, letterSpacing = 1.sp)
                            Text("ENCRYPTION: ACTIVE", color = primaryColor, fontSize = 8.sp, letterSpacing = 1.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(modifier = Modifier.fillMaxWidth().alpha(0.2f), color = primaryColor)
                        Spacer(modifier = Modifier.height(32.dp))

                        // Header with Icon
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp).alpha(0.8f),
                            tint = primaryColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "NEURAL VIBE ANALYZER", 
                            color = Color.White, 
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 2.sp
                        )
                        Text(
                            "SENSING INTERFACE v2.4", 
                            color = primaryColor, 
                            fontSize = 10.sp, 
                            letterSpacing = 4.sp,
                            modifier = Modifier.alpha(0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(48.dp))

                        // Input Container with Corner Accents
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Top-left corner
                            Canvas(modifier = Modifier.size(20.dp).align(Alignment.TopStart)) {
                                drawLine(primaryColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 2.dp.toPx())
                                drawLine(primaryColor, Offset(0f, 0f), Offset(0f, size.height), strokeWidth = 2.dp.toPx())
                            }
                            // Bottom-right corner
                            Canvas(modifier = Modifier.size(20.dp).align(Alignment.BottomEnd)) {
                                drawLine(primaryColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 2.dp.toPx())
                                drawLine(primaryColor, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 2.dp.toPx())
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("SUBJECT NAME", color = Color.Gray, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor.copy(alpha = 0.5f)) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = Color.DarkGray,
                                        focusedTextColor = Color.White,
                                        cursorColor = primaryColor
                                    )
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                var isExpanded by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = gender.uppercase(),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("SUBJECT GENDER", color = Color.Gray, fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Wc, contentDescription = null, tint = primaryColor.copy(alpha = 0.5f)) },
                                        trailingIcon = { Icon(if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null, tint = primaryColor) },
                                        modifier = Modifier.fillMaxWidth().clickable { isExpanded = true },
                                        enabled = false, // Use disabled state to intercept clicks reliably
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledBorderColor = if(isExpanded) primaryColor else Color.DarkGray,
                                            disabledTextColor = Color.White,
                                            disabledLabelColor = Color.Gray,
                                            disabledLeadingIconColor = primaryColor.copy(alpha = 0.5f),
                                            disabledTrailingIconColor = primaryColor
                                        )
                                    )
                                    // Hidden clickable layer to handle taps
                                    Box(modifier = Modifier.matchParentSize().clickable { isExpanded = true })

                                    DropdownMenu(
                                        expanded = isExpanded,
                                        onDismissRequest = { isExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f).background(DarkCharcoal).border(1.dp, primaryColor.copy(alpha = 0.5f))
                                    ) {
                                        listOf("Male", "Female", "Custom").forEach { selection ->
                                            DropdownMenuItem(
                                                text = { Text(selection.uppercase(), color = Color.White, letterSpacing = 1.sp) },
                                                onClick = {
                                                    gender = selection
                                                    isExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // High-tech Action Button
                        Button(
                            onClick = { 
                                if (name.isNotBlank() && gender.isNotBlank()) {
                                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) isScanning = true
                                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp), ambientColor = primaryColor, spotColor = primaryColor),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = DarkCharcoal)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("INITIALIZE NEURAL SCAN", color = DarkCharcoal, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Terminal Style Warning Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Red.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                                .border(0.5.dp, Color.Red.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "SYSTEM ADVISORY: [DECRYPTION ACTIVE] - NEURAL DATA SUBJECT TO AGGRESSIVE ROASTING PROTOCOLS. SYSTEM ACCURACY: 99.9% FOR SASSY OUTPUTS. PROCEED AT OWN RISK.",
                                color = Color.Red.copy(alpha = 0.8f),
                                fontSize = 7.sp,
                                letterSpacing = 1.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                    }
                }
                if (isScanning) {
                    ScanningScreen(
                        name = name, 
                        onScanComplete = { 
                            isScanning = false
                            
                            // Perform Gender Check after scan
                            val inputNameLower = name.trim().lowercase()
                            val allMappings = sharedPrefs.all
                            var genderMatched = true
                            
                            for (entry in allMappings) {
                                val key = entry.key.lowercase()
                                if (inputNameLower.contains(key)) {
                                    val value = entry.value as? String ?: ""
                                    val parts = value.split("|")
                                    if (parts.size > 1) {
                                        val adminGender = parts[0]
                                        if (adminGender.lowercase() != gender.lowercase()) {
                                            genderMatched = false
                                        }
                                    }
                                    break
                                }
                            }

                            if (genderMatched) {
                                showResult = true
                            } else {
                                showGenderAlert = true
                            }
                        }
                    )
                }
                if (showResult) ResultScreen(name = name, onScanAgain = { showResult = false; name = ""; gender = "" })
            }
        }
    }

    if (showAdminWelcome) {
        AdminWelcomeScreen(onFinished = {
            showAdminWelcome = false
            showAdminScreen = true
        })
    }

    if (showAdminScreen) {
        AdminScreen(onDismiss = { showAdminScreen = false })
    }
}

@Composable
fun AdminWelcomeScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var typedName by remember { mutableStateOf("") }
    val fullName = "SAMUJJWAL"
    val primaryColor = MaterialTheme.colorScheme.primary
    
    LaunchedEffect(Unit) {
        visible = true
        delay(600)
        fullName.forEachIndexed { index, _ ->
            typedName = fullName.substring(0, index + 1)
            delay((80..150).random().toLong())
        }
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCharcoal)
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        // Technical grid background
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.15f)) {
            val step = 30.dp.toPx()
            for (i in 0..size.width.toInt() step step.toInt()) drawLine(primaryColor, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 0.5f)
            for (i in 0..size.height.toInt() step step.toInt()) drawLine(primaryColor, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 0.5f)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "welcome_glow")
            val iconAlpha by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
                label = "icon_alpha"
            )

            Icon(
                Icons.Default.VerifiedUser,
                contentDescription = null,
                modifier = Modifier.size(100.dp).alpha(iconAlpha),
                tint = primaryColor
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "IDENTITY CONFIRMED",
                color = primaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp,
                modifier = Modifier.alpha(0.7f)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "HELLO, ",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 2.sp
                )
                Text(
                    typedName,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                )
                // Blinking cursor
                val cursorAlpha by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
                    label = "cursor"
                )
                Box(
                    modifier = Modifier
                        .size(width = 12.dp, height = 32.dp)
                        .background(primaryColor.copy(alpha = cursorAlpha))
                        .offset(x = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Loading bar
            Box(
                modifier = Modifier
                    .width(240.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                val progress by animateFloatAsState(
                    targetValue = if (typedName.length == fullName.length) 1f else 0f,
                    animationSpec = tween(1200, easing = LinearOutSlowInEasing),
                    label = "progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(primaryColor.copy(alpha = 0.2f), primaryColor)
                            )
                        )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "BYPASSING SECURITY PROTOCOLS...",
                color = primaryColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("VibeAdminPrefs", Context.MODE_PRIVATE) }
    var inputName by remember { mutableStateOf("") }
    var inputTitle by remember { mutableStateOf("") }
    var inputGender by remember { mutableStateOf("Male") }
    var isGenderExpanded by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var editingInfo by remember { mutableStateOf<Triple<String, Int, String>?>(null) } // Name, Index, Original Data
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Load existing mappings as individual pairs
    val mappings = remember { 
        mutableStateListOf<Triple<String, String, Int>>().apply {
            val all = sharedPrefs.all
            all.forEach { (key, value) ->
                if (value is String) {
                    val dataParts = value.split("|")
                    val pairsStr = if (dataParts.size > 1) dataParts[1] else dataParts[0]
                    pairsStr.split("[NEXT]").forEachIndexed { index, pairData ->
                        add(Triple(key, pairData, index))
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCharcoal,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(0.85f)
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NEURAL CONTROL PANEL", 
                        color = Color.White, 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "REPROGRAMMING INTERFACE v2.4", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontSize = 8.sp, 
                        letterSpacing = 2.sp,
                        modifier = Modifier.alpha(0.6f)
                    )
                }
                Icon(
                    Icons.Default.SettingsInputComponent, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        text = {
            Box(modifier = Modifier.fillMaxSize()) {
                // Subtle Grid Background for Admin
                Canvas(modifier = Modifier.fillMaxSize().alpha(0.03f)) {
                    val step = 20.dp.toPx()
                    for (i in 0..size.width.toInt() step step.toInt()) drawLine(Color.White, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 1f)
                    for (i in 0..size.height.toInt() step step.toInt()) drawLine(Color.White, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 1f)
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Input Section Container
                    Surface(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = inputName,
                                onValueChange = { inputName = it },
                                label = { Text("TARGET SUBJECT NAME", color = Color.Gray, fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = editingInfo == null,
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary, 
                                    unfocusedBorderColor = Color.DarkGray, 
                                    focusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                // Target Gender Selection (Pro style)
                                var isExp by remember { mutableStateOf(false) }
                                Box(modifier = Modifier.weight(1f)) {
                                    OutlinedTextField(
                                        value = inputGender.uppercase(),
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("GENDER", color = Color.Gray, fontSize = 10.sp) },
                                        trailingIcon = { Icon(if (isExp) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = false,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            disabledBorderColor = Color.DarkGray,
                                            disabledTextColor = Color.White,
                                            disabledLabelColor = Color.Gray,
                                            disabledLeadingIconColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Box(modifier = Modifier.matchParentSize().clickable { isExp = true })
                                    
                                    DropdownMenu(
                                        expanded = isExp,
                                        onDismissRequest = { isExp = false },
                                        modifier = Modifier.background(DarkCharcoal).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    ) {
                                        listOf("Male", "Female", "Custom").forEach { selection ->
                                            DropdownMenuItem(
                                                text = { Text(selection.uppercase(), color = Color.White, fontSize = 12.sp) },
                                                onClick = { inputGender = selection; isExp = false }
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))

                                // Image Selection (Pro thumbnail button)
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.DarkGray.copy(alpha = 0.5f))
                                        .clickable { imagePickerLauncher.launch("image/*") }
                                        .border(
                                            width = 1.dp, 
                                            color = if(selectedImageUri != null) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = inputTitle,
                                onValueChange = { inputTitle = it },
                                label = { Text("FORCED NEURAL CLASSIFICATION", color = Color.Gray, fontSize = 10.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Main Action Button with Shadow
                    Button(
                        onClick = {
                            if (inputName.isNotBlank() && inputTitle.isNotBlank()) {
                                val nameKey = inputName.trim().lowercase()
                                var currentPairData = inputTitle.trim()
                                
                                // Handle Image Storage
                                if (selectedImageUri != null) {
                                    if (selectedImageUri.toString().startsWith("content://")) {
                                        try {
                                            val fileName = "prank_${System.currentTimeMillis()}.jpg"
                                            val file = File(context.filesDir, fileName)
                                            context.contentResolver.openInputStream(selectedImageUri!!).use { input ->
                                                FileOutputStream(file).use { output ->
                                                    input?.copyTo(output)
                                                }
                                            }
                                            currentPairData = "${inputTitle.trim()}[URI]${file.absolutePath}"
                                            
                                            if (editingInfo != null) {
                                                editingInfo!!.third.split("[URI]").let {
                                                    if (it.size > 1) try { File(it[1]).delete() } catch (e: Exception) {}
                                                }
                                            }
                                        } catch (e: Exception) { e.printStackTrace() }
                                    } else {
                                        val uriStr = selectedImageUri.toString()
                                        val path = if (uriStr.startsWith("file://")) uriStr.substring(7) else uriStr
                                        currentPairData = "${inputTitle.trim()}[URI]$path"
                                    }
                                } else if (editingInfo != null) {
                                    editingInfo!!.third.split("[URI]").let {
                                        if (it.size > 1) try { File(it[1]).delete() } catch (e: Exception) {}
                                    }
                                }
                                
                                val existingVal = sharedPrefs.getString(nameKey, "") ?: ""
                                val dataParts = existingVal.split("|")
                                val existingPairs = if (dataParts.size > 1) {
                                    dataParts[1].split("[NEXT]").toMutableList()
                                } else if (dataParts[0].isNotEmpty()) {
                                    mutableListOf(dataParts[0])
                                } else {
                                    mutableListOf()
                                }
                                
                                if (editingInfo != null) {
                                    existingPairs[editingInfo!!.second] = currentPairData
                                } else {
                                    existingPairs.add(currentPairData)
                                }
                                
                                val finalVal = "${inputGender}|${existingPairs.joinToString("[NEXT]")}"
                                sharedPrefs.edit().putString(nameKey, finalVal).apply()
                                
                                mappings.clear()
                                sharedPrefs.all.forEach { (key, value) ->
                                    if (value is String) {
                                        val pts = value.split("|")
                                        val pairsStr = if (pts.size > 1) pts[1] else pts[0]
                                        pairsStr.split("[NEXT]").forEachIndexed { index, data ->
                                            mappings.add(Triple(key, data, index))
                                        }
                                    }
                                }
                                
                                inputName = ""; inputTitle = ""; selectedImageUri = null; editingInfo = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp), ambientColor = MaterialTheme.colorScheme.primary, spotColor = MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(if(editingInfo == null) Icons.Default.AddLink else Icons.Default.SyncAlt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if(editingInfo == null) "INJECT NEURAL PAIR" else "COMMIT REPROGRAMMING", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    }
                    
                    if (editingInfo != null) {
                        TextButton(
                            onClick = { inputName = ""; inputTitle = ""; selectedImageUri = null; editingInfo = null },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("ABORT EDITING", color = Color.Red.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                        Text(" ACTIVE REPOSITORY ", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(mappings) { triple ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingInfo = Triple(triple.first, triple.third, triple.second)
                                        inputName = triple.first
                                        val fullVal = sharedPrefs.getString(triple.first, "") ?: ""
                                        val pts = fullVal.split("|")
                                        if (pts.size > 1) inputGender = pts[0]
                                        val parts = triple.second.split("[URI]")
                                        inputTitle = parts[0]
                                        selectedImageUri = if(parts.size > 1) Uri.fromFile(File(parts[1])) else null
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (editingInfo?.first == triple.first && editingInfo?.second == triple.third) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                                    else Color.White.copy(alpha = 0.04f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp, 
                                    if (editingInfo?.first == triple.first && editingInfo?.second == triple.third)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                    else Color.White.copy(alpha = 0.05f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        val parts = triple.second.split("[URI]")
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.Black.copy(alpha = 0.3f))
                                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                        ) {
                                            if (parts.size > 1) {
                                                AsyncImage(
                                                    model = File(parts[1]), 
                                                    contentDescription = null, 
                                                    modifier = Modifier.fillMaxSize(), 
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Memory, 
                                                    contentDescription = null, 
                                                    modifier = Modifier.padding(12.dp).alpha(0.3f), 
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(16.dp))
                                        
                                        Column {
                                            Text(
                                                triple.first.uppercase(), 
                                                color = Color.White, 
                                                fontWeight = FontWeight.ExtraBold, 
                                                fontSize = 14.sp,
                                                letterSpacing = 0.5.sp
                                            )
                                            val fullVal = sharedPrefs.getString(triple.first, "") ?: ""
                                            val pts = fullVal.split("|")
                                            val displayGender = if (pts.size > 1) pts[0] else "N/A"
                                            Text(
                                                "${displayGender.uppercase()} • ${parts[0]}", 
                                                color = Color.Gray, 
                                                fontSize = 11.sp, 
                                                maxLines = 1,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            val nameKey = triple.first
                                            val indexToDelete = triple.third
                                            val existing = sharedPrefs.getString(nameKey, "") ?: ""
                                            val dataParts = existing.split("|")
                                            val genderPrefix = if (dataParts.size > 1) dataParts[0] else "Male"
                                            val pairsStr = if (dataParts.size > 1) dataParts[1] else dataParts[0]
                                            val pairs = pairsStr.split("[NEXT]").toMutableList()
                                            
                                            if (indexToDelete < pairs.size) {
                                                pairs[indexToDelete].split("[URI]").let {
                                                    if (it.size > 1) try { File(it[1]).delete() } catch (e: Exception) {}
                                                }
                                                pairs.removeAt(indexToDelete)
                                            }
                                            
                                            if (pairs.isEmpty()) sharedPrefs.edit().remove(nameKey).apply()
                                            else sharedPrefs.edit().putString(nameKey, "$genderPrefix|${pairs.joinToString("[NEXT]")}").apply()
                                            
                                            mappings.clear()
                                            sharedPrefs.all.forEach { (key, value) ->
                                                if (value is String) {
                                                    val pts2 = value.split("|")
                                                    val pStr = if (pts2.size > 1) pts2[1] else pts2[0]
                                                    pStr.split("[NEXT]").forEachIndexed { idx, data ->
                                                        mappings.add(Triple(key, data, idx))
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline, 
                                            contentDescription = "Delete", 
                                            tint = Color.Red.copy(alpha = 0.5f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.padding(bottom = 12.dp, end = 16.dp)
            ) { 
                Text("DISCONNECT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 12.sp) 
            }
        }
    )
}

@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = androidx.camera.core.Preview.Builder().build().also { it.surfaceProvider = previewView.surfaceProvider }
                val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                } catch (e: Exception) { e.printStackTrace() }
            }, executor)
            previewView
        },
        modifier = modifier
    )
}

@Composable
fun ScanningScreen(name: String, onScanComplete: () -> Unit) {
    var scanProgress by remember { mutableFloatStateOf(0f) }
    var currentInstruction by remember { mutableStateOf("CENTER FACE IN FRAME") }
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val primaryColor = MaterialTheme.colorScheme.primary
    val animatedProgress by animateFloatAsState(
        targetValue = scanProgress, 
        animationSpec = tween(1000, easing = LinearOutSlowInEasing), 
        label = "progress"
    )
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f, 
        targetValue = 1f, 
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Reverse), 
        label = "scanLine"
    )
    val frameAlpha by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "frameAlpha")

    LaunchedEffect(Unit) {
        val instructions = listOf("CENTER FACE IN FRAME", "MOVE SLIGHTLY TO THE LEFT", "TILT HEAD UPWARD", "BLINK TWICE", "HOLD STILL...", "SMILE FOR CALIBRATION", "LOOK DIRECTLY AT THE SENSORS")
        launch { 
            while (scanProgress < 1f) { 
                delay((1500..3000).random().toLong())
                currentInstruction = instructions.random() 
            } 
        }
        val steps = listOf(0.15f, 0.32f, 0.48f, 0.65f, 0.78f, 0.92f, 1.0f)
        for (target in steps) {
            val start = scanProgress
            val distance = target - start
            val iterations = (8..15).random()
            repeat(iterations) { 
                delay((30..60).random().toLong())
                scanProgress += distance / iterations 
            }
            if (target < 1f) delay((150..400).random().toLong())
        }
        delay(1000)
        onScanComplete()
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkCharcoal.copy(alpha = 0.95f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
            val step = 30.dp.toPx()
            for (i in 0..size.width.toInt() step step.toInt()) drawLine(Color.White, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 1f)
            for (i in 0..size.height.toInt() step step.toInt()) drawLine(Color.White, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 1f)
        }
        Box(modifier = Modifier.size(320.dp).graphicsLayer { alpha = frameAlpha }) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cornerSize = 40.dp.toPx(); val stroke = 4.dp.toPx()
                drawLine(primaryColor, Offset(0f, 0f), Offset(cornerSize, 0f), strokeWidth = stroke)
                drawLine(primaryColor, Offset(0f, 0f), Offset(0f, cornerSize), strokeWidth = stroke)
                drawLine(primaryColor, Offset(size.width, 0f), Offset(size.width - cornerSize, 0f), strokeWidth = stroke)
                drawLine(primaryColor, Offset(size.width, 0f), Offset(size.width, cornerSize), strokeWidth = stroke)
                drawLine(primaryColor, Offset(0f, size.height), Offset(cornerSize, size.height), strokeWidth = stroke)
                drawLine(primaryColor, Offset(0f, size.height), Offset(0f, size.height - cornerSize), strokeWidth = stroke)
                drawLine(primaryColor, Offset(size.width, size.height), Offset(size.width - cornerSize, size.height), strokeWidth = stroke)
                drawLine(primaryColor, Offset(size.width, size.height), Offset(size.width, size.height - cornerSize), strokeWidth = stroke)
            }
            Box(modifier = Modifier.fillMaxSize().padding(8.dp).clip(RoundedCornerShape(8.dp)).background(primaryColor.copy(alpha = 0.05f))) {
                CameraPreview(modifier = Modifier.fillMaxSize())
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = size.height * scanLineY
                    drawLine(Brush.horizontalGradient(listOf(Color.Transparent, primaryColor, Color.Transparent)), Offset(0f, y), Offset(size.width, y), strokeWidth = 6.dp.toPx())
                    drawRect(Brush.verticalGradient(listOf(Color.Transparent, primaryColor.copy(alpha = 0.2f), Color.Transparent), startY = y - 20.dp.toPx(), endY = y + 20.dp.toPx()), topLeft = Offset(0f, y - 20.dp.toPx()), size = androidx.compose.ui.geometry.Size(size.width, 40.dp.toPx()))
                }
            }
        }
        Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NEURAL UPLINK ACTIVE", color = primaryColor, fontSize = 12.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(8.dp))
            Text("ANALYZING: ${name.uppercase()}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(80.dp), color = primaryColor, strokeWidth = 4.dp, trackColor = primaryColor.copy(alpha = 0.1f) )
                Text("${(animatedProgress * 100).toInt()}%", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("[ $currentInstruction ]", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(bottom = 12.dp).alpha(0.8f))
            val statusText = when { animatedProgress < 0.3f -> "CALIBRATING AURA SENSORS..."; animatedProgress < 0.6f -> "MAPPING BIOMETRIC VIBES..."; animatedProgress < 0.9f -> "DECODING NEURAL SIGNATURES..."; else -> "FINALIZING VIBE REPORT..." }
            Text(statusText, color = primaryColor.copy(alpha = 0.7f), fontSize = 10.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun ResultScreen(name: String, onScanAgain: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("VibeAdminPrefs", Context.MODE_PRIVATE) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val results = listOf("PURE CHAOTIC ENERGY", "CHILL MASTER 3000", "NEURAL RED FLAG DETECTED", "PERFECT VIBE SYNCHRONY", "GLITCH IN THE MATRIX", "MAIN CHARACTER ENERGY", "EMOTIONAL ROLLERCOASTER", "100% CERTIFIED BADDIE")
    
    val storedData = remember {
        val inputName = name.trim().lowercase()
        val allMappings = sharedPrefs.all
        var foundData: String? = null
        
        // Check if any mapped key is part of the input name
        for (entry in allMappings) {
            val key = entry.key.lowercase()
            if (inputName.contains(key)) {
                foundData = entry.value as? String
                break // Stop at first match
            }
        }
        foundData
    }
    
    val (resultTitle, customImageUri) = remember {
        if (storedData != null) {
            val dataParts = storedData.split("|")
            val pairsStr = if (dataParts.size > 1) dataParts[1] else dataParts[0]
            val allPairs = pairsStr.split("[NEXT]")
            val selectedPair = allPairs.random()
            val parts = selectedPair.split("[URI]")
            if (parts.size > 1) {
                parts[0] to parts[1]
            } else {
                parts[0] to null
            }
        } else {
            results.random() to null
        }
    }
    
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); showContent = true }

    Box(modifier = Modifier.fillMaxSize().background(DarkCharcoal), contentAlignment = Alignment.Center) {
        // High-tech background elements
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) {
            val step = 40.dp.toPx()
            for (i in 0..size.width.toInt() step step.toInt()) drawLine(Color.White, Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 1f)
            for (i in 0..size.height.toInt() step step.toInt()) drawLine(Color.White, Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 1f)
            drawCircle(Brush.radialGradient(listOf(primaryColor, Color.Transparent), center = center, radius = size.maxDimension / 1.5f))
        }

        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(tween(1200, easing = EaseInOutQuart)) + 
                    slideInVertically(
                        initialOffsetY = { 80 }, 
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top Metadata
                Row(modifier = Modifier.fillMaxWidth().alpha(0.6f), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("REPORT_ID: #${(1000..9999).random()}", color = primaryColor, fontSize = 8.sp, letterSpacing = 1.sp)
                    Text("DATA_STABILITY: 100%", color = primaryColor, fontSize = 8.sp, letterSpacing = 1.sp)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name Card
                Box(contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawRect(primaryColor.copy(alpha = 0.05f))
                        drawLine(primaryColor, Offset(0f, 0f), Offset(20.dp.toPx(), 0f), strokeWidth = 2.dp.toPx())
                        drawLine(primaryColor, Offset(0f, 0f), Offset(0f, 20.dp.toPx()), strokeWidth = 2.dp.toPx())
                        drawLine(primaryColor, Offset(size.width, size.height), Offset(size.width - 20.dp.toPx(), size.height), strokeWidth = 2.dp.toPx())
                        drawLine(primaryColor, Offset(size.width, size.height), Offset(size.width, size.height - 20.dp.toPx()), strokeWidth = 2.dp.toPx())
                    }
                    Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 48.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOUR NAME", color = primaryColor, fontSize = 10.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Light)
                        Text(name.uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                // Main Analysis Result Container
                Box(contentAlignment = Alignment.Center) {
                    // Outer decorative rings
                    val infiniteTransition = rememberInfiniteTransition(label = "result_glow")
                    val ringAlpha by infiniteTransition.animateFloat(0.1f, 0.3f, infiniteRepeatable(tween(2000), RepeatMode.Reverse), label = "ring_alpha")
                    
                    Canvas(modifier = Modifier.size(260.dp)) {
                        drawCircle(primaryColor, radius = size.minDimension/2, style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f))), alpha = ringAlpha)
                        drawCircle(primaryColor, radius = size.minDimension/2.2f, style = Stroke(width = 0.5.dp.toPx()), alpha = ringAlpha * 0.5f)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NEURAL PROFILE MATCH",
                            color = primaryColor.copy(alpha = 0.8f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        // The Image / Icon
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(primaryColor.copy(alpha = 0.1f))
                                .border(2.dp, Brush.sweepGradient(listOf(primaryColor, Color.Transparent, primaryColor)), RoundedCornerShape(100.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (customImageUri != null) {
                                AsyncImage(
                                    model = customImageUri,
                                    contentDescription = "Matching Face",
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(100.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(100.dp).alpha(0.6f), tint = primaryColor)
                            }
                            
                            // Sweeping scan overlay
                            val scanPos by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "result_scan")
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val y = size.height * scanPos
                                drawLine(primaryColor.copy(alpha = 0.4f), Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Title Section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("NEURAL VIBE CLASSIFICATION:", color = Color.Gray, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = resultTitle,
                        color = primaryColor,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 34.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Action Button
                Button(
                    onClick = onScanAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .shadow(12.dp, RoundedCornerShape(12.dp), ambientColor = primaryColor, spotColor = primaryColor),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = DarkCharcoal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("INITIALIZE NEW SCAN", color = DarkCharcoal, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text("VERDICT FINALIZED BY AURA_CORE v2.4", color = primaryColor.copy(alpha = 0.4f), fontSize = 7.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VibeCheckTheme {
        MainContent(onThemeChange = {}, currentTheme = "Cyan")
    }
}
