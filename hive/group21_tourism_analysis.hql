-- =============================================================================
-- NYC TOURISM ANALYTICS - ANALYSIS ONLY
-- Database: dn2491_db
-- =============================================================================

-- 1. Switch to YOUR database
USE dn2491_db;

-- Sanity Check: If this returns empty, STOP. You need to create the base tables first.
SHOW TABLES; 

-- =============================================================================
-- STEP 3: CREATE DIMENSION TABLE 
-- =============================================================================

DROP TABLE IF EXISTS nyc_zipcodes;
CREATE TABLE nyc_zipcodes AS
SELECT DISTINCT 
    zipcode,
    FIRST_VALUE(borough) OVER (PARTITION BY zipcode ORDER BY cnt DESC) as borough
FROM (
    SELECT zipcode, borough, COUNT(*) as cnt 
    FROM mta_ridership 
    WHERE zipcode IS NOT NULL AND zipcode != '' AND LENGTH(zipcode) = 5
    GROUP BY zipcode, borough
) t;

-- =============================================================================
-- STEP 4: AGGREGATIONS
-- =============================================================================

-- 4a. MTA Ridership Aggregates
DROP TABLE IF EXISTS mta_zipcode_stats;
CREATE TABLE mta_zipcode_stats AS
SELECT 
    zipcode,
    borough,
    COUNT(DISTINCT station_id) as station_count,
    SUM(ridership) as total_ridership,
    AVG(ridership) as avg_hourly_ridership,
    SUM(CASE WHEN hour BETWEEN 7 AND 9 THEN ridership ELSE 0 END) as morning_rush_ridership,
    SUM(CASE WHEN hour BETWEEN 17 AND 19 THEN ridership ELSE 0 END) as evening_rush_ridership,
    SUM(CASE WHEN hour >= 22 OR hour <= 5 THEN ridership ELSE 0 END) as late_night_ridership,
    ROUND(100.0 * SUM(CASE WHEN hour >= 22 OR hour <= 5 THEN ridership ELSE 0 END) / 
          NULLIF(SUM(ridership), 0), 2) as late_night_pct
FROM mta_ridership
WHERE zipcode IS NOT NULL AND zipcode != '' AND year = 2024
GROUP BY zipcode, borough;

-- 4b. Hotel Review Aggregates
DROP TABLE IF EXISTS hotel_zipcode_stats;
CREATE TABLE hotel_zipcode_stats AS
SELECT 
    zipcode,
    COUNT(DISTINCT offering_id) as hotel_count,
    COUNT(*) as review_count,
    ROUND(AVG(overall), 2) as avg_overall_rating,
    ROUND(AVG(service), 2) as avg_service_rating,
    ROUND(AVG(cleanliness), 2) as avg_cleanliness_rating,
    ROUND(AVG(location_rating), 2) as avg_location_rating,
    ROUND(AVG(sleep_quality), 2) as avg_sleep_quality,
    ROUND(AVG(value_rating), 2) as avg_value_rating,
    ROUND(AVG(rooms), 2) as avg_rooms_rating
FROM hotel_reviews
WHERE zipcode IS NOT NULL AND zipcode != '' AND overall IS NOT NULL
GROUP BY zipcode;

-- 4c. Restaurant Inspection Aggregates
DROP TABLE IF EXISTS restaurant_zipcode_stats;
CREATE TABLE restaurant_zipcode_stats AS
SELECT 
    zipcode,
    boro,
    COUNT(DISTINCT camis) as restaurant_count,
    COUNT(*) as total_inspections,
    SUM(CASE WHEN grade = 'A' THEN 1 ELSE 0 END) as grade_a_count,
    ROUND(100.0 * SUM(CASE WHEN grade = 'A' THEN 1 ELSE 0 END) / 
          NULLIF(SUM(CASE WHEN grade IN ('A','B','C') THEN 1 ELSE 0 END), 0), 1) as pct_grade_a,
    ROUND(AVG(CASE WHEN score > 0 THEN score END), 1) as avg_score,
    SUM(CASE WHEN critical_flag = 'Critical' THEN 1 ELSE 0 END) as critical_violation_count,
    COUNT(DISTINCT cuisine_description) as cuisine_diversity
FROM restaurant_inspections
WHERE zipcode IS NOT NULL AND zipcode != ''
GROUP BY zipcode, boro;

-- 4d. Crime Aggregates
DROP TABLE IF EXISTS crime_zipcode_stats;
CREATE TABLE crime_zipcode_stats AS
SELECT 
    zipcode,
    arrest_boro as borough,
    COUNT(*) as total_arrests,
    SUM(CASE WHEN law_cat_cd = 'Felony' THEN 1 ELSE 0 END) as felony_count,
    ROUND(100.0 * SUM(CASE WHEN law_cat_cd = 'Felony' THEN 1 ELSE 0 END) / 
          NULLIF(COUNT(*), 0), 1) as felony_pct,
    SUM(CASE WHEN ofns_desc LIKE '%ASSAULT%' THEN 1 ELSE 0 END) as assault_count,
    SUM(CASE WHEN ofns_desc LIKE '%ROBBERY%' THEN 1 ELSE 0 END) as robbery_count
FROM nypd_arrests
WHERE zipcode IS NOT NULL AND zipcode != ''
GROUP BY zipcode, arrest_boro;

-- =============================================================================
-- STEP 5: INTEGRATED ANALYTICS VIEW
-- =============================================================================

DROP VIEW IF EXISTS neighborhood_analytics;
CREATE VIEW neighborhood_analytics AS
SELECT 
    z.zipcode,
    z.borough,
    COALESCE(m.station_count, 0) as station_count,
    COALESCE(m.total_ridership, 0) as total_ridership,
    COALESCE(m.avg_hourly_ridership, 0) as avg_hourly_ridership,
    COALESCE(h.hotel_count, 0) as hotel_count,
    COALESCE(h.avg_overall_rating, 0) as avg_hotel_rating,
    COALESCE(h.avg_value_rating, 0) as avg_hotel_value,
    COALESCE(r.restaurant_count, 0) as restaurant_count,
    COALESCE(r.pct_grade_a, 0) as pct_restaurants_grade_a,
    COALESCE(r.cuisine_diversity, 0) as cuisine_diversity,
    COALESCE(c.total_arrests, 0) as total_arrests,
    COALESCE(c.felony_count, 0) as felony_arrests,
    -- Satisfaction Index Calculation
    ROUND(
        (COALESCE(h.avg_overall_rating, 3) * 2 * 0.30) +
        (COALESCE(r.pct_grade_a, 50) / 10 * 0.20) +
        ((10 - LEAST(COALESCE(c.total_arrests, 0) / 50.0, 10)) * 0.30) +
        (LEAST(COALESCE(m.total_ridership, 0) / 500000.0, 10) * 0.20)
    , 2) as tourist_satisfaction_index
FROM nyc_zipcodes z
LEFT JOIN mta_zipcode_stats m ON z.zipcode = m.zipcode
LEFT JOIN hotel_zipcode_stats h ON z.zipcode = h.zipcode
LEFT JOIN restaurant_zipcode_stats r ON z.zipcode = r.zipcode
LEFT JOIN crime_zipcode_stats c ON z.zipcode = c.zipcode;

-- =============================================================================
-- STEP 6: ADVANCED METRICS (Temporal & Cuisine)
-- =============================================================================

DROP TABLE IF EXISTS transit_patterns;
CREATE TABLE transit_patterns AS
SELECT 
    zipcode,
    borough,
    SUM(CASE WHEN day_of_week IN ('Saturday', 'Sunday') THEN ridership ELSE 0 END) as weekend_ridership,
    SUM(CASE WHEN day_of_week NOT IN ('Saturday', 'Sunday') THEN ridership ELSE 0 END) as weekday_ridership,
    ROUND(
        SUM(CASE WHEN day_of_week IN ('Saturday', 'Sunday') THEN ridership ELSE 0 END) / 
        NULLIF(SUM(CASE WHEN day_of_week NOT IN ('Saturday', 'Sunday') THEN ridership ELSE 0 END), 0)
    , 2) as leisure_intensity_score
FROM mta_ridership
WHERE zipcode IS NOT NULL
GROUP BY zipcode, borough;

DROP TABLE IF EXISTS cuisine_specialization;
CREATE TABLE cuisine_specialization AS
SELECT 
    zipcode,
    cuisine_description,
    count(*) as restaurant_count,
    rank() OVER (PARTITION BY zipcode ORDER BY count(*) DESC) as rank_in_zip
FROM restaurant_inspections
WHERE zipcode IS NOT NULL 
GROUP BY zipcode, cuisine_description;

DROP VIEW IF EXISTS neighborhood_food_profile;
CREATE VIEW neighborhood_food_profile AS
SELECT 
    c.zipcode, 
    c.cuisine_description as dominant_cuisine,
    c.restaurant_count
FROM cuisine_specialization c
WHERE c.rank_in_zip = 1;

-- =============================================================================
-- STEP 7: FINAL DASHBOARD DATA
-- =============================================================================

DROP TABLE IF EXISTS final_dashboard_data;
CREATE TABLE final_dashboard_data AS
SELECT 
    na.*,
    tp.leisure_intensity_score,
    CASE 
        WHEN tp.leisure_intensity_score > 0.35 THEN 'Leisure/Tourist Hub'
        WHEN tp.leisure_intensity_score < 0.20 THEN 'Business/Commuter'
        ELSE 'Mixed Use'
    END as neighborhood_type,
    nfp.dominant_cuisine
FROM neighborhood_analytics na
LEFT JOIN transit_patterns tp ON na.zipcode = tp.zipcode
LEFT JOIN neighborhood_food_profile nfp ON na.zipcode = nfp.zipcode;

-- =============================================================================
-- STEP 8: EXPORT
-- =============================================================================

INSERT OVERWRITE DIRECTORY '/user/dn2491/exports/final_dashboard'
ROW FORMAT DELIMITED FIELDS TERMINATED BY ','
SELECT * FROM final_dashboard_data;
