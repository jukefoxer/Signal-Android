# WhatsApp Data Import for Signal Android

This is a fork of the private messaging app Signal with the goal to provide a method to import WhatsApp conversations and facilitating the transition from WhatsApp to Signal.

You can find the original project at: https://github.com/signalapp/Signal-Android

It's based on the fork of johanw666: https://github.com/johanw666/Signal-Android

## WhatsApp Data Import

This fork of the Signal App aims at providing a method to import one's WhatsApp conversations. It's currently still a pretty tedious process, but at least it's possible.

### What works

* Import 1-to-1 text conversation threads.
* Import group chat conversations if a group chat with the same name is set up in the Signal App.
* Importing images and videos messages from WhatsApp chats. (deactivated because of incorrect timestamps)

### What doesn't work

* Multimedia messages that are not images or videos are currently not imported.
* Multimedia have the wrong timestamp (now).
* It's pretty slow (10 seconds per 1000 messages).

### How to do it

* Extract your unencrypted msgstore.db from your WhatsApp installation. There are several methods to do so. WhatsAppDump seems to offer a possibility that doesn't require rooting the device. A more detailed description of how to do so might be added here in the future.
* Copy the msgstore.db file to the top level directory of your internal storage
* Make an encrypted Backup of your Signal Messages using the built-in feature of the Signal App.
* Build and install this version of the Signal App and import the encrypted Backup of your signal messages.
* You might have to go to the app permission settings and give it the permission to manage all of the external storage.
* Go to Backup => Import WhatsApp to start the import.
* Be patient until it finishes.
* If you're happy with the WhatsApp import create another encrypted backup of all Signal messages.
* Install the original Signal app again and import the encrypted Backup.

# Legal things
## Cryptography Notice

This distribution includes cryptographic software. The country in which you currently reside may have restrictions on the import, possession, use, and/or re-export to another country, of encryption software.
BEFORE using any encryption software, please check your country's laws, regulations and policies concerning the import, possession, or use, and re-export of encryption software, to see if this is permitted.
See <http://www.wassenaar.org/> for more information.

The U.S. Government Department of Commerce, Bureau of Industry and Security (BIS), has classified this software as Export Commodity Control Number (ECCN) 5D002.C.1, which includes information security software using or performing cryptographic functions with asymmetric algorithms.
The form and manner of this distribution makes it eligible for export under the License Exception ENC Technology Software Unrestricted (TSU) exception (see the BIS Export Administration Regulations, Section 740.13) for both object code and source code.

## License

Copyright 2013-2020 Signal

Licensed under the GPLv3: http://www.gnu.org/licenses/gpl-3.0.html

Google Play and the Google Play logo are trademarks of Google Inc.
