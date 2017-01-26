CREATE TABLE IF NOT EXISTS projects (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	project VARCHAR(100) NOT NULL,
	subproject VARCHAR(100) NOT NULL,
	language VARCHAR(100) NOT NULL,
	CONSTRAINT prj_uniq UNIQUE (project,subproject,language)
);
--;;
CREATE TABLE IF NOT EXISTS coverage_data (
	timestamp TIMESTAMP,
	projects_id INT NOT NULL,
	`lines` INT NOT NULL,
	covered INT NOT NULL
);
--;;
ALTER TABLE coverage_data ADD INDEX prj_id_idx (projects_id);
--;;
ALTER TABLE coverage_data ADD FOREIGN KEY (projects_id) REFERENCES projects (id);
