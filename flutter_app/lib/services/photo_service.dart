import 'package:camera/camera.dart';
import 'package:flutter/foundation.dart';
import 'package:uuid/uuid.dart';
import 'dart:io';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import '../models/photo.dart';
import 'database_service.dart';

class PhotoService {
  final DatabaseService _databaseService;
  final _uuid = const Uuid();

  PhotoService(this._databaseService);

  Future<String> get _photosDirectory async {
    final appDir = await getApplicationDocumentsDirectory();
    final photosDir = Directory(join(appDir.path, 'photos'));
    if (!await photosDir.exists()) {
      await photosDir.create(recursive: true);
    }
    return photosDir.path;
  }

  Future<Photo> savePhoto({
    required String filePath,
    required String type,
    String? description,
  }) async {
    final photosDir = await _photosDirectory;
    final fileName = '${_uuid.v4()}.jpg';
    final newPath = join(photosDir, fileName);

    // Copy file to app directory
    await File(filePath).copy(newPath);

    final now = DateTime.now().millisecondsSinceEpoch;
    final photo = Photo(
      id: _uuid.v4(),
      type: type,
      timestamp: now,
      filePath: newPath,
      description: description,
      createdAt: now,
      updatedAt: now,
    );

    // Save to database
    await _databaseService.database.insert('photos', photo.toJson());

    return photo;
  }

  Future<List<Photo>> getPhotos({String? type, int? limit}) async {
    final db = _databaseService.database;
    String query = 'SELECT * FROM photos';
    List<dynamic> args = [];

    if (type != null) {
      query += ' WHERE type = ?';
      args.add(type);
    }

    query += ' ORDER BY timestamp DESC';

    if (limit != null) {
      query += ' LIMIT ?';
      args.add(limit);
    }

    final results = await db.rawQuery(query, args);
    return results.map((json) => Photo.fromJson(json)).toList();
  }

  Future<Photo?> getPhotoById(String id) async {
    final db = _databaseService.database;
    final results = await db.query(
      'photos',
      where: 'id = ?',
      whereArgs: [id],
    );

    if (results.isEmpty) return null;
    return Photo.fromJson(results.first);
  }

  Future<void> deletePhoto(String id) async {
    final photo = await getPhotoById(id);
    if (photo != null) {
      // Delete file
      final file = File(photo.filePath);
      if (await file.exists()) {
        await file.delete();
      }

      // Delete from database
      await _databaseService.database.delete(
        'photos',
        where: 'id = ?',
        whereArgs: [id],
      );
    }
  }

  Future<void> updatePhoto(Photo photo) async {
    final updated = photo.copyWith(
      updatedAt: DateTime.now().millisecondsSinceEpoch,
    );

    await _databaseService.database.update(
      'photos',
      updated.toJson(),
      where: 'id = ?',
      whereArgs: [photo.id],
    );
  }

  Future<int> getPhotoCount({String? type}) async {
    final db = _databaseService.database;
    String query = 'SELECT COUNT(*) FROM photos';
    List<dynamic> args = [];

    if (type != null) {
      query += ' WHERE type = ?';
      args.add(type);
    }

    final result = await db.rawQuery(query, args);
    return Sqflite.firstIntValue(result) ?? 0;
  }
}

// Import required for Sqflite.firstIntValue
import 'package:sqflite/sqflite.dart' show Sqflite;
