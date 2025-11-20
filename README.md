Android TV launcher

Added all required intent-filters, permissions and dependencies those are also designed for mobile launcher apps

```
<category android:name="android.intent.category.HOME" />
<category android:name="android.intent.category.DEFAULT" />
<category android:name="android.intent.category.LEANBACK_LAUNCHER" />
```
Further requested to user to make the app as default launcher app though using RoleManager of System. This shows selector to user to choose default home app (Successfully working)

`Settings.ACTION_HOME_SETTINGS` does not work on TV devices, the system could not find target receiver to open this intent action. However it was working on Mobile devices. Having researched more on TV launcher, I found out that the system does not allow third-party apps. In addition, there are some kind of TV launcher apps on Play Market, but they can be exited by clickinh back button. Without system changes I could not find solution to gain default launcher access on TV devices. On mobile devices it is not a problem. `Checked on mobile, even this code is working as expected` 

![role home request](additional_files/role_home_request.png?raw=true)

Also tried to set priority for the app with `<intent-filter android:priority="3">` (no desired result).

Another option: Accessibility service to handle HOME button click (back button can be disabled from activity - easy)
Added all requirements for manifest, service, xml resource file
```
class TvAccessibilityService : AccessibilityService() {
    ...
}
```
When user enters to the APP it asks to give permission over Accessibility
![accessibility request](additional_files/accessibility_request.png?raw=true)

And app is ready to use, app shows the list of installed launchable apps. + saves last opened app to SharedPreferences.
![home screen](additional_files/home_screen.png?raw=true)

Result: 
- Back button can not be clicked at all (blocked by backPressedDispatcher)
- when home button is clicked, accessibility detects the event and opens app again. It seems a bit bad practice.
- opens app when OS is restarted

![Usage App](additional_files/recording.gif)

Link to APK file to install
![additional_files/android-tv-launcher.apk](additional_files/android-tv-launcher.apk)
