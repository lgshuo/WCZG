package com.yzkj.serialport

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.yzkj.serialport.listener.OnOpenSerialPortListener
import com.yzkj.serialport.listener.OnSerialPortDataListener
import com.yzkj.serialport.listener.Status
import com.yzkj.serialport.thread.SerialPortReadThread
import java.io.*
import java.lang.Exception

class SerialPortManager : SerialPort() {
    private var mFileInputStream: FileInputStream? = null
    private var mFileOutputStream: FileOutputStream? = null
    private var mFd: FileDescriptor? = null
    private var mOnOpenSerialPortListener: OnOpenSerialPortListener? = null
    private var mOnSerialPortDataListener: OnSerialPortDataListener? = null
    private var mSendingHandlerThread: HandlerThread? = null
    private var mSendingHandler: Handler? = null
    private var mSerialPortReadThread: SerialPortReadThread? = null

    /**
     * 打开串口
     *
     * @param device   串口设备
     * @param baudRate 波特率
     * @return 打开是否成功
     */
    fun openSerialPort(device: File, baudRate: Int): Boolean {
        Log.i(TAG, "openSerialPort: " + String.format("打开串口 %s  波特率 %s", device.path, baudRate))

        // 校验串口权限
        if (!device.canRead() || !device.canWrite()) {
            val chmod777: Boolean = chmod777(device)
            if (!chmod777) {
                Log.i(TAG, "openSerialPort: 没有读写权限")
                if (null != mOnOpenSerialPortListener) {
                    mOnOpenSerialPortListener!!.onFail(
                        device,
                        Status.NO_READ_WRITE_PERMISSION
                    )
                }
                return false
            }
        }
        try {
            mFd = open(device.absolutePath, baudRate, 0)
            mFileInputStream = FileInputStream(mFd)
            mFileOutputStream = FileOutputStream(mFd)
            Log.i(TAG, "openSerialPort: 串口已经打开 $mFd")
            if (null != mOnOpenSerialPortListener) {
                mOnOpenSerialPortListener!!.onSuccess(device)
            }
            // 开启发送消息的线程
            startSendThread()
            // 开启接收消息的线程
            startReadThread()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            if (null != mOnOpenSerialPortListener) {
                mOnOpenSerialPortListener!!.onFail(device, Status.OPEN_FAIL)
            }
        }
        return false
    }

    /**
     * 关闭串口
     */
    fun closeSerialPort() {
        if (null != mFd) {
            close()
            mFd = null
        }
        // 停止发送消息的线程
        stopSendThread()
        // 停止接收消息的线程
        stopReadThread()
        if (null != mFileInputStream) {
            try {
                mFileInputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mFileInputStream = null
        }
        if (null != mFileOutputStream) {
            try {
                mFileOutputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mFileOutputStream = null
        }
        mOnOpenSerialPortListener = null
        mOnSerialPortDataListener = null
    }

    /**
     * 添加打开串口监听
     *
     * @param listener listener
     * @return SerialPortManager
     */
    fun setOnOpenSerialPortListener(listener: OnOpenSerialPortListener?): SerialPortManager {
        mOnOpenSerialPortListener = listener
        return this
    }

    /**
     * 添加数据通信监听
     *
     * @param listener listener
     * @return SerialPortManager
     */
    fun setOnSerialPortDataListener(listener: OnSerialPortDataListener?): SerialPortManager {
        mOnSerialPortDataListener = listener
        return this
    }

    /**
     * 开启发送消息的线程
     */
    private fun startSendThread() {
        // 开启发送消息的线程
        mSendingHandlerThread = HandlerThread("mSendingHandlerThread")
        mSendingHandlerThread!!.start()
        // Handler
        mSendingHandler = object : Handler(mSendingHandlerThread!!.looper) {
            override fun handleMessage(msg: Message) {
                val sendBytes = msg.obj as ByteArray
                if (null != mFileOutputStream && null != sendBytes && 0 < sendBytes.size) {
                    try {
                        mFileOutputStream!!.write(sendBytes)
                        if (null != mOnSerialPortDataListener) {
                            mOnSerialPortDataListener!!.onDataSent(sendBytes)
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * 停止发送消息线程
     */
    private fun stopSendThread() {
        mSendingHandler = null
        if (null != mSendingHandlerThread) {
            mSendingHandlerThread!!.interrupt()
            mSendingHandlerThread!!.quit()
            mSendingHandlerThread = null
        }
    }

    /**
     * 开启接收消息的线程
     */
    private fun startReadThread() {
        mSerialPortReadThread = object : SerialPortReadThread(mFileInputStream) {
            override fun onDataReceived(bytes: ByteArray) {
                if (null != mOnSerialPortDataListener) {
                    mOnSerialPortDataListener!!.onDataReceived(bytes)
                }
            }
        }
        mSerialPortReadThread!!.start()
    }

    /**
     * 停止接收消息的线程
     */
    private fun stopReadThread() {
        if (null != mSerialPortReadThread) {
            mSerialPortReadThread!!.release()
        }
    }

    /**
     * 发送数据
     *
     * @param sendBytes 发送数据
     * @return 发送是否成功
     */
    fun sendBytes(sendBytes: ByteArray): Boolean {
        if (null != mFd && null != mFileInputStream && null != mFileOutputStream) {
            if (null != mSendingHandler) {
                val message = Message.obtain()
                message.obj = sendBytes
                return mSendingHandler!!.sendMessage(message)
            }
        }
        return false
    }

    companion object {
        private val TAG = SerialPortManager::class.java.simpleName
    }
}
