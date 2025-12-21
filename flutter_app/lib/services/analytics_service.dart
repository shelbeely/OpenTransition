import 'package:firebase_analytics/firebase_analytics.dart';
import 'package:flutter/foundation.dart';

class AnalyticsService {
  final FirebaseAnalytics _analytics = FirebaseAnalytics.instance;

  // Screen tracking
  Future<void> logScreenView(String screenName) async {
    try {
      await _analytics.logScreenView(screenName: screenName);
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  // Photo events
  Future<void> logPhotoCapture(String photoType) async {
    try {
      await _analytics.logEvent(
        name: 'photo_capture',
        parameters: {
          'photo_type': photoType,
        },
      );
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logPhotoDelete() async {
    try {
      await _analytics.logEvent(name: 'photo_delete');
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logPhotoView() async {
    try {
      await _analytics.logEvent(name: 'photo_view');
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  // Milestone events
  Future<void> logMilestoneCreate(String milestoneType) async {
    try {
      await _analytics.logEvent(
        name: 'milestone_create',
        parameters: {
          'milestone_type': milestoneType,
        },
      );
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logMilestoneEdit() async {
    try {
      await _analytics.logEvent(name: 'milestone_edit');
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logMilestoneDelete() async {
    try {
      await _analytics.logEvent(name: 'milestone_delete');
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  // Backup events
  Future<void> logBackupCreate() async {
    try {
      await _analytics.logEvent(name: 'backup_create');
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logBackupRestore() async {
    try {
      await _analytics.logEvent(name: 'backup_restore');
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  // Settings events
  Future<void> logThemeChange(String themeMode) async {
    try {
      await _analytics.logEvent(
        name: 'theme_change',
        parameters: {
          'theme_mode': themeMode,
        },
      );
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logAppLockToggle(bool enabled) async {
    try {
      await _analytics.logEvent(
        name: 'app_lock_toggle',
        parameters: {
          'enabled': enabled,
        },
      );
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  // User properties
  Future<void> setUserProperty(String name, String value) async {
    try {
      await _analytics.setUserProperty(name: name, value: value);
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  // App events
  Future<void> logAppOpen() async {
    try {
      await _analytics.logAppOpen();
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logLogin(String method) async {
    try {
      await _analytics.logLogin(loginMethod: method);
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }

  Future<void> logSignUp(String method) async {
    try {
      await _analytics.logSignUp(signUpMethod: method);
    } catch (e) {
      debugPrint('Analytics error: $e');
    }
  }
}
