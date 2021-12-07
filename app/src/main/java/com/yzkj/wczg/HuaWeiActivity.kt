package com.yzkj.wczg

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.view.View
import com.yzkj.wczg.base.BaseActivity
import com.yzkj.wczg.databinding.ActivityHuaWeiBinding
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCapture
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCaptureResult
import com.yzkj.wczg.utils.SpeechUtil
import java.util.*


class HuaWeiActivity : BaseActivity<ActivityHuaWeiBinding>(), View.OnClickListener{
    override fun initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA,
                ), 23
            )
        }
        SpeechUtil.get().initSpeech(this)
    }

    override fun initListener() {
        mBinding.face.setOnClickListener(this)
        mBinding.voice2text.setOnClickListener(this)
        mBinding.text2voice.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v?.id) {
            R.id.face -> {
                val capture: MLLivenessCapture = MLLivenessCapture.getInstance()
                capture.startDetect(this, object : MLLivenessCapture.Callback {
                    override fun onSuccess(result: MLLivenessCaptureResult?) {
                        var temp: Array<String>? = null
                        val live: String = result.toString()
                        val sub = live.substring(live.indexOf("{"), live.lastIndexOf("}"))
                        temp = sub.split(",").toTypedArray()
                        mBinding.textResult.setText(
                            "Liveness Detection Result: " + temp.get(0) + "}"
                        )
                    }

                    override fun onFailure(errorCode: Int) {
                        mBinding.textResult.setText(
                            "${errorCode}"
                        )
                    }

                })
            }
            R.id.text2voice -> {
                SpeechUtil.get().speek(mBinding.edittxt.text.toString())
            }
            R.id.voice2text -> {

            }
        }


    }
}