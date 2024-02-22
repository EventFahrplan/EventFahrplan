# Contribution guide

You are very welcome to contribute to this project.
In order to simplify the process please try to follow the following suggestions:

## How to start
* Use English for all communications to include as many people as possible.
* Create an issue to discuss your idea before you spend a lot of time into designing or coding.
* Leave an `I like to work on this` comment on an existing issue to let the maintainer know about it.
* Fork the repository into your personal GitHub account to get started.
* Create a separate branch for each topic.

## While contributing
* Make sure to apply [automatic code formatting and organizing imports][code-formatting] as it is provided by Android Studio.
* Write unit tests.
* Review and update the [README.md](README.md). Your changes may affect sections of the document.

## Git best practices
* Compose contextual atomic commits.
* Aim to answer the `Why?` question in every commit message.
* Make sure each commit compiles.
* Rebase your branch onto the latest commit on `master`.
* Do not merge `master` into your branch. The project maintainer will merge your branch into `master` as soon as it is approved.
* Feel free to reorganize the commits on your branch by using `git rebase --interactive` or `git push --force`.
* Use the issue and pull request templates provided for the GitHub repository.
* Describe the intent of your pull request. Include screenshots if appropriate. They help others to understand the before and after state.
* Be sure to include a textual description in both your commit(s) and in the pull request if you are implementing a new feature or changing the behavior of an existing one.

## Translations
* Contribute to the translation of the app into many languages. [Crowdin][crowdin-eventfahrplan] is used to organize all languages and texts.

[code-formatting]: http://stackoverflow.com/a/5581992/356895
[crowdin-eventfahrplan]: https://crowdin.com/project/eventfahrplan
