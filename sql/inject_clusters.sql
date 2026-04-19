USE reliefops;
DELETE FROM Victims;

-- Puri, Odisha Cluster (10 points)
INSERT INTO Victims (name, location, disaster_type, severity_level, is_isolated, latitude, longitude) VALUES
('Puri Cluster 1', 'Puri Coast', 'Flood', 5, 1, 19.8200, 85.8300),
('Puri Cluster 2', 'Puri Coast', 'Flood', 5, 1, 19.8210, 85.8310),
('Puri Cluster 3', 'Puri Coast', 'Flood', 5, 1, 19.8220, 85.8320),
('Puri Cluster 4', 'Puri Coast', 'Flood', 4, 0, 19.8230, 85.8330),
('Puri Cluster 5', 'Puri Coast', 'Flood', 4, 0, 19.8240, 85.8340),
('Puri Cluster 6', 'Puri Coast', 'Flood', 3, 0, 19.8250, 85.8350),
('Puri Cluster 7', 'Puri Coast', 'Flood', 5, 1, 19.8260, 85.8360),
('Puri Cluster 8', 'Puri Coast', 'Flood', 4, 0, 19.8270, 85.8370),
('Puri Cluster 9', 'Puri Coast', 'Flood', 3, 0, 19.8280, 85.8380),
('Puri Cluster 10', 'Puri Coast', 'Flood', 5, 1, 19.8290, 85.8390);

-- Visakhapatnam, AP Cluster (8 points)
INSERT INTO Victims (name, location, disaster_type, severity_level, is_isolated, latitude, longitude) VALUES
('Vizag Cluster 1', 'Vizag Coast', 'Storm', 5, 1, 17.7000, 83.2500),
('Vizag Cluster 2', 'Vizag Coast', 'Storm', 4, 0, 17.7010, 83.2510),
('Vizag Cluster 3', 'Vizag Coast', 'Storm', 5, 1, 17.7020, 83.2520),
('Vizag Cluster 4', 'Vizag Coast', 'Storm', 3, 0, 17.7030, 83.2530),
('Vizag Cluster 5', 'Vizag Coast', 'Storm', 4, 0, 17.7040, 83.2540),
('Vizag Cluster 6', 'Vizag Coast', 'Storm', 5, 1, 17.7050, 83.2550),
('Vizag Cluster 7', 'Vizag Coast', 'Storm', 4, 0, 17.7060, 83.2560),
('Vizag Cluster 8', 'Vizag Coast', 'Storm', 3, 0, 17.7070, 83.2570);

-- Chennai, TN Cluster (5 points)
INSERT INTO Victims (name, location, disaster_type, severity_level, is_isolated, latitude, longitude) VALUES
('Chennai Cluster 1', 'Marina', 'Flood', 5, 1, 13.0470, 80.2820),
('Chennai Cluster 2', 'Marina', 'Flood', 4, 0, 13.0480, 80.2830),
('Chennai Cluster 3', 'Marina', 'Flood', 5, 1, 13.0490, 80.2840),
('Chennai Cluster 4', 'Marina', 'Flood', 3, 0, 13.0500, 80.2850),
('Chennai Cluster 5', 'Marina', 'Flood', 4, 0, 13.0510, 80.2860);
