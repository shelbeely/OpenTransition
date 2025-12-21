import 'package:flutter/material.dart';
import '../widgets/bottom_navigation.dart';
import 'photos_screen.dart';
import 'milestones_screen.dart';
import 'gallery_screen.dart';
import 'settings_screen.dart';

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
              onPressed: () {
                // TODO: Navigate to add photo/milestone
              },
              icon: Icon(_selectedIndex == 0 ? Icons.camera_alt : Icons.add),
              label: Text(_selectedIndex == 0 ? 'Take Photo' : 'Add Milestone'),
            )
          : null,
    );
  }
}
