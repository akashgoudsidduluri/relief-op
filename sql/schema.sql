-- Relief-OP: Unified Database Decision Engine [V9.5]
-- Goal: 100% logic enforcement in SQL layer

DROP DATABASE IF EXISTS reliefops;
CREATE DATABASE reliefops;
USE reliefops;

-- ---------------------------------------------------------
-- 1. TABLES & SCHEMA
-- ---------------------------------------------------------

CREATE TABLE Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'Operator', 'Coordinator') NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE Victims (
    victim_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(150) NOT NULL,
    disaster_type ENUM('Flood', 'Earthquake', 'Fire', 'Storm', 'Tsunami') NOT NULL,
    severity_level INT DEFAULT 1 CHECK (severity_level BETWEEN 1 AND 5),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    contact_number VARCHAR(15),
    is_isolated BOOLEAN DEFAULT FALSE, -- Flag for infrastructure disruption
    UNIQUE(name, contact_number) -- Unique Identity Constraint
);

CREATE TABLE Shelters (
    shelter_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(150) NOT NULL,
    capacity INT NOT NULL CHECK (capacity > 0),
    occupancy INT DEFAULT 0 CHECK (occupancy >= 0),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8)
);

CREATE TABLE Resources (
    resource_id INT PRIMARY KEY AUTO_INCREMENT,
    resource_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    unit VARCHAR(30) NOT NULL
);

CREATE TABLE Requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    victim_id INT,
    resource_id INT,
    quantity_requested INT NOT NULL CHECK (quantity_requested > 0),
    remaining_quantity INT DEFAULT 0 CHECK (remaining_quantity >= 0),
    severity_level INT DEFAULT 1 CHECK (severity_level BETWEEN 1 AND 5),
    status ENUM('Pending', 'Approved', 'Fulfilled', 'Partial', 'Rejected') DEFAULT 'Pending',
    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (victim_id) REFERENCES Victims(victim_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES Resources(resource_id) ON DELETE SET NULL
);

CREATE TABLE Personnel (
    person_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    role_type VARCHAR(50) NOT NULL,
    assigned_shelter_id INT,
    FOREIGN KEY (assigned_shelter_id) REFERENCES Shelters(shelter_id) ON DELETE SET NULL
);

CREATE TABLE Suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    resource_id INT,
    quantity_supplied INT NOT NULL CHECK (quantity_supplied > 0),
    received_at_shelter_id INT, -- Realistic Coastal Flow: Direct delivery to shelter
    supply_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (resource_id) REFERENCES Resources(resource_id) ON DELETE SET NULL,
    FOREIGN KEY (received_at_shelter_id) REFERENCES Shelters(shelter_id) ON DELETE SET NULL
);

CREATE TABLE ShelterResources (
    shelter_id INT,
    resource_id INT,
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    PRIMARY KEY (shelter_id, resource_id),
    FOREIGN KEY (shelter_id) REFERENCES Shelters(shelter_id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES Resources(resource_id) ON DELETE CASCADE
);

CREATE TABLE SystemSettings (
    setting_key VARCHAR(50) PRIMARY KEY,
    setting_value VARCHAR(100) NOT NULL
);

CREATE TABLE Logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    action_type ENUM('REQUEST_FULFILLED', 'REQUEST_PARTIAL', 'REQUEST_REJECTED', 'SYSTEM_UPDATE') NOT NULL,
    resource_id INT,
    qty_before INT,
    qty_after INT,
    old_status VARCHAR(100),
    new_status VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE SET NULL
);

-- ---------------------------------------------------------
-- 2. ENFORCEMENT LAYER (TRIGGERS)
-- ---------------------------------------------------------

-- Enforce Shelter Capacity (Safety for MySQL version variance)
DELIMITER //
CREATE TRIGGER trig_validate_shelter_capacity
BEFORE UPDATE ON Shelters
FOR EACH ROW
BEGIN
    IF NEW.occupancy > NEW.capacity THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERR: Occupancy cannot exceed shelter capacity.';
    END IF;
END;
//
DELIMITER ;

-- Prevent Negative Stock (Safety Net)
DELIMITER //
CREATE TRIGGER trig_prevent_negative_stock
BEFORE UPDATE ON Resources
FOR EACH ROW
BEGIN
    IF NEW.quantity < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERR: Resource quantity cannot be negative.';
    END IF;
END;
//
-- Safety Net for Regional Stock
CREATE TRIGGER trig_prevent_negative_shelter_stock
BEFORE UPDATE ON ShelterResources
FOR EACH ROW
BEGIN
    IF NEW.quantity < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERR: Regional shelter stock cannot be negative.';
    END IF;
END;
//
-- Automatic Distributed Sync from Suppliers
CREATE TRIGGER trig_sync_shelter_stock_on_supply 
AFTER INSERT ON Suppliers 
FOR EACH ROW
BEGIN
    -- 1. Update Global Pool
    UPDATE Resources SET quantity = quantity + NEW.quantity_supplied 
    WHERE resource_id = NEW.resource_id;

    -- 2. Update Regional Pool (Directly at receiving shelter)
    IF NEW.received_at_shelter_id IS NOT NULL THEN
        INSERT INTO ShelterResources (shelter_id, resource_id, quantity)
        VALUES (NEW.received_at_shelter_id, NEW.resource_id, NEW.quantity_supplied)
        ON DUPLICATE KEY UPDATE quantity = quantity + NEW.quantity_supplied;
    END IF;
END;
//
DELIMITER ;

-- ---------------------------------------------------------
-- Integrated Forensic Logging (Triggers for Manual Actions)
-- ---------------------------------------------------------

DELIMITER //
CREATE TRIGGER trig_audit_victim_change AFTER UPDATE ON Victims FOR EACH ROW
BEGIN
    INSERT INTO Logs (user_id, action_type, old_status, new_status)
    VALUES (@current_user_id, 'SYSTEM_UPDATE', CONCAT('Victim Update: ', OLD.name), CONCAT('To: ', NEW.name));
END;
//
CREATE TRIGGER trig_audit_shelter_change AFTER UPDATE ON Shelters FOR EACH ROW
BEGIN
    INSERT INTO Logs (user_id, action_type, old_status, new_status)
    VALUES (@current_user_id, 'SYSTEM_UPDATE', CONCAT('Shelter Occupancy: ', OLD.occupancy), CONCAT('To: ', NEW.occupancy));
END;
//
CREATE TRIGGER trig_audit_personnel_add AFTER INSERT ON Personnel FOR EACH ROW
BEGIN
    INSERT INTO Logs (user_id, action_type, new_status)
    VALUES (@current_user_id, 'SYSTEM_UPDATE', CONCAT('New Personnel: ', NEW.name));
END;
//
DELIMITER ;

-- ---------------------------------------------------------
-- 3. DECISION ENGINE (STORED PROCEDURES)
-- ---------------------------------------------------------

DELIMITER //
CREATE PROCEDURE process_request(IN req_id INT)
BEGIN
    DECLARE res_id INT;
    DECLARE qty_req INT;
    DECLARE qty_avail_global INT;
    DECLARE target_shelter_id INT;
    DECLARE partial_qty INT;
    DECLARE old_status_val VARCHAR(20);

    -- 1. Session Guard
    IF @current_user_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERR: Database session not set.';
    END IF;

    START TRANSACTION;

    -- 2. Transactional Lock & Detection
    SELECT resource_id, quantity_requested, status INTO res_id, qty_req, old_status_val
    FROM Requests WHERE request_id = req_id FOR UPDATE;

    IF res_id IS NULL OR old_status_val NOT IN ('Pending', 'Approved') THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'ERR: Invalid request state.';
    END IF;

    -- 3. GLOBAL VS LOCAL CHECK
    SELECT quantity INTO qty_avail_global FROM Resources WHERE resource_id = res_id FOR UPDATE;

    -- 4. SMART SELECTION: Find a shelter that has this resource
    SELECT shelter_id INTO target_shelter_id 
    FROM ShelterResources 
    WHERE resource_id = res_id AND quantity > 0 
    ORDER BY quantity DESC LIMIT 1 FOR UPDATE;

    IF qty_avail_global >= qty_req AND target_shelter_id IS NOT NULL THEN
        -- Full Fulfillment (Multi-layer deduction)
        UPDATE Resources SET quantity = quantity - qty_req WHERE resource_id = res_id;
        UPDATE ShelterResources SET quantity = quantity - qty_req 
        WHERE shelter_id = target_shelter_id AND resource_id = res_id;
        
        UPDATE Requests SET status = 'Fulfilled', remaining_quantity = 0 WHERE request_id = req_id;
        
        INSERT INTO Logs (user_id, action_type, resource_id, qty_before, qty_after, old_status, new_status)
        VALUES (@current_user_id, 'REQUEST_FULFILLED', res_id, qty_avail_global, (qty_avail_global - qty_req), old_status_val, 'Fulfilled');

    ELSEIF qty_avail_global > 0 AND target_shelter_id IS NOT NULL THEN
        -- Partial Fulfillment
        SELECT LEAST(qty_avail_global, (SELECT quantity FROM ShelterResources WHERE shelter_id = target_shelter_id AND resource_id = res_id)) 
        INTO partial_qty;

        UPDATE Resources SET quantity = quantity - partial_qty WHERE resource_id = res_id;
        UPDATE ShelterResources SET quantity = quantity - partial_qty 
        WHERE shelter_id = target_shelter_id AND resource_id = res_id;

        UPDATE Requests SET status = 'Partial', remaining_quantity = (qty_req - partial_qty) WHERE request_id = req_id;
        
        INSERT INTO Logs (user_id, action_type, resource_id, qty_before, qty_after, old_status, new_status)
        VALUES (@current_user_id, 'REQUEST_PARTIAL', res_id, qty_avail_global, (qty_avail_global - partial_qty), old_status_val, 'Partial');
    ELSE
        -- Insufficient Distributed Supply
        UPDATE Requests SET status = 'Rejected' WHERE request_id = req_id;
        INSERT INTO Logs (user_id, action_type, resource_id, qty_before, qty_after, old_status, new_status)
        VALUES (@current_user_id, 'REQUEST_REJECTED', res_id, qty_avail_global, qty_avail_global, old_status_val, 'Rejected');
    END IF;

    COMMIT;
END;
//
DELIMITER ;

-- Best-Effort Atomic Batch Processor
DELIMITER //
CREATE PROCEDURE process_all_high_priority()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE req_id INT;
    DECLARE cur CURSOR FOR 
        SELECT request_id FROM Requests 
        WHERE status IN ('Pending', 'Approved') AND severity_level >= 4 
        ORDER BY severity_level DESC, request_time ASC 
        LIMIT 20;
    
    -- Continue on individual failures + Log failures
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION 
    BEGIN
        INSERT INTO Logs (user_id, action_type, new_status) 
        VALUES (@current_user_id, 'SYSTEM_UPDATE', 'BATCH_STEP_FAILED');
    END;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO req_id;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        CALL process_request(req_id);
    END LOOP;
    CLOSE cur;
END;
//
DELIMITER ;

-- ---------------------------------------------------------
-- 4. INSIGHTS LAYER (VIEWS)
-- ---------------------------------------------------------

-- Live Priority Queue
CREATE VIEW view_priority_requests AS
SELECT r.request_id, v.name AS victim, res.resource_name, r.quantity_requested, r.severity_level, r.status
FROM Requests r
JOIN Victims v ON r.victim_id = v.victim_id
JOIN Resources res ON r.resource_id = res.resource_id
WHERE r.status IN ('Pending', 'Approved')
ORDER BY r.severity_level DESC, r.request_time ASC;

-- Proactive Monitoring: Low Stock Alerts
CREATE VIEW view_low_stock_alerts AS
SELECT resource_name, quantity, unit
FROM Resources
WHERE quantity < (SELECT CAST(setting_value AS UNSIGNED) FROM SystemSettings WHERE setting_key = 'low_stock_threshold');

-- Shelter Utilization Analytics (Fixed Precision)
CREATE VIEW view_shelter_utilization AS
SELECT name, capacity, occupancy,
       (occupancy * 100.0 / capacity) AS utilization_percent
FROM Shelters;

-- Proactive Trends: Top 3 Most Demanded Resources
CREATE VIEW view_demand_trends AS
SELECT res.resource_name, SUM(r.quantity_requested) as total_demand
FROM Requests r
JOIN Resources res ON r.resource_id = res.resource_id
GROUP BY res.resource_id
ORDER BY total_demand DESC
LIMIT 3;

-- Predictive Pressure Indicator (Current Demand vs Current Stock)
CREATE VIEW view_current_stock_pressure AS
SELECT res.resource_name, 
       (SUM(r.quantity_requested) * 100.0 / NULLIF(res.quantity, 0)) as pressure_index
FROM Requests r
JOIN Resources res ON r.resource_id = res.resource_id
WHERE r.status IN ('Pending', 'Approved')
GROUP BY res.resource_id;

-- Distribution Imbalance View (Strategic Insights)
CREATE VIEW view_shelter_inventory_distribution AS
SELECT s.name AS shelter_name, res.resource_name, sr.quantity,
       CASE 
           WHEN sr.quantity < 20 THEN 'CRITICAL_LOW'
           WHEN sr.quantity < 50 THEN 'BALANCED'
           ELSE 'SURPLUS'
       END AS stock_status
FROM ShelterResources sr
JOIN Shelters s ON sr.shelter_id = s.shelter_id
JOIN Resources res ON sr.resource_id = res.resource_id;

-- Strategic Focus: Isolated Victims (Infrastructure Disruption)
CREATE VIEW view_isolated_victims AS
SELECT victim_id, name, location, disaster_type, severity_level, contact_number
FROM Victims
WHERE is_isolated = TRUE;

-- Logistics Imbalance: High Occupancy or Critical Stock
CREATE VIEW view_shelter_inventory_imbalance AS
SELECT s.name AS shelter_name, s.occupancy, s.capacity,
       (s.occupancy * 100.0 / s.capacity) as occupancy_rate,
       COUNT(sr.resource_id) as critical_resources_count
FROM Shelters s
LEFT JOIN ShelterResources sr ON s.shelter_id = sr.shelter_id AND sr.quantity < 10
GROUP BY s.shelter_id
HAVING occupancy_rate > 90 OR critical_resources_count > 0;

-- ---------------------------------------------------------
-- 5. STRATEGIC HEATMAP VIEW (V10.4)
-- ---------------------------------------------------------
CREATE VIEW view_map_master_heat AS
SELECT 
    'VICTIM' as entity_type,
    name as label,
    latitude,
    longitude,
    severity_level as intensity,
    disaster_type as detail,
    is_isolated as pulse_flag
FROM Victims
WHERE latitude IS NOT NULL AND longitude IS NOT NULL

UNION ALL

SELECT 
    'SCARCITY' as entity_type,
    resource_name as label,
    NULL as latitude, -- Scarcity is mapped to nearest shelter in Java logic
    NULL as longitude,
    99 as intensity, -- Sentinel for scarcity
    CONCAT('SCARCITY: ', pressure_index, '%') as detail,
    1 as pulse_flag
FROM view_current_stock_pressure
WHERE pressure_index > 100;

-- ---------------------------------------------------------
-- 6. INITIALIZATION & DATA
-- ---------------------------------------------------------

INSERT INTO SystemSettings (setting_key, setting_value) VALUES ('low_stock_threshold', '15');

INSERT INTO Users (name, role, username, password) VALUES 
('Akash Admin', 'Admin', 'admin', 'admin123'),
('Operator One', 'Operator', 'operator', 'op123'),
('Coord Central', 'Coordinator', 'coord', 'coord123');

INSERT INTO Resources (resource_name, quantity, unit) VALUES 
('Bottled Water', 0, 'Litres'),
('Medical Kits', 0, 'Boxes'), 
('Canned Food', 0, 'Units');

INSERT INTO Shelters (name, location, capacity, occupancy, latitude, longitude) VALUES 
('Vizag Cyclone Shelter', 'Visakhapatnam, AP', 500, 120, 17.6868, 83.2185),
('Kakinada Safety Hub', 'East Godavari, AP', 300, 45, 16.9891, 82.2475),
('Guntur Relief Hub', 'Guntur, AP', 400, 10, 16.3067, 80.4365),
('Puri Relief Camp', 'Puri, Odisha', 800, 450, 19.8135, 85.8312),
('Berhampur Safehouse', 'Ganjam, Odisha', 350, 90, 19.3150, 84.7941),
('Balasore Storm Center', 'Balasore, Odisha', 500, 20, 21.4942, 86.9317),
('Chennai Coastal Safehouse', 'Marina Beach, TN', 300, 280, 13.0475, 80.2824),
('Cuddalore Hub', 'Tamil Nadu', 450, 150, 11.7480, 79.7714),
('Nagapattinam Camp', 'Tamil Nadu', 400, 310, 10.7672, 79.8444),
('Digha Storm Center', 'Midnapore, WB', 400, 50, 21.6266, 87.5074),
('Diamond Harbour Hub', 'West Bengal', 350, 12, 22.1896, 88.2014),
('Bhubaneswar Central Center', 'Odisha', 1000, 10, 20.2961, 85.8245);

INSERT INTO Victims (name, location, disaster_type, severity_level, is_isolated, latitude, longitude) VALUES 
('Stranded Group A', 'Near Puri Coast', 'Flood', 5, 1, 19.8220, 85.8400),
('Isolated Village X', 'Coastal Vizag', 'Storm', 4, 1, 17.7000, 83.2500),
('Family Rescue B', 'Downtown Chennai', 'Flood', 2, 0, 13.0500, 80.2700);

-- ---------------------------------------------------------
-- INITIAL PERSONNEL & SUPPLIES
-- ---------------------------------------------------------
INSERT INTO Personnel (name, role_type, assigned_shelter_id) VALUES 
('Vol John', 'Volunteer', 1), ('Vol Sarah', 'Volunteer', 2), ('NDRF Unit 1', 'Expert', 5);

INSERT INTO Suppliers (name, resource_id, quantity_supplied, received_at_shelter_id) VALUES 
('Red Cross India', 1, 1000, 2), -- 1000L Water to Puri
('Pharma Relief', 2, 500, 1);    -- 500 Kits to Vizag

INSERT INTO Requests (victim_id, resource_id, quantity_requested, severity_level) VALUES 
(1, 1, 50, 5),
(2, 2, 10, 4);