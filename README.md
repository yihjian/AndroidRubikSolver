# Android Rubik Solver

This is a android studio project that can develop a rubik cube solving algorithm based on the picture of the cube taken by the users. It's the final project for our [CS125](https://cs125.cs.illinois.edu/) class.

The app processes the image taken by the camera and develops the color pattern of each surface of the rubik cube. Then, the patterns is fed through the Kociemba algorithm to calculate a solution. The solution comes with the option of visual annimation.

# Project Status

We have finished the development of the app. We'll try to further improve the color recognition algorithms

---Potential future updates---
1. Implement different solving algorithms (CFOP etc.).
2. Implement different size cubes.
3. Use a k-nn classifier to increase color recognition accuracy.
  
# Acknowledgement
1. Initial idea was inspired by the [MagicCube project](https://github.com/iostyle/MagicCube), and we derive part of our UI from this design.
2. [min2phase](https://github.com/cs0x7f/min2phase) solving algorithm.

# LICENSE
GNU v3.0
