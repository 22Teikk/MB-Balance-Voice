package com.teikk.mbbalancevoice

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Locale

class TtsService : AccessibilityService(), TextToSpeech.OnInitListener {
    private lateinit var tts: TextToSpeech
    companion object {
        private const val TAG  = "Teikk_tts"
        var isRunning = false
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val status = intent?.getStringExtra("status")
            val amount = intent?.getStringExtra("amount")
            val datetime = intent?.getStringExtra("datetime")
            val memo = intent?.getStringExtra("memo")
            // Gọi TTS hoặc phát chuông
            if (status == "+") {
                speak("Bạn đã nhận được ${formatMoneyForSpeech(amount.toString())}")
            } else {
                speak("Bạn đã chuyển ${formatMoneyForSpeech(amount.toString())}")
            }
        }
    }

    fun formatMoneyForSpeech(amount: String): String {
        // Loại bỏ dấu phẩy và các ký tự không phải số
        val cleanAmount = amount.replace("[^0-9]".toRegex(), "")

        // Chuyển đổi thành số
        val number = cleanAmount.toLongOrNull() ?: return "0 đồng"

        if (number == 0L) return "0 đồng"

        // Nếu số tiền lớn hơn hoặc bằng 1 triệu
        if (number >= 1_000_000) {
            val millions = number / 1_000_000
            val thousands = (number % 1_000_000) / 1_000
            val remainder = number % 1_000

            val result = StringBuilder()

            if (millions > 0) {
                result.append("$millions triệu")
            }

            if (thousands > 0) {
                if (result.isNotEmpty()) result.append(" ")
                result.append("$thousands nghìn")
            }

            if (remainder > 0) {
                if (result.isNotEmpty()) result.append(" ")
                result.append("$remainder")
            }

            return result.toString() + " đồng"
        }
        // Nếu số tiền lớn hơn hoặc bằng 1 nghìn
        else if (number >= 1_000) {
            val thousands = number / 1_000
            val remainder = number % 1_000

            val result = StringBuilder("$thousands nghìn")

            if (remainder > 0) {
                result.append(" $remainder")
            }

            return result.toString() + " đồng"
        }
        // Nếu số tiền nhỏ hơn 1 nghìn
        else {
            return "$number đồng"
        }
    }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        isRunning = true
        val filter = IntentFilter("com.teikk.mbbalancevoice.BALANCE_UPDATE")
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

    override fun onInterrupt() {

    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale("vi", "VN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "Ngôn ngữ không hỗ trợ", Toast.LENGTH_SHORT).show()
            } else {
                speak("Xin chào! Đây là giọng đọc tích hợp.")
            }
        } else {
            Toast.makeText(this, "Khởi tạo TTS thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    private fun speak(text: String) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )
        } catch (e: Exception) {
            Log.d(TAG, "Error setting volume: ${e.message}")
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        super.onDestroy()
        tts.stop()
        tts.shutdown()
    }
}
//Text: TK 09xxx016|GD: +500,000VND 11/05/25 14:46 |SD: 737,078VND|TU: 970422....1258|ND: Nguyen Tuan Kiet chuyen tien tu Viettel Money
