package com.yzkj.serialport

import java.io.File
import java.io.Serializable

data class Device(var name: String?,
                  var root:String?,
                  var file:File?
) : Serializable