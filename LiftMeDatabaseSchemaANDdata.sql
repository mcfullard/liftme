CREATE DATABASE  IF NOT EXISTS `liftme` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `liftme`;
-- MySQL dump 10.13  Distrib 5.6.23, for Win64 (x86_64)
--
-- Host: localhost    Database: liftme
-- ------------------------------------------------------
-- Server version	5.6.25-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `trip`
--

DROP TABLE IF EXISTS `trip`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `trip` (
  `tripID` int(11) NOT NULL AUTO_INCREMENT,
  `userID` int(11) NOT NULL,
  `pickUpLat` float DEFAULT NULL,
  `pickUpLong` float DEFAULT NULL,
  `dropOffLat` float DEFAULT NULL,
  `dropOffLong` float DEFAULT NULL,
  `pickUpTime` time DEFAULT NULL,
  `dropOffTime` time DEFAULT NULL,
  `date` date DEFAULT NULL,
  PRIMARY KEY (`tripID`,`userID`),
  KEY `userID_idx` (`userID`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `trip`
--

LOCK TABLES `trip` WRITE;
/*!40000 ALTER TABLE `trip` DISABLE KEYS */;
INSERT INTO `trip` VALUES (1,1,0.0005,0,-33.9592,25.5164,'14:00:00','23:00:00','1994-11-17'),(2,1,0,0,-33.9704,25.4947,'07:00:00','07:45:00','2016-04-21'),(3,1,0,0,-34.0092,25.6674,'08:00:00','08:15:00','2016-04-29'),(4,9,0,0,-33.9704,25.4947,'07:45:00','09:00:00','2017-01-01');
/*!40000 ALTER TABLE `trip` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `userID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) DEFAULT NULL,
  `surname` varchar(45) DEFAULT NULL,
  `password` varchar(45) NOT NULL,
  `email` varchar(255) NOT NULL,
  `contactNum` varchar(10) DEFAULT NULL,
  `availableAsDriver` bit(1) DEFAULT NULL,
  `numberOfPassengers` int(11) DEFAULT NULL,
  `authenticationToken` char(36) DEFAULT NULL,
  PRIMARY KEY (`userID`),
  UNIQUE KEY `email_UNIQUE` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'H','J','g','d','3','',2,'9AD12D0A-7AB0-4203-818C-EA4EB209708B'),(9,'Francois','du Plessis','francoispassword','francois@gmail.com','013123312','\0',3,'85260865-F142-47A9-B258-68DEFC030311'),(14,'Francois','du Plessis','francoispassword','F@gmail.com','013123312','\0',3,'B009A2FB-CCED-476C-A3BA-4026ADD51383'),(15,NULL,NULL,'newpassword1','new@user',NULL,NULL,NULL,'0F34E017-CBEA-46DB-BE20-CAC0C6B81BF0');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-05-05 17:08:59
