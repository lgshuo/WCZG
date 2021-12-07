package com.yzkj.serialport.listener

interface OnSerialPortDataListener {
    /**
     * 数据接收
     *
     * @param bytes 接收到的数据
     */
    fun onDataReceived(bytes: ByteArray)

    /**
     * 数据发送
     *
     * @param bytes 发送的数据
     */
    fun onDataSent(bytes: ByteArray)
}