package com.yzkj.wczg

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.yzkj.serialport.Device
import com.yzkj.serialport.SerialPortManager
import com.yzkj.serialport.listener.OnOpenSerialPortListener
import com.yzkj.serialport.listener.OnSerialPortDataListener
import com.yzkj.serialport.listener.Status
import com.yzkj.wczg.base.BaseActivity
import com.yzkj.wczg.databinding.ActivitySerialPortBinding
import com.yzkj.wczg.utils.MyFunc
import java.io.File
import java.util.*

class SerialPortActivity : BaseActivity<ActivitySerialPortBinding>(), OnOpenSerialPortListener {
    private val TAG = SerialPortActivity::class.java.simpleName
    companion object{
        val DEVICE = "device"
    }
    private var mSerialPortManager: SerialPortManager? = null

    override fun initData() {
        val device: Device? = intent.getSerializableExtra(DEVICE) as Device?
        Log.i(TAG, "onCreate: device = $device")
        if (null == device) {
            finish()
            return
        }
        mSerialPortManager = SerialPortManager()

        // 打开串口
        val openSerialPort = mSerialPortManager!!.setOnOpenSerialPortListener(this)
            .setOnSerialPortDataListener(object : OnSerialPortDataListener {
                override fun onDataReceived(bytes: ByteArray) {
                    Log.i(TAG, "onDataReceived [ byte[] ]: " + Arrays.toString(bytes))
                    Log.i(TAG, "onDataReceived [ String ]: " + String(bytes))
                    runOnUiThread { showToast(String.format("接收\n%s", String(bytes))) }
                }

                override fun onDataSent(bytes: ByteArray) {
                    Log.i(TAG, "onDataSent [ byte[] ]: " + Arrays.toString(bytes))
                    Log.i(TAG, "onDataSent [ String ]: " + String(bytes))
                    runOnUiThread { showToast(String.format("发送\n%s", String(bytes))) }
                }
            })
            .openSerialPort(device.file!!, 19200)
        Log.i(TAG, "onCreate: openSerialPort = $openSerialPort")
    }

    override fun onDestroy() {
        if (null != mSerialPortManager) {
            mSerialPortManager!!.closeSerialPort()
            mSerialPortManager = null
        }
        super.onDestroy()
    }

    /**
     * 串口打开成功
     *
     * @param device 串口
     */
    override fun onSuccess(device: File) {
        Toast.makeText(
            applicationContext,
            String.format("串口 [%s] 打开成功", device.path),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * 串口打开失败
     *
     * @param device 串口
     * @param status status
     */
    override fun onFail(device: File, status: Status) {
        when (status) {
            Status.NO_READ_WRITE_PERMISSION -> showDialog(device.path, "没有读写权限")
            Status.OPEN_FAIL -> showDialog(device.path, "串口打开失败")
            else -> showDialog(device.path, "串口打开失败")
        }
    }

    /**
     * 显示提示框
     *
     * @param title   title
     * @param message message
     */
    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("退出", DialogInterface.OnClickListener { dialog, id ->
                dialog.dismiss()
                finish()
            })
            .setCancelable(false)
            .create()
            .show()
    }

    override fun initListener() {
        mBinding.sendButton.setOnClickListener {
            val openHexCommand = "5501A20000F6"
            val sendContentBytes = MyFunc.HexToByteArr(openHexCommand)
            val sendBytes = mSerialPortManager!!.sendBytes(sendContentBytes)
            showToast(if (sendBytes) "发送成功" else "发送失败")
//            val sendContent = mBinding.sendContent.text.toString().trim { it <= ' ' }
//            if (TextUtils.isEmpty(sendContent)) {
//                Log.i(TAG, "onSend: 发送内容为 null")
//            } else {
//                val sendContentBytes = sendContent.toByteArray()
//                val sendBytes = mSerialPortManager!!.sendBytes(sendContentBytes)
//                Log.i(TAG, "onSend: sendBytes = $sendBytes")
//                showToast(if (sendBytes) "发送成功" else "发送失败")
//            }

        }
    }

    private var mToast: Toast? = null

    /**
     * Toast
     *
     * @param content content
     */
    private fun showToast(content: String) {
        if (null == mToast) {
            mToast = Toast.makeText(applicationContext, null, Toast.LENGTH_SHORT)
        }
        mToast!!.setText(content)
        mToast!!.show()
    }
}