import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text("My VPN App")),
        body: VPNControl(),
      ),
    );
  }
}

class VPNControl extends StatefulWidget {
  @override
  _VPNControlState createState() => _VPNControlState();
}

class _VPNControlState extends State<VPNControl> {
  static const platform = MethodChannel('com.example.my_vpn_app/wireguard');

  bool _isConnected = false;

  Future<void> _connect() async {
    try {
      final bool result = await platform.invokeMethod('connect', {
        'privateKey': 'yK32UskjKKtSrfaT38tmMW8ICLeX1pCSln5RHyPAPF8=',
        'publicKey': 'GJ3ozcyXDRsvNmir589sWJTk6RsrzjrGUHNZFICPRmo=',
        'endpoint': '31.172.78.128:51820',
        'address': '10.0.0.2/24',
        'dns': '8.8.8.8',
        'allowedIPs': '0.0.0.0/0, ::/0',
        'persistentKeepalive': 25,
      });
      setState(() {
        _isConnected = result;
      });
    } on PlatformException catch (e) {
      print("Failed to connect: '${e.message}'.");
    }
  }

  Future<void> _disconnect() async {
    try {
      final bool result = await platform.invokeMethod('disconnect');
      setState(() {
        _isConnected = !result;
      });
    } on PlatformException catch (e) {
      print("Failed to disconnect: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        ElevatedButton(
          onPressed: _isConnected ? null : _connect,
          child: Text("Подключиться"),
        ),
        ElevatedButton(
          onPressed: _isConnected ? _disconnect : null,
          child: Text("Отключиться"),
        ),
      ],
    );
  }
}

