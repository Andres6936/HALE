If you just want to download the latest release, go to

https://sourceforge.net/p/hale/wiki/Home/

This game is distributed under the terms of the GPL.  See COPYING.txt for details.

To Compile the source: run

>> javac "@compilerargs.txt"

Note that the "bin" directory must exist and also depending on your platform you might need to replace ":" in the classpath with ";"

Then, create a jar by going into bin/ and doing

>> jar cf hale.jar *

Finally, copy the jar up to the root 'hale' directory.

Start the game by running game.sh (linux) or Game.exe (windows).
