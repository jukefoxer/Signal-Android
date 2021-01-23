# WhatsApp Data Import for Signal Android

This is a fork of the private messaging app Signal with the goal to provide a method to import WhatsApp conversations and facilitating the transition from WhatsApp to Signal.

You can find the original project at: https://github.com/signalapp/Signal-Android

It's based on the fork of johanw666: https://github.com/johanw666/Signal-Android

## WhatsApp Data Import

This fork of the Signal App aims at providing a method to import one's WhatsApp conversations. It's currently still a pretty tedious process, but at least it's possible.

### What works

* Import 1-to-1 text conversation threads.
* Import group chat conversations if a group chat with the same name is set up in the Signal App.
* Importing audio images, stickers and videos messages from WhatsApp chats.

### What doesn't work

* Contact cards and locations are currently not imported.
* It's pretty slow (10 seconds per 1000 messages).

### How to do it

* Extract your unencrypted msgstore.db from your WhatsApp installation. There are several methods to do so. You can try WhatsApp-Key-Database-Extractor by Yuvraj: https://github.com/YuvrajRaghuvanshiS/WhatsApp-Key-Database-Extractor
* Copy the msgstore.db file to the top level directory of your internal storage
* Make an encrypted Backup of your Signal Messages using the built-in feature of the Signal App. You should make sure that the version of your Signal app and the version of the importer match. Otherwise there could be incompatibilities.
* Build and install this version of the Signal App.
* You might have to go to the app permission settings and give it the permission to manage all of the external storage.
* Got to backups -> import. Select the options you want, and click on import...
* Be patient until it finishes.
* Export a new backup if you're happy with the result.
* Import that new Backup in your existing Signal app (you might have to reinstall it to do so, as the main Signal app only allows to recover backups after a new installation).

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
