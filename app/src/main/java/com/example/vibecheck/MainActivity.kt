package com.example.vibecheck

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.core.content.ContextCompat
import com.example.vibecheck.ui.theme.DarkCharcoal
import com.example.vibecheck.ui.theme.NeonCyan
import com.example.vibecheck.ui.theme.VibeCheckTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
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
            delay((100..300).random().toLong())
            glitchOffset = ((-5)..5).random().dp
            glitchAlpha = 0.5f
            delay(50)
            glitchOffset = 0.dp
            glitchAlpha = 0f
        }
        
        delay(1000)
        phase = 3 // READY
        delay(1000)
        phase = 4 // TRANSITION
        delay(600)
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
                text = "NEURAL AURA INTERFACE v1.0",
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
    var showAdminScreen by remember { mutableStateOf(false) }
    var adminPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var isGenderExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) isScanning = true
    }

    LaunchedEffect(Unit) { visible = true }

    if (showAdminScreen) {
        AdminScreen(onDismiss = { showAdminScreen = false })
    }

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
                        showAdminScreen = true
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
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkCharcoal.copy(alpha = 0.7f),
                drawerContentColor = Color.White,
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .graphicsLayer { clip = true; shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp) }
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), Color.White.copy(alpha = 0.05f))),
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
                Text("VERSION 1.0.0", modifier = Modifier.fillMaxWidth().padding(16.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 12.sp, color = Color.Gray, letterSpacing = 2.sp)
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = DarkCharcoal
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = visible && !isScanning && !showResult,
                    enter = fadeIn(animationSpec = tween(1000)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("NEURAL VIBE ANALYZER", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, letterSpacing = 2.sp, modifier = Modifier.alpha(0.7f))
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("ENTER SUBJECT NAME", color = Color.Gray) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(expanded = isGenderExpanded, onExpandedChange = { isGenderExpanded = !isGenderExpanded }, modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = gender.uppercase(), onValueChange = {}, readOnly = true,
                                label = { Text("SELECT SUBJECT GENDER", color = Color.Gray) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderExpanded) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                            )
                            ExposedDropdownMenu(expanded = isGenderExpanded, onDismissRequest = { isGenderExpanded = false }, modifier = Modifier.background(DarkCharcoal).border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))) {
                                listOf("Male", "Female", "Custom").forEach { selection ->
                                    DropdownMenuItem(text = { Text(selection.uppercase(), color = Color.White, letterSpacing = 1.sp) }, onClick = { gender = selection; isGenderExpanded = false }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { 
                                if (name.isNotBlank() && gender.isNotBlank()) {
                                    val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                    if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) isScanning = true
                                    else permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("INITIALIZE SCAN", color = DarkCharcoal, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        }
                    }
                }
                if (isScanning) ScanningScreen(name = name, onScanComplete = { isScanning = false; showResult = true })
                if (showResult) ResultScreen(name = name, onScanAgain = { showResult = false; name = ""; gender = "" })
            }
        }
    }
}

@Composable
fun AdminScreen(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("VibeAdminPrefs", Context.MODE_PRIVATE) }
    var inputName by remember { mutableStateOf("") }
    var inputTitle by remember { mutableStateOf("") }
    
    // Load existing mappings
    val mappings = remember { 
        mutableStateListOf<Pair<String, String>>().apply {
            val all = sharedPrefs.all
            all.forEach { (key, value) ->
                if (value is String) add(key to value)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCharcoal,
        title = { Text("NEURAL CONTROL PANEL", color = Color.White, fontWeight = FontWeight.Black) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("TARGET NAME", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputTitle,
                    onValueChange = { inputTitle = it },
                    label = { Text("FORCED RESULT TITLE", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (inputName.isNotBlank() && inputTitle.isNotBlank()) {
                            sharedPrefs.edit().putString(inputName.trim().lowercase(), inputTitle.trim()).apply()
                            if (mappings.any { it.first == inputName.trim().lowercase() }) {
                                val idx = mappings.indexOfFirst { it.first == inputName.trim().lowercase() }
                                mappings[idx] = inputName.trim().lowercase() to inputTitle.trim()
                            } else {
                                mappings.add(inputName.trim().lowercase() to inputTitle.trim())
                            }
                            inputName = ""; inputTitle = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("INJECT MAPPING", color = DarkCharcoal, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                Text("ACTIVE MAPPINGS:", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, letterSpacing = 2.sp)
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(mappings) { mapping ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(mapping.first.uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(mapping.second, color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                sharedPrefs.edit().remove(mapping.first).apply()
                                mappings.remove(mapping)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("CLOSE", color = MaterialTheme.colorScheme.primary) }
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
    val animatedProgress by animateFloatAsState(targetValue = scanProgress, animationSpec = tween(600, easing = LinearOutSlowInEasing), label = "progress")
    val scanLineY by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse), label = "scanLine")
    val frameAlpha by infiniteTransition.animateFloat(0.4f, 1f, infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "frameAlpha")

    LaunchedEffect(Unit) {
        val instructions = listOf("CENTER FACE IN FRAME", "MOVE SLIGHTLY TO THE LEFT", "TILT HEAD UPWARD", "BLINK TWICE", "HOLD STILL...", "SMILE FOR CALIBRATION", "LOOK DIRECTLY AT THE SENSORS")
        launch { while (scanProgress < 1f) { delay(2000); currentInstruction = instructions.random() } }
        val steps = listOf(0.1f, 0.25f, 0.4f, 0.55f, 0.7f, 0.85f, 1.0f)
        for (target in steps) {
            val start = scanProgress
            val distance = target - start
            repeat(10) { delay((40..80).random().toLong()); scanProgress += distance / 10 }
            if (target < 1f) delay((200..400).random().toLong())
        }
        delay(800)
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
                CircularProgressIndicator(progress = { animatedProgress }, modifier = Modifier.size(80.dp), color = primaryColor, strokeWidth = 4.dp, trackColor = primaryColor.copy(alpha = 0.1f))
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
    
    val resultTitle = remember { 
        sharedPrefs.getString(name.trim().lowercase(), null) ?: results.random()
    }
    
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(300); showContent = true }

    Box(modifier = Modifier.fillMaxSize().background(DarkCharcoal), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f)) { drawCircle(Brush.radialGradient(listOf(primaryColor, Color.Transparent), center = center, radius = size.maxDimension / 2)) }
        AnimatedVisibility(visible = showContent, enter = fadeIn(tween(1000)) + slideInVertically(initialOffsetY = { 50 })) {
            Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text("SUBJECT IDENTIFIED", color = primaryColor, fontSize = 12.sp, letterSpacing = 4.sp)
                Text(name.uppercase(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(40.dp))
                Box(modifier = Modifier.size(200.dp).clip(RoundedCornerShape(100.dp)).background(primaryColor.copy(alpha = 0.1f)).border(2.dp, primaryColor, RoundedCornerShape(100.dp)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(100.dp).alpha(0.5f), tint = primaryColor)
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().alpha(0.3f), color = primaryColor, thickness = 1.dp)
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text("VIBE ANALYSIS COMPLETE:", color = Color.Gray, fontSize = 10.sp, letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(resultTitle, color = primaryColor, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 32.sp)
                Spacer(modifier = Modifier.height(64.dp))
                Button(onClick = onScanAgain, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor), shape = RoundedCornerShape(8.dp)) {
                    Text("INITIALIZE NEW SCAN", color = DarkCharcoal, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
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
