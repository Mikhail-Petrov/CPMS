CREATE DATABASE competencyjpa;
CREATE DATABASE competencyjpa_applications;

USE competencyjpa;

CREATE USER mastercjpa@localhost IDENTIFIED BY 'TestingPassword'; 
grant usage on *.* to mastercjpa@localhost; 
grant ALL on competencyjpa.* to mastercjpa@localhost;
grant ALL on competencyjpa_applications.* to mastercjpa@localhost;

CREATE DATABASE testingcompetencyjpa;

USE testingcompetencyjpa;

CREATE USER testingmastercjpa@localhost IDENTIFIED BY 'TestingPassword'; 
grant usage on *.* to testingmastercjpa@localhost; 
grant ALL on testingcompetencyjpa.* to testingmastercjpa@localhost;