import 'dart:convert';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'package:flutter/foundation.dart';
import '../models/photo.dart';
import '../models/milestone.dart';
import '../services/photo_service.dart';
import '../services/milestone_service.dart';
import 'package:intl/intl.dart';

class BackupService {
  final PhotoService _photoService;
  final MilestoneService _milestoneService;

  BackupService(this._photoService, this._milestoneService);

  Future<String> createBackup() async {
    try {
      // Get all data
      final photos = await _photoService.getPhotos();
      final milestones = await _milestoneService.getMilestones();

      // Create backup data structure
      final backupData = {
        'version': '1.0',
        'created_at': DateTime.now().toIso8601String(),
        'app_version': '1.3.0',
        'platform': 'flutter',
        'photos': photos.map((p) => p.toJson()).toList(),
        'milestones': milestones.map((m) => m.toJson()).toList(),
      };

      // Convert to JSON
      final jsonString = jsonEncode(backupData);

      // Save to file
      final directory = await getApplicationDocumentsDirectory();
      final timestamp = DateFormat('yyyyMMdd_HHmmss').format(DateTime.now());
      final fileName = 'opentransition_backup_$timestamp.json';
      final file = File('${directory.path}/$fileName');
      
      await file.writeAsString(jsonString);

      return file.path;
    } catch (e) {
      debugPrint('Error creating backup: $e');
      rethrow;
    }
  }

  Future<Map<String, dynamic>> restoreBackup(String filePath) async {
    try {
      final file = File(filePath);
      final jsonString = await file.readAsString();
      final backupData = jsonDecode(jsonString) as Map<String, dynamic>;

      // Validate backup version
      if (backupData['version'] != '1.0') {
        throw Exception('Unsupported backup version');
      }

      // Count items
      final photoCount = (backupData['photos'] as List).length;
      final milestoneCount = (backupData['milestones'] as List).length;

      return {
        'success': true,
        'photo_count': photoCount,
        'milestone_count': milestoneCount,
        'created_at': backupData['created_at'],
      };
    } catch (e) {
      debugPrint('Error restoring backup: $e');
      rethrow;
    }
  }

  Future<String> exportToJson() async {
    return await createBackup();
  }

  Future<List<String>> listBackups() async {
    try {
      final directory = await getApplicationDocumentsDirectory();
      final backupFiles = directory
          .listSync()
          .where((file) =>
              file.path.contains('opentransition_backup_') &&
              file.path.endsWith('.json'))
          .map((file) => file.path)
          .toList();

      return backupFiles;
    } catch (e) {
      debugPrint('Error listing backups: $e');
      return [];
    }
  }

  Future<void> deleteBackup(String filePath) async {
    try {
      final file = File(filePath);
      if (await file.exists()) {
        await file.delete();
      }
    } catch (e) {
      debugPrint('Error deleting backup: $e');
      rethrow;
    }
  }

  Future<Map<String, dynamic>> getBackupInfo(String filePath) async {
    try {
      final file = File(filePath);
      final jsonString = await file.readAsString();
      final backupData = jsonDecode(jsonString) as Map<String, dynamic>;

      return {
        'created_at': backupData['created_at'],
        'app_version': backupData['app_version'],
        'photo_count': (backupData['photos'] as List).length,
        'milestone_count': (backupData['milestones'] as List).length,
      };
    } catch (e) {
      debugPrint('Error getting backup info: $e');
      return {};
    }
  }
}
