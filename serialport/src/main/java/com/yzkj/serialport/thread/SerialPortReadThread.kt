package com.yzkj.serialport.thread

import android.util.Log
import java.io.IOException
import java.io.InputStream

abstract class SerialPortReadThread(private var mInputStream: InputStream?) : Thread() {
    abstract fun onDataReceived(bytes: ByteArray)
    private val mReadBuffer: ByteArray
    override fun run() {
        super.run()
        while (!isInterrupted) {
            try {
                if (null == mInputStream) {
                    return
                }
                Log.i(TAG, "run: ")
                val size = mInputStream!!.read(mReadBuffer)
                if (-1 == size || 0 >= size) {
                    return
                }
                val readBytes = ByteArray(size)
                System.arraycopy(mReadBuffer, 0, readBytes, 0, size)
                Log.i(TAG, "run: readBytes = " + String(readBytes))
                onDataReceived(readBytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }
        }
    }

    @Synchronized
    override fun start() {
        super.start()
    }

    /**
     * 关闭线程 释放资源
     */
    fun release() {
        interrupt()
        if (null != mInputStream) {
            try {
                mInputStream!!.close()
                mInputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private val TAG = SerialPortReadThread::class.java.simpleName
    }

    init {
        mReadBuffer = ByteArray(1024)
    }
}

