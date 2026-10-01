# Calender (Android — Java)

Offline Ethiopian ⇄ Gregorian date converter. Screens use match_parent + weight so they adapt phones, tablets, foldables, Tizen-class large panels.

[Download APK](https://github.com/alazar80/Calender/raw/main/Calender.apk)

## Features
- Ethiopian ↔ Gregorian
- Today in Africa/Addis_Ababa
- Dark theme / Amharic strings
- Responsive DrawerLayout + ConstraintLayout

## Data partitions (catalog)
See `docs/partitioned_data_exampless.txt`.
- P0 input: user dates, locale, theme
- P1 process: JDN conversion
- P2 output: calendar UI, APK artifacts
- P3 metadata: Gradle, manifests, languages

## Languages
Java (Android). Catalog: all_languages.txt in fitness-medical.

## Quick start
```bash
git clone https://github.com/alazar80/Calender.git
cd Calender
```
Open in Android Studio. Build APK.
