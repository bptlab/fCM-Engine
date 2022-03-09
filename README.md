# fCM Engine Based on CPNTools

**The version on this branch has been prepared for a user study evaluating recommendations for knowledge workers. When starting the engine the user can choose whether to receive recommendations or not. The example model is loaded automatically.**

Knowledge workers make interconnected decisions to drive processes.
Therefore, it is necessary to consider the available information, possible actions, and general rules.
The fragment-based Case Management (fCM) approach supports case models describing processes by a set of fragments, a domain model, object life cycles, and termination conditions. 

This repository contains a process execution engine for fCM based on [CPNTools](https://cpntools.org).
It can be used for fCM case models including data associations and their cardinality constraints.
Instead of interpreting case models directly, they've to be translated to CPN nets (see the [fcm2cpn compiler](https://github.com/bptlab/fcm2cpn/tree/caise)).
Given a CPN and an optional domain model (UML class diagram), the engine will guide knowledge workers while executing cases:
* it shows the enabled activities and their variants (input-output combinations)
* if a domain model has been provided forms will be presented to the user
* users can fill forms and complete activities. The state of the engine will be udpated automatically.
* if the user chose recommendations, activities and input-output combinations are filtered: compliant ones are bold, incompliant ones are struck through.

## Content of the Repository
* **Experiment:**
  * The raw data of the user study can be found in `src/main/resources/experiments/results.csv`
* **Examples:**
  * `src/main/resources/experiments/fragments.bpmn` a BPMN defining the fragments of the insurance claim example
  * `src/main/resources/experiments/domain_model.uml` a UML class diagram comprising the domain model for the example (modeled using [Papyrus](https://www.eclipse.org/papyrus/)
  * `src/main/resources/experiments/termination_condition.json` a json file specifying the example's termination condition
  * `colored_petri_net(Experiments).cpn` a CPN defining the behavior of the *Claim Handling* example above (generated using [fcm2cpn](https://github.com/bptlab/fcm2cpn/tree/caise) and manually adapted). This version is used by the engine.
  * `colored_petri_net(State_Space).cpn` a CPN defining the behavior of the *Claim Handling* example above (generated using [fcm2cpn](https://github.com/bptlab/fcm2cpn/tree/caise) and manually adapted). This version has been used for performance measurements. It has a finite state space.
* **Engine:**
  * `src/main/java` contains the source files for the process execution engine.
  * `src/main/resources/scenes` contains fxml and css files for the UI.
  * `lib/*.jar` the [Access/CPN](http://cpntools.org/access-cpn/) libraries required for the prototype.

## Usage

#### [Watch our short demo (~ 3 min) on Youtube.](https://youtu.be/ODpgQvxxQzY)

#### [Watch our not so short demo (~ 9 min) on Youtube.](https://youtu.be/ogvqiO6a9Wg) 

### Prerequisites

In order to run the engine you need
* java version 9 or higher
Furthermore, the project is configured with the gradle build tool and the instructions are written accordingly.
However, you can use an IDE of your choice or compile the sources manually.

### Compiling and Building the Binaries

We use gradle as build tool and for dependency management.
If you installed java and gradle, you can build the engone using the following command in the project's root directory.
````bash
gradle clean build
```` 

### Creating Inputs

Please see the compiler's readme, the paper, and the screencast for information on how to create the required inputs.
The engine takes a CPN describing the behavior of the case model as well as a domain model (UML class diagram) describing the classes and their attributes.

### Limitations
Currently only attributes of type String are supported.

### Running the Engine

You can run the Engine by running 
````bash
gradle run
````
Note, the state of the engine is volatile. Any changes you make will be lost once you close the engine.

### Compatibility

The engine has only been tested under Windows 10 64bit.

### Sources

All the sources are available in `src/main/*`, note that you have to add the Access/CPN libraries (`lib`) to your classpath in order to run/compile the tool.

### Dependencies & Requirements

The implementation requires java Version 9 or higher.
Please note, that the tool has dependencies, and that these dependencies may have different licenses. In the following we list the dependencies
* Access/CPN to create CPNtools compatible CPNs. The dependency is linked as a set of external libraries (see `lib/`)
* Eclipse EMF dependency for using Access/CPN
* jdom for XML parsing (domain model)
* radiance for styling the UI

### License

*fcm-Engine* is an execution engine for CPNs derived from fCM case models.
Copyright (C) 2020  Hasso Plattner Institute gGmbH, University of Potsdam

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
