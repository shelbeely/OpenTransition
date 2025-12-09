import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:idb_shim/idb.dart' as idb;
import 'package:idb_shim/idb_browser.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart' as path;
import '../models/photo.dart';
import '../models/milestone.dart';

/// Database service that works on both web (IndexedDB) and mobile (SQLite)
/// Implements full CRUD operations for photos and milestones
class DatabaseService {
  // Singleton pattern
  static final DatabaseService _instance = DatabaseService._internal();
  factory DatabaseService() => _instance;
  DatabaseService._internal();

  bool _initialized = false;
  
  // Web (IndexedDB)
  idb.Database? _idbDatabase;
  
  // Mobile (SQLite)
  Database? _sqlDatabase;
  
  static const String dbName = 'opentransition';
  static const int dbVersion = 1;
  static const String photosStore = 'photos';
  static const String milestonesStore = 'milestones';

  Future<void> init() async {
    if (_initialized) return;
    
    if (kIsWeb) {
      await _initWeb();
    } else {
      await _initMobile();
    }
    
    _initialized = true;
  }

  Future<void> _initWeb() async {
    final idbFactory = getIdbFactory()!;
    
    _idbDatabase = await idbFactory.open(
      dbName,
      version: dbVersion,
      onUpgradeNeeded: (idb.VersionChangeEvent event) {
        final db = event.database;
        
        // Create photos store
        if (!db.objectStoreNames.contains(photosStore)) {
          db.createObjectStore(photosStore, keyPath: 'id');
        }
        
        // Create milestones store
        if (!db.objectStoreNames.contains(milestonesStore)) {
          db.createObjectStore(milestonesStore, keyPath: 'id');
        }
      },
    );
    
    if (kDebugMode) {
      print('IndexedDB initialized for web');
    }
  }

  Future<void> _initMobile() async {
    final dbPath = await getDatabasesPath();
    final dbFilePath = path.join(dbPath, '$dbName.db');
    
    _sqlDatabase = await openDatabase(
      dbFilePath,
      version: dbVersion,
      onCreate: (Database db, int version) async {
        // Create photos table
        await db.execute('''
          CREATE TABLE photos (
            id TEXT PRIMARY KEY,
            type TEXT NOT NULL,
            imageData TEXT NOT NULL,
            dateTime TEXT NOT NULL,
            notes TEXT
          )
        ''');
        
        // Create milestones table
        await db.execute('''
          CREATE TABLE milestones (
            id TEXT PRIMARY KEY,
            title TEXT NOT NULL,
            description TEXT,
            date TEXT NOT NULL,
            achieved INTEGER NOT NULL DEFAULT 0
          )
        ''');
      },
    );
    
    if (kDebugMode) {
      print('SQLite initialized for mobile');
    }
  }

  // PHOTOS CRUD OPERATIONS
  
  Future<List<Photo>> getPhotos() async {
    if (kIsWeb) {
      return await _getPhotosWeb();
    } else {
      return await _getPhotosMobile();
    }
  }

  Future<List<Photo>> _getPhotosWeb() async {
    final txn = _idbDatabase!.transaction(photosStore, idb.idbModeReadOnly);
    final store = txn.objectStore(photosStore);
    final records = await store.getAll();
    await txn.completed;
    
    return records.map((e) => Photo.fromJson(Map<String, dynamic>.from(e as Map))).toList()
      ..sort((a, b) => b.dateTime.compareTo(a.dateTime));
  }

  Future<List<Photo>> _getPhotosMobile() async {
    final List<Map<String, dynamic>> maps = await _sqlDatabase!.query(
      'photos',
      orderBy: 'dateTime DESC',
    );
    
    return maps.map((map) => Photo.fromJson(map)).toList();
  }

  Future<List<Photo>> getPhotosByType(String type) async {
    if (kIsWeb) {
      return await _getPhotosByTypeWeb(type);
    } else {
      return await _getPhotosByTypeMobile(type);
    }
  }

  Future<List<Photo>> _getPhotosByTypeWeb(String type) async {
    final allPhotos = await _getPhotosWeb();
    return allPhotos.where((photo) => photo.type == type).toList();
  }

  Future<List<Photo>> _getPhotosByTypeMobile(String type) async {
    final List<Map<String, dynamic>> maps = await _sqlDatabase!.query(
      'photos',
      where: 'type = ?',
      whereArgs: [type],
      orderBy: 'dateTime DESC',
    );
    
    return maps.map((map) => Photo.fromJson(map)).toList();
  }

  Future<void> addPhoto(Photo photo) async {
    if (kIsWeb) {
      await _addPhotoWeb(photo);
    } else {
      await _addPhotoMobile(photo);
    }
  }

  Future<void> _addPhotoWeb(Photo photo) async {
    final txn = _idbDatabase!.transaction(photosStore, idb.idbModeReadWrite);
    final store = txn.objectStore(photosStore);
    await store.put(photo.toJson());
    await txn.completed;
  }

  Future<void> _addPhotoMobile(Photo photo) async {
    await _sqlDatabase!.insert(
      'photos',
      photo.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<void> updatePhoto(Photo photo) async {
    // Update is same as add with replace
    await addPhoto(photo);
  }

  Future<void> deletePhoto(String id) async {
    if (kIsWeb) {
      await _deletePhotoWeb(id);
    } else {
      await _deletePhotoMobile(id);
    }
  }

  Future<void> _deletePhotoWeb(String id) async {
    final txn = _idbDatabase!.transaction(photosStore, idb.idbModeReadWrite);
    final store = txn.objectStore(photosStore);
    await store.delete(id);
    await txn.completed;
  }

  Future<void> _deletePhotoMobile(String id) async {
    await _sqlDatabase!.delete(
      'photos',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  // MILESTONES CRUD OPERATIONS
  
  Future<List<Milestone>> getMilestones() async {
    if (kIsWeb) {
      return await _getMilestonesWeb();
    } else {
      return await _getMilestonesMobile();
    }
  }

  Future<List<Milestone>> _getMilestonesWeb() async {
    final txn = _idbDatabase!.transaction(milestonesStore, idb.idbModeReadOnly);
    final store = txn.objectStore(milestonesStore);
    final records = await store.getAll();
    await txn.completed;
    
    return records.map((e) => Milestone.fromJson(Map<String, dynamic>.from(e as Map))).toList()
      ..sort((a, b) => b.date.compareTo(a.date));
  }

  Future<List<Milestone>> _getMilestonesMobile() async {
    final List<Map<String, dynamic>> maps = await _sqlDatabase!.query(
      'milestones',
      orderBy: 'date DESC',
    );
    
    return maps.map((map) => Milestone.fromJson(map)).toList();
  }

  Future<void> addMilestone(Milestone milestone) async {
    if (kIsWeb) {
      await _addMilestoneWeb(milestone);
    } else {
      await _addMilestoneMobile(milestone);
    }
  }

  Future<void> _addMilestoneWeb(Milestone milestone) async {
    final txn = _idbDatabase!.transaction(milestonesStore, idb.idbModeReadWrite);
    final store = txn.objectStore(milestonesStore);
    await store.put(milestone.toJson());
    await txn.completed;
  }

  Future<void> _addMilestoneMobile(Milestone milestone) async {
    await _sqlDatabase!.insert(
      'milestones',
      milestone.toJson(),
      conflictAlgorithm: ConflictAlgorithm.replace,
    );
  }

  Future<void> updateMilestone(Milestone milestone) async {
    // Update is same as add with replace
    await addMilestone(milestone);
  }

  Future<void> deleteMilestone(String id) async {
    if (kIsWeb) {
      await _deleteMilestoneWeb(id);
    } else {
      await _deleteMilestoneMobile(id);
    }
  }

  Future<void> _deleteMilestoneWeb(String id) async {
    final txn = _idbDatabase!.transaction(milestonesStore, idb.idbModeReadWrite);
    final store = txn.objectStore(milestonesStore);
    await store.delete(id);
    await txn.completed;
  }

  Future<void> _deleteMilestoneMobile(String id) async {
    await _sqlDatabase!.delete(
      'milestones',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  // UTILITY OPERATIONS
  
  Future<void> clearAllData() async {
    if (kIsWeb) {
      await _clearAllDataWeb();
    } else {
      await _clearAllDataMobile();
    }
  }

  Future<void> _clearAllDataWeb() async {
    // Clear photos
    var txn = _idbDatabase!.transaction(photosStore, idb.idbModeReadWrite);
    var store = txn.objectStore(photosStore);
    await store.clear();
    await txn.completed;
    
    // Clear milestones
    txn = _idbDatabase!.transaction(milestonesStore, idb.idbModeReadWrite);
    store = txn.objectStore(milestonesStore);
    await store.clear();
    await txn.completed;
  }

  Future<void> _clearAllDataMobile() async {
    await _sqlDatabase!.delete('photos');
    await _sqlDatabase!.delete('milestones');
  }
  
  Future<void> close() async {
    if (kIsWeb) {
      _idbDatabase?.close();
    } else {
      await _sqlDatabase?.close();
    }
    _initialized = false;
  }
}
