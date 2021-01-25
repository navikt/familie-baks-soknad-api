#!/usr/bin/env sh

staged=$(git diff --name-only --cached)
mvn antrun:run@ktlint-format
for file in $staged; do git add $file; done
