# MTA Ridership Module

This module cleans and profiles MTA ridership data to generate transit-access and movement indicators for ZIP-level analysis.

## Key Files

- `cleaning_mapper.py`
- `cleaning_reducer.py`
- `profiling_mapper.py`
- `profiling_reducer.py`

## Inputs

- Raw MTA ridership CSV data (or pre-filtered subset)

## Outputs

- Cleaned and profiled aggregates suitable for Hive integration

## Run Notes

- Run cleaning stage before profiling stage.
- Test with small input subset first.
- Document final HDFS paths used in your environment when reproducing results.
