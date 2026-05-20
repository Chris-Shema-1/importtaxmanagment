-- ============================================================
-- SEED DATA — import_tax_management_system_db (MySQL)
-- Run this AFTER the server has started once (so hbm2ddl
-- creates all tables), or after running the schema manually.
-- ============================================================

-- Clear existing data (order respects FK constraints)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payments;
TRUNCATE TABLE invoices;
TRUNCATE TABLE import_items;
TRUNCATE TABLE taxes;
TRUNCATE TABLE notifications;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- ── USERS ──────────────────────────────────────────────────
-- Passwords are stored as plain text (matching app behaviour)
INSERT INTO users (full_name, email, username, password, role, created_at) VALUES
  ('Admin User',        'admin@importtax.com',    'admin',    'admin123',    'ADMIN',   '2024-01-10'),
  ('Maria Santos',      'maria@importtax.com',    'maria',    'maria123',    'MANAGER', '2024-02-14'),
  ('James Okonkwo',     'james@importtax.com',    'james',    'james123',    'OFFICER', '2024-03-01'),
  ('Priya Nair',        'priya@importtax.com',    'priya',    'priya123',    'OFFICER', '2024-03-15'),
  ('Carlos Mendez',     'carlos@importtax.com',   'carlos',   'carlos123',   'VIEWER',  '2024-04-20'),
  ('Aisha Kamara',      'aisha@importtax.com',    'aisha',    'aisha123',    'MANAGER', '2024-05-05'),
  ('Tom Eriksson',      'tom@importtax.com',      'tom',      'tom123',      'OFFICER', '2024-06-11'),
  ('Fatima Al-Hassan',  'fatima@importtax.com',   'fatima',   'fatima123',   'VIEWER',  '2024-07-22');

-- ── TAXES ──────────────────────────────────────────────────
INSERT INTO taxes (tax_name, tax_rate, description) VALUES
  ('Standard Import Duty',      15.00, 'Default duty applied to most imported goods'),
  ('Electronics Surcharge',     20.00, 'Additional levy on consumer electronics'),
  ('Agricultural Exemption',     5.00, 'Reduced rate for agricultural equipment'),
  ('Luxury Goods Tax',          35.00, 'High-value items such as jewellery and watches'),
  ('Pharmaceutical Relief',      2.50, 'Medicines and medical devices'),
  ('Textile Import Duty',       12.00, 'Clothing, fabrics and related materials'),
  ('Automotive Parts Duty',     18.00, 'Vehicle components and spare parts'),
  ('Chemical Products Tax',     22.00, 'Industrial and consumer chemicals'),
  ('Food & Beverage Duty',       8.00, 'Processed food and beverages'),
  ('Raw Materials Relief',       3.00, 'Unprocessed raw materials for manufacturing');

-- ── IMPORT ITEMS ───────────────────────────────────────────
INSERT INTO import_items
  (item_name, category, description, quantity, unit_price, country_of_origin,
   importer_name, tax_rate, total_tax, import_date, status, user_id)
VALUES
  ('Samsung 65" QLED TV',       'Electronics',    '4K Smart TV with HDR',              50,  1200.00, 'South Korea',  'TechWorld Imports',      20.00,  12000.00, '2025-01-05', 'CLEARED',  1),
  ('iPhone 15 Pro Max',         'Electronics',    'Apple flagship smartphone',         200,  1099.00, 'China',        'AppleZone Ltd',          20.00,  43960.00, '2025-01-12', 'CLEARED',  2),
  ('Toyota Camry Parts Kit',    'Automotive',     'OEM engine and body parts',          30,  3500.00, 'Japan',        'AutoParts Global',       18.00,  18900.00, '2025-01-20', 'CLEARED',  3),
  ('Organic Cotton Fabric',     'Textiles',       '100% organic, 200 GSM',            500,    12.50, 'India',        'FabricHouse Co.',        12.00,    750.00, '2025-02-03', 'CLEARED',  4),
  ('Paracetamol 500mg Bulk',    'Pharmaceuticals','Generic paracetamol tablets',      1000,     0.80, 'Germany',      'MedSupply GmbH',          2.50,     20.00, '2025-02-10', 'CLEARED',  1),
  ('Arabica Coffee Beans',      'Food & Beverage','Premium single-origin beans',       300,     8.00, 'Ethiopia',     'CoffeeTrade Africa',      8.00,    192.00, '2025-02-18', 'CLEARED',  2),
  ('Industrial Solvent X200',   'Chemicals',      'High-purity industrial solvent',    100,    45.00, 'Netherlands',  'ChemImport BV',          22.00,    990.00, '2025-03-01', 'CLEARED',  3),
  ('Rolex Submariner Watch',    'Luxury Goods',   'Swiss luxury dive watch',            10, 12000.00, 'Switzerland',  'LuxuryTime Imports',     35.00,  42000.00, '2025-03-08', 'CLEARED',  4),
  ('Wheat Harvester Attachment','Agriculture',    'Combine harvester header unit',       5,  8500.00, 'USA',          'AgriMach International',  5.00,   2125.00, '2025-03-15', 'CLEARED',  1),
  ('Dell XPS 15 Laptops',       'Electronics',    'High-performance business laptops', 100,  1800.00, 'China',        'TechWorld Imports',      20.00,  36000.00, '2025-03-22', 'CLEARED',  2),
  ('Silk Saree Collection',     'Textiles',       'Hand-woven pure silk sarees',       200,    95.00, 'India',        'FabricHouse Co.',        12.00,   2280.00, '2025-04-01', 'CLEARED',  3),
  ('BMW 3-Series Brake Pads',   'Automotive',     'OEM brake pad set',                 150,    85.00, 'Germany',      'AutoParts Global',       18.00,   2295.00, '2025-04-10', 'CLEARED',  4),
  ('Insulin Vials 10ml',        'Pharmaceuticals','Human insulin for diabetes',        500,    15.00, 'Denmark',      'MedSupply GmbH',          2.50,    187.50, '2025-04-18', 'CLEARED',  1),
  ('Olive Oil Extra Virgin',    'Food & Beverage','Cold-pressed, 5L bottles',          400,    22.00, 'Spain',        'MedFoods Import',         8.00,    704.00, '2025-04-25', 'CLEARED',  2),
  ('Epoxy Resin Industrial',    'Chemicals',      'Two-part structural epoxy',          80,    60.00, 'USA',          'ChemImport BV',          22.00,   1056.00, '2025-05-02', 'CLEARED',  3),
  ('Diamond Jewellery Set',     'Luxury Goods',   '18K gold with VS1 diamonds',          5, 25000.00, 'Belgium',      'LuxuryTime Imports',     35.00,  43750.00, '2025-05-09', 'PENDING',  4),
  ('Irrigation Pump System',    'Agriculture',    'Solar-powered drip irrigation',      20,  1200.00, 'Israel',       'AgriMach International',  5.00,   1200.00, '2025-05-14', 'PENDING',  1),
  ('Sony PlayStation 5',        'Electronics',    'Next-gen gaming console',           300,   550.00, 'Japan',        'AppleZone Ltd',          20.00,  33000.00, '2025-05-18', 'PENDING',  2),
  ('Denim Jeans Bulk',          'Textiles',       'Blue denim, assorted sizes',        800,    18.00, 'Bangladesh',   'FabricHouse Co.',        12.00,   1728.00, '2025-05-22', 'PENDING',  3),
  ('Vitamin C Supplements',     'Pharmaceuticals','1000mg effervescent tablets',       600,     2.50, 'China',        'MedSupply GmbH',          2.50,     37.50, '2025-05-25', 'PENDING',  4),
  ('Whisky 12-Year Single Malt','Food & Beverage','Scottish single malt, 700ml',       200,    65.00, 'Scotland',     'CoffeeTrade Africa',      8.00,   1040.00, '2025-05-28', 'PENDING',  1),
  ('Acetone Industrial Grade',  'Chemicals',      'High-purity acetone solvent',       120,    30.00, 'Germany',      'ChemImport BV',          22.00,    792.00, '2025-06-01', 'PENDING',  2),
  ('Porsche 911 Turbo Parts',   'Automotive',     'Genuine Porsche performance parts',  10,  9500.00, 'Germany',      'AutoParts Global',       18.00,  17100.00, '2025-06-05', 'HOLD',     3),
  ('Saffron Spice Premium',     'Food & Beverage','Grade-1 Iranian saffron, 1kg',       50,   800.00, 'Iran',         'MedFoods Import',         8.00,   3200.00, '2025-06-08', 'HOLD',     4),
  ('MacBook Pro M3 Max',        'Electronics',    'Apple professional laptop',          80,  3499.00, 'China',        'TechWorld Imports',      20.00,  55984.00, '2025-06-10', 'PENDING',  1);

-- ── INVOICES ───────────────────────────────────────────────
INSERT INTO invoices (invoice_number, total_tax_amount, issue_date) VALUES
  ('INV-2025-001',  12000.00, '2025-01-06'),
  ('INV-2025-002',  43960.00, '2025-01-13'),
  ('INV-2025-003',  18900.00, '2025-01-21'),
  ('INV-2025-004',    750.00, '2025-02-04'),
  ('INV-2025-005',     20.00, '2025-02-11'),
  ('INV-2025-006',    192.00, '2025-02-19'),
  ('INV-2025-007',    990.00, '2025-03-02'),
  ('INV-2025-008',  42000.00, '2025-03-09'),
  ('INV-2025-009',   2125.00, '2025-03-16'),
  ('INV-2025-010',  36000.00, '2025-03-23'),
  ('INV-2025-011',   2280.00, '2025-04-02'),
  ('INV-2025-012',   2295.00, '2025-04-11'),
  ('INV-2025-013',    187.50, '2025-04-19'),
  ('INV-2025-014',    704.00, '2025-04-26'),
  ('INV-2025-015',   1056.00, '2025-05-03');

-- ── PAYMENTS ───────────────────────────────────────────────
INSERT INTO payments (amount_paid, payment_date, payment_method, payment_status, invoice_id) VALUES
  (12000.00, '2025-01-08',  'BANK_TRANSFER', 'COMPLETED', 1),
  (43960.00, '2025-01-15',  'BANK_TRANSFER', 'COMPLETED', 2),
  (18900.00, '2025-01-23',  'CREDIT_CARD',   'COMPLETED', 3),
  (  750.00, '2025-02-06',  'CASH',          'COMPLETED', 4),
  (   20.00, '2025-02-13',  'CASH',          'COMPLETED', 5),
  (  192.00, '2025-02-21',  'BANK_TRANSFER', 'COMPLETED', 6),
  (  990.00, '2025-03-04',  'CHEQUE',        'COMPLETED', 7),
  (42000.00, '2025-03-11',  'BANK_TRANSFER', 'COMPLETED', 8),
  ( 2125.00, '2025-03-18',  'BANK_TRANSFER', 'COMPLETED', 9),
  (36000.00, '2025-03-25',  'CREDIT_CARD',   'COMPLETED', 10),
  ( 2280.00, '2025-04-04',  'BANK_TRANSFER', 'COMPLETED', 11),
  ( 2295.00, '2025-04-13',  'CHEQUE',        'COMPLETED', 12),
  (  187.50, '2025-04-21',  'CASH',          'COMPLETED', 13),
  (  704.00, '2025-04-28',  'BANK_TRANSFER', 'COMPLETED', 14),
  ( 1056.00, '2025-05-05',  'CREDIT_CARD',   'PENDING',   15);

-- ── NOTIFICATIONS ──────────────────────────────────────────
INSERT INTO notifications (message, notification_type, recipient, sent_at, status) VALUES
  ('Import batch INV-2025-001 cleared by customs.',                    'INFO',    'admin',  '2025-01-08', 'READ'),
  ('Payment of $43,960 received for INV-2025-002.',                   'INFO',    'maria',  '2025-01-15', 'READ'),
  ('INV-2025-003 payment confirmed via credit card.',                  'INFO',    'james',  '2025-01-23', 'READ'),
  ('Tax rate for Electronics updated to 20%.',                         'SYSTEM',  'admin',  '2025-02-01', 'READ'),
  ('Paracetamol bulk shipment cleared — low tax applied.',             'INFO',    'priya',  '2025-02-13', 'READ'),
  ('New user Carlos Mendez registered as VIEWER.',                     'SYSTEM',  'admin',  '2025-02-20', 'READ'),
  ('Luxury goods shipment (Rolex) requires additional documentation.', 'WARNING', 'admin',  '2025-03-08', 'READ'),
  ('INV-2025-008 payment of $42,000 processed successfully.',          'INFO',    'aisha',  '2025-03-11', 'READ'),
  ('System backup completed successfully.',                            'SYSTEM',  'admin',  '2025-03-20', 'READ'),
  ('Diamond jewellery shipment placed on HOLD — awaiting valuation.',  'ALERT',   'admin',  '2025-05-09', 'UNREAD'),
  ('Irrigation pump import pending customs clearance.',                'WARNING', 'james',  '2025-05-14', 'UNREAD'),
  ('PlayStation 5 batch (300 units) awaiting duty payment.',           'WARNING', 'maria',  '2025-05-18', 'UNREAD'),
  ('Porsche parts shipment flagged for inspection.',                   'ALERT',   'admin',  '2025-06-05', 'UNREAD'),
  ('Saffron import on HOLD — origin documentation required.',          'ALERT',   'fatima', '2025-06-08', 'UNREAD'),
  ('MacBook Pro M3 Max batch received — pending clearance.',           'INFO',    'tom',    '2025-06-10', 'UNREAD'),
  ('Monthly tax report generated for May 2025.',                       'SYSTEM',  'admin',  '2025-06-01', 'READ'),
  ('INV-2025-015 payment still PENDING — follow up required.',         'WARNING', 'admin',  '2025-05-10', 'UNREAD'),
  ('New tax rule: Pharmaceutical Relief reduced to 2.5%.',             'SYSTEM',  'priya',  '2025-04-15', 'READ'),
  ('User Aisha Kamara promoted to MANAGER role.',                      'SYSTEM',  'admin',  '2025-05-05', 'READ'),
  ('Quarterly audit scheduled for July 2025.',                         'SYSTEM',  'admin',  '2025-06-12', 'UNREAD');
