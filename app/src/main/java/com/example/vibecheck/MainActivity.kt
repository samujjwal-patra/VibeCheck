package com.example.vibecheck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        // Phase 1: Ambient (SCAN)
        delay(1200)
        phase = 2 // INITIALIZING
        
        // Random Glitch Effects during reveal
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
        // Background Grid/Data Pattern
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.1f * particleAlpha)) {
            val step = 40.dp.toPx()
            for (i in 0..size.width.toInt() step step.toInt()) {
                drawLine(NeonCyan, androidx.compose.ui.geometry.Offset(i.toFloat(), 0f), androidx.compose.ui.geometry.Offset(i.toFloat(), size.height), strokeWidth = 1f)
            }
            for (i in 0..size.height.toInt() step step.toInt()) {
                drawLine(NeonCyan, androidx.compose.ui.geometry.Offset(0f, i.toFloat()), androidx.compose.ui.geometry.Offset(size.width, i.toFloat()), strokeWidth = 1f)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.scale(if(phase >= 4) 0.9f else 1f).alpha(if(phase >= 4) 0f else 1f)
        ) {
            // THE CORE: Multi-layered Radar
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                // Outer Glow Ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseScale * 1.1f)
                        .alpha(pulseAlpha * 0.2f)
                        .background(NeonCyan, RoundedCornerShape(100.dp))
                        .blur(40.dp)
                )

                // Rotating Scanning Interface
                Canvas(modifier = Modifier.size(120.dp)) {
                    rotate(radarRotation) {
                        drawCircle(
                            color = NeonCyan,
                            radius = size.minDimension / 2,
                            style = Stroke(width = 1.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                        )
                        
                        // Scanning Sweep
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

                // Inner Core
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

            // BRANDING with Glitch & Scan
            Box(
                modifier = Modifier
                    .offset(x = glitchOffset)
                    .scale(brandingScale)
                    .alpha(brandingAlpha),
                contentAlignment = Alignment.Center
            ) {
                // Main Text
                Text(
                    text = "VIBECHECK",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 8.sp
                )

                // Cyan Ghosting (Glitch)
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

                // Scanning Beam overlay
                BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                    val width = maxWidth
                    
                    // Vertical Laser
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
            
            // Subtitle
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
    
    LaunchedEffect(Unit) {
        visible = true
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
                                .clickable { 
                                    onThemeChange(theme)
                                    showThemeDialog = false
                                }
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
                drawerContainerColor = DarkCharcoal,
                drawerContentColor = Color.White,
                modifier = Modifier.width(300.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(DarkCharcoal, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            )
                        )
                        .padding(vertical = 48.dp, horizontal = 24.dp)
                ) {
                    Column {
                        Text(
                            "VIBECHECK",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 4.sp
                        )
                        Text(
                            "NEURAL INTERFACE",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                    }
                }

                HorizontalDivider(color = Color.DarkGray)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "SYSTEM SETTINGS",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                NavigationDrawerItem(
                    label = { Text("Theme Selection", letterSpacing = 1.sp) },
                    selected = false,
                    onClick = { 
                        showThemeDialog = true
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text("ADMIN Controls", letterSpacing = 1.sp) },
                    selected = false,
                    onClick = { /* TODO */ },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedTextColor = Color.White
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(horizontal = 16.dp))
                
                Text(
                    "VERSION 1.0.0",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = DarkCharcoal
        ) { padding ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(animationSpec = tween(1000)) + 
                        slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "NEURAL VIBE ANALYZER",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        letterSpacing = 2.sp,
                        modifier = Modifier.alpha(0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("ENTER SUBJECT NAME", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "INITIALIZE SCAN",
                            color = DarkCharcoal,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
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
