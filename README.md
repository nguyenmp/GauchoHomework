Setting Up Your Environment

1. You need AndroidStudios.  I am working with 0.8.9 Beta.
2. You need Git.  I am working with 1.9.1.
3. Configure your Android SDK to have API 19, the support library, and the support repository.
4. `git clone https://github.com/nguyenmp/GauchoHomework` to create a directory called GauchoHomework that will contain our source code.
5. `git submodule update --init --recursive` initializes all our submodule dependencies
6. `./gradlew assembleDebug` builds the application.  gradlew is a gradle wrapper that's packed with our repo so you don't need to install gradle on your local machine.  At the time of this writing, we used Gradle 1.12
