import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:io';
import '../providers/photo_provider.dart';
import 'photo_detail_screen.dart';

class GalleryScreen extends StatefulWidget {
  const GalleryScreen({super.key});

  @override
  State<GalleryScreen> createState() => _GalleryScreenState();
}

class _GalleryScreenState extends State<GalleryScreen> {
  int _crossAxisCount = 3;
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
        title: const Text('Gallery'),
        actions: [
          IconButton(
            icon: const Icon(Icons.filter_list),
            onPressed: _showFilterDialog,
          ),
          PopupMenuButton<int>(
            icon: const Icon(Icons.grid_view),
            onSelected: (value) {
              setState(() {
                _crossAxisCount = value;
              });
            },
            itemBuilder: (context) => [
              const PopupMenuItem(
                value: 2,
                child: Text('2 columns'),
              ),
              const PopupMenuItem(
                value: 3,
                child: Text('3 columns'),
              ),
              const PopupMenuItem(
                value: 4,
                child: Text('4 columns'),
              ),
            ],
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
                    Icons.photo_library_outlined,
                    size: 64,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No photos in gallery',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Take some photos to see them here',
                    style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                  ),
                ],
              ),
            );
          }

          return GridView.builder(
            padding: const EdgeInsets.all(8),
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
              crossAxisCount: _crossAxisCount,
              crossAxisSpacing: 8,
              mainAxisSpacing: 8,
              childAspectRatio: 1,
            ),
            itemCount: provider.photos.length,
            itemBuilder: (context, index) {
              final photo = provider.photos[index];
              
              return GestureDetector(
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (context) => PhotoDetailScreen(photo: photo),
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
                            color: Theme.of(context).colorScheme.surfaceVariant,
                            child: const Center(
                              child: Icon(Icons.broken_image, size: 48),
                            ),
                          ),
                        ),
                        // Type badge
                        Positioned(
                          top: 4,
                          right: 4,
                          child: Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 6,
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: Colors.black.withOpacity(0.6),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              photo.type.toUpperCase(),
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
          );
        },
      ),
    );
  }
}
