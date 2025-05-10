package com.teikk.mbbalancevoice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager


class NotificationListener : NotificationListenerService(){
    private var mediaPlayer: MediaPlayer? = null


    override fun onCreate() {
        super.onCreate()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Lọc thông báo từ MB Bank
        if (sbn.packageName == "com.mbmobile") {
            val extras = sbn.notification.extras
            val title = extras.getString("android.title")
            val text = extras.getString("android.text")

//            if (title != null && text != null && text.contains("nhận được tiền", ignoreCase = true)) {
//                speakOut(text)
//            }

            if(title != null && title.equals("Thông báo biến động số dư") && text != null){
                Log.d("TINHTINH", title);
                val regex = Regex("""\+([\d,]+)VND\s([\d\/\:\s]+).+\|ND:\s([\w\s]+)""")
                val matchResult = regex.find(text)

                if (matchResult != null) {
                    val amount = matchResult.groupValues[1]
                    val datetime = matchResult.groupValues[2]
                    val memo = matchResult.groupValues[3]

                    // Gửi thông báo qua Broadcast
                    val intent = Intent("com.teikk.mbbalancevoice.BALANCE_UPDATE")
                    intent.putExtra("amount", amount)
                    intent.putExtra("datetime", datetime)
                    intent.putExtra("memo", memo)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                    playRingtone()
                } else {
                    Log.e("TINHTINH", "Không tìm thấy kết quả khớp.")
                    Log.e("TINHTINH", text)
                }
            }
        }
    }

    private fun playRingtone() {
        try {
            // Tăng âm lượng lên mức cao nhất
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(
                AudioManager.STREAM_MUSIC,
                audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
                0
            )

            // Phát file nhạc chuông MP3
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.bel) // Đặt tên file MP3 là `ringtone.mp3`
                mediaPlayer?.setOnCompletionListener {
                    stopRingtone() // Dừng phát nhạc khi hoàn thành
                }
            }

            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("MyNotificationListener", "Error playing ringtone: ${e.message}")
        }
    }

    private fun stopRingtone() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone() // Giải phóng MediaPlayer khi Service bị hủy
    }
}
