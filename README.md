Setting Up Your Environment
===========================

1. You need Git.  I am working with 1.9.1.
2. Configure your Android SDK to have API 19, the support library, and the support repository.
3. `git clone https://github.com/nguyenmp/GauchoHomework` to create a directory called GauchoHomework that will contain our source code.
4. `git submodule update --init --recursive` initializes all our submodule dependencies
5. `./gradlew assembleDebug` builds the application.  gradlew is a gradle wrapper that's packed with our repo so you don't need to install gradle on your local machine.  At the time of this writing, we used Gradle 1.12
6. If you want to work with Android Studios, import the project using the build.gradle file in the top level directory of this repo.  I am working with 0.8.9 Beta.  If you're using AS' SDK then you need to configure it like you did previously.

Errors
======

1. Submodules haven't been checked out yet.  When one of the submodules doesn't have any code or build script in it, the following error is thrown:

````
$ gradle assembleDebug

FAILURE: Build failed with an exception.

* What went wrong:
A problem occurred configuring project ':app'.
> Configuration with name 'default' not found.

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output.

BUILD FAILED

Total time: 4.681 secs
````

To fix this issue, simply run `git submodule update --init --recursive` in the root directory of the project.  This will check out the proper versions of our submodule GauchoSpace, the Java library for interfacing with the web app.
