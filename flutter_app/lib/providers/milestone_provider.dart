import 'package:flutter/foundation.dart';
import '../models/milestone.dart';
import '../services/milestone_service.dart';

class MilestoneProvider extends ChangeNotifier {
  final MilestoneService _milestoneService;
  List<Milestone> _milestones = [];
  bool _isLoading = false;
  String? _error;

  MilestoneProvider(this._milestoneService);

  List<Milestone> get milestones => _milestones;
  bool get isLoading => _isLoading;
  String? get error => _error;

  Future<void> loadMilestones() async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      _milestones = await _milestoneService.getMilestones();
    } catch (e) {
      _error = e.toString();
      debugPrint('Error loading milestones: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> createMilestone({
    required String title,
    String? description,
    required DateTime date,
    required String type,
  }) async {
    try {
      await _milestoneService.createMilestone(
        title: title,
        description: description,
        date: date,
        type: type,
      );
      await loadMilestones();
    } catch (e) {
      _error = e.toString();
      debugPrint('Error creating milestone: $e');
      notifyListeners();
    }
  }

  Future<void> updateMilestone(Milestone milestone) async {
    try {
      await _milestoneService.updateMilestone(milestone);
      final index = _milestones.indexWhere((m) => m.id == milestone.id);
      if (index != -1) {
        _milestones[index] = milestone;
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      debugPrint('Error updating milestone: $e');
      notifyListeners();
    }
  }

  Future<void> deleteMilestone(String id) async {
    try {
      await _milestoneService.deleteMilestone(id);
      _milestones.removeWhere((milestone) => milestone.id == id);
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      debugPrint('Error deleting milestone: $e');
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
