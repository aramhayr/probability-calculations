#!/bin/bash

# Run the probability Calculator
# 
# Usage:
#    ./calculator.bash -h - for help
#    ./calculator.bash -<mode> <tries> <arg1> ... <arg4>
#
#  Created by Aram Airapetian on 12/17/24.

v=1.1.2

utility=Calculator
library=target

echo $utility-$v
echo "Arguments: "

java -jar $library/$utility-$v-jar-with-dependencies.jar "$@"
