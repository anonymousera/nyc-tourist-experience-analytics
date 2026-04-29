USE dn2491_db;

-- =======================================================
-- STEP 1: CLEANING THE SPINE (Zipcodes)
-- =======================================================
DROP TABLE IF EXISTS nyc_zipcodes;
CREATE TABLE nyc_zipcodes AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    MAX(borough) as borough
FROM mta_ridership
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT);

-- =======================================================
-- STEP 2: AGGREGATE BASIC METRICS
-- =======================================================

-- 2a. HOTEL STATS
DROP TABLE IF EXISTS hotel_zipcode_stats;
CREATE TABLE hotel_zipcode_stats AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    COUNT(DISTINCT offering_id) as hotel_count,
    ROUND(AVG(rating_overall), 2) as avg_overall_rating
FROM hotel_reviews
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT);

-- 2b. RESTAURANT STATS
DROP TABLE IF EXISTS restaurant_zipcode_stats;
CREATE TABLE restaurant_zipcode_stats AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    COUNT(DISTINCT camis) as restaurant_count,
    ROUND(100.0 * SUM(CASE WHEN grade = 'A' THEN 1 ELSE 0 END) / 
          NULLIF(SUM(CASE WHEN grade IN ('A','B','C') THEN 1 ELSE 0 END), 0), 1) as pct_grade_a,
    COUNT(DISTINCT cuisine_desc) as cuisine_diversity
FROM restaurant_inspections
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT);

-- 2c. CRIME STATS
DROP TABLE IF EXISTS crime_zipcode_stats;
CREATE TABLE crime_zipcode_stats AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    COUNT(*) as total_arrests
FROM nypd_arrests
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT);

-- 2d. MTA STATS
DROP TABLE IF EXISTS mta_zipcode_stats;
CREATE TABLE mta_zipcode_stats AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    COUNT(DISTINCT station_id) as station_count,
    SUM(ridership) as total_ridership
FROM mta_ridership
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT);

-- =======================================================
-- STEP 3: ADVANCED INSIGHTS (RESTORED)
-- =======================================================

-- 3a. TRANSIT PATTERNS (Commuter vs. Leisure Analysis)
DROP TABLE IF EXISTS transit_patterns;
CREATE TABLE transit_patterns AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    -- Ratio of Weekend rides to Weekday rides
    ROUND(
        SUM(CASE WHEN day_of_week IN ('Saturday', 'Sunday') THEN ridership ELSE 0 END) / 
        NULLIF(SUM(CASE WHEN day_of_week NOT IN ('Saturday', 'Sunday') THEN ridership ELSE 0 END), 0)
    , 2) as leisure_intensity_score
FROM mta_ridership
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT);

-- 3b. DOMINANT CUISINE (What food is this zip famous for?)
DROP TABLE IF EXISTS cuisine_specialization;
CREATE TABLE cuisine_specialization AS
SELECT 
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    cuisine_desc,
    COUNT(*) as count
FROM restaurant_inspections
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT), cuisine_desc;

DROP VIEW IF EXISTS neighborhood_food_profile;
CREATE VIEW neighborhood_food_profile AS
SELECT t1.zip_int, t1.cuisine_desc as dominant_cuisine
FROM cuisine_specialization t1
JOIN (
    SELECT zip_int, MAX(count) as max_count 
    FROM cuisine_specialization 
    GROUP BY zip_int
) t2 ON t1.zip_int = t2.zip_int AND t1.count = t2.max_count;

-- =======================================================
-- STEP 4: FINAL JOIN (Including Advanced Columns)
-- =======================================================

DROP TABLE IF EXISTS final_dashboard_data;
CREATE TABLE final_dashboard_data AS
SELECT 
    z.zip_int as zipcode,
    z.borough,
    COALESCE(m.station_count, 0) as station_count,
    COALESCE(m.total_ridership, 0) as total_ridership,
    COALESCE(h.hotel_count, 0) as hotel_count,
    COALESCE(h.avg_overall_rating, 0) as avg_hotel_rating,
    COALESCE(r.restaurant_count, 0) as restaurant_count,
    COALESCE(r.pct_grade_a, 0) as pct_restaurants_grade_a,
    COALESCE(r.cuisine_diversity, 0) as cuisine_diversity,
    COALESCE(c.total_arrests, 0) as total_arrests,
    
    -- SCORE
    ROUND(
        (COALESCE(h.avg_overall_rating, 3) * 2 * 0.30) +
        (COALESCE(r.pct_grade_a, 50) / 10 * 0.20) +
        ((10 - LEAST(COALESCE(c.total_arrests, 0) / 50.0, 10)) * 0.30) +
        (LEAST(COALESCE(m.total_ridership, 0) / 500000.0, 10) * 0.20)
    , 2) as tourist_satisfaction_index,

    -- NEW COLUMNS
    COALESCE(tp.leisure_intensity_score, 0) as leisure_score,
    CASE 
        WHEN COALESCE(tp.leisure_intensity_score, 0) > 0.35 THEN 'Leisure/Tourist Hub'
        WHEN COALESCE(tp.leisure_intensity_score, 0) < 0.20 THEN 'Business/Commuter'
        ELSE 'Mixed Use'
    END as neighborhood_type,
    COALESCE(nfp.dominant_cuisine, 'Unknown') as dominant_cuisine

FROM nyc_zipcodes z
LEFT JOIN mta_zipcode_stats m ON z.zip_int = m.zip_int
LEFT JOIN hotel_zipcode_stats h ON z.zip_int = h.zip_int
LEFT JOIN restaurant_zipcode_stats r ON z.zip_int = r.zip_int
LEFT JOIN crime_zipcode_stats c ON z.zip_int = c.zip_int
LEFT JOIN transit_patterns tp ON z.zip_int = tp.zip_int
LEFT JOIN neighborhood_food_profile nfp ON z.zip_int = nfp.zip_int;

-- =======================================================
-- STEP 5: EXPORT TO HDFS
-- =======================================================
INSERT OVERWRITE DIRECTORY '/user/dn2491_nyu_edu/exports/final_dashboard'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT * FROM final_dashboard_data;
