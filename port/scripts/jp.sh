#!/bin/sh
# javap against the dumped compile classpath plus the NeoForge reference jar.
# First dump the classpath: ./gradlew --offline -q -I scripts/cp.gradle dumpCp  (writes <java.io.tmpdir>/mek_cp.txt)
HERE="$(cd "$(dirname "$0")" && pwd)"
NEO="$HERE/../../ref/neo/neoforge.jar"
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) SEP=';'; CPFILE="${TMP:-/tmp}/mek_cp.txt"; [ -f "$CPFILE" ] || CPFILE=/tmp/mek_cp.txt; NEO="$(cygpath -w "$NEO")" ;;
    *) SEP=':'; CPFILE=/tmp/mek_cp.txt ;;
esac
CP="$(tr -d '\r' < "$CPFILE" | tr '\n' "$SEP")$SEP$NEO"
exec javap -classpath "$CP" "$@"
