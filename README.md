# NYC Tourist Experience Analytics (RBDA Final Project)

This project builds a neighborhood-level analytics framework to answer a practical question: **which NYC ZIP codes provide the strongest overall tourist experience, and why**.  
We integrate hotel review quality, restaurant inspection quality, subway ridership, and NYPD arrest data into a unified ZIP-code model and analyze trade-offs across safety, mobility, dining, and lodging.

## Problem Statement

Urban tourism quality is multi-dimensional. Individual datasets only reveal partial signals.  
This repository combines four public datasets and produces integrated analytics to support data-driven comparisons of NYC neighborhoods.

## Tech Stack

- Hadoop ecosystem (HDFS, MapReduce)
- Java (MapReduce jobs)
- Python (cleaning/profiling pipelines)
- Hive / HiveQL (integration and analytical queries)
- Tableau (visual outputs in report)

## Data Sources Used

- **Hotel Reviews**: lodging quality and sentiment-style quality indicators
- **Restaurant Inspections**: food safety/inspection-based quality signals
- **MTA Ridership**: transit accessibility and movement intensity
- **NYPD Arrest Data**: safety context at neighborhood level

## End-to-End Pipeline

1. Ingest raw/semi-structured datasets.
2. Clean and standardize each source independently.
3. Transform to ZIP-code-compatible keys and aggregations.
4. Integrate unified tables via Hive.
5. Run analytical Hive queries to compute tourism-oriented indicators.
6. Export result datasets used in final report/visualization.

## Tourist Satisfaction Index (High-Level)

The project computes a composite tourism experience score at ZIP-code level by combining:

- Safety-related signals (crime/arrest context)
- Mobility/accessibility signals (MTA usage pattern)
- Dining quality signals (inspection outcomes)
- Lodging quality signals (hotel review-derived measures)

Weights and exact query logic are defined in the Hive layer and described in the report artifacts.

## Repository Structure

Current structure (before reorganization):

- `hotel_codes/`: Java MapReduce components for hotel pipeline
- `MTA_codes/`: Python cleaning/profiling reducers/mappers
- `nypd_arrest_codes/`: Java + Maven NYPD processing code and subsets
- `restaurant_inspection_codes/`: Java processing and execution scripts
- `group21_*.hql`: Hive integration and analysis scripts
- `RBDA_Final_Project_Report.pdf`: final report

## Reproducibility Notes

This repo contains code and sample subsets used in project execution. Full raw datasets may be too large for source control and should be managed through external storage/HDFS paths.

For each pipeline module, see folder-specific `README.md` files:

- `hotel_codes/README.md`
- `MTA_codes/README.md`
- `nypd_arrest_codes/README.md`
- `restaurant_inspection_codes/README.md``

