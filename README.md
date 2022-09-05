# PhotoSort ![version](https://img.shields.io/badge/version-0.3.0-blue)
**An Android application that organizes images into albums and sorts them**

## Overview
This application is one I developed before Kotlin had taken off as the flagship language for Android development. It incorporates many aged development paradigms that were popular at the time such as using multiple Activities. This app was an early version of ideas such as "Locked folders" which each Android manufacturer had to implement for their DCIM galleries. It allows a user to move cumbersome images out of their main image store to this app's specific one that is not able to be scraped by apps that requires media content from the internal storage.

## Features
- Gridview for images
- ViewPager to swipe through images after selecting from grid
- Listview for albums
- Adapter implementations for all three views above
- Change names of albums and reorder them
- Randomize image order temporarily
- Moves images from DCIM to private hidden folders in the app's storage

## Disclaimer
This app requires an old build of Gradle and JVM to be built.

## License
MIT License. See `LICENSE.txt` for more information.
