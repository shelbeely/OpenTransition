import 'package:json_annotation/json_annotation.dart';

part 'photo.g.dart';

@JsonSerializable()
class Photo {
  final String id;
  final String type; // face, body, custom
  final int timestamp;
  final String filePath;
  final String? thumbnailPath;
  final String? description;
  final int createdAt;
  final int updatedAt;
  
  Photo({
    required this.id,
    required this.type,
    required this.timestamp,
    required this.filePath,
    this.thumbnailPath,
    this.description,
    required this.createdAt,
    required this.updatedAt,
  });
  
  factory Photo.fromJson(Map<String, dynamic> json) => _$PhotoFromJson(json);
  Map<String, dynamic> toJson() => _$PhotoToJson(this);
  
  Photo copyWith({
    String? id,
    String? type,
    int? timestamp,
    String? filePath,
    String? thumbnailPath,
    String? description,
    int? createdAt,
    int? updatedAt,
  }) {
    return Photo(
      id: id ?? this.id,
      type: type ?? this.type,
      timestamp: timestamp ?? this.timestamp,
      filePath: filePath ?? this.filePath,
      thumbnailPath: thumbnailPath ?? this.thumbnailPath,
      description: description ?? this.description,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
