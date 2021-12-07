package com.yzkj.serialport.listener

import java.io.File

interface OnOpenSerialPortListener {
    fun onSuccess(device: File)

    fun onFail(device: File, status: Status)

}
enum class Status {
    NO_READ_WRITE_PERMISSION, OPEN_FAIL
}