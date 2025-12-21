import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class StorageService {
  final FlutterSecureStorage _secureStorage = const FlutterSecureStorage();
  SharedPreferences? _prefs;
  
  Future<void> initialize() async {
    _prefs = await SharedPreferences.getInstance();
  }
  
  // Secure storage for sensitive data
  Future<void> writeSecure(String key, String value) async {
    await _secureStorage.write(key: key, value: value);
  }
  
  Future<String?> readSecure(String key) async {
    return await _secureStorage.read(key: key);
  }
  
  Future<void> deleteSecure(String key) async {
    await _secureStorage.delete(key: key);
  }
  
  Future<void> deleteAllSecure() async {
    await _secureStorage.deleteAll();
  }
  
  // Regular preferences
  Future<bool> setBool(String key, bool value) async {
    _prefs ??= await SharedPreferences.getInstance();
    return await _prefs!.setBool(key, value);
  }
  
  bool? getBool(String key) {
    return _prefs?.getBool(key);
  }
  
  Future<bool> setString(String key, String value) async {
    _prefs ??= await SharedPreferences.getInstance();
    return await _prefs!.setString(key, value);
  }
  
  String? getString(String key) {
    return _prefs?.getString(key);
  }
  
  Future<bool> setInt(String key, int value) async {
    _prefs ??= await SharedPreferences.getInstance();
    return await _prefs!.setInt(key, value);
  }
  
  int? getInt(String key) {
    return _prefs?.getInt(key);
  }
  
  Future<bool> remove(String key) async {
    _prefs ??= await SharedPreferences.getInstance();
    return await _prefs!.remove(key);
  }
  
  Future<bool> clear() async {
    _prefs ??= await SharedPreferences.getInstance();
    return await _prefs!.clear();
  }
}
