import 'package:flutter/foundation.dart';
import '../models/photo.dart';
import '../services/photo_service.dart';

class PhotoProvider extends ChangeNotifier {
  final PhotoService _photoService;
  List<Photo> _photos = [];
  bool _isLoading = false;
  String? _error;
  String? _selectedType;

  PhotoProvider(this._photoService);

  List<Photo> get photos => _photos;
  bool get isLoading => _isLoading;
  String? get error => _error;
  String? get selectedType => _selectedType;

  Future<void> loadPhotos({String? type}) async {
    _isLoading = true;
    _error = null;
    _selectedType = type;
    notifyListeners();

    try {
      _photos = await _photoService.getPhotos(type: type);
    } catch (e) {
      _error = e.toString();
      debugPrint('Error loading photos: $e');
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> addPhoto({
    required String filePath,
    required String type,
    String? description,
  }) async {
    try {
      await _photoService.savePhoto(
        filePath: filePath,
        type: type,
        description: description,
      );
      await loadPhotos(type: _selectedType);
    } catch (e) {
      _error = e.toString();
      debugPrint('Error adding photo: $e');
      notifyListeners();
    }
  }

  Future<void> deletePhoto(String id) async {
    try {
      await _photoService.deletePhoto(id);
      _photos.removeWhere((photo) => photo.id == id);
      notifyListeners();
    } catch (e) {
      _error = e.toString();
      debugPrint('Error deleting photo: $e');
      notifyListeners();
    }
  }

  Future<void> updatePhoto(Photo photo) async {
    try {
      await _photoService.updatePhoto(photo);
      final index = _photos.indexWhere((p) => p.id == photo.id);
      if (index != -1) {
        _photos[index] = photo;
        notifyListeners();
      }
    } catch (e) {
      _error = e.toString();
      debugPrint('Error updating photo: $e');
      notifyListeners();
    }
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}
