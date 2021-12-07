package com.yzkj.serialport

import android.util.Log
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader
import java.util.ArrayList


class SerialPortFinder {
    /**
     * 获取 Drivers
     *
     * @return Drivers
     * @throws IOException IOException
     */
    @get:Throws(IOException::class)
    private val drivers: ArrayList<Driver>
        private get() {
            val drivers = ArrayList<Driver>()
            val lineNumberReader = LineNumberReader(FileReader(DRIVERS_PATH))
            var readLine: String
            while (lineNumberReader.readLine().also { readLine = it } != null) {
                val driverName = readLine.substring(0, 0x15).trim { it <= ' ' }
                val fields = readLine.split(" +").toTypedArray()
                if (fields.size >= 5 && fields[fields.size - 1] == SERIAL_FIELD) {
                    Log.d(TAG, "Found new driver " + driverName + " on " + fields[fields.size - 4])
                    drivers.add(Driver(driverName, fields[fields.size - 4]))
                }
            }
            return drivers
        }

    /**
     * 获取串口
     *
     * @return 串口
     */
    val devices: ArrayList<Device>
        get() {
            val devices = ArrayList<Device>()
            try {
                val drivers = drivers
                for (driver in drivers) {
                    val driverName: String = driver.name
                    val driverDevices: ArrayList<File> = driver.devices
                    for (file in driverDevices) {
                        val devicesName = file.name
                        devices.add(Device(devicesName, driverName, file))
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return devices
        }

    companion object {
        private val TAG = SerialPortFinder::class.java.simpleName
        private const val DRIVERS_PATH = "/proc/tty/drivers"
        private const val SERIAL_FIELD = "serial"
    }

    init {
        val file = File(DRIVERS_PATH)
        val b = file.canRead()
        Log.i(TAG, "SerialPortFinder: file.canRead() = $b")
    }
}
