#!/bin/sh
# Example for an external command with short runtime

echo "Example command"
echo "Received $# arguments:"
for arg in "$@"
do
    echo "Arg: $arg"
done
echo "Example warning" >&2
echo "Finished OK"
