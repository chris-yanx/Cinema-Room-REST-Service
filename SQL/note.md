## SQL Syntax

```
SELECT column_name [, list_of_other_columns]
     , aggregation [, list_of_aggregations]
FROM table_name
WHERE [list_of_conditions]
GROUP BY column_name [, list_of_other_columns]
HAVING [list_of_aggregate_conditions]
ORDER BY [list_of_columns/aliases];
```

### Aggregations
`COUNT` counts how many rows are in a particular column.

`SUM` adds together all the values in a particular column.

`MIN` and MAX return the lowest and highest values in a particular column, respectively.

`AVG` calculates the average of a group of selected values.


### Distinct
`SELECT DISTINCT` syntax to see only the unique values in a specific column. 

`
SELECT DISTINCT month
FROM tutorial.aapl_historical_stock_price
`

To include two (or more) columns in a SELECT DISTINCT clause, results will contain all of the unique pairs of those two columns:

`
SELECT DISTINCT year, month
FROM tutorial.aapl_historical_stock_price
`



