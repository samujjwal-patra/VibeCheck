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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vibecheck.ui.theme.DarkCharcoal
import com.example.vibecheck.ui.theme.NeonCyan
import com.example.vibecheck.ui.theme.VibeCheckTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VibeCheckTheme {
                MainContent()
            }
        }
    }
}

@Composable
fun MainContent() {
    var showSplash by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkCharcoal
    ) {
        if (showSplash) {
            SplashScreen(onAnimationFinished = { showSplash = false })
        } else {
            DashboardScreen()
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
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRotation"
    )

    // Phase 2: Branding Reveal
    val brandingAlpha by animateFloatAsState(
        targetValue = if (phase >= 2) 1f else 0f,
        animationSpec = tween(1000),
        label = "brandingAlpha"
    )
    val brandingOffset by animateDpAsState(
        targetValue = if (phase >= 2) 0.dp else 20.dp,
        animationSpec = tween(1000),
        label = "brandingOffset"
    )

    // Laser Sweep Animation
    val laserPosition by animateFloatAsState(
        targetValue = if (phase >= 2) 1.5f else -0.5f,
        animationSpec = tween(1500, easing = LinearEasing),
        label = "laserPosition"
    )

    // Phase 3 Transition
    val splashScale by animateFloatAsState(
        targetValue = if (phase >= 3) 0.8f else 1f,
        animationSpec = tween(500),
        label = "splashScale"
    )
    val splashAlpha by animateFloatAsState(
        targetValue = if (phase >= 3) 0f else 1f,
        animationSpec = tween(500),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        delay(1000) // Phase 1 duration
        phase = 2
        delay(1000) // Phase 2 reveal duration
        phase = 3
        delay(500) // Phase 3 transition duration
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(splashAlpha)
            .scale(splashScale),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Phase 1: Pulsing Radar Graphic
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(120.dp)) {
                // Glow effect
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(pulseScale)
                        .alpha(pulseAlpha * 0.3f)
                        .background(NeonCyan, RoundedCornerShape(50.dp))
                        .blur(30.dp)
                )
                
                Canvas(modifier = Modifier.size(80.dp).scale(pulseScale).alpha(pulseAlpha)) {
                    // Draw Radar Circles
                    drawCircle(
                        color = NeonCyan,
                        radius = size.minDimension / 2,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = NeonCyan,
                        radius = size.minDimension / 4,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    
                    // Draw scanning line
                    val sweepAngle = 60f
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.5f to NeonCyan.copy(alpha = 0.5f),
                            1f to NeonCyan
                        ),
                        startAngle = radarRotation,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        alpha = 0.6f
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Phase 2: Branding
            Box(
                modifier = Modifier
                    .offset(y = brandingOffset)
                    .alpha(brandingAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VibeCheck",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                // Laser line sweep
                if (phase >= 2) {
                    BoxWithConstraints(modifier = Modifier.matchParentSize()) {
                        val width = maxWidth
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(width)
                                .clip(RoundedCornerShape(4.dp))
                        ) {
                           Box(
                               modifier = Modifier
                                   .offset(x = width * (laserPosition - 0.5f))
                                   .width(2.dp)
                                   .fillMaxHeight()
                                   .background(
                                       brush = Brush.verticalGradient(
                                           colors = listOf(
                                               Color.Transparent,
                                               NeonCyan,
                                               Color.Transparent
                                           )
                                       )
                                   )
                                   .blur(2.dp)
                           )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }

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
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "NEURAL VIBE ANALYZER",
                color = NeonCyan,
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
                    focusedBorderColor = NeonCyan,
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
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
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

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VibeCheckTheme {
        MainContent()
    }
}
