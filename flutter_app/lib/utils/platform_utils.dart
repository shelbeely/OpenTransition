import 'package:flutter/foundation.dart' show kIsWeb;
import 'dart:io' show Platform;

class PlatformUtils {
  static bool get isWeb => kIsWeb;
  
  static bool get isMobile => !kIsWeb && (isAndroid || isIOS);
  
  static bool get isDesktop => !kIsWeb && (isWindows || isMacOS || isLinux);
  
  static bool get isAndroid {
    try {
      return !kIsWeb && Platform.isAndroid;
    } catch (e) {
      return false;
    }
  }
  
  static bool get isIOS {
    try {
      return !kIsWeb && Platform.isIOS;
    } catch (e) {
      return false;
    }
  }
  
  static bool get isWindows {
    try {
      return !kIsWeb && Platform.isWindows;
    } catch (e) {
      return false;
    }
  }
  
  static bool get isMacOS {
    try {
      return !kIsWeb && Platform.isMacOS;
    } catch (e) {
      return false;
    }
  }
  
  static bool get isLinux {
    try {
      return !kIsWeb && Platform.isLinux;
    } catch (e) {
      return false;
    }
  }
  
  static String get platformName {
    if (isWeb) return 'Web';
    if (isAndroid) return 'Android';
    if (isIOS) return 'iOS';
    if (isWindows) return 'Windows';
    if (isMacOS) return 'macOS';
    if (isLinux) return 'Linux';
    return 'Unknown';
  }
  
  static bool get supportsCameraCapture => isMobile || isWindows || isMacOS;
  
  static bool get supportsBiometrics => isMobile || isWindows || isMacOS;
  
  static bool get supportsFileSystemAccess => !isWeb;
}
