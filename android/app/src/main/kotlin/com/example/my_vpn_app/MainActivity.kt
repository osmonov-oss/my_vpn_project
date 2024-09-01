package com.example.my_vpn_app

import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity: FlutterActivity() {
    private val channel = "com.example.my_vpn_app/wireguard"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channel).setMethodCallHandler { call, result ->
            when (call.method) {
                "connect" -> {
                    val success = connectToWireGuard(
                        call.argument("privateKey") ?: "",
                        call.argument("publicKey") ?: "",
                        call.argument("endpoint") ?: "",
                        call.argument("address") ?: "",
                        call.argument("dns") ?: "",
                        call.argument("allowedIPs") ?: "",
                        call.argument<Int>("persistentKeepalive") ?: 0
                    )
                    if (success) {
                        result.success(true)
                    } else {
                        result.error("UNAVAILABLE", "Failed to connect to WireGuard", null)
                    }
                }
                "disconnect" -> {
                    val success = disconnectFromWireGuard()
                    if (success) {
                        result.success(true)
                    } else {
                        result.error("UNAVAILABLE", "Failed to disconnect from WireGuard", null)
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    private fun connectToWireGuard(privateKey: String, publicKey: String, endpoint: String, address: String, dns: String, allowedIPs: String, persistentKeepalive: Int): Boolean {
        return try {
            // Создаем временный конфигурационный файл для WireGuard
            val config = """
                [Interface]
                PrivateKey = $privateKey
                Address = $address
                DNS = $dns
                
                [Peer]
                PublicKey = $publicKey
                Endpoint = $endpoint
                AllowedIPs = $allowedIPs
                PersistentKeepalive = $persistentKeepalive
            """.trimIndent()

            // Записываем конфиг в файл
            val configFilePath = filesDir.absolutePath + "/wg0.conf"
            val writeConfig = ProcessBuilder("sh", "-c", "echo \"$config\" > $configFilePath").start()
            writeConfig.waitFor()

            // Запуск WireGuard с помощью созданного конфигурационного файла
            val startWireGuard = ProcessBuilder("wg-quick", "up", configFilePath).start()
            val exitCode = startWireGuard.waitFor()

            if (exitCode == 0) {
                true
            } else {
                val reader = BufferedReader(InputStreamReader(startWireGuard.errorStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    System.err.println(line)
                }
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun disconnectFromWireGuard(): Boolean {
        return try {
            // Остановка WireGuard
            val stopWireGuard = ProcessBuilder("wg-quick", "down", "wg0").start()
            val exitCode = stopWireGuard.waitFor()

            if (exitCode == 0) {
                true
            } else {
                val reader = BufferedReader(InputStreamReader(stopWireGuard.errorStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    System.err.println(line)
                }
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
