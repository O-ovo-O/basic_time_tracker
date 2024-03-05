# Time tracking app

Reasons this exists, was to check how difficult would it be to generate this app using chatgpt prompt.
Basically prompt used was something like `prompt-used.md`

Mind you - prompt is far from great but then generated code (using probably older model)
was far from perfect too. Didn't work - Android Room could not initialize its database in runtime :)

Whole setup was also rather unpleasant, i find it would be even harder for people without coding skills.


I sadly did not saved in vcs the original prompt, i'd need to recover it later, so i might force push this main branch ;) to rewrite history, you've been warned ;)

intention would be to show the original prompt, and failure

- https://stackoverflow.com/questions/46665621/android-room-persistent-appdatabase-impl-does-not-exist
- as well as couple of kapt and similar build related issues

and then in next commit replacement of Android Room with native sqlite approach.

Alas, i was too lazy, so i just commented the stuff out; anyways - maybe'll fix, we'll see.
