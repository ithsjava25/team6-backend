DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM app_user WHERE github_login = 'test_resident') THEN
            RAISE NOTICE 'Warning: test_resident does not exist. Migration might be running in wrong order?';
        END IF;
    END $$;


INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, created_at, updated_at)
SELECT
    'Laundry room is broken',
    'The dryer in the laundry room is not working. It does not start at all and smells burnt.',
    'LAUNDRY_ROOM',
    'OPEN',
    id,
    NOW() - INTERVAL '5 days',
    NOW() - INTERVAL '5 days'
FROM app_user
WHERE github_login = 'test_resident'
ON CONFLICT DO NOTHING;

INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, created_at, updated_at)
SELECT
    'Noise from neighbor',
    'The neighbor upstairs plays loud music after midnight every night. Spoke to them but no improvement.',
    'NOISE_DISTURBANCE',
    'IN_PROGRESS',
    id,
    NOW() - INTERVAL '3 days',
    NOW() - INTERVAL '1 day'
FROM app_user
WHERE github_login = 'test_resident'
ON CONFLICT DO NOTHING;

INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, created_at, updated_at)
SELECT
    'Garbage in stairwell',
    'Someone has left garbage bags in the stairwell on floor 3. Smells bad and attracts insects.',
    'OTHER',
    'OPEN',
    id,
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
FROM app_user
WHERE github_login = 'test_resident'
ON CONFLICT DO NOTHING;


INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, created_at, updated_at)
SELECT
    'Broken window in entrance',
    'Someone has smashed the glass window in the front door. Glass shards all over the floor.',
    'DAMAGE',
    'RESOLVED',
    id,
    NOW() - INTERVAL '10 days',
    NOW() - INTERVAL '2 days'
FROM app_user
WHERE github_login = 'test_resident2'
ON CONFLICT DO NOTHING;

INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, created_at, updated_at)
SELECT
    'Bike rack is full',
    'There is no space for more bikes. Many abandoned bikes are left cluttering the area.',
    'OTHER',
    'CLOSED',
    id,
    NOW() - INTERVAL '7 days',
    NOW() - INTERVAL '5 days'
FROM app_user
WHERE github_login = 'test_resident2'
ON CONFLICT DO NOTHING;


INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, assigned_to_id, created_at, updated_at)
SELECT
    'Water leak in basement',
    'Water is leaking from the ceiling in the laundry room. Emergency! Water is flowing into the hallway.',
    'DAMAGE',
    'IN_PROGRESS',
    (SELECT id FROM app_user WHERE github_login = 'test_resident'),
    (SELECT id FROM app_user WHERE github_login = 'test_handler'),
    NOW() - INTERVAL '2 days',
    NOW() - INTERVAL '1 day'
WHERE EXISTS (SELECT 1 FROM app_user WHERE github_login = 'test_resident')
  AND EXISTS (SELECT 1 FROM app_user WHERE github_login = 'test_handler')
ON CONFLICT DO NOTHING;

INSERT INTO incident (subject, description, incident_category, incident_status, created_by_id, assigned_to_id, created_at, updated_at)
SELECT
    'Elevator alarm not working',
    'The alarm button in the elevator is broken. Safety risk if someone gets trapped!',
    'OTHER',
    'OPEN',
    (SELECT id FROM app_user WHERE github_login = 'test_resident2'),
    (SELECT id FROM app_user WHERE github_login = 'test_handler'),
    NOW() - INTERVAL '1 day',
    NOW() - INTERVAL '1 day'
WHERE EXISTS (SELECT 1 FROM app_user WHERE github_login = 'test_resident2')
AND EXISTS (SELECT 1 FROM app_user WHERE github_login = 'test_handler')
ON CONFLICT DO NOTHING;