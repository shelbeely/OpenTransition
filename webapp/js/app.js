// Main application logic for OpenTransition Web App

class OpenTransitionApp {
    constructor() {
        this.currentView = 'home';
        this.currentFilter = 'all';
        this.isLocked = false;
        this.currentEditingMilestone = null;
        this.currentPhotoData = null;
    }

    async init() {
        // Initialize database
        await db.init();

        // Check if app is locked
        await this.checkLock();

        if (!this.isLocked) {
            // Initialize UI
            this.initUI();
            
            // Load initial data
            await this.loadData();

            // Apply saved theme
            await this.applyTheme();
        }
    }

    async checkLock() {
        const lockEnabled = await db.getSetting('lockEnabled');
        const lockPin = await db.getSetting('lockPin');

        if (lockEnabled && lockPin) {
            this.isLocked = true;
            this.showLockScreen();
        }
    }

    showLockScreen() {
        document.getElementById('lockScreen').style.display = 'flex';
        
        const unlockBtn = document.getElementById('unlockBtn');
        const unlockPin = document.getElementById('unlockPin');

        unlockBtn.onclick = async () => {
            const enteredPin = unlockPin.value;
            const savedPin = await db.getSetting('lockPin');

            if (enteredPin === savedPin) {
                this.isLocked = false;
                document.getElementById('lockScreen').style.display = 'none';
                this.initUI();
                await this.loadData();
                await this.applyTheme();
            } else {
                this.showToast('Incorrect PIN');
                unlockPin.value = '';
            }
        };

        unlockPin.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                unlockBtn.click();
            }
        });
    }

    initUI() {
        // Menu button
        document.getElementById('menuBtn').addEventListener('click', () => {
            this.toggleDrawer();
        });

        // Overlay
        document.getElementById('overlay').addEventListener('click', () => {
            this.closeDrawer();
        });

        // Navigation items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', () => {
                const view = item.dataset.view;
                this.navigateTo(view);
                this.closeDrawer();
            });
        });

        // Settings button in app bar
        document.getElementById('settingsBtn').addEventListener('click', () => {
            this.navigateTo('settings');
        });

        // Quick action buttons
        document.getElementById('addPhotoBtn').addEventListener('click', () => {
            this.openPhotoDialog();
        });

        document.getElementById('addMilestoneBtn').addEventListener('click', () => {
            this.openMilestoneDialog();
        });

        document.getElementById('fabAddMilestone').addEventListener('click', () => {
            this.openMilestoneDialog();
        });

        // Photo dialog
        document.getElementById('photoInput').addEventListener('change', (e) => {
            this.handlePhotoSelect(e);
        });

        document.getElementById('savePhotoBtn').addEventListener('click', () => {
            this.savePhoto();
        });

        // Milestone dialog
        document.getElementById('saveMilestoneBtn').addEventListener('click', () => {
            this.saveMilestone();
        });

        // Filter buttons
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                this.applyFilter(btn.dataset.filter);
            });
        });

        // Settings
        document.getElementById('enableLock').addEventListener('change', (e) => {
            this.toggleLock(e.target.checked);
        });

        document.getElementById('themeSelect').addEventListener('change', (e) => {
            this.changeTheme(e.target.value);
        });

        document.getElementById('exportDataBtn').addEventListener('click', () => {
            this.exportData();
        });

        document.getElementById('importDataBtn').addEventListener('click', () => {
            this.importData();
        });

        document.getElementById('clearDataBtn').addEventListener('click', () => {
            this.clearData();
        });

        // Set default date to today
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('photoDate').value = today;
        document.getElementById('milestoneDate').value = today;
    }

    toggleDrawer() {
        const drawer = document.getElementById('navDrawer');
        const overlay = document.getElementById('overlay');
        drawer.classList.toggle('open');
        overlay.classList.toggle('active');
    }

    closeDrawer() {
        const drawer = document.getElementById('navDrawer');
        const overlay = document.getElementById('overlay');
        drawer.classList.remove('open');
        overlay.classList.remove('active');
    }

    navigateTo(view) {
        // Update active nav item
        document.querySelectorAll('.nav-item').forEach(item => {
            if (item.dataset.view === view) {
                item.classList.add('active');
            } else {
                item.classList.remove('active');
            }
        });

        // Update views
        document.querySelectorAll('.view').forEach(v => {
            v.classList.remove('active');
        });
        document.getElementById(`${view}View`).classList.add('active');

        this.currentView = view;

        // Load view-specific data
        if (view === 'gallery') {
            this.loadGallery();
        } else if (view === 'milestones') {
            this.loadMilestones();
        } else if (view === 'home') {
            this.loadHome();
        } else if (view === 'settings') {
            this.loadSettings();
        }
    }

    async loadData() {
        await this.loadHome();
    }

    async loadHome() {
        // Update stats
        const photos = await db.getAllPhotos();
        const milestones = await db.getAllMilestones();

        document.getElementById('photoCount').textContent = photos.length;
        document.getElementById('milestoneCount').textContent = milestones.length;

        // Load recent photos
        const recentPhotos = photos.slice(0, 6);
        this.renderPhotos(recentPhotos, 'recentPhotos');
    }

    async loadGallery() {
        const photos = this.currentFilter === 'all' 
            ? await db.getAllPhotos()
            : await db.getPhotosByType(this.currentFilter);
        
        this.renderPhotos(photos, 'galleryGrid');
    }

    async loadMilestones() {
        const milestones = await db.getAllMilestones();
        const container = document.getElementById('milestonesList');

        if (milestones.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: rgba(0,0,0,0.6);">No milestones yet. Add your first milestone!</p>';
            return;
        }

        container.innerHTML = milestones.map(milestone => `
            <div class="milestone-item" data-id="${milestone.id}">
                <h3>${this.escapeHtml(milestone.title)}</h3>
                <p>${this.escapeHtml(milestone.description)}</p>
                <div class="milestone-date">${this.formatDate(milestone.timestamp)}</div>
            </div>
        `).join('');

        // Add click handlers
        document.querySelectorAll('.milestone-item').forEach(item => {
            item.addEventListener('click', () => {
                this.editMilestone(item.dataset.id);
            });
        });
    }

    async loadSettings() {
        const lockEnabled = await db.getSetting('lockEnabled');
        const theme = await db.getSetting('theme') || 'light';

        document.getElementById('enableLock').checked = lockEnabled || false;
        document.getElementById('themeSelect').value = theme;

        // Show/hide PIN section
        document.getElementById('lockPinSection').style.display = lockEnabled ? 'flex' : 'none';
    }

    renderPhotos(photos, containerId) {
        const container = document.getElementById(containerId);

        if (photos.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: rgba(0,0,0,0.6); grid-column: 1/-1;">No photos yet. Add your first photo!</p>';
            return;
        }

        container.innerHTML = photos.map(photo => `
            <div class="photo-item" data-id="${photo.id}">
                <img src="${photo.dataUrl}" alt="Photo">
                <div class="photo-type">${photo.type === 'face' ? 'Face' : 'Body'}</div>
                <div class="photo-date">${this.formatDate(photo.timestamp)}</div>
            </div>
        `).join('');

        // Add click handlers
        document.querySelectorAll('.photo-item').forEach(item => {
            item.addEventListener('click', () => {
                this.viewPhoto(item.dataset.id);
            });
        });
    }

    applyFilter(filter) {
        this.currentFilter = filter;

        // Update active button
        document.querySelectorAll('.filter-btn').forEach(btn => {
            if (btn.dataset.filter === filter) {
                btn.classList.add('active');
            } else {
                btn.classList.remove('active');
            }
        });

        this.loadGallery();
    }

    openPhotoDialog() {
        this.currentPhotoData = null;
        document.getElementById('photoInput').value = '';
        document.getElementById('photoPreview').innerHTML = '';
        document.getElementById('photoDate').value = new Date().toISOString().split('T')[0];
        document.getElementById('photoType').value = 'face';
        document.getElementById('photoDialog').showModal();
    }

    handlePhotoSelect(e) {
        const file = e.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (event) => {
            this.currentPhotoData = event.target.result;
            document.getElementById('photoPreview').innerHTML = `
                <img src="${event.target.result}" alt="Preview">
            `;
        };
        reader.readAsDataURL(file);
    }

    async savePhoto() {
        if (!this.currentPhotoData) {
            this.showToast('Please select a photo');
            return;
        }

        const date = new Date(document.getElementById('photoDate').value);
        const type = document.getElementById('photoType').value;

        const photo = {
            id: Database.generateUUID(),
            timestamp: date.getTime(),
            epochDay: Database.getEpochDay(date),
            type: type,
            dataUrl: this.currentPhotoData
        };

        await db.addPhoto(photo);
        
        document.getElementById('photoDialog').close();
        this.showToast('Photo saved successfully');
        
        if (this.currentView === 'home') {
            await this.loadHome();
        } else if (this.currentView === 'gallery') {
            await this.loadGallery();
        }
    }

    openMilestoneDialog(milestoneId = null) {
        this.currentEditingMilestone = milestoneId;

        if (milestoneId) {
            // Load milestone data for editing
            db.getMilestone(milestoneId).then(milestone => {
                document.getElementById('milestoneDialogTitle').textContent = 'Edit Milestone';
                document.getElementById('milestoneTitle').value = milestone.title;
                document.getElementById('milestoneDescription').value = milestone.description;
                const date = new Date(milestone.timestamp);
                document.getElementById('milestoneDate').value = date.toISOString().split('T')[0];
            });
        } else {
            // New milestone
            document.getElementById('milestoneDialogTitle').textContent = 'Add Milestone';
            document.getElementById('milestoneTitle').value = '';
            document.getElementById('milestoneDescription').value = '';
            document.getElementById('milestoneDate').value = new Date().toISOString().split('T')[0];
        }

        document.getElementById('milestoneDialog').showModal();
    }

    async saveMilestone() {
        const title = document.getElementById('milestoneTitle').value.trim();
        const description = document.getElementById('milestoneDescription').value.trim();
        const date = new Date(document.getElementById('milestoneDate').value);

        if (!title) {
            this.showToast('Please enter a title');
            return;
        }

        const milestone = {
            id: this.currentEditingMilestone || Database.generateUUID(),
            timestamp: date.getTime(),
            epochDay: Database.getEpochDay(date),
            title: title,
            description: description
        };

        if (this.currentEditingMilestone) {
            await db.updateMilestone(milestone);
            this.showToast('Milestone updated successfully');
        } else {
            await db.addMilestone(milestone);
            this.showToast('Milestone saved successfully');
        }

        document.getElementById('milestoneDialog').close();

        if (this.currentView === 'home') {
            await this.loadHome();
        } else if (this.currentView === 'milestones') {
            await this.loadMilestones();
        }
    }

    async editMilestone(id) {
        this.openMilestoneDialog(id);
    }

    async viewPhoto(id) {
        const photo = await db.getPhoto(id);
        if (!photo) return;

        // Create a simple photo viewer
        const viewer = document.createElement('dialog');
        viewer.className = 'dialog';
        viewer.innerHTML = `
            <div class="dialog-header">
                <h3>${photo.type === 'face' ? 'Face' : 'Body'} Photo</h3>
                <button class="close-btn" onclick="this.closest('dialog').close(); this.closest('dialog').remove();">
                    <i class="material-icons">close</i>
                </button>
            </div>
            <div class="dialog-content">
                <img src="${photo.dataUrl}" style="max-width: 100%; border-radius: 8px;">
                <p style="margin-top: 16px; text-align: center;">${this.formatDate(photo.timestamp)}</p>
            </div>
            <div class="dialog-actions">
                <button class="btn-secondary" onclick="app.deletePhotoWithConfirm('${photo.id}'); this.closest('dialog').close(); this.closest('dialog').remove();">Delete</button>
                <button class="btn-primary" onclick="this.closest('dialog').close(); this.closest('dialog').remove();">Close</button>
            </div>
        `;
        document.body.appendChild(viewer);
        viewer.showModal();
    }

    async deletePhotoWithConfirm(id) {
        if (confirm('Are you sure you want to delete this photo?')) {
            await db.deletePhoto(id);
            this.showToast('Photo deleted');
            
            if (this.currentView === 'home') {
                await this.loadHome();
            } else if (this.currentView === 'gallery') {
                await this.loadGallery();
            }
        }
    }

    async toggleLock(enabled) {
        await db.setSetting('lockEnabled', enabled);

        if (enabled) {
            document.getElementById('lockPinSection').style.display = 'flex';
            
            // Get or set PIN
            const currentPin = await db.getSetting('lockPin');
            if (!currentPin) {
                const pin = prompt('Enter a 4-digit PIN:');
                if (pin && /^\d{4}$/.test(pin)) {
                    await db.setSetting('lockPin', pin);
                    this.showToast('App lock enabled');
                } else {
                    this.showToast('Invalid PIN. Please enter 4 digits.');
                    document.getElementById('enableLock').checked = false;
                    await db.setSetting('lockEnabled', false);
                }
            }
        } else {
            document.getElementById('lockPinSection').style.display = 'none';
            this.showToast('App lock disabled');
        }
    }

    async changeTheme(theme) {
        await db.setSetting('theme', theme);
        await this.applyTheme();
    }

    async applyTheme() {
        const theme = await db.getSetting('theme') || 'light';
        
        if (theme === 'auto') {
            // Use system preference
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            document.body.dataset.theme = prefersDark ? 'dark' : 'light';
        } else {
            document.body.dataset.theme = theme;
        }
    }

    async exportData() {
        try {
            const data = await db.exportData();
            const json = JSON.stringify(data, null, 2);
            const blob = new Blob([json], { type: 'application/json' });
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `opentransition-backup-${new Date().toISOString().split('T')[0]}.json`;
            a.click();
            URL.revokeObjectURL(url);
            this.showToast('Data exported successfully');
        } catch (error) {
            console.error('Export error:', error);
            this.showToast('Error exporting data');
        }
    }

    async importData() {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'application/json,.ttbackup,.zip';
        
        input.onchange = async (e) => {
            const file = e.target.files[0];
            if (!file) return;

            try {
                // Check if it's a ZIP/ttbackup file (Android backup) or JSON (web backup)
                if (file.name.endsWith('.ttbackup') || file.name.endsWith('.zip')) {
                    await this.importAndroidBackup(file);
                } else {
                    // Web app JSON format
                    const text = await file.text();
                    const data = JSON.parse(text);
                    
                    if (confirm('This will replace all existing data. Are you sure?')) {
                        await db.importData(data);
                        this.showToast('Data imported successfully');
                        await this.loadData();
                        
                        if (this.currentView === 'home') {
                            await this.loadHome();
                        }
                    }
                }
            } catch (error) {
                console.error('Import error:', error);
                this.showToast('Error importing data');
            }
        };
        
        input.click();
    }

    async importAndroidBackup(file) {
        if (!confirm('This will replace all existing data with the Android app backup. Are you sure?')) {
            return;
        }

        try {
            // Load JSZip library dynamically
            if (typeof JSZip === 'undefined') {
                await this.loadJSZip();
            }

            const arrayBuffer = await file.arrayBuffer();
            const zip = await JSZip.loadAsync(arrayBuffer);
            
            // Extract data.json from the zip
            const dataJsonFile = zip.file('data.json');
            if (!dataJsonFile) {
                throw new Error('Invalid backup file: data.json not found');
            }

            const dataJsonText = await dataJsonFile.async('text');
            const androidData = JSON.parse(dataJsonText);

            // Convert Android format to web format
            const webData = await this.convertAndroidToWebFormat(androidData, zip);

            // Import the converted data
            await db.importData(webData);
            this.showToast('Android backup imported successfully');
            await this.loadData();
            
            if (this.currentView === 'home') {
                await this.loadHome();
            }
        } catch (error) {
            console.error('Android backup import error:', error);
            this.showToast('Error importing Android backup: ' + error.message);
        }
    }

    async convertAndroidToWebFormat(androidData, zip) {
        const webData = {
            version: 1,
            exportDate: new Date().toISOString(),
            photos: [],
            milestones: [],
            settings: {}
        };

        // Convert photos
        if (androidData.photos && Array.isArray(androidData.photos)) {
            for (const androidPhoto of androidData.photos) {
                try {
                    // Get the image file from the zip
                    const imageFile = zip.file(androidPhoto.fileName);
                    if (imageFile) {
                        const imageBlob = await imageFile.async('blob');
                        const dataUrl = await this.blobToDataUrl(imageBlob);

                        // Convert Android photo format to web format
                        const webPhoto = {
                            id: androidPhoto.id,
                            timestamp: androidPhoto.timestamp,
                            epochDay: androidPhoto.epochDay,
                            type: androidPhoto.type === 0 ? 'face' : 'body',
                            dataUrl: dataUrl
                        };
                        webData.photos.push(webPhoto);
                    }
                } catch (error) {
                    console.error('Error converting photo:', error);
                }
            }
        }

        // Convert milestones
        if (androidData.milestones && Array.isArray(androidData.milestones)) {
            for (const androidMilestone of androidData.milestones) {
                const webMilestone = {
                    id: androidMilestone.id,
                    timestamp: androidMilestone.timestamp,
                    epochDay: androidMilestone.epochDay,
                    title: androidMilestone.title,
                    description: androidMilestone.description || ''
                };
                webData.milestones.push(webMilestone);
            }
        }

        // Convert settings (if available)
        if (androidData.settings) {
            // Map Android settings to web settings
            if (androidData.settings.theme) {
                webData.settings.theme = this.mapAndroidTheme(androidData.settings.theme);
            }
        }

        return webData;
    }

    mapAndroidTheme(androidTheme) {
        // Map Android theme values to web theme values
        const themeMap = {
            'ORIGINAL': 'light',
            'DARK': 'dark',
            'SYSTEM_DEFAULT': 'auto'
        };
        return themeMap[androidTheme] || 'light';
    }

    async blobToDataUrl(blob) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onloadend = () => resolve(reader.result);
            reader.onerror = reject;
            reader.readAsDataURL(blob);
        });
    }

    async loadJSZip() {
        // Load JSZip library from CDN
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = 'https://cdnjs.cloudflare.com/ajax/libs/jszip/3.10.1/jszip.min.js';
            script.onload = resolve;
            script.onerror = () => reject(new Error('Failed to load JSZip library'));
            document.head.appendChild(script);
        });
    }

    async clearData() {
        if (confirm('This will delete ALL photos and milestones. This cannot be undone. Are you sure?')) {
            if (confirm('Are you ABSOLUTELY sure? This is your last warning!')) {
                await db.clearAllData();
                this.showToast('All data cleared');
                await this.loadData();
                
                if (this.currentView === 'home') {
                    await this.loadHome();
                }
            }
        }
    }

    showToast(message) {
        const toast = document.getElementById('toast');
        toast.textContent = message;
        toast.classList.add('show');
        
        setTimeout(() => {
            toast.classList.remove('show');
        }, 3000);
    }

    formatDate(timestamp) {
        const date = new Date(timestamp);
        return date.toLocaleDateString('en-US', { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric' 
        });
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Initialize app when DOM is ready
let app;
document.addEventListener('DOMContentLoaded', () => {
    app = new OpenTransitionApp();
    app.init();
});

// Listen for system theme changes
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', async () => {
    if (app) {
        const theme = await db.getSetting('theme');
        if (theme === 'auto') {
            app.applyTheme();
        }
    }
});
