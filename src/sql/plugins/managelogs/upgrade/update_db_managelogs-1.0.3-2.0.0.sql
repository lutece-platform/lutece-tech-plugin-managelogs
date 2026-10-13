-- liquibase formatted sql
-- changeset managelogs:update_db_managelogs-1.0.3-2.0.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT' AND admin_url = 'jsp/admin/plugins/managelogs/ManageLogProperties.jsp'
UPDATE core_admin_right SET admin_url = 'jsp/admin/plugins/managelogs/DownloadLog.jsp' WHERE id_right = 'MANAGELOGS_MANAGEMENT';

-- changeset managelogs:update_db_managelogs-1.0.3-2.0.0-rev1.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT' AND icon_url IS NULL
UPDATE core_admin_right SET icon_url = 'ti ti-file-download' WHERE id_right = 'MANAGELOGS_MANAGEMENT';
