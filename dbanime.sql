DROP DATABASE IF EXISTS `dbanime`;
CREATE DATABASE IF NOT EXISTS`dbanime`;
USE `dbanime`;

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
	`user_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_name` VARCHAR(32) NOT NULL UNIQUE,
    `region` ENUM('JP', 'AM', 'EU', 'AS', 'AU', 'AF', 'AC') NOT NULL,
    -- REGIONS: Japan, Americas, Europe, Asia, Australia, Africa, Antarctica
    `join_date` DATE NOT NULL
);

DROP TABLE IF EXISTS `studios`;
CREATE TABLE `studios` (
	`studio_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `studio_name` VARCHAR(32) NOT NULL UNIQUE
);

DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff` (
	`staff_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `first_name` VARCHAR(16) NOT NULL,
    `last_name` VARCHAR(16) NOT NULL,
    `occupation` VARCHAR(32) NOT NULL,
    `birthday` DATE NOT NULL
);

DROP TABLE IF EXISTS `animes`;
CREATE TABLE `animes` (
	`anime_id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `studio_id` INT NOT NULL,
    `title` VARCHAR(64) UNIQUE,
    `genre` ENUM('AD', 'CO', 'DR', 'HO', 'SF') NOT NULL,
    -- GENRES: Adventure, Comedy, Drama, Horror, Sci-Fi
    `air_date` DATE NOT NULL,
    `num_of_episodes` INT DEFAULT 1 CHECK (`num_of_episodes` > 0),
    FOREIGN KEY(`studio_id`) REFERENCES `studios`(`studio_id`)
);

DROP TABLE IF EXISTS `views`;
CREATE TABLE `views` (
	`user_id` INT NOT NULL,
    `anime_id` INT NOT NULL,
    `watched_episode` INT NOT NULL,
    `timestamp_watched` TIMESTAMP NOT NULL,
    PRIMARY KEY(`user_id`, `timestamp_watched`),
    FOREIGN KEY(`user_id`) REFERENCES `users`(`user_id`),
    FOREIGN KEY(`anime_id`) REFERENCES `animes`(`anime_id`)
);

DROP TABLE IF EXISTS `ratings`;
CREATE TABLE `ratings` (
	`user_id` INT NOT NULL,
    `anime_id` INT NOT NULL,
    `rating` INT NOT NULL,
    `comment` VARCHAR(2048),
    `last_episode_watched` INT NOT NULL,
    `last_edited_timestamp` TIMESTAMP NOT NULL,
    PRIMARY KEY(`user_id`, `anime_id`),
    FOREIGN KEY(`user_id`) REFERENCES `users`(`user_id`),
    FOREIGN KEY(`anime_id`) REFERENCES `animes`(`anime_id`)
);

DROP TABLE IF EXISTS `credits`;
CREATE TABLE `credits` (
	`staff_id` INT NOT NULL,
    `anime_id` INT NOT NULL,
    `episode` INT NOT NULL,
    `position` VARCHAR(32) NOT NULL,
    `department` ENUM('DP', 'AD', 'AN', 'EV', 'SS', 'TO') NOT NULL,
    -- DEPARTMENTS: Direction and Production, Art and Design, Animation, Sound and Music, Editing and Visual Effects, Script and Storyboarding, Technical and Other Staff
    PRIMARY KEY(`staff_id`, `anime_id`, `episode`),
    FOREIGN KEY(`staff_id`) REFERENCES `staff`(`staff_id`),
    FOREIGN KEY(`anime_id`) REFERENCES `animes`(`anime_id`)
);

DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows` (
	`follower_id` INT NOT NULL,
    `followed_id` INT NOT NULL,
    `following_since_date` DATE NOT NULL,
    PRIMARY KEY(`follower_id`, `followed_id`),
    FOREIGN KEY(`follower_id`) REFERENCES `users`(`user_id`),
    FOREIGN KEY(`followed_id`) REFERENCES `users`(`user_id`)
);

INSERT INTO `studios` (`studio_name`) VALUES
('Studio Zero'),            -- 1
('StarVision Studios'),     -- 2
('PixelCraft Studios'),     -- 3
('Dreamscape Productions'), -- 4
('NebulaWorks');            -- 5

INSERT INTO `users` (`user_name`, `region`, `join_date`) VALUES
('SunshineSoda',   'JP', '2022-01-02'), -- 1
('Periwinkle',     'EU', '2022-02-03'), -- 2
('PixelPirate',    'AM', '2022-04-22'), -- 3
('MysticBlaze',    'AS', '2022-04-30'), -- 4
('WaffleQuest',    'AU', '2022-05-12'); -- 5

INSERT INTO `animes` (`studio_id`, `title`, `genre`, `air_date`, `num_of_episodes`) VALUES
(1, 'Eternal Horizon',       'AD', '2022-01-06', 3), -- 1
(2, 'Violet Shards',         'DR', '2022-03-25', 2), -- 2
(2, 'Cosmic Eclipse',        'SF', '2022-04-15', 5), -- 3
(3, 'Wanderlust Chronicles', 'DR', '2023-03-03', 3), -- 4
(5, 'Unspoken Truth',        'AD', '2023-07-09', 2), -- 5
(4, 'Whispers of the Past',  'HO', '2023-09-03', 3), -- 6
(4, 'Phantom Dance',         'CO', '2023-12-23', 1); -- 7

INSERT INTO `views` (`user_id`, `anime_id`, `watched_episode`, `timestamp_watched`) VALUES
-- USER 1 (SunshineSoda)
(1, 1, 1, '2022-01-06 03:22:54'),
(1, 1, 2, '2022-01-06 04:50:21'),
(1, 1, 3, '2022-01-09 08:00:11'),
(1, 3, 1, '2022-07-01 09:01:53'),
(1, 3, 2, '2022-07-10 12:19:20'),
(1, 2, 1, '2023-01-14 16:20:08'),
-- USER 2 (Periwinkle)
(2, 2, 1, '2022-06-14 11:21:00'),
(2, 2, 2, '2022-06-14 12:32:05'),
(2, 7, 1, '2023-12-24 06:58:38'),
-- USER 3 (PixelPirate)
(3, 5, 1, '2023-07-11 12:03:06'),
(3, 5, 2, '2023-07-12 05:10:00'),
(3, 1, 1, '2023-08-03 03:04:56'),
(3, 6, 1, '2023-09-08 02:01:01'),
(3, 6, 2, '2023-11-02 19:07:30'),
(3, 6, 3, '2023-11-21 23:22:12'),
-- USER 4 (MysticBlaze)
(4, 2, 1, '2022-07-21 16:29:10'),
(4, 4, 1, '2023-03-03 01:58:26'),
(4, 4, 2, '2023-03-04 00:21:02'),
(4, 4, 3, '2023-05-07 16:08:14'),
-- USER 5 (WaffleQuest)
(5, 5, 1, '2023-07-09 12:34:42'),
(5, 5, 2, '2023-07-15 11:21:11'),
(5, 2, 1, '2023-09-11 09:42:02'),
(5, 1, 1, '2023-10-12 23:38:53'),
(5, 3, 1, '2023-10-13 20:57:08'),
(5, 3, 2, '2023-10-14 16:58:21');

INSERT INTO `ratings` (`user_id`, `anime_id`, `rating`, `comment`, `last_episode_watched`, `last_edited_timestamp`) VALUES
-- USER 1 (SunshineSoda)
(1, 1, 5, 'Amazing anime, love the characters! In fact, I watched it again!', 3, '2022-02-16 12:20:08'),
(1, 3, 2, 'Kinda mid, didn\'t really end up liking it at the end.',           2, '2022-07-10 18:12:01'),
(1, 2, 4, 'I love the color purple!',                                         1, '2023-02-15 06:02:09'),
-- USER 2 (Periwinkle)
(2, 2, 5, 'Periwinkle Periwinkle!',                                           2, '2022-06-15 03:01:13'),
(2, 7, 4, 'Very slay!',                                                       1, '2023-12-24 07:08:19'),
-- USER 3 (PixelPirate)
(3, 5, 4, 'Loved the overall storyline and visuals! Great job NebulaWorks',   2, '2023-07-12 09:11:02'),
(3, 1, 4, 'Great anime, great visuals, love it so far!',                      1, '2023-08-04 16:07:22'),
(3, 6, 1, 'Didn\'t really like it, hope they improve on the next season',     2, '2023-11-02 21:34:31'),
-- USER 4 (MysticBlaze)
(4, 2, 1, 'Lacked a bit of depth. Could\'ve been better imo.',                1, '2022-07-22 06:45:00'),
(4, 4, 5, 'I\'ve got no words for how great this anime is. MAN',              2, '2023-04-01 00:11:23'),
-- USER 5 (WaffleQuest)
(5, 5, 1, 'Action scenes feel very empty and plain. Could be so much better', 1, '2023-07-14 12:23:57'),
(5, 2, 5, 'I really do love the color purple tbh. That\'s all I can say :D',  1, '2023-09-12 14:31:08'),
(5, 1, 3, 'Didn\'t meet my expectations despite how my friends described it', 1, '2023-10-13 21:42:34'),
(5, 3, 5, 'I love sci-fi and boy did i love this anime! :DDDDDDD',            2, '2023-10-14 17:08:11');

INSERT INTO `staff` (`first_name`, `last_name`, `occupation`, `birthday`) VALUES
('Hiroshi', 'Takeda',    'Director',       '1975-02-10'), -- 1
('Maya',    'Shirogane', 'Animator',       '1981-07-22'), -- 2
('Kenji',   'Fujimoto',  'Sound Engineer', '1990-11-30'), -- 3
('Rika',    'Sakamoto',  'Producer',       '1986-01-15'), -- 4
('Yuki',    'Hamada',    'Writer',         '1993-09-09'), -- 5
('Takumi',  'Sato',      'Art Director',   '1987-04-04'), -- 6
('Ayumi',   'Kobayashi', 'Music Composer', '1992-12-17'), -- 7
('Daichi',  'Tanaka',    'Editor',         '1985-06-13'); -- 8

INSERT INTO `credits` (`staff_id`, `anime_id`, `episode`, `position`, `department`) VALUES
(1, 1, 2, 'Director', 'DP'),
(1, 7, 1, 'Director', 'DP'),
(2, 1, 2, 'Producer', 'DP'),
(2, 4, 3, 'Producer', 'DP'),
(3, 2, 2, 'Animator', 'AN'),
(4, 3, 4, 'Sound Engineer', 'SS'),
(4, 6, 2, 'Sound Engineer', 'SS'),
(5, 4, 3, 'Scriptwriter', 'TO'),
(6, 5, 1, 'Art Designer', 'AD'),
(7, 6, 2, 'Editor', 'EV'),
(8, 7, 1, 'Animator', 'AN');

INSERT INTO `follows` (`follower_id`, `followed_id`, `following_since_date`) VALUES
(1, 2, '2022-02-03'),
(2, 1, '2022-02-04'),
(3, 2, '2022-06-10'),
(2, 4, '2022-07-07'),
(5, 3, '2023-01-10'),
(1, 4, '2023-02-20'),
(4, 3, '2023-03-01');

-- ===================================REPORT PROCEDURES===================================

-- PROCEDURE
-- Usage "CALL SelectBestAnimeOverall()"
-- Creates a table called `best_anime` for overall best anime based on rating
DELIMITER //
-- DROP PROCEDURE IF EXISTS `SelectBestAnimeOverall`; 
CREATE PROCEDURE `SelectBestAnimeOverall`(
	IN param_genre VARCHAR(4)
)
BEGIN
	DROP TEMPORARY TABLE IF EXISTS `best_anime`;
	CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime` (
		overall VARCHAR(10),
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    ); 
    INSERT INTO `best_anime` (overall, title, genre, studio_name, rating)
    SELECT 
		"Overall" AS overall,
        title, 
        genre, 
        studio_name,
        ROUND(AVG(r.rating), 2) AS rating
    FROM 
        animes a
    JOIN 
        studios s ON a.studio_id = s.studio_id
    JOIN 
        ratings r ON a.anime_id = r.anime_id
	WHERE
		(a.genre = param_genre OR param_genre = "None")
    GROUP BY 
        a.anime_id
    ORDER BY 
        rating DESC
    LIMIT 3;
END //
DELIMITER ;



-- PROCEDURE
-- Usage: "CALL SelectBestAnimeInMonth(<param_month>)
-- Inserts the top 5 animes based on ratings in the specified month
-- Helper function for SelectBestAnimeMonth
DELIMITER //
-- DROP PROCEDURE IF EXISTS SelectBestAnimeInMonth;
CREATE PROCEDURE `SelectBestAnimeInMonth`(
	IN param_month INT,
	IN param_genre VARCHAR(4)
)
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime` (
		month_ VARCHAR(9),
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    );
    
    INSERT INTO `best_anime` (month_, title, genre, studio_name, rating)
    SELECT 
        MONTHNAME(CONCAT("0000-", param_month, "-00")) AS month_,
        a.title, 
        a.genre, 
        s.studio_name,
        ROUND(AVG(r.rating), 2) AS rating
    FROM 
        animes a
    JOIN 
        studios s ON a.studio_id = s.studio_id
    JOIN 
        ratings r ON a.anime_id = r.anime_id
    WHERE
        MONTH(a.air_date) = param_month AND
		(a.genre = param_genre OR param_genre = "None")
    GROUP BY 
        a.anime_id
    ORDER BY 
        rating DESC
	LIMIT 3;
END //
DELIMITER ;


-- PROCEDURE
-- Usage: "CALL SelectBestAnimeMonthly()"
-- Creates a table called `best_anime` for all the months with their top 5 animes
DELIMITER //
-- DROP PROCEDURE IF EXISTS SelectBestAnimeMonthly;
CREATE PROCEDURE `SelectBestAnimeMonthly`(
	IN param_genre VARCHAR(4)
)
BEGIN
	DECLARE i INT DEFAULT 1;
	DROP TEMPORARY TABLE IF EXISTS `best_anime`;
	CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime` (
		month_ VARCHAR(9),
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    );
	
	WHILE i <= 12 DO
		CALL SelectBestAnimeInMonth(i, param_genre);
		SET i = i + 1;
	END WHILE;
END //
DELIMITER ;



-- PROCEDURE
-- Usage "CALL SelectBestAnimeInSeason(<from_month>, <to_month>, <season_name>)"
-- Inserts the top 5 animes based on rating for the specified season
-- Used as a helper Procedure in SelectBestAnimeSeason
DELIMITER //
-- DROP PROCEDURE IF EXISTS `SelectBestAnimeInSeason`;
CREATE PROCEDURE `SelectBestAnimeInSeason`(
    IN from_month INT,
    IN to_month INT,
    IN season_name VARCHAR(6),
    IN param_genre VARCHAR(4)
)
BEGIN
	CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime` (
        season VARCHAR(6),
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    );

    INSERT INTO `best_anime` (season, title, genre, studio_name, rating)
    SELECT 
        season_name AS season,
        a.title, 
        a.genre, 
        s.studio_name,
        ROUND(AVG(r.rating), 2) AS rating
    FROM 
        animes a
    JOIN 
        studios s ON a.studio_id = s.studio_id
    JOIN 
        ratings r ON a.anime_id = r.anime_id
    WHERE
        MONTH(a.air_date) BETWEEN from_month AND to_month
        AND (a.genre = param_genre OR param_genre = "None")
    GROUP BY 
        a.anime_id
    ORDER BY 
        rating DESC
    LIMIT 3;
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL SelectBestAnimeSeasonal()"
-- Creates a table called `best_anime` with each season containing the top 5 animes aired in that season
DELIMITER //
-- DROP PROCEDURE IF EXISTS `SelectBestAnimeSeasonal`;
CREATE PROCEDURE `SelectBestAnimeSeasonal`(
	IN param_genre VARCHAR(4)
)
BEGIN
	DROP TEMPORARY TABLE IF EXISTS `best_anime`;
	CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime` (
        season VARCHAR(6),
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    );

    CALL SelectBestAnimeInSeason(1, 3, 'Winter', param_genre);
    CALL SelectBestAnimeInSeason(4, 6, 'Spring', param_genre);
    CALL SelectBestAnimeInSeason(7, 9, 'Summer', param_genre);
    CALL SelectBestAnimeInSeason(10, 12, 'Fall', param_genre);
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL SelectBestAnimeInMonth(<param_month>)
-- Inserts the top 5 animes based on ratings in the specified year
-- Helper function for SelectBestAnimeYear
DELIMITER //
-- DROP PROCEDURE IF EXISTS `SelectBestAnimeInYear`;
CREATE PROCEDURE `SelectBestAnimeInYear`(
	IN param_year INT,
    IN param_genre VARCHAR(4)
)
BEGIN
    CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime`(
        year_ INT,
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    );

    INSERT INTO `best_anime` (year_, title, genre, studio_name, rating)
    SELECT 
        param_year AS year_,
        a.title, 
        a.genre, 
        s.studio_name,
        ROUND(AVG(r.rating), 2) AS rating
    FROM 
        animes a
    JOIN 
        studios s ON a.studio_id = s.studio_id
    JOIN 
        ratings r ON a.anime_id = r.anime_id
    WHERE
        YEAR(a.air_date) = param_year
		AND (a.genre = param_genre OR param_genre = "None")
    GROUP BY 
        a.anime_id
    ORDER BY 
        rating DESC
    LIMIT 5;
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL SelectBestAnimeYearly()"
-- Creates a table called `best_anime` for all the years with their top 5 animes
DELIMITER //
-- DROP PROCEDURE IF EXISTS `SelectBestAnimeYearly`;
CREATE PROCEDURE `SelectBestAnimeYearly`(
	IN param_genre VARCHAR(4)
)
BEGIN
	DECLARE min INT;
    DECLARE max INT;

	SELECT MIN(YEAR(a.air_date)) INTO min FROM animes a;
    SELECT MAX(YEAR(a.air_date)) INTO max FROM animes a;
		
	DROP TEMPORARY TABLE IF EXISTS `best_anime`;
	CREATE TEMPORARY TABLE IF NOT EXISTS `best_anime`(
        year_ INT,
        title VARCHAR(255),
        genre VARCHAR(255),
        studio_name VARCHAR(255),
        rating DECIMAL(5, 2)
    );
    
	WHILE min <= max DO
		CALL SelectBestAnimeInYear(min, param_genre);
		SET min = min + 1;
	END WHILE;
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL ViewUserProfile(<param_user_id>)"
-- Returns the given user's name, number of unique anime's watched, and number of ratings made
DELIMITER //
-- DROP PROCEDURE IF EXISTS `ViewUserProfile`;
CREATE PROCEDURE `ViewUserProfile`(
    IN param_user_id INT,
    IN param_year INT
)
BEGIN
    SELECT 
        u.user_name,
        u.region,
        u.join_date,
        COUNT(DISTINCT v.anime_id) AS viewed_anime_count,
        COUNT(v.watched_episode) AS episodes_watched,
		COUNT(DISTINCT(r.last_edited_timestamp)) AS ratings_made
    FROM 
        users u
    LEFT JOIN 
        views v ON u.user_id = v.user_id AND (YEAR(v.timestamp_watched) = param_year OR param_year = 0)
	LEFT JOIN 
        ratings r ON u.user_id = r.user_id AND (YEAR(r.last_edited_timestamp) = param_year OR param_year = 0)
    WHERE
        u.user_id = param_user_id
    GROUP BY
        u.user_id;
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL ViewUserGenreAnime(<param_user_id>)"
-- Returns all the genres the user viewed on and their count, 
-- as well as the top anime based on the user's review
DELIMITER //
-- DROP PROCEDURE IF EXISTS `ViewUserGenreAnime`;
CREATE PROCEDURE `ViewUserGenreAnime`(
    IN param_user_id INT,
    IN param_year INT
)
BEGIN
	SELECT
		a.genre,
		COUNT(a.anime_id) AS `genre_count`,
		(
			SELECT 
				asub.title            
			FROM
				ratings r
			LEFT JOIN
				animes asub ON r.anime_id = asub.anime_id
			WHERE
				r.user_id = param_user_id
				AND asub.genre = a.genre
                AND (YEAR(r.last_edited_timestamp) = param_year OR param_year = 0)
			GROUP BY
				asub.anime_id
			ORDER BY
				AVG(r.rating)
			LIMIT 1
		)	AS `top_rated_anime`
	FROM
		users u
	LEFT JOIN
		views v ON u.user_id = v.user_id AND (YEAR(v.timestamp_watched) = param_year OR param_year = 0)
	LEFT JOIN
		animes a ON v.anime_id = a.anime_id
	WHERE
		u.user_id = param_user_id
	GROUP BY
		a.genre
	ORDER BY
		`genre_count` DESC;
END //
DELIMITER ;

-- PROCEDURE
-- Usage "CALL ViewBestStudio(<param_year>)"
-- Returns each studio with their average rating
DELIMITER //
-- DROP PROCEDURE IF EXISTS `ViewBestStudio`;
CREATE PROCEDURE `ViewBestStudio`(
	IN param_year YEAR
)
BEGIN
	SELECT 
		s.studio_name,
		ROUND(AVG(r.rating), 2) AS `studio_rating`,
        (
			SELECT 
				aa.title 
			FROM 
				animes aa
			JOIN
				ratings rr ON aa.anime_id = rr.anime_id
			WHERE
				aa.studio_id = s.studio_id AND 
                (YEAR(aa.air_date) = param_year OR param_year = 0)
			GROUP BY
				aa.anime_id
			ORDER BY
				AVG(rr.rating) DESC
			LIMIT 1
		) AS `highest_rated_anime`
	FROM
		studios s
	JOIN
		animes a ON s.studio_id = a.studio_id
	JOIN
		ratings r ON a.anime_id = r.anime_id
	WHERE
		(YEAR(a.air_date) = param_year OR param_year = 0)
	GROUP BY
		s.studio_id
	ORDER BY
		`studio_rating` DESC;
END//
DELIMITER ;


-- PROCEDURE
-- Usage "CALL RecommendFromWatchList(<param_user_id>)"
-- Returns a list of recommended titles where the user's last watch episode is not the final episode
DELIMITER //
-- DROP PROCEDURE IF EXISTS RecommendFromWatchList;
CREATE PROCEDURE RecommendFromWatchList(
	IN param_user_id INT,
    IN param_year INT
) 
BEGIN
	SELECT
		a.title,
        MAX(v.watched_episode) AS `last_watched`,
		a.num_of_episodes,
        a.air_date
    FROM
		animes a
	JOIN
		views v ON a.anime_id = v.anime_id AND v.user_id = param_user_id
	WHERE	
		(YEAR(a.air_date) = param_year OR param_year = 0)
    GROUP BY
		a.anime_id
	HAVING
		`last_watched` < a.num_of_episodes;
END // 
DELIMITER ; 


-- PROCEDURE
-- Usage "CALL RecommendFromGenre(<param_user_id>)"
-- Returns a list of recommended animes based on the users top 3 genres
-- where the anime recommended for each genre is the highest average rating 
-- and the user hasnt viewed a single episode yet
DELIMITER // 
-- DROP PROCEDURE IF EXISTS RecommendFromGenre;
CREATE PROCEDURE RecommendFromGenre(
	IN param_user_id INT,
    IN param_year INT
)
BEGIN
	SELECT
		a.genre,
		COUNT(a.anime_id) AS `genre_count`,
		(
			SELECT aa.title 
			FROM ratings r
			LEFT JOIN
				animes aa ON r.anime_id = aa.anime_id
			WHERE
				aa.genre = a.genre AND aa.anime_id NOT IN(
					SELECT DISTINCT(aaa.anime_id) FROM animes aaa JOIN views vv
                    ON aaa.anime_id = vv.anime_id AND vv.user_id = param_user_id
                ) AND (YEAR(aa.air_date) = param_year OR param_year = 0)
			GROUP BY aa.anime_id ORDER BY AVG(r.rating) DESC LIMIT 1
		)	AS `top_rated_anime`,
        (SELECT aaaa.air_date FROM animes aaaa WHERE `top_rated_anime` = aaaa.title) AS `top_rated_air_date`
	FROM
		users u
	LEFT JOIN
		views v ON u.user_id = v.user_id
	LEFT JOIN
		animes a ON v.anime_id = a.anime_id
	WHERE
		u.user_id = param_user_id 
	GROUP BY
		a.genre
	ORDER BY
		`genre_count` DESC;
END // 
DELIMITER ;


-- PROCEDURE
-- Usage "CALL RecommendFromFollows(<param_user_id>)"
-- Returns a list of animes rated by followed users where their rating is greater than 3
DELIMITER //
-- DROP PROCEDURE IF EXISTS RecommendFromFollows;
CREATE PROCEDURE RecommendFromFollows(
	IN param_user_id INT,
    IN param_year INT
)
BEGIN
	SELECT 
		u.user_name,
        a.title,
        r.`comment`,
        r.rating,
        a.air_date
    FROM
		`follows` f
	JOIN
		ratings r ON f.followed_id = r.user_id AND f.follower_id = param_user_id AND r.rating > 3
	JOIN
		users u ON f.followed_id = u.user_id
	JOIN
		animes a ON r.anime_id = a.anime_id
	WHERE
		YEAR(a.air_date) = param_year OR param_year = 0;
        
END //
DELIMITER ;


-- ===================================TRANSACTION PROCEDURES===================================


-- PROCEDURE
-- Usage: "CALL GetLastWatched(<param_user_id>, <param_anime_id>, <last_watched>)"
-- Gets the user's latest episode watched and places into last_watched, returns 0 if found none
DELIMITER //
-- DROP PROCEDURE IF EXISTS GetLastWatched;
CREATE PROCEDURE GetLastWatched(
	IN param_user_id INT,
    IN param_anime_id INT,
    OUT last_watched INT
)
BEGIN
	SELECT 	IFNULL(MAX(watched_episode), 0)
    INTO	last_watched
    FROM 	views v
    WHERE 	v.anime_id = param_anime_id
    AND 	v.user_id = param_user_id;
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL GetLastWatched(<param_user_id>, <param_anime_id>, <last_watched>)"
-- Gets the user's latest episode watched, returns 0 if found none
DELIMITER //
-- DROP PROCEDURE IF EXISTS GetLastWatchedQ;
CREATE PROCEDURE GetLastWatchedQ(
	IN param_user_id INT,
    IN param_anime_id INT
)
BEGIN
	SELECT 	IFNULL(MAX(watched_episode), 0)
    FROM 	views v
    WHERE 	v.anime_id = param_anime_id
    AND 	v.user_id = param_user_id;
END //
DELIMITER ;

-- PROCEDURE
-- Usage: "CALL WatchEpisode(<param_user_id>, <param_anime_id>)"
-- Inserts a new entry to views of the same user and anime and advanced with 1 episode
DELIMITER //
-- DROP PROCEDURE IF EXISTS WatchAnime;
CREATE PROCEDURE WatchAnime(
	IN param_user_id INT,
    IN param_anime_id INT
)
BEGIN
	DECLARE lastWatched INT;
	DECLARE maxEpisodes INT;

	SELECT num_of_episodes INTO maxEpisodes FROM animes WHERE anime_id = param_anime_id;

	CALL GetLastWatched(param_user_id, param_anime_id, lastWatched);

	IF (lastWatched < maxEpisodes) THEN
		INSERT INTO `views` (`user_id`, `anime_id`, `watched_episode`, `timestamp_watched`) 
		VALUES (param_user_id, param_anime_id, lastWatched + 1, CURRENT_TIMESTAMP());
	END IF;
END //  
DELIMITER ; 
