#!/usr/bin/env bash

BLINK='\033[33;5;7m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

containsElement () {
  local e match="$1"
  shift
  for e; do [[ "$e" == "$match" ]] && return 0; done
  return 1
}

staged=$(git diff --name-only --cached)
unstaged=$(git diff --name-only)


for file in $staged; do
  if containsElement "$file" "${unstaged[@]}"; then
    echo " "
    echo -e "========== ${BLINK}Partially staged file detected, we won't be able to automatically format and restage. Running check only.${NC} =========="
    echo " "

    mvn antrun:run@ktlint -q
    retval=$?
    if [ $retval -ne 0 ]; then
      echo " "
      echo -e "========== ktlint check failed. Run ${YELLOW}mvn antrun:run@ktlint-format${NC} and re-stage the file before committing again =========="
      echo " "

      exit $retval
    fi
    # No need to run check for every file, time to leave the loop
    break
  fi
done

mvn antrun:run@ktlint-format
