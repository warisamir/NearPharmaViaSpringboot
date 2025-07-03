##!/usr/bin/env bash
## wait-for-it.sh
#
## The MIT License (MIT)
## Copyright (c) 2015-2021 vishnubob
#
## Permission is hereby granted, free of charge, to any person obtaining a copy
## of this software and associated documentation files (the "Software"), to deal
## in the Software without restriction, including without limitation the rights
## to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
## copies of the Software, and to permit persons to whom the Software is
## provided to do so, subject to the following conditions:
#
## The above copyright notice and this permission notice shall be included in all
## copies or substantial portions of the Software.
#
## THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
## IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
## FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
## AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
## LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
## OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
## SOFTWARE.
#
## This script will wait for a TCP host and port to become available.
## Usage: ./wait-for-it.sh host:port -- command
#
#TIMEOUT=15
#QUIET=0
#HOST=
#PORT=
#CMD=""
#
#wait_for() {
#  local host="$1"
#  local port="$2"
#  local timeout="$3"
#  local result=1
#  local start_time=$(date +%s)
#
#  while [[ $(($(date +%s) - $start_time)) -lt $timeout ]]; do
#    nc -z "$host" "$port" > /dev/null 2>&1
#    result=$?
#    if [[ $result -eq 0 ]]; then
#      return 0
#    fi
#    sleep 1
#  done
#
#  return $result
#}
#
#parse_args() {
#  while [[ $# -gt 0 ]]; do
#    case "$1" in
#      --timeout)
#        TIMEOUT="$2"
#        shift
#        ;;
#      --quiet)
#        QUIET=1
#        ;;
#      *)
#        if [[ -z "$HOST" ]]; then
#          HOST="$1"
#        elif [[ -z "$PORT" ]]; then
#          PORT="$1"
#        else
#          CMD="$1"
#        fi
#        ;;
#    esac
#    shift
#  done
#}
#
## Parse the arguments
#parse_args "$@"
#
## Ensure we have a host and port
#if [[ -z "$HOST" || -z "$PORT" ]]; then
#  echo "Usage: $0 host:port [--timeout=<timeout>] [--quiet] -- <command>"
#  exit 1
#fi
#
## Wait for the host and port to be available
#echo "Waiting for $HOST:$PORT to be available..."
#if wait_for "$HOST" "$PORT" "$TIMEOUT"; then
#  echo "$HOST:$PORT is available!"
#else
#  echo "Timed out waiting for $HOST:$PORT"
#  exit 1
#fi
#
## Run the command (if provided)
#if [[ -n "$CMD" ]]; then
#  exec $CMD
#fi
