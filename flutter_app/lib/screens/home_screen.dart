import 'package:flutter/material.dart';
import '../widgets/bottom_navigation.dart';
import '../utils/platform_utils.dart';
import 'photos_screen.dart';
import 'milestones_screen.dart';
import 'gallery_screen.dart';
import 'settings_screen.dart';
import 'camera_screen.dart';
import 'add_edit_milestone_screen.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';
import '../providers/photo_provider.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _selectedIndex = 0;

  final List<Widget> _screens = const [
    PhotosScreen(),
    MilestonesScreen(),
    GalleryScreen(),
    SettingsScreen(),
  ];

  Future<void> _handleFloatingActionButton() async {
    if (_selectedIndex == 0) {
      // Take photo - show photo type selection first
      final photoType = await showDialog<String>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Select Photo Type'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.face),
                title: const Text('Face'),
                onTap: () => Navigator.of(context).pop('face'),
              ),
              ListTile(
                leading: const Icon(Icons.accessibility_new),
                title: const Text('Body'),
                onTap: () => Navigator.of(context).pop('body'),
              ),
              ListTile(
                leading: const Icon(Icons.photo),
                title: const Text('Custom'),
                onTap: () => Navigator.of(context).pop('custom'),
              ),
            ],
          ),
        ),
      );

      if (photoType != null && mounted) {
        String? imagePath;
        
        // Use camera for mobile, image picker for web/desktop
        if (PlatformUtils.isMobile && PlatformUtils.supportsCameraCapture) {
          imagePath = await Navigator.of(context).push<String>(
            MaterialPageRoute(
              builder: (context) => CameraScreen(photoType: photoType),
            ),
          );
        } else {
          // Use image picker for web and desktop
          final ImagePicker picker = ImagePicker();
          final XFile? image = await picker.pickImage(
            source: ImageSource.camera,
            preferredCameraDevice: CameraDevice.front,
          );
          imagePath = image?.path;
        }

        if (imagePath != null && mounted) {
          // Save photo with provider
          final photoProvider =
              Provider.of<PhotoProvider>(context, listen: false);
          await photoProvider.addPhoto(
            filePath: imagePath,
            type: photoType,
          );
          
          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Photo saved successfully')),
            );
          }
        }
      }
    } else if (_selectedIndex == 1) {
      // Add milestone
      await Navigator.of(context).push(
        MaterialPageRoute(
          builder: (context) => const AddEditMilestoneScreen(),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: _screens[_selectedIndex],
      bottomNavigationBar: BottomNavigation(
        selectedIndex: _selectedIndex,
        onIndexChanged: (index) {
          setState(() {
            _selectedIndex = index;
          });
        },
      ),
      floatingActionButton: _selectedIndex < 2
          ? FloatingActionButton.extended(
              onPressed: _handleFloatingActionButton,
              icon: Icon(_selectedIndex == 0 ? Icons.camera_alt : Icons.add),
              label: Text(_selectedIndex == 0 ? 'Take Photo' : 'Add Milestone'),
            )
          : null,
    );
  }
}
