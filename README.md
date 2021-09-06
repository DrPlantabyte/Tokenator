# Tokenator
Easily create virtual tabletop portrait tokens from images with the Tokenator!

![tokenator01](https://user-images.githubusercontent.com/1922739/132080450-23c7a8ce-aa67-41f2-a662-e70455f30531.png)

## Instructions
1. Open an image file (or copy-paste into the image area on the left)
2. Select your border style and color from the drop-down on the right
3. Click on the image where you want to make a portrait token, using ctrl+scroll or the slider at the bottom to adjust the size
4. Save your new token!

## Add Your Own Token Styles!
Place your own token border styles in the folder **C:\\Users\\\<username\>\\AppData\\Roaming\\Tokenator\\Token Borders** (or **~/.Tokenator/Token Borders**). Each style requires 3 files: **\<style name\>.mask.png**, **\<style name\>.overlay.png**, and **\<style name\>.color.png** (where \<style name\> is the name of your token border style). The mask file should be a solid shape on a transparent background (color does not matter), the overlay file contains the foreground of your token style (such as shading highlights), and the color file should be any color-changing elements in shades of red (red will be substituted for the user-specified color). Three examples are provided.
