-- Seed data for development and demo
-- All passwords: password123

-- Users (4 total: 1 admin, 2 engineers, 1 operator)
INSERT INTO users (username, email, password_hash, role) VALUES
('kgarcia',    'kgarcia@gridops.local',    '$2b$10$xhLVTBj2.IV9k4eI3yREN.TW/7Y0GzqJ5E0NRc76jlX5WlVZArxyy', 'ADMIN'),
('jsmith',     'jsmith@gridops.local',     '$2b$10$CTqmBWuTjqtDHCFrSE.94eKCdMYhHnRJP8y4iS0UpKo5OYOFIsvJm', 'ENGINEER'),
('mjones',     'mjones@gridops.local',     '$2b$10$i30eSQ982UfS0xGlzgLlu.qM3e9Kqqu3nn7.e1nJH.5gNV8y4Eh5G', 'ENGINEER'),
('rthompson',  'rthompson@gridops.local',  '$2b$10$xf5efC1PKSGaM2exS7MXqu81GbRIMU8fdMSWvB1n62QjocyDjCLHW', 'OPERATOR');

-- Assets (10 total, registered by kgarcia id=1)
INSERT INTO assets (asset_tag, name, asset_type, status, location, latitude, longitude, installed_date, created_by) VALUES
('SUB-PDX-001', 'St. Johns Substation',              'SUBSTATION',   'OPERATIONAL',  '8500 N Bradford St, Portland, OR 97203',    45.5907000, -122.7530000, '2008-06-14', 1),
('SUB-PDX-002', 'Sellwood Substation',               'SUBSTATION',   'OPERATIONAL',  '1707 SE Tacoma St, Portland, OR 97202',     45.4632000, -122.6554000, '2012-09-22', 1),
('TRF-PDX-001', 'St. Johns Transformer T1 115/13.8kV','TRANSFORMER', 'OPERATIONAL',  '8500 N Bradford St, Portland, OR 97203',    45.5907000, -122.7530000, '2008-06-14', 1),
('TRF-PDX-002', 'Sellwood Transformer T2 57/13.8kV', 'TRANSFORMER',  'DEGRADED',     '1707 SE Tacoma St, Portland, OR 97202',     45.4632000, -122.6554000, '2013-03-18', 1),
('LIN-PDX-001', 'Feeder 392 – NW Industrial',        'LINE_SEGMENT', 'OPERATIONAL',  'NW Front Ave, spans 1-22, Portland, OR',    45.5350000, -122.6730000, '2005-11-01', 1),
('LIN-PDX-002', 'Feeder 518 – SE Hawthorne',         'LINE_SEGMENT', 'MAINTENANCE',  'SE Hawthorne Blvd, spans 1-18, Portland, OR', 45.5120000, -122.6200000, '2007-04-20', 1),
('SWT-PDX-001', 'St. Johns Main Disconnect SW-101',  'SWITCH',       'OPERATIONAL',  '8500 N Bradford St, Portland, OR 97203',    45.5907000, -122.7530000, '2008-06-14', 1),
('SWT-PDX-002', 'Sellwood Recloser R-14',            'SWITCH',       'OPERATIONAL',  '1707 SE Tacoma St, Portland, OR 97202',     45.4632000, -122.6554000, '2016-02-28', 1),
('MTR-PDX-001', 'NW Naito Revenue Meter RM-0921',    'METER',        'OPERATIONAL',  '1400 NW Naito Pkwy, Portland, OR 97209',   45.5300000, -122.6780000, '2021-09-01', 1),
('MTR-PDX-002', 'SE Belmont AMI Meter SM-0412',      'METER',        'OFFLINE',      '3200 SE Belmont St, Portland, OR 97214',   45.5160000, -122.6400000, '2020-04-12', 1);

-- Asset inspections (6 total)
INSERT INTO asset_inspections (asset_id, inspected_by, inspection_date, notes, condition) VALUES
(1, 2, '2025-12-01 09:00:00-08', 'Annual substation inspection per NERC FAC-003. All protective relays tested and calibrated. Battery bank at 98% capacity. No deficiencies found.', 'GOOD'),
(2, 2, '2025-12-02 10:30:00-08', 'Annual substation inspection. Minor corrosion on 13.8kV bus bar connections. IR thermography shows 12°C rise on B-phase. Recommend re-torque at next outage window.', 'FAIR'),
(3, 3, '2025-11-15 08:00:00-08', 'Transformer oil sample collected for DGA analysis. Results within normal limits. Cooling fans operational. Bushing oil levels normal. No leaks detected.', 'GOOD'),
(4, 3, '2025-11-16 14:00:00-08', 'Oil temperature 78°C under 60% load — exceeds 72°C threshold. DGA shows elevated acetylene at 18 ppm. Recommend accelerated monitoring interval and maintenance scheduling.', 'POOR'),
(5, 2, '2025-10-20 07:30:00-07', 'Line clearance survey per OAR 860-024. Vegetation encroachment identified on span 14, clearance reduced to 2 ft from conductor. Notified vegetation management for priority trim.', 'FAIR'),
(6, 3, '2025-10-21 13:00:00-07', 'Line de-energized per switching order SO-2025-1842. Grounds applied at poles 6 and 18. Damaged splice at span 9 confirmed — conductor oxidation and reduced cross-section.', 'POOR');

-- Incidents (5 total, using sequence for incident numbers)
INSERT INTO incidents (incident_number, title, description, severity, status, asset_id, reported_by, assigned_to, resolution_notes, resolved_at) VALUES
('INC-' || LPAD(nextval('incident_number_seq')::text, 6, '0'),
 'Transformer oil temperature alarm – Sellwood T2',
 'SCADA alarm triggered at 14:22 PST. TRF-PDX-002 oil temperature reading 78°C at 60% load, exceeding 72°C operating threshold. Cooling fans confirmed running at maximum. DGA results from 11/16 inspection showed elevated acetylene (18 ppm). Potential winding insulation degradation.',
 'HIGH', 'IN_PROGRESS', 4, 4, 2, NULL, NULL),

('INC-' || LPAD(nextval('incident_number_seq')::text, 6, '0'),
 'AMI communication loss – SE Belmont cluster',
 'MTR-PDX-002 (SM-0412) has not reported interval data via RF mesh since 2026-01-10 08:00 PST. Last known reading within normal range. Adjacent meters reporting normally — suggests endpoint failure rather than collector issue. Customer has not reported outage.',
 'MEDIUM', 'ASSIGNED', 10, 4, 3, NULL, NULL),

('INC-' || LPAD(nextval('incident_number_seq')::text, 6, '0'),
 'Vegetation encroachment – Feeder 392 span 14',
 'Identified during routine line patrol on 2025-10-20. Douglas fir limbs within 2 ft of 13.8kV conductor at span 14 on Feeder 392. Minimum clearance per OAR 860-024 is 4 ft. Priority trim request submitted to vegetation management contractor.',
 'LOW', 'OPEN', 5, 2, NULL, NULL, NULL),

('INC-' || LPAD(nextval('incident_number_seq')::text, 6, '0'),
 'Scheduled splice replacement – Feeder 518 span 9',
 'Damaged splice identified during inspection on 2025-10-21. Conductor shows oxidation and reduced cross-section at span 9. Switching order SO-2025-1842 approved. Line to be de-energized and grounded for repair.',
 'MEDIUM', 'RESOLVED', 6, 3, 3, 'Splice replaced with full-tension compression connector per PGE standard ES-223. Line re-energized at 16:00 PST. Load test confirmed normal current flow. Switching order SO-2025-1842 closed.', '2026-01-15 16:00:00-08'),

('INC-' || LPAD(nextval('incident_number_seq')::text, 6, '0'),
 'Planned annual maintenance – St. Johns Substation',
 'Scheduled annual preventive maintenance for SUB-PDX-001 per maintenance plan MP-2026-SJ. Scope includes protective relay testing, battery bank load test, transformer oil sampling, breaker exercising, and ground grid integrity check.',
 'LOW', 'CLOSED', 1, 1, 2, 'All PM tasks completed per checklist MP-2026-SJ. Relay test results within tolerance. Battery bank passed 8-hour load test. Oil sample sent to lab. No corrective items identified.', '2026-01-20 12:00:00-08');

-- Update closed_at for the closed incident
UPDATE incidents SET closed_at = '2026-01-21 09:00:00-08' WHERE incident_number = 'INC-000005';

-- Incident history (audit trail for the seed incidents)
INSERT INTO incident_history (incident_id, changed_by, field_changed, old_value, new_value, change_note, created_at) VALUES
-- INC-000001: SCADA alarm reported by operator, supervisor assigns, engineer begins field response
(1, 4, 'status', NULL, 'OPEN', 'SCADA alarm received in control room', '2026-01-10 08:00:00-08'),
(1, 1, 'assigned_to', NULL, 'jsmith', 'Dispatched to Sellwood Substation', '2026-01-10 08:30:00-08'),
(1, 1, 'status', 'OPEN', 'ASSIGNED', NULL, '2026-01-10 08:30:00-08'),
(1, 2, 'status', 'ASSIGNED', 'IN_PROGRESS', 'On site at Sellwood. Verifying oil temperature readings and cooling system.', '2026-01-10 09:15:00-08'),

-- INC-000002: Operator notices data gap in AMI dashboard, supervisor assigns
(2, 4, 'status', NULL, 'OPEN', 'No interval data from SM-0412 in 48 hours', '2026-01-12 14:00:00-08'),
(2, 1, 'assigned_to', NULL, 'mjones', 'Assigned for field investigation', '2026-01-12 14:30:00-08'),
(2, 1, 'status', 'OPEN', 'ASSIGNED', NULL, '2026-01-12 14:30:00-08'),

-- INC-000003: Engineer finds vegetation issue during line patrol, logs directly
(3, 2, 'status', NULL, 'OPEN', 'Found during routine line patrol of Feeder 392', '2026-02-01 10:00:00-08'),

-- INC-000004: Full lifecycle for scheduled repair work
(4, 3, 'status', NULL, 'OPEN', 'Damaged splice confirmed during inspection — repair required', '2026-01-05 07:00:00-08'),
(4, 1, 'assigned_to', NULL, 'mjones', NULL, '2026-01-05 07:15:00-08'),
(4, 1, 'status', 'OPEN', 'ASSIGNED', 'Scheduled for next available outage window', '2026-01-05 07:15:00-08'),
(4, 3, 'status', 'ASSIGNED', 'IN_PROGRESS', 'Switching order SO-2025-1842 executed. Line de-energized and grounded.', '2026-01-14 06:00:00-08'),
(4, 3, 'status', 'IN_PROGRESS', 'RESOLVED', 'Splice replaced with compression connector per ES-223. Line re-energized and load tested.', '2026-01-15 16:00:00-08'),

-- INC-000005: Full lifecycle including supervisor close-out
(5, 1, 'status', NULL, 'OPEN', 'Annual PM window scheduled per maintenance plan MP-2026-SJ', '2025-12-15 09:00:00-08'),
(5, 1, 'assigned_to', NULL, 'jsmith', NULL, '2025-12-15 09:10:00-08'),
(5, 1, 'status', 'OPEN', 'ASSIGNED', NULL, '2025-12-15 09:10:00-08'),
(5, 2, 'status', 'ASSIGNED', 'IN_PROGRESS', 'Maintenance crew on site. Relay testing underway.', '2026-01-18 06:00:00-08'),
(5, 2, 'status', 'IN_PROGRESS', 'RESOLVED', 'All PM tasks completed per checklist MP-2026-SJ. No corrective items.', '2026-01-20 12:00:00-08'),
(5, 1, 'status', 'RESOLVED', 'CLOSED', 'Reviewed completion report. Results within tolerance. Closing.', '2026-01-21 09:00:00-08');
