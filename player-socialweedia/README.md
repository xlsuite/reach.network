# player-chromeapp

## Introduction

*player-chromeapp is a Chrome App, responsible for launching Viewer, to display HTML content from Rise Vision - our digital signage management application. More information about Viewer can be found in the Rise-Vision/viewer repository. Because player-chromeapp was built on the Chrome App architecture, we are able to provide a flexible Player for the Rise Vision digital signage application that can run on Chrome OS, Linux, Windows and MAC*

player-chromeapp works in conjunction with [Rise Vision](http://www.risevision.com), the [digital signage management application](http://rva.risevision.com/) that runs on [Google Cloud](https://cloud.google.com).

At this time Chrome is the only browser that this project and Rise Vision supports.

## Built With
- [Chrome Apps](https://developer.chrome.com/apps/about_apps)
- *JavaScript*

## Development

### Local Development Environment Setup and Installation

- Open Chrome browser and navigate to, chrome://extensions
- Select the checkbox, “Developer mode” in the top right
- Select “Load unpacked extensions…”
- Browse to and select the directory of your local repository for the Project
- Once loaded, the Chrome App "Rise Vision Chrome App Player" can be ran from the Chrome App Launcher


player-chromeapp uses the standard Chrome App architecture. To facilitate communication between Viewer and player, two local web servers are created using chrome.socket library.

- Player “js/player/player.js” running on port 9449 handles viewer commands.
- Cache “js/cache/cache.js” running on port 9494 is cache server for video files.

### Run Local
- "Rise Vision Chrome App Player" can be ran from the Chrome App Launcher
- Player will open full screen on primary monitor.
- Upon startup, the Rise Vision Player will require either a Display ID or Claim ID to connect your Display to the Platform. From the [Rise Vision Platform](http://rva.risevision.com) click on Displays, then Add Display give it a name and click save. Copy the Display ID and enter it in the Rise Vision Player on startup.
- One can change the display id, or shift between test and production platform by opening the ["Rise Player Configuration"](http://localhost:9449/config) page. Note: this page will be available only when the app is running.

### Dependencies
Latest version of Chrome Browser

## Submitting Issues
If you encounter problems or find defects we really want to hear about them. If you could take the time to add them as issues to this Repository it would be most appreciated. When reporting issues please use the following format where applicable:

**Reproduction Steps**

1. did this
2. then that
3. followed by this (screenshots / video captures always help)

**Expected Results**

What you expected to happen.

**Actual Results**

What actually happened. (screenshots / video captures always help)

## Contributing
All contributions are greatly appreciated and welcome! If you would first like to sound out your contribution ideas please post your thoughts to our [community](http://community.risevision.com), otherwise submit a pull request and we will do our best to incorporate it

### Languages
*If this Project supports Internationalization include this section:*

If you would like translate the user interface for this product to another language please complete the following:
- Download the english translation file from this repository.
- Download and install POEdit. This is software that you can use to write translations into another language.
- Open the translation file in the [POEdit](http://www.poedit.net/) program and set the language for which you are writing a translation.
- In the Source text window, you will see the English word or phrase to be translated. You can provide a translation for it in the Translation window.
- When the translation is complete, save it with a .po extension and email the file to support@risevision.com. Please be sure to indicate the Widget or app the translation file is for, as well as the language that it has been translated into, and we will integrate it after the translation has been verified.

*if the Project does not support Internationalization include this section and include this need in our suggested contributions*

In order to support languages i18n needs to be added to this repository.  Please refer to our Suggested Contributions.

### Suggested Contributions
- *we need this*
- *and we need that*
- *we could really use this*
- *and if we don't already have it (see above), we could use i18n Language Support*

## Resources
If you have any questions or problems please don't hesitate to join our lively and responsive community at http://community.risevision.com.

If you are looking for user documentation on Rise Vision please see http://www.risevision.com/help/users/

If you would like more information on developing applications for Rise Vision please visit http://www.risevision.com/help/developers/.

**Facilitator**

[Alan Clayton](https://github.com/alanclayton "Alan Clayton")
