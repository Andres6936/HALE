#!/bin/bash

java -Djava.library.path=lib/native/linux -classpath hale.jar:lib/lwjgl.jar:lib/TWL.jar:lib/xpp3-1.1.4c.jar:lib/json-smart-1.0.9.jar net.sf.hale.Game
