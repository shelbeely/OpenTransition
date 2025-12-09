class Photo {
  final String id;
  final String type; // 'face' or 'body'
  final String imagePath; // Path to image or base64 data URL
  final DateTime date;
  final DateTime createdAt;

  Photo({
    required this.id,
    required this.type,
    required this.imagePath,
    required this.date,
    required this.createdAt,
  });

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'type': type,
      'imagePath': imagePath,
      'date': date.toIso8601String(),
      'createdAt': createdAt.toIso8601String(),
    };
  }

  factory Photo.fromJson(Map<String, dynamic> json) {
    return Photo(
      id: json['id'],
      type: json['type'],
      imagePath: json['imagePath'],
      date: DateTime.parse(json['date']),
      createdAt: DateTime.parse(json['createdAt']),
    );
  }

  Photo copyWith({
    String? id,
    String? type,
    String? imagePath,
    DateTime? date,
    DateTime? createdAt,
  }) {
    return Photo(
      id: id ?? this.id,
      type: type ?? this.type,
      imagePath: imagePath ?? this.imagePath,
      date: date ?? this.date,
      createdAt: createdAt ?? this.createdAt,
    );
  }
}
