#!/usr/bin/env bash
#
# SPDX-License-Identifier: Apache-2.0
#
# Copyright The original authors
#
# Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
#

# blocked-certificates.sh — the certificates of etc/blocked.certs in the native binary.
#
# Usage:
#   etc/blocked-certificates.sh patch [java-home]   add the entries to the JDK's lib/security/blocked.certs
#                                                   (default: $GRAALVM_HOME, else $JAVA_HOME), which
#                                                   native-image bakes into the binary
#   etc/blocked-certificates.sh check <binary>      fail unless the built binary carries every entry
#
# Patching skips entries the JDK already lists, so running it again changes nothing.

set -euo pipefail

source="$(dirname "$0")/blocked.certs"

# The fingerprints of etc/blocked.certs; a Windows checkout may have CRLF line endings.
fingerprints() {
  tr -d '\r' < "$source" | grep -E '^[0-9A-F]{64}$'
}

patch() {
  local java_home="${1:-${GRAALVM_HOME:-${JAVA_HOME:?set GRAALVM_HOME or JAVA_HOME, or pass the JDK directory}}}"
  if command -v cygpath >/dev/null; then
    java_home="$(cygpath -u "$java_home")"
  fi
  local target="$java_home/lib/security/blocked.certs"
  [ -f "$target" ] || { echo "No blocked.certs in $java_home" >&2; exit 1; }
  # An appended entry must start on a line of its own.
  [ -z "$(tail -c 1 "$target")" ] || echo >> "$target"
  local added=0 fingerprint
  for fingerprint in $(fingerprints); do
    if ! grep -qx "$fingerprint" "$target"; then
      echo "$fingerprint" >> "$target"
      added=$((added + 1))
    fi
  done
  echo "Added $added entries to $target"
}

check() {
  local binary="${1:?pass the native binary}" missing=0 fingerprint
  for fingerprint in $(fingerprints); do
    if ! grep -qaF "$fingerprint" "$binary"; then
      echo "The binary does not block $fingerprint" >&2
      missing=$((missing + 1))
    fi
  done
  [ "$missing" = 0 ] || exit 1
  echo "$binary blocks every certificate in $source"
}

case "${1:-}" in
  patch) shift; patch "$@" ;;
  check) shift; check "$@" ;;
  *) echo "Usage: $0 patch [java-home] | check <binary>" >&2; exit 2 ;;
esac
