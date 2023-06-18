CREATE DATABASE `message_queue` /*!40100 DEFAULT CHARACTER SET utf8 */;
-- message_queue.mq_message definition

CREATE TABLE `mq_message` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT,
                              `content` varchar(255) NOT NULL,
                              `topic` varchar(100) NOT NULL,
                              `tag` varchar(100) DEFAULT NULL,
                              `status` tinyint(4) DEFAULT NULL,
                              `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              `delay` int(11) DEFAULT NULL,
                              `create_at` bigint(20) NOT NULL,
                              PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=utf8;

-- message_queue.orders definition

CREATE TABLE `orders` (
                          `id` bigint(20) NOT NULL AUTO_INCREMENT,
                          `order_name` varchar(100) DEFAULT NULL,
                          `create_date` date DEFAULT NULL,
                          `content` varchar(255) DEFAULT NULL,
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8;