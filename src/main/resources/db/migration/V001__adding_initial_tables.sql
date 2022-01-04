CREATE TABLE `plans` (
  `id` binary(16) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `limit_per_hour` int NOT NULL UNIQUE,
  `name` varchar(255) NOT NULL UNIQUE,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (`id`)
);

CREATE TABLE `users` (
  `id` binary(16) NOT NULL,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `email_id` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `user_plan_mappings` (
  `id` int NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `is_active` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  `plan_id` binary(16) NOT NULL,
  `user_id` binary(16) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrxbu9b6ar3bmufhyaq8t93ve5` (`plan_id`),
  KEY `FKde26wx3e5d2x2j1fbn8m2g5t8` (`user_id`),
  CONSTRAINT `FKde26wx3e5d2x2j1fbn8m2g5t8` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKrxbu9b6ar3bmufhyaq8t93ve5` FOREIGN KEY (`plan_id`) REFERENCES `plans` (`id`)
);