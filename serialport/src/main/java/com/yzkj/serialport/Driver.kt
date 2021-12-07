package com.yzkj.serialport

import android.util.Log
import java.io.File
import java.util.ArrayList

class Driver(val name: String, private val mDeviceRoot: String) {
    val devices: ArrayList<File>
        get() {
            val devices = ArrayList<File>()
            val dev = File("/dev")
            if (!dev.exists()) {
                Log.i(TAG, "getDevices: " + dev.absolutePath + " 不存在")
                return devices
            }
            if (!dev.canRead()) {
                Log.i(TAG, "getDevices: " + dev.absolutePath + " 没有读取权限")
                return devices
            }
            val files = dev.listFiles()
            var i: Int
            i = 0
            while (i < files.size) {
                if (files[i].absolutePath.startsWith(mDeviceRoot)) {
                    Log.d(TAG, "Found new device: " + files[i])
                    devices.add(files[i])
                }
                i++
            }
            return devices
        }

    companion object {
        private val TAG = Driver::class.java.simpleName
    }
}
