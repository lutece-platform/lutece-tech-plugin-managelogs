-- liquibase formatted sql
-- changeset managelogs:update_db_managelogs-1.0.3-1.1.0.sql
-- preconditions onFail:MARK_RAN onError:WARN
-- precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT' AND admin_url = 'jsp/admin/plugins/managelogs/ManageLogProperties.jsp'
UPDATE core_admin_right SET admin_url = 'jsp/admin/plugins/managelogs/DownloadLog.jsp' WHERE id_right = 'MANAGELOGS_MANAGEMENT';
