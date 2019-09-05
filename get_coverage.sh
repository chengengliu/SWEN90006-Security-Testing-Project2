#!/bin/sh

if [ -z ${TOOL_SUFFIX+x} ]
then
    # TOOL_SUFFIX not set
    # try to intelligently set TOOL_SUFFIX
    case $(uname) in
        Linux)
            TOOL_SUFFIX=-6.0
            ;;
        Darwin)
            TOOL_SUFFIX=
            ;;
        *)
            TOOL_SUFFIX=-6.0
    esac
fi


CLANG=clang${TOOL_SUFFIX}
LLVM_PROFDATA=llvm-profdata${TOOL_SUFFIX}
LLVM_COV=llvm-cov${TOOL_SUFFIX}

if [ -z "$(which ${CLANG})" ]
then
    echo "${CLANG} doesn't exist. Try setting TOOL_SUFFIX environment variable"
    exit 1
fi

if [ -z "$(which ${LLVM_PROFDATA})" ]
then
    echo "${LLVM_PROFDATA} doesn't exist. Try setting TOOL_SUFFIX environment variable"
    exit 1
fi

if [ -z "$(which ${LLVM_COV})" ]
then
    echo "${LLVM_COV} doesn't exist. Try setting TOOL_SUFFIX environment variable"
    exit 1
fi

echo "using ${CLANG}, ${LLVM_PROFDATA} and ${LLVM_COV}"

export LLVM_PROFILE_FILE="passbook-%m.profraw"

if [ $# -lt 1 ]
then
    echo "Usage: $0 inputfile1 [inputfile2 ...]"
    exit 1
fi


rm -f passbook*.profraw passbook.profdata
echo "First re-building to make sure -DNDEBUG is turned on..."
BINARY=./bin/original/passbook-cov
rm -f ${BINARY}
CLANG=${CLANG} CFLAGS="-DNDEBUG ${CFLAGS}" make ${BINARY}

${BINARY} $*

${LLVM_PROFDATA} merge -sparse passbook*.profraw -o passbook.profdata
${LLVM_COV} show ${BINARY} -instr-profile=passbook.profdata
${LLVM_COV} report ${BINARY} -instr-profile=passbook.profdata
