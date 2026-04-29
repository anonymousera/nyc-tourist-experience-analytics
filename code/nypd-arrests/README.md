# NYPD Arrests Module

This module processes NYPD arrest records and derives safety-oriented indicators aligned to ZIP-code analysis.

## Key Files

- `NYPDArrestsData.java`
- `NYPDArrestsDataMapper.java`
- `NYPDArrestsDataReducer.java`
- `pom.xml`: Maven build configuration
- `nypd-arrests.jar`: packaged job artifact
- `nyc_zip_data_lookup.csv`: ZIP lookup/enrichment support

## Sample Data and Artifacts

- `input_subset/`: sample input
- `output_subset/`: sample output
- `screenshots/`: execution/result snapshots

## Run Notes

- Build with Maven (`pom.xml`) and run the jar in Hadoop.
- Prefer tracking small sample data in Git and storing full-scale input/output externally.
- Remove platform artifacts (`.DS_Store`, `__MACOSX`) before final submission.
