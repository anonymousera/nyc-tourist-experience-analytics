USE dn2491_db;

SELECT 
    -- Clean the zipcode just like before
    CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT) as zip_int,
    MAX(borough) as borough,
    SUM(ridership) as total_rides,
    -- Count rides during "Party Hours" (10PM - 4AM)
    SUM(CASE WHEN hour >= 22 OR hour <= 4 THEN ridership ELSE 0 END) as night_rides,
    -- Calculate the Percentage
    ROUND((SUM(CASE WHEN hour >= 22 OR hour <= 4 THEN ridership ELSE 0 END) / SUM(ridership)) * 100, 2) as nightlife_pct
FROM mta_ridership
WHERE zipcode IS NOT NULL AND LENGTH(regexp_replace(zipcode, '[^0-9]', '')) >= 5
GROUP BY CAST(SUBSTR(regexp_replace(zipcode, '[^0-9]', ''), 1, 5) AS INT)
HAVING SUM(ridership) > 10000 -- Filter out tiny stations
ORDER BY nightlife_pct DESC
LIMIT 10;
