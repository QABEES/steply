EXECUTE:
```
- Download latest steply.zip file (from /tmp dir)(or 

Folder Structure:
-----------------
➜  pwd 

|random
├── example
│   ├── github_host_new.properties
│   ├── hello_world_status_ok_assertions_new.json
│   └── scenario_with_author_and_tag.json
├── target
│   ├── logs
│   │   └── executions.log
│   ├── zerocode-junit-granular-report.csv
│   ├── zerocode-junit-interactive-fuzzy-search.html
└── tests
   ├── hello_world_status_ok_assertions_new_v2.json
   ├── hello_world_status_ok_assertions_v2.json
   └── scenario_with_author_and_tag.json
➜  random 

- Copy to a different location(eg ~/Downloads/STEPLY_WORKSPACE), unzip it (./steply-dist)
  (This is where the symlink bin/steply.sh will point to)
  
- Execute test scenarios via CLI: 
  steply --scenario example/hello_world_status_ok_assertions_new.json --target-env example/github_host_new.properties
  or
  steply --scenario example/scenario_with_author_and_tag.json --target-env example/github_host_new.properties
  or
  steply --scenario tests/hello_world_status_ok_assertions_v2.json --target-env example/github_host_new.properties
  or
  steply --scenario tests/hello_world_status_ok_assertions_v2.json --host example/github_host_new.properties

- Run a test suite via CLI:
  steply --folder tests --target example/github_host_new.properties

- Even "--target" also works as "--target-env" (even "--targ" also works, CLI parser does a text matching)


```
