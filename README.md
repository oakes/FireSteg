Introduction:

FireSteg is a steganography sidebar extension powered by Java. Unlike many other steg programs, it:

-Hides multiple files across multiple images.
-Remembers the file names of whatever you hide.
-Automatically uses the lowest significant bit that space allows.
-Quickly adds images from websites via context menu.
-Encrypts all files and file names with a password before hiding them.

Directions:

After installing, you can use it by either going to Sidebar in the View menu, add the button to the the toolbar, or simply press Ctrl+Shift+F (on OS X replace Ctrl with Cmd).

How it works:

There are only two XUL files: firesteg.xul for the content in the sidebar, and firefoxOverlay.xul for the added View-->Sidebar menuitem, toolbar button, and the context menu items that appear when you right-click an image.

It also comes with FireSteg.jar in the root of the extension directory, which is a java program I wrote which takes command line arguments supplied by firesteg.xul (using nsIProcess). It does the actual steganography.

FireSteg should uninstall cleanly from your system, but keep in mind that if you add an image from a website via the added context menu items, FireSteg will pull it from the cache and save it in the FireSteg_Temp folder in your Firefox profile. That image will be deleted by firesteg.xul if you press "Remove" in the sidebar or by FireSteg.jar if you actually use it to hide or extract, to keep the folder from filling up.

Legal stuff:

I'm releasing it all under GPL version 2. You can find the source for the java app in the "src" directory of the extension.

This extension is based off the DIIT steganography project, which is available at the following address: http://diit.sourceforge.net/
