import 'dart:convert';
import 'package:archive/archive.dart';
import '../models/photo.dart';
import '../models/milestone.dart';

/// Service for importing and exporting .ttbackup files
/// Compatible with the Android app backup format
class BackupService {
  /// Export data to .ttbackup format (ZIP file)
  Future<List<int>> exportToTtbackup({
    required List<Photo> photos,
    required List<Milestone> milestones,
  }) async {
    final archive = Archive();

    // Create data.json
    final data = {
      'photos': photos.map((p) => p.toJson()).toList(),
      'milestones': milestones.map((m) => m.toJson()).toList(),
      'version': '1.0.0',
      'exportDate': DateTime.now().toIso8601String(),
    };
    
    final dataJson = jsonEncode(data);
    archive.addFile(ArchiveFile('data.json', dataJson.length, dataJson.codeUnits));

    // Add image files
    // TODO: Add actual image files to the archive
    // For each photo, extract base64 data and add as file

    // Encode to ZIP
    final zipEncoder = ZipEncoder();
    final zipData = zipEncoder.encode(archive);
    
    return zipData ?? [];
  }

  /// Import data from .ttbackup format (ZIP file)
  Future<Map<String, dynamic>> importFromTtbackup(List<int> zipData) async {
    final archive = ZipDecoder().decodeBytes(zipData);

    // Find and parse data.json
    final dataFile = archive.files.firstWhere(
      (file) => file.name == 'data.json',
      orElse: () => throw Exception('data.json not found in backup'),
    );

    final dataContent = utf8.decode(dataFile.content as List<int>);
    final data = jsonDecode(dataContent) as Map<String, dynamic>;

    // Parse photos
    final photos = (data['photos'] as List)
        .map((json) => Photo.fromJson(json as Map<String, dynamic>))
        .toList();

    // Parse milestones
    final milestones = (data['milestones'] as List)
        .map((json) => Milestone.fromJson(json as Map<String, dynamic>))
        .toList();

    // TODO: Extract image files and convert to appropriate format
    // For web: convert to base64 data URLs
    // For mobile: save to local storage

    return {
      'photos': photos,
      'milestones': milestones,
      'version': data['version'],
      'exportDate': data['exportDate'],
    };
  }
}
