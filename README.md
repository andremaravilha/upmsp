This is a fork of [tuliotoffolo/upmsp](https://github.com/tuliotoffolo/upmsp) in which the heuristic based on Simulated Annealing is modified to incorporate a model that selects a move based on its expected utility.

# Unrelated Parallel Machine Scheduling Problem with Sequence Dependent Setup Times

> **Contributors:** André L. Maravilha<sup>3,4</sup>, Letícia Mayra Pereira<sup>1,4</sup>, Felipe Campelo<sup>2,4</sup>  
> <sup>1</sup> *Programa de Pós-Graduação em Engenharia Elétrica, Universidade Federal de Minas Gerais ([PPGEE](https://www.ppgee.ufmg.br/), [UFMG](https://www.ufmg.br/))*  
> <sup>2</sup> *Depto. Engenharia Elétrica, Universidade Federal de Minas Gerais ([DEE](http://www.dee.ufmg.br/), [UFMG](https://www.ufmg.br/))*  
> <sup>3</sup> *Depto. Informática, Gestão e Design, Centro Fed. de Edu. Tecnológica de Minas Gerais ([DIGD-DV](http://www.digddv.cefetmg.br/), [CEFET-MG](https://www.cefetmg.br/))*  
> <sup>4</sup> *Operations Research and Complex Systems Lab., Universidade Federal de Minas Gerais ([ORCS Lab](http://orcslab.ppgee.ufmg.br/), [UFMG](https://www.ufmg.br/))*

## 1. Overview

Given a set N = {1, ..., n} of n jobs and a set M = {1, ..., m} of m parallel machines, each job j in N has to be processed on exactly one machine k in M. The machines are unrelated, i.e., the processing time of a job depends on the machine to which it is assigned to. Moreover, setup times dependent on the sequence and machine are considered (including initial setup times). The objective is to assign jobs to machines and determine the sequence in which they are processed in order to minimize the makespan, i.e., the time of completion of the last job to leave the system. This problem is known as Unrelated Parallel Machine Scheduling Problem with Sequence Dependent Setup Times (UPMSP).

This repository is a fork of [tuliotoffolo/upmsp](https://github.com/tuliotoffolo/upmsp) in which a heuristic based on Simulated Annealing, proposed by Santos et al. [[1](#references)], is modified to incorporate a model that selects a move (neighborhood function) based on its expected utility.

### 1.1. Instance files

This work used instances proposed by Vallada and Ruiz [[2](#references)] and Fanjul-Peyro et al. [[3](#references)] to perform the computational experiments. They can be downloaded from the home page of the [Applied Optimization Systems](http://soa.iti.es/problem-instances) group:
* [Test instances for calibration from Vallada and Ruiz](http://soa.iti.es/files/RSDSTCalibration.7z)
* [Large and small instances from Vallada and Ruiz](http://soa.iti.es/files/RSDST.7z)
* [Instance generator from Fanjul-Peyro et al.](http://soa.iti.es/files/Release_Instances_generator_UPMS.zip)


## 2. How to build and run this project

### 2.1. Building the project

This project was developed with Java 8. To compile this project you need the Java SE Development Kit 8 (JDK 8) installed in your computer. Inside the root directory of the project, run the following commands:
```
./gradlew clean
./gradlew build
```

After running the commands above, a file `upmsp.jar` will be available in the root directory of the project. You do not need any additional library to run the project. Gradle is configured to include all dependencies in the jar file.


### 2.2. Running the project

#### 2.2.1. Show the help message:

```
java -jar upmsp.jar --help
```
The command above will show commands available with this program. There is two commands available `optimize` and `analyze`. These commands are described bellow.

#### 2.2.2. General structure of the command line

```
java -jar upmsp.jar [command] [command options]
```  
in which `[command]` is the command to run and `[command options]` are command specific options. There are the following commands available:
* `optimize`: Optimize an instance of the problem.
* `analyze`: Perform the neighborhood analysis throughout the optimization process. This was the command used to get data to adjust the prediction model to the expected utility used in the modified Simulated Annealing.


#### 2.2.3. Command "optimize"

Usage:  
```
java -jar upmsp.jar optimize [options] <input> [<output>]
```

Example:  
```
java -jar upmsp.jar optimize --verbose --algorithm sa ./instances/I_50_10_S_1-9_1.txt ./solution.txt
```

Parameters:  
`<input>`  
(Required)
Path to the problem input file.

`[<output>]`  
(Optional)
Path to the (output) solution file.

`--algorithm <VALUE>`  
(Default: `sa`)  
Optimization algorithm. Available values are `sa` and `adaptive-sa`.

`--seed <VALUE>`  
(Default: `0`)  
Seed used to initialize the random number generator used by the algorithms.

`--time-limit <VALUE>`  
(Default: calculated according to the instance size)  
Total time for running the algorithm (in milliseconds).

`--iterations-limit <VALUE>`  
(Default: a very large value)  
Maximum number of iterations the algorithm can perform.

`--initial-temperature <VALUE>`  
(Default: `1.0`)  
Initial temperature for the Simulated Annealing.

`--cooling-rate <VALUE>`  
(Default: `0.96`)  
Cooling rate.

`--iterations-per-temperature <VALUE>`  
(Default: `1176628`)  
Number of iterations to run before change the temperature value.

`--coefficients-file <VALUE>`  
(Required if algorithm is set to `adaptive-sa`)  
Path to file containing the coefficients of the prediction model for the expected utility of moves.

`--update-frequency <VALUE>`  
(Default: `1`)  
Maximum probability of choosing a move.

`--max-probability <VALUE>`  
(Default: `1.0`)  
Maximum probability that can be assigned to the selection of a move.

`--disable <VALUE>`  
Disable a move. This parameter may be used more than once to disable multiple moves. Available values are: `shift`, `direct-swap`, `swap`, `switch`, `task-move` and `two-shift`.

`--track <VALUE>`  
(Optional)  
Track the values of makespan of incumbent solutions found throughout the optimization process. `VALUE` is the path to the file in which the data should be written.

`--verbose`  
If used, the algorithm progress is displayed on the screen.

#### 2.2.4. Command "analyze"

Usage:  
```
java -jar upmsp.jar analyze [options] <input> <output>
```

Examples:  
```
java -jar upmsp.jar analyze ./instances ./data.csv
```

Parameters:  
`<input>`  
(Required)  
Path of the directory with input problem files.

`<output>`  
(Required)  
Path of the (output) CSV file in which the data will be saved.

`--repetitions <VALUE>`  
(Default: `1`)  
Number of times the analysis will be repeated.

`--threads <VALUE>`  
(Default: number of threads available minus 1 or 1 if a single thread is available)  
The number of threads used to perform the analysis.

`--verbose`  
If used, the progress is displayed on the screen.


## References

1. Santos, H.G.; Toffolo, T.A.M.; Silva, C.L.T.F.; Berghe, G.V. "Analysis of stochastic local search methods for the unrelated parallel machine scheduling problem". International Transactions in Operational Research, 26(2), 707-724, 2019. (doi: [10.1111/itor.12316](https://doi.org/10.1111/itor.12316))

2. Vallada, E.; Ruiz, R. "Genetic algorithms for the unrelated parallel machine scheduling problem with sequence dependent setup times". European Journal of Operational Research, 211(3), 612-622, 2011. (doi: [10.1016/j.ejor.2011.01.011](https://doi.org/10.1016/j.ejor.2011.01.011))

3. Fanjul-Peyro, L; Ruiz, R.; Perea, F. "Reformulations and an exact algorithm for unrelated parallel machine scheduling problems with setup times". Computers and Operations Research, 101, 173-182, 2019. (doi: [10.1016/j.cor.2018.07.007](https://doi.org/10.1016/j.cor.2018.07.007))
