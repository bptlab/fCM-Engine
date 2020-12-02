# fCM to CPN Compiler

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

## Content of the Repository
* **Examples:**
  * `src/main/resources/conference_fragments.cpn` a CPN defining the behavior of the *paper submission and reviewing* example from the paper (generated using [fcm2cpn](https://github.com/bptlab/fcm2cpn/tree/caise)).
  * `src/main/resources/conference_domain_model.uml` contains a UML file comprising the domain model for the example (modeled using [Papyrus](https://www.eclipse.org/papyrus/)).
* **Engine:**
  * `src/main/java` contains the source files for the process execution engine.
  * `src/main/resources/scenes` contains fxml and css files for the UI.
  * `lib/*.jar` the [Access/CPN](http://cpntools.org/access-cpn/) libraries required for the prototype.

## Usage

### Prerequisites

In order to run the engine you need
* java version 11 or higher
* a compatible javaFX version.
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
* jfoenix for UI elements

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
