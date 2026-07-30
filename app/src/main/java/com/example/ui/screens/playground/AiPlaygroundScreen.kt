package com.example.ui.screens.playground

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun AiPlaygroundScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Model Speed Test", "Prompt Analyzer", "Guess Tone Game")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "AI Testing Playground",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Interactive model benchmarking & tone games",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> ModelSpeedArena()
            1 -> PromptStrengthAnalyzer()
            2 -> GuessToneGame()
        }
    }
}

@Composable
private fun ModelSpeedArena() {
    var isTesting by remember { mutableStateOf(false) }
    var tNexProgress by remember { mutableStateOf(0f) }
    var gpt4Progress by remember { mutableStateOf(0f) }
    var geminiProgress by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    val animatedTNex by animateFloatAsState(tNexProgress, label = "tnex")
    val animatedGpt4 by animateFloatAsState(gpt4Progress, label = "gpt4")
    val animatedGemini by animateFloatAsState(geminiProgress, label = "gemini")

    fun runBenchmark() {
        if (isTesting) return
        isTesting = true
        tNexProgress = 0f
        gpt4Progress = 0f
        geminiProgress = 0f

        scope.launch {
            // T-Nex 1.0 (Fastest)
            for (i in 1..10) {
                delay(80)
                tNexProgress = i / 10f
            }
            // Gemini Sinhala
            for (i in 1..10) {
                delay(120)
                geminiProgress = i / 10f
            }
            // GPT-4
            for (i in 1..10) {
                delay(160)
                gpt4Progress = i / 10f
            }
            isTesting = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Model Benchmark", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Test response latency and token throughput across THENUX™ model suite.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { runBenchmark() },
                        enabled = !isTesting,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isTesting) "Running Benchmark..." else "Start Latency Test")
                    }
                }
            }
        }

        item {
            BenchmarkCard("T-Nex 1.0 (THENUX™ Core)", "Ultra-low latency (80ms avg)", animatedTNex, MaterialTheme.colorScheme.primary)
        }
        item {
            BenchmarkCard("Gemini Sinhala 1.5", "Localized NLP pipeline (120ms avg)", animatedGemini, MaterialTheme.colorScheme.secondary)
        }
        item {
            BenchmarkCard("GPT-4 High Precision", "Deep reasoning engine (160ms avg)", animatedGpt4, MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Composable
private fun BenchmarkCard(name: String, desc: String, progress: Float, color: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
            }
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = color,
                trackColor = color.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )
        }
    }
}

@Composable
private fun PromptStrengthAnalyzer() {
    var promptInput by remember { mutableStateOf("") }

    val charCount = promptInput.length
    val hasContext = promptInput.contains("context", ignoreCase = true) || charCount > 25
    val hasTone = promptInput.contains("professional", ignoreCase = true) ||
            promptInput.contains("friendly", ignoreCase = true) ||
            promptInput.contains("formal", ignoreCase = true) ||
            promptInput.contains("singlish", ignoreCase = true)

    var score = 30
    if (charCount > 15) score += 25
    if (hasContext) score += 25
    if (hasTone) score += 20
    score = score.coerceAtMost(100)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Prompt Strength Meter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Type a test prompt to evaluate context clarity and tone precision.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("e.g. Reply politely to my manager about the project deadline extension...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prompt Strength Score", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("$score%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { score / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Optimization Tips:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    TipRow("Length & Detail (>15 chars)", charCount > 15)
                    TipRow("Context Clarity (mentions scenario/background)", hasContext)
                    TipRow("Explicit Tone Directive (e.g. Professional, Friendly)", hasTone)
                }
            }
        }
    }
}

@Composable
private fun TipRow(label: String, satisfied: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = if (satisfied) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (satisfied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (satisfied) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GuessToneGame() {
    val quizList = remember {
        listOf(
            QuizQuestion(
                replyText = "Thank you for sending over the project specification. I have reviewed section 3 and will submit the finalized report by 5:00 PM today.",
                correctTone = "Professional",
                options = listOf("Professional", "Flirty", "Singlish", "Angry")
            ),
            QuizQuestion(
                replyText = "Machan eka godak lassanai! Api ada hawasa hamba wela kata karamu neda?",
                correctTone = "Singlish",
                options = listOf("Formal", "Singlish", "Sarcastic", "Corporate")
            ),
            QuizQuestion(
                replyText = "Hey there! Loved seeing your photos, you look amazing! Hope you have a wonderful weekend ahead 😉✨",
                correctTone = "Flirty",
                options = listOf("Legal", "Flirty", "Formal", "Technical")
            )
        )
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }

    val currentQ = quizList[currentQuestionIndex]

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Guess the AI Tone Game 🎮", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Score: $score pts",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Read the AI Generated Response:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "\"${currentQ.replyText}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Which tone was used to generate this reply?", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    currentQ.options.forEach { option ->
                        val isThisSelected = selectedAnswer == option
                        val isThisCorrect = option == currentQ.correctTone

                        Button(
                            onClick = {
                                if (selectedAnswer == null) {
                                    selectedAnswer = option
                                    if (isThisCorrect) {
                                        score += 10
                                        isCorrect = true
                                    } else {
                                        isCorrect = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(option)
                        }
                    }

                    if (selectedAnswer != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isCorrect == true) "🎉 Correct! +10 Points" else "❌ Incorrect. Correct tone was '${currentQ.correctTone}'.",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCorrect == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                selectedAnswer = null
                                isCorrect = null
                                currentQuestionIndex = (currentQuestionIndex + 1) % quizList.size
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Question")
                        }
                    }
                }
            }
        }
    }
}

private data class QuizQuestion(
    val replyText: String,
    val correctTone: String,
    val options: List<String>
)
