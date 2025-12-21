import 'package:uuid/uuid.dart';
import '../models/milestone.dart';
import 'database_service.dart';

class MilestoneService {
  final DatabaseService _databaseService;
  final _uuid = const Uuid();

  MilestoneService(this._databaseService);

  Future<Milestone> createMilestone({
    required String title,
    String? description,
    required DateTime date,
    required String type,
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    final milestone = Milestone(
      id: _uuid.v4(),
      title: title,
      description: description,
      date: date.millisecondsSinceEpoch,
      type: type,
      createdAt: now,
      updatedAt: now,
    );

    await _databaseService.database.insert('milestones', milestone.toJson());
    return milestone;
  }

  Future<List<Milestone>> getMilestones({int? limit}) async {
    final db = _databaseService.database;
    String query = 'SELECT * FROM milestones ORDER BY date DESC';

    if (limit != null) {
      query += ' LIMIT ?';
    }

    final results = limit != null
        ? await db.rawQuery(query, [limit])
        : await db.rawQuery(query);

    return results.map((json) => Milestone.fromJson(json)).toList();
  }

  Future<Milestone?> getMilestoneById(String id) async {
    final db = _databaseService.database;
    final results = await db.query(
      'milestones',
      where: 'id = ?',
      whereArgs: [id],
    );

    if (results.isEmpty) return null;
    return Milestone.fromJson(results.first);
  }

  Future<void> updateMilestone(Milestone milestone) async {
    final updated = milestone.copyWith(
      updatedAt: DateTime.now().millisecondsSinceEpoch,
    );

    await _databaseService.database.update(
      'milestones',
      updated.toJson(),
      where: 'id = ?',
      whereArgs: [milestone.id],
    );
  }

  Future<void> deleteMilestone(String id) async {
    await _databaseService.database.delete(
      'milestones',
      where: 'id = ?',
      whereArgs: [id],
    );
  }

  Future<int> getMilestoneCount() async {
    final db = _databaseService.database;
    final result = await db.rawQuery('SELECT COUNT(*) FROM milestones');
    return Sqflite.firstIntValue(result) ?? 0;
  }

  Future<List<Milestone>> getMilestonesByDateRange(
    DateTime start,
    DateTime end,
  ) async {
    final db = _databaseService.database;
    final results = await db.query(
      'milestones',
      where: 'date >= ? AND date <= ?',
      whereArgs: [
        start.millisecondsSinceEpoch,
        end.millisecondsSinceEpoch,
      ],
      orderBy: 'date DESC',
    );

    return results.map((json) => Milestone.fromJson(json)).toList();
  }

  Future<List<Milestone>> getMilestonesByType(String type) async {
    final db = _databaseService.database;
    final results = await db.query(
      'milestones',
      where: 'type = ?',
      whereArgs: [type],
      orderBy: 'date DESC',
    );

    return results.map((json) => Milestone.fromJson(json)).toList();
  }
}

// Import required for Sqflite.firstIntValue
import 'package:sqflite/sqflite.dart' show Sqflite;
