import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'dart:io';
import '../providers/photo_provider.dart';
import '../models/photo.dart';
import 'photo_detail_screen.dart';

class TimelineScreen extends StatefulWidget {
  const TimelineScreen({super.key});

  @override
  State<TimelineScreen> createState() => _TimelineScreenState();
}

class _TimelineScreenState extends State<TimelineScreen> {
  String? _selectedType;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<PhotoProvider>(context, listen: false).loadPhotos();
    });
  }

  Map<String, List<Photo>> _groupPhotosByMonth(List<Photo> photos) {
    final Map<String, List<Photo>> grouped = {};
    
    for (final photo in photos) {
      final date = DateTime.fromMillisecondsSinceEpoch(photo.timestamp);
      final monthKey = DateFormat('MMMM yyyy').format(date);
      
      if (!grouped.containsKey(monthKey)) {
        grouped[monthKey] = [];
      }
      grouped[monthKey]!.add(photo);
    }
    
    return grouped;
  }

  void _showFilterDialog() {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Filter by Type'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ListTile(
              title: const Text('All Photos'),
              leading: Radio<String?>(
                value: null,
                groupValue: _selectedType,
                onChanged: (value) {
                  setState(() => _selectedType = value);
                  Provider.of<PhotoProvider>(context, listen: false)
                      .loadPhotos();
                  Navigator.of(context).pop();
                },
              ),
            ),
            ListTile(
              title: const Text('Face'),
              leading: Radio<String?>(
                value: 'face',
                groupValue: _selectedType,
                onChanged: (value) {
                  setState(() => _selectedType = value);
                  Provider.of<PhotoProvider>(context, listen: false)
                      .loadPhotos(type: value);
                  Navigator.of(context).pop();
                },
              ),
            ),
            ListTile(
              title: const Text('Body'),
              leading: Radio<String?>(
                value: 'body',
                groupValue: _selectedType,
                onChanged: (value) {
                  setState(() => _selectedType = value);
                  Provider.of<PhotoProvider>(context, listen: false)
                      .loadPhotos(type: value);
                  Navigator.of(context).pop();
                },
              ),
            ),
            ListTile(
              title: const Text('Custom'),
              leading: Radio<String?>(
                value: 'custom',
                groupValue: _selectedType,
                onChanged: (value) {
                  setState(() => _selectedType = value);
                  Provider.of<PhotoProvider>(context, listen: false)
                      .loadPhotos(type: value);
                  Navigator.of(context).pop();
                },
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Timeline'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: _showFilterDialog,
          ),
        ],
      ),
      body: Consumer<PhotoProvider>(
        builder: (context, provider, child) {
          if (provider.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }

          if (provider.error != null) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.error_outline, size: 48),
                  const SizedBox(height: 16),
                  Text('Error: ${provider.error}'),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () => provider.loadPhotos(),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (provider.photos.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.timeline_outlined,
                    size: 64,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No photos in timeline',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Take photos to see your progress over time',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),
                ],
              ),
            );
          }

          final groupedPhotos = _groupPhotosByMonth(provider.photos);
          final months = groupedPhotos.keys.toList();

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: months.length,
            itemBuilder: (context, index) {
              final month = months[index];
              final photos = groupedPhotos[month]!;

              return Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Month header
                  Padding(
                    padding: const EdgeInsets.symmetric(vertical: 16.0),
                    child: Row(
                      children: [
                        Container(
                          width: 4,
                          height: 24,
                          decoration: BoxDecoration(
                            color: Theme.of(context).colorScheme.primary,
                            borderRadius: BorderRadius.circular(2),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Text(
                          month,
                          style: Theme.of(context).textTheme.titleLarge?.copyWith(
                                fontWeight: FontWeight.bold,
                              ),
                        ),
                        const SizedBox(width: 8),
                        Chip(
                          label: Text('${photos.length}'),
                          backgroundColor:
                              Theme.of(context).colorScheme.secondaryContainer,
                          labelStyle: TextStyle(
                            color:
                                Theme.of(context).colorScheme.onSecondaryContainer,
                          ),
                        ),
                      ],
                    ),
                  ),

                  // Photos grid for this month
                  GridView.builder(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 3,
                      crossAxisSpacing: 8,
                      mainAxisSpacing: 8,
                      childAspectRatio: 1,
                    ),
                    itemCount: photos.length,
                    itemBuilder: (context, photoIndex) {
                      final photo = photos[photoIndex];
                      final date =
                          DateTime.fromMillisecondsSinceEpoch(photo.timestamp);

                      return GestureDetector(
                        onTap: () {
                          Navigator.of(context).push(
                            MaterialPageRoute(
                              builder: (context) =>
                                  PhotoDetailScreen(photo: photo),
                            ),
                          );
                        },
                        child: Hero(
                          tag: 'photo_${photo.id}',
                          child: Card(
                            clipBehavior: Clip.antiAlias,
                            margin: EdgeInsets.zero,
                            child: Stack(
                              fit: StackFit.expand,
                              children: [
                                Image.file(
                                  File(photo.filePath),
                                  fit: BoxFit.cover,
                                  errorBuilder: (context, error, stackTrace) =>
                                      Container(
                                    color: Theme.of(context)
                                        .colorScheme
                                        .surfaceVariant,
                                    child: const Center(
                                      child: Icon(Icons.broken_image, size: 32),
                                    ),
                                  ),
                                ),
                                // Date badge
                                Positioned(
                                  bottom: 4,
                                  right: 4,
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 4,
                                      vertical: 2,
                                    ),
                                    decoration: BoxDecoration(
                                      color: Colors.black.withOpacity(0.6),
                                      borderRadius: BorderRadius.circular(4),
                                    ),
                                    child: Text(
                                      DateFormat('d').format(date),
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 10,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ),
                                ),
                                // Type badge
                                Positioned(
                                  top: 4,
                                  right: 4,
                                  child: Container(
                                    padding: const EdgeInsets.symmetric(
                                      horizontal: 4,
                                      vertical: 2,
                                    ),
                                    decoration: BoxDecoration(
                                      color: Colors.black.withOpacity(0.6),
                                      borderRadius: BorderRadius.circular(4),
                                    ),
                                    child: Text(
                                      photo.type.substring(0, 1).toUpperCase(),
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 10,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ),
                        ),
                      );
                    },
                  ),

                  if (index < months.length - 1) const SizedBox(height: 24),
                ],
              );
            },
          );
        },
      ),
    );
  }
}
