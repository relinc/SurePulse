# SurePulse

SurePulse is a data processing and analysis tool aimed at data collected from tests performed on a Split Hopkinson Pressure Bar
## Dependencies
Please install the oracle jdk that comes with java fx included.
Gradle v5 is required.
## Develop
With the latest IntelliJ IDE, import project as Gradle project.

## Build
To build SurePulse, run ```gradle build```. To build a deployable installer run ```gradle jN```. Run ```gradle clean``` if any errors occur. The installer will be located at ```build/jfx/native```.
To build an installer on windows run ```choco install innosetup``` and ```choco install wixtoolset``` (only the first may be necessary)
## Overview
![Flowchart](DataProcessor/SUREPulseSoftwareFlowChart.png)

