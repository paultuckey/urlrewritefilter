## General Tips for debugging UrlRewriteFilter ##

Because every java web project is slightly different, debugging can be difficult. Try to:
  * Turn on debug logging (see filter params in manual).
  * Simplify the problem as much as possible.  Perhaps make a new web application without other filters/servlets, try and replicate the problem there.
  * Use an HTTP debugger (such as fiddler) to check that what you think is happening really is.

If you think you have found a bug:
  * Read the source
  * Write a unit test that proves the bug, this reduce the amount of time it will take to fix the bug.
  * See [issues list](http://code.google.com/p/urlrewritefilter/issues/list) and add a new issue if necessary
