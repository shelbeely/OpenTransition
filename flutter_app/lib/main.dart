import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:firebase_core/firebase_core.dart';
import 'screens/splash_screen.dart';
import 'services/database_service.dart';
import 'services/auth_service.dart';
import 'services/storage_service.dart';
import 'services/photo_service.dart';
import 'services/milestone_service.dart';
import 'providers/photo_provider.dart';
import 'providers/milestone_provider.dart';
import 'providers/settings_provider.dart';
import 'theme/app_theme.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Initialize Firebase (with error handling for development)
  try {
    await Firebase.initializeApp();
  } catch (e) {
    debugPrint('Firebase initialization skipped: $e');
  }
  
  // Initialize services
  final databaseService = DatabaseService();
  await databaseService.initialize();
  
  final authService = AuthService();
  final storageService = StorageService();
  await storageService.initialize();
  
  final photoService = PhotoService(databaseService);
  final milestoneService = MilestoneService(databaseService);
  
  runApp(
    MultiProvider(
      providers: [
        // Services
        Provider<DatabaseService>.value(value: databaseService),
        Provider<StorageService>.value(value: storageService),
        Provider<PhotoService>.value(value: photoService),
        Provider<MilestoneService>.value(value: milestoneService),
        
        // Auth (ChangeNotifier)
        ChangeNotifierProvider<AuthService>.value(value: authService),
        
        // Providers (ChangeNotifier)
        ChangeNotifierProvider<PhotoProvider>(
          create: (_) => PhotoProvider(photoService),
        ),
        ChangeNotifierProvider<MilestoneProvider>(
          create: (_) => MilestoneProvider(milestoneService),
        ),
        ChangeNotifierProvider<SettingsProvider>(
          create: (_) => SettingsProvider(storageService),
        ),
      ],
      child: const OpenTransitionApp(),
    ),
  );
}

class OpenTransitionApp extends StatelessWidget {
  const OpenTransitionApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<SettingsProvider>(
      builder: (context, settings, child) {
        return MaterialApp(
          title: 'OpenTransition',
          theme: AppTheme.lightTheme,
          darkTheme: AppTheme.darkTheme,
          themeMode: settings.themeModeEnum,
          home: const SplashScreen(),
          debugShowCheckedModeBanner: false,
        );
      },
    );
  }
}
