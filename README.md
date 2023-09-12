# Klonk
Klonk is a simple but robust text editor that aims for fluid ease-of-use and convenience over exotic features. I wrote it for myself and use it as my everyday editor.

Capabilities worth mentioning:

* 100% Java as well as convenient OSX/MacOS & Windows executables
* Keyboard-friendly but equally menu-friendly
* Dirt-simple undo/redo that allows [recovery of all edit states](./TheGURQ.md) from beginning to end
* Open files over SSH
* File encryption
* Execute shell scripts without switching applications
* The usual things: Trailing-whitespace-trim, auto-indent with tabs or spaces, line wrap control, sorting, marking, alignment tricks, multi-line find & replace with regex, and so forth

# Download
[Download zip file here](https://zaboople.github.io/pages/klonk/index.html). Includes jar & Windows .exe files (for MacOS installation refer to "Building it" below). Note that the Windows .exe does not include the required Java installation on your computer. We've been building for Java 16 lately.

# Building it
To build, use a Java 9+ JDK and a reasonably recent version of [Apache Ant](http://ant.apache.org/).

# Building and running natively

## Windows
To build a Windows Klonk.exe you will need [JSmooth](http://jsmooth.sourceforge.net/). Type `ant help` in the git checkout directory for detailed instructions. Note that because the executable does not contain a Java virtual machine of its own, it needs to find one on your computer. You can go to:
  - Desktop
  - Right click "My Computer" (or whatever it's named)
  - Click "Properties
  - Select the "Advanced" tab
  - Click "Environment Variables"
  - Under "System variables" click "New"
    - For Variable Name, enter "JAVA_HOME";
    - For Variable Value, enter the path of your computer's java install, e.g. "c:\Program Files\Java\jre-9.0.1"
    - And click "Apply" or "Save"

## Macintosh
OSX/MacOS native executables are supported via Java's built-in `javapackager` utility. Refer to the script [lib/makedmg](lib/makedmg). Note that this script includes a `-Bruntime=` flag that tells javapackager not to put a java JRE into the install, and this only works correctly when building for java 1.8.0_92 and above. You need to remove it for earlier versions of Java 8 (or just upgrade to the latest).
