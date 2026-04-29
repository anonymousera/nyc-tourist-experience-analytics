#!/usr/bin/env python3
"""
MTA Dataset Reducer (FIXED)
"""
import sys

def main():
    current_hash = None
    # Header
    print("transit_timestamp,year,month,day,hour,day_of_week,transit_mode,station_id,station_name,borough,payment_method,fare_class,ridership,transfers,latitude,longitude,zipcode")
    
    for line in sys.stdin:
        try:
            key, val = line.strip().split('\t', 1)
            # Extra safety: strip the value part too
            val = val.strip() 
            
            if key != current_hash:
                print(val)
                current_hash = key
        except: continue

if __name__ == "__main__": main()
