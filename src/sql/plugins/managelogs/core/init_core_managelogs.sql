-- liquibase formatted sql
-- changeset managelogs:init_core_managelogs.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT'

--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('MANAGELOGS_MANAGEMENT','managelogs.adminFeature.ManageLogsProperties.name',1,'jsp/admin/plugins/managelogs/DownloadLog.jsp','managelogs.adminFeature.ManageLogsProperties.description',0,'managelogs',NULL,'ti ti-file-download',NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'MANAGELOGS_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('MANAGELOGS_MANAGEMENT',1);


-- changeset managelogs:init_core_managelogs-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT' AND admin_url = 'jsp/admin/plugins/managelogs/ManageLogProperties.jsp'
UPDATE core_admin_right SET admin_url = 'jsp/admin/plugins/managelogs/DownloadLog.jsp' WHERE id_right = 'MANAGELOGS_MANAGEMENT';

-- changeset managelogs:init_core_managelogs-rev2.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT' AND icon_url IS NULL
UPDATE core_admin_right SET icon_url = 'ti ti-file-download' WHERE id_right = 'MANAGELOGS_MANAGEMENT';
