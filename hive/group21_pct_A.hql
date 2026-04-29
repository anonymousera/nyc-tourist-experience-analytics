SELECT 
    cuisine_desc,
    COUNT(*) as total_inspections,
    -- Count A Grades
    SUM(CASE WHEN grade = 'A' THEN 1 ELSE 0 END) as grade_a_count,
    -- Calculate Percentage
    ROUND((SUM(CASE WHEN grade = 'A' THEN 1 ELSE 0 END) / COUNT(*)) * 100, 1) as pct_clean
FROM restaurant_inspections
GROUP BY cuisine_desc
HAVING COUNT(*) > 50 -- Only look at major cuisines
ORDER BY pct_clean DESC;
