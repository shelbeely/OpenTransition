import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../services/auth_service.dart';

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final authService = Provider.of<AuthService>(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Settings'),
      ),
      body: ListView(
        children: [
          // Security Section
          _buildSectionHeader(context, 'Security'),
          ListTile(
            leading: const Icon(Icons.lock),
            title: const Text('App Lock'),
            subtitle: const Text('Secure your app with PIN or biometric'),
            trailing: Switch(
              value: false, // TODO: Connect to settings
              onChanged: (value) {
                // TODO: Toggle app lock
              },
            ),
          ),
          ListTile(
            leading: const Icon(Icons.fingerprint),
            title: const Text('Biometric Authentication'),
            subtitle: const Text('Use fingerprint or face recognition'),
            trailing: Switch(
              value: false, // TODO: Connect to settings
              onChanged: (value) {
                // TODO: Toggle biometric
              },
            ),
          ),
          ListTile(
            leading: const Icon(Icons.lock_person),
            title: const Text('Decoy Vault'),
            subtitle: const Text('Create a separate vault with different passcode'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              // TODO: Navigate to decoy vault setup
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
              // TODO: Navigate to backup screen
            },
          ),
          ListTile(
            leading: const Icon(Icons.file_download),
            title: const Text('Import from TransTracks'),
            subtitle: const Text('Import your TransTracks data'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              // TODO: Navigate to import screen
            },
          ),
          
          const Divider(),
          
          // Appearance Section
          _buildSectionHeader(context, 'Appearance'),
          ListTile(
            leading: const Icon(Icons.palette),
            title: const Text('Theme'),
            subtitle: const Text('Light, Dark, or System'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              // TODO: Show theme picker
            },
          ),
          
          const Divider(),
          
          // Account Section
          _buildSectionHeader(context, 'Account'),
          if (authService.isAuthenticated)
            ListTile(
              leading: const Icon(Icons.logout),
              title: const Text('Sign Out'),
              onTap: () async {
                await authService.signOut();
              },
            )
          else
            ListTile(
              leading: const Icon(Icons.login),
              title: const Text('Sign In'),
              onTap: () {
                // TODO: Navigate to sign in screen
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
              // TODO: Show about dialog
            },
          ),
          ListTile(
            leading: const Icon(Icons.privacy_tip),
            title: const Text('Privacy Policy'),
            trailing: const Icon(Icons.arrow_forward_ios, size: 16),
            onTap: () {
              // TODO: Show privacy policy
            },
          ),
          
          const SizedBox(height: 16),
          Center(
            child: Text(
              'Version 1.3.0',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
            ),
          ),
          const SizedBox(height: 32),
        ],
      ),
    );
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
