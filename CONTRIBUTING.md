# Contribution guide

You are very welcome to contribute to this project.
In order to simplify the process please try to follow the following suggestions:

## How to start
* Use English for all communication to include as many people as possible.
* Create an issue to discuss your idea before you invest a lot of time into designing or coding.
* Leave an `I like to work on this` comment on an existing issue to let the maintainer know about it.
* Fork the repository into your personal GitHub account to start working.
* Create a separate branch for each topic.

## While contributing
* Make sure to apply [automatic code formatting and organize imports][code-formatting] as it is provided by Android Studio.
* Write unit tests.
* Check and update the [README.md](README.md). Your changes might affect sections in the document.

## Git best practices
* Compose contextual atomic commits.
* Aim to answer the question `Why?` in every commit message.
* Make sure each commit compiles.
* Rebase your branch onto the latest commit on `master`.
* Do not merge `master` into your branch. The project maintainer will merge your branch into `master` once it is approved.
* Feel free to reorganize the commits on your branch by using `git rebase --interactive` or `git push --force`.
* Use the issue and pull request templates prepared for the GitHub repository.
* Describe the intention of your pull request. Add screenshots if suitable. They help others to understand the before and after state.
* Make sure to add a textual description both in your commit(s) and in the pull request if you implement a new feature or you change the behavior of an existing one.

## Translations
* Contribute to translate the app into many languages. [Crowdin][crowdin-eventfahrplan] is used to organize all languages and texts.

[code-formatting]: http://stackoverflow.com/a/5581992/356895
[crowdin-eventfahrplan]: https://crowdin.com/project/eventfahrplan
