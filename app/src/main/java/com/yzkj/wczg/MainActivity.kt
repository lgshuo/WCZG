package com.yzkj.wczg

import android.Manifest
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import com.yzkj.serialport.Device
import com.yzkj.serialport.SerialPortFinder
import com.yzkj.wczg.adapter.DeviceAdapter
import com.yzkj.wczg.base.BaseActivity
import com.yzkj.wczg.databinding.ActivityMainBinding
import java.util.ArrayList

class MainActivity : BaseActivity<ActivityMainBinding>(), OnItemClickListener {
    val REQUEST_PERMISSION_CODE = 254
    private val TAG: String = MainActivity::class.java.getSimpleName()
    private lateinit var mDeviceAdapter: DeviceAdapter
    override fun initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQUEST_PERMISSION_CODE
            )
        }
        val listView = findViewById(R.id.lv_devices) as ListView


        val serialPortFinder = SerialPortFinder()

        val devices: ArrayList<Device> = serialPortFinder.devices

        if (listView != null) {
            listView.emptyView = findViewById(R.id.tv_empty)
            mDeviceAdapter = DeviceAdapter(applicationContext, devices)
            listView.adapter = mDeviceAdapter
            listView.setOnItemClickListener(this)
        }
    }


    override fun initListener() {
        val openHexCommand = "5501A20000F6"

    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val device: Device? = mDeviceAdapter.getItem(position)

        val intent = Intent(this, SerialPortActivity::class.java)
        intent.putExtra(SerialPortActivity.DEVICE, device)
        startActivity(intent)
    }
}

