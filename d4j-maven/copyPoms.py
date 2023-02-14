import os
import shutil


def copyPoms():
    d4jMvnProjDir = '/home/yicheng/apr/d4jMvn/Projects/'
    for pid in os.listdir(d4jMvnProjDir):
        pidDir = os.path.join(d4jMvnProjDir, pid)
        if not os.path.isdir(pidDir):
            continue
        for bid in os.listdir(pidDir):
            projectPath = os.path.join(pidDir, bid)
            if not os.path.isdir(projectPath):
                continue
            os.makedirs(os.path.join('poms', pid, bid), exist_ok=True)
            sourcePath = os.path.join(projectPath, 'pom.xml')
            targetPath = os.path.join('poms', pid, bid, 'pom.xml')
            print('copying {} to {}'.format(sourcePath, targetPath))
            shutil.copy(sourcePath, targetPath)


def copyAllTestsFiles():
    d4jMvnProjDir = '/home/yicheng/apr/d4jMvnForUniapr/d4jMvnProj/'
    for pid in os.listdir(d4jMvnProjDir):   
        pidDir = os.path.join(d4jMvnProjDir, pid)
        if not os.path.isdir(pidDir):
            continue
        for bid in os.listdir(pidDir):
            projectPath = os.path.join(pidDir, bid)
            if not os.path.isdir(projectPath):
                continue
            os.makedirs(os.path.join('all_tests_files', pid, bid), exist_ok=True)
            sourcePath = os.path.join(projectPath, 'all_tests')
            if not os.path.isfile(sourcePath):
                print('[ERROR] File not found: {}'.format(sourcePath))
                continue
            targetPath = os.path.join('all_tests_files', pid, bid)
            print('copying {} to {}'.format(sourcePath, targetPath))
            shutil.copy(sourcePath, targetPath)


if __name__ == '__main__':
    copyAllTestsFiles()
