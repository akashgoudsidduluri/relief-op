-- Relief-OP: Sample Data for Testing
-- Run this AFTER schema.sql to populate test data
-- Usage: mysql -u root -p reliefops < sample_data.sql

USE reliefops;

-- Add more test victims for map visualization
INSERT INTO Victims (name, location, disaster_type, severity_level, is_isolated, latitude, longitude, contact_number) VALUES
('Victim Case 1', 'Puri Beach', 'Flood', 5, 1, 19.8135, 85.8312, '9876543210'),
('Victim Case 2', 'Visakhapatnam Port', 'Storm', 4, 1, 17.6868, 83.2185, '9876543211'),
('Victim Case 3', 'Kakinada Harbor', 'Tsunami', 5, 1, 16.9891, 82.2475, '9876543212'),
('Victim Case 4', 'Chennai Marina', 'Flood', 3, 0, 13.0475, 80.2824, '9876543213'),
('Victim Case 5', 'Cuddalore Port', 'Storm', 2, 0, 11.7480, 79.7714, '9876543214'),
('Victim Case 6', 'Nagapattinam', 'Tsunami', 4, 1, 10.7672, 79.8444, '9876543215'),
('Victim Case 7', 'Digha Beach', 'Cyclone', 3, 0, 21.6266, 87.5074, '9876543216'),
('Victim Case 8', 'Balasore Coast', 'Storm', 2, 0, 21.4942, 86.9317, '9876543217');

-- Add more requests
INSERT INTO Requests (victim_id, resource_id, quantity_requested, remaining_quantity, severity_level, notes) VALUES
(1, 1, 100, 50, 5, 'Critical water shortage at Puri coast'),
(2, 2, 50, 20, 4, 'Medical supplies needed urgently'),
(3, 3, 200, 100, 5, 'Food supplies for tsunami victims'),
(4, 1, 50, 25, 3, 'Water for Chennai relief'),
(5, 2, 30, 15, 2, 'Basic medical kits'),
(6, 3, 150, 80, 4, 'Emergency food supplies');

-- Add more resources with stock levels
UPDATE Resources SET quantity = 5000 WHERE resource_name = 'Bottled Water';
UPDATE Resources SET quantity = 2000 WHERE resource_name = 'Medical Kits';
UPDATE Resources SET quantity = 3000 WHERE resource_name = 'Canned Food';

-- Add shelter resources stock
INSERT INTO ShelterResources (shelter_id, resource_id, quantity) VALUES
(1, 1, 500),  -- Vizag: 500L water
(1, 2, 200),  -- Vizag: 200 med kits
(2, 1, 300),  -- Kakinada: 300L water
(2, 3, 400),  -- Kakinada: 400 canned food
(3, 1, 200),  -- Guntur: 200L water
(4, 3, 600),  -- Puri: 600 canned food
(5, 1, 250),  -- Berhampur: 250L water
(6, 2, 150);  -- Balasore: 150 med kits

-- Add more helpers and volunteers
INSERT INTO Personnel (name, role_type, assigned_shelter_id) VALUES
('Helper A', 'Helper', 1),
('Helper B', 'Helper', 2),
('Helper C', 'Helper', 3),
('Volunteer 1', 'Volunteer', 1),
('Volunteer 2', 'Volunteer', 2),
('Volunteer 3', 'Volunteer', 3),
('NDRF Expert 1', 'Expert', 4),
('NDRF Expert 2', 'Expert', 5);

-- Add more suppliers
INSERT INTO Suppliers (name, resource_id, quantity_supplied, received_at_shelter_id, supply_date) VALUES
('Red Cross India', 1, 2000, 1, NOW()),
('Doctors Without Borders', 2, 1000, 2, NOW()),
('Food For All NGO', 3, 3000, 3, NOW()),
('Indian Navy', 1, 1500, 4, NOW()),
('State Relief Fund', 2, 800, 5, NOW());

-- Sample audit logs (for forensic view)
INSERT INTO Logs (user_id, action_type, action_timestamp) VALUES
(1, 'REQUEST_FULFILLED', NOW()),
(2, 'REQUEST_PARTIAL', DATE_SUB(NOW(), INTERVAL 2 HOUR)),
(3, 'SYSTEM_UPDATE', DATE_SUB(NOW(), INTERVAL 5 HOUR)),
(1, 'REQUEST_REJECTED', DATE_SUB(NOW(), INTERVAL 1 DAY));
