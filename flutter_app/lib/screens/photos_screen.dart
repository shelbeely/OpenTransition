import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'dart:io';
import '../providers/photo_provider.dart';
import 'photo_detail_screen.dart';

class PhotosScreen extends StatefulWidget {
  const PhotosScreen({super.key});

  @override
  State<PhotosScreen> createState() => _PhotosScreenState();
}

class _PhotosScreenState extends State<PhotosScreen> {
  String? _selectedFilter;

  @override
  void initState() {
    super.initState();
    // Load photos when screen is first displayed
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<PhotoProvider>(context, listen: false).loadPhotos();
    });
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
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() => _selectedFilter = value);
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
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() => _selectedFilter = value);
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
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() => _selectedFilter = value);
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
                groupValue: _selectedFilter,
                onChanged: (value) {
                  setState(() => _selectedFilter = value);
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
        title: const Text('Photos'),
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
                    Icons.camera_alt_outlined,
                    size: 64,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No photos yet',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Tap the camera button to take your first photo',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),
                ],
              ),
            );
          }

          return ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: provider.photos.length,
            itemBuilder: (context, index) {
              final photo = provider.photos[index];
              final date =
                  DateTime.fromMillisecondsSinceEpoch(photo.timestamp);
              final dateStr = DateFormat('MMM dd, yyyy HH:mm').format(date);

              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: ClipRRect(
                    borderRadius: BorderRadius.circular(8),
                    child: Image.file(
                      File(photo.filePath),
                      width: 56,
                      height: 56,
                      fit: BoxFit.cover,
                      errorBuilder: (context, error, stackTrace) =>
                          const Icon(Icons.broken_image, size: 56),
                    ),
                  ),
                  title: Text(photo.type.toUpperCase()),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (photo.description != null &&
                          photo.description!.isNotEmpty)
                        Text(
                          photo.description!,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                      Text(dateStr),
                    ],
                  ),
                  trailing: PopupMenuButton(
                    itemBuilder: (context) => [
                      const PopupMenuItem(
                        value: 'view',
                        child: Row(
                          children: [
                            Icon(Icons.visibility),
                            SizedBox(width: 8),
                            Text('View'),
                          ],
                        ),
                      ),
                      const PopupMenuItem(
                        value: 'delete',
                        child: Row(
                          children: [
                            Icon(Icons.delete, color: Colors.red),
                            SizedBox(width: 8),
                            Text('Delete',
                                style: TextStyle(color: Colors.red)),
                          ],
                        ),
                      ),
                    ],
                    onSelected: (value) async {
                      if (value == 'view') {
                        Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (context) =>
                                PhotoDetailScreen(photo: photo),
                          ),
                        );
                      } else if (value == 'delete') {
                        final confirmed = await showDialog<bool>(
                          context: context,
                          builder: (context) => AlertDialog(
                            title: const Text('Delete Photo'),
                            content: const Text(
                                'Are you sure you want to delete this photo?'),
                            actions: [
                              TextButton(
                                onPressed: () =>
                                    Navigator.of(context).pop(false),
                                child: const Text('Cancel'),
                              ),
                              TextButton(
                                onPressed: () =>
                                    Navigator.of(context).pop(true),
                                child: const Text('Delete',
                                    style: TextStyle(color: Colors.red)),
                              ),
                            ],
                          ),
                        );

                        if (confirmed == true) {
                          await provider.deletePhoto(photo.id);
                        }
                      }
                    },
                  ),
                  onTap: () {
                    Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (context) => PhotoDetailScreen(photo: photo),
                      ),
                    );
                  },
                ),
              );
            },
          );
        },
      ),
    );
  }
}
