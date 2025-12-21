import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';
import '../providers/settings_provider.dart';
import '../utils/platform_utils.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  void _showThemeDialog(BuildContext context) {
    final settingsProvider = Provider.of<SettingsProvider>(context, listen: false);
    
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Choose Theme'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            RadioListTile<String>(
              title: const Text('Light'),
              value: 'light',
              groupValue: settingsProvider.themeMode,
              onChanged: (value) {
                if (value != null) {
                  settingsProvider.setThemeMode(value);
                  Navigator.of(context).pop();
                }
              },
            ),
            RadioListTile<String>(
              title: const Text('Dark'),
              value: 'dark',
              groupValue: settingsProvider.themeMode,
              onChanged: (value) {
                if (value != null) {
                  settingsProvider.setThemeMode(value);
                  Navigator.of(context).pop();
                }
              },
            ),
            RadioListTile<String>(
              title: const Text('System Default'),
              value: 'system',
              groupValue: settingsProvider.themeMode,
              onChanged: (value) {
                if (value != null) {
                  settingsProvider.setThemeMode(value);
                  Navigator.of(context).pop();
                }
              },
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: Consumer<SettingsProvider>(
        builder: (context, settings, child) {
          return ListView(
            children: [
              // Platform Info (for development/debugging)
              if (PlatformUtils.isWeb || PlatformUtils.isDesktop)
                Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Card(
                    color: Theme.of(context).colorScheme.secondaryContainer,
                    child: Padding(
                      padding: const EdgeInsets.all(12.0),
                      child: Row(
                        children: [
                          Icon(
                            Icons.info_outline,
                            color: Theme.of(context).colorScheme.onSecondaryContainer,
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Text(
                              'Running on ${PlatformUtils.platformName}',
                              style: TextStyle(
                                color: Theme.of(context).colorScheme.onSecondaryContainer,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              
              // Security Section
              _buildSectionHeader(context, 'Security'),
              if (PlatformUtils.supportsBiometrics)
                ListTile(
                  leading: const Icon(Icons.lock),
                  title: const Text('App Lock'),
                  subtitle: const Text('Secure your app with PIN or biometric'),
                  trailing: Switch(
                    value: settings.lockEnabled,
                    onChanged: (value) async {
                      await settings.setLockEnabled(value);
                    },
                  ),
                ),
              if (PlatformUtils.supportsBiometrics)
                ListTile(
                  leading: const Icon(Icons.fingerprint),
                  title: const Text('Biometric Authentication'),
                  subtitle: const Text('Use fingerprint or face recognition'),
                  trailing: Switch(
                    value: settings.biometricEnabled,
                    onChanged: settings.lockEnabled
                        ? (value) async {
                            await settings.setBiometricEnabled(value);
                          }
                        : null,
                  ),
                  enabled: settings.lockEnabled,
                ),
              if (!PlatformUtils.supportsBiometrics)
                ListTile(
                  leading: const Icon(Icons.lock_outline),
                  title: const Text('Biometric Authentication'),
                  subtitle: Text(
                    'Not available on ${PlatformUtils.platformName}',
                    style: TextStyle(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                  ),
                  enabled: false,
                ),
              ListTile(
                leading: const Icon(Icons.lock_person),
                title: const Text('Decoy Vault'),
                subtitle: const Text('Create a separate vault with different passcode'),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Feature coming soon')),
                  );
                },
              ),
              
              const Divider(),
              
              // Backup Section
              _buildSectionHeader(context, 'Data'),
              ListTile(
                leading: const Icon(Icons.backup),
                title: const Text('Backup & Export'),
                subtitle: const Text('Export your data'),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Feature coming soon')),
                  );
                },
              ),
              ListTile(
                leading: const Icon(Icons.file_download),
                title: const Text('Import from TransTracks'),
                subtitle: const Text('Import your TransTracks data'),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Feature coming soon')),
                  );
                },
              ),
              
              const Divider(),
              
              // Appearance Section
              _buildSectionHeader(context, 'Appearance'),
              ListTile(
                leading: const Icon(Icons.palette),
                title: const Text('Theme'),
                subtitle: Text(_getThemeLabel(settings.themeMode)),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () => _showThemeDialog(context),
              ),
              
              const Divider(),
              
              // Account Section
              _buildSectionHeader(context, 'Account'),
              if (authService.isAuthenticated)
                ListTile(
                  leading: const Icon(Icons.logout),
                  title: const Text('Sign Out'),
                  onTap: () async {
                    final confirmed = await showDialog<bool>(
                      context: context,
                      builder: (context) => AlertDialog(
                        title: const Text('Sign Out'),
                        content: const Text('Are you sure you want to sign out?'),
                        actions: [
                          TextButton(
                            onPressed: () => Navigator.of(context).pop(false),
                            child: const Text('Cancel'),
                          ),
                          TextButton(
                            onPressed: () => Navigator.of(context).pop(true),
                            child: const Text('Sign Out'),
                          ),
                        ],
                      ),
                    );
                    
                    if (confirmed == true) {
                      await authService.signOut();
                    }
                  },
                )
              else
                ListTile(
                  leading: const Icon(Icons.login),
                  title: const Text('Sign In'),
                  onTap: () {
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Sign in feature coming soon')),
                    );
                  },
                ),
              
              const Divider(),
              
              // About Section
              _buildSectionHeader(context, 'About'),
              ListTile(
                leading: const Icon(Icons.info),
                title: const Text('About OpenTransition'),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () {
                  showAboutDialog(
                    context: context,
                    applicationName: 'OpenTransition',
                    applicationVersion: '1.3.0 (Flutter)',
                    applicationIcon: const FlutterLogo(size: 48),
                    children: [
                      const Text(
                        'A transition tracking application for transgender people, '
                        'focusing on photo tracking and milestone management.',
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'Running on ${PlatformUtils.platformName}',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  );
                },
              ),
              ListTile(
                leading: const Icon(Icons.privacy_tip),
                title: const Text('Privacy Policy'),
                trailing: const Icon(Icons.arrow_forward_ios, size: 16),
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Privacy policy coming soon')),
                  );
                },
              ),
              
              const SizedBox(height: 16),
              Center(
                child: Text(
                  'Version 1.3.0 (Flutter)',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                ),
              ),
              Center(
                child: Text(
                  PlatformUtils.platformName,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                      ),
                ),
              ),
              const SizedBox(height: 32),
            ],
          );
        },
      ),
    );
  }

  String _getThemeLabel(String themeMode) {
    switch (themeMode) {
      case 'light':
        return 'Light';
      case 'dark':
        return 'Dark';
      default:
        return 'System Default';
    }
  }

  Widget _buildSectionHeader(BuildContext context, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 16, 16, 8),
      child: Text(
        title.toUpperCase(),
        style: Theme.of(context).textTheme.labelSmall?.copyWith(
              color: Theme.of(context).colorScheme.primary,
              fontWeight: FontWeight.bold,
            ),
      ),
    );
  }
}
