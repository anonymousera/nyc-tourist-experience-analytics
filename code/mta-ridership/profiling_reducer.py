#!/usr/bin/env python3
"""
MTA Dataset Profiling Reducer - FINAL AGGREGATION FIXED
Aggregates counts so you get ONE line per metric, not millions.
"""

import sys
from collections import defaultdict

def main():
    # Dictionary to store total counts: results[category][key] = total_count
    results = defaultdict(lambda: defaultdict(int))

    for line in sys.stdin:
        line = line.strip()
        if not line:
            continue
            
        try:
            # Parse: Category <tab> Key <tab> Count
            parts = line.split('\t')
            
            if len(parts) >= 3:
                category = parts[0].strip()
                key = parts[1].strip()
                count = int(parts[-1].strip())
                
                # SPECIAL HANDLING:
                # For high-cardinality fields (like timestamps or station names), 
                # we don't want to list every single one in the report.
                # We only want to know if they are "Present" or "Missing".
                
                if category == "non_missing_values":
                    # Group all specific values into a generic "count" for that column
                    results[category][key] += count
                    
                elif category == "station_dist":
                    # For stations, we DO want to keep the specific names to find top stations
                    results[category][key] += count
                    
                elif category == "daily_distribution":
                    # We want to keep dates to find start/end date
                    results[category][key] += count
                    
                else:
                    # For everything else (valid/invalid checks, borough, etc.), keep as is
                    results[category][key] += count

        except ValueError:
            continue

    # --- PRINT THE REPORT ---
    
    # 1. Print non_missing_values (The row counts per column)
    # This will now print just ONE line per column (e.g. "ridership  100000")
    for key, count in sorted(results['non_missing_values'].items()):
        print(f"non_missing_values\t{key}\t{count}")

    # 2. Print Summary Stats (Valid/Invalid, Boroughs, etc)
    for category in sorted(results.keys()):
        if category == 'non_missing_values': continue # Already printed
        
        # Sort by count descending to see "Top" items first
        sorted_items = sorted(results[category].items(), key=lambda x: x[1], reverse=True)
        
        # Limit output for huge lists (like stations or dates) to Top 5 and Bottom 5
        # so your report doesn't explode
        if len(sorted_items) > 20:
            # Print Top 5
            for k, v in sorted_items[:5]:
                print(f"{category}\t{k}\t{v}")
            print(f"{category}\t... [ {len(sorted_items)-10} more items ] ...\t0")
            # Print Bottom 5
            for k, v in sorted_items[-5:]:
                print(f"{category}\t{k}\t{v}")
        else:
            # Print all if it's a short list (like Boroughs)
            for k, v in sorted_items:
                print(f"{category}\t{k}\t{v}")

if __name__ == "__main__":
    main()
