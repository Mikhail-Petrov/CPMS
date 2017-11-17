-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.7.11-log - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL Version:             9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
-- Dumping data for table competencyjpa.competency: ~8 rows (approximately)
/*!40000 ALTER TABLE `competency` DISABLE KEYS */;
INSERT INTO `competency` (`ID`, `LEVEL`, `owner`, `SKILL`) VALUES
	(1, 2, 1, 5),
	(2, 2, 1, 4),
	(3, 1, 1, 2),
	(4, 1, 1, 1),
	(5, 2, 2, 3),
	(6, 2, 2, 2),
	(7, 1, 2, 1),
	(8, 2, 3, 7),
	(9, 2, 3, 1);
/*!40000 ALTER TABLE `competency` ENABLE KEYS */;

-- Dumping data for table competencyjpa.evidence: ~3 rows (approximately)
/*!40000 ALTER TABLE `evidence` DISABLE KEYS */;
INSERT INTO `evidence` (`ID`, `AcquiredDate`, `Description`, `Description_RU`, `ExpirationDate`, `Type`, `competency_ID`) VALUES
	(1, '2010-01-01', 'Developing first application', 'Разработка первого приложения', '2012-01-01', 'EXPERIENCE', 1),
	(2, '2013-01-01', 'Developing second application', 'Разработка второго приложения', '2015-01-01', 'EXPERIENCE', 1),
	(3, '2015-01-01', 'Development certificate', 'Сертификат', '2016-01-01', 'CERTIFICATE', 5);
/*!40000 ALTER TABLE `evidence` ENABLE KEYS */;

-- Dumping data for table competencyjpa.profile: ~3 rows (approximately)
/*!40000 ALTER TABLE `profile` DISABLE KEYS */;
INSERT INTO `profile` (`DTYPE`, `ID`, `about`, `about_RU`, `company`, `company_RU`, `email`, `familyname`, `familyname_RU`, `name`, `name_RU`, `address`, `address_RU`, `title`, `title_RU`, `website`) VALUES
	('Company', 1, 'A company created for demonstation. Carries out Java web development with Spring Framework, but has other skills.', 'Компания, созданная для демонстрации возможностей системы. Занимается разработкой веб приложений с использованием Spring Framework, но также владеет и другими умениями.', NULL, NULL, 'dc1@example.com', NULL, NULL, NULL, NULL, 'Saint - Petersburg, Birzhevaya Liniya 14', 'Санкт - Петербург, Биржевая линия, дом 14', 'Demo Company #1', NULL, 'www.dc1.example.com'),
	('Company', 2, 'A company created for demonstation. Carries out C# web development.', 'Компания, созданная для демонстрации возможностей системы. Занимается разработкой с использованием C#.', NULL, NULL, 'dc2@example.com', NULL, NULL, NULL, NULL, 'Saint - Petersburg, Birzhevaya Liniya 14', 'Санкт - Петербург, Биржевая линия, дом 14', 'Demo Company #2', NULL, 'dc2.example.com'),
	('Company', 3, 'A company created for demonstation. Carries out some machine learning.', 'Компания, созданная для демонстрации возможностей системы. Занимается машинным обучением.', NULL, NULL, 'dc3@example.com', NULL, NULL, NULL, NULL, 'Saint - Petersburg, Birzhevaya Liniya 14', 'Санкт - Петербург, Биржевая линия, дом 14', 'Demo Company #3', NULL, 'dc3.example.com');
/*!40000 ALTER TABLE `profile` ENABLE KEYS */;

-- Dumping data for table competencyjpa.requirement: ~2 rows (approximately)
/*!40000 ALTER TABLE `requirement` DISABLE KEYS */;
INSERT INTO `requirement` (`ID`, `LEVEL`, `SKILL`, `task`) VALUES
	(1, 1, 2, 1),
	(2, 1, 7, 2);
/*!40000 ALTER TABLE `requirement` ENABLE KEYS */;

-- Dumping data for table competencyjpa.role: ~2 rows (approximately)
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` (`ID`, `ROLENAME`, `OWNER`) VALUES
	(1, 'RESIDENT', 1),
	(2, 'RESIDENT', 2),
	(3, 'RESIDENT', 3);
/*!40000 ALTER TABLE `role` ENABLE KEYS */;

-- Dumping data for table competencyjpa.skill: ~14 rows (approximately)
/*!40000 ALTER TABLE `skill` DISABLE KEYS */;
INSERT INTO `skill` (`ID`, `ABOUT`, `ABOUT_RU`, `draft`, `MAXLEVEL`, `NAME`, `NAME_RU`, `owner`, `PARENT`) VALUES
	(1, 'Developing software of any type or complexity', 'Разработка ПО любого типа или сложности', b'0', 3, 'Software Development', 'Разработка ПО', NULL, NULL),
	(2, 'Developing web applications of any type and any complexity', 'Разработка веб-приложений любого типа и сложности', b'0', 3, 'Web Development', 'Разработка Web - Приложений', NULL, 1),
	(3, 'Developing web - based applications and systems using C# language', 'Разработка web систем и приложений с использованием языка программирования C#', b'0', 3, 'C# Web  Development', 'Веб разработка с использованием C#', NULL, 2),
	(4, 'Developing web based systems and applications using Java programming language', 'Разработка web приложений и систем с использованием языка программирования Java', b'0', 3, 'Java Web Development', 'Разработка web приложений с использованием Java', NULL, 2),
	(5, 'Developing web based applications and systems using Spring Technology stack ', 'Разработка web приложений и систем с импользованием стека технологий Spring', b'0', 3, 'Java with Spring Framework web development', 'Разработка web приложений с использованием Spring Framework', NULL, 4),
	(7, '', '', b'0', 3, 'Machine Learning', 'Машинное обучение', NULL, NULL),
	(9, '', '', b'0', 1, 'Scala web development', 'Веб разработка с использованием Scala', NULL, 4),
	(10, '', '', b'1', 1, 'Scala web development with Play Flamework', 'Разработка web приложения с использованием Scala и Play Framework', 1, 9),
	(11, '', '', b'1', 1, 'Testing', 'Тестирование', 1, 1),
	(12, '', '', b'1', 1, 'Test Driven Development', 'Разработка через тестирование', 1, 11),
	(15, 'Developing for mobile devices using any technology or stack', 'Разработка для мобильных устройств с использованием любого языка программирования или стека', b'0', 1, 'Mobile Development', 'Мобильная разработка', NULL, 1),
	(16, 'Developing applications for Android API', 'Разработка приложений под Android API', b'1', 3, 'Android Development', 'Разработка под Android', 2, 15),
	(17, '', '', b'0', 3, 'Embedded Development', ' Разработка на встроенных устройствах', NULL, 1);
/*!40000 ALTER TABLE `skill` ENABLE KEYS */;

-- Dumping data for table competencyjpa.skilllevel: ~29 rows (approximately)
/*!40000 ALTER TABLE `skilllevel` DISABLE KEYS */;
INSERT INTO `skilllevel` (`ID`, `ABOUT`, `ABOUT_RU`, `LEVEL`, `SKILL`) VALUES
	(1, '5 or more years of experiece', '5 или более лет опыта', 1, 1),
	(2, '15 or more years of experiece', '15 или более лет опыта', 2, 1),
	(3, '30 or more years of experiece', '30 или более лет опыта', 3, 1),
	(4, '5 or more years of experience', '5 или более лет опыта', 1, 2),
	(5, '10 or more years of experience', '10 или более лет опыта', 2, 2),
	(6, '15 or more years of experience', '15 или более лет опыта', 3, 2),
	(7, '1 carried out project', '1 выполненный проект', 1, 3),
	(8, '2 carried out projects', '2 выполненных проекта', 2, 3),
	(9, '3 or more carried out projects', '3 или более выполненных проекта', 3, 3),
	(10, '1 carried out project', '1 выполненный проект', 1, 4),
	(11, '2 carried out projects', '2 выполненных проекта', 2, 4),
	(12, '3 or more carried out projects', '3 или более выполненных проекта', 3, 4),
	(13, '1 carried out project', '1 выполненный проект', 1, 5),
	(14, '2 carried out projects', '2 выполненных проекта', 2, 5),
	(15, '3 or more carried out projects', '3 или более выполненных проекта', 3, 5),
	(19, '5 or more years of experience', '5 или более лет опыта', 1, 7),
	(20, '15 or more years of experience', '15 или более лет опыта', 2, 7),
	(21, '30 or more years of experience', '30 или более лет опыта', 3, 7),
	(23, '', '', 1, 9),
	(24, '', '', 1, 10),
	(25, '', '', 1, 11),
	(26, '', '', 1, 12),
	(29, '', '', 1, 15),
	(30, '', '', 1, 16),
	(31, '', '', 2, 16),
	(32, '', '', 3, 16),
	(33, '', '', 1, 17),
	(34, '', '', 2, 17),
	(35, '', '', 3, 17);
/*!40000 ALTER TABLE `skilllevel` ENABLE KEYS */;

-- Dumping data for table competencyjpa.task: ~2 rows (approximately)
/*!40000 ALTER TABLE `task` DISABLE KEYS */;
INSERT INTO `task` (`ID`, `ABOUT`, `ABOUT_RU`, `NAME`, `NAME_RU`) VALUES
	(1, 'Developing a web application with any known technology', 'Разработка веб-приложения с использованием любого стека технологий', 'Developing a web site', 'Разработка Web - Сайта'),
	(2, 'Using machine learning for creating classier', 'Использование машиноого обучения для обучения классификатора', 'Classifier learning', 'Обучением классификатора');
/*!40000 ALTER TABLE `task` ENABLE KEYS */;

-- Dumping data for table competencyjpa.user: ~2 rows (approximately)
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`ID`, `PASSWORD`, `profileId`, `username`) VALUES
	(1, '$2a$10$uACKP4Eh.Bsf8.qyRzOzLuyWHy10uZXJuXxRWLxj7au8VmmE1v0eC', 1, 'dc1'),
	(2, '$2a$10$5yVLXR/7VYlJDwOxDofbDOESAs5u..KYB8o47/zbfEpHT4PHdssx.', 2, 'dc2'),
	(3, '$2a$10$tIW8xsTpSQHrNQOfY3TOTeZl9fSnkjjF5XVqdYfOgfoFxJLT90RMa', 3, 'dc3');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
