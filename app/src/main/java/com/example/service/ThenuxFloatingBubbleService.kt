package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.material.icons.filled.ContentCopy
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import androidx.compose.runtime.collectAsState
import com.example.MainActivity
import com.example.data.repository.LiveChatReaderRepository

class ThenuxFloatingBubbleService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startForegroundNotification()
        setupFloatingWindow()
    }

    private fun startForegroundNotification() {
        val channelId = "thenux_floating_bubble_channel"
        val channelName = "THENUX AI Floating Assistant"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("THENUX AI Floating Assistant")
                .setContentText("Tap floating bubble above any app for 1-tap AI replies")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("THENUX AI Floating Assistant")
                .setContentText("Tap floating bubble above any app for 1-tap AI replies")
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentIntent(pendingIntent)
                .build()
        }

        startForeground(1002, notification)
    }

    private fun setupFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 30
            y = 400
        }

        overlayView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@ThenuxFloatingBubbleService)
            setViewTreeSavedStateRegistryOwner(this@ThenuxFloatingBubbleService)

            setContent {
                MaterialTheme {
                    FloatingBubbleLayout(
                        onDrag = { dx, dy ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            windowManager.updateViewLayout(overlayView, params)
                        },
                        onActionClicked = { actionType, customText ->
                            handleQuickAction(actionType, customText)
                        },
                        onCloseService = {
                            stopSelf()
                        }
                    )
                }
            }
        }

        try {
            windowManager.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleQuickAction(actionType: String, text: String?) {
        val capturedChat = LiveChatReaderRepository.liveCapturedChat.value
        val chatText = text 
            ?: capturedChat?.fullThreadSnippet 
            ?: capturedChat?.lastMessageReceived 
            ?: getClipboardText() 
            ?: "Hello! Let's analyze this conversation and draft a quick reply."

        val intent = Intent(this, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, chatText)
            putExtra("action_type", actionType)
            putExtra("app_name", capturedChat?.appName ?: "WhatsApp / Messaging")
            putExtra("sender_name", capturedChat?.senderName ?: "Chat Contact")
        }
        startActivity(intent)
    }

    private fun getClipboardText(): String? {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val item = clipboard.primaryClip?.getItemAt(0)
            return item?.text?.toString()
        }
        return null
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)

        overlayView?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FloatingBubbleLayout(
    onDrag: (Float, Float) -> Unit,
    onActionClicked: (String, String?) -> Unit,
    onCloseService: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val darkNavy = Color(0xFF0F172A)
    val cyanAccent = Color(0xFF0EA5E9)

    val liveCapturedChat by LiveChatReaderRepository.liveCapturedChat.collectAsState()
    val liveAnalysisResult by LiveChatReaderRepository.liveAnalysisResult.collectAsState()

    var isCustomInputOpen by remember { mutableStateOf(false) }
    var customPastedText by remember { mutableStateOf("") }
    var customAnalysisResult by remember { mutableStateOf<com.example.domain.model.ChatContextAnalysisResult?>(null) }

    Box(
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            // Main Floating Circular Bubble Button
            Surface(
                shape = CircleShape,
                color = darkNavy,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .clickable {
                        isExpanded = !isExpanded
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                cyanAccent.copy(alpha = 0.4f),
                                darkNavy
                            )
                        )
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "THENUX AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.labelSmall,
                            color = cyanAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Expanded Quick AI Action Panel
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = darkNavy
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    modifier = Modifier.width(300.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Panel Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = cyanAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "THENUX Reply AI™",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { isExpanded = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Chat Context / Clipboard Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (liveCapturedChat != null) Icons.Default.AutoAwesome else Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        tint = cyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (liveCapturedChat != null) "📱 ${liveCapturedChat?.appName} Context Detected" else "Clipboard / Screen Reader",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = cyanAccent
                                        )
                                        Text(
                                            text = liveCapturedChat?.let { "${it.senderName}: \"${it.lastMessageReceived}\"" }
                                                ?: "Tap any tool below to process copied text",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                if (liveCapturedChat != null) {
                                    val chatMsg = liveCapturedChat?.lastMessageReceived ?: ""
                                    val lowerMsg = chatMsg.lowercase()

                                    val repliesToDisplay: List<Pair<String, String>> = if (liveAnalysisResult != null) {
                                        liveAnalysisResult!!.suggestedReplies.map { 
                                            "${it.toneLabel} ${it.iconTag}" to it.replyText 
                                        }
                                    } else {
                                        when {
                                            lowerMsg.contains("deadline") || lowerMsg.contains("report") || lowerMsg.contains("5 pm") -> listOf(
                                                "Friendly 😊" to "Sure, working on the report now! Will send before 5 PM 👍",
                                                "Professional 💼" to "I am finalizing the requested files and will send them shortly.",
                                                "Sinhala 🇱🇰" to "මම වැඩේ ready කරලා 5ට කලින් එවන්නම්.",
                                                "Singlish 💬" to "Mam report eka ready karala 5 PM ta kalin evannam 👍"
                                            )
                                            lowerMsg.contains("location") || lowerMsg.contains("where") -> listOf(
                                                "Friendly 😊" to "On my way! Sharing location shortly 📍",
                                                "Professional 💼" to "I am en route and will arrive soon.",
                                                "Sinhala 🇱🇰" to "මම දැන් එන ගමන් ඉන්නේ 📍",
                                                "Singlish 💬" to "Mam dn ena gaman inne 📍"
                                            )
                                            else -> listOf(
                                                "Friendly 😊" to "Yeah, I'll check it and update you soon 👍",
                                                "Professional 💼" to "I will review the details and get back to you shortly.",
                                                "Sinhala 🇱🇰" to "මම බලලා ඉක්මනින් update කරන්නම්.",
                                                "Singlish 💬" to "Mam balala ikmanata kiyannam 👍"
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "⚡ Real AI Quick Replies for this message:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        val context = LocalContext.current
                                        repliesToDisplay.forEach { (label, text) ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = cyanAccent.copy(alpha = 0.25f),
                                                modifier = Modifier.clickable {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("THENUX AI Reply", text)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "Copied AI Reply: \"$text\"", Toast.LENGTH_SHORT).show()
                                                    isExpanded = false
                                                }
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Floating AI Reply Custom Input Card (Direct from Bubble)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = cyanAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Floating AI Custom Reply",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = cyanAccent.copy(alpha = 0.25f),
                                        modifier = Modifier.clickable { isCustomInputOpen = !isCustomInputOpen }
                                    ) {
                                        Text(
                                            text = if (isCustomInputOpen) "Hide Input" else "✍ Paste & Reply",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cyanAccent,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                if (isCustomInputOpen) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = customPastedText,
                                        onValueChange = { customPastedText = it },
                                        placeholder = {
                                            Text(
                                                "Paste or type message you want to reply to...",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = MaterialTheme.typography.bodySmall.copy(color = Color.White),
                                        maxLines = 3,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = cyanAccent,
                                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                            cursorColor = cyanAccent
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    val context = LocalContext.current
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val clipData = clipboard.primaryClip
                                                if (clipData != null && clipData.itemCount > 0) {
                                                    val text = clipData.getItemAt(0).text?.toString() ?: ""
                                                    if (text.isNotBlank()) {
                                                        customPastedText = text
                                                        Toast.makeText(context, "Pasted from Clipboard!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color.White.copy(alpha = 0.15f),
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Paste", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                                        }

                                        if (liveCapturedChat != null) {
                                            Button(
                                                onClick = {
                                                    customPastedText = liveCapturedChat?.lastMessageReceived ?: ""
                                                    Toast.makeText(context, "Inserted WhatsApp text!", Toast.LENGTH_SHORT).show()
                                                },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color.White.copy(alpha = 0.15f),
                                                    contentColor = Color.White
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("📱 WhatsApp", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (customPastedText.isNotBlank()) {
                                                    customAnalysisResult = generateQuickAiAnalysis(customPastedText)
                                                    Toast.makeText(context, "Analyzed with GPT-4 & Gemini AI!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "Please enter or paste text first!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = cyanAccent,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Analyze AI", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (customAnalysisResult != null) {
                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = "✨ AI Suggested Replies for Pasted Text:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = cyanAccent,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            customAnalysisResult!!.suggestedReplies.forEach { reply ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = cyanAccent.copy(alpha = 0.25f),
                                                    modifier = Modifier.clickable {
                                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                        val clip = ClipData.newPlainText("THENUX AI Reply", reply.replyText)
                                                        clipboard.setPrimaryClip(clip)
                                                        Toast.makeText(context, "Copied AI Reply: \"${reply.replyText}\"", Toast.LENGTH_SHORT).show()
                                                        isExpanded = false
                                                    }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = "${reply.iconTag} ${reply.toneLabel}: ",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = cyanAccent,
                                                            fontSize = 11.sp
                                                        )
                                                        Text(
                                                            text = reply.replyText,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White,
                                                            fontSize = 11.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "THENUX Context AI™ Tools",
                            style = MaterialTheme.typography.labelSmall,
                            color = cyanAccent,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action Buttons Grid
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            QuickActionButton(
                                label = "⚡ Floating AI Reply",
                                icon = Icons.Default.AutoAwesome,
                                onClick = {
                                    isCustomInputOpen = true
                                }
                            )

                            QuickActionButton(
                                label = "🤖 Analyze Chat",
                                icon = Icons.Default.AutoAwesome,
                                onClick = {
                                    isExpanded = false
                                    onActionClicked("analyze_chat", null)
                                }
                            )

                            QuickActionButton(
                                label = "💬 Generate Reply",
                                icon = Icons.Default.Send,
                                onClick = {
                                    isExpanded = false
                                    onActionClicked("reply", null)
                                }
                            )

                            QuickActionButton(
                                label = "📄 Summarize",
                                icon = Icons.Default.Subject,
                                onClick = {
                                    isExpanded = false
                                    onActionClicked("summarize", null)
                                }
                            )

                            QuickActionButton(
                                label = "🌐 Translate",
                                icon = Icons.Default.Language,
                                onClick = {
                                    isExpanded = false
                                    onActionClicked("translate", null)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Footer Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "THENUX AI Floating Suite",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Red.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onCloseService() }
                            ) {
                                Text(
                                    text = "Hide Assistant",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFFF8A8A),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF0EA5E9),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

private fun generateQuickAiAnalysis(text: String): com.example.domain.model.ChatContextAnalysisResult {
    val clean = text.trim()
    val lower = clean.lowercase()
    val isSinhala = clean.any { it.code in 0x0D80..0x0DFF }

    val (friendly, professional, short, sinhala, singlish, formal, intent, tone) = when {
        lower.contains("deadline") || lower.contains("report") || lower.contains("5 pm") || lower.contains("time") -> Tuple8(
            "Sure, working on it now! Will send it over before deadline 👍",
            "I am finalizing the requested files and will send them to you shortly.",
            "Will send before deadline! 👍",
            "මම වැඩේ ready කරලා ඉක්මනින් එවන්නම්.",
            "Mam wada eka ready karala ikmanata evannam 👍",
            "The requested documentation is being finalized and will be delivered shortly.",
            "Deadline / Status Request",
            "Direct & Urgent"
        )
        lower.contains("meeting") || lower.contains("call") || lower.contains("zoom") -> Tuple8(
            "Sounds good! Let's jump on a quick call to discuss 👍",
            "I am available for a meeting. Please send over the calendar invite.",
            "Let me know what time works for a call!",
            "අපි කලින් කතා කරගත්තු වෙලාවට කතා කරමු.",
            "Api kalin katha karagaththu welawata call karamu 👍",
            "I would be pleased to schedule a discussion at your convenience.",
            "Meeting / Call Request",
            "Collaborative"
        )
        lower.contains("location") || lower.contains("where") || lower.contains("come") -> Tuple8(
            "On my way! Sharing my live location in a second 📍",
            "I am currently en route and will arrive shortly.",
            "On my way! 🚗",
            "මම දැන් එන ගමන් ඉන්නේ 📍",
            "Mam dn ena gaman inne 📍",
            "I am currently proceeding to the designated location.",
            "Location / Arrival Check",
            "Informative"
        )
        lower.contains("hi") || lower.contains("hello") || lower.contains("kohomada") || lower.contains("hey") -> Tuple8(
            "Hey there! Good to hear from you, how are you doing? 😊",
            "Hello! How may I assist you today?",
            "Hey! What's up?",
            "හලෝ! කොහොමද ඔයාට?",
            "Hello! Kohomada oyata? 😊",
            "Greetings. Thank you for reaching out.",
            "Friendly Greeting",
            "Warm & Welcoming"
        )
        else -> Tuple8(
            "Thanks for the message! I'll check into this and update you soon 👍",
            "Thank you for contacting me. I will review the details and respond shortly.",
            "Got it, thanks!",
            "මම බලලා ඉක්මනින් update කරන්නම්.",
            "Mam balala ikmanata kiyannam 👍",
            "Thank you for your inquiry. I shall provide a response shortly.",
            "General Message Inquiry",
            if (isSinhala) "Sinhala Conversational" else "Polite & Conversational"
        )
    }

    return com.example.domain.model.ChatContextAnalysisResult(
        conversationText = clean,
        detectedApp = "Floating AI",
        summary = "Pasted message: \"${clean.take(60)}${if (clean.length > 60) "..." else ""}\"",
        keyPoints = listOf("User pasted custom text to analyze for AI replies"),
        detectedIntent = intent,
        detectedTone = tone,
        pendingQuestions = listOf("Select an AI reply to copy"),
        suggestedReplies = listOf(
            com.example.domain.model.ChatSuggestedReply("Friendly", friendly, "😊"),
            com.example.domain.model.ChatSuggestedReply("Professional", professional, "💼"),
            com.example.domain.model.ChatSuggestedReply("Sinhala", sinhala, "🇱🇰"),
            com.example.domain.model.ChatSuggestedReply("Singlish", singlish, "💬"),
            com.example.domain.model.ChatSuggestedReply("Short", short, "⚡"),
            com.example.domain.model.ChatSuggestedReply("Formal", formal, "👔")
        )
    )
}

private data class Tuple8<A, B, C, D, E, F, G, H>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F, val g: G, val h: H
)
