# Project Goal

The primary goal was to create a Java Android application with the following functionalities:

1.  **Main Activity:** A simple splash screen with a single button ("Launch camera") that navigates to the Camera Activity and finishes itself.
2.  **Camera Activity - Live Preview:** Accesses the device's hardware camera (rear by default) and displays a live preview within the activity using CameraX.
3.  **Photo Capture:** A "Capture" button to take a photo, which is then saved to the application's private external storage.
4.  **Recent Capture Thumbnail:** An `ImageView` that displays a thumbnail of the most recently captured photo. This updates instantly after a new photo is taken and loads the last photo upon app startup.
5.  **Camera Flip:** A "Flip" button to seamlessly switch between the rear and front cameras.

**Package Name:** `com.project.cameraapp`

## My Contribution & Learning Journey :

*   **Understand Project Setup:** Grasp the basics of creating an Android Studio project, setting up activities, and managing layouts.
*   **Permissions Management:** Learned the critical process of declaring and requesting runtime permissions (Camera, Storage) for modern Android versions.
*   **CameraX Mastery:** Gained hands-on experience with the CameraX Jetpack library, simplifying camera app development by learning about `PreviewView`, `CameraSelector`, `ImageCapture`, `ProcessCameraProvider`, and lifecycle binding. This was a core and challenging part!
*   **File Management:** Understood how to save image files to app-specific directories and efficiently load/downsample them for thumbnail display to prevent `OutOfMemoryError`.
*   **User Interaction:** Implemented `OnClickListener`s for buttons and managed basic UI updates.
*   **Debugging & Problem Solving:** Faced and resolved issues like dependency conflicts (e.g., `PreviewView` not found initially), which taught me the importance of correct dependency management in `build.gradle` and `libs.versions.toml`.
*   **Code Organization:** Practiced structuring Java code within an Android activity, breaking down complex tasks into smaller, manageable methods.

## Acknowledgement: Assistance from Gemini

I would like to acknowledge the invaluable assistance received from **Gemini**, an AI assistant, throughout this project. Gemini helped me by:

*   Guiding me through crucial dependency setup when issues arose, such as identifying and fixing the missing CameraX dependencies for `PreviewView`.

Gemini's guidance was instrumental in breaking down the project into manageable parts, providing clear instructions, and helping me navigate common development hurdles.

## Resources Researched

*   **Android Developers Documentation:**
    *   Creating a New Project
    *   `Intent` and `startActivity()`
    *   `Activity` Lifecycle (`finish()`)
    *   Request App Permissions (runtime permissions)
    *   `AndroidManifest.xml` (`<uses-permission>`, `<uses-feature>`)
    *   CameraX Overview
    *   CameraX: Implement a preview (`PreviewView`, `Preview` use case)
    *   CameraX: Take a photo (`ImageCapture` use case, `OnImageSavedCallback`)
    *   CameraX: Select a camera (`CameraSelector`, `LENS_FACING_BACK`, `LENS_FACING_FRONT`)
    *   CameraX: Lifecycle Management (`unbindAll()`, `bindToLifecycle()`)
    *   `ImageView`
    *   Loading Large Bitmaps Efficiently (`BitmapFactory.Options`, `inSampleSize`)
    *   User Feedback (`Toast` messages)
    *   Logcat for debugging
    *   Accessibility (`android:contentDescription`)
    *   
*   **Google's Maven Repository:** For checking the latest stable versions of CameraX and other AndroidX libraries.
*   **Coding Reel & Medium Tutorials:** Various online tutorials for step-by-step CameraX integration and common Android development patterns.
*   **GeeksforGeeks Articles:** Explained various Android concepts and provided code examples for tasks like `ImageCapture`.
*   **Stack Overflow:** Consulted for specific programming challenges, such as listing files in a directory, sorting by modification date, and efficient bitmap handling.
