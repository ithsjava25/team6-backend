--Testusers (can not auth through github, only for testing)
INSERT INTO app_user (id, github_id, github_login, name, email, role, is_active, avatar_url, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'test_111', 'test_resident', 'Test Resident', 'resident@test.com', 'RESIDENT', true, 'https://ui-avatars.com/api/?background=10b981&color=fff&name=TR', NOW(), NOW()),
    (gen_random_uuid(), 'test_222', 'test_handler', 'Test Handler', 'handler@test.com', 'HANDLER', true, 'https://ui-avatars.com/api/?background=f97316&color=fff&name=TH', NOW(), NOW()),
    (gen_random_uuid(), 'test_333', 'test_pending', 'Test Pending', 'pending@test.com', 'PENDING', true, 'https://ui-avatars.com/api/?background=6b7280&color=fff&name=TP', NOW(), NOW()),
    (gen_random_uuid(), 'test_444', 'test_resident2', 'Test Resident 2', 'resident2@test.com', 'RESIDENT', true, 'https://ui-avatars.com/api/?background=10b981&color=fff&name=T2', NOW(), NOW()),
    (gen_random_uuid(), 'test_555', 'test_inactive', 'Test Inactive', 'inactive@test.com', 'RESIDENT', false, 'https://ui-avatars.com/api/?background=6b7280&color=fff&name=TI', NOW(), NOW());