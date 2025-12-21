import 'package:flutter/material.dart';
import '../services/storage_service.dart';

class SettingsProvider extends ChangeNotifier {
  final StorageService _storageService;

  // Settings
  bool _lockEnabled = false;
  bool _biometricEnabled = false;
  bool _isDarkMode = false;
  String _themeMode = 'system'; // 'light', 'dark', 'system'

  SettingsProvider(this._storageService) {
    _loadSettings();
  }

  // Getters
  bool get lockEnabled => _lockEnabled;
  bool get biometricEnabled => _biometricEnabled;
  bool get isDarkMode => _isDarkMode;
  String get themeMode => _themeMode;

  ThemeMode get themeModeEnum {
    switch (_themeMode) {
      case 'light':
        return ThemeMode.light;
      case 'dark':
        return ThemeMode.dark;
      default:
        return ThemeMode.system;
    }
  }

  Future<void> _loadSettings() async {
    _lockEnabled = _storageService.getBool('lock_enabled') ?? false;
    _biometricEnabled = _storageService.getBool('biometric_enabled') ?? false;
    _themeMode = _storageService.getString('theme_mode') ?? 'system';
    
    // Determine if dark mode based on theme mode and system
    // This is simplified - in real app would check system theme
    _isDarkMode = _themeMode == 'dark';
    
    notifyListeners();
  }

  Future<void> setLockEnabled(bool enabled) async {
    _lockEnabled = enabled;
    await _storageService.setBool('lock_enabled', enabled);
    notifyListeners();
  }

  Future<void> setBiometricEnabled(bool enabled) async {
    _biometricEnabled = enabled;
    await _storageService.setBool('biometric_enabled', enabled);
    notifyListeners();
  }

  Future<void> setThemeMode(String mode) async {
    _themeMode = mode;
    _isDarkMode = mode == 'dark';
    await _storageService.setString('theme_mode', mode);
    notifyListeners();
  }

  Future<void> clearAllData() async {
    await _storageService.clear();
    await _loadSettings();
  }
}
