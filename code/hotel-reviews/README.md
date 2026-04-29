# Hotel Reviews Module

This module processes hotel review data and produces lodging-quality signals used in the NYC tourism analytics pipeline.

## Key Files

- `HotelReviewsDriver.java`: job orchestration
- `HotelReviewsMapper.java`: extraction/transformation logic
- `HotelReviewsReducer.java`: aggregation logic
- `data_snippet_json_file.json`: sample input snippet
- `shell commands.java`: historical run command notes (recommended rename: `run_commands.txt`)

## Inputs

- Raw or sampled hotel review records (JSON-like structure)

## Outputs

- Aggregated intermediate output files (example artifacts: `filtered-r-00000`, `stats-r-00000`)

## Run Notes

- Compile Java classes and package/run in Hadoop environment.
- Validate mapper/reducer output on small subset before full run.
- Keep only sample artifacts in Git; store full outputs externally.
