package com.example.weatherstation.data.local

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresPermission
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withTimeout
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

class BleManager(private val context: Context) {

    companion object {
        val SERVICE_UUID: UUID    = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
        val SSID_CHAR_UUID: UUID  = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
        val PASS_CHAR_UUID: UUID  = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
        val TOKEN_CHAR_UUID: UUID = UUID.fromString("0000FFF3-0000-1000-8000-00805F9B34FB")
    }

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val adapter: BluetoothAdapter? = bluetoothManager.adapter
    private val scanner: BluetoothLeScanner? get() = adapter?.bluetoothLeScanner

    private var currentGatt: BluetoothGatt? = null
    private var connectDeferred: CompletableDeferred<Boolean>? = null
    private var writeDeferred: CompletableDeferred<Boolean>? = null

    private val gattCallback = object : BluetoothGattCallback() {

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    currentGatt = gatt
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    currentGatt = null
                    connectDeferred?.complete(false)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            val ok = status == BluetoothGatt.GATT_SUCCESS
            connectDeferred?.complete(ok)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            writeDeferred?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }
    }

    @SuppressLint("MissingPermission")
    fun scanForDevices(): Flow<ScanResult> = callbackFlow {
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                trySend(result)
            }
        }
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner?.startScan(null, settings, callback)
        awaitClose { scanner?.stopScan(callback) }
    }

    @SuppressLint("MissingPermission")
    suspend fun connect(device: BluetoothDevice): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        connectDeferred = deferred
        currentGatt = device.connectGatt(context, false, gattCallback)
        return try {
            withTimeout(15_000.milliseconds) { deferred.await() }
        } catch (_: TimeoutCancellationException) {
            currentGatt?.close()
            currentGatt = null
            false
        } finally {
            connectDeferred = null
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun writeCharacteristic(characteristicUuid: UUID, data: ByteArray): Boolean {
        val gatt = currentGatt ?: return false
        val service = gatt.getService(SERVICE_UUID) ?: return false
        val chr = service.getCharacteristic(characteristicUuid) ?: return false

        val deferred = CompletableDeferred<Boolean>()
        writeDeferred = deferred

        val started: Boolean = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt.writeCharacteristic(
                chr,
                data,
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            ) == BluetoothStatusCodes.SUCCESS
        } else {
            @Suppress("DEPRECATION")
            chr.value = data
            @Suppress("DEPRECATION")
            gatt.writeCharacteristic(chr)
        }

        if (!started) {
            writeDeferred = null
            return false
        }

        return try {
            withTimeout(5_000.milliseconds) { deferred.await() }
        } catch (_: TimeoutCancellationException) {
            false
        } finally {
            writeDeferred = null
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        currentGatt?.disconnect()
        currentGatt?.close()
        currentGatt = null
    }
}