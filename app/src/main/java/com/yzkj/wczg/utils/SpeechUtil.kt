package com.yzkj.wczg.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import java.util.*

class SpeechUtil private constructor() : TextToSpeech.OnInitListener {
    var mTTs: TextToSpeech? = null

    companion object {
        private var instance: SpeechUtil? = null
            get() {
                if (field == null) {
                    field = SpeechUtil()
                }
                return field
            }

        fun get(): SpeechUtil {
            return instance!!
        }
    }

    fun initSpeech(
        context: Context,
        pitch: Float = 1f,
        speechRate: Float = 0.5f,
        language: Locale = Locale.CHINA
    ) {
        mTTs = TextToSpeech(context, this)
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        mTTs!!.setPitch(pitch)
        mTTs!!.setSpeechRate(speechRate)
        mTTs!!.language = language
    }


    fun speek(
        speekText: String, queueMode: Int = TextToSpeech.QUEUE_ADD,
        params: Bundle? = null,
        utteranceId: String = "id${speekText}"
    ) {
        if (TextUtils.isEmpty(speekText.trim())) {
            return
        }
        if (mTTs != null && !mTTs!!.isSpeaking()) {
            mTTs!!.speak(speekText.trim(), queueMode, params, utteranceId)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // setLanguage设置语言
            val result = mTTs!!.setLanguage(Locale.CHINA)
            // TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失
            // TextToSpeech.LANG_NOT_SUPPORTED：不支持
            if (result == TextToSpeech.LANG_MISSING_DATA) {
                Log.e("speehUtil", "数据丢失")
            } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("speehUtil", "不支持语音播放")
            }


        }
    }

    fun release() {
        mTTs?.shutdown()
    }
}


