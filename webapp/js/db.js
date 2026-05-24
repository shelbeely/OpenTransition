// Database module for OpenTransition Web App
// Uses IndexedDB for local storage

const DB_NAME = 'OpenTransitionDB';
const DB_VERSION = 1;
const PHOTOS_STORE = 'photos';
const MILESTONES_STORE = 'milestones';
const SETTINGS_STORE = 'settings';

class Database {
    constructor() {
        this.db = null;
    }

    // Initialize the database
    async init() {
        return new Promise((resolve, reject) => {
            const request = indexedDB.open(DB_NAME, DB_VERSION);

            request.onerror = () => {
                console.error('Database failed to open');
                reject(request.error);
            };

            request.onsuccess = () => {
                this.db = request.result;
                console.log('Database opened successfully');
                resolve(this.db);
            };

            request.onupgradeneeded = (event) => {
                const db = event.target.result;

                // Create photos store
                if (!db.objectStoreNames.contains(PHOTOS_STORE)) {
                    const photosStore = db.createObjectStore(PHOTOS_STORE, { keyPath: 'id' });
                    photosStore.createIndex('timestamp', 'timestamp', { unique: false });
                    photosStore.createIndex('type', 'type', { unique: false });
                    photosStore.createIndex('epochDay', 'epochDay', { unique: false });
                }

                // Create milestones store
                if (!db.objectStoreNames.contains(MILESTONES_STORE)) {
                    const milestonesStore = db.createObjectStore(MILESTONES_STORE, { keyPath: 'id' });
                    milestonesStore.createIndex('timestamp', 'timestamp', { unique: false });
                    milestonesStore.createIndex('epochDay', 'epochDay', { unique: false });
                }

                // Create settings store
                if (!db.objectStoreNames.contains(SETTINGS_STORE)) {
                    db.createObjectStore(SETTINGS_STORE, { keyPath: 'key' });
                }

                console.log('Database setup complete');
            };
        });
    }

    // Photo operations
    async addPhoto(photo) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PHOTOS_STORE], 'readwrite');
            const store = transaction.objectStore(PHOTOS_STORE);
            const request = store.add(photo);

            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async getPhoto(id) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PHOTOS_STORE], 'readonly');
            const store = transaction.objectStore(PHOTOS_STORE);
            const request = store.get(id);

            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async getAllPhotos() {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PHOTOS_STORE], 'readonly');
            const store = transaction.objectStore(PHOTOS_STORE);
            const request = store.getAll();

            request.onsuccess = () => {
                const photos = request.result;
                // Sort by timestamp descending
                photos.sort((a, b) => b.timestamp - a.timestamp);
                resolve(photos);
            };
            request.onerror = () => reject(request.error);
        });
    }

    async getPhotosByType(type) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PHOTOS_STORE], 'readonly');
            const store = transaction.objectStore(PHOTOS_STORE);
            const index = store.index('type');
            const request = index.getAll(type);

            request.onsuccess = () => {
                const photos = request.result;
                photos.sort((a, b) => b.timestamp - a.timestamp);
                resolve(photos);
            };
            request.onerror = () => reject(request.error);
        });
    }

    async deletePhoto(id) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([PHOTOS_STORE], 'readwrite');
            const store = transaction.objectStore(PHOTOS_STORE);
            const request = store.delete(id);

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    // Milestone operations
    async addMilestone(milestone) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([MILESTONES_STORE], 'readwrite');
            const store = transaction.objectStore(MILESTONES_STORE);
            const request = store.add(milestone);

            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async getMilestone(id) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([MILESTONES_STORE], 'readonly');
            const store = transaction.objectStore(MILESTONES_STORE);
            const request = store.get(id);

            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async getAllMilestones() {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([MILESTONES_STORE], 'readonly');
            const store = transaction.objectStore(MILESTONES_STORE);
            const request = store.getAll();

            request.onsuccess = () => {
                const milestones = request.result;
                // Sort by timestamp descending
                milestones.sort((a, b) => b.timestamp - a.timestamp);
                resolve(milestones);
            };
            request.onerror = () => reject(request.error);
        });
    }

    async updateMilestone(milestone) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([MILESTONES_STORE], 'readwrite');
            const store = transaction.objectStore(MILESTONES_STORE);
            const request = store.put(milestone);

            request.onsuccess = () => resolve(request.result);
            request.onerror = () => reject(request.error);
        });
    }

    async deleteMilestone(id) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([MILESTONES_STORE], 'readwrite');
            const store = transaction.objectStore(MILESTONES_STORE);
            const request = store.delete(id);

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    // Settings operations
    async getSetting(key) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([SETTINGS_STORE], 'readonly');
            const store = transaction.objectStore(SETTINGS_STORE);
            const request = store.get(key);

            request.onsuccess = () => resolve(request.result ? request.result.value : null);
            request.onerror = () => reject(request.error);
        });
    }

    async setSetting(key, value) {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction([SETTINGS_STORE], 'readwrite');
            const store = transaction.objectStore(SETTINGS_STORE);
            const request = store.put({ key, value });

            request.onsuccess = () => resolve();
            request.onerror = () => reject(request.error);
        });
    }

    // Export all data
    async exportData() {
        const photos = await this.getAllPhotos();
        const milestones = await this.getAllMilestones();
        const theme = await this.getSetting('theme');
        const lockEnabled = await this.getSetting('lockEnabled');

        return {
            version: 1,
            exportDate: new Date().toISOString(),
            photos,
            milestones,
            settings: {
                theme,
                lockEnabled
            }
        };
    }

    // Import data
    async importData(data) {
        // Clear existing data
        await this.clearAllData();

        // Import photos
        if (data.photos && Array.isArray(data.photos)) {
            for (const photo of data.photos) {
                await this.addPhoto(photo);
            }
        }

        // Import milestones
        if (data.milestones && Array.isArray(data.milestones)) {
            for (const milestone of data.milestones) {
                await this.addMilestone(milestone);
            }
        }

        // Import settings
        if (data.settings) {
            if (data.settings.theme) {
                await this.setSetting('theme', data.settings.theme);
            }
            if (data.settings.lockEnabled !== undefined) {
                await this.setSetting('lockEnabled', data.settings.lockEnabled);
            }
        }
    }

    // Clear all data
    async clearAllData() {
        return new Promise((resolve, reject) => {
            const transaction = this.db.transaction(
                [PHOTOS_STORE, MILESTONES_STORE, SETTINGS_STORE],
                'readwrite'
            );

            const photosStore = transaction.objectStore(PHOTOS_STORE);
            const milestonesStore = transaction.objectStore(MILESTONES_STORE);
            const settingsStore = transaction.objectStore(SETTINGS_STORE);

            photosStore.clear();
            milestonesStore.clear();
            settingsStore.clear();

            transaction.oncomplete = () => resolve();
            transaction.onerror = () => reject(transaction.error);
        });
    }

    // Helper: Generate UUID
    static generateUUID() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            const r = Math.random() * 16 | 0;
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    // Helper: Get epoch day from date
    static getEpochDay(date) {
        return Math.floor(date.getTime() / (1000 * 60 * 60 * 24));
    }
}

// Create global database instance
const db = new Database();
