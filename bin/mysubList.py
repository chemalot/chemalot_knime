#!/bin/env python
from __future__ import absolute_import, division, print_function
import sys
import os
# import re
import subprocess

from mysub import get_scheduler, _get_jobs_with_prefix


def warn(*argv):
    print(*argv, file=sys.stderr) # write to stderr



def list_lsf(args_dict):
    modCmd = ['bjobs', '-o', 'JOBID:7 STAT:4 QUEUE:6 FROM_HOST:11 first_host:11 slots:2 SUBMIT_TIME:12 name']
    #
    if args_dict['q']:
        queue = args_dict['q'][0]
        modCmd += ['-q', newQueue]
    ps = subprocess.Popen(modCmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    psOut, psErr = ps.communicate()
    if sys.version_info.major == 3:
        psOut = psOut.decode()
    print(psOut)
    if psErr:
        if sys.version_info.major == 3:
            psErr = psErr.decode()
        warn(psErr)



def list_slurm(args_dict):
    modCmd = ['squeue', '-u', os.environ['USER']]

    if args_dict['q']:
        queue = args_dict['q'][0]
        modCmd += ['--qos={}'.format(queue)]

    #warn('slurm cmd =', ' '.join(modCmd))
    ps = subprocess.Popen(modCmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    psOut, psErr = ps.communicate()
    if sys.version_info.major == 3:
        psOut = psOut.decode()
    print(psOut)
    if psErr:
        if sys.version_info.major == 3:
            psErr = psErr.decode()
        warn(psErr)



if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter, description='Modify Pending Jobs.')
    parser.add_argument('-q' ,       metavar='queueName' ,   nargs=1,  help='Show only jobs for queue type to veryshort|short|medium|long|verylong|smp|gpu')
    args      = parser.parse_args()
    args_dict = vars(args)

    exit(locals()["list_%s" % get_scheduler()](args_dict))
