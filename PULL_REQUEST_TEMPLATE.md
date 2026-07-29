# Pull Request: fix missing ic_launcher resources

This PR adds missing launcher resources to fix the Android CI resource linking error where @mipmap/ic_launcher was not found.

Files added:
- app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
- app/src/main/res/drawable/ic_launcher_foreground.xml
- app/src/main/res/values/colors.xml

These are placeholder resources:
- ic_launcher_foreground.xml contains a simple vector placeholder (replace with your real icon if you have one).
- colors.xml defines ic_launcher_background color.

Reason: The CI failed with "resource mipmap/ic_launcher not found" during :processReleaseResources. Adding these resources restores the missing reference and should allow the build to proceed.

If you'd like I can replace the placeholder vector with your provided PNG/SVG assets and generate density-specific mipmap images.
