#!/bin/env python
from __future__ import absolute_import, division, print_function
import sys
import os
import time
import subprocess
import shlex;
import argparse
import logging
import tempfile

logging.basicConfig(level=logging.INFO,
                    format='%(asctime)s %(levelname)-8s %(message)s',
                    datefmt='%a, %d %b %Y %H:%M:%S');

queue_info = {}
queue_info['lsf'] = \
'''               rescomp3 Queue Info:
    --------------------------------------------
    |   QueueName     TimeLimit     Priority   |
    |   veryshort      10 min         50       |
    |   short           2 hrs         40       |
    |   medium         24 hrs         30       |
    |   long           72 hrs         20       |
    |   verylong      unlimited       10       |
    |   smp           unlimited       30       |
    |   gpu           unlimited       30       |
    --------------------------------------------'''


queue_info['slurm'] = \
'''               Rosalind Queue Info:
    --------------------------------------------
    |   QueueName     TimeLimit     Priority   |
    |   veryshort      10 min         1000     |
    |   short           2 hrs         500      |
    |   medium         24 hrs         100      |
    |   long           72 hrs         10       |
    |   verylong       14 days        10       |
    |   interactive     6 days        1000     |
    |   gpu - partition defaults to long       |
    |   smp - partition defaults to long       |
    --------------------------------------------'''


SLURM_PARTITION_MAP = { 'smp':'himem', 'gpu':'gpu' }
DEFAULT_SLURM_QUEUE = "long"

def warn(*argv):
    print(*argv, file=sys.stderr) # write to stderr

def writeFile(fileName, outputString, permission=None):
    """ write string to file, overwriting existing files"""
    f = open(fileName,'w')
    f.write(outputString)
    f.close()

    if permission: os.chmod(fileName, permission)

def writeTempFile(outputString, dir=None, suffix='', permission=None):
    """Note the file is not removed"""
    fd, fp = tempfile.mkstemp(suffix=suffix, prefix='tmp', dir=dir, text="rb" )
    writeFile(fp, outputString, permission)
    os.close(fd)

    if permission is None:
        # restore permissions as of umask
        old_umask = os.umask(0)
        os.umask(old_umask)
        octal_file_chmod = int('666', 8) - old_umask
        os.chmod(fp, octal_file_chmod)
    else:
        os.chmod(fp, permission)

    return fp



def _get_mem_per_cpu(total_requested_memory, requested_num_cpus):
    """ Calculate memory per CPU and set it to at least 1GB if less """

    mem_per_cpu = int( total_requested_memory / requested_num_cpus)
    if mem_per_cpu < 1:
        return 1
    else:
        return mem_per_cpu



def submit_to_lsf(args_dict):
    """
    This function submits job to the LSF scheduler using args specified on the comman line

    :param args_dict: A dictionary of command line arguments
    :type: dict

    :return: if waiting error code from called process, if non-blocking return 1 on error submitting
    :rtype: int
    """

    os.environ['BSUB_STDERR'] = 'y';
    os.environ['LSF_INTERACTIVE_STDERR'] = 'y';

    queue_name    = args_dict['q']
    mem_per_cpu   = _get_mem_per_cpu(args_dict['totalMem'], args_dict['nCPU'])
    num_cpu       = args_dict['nCPU']
    job_name      = args_dict['jobName']
    nGPU          = args_dict['nGPU'];
    if args_dict.get('output', None):
        out_name = args_dict['output']
    elif args_dict['array']:
        out_name = f'{job_name}.%I.log'
    else:
        out_name = f'{job_name}.log'

    bsub_cmd_list  = [ 'bsub', 
                       '-q', queue_name, 
                       '-n', str(num_cpu), 
                       '-R', '"rusage[mem=%d] span[hosts=1]"' % (mem_per_cpu) ];

    if nGPU > 0:
       warn("nGPU currently not supported on lsf!")
       exit(1)

    if args_dict['limit']:
        limit = args_dict['limit']
        if not ':' in limit:
            limit = f'{limit}:00'
        bsub_cmd_list += ['-W', limit]   

    if args_dict['x']:
        bsub_cmd_list += ['-x']   

    if args_dict['waitFor']:
        prefix = args_dict['waitFor']
        bsub_cmd_list += [ '-w', 'ended("%s")' %(prefix) ]

    if args_dict['interactive']: # Interactive Mode: no -oo log option as it write everything to a file
        if args_dict['array']: raise RuntimeError("-array Interactive job not allowed")
        
        bsub_cmd_list += ['-Is', '-J', job_name ];
        if args_dict['cmd']:
            cmd = args_dict['cmd']
            if len(cmd) == 1:
                bsub_cmd_list += args_dict['cmd']
            else:
                # workaround for bug in lsf dealing with single quotes
                bsub_cmd_list += [cmd.replace("'","'\\''") for cmd in args_dict['cmd']]
        if args_dict['debug']:
            warn('-------------------- Submitting Interactive Job ----------------------')
            warn( ' '.join(bsub_cmd_list))

        try:
            subprocess.check_call( bsub_cmd_list );
        except subprocess.CalledProcessError as e:
            logging.error( "Execution: %s failed: %s" % (e.cmd, e.returncode) );
            return e.returncode; #Here we could propagae the error upstream
    else:   # Background Mode.
        if args_dict['array']:
            job_name = f"{job_name}[{args_dict['array'][0]}:{args_dict['array'][1]}]"
        bsub_cmd_list += ['-J', job_name, '-oo', out_name]

        if args_dict['wait']:
            bsub_cmd_list += ['-K']
        if args_dict['cmd']:
            cmd = args_dict['cmd']
            if len(cmd) == 1:
                bsub_cmd_list += args_dict['cmd']
            else:
                # workaround for bug in lsf dealing with single quotes
                bsub_cmd_list += [cmd.replace("'","'\\''") for cmd in args_dict['cmd']]
        if args_dict['debug']:
            warn('-------------------- Submitting Background Job ----------------------')
            warn( ' '.join(bsub_cmd_list))
        
        try:
            cmd_output = subprocess.check_output(bsub_cmd_list).strip();
         
            if cmd_output: warn(cmd_output.decode('utf-8'));
        except subprocess.CalledProcessError as e:
            logging.error( "Execution: %s failed: %s" % (e.cmd, e.returncode) );
            return e.returncode; #Here we could propagate the error upstream
        except OSError as e:
            logging.error( "Execution: %s failed: (%d) %s" % (' '.join(bsub_cmd_list), e.errno, e.strerror ));
            return e.errno; 
 
        return 0;


def _get_jobs_with_prefix(job_prefix):
    """
    :param job_prefix: Prefix or name of the job dependency. This could potentially be regex in the future, but for now we can maintain LSF compatibility
    :type: str

    :return: A list of job ids currently in the system whose name match this prefix or job name
    :rtype: list of str
    """

    cmd_line = "squeue -h -o %j,%A -u " + os.environ['USER'];
    cmd = shlex.split(cmd_line);

    use_regex = False;
    if job_prefix.endswith('*'):
       job_prefix = job_prefix[:-1];
       use_regex = True;

    matching_job_list = None;
    try:
        job_list = subprocess.check_output(cmd, universal_newlines=True).split('\n');
        if use_regex:
           matching_job_list = [job.split(",")[1] for job in job_list if job.startswith(job_prefix)];
        else:
           #There should be at most one jobid here (unless there are multiple jobs with the same name ;-)
           matching_job_list = [job.split(",")[1] for job in job_list if job.split(',')[0] == job_prefix];
           

    except subprocess.CalledProcessError as e:
        logging.error( "Execution: %s failed: %s" % (e.cmd, e.returncode) );
        return None; #Here we could propagae the error upstream
    except OSError as e:
        logging.error( "Execution: %s failed: (%d) %s" % (' '.join(slurm_cmd_list), e.errno, e.strerror ));
        return None;    

    return matching_job_list;


def _slurm_qos2partition(qos_name):
    """
    Given Slurm QOS, this function maps it to the correct partition. This is specific to Slurm, where
    gpu and himem (smp) sit on separate partitions. For now we can use default QoS to long? otherwise,
    we can extend the interface to when one specifies queue, it can be for example qos[:partition]

    :param qos_name: Name of the QoS from the --queue parameter, such as short, smp, gpu
    :type: str

    :return: Name of partition in Slurm if the queue (such as smp) actually maps into a partition, None otherwise
    :rtype: str
    """
    if qos_name in SLURM_PARTITION_MAP:
       return SLURM_PARTITION_MAP[qos_name];
    else:
       return None;


def submit_to_slurm(args_dict):
    """
    This function submits job to the LSF scheduler using args specified on the comman line

    :param args_dict: A dictionary of command line arguments
    :type: dict

    :return: if waiting error code from called process, if non-blocking return 1 on error submitting
    :rtype: int
    """

    queue_name    = args_dict['q'];
    partition_name = _slurm_qos2partition(queue_name);
    if partition_name:
       queue_name = DEFAULT_SLURM_QUEUE
    total_mem     = args_dict['totalMem']
    num_cpu       = args_dict['nCPU']
    job_name      = args_dict['jobName']
    nGPU          = args_dict['nGPU']
    gpu_type      = args_dict['GPUType']
    out_name      = f'{job_name}.log'
    if args_dict.get('output', None):
        out_name = args_dict['output']
    elif args_dict['array']:
        out_name = f'{job_name}.%a.log'
    else:
        out_name = f'{job_name}.log'

    #Note: specific command will be prepended later as the choice is behween sbatch and srun depending on whether the job runs is blocking or non-blocking
    slurm_cmd_list = ['--qos='+queue_name,
                      '--cpus-per-task='+str(num_cpu),
                      '--job-name='+job_name,
                      '--mem='+str(total_mem)+'G'];
    if partition_name:
        slurm_cmd_list += ['-p', partition_name];

    if args_dict['x']:
        slurm_cmd_list += ['--exclusive'];

    if nGPU > 0:
        if gpu_type == 'GAI':
            slurm_cmd_list += ['-p', 'gai', '--account=gai_acct', f'--gres=gpu:volta:{nGPU}']
        else:
            slurm_cmd_list += ['-p', 'gpu', f'--gres=gpu:pascal:{nGPU}']
        #if not args_dict['x']:
        #   slurm_cmd_list += ['--exclusive=user']

    if args_dict['limit']:
        limit = args_dict['limit']
        if not ':' in limit:
            limit = f'{limit}:00'
        slurm_cmd_list += ['-t', f'{limit}:00']

    if args_dict['waitFor']:
        job_prefix = args_dict['waitFor'];
        job_id_list = _get_jobs_with_prefix(job_prefix);
        if job_id_list:
            slurm_cmd_list += ['--dependency=afterany:'+':'.join(job_id_list)];
        else:
            logging.warning("Specified job dependency pattern: \"%s\" but no job with this pattern is currently in the system" % (job_prefix));
            warn("Specified job dependency pattern: \"%s\" but no job with this pattern is currently in the system" % (job_prefix));

    shell = os.environ.get("SHELL","tcsh")
    if args_dict['interactive'] or args_dict['wait']:

        if args_dict['array']: raise RuntimeError("--array not supported for interactive job")

        slurm_cmd_list = ['salloc', '-Q'] + slurm_cmd_list + ['srun', '-Q'] + slurm_cmd_list; 
        if args_dict['wait']: slurm_cmd_list += [ '--output='+out_name]
        cmds = args_dict['cmd']
        if len(cmds) > 0:
            slurm_cmd_list += [shell, '-fc'];
            if len(cmds) == 1:
                slurm_cmd_list +=  cmds
            else:
                cmdStr = ""
                for cmd in cmds:
                   cmdStr += "'" + cmd.replace("'","'\\''") + "' "
                slurm_cmd_list += [cmdStr]
        else:
            # commands are from sdtin eg. "<<coms"
            slurm_cmd_list += [shell, '-f'];
        if args_dict['debug']:
            warn('-------------------- Submitting Interactive Job ----------------------');
            warn(':'.join(slurm_cmd_list));

        try:
            time.sleep(0.05)
            res=subprocess.run( slurm_cmd_list, check=True );
        except subprocess.CalledProcessError as e:
            logging.error( "Execution: %s failed: %s" % (e.cmd, e.returncode) );
            return e.returncode; #Here we could propagate the error upstream
        return 0;
    else:
        script = None
        if args_dict['wait']: slurm_cmd_list += [ '-W']

        out_opt = f'--output={out_name}'
        if args_dict['array']:
            slurm_cmd_list += [f"--array={args_dict['array'][0]}-{args_dict['array'][1]}"]
            out_opt = f'--output={out_name}'
        slurm_cmd_list += [ out_opt ]

        slurm_cmd_list.insert(0, 'sbatch');
        cmds = args_dict['cmd']
        if len(cmds) > 0:
           cmdStr = "#!"+shell+"\n"
           if len(cmds) > 1:
              cmdStr = "#!"+shell+"\n"
              for cmd in cmds:
                 cmdStr += "'" + cmd.replace("'","'\\''") + "' "
           else:
              cmdStr += cmds[0]
           script = writeTempFile(cmdStr,dir='/tmp')
           slurm_cmd_list.extend([script]);
           # else: commands will be read from stdin, eg. with <<

        if args_dict['debug']:
            warn('-------------------- Submitting Background Job ----------------------');
            warn(' '.join(slurm_cmd_list));

        try:
            cmd_output = subprocess.check_output(slurm_cmd_list);
            if script is not None: os.remove(script)
            #warn(cmd_output.strip())
        except subprocess.CalledProcessError as e:
            logging.error( "Execution: %s failed: %s" % (e.cmd, e.returncode) );
            return e.returncode; #Here we could propagae the error upstream
        except OSError as e:
            logging.error( "Execution: %s failed: (%d) %s" % (' '.join(slurm_cmd_list), e.errno, e.strerror ));
            return e.errno;

    return 0;



def _is_exe(fpath):
    """
    This function determines whether a file specified by a full path is executable

    :param fpath: Path to a a file
    :type: str

    :return: True/False
    :rtype: bool 
    """
    return os.path.isfile(fpath) and os.access(fpath, os.X_OK)


def _which(program):
    """
    This function looks for executable along the PATH
    
    :param program: Executable that we are looking for
    :type: str

    :return: full path to the first executable by the name if found, None otherwise
    :rtype: str
    """
    
    fpath, fname = os.path.split(program)
    if fpath:
        if _is_exe(program):
            return program
    else:
        for path in os.environ["PATH"].split(os.pathsep):
            path = path.strip('"')
            exe_file = os.path.join(path, program)
            if _is_exe(exe_file):
                return exe_file

    return None


def get_scheduler():
    """
    This function determines what scheduler we are running on (currently supported are Slurm and LSF)

    :returns: Scheduler type "slurm" or "lsf"
    :rtype: str

    credit: hpcspark
    """
    scheduler = 'lsf'
    if _which('squeue') is not None: 
        scheduler = 'slurm'

    return scheduler


def computeQueue(limitStr):
   splt = limitStr.split(":")
   if len(splt) == 2:
      h = int(splt[0])
      m = int(splt[1])
   else:
      h = int(splt[0])
      m = 0

   if h == 0 and m <= 10: return "veryshort"

   if m > 0: h +=1
   if h <= 2: return "short"
   if h <= 24: return "medium"
   if h <= 72: return "long"

   return "verylong"


# bsub -q short -J 'psi4' -n 4 -R "rusage[mem=5] span[hosts=1]" ~/psi4/bin/psi4 test.dat
# mysub.lsf.py -q quename [-nCPU n] -log logFName -totalMem nGB [-waitFor prefix] [-wait] cmd ....
#
#
# -waitFor prefix submit job such that it executes only after all jobs with given prefix commit
#    bsub option is -w 'ended("<prefia>.*")'
# -log bsub -oo
# -wait is do not exit script until job copletes bsub -K
# -totalMem: note that with bsub totalMem = nCPU*mem


if __name__ == '__main__':
    import copy

    scheduler = get_scheduler();

    parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter, description=queue_info[scheduler])
    parser.add_argument('-q' ,       metavar='queue_or_qos', choices=['veryshort','short','medium','long','verylong','smp','gpu'], help='veryshort|short|medium|long|verylong|smp|gpu', default='veryshort')
    parser.add_argument('-limit',    metavar='0:00',        type=str, help='time limit, e.g. ("12" = 12H, "1:30" = 1.5H), if set no -q option is needed.')
    parser.add_argument('-nCPU',     metavar='int',         type=int, help='Number of CPUs', default=1)
    parser.add_argument('-jobName' , metavar='string',      type=str, help='Job Name also Log Name', required=True)
    parser.add_argument('-output' , metavar='string',      type=str, help='Write stdout and stderror to this file. (default -jobName.log)')
    parser.add_argument('-totalMem', metavar='int',         type=int, help='Total Shared Memory in GB', default=10)
    parser.add_argument('-array',    metavar='int',     type=int, nargs=2, help='start array job with min and max id (lsf: must be > 0)')
    parser.add_argument('-waitFor',  metavar='prefix',      type=str, help='Wait for Jobs to Finish Then Start Current Job. Either prefix \'test*\' or name \'test1\''); 
    parser.add_argument('-wait',     action ='store_true',            help='Execute in Fore Ground')
#    parser.add_argument('-sub' ,     metavar='queueSystem', type=str, help='Which Clusters to Use (default=bsub)', default='bsub')
    parser.add_argument('-interactive', action ='store_true',         help='Execute in Interactive Mode')
    parser.add_argument('-x',           action='store_true' ,         help='Exclusive Other Jobs On Node')
    parser.add_argument('-nGPU',     metavar='int',          type=int,help='Number of GPUs', default=0)
    parser.add_argument('-debug',   action ='store_true',             help='Print additional debug info to stderr')
    parser.add_argument('-GPUType',  metavar='int',          type=str,help='GAI,None', default=None,
                        choices=['GAI'])
    parser.add_argument('--', dest="none", metavar='command',     nargs='*',help='Commands to Submit')


    args = parser.parse_known_args()
    # array with 2 elements Namepsace with mysub options
    #   and array with postional elements starting with --
    args_dict=vars(args[0])
    del args_dict['none']  # remove "none" that is set by dest="none"
    args_dict['cmd'] = args[1][1:]

    if args_dict['limit'] and args_dict['q'] == 'veryshort':
       args_dict['q'] = computeQueue(args_dict['limit'])

    # call submit_to_lsf() or submit_to_slurm()
    exit(locals()["submit_to_%s" % get_scheduler()](args_dict))

