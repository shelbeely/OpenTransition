import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/milestone_provider.dart';
import '../models/milestone.dart';

class AddEditMilestoneScreen extends StatefulWidget {
  final Milestone? milestone;

  const AddEditMilestoneScreen({
    super.key,
    this.milestone,
  });

  @override
  State<AddEditMilestoneScreen> createState() => _AddEditMilestoneScreenState();
}

class _AddEditMilestoneScreenState extends State<AddEditMilestoneScreen> {
  final _formKey = GlobalKey<FormState>();
  late TextEditingController _titleController;
  late TextEditingController _descriptionController;
  late DateTime _selectedDate;
  String _selectedType = 'general';

  final List<Map<String, dynamic>> _milestoneTypes = [
    {'value': 'general', 'label': 'General', 'icon': Icons.flag},
    {'value': 'medical', 'label': 'Medical', 'icon': Icons.medical_services},
    {'value': 'social', 'label': 'Social', 'icon': Icons.people},
    {'value': 'legal', 'label': 'Legal', 'icon': Icons.gavel},
    {'value': 'personal', 'label': 'Personal', 'icon': Icons.star},
  ];

  @override
  void initState() {
    super.initState();
    _titleController = TextEditingController(text: widget.milestone?.title ?? '');
    _descriptionController = TextEditingController(text: widget.milestone?.description ?? '');
    _selectedDate = widget.milestone != null
        ? DateTime.fromMillisecondsSinceEpoch(widget.milestone!.date)
        : DateTime.now();
    _selectedType = widget.milestone?.type ?? 'general';
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    super.dispose();
  }

  Future<void> _selectDate(BuildContext context) async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2000),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );

    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  Future<void> _saveMilestone() async {
    if (!_formKey.currentState!.validate()) return;

    final provider = Provider.of<MilestoneProvider>(context, listen: false);

    if (widget.milestone == null) {
      // Create new milestone
      await provider.createMilestone(
        title: _titleController.text.trim(),
        description: _descriptionController.text.trim().isEmpty
            ? null
            : _descriptionController.text.trim(),
        date: _selectedDate,
        type: _selectedType,
      );
    } else {
      // Update existing milestone
      final updated = widget.milestone!.copyWith(
        title: _titleController.text.trim(),
        description: _descriptionController.text.trim().isEmpty
            ? null
            : _descriptionController.text.trim(),
        date: _selectedDate.millisecondsSinceEpoch,
        type: _selectedType,
      );
      await provider.updateMilestone(updated);
    }

    if (mounted) {
      Navigator.of(context).pop();
    }
  }

  @override
  Widget build(BuildContext context) {
    final isEditing = widget.milestone != null;

    return Scaffold(
      appBar: AppBar(
        title: Text(isEditing ? 'Edit Milestone' : 'Add Milestone'),
        actions: [
          TextButton(
            onPressed: _saveMilestone,
            child: const Text('Save'),
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // Title
            TextFormField(
              controller: _titleController,
              decoration: const InputDecoration(
                labelText: 'Title',
                hintText: 'Enter milestone title',
                prefixIcon: Icon(Icons.title),
              ),
              validator: (value) {
                if (value == null || value.trim().isEmpty) {
                  return 'Please enter a title';
                }
                return null;
              },
              textCapitalization: TextCapitalization.sentences,
            ),
            const SizedBox(height: 16),

            // Description
            TextFormField(
              controller: _descriptionController,
              decoration: const InputDecoration(
                labelText: 'Description (optional)',
                hintText: 'Enter description',
                prefixIcon: Icon(Icons.description),
              ),
              maxLines: 3,
              textCapitalization: TextCapitalization.sentences,
            ),
            const SizedBox(height: 16),

            // Date
            ListTile(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(8),
                side: BorderSide(
                  color: Theme.of(context).colorScheme.outline,
                ),
              ),
              leading: const Icon(Icons.calendar_today),
              title: const Text('Date'),
              subtitle: Text(
                '${_selectedDate.year}-${_selectedDate.month.toString().padLeft(2, '0')}-${_selectedDate.day.toString().padLeft(2, '0')}',
              ),
              trailing: const Icon(Icons.edit),
              onTap: () => _selectDate(context),
            ),
            const SizedBox(height: 16),

            // Type
            Text(
              'Type',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _milestoneTypes.map((type) {
                final isSelected = _selectedType == type['value'];
                return ChoiceChip(
                  label: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(
                        type['icon'],
                        size: 18,
                        color: isSelected
                            ? Theme.of(context).colorScheme.onPrimary
                            : Theme.of(context).colorScheme.onSurface,
                      ),
                      const SizedBox(width: 4),
                      Text(type['label']),
                    ],
                  ),
                  selected: isSelected,
                  onSelected: (selected) {
                    if (selected) {
                      setState(() {
                        _selectedType = type['value'];
                      });
                    }
                  },
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }
}
