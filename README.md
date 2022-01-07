youtube-dl-GUI is a GUI frontend for command line program [youtube-dl](https://yt-dl.org/),
which, in turn, can be used to downloaded video from YouTube and other sites.
To run this program, you need to install youtube-dl in one of the PATH directories.
In Windows system, for simplicity, youtube-dl.exe file can be thrown in the same directory as youtube-dl-GUI.jar.
youtube-dl download and installation guide [here](https://github.com/ytdl-org/youtube-dl/blob/master/README.md#installation)

If nothing happens when you click on the youtube-dl-GUI.jar file, you probably need to install the [Java software](https://www.oracle.com/java/technologies/downloads/).
You can run youtube-dl-GUI.jar with Java vesrion 1.8. Compiled binary file is located under bin directory.
The operation of the GUI program itself will hopefully be self-explanatory.
In the URL field paste (Ctrl-V) the address from YouTube and push "Get Info" button. youtube-dl program will collect info about video formats.
For simplicity, in option field lets choose format "Default". But the downloaded video may be slow - too big resolution
and vp9 codec requires faster PC. In this case, you should try with other options.
Keep in mind, it's better to avoid av01 video format. Look for avc1 or vp9.
