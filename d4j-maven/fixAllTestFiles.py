"""
In some all_tests files, the format is different. E.g., "testListAddByIndexBoundsChecking2(ArrayStackTest.testListAddByIndexBoundsChecking2) " for collections-25.
But the correct format should be "testSerialization(org.jfree.chart.annotations.junit.CategoryLineAnnotationTests)"
"""

import os, shutil
from pathlib import Path
import subprocess as sp

def getD4jProperty(projPath: Path, property: str):
    return sp.check_output("defects4j export -p {}".format(property), shell=True, universal_newlines=True, cwd=str(projPath)).strip()

def getD4jProjSrcRelativePath(projPath: Path):
    return getD4jProperty(projPath, 'dir.src.tests')

def getPrefix(projPath: Path, relativeSrcPath: str):
    javaFilePath = sp.check_output("find {} -name '*.java' | head -1".format(relativeSrcPath), cwd=str(projPath), universal_newlines=True, shell=True).strip()
    tmp = os.path.relpath(javaFilePath, relativeSrcPath)
    print(str(javaFilePath))
    print(relativeSrcPath)
    print(tmp)
    if tmp.startswith('/'):
        tmp = tmp[1:]
    return tmp.split('/')[0]

def fix(projPath: Path, allTestFilePath: Path, relativeSrcPath=None):
    content = []
    if relativeSrcPath is None:
        relativeSrcPath = getD4jProjSrcRelativePath(projPath)
    # prefix = getPrefix(projPath, relativeSrcPath)
    # print(prefix)
    allTestBakFile = allTestFilePath.parent / (allTestFilePath.name + '.bak')
    if not allTestBakFile.exists():
        shutil.copy(str(allTestFilePath), str(allTestBakFile))
    with allTestFilePath.open() as f:
        cnt = 0
        for line in f:
            tmp = line.strip().split('(')
            testName = tmp[0]
            className = tmp[1][:-1]
            # if not className.startswith(prefix):
            if not className.startswith('org') and not className.startswith('com'):
                # print('Error Line: ' + line.strip())
                correctClassName = getCorrectClassName(projPath, className.split('.')[0], relativeSrcPath)
                cnt += 1
                fixedLine = testName + '(' + correctClassName + ')'
                content.append(fixedLine)
            else:
                content.append(line.strip())
    with allTestFilePath.open(mode='w') as f:
        for line in content:
            f.write(line + '\n')
    print('Fixed {} lines'.format(cnt))

def getCorrectClassName(projPath: Path, simpleClassName: str, relativeSrcPath: str):
    javaFilePathList = sp.check_output("find {} -name '{}.java'".format(relativeSrcPath, simpleClassName), cwd=str(projPath), universal_newlines=True, shell=True).strip().split()
    if len(javaFilePathList) != 1:
        print('There are not one java file in {} sharing with the name {}: {}'.format(str(projPath/ relativeSrcPath), simpleClassName + '.java', str(javaFilePathList)))
    assert len(javaFilePathList) > 0
    tmp = os.path.relpath(javaFilePathList[0], relativeSrcPath)
    if tmp.startswith('/'):
        tmp = tmp[1:]
    return tmp[0:-5].replace('/', '.')


mutBenchProjPath = Path('/home/yicheng/research/mutd4j/validation/d4jMvnForUniapr/d4jMvnProj')
def fixForMutBenchProj():
    for project in mutBenchProjPath.iterdir():
        if not project.is_dir():
            continue
        for projPath in project.iterdir():
            allTestFilePath = projPath / 'all_tests'
            if not projPath.is_dir() or not allTestFilePath.exists():
                continue
            print('Start fixing ' + str(projPath))
            fix(projPath, allTestFilePath)

if __name__ == '__main__':
    # fix(Path('/home/yicheng/research/mutd4j/validation/test/collections-25'), Path('/home/yicheng/research/mutd4j/validation/test/collections-25/all_tests'))
    fixForMutBenchProj()