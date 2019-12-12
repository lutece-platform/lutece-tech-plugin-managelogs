
--
-- Data for table core_admin_right
--
DELETE FROM core_admin_right WHERE id_right = 'MANAGELOGS_MANAGEMENT';
INSERT INTO core_admin_right (id_right,name,level_right,admin_url,description,is_updatable,plugin_name,id_feature_group,icon_url,documentation_url, id_order ) VALUES 
('MANAGELOGS_MANAGEMENT','managelogs.adminFeature.ManageLogsProperties.name',1,'jsp/admin/plugins/managelogs/ManageLogProperties.jsp','managelogs.adminFeature.ManageLogsProperties.description',0,'managelogs',NULL,NULL,NULL,4);


--
-- Data for table core_user_right
--
DELETE FROM core_user_right WHERE id_right = 'MANAGELOGS_MANAGEMENT';
INSERT INTO core_user_right (id_right,id_user) VALUES ('MANAGELOGS_MANAGEMENT',1);

