#!/usr/bin/env bash
# Compile and open the simulation window.
#   scripts/run.sh            open the window at generation 0
#   scripts/run.sh 200        open it and run 200 generations
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/javafx-path.sh

rm -rf build && mkdir build
javac ${FX_JARS:+--module-path "$FX_JARS" --add-modules javafx.controls} -d build src/*.java
exec java ${FX_JARS:+--module-path "$FX_JARS" --add-modules javafx.controls} \
  -cp build SimulatorView ${1:+--steps="$1"}
