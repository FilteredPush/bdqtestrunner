#!/usr/bin/env bash
parent_path=$( cd "$(dirname "$0)")" ; pwd -P )
java -cp "$parent_path"/target/bdqtestrunner-1.0.0.jar:./* org.filteredpush.qc.bdqtestrunner.TestOfTestSpreasheetUtility
