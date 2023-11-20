package com.example.project_s

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.widget.Toolbar

class ModeConfirmDialog(context: Context) : Dialog(context){
    private val scaleUpAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.scale_up) }
    private val scaleDownAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.scale_down) }
    private lateinit var confirmDialogToolbar: Toolbar
    private var tts: TextToSpeech? = null


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_confirm)
        ttsSpeak("결제 완료 되었습니다.")
        confirmDialogToolbar = findViewById(R.id.confirm_dialog_toolbar)
        confirmDialogToolbar.setBackgroundColor(Color.GRAY)

        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = attributes
            params.windowAnimations = R.style.AnimationPopupStyle
            attributes = params
        }

        findViewById<Button>(R.id.confirmButton).setOnClickListener {
            dismiss()
        }
    }

    private fun ttsSpeak(strTTS:String){
        tts?.speak(strTTS, TextToSpeech.QUEUE_FLUSH,null,null)
    }
}