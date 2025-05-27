This project is a part of a Master Thesis in Interaction Technology and Design. The application is the frontend for viewing and translating NIfTI files (medical images) from one domain to another (CT to PET), done on a backend server (HTTP). The application works as a standalone without the server if one dont want to translate files.

Server is located in https://github.com/FraggeGaming/ThesisInferenceServer/tree/main

This is a Kotlin Multiplatform project targeting Desktop (Kotlin 2.1.0) (Compose 1.8.1) (Gradle 8.9) (Java 17).

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `desktopMain` is for code that’s for the desktop and where the whole project is structured for.
  With two main screens in the subfolder `Screens`
  - `UploadData` is the first entrypoint of the applicaiton, where the user uploads the prefered NIfTI and chooses a translation model
  - `MainView` is for viewing the images. All input and generated images can be exported in the main view to a ZIP to view the translated files in a prefered external tool
  
Certain folders contain executable python script to parse the NIfTI into

* `external` has three executable scripts, one for Windows, one for macOS, and one for Linux and use the Nibabel library to parse NIfTI files into NumPy arrays.
  - When the model has been running, three folders, and two files will appear, `input_gz`, `output_gz`, `output_npy`, and the files `config.properties` and `SavedMappings.txt`
  - `input_gz` Has the user uploaded NIfTI files.
  -  `output_gz` has the AI translated NIfTI files.
  -  `output_npy` has the parsed NumPy from the input and output NIfTI files.
  -  `config.properties` is where the serverIP is located and can be changed.
  -  `SavedMappings.txt` has the information required to store previously translated or uploaded files for easy tracking. When files gets deleted in the UI, they also get deleted in the input/output directories, aswell as in the `SavedMappings.txt`.

To run the project in intellij:
- Go to Settings - Build, Execution, Deployment - Build Tools - Gradle: and set Gradle JVM to Java 17
- Make sure that the project also has an compatible SDK. Go to Settings - Project Structure - Project: Set SDK and Language Level to >= 17.
- When starting the application for the first time, the executable (`external`) scripts might be flagged by the OS. In order to use the application, allow the scripts to run if flagged.

* To run the image-to-image translation the app must be running aswell as the server.
- Go to the (`external`) folder - Locate (`config.properties`) - If there is no such file, start the application one time for it to appear. There exists an server IP which one can change to the disired path.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…


## License

This project is licensed under the GNU General Public License v3.0 (GPLv3).  
You are free to use, modify, and distribute this software under the same license terms.  
See the [LICENSE](LICENSE) file for full license text and conditions.

© 2025 [Fardis Nazemroaya Sedeh]