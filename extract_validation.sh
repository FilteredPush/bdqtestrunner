#!/usr/bin/env bash
parent_path=$( cd "$(dirname "$0)")" ; pwd -P )
java -cp "$parent_path"/target/bdqtestrunner-0.0.1-SNAPSHOT.jar:./* org.filteredpush.qc.bdqtestrunner.TestOfTestSpreasheetUtility
