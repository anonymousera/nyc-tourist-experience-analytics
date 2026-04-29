#!/usr/bin/env python3
"""
MTA Dataset Profiling Mapper
Analyzes data quality metrics for each column
Author: Debdeep Naha (dn2491)
Course: CSGA 2436 - Realtime and Big Data Analytics
"""

import sys
import csv
import re
from datetime import datetime

def is_valid_timestamp(timestamp_str):
    """Validate timestamp format MM/DD/YYYY HH:MM:SS AM/PM"""
    try:
        datetime.strptime(timestamp_str, '%m/%d/%Y %I:%M:%S %p')
        return True
    except:
        return False

def is_valid_coordinate(lat, lon):
    """Validate latitude and longitude ranges"""
    try:
        lat_val = float(lat)
        lon_val = float(lon)
        # NYC approximate bounds: lat 40.4-41.0, lon -74.3 to -73.7
        return (40.0 <= lat_val <= 41.5) and (-75.0 <= lon_val <= -73.0)
    except:
        return False

def is_valid_ridership(ridership_str):
    """Check if ridership is a valid number"""
    try:
        # Remove commas and check if it's a valid number
        int(ridership_str.replace(',', ''))
        return True
    except:
        return False

def main():
    reader = csv.reader(sys.stdin)
    header = next(reader, None)  # Skip header

    line_num = 0
    for row in reader:
        line_num += 1

        # Handle incomplete rows
        if len(row) != 12:
            print(f"row_count_issue\tincomplete_rows\t1")
            continue

        try:
            transit_timestamp = row[0].strip()
            transit_mode = row[1].strip()
            station_complex_id = row[2].strip()
            station_complex = row[3].strip()
            borough = row[4].strip()
            payment_method = row[5].strip()
            fare_class_category = row[6].strip()
            ridership = row[7].strip()
            transfers = row[8].strip()
            latitude = row[9].strip()
            longitude = row[10].strip()
            georeference = row[11].strip()

            # Total record count
            print(f"total_records\tcount\t1")

            # 1. Check for missing values in each column
            columns = [
                ('transit_timestamp', transit_timestamp),
                ('transit_mode', transit_mode),
                ('station_complex_id', station_complex_id),
                ('station_complex', station_complex),
                ('borough', borough),
                ('payment_method', payment_method),
                ('fare_class_category', fare_class_category),
                ('ridership', ridership),
                ('transfers', transfers),
                ('latitude', latitude),
                ('longitude', longitude),
                ('georeference', georeference)
            ]

            for col_name, col_value in columns:
                if not col_value or col_value.lower() in ['null', 'na', 'n/a', '']:
                    print(f"missing_values\t{col_name}\t1")
                else:
                    print(f"non_missing_values\t{col_name}\t1")

            # 2. Timestamp validation
            if transit_timestamp:
                if is_valid_timestamp(transit_timestamp):
                    print(f"timestamp_validation\tvalid\t1")
                    # Extract hour for temporal analysis
                    try:
                        dt = datetime.strptime(transit_timestamp, '%m/%d/%Y %I:%M:%S %p')
                        print(f"hourly_distribution\t{dt.hour:02d}\t1")
                        print(f"daily_distribution\t{dt.strftime('%Y-%m-%d')}\t1")
                        print(f"day_of_week\t{dt.strftime('%A')}\t1")
                    except:
                        pass
                else:
                    print(f"timestamp_validation\tinvalid\t1")

            # 3. Transit mode distribution
            if transit_mode:
                print(f"transit_mode_dist\t{transit_mode}\t1")

            # 4. Borough distribution
            if borough:
                print(f"borough_dist\t{borough}\t1")

            # 5. Payment method distribution
            if payment_method:
                print(f"payment_method_dist\t{payment_method}\t1")

            # 6. Fare class distribution
            if fare_class_category:
                print(f"fare_class_dist\t{fare_class_category}\t1")

            # 7. Station complex distribution (top stations)
            if station_complex:
                print(f"station_dist\t{station_complex}\t1")

            # 8. Ridership validation and statistics
            if ridership:
                if is_valid_ridership(ridership):
                    print(f"ridership_validation\tvalid\t1")
                    ridership_val = int(ridership.replace(',', ''))
                    print(f"ridership_sum\ttotal\t{ridership_val}")

                    # Categorize ridership
                    if ridership_val == 0:
                        print(f"ridership_category\tzero\t1")
                    elif ridership_val <= 10:
                        print(f"ridership_category\tlow_1-10\t1")
                    elif ridership_val <= 50:
                        print(f"ridership_category\tmedium_11-50\t1")
                    elif ridership_val <= 200:
                        print(f"ridership_category\thigh_51-200\t1")
                    else:
                        print(f"ridership_category\tvery_high_200+\t1")

                    # Check for outliers (extremely high ridership)
                    if ridership_val > 5000:
                        print(f"ridership_outliers\textremely_high\t1")
                else:
                    print(f"ridership_validation\tinvalid\t1")

            # 9. Transfers validation
            if transfers:
                try:
                    transfers_val = int(transfers.replace(',', ''))
                    print(f"transfers_validation\tvalid\t1")
                    print(f"transfers_sum\ttotal\t{transfers_val}")

                    # Transfers analysis
                    if transfers_val == 0:
                        print(f"transfers_category\tno_transfers\t1")
                    elif transfers_val <= 5:
                        print(f"transfers_category\tlow_1-5\t1")
                    else:
                        print(f"transfers_category\thigh_6+\t1")
                except:
                    print(f"transfers_validation\tinvalid\t1")

            # 10. Coordinate validation
            if latitude and longitude:
                if is_valid_coordinate(latitude, longitude):
                    print(f"coordinate_validation\tvalid\t1")
                else:
                    print(f"coordinate_validation\tinvalid\t1")

            # 11. Station ID validation
            if station_complex_id:
                try:
                    int(station_complex_id)
                    print(f"station_id_validation\tvalid\t1")
                    print(f"unique_stations\t{station_complex_id}\t1")
                except:
                    print(f"station_id_validation\tinvalid\t1")

            # 12. Check for duplicate detection (emit key for duplicate checking)
            duplicate_key = f"{transit_timestamp}|{station_complex_id}|{payment_method}|{fare_class_category}"
            print(f"duplicate_check\t{duplicate_key}\t1")

        except Exception as e:
            print(f"parsing_errors\terror\t1", file=sys.stderr)
            continue

if __name__ == "__main__":
    main()
