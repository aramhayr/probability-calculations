#!/bin/bash

# Runs multiple probability calculations
#
# For property and functinality descritpions
# run:
#    ./calculator.bash -h
#
#  Created by Aram Airapetian on 12/17/24.
#

tries=175000

# Mode specifiers
o=o
t=t

timestamp=$(date "+%Y-%m-%d %H:%M:%S")

./calculator.bash -h

# Triangle Generation
echo "Started batch processing: $timestamp"
echo "Triangles Generation: $timestamp"
./calculator.bash -G$t$o $tries 1 1 y n

# Rectangle
echo "Rectangle: $timestamp"
./calculator.bash -r$t$o $tries -0.5 0.5 -0.5 0.5
./calculator.bash -r$t$o $tries -1.0 1.0 -0.5 0.5
./calculator.bash -r$t$o $tries -1.5 1.5 -0.5 0.5

# Ellipse
echo "Ellipse: $timestamp"
./calculator.bash -e$t$o $tries 1 1 0 0
./calculator.bash -e$t$o $tries 1 2 0 0
./calculator.bash -e$t$o $tries 1 3 0 0

# Normal
echo "Normal: $timestamp"
./calculator.bash -n$t$o $tries
# Bertrand 2nd method
./calculator.bash -b$t$o $tries

# Infinite plane: 2 random number division
echo "Infinite planes: $timestamp"
./calculator.bash -i$t$o $tries 0 1 0.09 1.4
# Infinite plane: fractal squares
./calculator.bash -f$t$o $tries 8

#S, M, and l methods
echo "S, M, and l methods: $timestamp"
./calculator.bash -s$t$o $tries 137
./calculator.bash -m$t$o $tries
./calculator.bash -l$t$o $tries
echo "Ended batch processing:  $timestamp"
