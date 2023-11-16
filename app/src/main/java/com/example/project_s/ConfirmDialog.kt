package com.example.project_s

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.Button

class ConfirmDialog(context: Context) : Dialog(context){
    private val scaleUpAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.scale_up) }
    private val scaleDownAnimation by lazy { AnimationUtils.loadAnimation(context, R.anim.scale_down) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_confirm)
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
}