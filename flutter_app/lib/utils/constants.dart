// App-wide constants
class AppConstants {
  // App Info
  static const String appName = 'OpenTransition';
  static const String appVersion = '1.3.0';
  static const String appPlatform = 'Flutter';
  
  // Database
  static const String databaseName = 'opentransition.db';
  static const int databaseVersion = 1;
  
  // Storage Keys
  static const String keyLockEnabled = 'lock_enabled';
  static const String keyBiometricEnabled = 'biometric_enabled';
  static const String keyThemeMode = 'theme_mode';
  static const String keyPinHash = 'pin_hash';
  static const String keyFirstLaunch = 'first_launch';
  
  // Photo Types
  static const String photoTypeFace = 'face';
  static const String photoTypeBody = 'body';
  static const String photoTypeCustom = 'custom';
  
  static const List<String> photoTypes = [
    photoTypeFace,
    photoTypeBody,
    photoTypeCustom,
  ];
  
  // Milestone Types
  static const String milestoneTypeGeneral = 'general';
  static const String milestoneTypeMedical = 'medical';
  static const String milestoneTypeSocial = 'social';
  static const String milestoneTypeLegal = 'legal';
  static const String milestoneTypePersonal = 'personal';
  
  static const List<String> milestoneTypes = [
    milestoneTypeGeneral,
    milestoneTypeMedical,
    milestoneTypeSocial,
    milestoneTypeLegal,
    milestoneTypePersonal,
  ];
  
  // Theme Modes
  static const String themeModeLight = 'light';
  static const String themeModeDark = 'dark';
  static const String themeModeSystem = 'system';
  
  // Analytics Events
  static const String eventPhotoCapture = 'photo_capture';
  static const String eventPhotoDelete = 'photo_delete';
  static const String eventPhotoView = 'photo_view';
  static const String eventMilestoneCreate = 'milestone_create';
  static const String eventMilestoneEdit = 'milestone_edit';
  static const String eventMilestoneDelete = 'milestone_delete';
  static const String eventBackupCreate = 'backup_create';
  static const String eventBackupRestore = 'backup_restore';
  static const String eventThemeChange = 'theme_change';
  static const String eventAppLockToggle = 'app_lock_toggle';
  
  // Backup
  static const String backupVersion = '1.0';
  static const String backupFilePrefix = 'opentransition_backup_';
  static const String backupFileExtension = '.json';
  
  // UI
  static const double paddingSmall = 8.0;
  static const double paddingMedium = 16.0;
  static const double paddingLarge = 24.0;
  
  static const double borderRadiusSmall = 4.0;
  static const double borderRadiusMedium = 8.0;
  static const double borderRadiusLarge = 12.0;
  
  // Grid
  static const int gridColumnsMin = 2;
  static const int gridColumnsDefault = 3;
  static const int gridColumnsMax = 4;
  
  // Responsive Breakpoints
  static const double breakpointMobile = 650.0;
  static const double breakpointTablet = 1100.0;
  
  // Limits
  static const int maxPhotosPerLoad = 100;
  static const int maxMilestonesPerLoad = 100;
  static const int maxBackupsToKeep = 10;
}
