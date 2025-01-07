#!/usr/local/bin/bash

#  process-csv.bash
#
# Creates summary table of the Appendix of [Hay2024b]
# Usage: ./process-csv.bash <path/to/folder> <abc>

# -------------------------------------------------------
# This script does the following:
##  Checks for correct number of arguments.
##  Sets up the output file name based on the folder name and abc (typically <tries> is useful) parameter.
##  Defines a map for run types (you can add more as needed).
##  Defines the rows to extract.
##  Initializes the output file with the header row.
##  Processes each matching CSV file:
## -- Extracts the run type from the filename.
## -- Replaces the run type with the mapped value if available.
## -- Adds the run name to the output file.
## -- Extracts and appends the specified rows.
##  Outputs a completion message.
# -------------------------------------------------------

#  Created by Aram Airapetian & Perplexity.ai on 12/22/24.
#

# Define the map for run types
declare -A run_map
run_map["run-b"]="Bertrand (B#2)"
run_map["run-e"]="Point on ellipse (B#3)"
run_map["run-G"]="Generated triangle"
run_map["run-n"]="Gaussian (normal)"
run_map["run-r"]="Point on rectangle"
run_map["run-f"]="Fractal real number"
run_map["run-i"]="Random quotent"
run_map["run-s"]="S method"
run_map["run-m"]="M method"
run_map["run-l"]="L method"

# Check if correct number of arguments is provided
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <folder_path> <abc>"
    exit 1
fi

folder_path="$1"
abc="$2"

# Define the output file name
output_file="${folder_path##*/}-$abc.csv"

# Define the rows to extract
rows_to_extract=("SdM" "MdL" "ρ1" "Θ1" "Version")

# Initialize the output file with the header
echo "Columns,μ,Md,Min,Max,σ²,σ,Σ,Skew,Kurt" > "$output_file"

# Process each matching CSV file
for file in "$folder_path"/*-"$abc"-*.csv; do
    if [ -f "$file" ]; then
        # Extract the run type from the filename
        run_type=$(basename "$file" | grep -oE 'run-[a-zA-Z]')
        
        # Replace run type with mapped value
        if [ -n "${run_map[$run_type]}" ]; then
            run_name="${run_map[$run_type]}"
        else
            run_name="$run_type"
        fi
        
        # Add the run name to the output file
        echo "$run_name" >> "$output_file"
        
        # Extract and append specified rows
        for row in "${rows_to_extract[@]}"; do
            grep "^$row," "$file" >> "$output_file"
        done
    fi
done

echo "Processing complete. Output saved to $output_file"
