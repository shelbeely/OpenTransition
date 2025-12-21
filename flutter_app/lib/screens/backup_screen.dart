import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/backup_service.dart';
import '../services/photo_service.dart';
import '../services/milestone_service.dart';
import 'package:intl/intl.dart';
import 'package:file_picker/file_picker.dart';

class BackupScreen extends StatefulWidget {
  const BackupScreen({super.key});

  @override
  State<BackupScreen> createState() => _BackupScreenState();
}

class _BackupScreenState extends State<BackupScreen> {
  bool _isCreating = false;
  bool _isRestoring = false;
  List<String> _backups = [];
  late BackupService _backupService;

  @override
  void initState() {
    super.initState();
    _initializeService();
  }

  void _initializeService() {
    final photoService = Provider.of<PhotoService>(context, listen: false);
    final milestoneService =
        Provider.of<MilestoneService>(context, listen: false);
    _backupService = BackupService(photoService, milestoneService);
    _loadBackups();
  }

  Future<void> _loadBackups() async {
    final backups = await _backupService.listBackups();
    setState(() {
      _backups = backups;
    });
  }

  Future<void> _createBackup() async {
    setState(() {
      _isCreating = true;
    });

    try {
      final filePath = await _backupService.createBackup();
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Backup created successfully'),
            action: SnackBarAction(
              label: 'View',
              onPressed: () {
                // Show backup location
              },
            ),
          ),
        );
        await _loadBackups();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to create backup: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isCreating = false;
        });
      }
    }
  }

  Future<void> _restoreBackup(String filePath) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Restore Backup'),
        content: const Text(
          'This will replace your current data with the backup. Continue?',
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            child: const Text('Restore'),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    setState(() {
      _isRestoring = true;
    });

    try {
      final result = await _backupService.restoreBackup(filePath);
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              'Restored ${result['photo_count']} photos and ${result['milestone_count']} milestones',
            ),
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to restore backup: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() {
          _isRestoring = false;
        });
      }
    }
  }

  Future<void> _importBackup() async {
    try {
      FilePickerResult? result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ['json'],
      );

      if (result != null && result.files.single.path != null) {
        await _restoreBackup(result.files.single.path!);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to import backup: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  Future<void> _deleteBackup(String filePath) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Delete Backup'),
        content: const Text('Are you sure you want to delete this backup?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.of(context).pop(true),
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text('Delete'),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    try {
      await _backupService.deleteBackup(filePath);
      await _loadBackups();
      
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Backup deleted')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to delete backup: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  String _formatFileName(String filePath) {
    final fileName = filePath.split('/').last;
    return fileName.replaceAll('opentransition_backup_', '').replaceAll('.json', '');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Backup & Export'),
      ),
      body: Column(
        children: [
          // Actions
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton.icon(
                    onPressed: _isCreating ? null : _createBackup,
                    icon: _isCreating
                        ? const SizedBox(
                            width: 16,
                            height: 16,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.backup),
                    label: Text(_isCreating ? 'Creating...' : 'Create Backup'),
                  ),
                ),
                const SizedBox(height: 8),
                SizedBox(
                  width: double.infinity,
                  child: OutlinedButton.icon(
                    onPressed: _isRestoring ? null : _importBackup,
                    icon: const Icon(Icons.file_upload),
                    label: const Text('Import Backup'),
                  ),
                ),
              ],
            ),
          ),

          const Divider(),

          // Backup list header
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: Row(
              children: [
                Text(
                  'Saved Backups',
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                const Spacer(),
                TextButton(
                  onPressed: _loadBackups,
                  child: const Text('Refresh'),
                ),
              ],
            ),
          ),

          // Backup list
          Expanded(
            child: _backups.isEmpty
                ? Center(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          Icons.backup_outlined,
                          size: 64,
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                        const SizedBox(height: 16),
                        Text(
                          'No backups found',
                          style: Theme.of(context).textTheme.titleLarge,
                        ),
                        const SizedBox(height: 8),
                        Text(
                          'Create a backup to get started',
                          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                color: Theme.of(context).colorScheme.onSurfaceVariant,
                              ),
                        ),
                      ],
                    ),
                  )
                : ListView.builder(
                    itemCount: _backups.length,
                    itemBuilder: (context, index) {
                      final backup = _backups[index];
                      return FutureBuilder<Map<String, dynamic>>(
                        future: _backupService.getBackupInfo(backup),
                        builder: (context, snapshot) {
                          final info = snapshot.data ?? {};
                          final createdAt = info['created_at'] as String?;
                          final photoCount = info['photo_count'] as int? ?? 0;
                          final milestoneCount = info['milestone_count'] as int? ?? 0;

                          return Card(
                            margin: const EdgeInsets.symmetric(
                              horizontal: 16,
                              vertical: 4,
                            ),
                            child: ListTile(
                              leading: CircleAvatar(
                                backgroundColor:
                                    Theme.of(context).colorScheme.primaryContainer,
                                child: Icon(
                                  Icons.backup,
                                  color:
                                      Theme.of(context).colorScheme.onPrimaryContainer,
                                ),
                              ),
                              title: Text(_formatFileName(backup)),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  if (createdAt != null)
                                    Text(
                                      DateFormat('MMM dd, yyyy HH:mm')
                                          .format(DateTime.parse(createdAt)),
                                    ),
                                  Text('$photoCount photos, $milestoneCount milestones'),
                                ],
                              ),
                              trailing: PopupMenuButton(
                                itemBuilder: (context) => [
                                  const PopupMenuItem(
                                    value: 'restore',
                                    child: Row(
                                      children: [
                                        Icon(Icons.restore),
                                        SizedBox(width: 8),
                                        Text('Restore'),
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
                                onSelected: (value) {
                                  if (value == 'restore') {
                                    _restoreBackup(backup);
                                  } else if (value == 'delete') {
                                    _deleteBackup(backup);
                                  }
                                },
                              ),
                              isThreeLine: true,
                            ),
                          );
                        },
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
