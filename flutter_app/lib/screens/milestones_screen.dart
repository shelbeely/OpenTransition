import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import '../providers/milestone_provider.dart';
import 'add_edit_milestone_screen.dart';

class MilestonesScreen extends StatefulWidget {
  const MilestonesScreen({super.key});

  @override
  State<MilestonesScreen> createState() => _MilestonesScreenState();
}

class _MilestonesScreenState extends State<MilestonesScreen> {
  @override
  void initState() {
    super.initState();
    // Load milestones when screen is first displayed
    WidgetsBinding.instance.addPostFrameCallback((_) {
      Provider.of<MilestoneProvider>(context, listen: false).loadMilestones();
    });
  }

  IconData _getIconForType(String type) {
    switch (type) {
      case 'medical':
        return Icons.medical_services;
      case 'social':
        return Icons.people;
      case 'legal':
        return Icons.gavel;
      case 'personal':
        return Icons.star;
      default:
        return Icons.flag;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Milestones'),
        actions: [
          IconButton(
            icon: const Icon(Icons.sort),
            onPressed: () {
              // TODO: Show sort options
            },
          ),
        ],
      ),
      body: Consumer<MilestoneProvider>(
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
                    onPressed: () => provider.loadMilestones(),
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }

          if (provider.milestones.isEmpty) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.flag_outlined,
                    size: 64,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No milestones yet',
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Tap the + button to add your first milestone',
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
            itemCount: provider.milestones.length,
            itemBuilder: (context, index) {
              final milestone = provider.milestones[index];
              final date = DateTime.fromMillisecondsSinceEpoch(milestone.date);
              final dateStr = DateFormat('MMM dd, yyyy').format(date);

              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor:
                        Theme.of(context).colorScheme.primaryContainer,
                    child: Icon(
                      _getIconForType(milestone.type),
                      color:
                          Theme.of(context).colorScheme.onPrimaryContainer,
                    ),
                  ),
                  title: Text(milestone.title),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      if (milestone.description != null &&
                          milestone.description!.isNotEmpty)
                        Text(
                          milestone.description!,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                      const SizedBox(height: 4),
                      Text(dateStr),
                    ],
                  ),
                  trailing: PopupMenuButton(
                    itemBuilder: (context) => [
                      const PopupMenuItem(
                        value: 'edit',
                        child: Row(
                          children: [
                            Icon(Icons.edit),
                            SizedBox(width: 8),
                            Text('Edit'),
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
                      if (value == 'edit') {
                        await Navigator.of(context).push(
                          MaterialPageRoute(
                            builder: (context) =>
                                AddEditMilestoneScreen(milestone: milestone),
                          ),
                        );
                      } else if (value == 'delete') {
                        final confirmed = await showDialog<bool>(
                          context: context,
                          builder: (context) => AlertDialog(
                            title: const Text('Delete Milestone'),
                            content: const Text(
                                'Are you sure you want to delete this milestone?'),
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
                          await provider.deleteMilestone(milestone.id);
                        }
                      }
                    },
                  ),
                  isThreeLine: milestone.description != null &&
                      milestone.description!.isNotEmpty,
                  onTap: () async {
                    await Navigator.of(context).push(
                      MaterialPageRoute(
                        builder: (context) =>
                            AddEditMilestoneScreen(milestone: milestone),
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
