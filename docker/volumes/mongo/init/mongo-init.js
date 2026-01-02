// 1. Switch to the 'admin' database to create centralized users
db = db.getSiblingDB('admin');

// --- Global/Admin Users ---

// Create user 'wd' with access to both functional databases
db.createUser({
  user: "wd",
  pwd: "wd",
  roles: [
    { role: "readWrite", db: "tdk-settings-db" },
    { role: "readWrite", db: "tdk-healthcare-db" }
  ]
});

// Create user 'me' with administrative and broad read/write permissions
db.createUser({
  user: "me",
  pwd: "me",
  roles: [
    { role: "readWrite", db: "tdk-settings-db" },
    { role: "readWrite", db: "tdk-healthcare-db" },
    { role: "readAnyDatabase", db: "admin" }
  ]
});

// --- Dedicated Database Users (Restricted) ---

// Create a dedicated user for tdk-settings-db only
db.createUser({
  user: "user_settings",
  pwd: "password_settings",
  roles: [
    { role: "readWrite", db: "tdk-settings-db" }
  ]
});

// Create a dedicated user for tdk-healthcare-db only
db.createUser({
  user: "user_healthcare",
  pwd: "password_healthcare",
  roles: [
    { role: "readWrite", db: "tdk-healthcare-db" }
  ]
});

// 2. Initialize the 'tdk-settings-db' database and its collections
db = db.getSiblingDB('tdk-settings-db');
db.createCollection('configurations');
db.createCollection('configurationSchemas');
db.createCollection('products');

// 3. Initialize the 'tdk-healthcare-db' database and its collections
db = db.getSiblingDB('tdk-healthcare-db');
db.createCollection('patients');
db.createCollection('locations');
