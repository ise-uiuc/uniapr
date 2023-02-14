import os
import shutil
import argparse
import subprocess as sp

# v1.2.0: Closure 1-133, v2.0.0: Closure 1-176
# pid: (bid range, deprecated list)
projDict = {
    'Chart': (list(range(1, 27)), []),
    'Closure': (list(range(1, 134)), [63, 93]),
    'Lang': (list(range(1, 66)), [2]),
    'Math': (list(range(1, 107)), []),
    'Mockito': (list(range(1, 39)), []),
    'Time': (list(range(1, 28)), [21])
}

projDir = 'd4j-maven-projects/'


def getSetOfFailingTest(pid, bid):
    process = sp.Popen('defects4j info -p {} -b {}'.format(pid, bid),
                       shell=True, stderr=sp.PIPE, stdout=sp.PIPE, universal_newlines=True)
    stdout, _ = process.communicate()
    lines = stdout.strip().split('\n')
    start = False
    res = set()
    for line in lines:
        if 'Root cause in triggering tests:' in line:
            start = True
        elif '------------------------------------------------------' in line and start == True:
            start = False
            break
        elif line.startswith(' - ') and start == True:
            tmp = line[3:]
            res.add(tmp)
    return res

def applyPatch(pid, bid, projPath: str, patchPath: str, fileToBackup: str):
    """All arguments should be absolute path"""
    if os.path.isfile(fileToBackup) and not os.path.isfile(fileToBackup + '.bak'):
        shutil.copy(fileToBackup, fileToBackup + '.bak')
    elif os.path.isfile(fileToBackup + '.bak') and os.path.isfile(fileToBackup):
        shutil.copy(fileToBackup + '.bak', fileToBackup)
    process = sp.Popen('cd {} && patch -N -p1 < {}'.format(projPath, os.path.abspath(patchPath)),
                       shell=True, stderr=sp.PIPE, stdout=sp.PIPE, universal_newlines=True)
    stdout, stderr = process.communicate()
    exitCode = process.poll()
    succeed = True
    if exitCode != 0:
        # print('[ERROR] Failed to fix {}-{}'.format(pid, bid))
        succeed = False
    print(stdout)
    print(stderr)
    return succeed

def main(args):
    for pid in projDict.keys():
        bidList, depList = projDict[pid]
        for bid in bidList:
            if bid in depList:
                continue
            os.makedirs(os.path.join(projDir, pid), exist_ok=True)
            projPath = os.path.join(projDir, pid, str(bid))

            if os.path.isdir(projPath) and os.path.isfile(os.path.join(projPath, 'all_tests')):  # the existence of all_tests file indicates checking out is complete
                print('Skipping {}'.format(projPath))
                continue
            elif os.path.isdir(projPath):
                shutil.rmtree(projPath)  # if the directory exists, but no all_tests generated, it means the checking out process aborted at some point. Prefer redoing.

            print("\n================ Checking out {}-{} ================".format(pid, bid))

            # checkout defects4j projects
            sp.run("defects4j checkout -p {} -v {}b -w {}".format(pid, bid, projPath), shell=True)

            # copy corresponding pom.xml
            shutil.copy(os.path.join('poms', pid, str(bid), 'pom.xml'), projPath)

            # apply patch to source file
            if pid == 'Lang':
                targetFile = os.path.join(projPath, 'src/test/org/apache/commons/lang/builder/StandardToStringStyleTest.java')
                applyPatch(pid, bid, projPath, os.path.abspath(os.path.join('patches', 'lang.patch')), targetFile)
                if bid == 64:
                    targetFile = os.path.join(projPath, 'src/test/org/apache/commons/lang/enums/ValuedEnumTest.java')
                    applyPatch(pid, bid, projPath, os.path.abspath(os.path.join('patches', 'lang64.patch')), targetFile)
            if pid == 'Math':
                targetFile = os.path.join(projPath, 'src/test/java/org/apache/commons/math3/genetics/UniformCrossoverTest.java')
                succeed = applyPatch(pid, bid, projPath, os.path.abspath(os.path.join('patches', 'math.patch')), targetFile)
                if not succeed:
                    applyPatch(pid, bid, projPath, os.path.abspath(os.path.join('patches', 'math2.patch')), targetFile)
            if pid == 'Time':
                targetFile = os.path.join(projPath, 'src/test/java/org/joda/time/TestDateTime_Basics.java')
                applyPatch(pid, bid, projPath, os.path.abspath(os.path.join('patches', 'time.patch')), targetFile)

            # compile projects
            sp.run('mvn clean', shell=True, check=False, cwd=projPath)
            if pid == 'Closure' and bid == 106:
                process = sp.Popen('mvn test-compile', shell=True, stderr=sp.PIPE,
                                   stdout=sp.PIPE, universal_newlines=True, cwd=projPath)
            else:
                process = sp.Popen('defects4j compile', shell=True, stderr=sp.PIPE,
                                   stdout=sp.PIPE, universal_newlines=True, cwd=projPath)
            stdout, stderr = process.communicate()
            exitCode = process.poll()
            if exitCode != 0:
                print('[ERROR] Failed to compile {}-{}'.format(pid, bid))
            print(stdout)
            print(stderr)

            if args.skipTest:
                # copy the all_tests file
                shutil.copy(os.path.join('all_tests_files', pid, str(bid), 'all_tests'), projPath)
            else:
                # check defects4j test result
                sp.run("defects4j test", shell=True, cwd=projPath)  # to generate all_tests and failing_tests file
                actualFailing = set()
                with open(os.path.join(projPath, 'failing_tests')) as file:
                    for line in file:
                        if line.startswith('--- '):
                            actualFailing.add(line.strip()[4:])
                expectedFailing = getSetOfFailingTest(pid, bid)
                if actualFailing != expectedFailing:
                    print('[ERROR] `defects4j test` fails unexpected tests!')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--skipTest', help='skip running `defects4j test` after checking out projects', action='store_true')
    args = parser.parse_args()
    main(args)
