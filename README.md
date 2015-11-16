# CampFahrplan

CampFahrplan is a viewer for schedules of [Chaos Computer Club e.V. events][ccc-events]
such as Chaos Communication Camp or Chaos Communication Congress.

In addition to an daily overview of talks, the app lets you read the abstracts,
highlight talks, add a talk to your calendar, share talks with others, and
set reminders within the app.

[![Available for Android at Google Play](gfx/google-play-badge.png)][playstore]

Moreover, the app is designed to consume schedule data published in a specific
format as provided by [Pentabarf][pentabarf] or its successor [Frab][frab].
Therefore, the app can be re-deployed for other events using the same schedule
file format.


## Release build / signing

Before you can run a release build for your product flavor
please create a `gradle.properties` file in the `app` module.
A template file is provided which contains the required settings -
see [`app/gradle.properties.example`](app/gradle.properties.example).


## Contributing

Contributions can be served as contextual atomic branches which should
be rebased onto the current `HEAD` of the `master` branch. Please make
sure to apply [automatic code formatting and organize imports][code-formatting]
as it is provided by Android Studio and IntelliJ. To automate this a
*checkstyle* definition will be provided in the near future.


## Licenses

Portions Copyright 2008-2011 The K-9 Dog Walkers and 2006-2011 the Android Open Source Project.

LICENSE

```
Copyright 2011-2015 Daniel Dorau
Copyright 2015 SubOptimal, entropynil, johnjohndoe

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

[ccc-events]: http://events.ccc.de
[playstore]: https://play.google.com/store/apps/details?id=nerd.tuxmobil.fahrplan.congress
[pentabarf]: https://github.com/nevs/pentabarf
[frab]: https://github.com/frab/frab
[code-formatting]: http://stackoverflow.com/a/5581992/356895
