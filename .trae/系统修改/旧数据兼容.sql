-- 根据 repair.repairPerson 里的名字匹配 maintenance_person.name，回填 maintenance_id
UPDATE repair r
INNER JOIN maintenance_person mp ON r.repair_person = mp.name
SET r.maintenance_id = mp.id
WHERE r.maintenance_id IS NULL AND r.repair_person IS NOT NULL AND r.repair_person != '';

-- 验证匹配结果
SELECT r.id, r.repair_number, r.repair_person, r.maintenance_id, mp.name AS matched_name
FROM repair r
LEFT JOIN maintenance_person mp ON r.maintenance_id = mp.id
WHERE r.repair_person IS NOT NULL AND r.repair_person != '';
