import 'package:json_annotation/json_annotation.dart';

part 'milestone.g.dart';

@JsonSerializable()
class Milestone {
  final String id;
  final String title;
  final String? description;
  final int date;
  final String type;
  final int createdAt;
  final int updatedAt;
  
  Milestone({
    required this.id,
    required this.title,
    this.description,
    required this.date,
    required this.type,
    required this.createdAt,
    required this.updatedAt,
  });
  
  factory Milestone.fromJson(Map<String, dynamic> json) => 
      _$MilestoneFromJson(json);
  Map<String, dynamic> toJson() => _$MilestoneToJson(this);
  
  Milestone copyWith({
    String? id,
    String? title,
    String? description,
    int? date,
    String? type,
    int? createdAt,
    int? updatedAt,
  }) {
    return Milestone(
      id: id ?? this.id,
      title: title ?? this.title,
      description: description ?? this.description,
      date: date ?? this.date,
      type: type ?? this.type,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}
